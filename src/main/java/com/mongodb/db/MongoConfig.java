package com.mongodb.db;

import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.*;
import static org.springframework.data.mongodb.core.schema.QueryCharacteristics.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoJsonSchemaCreator;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateEncryptedCollectionParams;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.mongodb.domain.Employee;
import com.mongodb.domain.LocalCMKService;

@Configuration
public class MongoConfig implements ApplicationRunner {

    @Value("${app.mongodb.cryptSharedLibPath}")
    private String cryptSharedLibPath;

    @Value("${app.mongodb.encryptedCollectionName}")
    private String encryptedCollectionName;

    @Value("${app.mongodb.encryptedDatabaseName}")
    private String encryptedDatabaseName;

    @Value("${app.mongodb.keyVaultNamespace}")
    private String keyVaultNamespace;

    @Value("${app.mongodb.uri}")
    private String uri;

    private Map<String, Map<String, Object>> kmsProviderCredentials;
    private final LocalCMKService localCMKService;

    MongoConfig(LocalCMKService localCMKService) {
        this.localCMKService = localCMKService;
    }

    @Bean
    public MongoClient mongoClient() throws IOException {
        return MongoClients.create(getMongoClientSettings());
    }

    @Bean
    MongoOperations mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, encryptedDatabaseName);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var mongoTemplate = mongoTemplate(mongoClient());

        if (mongoTemplate.collectionExists(encryptedCollectionName)) {
            return;
        }

        ClientEncryptionSettings encryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                        .applyConnectionString(new com.mongodb.ConnectionString(uri))
                        .build())
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(kmsProviderCredentials)
                .build();

        try (ClientEncryption clientEncryption = ClientEncryptions.create(encryptionSettings)) {
//            manualCollectionSetup(clientEncryption, mongoTemplate);
            derivedCollection(mongoTemplate, clientEncryption);
        }
    }

    private void derivedCollection(MongoOperations template, ClientEncryption clientEncryption) {

        MongoJsonSchema employeeSchema = MongoJsonSchemaCreator.create(new MongoMappingContext())
                .filter(MongoJsonSchemaCreator.encryptedOnly())
                .createSchemaFor(Employee.class);

        Document encryptedFields = CollectionOptions.encryptedCollection(employeeSchema)
                .getEncryptedFieldsOptions()
                .map(CollectionOptions.EncryptedFieldsOptions::toDocument)
                .orElseThrow();

        template.execute(db -> clientEncryption.createEncryptedCollection(db, template.getCollectionName(Employee.class), new CreateCollectionOptions()
				.encryptedFields(encryptedFields), new CreateEncryptedCollectionParams("local")));
    }

    private void manualCollectionSetup(ClientEncryption clientEncryption, MongoOperations mongoTemplate) {
        BsonBinary dkSalary = clientEncryption.createDataKey("local", new com.mongodb.client.model.vault.DataKeyOptions());

        CollectionOptions collectionOptions = CollectionOptions.encryptedCollection(options -> options
                .queryable(encrypted(float64("salary")).algorithm("Range").keyId(dkSalary.asUuid()), range().contention(0).precision(2).min(0.0).max(9999999.0))
        );

        mongoTemplate.createCollection(Employee.class, collectionOptions);
    }

    private MongoClientSettings getMongoClientSettings() throws IOException {
        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .autoEncryptionSettings(getAutoEncryptionSettings())
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
    }

    private AutoEncryptionSettings getAutoEncryptionSettings() throws IOException {
        kmsProviderCredentials = localCMKService.getKmsProviderCredentials();

        return AutoEncryptionSettings.builder()
                .keyVaultNamespace(keyVaultNamespace)
                .extraOptions(createExtraOptions())
                .kmsProviders(kmsProviderCredentials)
                .build();
    }

    private Map<String, Object> createExtraOptions() {
        Map<String, Object> extraOptions = new HashMap<>();
        extraOptions.put("cryptSharedLibPath", cryptSharedLibPath);
        return extraOptions;
    }
}
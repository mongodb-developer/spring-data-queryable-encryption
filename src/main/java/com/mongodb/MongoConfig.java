package com.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateEncryptedCollectionParams;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import org.bson.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.*;
import static org.springframework.data.mongodb.core.schema.QueryCharacteristics.equality;
import static org.springframework.data.mongodb.core.schema.QueryCharacteristics.range;

@Configuration
public class MongoConfig implements ApplicationRunner {

    @Value("${app.mongodb.encryptedCollectionName}")
    private String encryptedCollectionName;

    @Value("${app.mongodb.encryptedDatabaseName}")
    private String encryptedDatabaseName;

    @Value("${app.mongodb.cryptSharedLibPath}")
    private String cryptSharedLibPath;

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
        if (! mongoTemplate.collectionExists(encryptedCollectionName)) {

            ClientEncryptionSettings clientEncryptionSettings = ClientEncryptionSettings.builder()
                .keyVaultMongoClientSettings(MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(uri))
                        .build())
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(kmsProviderCredentials)
                .build();

            ClientEncryption clientEncryption = ClientEncryptions.create(clientEncryptionSettings);
            CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions().encryptedFields(getEncryptedFields());

            try {

                clientEncryption.createEncryptedCollection(
                        mongoClient().getDatabase(encryptedDatabaseName),
                        encryptedCollectionName,
                        createCollectionOptions,
                        new CreateEncryptedCollectionParams("local").masterKey(new BsonDocument()));
            }

            catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        }
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

        Map<String, Object> extraOptions = new HashMap<>();
        extraOptions.put("cryptSharedLibPath", cryptSharedLibPath);

        return AutoEncryptionSettings.builder()
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(kmsProviderCredentials)
                .extraOptions(extraOptions)
                .build();
    }
}

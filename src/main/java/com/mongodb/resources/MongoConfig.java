package com.mongodb.resources;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import com.mongodb.domain.Employee;
import com.mongodb.domain.LocalCMKService;
import org.bson.BsonBinary;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.*;
import static org.springframework.data.mongodb.core.schema.QueryCharacteristics.equality;
import static org.springframework.data.mongodb.core.schema.QueryCharacteristics.range;

@Configuration
public class MongoConfig implements ApplicationRunner {

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


//        mongoClient().getDatabase("encryption").drop();
//        mongoClient().getDatabase("hrsystem").drop();

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
            BsonBinary dataKeyId = clientEncryption.createDataKey("local", new com.mongodb.client.model.vault.DataKeyOptions().keyAltNames(List.of("name")));
            BsonBinary dataKeyId2 = clientEncryption.createDataKey("local", new com.mongodb.client.model.vault.DataKeyOptions().keyAltNames(List.of("age")));

            CollectionOptions collectionOptions = CollectionOptions.encryptedCollection(options -> options
                    .queryable(encrypted(string("name")).algorithm("Indexed").keyId(dataKeyId.asUuid()), equality().contention(0))
                    .queryable(encrypted(int32("age")).algorithm("Range").keyId(dataKeyId2.asUuid()), range().contention(0).min(0).max(130))
            );

            mongoTemplate.createCollection(Employee.class, collectionOptions);
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

        return AutoEncryptionSettings.builder()
                .keyVaultNamespace(keyVaultNamespace)
                .kmsProviders(kmsProviderCredentials)
                .build();
    }
}

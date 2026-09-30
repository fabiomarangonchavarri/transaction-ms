package com.nttdata.config;

import com.azure.cosmos.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CosmosConfig {

    @ConfigProperty(name = "cosmos.endpoint")
    String endpoint;

    @ConfigProperty(name = "cosmos.key")
    String key;

    @Produces
    @ApplicationScoped
    public CosmosAsyncClient cosmosClient() {

        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .buildAsyncClient();
    }

    @Produces
    @ApplicationScoped
    public CosmosAsyncDatabase cosmosDatabase(
            CosmosAsyncClient client) {

        return client.getDatabase("Bank");
    }
}

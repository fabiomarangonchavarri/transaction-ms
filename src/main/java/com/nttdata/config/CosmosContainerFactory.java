package com.nttdata.config;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CosmosContainerFactory {

    private final CosmosAsyncDatabase database;

    public CosmosContainerFactory(
            CosmosAsyncDatabase database) {

        this.database = database;
    }

    public CosmosAsyncContainer getContainer(String name) {
        return database.getContainer(name);
    }
}

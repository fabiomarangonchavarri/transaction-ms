package com.nttdata.repository;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.nttdata.config.CosmosContainerFactory;
import com.nttdata.model.PurchaseDto;
import com.nttdata.model.PurchaseResponseDto;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PurchaseRepository {

    private final CosmosAsyncContainer container;

    public PurchaseRepository(CosmosContainerFactory factory,
                             @ConfigProperty(name = "cosmos.containers.purchases") String containerName) {
        this.container = factory.getContainer(containerName);
    }

    public Uni<PurchaseDto> create(PurchaseDto purchase) {

        return Uni.createFrom()
                .completionStage(
                        container
                                .createItem(purchase)
                                .toFuture()
                )
                .chain(response ->
                        Uni.createFrom()
                                .completionStage(
                                        container
                                                .readItem(
                                                        purchase.id(),
                                                        new PartitionKey(purchase.cardId()),
                                                        PurchaseDto.class
                                                )
                                                .toFuture()
                                )
                )
                .map(CosmosItemResponse::getItem);
    }

    public Uni<List<PurchaseResponseDto>> getPurchasesByCardId(String cardId) {

        String query = String.format("SELECT c.id, c.amount, c.currency, c.description, c.timestamp FROM c WHERE c.cardId = '%s' ORDER BY c.timestamp DESC", cardId);

        return Uni.createFrom()
                .completionStage(
                        container.queryItems(query, PurchaseResponseDto.class)
                                .collectList()
                                .toFuture()
                );
    }


}

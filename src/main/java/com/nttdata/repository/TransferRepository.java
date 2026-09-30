package com.nttdata.repository;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.nttdata.config.CosmosContainerFactory;
import com.nttdata.model.PurchaseDto;
import com.nttdata.model.PurchaseResponseDto;
import com.nttdata.model.TransferDto;
import com.nttdata.model.TransferResponseDto;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped
public class TransferRepository {

    private final CosmosAsyncContainer container;

    public TransferRepository(CosmosContainerFactory factory,
                              @ConfigProperty(name = "cosmos.containers.transfers") String containerName) {
        this.container = factory.getContainer(containerName);
    }

    public Uni<TransferDto> create(TransferDto transfer) {

        return Uni.createFrom()
                .completionStage(
                        container
                                .createItem(transfer)
                                .toFuture()
                )
                .chain(response ->
                        Uni.createFrom()
                                .completionStage(
                                        container
                                                .readItem(
                                                        transfer.id(),
                                                        new PartitionKey(transfer.sourceAccountId()),
                                                        TransferDto.class
                                                )
                                                .toFuture()
                                )
                )
                .map(CosmosItemResponse::getItem);
    }

    public Uni<List<TransferDto>> getTransfersByAccountId(String accountId) {

        String query = String.format("SELECT c.id, c.sourceAccountId, c.destinationAccountId, c.amount, c.currency, c.amountReceived, c.destinationAccountCurrency, c.description, c.timestamp FROM c WHERE c.sourceAccountId = '%s' OR c.destinationAccountId = '%s' ORDER BY c.timestamp DESC", accountId, accountId);

        return Uni.createFrom()
                .completionStage(
                        container.queryItems(query, TransferDto.class)
                                .collectList()
                                .toFuture()
                );
    }

}

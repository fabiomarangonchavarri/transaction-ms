package com.nttdata.repository;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.nttdata.config.CosmosContainerFactory;
import com.nttdata.model.CardDto;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.smartcardio.Card;
import java.util.Optional;

@ApplicationScoped
public class CardRepository {

    private final CosmosAsyncContainer container;

    public CardRepository(CosmosContainerFactory factory,
                          @ConfigProperty(name = "cosmos.containers.cards") String containerName){
        this.container = factory.getContainer(containerName);
    }

    public Uni<Optional<CardDto>> getCardByDocumentNumberAndCardNumber(String documentNumber, String cardNumber) {
        String query = String.format("SELECT c.id, c.customerDocumentNumber, c.cardNumber, c.totalBalance, c.usedBalancePEN, c.usedBalanceUSD FROM c WHERE c.customerDocumentNumber = '%s' AND c.cardNumber = '%s'", documentNumber, cardNumber);

        return Uni.createFrom()
                .completionStage(
                        container.queryItems(query, CardDto.class)
                                .collectList()
                                .toFuture()
                                .thenApply(list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)))
                );
    }

    public Uni<CardDto> update(CardDto card) {
        return Uni.createFrom()
                .completionStage(
                        container
                                .upsertItem(card)
                                .toFuture()
                )
                .chain(() ->
                        Uni.createFrom()
                                .completionStage(
                                        container
                                                .readItem(
                                                        card.id(),
                                                        new PartitionKey(card.customerDocumentNumber()),
                                                        CardDto.class
                                                )
                                                .toFuture()
                                )
                )
                .map(CosmosItemResponse::getItem);
    }

}

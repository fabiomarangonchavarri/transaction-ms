package com.nttdata.repository;

import com.azure.cosmos.CosmosAsyncContainer;
import com.nttdata.config.CosmosContainerFactory;
import com.nttdata.model.ExchangeRateDto;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ApplicationScoped
public class ExchangeRateRepository {

    private final CosmosAsyncContainer container;

    public ExchangeRateRepository(CosmosContainerFactory factory,
                                  @ConfigProperty(name = "cosmos.containers.exchangeRates") String containerName) {
        this.container = factory.getContainer(containerName);
    }

    public Uni<Optional<ExchangeRateDto>> getLastExchangeRate() {

        String query = "SELECT TOP 1 c.pair, c.buyRate, c.sellRate FROM c WHERE c.pair = 'USD-PEN' ORDER BY c.timestamp DESC";

        return Uni.createFrom()
                .completionStage(
                        container.queryItems(query, ExchangeRateDto.class)
                                .collectList()
                                .toFuture()
                                .thenApply(list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)))
                );
    }
}

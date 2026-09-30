package com.nttdata.repository;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.nttdata.config.CosmosContainerFactory;
import com.nttdata.model.AccountDto;
import com.nttdata.model.CardDto;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ApplicationScoped
public class AccountRepository {

    private final CosmosAsyncContainer container;

    public AccountRepository(CosmosContainerFactory factory,
                          @ConfigProperty(name = "cosmos.containers.accounts") String containerName){
        this.container = factory.getContainer(containerName);
    }

    public Uni<Optional<AccountDto>> getAccountByDocumentNumberAndAccountNumber(String documentNumber, String accountNumber) {
        String query = String.format("SELECT c.id, c.customerDocumentNumber, c.accountNumber, c.accountType, c.currency, c.balance, c.status, c.lastUpdated FROM c WHERE c.customerDocumentNumber = '%s' AND c.accountNumber = '%s' AND c.status = 'A'", documentNumber, accountNumber);

        return Uni.createFrom()
                .completionStage(
                        container.queryItems(query, AccountDto.class)
                                .collectList()
                                .toFuture()
                                .thenApply(list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)))
                );
    }

    public Uni<Optional<AccountDto>> getAccountByAccountNumber(String accountNumber) {
        String query = String.format("SELECT c.id, c.customerDocumentNumber, c.accountNumber, c.accountType, c.currency, c.balance, c.status, c.lastUpdated FROM c WHERE c.accountNumber = '%s' AND c.status = 'A'", accountNumber);

        return Uni.createFrom()
                .completionStage(
                        container.queryItems(query, AccountDto.class)
                                .collectList()
                                .toFuture()
                                .thenApply(list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)))
                );
    }

    public Uni<Optional<AccountDto>> getAccountById(String id) {
        String query = String.format("SELECT c.id, c.customerDocumentNumber, c.accountNumber, c.accountType, c.currency, c.balance, c.status, c.lastUpdated FROM c WHERE c.id = '%s' AND c.status = 'A'", id);

        return Uni.createFrom()
                .completionStage(
                        container.queryItems(query, AccountDto.class)
                                .collectList()
                                .toFuture()
                                .thenApply(list -> list.isEmpty() ? Optional.empty() : Optional.of(list.get(0)))
                );
    }

    public Uni<AccountDto> update(AccountDto account) {
        return Uni.createFrom()
                .completionStage(
                        container
                                .upsertItem(account)
                                .toFuture()
                )
                .chain(() ->
                        Uni.createFrom()
                                .completionStage(
                                        container
                                                .readItem(
                                                        account.id(),
                                                        new PartitionKey(account.customerDocumentNumber()),
                                                        AccountDto.class
                                                )
                                                .toFuture()
                                )
                )
                .map(CosmosItemResponse::getItem);
    }

}

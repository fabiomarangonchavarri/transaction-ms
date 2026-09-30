package com.nttdata.service;

import com.nttdata.model.*;
import com.nttdata.repository.*;
import com.nttdata.util.Functions;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public TransferService(TransferRepository transferRepository, AccountRepository accountRepository, ExchangeRateRepository exchangeRateRepository) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public Uni<TransferDto> create(TransferRequestDto transferRequestDto) {

        return accountRepository.getAccountByDocumentNumberAndAccountNumber(transferRequestDto.customerDocumentNumber(), transferRequestDto.sourceAccountNumber())
                .onItem().transformToUni(sourceAccountOpt -> {
                    if (sourceAccountOpt.isEmpty()) {
                        return Uni.createFrom().failure(new RuntimeException("Not valid account"));
                    }

                    if (!sourceAccountOpt.get().currency().equals(transferRequestDto.currency())) {
                        return Uni.createFrom().failure(new RuntimeException("Currency must be the same as source account"));
                    }

                    return accountRepository.getAccountByAccountNumber(transferRequestDto.destinationAccountNumber())
                            .onItem().transformToUni(destinationAccountOpt -> {
                                if(destinationAccountOpt.isEmpty()) {
                                    return Uni.createFrom().failure(new RuntimeException("Destination account does not exist"));
                                }

                                return exchangeRateRepository.getLastExchangeRate()
                                        .onItem().transformToUni(exchangeRateOpt -> {
                                            Double amountToTransfer;

                                            if (!sourceAccountOpt.get().currency().equals(destinationAccountOpt.get().currency())) {
                                                if (exchangeRateOpt.isPresent()) {
                                                    if(sourceAccountOpt.get().currency().equals("PEN")) {
                                                        amountToTransfer = transferRequestDto.amount() / exchangeRateOpt.get().sellRate();
                                                    } else {
                                                        amountToTransfer = transferRequestDto.amount() * exchangeRateOpt.get().buyRate();
                                                    }
                                                } else {
                                                    return Uni.createFrom().failure(new RuntimeException("Exchange rate not available"));
                                                }
                                            } else {
                                                amountToTransfer = transferRequestDto.amount();
                                            }

                                            if (sourceAccountOpt.get().balance() < transferRequestDto.amount() && sourceAccountOpt.get().accountType().equals("SAVINGS")) {
                                                return Uni.createFrom().failure(new RuntimeException("Insufficient balance"));
                                            }

                                            AccountDto newSourceAccount = new AccountDto(
                                                    sourceAccountOpt.get().id(),
                                                    sourceAccountOpt.get().customerDocumentNumber(),
                                                    sourceAccountOpt.get().accountNumber(),
                                                    sourceAccountOpt.get().accountType(),
                                                    sourceAccountOpt.get().currency(),
                                                    sourceAccountOpt.get().balance() - transferRequestDto.amount(),
                                                    sourceAccountOpt.get().status(),
                                                    OffsetDateTime.now()
                                            );

                                            accountRepository.update(newSourceAccount);

                                            AccountDto newDestinationAccount = new AccountDto(
                                                    destinationAccountOpt.get().id(),
                                                    destinationAccountOpt.get().customerDocumentNumber(),
                                                    destinationAccountOpt.get().accountNumber(),
                                                    destinationAccountOpt.get().accountType(),
                                                    destinationAccountOpt.get().currency(),
                                                    destinationAccountOpt.get().balance() + Functions.roundToTwoDecimalPlaces(amountToTransfer),
                                                    destinationAccountOpt.get().status(),
                                                    OffsetDateTime.now()
                                            );

                                            accountRepository.update(newDestinationAccount);

                                            TransferDto transferDto = new TransferDto(
                                                    UUID.randomUUID().toString().toUpperCase(),
                                                    sourceAccountOpt.get().id(),
                                                    destinationAccountOpt.get().id(),
                                                    transferRequestDto.amount(),
                                                    transferRequestDto.currency(),
                                                    Functions.roundToTwoDecimalPlaces(amountToTransfer),
                                                    destinationAccountOpt.get().currency(),
                                                    transferRequestDto.description(),
                                                    OffsetDateTime.now()
                                            );

                                            return transferRepository.create(transferDto);
                                        });

                            });


                });


    }

    public Uni<List<TransferResponseDto>> getTransfersByAccountId(String accountId) {
        return transferRepository.getTransfersByAccountId(accountId)
                .onItem().transformToMulti(transfers -> Multi.createFrom().iterable(transfers))
                .onItem().transformToUniAndMerge(transfer -> {

                    String accountIdToSearch =
                            transfer.sourceAccountId().equals(accountId)
                                    ? transfer.destinationAccountId()
                                    : transfer.sourceAccountId();

                    return accountRepository.getAccountById(accountIdToSearch)
                            .onItem().transform(accountOpt -> {

                                if (accountOpt.isEmpty()) {
                                    return null;
                                }

                                var account = accountOpt.get();

                                return new TransferResponseDto(
                                    transfer.id(),
                                    account.accountNumber(),
                                    transfer.sourceAccountId().equals(accountId) ? -1 * transfer.amount() : transfer.amountReceived(),
                                    transfer.sourceAccountId().equals(accountId) ? transfer.currency() : transfer.destinationAccountCurrency(),
                                    transfer.description(),
                                    transfer.timestamp()
                                );
                            });
                })
                .collect().asList();
    }


}

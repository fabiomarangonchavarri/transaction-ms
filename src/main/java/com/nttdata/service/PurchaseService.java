package com.nttdata.service;

import com.nttdata.model.*;
import com.nttdata.repository.CardRepository;
import com.nttdata.repository.ExchangeRateRepository;
import com.nttdata.repository.PurchaseRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final CardRepository cardRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public PurchaseService(PurchaseRepository purchaseRepository, CardRepository cardRepository, ExchangeRateRepository exchangeRateRepository) {
        this.purchaseRepository = purchaseRepository;
        this.cardRepository = cardRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public Uni<PurchaseDto> create(PurchaseRequestDto purchaseRequestDto) {

        return cardRepository.getCardByDocumentNumberAndCardNumber(purchaseRequestDto.customerDocumentNumber(), purchaseRequestDto.cardNumber())
                .onItem().transformToUni(cardOpt -> {
                    if (cardOpt.isEmpty()) {
                        return Uni.createFrom().failure(new RuntimeException("Not valid card"));
                    }

                    return exchangeRateRepository.getLastExchangeRate()
                            .onItem().transformToUni(exchangeRateOpt -> {
                                Double amountInPEN;

                                if (purchaseRequestDto.currency().equalsIgnoreCase("USD")) {
                                    if (exchangeRateOpt.isPresent()) {
                                        amountInPEN = purchaseRequestDto.amount() * exchangeRateOpt.get().sellRate();
                                    } else {
                                        return Uni.createFrom().failure(new RuntimeException("Exchange rate not available"));
                                    }
                                } else {
                                    amountInPEN = purchaseRequestDto.amount();
                                }

                                if (cardOpt.get().totalBalance() - cardOpt.get().usedBalancePEN() - cardOpt.get().usedBalanceUSD() * (exchangeRateOpt.map(ExchangeRateDto::sellRate).orElse(1.0)) < amountInPEN) {
                                    return Uni.createFrom().failure(new RuntimeException("Insufficient balance"));
                                }

                                CardDto newCard = new CardDto(
                                        cardOpt.get().id(),
                                        cardOpt.get().customerDocumentNumber(),
                                        cardOpt.get().cardNumber(),
                                        cardOpt.get().totalBalance(),
                                        cardOpt.get().usedBalancePEN() + (purchaseRequestDto.currency().equals("PEN") ? purchaseRequestDto.amount() : 0),
                                        cardOpt.get().usedBalanceUSD() + (purchaseRequestDto.currency().equals("USD") ? purchaseRequestDto.amount() : 0)
                                );

                                cardRepository.update(newCard);

                                PurchaseDto purchaseDto = new PurchaseDto(
                                        UUID.randomUUID().toString().toUpperCase(),
                                        cardOpt.get().id(),
                                        purchaseRequestDto.description(),
                                        purchaseRequestDto.amount(),
                                        purchaseRequestDto.currency(),
                                        OffsetDateTime.now()
                                );

                                return purchaseRepository.create(purchaseDto);
                            });
                });


    }

    public Uni<List<PurchaseResponseDto>> getPurchasesByCardId(String cardId) {
        return this.purchaseRepository.getPurchasesByCardId(cardId);
    }

}

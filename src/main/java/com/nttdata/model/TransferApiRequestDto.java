package com.nttdata.model;

public record TransferApiRequestDto(
        String sourceAccountNumber,
        String destinationAccountNumber,
        Double amount,
        String currency,
        String description
) {
}

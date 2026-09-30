package com.nttdata.model;

import java.time.OffsetDateTime;

public record TransferRequestDto(
        String customerDocumentNumber,
        String sourceAccountNumber,
        String destinationAccountNumber,
        Double amount,
        String currency,
        String description) {
}

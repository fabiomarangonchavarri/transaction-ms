package com.nttdata.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record TransferDto(
        String id,
        String sourceAccountId,
        String destinationAccountId,
        Double amount,
        String currency,
        Double amountReceived,
        String destinationAccountCurrency,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime timestamp
) {
}

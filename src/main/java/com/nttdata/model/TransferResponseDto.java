package com.nttdata.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record TransferResponseDto(
        String id,
        String accountNumber,
        Double amount,
        String currency,
        String description,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime timestamp) {
}

package com.nttdata.controller;

import com.nttdata.model.*;
import com.nttdata.service.TransferService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/transfers")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class TransferResource {

    @Inject
    JsonWebToken jwt;

    private final TransferService transferService;

    public TransferResource(TransferService transferService) {
        this.transferService = transferService;
    }

    @POST
    public Uni<Response> create(TransferApiRequestDto request) {

        TransferRequestDto transferRequestDto = new TransferRequestDto(
                this.jwt.getSubject(),
                request.sourceAccountNumber(),
                request.destinationAccountNumber(),
                request.amount(),
                request.currency(),
                request.description()
        );

        return transferService.create(transferRequestDto)
                .onItem().transform(transfer ->
                        Response.status(Response.Status.CREATED)
                                .entity(transfer)
                                .build()
                )
                .onFailure(IllegalArgumentException.class)
                .recoverWithItem(exception ->
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity(exception.getMessage())
                                .build()
                )
                .onFailure()
                .recoverWithItem(exception ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity("Error interno al crear la transferencia")
                                .build()
                );
    }

    @GET
    @Path("/{accountId}")
    public Uni<List<TransferResponseDto>> getTransfersByAccountId(@PathParam("accountId") String accountId) {
        return this.transferService.getTransfersByAccountId(accountId);
    }

}

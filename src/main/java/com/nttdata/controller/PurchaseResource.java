package com.nttdata.controller;

import com.nttdata.model.PurchaseApiRequestDto;
import com.nttdata.model.PurchaseRequestDto;
import com.nttdata.model.PurchaseResponseDto;
import com.nttdata.repository.PurchaseRepository;
import com.nttdata.service.PurchaseService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/purchases")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class PurchaseResource {

    @Inject
    JsonWebToken jwt;

    private final PurchaseService purchaseService;

    public PurchaseResource(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @POST
    public Uni<Response> create(PurchaseApiRequestDto request) {

        PurchaseRequestDto purchaseRequestDto = new PurchaseRequestDto(
                this.jwt.getSubject(),
                request.cardNumber(),
                request.currency(),
                request.amount(),
                request.description()
        );

        return purchaseService.create(purchaseRequestDto)
                .onItem().transform(purchase ->
                        Response.status(Response.Status.CREATED)
                                .entity(purchase)
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
                                .entity("Error interno al crear la compra")
                                .build()
                );
    }

    @GET
    @Path("/{cardId}")
    public Uni<List<PurchaseResponseDto>> getPurchasesByCardId(@PathParam("cardId") String cardId) {
        return this.purchaseService.getPurchasesByCardId(cardId);
    }

}

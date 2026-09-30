package com.nttdata.controller;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/test")
    @RolesAllowed("CUSTOMER")
    public Response test() {
        String documentId = jwt.getSubject();
        return Response.ok(documentId).build();
    }

}

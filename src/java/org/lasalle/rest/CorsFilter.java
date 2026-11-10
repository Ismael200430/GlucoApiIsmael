/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Permite que un frontend web consuma la API desde cualquier origen.
 * También responde las peticiones preflight OPTIONS.
 *
 * @author Lucy Flores
 */
@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String ALLOW_ORIGIN = "*";
    private static final String ALLOW_METHODS = "GET, POST, OPTIONS, HEAD";
    private static final String ALLOW_HEADERS = "Content-Type, Accept, Origin, X-Requested-With";
    private static final String MAX_AGE = "86400";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(Response.ok()
                    .header("Access-Control-Allow-Origin", ALLOW_ORIGIN)
                    .header("Access-Control-Allow-Methods", ALLOW_METHODS)
                    .header("Access-Control-Allow-Headers", ALLOW_HEADERS)
                    .header("Access-Control-Max-Age", MAX_AGE)
                    .build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", ALLOW_ORIGIN);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", ALLOW_METHODS);
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", ALLOW_HEADERS);
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", MAX_AGE);
    }
}

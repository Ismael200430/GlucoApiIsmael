/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.rest;

import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;
import org.lasalle.controller.ControllerMedida;
import org.lasalle.controller.ControllerUsuario;
import org.lasalle.model.Medida;
import org.lasalle.model.Usuario;

/**
 *
 * @author Lucy Flores
 */
@Path("glucosa")
public class RestGlucosa {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);
    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    @Path("saludar")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludar() {
        String respuesta = """
                           { "response" : "Hola desde Java" }
                           """;
        return Response.ok(respuesta).build();
    }

    @Path("saludarPersonalizado")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response saludarPersonalizado(
            @QueryParam("nombre") String nombre
    ) {
        String respuesta = """
                           { "response" : "Hola %s desde Java" }
                           """;
        respuesta = String.format(respuesta, nombre);
        return Response.ok(respuesta).build();
    }

    @Path("cuadrado")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculo(
            @QueryParam("numero") int numero
    ) {
        int cuadrado = numero * numero;
        String respuesta = """
                           { "response" : "Hola el cuadrado de %s es %s" }
                           """;
        respuesta = String.format(respuesta, numero, cuadrado);
        return Response.ok(respuesta).build();
    }

    @Path("getAllUsuarios")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsuarios() {
        Gson gson = new Gson();
        try {
            List<Usuario> lista = new ControllerUsuario().getUsuarios();
            return Response.ok(gson.toJson(lista)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity(gson.toJson(Map.of(
                            "error", "No se pudo obtener la lista de usuarios",
                            "detalle", String.valueOf(e),
                            "causa", String.valueOf(e.getCause()),
                            "host", String.valueOf(System.getenv("DB_HOST")),
                            "puerto", String.valueOf(System.getenv("DB_PORT")),
                            "base", String.valueOf(System.getenv("DB_NAME")),
                            "usuario", String.valueOf(System.getenv("DB_USER")),
                            "ssl", String.valueOf(System.getenv("DB_SSL_MODE")),
                            "passwordConfigurado", String.valueOf(
                                    System.getenv("DB_PASSWORD") != null
                                    && !System.getenv("DB_PASSWORD").isBlank()))))
                    .build();
        }
    }

    @Path("crearUsuario")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearUsuario(
            @FormParam("nombre") String nombre,
            @FormParam("primerApellido") String primerApellido,
            @FormParam("segundoApellido") String segundoApellido,
            @FormParam("email") String email) {

        Gson gson = new Gson();

        if (nombre == null || nombre.isBlank()
                || primerApellido == null || primerApellido.isBlank()
                || email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(gson.toJson(Map.of(
                            "error", "nombre, primerApellido y email son obligatorios")))
                    .build();
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setPrimerApellido(primerApellido.trim());
        usuario.setSegundoApellido(
                segundoApellido == null || segundoApellido.isBlank()
                        ? null
                        : segundoApellido.trim());
        usuario.setEmail(email.trim());

        try {
            int id = new ControllerUsuario().insertUsuario(usuario);
            usuario.setIdUsuario(id);

            return Response.status(Response.Status.CREATED)
                    .entity(gson.toJson(usuario))
                    .build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity(gson.toJson(Map.of("error", "No se pudo guardar el usuario")))
                    .build();
        }
    }

    @Path("registrarMedida")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarMedida(
            @FormParam("idUsuario") int idUsuario,
            @FormParam("glucosa") int glucosa,
            @FormParam("fechaHora") String fechaHora,
            @FormParam("contexto") String contexto,
            @FormParam("notas") String notas) {

        Gson gson = new Gson();

        if (idUsuario <= 0) {
            return badRequest(gson, "idUsuario es obligatorio y debe ser mayor a 0");
        }
        if (glucosa < 20 || glucosa > 600) {
            return badRequest(gson, "glucosa debe estar entre 20 y 600 mg/dL");
        }

        String contextoLimpio = contexto == null || contexto.isBlank() ? null : contexto.trim();
        String notasLimpio = notas == null || notas.isBlank() ? null : notas.trim();

        if (contextoLimpio != null && contextoLimpio.length() > 40) {
            return badRequest(gson, "contexto no debe superar 40 caracteres");
        }
        if (notasLimpio != null && notasLimpio.length() > 100) {
            return badRequest(gson, "notas no debe superar 100 caracteres");
        }

        String fechaNormalizada = normalizarFechaHora(fechaHora);
        if (fechaNormalizada == null) {
            return badRequest(gson,
                    "fechaHora debe tener un formato válido, por ejemplo 2026-11-09 08:30:00");
        }

        try {
            if (!new ControllerUsuario().existsUsuario(idUsuario)) {
                return badRequest(gson, "El idUsuario no existe");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity(gson.toJson(Map.of("error", "No se pudo validar el usuario")))
                    .build();
        }

        Medida medida = new Medida();
        medida.setIdUsuario(idUsuario);
        medida.setGlucosa(glucosa);
        medida.setFechaHora(fechaNormalizada);
        medida.setContexto(contextoLimpio);
        medida.setNotas(notasLimpio);

        try {
            int id = new ControllerMedida().insertMedida(medida);
            medida.setIdMedida(id);

            return Response.status(Response.Status.CREATED)
                    .entity(gson.toJson(medida))
                    .build();
        } catch (SQLIntegrityConstraintViolationException e) {
            return badRequest(gson, "El idUsuario no existe");
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity(gson.toJson(Map.of("error", "No se pudo guardar la medida")))
                    .build();
        }
    }

    @Path("getAllMedidas")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMedidas() {
        Gson gson = new Gson();
        try {
            List<Medida> lista = new ControllerMedida().getMedidas();
            return Response.ok(gson.toJson(lista)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity(gson.toJson(Map.of("error", "No se pudo obtener la lista de medidas")))
                    .build();
        }
    }

    @Path("getMedidasPorUsuario")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMedidasPorUsuario(@QueryParam("idUsuario") int idUsuario) {
        Gson gson = new Gson();

        if (idUsuario <= 0) {
            return badRequest(gson, "idUsuario es obligatorio y debe ser mayor a 0");
        }

        try {
            List<Medida> lista = new ControllerMedida().getMedidasPorUsuario(idUsuario);
            return Response.ok(gson.toJson(lista)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity(gson.toJson(Map.of("error", "No se pudo obtener las medidas del usuario")))
                    .build();
        }
    }

    private String normalizarFechaHora(String fechaHora) {
        if (fechaHora == null || fechaHora.isBlank()) {
            return LocalDateTime.now(ZONA).format(FORMATO_FECHA);
        }

        String valor = fechaHora.trim();

        try {
            OffsetDateTime offset = OffsetDateTime.parse(valor);
            return offset.atZoneSameInstant(ZONA)
                    .toLocalDateTime()
                    .format(FORMATO_FECHA);
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDateTime local = LocalDateTime.parse(valor);
            return local.format(FORMATO_FECHA);
        } catch (DateTimeParseException ignored) {
        }

        try {
            String normalizado = valor.replace('T', ' ');
            if (normalizado.length() == 16) {
                normalizado += ":00";
            }
            LocalDateTime local = LocalDateTime.parse(normalizado, FORMATO_FECHA);
            return local.format(FORMATO_FECHA);
        } catch (DateTimeParseException ignored) {
        }

        return null;
    }

    private Response badRequest(Gson gson, String mensaje) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(gson.toJson(Map.of("error", mensaje)))
                .build();
    }
}

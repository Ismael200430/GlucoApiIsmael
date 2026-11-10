/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.model.ConnectionMysql;
import org.lasalle.model.Usuario;

/**
 *
 * @author mykec
 */
public class ControllerUsuario {

    public List<Usuario> getUsuarios() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT idUsuario, nombre, primerApellido, segundoApellido, email FROM usuario";

        ConnectionMysql connMysql = new ConnectionMysql();
        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                lista.add(fillUsuario(rs));
            }
        }
        return lista;
    }

    public boolean existsUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT 1 FROM usuario WHERE idUsuario = ?";

        ConnectionMysql connMysql = new ConnectionMysql();
        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idUsuario);

            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insertUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, primerApellido, segundoApellido, email) "
                + "VALUES (?, ?, ?, ?)";

        ConnectionMysql connMysql = new ConnectionMysql();
        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, usuario.getNombre());
            pstm.setString(2, usuario.getPrimerApellido());
            pstm.setString(3, usuario.getSegundoApellido());
            pstm.setString(4, usuario.getEmail());

            int filas = pstm.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó ningún usuario");
            }

            try (ResultSet keys = pstm.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    private Usuario fillUsuario(ResultSet rs) throws SQLException {
        Usuario user = new Usuario();
        user.setIdUsuario(rs.getInt("idUsuario"));
        user.setNombre(rs.getString("nombre"));
        user.setPrimerApellido(rs.getString("primerApellido"));
        user.setSegundoApellido(rs.getString("segundoApellido"));
        user.setEmail(rs.getString("email"));
        return user;
    }
}

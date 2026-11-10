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
import org.lasalle.model.Medida;

/**
 *
 * @author mykec
 */
public class ControllerMedida {

    public int insertMedida(Medida medida) throws SQLException {
        String sql = "INSERT INTO medida (idUsuario, glucosa, fechaHora, contexto, notas) "
                + "VALUES (?, ?, ?, ?, ?)";

        ConnectionMysql connMysql = new ConnectionMysql();
        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setInt(1, medida.getIdUsuario());
            pstm.setInt(2, medida.getGlucosa());
            pstm.setString(3, medida.getFechaHora());
            pstm.setString(4, medida.getContexto());
            pstm.setString(5, medida.getNotas());

            int filas = pstm.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó ninguna medida");
            }

            try (ResultSet keys = pstm.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Medida> getMedidas() throws SQLException {
        List<Medida> lista = new ArrayList<>();
        String sql = "SELECT idMedida, idUsuario, glucosa, fechaHora, contexto, notas "
                + "FROM medida ORDER BY fechaHora DESC, idMedida DESC";

        ConnectionMysql connMysql = new ConnectionMysql();
        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                lista.add(fillMedida(rs));
            }
        }
        return lista;
    }

    public List<Medida> getMedidasPorUsuario(int idUsuario) throws SQLException {
        List<Medida> lista = new ArrayList<>();
        String sql = "SELECT idMedida, idUsuario, glucosa, fechaHora, contexto, notas "
                + "FROM medida WHERE idUsuario = ? ORDER BY fechaHora DESC, idMedida DESC";

        ConnectionMysql connMysql = new ConnectionMysql();
        try (Connection conn = connMysql.open();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idUsuario);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    lista.add(fillMedida(rs));
                }
            }
        }
        return lista;
    }

    private Medida fillMedida(ResultSet rs) throws SQLException {
        Medida medida = new Medida();
        medida.setIdMedida(rs.getInt("idMedida"));
        medida.setIdUsuario(rs.getInt("idUsuario"));
        medida.setGlucosa(rs.getInt("glucosa"));
        medida.setFechaHora(rs.getString("fechaHora"));
        medida.setContexto(rs.getString("contexto"));
        medida.setNotas(rs.getString("notas"));
        return medida;
    }
}

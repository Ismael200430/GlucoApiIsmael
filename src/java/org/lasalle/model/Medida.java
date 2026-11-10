/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.model;

/**
 *
 * @author mykec
 */
public class Medida {
    private int idMedida;
    private int idUsuario;
    private int glucosa;
    private String fechaHora;
    private String contexto;
    private String notas;
    
    public Medida(){}

    public Medida(int idMedida, int idUsuario, int glucosa, String fechaHora, String contexto, String notas) {
        this.idMedida = idMedida;
        this.idUsuario = idUsuario;
        this.glucosa = glucosa;
        this.fechaHora = fechaHora;
        this.contexto = contexto;
        this.notas = notas;
    }

    public int getIdMedida() {
        return idMedida;
    }

    public void setIdMedida(int idMedida) {
        this.idMedida = idMedida;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getGlucosa() {
        return glucosa;
    }

    public void setGlucosa(int glucosa) {
        this.glucosa = glucosa;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getContexto() {
        return contexto;
    }

    public void setContexto(String contexto) {
        this.contexto = contexto;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
    
    
}

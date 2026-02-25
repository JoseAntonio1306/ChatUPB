package edu.upb.chatupb_v2.bl.message;

public abstract class Message {
    private String codigo;
    private String ip;

    public Message(String codigo) {
        this.codigo = codigo;
    }
    public String getCodigo() {
        return codigo;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public abstract String generarTrama();
}

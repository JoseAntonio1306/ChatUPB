package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.regex.Pattern;
@Getter
@Setter
public class Hello extends AbstractMessage {
    private String idUsuario;

    public Hello() {
        super("004");
    }
    public Hello(String idUsuario) {
        super("004");
        this.idUsuario = idUsuario;
    }

    public static Hello parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 2) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new Hello(split[1]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) throws IOException {
        client.send(this);
    }
    }

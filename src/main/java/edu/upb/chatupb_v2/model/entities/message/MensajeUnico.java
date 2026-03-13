package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.regex.Pattern;

@Getter
@Setter
public class MensajeUnico extends AbstractMessage {
    private String idUsuario;
    private String idMensaje;
    private String mensaje;

    public MensajeUnico() {
        super("012");
    }
    public MensajeUnico(String idUsuario, String idMensaje, String mensaje) {
        super("012");
        this.idUsuario = idUsuario;
        this.idMensaje = idMensaje;
        this.mensaje = mensaje;
    }

    public static MensajeUnico parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 4) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new MensajeUnico(split[1], split[2], split[3]);
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario +"|" + idMensaje + "|" + mensaje + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) throws IOException {
        client.send(this);
    }
}

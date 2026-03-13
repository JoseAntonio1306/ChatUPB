package edu.upb.chatupb_v2.model.entities.message;

import edu.upb.chatupb_v2.model.server.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Base64;
import java.util.regex.Pattern;

@Getter
@Setter
public class EnviarImagen extends AbstractMessage{
    private String idUsuario;
    private String idMessage;
    private byte[] image;

    public EnviarImagen() {
        super("021");
    }
    public EnviarImagen(String idUsuario, String idMessage, byte[] image) {
        super("021");
        this.idUsuario = idUsuario;
        this.idMessage = idMessage;
        this.image = image;
    }

    public static EnviarImagen parse(String trama) {
        String[] split = trama.split(Pattern.quote("|"));
        if(split.length != 4) {
            throw new IllegalArgumentException("Formato de trama no valido");
        }
        return new EnviarImagen(split[1], split[2], Base64.getDecoder().decode(split[3]));
    }

    @Override
    public String generarTrama() {
        return getCodigo() +"|" +idUsuario +"|" + idMessage +"|" +Base64.getEncoder().encodeToString(image) + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) throws IOException {
//        String base64 = Base64.getEncoder().encodeToString(image);
        client.send(this);
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.model.server;

import edu.upb.chatupb_v2.model.entities.message.*;
import edu.upb.chatupb_v2.model.security.SecureSession;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author rlaredo
 */
public class SocketClient extends Thread {
    private final Socket socket;
    private final String ip;
    private final DataOutputStream dout;
    private final BufferedReader br;
    private List<SocketListener> socketListener = new ArrayList<>();
    private volatile SecureSession secureSession;

    public SecureSession getSecureSession() { return secureSession; }
    public void setSecureSession(SecureSession secureSession) { this.secureSession = secureSession; }

    public String getIp() {
        return ip;
    }

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public SocketClient(String ip) throws IOException {
        this.socket = new Socket(ip, 1900);
        this.ip = ip;
        dout = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public void addListener(SocketListener listener) {
        this.socketListener.add(listener);
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = br.readLine()) != null) {
                System.out.println(message);

                String plain = message;
                boolean handshake = plain.startsWith("014|") || plain.startsWith("015|");

                // descifrar solo si NO es handshake y tenemos sesión
                if (!handshake && secureSession != null) {
                    try {
                        plain = secureSession.decryptFromWire(message);
                    } catch (Exception e) {
                        System.out.println("[SECURITY] No se pudo descifrar payload: " + e.getMessage());
                        continue;
                    }
                }

                String split[] = plain.split(Pattern.quote("|"));
                if (split.length == 0) return;

//                String split[] = message.split(Pattern.quote("|"));
//                if (split.length == 0) {
//                    return;
//                }
                System.out.println("Llego");
                switch (split[0]) {
                    case "001": {
                        System.out.println("Es invitacion");
                        Invitacion invitacion = Invitacion.parse(plain);
                        invitacion.setIp(ip);
//                        Mediador.getInstance().sendMessage(invitacion.getIdUsuario(), invitacion);
                        notificar(invitacion);
                        break;
                    }
                    case "002": {
                        System.out.println("Es aceptacion");
                        Aceptar aceptar = Aceptar.parse(plain);
//                        Mediador.getInstance().addClient(aceptar.getIdUsuario(), this);
//                        Mediador.getInstance().sendMessage(aceptar.getIdUsuario(), aceptar);
                        notificar(aceptar);
                        break;
                    }
                    case "003": {
                        System.out.println("Es rechazo");
                        Rechazar rechazo = Rechazar.parse(plain);
                        rechazo.setIp(ip);
                        notificar(rechazo);
                        break;
                    }
                    case "004": {
                        System.out.println("Es hello");
                        Hello hello = Hello.parse(plain);
                        hello.setIp(ip);
                        notificar(hello);
                        break;
                    }
                    case "005": {
                        System.out.println("Es aceptación del hello");
                        AcceptHello acceptHello = AcceptHello.parse(plain);
                        acceptHello.setIp(ip);
                        notificar(acceptHello);
                        break;
                    }
                    case "006": {
                        System.out.println("Es rechazo del hello");
                        RejectHello rejectHello = RejectHello.parse(plain);
                        rejectHello.setIp(ip);
                        notificar(rejectHello);
                        break;
                    }
                    case "007": {
                        System.out.println("Es mensaje");
                        Chat chat = Chat.parse(plain);
                        notificar(chat);
                        break;
                    }
                    case "008": {
                        System.out.println("Es confirmación de que llego el mensaje");
                        ReceiveMessage receiveMessage = ReceiveMessage.parse(plain);
                        notificar(receiveMessage);
                        break;
                    }
                    case "0018": {
                        System.out.println("Esta fuera de linea");
                        Offline offline = Offline.parse(plain);
                        notificar(offline);
                        break;
                    }
                    case "014": {
                        SecureConnect sc = SecureConnect.parse(plain);
                        sc.setIp(ip);
                        notificar(sc);
                        break;
                    }
                    case "015": {
                        SecureKey sk = SecureKey.parse(plain);
                        sk.setIp(ip);
                        notificar(sk);
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //172.16.72.1
    public void notificar(Message message) {
        for (SocketListener listener : socketListener) {
            java.awt.EventQueue.invokeLater(() -> listener.onMessage(this, message));
        }
    }

//    public void send(Message message) throws IOException {
//        message = message + System.lineSeparator();
//        try {
//            dout.write(message.generarTrama().getBytes("UTF-8"));
//            dout.flush();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void send(Message message) throws IOException {
        try {
            String frame = message.generarTrama();

            // quitar salto final para que no quede dentro del cifrado
            if (frame.endsWith(System.lineSeparator())) {
                frame = frame.substring(0, frame.length() - System.lineSeparator().length());
            }

            boolean handshake = frame.startsWith("014|") || frame.startsWith("015|");
            SecureSession s = secureSession;

            if (!handshake && s != null) {
                frame = s.encryptToWire(frame);
            }

            // SIEMPRE mandamos una línea
            frame = frame + System.lineSeparator();

            dout.write(frame.getBytes("UTF-8"));
            dout.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.socket.close();
            this.br.close();
            this.dout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

package edu.upb.chatupb_v2.model.server;

import edu.upb.chatupb_v2.model.dao.ContactDao;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.message.*;
import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.settings.UserSettings;
import edu.upb.chatupb_v2.view.IChatView;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Mediador implements SocketListener{
    private volatile IChatView view;
    private final ContactDao contactDao = new ContactDao();
    private final Set<String> pendingInvitations = new HashSet<>();

    private volatile String localUserId;

    private final Map<String, String> pendingHelloByIp = new ConcurrentHashMap<>();
    private final Set<String> pendingHelloContacts = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mediador-scheduler");
        t.setDaemon(true);
        return t;
    });

    private static final Mediador INSTANCE = new Mediador();

    private final HashMap<String, SocketClient> clients = new HashMap<>();

    private Mediador(){}

    public static Mediador getInstance(){
        return INSTANCE;
    }

    public void setView(IChatView view) {
        this.view = view;
    }

    public void setLocalUser(String userId) {
        this.localUserId = userId;
    }

    // Obtiene la IP del SocketClient asociado a un usuario.
    public String getIp(String userId) {
        SocketClient c = this.clients.get(userId);
        return (c == null) ? null : c.getIp();
    }

    public void addClient(String userId, SocketClient client){
        this.clients.put(userId, client);
    }

    public void removeClient(String userId){
        this.clients.remove(userId);
    }

    public void sendMessage(String userId, AbstractMessage message){
        SocketClient client = this.clients.get(userId);
        if (client == null) return;

        try{
            client.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Enviando mensaje: " + message.generarTrama());
    }

    public void checkPresence(String idUsuario) {
        try {
            Contact contact = contactDao.findByCode(idUsuario);
            if (contact == null || contact.getIp() == null || contact.getIp().isBlank()) {
                System.out.println("No hay IP guardada para: " + idUsuario);
                notifyContactStatus(idUsuario, false);
                return;
            }

            String ip = contact.getIp().trim();

            // Reutilizar cliente si existe y coincide la IP
            SocketClient existing = clients.get(idUsuario);
            if (existing == null || existing.getIp() == null || !existing.getIp().equals(ip)) {
                try {
                    SocketClient newClient = new SocketClient(ip);
                    newClient.addListener(this);
                    newClient.start();
                    clients.put(idUsuario, newClient);
                } catch (Exception e) {
                    System.out.println("No se pudo conectar a " + ip + " para " + idUsuario + ": " + e.getMessage());
                    notifyContactStatus(idUsuario, false);
                    return;
                }
            }


            String myId = localUserId;
            if (myId == null || myId.isBlank()) {
                myId = UserSettings.getUserId();
            }
            pendingHelloContacts.add(idUsuario);
            pendingHelloByIp.put(ip, idUsuario);

            System.out.println("Enviando HELLO a " + idUsuario );
            sendMessage(idUsuario, new Hello(myId));

            //si no llega AcceptHello, marcamos offline
            scheduler.schedule(() -> {
                if (pendingHelloContacts.remove(idUsuario)) {
                    pendingHelloByIp.values().removeIf(v -> v.equals(idUsuario));
                    System.out.println("no hubo respuesta de " + idUsuario);
                    notifyContactStatus(idUsuario, false);
                }
            }, 3, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.out.println("Error en checkPresence: " + e.getMessage());
            notifyContactStatus(idUsuario, false);
        }
    }


    /** Rechaza invitación: envía Rechazar, borra el contacto (si fue persistido) y cierra la conexión. */
    public void rejectInvitation(String userId) {
        try {
            sendMessage(userId, new Rechazar());
        } catch (Exception ignored) {
        }

        // Si lo guardamos cuando llegó la invitación, lo eliminamos al rechazar
        try {
            contactDao.deleteByCode(userId);
        } catch (Exception ignored) {
        }

        pendingInvitations.remove(userId);
        closeClient(userId);
    }

    public void closeClient(String userId) {
        SocketClient client = this.clients.remove(userId);
        if (client != null) {
            client.close();
        }
    }

    public void invitacion(String ip, String myId, String myName){
        SocketClient client;
        try{
            client = new SocketClient(ip);
            client.addListener(this);
            client.start();

        } catch (Exception e) {
            throw new OperationException("No se logro establecer la conexion");
        }
        Invitacion invitacion = new Invitacion();
        invitacion.setIdUsuario(myId);
        invitacion.setNombre(myName);
        try {
            client.send(invitacion);
        } catch (IOException e) {
            throw new OperationException("No se logro enviar el mensaje ");
        }
    }

    @Override
    public void onMessage(SocketClient socketClient, AbstractMessage message) {
        // Guardar el socket contra el id del usuario emisor para poder responder después
        String senderId = extractSenderId(message);
        if (senderId != null) {
            this.clients.put(senderId, socketClient);
        }
//region hello before
//        if (message instanceof Hello hello) {
//            System.out.println("Recibido HELLO de: " + hello.getIdUsuario());
//
//            // Guardamos/actualizamos IP del emisor si ya existe en contactos
//            try {
//                Contact existing = contactDao.findByCode(hello.getIdUsuario());
//                if (existing != null) {
//                    existing.setIp(socketClient.getIp());
//                    contactDao.update(existing);
//                }
//                if (existing == null) {
//
//                }
//            } catch (Exception ignored) {}
//
//            notifyContactStatus(hello.getIdUsuario(), true);
//
//            // Responder AcceptHello automaticamente
//            String id = hello.getIdUsuario();
//            String myId = localUserId;
//            if (myId == null || myId.isBlank()) {
//                myId = UserSettings.getUserId();
//            }
//            try {
//                socketClient.send(new AcceptHello(myId));
//                System.out.println("Respondido ACCEPT_HELLO a " + hello.getIdUsuario());
//            } catch (Exception e) {
//                System.out.println("No se pudo responder ACCEPT_HELLO: " + e.getMessage());
//            }
//            return;
//        }
//endregion
        if (message instanceof Hello hello) {
            System.out.println("Recibido Hello de: " + hello.getIdUsuario());

            String id = hello.getIdUsuario();

            // 1) Buscar en BD si el contacto existe
            Contact existing = null;
            try {
                existing = contactDao.findByCode(id);
            } catch (Exception e) {
                System.out.println("Error consultando BD: " + e.getMessage());
            }

            // 2) Si no existe en BD se rechaza
            if (existing == null) {
                System.out.println("Hello rechazado: " + id);

                String myId = localUserId;
                if (myId == null || myId.isBlank()) {
                    myId = UserSettings.getUserId();
                }

                try {
                    socketClient.send(new RejectHello());
                    System.out.println("Se rechazo el hello a: " + id);
                } catch (Exception e) {
                    System.out.println("No se pudo rechazar el hello: " + e.getMessage());
                }
                return;
            }

            // 3) Si existe en db lo aceptamos, actualizamos la IP y se lo marca online
            try {
                existing.setIp(socketClient.getIp());
                contactDao.update(existing);
            } catch (Exception ignored) {}

            notifyContactStatus(senderId, true);

            String myId = localUserId;
            if (myId == null || myId.isBlank()) {
                myId = UserSettings.getUserId();
            }

            try {
                socketClient.send(new AcceptHello(myId));
                System.out.println("Se acepto el hello a: " + senderId);
            } catch (Exception e) {
                System.out.println("No se pudo aceptar el hello: " + e.getMessage());
            }
            return;
        }

        if (message instanceof AcceptHello acceptHello) {
            System.out.println("El hello fue aceptado por: " + acceptHello.getIdUsuario() );
            pendingHelloContacts.remove(acceptHello.getIdUsuario());
            pendingHelloByIp.remove(socketClient.getIp());
            notifyContactStatus(acceptHello.getIdUsuario(), true);
            return;
        }

        if (message instanceof RejectHello rejectHello) {
            String ip = rejectHello.getIp();
            String contactCode = (ip == null) ? null : pendingHelloByIp.remove(ip);
            if (contactCode != null) {
                pendingHelloContacts.remove(contactCode);
                System.out.println("Te rechazaron el hello de " + contactCode );
                notifyContactStatus(contactCode, false);
            } else {
                System.out.println("REJECT_HELLO recibido");
            }
            return;
        }

        // Confirmacion de entrega de mensaje
        if (message instanceof ReceiveMessage rm) {
            System.out.println("Confirmación recibida para mensaje: " + rm.getIdMensaje());
            return;
        }

        // Cuando llega un mensaje Chat, responder automaticamente
        if (message instanceof Chat chat) {
            try {
                sendMessage(chat.getIdUsuario(), new ReceiveMessage(chat.getIdMensaje()));
                System.out.println("Enviado ReceiveMessage para: " + chat.getIdMensaje());
            } catch (Exception e) {
                System.out.println(" No se pudo enviar ReceiveMessage: " + e.getMessage());
            }
            notifyContactStatus(chat.getIdUsuario(), true);
            // seguimos, para reenviar a la vista
        }

        // cuando llega invitación, persistir y si se rechaza, eliminar
        if (message instanceof Invitacion inv) {
            try {
                contactDao.upsert(inv.getIdUsuario(), inv.getNombre(), socketClient.getIp());
                pendingInvitations.add(inv.getIdUsuario());
            } catch (Exception e) {
                System.out.println("No se pudo guardar invitación entrante: " + e.getMessage());
            }
        }

        if (message instanceof EnviarContacto enviarContacto) {
            try {
                contactDao.upsert(enviarContacto.getIdUsuario(), enviarContacto.getNombre(), socketClient.getIp());
            } catch (Exception e) {
                System.out.println("No se pudo guardar contacto entrante: " + e.getMessage());
            }
        }

        // Reenviar a la vista (sin SocketClient)
        IChatView v = this.view;
        if (v != null) {
            v.onSocketMessage(message);
        }

        // Si el otro se desconecta, cerramos su conexión local
        if (message instanceof Offline off) {
            closeClient(off.getIdUsuario());
            notifyContactStatus(off.getIdUsuario(), false);
        }
    }

    private String extractSenderId(AbstractMessage message) {
        if (message instanceof Invitacion inv) return inv.getIdUsuario();
        if (message instanceof Aceptar ac) return ac.getIdUsuario();
        if (message instanceof Chat ch) return ch.getIdUsuario();
        if (message instanceof Offline off) return off.getIdUsuario();
        if (message instanceof EnviarContacto ec) return ec.getIdUsuario();
        return null;
    }

    private void notifyContactStatus(String contactCode, boolean online) {
        IChatView v = this.view;
        if (v == null || contactCode == null) return;

        // Si estamos en un hilo que no es Swing (por ejemplo timeout), lo mandamos al EDT.
        if (SwingUtilities.isEventDispatchThread()) {
            v.onContactStatusChanged(contactCode, online);
        } else {
            SwingUtilities.invokeLater(() -> v.onContactStatusChanged(contactCode, online));
        }
    }
}

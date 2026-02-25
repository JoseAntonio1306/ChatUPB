package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.bl.message.*;
import edu.upb.chatupb_v2.bl.server.Mediador;
import edu.upb.chatupb_v2.bl.server.SocketClient;
import edu.upb.chatupb_v2.bl.server.SocketListener;
import edu.upb.chatupb_v2.bl.server.Mediador;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainChatUI extends JFrame implements SocketListener{

    private final String myUserId = "00000001-0001-0001-0001-000000000001";//UUID.randomUUID().toString()
    private final String myName;

    private final DefaultListModel<ContactItem> contactosModel = new DefaultListModel<>();
    private final JList<ContactItem> listaContactos = new JList<>(contactosModel);

    private final JButton btnAddContacto = new JButton("Añadir contacto");

    private final JLabel lblNombreContacto = new JLabel("Nombre del contacto");
    private final JTextArea txtChat = new JTextArea();

    private final JTextField txtMensaje = new JTextField();
    private final JButton btnEnviar = new JButton("Enviar");

    private final JButton btnOffline = new JButton("Offline");

    private ContactItem contactoSeleccionado = null;

    // historial en memoria: userId -> texto del chat
    private final Map<String, StringBuilder> historial = new HashMap<>();

    public MainChatUI() {
        super("ChatUPB");

        String nombre = JOptionPane.showInputDialog(this, "Tu nombre:", "Usuario", JOptionPane.QUESTION_MESSAGE);
        if (nombre == null || nombre.trim().isEmpty()) nombre = "Jose";
        myName = nombre.trim();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        construirUI();
        conectarEventos();

//        txtChat.append("Tu ID: " + myUserId + "\n");
//        txtChat.append("Tu nombre: " + myName + "\n\n");
//        txtChat.append("Usa 'Añadir contacto' para conectar.\n");
    }

    public String getMyUserId() { return myUserId; }
    public String getMyName() { return myName; }

    private void construirUI() {
        JPanel panelIzq = new JPanel(new BorderLayout(8, 8));
        panelIzq.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelIzq.add(btnAddContacto, BorderLayout.NORTH);

        listaContactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelIzq.add(new JScrollPane(listaContactos), BorderLayout.CENTER);

        JPanel panelDer = new JPanel(new BorderLayout(8, 8));
        panelDer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JPanel panelDer2 = new JPanel(new BorderLayout(8,8));
        lblNombreContacto.setFont(lblNombreContacto.getFont().deriveFont(Font.BOLD, 16f));
        panelDer2.add(lblNombreContacto, BorderLayout.CENTER);
        panelDer2.add(btnOffline, BorderLayout.EAST);
        panelDer.add(panelDer2, BorderLayout.NORTH);

//        lblNombreContacto.setFont(lblNombreContacto.getFont().deriveFont(Font.BOLD, 16f));
//        panelDer.add(lblNombreContacto, BorderLayout.NORTH);
//        panelDer.add(new JPanel(btnOffline.getLayout()), BorderLayout.NORTH);

        txtChat.setEditable(false);
        panelDer.add(new JScrollPane(txtChat), BorderLayout.CENTER);

        JPanel panelEnviar = new JPanel(new BorderLayout(8, 8));
        panelEnviar.add(txtMensaje, BorderLayout.CENTER);
        panelEnviar.add(btnEnviar, BorderLayout.EAST);
        panelDer.add(panelEnviar, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, panelDer);
        split.setDividerLocation(280);
        getContentPane().add(split);
    }

    private void conectarEventos() {
        btnAddContacto.addActionListener(e -> abrirUIAgregarContacto());

        listaContactos.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            contactoSeleccionado = listaContactos.getSelectedValue();

            if (contactoSeleccionado == null) {
                lblNombreContacto.setText("Nombre del contacto");
                txtChat.setText("");
                return;
            }

            lblNombreContacto.setText(contactoSeleccionado.nombre);
            StringBuilder sb = historial.getOrDefault(contactoSeleccionado.userId, new StringBuilder());
            txtChat.setText(sb.toString());
        });

        btnEnviar.addActionListener(e -> enviarMensaje());
        txtMensaje.addActionListener(e -> enviarMensaje()); // Enter
        btnOffline.addActionListener(e -> mandarOffline());
    }

    private void abrirUIAgregarContacto() {
        ChatUI ui = new ChatUI(this);
        ui.setVisible(true);
    }

    private void mandarOffline(){

            Message offline = new Offline(contactoSeleccionado.userId);
            Mediador.getInstance().sendMessage(myUserId, offline);

    }

    private void enviarMensaje() {
        String texto = txtMensaje.getText();
        if (texto == null) return;
        texto = texto.trim();
        if (texto.isEmpty()) return;

        if (contactoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero selecciona un contacto.");
            return;
        }

        appendToHistory(contactoSeleccionado.userId, "Yo: " + texto);
        txtMensaje.setText("");

        Message chat = new Chat(myUserId, UUID.randomUUID().toString(), texto);
        Mediador.getInstance().sendMessage(contactoSeleccionado.userId, chat);
    }

    private void appendToHistory(String contactId, String line) {
        StringBuilder sb = historial.computeIfAbsent(contactId, k -> new StringBuilder());
        sb.append(line).append("\n");

        // si justo estoy viendo ese chat, actualizo pantalla
        if (contactoSeleccionado != null && contactoSeleccionado.userId.equals(contactId)) {
            txtChat.setText(sb.toString());
        }
    }

    private void upsertContacto(String userId, String nombre) {
        for (int i = 0; i < contactosModel.size(); i++) {
            ContactItem c = contactosModel.get(i);
            if (c.userId.equals(userId)) {
                c.nombre = nombre;
                listaContactos.repaint();
                return;
            }
        }
        contactosModel.addElement(new ContactItem(userId, nombre));
    }

//    @Override
    public void onMessage(SocketClient socketClient, Message message) {

//        if (message instanceof Invitacion) {
//            Invitacion inv = (Invitacion) message;
//
//            int respuesta = JOptionPane.showConfirmDialog(
//                    this,
//                    "Invitación de: " + inv.getNombre(),
//                    "Invitación",
//                    JOptionPane.YES_NO_OPTION
//            );
//
//            if (respuesta == JOptionPane.YES_OPTION) {
//                // Guardar socket para poder enviarle cosas a ese userId
//                Mediador.getInstance().addClient(inv.getIdUsuario(), socketClient);
//
//                // Agregar contacto a la lista
//                upsertContacto(inv.getIdUsuario(), inv.getNombre());
//
//                // Responder aceptando
//                Message aceptar = new Aceptar(myUserId, myName);
//                Mediador.getInstance().sendMessage(inv.getIdUsuario(), aceptar);
//            }else{
//                try {
//                    Message rechazar = new Rechazar();
//                    socketClient.send(rechazar);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//            return;
//        }
//
//        if (message instanceof Aceptar) {
//            Aceptar ac = (Aceptar) message;
//
//            // Guardar socket para ese userId (ahora ya puedo mandarle mensajes)
//            Mediador.getInstance().addClient(ac.getIdUsuario(), socketClient);
//
//            // Agregar contacto
//            upsertContacto(ac.getIdUsuario(), ac.getNombre());
//
//            JOptionPane.showMessageDialog(this, ac.getNombre() + " aceptó tu invitación.");
//
//            // auto-seleccionar para chatear
//            for (int i = 0; i < contactosModel.size(); i++) {
//                if (contactosModel.get(i).userId.equals(ac.getIdUsuario())) {
//                    listaContactos.setSelectedIndex(i);
//                    break;
//                }
//            }
//            return;
//        }
//        if (message instanceof Rechazar) {
//            JOptionPane.showMessageDialog(this, "Offline.");
//            return;
//        }
//
//        if (message instanceof Chat) {
//            Chat chat = (Chat) message;
//            // por si llega un mensaje de alguien “nuevo”
//            upsertContacto(chat.getIdUsuario(), chat.getIdUsuario());
//
//            appendToHistory(chat.getIdUsuario(), "Él: " + chat.getMensaje());
//        }
//
//        if(message instanceof Offline) {
//            Offline offline = (Offline) message;
//
//            Mediador.getInstance().addClient(offline.getIdUsuario(), socketClient);
//            Mediador.getInstance().sendMessage(offline.getIdUsuario(), offline);
//
//            JOptionPane.showMessageDialog(this, "Offline.");
//            socketClient.close();
//        }
    }

    private final SocketListener socketListener = new SocketListener() {
        @Override
        public void onMessage(SocketClient socketClient, Message message) {
            helpWithMessages(socketClient, message);
        }
    };

    public SocketListener getSocketListener() { return socketListener; }

    private void helpWithMessages(SocketClient socketClient, Message message) {
        if (message instanceof Invitacion) {
            Invitacion inv = (Invitacion) message;

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "Invitación de: " + inv.getNombre(),
                    "Invitación",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                // Guardar socket para poder enviarle cosas a ese userId
                Mediador.getInstance().addClient(inv.getIdUsuario(), socketClient);

                // Agregar contacto a la lista
                upsertContacto(inv.getIdUsuario(), inv.getNombre());

                // Responder aceptando
                Message aceptar = new Aceptar(myUserId, myName);
                Mediador.getInstance().sendMessage(inv.getIdUsuario(), aceptar);
            }else{
                try {
                    Message rechazar = new Rechazar();
                    socketClient.send(rechazar);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return;
        }

        if (message instanceof Aceptar) {
            Aceptar ac = (Aceptar) message;

            // Guardar socket para ese userId (ahora ya puedo mandarle mensajes)
            Mediador.getInstance().addClient(ac.getIdUsuario(), socketClient);

            // Agregar contacto
            upsertContacto(ac.getIdUsuario(), ac.getNombre());

            JOptionPane.showMessageDialog(this, ac.getNombre() + " aceptó tu invitación.");

            // auto-seleccionar para chatear
            for (int i = 0; i < contactosModel.size(); i++) {
                if (contactosModel.get(i).userId.equals(ac.getIdUsuario())) {
                    listaContactos.setSelectedIndex(i);
                    break;
                }
            }
            return;
        }
        if (message instanceof Rechazar) {
            JOptionPane.showMessageDialog(this, "Offline.");
            return;
        }

        if (message instanceof Chat) {
            Chat chat = (Chat) message;
            // por si llega un mensaje de alguien “nuevo”
            upsertContacto(chat.getIdUsuario(), chat.getIdUsuario());

            appendToHistory(chat.getIdUsuario(), "Él: " + chat.getMensaje());
        }

        if(message instanceof Offline) {
            Offline offline = (Offline) message;

            Mediador.getInstance().addClient(offline.getIdUsuario(), socketClient);
            Mediador.getInstance().sendMessage(offline.getIdUsuario(), offline);

            JOptionPane.showMessageDialog(this, "Offline.");
            socketClient.close();
        }
    }

    private static class ContactItem {
        String userId;
        String nombre;

        ContactItem(String userId, String nombre) {
            this.userId = userId;
            this.nombre = (nombre == null || nombre.trim().isEmpty()) ? userId : nombre;
        }

//        @Override
//        protected void handleCommon(SocketClient socketClient, Message message) {
//            // Si llega Aceptar o Invitacion, podrías registrar el socket
//            // OJO: esto depende de cómo quieres tu flujo, pero es lo que te "sugiere" el enunciado.
//
//            if (message instanceof edu.upb.chatupb_v2.bl.message.Aceptar) {
//                var ac = (edu.upb.chatupb_v2.bl.message.Aceptar) message;
//                Mediador.getInstance().addClient(ac.getIdUsuario(), socketClient);
//            }
//
//            if (message instanceof edu.upb.chatupb_v2.bl.message.Offline) {
//                var off = (edu.upb.chatupb_v2.bl.message.Offline) message;
//                Mediador.getInstance().removeClient(off.getIdUsuario());
//                try { socketClient.close(); } catch (Exception ignored) {}
//            }
//        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}

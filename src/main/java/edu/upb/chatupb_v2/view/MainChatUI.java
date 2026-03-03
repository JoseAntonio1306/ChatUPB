package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.message.*;
import edu.upb.chatupb_v2.model.server.Mediador;
import edu.upb.chatupb_v2.model.server.SocketClient;
import edu.upb.chatupb_v2.model.server.SocketListener;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.dao.ContactDao;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MainChatUI extends JFrame implements IChatView {

    private final String myUserId = "00000001-0001-0001-0001-000000000001";
    private final String myName;

    private final ContactDao contactDao = new ContactDao();
    private ContactController contactController;
    private MessageController messageController;

    private final DefaultListModel<Contact> contactosModel = new DefaultListModel<>();
    private final JList<Contact> listaContactos = new JList<>(contactosModel);

    private final JButton btnAddContacto = new JButton("Añadir contacto");

    private final JLabel lblNombreContacto = new JLabel("Nombre del contacto");
    private final JTextArea txtChat = new JTextArea();

    private final JTextField txtMensaje = new JTextField();
    private final JButton btnEnviar = new JButton("Enviar");

    private final JButton btnOffline = new JButton("Offline");

    private Contact contactoSeleccionado = null;

    // historial en memoria: code(userId) -> texto del chat
//    private final Map<String, StringBuilder> historial = new HashMap<>();

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

        MessageController msgController = new MessageController(this, myUserId);
        setMessageController(msgController);

//        cargarContactosDesdeDB();
        ContactController controller = new ContactController(this);
        setContactController(controller);
        controller.onLoad();
    }

    public String getMyUserId() {
        return myUserId;
    }

    public String getMyName() {
        return myName;
    }

    private void construirUI() {
        JPanel panelIzq = new JPanel(new BorderLayout(8, 8));
        panelIzq.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelIzq.add(btnAddContacto, BorderLayout.NORTH);

        listaContactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaContactos.setCellRenderer(new ContactRenderer());
        panelIzq.add(new JScrollPane(listaContactos), BorderLayout.CENTER);

        JPanel panelDer = new JPanel(new BorderLayout(8, 8));
        panelDer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelHeader = new JPanel(new BorderLayout(8, 8));
        lblNombreContacto.setFont(lblNombreContacto.getFont().deriveFont(Font.BOLD, 16f));
        panelHeader.add(lblNombreContacto, BorderLayout.CENTER);
        panelHeader.add(btnOffline, BorderLayout.EAST);
        panelDer.add(panelHeader, BorderLayout.NORTH);

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

            lblNombreContacto.setText(contactoSeleccionado.getName());
            if (messageController != null) {
                messageController.onOpenConversation(contactoSeleccionado);
            } else {
                txtChat.setText("");
            }
        });

        btnEnviar.addActionListener(e -> enviarMensaje());
        txtMensaje.addActionListener(e -> enviarMensaje());
        btnOffline.addActionListener(e -> mandarOffline());
    }

    private void cargarContactosDesdeDB() {
        contactosModel.clear();
        try {
            List<Contact> contactos = contactDao.findAll();
            for (Contact c : contactos) {
                c.setStateConnect(false); // estado en runtime
                contactosModel.addElement(c);
            }
        } catch (SQLException e) {
            System.err.println("[DB] No se pudo cargar contactos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirUIAgregarContacto() {
        ChatUI ui = new ChatUI(this);
        ui.setVisible(true);
    }

    private void mandarOffline() {
        if (contactoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero selecciona un contacto.");
            return;
        }
        Message offline = new Offline(contactoSeleccionado.getCode());
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

        txtMensaje.setText("");

        String messageId = UUID.randomUUID().toString();

        if (messageController != null) {
            messageController.onOutgoingMessage(contactoSeleccionado, messageId, texto);
            messageController.onOpenConversation(contactoSeleccionado); // recargar historial
        }

        Message chat = new Chat(myUserId, messageId, texto);
        Mediador.getInstance().sendMessage(contactoSeleccionado.getCode(), chat);
    }

//    private void appendToHistory(String contactCode, String line) {
//        StringBuilder sb = historial.computeIfAbsent(contactCode, k -> new StringBuilder());
//        sb.append(line).append("\n");
//
//        if (contactoSeleccionado != null && contactoSeleccionado.getCode().equals(contactCode)) {
//            txtChat.setText(sb.toString());
//        }
//    }

    private void upsertContactoEnUI(Contact contact) {
        for (int i = 0; i < contactosModel.size(); i++) {
            Contact c = contactosModel.get(i);
            if (c.getCode().equals(contact.getCode())) {
                c.setName(contact.getName());
                c.setIp(contact.getIp());
                c.setStateConnect(contact.isStateConnect());
                listaContactos.repaint();
                return;
            }
        }
        contactosModel.addElement(contact);
    }

    private Contact persistContact(String code, String name, String ip) {
        try {
            return contactDao.upsert(code, name, ip);
        } catch (Exception e) {
            System.err.println("[DB] No se pudo guardar/actualizar contacto: " + e.getMessage());
            return Contact.builder().code(code).name(name).ip(ip).stateConnect(false).build();
        }
    }

    private void handleCommon(SocketClient socketClient, Message message) {
        if (message instanceof Offline) {
            Offline off = (Offline) message;

            // marco como offline en UI
            for (int i = 0; i < contactosModel.size(); i++) {
                Contact c = contactosModel.get(i);
                if (c.getCode().equals(off.getIdUsuario())) {
                    c.setStateConnect(false);
                    listaContactos.repaint();
                    break;
                }
            }

            JOptionPane.showMessageDialog(this, "Offline.");
        }
    }

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
                Mediador.getInstance().addClient(inv.getIdUsuario(), socketClient);

                String ip = socketClient.getIp();
                Contact contact = persistContact(inv.getIdUsuario(), inv.getNombre(), ip);
                contact.setStateConnect(true);
                upsertContactoEnUI(contact);

                Message aceptar = new Aceptar(myUserId, myName);
                Mediador.getInstance().sendMessage(inv.getIdUsuario(), aceptar);
            } else {
                try {
                    socketClient.send(new Rechazar());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        if (message instanceof Aceptar) {
            Aceptar ac = (Aceptar) message;
            Mediador.getInstance().addClient(ac.getIdUsuario(), socketClient);

            String ip = socketClient.getIp();
            Contact contact = persistContact(ac.getIdUsuario(), ac.getNombre(), ip);
            contact.setStateConnect(true);
            upsertContactoEnUI(contact);

            JOptionPane.showMessageDialog(this, ac.getNombre() + " aceptó tu invitación.");

            // auto-seleccionar para chatear
            for (int i = 0; i < contactosModel.size(); i++) {
                if (contactosModel.get(i).getCode().equals(ac.getIdUsuario())) {
                    listaContactos.setSelectedIndex(i);
                    break;
                }
            }
            return;
        }

        if (message instanceof Rechazar) {
            JOptionPane.showMessageDialog(this, "Rechazaron tu invitación.");
            return;
        }

        if (message instanceof Chat) {
            Chat chat = (Chat) message;

            if (messageController != null) {
                messageController.onIncomingMessage(chat);

                if (contactoSeleccionado != null && contactoSeleccionado.getCode().equals(chat.getIdUsuario())) {
                    messageController.onOpenConversation(contactoSeleccionado);
                }
            }

            String ip = socketClient.getIp();

            String nameToUse = chat.getIdUsuario();
            try {
                Contact existing = contactDao.findByCode(chat.getIdUsuario());
                if (existing != null && existing.getName() != null && !existing.getName().isBlank()) {
                    nameToUse = existing.getName();
                }
            } catch (Exception ignored) {}

            Contact contact = persistContact(chat.getIdUsuario(), nameToUse, ip);
            contact.setStateConnect(true);
            upsertContactoEnUI(contact);

            return;
        }

        handleCommon(socketClient, message);
    }


    @Override
    public void setContactController(ContactController contactController) {
        this.contactController = contactController;
    }

//    @Override
//    public void onLoad(List<Contact> contactsList) {
//        try {
    ////                modelo.removeAllElements();
//            contactosModel.addAll(contactsList);
//            mainView.lista.setModel(contactosModel);
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }
    @Override
    public void onLoad(List<Contact> contactsList) {
        SwingUtilities.invokeLater(() -> {
            contactosModel.clear();

            for (Contact c : contactsList) {
                c.setStateConnect(false); // runtime
                contactosModel.addElement(c);
            }

            listaContactos.setModel(contactosModel);

            listaContactos.repaint();
        });
    }

    @Override
    public void setMessageController(MessageController messageController) {
        this.messageController = messageController;
    }

    @Override
    public void onChatHistoryLoaded(String contactCode, String historyText) {
        // solo actualiza si ese contacto es el seleccionado
        if (contactoSeleccionado != null && contactoSeleccionado.getCode().equals(contactCode)) {
            txtChat.setText(historyText);
        }
    }

    @Override
    public void onSocketMessage(SocketClient socketClient, Message message) {
        helpWithMessages(socketClient, message);
    }
}

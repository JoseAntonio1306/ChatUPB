package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.AnalizadorController;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.model.dao.ContactDao;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.message.*;
import edu.upb.chatupb_v2.model.server.Mediador;
import edu.upb.chatupb_v2.view.components.RoundButton;
import edu.upb.chatupb_v2.view.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainChatUI extends JFrame implements IChatView {

    private static final Color COLOR_APP_BACKGROUND = new Color(243, 246, 251);
    private static final Color COLOR_CHAT_BACKGROUND = new Color(248, 250, 252);

    private static final Color COLOR_HEADER = new Color(30, 41, 59);
    private static final Color COLOR_HEADER_TEXT = Color.WHITE;

    private static final Color COLOR_OUTGOING = new Color(232, 240, 255);
    private static final Color COLOR_INCOMING = Color.WHITE;

    private static final Color COLOR_BORDER = new Color(226, 232, 240);
    private static final Color COLOR_META = new Color(100, 116, 139);

    private static final Color COLOR_ACCENT = new Color(79, 70, 229);
    private static final Color COLOR_SEND_BUTTON = new Color(67, 56, 202);
    private static final Color COLOR_SEND_BUTTON_HOVER = new Color(79, 70, 229);
    private static final Color COLOR_SEND_BUTTON_PRESSED = new Color(55, 48, 163);

    private static final DateTimeFormatter DATABASE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter HOUR_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final String myUserId;
    private final String myName;

    private final AnalizadorController textAnalyzer = new AnalizadorController();

    private final ContactDao contactDao = new ContactDao();
    private ContactController contactController;
    private MessageController messageController;

    private final DefaultListModel<Contact> contactosModel = new DefaultListModel<>();
    private final JList<Contact> listaContactos = new JList<>(contactosModel);

    private final JButton btnAddContacto = new JButton("Añadir contacto");
    private final JButton btnConectar = new JButton("Conectar");

    private final JLabel lblNombreContacto = new JLabel("Nombre del contacto");
    private final JPanel panelMensajes = new JPanel();
    private final JScrollPane scrollMensajes = new JScrollPane(panelMensajes);

    private final JTextArea txtMensaje = new JTextArea(1, 20);
    private final JButton btnEnviar = new RoundButton("Enviar");

    private final JButton btnOffline = new JButton("Offline");
    private final JButton btnEnviarContacto = new JButton("Enviar contacto");

    private Contact contactoSeleccionado = null;

    public MainChatUI(String myUserId, String myName) {
        super("ChatUPB");
        this.myUserId = myUserId;

        if (myName == null) {
            throw new IllegalArgumentException("myName no puede ser null");
        }

        String cleaned = myName.trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("myName no puede estar vacío");
        }

        if (cleaned.length() > 60) {
            cleaned = cleaned.substring(0, 60);
        }

        this.myName = cleaned;

        Mediador.getInstance().setLocalUser(myUserId);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        construirUI();
        conectarEventos();

        MessageController msgController = new MessageController(this, myUserId);
        setMessageController(msgController);

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

        JPanel panelTop = new JPanel(new GridLayout(1, 2, 8, 8));
        panelTop.add(btnAddContacto);
        panelTop.add(btnConectar);
        panelIzq.add(panelTop, BorderLayout.NORTH);

        listaContactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaContactos.setCellRenderer(new ContactRenderer());
        panelIzq.add(new JScrollPane(listaContactos), BorderLayout.CENTER);

        JPanel panelDer = new JPanel(new BorderLayout(8, 8));
        panelDer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelDer.setBackground(COLOR_CHAT_BACKGROUND);

        JPanel panelHeader = new JPanel(new BorderLayout(8, 8));
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        lblNombreContacto.setFont(lblNombreContacto.getFont().deriveFont(Font.BOLD, 16f));
        panelHeader.add(lblNombreContacto, BorderLayout.WEST);
        panelHeader.add(btnEnviarContacto, BorderLayout.CENTER);
        panelHeader.add(btnOffline, BorderLayout.EAST);
        panelDer.add(panelHeader, BorderLayout.NORTH);

        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBackground(COLOR_CHAT_BACKGROUND);
        panelMensajes.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        scrollMensajes.setBorder(null);
        scrollMensajes.getVerticalScrollBar().setUnitIncrement(16);
        scrollMensajes.getViewport().setBackground(COLOR_CHAT_BACKGROUND);
        panelDer.add(scrollMensajes, BorderLayout.CENTER);

        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtMensaje.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        txtMensaje.setOpaque(false);

        JScrollPane scrollInput = new JScrollPane(txtMensaje);
        scrollInput.setBorder(null);
        scrollInput.setOpaque(false);
        scrollInput.getViewport().setOpaque(false);
        scrollInput.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollInput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollInput.setPreferredSize(new Dimension(100, 46));

        RoundedPanel cajaMensaje = new RoundedPanel(24);
        cajaMensaje.setLayout(new BorderLayout());
        cajaMensaje.setBackground(Color.WHITE);
        cajaMensaje.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        cajaMensaje.add(scrollInput, BorderLayout.CENTER);

        btnEnviar.setPreferredSize(new Dimension(46, 46));

        JPanel panelEnviar = new JPanel(new BorderLayout(10, 0));
        panelEnviar.setBackground(COLOR_HEADER);
        panelEnviar.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        panelEnviar.add(cajaMensaje, BorderLayout.CENTER);
        panelEnviar.add(btnEnviar, BorderLayout.EAST);
        panelDer.add(panelEnviar, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, panelDer);
        split.setDividerLocation(280);
        getContentPane().add(split);
    }

    private void conectarEventos() {
        btnAddContacto.addActionListener(e -> abrirUIAgregarContacto());
        btnConectar.addActionListener(e -> conectarPorHello());

        listaContactos.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            contactoSeleccionado = listaContactos.getSelectedValue();

            if (contactoSeleccionado == null) {
                lblNombreContacto.setText("Nombre del contacto");
                limpiarPanelMensajes();
                return;
            }

            lblNombreContacto.setText(contactoSeleccionado.getName());
            if (messageController != null) {
                messageController.onOpenConversation(contactoSeleccionado);
            } else {
                limpiarPanelMensajes();
            }
        });

        btnEnviar.addActionListener(e -> enviarMensaje());
        configurarAtajosDeTeclado();
        btnOffline.addActionListener(e -> mandarOffline());
        btnEnviarContacto.addActionListener(e -> enviarContacto());
    }

    private void configurarAtajosDeTeclado() {
        InputMap inputMap = txtMensaje.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = txtMensaje.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "enviarMensaje");
        inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "saltoLinea");

        actionMap.put("enviarMensaje", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                enviarMensaje();
            }
        });

        actionMap.put("saltoLinea", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                txtMensaje.append("\n");
            }
        });
    }

    private void conectarPorHello() {
        if (contactoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero selecciona un contacto.");
            return;
        }

        System.out.println("Solicitando HELLO a: " + contactoSeleccionado.getCode());
        Mediador.getInstance().checkPresence(contactoSeleccionado.getCode());
    }

    private void cargarContactosDesdeDB() {
        contactosModel.clear();
        try {
            List<Contact> contactos = contactDao.findAll();
            for (Contact c : contactos) {
                c.setStateConnect(false);
                contactosModel.addElement(c);
            }
        } catch (SQLException e) {
            System.err.println("No se pudo cargar contactos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enviarContacto() {
        if (contactoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "Primero selecciona el amigo al que le vas a pasar el contacto.");
            return;
        }

        List<Contact> contactosParaCompartir = new ArrayList<>();
        for (int i = 0; i < contactosModel.size(); i++) {
            Contact c = contactosModel.get(i);
            if (!c.getCode().equals(contactoSeleccionado.getCode())) {
                contactosParaCompartir.add(c);
            }
        }

        if (contactosParaCompartir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tienes otro contacto para compartir.");
            return;
        }

        String[] opciones = new String[contactosParaCompartir.size()];
        for (int i = 0; i < contactosParaCompartir.size(); i++) {
            Contact c = contactosParaCompartir.get(i);
            opciones[i] = c.getName();
        }

        String opcionElegida = (String) JOptionPane.showInputDialog(
                this,
                "Selecciona el contacto que quieres compartir:",
                "Pasar contacto",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (opcionElegida == null) {
            return;
        }

        Contact contactoACompartir = null;
        for (int i = 0; i < contactosParaCompartir.size(); i++) {
            if (opciones[i].equals(opcionElegida)) {
                contactoACompartir = contactosParaCompartir.get(i);
                break;
            }
        }

        if (contactoACompartir == null) {
            return;
        }

        edu.upb.chatupb_v2.model.entities.message.Message compartir = new EnviarContacto(
                contactoACompartir.getCode(),
                contactoACompartir.getName(),
                contactoACompartir.getIp() == null ? "" : contactoACompartir.getIp()
        );

        Mediador.getInstance().sendMessage(contactoSeleccionado.getCode(), compartir);
        JOptionPane.showMessageDialog(this, "Contacto enviado correctamente.");
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

        edu.upb.chatupb_v2.model.entities.message.Message offline = new Offline(myUserId);
        Mediador.getInstance().sendMessage(contactoSeleccionado.getCode(), offline);
    }

    private void enviarMensaje() {
        String textoOriginal = txtMensaje.getText();
        if (textoOriginal == null) return;

        String texto = textoOriginal.trim();
        if (texto.isEmpty()) return;

        if (contactoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero selecciona un contacto.");
            return;
        }

        String textoProcesado = textAnalyzer.analyze(texto);

        txtMensaje.setText("");
        txtMensaje.requestFocusInWindow();

        String messageId = UUID.randomUUID().toString();

        if (messageController != null) {
            messageController.onOutgoingMessage(contactoSeleccionado, messageId, textoProcesado);
            messageController.onOpenConversation(contactoSeleccionado);
        }

        edu.upb.chatupb_v2.model.entities.message.Message chat =
                new Chat(myUserId, messageId, textoProcesado);
        Mediador.getInstance().sendMessage(contactoSeleccionado.getCode(), chat);
    }

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
        if (contactController == null) {
            return Contact.builder()
                    .code(code)
                    .name(name)
                    .ip(ip)
                    .stateConnect(false)
                    .build();
        }
        return contactController.saveContact(code, name, ip);
    }

    private void handleCommon(edu.upb.chatupb_v2.model.entities.message.Message message) {
        if (message instanceof Offline) {
            Offline off = (Offline) message;

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

    private void helpWithMessages(edu.upb.chatupb_v2.model.entities.message.Message message) {
        if (message instanceof Invitacion) {
            Invitacion inv = (Invitacion) message;

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "Invitación de: " + inv.getNombre(),
                    "Invitación",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                String ip = Mediador.getInstance().getIp(inv.getIdUsuario());
                Contact contact = persistContact(inv.getIdUsuario(), inv.getNombre(), ip);
                contact.setStateConnect(true);
                upsertContactoEnUI(contact);

                edu.upb.chatupb_v2.model.entities.message.Message aceptar =
                        new Aceptar(myUserId, myName);
                Mediador.getInstance().sendMessage(inv.getIdUsuario(), aceptar);
            } else {
                Mediador.getInstance().rejectInvitation(inv.getIdUsuario());
            }
            return;
        }

        if (message instanceof Aceptar) {
            Aceptar ac = (Aceptar) message;

            String ip = Mediador.getInstance().getIp(ac.getIdUsuario());
            Contact contact = persistContact(ac.getIdUsuario(), ac.getNombre(), ip);
            contact.setStateConnect(true);
            upsertContactoEnUI(contact);

            JOptionPane.showMessageDialog(this, ac.getNombre() + " aceptó tu invitación.");

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
            }

            if (contactoSeleccionado != null
                    && contactoSeleccionado.getCode().equals(chat.getIdUsuario())) {
                if (messageController != null) {
                    messageController.onOpenConversation(contactoSeleccionado);
                }
            }

            String ip = Mediador.getInstance().getIp(chat.getIdUsuario());

            String nameToUse = chat.getIdUsuario();
            try {
                Contact existing = contactDao.findByCode(chat.getIdUsuario());
                if (existing != null
                        && existing.getName() != null
                        && !existing.getName().isBlank()) {
                    nameToUse = existing.getName();
                }
            } catch (Exception ignored) {
            }

            Contact contact = persistContact(chat.getIdUsuario(), nameToUse, ip);
            contact.setStateConnect(true);
            upsertContactoEnUI(contact);
            return;
        }

        if (message instanceof EnviarContacto enviarContacto) {
            Contact contact = persistContact(
                    enviarContacto.getIdUsuario(),
                    enviarContacto.getNombre(),
                    enviarContacto.getIp()
            );
            upsertContactoEnUI(contact);
            JOptionPane.showMessageDialog(this,
                    "Recibiste el contacto de: " + contact.getName());
            return;
        }

        handleCommon(message);
    }

    private void limpiarPanelMensajes() {
        panelMensajes.removeAll();
        panelMensajes.revalidate();
        panelMensajes.repaint();
    }

    private void renderConversation(List<edu.upb.chatupb_v2.model.entities.Message> messages) {
        panelMensajes.removeAll();

        for (edu.upb.chatupb_v2.model.entities.Message message : messages) {
            boolean isMine = myUserId.equals(message.getSenderCode());
            panelMensajes.add(createMessageRow(message, isMine));
            panelMensajes.add(Box.createVerticalStrut(8));
        }

        panelMensajes.revalidate();
        panelMensajes.repaint();
        scrollToBottom();
    }

    private JPanel createMessageRow(
            edu.upb.chatupb_v2.model.entities.Message message,
            boolean isMine
    ) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel bubble = new RoundedPanel(18);
        bubble.setLayout(new BorderLayout(0, 6));
        bubble.setBackground(isMine ? COLOR_OUTGOING : COLOR_INCOMING);
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));

        JLabel lblMessage = new JLabel(buildMessageHtml(message.getMessage()));
        lblMessage.setFont(new Font("SansSerif", Font.PLAIN, 14));
        bubble.add(lblMessage, BorderLayout.CENTER);

        JLabel lblHour = new JLabel(formatHour(message.getCreatedDate()));
        lblHour.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblHour.setForeground(new Color(110, 110, 110));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(lblHour);
        bubble.add(footer, BorderLayout.SOUTH);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(bubble, isMine ? BorderLayout.EAST : BorderLayout.WEST);

        if (isMine) {
            row.add(holder, BorderLayout.EAST);
        } else {
            row.add(holder, BorderLayout.WEST);
        }

        return row;
    }

    private String buildMessageHtml(String text) {
        String originalText = text == null ? "" : text;
        int estimatedWidth = Math.min(
                260,
                Math.max(80, originalText.replace("\n", " ").length() * 7)
        );
        String safeText = escapeHtml(originalText).replace("\n", "<br>");

        return "<html><body style='width: "
                + estimatedWidth
                + "px; font-family: sans-serif;'>"
                + safeText
                + "</body></html>";
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String formatHour(String createdDate) {
        if (createdDate == null || createdDate.isBlank()) {
            return "";
        }

        try {
            LocalDateTime parsedDate = LocalDateTime.parse(createdDate, DATABASE_FORMAT);
            return parsedDate.format(HOUR_FORMAT);
        } catch (DateTimeParseException e) {
            return createdDate;
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar scrollBar = scrollMensajes.getVerticalScrollBar();
            scrollBar.setValue(scrollBar.getMaximum());
        });
    }

    @Override
    public void setContactController(ContactController contactController) {
        this.contactController = contactController;
    }

    @Override
    public void onLoad(List<Contact> contactsList) {
        SwingUtilities.invokeLater(() -> {
            contactosModel.clear();

            for (Contact c : contactsList) {
                c.setStateConnect(false);
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
    public void onChatHistoryLoaded(String contactCode, List<Message> messages) {
        if (contactoSeleccionado != null
                && contactoSeleccionado.getCode().equals(contactCode)) {
            SwingUtilities.invokeLater(() -> renderConversation(messages));
        }
    }

    @Override
    public void onSocketMessage(edu.upb.chatupb_v2.model.entities.message.Message message) {
        helpWithMessages(message);
    }

    @Override
    public void onContactStatusChanged(String contactCode, boolean online) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < contactosModel.size(); i++) {
                Contact c = contactosModel.get(i);
                if (c.getCode().equals(contactCode)) {
                    c.setStateConnect(online);
                    listaContactos.repaint();
                    return;
                }
            }

            try {
                Contact fromDb = contactDao.findByCode(contactCode);
                if (fromDb != null) {
                    fromDb.setStateConnect(online);
                    contactosModel.addElement(fromDb);
                    listaContactos.repaint();
                }
            } catch (Exception ignored) {
            }
        });
    }

}
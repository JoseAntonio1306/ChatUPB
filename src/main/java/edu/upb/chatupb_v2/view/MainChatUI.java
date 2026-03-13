package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.AnalizadorController;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.model.dao.ContactDao;
import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.message.*;
import edu.upb.chatupb_v2.model.server.Mediador;
import edu.upb.chatupb_v2.view.components.AccentButton;
import edu.upb.chatupb_v2.view.components.SurfacePanel;

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

    private static final Color COLOR_CHAT_BACKGROUND = new Color(245, 247, 250);
    private static final Color COLOR_OUTGOING = new Color(239, 244, 255);
    private static final Color COLOR_INCOMING = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(229, 231, 235);
    private static final Color COLOR_META = new Color(107, 114, 128);
    private static final Color COLOR_APP_BACKGROUND = new Color(243, 246, 251);
    private static final Color COLOR_HEADER = new Color(30, 41, 59);
    private static final Color COLOR_HEADER_TEXT = Color.WHITE;
    private static final Color COLOR_STATUS_ONLINE = new Color(34, 197, 94);
    private static final Color COLOR_STATUS_OFFLINE = new Color(239, 68, 68);
    private static final Color COLOR_STATUS_NEUTRAL = new Color(148, 163, 184);


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
    private final JLabel lblEstadoContacto = new JLabel("● Sin seleccionar");
    private final JPanel panelMensajes = new JPanel();
    private final JScrollPane scrollMensajes = new JScrollPane(panelMensajes);

    private final JTextArea txtMensaje = new JTextArea(1, 20);
    private JScrollPane scrollInput;
    private SurfacePanel composerBox;
    private JPanel panelEnviar;

    private static final int COMPOSER_MIN_HEIGHT = 48;
    private static final int COMPOSER_MAX_HEIGHT = 110;
    private final JButton btnEnviar = new AccentButton("Enviar");
    private final JButton btnImagen = new JButton("Foto");

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
        stylizeTopButton(btnAddContacto);
        stylizeTopButton(btnConectar);
        btnAddContacto.setPreferredSize(new Dimension(0, 38));
        btnConectar.setPreferredSize(new Dimension(0, 38));

        JPanel panelIzq = new JPanel(new BorderLayout(8, 8));
        panelIzq.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelTop = new JPanel(new GridLayout(1, 2, 8, 8));
        panelTop.add(btnAddContacto);
        panelTop.add(btnConectar);
        panelIzq.add(panelTop, BorderLayout.NORTH);

        panelIzq.setBackground(COLOR_APP_BACKGROUND);
        panelTop.setBackground(COLOR_APP_BACKGROUND);
        listaContactos.setBackground(Color.WHITE);

        listaContactos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaContactos.setCellRenderer(new ContactRenderer());
        listaContactos.setFixedCellHeight(42);
        listaContactos.setSelectionBackground(new Color(224, 231, 255));
        listaContactos.setSelectionForeground(new Color(30, 41, 59));
        listaContactos.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        JScrollPane scrollContactos = new JScrollPane(listaContactos);
        scrollContactos.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        panelIzq.add(scrollContactos, BorderLayout.CENTER);

        JPanel panelDer = new JPanel(new BorderLayout(8, 8));
        panelDer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelDer.setBackground(COLOR_CHAT_BACKGROUND);

        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(COLOR_HEADER);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        lblNombreContacto.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblNombreContacto.setForeground(COLOR_HEADER_TEXT);

        lblEstadoContacto.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblEstadoContacto.setForeground(COLOR_STATUS_NEUTRAL);

        JPanel panelTitulo = new JPanel();
        panelTitulo.setOpaque(false);
        panelTitulo.setLayout(new BoxLayout(panelTitulo, BoxLayout.Y_AXIS));
        panelTitulo.add(lblNombreContacto);
        panelTitulo.add(Box.createVerticalStrut(2));
        panelTitulo.add(lblEstadoContacto);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelAcciones.setOpaque(false);

        styleHeaderActionButton(btnEnviarContacto, 150, 34);
        styleHeaderActionButton(btnOffline, 90, 34);

        panelAcciones.add(btnEnviarContacto);
        panelAcciones.add(btnOffline);

        panelHeader.add(panelTitulo, BorderLayout.WEST);
        panelHeader.add(panelAcciones, BorderLayout.EAST);

        panelDer.add(panelHeader, BorderLayout.NORTH);

        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBackground(COLOR_CHAT_BACKGROUND);
        panelMensajes.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        panelMensajes.setAlignmentY(Component.TOP_ALIGNMENT);

        scrollMensajes.setBorder(null);
        scrollMensajes.getViewport().setBackground(COLOR_CHAT_BACKGROUND);
        scrollMensajes.getVerticalScrollBar().setUnitIncrement(16);
        panelDer.add(scrollMensajes, BorderLayout.CENTER);

        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setRows(1);
        txtMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtMensaje.setBorder(null);
        txtMensaje.setMargin(new Insets(8, 0, 8, 0));
        txtMensaje.setOpaque(false);
        txtMensaje.setForeground(new Color(30, 41, 59));
        txtMensaje.setCaretColor(new Color(30, 41, 59));

        scrollInput = new JScrollPane(txtMensaje);
        scrollInput.setBorder(null);
        scrollInput.setViewportBorder(null);
        scrollInput.setOpaque(false);
        scrollInput.getViewport().setOpaque(false);
        scrollInput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollInput.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollInput.getVerticalScrollBar().setUnitIncrement(12);
        scrollInput.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollInput.setPreferredSize(new Dimension(100, 32));

        styleComposerSecondaryButton(btnImagen);
        btnImagen.setPreferredSize(new Dimension(52, 38));
        btnImagen.setToolTipText("Enviar imagen");

        composerBox = new SurfacePanel(
                16,
                Color.WHITE,
                COLOR_BORDER
        );
        composerBox.setLayout(new GridBagLayout());
        composerBox.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        composerBox.setPreferredSize(new Dimension(100, COMPOSER_MIN_HEIGHT));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;

// botón foto
        c.gridx = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        composerBox.add(btnImagen, c);

// espacio
        c.gridx = 1;
        composerBox.add(Box.createHorizontalStrut(6), c);

// separador
        JPanel separator = new JPanel();
        separator.setBackground(COLOR_BORDER);
        separator.setPreferredSize(new Dimension(1, 22));
        separator.setMinimumSize(new Dimension(1, 22));
        separator.setMaximumSize(new Dimension(1, 22));

        c.gridx = 2;
        composerBox.add(separator, c);

// espacio
        c.gridx = 3;
        composerBox.add(Box.createHorizontalStrut(8), c);

// input
        c.gridx = 4;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        composerBox.add(scrollInput, c);

        btnEnviar.setPreferredSize(new Dimension(96, 42));

        Color topNormal = new Color(51, 65, 85);
        Color topHover = new Color(71, 85, 105);
        Color topPressed = new Color(30, 41, 59);

        addHoverEffect(btnAddContacto, topNormal, topHover, topPressed);
        addHoverEffect(btnConectar, topNormal, topHover, topPressed);
        addHoverEffect(btnEnviarContacto, topNormal, topHover, topPressed);
        addHoverEffect(btnOffline, topNormal, topHover, topPressed);

        panelEnviar = new JPanel(new BorderLayout(10, 0));
        panelEnviar.setBackground(COLOR_CHAT_BACKGROUND);
        panelEnviar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelEnviar.add(composerBox, BorderLayout.CENTER);
        panelEnviar.add(btnEnviar, BorderLayout.EAST);

        panelDer.add(panelEnviar, BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, panelDer);
        split.setDividerLocation(280);
        split.setBorder(null);
        split.setContinuousLayout(true);
        getContentPane().add(split);
        actualizarEstadoComposer(false);

        instalarAutoResizeComposer();
        actualizarAlturaComposer();
    }

    private void conectarEventos() {
        btnAddContacto.addActionListener(e -> abrirUIAgregarContacto());
        btnConectar.addActionListener(e -> conectarPorHello());

        listaContactos.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            contactoSeleccionado = listaContactos.getSelectedValue();

            if (contactoSeleccionado == null) {
                actualizarEstadoHeader(null);
                actualizarEstadoComposer(false);
                limpiarPanelMensajes();
                return;
            }

            actualizarEstadoHeader(contactoSeleccionado);
            actualizarEstadoComposer(true);


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
        btnImagen.addActionListener(e -> enviarImagen());
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
                txtMensaje.insert("\n", txtMensaje.getCaretPosition());
                actualizarAlturaComposer();
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

    private void enviarImagen() {
        if (contactoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Primero selecciona un contacto.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            java.io.File file = fileChooser.getSelectedFile();
            byte[] imageBytes = java.nio.file.Files.readAllBytes(file.toPath());

            String messageId = UUID.randomUUID().toString();

            if (messageController != null) {
                messageController.onOutgoingImage(contactoSeleccionado, messageId, imageBytes);
                messageController.onOpenConversation(contactoSeleccionado);
            }

            AbstractMessage enviarImagen = new EnviarImagen(
                    myUserId,
                    messageId,
                    imageBytes
            );

            Mediador.getInstance().sendMessage(contactoSeleccionado.getCode(), enviarImagen);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo enviar la imagen: " + e.getMessage());
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

        AbstractMessage compartir = new EnviarContacto(
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

        AbstractMessage offline = new Offline(myUserId);
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
        actualizarAlturaComposer();
        txtMensaje.requestFocusInWindow();

        String messageId = UUID.randomUUID().toString();

        if (messageController != null) {
            messageController.onOutgoingMessage(contactoSeleccionado, messageId, textoProcesado);
            messageController.onOpenConversation(contactoSeleccionado);
        }

        AbstractMessage chat =
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

    private void handleCommon(AbstractMessage message) {
        if (message instanceof Offline) {
            Offline off = (Offline) message;

            for (int i = 0; i < contactosModel.size(); i++) {
                Contact c = contactosModel.get(i);
                if (c.getCode().equals(off.getIdUsuario())) {
                    c.setStateConnect(false);
                    listaContactos.repaint();

                    if (contactoSeleccionado != null
                            && contactoSeleccionado.getCode().equals(c.getCode())) {
                        contactoSeleccionado.setStateConnect(false);
                        actualizarEstadoHeader(contactoSeleccionado);
                    }
                    break;
                }
            }

            JOptionPane.showMessageDialog(this, "Offline.");
        }
    }

    private void helpWithMessages(AbstractMessage message) {
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
                if (contactoSeleccionado != null && contactoSeleccionado.getCode().equals(contact.getCode())) {
                    contactoSeleccionado.setStateConnect(true);
                    actualizarEstadoHeader(contactoSeleccionado);
                }

                AbstractMessage aceptar = new Aceptar(myUserId, myName);
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
            if (contactoSeleccionado != null
                    && contactoSeleccionado.getCode().equals(contact.getCode())) {
                contactoSeleccionado.setStateConnect(true);
                actualizarEstadoHeader(contactoSeleccionado);
            }

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
            if (contactoSeleccionado != null
                    && contactoSeleccionado.getCode().equals(contact.getCode())) {
                contactoSeleccionado.setStateConnect(true);
                actualizarEstadoHeader(contactoSeleccionado);
            }
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

        if (message instanceof EnviarImagen enviarImagen) {

            if (messageController != null) {
                messageController.onIncomingImage(enviarImagen);
            }

            if (contactoSeleccionado != null
                    && contactoSeleccionado.getCode().equals(enviarImagen.getIdUsuario())) {
                if (messageController != null) {
                    messageController.onOpenConversation(contactoSeleccionado);
                }
            }

            String ip = Mediador.getInstance().getIp(enviarImagen.getIdUsuario());

            String nameToUse = enviarImagen.getIdUsuario();
            try {
                Contact existing = contactDao.findByCode(enviarImagen.getIdUsuario());
                if (existing != null
                        && existing.getName() != null
                        && !existing.getName().isBlank()) {
                    nameToUse = existing.getName();
                }
            } catch (Exception ignored) {
            }

            Contact contact = persistContact(enviarImagen.getIdUsuario(), nameToUse, ip);
            contact.setStateConnect(true);
            upsertContactoEnUI(contact);

            if (contactoSeleccionado != null
                    && contactoSeleccionado.getCode().equals(contact.getCode())) {
                contactoSeleccionado.setStateConnect(true);
                actualizarEstadoHeader(contactoSeleccionado);
            }
            return;
        }

        handleCommon(message);
    }

    private void limpiarPanelMensajes() {
        panelMensajes.removeAll();
        panelMensajes.revalidate();
        panelMensajes.repaint();
    }

    private void renderConversation(List<Message> messages) {
        panelMensajes.removeAll();

        for (Message message : messages) {
            boolean isMine = myUserId.equals(message.getSenderCode());
            panelMensajes.add(createMessageRow(message, isMine));
            panelMensajes.add(Box.createVerticalStrut(8));
        }

        panelMensajes.add(Box.createVerticalGlue());

        panelMensajes.revalidate();
        panelMensajes.repaint();
        scrollToBottom();
    }

    private JPanel createMessageRow(Message message, boolean isMine) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.setBorder(BorderFactory.createEmptyBorder(
                4,
                isMine ? 110 : 8,
                4,
                isMine ? 8 : 110
        ));

        SurfacePanel bubble = new SurfacePanel(
                16,
                isMine ? COLOR_OUTGOING : COLOR_INCOMING,
                COLOR_BORDER
        );
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(BorderFactory.createEmptyBorder(8, 12, 6, 12));

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setOpaque(false);

        if ("IMAGE".equalsIgnoreCase(message.getType())) {
            JLabel lblImage = buildImageLabel(message.getMessage());
            content.add(lblImage, BorderLayout.CENTER);
        } else {
            JLabel lblMessage = new JLabel(buildMessageHtml(message.getMessage()));
            lblMessage.setFont(new Font("SansSerif", Font.PLAIN, 14));
            content.add(lblMessage, BorderLayout.CENTER);
        }

        JLabel lblHour = new JLabel(formatHour(message.getCreatedDate()));
        lblHour.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblHour.setForeground(COLOR_META);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(lblHour);
        content.add(footer, BorderLayout.SOUTH);

        bubble.add(content, BorderLayout.CENTER);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);

        if (isMine) {
            holder.add(bubble, BorderLayout.EAST);
            row.add(holder, BorderLayout.EAST);
        } else {
            holder.add(bubble, BorderLayout.WEST);
            row.add(holder, BorderLayout.WEST);
        }

        Dimension preferred = row.getPreferredSize();
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));

        return row;
    }

    private JLabel buildImageLabel(String base64) {
        JLabel label = new JLabel();

        try {
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(imageBytes);
            java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(bis);

            if (bufferedImage != null) {
                int maxWidth = 220;
                int maxHeight = 220;

                int originalWidth = bufferedImage.getWidth();
                int originalHeight = bufferedImage.getHeight();

                double scale = Math.min(
                        (double) maxWidth / originalWidth,
                        (double) maxHeight / originalHeight
                );

                scale = Math.min(scale, 1.0);

                int newWidth = (int) (originalWidth * scale);
                int newHeight = (int) (originalHeight * scale);

                Image scaled = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
            } else {
                label.setText("[Imagen no disponible]");
            }
        } catch (Exception e) {
            label.setText("[Imagen inválida]");
        }

        return label;
    }

    private String buildMessageHtml(String text) {
        String originalText = text == null ? "" : text;
        int estimatedWidth = Math.min(
                300,
                Math.max(110, originalText.replace("\n", " ").length() * 7)
        );
        String safeText = escapeHtml(originalText).replace("\n", "<br>");

        return "<html><body style='width: "
                + estimatedWidth
                + "px; font-family: sans-serif; color: #111827;'>"
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
    public void onSocketMessage(AbstractMessage message) {
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

                    if (contactoSeleccionado != null
                            && contactoSeleccionado.getCode().equals(contactCode)) {
                        contactoSeleccionado.setStateConnect(online);
                        actualizarEstadoHeader(contactoSeleccionado);
                    }
                    return;
                }
            }

            try {
                Contact fromDb = contactDao.findByCode(contactCode);
                if (fromDb != null) {
                    fromDb.setStateConnect(online);
                    contactosModel.addElement(fromDb);
                    listaContactos.repaint();

                    if (contactoSeleccionado != null
                            && contactoSeleccionado.getCode().equals(contactCode)) {
                        contactoSeleccionado = fromDb;
                        actualizarEstadoHeader(contactoSeleccionado);
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void stylizeTopButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(51, 65, 85));
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    private void styleComposerSecondaryButton(JButton button) {
        Color normalText = new Color(51, 65, 85);
        Color hoverText = new Color(37, 99, 235);
        Color hoverBg = new Color(241, 245, 249);
        Color disabledText = new Color(148, 163, 184);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(normalText);
        button.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setOpaque(true);
                    button.setBackground(hoverBg);
                    button.setForeground(hoverText);
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setOpaque(false);
                    button.setForeground(normalText);
                    button.repaint();
                }
            }
        });

        button.addChangeListener(e -> {
            if (!button.isEnabled()) {
                button.setOpaque(false);
                button.setForeground(disabledText);
            } else if (!button.getModel().isRollover()) {
                button.setForeground(normalText);
            }
        });
    }

    private void styleHeaderActionButton(JButton button, int width, int height) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(51, 65, 85));
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height));
    }

    private void actualizarEstadoHeader(Contact contact) {
        if (contact == null) {
            lblNombreContacto.setText("Nombre del contacto");
            lblEstadoContacto.setText("● Sin seleccionar");
            lblEstadoContacto.setForeground(COLOR_STATUS_NEUTRAL);
            return;
        }

        lblNombreContacto.setText(contact.getName());

        if (contact.isStateConnect()) {
            lblEstadoContacto.setText("● En línea");
            lblEstadoContacto.setForeground(COLOR_STATUS_ONLINE);
        } else {
            lblEstadoContacto.setText("● Desconectado");
            lblEstadoContacto.setForeground(COLOR_STATUS_OFFLINE);
        }
    }
    private void actualizarEstadoComposer(boolean enabled) {
        txtMensaje.setEnabled(enabled);
        btnEnviar.setEnabled(enabled);
        btnImagen.setEnabled(enabled);

        txtMensaje.setForeground(enabled ? new Color(30, 41, 59) : new Color(148, 163, 184));

        btnImagen.repaint();
        btnEnviar.repaint();
        txtMensaje.repaint();
    }

    private void addHoverEffect(JButton button, Color normal, Color hover, Color pressed) {
        button.setBackground(normal);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(normal);
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(pressed);
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    if (button.contains(e.getPoint())) {
                        button.setBackground(hover);
                    } else {
                        button.setBackground(normal);
                    }
                }
            }
        });
    }
    private void instalarAutoResizeComposer() {
        txtMensaje.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizarAlturaComposer();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizarAlturaComposer();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizarAlturaComposer();
            }
        });
    }

    private void actualizarAlturaComposer() {
        SwingUtilities.invokeLater(() -> {
            int width = txtMensaje.getWidth();
            if (width <= 0) {
                return;
            }

            txtMensaje.setSize(new Dimension(width, Short.MAX_VALUE));
            int preferredTextHeight = txtMensaje.getPreferredSize().height;

            int targetTextHeight = Math.max(
                    28,
                    Math.min(COMPOSER_MAX_HEIGHT - 16, preferredTextHeight)
            );

            scrollInput.setPreferredSize(new Dimension(scrollInput.getWidth(), targetTextHeight));
            composerBox.setPreferredSize(new Dimension(composerBox.getWidth(), targetTextHeight + 16));

            composerBox.revalidate();
            composerBox.repaint();
            panelEnviar.revalidate();
            panelEnviar.repaint();
        });
    }
}
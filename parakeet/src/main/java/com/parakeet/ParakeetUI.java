package com.parakeet;
import java.awt.*;
import javax.swing.*;
import org.bson.Document;
import com.mongodb.client.model.Sorts;

public class ParakeetUI extends JFrame {

    private final ParakeetBackend.Session session;
    private final int userID = 78;

    // ui components
    private final JPanel messagePanel;
    private final JScrollPane scrollPane;
    private final JTextField input;
    private final JButton sendButton;

    private int nextMessageY = 10;

    public ParakeetUI() {

        // connects to database
        session = ParakeetBackend.Session.getSession();

        // settings for the window of app
        setTitle("Parakeet");
        setSize(500,500);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // creates panel for messages to be in
        messagePanel = new JPanel(null);
        messagePanel.setBackground(new Color(227, 229, 232));
        messagePanel.setPreferredSize(new Dimension(480, 350));

        // allows to scroll panel
        scrollPane = new JScrollPane(messagePanel);
        scrollPane.setBounds(0, 0, 486, 415);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);

        // creates the section to type up new messages
        input = new JTextField();
        input.setBounds(0, 413, 385, 50);

        // creates the button to send messages
        sendButton = new JButton("Send!");
        sendButton.setBounds(385, 414, 100, 48);
        
        // adds components to app window
        add(input);
        add(sendButton);
        add(scrollPane);
        
        // calls function that loads messages from MongoDB
        loadMessagesFromDatabase();

        // sends message when the send button is pressed
        sendButton.addActionListener((actionEvent) -> {
            String message = input.getText();

            // stores message in database if there is text in message
            if (!message.isEmpty()) {
                boolean stored = session.sendMessage(userID, message);

                // displays error message if the message wasnt stored and leaves uh oh
                if (!stored) {
                    JOptionPane.showMessageDialog(this, "Error: MEssage could not be stored :(");
                    return;
                }

                // calls function to display after stored
                addMessageToScreen("You", message, true);

                //clears input box and the curso back into it
                input.setText("");
                input.requestFocusInWindow();

            }
        });

        //pressing enter clicks send button
        input.addActionListener(event -> sendButton.doClick());

        // puts cursor back into input box
        SwingUtilities.invokeLater(() -> {
            input.requestFocusInWindow();
        });
    }

    // loads past messages from mongodb
    private void loadMessagesFromDatabase() {
        // gets rid of messages that have already been displayed
        messagePanel.removeAll();
        nextMessageY = 10;

        // fetches messages starting from oldest from database
        for (Document document : session.getMessagesDB()
            .find()
            .sort(Sorts.ascending("_id"))) {
                int senderID = document.getInteger("uID");
                String message = document.getString("msg");

                // checks if current user did send the message
                boolean sentByCurrentUser = senderID == userID;

                String senderName;

                // assigns name to sender of message
                if (sentByCurrentUser) {
                    senderName = "You";
                } else {
                    senderName = "User" + senderID;
                }

                // displays message that was stored
                addMessageToScreen(senderName, message, sentByCurrentUser);
            }

            // moves scrollbar to bottom as newer messages come in
            SwingUtilities.invokeLater(() -> {
                JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            });
    }

    //creates and display a message box
    private void addMessageToScreen(String sender, String message, boolean sentByCurrentUser) {
        int minimumWidth = 200;
        int minimumHeight = 50;
        int maximumWidth = 400;

        // displays name of sender before message contents
        String fullMessage = sender + ": " + message;

        // allows long messages to become multiple lines
        JTextArea messageBox = new JTextArea(fullMessage);
        messageBox.setEditable(false);
        messageBox.setLineWrap(true);
        messageBox.setWrapStyleWord(true);
        messageBox.setForeground(Color.WHITE);

        // assigns blue to messages you send, green to anyone not you
        if (sentByCurrentUser) {
            messageBox.setBackground(new Color(3, 162, 233));
        } else {
            messageBox.setBackground(new Color(20, 186, 23));
        }

        //adds space between text and box
        messageBox.setMargin(new Insets(8, 10, 8, 10));

        // measures the pixel width of text and makes sure the width of message is within limits
        FontMetrics fontMetrics = messageBox.getFontMetrics(messageBox.getFont());
        int textWidth = fontMetrics.stringWidth(fullMessage) + 20;
        int messageWidth = Math.max(minimumWidth, Math.min(maximumWidth, textWidth));

        // calculates height for message box
        messageBox.setSize(messageWidth, Short.MAX_VALUE);

        //stores how much space message box needs
        Dimension preferredSize = messageBox.getPreferredSize();
        int messageHeight = Math.max(minimumHeight, preferredSize.height);
        
        // for calculating position of mbox
        int panelWidth = 475;
        int margin = 25;
        int messageX;

        // messages sent show on right, received on left
        if (sentByCurrentUser) {
            messageX = panelWidth - margin - messageWidth;
        } else {
            messageX= margin;
        }

        // sets positions and size of mbox, adds it to screen
        messageBox.setBounds(messageX, nextMessageY, messageWidth, messageHeight);
        messagePanel.add(messageBox);

        //mpoves next message lower
        nextMessageY += messageHeight + 5;

        //increases height to be scrolled
        messagePanel.setPreferredSize(new Dimension(480, Math.max(350, nextMessageY + 20)));

        // recalculates panel and re-displays
        messagePanel.revalidate();
        messagePanel.repaint();
    }
}

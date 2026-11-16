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

        
        SwingUtilities.invokeLater(() -> {
            input.requestFocusInWindow();
        });
    }

    private void loadMessagesFromDatabase() {
        messagePanel.removeAll();
        nextMessageY = 10;

        for (Document document : session.getMessagesDB()
            .find()
            .sort(Sorts.ascending("_id"))) {
                int senderID = document.getInteger("uID");
                String message = document.getString("msg");

                boolean sentByCurrentUser = senderID == userID;

                String senderName;

                if (sentByCurrentUser) {
                    senderName = "You";
                } else {
                    senderName = "User" + senderID;
                }

                addMessageToScreen(senderName, message, sentByCurrentUser);
            }

            SwingUtilities.invokeLater(() -> {
                JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
                scrollBar.setValue(scrollBar.getMaximum());
            });
    }

    private void addMessageToScreen(String sender, String message, boolean sentByCurrentUser) {
        int minimumWidth = 200;
        int minimumHeight = 50;
        int maximumWidth = 400;

        String fullMessage = sender + ": " + message;

        JTextArea messageBox = new JTextArea(fullMessage);

        messageBox.setEditable(false);
        messageBox.setLineWrap(true);
        messageBox.setWrapStyleWord(true);
        messageBox.setForeground(Color.WHITE);

        if (sentByCurrentUser) {
            messageBox.setBackground(new Color(3, 162, 233));
        } else {
            messageBox.setBackground(new Color(20, 186, 23));
        }

        messageBox.setMargin(new Insets(8, 10, 8, 10));

        FontMetrics fontMetrics = messageBox.getFontMetrics(messageBox.getFont());

        int textWidth = fontMetrics.stringWidth(fullMessage) + 20;
        int messageWidth = Math.max(minimumWidth, Math.min(maximumWidth, textWidth));

        messageBox.setSize(messageWidth, Short.MAX_VALUE);

        Dimension preferredSize = messageBox.getPreferredSize();

        int messageHeight = Math.max(minimumHeight, preferredSize.height);
        int panelWidth = 400;
        int margin = 25;
        int messageX;

        if (sentByCurrentUser) {
            messageX = panelWidth - margin - messageWidth;
        } else {
            messageX= margin;
        }

        messageBox.setBounds(messageX, nextMessageY, messageWidth, messageHeight);
        messagePanel.add(messageBox);

        nextMessageY += messageHeight + 5;

        messagePanel.setPreferredSize(new Dimension(480, Math.max(350, nextMessageY + 20)));

        messagePanel.revalidate();
        messagePanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParakeetUI window = new ParakeetUI();
            window.setVisible(true);
            // Start and display your window here
        });
    }
}
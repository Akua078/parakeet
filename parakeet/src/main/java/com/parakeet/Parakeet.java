package com.parakeet;

import javax.swing.SwingUtilities;

public class Parakeet {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParakeetUI window = new ParakeetUI();
            window.setVisible(true);
            // Start and display your window here
        });
    }
}

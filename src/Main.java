import java.io.IOException;

import javax.swing.SwingUtilities;

import client.GameWindow;
import client.LoginDialog;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Temporary turned off for testing pusposes 
            // new LoginDialog();
            try {
                new GameWindow();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }
}

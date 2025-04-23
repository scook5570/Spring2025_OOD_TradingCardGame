
import java.io.IOException;

import javax.swing.SwingUtilities;

import client.CollectionWindow;
import client.LoginScreen;

public class Main {
    public static void main(String[] args) throws IOException {
        // LoginScreen loginScreen = new LoginScreen();
        // loginScreen.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            new LoginScreen();
        });

    }
}


import java.io.IOException;

import javax.swing.SwingUtilities;

import client.CollectionWindow;
import client.GameWindow;

public class Main {
    public static void main(String[] args) throws IOException {
        // LoginScreen loginScreen = new LoginScreen();
        // loginScreen.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            try {
                GameWindow gameWindow = new GameWindow();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CollectionWindow collectionWindow = new CollectionWindow("Britogears");
            // collectionWindow.setVisible(true);
            // gameWindow.setVisible(true);
        });

    }
}

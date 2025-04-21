

import java.io.IOException;

import client.CollectionWindow;
import client.GameWindow;

public class Main {
    public static void main(String[] args) throws IOException {
        //LoginScreen loginScreen = new LoginScreen();
        //loginScreen.setVisible(true);

        GameWindow gameWindow = new GameWindow();
        CollectionWindow collectionWindow = new CollectionWindow();
        collectionWindow.setVisible(true);
        // gameWindow.setVisible(true);
    }
}

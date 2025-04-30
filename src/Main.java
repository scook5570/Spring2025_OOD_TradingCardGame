import javax.swing.SwingUtilities;

import client.frames.LoginDialog;
import client.frames.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Temporary turned off for testing pusposes
            // new LoginDialog();

            new MainFrame("Britogears");

            // try {
            //     new GameWindow("asfihw k");
            // } catch (IOException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }
        });
    }
}

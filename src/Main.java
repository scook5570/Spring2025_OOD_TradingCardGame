import javax.swing.SwingUtilities;

import client.LoginDialog;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginDialog();
        });
    }
}

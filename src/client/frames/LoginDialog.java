package client.frames;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

import javax.swing.*;

import shared.MessageSocket;
import shared.messages.*;

// A simple dialog for login
public class LoginDialog extends JDialog {
    private final String SERVERADDRESS = "localhost";
    private final int PORT = 5000;
    // Alphanumeric and 5-12 characters
    private final String USERREGEX = "[A-Z,a-z,0-9]{5,12}";
    // At least one digit, one uppercase letter, one special character, and 8+
    // characters
    private final String PASSREGEX = "^(?=.*\\d)(?=.*[A-Z])(?=.*[\\W_]).{8,}$";

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginDialog() {
        setTitle("Login");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setResizable(false);
        // Doesn't naturally stop program so stop when closed
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                System.exit(0);
            }
        });

        // Labels
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        this.messageLabel = new JLabel("");

        // Text fields
        this.usernameField = new JTextField(15);
        this.passwordField = new JPasswordField(15);

        // Focuses on password field when enter user hit [enter] on the username field
        usernameField.addActionListener(e -> {
            passwordField.requestFocusInWindow();
        });

        // Attempts login when enter user hit [enter] on password field
        passwordField.addActionListener(e -> authenticate("Login"));
        // Buttons
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Clear existing layout and constraints
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints loginGBC = new GridBagConstraints();

        // Set insets for spacing
        loginGBC.insets = new Insets(5, 5, 5, 5);

        loginGBC.gridx = 0; // Column 0
        loginGBC.gridy = 0; // Row 0
        loginPanel.add(usernameLabel, loginGBC);

        loginGBC.gridx = 1; // Column 1
        loginGBC.gridy = 0; // Row 0
        loginPanel.add(usernameField, loginGBC);

        loginGBC.gridx = 0; // Column 0
        loginGBC.gridy = 1; // Row 1
        loginPanel.add(passwordLabel, loginGBC);

        loginGBC.gridx = 1; // Column 1
        loginGBC.gridy = 1; // Row 1
        loginPanel.add(passwordField, loginGBC);

        loginGBC.gridx = 0; // Column 0
        loginGBC.gridy = 4; // Row 4
        loginGBC.gridwidth = 2; // Span two columns
        loginPanel.add(messageLabel, loginGBC);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        add(loginPanel);

        // Add loginPanel at (0, 0)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginPanel, gbc);

        // Add buttonPanel just below at (0, 1)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // Button action listener for login
        loginButton.addActionListener(e -> authenticate("Login"));

        // Button action listener for register
        registerButton.addActionListener(e -> authenticate("Register"));

        setVisible(true);
    }

    /**
     * Checks the user credentials for regex requiremnets
     * 
     * @return If the credentiasl meet regex requirements
     */
    private boolean validateCredentials() {
        // Getting credentials
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Check if both username and password are invalid
        if (!username.matches(USERREGEX) && !password.matches(PASSREGEX)) {
            messageLabel.setText("Invalid Credentials");
            return false;
        }

        // Check for valid username
        if (!username.matches(USERREGEX)) {
            messageLabel.setText("Invalid Username");
            return false;
        }

        // Check for valid password
        if (!password.matches(PASSREGEX)) {
            messageLabel.setText("Invalid Password");
            return false;
        }

        // Both valid so return true
        return true;
    }

    /**
     * Attempts to login or register
     * 
     * @param requestType Whether it should "Login" or "Register" the user
     */
    private void authenticate(String requestType) {
        // Getting credentials
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Assure credentials meet regex requirements
        if (!validateCredentials()) {
            return;
        }

        try {
            // Connect to the server
            MessageSocket messageSocket = new MessageSocket(new Socket(SERVERADDRESS, PORT));
            UserCredRequest userCredRequest = new UserCredRequest(requestType, username, password);
            messageLabel.setText("Sending " + requestType + " request...");
            messageSocket.sendMessage(userCredRequest);

            // Receive and process the response
            Message response = messageSocket.getMessage();
            if (response instanceof UserCredResponse) {
                UserCredResponse userCredResponse = (UserCredResponse) response;
                if (userCredResponse.isSuccess() && requestType == "Login") {
                    new MainFrame(username);
                    dispose();
                } else if (userCredResponse.isSuccess()) {
                    messageLabel.setText("Successfully Registered");
                } else {
                    messageLabel.setText(requestType + " Failed");
                }
            } else {
                messageLabel.setText("Unexpected response type: " + response.getType());
            }

            // Close the connection
            messageSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

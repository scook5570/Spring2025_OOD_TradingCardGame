package client;

import javax.swing.*;

import shared.MessageSocket;
import shared.messages.Message;
import shared.messages.UserCredRequest;
import shared.messages.UserCredResponse;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;

public class LoginScreen extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton registerButton;
    private final JLabel usernameLabel;
    private final JLabel passwordLabel;
    private final JLabel messageLabel;
    private final JPanel loginPanel;
    private final JPanel buttonPanel;
    String serverAddress = "localhost";
    int port = 5000;
    // Alphanumeric and 5-12 characters
    String userRegex = "[A-Z,a-z,0-9]{5,12}";
    // At least one digit, one uppercase letter, one special character, 8+
    // characters
    String passRegex = "^(?=.*\\d)(?=.*[A-Z])(?=.*[\\W_]).{8,}$";

    public LoginScreen() {
        setTitle("Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setResizable(false);

        // Labels
        usernameLabel = new JLabel("Username:");
        passwordLabel = new JLabel("Password:");
        messageLabel = new JLabel("");

        // Text fields
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        // Focuses on password field when enter user hit [enter] on the username field
        usernameField.addActionListener(e -> {
            passwordField.requestFocusInWindow();
        });

        // Attempts login when enter user hit [enter] on password field
        passwordField.addActionListener(e -> authenticate("Login"));
        // Buttons
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        // Clear existing layout and constraints
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        loginPanel = new JPanel(new GridBagLayout());
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

        buttonPanel = new JPanel(new GridLayout(1, 2));
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
     * @return If the credentiasl meet regex requirements
     */
    private boolean validateCredentials() {
        // Getting credentials
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Check if both username and password are invalid
        if (!username.matches(userRegex) && !password.matches(passRegex)) {
            messageLabel.setText("Invalid Credentials");
            return false;
        }

        // Check for valid username
        if (!username.matches(userRegex)) {
            messageLabel.setText("Invalid Username");
            return false;
        }

        // Check for valid password
        if (!password.matches(passRegex)) {
            messageLabel.setText("Invalid Password");
            return false;
        }

        // Both valid so return true
        return true;
    }

    /**
     * Attempts to login or register 
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
            MessageSocket messageSocket = new MessageSocket(new Socket(serverAddress, port));
            UserCredRequest userCredRequest = new UserCredRequest(requestType, username, password);
            messageLabel.setText("Sending " + requestType + " request...");
            messageSocket.sendMessage(userCredRequest);

            // Receive and process the response
            Message response = messageSocket.getMessage();
            if (response instanceof UserCredResponse) {
                UserCredResponse userCredResponse = (UserCredResponse) response;
                if (userCredResponse.isSuccess() && requestType == "Login") {
                    new GameWindow();
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

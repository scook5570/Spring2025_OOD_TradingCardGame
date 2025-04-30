package client.frames;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

import javax.swing.*;

import client.utils.TCGUtils;
import shared.MessageSocket;
import shared.messages.*;

/**
 * A dialog window for user authentication (Login/Register).
 */
public class LoginDialog extends JDialog {
    // Regex pattern: Alphanumeric username, 5-12 characters
    private final String USERREGEX = "[A-Z,a-z,0-9]{5,12}";

    // Regex pattern: Password must have at least 1 digit, 1 uppercase letter, 1
    // special character, and minimum 8 characters
    private final String PASSREGEX = "^(?=.*\\d)(?=.*[A-Z])(?=.*[\\W_]).{8,}$";

    // UI components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    /**
     * Constructs the LoginDialog window with all UI components.
     */
    public LoginDialog() {
        setTitle("Login");
        setSize(400, 250);
        setLocationRelativeTo(null); // Center on screen
        setAlwaysOnTop(true);
        setResizable(false);

        // Ensure the entire program exits if this window is closed
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                System.exit(0);
            }
        });

        // Create labels
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        this.messageLabel = new JLabel("");

        // Create text fields
        this.usernameField = new JTextField(15);
        this.passwordField = new JPasswordField(15);

        // Move focus to password field when pressing Enter on username field
        usernameField.addActionListener(e -> {
            passwordField.requestFocusInWindow();
        });

        // Trigger login attempt when pressing Enter in password field
        passwordField.addActionListener(e -> authenticate("Login"));

        // Create buttons
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Set main layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around components

        // Create a panel for the login form (labels and text fields)
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints loginGBC = new GridBagConstraints();
        loginGBC.insets = new Insets(5, 5, 5, 5); // Smaller spacing inside login panel

        // Username label
        loginGBC.gridx = 0;
        loginGBC.gridy = 0;
        loginPanel.add(usernameLabel, loginGBC);

        // Username field
        loginGBC.gridx = 1;
        loginGBC.gridy = 0;
        loginPanel.add(usernameField, loginGBC);

        // Password label
        loginGBC.gridx = 0;
        loginGBC.gridy = 1;
        loginPanel.add(passwordLabel, loginGBC);

        // Password field
        loginGBC.gridx = 1;
        loginGBC.gridy = 1;
        loginPanel.add(passwordField, loginGBC);

        // Message label for status feedback
        loginGBC.gridx = 0;
        loginGBC.gridy = 4;
        loginGBC.gridwidth = 2;
        loginPanel.add(messageLabel, loginGBC);

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Add panels to the dialog window
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // Button listeners
        loginButton.addActionListener(e -> authenticate("Login"));
        registerButton.addActionListener(e -> authenticate("Register"));

        // Make the dialog visible
        setVisible(true);
    }

    /**
     * Validates the entered username and password against regex patterns.
     *
     * @return true if credentials are valid, false otherwise
     */
    private boolean validateCredentials() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Both username and password invalid
        if (!username.matches(USERREGEX) && !password.matches(PASSREGEX)) {
            messageLabel.setText("Invalid Credentials");
            return false;
        }

        // Invalid username
        if (!username.matches(USERREGEX)) {
            messageLabel.setText("Invalid Username");
            return false;
        }

        // Invalid password
        if (!password.matches(PASSREGEX)) {
            messageLabel.setText("Invalid Password");
            return false;
        }

        // Both are valid
        return true;
    }

    /**
     * Handles authentication (Login or Register) by communicating with the server.
     *
     * @param requestType The type of request: "Login" or "Register"
     */
    private void authenticate(String requestType) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Validate credentials before sending to server
        if (!validateCredentials()) {
            return;
        }

        try {
            // Create a connection to the server
            MessageSocket messageSocket = new MessageSocket(new Socket(TCGUtils.SERVERADDRESS, TCGUtils.PORT));
            UserCredRequest userCredRequest = new UserCredRequest(requestType, username, password);

            // Update UI message
            messageLabel.setText("Sending " + requestType + " request...");

            // Send authentication request
            messageSocket.sendMessage(userCredRequest);

            // Wait for and process the server response
            Message response = messageSocket.getMessage();
            if (response instanceof UserCredResponse) {
                UserCredResponse userCredResponse = (UserCredResponse) response;

                if (userCredResponse.isSuccess() && requestType.equals("Login")) {
                    // Successful login: open main application window
                    dispose(); // Close login dialog
                    new MainFrame(username);
                } else if (userCredResponse.isSuccess()) {
                    // Successful registration
                    messageLabel.setText("Successfully Registered");
                } else {
                    // Failed login or registration
                    messageLabel.setText(requestType + " Failed");
                }
            } else {
                messageLabel.setText("Unexpected response type: " + response.getType());
            }

            // Close server connection
            messageSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package client;

import shared.MessageSocket;
import shared.messages.Message;
import shared.messages.UserCredRequest;
import shared.messages.UserCredResponse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

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

    public LoginScreen() {
        setTitle("Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setResizable(false);
        setLayout(new GridBagLayout());

        // Labels
        usernameLabel = new JLabel("Username:");
        passwordLabel = new JLabel("Password:");
        messageLabel = new JLabel("", SwingConstants.CENTER);

        // Text fields
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        loginPanel = new JPanel(new GridBagLayout());
        buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        setupLoginPanel();
        setupButtonPanel();
        layoutComponents();
        setupListeners();
    }

    /**
     * A method that sets up and creates the login panel
     */
    private void setupLoginPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(messageLabel, gbc);
    }

    /**
     * A method to set up and create the button panel
     */
    private void setupButtonPanel() {
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
    }

    /**
     * A method for setting up the layout of the login screen
     */
    private void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginPanel, gbc);

        gbc.gridy = 1;
        add(buttonPanel, gbc);
    }

    /**
     * A method for setting up event listeners for buttons
     */
    private void setupListeners() {
        loginButton.addActionListener(e -> handleLogin());

        registerButton.addActionListener(e -> handleRegister());
    }

    /**
     * A method to handle user login using user credentials request of type "Login", open game window upon login
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in both fields.");
            return;
        }

        new Thread(() -> {
            try (MessageSocket loginSocket = new MessageSocket("localhost", 5000)) {
                UserCredRequest loginRequest = new UserCredRequest("Login", username, password);
                loginSocket.sendMessage(loginRequest);
                Message message = loginSocket.getMessage();

                if (message instanceof UserCredResponse response) {
                    if (response.isSuccess()) {
                        SwingUtilities.invokeLater(() -> {
                            showSuccess("Login successful!");
                            dispose();
                            try {
                                new GameWindow(username).setVisible(true);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> showError("Login failed. Please check your credentials."));
                    }
                } else {
                    SwingUtilities.invokeLater(() -> showError("Unexpected response type from server during login."));
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> showError("Login error: " + ex.getMessage()));
            }
        }).start();
    }

    /**
     * A method to handle user registration using user credential request of type "Register"
     */
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in both fields.");
            return;
        }
        // may have to double check this, was having issues with the UI being blocked when requests were sent out
        new Thread(() -> {
            try (MessageSocket registerSocket = new MessageSocket("localhost", 5000)) {
                UserCredRequest registerRequest = new UserCredRequest("Register", username, password);
                registerSocket.sendMessage(registerRequest);
                Message message = registerSocket.getMessage();

                if (message instanceof UserCredResponse response) {
                    if (response.isSuccess()) {
                        SwingUtilities.invokeLater(() -> showSuccess("Registration successful!"));
                    } else {
                        SwingUtilities.invokeLater(() -> showError("Registration failed. Username may already be taken."));
                    }
                } else {
                    SwingUtilities.invokeLater(() -> showError("Unexpected response type from server during registration."));
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> showError("Registration error: " + ex.getMessage()));
            }
        }).start();
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel messageLabel;

    public LoginScreen() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        // Labels
        usernameLabel = new JLabel("Username:");
        passwordLabel = new JLabel("Password:");
        messageLabel = new JLabel("");

        // Text fields
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        // Buttons
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        // Create a layout manager
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Set insets for spacing
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;  // Column 0
        gbc.gridy = 0;  // Row 0
        add(usernameLabel, gbc);

        gbc.gridx = 1;  // Column 1
        gbc.gridy = 0;  // Row 0
        add(usernameField, gbc);

        gbc.gridx = 0;  // Column 0
        gbc.gridy = 1;  // Row 1
        add(passwordLabel, gbc);

        gbc.gridx = 1;  // Column 1
        gbc.gridy = 1;  // Row 1
        add(passwordField, gbc);

        gbc.gridx = 1;  // Column 1
        gbc.gridy = 2;  // Row 2
        gbc.gridwidth = 1;  // Span one column
        add(loginButton, gbc);

        gbc.gridx = 1;  // Column 1
        gbc.gridy = 3;  // Row 3
        gbc.gridwidth = 1;  // Span one column
        add(registerButton, gbc);

        gbc.gridx = 0;  // Column 0
        gbc.gridy = 4;  // Row 4
        gbc.gridwidth = 2;  // Span two columns
        add(messageLabel, gbc);

        // Button action listener for login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                // TODO: Implement logic to check user credentials from a JSON file or other database
                // Test credentials for now
                if ("user".equals(username) && "password".equals(password)) {
                    messageLabel.setText("Login successful!");
                } else {
                    messageLabel.setText("Invalid credentials!");
                    // TODO: Implement a counter for failed login attempts
                    // TODO: Implement password reset
                }
            }
        });

        // Button action listener for register
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Simple registration logic (can be expanded)
                if (!username.isEmpty() && !password.isEmpty()) {
                    // TODO: Save the new user data (username and password) into a JSON file
                    messageLabel.setText("Registration successful!");
                } else {
                    messageLabel.setText("Please fill in both fields.");
                    // TODO: Add password strength validation here (using regex)
                }
            }
        });
    }
}

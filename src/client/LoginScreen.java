package client;

import javax.smartcardio.Card;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

        // Clear existing layout and constraints
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints loginGBC = new GridBagConstraints();

        // Set insets for spacing
        loginGBC.insets = new Insets(5, 5, 5, 5);

        loginGBC.gridx = 0;  // Column 0
        loginGBC.gridy = 0;  // Row 0
        loginPanel.add(usernameLabel, loginGBC);

        loginGBC.gridx = 1;  // Column 1
        loginGBC.gridy = 0;  // Row 0
        loginPanel.add(usernameField, loginGBC);

        loginGBC.gridx = 0;  // Column 0
        loginGBC.gridy = 1;  // Row 1
        loginPanel.add(passwordLabel, loginGBC);

        loginGBC.gridx = 1;  // Column 1
        loginGBC.gridy = 1;  // Row 1
        loginPanel.add(passwordField, loginGBC);

        loginGBC.gridx = 0;  // Column 0
        loginGBC.gridy = 4;  // Row 4
        loginGBC.gridwidth = 2;  // Span two columns
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

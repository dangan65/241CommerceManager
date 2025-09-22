package ClothingStoreApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class LoginWindow {
    private static final Path USER_FILE = Paths.get("users.txt");
    private final Map<String, String> users = new HashMap<>();

    public LoginWindow() {
        loadUsers();

        JFrame frame = new JFrame("User Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(340, 180);
        frame.setLayout(new GridLayout(3, 2, 8, 8));

        JLabel userLabel = new JLabel("Username:");
        JTextField userText = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passText = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // LOGIN button logic
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText().trim();
                String password = new String(passText.getPassword());

                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Enter a username.");
                    return;
                }

                String stored = users.get(username);
                if (stored != null && stored.equals(password)) {
                    JOptionPane.showMessageDialog(frame, "Welcome, " + username + "!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid username or password.");
                }
            }
        });

        // REGISTER button logic
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openRegistrationDialog(frame);
            }
        });

        frame.add(userLabel);
        frame.add(userText);
        frame.add(passLabel);
        frame.add(passText);
        frame.add(loginButton);
        frame.add(registerButton);

        frame.setVisible(true);
    }

    private void openRegistrationDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Register", true);
        dialog.setLayout(new GridLayout(4, 2, 8, 8));
        dialog.setSize(360, 200);
        dialog.setLocationRelativeTo(parent);

        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        JPasswordField confirm = new JPasswordField();
        JButton create = new JButton("Create");
        JButton cancel = new JButton("Cancel");

        dialog.add(new JLabel("New username:"));
        dialog.add(user);
        dialog.add(new JLabel("Password:"));
        dialog.add(pass);
        dialog.add(new JLabel("Confirm password:"));
        dialog.add(confirm);
        dialog.add(create);
        dialog.add(cancel);

        // CREATE account
        create.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String u = user.getText().trim();
                String p1 = new String(pass.getPassword());
                String p2 = new String(confirm.getPassword());

                if (u.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Username cannot be empty.");
                    return;
                }
                if (p1.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Password cannot be empty.");
                    return;
                }
                if (!p1.equals(p2)) {
                    JOptionPane.showMessageDialog(dialog, "Passwords do not match.");
                    return;
                }
                if (users.containsKey(u)) {
                    JOptionPane.showMessageDialog(dialog, "Username already exists.");
                    return;
                }

                users.put(u, p1);
                if (saveUsers()) {
                    JOptionPane.showMessageDialog(dialog, "Account created! You can log in now.");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save user file.");
                }
            }
        });

        // CANCEL button
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }

    private void loadUsers() {
        try {
            if (Files.notExists(USER_FILE)) {
                Files.createFile(USER_FILE);
                return;
            }
            for (String line : Files.readAllLines(USER_FILE, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1]);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read users.txt: " + e.getMessage());
        }
    }

    private boolean saveUsers() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> e : users.entrySet()) {
            lines.add(e.getKey() + "," + e.getValue());
        }
        try {
            Files.write(USER_FILE, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            System.err.println("Could not write users.txt: " + e.getMessage());
            return false;
        }
    }
}

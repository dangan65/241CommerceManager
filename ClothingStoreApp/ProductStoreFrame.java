

import javax.swing.*;
import java.awt.*;

public class ProductStoreFrame extends JFrame {
    public ProductStoreFrame(String username) {
        super("Clothing Store — Products");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Shared data
        Inventory inventory = new Inventory();
        seedDemoData(inventory); // demo data for convenience
        OrdersHistory history = new OrdersHistory();
        String sessionUser = (username == null || username.trim().isEmpty()) ? "guest" : username;

        JTabbedPane tabs = new JTabbedPane();
        
        // Only add Admin tab if user is an admin
        if (isAdmin(sessionUser)) {
            tabs.addTab("Admin", new AdminPanel(inventory));
        }
        
        tabs.addTab("Shop", new ShopPanel(inventory, sessionUser, history));
        tabs.addTab("Orders", new OrdersPanel(sessionUser, history));

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        
        // Add logout button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel userLabel = new JLabel("Logged in as: " + sessionUser + 
                                    (isAdmin(sessionUser) ? " (ADMIN)" : ""));
        JButton logoutButton = new JButton("Logout");
        
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", 
                "Logout Confirmation", 
                JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                new LoginWindow();
            }
        });
        
        topPanel.add(userLabel);
        topPanel.add(logoutButton);
        add(topPanel, BorderLayout.NORTH);
        
        // Update window title to show current user
        if (!sessionUser.equals("guest")) {
            setTitle("Clothing Store — Products (Logged in as: " + sessionUser + 
                    (isAdmin(sessionUser) ? " - ADMIN" : "") + ")");
        }
    }
    
    /**
     * Check if the given username has admin privileges
     * @param username The username to check
     * @return true if user is an admin, false otherwise
     */
    private boolean isAdmin(String username) {
        // Define admin usernames here
        return "admin".equals(username) || "administrator".equals(username) || "root".equals(username);
    }

    private void seedDemoData(Inventory inv) {
        inv.addProduct(new Product("TSH-001", "T-Shirt", "Classic Tee", 14.99, 25));
        inv.addProduct(new Product("HD-101", "Hoodie", "Fleece Hoodie", 39.99, 12));
        inv.addProduct(new Product("JN-501", "Jeans", "Slim Fit Jeans", 49.99, 10));
        inv.addProduct(new Product("CP-200", "Cap", "Dad Hat", 12.50, 50));
        inv.addProduct(new Product("SN-777", "Sneakers", "Low-top Sneaker", 69.95, 8));
    }
}

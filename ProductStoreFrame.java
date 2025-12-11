import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class ProductStoreFrame extends JFrame {
    private final Inventory inventory;
    private final OrdersHistory history;
    private final String sessionUser;
    
    public ProductStoreFrame(String username) {
        super("Clothing Store – E-Commerce Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        // Shared data
        inventory = new Inventory();
        history = new OrdersHistory();
        sessionUser = (username == null || username.trim().isEmpty()) ? "guest" : username;
        
        // Load persisted data on startup
        loadPersistedData();

        JTabbedPane tabs = new JTabbedPane();
        
        // Admin tabs
        if (isAdmin(sessionUser)) {
            tabs.addTab("📦 Admin Panel", new AdminPanel(inventory));
            tabs.addTab("📊 Reports", new ReportsPanel(inventory, history));
            tabs.addTab("⚙️ Order Processing", new OrderProcessingPanel(history));
        }
        
        // Customer tabs
        tabs.addTab("🛍️ Shop", new ShopPanel(inventory, sessionUser, history));
        tabs.addTab("📋 My Orders", new OrdersPanel(sessionUser, history, isAdmin(sessionUser)));

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        
        // Top panel with user info and logout
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Left side - welcome message
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel welcomeLabel = new JLabel("Welcome, " + sessionUser + 
                                        (isAdmin(sessionUser) ? " 👑 (ADMIN)" : " 🛒"));
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        leftPanel.add(welcomeLabel);
        
        // Right side - logout button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("🚪 Logout");
        
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?\n\n" +
                "⚠️ Any unsaved changes will be lost.\n" +
                "Make sure to save your data before logging out.",
                "Logout Confirmation", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                new LoginWindow();
            }
        });
        
        rightPanel.add(logoutButton);
        
        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        add(topPanel, BorderLayout.NORTH);
        
        // Window closing listener to save data
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });
        
        // Update window title
        if (!sessionUser.equals("guest")) {
            setTitle("Clothing Store – " + sessionUser + 
                    (isAdmin(sessionUser) ? " (ADMIN)" : " (Customer)"));
        }
    }
    
    /**
     * Load persisted data on application startup
     */
    private void loadPersistedData() {
        // Load products
        List<Product> loadedProducts = DataPersistence.loadProducts();
        
        if (loadedProducts.isEmpty()) {
            // If no saved products, seed demo data
            seedDemoData(inventory);
            System.out.println("No saved products found. Loading demo data.");
        } else {
            // Load saved products
            for (Product p : loadedProducts) {
                inventory.addProduct(p);
            }
            System.out.println("✓ Loaded " + loadedProducts.size() + " products from file.");
        }
        
        // Load orders
        Map<String, List<Order>> loadedOrders = DataPersistence.loadOrders(inventory);
        if (!loadedOrders.isEmpty()) {
            history.loadHistory(loadedOrders);
            System.out.println("✓ Loaded order history for " + loadedOrders.size() + " users.");
        }
    }
    
    /**
     * Handle window closing with save prompt
     */
    private void handleWindowClose() {
        int option = JOptionPane.showConfirmDialog(this,
            "Do you want to save your data before closing?\n\n" +
            "💾 Yes - Save all data and close\n" +
            "❌ No - Close without saving\n" +
            "⏸️ Cancel - Return to application",
            "Save Before Exit",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            // Save data
            boolean productsSaved = DataPersistence.saveProducts(inventory.getProducts());
            boolean ordersSaved = DataPersistence.saveOrders(history.getAllHistory());
            
            if (productsSaved && ordersSaved) {
                JOptionPane.showMessageDialog(this,
                    "✓ All data saved successfully!\n\n" +
                    "Products: " + inventory.getProducts().size() + "\n" +
                    "Orders: " + history.getAllHistory().values().stream()
                        .mapToInt(List::size).sum(),
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            } else {
                int retry = JOptionPane.showConfirmDialog(this,
                    "❌ Failed to save some data.\n\n" +
                    "Products saved: " + (productsSaved ? "✓" : "✗") + "\n" +
                    "Orders saved: " + (ordersSaved ? "✓" : "✗") + "\n\n" +
                    "Do you still want to close without saving?",
                    "Save Failed",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (retry == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        } else if (option == JOptionPane.NO_OPTION) {
            // Close without saving
            System.exit(0);
        }
        // Cancel - do nothing, window stays open
    }
     
    /**
     * Check if the given username has admin privileges
     */
    private boolean isAdmin(String username) {
        return "admin".equals(username) || "administrator".equals(username) || "root".equals(username);
    }

    /**
     * Seeds demo data if no persisted data is found
     */
    private void seedDemoData(Inventory inv) {
        inv.addProduct(new Product("TSH-001", "T-Shirt", "Classic Tee", 14.99, 25));
        inv.addProduct(new Product("TSH-002", "T-Shirt", "V-Neck Tee", 16.99, 30));
        inv.addProduct(new Product("HD-101", "Hoodie", "Fleece Hoodie", 39.99, 12));
        inv.addProduct(new Product("HD-102", "Hoodie", "Zip Hoodie", 44.99, 8));
        inv.addProduct(new Product("JN-501", "Jeans", "Slim Fit Jeans", 49.99, 10));
        inv.addProduct(new Product("JN-502", "Jeans", "Straight Leg Jeans", 54.99, 15));
        inv.addProduct(new Product("CP-200", "Cap", "Dad Hat", 12.50, 50));
        inv.addProduct(new Product("CP-201", "Cap", "Snapback", 15.99, 40));
        inv.addProduct(new Product("SN-777", "Sneakers", "Low-top Sneaker", 69.95, 8));
        inv.addProduct(new Product("SN-778", "Sneakers", "High-top Sneaker", 79.95, 5));
    }
}
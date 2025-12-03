import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

public class ProductStoreFrame extends JFrame {
    private final Inventory inventory;
    private final OrdersHistory history;
    private final String sessionUser;
    
    public ProductStoreFrame(String username) {
        super("Clothing Store – Products");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Shared data
        inventory = new Inventory();
        history = new OrdersHistory();
        sessionUser = (username == null || username.trim().isEmpty()) ? "guest" : username;
        
        // FEATURE 5: Load persisted data on startup
        loadPersistedData();

        JTabbedPane tabs = new JTabbedPane();
        
        // Only add Admin tab if user is an admin
        if (isAdmin(sessionUser)) {
            tabs.addTab("Admin", new AdminPanel(inventory));
        }
        
        tabs.addTab("Shop", new ShopPanel(inventory, sessionUser, history));
        tabs.addTab("Orders", new OrdersPanel(sessionUser, history));
        
        // FEATURE 2: Add Order Processing tab for admins
        if (isAdmin(sessionUser)) {
            tabs.addTab("Order Processing", createOrderProcessingPanel());
        }

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        
        // Add logout button and user info
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel userLabel = new JLabel("Logged in as: " + sessionUser + 
                                    (isAdmin(sessionUser) ? " (ADMIN)" : ""));
        JButton logoutButton = new JButton("Logout");
        
        // FEATURE 3: Improved logout confirmation
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?\n\n" +
                "Any unsaved changes will be lost.\n" +
                "Make sure to save your data before logging out.",
                "Logout Confirmation", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                dispose();
                new LoginWindow();
            }
        });
        
        topPanel.add(userLabel);
        topPanel.add(logoutButton);
        add(topPanel, BorderLayout.NORTH);
        
        // FEATURE 5: Add window closing listener to save data
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });
        
        // Update window title
        if (!sessionUser.equals("guest")) {
            setTitle("Clothing Store – Products (Logged in as: " + sessionUser + 
                    (isAdmin(sessionUser) ? " - ADMIN" : "") + ")");
        }
    }
    
    /**
     * FEATURE 5: Load persisted data on application startup
     */
    private void loadPersistedData() {
        // Load products
        List<Product> loadedProducts = DataPersistence.loadProducts();
        
        if (loadedProducts.isEmpty()) {
            // If no saved products, seed demo data
            seedDemoData(inventory);
        } else {
            // Load saved products
            for (Product p : loadedProducts) {
                inventory.addProduct(p);
            }
            System.out.println("Loaded " + loadedProducts.size() + " products from file.");
        }
        
        // Load orders
        Map<String, List<Order>> loadedOrders = DataPersistence.loadOrders(inventory);
        if (!loadedOrders.isEmpty()) {
            history.loadHistory(loadedOrders);
            System.out.println("Loaded order history for " + loadedOrders.size() + " users.");
        }
    }
    
    /**
     * FEATURE 5: Handle window closing with save prompt
     */
    private void handleWindowClose() {
        int option = JOptionPane.showConfirmDialog(this,
            "Do you want to save your data before closing?\n\n" +
            "• Yes: Save and close\n" +
            "• No: Close without saving\n" +
            "• Cancel: Return to application",
            "Save Before Exit",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            // Save data
            boolean productsSaved = DataPersistence.saveProducts(inventory.getProducts());
            boolean ordersSaved = DataPersistence.saveOrders(history.getAllHistory());
            
            if (productsSaved && ordersSaved) {
                JOptionPane.showMessageDialog(this,
                    "Data saved successfully!",
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            } else {
                int retry = JOptionPane.showConfirmDialog(this,
                    "Failed to save some data.\n\n" +
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
     * FEATURE 2: Creates the Order Processing panel to demonstrate Queue usage
     */
    private JPanel createOrderProcessingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea displayArea = new JTextArea(20, 60);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        JButton processNextBtn = new JButton("Process Next Pending Order");
        JButton completeNextBtn = new JButton("Complete Next Processing Order");
        JButton viewStatsBtn = new JButton("View Statistics");
        JButton refreshBtn = new JButton("Refresh Display");
        
        processNextBtn.addActionListener(e -> {
            Order order = history.processNextPendingOrder();
            if (order != null) {
                displayArea.append("✓ Moved order to PROCESSING queue\n");
                displayArea.append("  User: " + order.getUsername() + "\n");
                displayArea.append("  Total: $" + String.format("%.2f", order.getTotal()) + "\n\n");
            } else {
                displayArea.append("✗ No pending orders to process\n\n");
            }
        });
        
        completeNextBtn.addActionListener(e -> {
            Order order = history.completeNextOrder();
            if (order != null) {
                displayArea.append("✓ Order COMPLETED\n");
                displayArea.append("  User: " + order.getUsername() + "\n");
                displayArea.append("  Total: $" + String.format("%.2f", order.getTotal()) + "\n\n");
            } else {
                displayArea.append("✗ No orders in processing queue\n\n");
            }
        });
        
        viewStatsBtn.addActionListener(e -> {
            displayArea.setText("");
            displayArea.append(history.getProcessingStatistics());
            displayArea.append("\n");
            
            // Show pending orders
            List<Order> pending = history.getPendingOrders();
            if (!pending.isEmpty()) {
                displayArea.append("\nPENDING ORDERS:\n");
                displayArea.append("-" .repeat(40) + "\n");
                for (Order order : pending) {
                    displayArea.append(String.format("User: %s | Items: %d | Total: $%.2f\n",
                        order.getUsername(), order.getItems().size(), order.getTotal()));
                }
            }
            
            // Show processing orders
            List<Order> processing = history.getProcessingOrders();
            if (!processing.isEmpty()) {
                displayArea.append("\nPROCESSING ORDERS:\n");
                displayArea.append("-" .repeat(40) + "\n");
                for (Order order : processing) {
                    displayArea.append(String.format("User: %s | Items: %d | Total: $%.2f\n",
                        order.getUsername(), order.getItems().size(), order.getTotal()));
                }
            }
        });
        
        refreshBtn.addActionListener(e -> {
            displayArea.setText("Order Processing Queue System\n");
            displayArea.append("=" .repeat(40) + "\n\n");
            displayArea.append("This panel demonstrates Queue data structure usage.\n\n");
            displayArea.append("Orders flow through three stages:\n");
            displayArea.append("1. PENDING (newly placed orders)\n");
            displayArea.append("2. PROCESSING (orders being fulfilled)\n");
            displayArea.append("3. COMPLETED (finished orders)\n\n");
            displayArea.append("Click 'View Statistics' to see current status.\n");
        });
        
        buttonPanel.add(processNextBtn);
        buttonPanel.add(completeNextBtn);
        buttonPanel.add(viewStatsBtn);
        buttonPanel.add(refreshBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize display
        displayArea.setText("Order Processing Queue System\n");
        displayArea.append("=" .repeat(40) + "\n\n");
        displayArea.append("This panel demonstrates Queue data structure usage.\n\n");
        displayArea.append("Orders flow through three stages:\n");
        displayArea.append("1. PENDING (newly placed orders)\n");
        displayArea.append("2. PROCESSING (orders being fulfilled)\n");
        displayArea.append("3. COMPLETED (finished orders)\n\n");
        displayArea.append("Click 'View Statistics' to see current status.\n");
        
        return panel;
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
        inv.addProduct(new Product("HD-101", "Hoodie", "Fleece Hoodie", 39.99, 12));
        inv.addProduct(new Product("JN-501", "Jeans", "Slim Fit Jeans", 49.99, 10));
        inv.addProduct(new Product("CP-200", "Cap", "Dad Hat", 12.50, 50));
        inv.addProduct(new Product("SN-777", "Sneakers", "Low-top Sneaker", 69.95, 8));
    }
}
package ClothingStoreApp;

import javax.swing.*;
import java.awt.*;

public class ProductStoreFrame extends JFrame {
    public ProductStoreFrame(String username) {
        super("Clothing Store — Products");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Shared data
        Inventory inventory = new Inventory();
        seedDemoData(inventory); // demo data for convenience
        OrdersHistory history = new OrdersHistory();
        String sessionUser = (username == null || username.trim().isEmpty()) ? "guest" : username;

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Admin", new AdminPanel(inventory));
        tabs.addTab("Shop", new ShopPanel(inventory, sessionUser, history));
        tabs.addTab("Orders", new OrdersPanel(sessionUser, history));

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    private void seedDemoData(Inventory inv) {
        inv.addProduct(new Product("TSH-001", "T-Shirt", "Classic Tee", 14.99, 25));
        inv.addProduct(new Product("HD-101", "Hoodie", "Fleece Hoodie", 39.99, 12));
        inv.addProduct(new Product("JN-501", "Jeans", "Slim Fit Jeans", 49.99, 10));
        inv.addProduct(new Product("CP-200", "Cap", "Dad Hat", 12.50, 50));
        inv.addProduct(new Product("SN-777", "Sneakers", "Low-top Sneaker", 69.95, 8));
    }
}

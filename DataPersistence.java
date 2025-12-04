import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DataPersistence class handles saving and loading of products and orders to/from files.
 * Demonstrates file I/O operations for data persistence across application sessions.
 */
public class DataPersistence {
    private static final Path PRODUCTS_FILE = Paths.get("products.txt");
    private static final Path ORDERS_FILE = Paths.get("orders.txt");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Saves all products to file
     * Format: ID,Category,Name,Price,Quantity
     */
    public static boolean saveProducts(List<Product> products) {
        try {
            List<String> lines = new ArrayList<>();
            for (Product p : products) {
                if (p.getID() != null && p.getName() != null) {
                    lines.add(String.format("%s|%s|%s|%.2f|%d",
                        escapeDelimiters(p.getID()),
                        escapeDelimiters(p.getCategory()),
                        escapeDelimiters(p.getName()),
                        p.getPrice(),
                        p.getQuantity()));
                }
            }
            Files.write(PRODUCTS_FILE, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving products: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads products from file
     */
    public static List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        
        if (Files.notExists(PRODUCTS_FILE)) {
            return products; // Return empty list if file doesn't exist
        }
        
        try {
            List<String> lines = Files.readAllLines(PRODUCTS_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    try {
                        Product p = new Product(
                            unescapeDelimiters(parts[0].trim()),
                            unescapeDelimiters(parts[1].trim()),
                            unescapeDelimiters(parts[2].trim()),
                            Double.parseDouble(parts[3].trim()),
                            Integer.parseInt(parts[4].trim())
                        );
                        products.add(p);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed product line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        
        return products;
    }
    
    /**
     * Saves order history to file
     * Format: Username|Timestamp|ProductID|ProductName|Quantity|Price
     */
    public static boolean saveOrders(Map<String, List<Order>> orderHistory) {
        try {
            List<String> lines = new ArrayList<>();
            
            for (Map.Entry<String, List<Order>> entry : orderHistory.entrySet()) {
                String username = entry.getKey();
                for (Order order : entry.getValue()) {
                    String timestamp = order.getCreatedAt().format(DATE_FORMATTER);
                    for (OrderItem item : order.getItems()) {
                        lines.add(String.format("%s|%s|%s|%s|%d|%.2f",
                            escapeDelimiters(username),
                            timestamp,
                            escapeDelimiters(item.getProduct().getID()),
                            escapeDelimiters(item.getProduct().getName()),
                            item.getQuantity(),
                            item.getProduct().getPrice()));
                    }
                }
            }
            
            Files.write(ORDERS_FILE, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving orders: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads order history from file
     * Note: This reconstructs orders but products need to reference current inventory
     */
    public static Map<String, List<Order>> loadOrders(Inventory inventory) {
        Map<String, List<Order>> orderHistory = new HashMap<>();
        
        if (Files.notExists(ORDERS_FILE)) {
            return orderHistory; // Return empty map if file doesn't exist
        }
        
        try {
            List<String> lines = Files.readAllLines(ORDERS_FILE, StandardCharsets.UTF_8);
            Map<String, Map<String, Order>> userOrdersByTimestamp = new HashMap<>();
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    try {
                        String username = unescapeDelimiters(parts[0].trim());
                        String timestamp = parts[1].trim();
                        String productId = unescapeDelimiters(parts[2].trim());
                        String productName = unescapeDelimiters(parts[3].trim());
                        int quantity = Integer.parseInt(parts[4].trim());
                        double price = Double.parseDouble(parts[5].trim());
                        
                        // Get or create user's order map
                        userOrdersByTimestamp.putIfAbsent(username, new HashMap<>());
                        Map<String, Order> userOrders = userOrdersByTimestamp.get(username);
                        
                        // Get or create order for this timestamp
                        if (!userOrders.containsKey(timestamp)) {
                            Order order = new Order(username);
                            // Use reflection or add a setter to set the createdAt time
                            userOrders.put(timestamp, order);
                        }
                        
                        Order order = userOrders.get(timestamp);
                        
                        // Try to find product in current inventory, or create a snapshot
                        Product product = findProductById(inventory, productId);
                        if (product == null) {
                            // Create a product snapshot for historical order
                            product = new Product(productId, "", productName, price, 0);
                        }
                        
                        order.addItem(new OrderItem(product, quantity));
                        
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping malformed order line: " + line);
                    }
                }
            }
            
            // Convert to final structure
            for (Map.Entry<String, Map<String, Order>> entry : userOrdersByTimestamp.entrySet()) {
                List<Order> orders = new ArrayList<>(entry.getValue().values());
                orderHistory.put(entry.getKey(), orders);
            }
            
        } catch (IOException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
        
        return orderHistory;
    }
    
    /**
     * Helper method to find product by ID in inventory
     */
    private static Product findProductById(Inventory inventory, String id) {
        for (Product p : inventory.getProducts()) {
            if (p.getID() != null && p.getID().equals(id)) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Escapes pipe delimiters in strings to prevent parsing issues
     */
    private static String escapeDelimiters(String str) {
        if (str == null) return "";
        return str.replace("|", "&#124;");
    }
    
    /**
     * Unescapes pipe delimiters from strings
     */
    private static String unescapeDelimiters(String str) {
        if (str == null) return "";
        return str.replace("&#124;", "|");
    }
    
    /**
     * Creates backup of data files
     */
    public static void createBackup() {
        try {
            if (Files.exists(PRODUCTS_FILE)) {
                Path backup = Paths.get("products_backup.txt");
                Files.copy(PRODUCTS_FILE, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            if (Files.exists(ORDERS_FILE)) {
                Path backup = Paths.get("orders_backup.txt");
                Files.copy(ORDERS_FILE, backup, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }
}
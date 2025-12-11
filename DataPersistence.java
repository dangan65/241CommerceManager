import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataPersistence {
    private static final Path PRODUCTS_FILE = Paths.get("products.txt");
    private static final Path ORDERS_FILE = Paths.get("orders.txt");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
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
            return false;
        }
    }
    
    public static List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        
        if (Files.notExists(PRODUCTS_FILE)) {
            return products;
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
                    }
                }
            }
        } catch (IOException e) {
        }
        
        return products;
    }
    
    public static boolean saveOrders(Map<String, List<Order>> orderHistory) {
        try {
            List<String> lines = new ArrayList<>();
            
            for (Map.Entry<String, List<Order>> entry : orderHistory.entrySet()) {
                String username = entry.getKey();
                for (Order order : entry.getValue()) {
                    String timestamp = order.getCreatedAt().format(DATE_FORMATTER);
                    for (OrderItem item : order.getItems()) {
                        lines.add(String.format("%s|%s|%s|%s|%d|%.2f|%s",
                            escapeDelimiters(username),
                            timestamp,
                            escapeDelimiters(item.getProduct().getID()),
                            escapeDelimiters(item.getProduct().getName()),
                            item.getQuantity(),
                            item.getProduct().getPrice(),
                            order.getStatus().name()
                        ));
                    }
                }
            }
            
            Files.write(ORDERS_FILE, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public static Map<String, List<Order>> loadOrders(Inventory inventory) {
        Map<String, List<Order>> orderHistory = new HashMap<>();
        
        if (Files.notExists(ORDERS_FILE)) {
            return orderHistory;
        }
        
        try {
            List<String> lines = Files.readAllLines(ORDERS_FILE, StandardCharsets.UTF_8);
            Map<String, Map<String, Order>> userOrdersByTimestamp = new HashMap<>();
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split("\\|");
                if (parts.length == 7) {
                    try {
                        String username = unescapeDelimiters(parts[0].trim());
                        String timestamp = parts[1].trim();
                        String productId = unescapeDelimiters(parts[2].trim());
                        String productName = unescapeDelimiters(parts[3].trim());
                        int quantity = Integer.parseInt(parts[4].trim());
                        double price = Double.parseDouble(parts[5].trim());
                        Order.OrderStatus status = Order.OrderStatus.valueOf(parts[6].trim());
                        
                        userOrdersByTimestamp.putIfAbsent(username, new HashMap<>());
                        Map<String, Order> userOrders = userOrdersByTimestamp.get(username);
                        
                        if (!userOrders.containsKey(timestamp)) {
                            Order order = new Order(username);
                            order.setStatus(status);
                            userOrders.put(timestamp, order);
                        }
                        
                        Order order = userOrders.get(timestamp);
                        
                        Product product = findProductById(inventory, productId);
                        if (product == null) {
                            product = new Product(productId, "", productName, price, 0);
                        }
                        
                        order.addItem(new OrderItem(product, quantity));
                        
                    } catch (Exception e) {
                    }
                }
            }
            
            for (Map.Entry<String, Map<String, Order>> entry : userOrdersByTimestamp.entrySet()) {
                List<Order> orders = new ArrayList<>(entry.getValue().values());
                orderHistory.put(entry.getKey(), orders);
            }
            
        } catch (IOException e) {
        }
        
        return orderHistory;
    }
    
    private static Product findProductById(Inventory inventory, String id) {
        for (Product p : inventory.getProducts()) {
            if (p.getID() != null && p.getID().equals(id)) {
                return p;
            }
        }
        return null;
    }
    
    private static String escapeDelimiters(String str) {
        if (str == null) return "";
        return str.replace("|", "&#124;");
    }
    
    private static String unescapeDelimiters(String str) {
        if (str == null) return "";
        return str.replace("&#124;", "|");
    }
    
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
        }
    }
}

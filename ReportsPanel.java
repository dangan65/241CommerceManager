import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * ReportsPanel provides comprehensive analytics and reporting for administrators.
 * Includes: out-of-stock products, total orders, most ordered product, and revenue.
 */
public class ReportsPanel extends JPanel {
    private final Inventory inventory;
    private final OrdersHistory history;
    private final JTextArea reportArea;
    
    public ReportsPanel(Inventory inventory, OrdersHistory history) {
        this.inventory = inventory;
        this.history = history;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("📊 Admin Reports & Analytics");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Report display area
        reportArea = new JTextArea(25, 70);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        JButton outOfStockBtn = new JButton("Out of Stock Products");
        JButton totalOrdersBtn = new JButton("Total Orders");
        JButton mostOrderedBtn = new JButton("Most Ordered Product");
        JButton revenueBtn = new JButton("Total Revenue");
        JButton fullReportBtn = new JButton("Generate Full Report");
        JButton clearBtn = new JButton("Clear");
        
        outOfStockBtn.addActionListener(e -> showOutOfStockReport());
        totalOrdersBtn.addActionListener(e -> showTotalOrdersReport());
        mostOrderedBtn.addActionListener(e -> showMostOrderedReport());
        revenueBtn.addActionListener(e -> showRevenueReport());
        fullReportBtn.addActionListener(e -> showFullReport());
        clearBtn.addActionListener(e -> reportArea.setText(""));
        
        buttonPanel.add(outOfStockBtn);
        buttonPanel.add(totalOrdersBtn);
        buttonPanel.add(mostOrderedBtn);
        buttonPanel.add(revenueBtn);
        buttonPanel.add(fullReportBtn);
        buttonPanel.add(clearBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Show initial message
        showWelcomeMessage();
    }
    
    private void showWelcomeMessage() {
        reportArea.setText("═══════════════════════════════════════════════════════════════\n");
        reportArea.append("              ADMIN REPORTS & ANALYTICS DASHBOARD\n");
        reportArea.append("═══════════════════════════════════════════════════════════════\n\n");
        reportArea.append("Select a report type from the buttons below:\n\n");
        reportArea.append("  • Out of Stock Products - Products with 0 quantity\n");
        reportArea.append("  • Total Orders - Count of all orders placed\n");
        reportArea.append("  • Most Ordered Product - Product ordered most frequently\n");
        reportArea.append("  • Total Revenue - Sum of all order totals\n");
        reportArea.append("  • Generate Full Report - Complete analytics summary\n");
    }
    
    /**
     * REQUIRED REPORT 1: Out of Stock Products
     */
    private void showOutOfStockReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("                   OUT OF STOCK PRODUCTS REPORT\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        List<Product> outOfStock = new ArrayList<>();
        for (Product p : inventory.getProducts()) {
            if (p.getQuantity() == 0) {
                outOfStock.add(p);
            }
        }
        
        if (outOfStock.isEmpty()) {
            sb.append("✓ All products are in stock!\n\n");
        } else {
            sb.append(String.format("Found %d product(s) out of stock:\n\n", outOfStock.size()));
            sb.append(String.format("%-15s %-30s %-15s %10s\n", "ID", "Name", "Category", "Price"));
            sb.append("─".repeat(70)).append("\n");
            
            for (Product p : outOfStock) {
                sb.append(String.format("%-15s %-30s %-15s $%9.2f\n",
                    truncate(p.getID(), 15),
                    truncate(p.getName(), 30),
                    truncate(p.getCategory(), 15),
                    p.getPrice()));
            }
            
            sb.append("\n⚠ Action Required: Restock these products to continue sales.\n");
        }
        
        reportArea.setText(sb.toString());
    }
    
    /**
     * REQUIRED REPORT 2: Total Orders Placed
     */
    private void showTotalOrdersReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("                     TOTAL ORDERS REPORT\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        Map<String, List<Order>> allOrders = history.getAllHistory();
        int totalOrders = 0;
        Map<String, Integer> ordersByUser = new HashMap<>();
        
        for (Map.Entry<String, List<Order>> entry : allOrders.entrySet()) {
            String username = entry.getKey();
            int userOrderCount = entry.getValue().size();
            totalOrders += userOrderCount;
            ordersByUser.put(username, userOrderCount);
        }
        
        sb.append(String.format("Total Orders Placed: %d\n\n", totalOrders));
        
        if (totalOrders > 0) {
            sb.append("Orders by Customer:\n");
            sb.append("─".repeat(50)).append("\n");
            sb.append(String.format("%-30s %15s\n", "Customer", "Orders"));
            sb.append("─".repeat(50)).append("\n");
            
            // Sort by order count descending
            ordersByUser.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> {
                    sb.append(String.format("%-30s %15d\n", 
                        truncate(entry.getKey(), 30), entry.getValue()));
                });
            
            sb.append("\n");
            sb.append(String.format("Average Orders per Customer: %.2f\n", 
                (double) totalOrders / ordersByUser.size()));
        }
        
        reportArea.setText(sb.toString());
    }
    
    /**
     * REQUIRED REPORT 3: Most Frequently Ordered Product
     */
    private void showMostOrderedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("                MOST FREQUENTLY ORDERED PRODUCT\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        // Frequency map: Product ID -> Total Quantity Ordered
        Map<String, Integer> productFrequency = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();
        Map<String, Double> productPrices = new HashMap<>();
        
        Map<String, List<Order>> allOrders = history.getAllHistory();
        
        for (List<Order> orders : allOrders.values()) {
            for (Order order : orders) {
                for (OrderItem item : order.getItems()) {
                    Product p = item.getProduct();
                    String id = p.getID();
                    
                    productFrequency.put(id, 
                        productFrequency.getOrDefault(id, 0) + item.getQuantity());
                    productNames.put(id, p.getName());
                    productPrices.put(id, p.getPrice());
                }
            }
        }
        
        if (productFrequency.isEmpty()) {
            sb.append("No orders have been placed yet.\n");
        } else {
            // Sort by frequency descending
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(productFrequency.entrySet());
            sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            // Show top 10
            sb.append("Top 10 Most Ordered Products:\n\n");
            sb.append(String.format("%-5s %-15s %-30s %12s %10s\n", 
                "Rank", "Product ID", "Product Name", "Times Ordered", "Revenue"));
            sb.append("─".repeat(75)).append("\n");
            
            int rank = 1;
            for (Map.Entry<String, Integer> entry : sorted) {
                if (rank > 10) break;
                
                String id = entry.getKey();
                int quantity = entry.getValue();
                String name = productNames.getOrDefault(id, "Unknown");
                double price = productPrices.getOrDefault(id, 0.0);
                double revenue = quantity * price;
                
                sb.append(String.format("%-5d %-15s %-30s %12d $%9.2f\n",
                    rank, truncate(id, 15), truncate(name, 30), quantity, revenue));
                
                rank++;
            }
            
            // Highlight the winner
            Map.Entry<String, Integer> topProduct = sorted.get(0);
            sb.append("\n");
            sb.append("🏆 MOST ORDERED PRODUCT:\n");
            sb.append("   Product: " + productNames.get(topProduct.getKey()) + "\n");
            sb.append("   ID: " + topProduct.getKey() + "\n");
            sb.append("   Total Quantity Ordered: " + topProduct.getValue() + "\n");
        }
        
        reportArea.setText(sb.toString());
    }
    
    /**
     * REQUIRED REPORT 4: Total Revenue
     */
    private void showRevenueReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("                      REVENUE REPORT\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        
        Map<String, List<Order>> allOrders = history.getAllHistory();
        
        double totalRevenue = 0.0;
        double totalTax = 0.0;
        int totalOrders = 0;
        int totalItems = 0;
        
        Map<String, Double> revenueByUser = new HashMap<>();
        
        for (Map.Entry<String, List<Order>> entry : allOrders.entrySet()) {
            String username = entry.getKey();
            double userRevenue = 0.0;
            
            for (Order order : entry.getValue()) {
                double orderTotal = order.getTotal();
                double orderTax = order.getTaxAmount();
                
                totalRevenue += orderTotal;
                totalTax += orderTax;
                totalOrders++;
                totalItems += order.getItems().size();
                userRevenue += orderTotal;
            }
            
            revenueByUser.put(username, userRevenue);
        }
        
        sb.append(String.format("Total Revenue (including tax): $%,.2f\n", totalRevenue));
        sb.append(String.format("Total Tax Collected: $%,.2f\n", totalTax));
        sb.append(String.format("Net Revenue (excluding tax): $%,.2f\n\n", totalRevenue - totalTax));
        
        sb.append(String.format("Total Orders: %d\n", totalOrders));
        sb.append(String.format("Total Items Sold: %d\n", totalItems));
        
        if (totalOrders > 0) {
            sb.append(String.format("Average Order Value: $%.2f\n\n", totalRevenue / totalOrders));
            
            sb.append("Revenue by Customer:\n");
            sb.append("─".repeat(50)).append("\n");
            sb.append(String.format("%-30s %15s\n", "Customer", "Revenue"));
            sb.append("─".repeat(50)).append("\n");
            
            // Sort by revenue descending
            revenueByUser.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    sb.append(String.format("%-30s $%14,.2f\n",
                    escapeFormat(truncate(entry.getKey(), 30)),
                    entry.getValue()));
                });
        }
        
        reportArea.setText(sb.toString());
    }
    
    /**
     * Generates a comprehensive report with all metrics
     */
    private void showFullReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("                  COMPREHENSIVE ANALYTICS REPORT\n");
        sb.append("═══════════════════════════════════════════════════════════════\n\n");
        sb.append("Generated: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
        
        // Inventory Summary
        sb.append("┌─ INVENTORY SUMMARY ─────────────────────────────────────────┐\n");
        int totalProducts = inventory.getProducts().size();
        int outOfStockCount = 0;
        int lowStockCount = 0;
        double totalInventoryValue = 0.0;
        
        for (Product p : inventory.getProducts()) {
            if (p.getQuantity() == 0) outOfStockCount++;
            else if (p.getQuantity() < 5) lowStockCount++;
            totalInventoryValue += p.getPrice() * p.getQuantity();
        }
        
        sb.append(String.format("  Total Products: %d\n", totalProducts));
        sb.append(String.format("  Out of Stock: %d\n", outOfStockCount));
        sb.append(String.format("  Low Stock (<5): %d\n", lowStockCount));
        sb.append(String.format("  Inventory Value: $%,.2f\n", totalInventoryValue));
        sb.append("└─────────────────────────────────────────────────────────────┘\n\n");
        
        // Order Summary
        sb.append("┌─ ORDER SUMMARY ─────────────────────────────────────────────┐\n");
        Map<String, List<Order>> allOrders = history.getAllHistory();
        int totalOrders = 0;
        double totalRevenue = 0.0;
        
        for (List<Order> orders : allOrders.values()) {
            totalOrders += orders.size();
            for (Order order : orders) {
                totalRevenue += order.getTotal();
            }
        }
        
        sb.append(String.format("  Total Orders: %d\n", totalOrders));
        sb.append(String.format("  Total Revenue: $%,.2f\n", totalRevenue));
        if (totalOrders > 0) {
            sb.append(String.format("  Average Order: $%.2f\n", totalRevenue / totalOrders));
        }
        sb.append("└─────────────────────────────────────────────────────────────┘\n\n");
        
        // Product Performance
        sb.append("┌─ PRODUCT PERFORMANCE ───────────────────────────────────────┐\n");
        Map<String, Integer> productFrequency = new HashMap<>();
        
        for (List<Order> orders : allOrders.values()) {
            for (Order order : orders) {
                for (OrderItem item : order.getItems()) {
                    String id = item.getProduct().getID();
                    productFrequency.put(id, 
                        productFrequency.getOrDefault(id, 0) + item.getQuantity());
                }
            }
        }
        
        if (!productFrequency.isEmpty()) {
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(productFrequency.entrySet());
            sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            Map.Entry<String, Integer> top = sorted.get(0);
            Product topProduct = findProductById(top.getKey());
            
            sb.append(String.format("  Most Ordered: %s (%d units)\n", 
                topProduct != null ? topProduct.getName() : "Unknown", top.getValue()));
        } else {
            sb.append("  No sales data available\n");
        }
        sb.append("└─────────────────────────────────────────────────────────────┘\n\n");
        
        sb.append("For detailed breakdowns, use the specific report buttons above.\n");
        
        reportArea.setText(sb.toString());
    }
    
    private Product findProductById(String id) {
        for (Product p : inventory.getProducts()) {
            if (p.getID().equals(id)) return p;
        }
        return null;
    }
    
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    private String escapeFormat(String s) {
        return s.replace("%", "%%");
    }
}
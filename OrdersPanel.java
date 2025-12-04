import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdersPanel extends JPanel {
    private final String username;
    private final OrdersHistory history;
    private final JTextArea area = new JTextArea(16, 60);
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final boolean isAdmin;

    public OrdersPanel(String username, OrdersHistory history, boolean isAdmin) {
        this.username = (username == null || username.trim().isEmpty()) ? "guest" : username;
        this.history = history;
        this.isAdmin = isAdmin;

        setLayout(new BorderLayout(8, 8));

        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        
        JButton refresh = new JButton("Refresh");
        JButton saveOrders = new JButton("Save Orders");
        JButton viewAllOrders = new JButton("View All Orders (Admin)");
        JButton updateStatus = new JButton("Update Order Status");
        
        refresh.addActionListener(e -> refreshView());
        
        saveOrders.addActionListener(e -> {
            boolean success = DataPersistence.saveOrders(history.getAllHistory());
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Order history saved successfully to orders.txt!",
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to save order history.\nPlease check file permissions.",
                    "Save Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Admin-only features
        viewAllOrders.addActionListener(e -> showAllOrders());
        updateStatus.addActionListener(e -> updateOrderStatus());
        
        buttonPanel.add(refresh);
        buttonPanel.add(saveOrders);
        
        if (isAdmin) {
            buttonPanel.add(viewAllOrders);
            buttonPanel.add(updateStatus);
        }
        
        add(buttonPanel, BorderLayout.SOUTH);

        // Initial load
        refreshView();
    }

    private void refreshView() {
        List<Order> orders = history.get(username);
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("═".repeat(70)).append("\n");
        sb.append("ORDER HISTORY FOR: ").append(username.toUpperCase()).append("\n");
        sb.append("═".repeat(70)).append("\n\n");
        
        if (orders.isEmpty()) {
            sb.append("No orders found.\n\n");
            sb.append("Start shopping in the 'Shop' tab to place your first order!\n");
        } else {
            sb.append("Total Orders: ").append(orders.size()).append("\n");
            
            double totalSpent = 0;
            for (Order o : orders) {
                totalSpent += o.getTotal();
            }
            sb.append("Total Spent: $").append(String.format("%.2f", totalSpent)).append("\n\n");
            sb.append("─".repeat(70)).append("\n\n");
            
            int orderNum = 1;
            for (Order o : orders) {
                sb.append("ORDER #").append(orderNum++).append("\n");
                sb.append("Order ID: ").append(o.getOrderId()).append("\n");
                sb.append("Date: ").append(dtf.format(o.getCreatedAt())).append("\n");
                sb.append("Status: ").append(getStatusIcon(o.getStatus())).append(" ")
                  .append(o.getStatusDisplay()).append("\n");
                
                if (!o.getShippingAddress().isEmpty()) {
                    sb.append("Shipping: ").append(o.getShippingAddress()).append("\n");
                }
                
                sb.append("\nItems:\n");
                
                for (OrderItem item : o.getItems()) {
                    sb.append(String.format("  • %-30s x %-3d  $%8.2f\n",
                        truncate(item.getProduct().getName(), 30),
                        item.getQuantity(),
                        item.getProduct().getPrice()));
                }
                
                sb.append("\n");
                sb.append(String.format("Subtotal: $%8.2f\n", o.getSubtotal()));
                if (o.getTaxAmount() > 0) {
                    sb.append(String.format("Tax:      $%8.2f\n", o.getTaxAmount()));
                }
                sb.append(String.format("Total:    $%8.2f\n", o.getTotal()));
                sb.append("─".repeat(70)).append("\n\n");
            }
        }
        
        area.setText(sb.toString());
        area.setCaretPosition(0);
    }
    
    /**
     * Admin feature: View all orders from all users
     */
    private void showAllOrders() {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "Admin access required.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("═".repeat(70)).append("\n");
        sb.append("                     ALL ORDERS (ADMIN VIEW)\n");
        sb.append("═".repeat(70)).append("\n\n");
        
        var allOrders = history.getAllHistory();
        int totalOrders = 0;
        double totalRevenue = 0;
        
        for (var entry : allOrders.entrySet()) {
            String user = entry.getKey();
            List<Order> userOrders = entry.getValue();
            
            if (!userOrders.isEmpty()) {
                sb.append("\n").append("─".repeat(70)).append("\n");
                sb.append("Customer: ").append(user).append(" (").append(userOrders.size()).append(" orders)\n");
                sb.append("─".repeat(70)).append("\n");
                
                for (Order order : userOrders) {
                    sb.append(String.format("  %s | %s | %s | $%.2f\n",
                        order.getOrderId(),
                        dtf.format(order.getCreatedAt()),
                        order.getStatusDisplay(),
                        order.getTotal()));
                    totalOrders++;
                    totalRevenue += order.getTotal();
                }
            }
        }
        
        sb.append("\n").append("═".repeat(70)).append("\n");
        sb.append(String.format("TOTAL: %d orders | Revenue: $%.2f\n", totalOrders, totalRevenue));
        sb.append("═".repeat(70)).append("\n");
        
        JTextArea allOrdersArea = new JTextArea(sb.toString(), 20, 70);
        allOrdersArea.setEditable(false);
        allOrdersArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        JOptionPane.showMessageDialog(this, new JScrollPane(allOrdersArea), 
            "All Orders", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Admin feature: Update order status
     */
    private void updateOrderStatus() {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(this, "Admin access required.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get all orders
        var allOrders = history.getAllHistory();
        java.util.List<Order> allOrdersList = new java.util.ArrayList<>();
        for (var orders : allOrders.values()) {
            allOrdersList.addAll(orders);
        }
        
        if (allOrdersList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No orders to update.", "No Orders", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Create selection dialog
        String[] orderOptions = new String[allOrdersList.size()];
        for (int i = 0; i < allOrdersList.size(); i++) {
            Order o = allOrdersList.get(i);
            orderOptions[i] = String.format("%s - %s - %s - $%.2f",
                o.getOrderId(), o.getUsername(), o.getStatusDisplay(), o.getTotal());
        }
        
        String selected = (String) JOptionPane.showInputDialog(this,
            "Select order to update:",
            "Update Order Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            orderOptions,
            orderOptions[0]);
        
        if (selected == null) return;
        
        // Find selected order
        int selectedIndex = java.util.Arrays.asList(orderOptions).indexOf(selected);
        Order selectedOrder = allOrdersList.get(selectedIndex);
        
        // Select new status
        Order.OrderStatus[] statuses = Order.OrderStatus.values();
        String[] statusOptions = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            statusOptions[i] = statuses[i].getDisplayName();
        }
        
        String newStatusStr = (String) JOptionPane.showInputDialog(this,
            "Current status: " + selectedOrder.getStatusDisplay() + "\n\nSelect new status:",
            "Update Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statusOptions,
            selectedOrder.getStatusDisplay());
        
        if (newStatusStr == null) return;
        
        // Update status
        for (Order.OrderStatus status : statuses) {
            if (status.getDisplayName().equals(newStatusStr)) {
                selectedOrder.setStatus(status);
                JOptionPane.showMessageDialog(this,
                    "Order " + selectedOrder.getOrderId() + " status updated to: " + newStatusStr,
                    "Status Updated",
                    JOptionPane.INFORMATION_MESSAGE);
                refreshView();
                break;
            }
        }
    }
    
    /**
     * Gets icon for order status
     */
    private String getStatusIcon(Order.OrderStatus status) {
        switch (status) {
            case PLACED: return "📝";
            case PROCESSING: return "⚙️";
            case SHIPPED: return "🚚";
            case DELIVERED: return "✅";
            case CANCELLED: return "❌";
            default: return "•";
        }
    }
    
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrdersPanel extends JPanel {
    private final String username;
    private final OrdersHistory history;
    private final JTextArea area = new JTextArea(16, 60);
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public OrdersPanel(String username, OrdersHistory history) {
        this.username = (username == null || username.trim().isEmpty()) ? "guest" : username;
        this.history = history;

        setLayout(new BorderLayout(8, 8));

        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        
        JButton refresh = new JButton("Refresh");
        JButton saveOrders = new JButton("Save Orders");
        
        refresh.addActionListener(e -> refreshView());
        
        // FEATURE 5: Save orders button
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
        
        buttonPanel.add(refresh);
        buttonPanel.add(saveOrders);
        
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
                sb.append("Date: ").append(dtf.format(o.getCreatedAt())).append("\n");
                sb.append("Status: COMPLETED\n");
                sb.append("\nItems:\n");
                
                for (OrderItem item : o.getItems()) {
                    sb.append(String.format("  • %-30s x %-3d  $%8.2f\n",
                        truncate(item.getProduct().getName(), 30),
                        item.getQuantity(),
                        item.getProduct().getPrice()));
                }
                
                sb.append("\n");
                sb.append(String.format("Order Total: $%8.2f\n", o.getTotal()));
                sb.append("─".repeat(70)).append("\n\n");
            }
        }
        
        area.setText(sb.toString());
        area.setCaretPosition(0);
    }
    
    /**
     * Helper method to truncate long strings
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
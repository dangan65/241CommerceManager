

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
        add(new JScrollPane(area), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshView());
        add(refresh, BorderLayout.SOUTH);

        // initial load
        refreshView();
    }

    private void refreshView() {
        List<Order> orders = history.get(username);
        StringBuilder sb = new StringBuilder();
        if (orders.isEmpty()) {
            sb.append("(No past orders)\n");
        } else {
            for (Order o : orders) {
                sb.append("Order @ ").append(dtf.format(o.getCreatedAt()))
                  .append("  Total: $").append(String.format("%.2f", o.getTotal())).append("\n");
                for (OrderItem item : o.getItems()) {
                    sb.append("   - ")
                      .append(item.getProduct().getName())
                      .append(" x ").append(item.getQuantity())
                      .append(" ($").append(String.format("%.2f", item.getProduct().getPrice())).append(")")
                      .append("\n");
                }
                sb.append("\n");
            }
        }
        area.setText(sb.toString());
        area.setCaretPosition(0);
    }
}

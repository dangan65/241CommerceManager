

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final String username;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items = new ArrayList<>();

    public Order(String username) {
        this.username = (username == null || username.trim().isEmpty()) ? "guest" : username;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        if (item != null && item.getQuantity() > 0) {
            items.add(item);
        }
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getTotal() {
        double sum = 0.0;
        for (OrderItem item : items) {
            sum += item.getLineTotal();
        }
        return sum;
    }
}

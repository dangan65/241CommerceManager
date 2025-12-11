import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private static int orderCounter = 1000;
    
    private final String orderId;
    private final String username;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items = new ArrayList<>();
    private String shippingAddress = "";
    private String stateCode = "";
    private OrderStatus status = OrderStatus.PLACED;

    public enum OrderStatus {
        PLACED("Order Placed"),
        PROCESSING("Processing"),
        SHIPPED("Shipped"),
        DELIVERED("Delivered"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        OrderStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public Order(String username) {
        this.orderId = generateOrderId();
        this.username = (username == null || username.trim().isEmpty()) ? "guest" : username;
        this.createdAt = LocalDateTime.now();
    }

    private static synchronized String generateOrderId() {
        return "ORD-" + (orderCounter++);
    }

    public void addItem(OrderItem item) {
        if (item != null && item.getQuantity() > 0) {
            items.add(item);
        }
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress != null ? shippingAddress : "";
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode != null ? stateCode : "";
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status != null ? status : OrderStatus.PLACED;
    }

    public String getStatusDisplay() {
        return status.getDisplayName();
    }

    public double getSubtotal() {
        double sum = 0.0;
        for (OrderItem item : items) {
            sum += item.getLineTotal();
        }
        return sum;
    }

    public double getTaxAmount() {
        if (stateCode == null || stateCode.trim().isEmpty()) {
            return 0.0;
        }
        return TaxCalculator.calculateTax(getSubtotal(), stateCode);
    }

    public double getTotal() {
        return getSubtotal() + getTaxAmount();
    }

    @Override
    public String toString() {
        return String.format("Order{id='%s', user='%s', items=%d, total=$%.2f, status=%s}", 
                           orderId, username, items.size(), getTotal(), status.getDisplayName());
    }
}

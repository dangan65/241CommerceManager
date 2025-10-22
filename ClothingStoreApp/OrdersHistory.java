package ClothingStoreApp;

import java.util.*;

public class OrdersHistory {
    // simple per-user, in-memory history
    private final Map<String, List<Order>> history = new HashMap<>();

    public void add(String username, Order order) {
        history.computeIfAbsent(username == null ? "guest" : username, k -> new ArrayList<>()).add(order);
    }

    public List<Order> get(String username) {
        return history.getOrDefault(username == null ? "guest" : username, Collections.emptyList());
    }
}


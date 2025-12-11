import java.util.*;

/**
 * OrdersHistory manages order records with a Queue-based processing system.
 * FEATURE 2: Added Queue data structure for order processing workflow.
 * FEATURE 3: Added Stack data structure for recently viewed orders tracking.
 */
public class OrdersHistory {
    private final Map<String, List<Order>> history = new HashMap<>();
    
    private final Queue<Order> pendingOrdersQueue = new LinkedList<>();
    private final Queue<Order> processingQueue = new LinkedList<>();
    private final List<Order> completedOrders = new ArrayList<>();
    
    private final Stack<Order> recentlyViewedStack = new Stack<>();
    private static final int MAX_RECENT_VIEWS = 10;

    /**
     * Adds an order to user's history and to pending queue for processing
     */
    public void add(String username, Order order) {
        String user = username == null ? "guest" : username;
        history.computeIfAbsent(user, k -> new ArrayList<>()).add(order);
        
        order.setStatus(Order.OrderStatus.PLACED);
        pendingOrdersQueue.offer(order);
    }

    /**
     * Gets all orders for a specific user
     */
    public List<Order> get(String username) {
        return history.getOrDefault(username == null ? "guest" : username, Collections.emptyList());
    }
    
    /**
     * Gets the complete order history map (for data persistence)
     */
    public Map<String, List<Order>> getAllHistory() {
        return new HashMap<>(history);
    }
    
    /**
     * Loads order history from persistence (used on startup)
     */
    public void loadHistory(Map<String, List<Order>> loadedHistory) {
        history.clear();
        history.putAll(loadedHistory);

        pendingOrdersQueue.clear();
        processingQueue.clear();
        completedOrders.clear();
        recentlyViewedStack.clear();

        for (List<Order> orders : loadedHistory.values()) {
            for (Order o : orders) {
                switch (o.getStatus()) {
                    case PLACED:
                        pendingOrdersQueue.offer(o);
                        break;
                    case PROCESSING:
                        processingQueue.offer(o);
                        break;
                    case SHIPPED:
                    case DELIVERED:
                    case COMPLETED:
                        completedOrders.add(o);
                        break;
                    case CANCELLED:
                        completedOrders.add(o);
                        break;
                }
            }
        }
    }
    
    public Order processNextPendingOrder() {
        Order order = pendingOrdersQueue.poll();
        if (order != null) {
            order.setStatus(Order.OrderStatus.PROCESSING);
            processingQueue.offer(order);
        }
        return order;
    }
    
    public Order completeNextOrder() {
        Order order = processingQueue.poll();
        if (order != null) {
            order.setStatus(Order.OrderStatus.COMPLETED);
            completedOrders.add(order);
        }
        return order;
    }
    
    public int getPendingOrderCount() {
        return pendingOrdersQueue.size();
    }
    
    public int getProcessingOrderCount() {
        return processingQueue.size();
    }
    
    public int getCompletedOrderCount() {
        return completedOrders.size();
    }
    
    public List<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrdersQueue);
    }
    
    public List<Order> getProcessingOrders() {
        return new ArrayList<>(processingQueue);
    }
    
    public List<Order> getCompletedOrders() {
        return new ArrayList<>(completedOrders);
    }
    
    public Order peekNextPending() {
        return pendingOrdersQueue.peek();
    }
    
    public Order peekNextProcessing() {
        return processingQueue.peek();
    }
    
    public String getProcessingStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order Processing Statistics:\n");
        sb.append("=".repeat(40)).append("\n");
        sb.append(String.format("Pending Orders: %d\n", pendingOrdersQueue.size()));
        sb.append(String.format("Processing Orders: %d\n", processingQueue.size()));
        sb.append(String.format("Completed Orders: %d\n", completedOrders.size()));
        sb.append(String.format("Total Orders: %d\n", getTotalOrderCount()));
        return sb.toString();
    }
    
    public int getTotalOrderCount() {
        return pendingOrdersQueue.size() + processingQueue.size() + completedOrders.size();
    }
    
    public void clearAllQueues() {
        pendingOrdersQueue.clear();
        processingQueue.clear();
        completedOrders.clear();
    }
    
    public int processAllPendingOrders() {
        int processed = 0;
        while (!pendingOrdersQueue.isEmpty()) {
            processNextPendingOrder();
            processed++;
        }
        return processed;
    }
    
    public int completeAllProcessingOrders() {
        int completed = 0;
        while (!processingQueue.isEmpty()) {
            completeNextOrder();
            completed++;
        }
        return completed;
    }
    
    /**
     * Stack-based recently viewed orders tracking
     * Maintains last 10 viewed orders in LIFO order
     */
    public void markOrderAsViewed(Order order) {
        if (order == null) return;
        
        recentlyViewedStack.remove(order);
        
        recentlyViewedStack.push(order);
        
        if (recentlyViewedStack.size() > MAX_RECENT_VIEWS) {
            recentlyViewedStack.remove(0);
        }
    }

    public Stack<Order> getRecentlyViewedOrders() {
        Stack<Order> copy = new Stack<>();
        copy.addAll(recentlyViewedStack);
        return copy;
    }

    public Order getLastViewedOrder() {
        return recentlyViewedStack.isEmpty() ? null : recentlyViewedStack.peek();
    }

    public String getRecentlyViewedSummary() {
        if (recentlyViewedStack.isEmpty()) {
            return "No recently viewed orders.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Recently Viewed Orders (Most Recent First):\n");
        sb.append("=".repeat(50)).append("\n");
        
        Stack<Order> temp = new Stack<>();
        temp.addAll(recentlyViewedStack);
        
        int count = 1;
        while (!temp.isEmpty()) {
            Order o = temp.pop();
            sb.append(String.format("%d. %s - %s - $%.2f\n", 
                count++, o.getOrderId(), o.getUsername(), o.getTotal()));
        }
        
        return sb.toString();
    }
}
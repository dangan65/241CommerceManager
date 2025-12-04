import java.util.*;

/**
 * OrdersHistory manages order records with a Queue-based processing system.
 * FEATURE 2: Added Queue data structure for order processing workflow.
 */
public class OrdersHistory {
    // Per-user order history storage
    private final Map<String, List<Order>> history = new HashMap<>();
    
    // FEATURE 2: Queue for pending order processing
    private final Queue<Order> pendingOrdersQueue = new LinkedList<>();
    private final Queue<Order> processingQueue = new LinkedList<>();
    private final List<Order> completedOrders = new ArrayList<>();

    /**
     * Adds an order to user's history and to pending queue for processing
     */
    public void add(String username, Order order) {
        String user = username == null ? "guest" : username;
        history.computeIfAbsent(user, k -> new ArrayList<>()).add(order);
        
        // FEATURE 2: Add to pending queue for processing workflow
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
        
        // Optionally add loaded orders to completed queue
        for (List<Order> orders : loadedHistory.values()) {
            completedOrders.addAll(orders);
        }
    }
    
    // ===== FEATURE 2: Queue-based Order Processing Methods =====
    
    /**
     * Moves an order from pending to processing queue
     * Returns the order being processed, or null if queue is empty
     */
    public Order processNextPendingOrder() {
        Order order = pendingOrdersQueue.poll();
        if (order != null) {
            processingQueue.offer(order);
        }
        return order;
    }
    
    /**
     * Completes an order from the processing queue
     * Returns the completed order, or null if processing queue is empty
     */
    public Order completeNextOrder() {
        Order order = processingQueue.poll();
        if (order != null) {
            completedOrders.add(order);
        }
        return order;
    }
    
    /**
     * Gets the number of pending orders
     */
    public int getPendingOrderCount() {
        return pendingOrdersQueue.size();
    }
    
    /**
     * Gets the number of orders currently being processed
     */
    public int getProcessingOrderCount() {
        return processingQueue.size();
    }
    
    /**
     * Gets the number of completed orders
     */
    public int getCompletedOrderCount() {
        return completedOrders.size();
    }
    
    /**
     * Gets a copy of all pending orders (without removing them)
     */
    public List<Order> getPendingOrders() {
        return new ArrayList<>(pendingOrdersQueue);
    }
    
    /**
     * Gets a copy of all orders in processing
     */
    public List<Order> getProcessingOrders() {
        return new ArrayList<>(processingQueue);
    }
    
    /**
     * Gets a copy of all completed orders
     */
    public List<Order> getCompletedOrders() {
        return new ArrayList<>(completedOrders);
    }
    
    /**
     * Peek at the next pending order without removing it
     */
    public Order peekNextPending() {
        return pendingOrdersQueue.peek();
    }
    
    /**
     * Peek at the next processing order without removing it
     */
    public Order peekNextProcessing() {
        return processingQueue.peek();
    }
    
    /**
     * Gets order processing statistics
     */
    public String getProcessingStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order Processing Statistics:\n");
        sb.append("=" .repeat(40)).append("\n");
        sb.append(String.format("Pending Orders: %d\n", pendingOrdersQueue.size()));
        sb.append(String.format("Processing Orders: %d\n", processingQueue.size()));
        sb.append(String.format("Completed Orders: %d\n", completedOrders.size()));
        sb.append(String.format("Total Orders: %d\n", getTotalOrderCount()));
        return sb.toString();
    }
    
    /**
     * Gets total number of orders across all queues
     */
    public int getTotalOrderCount() {
        return pendingOrdersQueue.size() + processingQueue.size() + completedOrders.size();
    }
    
    /**
     * Clears all order queues (use with caution)
     */
    public void clearAllQueues() {
        pendingOrdersQueue.clear();
        processingQueue.clear();
        completedOrders.clear();
    }
    
    /**
     * Process all pending orders automatically
     */
    public int processAllPendingOrders() {
        int processed = 0;
        while (!pendingOrdersQueue.isEmpty()) {
            processNextPendingOrder();
            processed++;
        }
        return processed;
    }
    
    /**
     * Complete all processing orders automatically
     */
    public int completeAllProcessingOrders() {
        int completed = 0;
        while (!processingQueue.isEmpty()) {
            completeNextOrder();
            completed++;
        }
        return completed;
    }
}
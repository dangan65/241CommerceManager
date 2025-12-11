/**
 * InsufficientStockException.java
 * Thrown when attempting to order more items than available in inventory
 */
public class InsufficientStockException extends Exception {
    private final String productName;
    private final int requested;
    private final int available;
    
    public InsufficientStockException(String productName, int requested, int available) {
        super(String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d", 
            productName, requested, available));
        this.productName = productName;
        this.requested = requested;
        this.available = available;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public int getRequested() {
        return requested;
    }
    
    public int getAvailable() {
        return available;
    }
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Cart class represents a shopping cart that holds OrderItems.
 * Provides functionality to add, remove, update, and manage cart items.
 */
public class Cart {
    private final List<OrderItem> items;
    
    /**
     * Constructor initializes an empty cart
     */
    public Cart() {
        this.items = new ArrayList<>();
    }
    
    /**
     * Adds a product to the cart with specified quantity
     * If the product already exists in cart, updates the quantity
     * @param product The product to add
     * @param quantity The quantity to add
     * @return true if successfully added, false if invalid input
     */
    public boolean addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return false;
        }
        
        // Check if product already exists in cart
        for (OrderItem item : items) {
            if (item.getProduct().equals(product)) {
                // Update existing item quantity
                item.setQuantity(item.getQuantity() + quantity);
                return true;
            }
        }
        
        // Add new item to cart
        items.add(new OrderItem(product, quantity));
        return true;
    }
    
    /**
     * Adds an OrderItem directly to the cart
     * @param orderItem The OrderItem to add
     * @return true if successfully added, false if invalid input
     */
    public boolean addItem(OrderItem orderItem) {
        if (orderItem == null || orderItem.getQuantity() <= 0) {
            return false;
        }
        
        // Check if product already exists in cart
        for (OrderItem item : items) {
            if (item.getProduct().equals(orderItem.getProduct())) {
                // Update existing item quantity
                item.setQuantity(item.getQuantity() + orderItem.getQuantity());
                return true;
            }
        }
        
        // Add new item to cart
        items.add(new OrderItem(orderItem.getProduct(), orderItem.getQuantity()));
        return true;
    }
    
    /**
     * Removes a product from the cart entirely
     * @param product The product to remove
     * @return true if product was found and removed, false otherwise
     */
    public boolean removeItem(Product product) {
        if (product == null) {
            return false;
        }
        
        Iterator<OrderItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            OrderItem item = iterator.next();
            if (item.getProduct().equals(product)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes a specific OrderItem from the cart
     * @param orderItem The OrderItem to remove
     * @return true if item was found and removed, false otherwise
     */
    public boolean removeItem(OrderItem orderItem) {
        if (orderItem == null) {
            return false;
        }
        return items.remove(orderItem);
    }
    
    /**
     * Updates the quantity of a specific product in the cart
     * @param product The product to update
     * @param newQuantity The new quantity (if 0 or negative, removes the item)
     * @return true if product was found and updated, false otherwise
     */
    public boolean updateQuantity(Product product, int newQuantity) {
        if (product == null) {
            return false;
        }
        
        if (newQuantity <= 0) {
            return removeItem(product);
        }
        
        for (OrderItem item : items) {
            if (item.getProduct().equals(product)) {
                item.setQuantity(newQuantity);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the quantity of a specific product in the cart
     * @param product The product to check
     * @return The quantity of the product in cart, 0 if not found
     */
    public int getQuantity(Product product) {
        if (product == null) {
            return 0;
        }
        
        for (OrderItem item : items) {
            if (item.getProduct().equals(product)) {
                return item.getQuantity();
            }
        }
        return 0;
    }
    
    /**
     * Checks if the cart contains a specific product
     * @param product The product to check
     * @return true if cart contains the product, false otherwise
     */
    public boolean contains(Product product) {
        if (product == null) {
            return false;
        }
        
        for (OrderItem item : items) {
            if (item.getProduct().equals(product)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets all items in the cart
     * @return Unmodifiable list of OrderItems in the cart
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    /**
     * Gets the total number of items in the cart (sum of all quantities)
     * @return Total item count
     */
    public int getTotalItemCount() {
        int total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }
    
    /**
     * Gets the number of unique products in the cart
     * @return Number of different products
     */
    public int getUniqueItemCount() {
        return items.size();
    }
    
    /**
     * Calculates the total price of all items in the cart
     * @return Total cart value
     */
    public double getTotal() {
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.getLineTotal();
        }
        return total;
    }
    
    /**
     * Checks if the cart is empty
     * @return true if cart has no items, false otherwise
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    /**
     * Removes all items from the cart
     */
    public void clear() {
        items.clear();
    }
    
    /**
     * Validates if all items in cart have sufficient stock
     * @return true if all items have enough stock, false otherwise
     */
    public boolean validateStock() {
        for (OrderItem item : items) {
            if (item.getProduct().getQuantity() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Gets a list of items that don't have sufficient stock
     * @return List of OrderItems with insufficient stock
     */
    public List<OrderItem> getInsufficientStockItems() {
        List<OrderItem> insufficientItems = new ArrayList<>();
        for (OrderItem item : items) {
            if (item.getProduct().getQuantity() < item.getQuantity()) {
                insufficientItems.add(item);
            }
        }
        return insufficientItems;
    }
    
    /**
     * Creates a formatted string representation of the cart
     * @return String representation of cart contents
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "Cart is empty";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Cart Contents:\n");
        sb.append("=" .repeat(40)).append("\n");
        
        for (OrderItem item : items) {
            sb.append(String.format("%-20s x %-3d = $%8.2f\n", 
                item.getProduct().getName(), 
                item.getQuantity(), 
                item.getLineTotal()));
        }
        
        sb.append("=" .repeat(40)).append("\n");
        sb.append(String.format("Total Items: %-10d Total: $%8.2f\n", 
            getTotalItemCount(), getTotal()));
        
        return sb.toString();
    }
    
    /**
     * Creates a detailed summary of the cart including product details
     * @return Detailed string representation
     */
    public String getDetailedSummary() {
        if (isEmpty()) {
            return "Cart is empty";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Detailed Cart Summary:\n");
        sb.append("=" .repeat(60)).append("\n");
        
        for (OrderItem item : items) {
            Product p = item.getProduct();
            sb.append(String.format("Product ID: %s\n", p.getID()));
            sb.append(String.format("Name: %s\n", p.getName()));
            sb.append(String.format("Category: %s\n", p.getCategory()));
            sb.append(String.format("Price: $%.2f\n", p.getPrice()));
            sb.append(String.format("Quantity in Cart: %d\n", item.getQuantity()));
            sb.append(String.format("Available Stock: %d\n", p.getQuantity()));
            sb.append(String.format("Line Total: $%.2f\n", item.getLineTotal()));
            sb.append("-" .repeat(40)).append("\n");
        }
        
        sb.append(String.format("Total Unique Products: %d\n", getUniqueItemCount()));
        sb.append(String.format("Total Items: %d\n", getTotalItemCount()));
        sb.append(String.format("Cart Total: $%.2f\n", getTotal()));
        
        return sb.toString();
    }
}

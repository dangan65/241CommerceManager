/**
 * AdminAction class represents an action performed by an admin that can be undone.
 * Used in conjunction with Stack data structure for undo functionality.
 */
public class AdminAction {
    public enum ActionType {
        ADD, UPDATE, DELETE
    }
    
    private final ActionType type;
    private final Product productSnapshot;
    private final Product previousState; // For UPDATE actions
    
    /**
     * Constructor for ADD and DELETE actions
     */
    public AdminAction(ActionType type, Product productSnapshot) {
        this.type = type;
        this.productSnapshot = productSnapshot != null ? cloneProduct(productSnapshot) : null;
        this.previousState = null;
    }
    
    /**
     * Constructor for UPDATE actions (stores both old and new state)
     */
    public AdminAction(ActionType type, Product previousState, Product newState) {
        this.type = type;
        this.productSnapshot = newState != null ? cloneProduct(newState) : null;
        this.previousState = previousState != null ? cloneProduct(previousState) : null;
    }
    
    /**
     * Creates a deep copy of a product
     */
    private Product cloneProduct(Product p) {
        return new Product(p.getID(), p.getCategory(), p.getName(), p.getPrice(), p.getQuantity());
    }
    
    public ActionType getType() {
        return type;
    }
    
    public Product getProductSnapshot() {
        return productSnapshot;
    }
    
    public Product getPreviousState() {
        return previousState;
    }
    
    @Override
    public String toString() {
        return String.format("AdminAction{type=%s, product=%s}", 
            type, productSnapshot != null ? productSnapshot.getName() : "null");
    }
}
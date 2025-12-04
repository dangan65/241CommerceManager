import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ShopPanel extends JPanel {
    private final Inventory inventory;
    private final String username;
    private final OrdersHistory history;

    private final JTable table;
    private final ProductTableModel tableModel;
    private final Cart cart = new Cart();

    private final JTextField qtyF = new JTextField("1");
    private final JLabel cartTotalL = new JLabel("Total: $0.00");
    private final JLabel cartItemsL = new JLabel("Items: 0");

    public ShopPanel(Inventory inventory, String username, OrdersHistory history) {
        this.inventory = inventory;
        this.username = (username == null || username.trim().isEmpty()) ? "guest" : username;
        this.history = history;

        setLayout(new BorderLayout(8,8));

        tableModel = new ProductTableModel(inventory.getProducts());
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bottom.add(new JLabel("Qty:"));
        qtyF.setColumns(5);
        bottom.add(qtyF);

        JButton addToCart = new JButton("Add to Cart");
        JButton viewCart = new JButton("View Cart");
        JButton removeFromCart = new JButton("Remove from Cart");
        JButton checkout = new JButton("Checkout");

        addToCart.addActionListener(this::onAddToCart);
        viewCart.addActionListener(e -> showCart());
        removeFromCart.addActionListener(this::onRemoveFromCart);
        checkout.addActionListener(e -> onCheckout());

        bottom.add(addToCart);
        bottom.add(removeFromCart);
        bottom.add(viewCart);
        bottom.add(checkout);
        bottom.add(cartItemsL);
        bottom.add(cartTotalL);

        add(bottom, BorderLayout.SOUTH);
    }

    private void onAddToCart(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) { 
            showError("Please select a product from the table to add to cart."); 
            return; 
        }
        Product p = inventory.getProducts().get(row);

        // FEATURE 4: Improved validation and error messages
        String qtyText = qtyF.getText().trim();
        if (qtyText.isEmpty()) {
            showError("Please enter a quantity.");
            qtyF.requestFocus();
            return;
        }

        int q;
        try { 
            q = Integer.parseInt(qtyText); 
        } catch (NumberFormatException ex) { 
            showError("Invalid quantity format. Please enter a whole number (e.g., 1, 2, 5).");
            qtyF.requestFocus();
            qtyF.selectAll();
            return; 
        }
        
        if (q <= 0) { 
            showError("Quantity must be a positive number greater than 0."); 
            qtyF.requestFocus();
            qtyF.selectAll();
            return; 
        }
        
        if (p.getQuantity() < q) { 
            showError("Insufficient stock!\n\n" +
                     "Product: " + p.getName() + "\n" +
                     "Requested: " + q + "\n" +
                     "Available: " + p.getQuantity() + "\n\n" +
                     "Please reduce the quantity or select a different product.");
            return; 
        }

        cart.addItem(p, q);
        updateTotal();
        showSuccess("Added " + q + " x " + p.getName() + " to cart!\n" +
                   "Cart total: $" + String.format("%.2f", cart.getTotal()));
    }

    private void onRemoveFromCart(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) { 
            showError("Please select a product from the table to remove from cart."); 
            return; 
        }
        Product p = inventory.getProducts().get(row);
        
        if (!cart.contains(p)) {
            showError("The product '" + p.getName() + "' is not in your cart.");
            return;
        }
        
        int currentQty = cart.getQuantity(p);
        
        // FEATURE 3: Improved confirmation dialog
        String[] options = {"Remove All (" + currentQty + ")", "Remove Some", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, 
            "Product: " + p.getName() + "\n" +
            "Current quantity in cart: " + currentQty + "\n" +
            "Price per item: $" + String.format("%.2f", p.getPrice()) + "\n" +
            "Line total: $" + String.format("%.2f", p.getPrice() * currentQty) + "\n\n" +
            "How would you like to remove this item?",
            "Remove from Cart", 
            JOptionPane.YES_NO_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, options, options[2]);
            
        if (choice == 0) { // Remove All
            cart.removeItem(p);
            updateTotal();
            showSuccess("Removed all " + currentQty + " x " + p.getName() + " from cart.");
        } else if (choice == 1) { // Remove Some
            String input = JOptionPane.showInputDialog(this, 
                "Enter quantity to remove (1-" + currentQty + "):", "1");
            if (input != null) {
                try {
                    int removeQty = Integer.parseInt(input.trim());
                    if (removeQty <= 0) {
                        showError("Quantity must be a positive number.");
                        return;
                    }
                    if (removeQty > currentQty) {
                        showError("Cannot remove " + removeQty + " items.\n" +
                                "Only " + currentQty + " items in cart.");
                        return;
                    }
                    if (removeQty >= currentQty) {
                        cart.removeItem(p);
                        showSuccess("Removed all " + currentQty + " x " + p.getName() + " from cart.");
                    } else {
                        cart.updateQuantity(p, currentQty - removeQty);
                        showSuccess("Removed " + removeQty + " x " + p.getName() + " from cart.\n" +
                                  "Remaining in cart: " + (currentQty - removeQty));
                    }
                    updateTotal();
                } catch (NumberFormatException ex) {
                    showError("Invalid quantity format. Please enter a whole number.");
                }
            }
        }
        // choice == 2 is Cancel, do nothing
    }

    private void showCart() {
        if (cart.isEmpty()) { 
            JOptionPane.showMessageDialog(this, 
                "Your shopping cart is empty.\n\n" +
                "Browse the products above and click 'Add to Cart' to start shopping!", 
                "Empty Cart", 
                JOptionPane.INFORMATION_MESSAGE); 
            return; 
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(cart.toString()).append("\n");
        sb.append("Cart Statistics:\n");
        sb.append("Unique Products: ").append(cart.getUniqueItemCount()).append("\n");
        sb.append("Total Items: ").append(cart.getTotalItemCount()).append("\n");
        
        JTextArea area = new JTextArea(sb.toString(), 16, 50);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Shopping Cart", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onCheckout() {
        if (cart.isEmpty()) { 
            showError("Your cart is empty. Add some products before checking out."); 
            return; 
        }

        // Validate stock first using Cart's built-in validation
        if (!cart.validateStock()) {
            var insufficientItems = cart.getInsufficientStockItems();
            StringBuilder sb = new StringBuilder("Cannot complete checkout due to insufficient stock:\n\n");
            for (OrderItem oi : insufficientItems) {
                sb.append("• ").append(oi.getProduct().getName())
                  .append("\n  Requested: ").append(oi.getQuantity())
                  .append("\n  Available: ").append(oi.getProduct().getQuantity())
                  .append("\n\n");
            }
            sb.append("Please adjust your cart quantities and try again.");
            showError(sb.toString());
            return;
        }
        
        // Show address dialog to collect shipping info and calculate tax
        AddressDialog addressDialog = new AddressDialog(SwingUtilities.windowForComponent(this));
        addressDialog.setVisible(true);
        
        if (!addressDialog.isConfirmed()) {
            // User cancelled the address dialog
            return;
        }
        
        // Get address and state information
        String fullAddress = addressDialog.getFullAddress();
        String stateCode = addressDialog.getStateCode();
        
        // Calculate tax and total
        double subtotal = cart.getTotal();
        double taxAmount = TaxCalculator.calculateTax(subtotal, stateCode);
        double total = subtotal + taxAmount;
        int itemCount = cart.getTotalItemCount();
        
        // FEATURE 3: Confirmation dialog before checkout
        int confirm = JOptionPane.showConfirmDialog(this,
            "Ready to checkout?\n\n" +
            "Shipping to: " + fullAddress + "\n\n" +
            "Subtotal: $" + String.format("%.2f", subtotal) + "\n" +
            "Tax (" + stateCode + "): $" + String.format("%.2f", taxAmount) + "\n" +
            "Total: $" + String.format("%.2f", total) + "\n\n" +
            "Total Items: " + itemCount + "\n\n" +
            "Click Yes to complete your order.",
            "Confirm Checkout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return; // User cancelled
        }

        // Decrement stock
        for (OrderItem oi : cart.getItems()) {
            Product p = oi.getProduct();
            p.setQuantity(p.getQuantity() - oi.getQuantity());
        }

        // Record order with address and tax info
        Order order = new Order(username);
        for (OrderItem oi : cart.getItems()) {
            order.addItem(oi);
        }
        order.setShippingAddress(fullAddress);
        order.setStateCode(stateCode);
        
        history.add(username, order);
        
        // FEATURE 2: Automatically process the order through the queue
        history.processNextPendingOrder();
        history.completeNextOrder();

        // Reset cart + refresh UI
        cart.clear();
        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
        updateTotal();

        // FEATURE 4: Improved success message
        showSuccess("Order Completed Successfully! 🎉\n\n" +
                   "Order ID: " + order.getOrderId() + "\n" +
                   "Items: " + itemCount + "\n" +
                   "Subtotal: $" + String.format("%.2f", subtotal) + "\n" +
                   "Tax: $" + String.format("%.2f", taxAmount) + "\n" +
                   "Total: $" + String.format("%.2f", total) + "\n\n" +
                   "Shipping to:\n" + fullAddress + "\n\n" +
                   "Thank you for your purchase!\n" +
                   "View your order in the 'Orders' tab.");
    }

    private void updateTotal() {
        cartTotalL.setText("Total: $" + String.format("%.2f", cart.getTotal()));
        cartItemsL.setText("Items: " + cart.getTotalItemCount());
    }
    
    // FEATURE 4: Improved error and success messaging
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}

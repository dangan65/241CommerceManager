

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
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a product."); return; }
        Product p = inventory.getProducts().get(row);

        int q;
        try { q = Integer.parseInt(qtyF.getText().trim()); } catch (Exception ex) { q = 1; }
        if (q <= 0) { JOptionPane.showMessageDialog(this, "Quantity must be positive."); return; }
        if (p.getQuantity() < q) { JOptionPane.showMessageDialog(this, "Not enough stock."); return; }

        cart.addItem(p, q); // reserve in cart; inventory decremented at checkout
        updateTotal();
        JOptionPane.showMessageDialog(this, "Added " + q + " x " + p.getName() + " to cart.");
    }

    private void onRemoveFromCart(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a product to remove from cart."); return; }
        Product p = inventory.getProducts().get(row);
        
        if (!cart.contains(p)) {
            JOptionPane.showMessageDialog(this, p.getName() + " is not in your cart.");
            return;
        }
        
        int currentQty = cart.getQuantity(p);
        String[] options = {"Remove All", "Remove Some", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, 
            "Current quantity in cart: " + currentQty + "\nHow would you like to remove " + p.getName() + "?",
            "Remove from Cart", 
            JOptionPane.YES_NO_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, options, options[2]);
            
        if (choice == 0) { // Remove All
            cart.removeItem(p);
            updateTotal();
            JOptionPane.showMessageDialog(this, "Removed all " + p.getName() + " from cart.");
        } else if (choice == 1) { // Remove Some
            String input = JOptionPane.showInputDialog(this, 
                "Enter quantity to remove (1-" + currentQty + "):", "1");
            if (input != null) {
                try {
                    int removeQty = Integer.parseInt(input.trim());
                    if (removeQty <= 0) {
                        JOptionPane.showMessageDialog(this, "Quantity must be positive.");
                        return;
                    }
                    if (removeQty >= currentQty) {
                        cart.removeItem(p);
                        JOptionPane.showMessageDialog(this, "Removed all " + p.getName() + " from cart.");
                    } else {
                        cart.updateQuantity(p, currentQty - removeQty);
                        JOptionPane.showMessageDialog(this, "Removed " + removeQty + " x " + p.getName() + " from cart.");
                    }
                    updateTotal();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid quantity entered.");
                }
            }
        }
        // choice == 2 is Cancel, do nothing
    }

    private void showCart() {
        if (cart.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "(Cart is empty)"); 
            return; 
        }
        
        // Use Cart's built-in formatting with additional cart statistics
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
        if (cart.isEmpty()) { JOptionPane.showMessageDialog(this, "Cart is empty."); return; }

        // Validate stock first using Cart's built-in validation
        if (!cart.validateStock()) {
            // Get items with insufficient stock and show details
            var insufficientItems = cart.getInsufficientStockItems();
            StringBuilder sb = new StringBuilder("Insufficient stock for:\n");
            for (OrderItem oi : insufficientItems) {
                sb.append("- ").append(oi.getProduct().getName())
                  .append(" (need: ").append(oi.getQuantity())
                  .append(", available: ").append(oi.getProduct().getQuantity()).append(")\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString());
            return;
        }

        // Decrement stock
        for (OrderItem oi : cart.getItems()) {
            Product p = oi.getProduct();
            p.setQuantity(p.getQuantity() - oi.getQuantity());
        }

        // Record order
        Order order = new Order(username);
        for (OrderItem oi : cart.getItems()) order.addItem(oi);
        history.add(username, order);

        // Reset cart + refresh UI
        cart.clear();
        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
        updateTotal();

        JOptionPane.showMessageDialog(this, "Order placed! Total: $" + String.format("%.2f", order.getTotal()));
    }

    private void updateTotal() {
        cartTotalL.setText("Total: $" + String.format("%.2f", cart.getTotal()));
        cartItemsL.setText("Items: " + cart.getTotalItemCount());
    }
}
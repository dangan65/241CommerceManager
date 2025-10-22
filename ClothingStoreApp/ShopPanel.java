package ClothingStoreApp;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ShopPanel extends JPanel {
    private final Inventory inventory;
    private final String username;
    private final OrdersHistory history;
    private final JTable table;
    private final ProductTableModel tableModel;
    private final List<OrderItem> cart = new ArrayList<>();

    private final JTextField qtyF = new JTextField("1");
    private final JLabel cartTotalL = new JLabel("Total: $0.00");

    public ShopPanel(Inventory inventory, String username, OrdersHistory history) {
        this.inventory = inventory;
        this.username = username == null ? "guest" : username;
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
        JButton checkout = new JButton("Checkout");

        addToCart.addActionListener(this::onAddToCart);
        viewCart.addActionListener(e -> showCart());
        checkout.addActionListener(e -> onCheckout());

        bottom.add(addToCart);
        bottom.add(viewCart);
        bottom.add(checkout);
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

        // Reserve in cart but don't decrement inventory until checkout
        cart.add(new OrderItem(p, q));
        updateTotal();
        JOptionPane.showMessageDialog(this, "Added " + q + " x " + p.getName() + " to cart.");
    }

    private void showCart() {
        if (cart.isEmpty()) { JOptionPane.showMessageDialog(this, "(Cart is empty)"); return; }
        StringBuilder sb = new StringBuilder("Cart:\n");
        for (OrderItem oi : cart) {
            sb.append(oi.getProduct().getName())
              .append(" x ").append(oi.getQuantity())
              .append(" = $").append(String.format("%.2f", oi.getLineTotal()))
              .append("\n");
        }
        sb.append("\n").append(cartTotalL.getText());
        JTextArea area = new JTextArea(sb.toString(), 14, 50);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Cart", JOptionPane.INFORMATION_MESSAGE);
    }
}
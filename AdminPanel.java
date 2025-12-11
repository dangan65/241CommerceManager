import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * AdminPanel provides comprehensive product inventory management for administrators.
 * 
 * Stack Data Structure for Undo Functionality
 * Maintains a stack of AdminAction objects to support undo operations for:
 * - ADD: Undo by removing the added product
 * - UPDATE: Undo by restoring previous product state
 * - DELETE: Undo by re-adding the deleted product
 * 
 * Additional Features:
 * - Heap-based sorting (PriorityQueue) for price-sorted product view
 * - Product search by name
 * - Full CRUD operations on inventory
 */
public class AdminPanel extends JPanel {
    private final Inventory inventory;
    private final JTable table;
    private final ProductTableModel tableModel;

    // Undo system
    private final Stack<AdminAction> undoStack = new Stack<>();
    private final JButton undoBtn = new JButton("Undo Last Action");
    private final JLabel undoStatusLabel = new JLabel("No actions to undo");

    public AdminPanel(Inventory inventory) {
        this.inventory = inventory;
        setLayout(new BorderLayout(8,8));

        tableModel = new ProductTableModel(inventory.getProducts());
        table = new JTable(tableModel);

        // Allow double-click editing in table
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", true);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // AUTOSAVE whenever the table changes
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                DataPersistence.saveProducts(inventory.getProducts());
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        JButton addBtn = new JButton("Add Product");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton sortByPriceBtn = new JButton("View Sorted by Price (Heap)");
        JButton viewAllBtn = new JButton("View All");
        JButton searchBtn = new JButton("Search Name");
        JTextField searchF = new JTextField(12);

        addBtn.addActionListener(this::onAdd);
        deleteBtn.addActionListener(this::onDelete);
        viewAllBtn.addActionListener(e -> refreshTable());
        sortByPriceBtn.addActionListener(e -> showSortedByPrice());
        searchBtn.addActionListener(e -> searchByName(searchF.getText().trim()));

        undoBtn.addActionListener(e -> performUndo());
        undoBtn.setEnabled(false);

        bottom.add(addBtn);
        bottom.add(deleteBtn);
        bottom.add(viewAllBtn);
        bottom.add(sortByPriceBtn);
        bottom.add(new JLabel("Name:"));
        bottom.add(searchF);
        bottom.add(searchBtn);
        bottom.add(undoBtn);

        add(bottom, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(undoStatusLabel);
        add(topPanel, BorderLayout.PAGE_START);
    }

    private void onAdd(ActionEvent e) {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (id == null || id.trim().isEmpty()) return;

        String name = JOptionPane.showInputDialog(this, "Enter Product Name:");
        if (name == null || name.trim().isEmpty()) return;

        String category = JOptionPane.showInputDialog(this, "Enter Category:");
        if (category == null || category.trim().isEmpty()) return;

        String priceStr = JOptionPane.showInputDialog(this, "Enter Price:");
        if (priceStr == null || priceStr.trim().isEmpty()) return;

        String qtyStr = JOptionPane.showInputDialog(this, "Enter Quantity:");
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            double price = Double.parseDouble(priceStr);
            int qty = Integer.parseInt(qtyStr);

            Product p = new Product(id.trim(), category.trim(), name.trim(), price, qty);
            inventory.addProduct(p);
            undoStack.push(new AdminAction(AdminAction.ActionType.ADD, p));
            updateUndoButton();

            refreshTable();
            DataPersistence.saveProducts(inventory.getProducts());

            JOptionPane.showMessageDialog(this, "Product added successfully!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid price or quantity.");
        }
    }

    private void onDelete(ActionEvent e) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        Product p = inventory.getProducts().get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete product:\n" + p.getName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            undoStack.push(new AdminAction(AdminAction.ActionType.DELETE, p));
            updateUndoButton();

            inventory.removeProduct(p);
            refreshTable();
            DataPersistence.saveProducts(inventory.getProducts());
        }
    }

    private void performUndo() {
        if (undoStack.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No actions to undo.");
            return;
        }

        AdminAction action = undoStack.pop();

        switch (action.getType()) {
            case ADD:
                inventory.removeProduct(action.getProductSnapshot());
                break;

            case DELETE:
                inventory.addProduct(action.getProductSnapshot());
                break;

            case UPDATE:
                Product prev = action.getPreviousState();
                for (Product p : inventory.getProducts()) {
                    if (p.getID().equals(action.getProductSnapshot().getID())) {
                        p.setID(prev.getID());
                        p.setName(prev.getName());
                        p.setCategory(prev.getCategory());
                        p.setPrice(prev.getPrice());
                        p.setQuantity(prev.getQuantity());
                        break;
                    }
                }
                break;
        }

        refreshTable();
        DataPersistence.saveProducts(inventory.getProducts());
        updateUndoButton();
    }

    private void showSortedByPrice() {
        PriorityQueue<Product> heap =
                new PriorityQueue<>(Comparator.comparingDouble(Product::getPrice));
        heap.addAll(inventory.getProducts());

        StringBuilder sb = new StringBuilder("Products by ascending price:\n\n");

        while (!heap.isEmpty()) {
            sb.append(heap.poll()).append("\n");
        }

        JTextArea area = new JTextArea(sb.toString(), 16, 60);
        area.setEditable(false);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Sorted by Price (Heap)",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void searchByName(String q) {
        if (q.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a name to search.");
            return;
        }

        List<Product> list = inventory.getProducts();
        StringBuilder sb = new StringBuilder();

        for (Product p : list) {
            if (p.getName().toLowerCase().contains(q.toLowerCase())) {
                sb.append(p).append("\n");
            }
        }

        if (sb.length() == 0) sb.append("No products found.");

        JTextArea area = new JTextArea(sb.toString(), 12, 60);
        area.setEditable(false);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Search Results",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void refreshTable() {
        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
    }

    private void updateUndoButton() {
        undoBtn.setEnabled(!undoStack.isEmpty());

        if (undoStack.isEmpty()) {
            undoStatusLabel.setText("No actions to undo");
        } else {
            undoStatusLabel.setText("Undo available (" + undoStack.size() + ")");
        }
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import javax.swing.table.AbstractTableModel;

public class AdminPanel extends JPanel {
    private final Inventory inventory;
    private final JTable table;
    private final ProductTableModel tableModel;
    private final JTextField idF = new JTextField();
    private final JTextField nameF = new JTextField();
    private final JTextField catF = new JTextField();
    private final JTextField priceF = new JTextField();
    private final JTextField qtyF = new JTextField();
    private final JTextField searchF = new JTextField();
    
    // FEATURE 1: Stack for Undo functionality
    private final Stack<AdminAction> undoStack = new Stack<>();
    private final JButton undoBtn = new JButton("Undo Last Action");
    private final JLabel undoStatusLabel = new JLabel("No actions to undo");

    public AdminPanel(Inventory inventory) {
        this.inventory = inventory;
        setLayout(new BorderLayout(8,8));

        tableModel = new ProductTableModel(inventory.getProducts());
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(2, 6, 6, 6));
        form.add(new JLabel("ID"));
        form.add(new JLabel("Name"));
        form.add(new JLabel("Category"));
        form.add(new JLabel("Price"));
        form.add(new JLabel("Qty"));
        form.add(new JLabel()); // spacer

        form.add(idF);
        form.add(nameF);
        form.add(catF);
        form.add(priceF);
        form.add(qtyF);

        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(this::onAdd);
        form.add(addBtn);

        add(form, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton updateBtn = new JButton("Update Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton viewAllBtn = new JButton("View All");
        JButton sortByPriceBtn = new JButton("View Sorted by Price (Heap)");
        JButton searchBtn = new JButton("Search Name");
        JButton saveBtn = new JButton("Save Data");

        updateBtn.addActionListener(this::onUpdate);
        deleteBtn.addActionListener(this::onDelete);
        viewAllBtn.addActionListener(e -> refreshTable());
        sortByPriceBtn.addActionListener(e -> showSortedByPrice());
        searchBtn.addActionListener(e -> searchByName());
        saveBtn.addActionListener(e -> saveData());
        
        // FEATURE 1: Undo button configuration
        undoBtn.addActionListener(e -> performUndo());
        undoBtn.setEnabled(false);

        bottom.add(updateBtn);
        bottom.add(deleteBtn);
        bottom.add(viewAllBtn);
        bottom.add(sortByPriceBtn);
        bottom.add(new JLabel("Name:"));
        searchF.setColumns(12);
        bottom.add(searchF);
        bottom.add(searchBtn);
        bottom.add(saveBtn);
        bottom.add(undoBtn);

        add(bottom, BorderLayout.SOUTH);
        
        // Add undo status label at top
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(undoStatusLabel);
        add(topPanel, BorderLayout.PAGE_START);
    }

    private void onAdd(ActionEvent e) {
        try {
            // FEATURE 4: Improved error messages with specific validation
            String id = idF.getText().trim();
            String category = catF.getText().trim();
            String name = nameF.getText().trim();
            String priceStr = priceF.getText().trim();
            String qtyStr = qtyF.getText().trim();
            
            // Validate all fields
            if (id.isEmpty()) {
                showError("Product ID cannot be empty.");
                idF.requestFocus();
                return;
            }
            if (name.isEmpty()) {
                showError("Product name cannot be empty.");
                nameF.requestFocus();
                return;
            }
            if (category.isEmpty()) {
                showError("Category cannot be empty.");
                catF.requestFocus();
                return;
            }
            if (priceStr.isEmpty()) {
                showError("Price cannot be empty.");
                priceF.requestFocus();
                return;
            }
            if (qtyStr.isEmpty()) {
                showError("Quantity cannot be empty.");
                qtyF.requestFocus();
                return;
            }
            
            // Check for duplicate ID
            for (Product existing : inventory.getProducts()) {
                if (existing.getID().equals(id)) {
                    showError("Product ID '" + id + "' already exists. Please use a unique ID.");
                    idF.requestFocus();
                    return;
                }
            }
            
            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    showError("Price must be a positive number greater than 0.");
                    priceF.requestFocus();
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Invalid price format. Please enter a valid number (e.g., 19.99).");
                priceF.requestFocus();
                return;
            }
            
            int quantity;
            try {
                quantity = Integer.parseInt(qtyStr);
                if (quantity < 0) {
                    showError("Quantity cannot be negative. Please enter 0 or a positive number.");
                    qtyF.requestFocus();
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Invalid quantity format. Please enter a whole number (e.g., 10).");
                qtyF.requestFocus();
                return;
            }
            
            Product p = new Product(id, category, name, price, quantity);
            inventory.addProduct(p);
            
            // FEATURE 1: Push ADD action to undo stack
            undoStack.push(new AdminAction(AdminAction.ActionType.ADD, p));
            updateUndoButton();
            
            refreshTable();
            clearForm();
            showSuccess("Product '" + name + "' added successfully!");
        } catch (Exception ex) {
            showError("Unexpected error while adding product: " + ex.getMessage());
        }
    }

    private void onUpdate(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) { 
            showError("Please select a product row to update."); 
            return; 
        }
        
        Product p = inventory.getProducts().get(row);
        
        // FEATURE 1: Store previous state for undo
        Product previousState = new Product(p.getID(), p.getCategory(), p.getName(), 
                                           p.getPrice(), p.getQuantity());
        
        try {
            boolean hasChanges = false;
            
            if (!idF.getText().trim().isEmpty()) {
                String newId = idF.getText().trim();
                // Check if new ID conflicts with existing products
                for (Product existing : inventory.getProducts()) {
                    if (existing != p && existing.getID().equals(newId)) {
                        showError("Product ID '" + newId + "' is already in use. Please choose a different ID.");
                        return;
                    }
                }
                p.setID(newId);
                hasChanges = true;
            }
            
            if (!nameF.getText().trim().isEmpty()) {
                p.setName(nameF.getText().trim());
                hasChanges = true;
            }
            
            if (!catF.getText().trim().isEmpty()) {
                p.setCategory(catF.getText().trim());
                hasChanges = true;
            }
            
            if (!priceF.getText().trim().isEmpty()) {
                try {
                    double price = Double.parseDouble(priceF.getText().trim());
                    if (price <= 0) {
                        showError("Price must be a positive number greater than 0.");
                        return;
                    }
                    p.setPrice(price);
                    hasChanges = true;
                } catch (NumberFormatException ex) {
                    showError("Invalid price format. Please enter a valid number (e.g., 19.99).");
                    return;
                }
            }
            
            if (!qtyF.getText().trim().isEmpty()) {
                try {
                    int qty = Integer.parseInt(qtyF.getText().trim());
                    if (qty < 0) {
                        showError("Quantity cannot be negative.");
                        return;
                    }
                    p.setQuantity(qty);
                    hasChanges = true;
                } catch (NumberFormatException ex) {
                    showError("Invalid quantity format. Please enter a whole number (e.g., 10).");
                    return;
                }
            }
            
            if (!hasChanges) {
                showError("No changes made. Please enter at least one field to update.");
                return;
            }
            
            // FEATURE 1: Push UPDATE action to undo stack
            undoStack.push(new AdminAction(AdminAction.ActionType.UPDATE, previousState, p));
            updateUndoButton();
            
            refreshTable();
            clearForm();
            showSuccess("Product '" + p.getName() + "' updated successfully!");
        } catch (Exception ex) {
            showError("Update failed: " + ex.getMessage());
        }
    }

    private void onDelete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) { 
            showError("Please select a product row to delete."); 
            return; 
        }
        
        Product p = inventory.getProducts().get(row);
        
        // FEATURE 3: Confirmation dialog for destructive action
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the product:\n\n" +
            "ID: " + p.getID() + "\n" +
            "Name: " + p.getName() + "\n" +
            "Category: " + p.getCategory() + "\n\n" +
            "This action can be undone using the Undo button.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return; // User cancelled
        }
        
        // FEATURE 1: Push DELETE action to undo stack (before removing)
        undoStack.push(new AdminAction(AdminAction.ActionType.DELETE, p));
        updateUndoButton();
        
        inventory.removeProduct(p);
        refreshTable();
        showSuccess("Product '" + p.getName() + "' deleted successfully!");
    }
    
    // FEATURE 1: Undo functionality implementation
    private void performUndo() {
        if (undoStack.isEmpty()) {
            showError("No actions to undo.");
            return;
        }
        
        AdminAction action = undoStack.pop();
        
        try {
            switch (action.getType()) {
                case ADD:
                    // Undo ADD: remove the product
                    Product addedProduct = action.getProductSnapshot();
                    inventory.removeProduct(addedProduct);
                    showSuccess("Undone: Removed product '" + addedProduct.getName() + "'");
                    break;
                    
                case DELETE:
                    // Undo DELETE: re-add the product
                    Product deletedProduct = action.getProductSnapshot();
                    inventory.addProduct(deletedProduct);
                    showSuccess("Undone: Restored product '" + deletedProduct.getName() + "'");
                    break;
                    
                case UPDATE:
                    // Undo UPDATE: restore previous state
                    Product previousState = action.getPreviousState();
                    Product currentState = action.getProductSnapshot();
                    
                    // Find and update the product
                    for (int i = 0; i < inventory.getProducts().size(); i++) {
                        Product p = inventory.getProducts().get(i);
                        if (p.getID().equals(currentState.getID())) {
                            p.setID(previousState.getID());
                            p.setName(previousState.getName());
                            p.setCategory(previousState.getCategory());
                            p.setPrice(previousState.getPrice());
                            p.setQuantity(previousState.getQuantity());
                            break;
                        }
                    }
                    showSuccess("Undone: Reverted changes to product '" + previousState.getName() + "'");
                    break;
            }
            
            refreshTable();
            updateUndoButton();
            
        } catch (Exception ex) {
            showError("Failed to undo action: " + ex.getMessage());
        }
    }
    
    // FEATURE 1: Update undo button state
    private void updateUndoButton() {
        undoBtn.setEnabled(!undoStack.isEmpty());
        if (undoStack.isEmpty()) {
            undoStatusLabel.setText("No actions to undo");
        } else {
            AdminAction lastAction = undoStack.peek();
            undoStatusLabel.setText("Last action: " + lastAction.getType() + 
                                   " - " + undoStack.size() + " action(s) in history");
        }
    }

    private void showSortedByPrice() {
        // Use a min-heap to demonstrate heap-based ordering
        PriorityQueue<Product> heap = new PriorityQueue<>(Comparator.comparingDouble(Product::getPrice));
        heap.addAll(inventory.getProducts());
        StringBuilder sb = new StringBuilder("Products by ascending price:\n\n");
        while (!heap.isEmpty()) sb.append(heap.poll()).append("\n");
        JTextArea area = new JTextArea(sb.toString(), 16, 60);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Sorted by Price (Heap)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void searchByName() {
        String q = searchF.getText().trim().toLowerCase();
        if (q.isEmpty()) { 
            showError("Please enter a product name to search for."); 
            return; 
        }
        List<Product> list = inventory.getProducts();
        StringBuilder sb = new StringBuilder("Matches for \"" + q + "\":\n\n");
        int count = 0;
        for (Product p : list) {
            if (p.getName() != null && p.getName().toLowerCase().contains(q)) {
                sb.append(p).append("\n");
                count++;
            }
        }
        if (count == 0) {
            sb.append("No products found matching '").append(q).append("'.\n");
        } else {
            sb.insert(0, "Found " + count + " product(s) matching \"" + q + "\":\n\n");
        }
        JTextArea area = new JTextArea(sb.toString(), 12, 60);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Search Results", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // FEATURE 5: Data persistence
    private void saveData() {
        boolean success = DataPersistence.saveProducts(inventory.getProducts());
        if (success) {
            showSuccess("Product data saved successfully to products.txt!");
        } else {
            showError("Failed to save product data. Please check file permissions.");
        }
    }

    private void refreshTable() { 
        ((AbstractTableModel) table.getModel()).fireTableDataChanged(); 
    }
    
    private void clearForm() { 
        idF.setText(""); 
        nameF.setText(""); 
        catF.setText(""); 
        priceF.setText(""); 
        qtyF.setText(""); 
    }
    
    // FEATURE 4: Improved error and success messaging
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
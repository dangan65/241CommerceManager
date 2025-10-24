

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
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

        updateBtn.addActionListener(this::onUpdate);
        deleteBtn.addActionListener(this::onDelete);
        viewAllBtn.addActionListener(e -> refreshTable());
        sortByPriceBtn.addActionListener(e -> showSortedByPrice());
        searchBtn.addActionListener(e -> searchByName());

        bottom.add(updateBtn);
        bottom.add(deleteBtn);
        bottom.add(viewAllBtn);
        bottom.add(sortByPriceBtn);
        bottom.add(new JLabel("Name:"));
        searchF.setColumns(12);
        bottom.add(searchF);
        bottom.add(searchBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    private void onAdd(ActionEvent e) {
        try {
            Product p = new Product(
                    idF.getText().trim(),
                    catF.getText().trim(),
                    nameF.getText().trim(),
                    Double.parseDouble(priceF.getText().trim()),
                    Integer.parseInt(qtyF.getText().trim())
            );
            inventory.addProduct(p);
            refreshTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
        }
    }

    private void onUpdate(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row."); return; }
        Product p = inventory.getProducts().get(row);
        try {
            if (!idF.getText().trim().isEmpty()) p.setID(idF.getText().trim());
            if (!nameF.getText().trim().isEmpty()) p.setName(nameF.getText().trim());
            if (!catF.getText().trim().isEmpty()) p.setCategory(catF.getText().trim());
            if (!priceF.getText().trim().isEmpty()) p.setPrice(Double.parseDouble(priceF.getText().trim()));
            if (!qtyF.getText().trim().isEmpty()) p.setQuantity(Integer.parseInt(qtyF.getText().trim()));
            refreshTable();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }

    private void onDelete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row."); return; }
        Product p = inventory.getProducts().get(row);
        inventory.removeProduct(p);
        refreshTable();
    }

    private void showSortedByPrice() {
        // Use a min-heap to demonstrate heap-based ordering
        PriorityQueue<Product> heap = new PriorityQueue<>(Comparator.comparingDouble(Product::getPrice));
        heap.addAll(inventory.getProducts());
        StringBuilder sb = new StringBuilder("Products by ascending price:\n");
        while (!heap.isEmpty()) sb.append(heap.poll()).append("\n");
        JTextArea area = new JTextArea(sb.toString(), 16, 60);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Sorted by Price (Heap)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void searchByName() {
        String q = searchF.getText().trim().toLowerCase();
        if (q.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter a name to search."); return; }
        List<Product> list = inventory.getProducts();
        StringBuilder sb = new StringBuilder("Matches for \"" + q + "\":\n");
        for (Product p : list) {
            if (p.getName() != null && p.getName().toLowerCase().contains(q)) {
                sb.append(p).append("\n");
            }
        }
        if (sb.toString().endsWith(":\n")) sb.append("(no results)\n");
        JTextArea area = new JTextArea(sb.toString(), 12, 60);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Search by Name", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshTable() { ((AbstractTableModel) table.getModel()).fireTableDataChanged(); }
    private void clearForm() { idF.setText(""); nameF.setText(""); catF.setText(""); priceF.setText(""); qtyF.setText(""); }
}



import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ProductTableModel extends AbstractTableModel {
    private final String[] cols = {"ID", "Name", "Category", "Price", "Qty"};
    private final List<Product> ref;

    public ProductTableModel(List<Product> backingList) {
        this.ref = backingList; // live view of Inventory.getProducts()
    }

    @Override public int getRowCount() { return ref.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override public Object getValueAt(int r, int c) {
        Product p = ref.get(r);
        switch (c) {
            case 0: return p.getID();
            case 1: return p.getName();
            case 2: return p.getCategory();
            case 3: return p.getPrice();
            case 4: return p.getQuantity();
            default: return "";
        }
    }

    @Override public boolean isCellEditable(int r, int c) { return false; }
}


import java.util.List;
import javax.swing.table.AbstractTableModel;
/**
 * ProductTableModel provides JTable model for displaying product inventory.
 * 
 * Columns: ID, Name, Category, Price, Quantity
 * 
 * Features:
 * - Live view of inventory (backed by same list)
 * - Read-only cells (not editable via table)
 * - Automatic updates when inventory changes
 * 
 * Used by both AdminPanel and ShopPanel for product display.
 */

public class ProductTableModel extends AbstractTableModel {
    private final String[] cols = {"ID", "Name", "Category", "Price", "Qty"};
    private final List<Product> ref;

    public ProductTableModel(List<Product> backingList) {
        this.ref = backingList; 
    }

    @Override 
    public int getRowCount() { 
        return ref.size(); 
    }

    @Override 
    public int getColumnCount() { 
        return cols.length; 
    }

    @Override 
    public String getColumnName(int c) { 
        return cols[c]; 
    }

    @Override 
    public Object getValueAt(int r, int c) {
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

    @Override
    public boolean isCellEditable(int r, int c) {
        // ONLY Qty editable
        return c == 4;
    }

    @Override
    public void setValueAt(Object v, int r, int c) {
        Product p = ref.get(r);

        switch (c) {
            case 4: // Qty
                try {
                    int qty = Integer.parseInt(v.toString());
                    if (qty < 0) qty = 0;
                    p.setQuantity(qty);
                } catch (Exception ex) {
                    // ignore invalid input
                }
                break;
        }

        fireTableRowsUpdated(r, r);
    }
}

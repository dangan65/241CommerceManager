import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OrderProcessingPanel extends JPanel {

    private final OrdersHistory history;

    private final DefaultTableModel pendingModel = new DefaultTableModel(
            new Object[]{"Order ID", "User", "Total", "Status"}, 0);

    private final DefaultTableModel processingModel = new DefaultTableModel(
            new Object[]{"Order ID", "User", "Total", "Status"}, 0);

    private final DefaultTableModel completedModel = new DefaultTableModel(
            new Object[]{"Order ID", "User", "Total", "Status"}, 0);

    private final JTable pendingTable = new JTable(pendingModel);
    private final JTable processingTable = new JTable(processingModel);
    private final JTable completedTable = new JTable(completedModel);

    public OrderProcessingPanel(OrdersHistory history) {
        this.history = history;

        setLayout(new BorderLayout(10, 10));

        JPanel tablesPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        tablesPanel.add(makePanel("Pending Orders", pendingTable));
        tablesPanel.add(makePanel("Processing Orders", processingTable));
        tablesPanel.add(makePanel("Completed Orders", completedTable));

        add(tablesPanel, BorderLayout.CENTER);

        JButton processNext = new JButton("Process Next Pending");
        JButton completeNext = new JButton("Complete Next Processing");
        JButton stats = new JButton("View Statistics");
        JButton refresh = new JButton("Refresh");

        processNext.addActionListener(e -> {
            history.processNextPendingOrder();
            refreshTables();
        });

        completeNext.addActionListener(e -> {
            history.completeNextOrder();
            refreshTables();
        });

        stats.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        history.getProcessingStatistics(),
                        "Order Statistics",
                        JOptionPane.INFORMATION_MESSAGE));

        refresh.addActionListener(e -> refreshTables());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttons.add(processNext);
        buttons.add(completeNext);
        buttons.add(stats);
        buttons.add(refresh);

        add(buttons, BorderLayout.SOUTH);

        refreshTables();
    }

    private JPanel makePanel(String title, JTable table) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(lbl, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void refreshTables() {
        loadTable(pendingModel, history.getPendingOrders());
        loadTable(processingModel, history.getProcessingOrders());
        loadTable(completedModel, history.getCompletedOrders());
    }

    private void loadTable(DefaultTableModel model, List<Order> list) {
        model.setRowCount(0);
        for (Order o : list) {
            model.addRow(new Object[]{
                    o.getOrderId(),
                    o.getUsername(),
                    String.format("%.2f", o.getTotal()),
                    o.getStatusDisplay()
            });
        }
    }
}

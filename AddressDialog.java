import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * AddressDialog collects shipping address information during checkout.
 * Used to calculate state-based tax rates.
 */
public class AddressDialog extends JDialog {
    private final JTextField streetField;
    private final JTextField cityField;
    private final JComboBox<String> stateCombo;
    private final JTextField zipField;
    private final JLabel taxRateLabel;
    
    private boolean confirmed = false;
    
    public AddressDialog(Window parent) {
        super(parent, "Shipping Address", ModalityType.APPLICATION_MODAL);
        
        setLayout(new BorderLayout(10, 10));
        setSize(450, 300);
        setLocationRelativeTo(parent);
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Enter Shipping Address");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Street
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Street Address:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        streetField = new JTextField(25);
        formPanel.add(streetField, gbc);
        
        // City
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("City:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        cityField = new JTextField(25);
        formPanel.add(cityField, gbc);
        
        // State
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("State:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        String[] states = TaxCalculator.getAllStateCodes();
        java.util.Arrays.sort(states);
        stateCombo = new JComboBox<>(states);
        stateCombo.setSelectedItem("PA"); // Default to Pennsylvania
        stateCombo.addActionListener(e -> updateTaxRate());
        formPanel.add(stateCombo, gbc);
        
        // ZIP
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("ZIP Code:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        zipField = new JTextField(10);
        formPanel.add(zipField, gbc);
        
        // Tax rate display
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        taxRateLabel = new JLabel("Tax Rate: 6.00%");
        taxRateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        taxRateLabel.setForeground(new Color(0, 100, 0));
        formPanel.add(taxRateLabel, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton confirmBtn = new JButton("Continue to Payment");
        JButton cancelBtn = new JButton("Cancel");
        
        confirmBtn.addActionListener(e -> {
            if (validateAddress()) {
                confirmed = true;
                dispose();
            }
        });
        
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set initial tax rate
        updateTaxRate();
        
        // Enter key confirms
        getRootPane().setDefaultButton(confirmBtn);
    }
    
    private void updateTaxRate() {
        String state = (String) stateCombo.getSelectedItem();
        if (state != null) {
            String rateDisplay = TaxCalculator.getTaxRateDisplay(state);
            taxRateLabel.setText("Tax Rate for " + state + ": " + rateDisplay);
        }
    }
    
    private boolean validateAddress() {
        if (streetField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Street address is required.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            streetField.requestFocus();
            return false;
        }
        
        if (cityField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "City is required.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            cityField.requestFocus();
            return false;
        }
        
        String zip = zipField.getText().trim();
        if (zip.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "ZIP code is required.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            zipField.requestFocus();
            return false;
        }
        
        // Basic ZIP validation (5 digits or 5+4 format)
        if (!zip.matches("\\d{5}(-\\d{4})?")) {
            JOptionPane.showMessageDialog(this, 
                "Invalid ZIP code format.\nPlease enter 5 digits (e.g., 19104) or ZIP+4 format (e.g., 19104-1234).", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            zipField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getFullAddress() {
        return String.format("%s, %s, %s %s",
            streetField.getText().trim(),
            cityField.getText().trim(),
            stateCombo.getSelectedItem(),
            zipField.getText().trim());
    }
    
    public String getStateCode() {
        return (String) stateCombo.getSelectedItem();
    }
    
    public String getStreet() {
        return streetField.getText().trim();
    }
    
    public String getCity() {
        return cityField.getText().trim();
    }
    
    public String getZip() {
        return zipField.getText().trim();
    }
}
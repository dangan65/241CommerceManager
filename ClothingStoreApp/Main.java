package ClothingStoreApp;
public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginWindow();                         // your login window
            new ProductStoreFrame("guest").setVisible(true);  // products UI
    });
}

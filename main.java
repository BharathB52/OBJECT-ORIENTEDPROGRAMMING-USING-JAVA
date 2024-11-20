import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class SmallBasketApp extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/orders";
    private static final String USER = "aswin";
    private static final String PASSWORD = "aswin";
    // Updated prices for specified items
    double[] MRPS = {99999.00, 799.99, 749.00, 2999.00, 19.99, 699.00, 600.00, 9.99, 199.99, 5.99};


    public SmallBasketApp() {
        setTitle("SmallBasket"); // Updated title
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create buttons
        JButton viewButton = new JButton("View Orders");
        JButton deleteButton = new JButton("Delete Order");
        JButton buyItemButton = new JButton("Buy Item");

        // Add action listeners
        viewButton.addActionListener(e -> openViewOrdersFrame());
        deleteButton.addActionListener(e -> openDeleteOrderFrame());
        buyItemButton.addActionListener(e -> openBuyItemFrame());

        // Set up GridLayout panel and add buttons
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.add(viewButton);
        panel.add(deleteButton);
        panel.add(buyItemButton);

        // Add panel to frame
        add(panel);
    }

    private void openBuyItemFrame() {
        JFrame buyFrame = new JFrame("Buy Item - SmallBasket");
        buyFrame.setSize(400, 400);
        buyFrame.setLocationRelativeTo(null);
        buyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 5, 5));
    
        String[] items = {"iPhone", "Asus Laptop", "Java Programming Book", "Logitech Mouse", "REC Cashew Nuts", 
                          "Scientific Calculator", "Steel Waterbottle", "Teddy Bear", "Titan Watch", "Sunrise Coffee Powder"};
    
        String[] sellers = {"Apple Pvt.", "Asus India Pvt.", "Fatim Stores", "Logitech Technologies Pvt.", 
                            "REC Botanical Garden", "Casio Pvt.", "Sitha Pvt.", "ABC Handcrafts Pvt.", 
                            "Titan India Pvt.", "NESCAFE Pvt."};
    
        JTextField nameField = new JTextField();
        JTextField addressField = new JTextField();
    
        JComboBox<String> itemComboBox = new JComboBox<>(items);
        JComboBox<Integer> quantityComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 10});
        JLabel sellerNameLabel = new JLabel(sellers[0]);
        JComboBox<String> paymentMethodComboBox = new JComboBox<>(new String[]{"GPay", "Net Banking", "Credit Card", "Debit Card", "Cash"});
        JLabel mrpLabel = new JLabel("MRP: ₹" + MRPS[0]);
        JLabel priceLabel = new JLabel("Total Price: ₹" + MRPS[0]);
        JButton buyButton = new JButton("Buy");
    
        itemComboBox.addActionListener(e -> {
            int selectedIndex = itemComboBox.getSelectedIndex();
            sellerNameLabel.setText(sellers[selectedIndex]);
            mrpLabel.setText("MRP: ₹" + MRPS[selectedIndex]);
            updatePrice(itemComboBox, quantityComboBox, priceLabel);
        });
    
        quantityComboBox.addActionListener(e -> updatePrice(itemComboBox, quantityComboBox, priceLabel));
    
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Select Item:"));
        formPanel.add(itemComboBox);
        formPanel.add(new JLabel("MRP for One Item:"));
        formPanel.add(mrpLabel);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(quantityComboBox);
        formPanel.add(new JLabel("Seller Name:"));
        formPanel.add(sellerNameLabel);
        formPanel.add(new JLabel("Payment Method:"));
        formPanel.add(paymentMethodComboBox);
        formPanel.add(priceLabel);
        formPanel.add(buyButton);
    
        buyFrame.add(formPanel);
        buyFrame.setVisible(true);
    
        buyButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String selectedItem = (String) itemComboBox.getSelectedItem();
            int quantity = (Integer) quantityComboBox.getSelectedItem();
            String sellerName = sellerNameLabel.getText();
            String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
            double price = MRPS[itemComboBox.getSelectedIndex()] * quantity;
    
            if (name.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(buyFrame, "Please enter your Name and Address.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            int rowsInserted = insertOrder(name, selectedItem, price, sellerName, address, paymentMethod);
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(buyFrame, "Purchase was successful!");
                buyFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(buyFrame, "Error processing purchase.");
            }
        });
    }
    
    
    

    private void updatePrice(JComboBox<String> itemComboBox, JComboBox<Integer> quantityComboBox, JLabel priceLabel) {
        // Get the selected item's index
        int selectedIndex = itemComboBox.getSelectedIndex();
        
        // Get the quantity selected
        int quantity = (Integer) quantityComboBox.getSelectedItem();
        
        // Calculate the total price based on the MRP and quantity
        double total = MRPS[selectedIndex] * quantity;
        
        // Update the price label to show the calculated total price
        priceLabel.setText("Total Price: ₹" + String.format("%.2f", total));
    }
    
    

    private void openViewOrdersFrame() {
        JFrame viewFrame = new JFrame("View Orders - SmallBasket");
        viewFrame.setSize(600, 400);
        viewFrame.setLocationRelativeTo(null);
        viewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
        // Create a table model and set up the JTable
        DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Order ID", "Name", "Product", "Price", "Seller", "Address", "Payment Method"}, 0);
        JTable table = new JTable(tableModel);
    
        // Populate the table with data from the database
        String sql = "SELECT id, name, product_name, product_price, seller_name, address, method_of_payment FROM orders";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String product = resultSet.getString("product_name");
                double price = resultSet.getDouble("product_price");
                String seller = resultSet.getString("seller_name");
                String address = resultSet.getString("address");
                String paymentMethod = resultSet.getString("method_of_payment");
                tableModel.addRow(new Object[]{id, name, product, price, seller, address, paymentMethod});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(viewFrame, "Error retrieving orders from the database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    
        // Add the table to a JScrollPane and add it to the frame
        JScrollPane scrollPane = new JScrollPane(table);
        viewFrame.add(scrollPane);
    
        viewFrame.setVisible(true);
    }
    private int deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, orderId);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void openDeleteOrderFrame() {
        JFrame deleteFrame = new JFrame("Delete Order - SmallBasket");
        deleteFrame.setSize(600, 400);
        deleteFrame.setLocationRelativeTo(null);
        deleteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
        // Create a table model and set up the JTable
        DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Order ID", "Name", "Product", "Price", "Seller", "Address", "Payment Method"}, 0);
        JTable table = new JTable(tableModel);
    
        // Populate the table with data from the database
        String sql = "SELECT id, name, product_name, product_price, seller_name, address, method_of_payment FROM orders";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String product = resultSet.getString("product_name");
                double price = resultSet.getDouble("product_price");
                String seller = resultSet.getString("seller_name");
                String address = resultSet.getString("address");
                String paymentMethod = resultSet.getString("method_of_payment");
                tableModel.addRow(new Object[]{id, name, product, price, seller, address, paymentMethod});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(deleteFrame, "Error retrieving orders from the database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    
        // Add the table to a JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
    
        // Create a delete button
        JButton deleteButton = new JButton("Delete Selected Order");
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(deleteFrame, "Please select an order to delete.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            int orderId = (int) tableModel.getValueAt(selectedRow, 0); // Get the Order ID
            int confirm = JOptionPane.showConfirmDialog(deleteFrame, 
                "Are you sure you want to delete this order?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
    
            if (confirm == JOptionPane.YES_OPTION) {
                int rowsDeleted = deleteOrder(orderId);
                if (rowsDeleted > 0) {
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(deleteFrame, "Order deleted successfully.");
                } else {
                    JOptionPane.showMessageDialog(deleteFrame, "Error deleting order.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    
        // Set up the frame layout
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(deleteButton, BorderLayout.SOUTH);
    
        deleteFrame.add(panel);
        deleteFrame.setVisible(true);
    }

    private int insertOrder(String name, String productName, double productPrice, String sellerName, String address, String paymentMethod) {
        String sql = "INSERT INTO orders (name, product_name, product_price, seller_name, address, method_of_payment) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, productName);
            preparedStatement.setDouble(3, productPrice);
            preparedStatement.setString(4, sellerName);
            preparedStatement.setString(5, address);
            preparedStatement.setString(6, paymentMethod);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SmallBasketApp app = new SmallBasketApp();
            app.setVisible(true);
        });
    }
}

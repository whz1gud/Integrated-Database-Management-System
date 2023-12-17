package managers;

import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class OrderManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void updateOrderStatus(Connection dbConnection) {
        try {
            displayAllOrders(dbConnection);

            System.out.print("Enter the Order ID to update its status: ");
            int orderId = scanner.nextInt();
            scanner.nextLine();

            if (!doesOrderExist(dbConnection, orderId)) {
                System.out.println("Order with the specified ID does not exist.");
                return;
            }

            System.out.print("Enter the new status (Paid or Delivered): ");
            String newStatus = scanner.nextLine().trim();

            if (!newStatus.equals("Paid") && !newStatus.equals("Delivered")) {
                System.out.println("Invalid status. Only 'Paid' or 'Delivered' are allowed.");
                return;
            }

            String updateSQL = "UPDATE \"Order\" SET \"Status\" = ? WHERE \"Order_ID\" = ?;";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(updateSQL)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, orderId);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Order status updated successfully.");
                } else {
                    System.out.println("Failed to update order status.");
                }
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while updating the order status.");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid order ID.");
            scanner.nextLine();
        }
    }

    public static void searchOrdersByCustomer(Connection dbConnection) {
        try {
            System.out.println("Available Customers:");
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT \"Customer_ID\", \"Name\" FROM \"Customer\"")) {
                while (rs.next()) {
                    System.out.println("Customer ID: " + rs.getInt("Customer_ID") + ", Name: " + rs.getString("Name"));
                }
            }

            int customerId;
            System.out.print("Enter a valid Customer ID: ");
            while (true) {
                try {
                    customerId = scanner.nextInt();
                    if (isValidCustomer(dbConnection, customerId)) {
                        break;
                    } else {
                        System.out.print("Invalid Customer ID. Please try again: ");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next();
                }
            }

            String query = "SELECT * FROM \"Order\" WHERE \"Customer_ID\" = ?";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
                pstmt.setInt(1, customerId);
                ResultSet rs = pstmt.executeQuery();

                System.out.println("Orders for Customer ID " + customerId + ":");
                while (rs.next()) {
                    // Output order details - adjust as necessary to match your table structure
                    System.out.println("Order ID: " + rs.getInt("Order_ID") +
                            ", Total Price: " + rs.getDouble("Total_Price") +
                            ", Date: " + rs.getDate("Date") +
                            ", Status: " + rs.getString("Status"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error occurred.");
            e.printStackTrace();
        }
    }

    private static boolean isValidCustomer(Connection dbConnection, int customerId) throws SQLException {
        String query = "SELECT EXISTS (SELECT 1 FROM \"Customer\" WHERE \"Customer_ID\" = ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

    private static void displayAllOrders(Connection dbConnection) throws SQLException {
        String selectSQL = "SELECT \"Order_ID\", \"Status\" FROM \"Order\";";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("Order_ID | Status");
            while (rs.next()) {
                int orderId = rs.getInt("Order_ID");
                String status = rs.getString("Status");
                System.out.println(orderId + "        | " + status);
            }
        }
    }

    private static boolean doesOrderExist(Connection dbConnection, int orderId) throws SQLException {
        String existSQL = "SELECT COUNT(*) FROM \"Order\" WHERE \"Order_ID\" = ?;";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(existSQL)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
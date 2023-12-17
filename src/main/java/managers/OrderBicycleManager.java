package managers;

import java.sql.*;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Set;

public class OrderBicycleManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void createOrderWithBicycles(Connection dbConnection) {
        int customerId, employeeId, bicycleId, quantity;
        int newOrderId = -1;
        Set<Integer> selectedBicycles = new HashSet<>();

        try {
            dbConnection.setAutoCommit(false); // Start transaction

            customerId = getValidCustomerId(dbConnection);
            employeeId = getValidEmployeeId(dbConnection);

            String insertOrderQuery = "INSERT INTO \"Order\" (\"Customer_ID\", \"Employee_ID\", \"Status\") VALUES (?, ?, 'Paid') RETURNING \"Order_ID\"";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(insertOrderQuery)) {
                pstmt.setInt(1, customerId);
                pstmt.setInt(2, employeeId);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    newOrderId = rs.getInt("Order_ID");
                }
            }

            String userInput;
            do {
                if (selectedBicycles.size() >= getTotalBicycleCount(dbConnection)) {
                    System.out.println("All available bicycles have been selected for the order.");
                    break;
                }

                bicycleId = getValidBicycleId(dbConnection, selectedBicycles);
                quantity = getValidQuantity();

                selectedBicycles.add(bicycleId);

                String insertOrderBicycleQuery = "INSERT INTO \"Order_Bicycle\" (\"Order_ID\", \"Bicycle_ID\", \"Quantity\") VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = dbConnection.prepareStatement(insertOrderBicycleQuery)) {
                    pstmt.setInt(1, newOrderId);
                    pstmt.setInt(2, bicycleId);
                    pstmt.setInt(3, quantity);
                    pstmt.executeUpdate();
                }

                do {
                    System.out.print("Add another bicycle to the order? (y/n): ");
                    userInput = scanner.next().trim().toLowerCase();
                    if ("y".equals(userInput)) {
                        break;
                    } else if ("n".equals(userInput)) {
                        break;
                    } else {
                        System.out.println("Invalid input. Please enter 'y' for yes or 'n' for no.");
                    }
                } while (!"y".equals(userInput) && !"n".equals(userInput));

            } while ("y".equals(userInput));

            dbConnection.commit(); // Commit transaction if all operations are successful
            System.out.println("New order created successfully with Order ID: " + newOrderId);
        } catch (SQLException e) {
            System.out.println("Database error occurred. Rolling back transaction.");
            try {
                dbConnection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Rolling back transaction.");
            try {
                dbConnection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            scanner.next();
        } finally {
            try {
                dbConnection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getTotalBicycleCount(Connection dbConnection) throws SQLException {
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM \"Bicycle\"")) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    private static int getValidCustomerId(Connection dbConnection) throws SQLException {
        int customerId;
        Statement stmt = dbConnection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT \"Customer_ID\", \"Name\" FROM \"Customer\"");

        System.out.println("Available Customers:");
        while (rs.next()) {
            System.out.println("Customer ID: " + rs.getInt("Customer_ID") + ", Name: " + rs.getString("Name"));
        }

        System.out.print("Enter a valid Customer ID: ");
        while (true) {
            try {
                customerId = scanner.nextInt();
                if (isValidCustomer(dbConnection, customerId)) {
                    return customerId;
                } else {
                    System.out.print("Invalid Customer ID. Please try again: ");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
        }
    }

    private static boolean isValidCustomer(Connection dbConnection, int customerId) throws SQLException {
        PreparedStatement pstmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM \"Customer\" WHERE \"Customer_ID\" = ?");
        pstmt.setInt(1, customerId);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        return count > 0;
    }

    private static int getValidEmployeeId(Connection dbConnection) throws SQLException {
        int employeeId;
        Statement stmt = dbConnection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT \"Employee_ID\", \"Name\" FROM \"Employee\"");

        System.out.println("Available Employees:");
        while (rs.next()) {
            System.out.println("Employee ID: " + rs.getInt("Employee_ID") + ", Name: " + rs.getString("Name"));
        }

        System.out.print("Enter a valid Employee ID: ");
        while (true) {
            try {
                employeeId = scanner.nextInt();
                if (isValidEmployee(dbConnection, employeeId)) {
                    return employeeId;
                } else {
                    System.out.print("Invalid Employee ID. Please try again: ");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
        }
    }

    private static boolean isValidEmployee(Connection dbConnection, int employeeId) throws SQLException {
        PreparedStatement pstmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM \"Employee\" WHERE \"Employee_ID\" = ?");
        pstmt.setInt(1, employeeId);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        return count > 0;
    }

    private static int getValidBicycleId(Connection dbConnection, Set<Integer> selectedBicycles) throws SQLException {
        int bicycleId;
        Statement stmt = dbConnection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT \"Bicycle_ID\", \"Model\", \"Price\", \"Type\", \"Size\", \"Color\" FROM \"Bicycle\"");

        System.out.println("Available Bicycles:");
        while (rs.next()) {
            bicycleId = rs.getInt("Bicycle_ID");
            if (!selectedBicycles.contains(bicycleId)) {
                System.out.printf("Bicycle ID: %d, Model: %s, Price: %.2f, Type: %s, Size: %d, Color: %s%n",
                        bicycleId, rs.getString("Model"), rs.getDouble("Price"),
                        rs.getString("Type"), rs.getInt("Size"), rs.getString("Color"));
            }
        }

        System.out.print("Enter a valid Bicycle ID: ");
        while (true) {
            try {
                bicycleId = scanner.nextInt();
                if (!selectedBicycles.contains(bicycleId) && isValidBicycle(dbConnection, bicycleId)) {
                    return bicycleId;
                } else {
                    System.out.print("Invalid or already selected Bicycle ID. Please try again: ");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
        }
    }

    private static boolean isValidBicycle(Connection dbConnection, int bicycleId) throws SQLException {
        PreparedStatement pstmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM \"Bicycle\" WHERE \"Bicycle_ID\" = ?");
        pstmt.setInt(1, bicycleId);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        return count > 0;
    }

    private static int getValidQuantity() {
        int quantity;
        System.out.print("Enter the quantity: ");
        while (true) {
            try {
                quantity = scanner.nextInt();
                if (quantity > 0) {
                    return quantity;
                } else {
                    System.out.print("Invalid quantity. The number must be greater than 0. Please try again: ");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
                scanner.next();
            }
        }
    }
}
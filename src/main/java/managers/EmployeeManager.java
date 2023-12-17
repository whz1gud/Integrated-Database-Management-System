package managers;

import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class EmployeeManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void removeEmployee(Connection dbConnection) {
        try {
            String checkQuery = "SELECT \"Employee_ID\", \"Name\" FROM \"Employee\" "
                    + "WHERE NOT EXISTS (SELECT 1 FROM \"Order\" WHERE \"Employee_ID\" = \"Employee\".\"Employee_ID\")";
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(checkQuery);

            if (!rs.isBeforeFirst()) {
                System.out.println("No employees are available for deletion (all are managing at least one order).");
                return;
            }

            while (rs.next()) {
                System.out.println("Employee ID: " + rs.getInt("Employee_ID") + ", Name: " + rs.getString("Name"));
            }

            System.out.print("Enter the Employee ID to delete: ");
            while (true) {
                try {
                    int employeeId = scanner.nextInt();
                    String deleteQuery = "DELETE FROM \"Employee\" WHERE \"Employee_ID\" = ? AND NOT EXISTS "
                            + "(SELECT 1 FROM \"Order\" WHERE \"Employee_ID\" = ?)";
                    try (PreparedStatement pstmt = dbConnection.prepareStatement(deleteQuery)) {
                        pstmt.setInt(1, employeeId);
                        pstmt.setInt(2, employeeId);

                        int affectedRows = pstmt.executeUpdate();
                        if (affectedRows > 0) {
                            System.out.println("Employee deleted successfully.");
                        } else {
                            System.out.println("Failed to delete employee. They may be managing an order or do not exist.");
                        }
                    }
                    break;
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a valid employee ID.");
                    scanner.next();
                } catch (SQLException e) {
                    System.out.println("Database error occurred.");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error occurred.");
            e.printStackTrace();
        }
    }
}

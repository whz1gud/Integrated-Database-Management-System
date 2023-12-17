package managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class CustomerManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void createCustomer(Connection dbConnection) {
        String name = promptForCustomerName();
        String email = promptForCustomerEmail();

        if (name == null || email == null) {
            System.out.println("Customer creation cancelled.");
            return;
        }

        String insertCustomerSQL = "INSERT INTO \"Customer\" (\"Name\", \"Email\") VALUES (?, ?);";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(insertCustomerSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Customer created successfully.");
            } else {
                System.out.println("Customer creation failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error occurred while creating the customer.");
            e.printStackTrace();
        }
    }

    private static String promptForCustomerName() {
        System.out.print("Enter customer's name: ");
        String name = scanner.nextLine().trim();

        // Basic validation: name shouldn't contain numbers
        if (name.matches(".*\\d.*")) {
            System.out.println("Invalid name. The name should not contain numbers.");
            return null;
        }
        return name;
    }

    private static String promptForCustomerEmail() {
        System.out.print("Enter customer's email: ");
        String email = scanner.nextLine().trim();

        // Basic email validation: should contain "@" and "."
        if (!email.contains("@") || !email.contains(".")) {
            System.out.println("Invalid email. Please enter a valid email address.");
            return null;
        }
        return email;
    }
}

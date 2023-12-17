package ui;

import managers.CustomerManager;
import managers.EmployeeManager;
import managers.OrderBicycleManager;
import managers.OrderManager;

import java.sql.Connection;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner = new Scanner(System.in);

    public int displayMenu() {
        while (true) {
            System.out.println("1. Register a New Customer");
            System.out.println("2. Update Order Status");
            System.out.println("3. Remove Employee");
            System.out.println("4. Create a New Order");
            System.out.println("5. Search Orders by Customer");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");

            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input, please enter a number.");
                scanner.nextLine();
            }
        }
    }

    public boolean handleUserChoice(int choice, Connection dbConnection) {
        switch (choice) {
            case 1:
                // Call the method to create a new customer.
                CustomerManager.createCustomer(dbConnection);
                break;
            case 2:
                // Call the method to update an order's status.
                OrderManager.updateOrderStatus(dbConnection);
                break;
            case 3:
                // Call the method to remove an employee.
                EmployeeManager.removeEmployee(dbConnection);
                break;
            case 4:
                // Call the method to create a new order.
                OrderBicycleManager.createOrderWithBicycles(dbConnection);
                break;
            case 5:
                // Call the method to search orders by customer.
                OrderManager.searchOrdersByCustomer(dbConnection);
                break;
            case 6:
                System.out.println("Exiting program.");
                return true;
            default:
                System.out.println("Invalid option, please try again.");
                break;
        }
        return false;
    }
}
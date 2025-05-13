import java.util.Scanner;


public class StudentManagementSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StudentManagementSystemImpl sms = new StudentManagementSystemImpl("students.txt");
        int choice;

        do {
            // Display menu
            System.out.println("\n===== STUDENT MANAGEMENT SYSTEM =====");
            System.out.println("1. Add New Student");
            System.out.println("2. View All Students");
            System.out.println("3. Search Student");
            System.out.println("4. Update Student Information");
            System.out.println("5. Delete Student");
            System.out.println("6. Generate Academic Reports");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            
            // Get user choice with input validation
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                choice = -1;
                continue;
            }

            // Process user choice
            switch (choice) {
                case 1 -> sms.addStudent();
                case 2 -> sms.viewAllStudents();
                case 3 -> sms.searchStudent();
                case 4 -> sms.updateStudent();
                case 5 -> sms.deleteStudent();
                case 6 -> sms.generateReports();
                case 0 -> System.out.println("\nThank you for using Student Management System!");
                default -> System.out.println("\nInvalid choice. Please try again.");
            }
        } while (choice != 0);
        
        scanner.close();
    }
} 
    


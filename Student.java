import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.*;

/**
 * Student class to represent individual student records
 */
class Student {
    private String rollNumber;
    private String name;
    private int age;
    private String course;
    private double gpa;

    // Default constructor
    public Student() {
        this.age = 0;
        this.gpa = 0.0;
    }

    // Parameterized constructor
    public Student(String rollNumber, String name, int age, String course, double gpa) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.age = age;
        this.course = course;
        this.gpa = gpa;
    }

    // Getters
    public String getRollNumber() { return rollNumber; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getCourse() { return course; }
    public double getGpa() { return gpa; }

    // Setters
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setCourse(String course) { this.course = course; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    @Override
    public String toString() {
        return String.format("%-12s %-25s %-8d %-15s %.2f", 
                rollNumber, name, age, course, gpa);
    }

    // Convert to CSV format for storage
    public String toCsv() {
        return String.join(",", rollNumber, name, String.valueOf(age), course, String.valueOf(gpa));
    }

    // Parse from CSV format
    public static Student fromCsv(String csv) {
        String[] parts = csv.split(",");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid CSV format for Student");
        }
        
        return new Student(
            parts[0],
            parts[1],
            Integer.parseInt(parts[2]),
            parts[3],
            Double.parseDouble(parts[4])
        );
    }
}

/**
 * StudentManagementSystemImpl class to manage operations on students
 */
class StudentManagementSystemImpl {
    private List<Student> students;
    private final String filename;
    private final Scanner scanner;

    // Constructor
    public StudentManagementSystemImpl(String filename) {
        this.filename = filename;
        this.scanner = new Scanner(System.in);
        this.students = new ArrayList<>();
        if (!loadFromFile()) {
            System.out.println("No existing student data found. Starting with an empty database.");
        } else {
            System.out.println("Loaded " + students.size() + " students from database.");
        }
    }

    // Load students from file using streams
    private boolean loadFromFile() {
        try {
            if (!Files.exists(Paths.get(filename))) {
                return false;
            }
            
            students = Files.lines(Paths.get(filename))
                    .filter(line -> !line.trim().isEmpty())
                    .map(Student::fromCsv)
                    .collect(Collectors.toCollection(ArrayList::new));
            return true;
        } catch (IOException e) {
            System.out.println("Error loading student data: " + e.getMessage());
            return false;
        }
    }

    // Save students to file using streams
    private boolean saveToFile() {
        try {
            List<String> lines = students.stream()
                    .map(Student::toCsv)
                    .collect(Collectors.toList());
            
            Files.write(Paths.get(filename), lines);
            return true;
        } catch (IOException e) {
            System.out.println("Error saving student data: " + e.getMessage());
            return false;
        }
    }

    // Find a student by roll number using streams
    private Optional<Student> findStudent(String rollNumber) {
        return students.stream()
                .filter(student -> student.getRollNumber().equals(rollNumber))
                .findFirst();
    }

    // Input validation methods
    private double getValidGpa() {
        double gpa = -1;
        while (gpa < 0 || gpa > 4.0) {
            try {
                System.out.print("Enter GPA (0.0-4.0): ");
                gpa = Double.parseDouble(scanner.nextLine().trim());
                if (gpa < 0 || gpa > 4.0) {
                    System.out.println("Invalid GPA. Please enter a value between 0.0 and 4.0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
        return gpa;
    }

    private int getValidAge() {
        int age = -1;
        while (age < 16 || age > 100) {
            try {
                System.out.print("Enter Age: ");
                age = Integer.parseInt(scanner.nextLine().trim());
                if (age < 16 || age > 100) {
                    System.out.println("Invalid age. Please enter a value between 16 and 100.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
        return age;
    }

    private String getValidRollNumber() {
        String rollNumber = "";
        while (rollNumber.trim().isEmpty()) {
            System.out.print("Enter Roll Number: ");
            rollNumber = scanner.nextLine().trim();
            if (rollNumber.isEmpty()) {
                System.out.println("Roll number cannot be empty. Please enter a valid roll number.");
            }
        }
        return rollNumber;
    }

    // Add a new student
    public void addStudent() {
        System.out.println("\n=== Add New Student ===");
        
        String rollNumber = getValidRollNumber();
        
        // Check if roll number already exists using contains and stream
        boolean rollExists = students.stream()
                .map(Student::getRollNumber)
                .anyMatch(roll -> roll.equals(rollNumber));
        
        if (rollExists) {
            System.out.println("Error: Student with roll number " + rollNumber + " already exists.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        
        int age = getValidAge();
        
        System.out.print("Enter Course: ");
        String course = scanner.nextLine().trim();
        
        double gpa = getValidGpa();

        // Create and add student
        Student newStudent = new Student(rollNumber, name, age, course, gpa);
        students.add(newStudent);
        
        if (saveToFile()) {
            System.out.println("Student added successfully!");
        } else {
            System.out.println("Error: Failed to save student data.");
            students.remove(newStudent); // Rollback if save fails
        }
    }

    // View all students with sorted output
    public void viewAllStudents() {
        if (students.isEmpty()) {
            System.out.println("\nNo students in the database.");
            return;
        }

        System.out.println("\n=== All Students ===");
        System.out.printf("%-12s %-25s %-8s %-15s %-8s%n", 
                "Roll Number", "Name", "Age", "Course", "GPA");
        System.out.println("-".repeat(68));

        // Use streams to sort and display all students
        students.stream()
                .sorted(Comparator.comparing(Student::getName))
                .forEach(System.out::println);
        
        System.out.println("\nTotal Students: " + students.size());
    }

    // Search for a student
    public void searchStudent() {
        if (students.isEmpty()) {
            System.out.println("\nNo students in the database.");
            return;
        }

        System.out.println("\n=== Search Student ===");
        System.out.print("Enter Roll Number to search: ");
        String rollNumber = scanner.nextLine().trim();

        // Use Optional from streams to handle the case where student is not found
        findStudent(rollNumber).ifPresentOrElse(
            student -> {
                System.out.println("\nStudent Found:");
                System.out.printf("%-12s %-25s %-8s %-15s %-8s%n", 
                        "Roll Number", "Name", "Age", "Course", "GPA");
                System.out.println("-".repeat(68));
                System.out.println(student);
            },
            () -> System.out.println("Student with Roll Number " + rollNumber + " not found.")
        );
    }

    // Update student information
    public void updateStudent() {
        if (students.isEmpty()) {
            System.out.println("\nNo students in the database.");
            return;
        }

        System.out.println("\n=== Update Student ===");
        System.out.print("Enter Roll Number of student to update: ");
        String rollNumber = scanner.nextLine().trim();

        // Find the student and update if found
        Optional<Student> studentOpt = findStudent(rollNumber);
        if (studentOpt.isEmpty()) {
            System.out.println("Student with Roll Number " + rollNumber + " not found.");
            return;
        }

        Student student = studentOpt.get();
        System.out.println("\nCurrent Student Information:");
        System.out.printf("%-12s %-25s %-8s %-15s %-8s%n", 
                "Roll Number", "Name", "Age", "Course", "GPA");
        System.out.println("-".repeat(68));
        System.out.println(student);

        System.out.println("\nEnter new information (leave blank to keep current value):");
        
        System.out.print("Enter Name [" + student.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) student.setName(name);
        
        System.out.print("Enter Age [" + student.getAge() + "]: ");
        String ageStr = scanner.nextLine().trim();
        if (!ageStr.isEmpty()) {
            try {
                int age = Integer.parseInt(ageStr);
                if (age >= 16 && age <= 100) {
                    student.setAge(age);
                } else {
                    System.out.println("Invalid age. Keeping current value.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid age format. Keeping current value.");
            }
        }
        
        System.out.print("Enter Course [" + student.getCourse() + "]: ");
        String course = scanner.nextLine().trim();
        if (!course.isEmpty()) student.setCourse(course);
        
        System.out.printf("Enter GPA (0.0-4.0) [%.2f]: ", student.getGpa());
        String gpaStr = scanner.nextLine().trim();
        if (!gpaStr.isEmpty()) {
            try {
                double gpa = Double.parseDouble(gpaStr);
                if (gpa >= 0.0 && gpa <= 4.0) {
                    student.setGpa(gpa);
                } else {
                    System.out.println("Invalid GPA. Keeping current value.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid GPA format. Keeping current value.");
            }
        }

        if (saveToFile()) {
            System.out.println("Student information updated successfully!");
        } else {
            System.out.println("Error: Failed to save updated information.");
            loadFromFile(); // Rollback if save fails
        }
    }

    // Delete a student
    public void deleteStudent() {
        if (students.isEmpty()) {
            System.out.println("\nNo students in the database.");
            return;
        }

        System.out.println("\n=== Delete Student ===");
        System.out.print("Enter Roll Number of student to delete: ");
        String rollNumber = scanner.nextLine().trim();

        Optional<Student> studentOpt = findStudent(rollNumber);
        if (studentOpt.isEmpty()) {
            System.out.println("Student with Roll Number " + rollNumber + " not found.");
            return;
        }

        Student student = studentOpt.get();
        System.out.println("\nStudent to be deleted:");
        System.out.printf("%-12s %-25s %-8s %-15s %-8s%n", 
                "Roll Number", "Name", "Age", "Course", "GPA");
        System.out.println("-".repeat(68));
        System.out.println(student);

        System.out.print("\nAre you sure you want to delete this student? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            students.remove(student);
            if (saveToFile()) {
                System.out.println("Student deleted successfully!");
            } else {
                System.out.println("Error: Failed to save changes.");
                loadFromFile(); // Rollback if save fails
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // Generate academic reports and analytics using streams
    public void generateReports() {
        if (students.isEmpty()) {
            System.out.println("\nNo students in the database.");
            return;
        }

        System.out.println("\n=== Academic Analytics ===");

        // Calculate average GPA using streams
        DoubleSummaryStatistics gpaStats = students.stream()
                .collect(Collectors.summarizingDouble(Student::getGpa));
        
        double averageGpa = gpaStats.getAverage();
        double maxGpa = gpaStats.getMax();
        double minGpa = gpaStats.getMin();

        // Find highest and lowest GPA students using streams
        Optional<Student> maxGpaStudent = students.stream()
                .max(Comparator.comparingDouble(Student::getGpa));
        
        Optional<Student> minGpaStudent = students.stream()
                .min(Comparator.comparingDouble(Student::getGpa));

        // Count students by course using streams and collectors
        Map<String, Long> courseStats = students.stream()
                .collect(Collectors.groupingBy(
                    Student::getCourse, 
                    TreeMap::new,    // Use TreeMap to sort courses alphabetically
                    Collectors.counting()
                ));

        // Count students by GPA range using streams
        long excellentCount = students.stream().filter(s -> s.getGpa() >= 3.5).count();
        long goodCount = students.stream().filter(s -> s.getGpa() >= 3.0 && s.getGpa() < 3.5).count();
        long averageCount = students.stream().filter(s -> s.getGpa() >= 2.0 && s.getGpa() < 3.0).count();
        long belowAverageCount = students.stream().filter(s -> s.getGpa() < 2.0).count();

        // Display analytics
        System.out.println("Total number of students: " + students.size());
        System.out.printf("Average GPA: %.2f%n", averageGpa);
        
        // Print highest GPA student
        maxGpaStudent.ifPresent(student -> 
            System.out.printf("Highest GPA: %.2f (%s, %s)%n", 
                maxGpa, student.getName(), student.getRollNumber())
        );
        
        // Print lowest GPA student
        minGpaStudent.ifPresent(student -> 
            System.out.printf("Lowest GPA: %.2f (%s, %s)%n", 
                minGpa, student.getName(), student.getRollNumber())
        );

        System.out.println("\nPerformance Distribution:");
        System.out.printf("Excellent (3.5-4.0): %d students (%.1f%%)%n", 
                excellentCount, (excellentCount * 100.0 / students.size()));
        System.out.printf("Good (3.0-3.49): %d students (%.1f%%)%n", 
                goodCount, (goodCount * 100.0 / students.size()));
        System.out.printf("Average (2.0-2.99): %d students (%.1f%%)%n", 
                averageCount, (averageCount * 100.0 / students.size()));
        System.out.printf("Below Average (<2.0): %d students (%.1f%%)%n", 
                belowAverageCount, (belowAverageCount * 100.0 / students.size()));

        System.out.println("\nEnrollment by Course:");
        courseStats.forEach((course, count) -> 
            System.out.printf("%s: %d students%n", course, count)
        );
    }
}


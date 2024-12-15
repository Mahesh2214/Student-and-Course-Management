
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class RoleBasedAccess {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
        setupDatabase();
    }

    private static void setupDatabase() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
            Statement stmt = conn.createStatement();

            // Create Course Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Course (" +
                    "course_id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "course_name VARCHAR(255) NOT NULL, " +
                    "course_code VARCHAR(100) UNIQUE NOT NULL, " +
                    "course_duration INTEGER NOT NULL)");

            // Create Student Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Student (" +
                    "student_id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                    "student_name VARCHAR(255) NOT NULL, " +
                    "student_email VARCHAR(255) UNIQUE NOT NULL, " +
                    "course_id INTEGER, " +
                    "FOREIGN KEY (course_id) REFERENCES Course(course_id))");

            System.out.println("Database setup complete.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add Student with Course Assignment
    public static void addStudent(String studentName, String studentEmail, int courseId) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
            String validateCourseQuery = "SELECT * FROM Course WHERE course_id = ?";
            PreparedStatement validateStmt = conn.prepareStatement(validateCourseQuery);
            validateStmt.setInt(1, courseId);
            ResultSet rs = validateStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Course does not exist. Student not added.");
                return;
            }

            String insertStudentQuery = "INSERT INTO Student (student_name, student_email, course_id) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertStudentQuery);
            insertStmt.setString(1, studentName);
            insertStmt.setString(2, studentEmail);
            insertStmt.setInt(3, courseId);

            insertStmt.executeUpdate();
            System.out.println("Student added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve Student Details with Course Information
    public static void fetchAllStudents() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
            String query = "SELECT s.student_id, s.student_name, s.student_email, c.course_name FROM Student s " +
                    "LEFT JOIN Course c ON s.course_id = c.course_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                System.out.printf("ID: %d, Name: %s, Email: %s, Course: %s\n",
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("student_email"),
                        rs.getString("course_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve Students Enrolled in a Specific Course
    public static void fetchStudentsByCourse(int courseId) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {   //Enter your database user_Name and your_Password 
            String query = "SELECT student_id, student_name, student_email FROM Student WHERE course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.printf("ID: %d, Name: %s, Email: %s\n",
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("student_email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update Student Details with Course Modification
    public static void updateStudent(int studentId, String newName, String newEmail, int newCourseId) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
            conn.setAutoCommit(false);

            String validateCourseQuery = "SELECT * FROM Course WHERE course_id = ?";
            PreparedStatement validateStmt = conn.prepareStatement(validateCourseQuery);
            validateStmt.setInt(1, newCourseId);
            ResultSet rs = validateStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Course does not exist. Update failed.");
                conn.rollback();
                return;
            }

            String updateStudentQuery = "UPDATE Student SET student_name = ?, student_email = ?, course_id = ? WHERE student_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateStudentQuery);
            updateStmt.setString(1, newName);
            updateStmt.setString(2, newEmail);
            updateStmt.setInt(3, newCourseId);
            updateStmt.setInt(4, studentId);

            updateStmt.executeUpdate();
            conn.commit();
            System.out.println("Student details updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete Students and Handle Course Implications
    public static void deleteStudent(int studentId) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@2214")) {
            String deleteQuery = "DELETE FROM Student WHERE student_id = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteQuery);
            stmt.setInt(1, studentId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Student deleted successfully.");
            } else {
                System.out.println("No student found with the given ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    public LoginFrame() {
        setTitle("Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        loginButton = new JButton("Login");
        messageLabel = new JLabel("", SwingConstants.CENTER);

        loginButton.addActionListener(new LoginAction());

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(new JLabel("")); // Empty cell
        add(loginButton);
        add(messageLabel);

        setVisible(true);
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if ("admin".equals(username) && "admin123".equals(password)) {
                dispose();
                new AdminDashboard();
            } else if ("student".equals(username) && "student123".equals(password)) {
                dispose();
                new StudentDashboard();
            } else {
                messageLabel.setText("Invalid credentials! Try again.");
                messageLabel.setForeground(Color.RED);
            }
        }
    }
}


class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, Admin!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton manageCoursesButton = new JButton("Manage Courses");
        JButton viewStudentsButton = new JButton("View Students");

        manageCoursesButton.addActionListener(e -> showManageCoursesDialog());
        viewStudentsButton.addActionListener(e -> showStudentDetails());

        buttonPanel.add(manageCoursesButton);
        buttonPanel.add(viewStudentsButton);

        add(welcomeLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void showManageCoursesDialog() {
        // Create a new frame for managing courses
        JFrame manageCoursesFrame = new JFrame("Manage Courses");
        manageCoursesFrame.setSize(400, 300);
        manageCoursesFrame.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel nameLabel = new JLabel("Course Name:");
        JTextField nameField = new JTextField();

        JLabel codeLabel = new JLabel("Course Code:");
        JTextField codeField = new JTextField();

        JLabel durationLabel = new JLabel("Duration:");
        JTextField durationField = new JTextField();

        JButton addButton = new JButton("Add Course");
        addButton.addActionListener(e -> {
            String courseName = nameField.getText();
            String courseCode = codeField.getText();
            int courseDuration;
            try {
                courseDuration = Integer.parseInt(durationField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(manageCoursesFrame, "Duration must be a valid number.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
                String query = "INSERT INTO Course (course_name, course_code, course_duration) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, courseName);
                stmt.setString(2, courseCode);
                stmt.setInt(3, courseDuration);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(manageCoursesFrame, "Course added successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(manageCoursesFrame, "Error adding course.");
            }
        });

        manageCoursesFrame.add(nameLabel);
        manageCoursesFrame.add(nameField);
        manageCoursesFrame.add(codeLabel);
        manageCoursesFrame.add(codeField);
        manageCoursesFrame.add(durationLabel);
        manageCoursesFrame.add(durationField);
        manageCoursesFrame.add(new JLabel()); // Empty cell
        manageCoursesFrame.add(addButton);

        manageCoursesFrame.setVisible(true);
    }

    private void showStudentDetails() {
        // Create a new frame to display students
        JFrame studentDetailsFrame = new JFrame("Student Details");
        studentDetailsFrame.setSize(600, 400);

        String[] columnNames = {"ID", "Name", "Email", "Course"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable studentTable = new JTable(tableModel);

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
            String query = "SELECT s.student_id, s.student_name, s.student_email, c.course_name FROM Student s " +
                    "LEFT JOIN Course c ON s.course_id = c.course_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("student_id");
                String name = rs.getString("student_name");
                String email = rs.getString("student_email");
                String course = rs.getString("course_name");
                tableModel.addRow(new Object[]{id, name, email, course});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(studentDetailsFrame, "Error fetching student details.");
        }

        JScrollPane scrollPane = new JScrollPane(studentTable);
        studentDetailsFrame.add(scrollPane, BorderLayout.CENTER);

        // Add update and delete buttons
        JPanel buttonPanel = new JPanel();
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");

        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        studentDetailsFrame.add(buttonPanel, BorderLayout.SOUTH);

        // Update action
        updateButton.addActionListener(e -> {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(studentDetailsFrame, "Please select a student to update.");
                return;
            }

            int studentId = (int) tableModel.getValueAt(selectedRow, 0); // Get student_id from the selected row
            String currentName = (String) tableModel.getValueAt(selectedRow, 1);
            String currentEmail = (String) tableModel.getValueAt(selectedRow, 2);
            String currentCourse = (String) tableModel.getValueAt(selectedRow, 3); // Not used in query but for UI

            // Create input fields pre-filled with current values
            JTextField nameField = new JTextField(currentName);
            JTextField emailField = new JTextField(currentEmail);

            JPanel updatePanel = new JPanel(new GridLayout(2, 2));
            updatePanel.add(new JLabel("Name:"));
            updatePanel.add(nameField);
            updatePanel.add(new JLabel("Email:"));
            updatePanel.add(emailField);

            // Show input dialog to update student details
            int result = JOptionPane.showConfirmDialog(studentDetailsFrame, updatePanel, "Update Student",
                    JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String updatedName = nameField.getText().trim();
                String updatedEmail = emailField.getText().trim();

                if (updatedName.isEmpty() || updatedEmail.isEmpty()) {
                    JOptionPane.showMessageDialog(studentDetailsFrame, "Fields cannot be empty.");
                    return;
                }

                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
                    String updateQuery = "UPDATE Student SET student_name = ?, student_email = ? WHERE student_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(updateQuery);
                    stmt.setString(1, updatedName); // Set new name
                    stmt.setString(2, updatedEmail); // Set new email
                    stmt.setInt(3, studentId); // Set student_id to identify record

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        // Update the JTable row
                        tableModel.setValueAt(updatedName, selectedRow, 1);
                        tableModel.setValueAt(updatedEmail, selectedRow, 2);
                        JOptionPane.showMessageDialog(studentDetailsFrame, "Student updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(studentDetailsFrame, "Failed to update student.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(studentDetailsFrame, "Error updating student.");
                }
            }
        });


        // Delete action
        deleteButton.addActionListener(e -> {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(studentDetailsFrame, "Please select a student to delete.");
                return;
            }

            int studentId = (int) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(studentDetailsFrame,
                    "Are you sure you want to delete this student?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
                    String deleteQuery = "DELETE FROM Student WHERE student_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(deleteQuery);
                    stmt.setInt(1, studentId);
                    stmt.executeUpdate();

                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(studentDetailsFrame, "Student deleted successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(studentDetailsFrame, "Error deleting student.");
                }
            }
        });

        studentDetailsFrame.setVisible(true);
    }

}


    class StudentDashboard extends JFrame {
        private String username;

        public StudentDashboard() {
            this.username = username; // Store the logged-in username
            setTitle("Student Dashboard");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            JLabel welcomeLabel = new JLabel("Welcome, Student!", SwingConstants.CENTER);
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));

            JButton addDetailsButton = new JButton("Add My Details");
            JButton viewDetailsButton = new JButton("View My Details");
            JButton viewCoursesButton = new JButton("View Courses");

            addDetailsButton.addActionListener(e -> showAddDetailsDialog(username));
            viewDetailsButton.addActionListener(e -> showStudentDetails());
            viewCoursesButton.addActionListener(e -> showAvailableCourses());

            buttonPanel.add(addDetailsButton);
            buttonPanel.add(viewDetailsButton);
            buttonPanel.add(viewCoursesButton);

            add(welcomeLabel, BorderLayout.NORTH);
            add(buttonPanel, BorderLayout.CENTER);

            setVisible(true);
        }

        private void showAddDetailsDialog(String username) {
            JFrame addDetailsFrame = new JFrame("Add My Details");
            addDetailsFrame.setSize(400, 300);
            addDetailsFrame.setLayout(new GridLayout(4, 2, 10, 10));

            JLabel nameLabel = new JLabel("Name:");
            JTextField nameField = new JTextField();

            JLabel emailLabel = new JLabel("Email:");
            JTextField emailField = new JTextField(username); // Pre-filled with username (email)

            JLabel courseLabel = new JLabel("Course:");
            JComboBox<String> courseComboBox = new JComboBox<>();

            // Fetch courses and populate the combo box
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
                String query = "SELECT course_id, course_name FROM Course";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    int courseId = rs.getInt("course_id");
                    String courseName = rs.getString("course_name");
                    courseComboBox.addItem(courseId + " - " + courseName);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(addDetailsFrame, "Error fetching courses.");
                return;
            }

            JButton addButton = new JButton("Add Details");
            addButton.addActionListener(e -> {
                String studentName = nameField.getText();
                String studentEmail = emailField.getText();
                String selectedCourse = (String) courseComboBox.getSelectedItem();

                if (selectedCourse == null || selectedCourse.isEmpty()) {
                    JOptionPane.showMessageDialog(addDetailsFrame, "Please select a course.");
                    return;
                }

                // Extract the course ID from the selected course
                int courseId = Integer.parseInt(selectedCourse.split(" - ")[0]);

                // Call method to add student details
                RoleBasedAccess.addStudent(studentName, studentEmail, courseId);

                JOptionPane.showMessageDialog(addDetailsFrame, "Details added successfully!");
                addDetailsFrame.dispose();
            });

            addDetailsFrame.add(nameLabel);
            addDetailsFrame.add(nameField);
            addDetailsFrame.add(emailLabel);
            addDetailsFrame.add(emailField);
            addDetailsFrame.add(courseLabel);
            addDetailsFrame.add(courseComboBox);
            addDetailsFrame.add(new JLabel()); // Empty cell
            addDetailsFrame.add(addButton);

            addDetailsFrame.setVisible(true);
        }


        private void showStudentDetails() {
        }

        private void showAddDetailsDialog() {
            JFrame addDetailsFrame = new JFrame("Add My Details");
            addDetailsFrame.setSize(400, 300);
            addDetailsFrame.setLayout(new GridLayout(4, 2, 10, 10));

            JLabel nameLabel = new JLabel("Name:");
            JTextField nameField = new JTextField();

            JLabel emailLabel = new JLabel("Email:");
            JTextField emailField = new JTextField(username); // Pre-filled with username (email)

            JLabel courseLabel = new JLabel("Course ID:");
            JTextField courseField = new JTextField();

            JButton addButton = new JButton("Add Details");
            addButton.addActionListener(e -> {
                String studentName = nameField.getText();
                String studentEmail = emailField.getText();
                int courseId;

                try {
                    courseId = Integer.parseInt(courseField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(addDetailsFrame, "Course ID must be a valid number.");
                    return;
                }

                // Call method to add student details
                RoleBasedAccess.addStudent(studentName, studentEmail, courseId);

                JOptionPane.showMessageDialog(addDetailsFrame, "Details added successfully!");
                addDetailsFrame.dispose();
            });

            addDetailsFrame.add(nameLabel);
            addDetailsFrame.add(nameField);
            addDetailsFrame.add(emailLabel);
            addDetailsFrame.add(emailField);
            addDetailsFrame.add(courseLabel);
            addDetailsFrame.add(courseField);
            addDetailsFrame.add(new JLabel()); // Empty cell
            addDetailsFrame.add(addButton);

            addDetailsFrame.setVisible(true);
        }

        private void addMyDetails() {
            JFrame addDetailsFrame = new JFrame("Add My Details");
            addDetailsFrame.setSize(400, 300);
            addDetailsFrame.setLayout(new GridLayout(3, 2, 10, 10));

            JLabel nameLabel = new JLabel("Name:");
            JTextField nameField = new JTextField();

            JLabel courseIdLabel = new JLabel("Course ID:");
            JTextField courseIdField = new JTextField();

            JButton submitButton = new JButton("Submit");

            submitButton.addActionListener(e -> {
                String name = nameField.getText();
                int courseId;

                try {
                    courseId = Integer.parseInt(courseIdField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(addDetailsFrame, "Course ID must be a valid number.");
                    return;
                }

                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
                    String query = "INSERT INTO Student (student_name, student_email, course_id) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, name);
                    stmt.setString(2, username); // Use the logged-in email
                    stmt.setInt(3, courseId);

                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(addDetailsFrame, "Details added successfully!");
                    addDetailsFrame.dispose();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(addDetailsFrame, "Error adding details.");
                }
            });

            addDetailsFrame.add(nameLabel);
            addDetailsFrame.add(nameField);
            addDetailsFrame.add(courseIdLabel);
            addDetailsFrame.add(courseIdField);
            addDetailsFrame.add(new JLabel()); // Empty cell
            addDetailsFrame.add(submitButton);

            addDetailsFrame.setVisible(true);
        }

        private void showAvailableCourses() {
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/student_course_db?user=root&password=Mahesh@123")) {
                String query = "SELECT course_name, course_code, course_duration FROM Course";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                StringBuilder courses = new StringBuilder("Available Courses:\n");
                while (rs.next()) {
                    courses.append(String.format("Name: %s, Code: %s, Duration: %d months\n",
                            rs.getString("course_name"),
                            rs.getString("course_code"),
                            rs.getInt("course_duration")));
                }

                JOptionPane.showMessageDialog(this, courses.toString(), "Courses", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error fetching courses.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

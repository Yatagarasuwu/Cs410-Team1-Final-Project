import java.sql.*;
import java.util.*;

public class GradebookShell {
    private static final Scanner scanner = new Scanner(System.in);
    private static Connection conn;
    private static String currentClassID = null;

    public static void main(String[] args) throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite:gradebook.db");
        setupDatabase();

        while (true) {
            System.out.print("> ");
            String[] command = scanner.nextLine().split(" ", 2);
            String action = command[0];
            String argsLine = command.length > 1 ? command[1] : "";

            try {
                switch (action) {
                    case "exit": return;
                    case "new-class": newClass(argsLine); break;
                    case "list-classes": listClasses(); break;
                    case "select-class": selectClass(argsLine); break;
                    case "show-class": showClass(); break;
                    case "add-category": addCategory(argsLine); break;
                    case "show-categories": showCategories(); break;
                    case "add-assignment": addAssignment(argsLine); break;
                    case "show-assignment": showAssignments(); break;
                    case "add-student": addStudent(argsLine); break;
                    case "show-students": showStudents(argsLine); break;
                    case "grade": gradeAssignment(argsLine); break;
                    case "student-grades": studentGrades(argsLine); break;
                    case "gradebook": showGradebook(); break;

                    default: System.out.println("Unknown command");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void setupDatabase() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Student (" +
            "    studentID TEXT PRIMARY KEY," +
            "    username TEXT UNIQUE," +
            "    name TEXT" +
            ");"
        );
    
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Class (" +
            "    classID TEXT PRIMARY KEY," +
            "    courseNumber TEXT," +
            "    term TEXT," +
            "    section TEXT," +
            "    description TEXT" +
            ");"
        );
    
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Category (" +
            "    categoryID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    name TEXT," +
            "    weight REAL," +
            "    classID TEXT," +
            "    FOREIGN KEY(classID) REFERENCES Class(classID)" +
            ");"
        );
    
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Assignment (" +
            "    assignmentID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    name TEXT," +
            "    description TEXT," +
            "    pointValue REAL," +
            "    categoryID INTEGER," +
            "    classID TEXT," +
            "    FOREIGN KEY(categoryID) REFERENCES Category(categoryID)," +
            "    FOREIGN KEY(classID) REFERENCES Class(classID)," +
            "    UNIQUE(classID, name)" +
            ");"
        );
    
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Enrollment (" +
            "    studentID TEXT," +
            "    classID TEXT," +
            "    PRIMARY KEY(studentID, classID)," +
            "    FOREIGN KEY(studentID) REFERENCES Student(studentID)," +
            "    FOREIGN KEY(classID) REFERENCES Class(classID)" +
            ");"
        );
    
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS Grade (" +
            "    studentID TEXT," +
            "    assignmentID INTEGER," +
            "    score REAL," +
            "    PRIMARY KEY(studentID, assignmentID)," +
            "    FOREIGN KEY(studentID) REFERENCES Student(studentID)," +
            "    FOREIGN KEY(assignmentID) REFERENCES Assignment(assignmentID)" +
            ");"
        );
    }

    private static void newClass(String args) throws SQLException {
        String[] parts = parseArgs(args, 4);
        String id = parts[0] + "-" + parts[1] + "-" + parts[2];
        PreparedStatement ps = conn.prepareStatement("INSERT INTO Class VALUES (?, ?, ?, ?, ?)");
        ps.setString(1, id);
        ps.setString(2, parts[0]);
        ps.setString(3, parts[1]);
        ps.setString(4, parts[2]);
        ps.setString(5, parts[3]);
        ps.executeUpdate();
        System.out.println("Class created: " + id);
    }

    private static void listClasses() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT Class.classID, Class.description, COUNT(Enrollment.studentID) as studentCount " +
            "FROM Class LEFT JOIN Enrollment ON Class.classID = Enrollment.classID " +
            "GROUP BY Class.classID;"
        );
        while (rs.next()) {
            System.out.printf("%s: %s [%d students]%n", rs.getString("classID"), rs.getString("description"), rs.getInt("studentCount"));
        }
    }

    private static void showClass() {
        System.out.println(currentClassID == null ? "No class selected." : "Current class: " + currentClassID);
    }

    // Adds a category to the currently selected class
private static void addCategory(String args) throws SQLException {
    if (currentClassID == null) {
        System.out.println("No class selected.");
        return;
    }
    String[] parts = parseArgs(args, 2); // name, weight
    String name = parts[0];
    double weight = Double.parseDouble(parts[1]);

    PreparedStatement ps = conn.prepareStatement(
        "INSERT INTO Category (name, weight, classID) VALUES (?, ?, ?)"
    );
    ps.setString(1, name);
    ps.setDouble(2, weight);
    ps.setString(3, currentClassID);
    ps.executeUpdate();
    System.out.println("Category added.");
}

// Displays all categories for the currently selected class
private static void showCategories() throws SQLException {
    if (currentClassID == null) {
        System.out.println("No class selected.");
        return;
    }
    PreparedStatement ps = conn.prepareStatement(
        "SELECT name, weight FROM Category WHERE classID = ?"
    );
    ps.setString(1, currentClassID);
    ResultSet rs = ps.executeQuery();
    while (rs.next()) {
        System.out.printf("%s: %.2f%%%n", rs.getString("name"), rs.getDouble("weight"));
    }
}

// Adds an assignment to a category in the currently selected class
private static void addAssignment(String args) throws SQLException {
    if (currentClassID == null) {
        System.out.println("No class selected.");
        return;
    }
    String[] parts = parseArgs(args, 4); 
    String name = parts[0];
    String description = parts[1];
    double points = Double.parseDouble(parts[2]);
    String categoryName = parts[3];

    PreparedStatement findCategory = conn.prepareStatement(
        "SELECT categoryID FROM Category WHERE name = ? AND classID = ?"
    );
    findCategory.setString(1, categoryName);
    findCategory.setString(2, currentClassID);
    ResultSet rs = findCategory.executeQuery();

    if (!rs.next()) {
        System.out.println("Category not found.");
        return;
    }
    int categoryID = rs.getInt("categoryID");

    PreparedStatement ps = conn.prepareStatement(
        "INSERT INTO Assignment (name, description, pointValue, categoryID, classID) VALUES (?, ?, ?, ?, ?)"
    );
    ps.setString(1, name);
    ps.setString(2, description);
    ps.setDouble(3, points);
    ps.setInt(4, categoryID);
    ps.setString(5, currentClassID);
    ps.executeUpdate();
    System.out.println("Assignment added.");
}

// Shows all assignments in the selected class
private static void showAssignments() throws SQLException {
    if (currentClassID == null) {
        System.out.println("No class selected.");
        return;
    }
    PreparedStatement ps = conn.prepareStatement(
        "SELECT name, description, pointValue FROM Assignment WHERE classID = ?"
    );
    ps.setString(1, currentClassID);
    ResultSet rs = ps.executeQuery();
    while (rs.next()) {
        System.out.printf("%s (%.2f points): %s%n",
            rs.getString("name"),
            rs.getDouble("pointValue"),
            rs.getString("description"));
    }
}

// Adds a student and enrolls them in the current class
private static void addStudent(String args) throws SQLException {
    if (currentClassID == null) {
        System.out.println("No class selected.");
        return;
    }
    String[] parts = parseArgs(args, 3); // id, username, name
    String id = parts[0];
    String username = parts[1];
    String name = parts[2];

    PreparedStatement ps = conn.prepareStatement(
        "INSERT OR IGNORE INTO Student (studentID, username, name) VALUES (?, ?, ?)"
    );
    ps.setString(1, id);
    ps.setString(2, username);
    ps.setString(3, name);
    ps.executeUpdate();

    PreparedStatement enroll = conn.prepareStatement(
        "INSERT OR IGNORE INTO Enrollment (studentID, classID) VALUES (?, ?)"
    );
    enroll.setString(1, id);
    enroll.setString(2, currentClassID);
    enroll.executeUpdate();

    System.out.println("Student added and enrolled.");
}

private static void selectClass(String args) throws SQLException {
    String classID = args.trim();
    
    PreparedStatement check = conn.prepareStatement("SELECT * FROM Class WHERE classID = ?");
    check.setString(1, classID);
    ResultSet rs = check.executeQuery();
    if (rs.next()) {
        currentClassID = classID;
        System.out.println("Now using class: " + classID);
    } else {
        System.out.println("Class not found: " + classID);
    }
}


// Shows all students enrolled in the current class
private static void showStudents(String args) throws SQLException {
    if (currentClassID == null) {
        System.out.println("No class selected.");
        return;
    }
    PreparedStatement ps = conn.prepareStatement(
        "SELECT Student.studentID, username, name " +
        "FROM Student JOIN Enrollment ON Student.studentID = Enrollment.studentID " +
        "WHERE Enrollment.classID = ?"
    );
    ps.setString(1, currentClassID);
    ResultSet rs = ps.executeQuery();
    while (rs.next()) {
        System.out.printf("%s (%s): %s%n",
            rs.getString("studentID"),
            rs.getString("username"),
            rs.getString("name"));
    }
}

// Assigns a grade for a student on a specific assignment
private static void gradeAssignment(String args) throws SQLException {
    if (currentClassID == null) {
        System.out.println("No class selected.");
        return;
    }
    String[] parts = parseArgs(args, 3); // studentID, assignment name, score
    String studentID = parts[0];
    String assignmentName = parts[1];
    double score = Double.parseDouble(parts[2]);

    PreparedStatement findAssignment = conn.prepareStatement(
        "SELECT assignmentID FROM Assignment WHERE name = ? AND classID = ?"
    );
    findAssignment.setString(1, assignmentName);
    findAssignment.setString(2, currentClassID);
    ResultSet rs = findAssignment.executeQuery();

    if (!rs.next()) {
        System.out.println("Assignment not found.");
        return;
    }
    int assignmentID = rs.getInt("assignmentID");

    PreparedStatement ps = conn.prepareStatement(
        "INSERT OR REPLACE INTO Grade (studentID, assignmentID, score) VALUES (?, ?, ?)"
    );
    ps.setString(1, studentID);
    ps.setInt(2, assignmentID);
    ps.setDouble(3, score);
    ps.executeUpdate();

    System.out.println("Grade recorded.");
}

// Shows all grades for a student
private static void studentGrades(String args) throws SQLException {
    if (currentClassID == null) {
        System.out.println("No class selected.");
        return;
    }
    String studentID = args.trim();
    PreparedStatement ps = conn.prepareStatement(
        "SELECT A.name, G.score, A.pointValue " +
        "FROM Grade G " +
        "JOIN Assignment A ON G.assignmentID = A.assignmentID " +
        "WHERE G.studentID = ? AND A.classID = ?"
    );
    ps.setString(1, studentID);
    ps.setString(2, currentClassID);
    ResultSet rs = ps.executeQuery();
    while (rs.next()) {
        System.out.printf("%s: %.2f / %.2f%n",
            rs.getString("name"),
            rs.getDouble("score"),
            rs.getDouble("pointValue"));
    }
}

// Displays a gradebook-style overview of all students and their grades
private static void showGradebook() throws SQLException {
    if (currentClassID == null) {
        System.out.println("No class selected.");
        return;
    }

    PreparedStatement students = conn.prepareStatement(
        "SELECT studentID, name FROM Student " +
        "WHERE studentID IN (SELECT studentID FROM Enrollment WHERE classID = ?)"
    );
    students.setString(1, currentClassID);
    ResultSet rs = students.executeQuery();
    while (rs.next()) {
        String studentID = rs.getString("studentID");
        String name = rs.getString("name");

        PreparedStatement grades = conn.prepareStatement(
            "SELECT G.score, A.pointValue, C.weight " +
            "FROM Grade G " +
            "JOIN Assignment A ON G.assignmentID = A.assignmentID " +
            "JOIN Category C ON A.categoryID = C.categoryID " +
            "WHERE G.studentID = ? AND A.classID = ?"
        );
        grades.setString(1, studentID);
        grades.setString(2, currentClassID);

        ResultSet gradeRs = grades.executeQuery();
        double total = 0, weightSum = 0;

        while (gradeRs.next()) {
            double score = gradeRs.getDouble("score");
            double points = gradeRs.getDouble("pointValue");
            double weight = gradeRs.getDouble("weight");

            if (points > 0) {
                total += (score / points) * weight;
                weightSum += weight;
            }
        }

        double percent = (weightSum > 0) ? (total / weightSum) * 100 : 0;
        System.out.printf("%s (%s): %.2f%%%n", name, studentID, percent);
    }
}

private static String[] parseArgs(String input, int expected) {
    List<String> tokens = new ArrayList<>();
    Scanner scanner = new Scanner(input);
    scanner.useDelimiter("\"");
    boolean insideQuotes = false;

    while (scanner.hasNext()) {
        if (insideQuotes) {
            tokens.add(scanner.next());
            insideQuotes = false;
        } else {
            String[] parts = scanner.next().trim().split("\\s+");
            for (String part : parts) {
                if (!part.isEmpty()) tokens.add(part);
            }
            insideQuotes = true;
        }
    }
    scanner.close();

    if (tokens.size() != expected) {
        throw new IllegalArgumentException("Expected " + expected + " arguments.");
    }

    return tokens.toArray(new String[0]);
}

}
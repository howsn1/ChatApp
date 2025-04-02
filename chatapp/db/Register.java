package db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Register {


public class UserDAO {
    public static boolean registerUser(String username, String password) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); // TODO: Hash the password before storing!
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static void main(String[] args) {
        if (registerUser("testUser", "testPass")) {
            System.out.println(" User registered successfully!");
        } else {
            System.out.println(" Registration failed.");
        }
    }
}

    
}

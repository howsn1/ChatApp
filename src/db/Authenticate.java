package src.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Authenticate {
    
    public static boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In a real app, use password hashing!
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // If there's a result, login is valid
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
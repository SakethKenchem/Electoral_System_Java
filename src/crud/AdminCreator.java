//package crud;
//
//import db.Database;
//import util.PasswordUtil;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//
//public class AdminCreator {
//
//    /**
//     * Inserts a new admin into the database with a hashed password.
//     * This is useful for manual setup or one-time administrative tasks.
//     * * @param username The username for the new admin.
//     * @param password The plain-text password for the new admin.
//     */
//    public static void createAdmin(String username, String password) throws Exception {
//        String hashedPassword = PasswordUtil.hashPassword(password);
//        
//        if (hashedPassword == null) {
//            System.err.println("Error: Failed to hash the password.");
//            return;
//        }
//
//        String sql = "INSERT INTO admins (username, password) VALUES (?, ?)";
//
//        try (Connection conn = Database.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, username);
//            pstmt.setString(2, hashedPassword);
//            
//            int rowsAffected = pstmt.executeUpdate();
//            
//            if (rowsAffected > 0) {
//                System.out.println("Success: Admin '" + username + "' inserted successfully!");
//            } else {
//                System.out.println("Warning: Admin '" + username + "' could not be inserted.");
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Database error while creating admin: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    // Example of how to use this method
//    public static void main(String[] args) throws Exception {
//        // You would typically run this from a separate main method to set up your first admin.
//        // For a permanent solution, you would build an admin registration UI.
//        
//        String newAdminUsername = "";
//        String newAdminPassword = "";
//        
//        System.out.println("Attempting to create a new admin...");
//        createAdmin(newAdminUsername, newAdminPassword);
//    }
//}
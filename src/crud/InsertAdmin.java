package crud;

import db.Database;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class InsertAdmin {
    public static void main(String[] args) {
        String username = "Saketh";
        String plainPassword = "S00per-d00per";
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO admins (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Admin inserted successfully.");
            } else {
                System.out.println("⚠ Failed to insert admin.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

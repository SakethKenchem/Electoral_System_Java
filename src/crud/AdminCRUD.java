package crud;

import db.Database;
import model.Admin;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminCRUD {

    public static boolean validateLogin(String username, String password) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM admins WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                return PasswordUtil.hashPassword(password).equals(storedHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

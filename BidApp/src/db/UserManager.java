package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserManager {

	public static boolean registerUser(String username, String password, String role) throws Exception {
		String sql = "INSERT INTO user (username, password, role) VALUES (?, ?, ?)";
		String encryptedPassword = PasswordUtil.encryptPassword(password);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, username);
			preparedStatement.setString(2, encryptedPassword); 
			preparedStatement.setString(3, role); 

			int rowsAffected = preparedStatement.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			System.out.println("Error al registrar el usuario: " + e.getMessage());
			return false;
		}
	}

	public static boolean deleteUser(String username) {
		String sql = "DELETE FROM user WHERE username = ?";

		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, username);

			int rowsAffected = preparedStatement.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			System.out.println("Error al eliminar el usuario: " + e.getMessage());
			return false;
		}
	}

	public static boolean validateLogin(String username, String password) throws Exception {
		try (Connection conn = DatabaseManager.getConnection()) {
			String query = "SELECT password FROM user WHERE username = ?";
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setString(1, username);
				ResultSet rs = stmt.executeQuery();

				if (rs.next()) {
					String storedEncryptedPassword = rs.getString("password");

					return PasswordUtil.verifyPassword(password, storedEncryptedPassword);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static String getUserRole(String username, String password) throws Exception {
	    if (validateLogin(username, password)) {
	        String sql = "SELECT role FROM user WHERE username = ?";

	        try (Connection connection = DatabaseManager.getConnection();
	             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

	            preparedStatement.setString(1, username);

	            try (ResultSet rs = preparedStatement.executeQuery()) {
	                if (rs.next()) {
	                    return rs.getString("role");  
	                } else {
	                    return null;  
	                }
	            }
	        } catch (SQLException e) {
	            System.out.println("Error al obtener el rol del usuario: " + e.getMessage());
	            return null;
	        }
	    } else {
	        return null;  
	    }
	}
	
	 public static int getUserId(String username) {
	        int userId = -1;  
	        String sql = "SELECT id FROM user WHERE username = ?";
	        
	        try (Connection connection = DatabaseManager.getConnection();
	             PreparedStatement stmt = connection.prepareStatement(sql)) {
	            
	            stmt.setString(1, username);  
	            ResultSet rs = stmt.executeQuery();
	            
	            if (rs.next()) {
	                userId = rs.getInt("id");  
	            }
	            
	        } catch (SQLException e) {
	            e.printStackTrace();  
	        }
	        
	        return userId;  
	    }

}

package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import blockchain.Block;

public class BlockchainDB {

    private static final String DB_URL = "jdbc:mysql://testdb.cx8ai0gooz0e.eu-west-3.rds.amazonaws.com:3306/auction_db";
    private static final String DB_USER = "admin"; // Usuario de RDS
    private static final String DB_PASSWORD = "admin1234"; // Contraseña de RDS

    public static void registerBlockchainEvent(String eventType, String eventData, String blockchainHash) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO blockchain_events (event_type, event_data, blockchain_hash) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, eventType);
                statement.setString(2, eventData);
                statement.setString(3, blockchainHash);
                statement.executeUpdate();
                System.out.println("Evento de blockchain registrado correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al registrar el evento en la base de datos: " + e.getMessage());
        }
    }
    
    public void saveBlock(Block block) throws Exception {
        String query = "INSERT INTO blockchain (block_hash, previous_block_hash, data, timestamp) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); 
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, block.getHash());
            stmt.setString(2, block.getPreviousHash());
            stmt.setTimestamp(4, new Timestamp(block.getTimeStamp()));

            String encryptedData = EncryptionUtil.encrypt(block.getData());
            stmt.setString(3, encryptedData);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Block> loadBlockchain() {
        List<Block> blockchain = new ArrayList<>();
        String query = "SELECT * FROM blockchain ORDER BY id";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); 
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                String hash = rs.getString("block_hash");
                String previousHash = rs.getString("previous_block_hash");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                String data = rs.getString("data");

                Block block = new Block(data, previousHash);
                block.setHash(hash);
                block.setTimeStamp(timestamp.getTime());
                blockchain.add(block);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return blockchain;
    }
}

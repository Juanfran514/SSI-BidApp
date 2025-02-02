package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import db.DatabaseManager;
import models.Bid;
import blockchain.Blockchain;
import db.BlockchainDB;

public class BidManager {

    private Blockchain bidBlockchain;
    private static int difficulty=4;

    public BidManager() throws SQLException {
        this.bidBlockchain = new Blockchain(difficulty);
    }

    public static boolean deleteBid(int bidId) throws Exception {
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM bids WHERE bid_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, bidId);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    String eventData = "Bid deleted with ID " + bidId;
                    Blockchain bidBlockchain = new Blockchain(difficulty); 
                    bidBlockchain.addBlock(eventData);  

                    String blockchainHash = bidBlockchain.getLatestBlock().getHash();
                    BlockchainDB.registerBlockchainEvent("Bid Deleted", eventData, blockchainHash);

                    System.out.println("Bid deleted properly.");
                    return true;
                } else {
                    System.out.println("Couldn't find bid with ID.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting bid: " + e.getMessage());
            return false;
        }
    }

}

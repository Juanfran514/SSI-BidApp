package services;

import blockchain.Block;
import blockchain.Blockchain;
import db.BlockchainDB;
import db.DatabaseManager;
import models.Auction;
import models.AuctionBidInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {

	private Blockchain auctionBlockchain;
	private int difficulty=4;

	public AuctionManager() throws SQLException {
		this.auctionBlockchain = new Blockchain(difficulty);
	}

	public void createAuction(int sellerId, String title, String description, double startPrice,
			LocalDateTime startDate, LocalDateTime endDate) throws Exception {
		try (Connection connection = DatabaseManager.getConnection()) {
			String sql = "INSERT INTO auctions (seller_id, title, description, start_price, current_price, start_date, end_date, status, created_at) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, 'active', NOW())";
			try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
				statement.setInt(1, sellerId);
				statement.setString(2, title);
				statement.setString(3, description);
				statement.setDouble(4, startPrice);
				statement.setDouble(5, startPrice); 
				statement.setTimestamp(6, Timestamp.valueOf(startDate));
				statement.setTimestamp(7, Timestamp.valueOf(endDate));

				statement.executeUpdate();

				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int auctionId = generatedKeys.getInt(1);

						String eventData = "Subasta creada con ID " + auctionId + ": " + title + " - " + description;
						auctionBlockchain.addBlock(eventData); 

						String blockchainHash = auctionBlockchain.getLatestBlock().getHash();
						BlockchainDB.registerBlockchainEvent("Subasta Creada", eventData, blockchainHash);

						System.out.println("Subasta registrada correctamente en la base de datos y en la blockchain.");
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("Error al crear la subasta: " + e.getMessage());
		}
	}

	public void displayAllActiveAuctions() {
		try (Connection connection = DatabaseManager.getConnection()) {
			String sql = "SELECT a.auction_id, a.seller_id, a.title, a.description, a.start_price, a.current_price, a.start_date, a.end_date, a.status, u.username "
					+ "FROM auctions a " + "JOIN user u ON a.seller_id = u.id " + "WHERE a.status = 'active'"; 

			try (PreparedStatement statement = connection.prepareStatement(sql);
					ResultSet resultSet = statement.executeQuery()) {

				boolean hasAuctions = false;
				while (resultSet.next()) {
					int auctionId = resultSet.getInt("auction_id");
					int sellerId = resultSet.getInt("seller_id");
					String sellerUsername = resultSet.getString("username"); 
					String title = resultSet.getString("title");
					String description = resultSet.getString("description");
					double startPrice = resultSet.getDouble("start_price");
					double currentPrice = resultSet.getDouble("current_price");
					Timestamp startDate = resultSet.getTimestamp("start_date");
					Timestamp endDate = resultSet.getTimestamp("end_date");
					String status = resultSet.getString("status");

					System.out.println("Auction ID: " + auctionId);
					System.out.println("Seller: " + sellerUsername);
					System.out.println("Title: " + title);
					System.out.println("Description: " + description);
					System.out.println("Starting Price: " + startPrice);
					System.out.println("Actual Price: " + currentPrice);
					System.out.println("Beggining Date: " + startDate);
					System.out.println("Ending Date: " + endDate);
					System.out.println("State: " + status);
					System.out.println("--------------------------------------------");
					hasAuctions = true;
				}

				if (!hasAuctions) {
					System.out.println("No active auctions right now.");
				}

			}
		} catch (SQLException e) {
			System.err.println("Error obtaining active auctions: " + e.getMessage());
		}
	}

	public static List<Auction> getAllAuctions() {
		List<Auction> auctions = new ArrayList<>();
		String sql = "SELECT a.auction_id, a.seller_id, a.title, a.description, a.start_price, a.current_price, a.start_date, a.end_date, a.status, u.username "
				+ "FROM auctions a " + "JOIN user u ON a.seller_id = u.id";

		try (Connection connection = DatabaseManager.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				int auctionId = resultSet.getInt("auction_id");
				int sellerId = resultSet.getInt("seller_id");
				String sellerUsername = resultSet.getString("username"); 
				String title = resultSet.getString("title");
				String description = resultSet.getString("description");
				double startPrice = resultSet.getDouble("start_price");
				double currentPrice = resultSet.getDouble("current_price");
				Timestamp startDate = resultSet.getTimestamp("start_date");
				Timestamp endDate = resultSet.getTimestamp("end_date");
				String status = resultSet.getString("status");

				Auction auction = new Auction(auctionId, sellerId, title, description, startPrice, currentPrice,
						startDate, endDate, status);
				auctions.add(auction);
			}
		} catch (SQLException e) {
			System.err.println("Error obtaining auctions: " + e.getMessage());
		}

		return auctions;
	}

	public List<Auction> getAuctionsByBidder(String username) {
		String query = "SELECT auctions.title, auctions.seller_id, auctions.start_price " + "FROM auctions "
				+ "JOIN bids ON auctions.auction_id = bids.auction_id " + "WHERE bids.bidder_id = ?"; 

		List<Auction> auctions = new ArrayList<>();

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				String name = rs.getString("name");
				String seller = rs.getString("seller");
				double initialPrice = rs.getDouble("initial_price");
				auctions.add(new Auction(0, 0, name, seller, initialPrice, initialPrice, null, null, seller));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return auctions;
	}

	public static List<Auction> getAuctionsBySeller(String username) {
		List<Auction> auctions = new ArrayList<>();

		String sql = "SELECT * FROM auction WHERE created_by = ?"; 

		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, username);

			try (ResultSet rs = preparedStatement.executeQuery()) {
				while (rs.next()) {
					Auction auction = new Auction(rs.getInt("id"), 0, rs.getString("name"), rs.getString("description"),
							rs.getDouble("initial_price"), 0, null, null, rs.getString("created_by"));
					auctions.add(auction); 
				}
			}
		} catch (SQLException e) {
			System.out.println("Error obtaining seller's auctions: " + e.getMessage());
		}

		return auctions;
	}

	public static boolean createAuction(int sellerId, String name, String description, double initialPrice,
			String endDateString) throws ParseException {
		String sql = "INSERT INTO auctions (seller_id, title, description, start_price, end_date) VALUES (?, ?, ?, ?, ?)";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		java.util.Date date = formatter.parse(endDateString);

		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setInt(1, sellerId); 
			preparedStatement.setString(2, name);
			preparedStatement.setString(3, description);
			preparedStatement.setDouble(4, initialPrice);
			preparedStatement.setDate(5, new java.sql.Date(date.getTime()));

			int rowsAffected = preparedStatement.executeUpdate();
			return rowsAffected > 0; 

		} catch (SQLException e) {
			System.out.println("Error al creating auction: " + e.getMessage());
			return false;
		}
	}

	public void displayBlockchain() {
		System.out.println("Showing full blockchain:");
		for (Block block : auctionBlockchain.getBlockchain()) {
			System.out.println("---------------------------------------------------");
			System.out.println("Data: " + block.getData());
			System.out.println("Hash: " + block.getHash());
			System.out.println("Previous Hash: " + block.getPreviousHash());
			System.out.println("Timestamp: " + block.getTimeStamp());
			System.out.println("---------------------------------------------------");
		}
	}

	public boolean registerBid(int auctionId, int bidderId, double bidAmount) {
		try (Connection connection = DatabaseManager.getConnection()) {
			String checkCurrentPriceQuery = "SELECT current_price FROM auctions WHERE auction_id = ?";
			try (PreparedStatement checkStmt = connection.prepareStatement(checkCurrentPriceQuery)) {
				checkStmt.setInt(1, auctionId);
				ResultSet resultSet = checkStmt.executeQuery();

				if (resultSet.next()) {
					double currentPrice = resultSet.getDouble("current_price");

					if (bidAmount > currentPrice) {
						String sql = "INSERT INTO bids (auction_id, bidder_id, bid_amount, bid_date) VALUES (?, ?, ?, NOW())";
						try (PreparedStatement statement = connection.prepareStatement(sql)) {
							statement.setInt(1, auctionId);
							statement.setInt(2, bidderId);
							statement.setDouble(3, bidAmount);
							statement.executeUpdate();

							String updateAuctionQuery = "UPDATE auctions SET current_price = ? WHERE auction_id = ?";
							try (PreparedStatement updateStmt = connection.prepareStatement(updateAuctionQuery)) {
								updateStmt.setDouble(1, bidAmount);
								updateStmt.setInt(2, auctionId);
								updateStmt.executeUpdate();
							}

							System.out.println("Bid correctly registered in the database.");
							return true;
						}
					} else {
						System.out.println("The bid quantity must be higher than the actual.");
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("Error registering bid: " + e.getMessage());
		}
		return false;
	}

	public void displayBidsForAuction(int auctionId) {
		try (Connection connection = DatabaseManager.getConnection()) {
			String sql = "SELECT b.bid_amount, b.bid_date, u.username " + "FROM bids b "
					+ "JOIN user u ON b.bidder_id = u.id " + "WHERE b.auction_id = ? " + "ORDER BY b.bid_date DESC"; 

			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				statement.setInt(1, auctionId);
				ResultSet resultSet = statement.executeQuery();

				boolean hasBids = false;
				while (resultSet.next()) {
					double bidAmount = resultSet.getDouble("bid_amount");
					Timestamp bidDate = resultSet.getTimestamp("bid_date");
					String username = resultSet.getString("username");

					System.out.println("Puja de " + username + ": " + bidAmount + " en " + bidDate);
					hasBids = true;
				}

				if (!hasBids) {
					System.out.println("No bids for this auction.");
				}
			}
		} catch (SQLException e) {
			System.err.println("Error obtaining bids: " + e.getMessage());
		}
	}

	public List<AuctionBidInfo> getAuctionsWhereUserHasBid(String username) {
		List<AuctionBidInfo> auctionsWithBids = new ArrayList<>();

		String query = "SELECT b.auction_id, b.bid_amount, a.title, a.description, a.start_price, a.current_price "
				+ "FROM bids b " + "JOIN user u ON b.bidder_id = u.id "
				+ "JOIN auctions a ON b.auction_id = a.auction_id " + "WHERE u.username = ?";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, username); 
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				int auctionId = rs.getInt("auction_id");
				double bidAmount = rs.getDouble("bid_amount");
				String title = rs.getString("title");
				String description = rs.getString("description");
				double startPrice = rs.getDouble("start_price");
				double currentPrice = rs.getDouble("current_price");

				// Crear un objeto que contenga la subasta y la puja
				AuctionBidInfo auctionBidInfo = new AuctionBidInfo(auctionId, title, description, startPrice,
						currentPrice, bidAmount);
				auctionsWithBids.add(auctionBidInfo); // Añadir la subasta y la puja a la lista
			}

		} catch (SQLException e) {
			System.err.println("Error obtaining auctions where user made bids: " + e.getMessage());
		}

		return auctionsWithBids; // Retornar la lista de subastas con pujas
	}

	public static Auction getAuctionById(int auctionId) {
		Auction auction = null;
		String query = "SELECT * FROM auctions WHERE auction_id = ?"; // Asegúrate de que el nombre de la columna sea
																		// correcto

		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			preparedStatement.setInt(1, auctionId);

			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				auction = new Auction(resultSet.getInt("auction_id"), resultSet.getInt("seller_id"),
						resultSet.getString("title"), resultSet.getString("description"),
						resultSet.getDouble("start_price"), resultSet.getDouble("current_price"),
						resultSet.getTimestamp("start_date"), resultSet.getTimestamp("end_date"),
						resultSet.getString("status"));
			}
		} catch (SQLException e) {
			System.err.println("Error obtaining auction by ID: " + e.getMessage());
		}

		return auction;
	}

	public double getBidAmountForUser(int auctionId, int userId) {
		double bidAmount = 0.0; 

		try (Connection connection = DatabaseManager.getConnection()) { 
			String query = "SELECT bid_amount FROM bids WHERE auction_id = ? AND bidder_id = ?";
			PreparedStatement stmt = connection.prepareStatement(query);
			stmt.setInt(1, auctionId);
			stmt.setInt(2, userId);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				bidAmount = rs.getDouble("bid_amount"); 
			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return bidAmount; 
	}

	public boolean updateBid(int auctionId, int bidderId, double newBidAmount) {
		boolean success = false;

		try (Connection connection = DatabaseManager.getConnection()) { 
			double currentBidAmount = getBidAmountForUser(auctionId, bidderId);

			if (newBidAmount > currentBidAmount) {
				String query = "UPDATE bids SET bid_amount = ? WHERE auction_id = ? AND bidder_id = ?";
				PreparedStatement stmt = connection.prepareStatement(query);
				stmt.setDouble(1, newBidAmount);
				stmt.setInt(2, auctionId);
				stmt.setInt(3, bidderId);

				int rowsAffected = stmt.executeUpdate();

				if (rowsAffected > 0) {
					success = true;
				}

				stmt.close();
			} else {
				System.out.println("New bid must be higher than the actual.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return success;
	}

	public boolean deleteAuction(int auctionId) throws Exception {
		try (Connection connection = DatabaseManager.getConnection()) {
			String deleteBidsSql = "DELETE FROM bids WHERE auction_id = ?";
			try (PreparedStatement deleteBidsStmt = connection.prepareStatement(deleteBidsSql)) {
				deleteBidsStmt.setInt(1, auctionId);
				deleteBidsStmt.executeUpdate();
			}

			String deleteAuctionSql = "DELETE FROM auctions WHERE auction_id = ?";
			try (PreparedStatement deleteAuctionStmt = connection.prepareStatement(deleteAuctionSql)) {
				deleteAuctionStmt.setInt(1, auctionId);
				int rowsAffected = deleteAuctionStmt.executeUpdate();

				if (rowsAffected > 0) {
					String eventData = "Auction deleted with ID " + auctionId;
					auctionBlockchain.addBlock(eventData); 

					String blockchainHash = auctionBlockchain.getLatestBlock().getHash();
					BlockchainDB.registerBlockchainEvent("Auction eliminated", eventData, blockchainHash);

					System.out.println("Auction deleted.");
					return true;
				} else {
					System.out.println("Didn't find auction with that ID.");
					return false;
				}
			}
		} catch (SQLException e) {
			System.err.println("Error deleting auction: " + e.getMessage());
			return false;
		}
	}

	public void addAuction(Auction auction) throws Exception {
		String auctionDetails = "Subasta añadida: " + auction.toString();

		System.out.println("Adding auction to Blockchain: " + auctionDetails);

		auctionBlockchain.addBlock(auctionDetails);
	}
	
    public void addBlockToBlockchain(String auctionName, String auctionDesc, double initialPrice, String endDateString) throws Exception {
        String blockData = "Auction: " + auctionName + ", Description: " + auctionDesc + ", Starting Price " + initialPrice + ", Ending Date: " + endDateString;

        String previousHash = auctionBlockchain.getBlockchain().isEmpty() ? "0" : auctionBlockchain.getBlockchain().get(auctionBlockchain.getBlockchain().size() - 1).getHash();

        Block newBlock = new Block(previousHash, blockData);

        auctionBlockchain.addBlock(blockData);
    }
    
    public Blockchain getAuctionBlockchain() {
        return auctionBlockchain;
    }

    public void loadBlockchainFromDB() {
        BlockchainDB db = new BlockchainDB();
        List<Block> loadedBlockchain = db.loadBlockchain();
        
        Blockchain blockchain = new Blockchain(difficulty);
        blockchain.setBlockchain(loadedBlockchain);
    }
}

package main;

import db.UserManager;
import models.Auction;
import models.AuctionBidInfo;
import services.AuctionManager;
import validators.CSRValidator;
import validators.KeyValidator;
import validators.USBValidator;

import javax.swing.*;


import java.awt.*;
import java.util.List;
import java.sql.SQLException;
import java.text.ParseException;

public class AuctionApp {
    private JFrame frame;
    private boolean isLoggedIn = false;
    private String loggedUsername;
    private String loggedRole;
    private AuctionManager auctionManager;
    
    public AuctionApp() throws SQLException {
        auctionManager = new AuctionManager();
        auctionManager.loadBlockchainFromDB();
        frame = new JFrame("Auction Application");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        showMainMenu();
    }

    private void showMainMenu() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Welcome to the Bid Application", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton registerButton = new JButton("Register User");
        registerButton.addActionListener(e -> {
			try {
				showRegisterForm();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
        buttonPanel.add(registerButton);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            try {
                showLoginForm();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(loginButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        panel.add(buttonPanel, BorderLayout.CENTER);
        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    private void showRegisterForm() throws Exception {
        JPanel registerPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        String[] roles = {"bidder", "seller"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        registerPanel.add(new JLabel("User:"));
        registerPanel.add(usernameField);
        registerPanel.add(new JLabel("password:"));
        registerPanel.add(passwordField);
        registerPanel.add(new JLabel("Role:"));
        registerPanel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(frame, registerPanel, "User Register",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleBox.getSelectedItem();

            if ("seller".equals(role)) {
                if (!USBValidator.verifyUSBFiles()) {
                    JOptionPane.showMessageDialog(frame, "Couldn't verify USB files. Can't register.", 
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String privateKeyPassword = JOptionPane.showInputDialog(frame, "Type the password for the privateKey:");

                boolean isKeyValid = KeyValidator.validatePrivateKey("D:\\privateKey.key", privateKeyPassword); // Cambiar la ruta según corresponda
                boolean isCSRValid = CSRValidator.validateCSR("D:\\CCSR.csr"); // Cambiar la ruta según corresponda

                if (!isKeyValid || !isCSRValid) {
                    JOptionPane.showMessageDialog(frame, "Files are not valid.", 
                                                  "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            boolean success = UserManager.registerUser(username, password, role); 
            if (success) {
                JOptionPane.showMessageDialog(frame, "User succesfully registered.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Error registering user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showLoginForm() throws Exception {
        JPanel loginPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        loginPanel.add(new JLabel("User:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(frame, loginPanel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (UserManager.validateLogin(username, password)) {
                isLoggedIn = true;
                loggedUsername = username;
                loggedRole = UserManager.getUserRole(username, password);
                JOptionPane.showMessageDialog(frame, "Succesfully logged. Welcome, " + loggedUsername + "!");

                if ("seller".equals(loggedRole)) {
                    setupMenuForSeller();
                } else {
                    setupMenuForBidder();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "User or password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setupMenuForSeller() {
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        JButton createAuctionButton = new JButton("Create auction");
        createAuctionButton.addActionListener(e -> {
			try {
				try {
					showCreateAuctionForm();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		});
        buttonPanel.add(createAuctionButton);

        JButton viewAuctionsButton = new JButton("See auction");
        viewAuctionsButton.addActionListener(e -> showAllAuctions());
        buttonPanel.add(viewAuctionsButton);

        JButton myAuctionsButton = new JButton("My Auctions");
        myAuctionsButton.addActionListener(e -> showMyAuctions());
        buttonPanel.add(myAuctionsButton);

        frame.getContentPane().removeAll();
        frame.getContentPane().add(buttonPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void setupMenuForBidder() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JButton viewAuctionsButton = new JButton("See auctions");
        viewAuctionsButton.addActionListener(e -> showAllAuctions());
        buttonPanel.add(viewAuctionsButton);

        JButton myAuctionsButton = new JButton("My Auctions");
        myAuctionsButton.addActionListener(e -> showMyAuctions());
        buttonPanel.add(myAuctionsButton);

        frame.getContentPane().removeAll();
        frame.getContentPane().add(buttonPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void showCreateAuctionForm() throws ParseException, SQLException {
        JPanel createAuctionPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        JTextField auctionNameField = new JTextField();
        JTextField auctionDescField = new JTextField();
        JTextField initialPriceField = new JTextField();
        JTextField endDateField = new JTextField();
        
        createAuctionPanel.add(new JLabel("Auction name:"));
        createAuctionPanel.add(auctionNameField);
        createAuctionPanel.add(new JLabel("Auction Description:"));
        createAuctionPanel.add(auctionDescField);
        createAuctionPanel.add(new JLabel("Initial price:"));
        createAuctionPanel.add(initialPriceField);
        createAuctionPanel.add(new JLabel("Ending Date (yyyy-MM-dd HH:mm):"));
        createAuctionPanel.add(endDateField);

        int result = JOptionPane.showConfirmDialog(frame, createAuctionPanel, "Create Auction",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String auctionName = auctionNameField.getText();
            String auctionDesc = auctionDescField.getText();
            String endDateString = endDateField.getText();
            double initialPrice;
            int sellerId = UserManager.getUserId(loggedUsername);

            try {
                initialPrice = Double.parseDouble(initialPriceField.getText());
                boolean success = AuctionManager.createAuction(sellerId, auctionName, auctionDesc, initialPrice, endDateString);

                if (success) {
                    JOptionPane.showMessageDialog(frame, "Auction created succesfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                    auctionManager.addBlockToBlockchain(auctionName, auctionDesc, initialPrice, endDateString); // Añadir el bloque
                    auctionManager.displayBlockchain(); 

                } else {
                    JOptionPane.showMessageDialog(frame, "Error creating the auction.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Initial price shoudl be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }




    private void showAllAuctions() {
        List<Auction> auctions = AuctionManager.getAllAuctions();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            if ("seller".equals(loggedRole)) {
                setupMenuForSeller(); 
            } else {
                setupMenuForBidder(); 
            }
        });

        panel.add(backButton); 

        for (Auction auction : auctions) {
            JPanel auctionRow = new JPanel(new GridLayout(1, 4));
            auctionRow.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            auctionRow.add(new JLabel("Name: " + auction.getTitle()));
            auctionRow.add(new JLabel("Seller: " + auction.getSellerId()));
            auctionRow.add(new JLabel("Initial price: " + auction.getStartPrice()));

            JButton bidButton = new JButton("Make a Bid");
            bidButton.addActionListener(e -> showBidForm(auction)); 
            auctionRow.add(bidButton);

            panel.add(auctionRow);
        }

        showAuctionList(panel, "All Auctions");
    }



    private void showMyAuctions() {
        List<AuctionBidInfo> auctions = auctionManager.getAuctionsWhereUserHasBid(loggedUsername); 

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            if ("seller".equals(loggedRole)) {
                setupMenuForSeller(); 
            } else {
                setupMenuForBidder(); 
            }
        });
        
        panel.add(backButton);

        if (auctions.isEmpty()) {
            panel.add(new JLabel("You didn't make any bid."));
        } else {
            for (AuctionBidInfo auction : auctions) {
            	double bidAmount = auctionManager.getBidAmountForUser(auction.getAuctionId(), UserManager.getUserId(loggedUsername)); 
                JPanel auctionRow = new JPanel(new GridLayout(1, 5)); 
                auctionRow.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                auctionRow.add(new JLabel("Name: " + auction.getTitle()));
                auctionRow.add(new JLabel("Initial Price: " + auction.getStartPrice()));
                auctionRow.add(new JLabel("Your Bid: " + bidAmount)); 
                
                JButton changeBidButton = new JButton("Change Bid");
                changeBidButton.addActionListener(e -> showChangeBidForm(auction)); 
                auctionRow.add(changeBidButton);

                panel.add(auctionRow);
            }
        }

        showAuctionList(panel, "My Auctions");
    }
    
    private void showChangeBidForm(AuctionBidInfo auction) {
        JPanel bidPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        JTextField bidAmountField = new JTextField();

        bidPanel.add(new JLabel("New Bid Quantity:"));
        bidPanel.add(bidAmountField);

        int result = JOptionPane.showConfirmDialog(frame, bidPanel, "Change Bid",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String bidAmountText = bidAmountField.getText();
            try {
                double bidAmount = Double.parseDouble(bidAmountText);
                int bidderId = UserManager.getUserId(loggedUsername); 

                boolean success = auctionManager.updateBid(auction.getAuctionId(), bidderId, bidAmount);

                if (success) {
                    JOptionPane.showMessageDialog(frame, "Bid updated succesfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Error updating the bid.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "The bid price should be valid.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAuctionList(JPanel panel, String title) {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
        frame.setTitle(title);
        frame.revalidate();
        frame.repaint();
    }
    
    private void showBidForm(Auction auction) {
        JPanel bidPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        JTextField bidAmountField = new JTextField();

        bidPanel.add(new JLabel("Bid Quantity:"));
        bidPanel.add(bidAmountField);

        int result = JOptionPane.showConfirmDialog(frame, bidPanel, "Make Bid",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String bidAmountText = bidAmountField.getText();
            try {
                double bidAmount = Double.parseDouble(bidAmountText);
                int bidderId = UserManager.getUserId(loggedUsername); 

                boolean success = auctionManager.registerBid(auction.getAuctionId(), bidderId, bidAmount);

                if (success) {
                    JOptionPane.showMessageDialog(frame, "Bid made succesfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Error creating the bid.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "EThe bid quantity should be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
			try {
				new AuctionApp();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
    }
}

package models;

public class Bid {
    private int auctionId;
    private String username;
    private double amount;

    public Bid(int auctionId, String username, double amount) {
        this.auctionId = auctionId;
        this.username = username;
        this.amount = amount;
    }

    // Getters y setters

    public int getAuctionId() {
        return auctionId;
    }

    public String getUsername() {
        return username;
    }

    public double getAmount() {
        return amount;
    }
}

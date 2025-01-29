package models;

public class AuctionBidInfo {
    private int auctionId;
    private String title;
    private String description;
    private double startPrice;
    private double currentPrice;
    private double bidAmount;

    public AuctionBidInfo(int auctionId, String title, String description, double startPrice, double currentPrice, double bidAmount) {
        this.auctionId = auctionId;
        this.title = title;
        this.description = description;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.bidAmount = bidAmount;
    }

    // Getters y Setters
    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }
}

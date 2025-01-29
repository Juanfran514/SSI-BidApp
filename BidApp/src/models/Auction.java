package models;

import java.sql.Timestamp;


public class Auction {
    private int auctionId;
    private int sellerId;
    private String title;
    private String description;
    private double startPrice;
    private double currentPrice;
    public int getAuctionId() {
		return auctionId;
	}

	public void setAuctionId(int auctionId) {
		this.auctionId = auctionId;
	}

	public int getSellerId() {
		return sellerId;
	}

	public void setSellerId(int sellerId) {
		this.sellerId = sellerId;
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

	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private Timestamp startDate;
    private Timestamp endDate;
    private String status;

    public Auction(int auctionId, int sellerId, String title, String description, double startPrice, double currentPrice, Timestamp startDate, Timestamp endDate, String status) {
        this.auctionId = auctionId;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }


    @Override
    public String toString() {
        return "Auction ID: " + auctionId +
                "\nSeller ID: " + sellerId +
                "\nTitle: " + title +
                "\nDescription: " + description +
                "\nStart Price: " + startPrice +
                "\nActual Price: " + currentPrice +
                "\nBeggining Date: " + startDate +
                "\nEnding Date: " + endDate +
                "\nState: " + status;
    }
    
}

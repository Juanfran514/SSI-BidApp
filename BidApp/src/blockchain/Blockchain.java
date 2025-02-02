package blockchain;

import java.util.ArrayList;
import java.util.List;

import db.BlockchainDB;

public class Blockchain {
    private List<Block> blockchain;
    private int difficulty;  

    public List<Block> getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    // Constructor
    public Blockchain(int difficulty) {
        this.blockchain = new ArrayList<>();
        this.difficulty = difficulty;
        loadBlockchainFromDatabase();
    }

    private void loadBlockchainFromDatabase() {
        BlockchainDB db = new BlockchainDB();
        List<Block> blocksFromDb = db.loadBlockchain();

        if (blocksFromDb.isEmpty()) {
            addGenesisBlock();
        } else {
            blockchain.addAll(blocksFromDb);
        }
    }

    private void addGenesisBlock() {
        Block genesisBlock = new Block("Genesis Block", "0");
        genesisBlock.mineBlock(difficulty);  
        blockchain.add(genesisBlock);
    }

    public void addBlock(String auctionDetails) throws Exception {
        Block newBlock = new Block(getLatestBlock().getHash(), auctionDetails);
        
        newBlock.mineBlock(difficulty);  
        
        blockchain.add(newBlock);

        BlockchainDB db = new BlockchainDB();
        db.saveBlock(newBlock);
    }

    public Block getLatestBlock() {
        if (blockchain.size() > 0) {
            return blockchain.get(blockchain.size() - 1);
        } else {
            return null;
        }
    }

    public void printBlockchain() {
        for (Block block : blockchain) {
            System.out.println("Block hash: " + block.getHash());
            System.out.println("Previous block hash: " + block.getPreviousHash());
            System.out.println("Data: " + block.getData());
            System.out.println("Nonce: " + block.getNonce());
            System.out.println("-------------------------");
        }
    }
}

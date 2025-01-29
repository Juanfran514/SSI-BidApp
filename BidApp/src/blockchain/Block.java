package blockchain;

import java.time.Instant;

public class Block {
    private String previousHash;
    private String hash;
    private String data; 
    private long timeStamp;
    private int nonce; // Nonce for PoW

    public Block(String previousHash, String data) {
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = Instant.now().getEpochSecond();
        this.nonce = 0; 
        this.hash = calculateHash(); 
    }

    public String calculateHash() {
        return StringUtil.applySha256(previousHash + timeStamp + data + nonce);
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); 
        
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++; 
            hash = calculateHash(); 
        }
        
        System.out.println("Bloque minado: " + hash);
    }

    // Getters
    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public String getData() {
        return data;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

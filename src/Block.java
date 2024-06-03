package src;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lib.Transaction;
import utils.StringUtil;

/**
 * Represents a block in the blockchain.
 */
public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public long timeStamp;
    public int nonce;
    public List<Transaction> transactions = new ArrayList<>();

    /**
     * Constructs a new block with the given previous hash.
     * @param previousHash the hash of the previous block.
     */
    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = this.calculateHash();
    }

    /**
     * Calculates the hash of the block.
     * @return the calculated hash.
     */
    public String calculateHash() {
        return StringUtil.applySha256(
                previousHash +
                Long.toString(timeStamp) +
                Integer.toString(nonce) +
                this.merkleRoot
        );
    }

    /**
     * Mines the block with the given difficulty.
     * @param difficulty the difficulty of the mining process.
     */
    public void mineBlock(int difficulty) {
        this.merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!this.hash.substring(0, difficulty).equals(target)) {
            nonce += 1;
            this.hash = this.calculateHash();
        }
        System.out.println("[+] Block mined: " + this.hash);
    }

    /**
     * Adds a transaction to the block.
     * @param transaction the transaction to add.
     * @return true if the transaction is added successfully, false otherwise.
     */
    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) {
            return false;
        }

        if (!previousHash.equals("0")) {
            if (!transaction.processTransaction()) {
                System.out.println("[!] Transaction failed to process. Discarded.");
                return false;
            }
        }

        this.transactions.add(transaction);
        return true;
    }

    /**
     * Gets the hash of the block.
     * @return the hash of the block.
     */
    public String getHash() {
        return this.hash;
    }

    /**
     * Gets the hash of the previous block.
     * @return the hash of the previous block.
     */
    public String getPreviousHash() {
        return this.previousHash;
    }

    /**
     * Gets the list of transactions in the block.
     * @return the list of transactions.
     */
    public List<Transaction> getTransactions() {
        return this.transactions;
    }
}

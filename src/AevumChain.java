package src;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lib.Transaction;
import lib.TransInput;
import lib.TransOutput;

/**
 * Represents a blockchain.
 */
public class AevumChain {

    public static List<Block> blockchain = new ArrayList<>();
    public static HashMap<String, TransOutput> UTXOs = new HashMap<>();
    public static final int DIFFICULTY = 3;
    public static final float MIN_TRANSACTION = 0.1f;
    public static final float MINER_REWARD = 50f;
    public static Transaction genesisTransaction;
    public static Wallet coinbase;

    /**
     * Main method to run the blockchain simulation.
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();
        coinbase = new Wallet();

        System.out.println("Creating and Mining Genesis block...");
        Block genesis = new Block("0");
        genesisTransaction = mintBlock(genesis, walletA);

        Block block1 = new Block(blockchain.get(blockchain.size() - 1).getHash());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40f));
        mintBlock(block1, walletA);
        System.out.println("WalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(blockchain.get(blockchain.size() - 1).getHash());
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000f));
        mintBlock(block2, walletA);
        System.out.println("WalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(blockchain.get(blockchain.size() - 1).getHash());
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 20f));
        mintBlock(block3, walletB);
        System.out.println("WalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        System.out.println("\n[+] Blockchain validity: " + isChainValid());
    }

    /**
     * Mints a new block in the blockchain.
     * @param newBlock     the new block to be added.
     * @param minerWallet  the wallet of the miner.
     * @return the coinbase transaction.
     */
    public static Transaction mintBlock(Block newBlock, Wallet minerWallet) {
        Transaction coinbaseTx = new Transaction(coinbase.getPublicKey(), minerWallet.getPublicKey(), MINER_REWARD, null);
        coinbaseTx.generateSignature(coinbase.getPrivateKey());
        coinbaseTx.setTransactionID("0");
        TransOutput output = new TransOutput(coinbaseTx.getRecipient(), coinbaseTx.getValue(), coinbaseTx.getTransactionID());
        coinbaseTx.getOutputs().add(output);
        UTXOs.put(output.getID(), output);

        newBlock.mineBlock(DIFFICULTY);
        blockchain.add(newBlock);
        newBlock.addTransaction(coinbaseTx);
        return coinbaseTx;
    }

    /**
     * Checks if the blockchain is valid.
     * @return true if the blockchain is valid, false otherwise.
     */
    public static boolean isChainValid() {
        HashMap<String, TransOutput> tempUTXOs = new HashMap<>();
        String hashTarget = new String(new char[DIFFICULTY]).replace('\0', '0');
        tempUTXOs.put(genesisTransaction.getOutputs().get(0).getID(), genesisTransaction.getOutputs().get(0));

        for (int i = 1; i < blockchain.size(); i++) {
            Block currentBlock = blockchain.get(i);
            Block previousBlock = blockchain.get(i - 1);

            if (!isCurrentHashValid(currentBlock)) return false;
            if (!isPreviousHashValid(currentBlock, previousBlock)) return false;
            if (!isBlockMined(currentBlock, hashTarget)) return false;
            if (!areTransactionsValid(currentBlock, tempUTXOs)) return false;
        }
        return true;
    }

    private static boolean isCurrentHashValid(Block currentBlock) {
        if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
            System.out.println("#Current Hashes not equal");
            return false;
        }
        return true;
    }

    private static boolean isPreviousHashValid(Block currentBlock, Block previousBlock) {
        if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
            System.out.println("#Previous Hashes not equal");
            return false;
        }
        return true;
    }

    private static boolean isBlockMined(Block currentBlock, String hashTarget) {
        if (!currentBlock.getHash().substring(0, DIFFICULTY).equals(hashTarget)) {
            System.out.println("#This block hasn't been mined");
            return false;
        }
        return true;
    }

    private static boolean areTransactionsValid(Block currentBlock, HashMap<String, TransOutput> tempUTXOs) {
        for (Transaction currentTransaction : currentBlock.getTransactions()) {
            if (!currentTransaction.verifySignature()) {
                System.out.println("#Signature on Transaction is Invalid");
                return false;
            }
            if (!"0".equals(currentTransaction.getTransactionID()) && currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                System.out.println("#Inputs are not equal to outputs on Transaction");
                return false;
            }
            if (!areTransactionInputsValid(currentTransaction, tempUTXOs)) return false;
            if (!areTransactionOutputsValid(currentTransaction, tempUTXOs)) return false;
        }
        return true;
    }

    private static boolean areTransactionInputsValid(Transaction currentTransaction, HashMap<String, TransOutput> tempUTXOs) {
        for (TransInput input : currentTransaction.getInputs()) {
            if (!"0".equals(currentTransaction.getTransactionID())) {
                TransOutput tempOutput = tempUTXOs.get(input.getTransOutputID());
                if (tempOutput == null) {
                    System.out.println("#Referenced input on Transaction is Missing");
                    return false;
                }
                if (input.getUTXO().getValue() != tempOutput.getValue()) {
                    System.out.println("#Referenced input Transaction value is Invalid");
                    return false;
                }
                tempUTXOs.remove(input.getTransOutputID());
            }
        }
        return true;
    }

    private static boolean areTransactionOutputsValid(Transaction currentTransaction, HashMap<String, TransOutput> tempUTXOs) {
        for (TransOutput output : currentTransaction.getOutputs()) {
            tempUTXOs.put(output.getID(), output);
        }
        if (!currentTransaction.getOutputs().get(0).getRecipient().equals(currentTransaction.getRecipient())) {
            System.out.println("#Transaction output recipient is not correct");
            return false;
        }
        if (!"0".equals(currentTransaction.getTransactionID()) && !currentTransaction.getOutputs().get(1).getRecipient().equals(currentTransaction.getSender())) {
            System.out.println("#Transaction output 'change' is not sender.");
            return false;
        }
        return true;
    }
}
           

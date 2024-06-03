package src;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import lib.TransInput;
import lib.TransOutput;
import lib.Transaction;

/**
 * Represents a wallet used for managing transactions.
 */
public class Wallet {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private HashMap<String, TransOutput> UTXOs;

    /**
     * Constructs a new wallet and generates a key pair.
     */
    public Wallet() {
        this.UTXOs = new HashMap<>();
        generateKeyPair();
    }

    /**
     * Generates a new key pair for the wallet.
     */
    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the balance of the wallet by calculating the total value of unspent outputs.
     * @return the balance of the wallet.
     */
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransOutput> item : AevumChain.UTXOs.entrySet()) {
            TransOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKey)) {
                UTXOs.put(UTXO.getID(), UTXO);
                total += UTXO.getValue();
            }
        }
        return total;
    }

    /**
     * Sends funds from this wallet to a recipient.
     * @param recipient the public key of the recipient.
     * @param value     the amount of funds to send.
     * @return the transaction if successful, null otherwise.
     */
    public Transaction sendFunds(PublicKey recipient, float value) {
        if (getBalance() < value) {
            System.out.println("[!] Not enough funds to send transaction. Transaction Discarded.");
            return null;
        }

        List<TransInput> inputs = gatherInputs(value);

        Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        removeUsedUTXOs(inputs);

        return newTransaction;
    }

    /**
     * Gathers unspent outputs as inputs for a transaction.
     * @param value the amount of funds to send.
     * @return the list of transaction inputs.
     */
    private List<TransInput> gatherInputs(float value) {
        List<TransInput> inputs = new ArrayList<>();
        float total = 0;
        for (Map.Entry<String, TransOutput> item : UTXOs.entrySet()) {
            TransOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransInput(UTXO.getID()));
            if (total > value) break;
        }
        return inputs;
    }

    /**
     * Removes used unspent outputs from the wallet.
     * @param inputs the list of transaction inputs.
     */
    private void removeUsedUTXOs(List<TransInput> inputs) {
        for (TransInput input : inputs) {
            UTXOs.remove(input.getTransOutputID());
        }
    }

    /**
     * Gets the private key of the wallet.
     * @return the private key.
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Gets the public key of the wallet.
     * @return the public key.
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
}

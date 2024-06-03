package lib;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import src.AevumChain;
import utils.StringUtil;

/**
 * Represents a transaction in the blockchain.
 */
public class Transaction {
    private static int sequence = 0;

    private String transactionID;
    private PublicKey sender;
    private PublicKey recipient;
    private float value;
    private byte[] signature;

    private List<TransInput> inputs;
    private List<TransOutput> outputs;

    /**
     * Constructs a new transaction.
     * @param from   the sender's public key.
     * @param to     the recipient's public key.
     * @param value  the value to be sent.
     * @param inputs the list of inputs for this transaction.
     */
    public Transaction(PublicKey from, PublicKey to, float value, List<TransInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs == null ? new ArrayList<>() : inputs;
        this.outputs = new ArrayList<>();
    }

    /**
     * Calculates the hash of the transaction.
     * @return the calculated hash.
     */
    private String calculateHash() {
        sequence++;
        return StringUtil.applySha256(
            StringUtil.getStringFromKey(sender) +
            StringUtil.getStringFromKey(recipient) +
            Float.toString(value) +
            sequence
        );
    }

    /**
     * Generates a signature for the transaction using the private key.
     * @param privateKey the private key to sign the transaction.
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + 
                      StringUtil.getStringFromKey(recipient) + 
                      Float.toString(value);
        this.signature = StringUtil.applyECDSASig(privateKey, data);
    }

    /**
     * Verifies the signature of the transaction.
     * @return true if the signature is valid, false otherwise.
     */
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + 
                      StringUtil.getStringFromKey(recipient) + 
                      Float.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    /**
     * Processes the transaction by verifying the signature, checking the transaction value,
     * updating the UTXOs, and adding outputs.
     * @return true if the transaction is processed successfully, false otherwise.
     */
    public boolean processTransaction() {
        if (!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        gatherTransactionInputs();

        if (transactionID == null && getInputsValue() < AevumChain.MIN_TRANSACTION) {
            System.out.println("#Transaction Inputs too small: " + getInputsValue());
            return false;
        }

        float leftOver = getInputsValue() - value;
        if (transactionID == null) {
            transactionID = calculateHash();
        }
        outputs.add(new TransOutput(recipient, value, transactionID));
        outputs.add(new TransOutput(sender, leftOver, transactionID));

        updateUTXOs();

        return true;
    }

    /**
     * Gathers the transaction inputs from the UTXOs.
     */
    private void gatherTransactionInputs() {
        for (TransInput input : inputs) {
            input.setUTXO(AevumChain.UTXOs.get(input.getTransOutputID()));
        }
    }

    /**
     * Updates the Unspent Transaction Outputs (UTXOs) after processing the transaction.
     */
    private void updateUTXOs() {
        for (TransOutput output : outputs) {
            AevumChain.UTXOs.put(output.getID(), output);
        }

        for (TransInput input : inputs) {
            if (input.getUTXO() != null) {
                AevumChain.UTXOs.remove(input.getUTXO().getID());
            }
        }
    }

    /**
     * Calculates the total value of inputs in the transaction.
     * @return the total value of inputs.
     */
    public float getInputsValue() {
        float total = 0;
        for (TransInput input : inputs) {
            if (input.getUTXO() != null) {
                total += input.getUTXO().getValue();
            }
        }
        return total;
    }

    /**
     * Calculates the total value of outputs in the transaction.
     * @return the total value of outputs.
     */
    public float getOutputsValue() {
        float total = 0;
        for (TransOutput output : outputs) {
            total += output.getValue();
        }
        return total;
    }

    /**
     * Gets the ID of the transaction.
     * @return the transaction ID.
     */
    public String getTransactionID() {
        return this.transactionID;
    }

    /**
     * Gets the recipient's public key.
     * @return the recipient's public key.
     */
    public PublicKey getRecipient() {
        return this.recipient;
    }

    /**
     * Gets the sender's public key.
     * @return the sender's public key.
     */
    public PublicKey getSender() {
        return this.sender;
    }

    /**
     * Gets the list of transaction inputs.
     * @return the list of transaction inputs.
     */
    public List<TransInput> getInputs() {
        return this.inputs;
    }

    /**
     * Gets the list of transaction outputs.
     * @return the list of transaction outputs.
     */
    public List<TransOutput> getOutputs() {
        return this.outputs;
    }

    /**
     * Gets the value of the transaction.
     * @return the value of the transaction.
     */
    public float getValue() {
        return this.value;
    }

    /**
     * Sets the transaction ID.
     * @param transactionID the new transaction ID.
     */
    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }
}

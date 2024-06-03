package lib;

import java.security.PublicKey;

import utils.StringUtil;

/**
 * Represents an output for a transaction.
 */
public class TransOutput {
    private String ID;
    private PublicKey recipient;
    private float value;
    private String parentTransactionID;

    /**
     * Constructs a new transaction output.
     * @param recipient           the public key of the recipient.
     * @param value               the value of the output.
     * @param parentTransactionID the ID of the parent transaction.
     */
    public TransOutput(PublicKey recipient, float value, String parentTransactionID) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionID = parentTransactionID;
        this.ID = generateID();
    }

    /**
     * Generates the ID for the output.
     * @return the generated ID.
     */
    private String generateID() {
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(recipient) +
                        Float.toString(value) +
                        parentTransactionID
        );
    }

    /**
     * Checks if the output belongs to the specified public key.
     * @param publicKey the public key to check.
     * @return true if the output belongs to the public key, false otherwise.
     */
    public boolean isMine(PublicKey publicKey) {
        return publicKey.equals(recipient);
    }

    /**
     * Gets the ID of the output.
     * @return the output ID.
     */
    public String getID() {
        return ID;
    }

    /**
     * Gets the recipient's public key.
     * @return the recipient's public key.
     */
    public PublicKey getRecipient() {
        return recipient;
    }

    /**
     * Sets the recipient's public key and updates the output ID.
     * @param recipient the new recipient's public key.
     */
    public void setRecipient(PublicKey recipient) {
        this.recipient = recipient;
        this.ID = generateID();
    }

    /**
     * Gets the value of the output.
     * @return the output value.
     */
    public float getValue() {
        return value;
    }

    /**
     * Sets the value of the output and updates the output ID.
     * @param value the new output value.
     */
    public void setValue(float value) {
        this.value = value;
        this.ID = generateID();
    }

    /**
     * Gets the ID of the parent transaction.
     * @return the parent transaction ID.
     */
    public String getParentTransactionID() {
        return parentTransactionID;
    }

    /**
     * Sets the ID of the parent transaction and updates the output ID.
     * @param parentTransactionID the new parent transaction ID.
     */
    public void setParentTransactionID(String parentTransactionID) {
        this.parentTransactionID = parentTransactionID;
        this.ID = generateID();
    }
}

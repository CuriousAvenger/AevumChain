package lib;

/**
 * Represents an input for a transaction.
 */
public class TransInput {
    private String transOutputID;
    private TransOutput UTXO;

    /**
     * Constructs a new transaction input.
     * @param transOutputID the ID of the corresponding transaction output.
     */
    public TransInput(String transOutputID) {
        this.transOutputID = transOutputID;
    }

    /**
     * Gets the ID of the corresponding transaction output.
     * @return the transaction output ID.
     */
    public String getTransOutputID() {
        return transOutputID;
    }

    /**
     * Sets the ID of the corresponding transaction output.
     * @param transOutputID the new transaction output ID.
     */
    public void setTransOutputID(String transOutputID) {
        this.transOutputID = transOutputID;
    }

    /**
     * Gets the Unspent Transaction Output (UTXO) for this input.
     * @return the UTXO.
     */
    public TransOutput getUTXO() {
        return UTXO;
    }

    /**
     * Sets the Unspent Transaction Output (UTXO) for this input.
     * @param UTXO the new UTXO.
     */
    public void setUTXO(TransOutput UTXO) {
        this.UTXO = UTXO;
    }
}

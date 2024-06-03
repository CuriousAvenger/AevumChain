package utils;

import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import lib.Transaction;

/**
 * Utility class for string operations and cryptographic functions.
 */
public class StringUtil {

    private StringUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Applies SHA-256 hashing to the input string.
     * @param input the input string.
     * @return the hashed string.
     */
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexBuffer = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexBuffer.append('0');
                }
                hexBuffer.append(hex);
            }
            return hexBuffer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Applies ECDSA signature to the input string using the private key.
     * @param privateKey the private key.
     * @param input      the input string.
     * @return the signature bytes.
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        try {
            Signature dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            dsa.update(input.getBytes());
            return dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies ECDSA signature with the public key and input data.
     * @param publicKey  the public key.
     * @param data       the input data.
     * @param signature  the signature bytes.
     * @return true if the signature is valid, false otherwise.
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a key to a string using Base64 encoding.
     * @param key the key to convert.
     * @return the Base64-encoded string representation of the key.
     */
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Calculates the Merkle root of a list of transactions.
     * @param transactions the list of transactions.
     * @return the Merkle root.
     */
    public static String getMerkleRoot(List<Transaction> transactions) {
        int count = transactions.size();
        List<String> previousTreeLayer = new ArrayList<>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.getTransactionID());
        }

        while (count > 1) {
            List<String> treeLayer = new ArrayList<>();
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                String combinedHash = applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i));
                treeLayer.add(combinedHash);
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return previousTreeLayer.isEmpty() ? "" : previousTreeLayer.get(0);
    }
}

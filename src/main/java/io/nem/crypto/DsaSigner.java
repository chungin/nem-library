package io.nem.crypto;

/**
 * Interface that supports signing and verification of arbitrarily sized message.
 */
public interface DsaSigner {

    /**
     * Signs the SHA3 hash of an arbitrarily sized message.
     *
     * @param data The message to sign.
     * @return The generated signature.
     */
    Signature sign(final byte[] data);

    /**
     * Determines if the signature is canonical.
     *
     * @param signature The signature.
     * @return true if the signature is canonical.
     */
    boolean isCanonicalSignature(final Signature signature);
}

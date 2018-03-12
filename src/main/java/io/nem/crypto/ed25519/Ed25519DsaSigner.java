package io.nem.crypto.ed25519;

import io.nem.crypto.*;
import io.nem.crypto.ed25519.arithmetic.Ed25519EncodedFieldElement;
import io.nem.crypto.ed25519.arithmetic.Ed25519EncodedGroupElement;
import io.nem.crypto.ed25519.arithmetic.Ed25519Group;
import io.nem.crypto.ed25519.arithmetic.Ed25519GroupElement;
import io.nem.utils.ArrayUtils;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Implementation of the DSA signer for Ed25519.
 */
public class Ed25519DsaSigner implements DsaSigner {

    private final KeyPair keyPair;

    /**
     * Creates a Ed25519 DSA signer.
     *
     * @param keyPair The key pair to use.
     */
    public Ed25519DsaSigner(final KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    /**
     * Gets the underlying key pair.
     *
     * @return The key pair.
     */
    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    @Override
    public Signature sign(final byte[] data) {
        if (!this.getKeyPair().hasPrivateKey()) {
            throw new CryptoException("cannot sign without private key");
        }

        // Hash the private key to improve randomness.
        final byte[] hash = Hashes.sha3_512(ArrayUtils.toByteArray(this.getKeyPair().getPrivateKey().getRaw(), 32));

        // r = H(hash_b,...,hash_2b-1, data) where b=256.
        final Ed25519EncodedFieldElement r = new Ed25519EncodedFieldElement(Hashes.sha3_512(
                Arrays.copyOfRange(hash, 32, 64),        // only include the last 32 bytes of the private key hash
                data));

        // Reduce size of r since we are calculating mod group order anyway
        final Ed25519EncodedFieldElement rModQ = r.modQ();

        // R = rModQ * base point.
        final Ed25519GroupElement R = Ed25519Group.BASE_POINT.scalarMultiply(rModQ);
        final Ed25519EncodedGroupElement encodedR = R.encode();

        // S = (r + H(encodedR, encodedA, data) * a) mod group order where
        // encodedR and encodedA are the little endian encodings of the group element R and the public key A and
        // a is the lower 32 bytes of hash after clamping.
        final Ed25519EncodedFieldElement h = new Ed25519EncodedFieldElement(Hashes.sha3_512(
                encodedR.getRaw(),
                this.getKeyPair().getPublicKey().getRaw(),
                data));
        final Ed25519EncodedFieldElement hModQ = h.modQ();
        final Ed25519EncodedFieldElement encodedS = hModQ.multiplyAndAddModQ(
                Ed25519Utils.prepareForScalarMultiply(this.getKeyPair().getPrivateKey()),
                rModQ);

        // Signature is (encodedR, encodedS)
        final Signature signature = new Signature(encodedR.getRaw(), encodedS.getRaw());
        if (!this.isCanonicalSignature(signature)) {
            throw new CryptoException("Generated signature is not canonical");
        }

        return signature;
    }

    @Override
    public boolean isCanonicalSignature(final Signature signature) {
        return -1 == signature.getS().compareTo(Ed25519Group.GROUP_ORDER) &&
                1 == signature.getS().compareTo(BigInteger.ZERO);
    }
}

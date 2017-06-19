package org.ednull.hits.data;

/**
 * Base class for signed things.
 */
public abstract class BBSigned {

    // signed hash of above data
    public String signature;
    // pubkey of signer
    public String signer;
}

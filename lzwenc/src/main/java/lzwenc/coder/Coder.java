/**
 */
package lzwenc.coder;

import java.util.BitSet;

/**
 * @author WilX
 * General interface for integers to bits coders.
 */
public
interface Coder
{
    /**
     * This function should encode given number into series of bits.
     * @param num A number to encode.
     * @return Resulting bit string.
     */
    public BitSet encode (long num);

    /**
     * Returns length of code for given number, in bits.
     * @param num A number to encode.
     * @return Length in bits.
     */
    public int codeLen (long num);
}

package lzwenc.coder;

import java.util.BitSet;

/**
 * @author WilX
 * This is an implementation of binary code coder.
 */
public
class Binary
    implements Coder
{
    public
    Binary (int w)
    {
        width = w;
    }

    /**
     * Returns lenght of binary code, the number of highest bit + 1.
     *
     * @see lzwenc.coder.Coder#codeLen(long)
     */
    public
    int
    codeLen (long num)
    {
        return encode (num).length ();
    }

    /**
     * @see lzwenc.coder.Coder#encode(long)
     */
    public
    BitSet
    encode (long num)
    {
        BitSet bs = new BitSet (width);
        int w = Math.min (width, Long.SIZE - 1);
        for (int i = 0; i < w; ++i, num >>>= 1)
            bs.set (i, (num & 1) == 1);
        return bs;
    }

    public
    int
    getWidth ()
    {
        return width;
    }

    public
    void
    setWidth (int w)
    {
        width = w;
    }


    protected int width;
}

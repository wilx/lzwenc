package wilx.utils;

import java.util.BitSet;

/**
 * @author Václav Haisman
 *
 */
public
class Utils
{
    public
    static
    void
    abort ()
    {
        new Throwable ("abort() called").printStackTrace ();
        System.exit (1);
    }

    public
    static
    double
    log2 (final double x)
    {
        return Math.log (x) / Math.log (2);
    }

    public
    static
    String
    bitSet2String (final BitSet bs)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bs.length (); ++i)
        {
            final char ch = bs.get (bs.length () - i) ? '1' : '0';
            sb.append (ch);
        }
        return sb.toString ();
    }

}

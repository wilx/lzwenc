package lzwenc.compr;
import java.util.HashMap;


/**
 * @author WilX
 *
 */
public class InnerNode
    extends Node
{
	public
    InnerNode(int n)
    {
        super (n);
        assert n >= 0;
    }

    @Override
    public
    InnerNode
    nextNode (byte ch)
    {
        return edges.get (ch);
    }

    @Override
    public
    void
    insertNode (Node n, byte ch)
    {
        assert n.getClass().isInstance(InnerNode.class);
        Byte b = ch;
        assert edges.get (b) == null;
        edges.put (b, (InnerNode)n);
    }


    private HashMap<Byte, InnerNode> edges = new HashMap<>();
}

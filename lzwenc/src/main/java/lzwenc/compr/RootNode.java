/**
 *
 */
package lzwenc.compr;


import wilx.utils.Utils;

/**
 * @author Václav Haisman
 *
 */
public
class RootNode
	extends Node
{
	public
	RootNode (final int num_nodes)
	{
		super (-1);
        edges = new InnerNode[num_nodes];
        for (int i = 0 ; i < edges.length; ++i)
            edges[i] = new InnerNode (i);
	}

	@Override
	public
	InnerNode
	nextNode (final byte ch)
	{
		return edges[ch - Byte.MIN_VALUE];
	}

    @Override
    public
    void
    insertNode (final Node n, final byte ch)
    {
        Utils.abort ();
        //assert n.getClass().isInstance(InnerNode.class);
        //assert edges[ch - Byte.MIN_VALUE] == null;
        //edges[ch - Byte.MIN_VALUE] = (InnerNode)n;
    }

	private final InnerNode[] edges;
};

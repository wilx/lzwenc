/**
 * This package implements byte oriented LZW compression.
 */
package lzwenc.compr;

/**
 * @author Václav Haisman
 *
 */
public 
class LZWEncContext
{
    /**
     * @author Václav Haisman
     *
     */
    public 
    interface CompressionObserver
    {
        public void compressionStep (LZWEncContext ctx, byte letter, int output,
                                     Node prev, Node current, Node added);
    };
    
 
    public
    LZWEncContext (CompressionObserver obs, int num_nodes)
    {
        tree = new RootNode (num_nodes);
        current = tree;
        // First num_nodes nodes are coresponding byte values.
        node_count = num_nodes;
        observer = obs;
    }
    
    public
    LZWEncContext (CompressionObserver obs)
    {
        this (obs, 256);
    }
    
    public
    LZWEncContext ()
    {
        this (new CompressionObserver ()
        {
            public void compressionStep (LZWEncContext ctx, byte letter,
                    int output, Node prev, Node cur, Node added)
            { }
        });
    }

    
    public
    int
    encLetter (byte letter)
    {
        Node node = current != null ? current : tree;
        Node next = node.nextNode (letter);
        if (next == null)
        {
            assert node != tree;
            
            int output = node.getNum ();
            int num = node_count;
            RootNode root = tree;
            InnerNode new_current = root.nextNode (letter);
            
            // Insert new node.
            Node new_node = new InnerNode (num);
            node.insertNode (new_node, letter);
            node_count += 1;
            current = new_current;
            observer.compressionStep (this, letter, output, node, current, new_node);          
            return output;
        }
        else 
        {
            current = next;
            observer.compressionStep (this, letter, -1, node, current, null);
            return -1;
        }
    }
    
    public
    int
    finishEncoding ()
    {
        return current.getNum ();
    }
    
    public 
    RootNode 
    getTree ()
    {
        return tree;
    }

    public 
    Node 
    getCurrent ()
    {
        return current;
    }

    public 
    int 
    getNodeCount ()
    {
        return node_count;
    }

    
    protected RootNode tree;
    protected Node current;
    protected int node_count;
    protected CompressionObserver observer;
}

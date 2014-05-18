/**
 *
 */
package lzwenc.compr;

/**
 * @author WilX
 *
 */
public
abstract
class Node
{
	public
	Node (int n)
	{
		num = n;
	}

	public
	int
	getNum ()
	{
		return num;
	}

	abstract Node nextNode (byte ch);
    abstract void insertNode (Node n, byte ch);

	private int num;
};

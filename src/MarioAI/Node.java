package MarioAI;

import java.util.ArrayList;

public class Node {
	public ArrayList<Node> edges = new ArrayList<Node>();
	public final short x;
	public final short y;
	public final byte type;
	public final short coloumn;
	public final short row;
	
	public Node(short x, short y, short coloumn, short row, byte type)
	{
		this.x = x;
		this.y = y;
		this.coloumn = coloumn;
		this.row = row;
		this.type = type;
	}
}

package MarioAI;

import java.util.ArrayList;

public class Node {
	public ArrayList<Node> edges = new ArrayList<Node>();
	public final short x;
	public final short y;
	
	public Node(short x, short y)
	{
		this.x = x;
		this.y = y;
	}
}

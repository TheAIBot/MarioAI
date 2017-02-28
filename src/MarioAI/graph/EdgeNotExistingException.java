package MarioAI.graph;

public class EdgeNotExistingException extends Exception{
	
	public EdgeNotExistingException() {
		super("The program tried to acces an edge that didn't exist, though it should have.");
	}

}

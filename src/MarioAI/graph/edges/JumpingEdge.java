package MarioAI.graph.edges;


import MarioAI.Hasher;
import MarioAI.graph.Function;
import MarioAI.graph.nodes.Node;

public class JumpingEdge extends DirectedEdge implements Function{
	private float a;
	private float b;
	private float c; // Parameters of the polynomial.
	private float topPointX;
	private float topPointY; // Coordinates of the toppunkt
	// TODO Maybe change ceil after discussions. Problem associated with making
	// this into an integer
	// , as sometimes two possible jump position in a row will be registrated as
	// possible to reach,
	// even though it is only one it should be possible to reach. Mario changing
	// position in the air can compensate for this.
	public int ceiledTopPointX; //TODO change to private after tests.
	public int ceiledTopPointY; // Ceildes coordinates of the toppunkt
	
	public JumpingEdge(Node source, Node target, JumpingEdge polynomial) {
		super(source, target);
		this.a = polynomial.a;
		this.b = polynomial.b;
		this.c = polynomial.c;
		this.topPointX = polynomial.topPointX;
		this.topPointY = polynomial.topPointY;
		this.ceiledTopPointX = polynomial.ceiledTopPointX;
		this.ceiledTopPointY = polynomial.ceiledTopPointY;
		hash = Hasher.hashEdge(this, getExtraEdgeHashcode());
		//Needs to be rehashed, as the hash should depend on the height of the jump:
	}

	public JumpingEdge(Node source, Node target) {
		super(source, target);
		this.a = 0;
		this.b = 0;
		this.c = 0;
	}
	
	public JumpingEdge(Node source, Node target, int ceiledTopPointY) {
		this(source, target);
		this.ceiledTopPointY = ceiledTopPointY;
		this.topPointY = ceiledTopPointY;
		hash = Hasher.hashEdge(this, getExtraEdgeHashcode());
	}

	public void setToJumpPolynomial(Node startingPosition, int nodeColoumn, float jumpRange, float jumpHeight) {
		a = -4 * jumpHeight / (jumpRange * jumpRange);
		b = (8 * nodeColoumn + 4 * jumpRange) * jumpHeight / (jumpRange * jumpRange);
		c = (-4 * nodeColoumn * (nodeColoumn + jumpRange) * jumpHeight + startingPosition.y * jumpRange * jumpRange) / (jumpRange * jumpRange);
		setTopPoint();
	}

	public boolean isPastTopPoint(int startPosition, int currentPosition) {
		return (startPosition <= topPointX && topPointX <= currentPosition || //Going right
				  startPosition >= topPointX && topPointX >= currentPosition);  //Going left.
	}

	public float getTopPointX() {
		return (topPointX);
	}

	public float getTopPointY() {
		return topPointY;
	}

	public float getCeiledTopPointX() {
		return (ceiledTopPointX);
	}

	public float getCeiledTopPointY() {
		return ceiledTopPointY;
	}

	private void setTopPoint() {
		float x = ((-b / a) / 2);
		float y = f(x);
		setTopPoint(x, y);
	}
	
	public void setTopPoint(float x, float y) {
		topPointX = x;
		topPointY = y;
		ceiledTopPointX = (short) Math.ceil(topPointX);
		ceiledTopPointY = (short) Math.ceil(topPointY);
	}

	public float f(float x) {
		return a * x * x + b * x + c;
	}
	
	@Override
	public float getMaxY() {
		return topPointY - (int)source.y;
	}

	public float getWeight() {
		return 0.1f;
	}
	
	@Override
	protected int getExtraEdgeHashcode() {
		final int jumpType = 1; //it is a jump edge type
		//Its jump height. Max is 4 min is 0, giving 3 bits.
		//3 plus 1 but for jump type 
		final int jumpHeight = ((ceiledTopPointY - (int)source.y) & 0xf) << 1;		
		return jumpHeight | jumpType;
	}
	
}
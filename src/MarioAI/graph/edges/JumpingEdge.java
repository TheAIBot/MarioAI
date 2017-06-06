package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.JumpDirection;
import MarioAI.graph.nodes.Node;

public class JumpingEdge extends DirectedEdge {
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
	public int ceiledTopPointY; // Ceiled coordinates of the toppunkt
	
	public JumpingEdge(Node source, Node target, JumpingEdge polynomial, boolean useSuperSpeed) {
		super(source, target, useSuperSpeed);
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
		this(source, target, false);
	}
	
	public JumpingEdge(Node source, Node target, boolean useSuperSpeed) {
		super(source, target, useSuperSpeed);
		this.a = 0;
		this.b = 0;
		this.c = 0;
		//This shouldn't be hashed
		//hash = Hasher.hashEdge(this, getExtraEdgeHashcode());
	}
	
	public JumpingEdge(Node source, Node target, int ceiledTopPointY, boolean useSuperSpeed) {
		this(source, target, useSuperSpeed);
		this.ceiledTopPointY = ceiledTopPointY + source.y;
		this.topPointY = ceiledTopPointY;
		hash = Hasher.hashEdge(this, getExtraEdgeHashcode());
	}

	public void setToJumpPolynomial(Node startingPosition, int nodeColoumn, float jumpRange, float jumpHeight) {
		//The x coordinate is measured as a function of it position in the level matrix.
		//This does not cause any problems, as a given edge is only made using one and only one level matrix,
		//and the equals method does not discern edges based on this.
		
		//TODO described it in the report.
		
		
		a = -4 * jumpHeight / (jumpRange * jumpRange);
		b = (8 * nodeColoumn + 4 * jumpRange) * jumpHeight / (jumpRange * jumpRange);
		c = (-4 * nodeColoumn * (nodeColoumn + jumpRange) * jumpHeight + startingPosition.y * jumpRange * jumpRange) / (jumpRange * jumpRange);
		setTopPoint();
	}
	
	public void setToFallPolynomial(Node startingPosition, int nodeColumn, float fallRange) {
		a = (-4) / (fallRange*fallRange);
		b = ((8*nodeColumn)/(fallRange*fallRange));
		c = - (- startingPosition.y * fallRange * fallRange + 4 * nodeColumn * nodeColumn)/(fallRange*fallRange);
		//We want to directly set the toppoint, as this must be precise, 
		//to ensure that the jumpAlong algorithm makes no mistakes.
		
		//Its toppoint is exactly at its starting position
		setTopPoint(nodeColumn, startingPosition.y);
	}

	public boolean isPastTopPoint(JumpDirection direction, int currentXPosition) {
		return (direction.getHorizontalDirectionAsInt() == 1 	&& topPointX <= currentXPosition || //Going right
				  direction.getHorizontalDirectionAsInt() == -1 && topPointX >= currentXPosition);  //Going left.
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
		return topPointY - source.y;
	}

	public float getWeight() {
		return 0.1f;
	}
	
	@Override
	protected byte getExtraEdgeHashcode() {
		final byte jumpType = 0b0001_0000;
		//Its jump height. Max is 4 min is 0, giving 3 bits.
		//3 plus 1 but for jump type 
		final byte jumpHeight = (byte)Math.round(getMaxY()); 
		return (byte) (jumpHeight | jumpType);
	}
	
	public FallEdge getCorrespondingFallEdge(){
		return new FallEdge(this.source, this.target, useSuperSpeed);
	}
	
	public float getParameterA(){
		return this.a;
	}

	public float getParameterB(){
		return this.b;
	}
	
	public float getParameterC(){
		return this.c;
	}
	
}
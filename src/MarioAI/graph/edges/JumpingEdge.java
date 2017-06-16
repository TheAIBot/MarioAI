package MarioAI.graph.edges;

import MarioAI.Hasher;
import MarioAI.graph.edges.edgeCreation.JumpDirection;
import MarioAI.graph.nodes.Node;

/**A directed edge for jumping
 * @author jesper
 *
 */
public class JumpingEdge extends DirectedEdge {
	private float a;
	private float b;
	private float c; // Parameters of the polynomial.
	private float topPointX;
	private float topPointY; // Coordinates of the toppunkt
	public int ceiledTopPointX;
	public int ceiledTopPointY; // Ceiled coordinates of the toppunkt
	
	/** Initializes a jumpingedge to have the given source and target, as well as following the given polynial.
	 * @param source Source of the edge.
	 * @param target Target of the edge.
	 * @param polynomial Polynomial the edge should follow.
	 * @param useSuperSpeed Whether it should use superspeed or not.
	 */
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


	/** Initializes a jumping edge to have the given source and target. Will not follow a specific polynomial.
	 * @param source Source of the edge.
	 * @param target Target of the edge.
	 */
	public JumpingEdge(Node source, Node target) {
		this(source, target, false);
	}
	

	/** Initializes a jumping edge to have the given source and target. Will not follow a specific polynomial.
	 * @param source Source of the edge.
	 * @param target Target of the edge.
	 * @param useSuperSpeed Whether it should use superspeed or not.
	 */
	public JumpingEdge(Node source, Node target, boolean useSuperSpeed) {
		super(source, target, useSuperSpeed);
		this.a = 0;
		this.b = 0;
		this.c = 0;
		//This shouldn't be hashed
		//hash = Hasher.hashEdge(this, getExtraEdgeHashcode());
	}
	/** Initializes a jumping edge to have the given source and target. Will not follow a specific polynomial.
	 * Set to jump to a given height ceiledTopPointY.
	 * @param source Source of the edge.
	 * @param target Target of the edge.
	 * @param ceiledTopPointY The height the jump ends at.
	 * @param useSuperSpeed Whether to use superspeed or not.
	 */
	public JumpingEdge(Node source, Node target, int ceiledTopPointY, boolean useSuperSpeed) {
		this(source, target, useSuperSpeed);
		this.ceiledTopPointY = ceiledTopPointY + source.y;
		this.topPointY = ceiledTopPointY;
		hash = Hasher.hashEdge(this, getExtraEdgeHashcode());
	}
	
	/**Sets the jumpingedge to follow a given polynomial, based on the given parameters.
	 * The explanation of the relation can be seen in the report.
	 * @param startingPosition The Node from which the jump stems.
	 * @param nodeColoumn The column in a levelmatrix the jump starts at
	 * @param jumpRange Range of the jump on a flat level.
	 * @param jumpHeight Height of the jump.
	 */
	public void setToJumpPolynomial(Node startingPosition, int nodeColoumn, float jumpRange, float jumpHeight) {
		//The x coordinate is measured as a function of it position in the level matrix.
		//This does not cause any problems, as a given edge is only made using one and only one level matrix,
		//and the equals method does not discern edges based on this.
		
		
		
		a = -4 * jumpHeight / (jumpRange * jumpRange);
		b = (8 * nodeColoumn + 4 * jumpRange) * jumpHeight / (jumpRange * jumpRange);
		c = (-4 * nodeColoumn * (nodeColoumn + jumpRange) * jumpHeight + startingPosition.y * jumpRange * jumpRange) / (jumpRange * jumpRange);
		setTopPoint();
	}

	/**Sets the jumpingedge to follow a given fall polynomial, based on the given parameters.
	 * The explanation of the relation can be seen in the report.
	 * @param startingPosition The Node from which the jump stems.
	 * @param nodeColoumn The column in a levelmatrix the jump starts at
	 * @param fallRange Lenght it will fall from it's initial position, when it has gone 4 blocks down.
	 */
	public void setToFallPolynomial(Node startingPosition, int nodeColumn, float fallRange) {
		a = (-4) / (fallRange*fallRange);
		b = ((8*nodeColumn)/(fallRange*fallRange));
		c = - (- startingPosition.y * fallRange * fallRange + 4 * nodeColumn * nodeColumn)/(fallRange*fallRange);
		//We want to directly set the toppoint, as this must be precise, 
		//to ensure that the jumpAlong algorithm makes no mistakes.
		
		//Its toppoint is exactly at its starting position
		setTopPoint(nodeColumn, startingPosition.y);
	}

	/** Returns whether or not a given x position is past the polynomials top point, 
	 *  based on the direction it is going.
	 * @param direction Direction of the jump.
	 * @param currentXPosition The x position of the jump.
	 * @return True if it is past the top point, else false.
	 */
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

	/**Based on the a, b and c values of the polynomial, it sets its top point.
	 */
	private void setTopPoint() {
		float x = ((-b / a) / 2); //Maths
		float y = f(x);
		setTopPoint(x, y);
	}
	/** Sets the coordinates of the top point of the edge, to the given coordinates.
	 * @param x X coordinate of the top point.
	 * @param y Y coordinate of the top point.
	 */
	public void setTopPoint(float x, float y) {
		topPointX = x;
		topPointY = y;
		ceiledTopPointX = (short) Math.ceil(topPointX);
		ceiledTopPointY = (short) Math.ceil(topPointY);
	}
	/** Gets the height of the jump at a given x value. Follows a polynomial.
	 * @param x The x value to get the height at.
	 * @return The height of the jump at the given x value.
	 */
	public float f(float x) {
		return a * x * x + b * x + c;
	}
	
	@Override
	public float getMaxY() {
		return topPointY - source.y;
	}

	public float getWeight() {
		return 2.1f;
	}
	
	@Override
	protected byte getExtraEdgeHashcode() {
		final byte jumpType = 0b0001_0000;
		//Its jump height. Max is 4 min is 0, giving 3 bits.
		//3 plus 1 but for jump type 
		final byte jumpHeight = (byte)Math.round(getMaxY()); 
		return (byte) (jumpHeight | jumpType);
	}
	/** Gets the jump edge converted into a fall edge.
	 * @return The jump edge converted to a fall edge.
	 */
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
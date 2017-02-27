package MarioAI;

public class SecondOrderPolynomial {
	private float a;
	private float b;
	private float c; // Parameters of the polynomial.
	private float topPointX;
	private float topPointY; // Cordinates of the toppunkt
	// TODO Maybe change ceil after discussions. Problem associated with making
	// this into an integer
	// , as sometimes two possible jump position in a row will be registrated as
	// possible to reach,
	// even though it is only one it should be possible to reach. Mario changing
	// position in the air can compensate for this.
	private short ceiledTopPointX;
	private short ceiledTopPointY; // Ceildes coordinates of the toppunkt

	public SecondOrderPolynomial(Node startingPosition, short nodeColoumn, float jumpRange, float jumpHeight) {
		setToJumpPolynomial(startingPosition, nodeColoumn, jumpRange, jumpHeight);
	}

	public SecondOrderPolynomial() {
		a = 0;
		b = 0;
		c = 0;
	}

	//
	public void setToJumpPolynomial(Node startingPosition, short nodeColoumn, float jumpRange, float jumpHeight) {
		a = -4 * jumpHeight / (jumpRange * jumpRange);
		b = (8 * nodeColoumn + 4 * jumpRange) * jumpHeight / (jumpRange * jumpRange);
		c = (-4 * nodeColoumn * (nodeColoumn + jumpRange) * jumpHeight + startingPosition.y * jumpRange * jumpRange) / (jumpRange * jumpRange);
		setTopPoint();
	}

	public boolean isPastTopPoint(short startPosition, short currentPosition) {
		return (startPosition <= topPointX && topPointX <= currentPosition || startPosition >= topPointX && topPointX >= currentPosition);
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
		topPointX = ((-b / a) / 2);
		topPointY = f(topPointX);
		ceiledTopPointX = (short) Math.ceil(ceiledTopPointX);
		ceiledTopPointY = (short) Math.ceil(ceiledTopPointY);
	}

	public float f(float x) {
		return a * x * x + b * x + c;
	}

}
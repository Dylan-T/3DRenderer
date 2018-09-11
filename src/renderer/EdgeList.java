package renderer;

/**
 * EdgeList should store the data for the edge list of a single polygon in your
 * scene. A few method stubs have been provided so that it can be tested, but
 * you'll need to fill in all the details.
 *
 * You'll probably want to add some setters as well as getters or, for example,
 * an addRow(y, xLeft, xRight, zLeft, zRight) method.
 */
public class EdgeList {
	float[][] edgelist;
	int startY;
	int endY;


	public EdgeList(int startY, int endY) {
		this.startY = startY;
		this.endY = endY;
		edgelist = new float[4][endY-startY + 1];
	}

	public void addLeftRow(int y, float xLeft, float zLeft){
		if(!(y< 0) && !(y > endY)) {
			edgelist[0][y - startY] = xLeft;
			edgelist[1][y - startY] = zLeft;
		}
	}

	public void addRightRow(int y, float xRight, float zRight){
		if(!(y< 0) && !(y > endY)) {
			edgelist[2][y - startY] = xRight;
			edgelist[3][y - startY] = zRight;
		}
	}

	public int getStartY() {
		return startY;
	}

	public int getEndY() {
		return endY;
	}

	public float getLeftX(int y) {
		return edgelist[0][y - startY];
	}

	public float getRightX(int y) {
		return edgelist[2][y - startY];
	}

	public float getLeftZ(int y) {
		return edgelist[1][y - startY];
	}

	public float getRightZ(int y) {
		return edgelist[3][y - startY];
	}
}

// code for comp261 assignments

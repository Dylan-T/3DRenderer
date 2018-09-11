package renderer;

import java.awt.Color;
import java.util.ArrayList;

import renderer.Scene.Polygon;

/**
 * The Pipeline class has method stubs for all the major components of the
 * rendering pipeline, for you to fill in.
 *
 * Some of these methods can get quite long, in which case you should strongly
 * consider moving them out into their own file. You'll need to update the
 * imports in the test suite if you do.
 */
public class Pipeline {

	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise.
	 */
	public static boolean isHidden(Polygon poly) {
		//STAGE 1
		/*
		 * viewing along z axis
		 * visible if normal has negative Z coordinate value
		 *
		 */

		//get normal
		Vector3D[] points = poly.getVertices();
		Vector3D a = points[1].minus(points[0]);
		Vector3D b = points[2].minus(points[1]);
		Vector3D n = a.crossProduct(b);
		//if z positive is hidden
		return n.z > 0;
	}

	/**
	 * Computes the colour of a polygon on the screen, once the lights, their
	 * angles relative to the polygon's face, and the reflectance of the polygon
	 * have been accounted for.
	 *
	 * @param lightDirection
	 *            The Vector3D pointing to the directional light read in from
	 *            the file.
	 * @param lightColor
	 *            The color of that directional light.
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend
	 *            on the direction.
	 */
	public static Color getShading(Polygon poly, Vector3D lightDirection, Color lightColor, Color ambientLight) {
		//STAGE 1

		//calculate normal
		Vector3D[] points = poly.getVertices();
		Vector3D a = points[1].minus(points[0]);
		Vector3D b = points[2].minus(points[1]);
		Vector3D n = a.crossProduct(b);

		//calculate cos(theta)
		float angle = n.cosTheta(lightDirection);
		if(angle < 0) angle = 0;

		//calculate shading
		int red = (int) (ambientLight.getRed()*(poly.getReflectance().getRed()/255.0f) + lightColor.getRed()*(poly.getReflectance().getRed()/255.0f)*angle);
		int green = (int) (ambientLight.getGreen()*(poly.getReflectance().getGreen()/255.0f) + (lightColor.getGreen()*poly.getReflectance().getGreen()/255.0f)*angle);
		int blue = (int) (ambientLight.getBlue()*(poly.getReflectance().getBlue()/255.0f) + (lightColor.getBlue()*poly.getReflectance().getBlue()/255.0f)*angle);
		if(red > 255) red = 255;
		if(green > 255) green = 255;
		if(blue > 255) blue = 255;

		return new Color(red, green, blue);
	}

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 *
	 * @param scene
	 *            The original Scene.
	 * @param xRot
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yRot
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xRot, float yRot) {

		Transform rotMat = Transform.newXRotation(xRot).compose(Transform.newYRotation(yRot));

		Vector3D light = rotMat.multiply(scene.getLight());
		ArrayList<Polygon> polygons = new ArrayList<Polygon>();
		for(Polygon p: scene.getPolygons()) {
			Vector3D[] v = p.getVertices();
			for(int i = 0; i < v.length; i++) {
				v[i] = rotMat.multiply(v[i]);
			}
			polygons.add(new Polygon(v[0], v[1], v[2], p.getReflectance()));
		}
		return new Scene(polygons ,light);
	}

	/**
	 * This should translate the scene by the appropriate amount.
	 *
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene) {
		// TODO fill this in.
		return null;
	}

	/**
	 * This should scale the scene.
	 *
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene) {
		// TODO fill this in.
		return null;
	}

	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		int startY = Integer.MAX_VALUE;
		int endY = Integer.MIN_VALUE;
		//Get startY & endY
		for(Vector3D v: poly.getVertices()) {
			if(startY > v.y) startY = (int) v.y;
			if(endY < v.y) endY = (int) v.y;
		}
		EdgeList edge = new EdgeList(startY, endY);

		/**
		 * Scan each edge and update the 2-column EdgeList
		 *
		 */
		Vector3D b;
		Vector3D a;
		for(int i = 0; i < poly.vertices.length; i++){ //each edge
			int j = i+1;
			if(j >= 3) j = 0;

			a = poly.getVertices()[i];
			b = poly.getVertices()[j];


			//Get slopes
			float xSlope = (b.x-a.x)/(b.y-a.y);
			float zSlope = (b.z-a.z)/(b.y-a.y);
			//Get initial values
			float x = a.x;
			float z = a.z;
			float y = a.y;
			if(a.y < b.y) {	//if going down
				while(y <= Math.round(b.y)) {
					edge.addLeftRow(Math.round(y), x, z);
					x += xSlope;
					z += zSlope;
					y++;
				}
			}else { //if going up
				while(y >= Math.round(b.y)) {
					edge.addRightRow(Math.round(y), x, z);
					x -= xSlope;
					z -= zSlope;
					y = y - 1;
				}
			}
		}
		return edge;
	}

	/**
	 * Fills a zbuffer with the contents of a single edge list according to the
	 * lecture slides.
	 *
	 * The idea here is to make zbuffer and zdepth arrays in your main loop, and
	 * pass them into the method to be modified.
	 *
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor) {
		int startY = polyEdgeList.getStartY();
		int endY = polyEdgeList.getEndY();

		for(int y = startY; y < endY; y++) {

			//~~~ edit This ~~~ do not render pixels that are out-of-boundary
	        if (y + startY < 0 || y + startY >= zbuffer.length) continue;

			float slope = (polyEdgeList.getRightZ(y) - polyEdgeList.getLeftZ(y)) /(polyEdgeList.getRightX(y) - polyEdgeList.getLeftX(y));
			int x = Math.round(polyEdgeList.getLeftX(y));
			float z = polyEdgeList.getLeftZ(y) + slope * (x - polyEdgeList.getLeftX(y));

			while(x < Math.round(polyEdgeList.getRightX(y))) {
				// do not render pixels that are out-of-boundary
	            if (x < 0 || x >= zbuffer.length) {
	                z += slope;
	                x++;
	                continue;
	            }

				if(z < zdepth[x][y]) {
					zbuffer[x][y] = polyColor;
					zdepth[x][y] = z;
				}
				z += slope;
				x++;
			}
		}
	}
}

// code for comp261 assignments

package renderer;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import renderer.Scene.Polygon;

public class Renderer extends GUI {
	Scene scene;

	@Override
	protected void onLoad(File file) {
		/*
		 * This method should parse the given file into a Scene object, which
		 * you store and use to render an image.
		 */

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			//Get light source
			String line = reader.readLine();
			String[] tokens = line.split(" ");
			float x = Float.parseFloat(tokens[0]);
			float y = Float.parseFloat(tokens[1]);
			float z = Float.parseFloat(tokens[2]);
			Vector3D light = new Vector3D(x,y,z);

			//Load polygons
			List<Polygon> polygons = new ArrayList<Polygon>();
			while((line = reader.readLine()) != null) {
				tokens = line.split(" ");
				//polygon points
				float[] points = new float[9];
				for(int i= 0; i < 9; i++) {
					points[i] = Float.parseFloat(tokens[i]);
				}
				//polygon color
				int[] color = new int[3];
				int count = 0;
				for(int i = 9; i < 12; i++) {
					color[count] = Integer.parseInt(tokens[i]);
					count++;
				}
				polygons.add(new Polygon(points, color));
			}
			scene = new Scene(polygons, light); //Create scene
			scene = Pipeline.scaleScene(scene);
			scene = Pipeline.translateScene(scene);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onKeyPress(KeyEvent ev) {
		if(scene == null) return;
		switch(ev.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			scene = Pipeline.rotateScene(scene, 0, -0.5f);
			break;
		case KeyEvent.VK_RIGHT:
			scene = Pipeline.rotateScene(scene, 0, 0.5f);
			break;
		case KeyEvent.VK_UP:
			scene = Pipeline.rotateScene(scene, -0.5f, 0);
			break;
		case KeyEvent.VK_DOWN:
			scene = Pipeline.rotateScene(scene, 0.5f, 0);
			break;
		}
		redraw();
	}

	@Override
	protected BufferedImage render() {
		/*
		 * This method should put together the pieces of your renderer, as
		 * described in the lecture. This will involve calling each of the
		 * static method stubs in the Pipeline class, which you also need to
		 * fill in.
		 */
		Color[][] zbuffer = new Color[CANVAS_WIDTH][CANVAS_HEIGHT];
		float[][] zdepth = new float[CANVAS_WIDTH][CANVAS_HEIGHT];
		for(int i = 0; i < zbuffer.length; i++) {
			for(int j = 0; j < zbuffer[i].length; j++) {
				zdepth[i][j] = Float.POSITIVE_INFINITY;
				zbuffer[i][j] = Color.GRAY;
			}
		}

		if(scene == null) return convertBitmapToImage(zbuffer); //If there's no scene render blank image

		for(Polygon poly: scene.getPolygons()) {
			if(!Pipeline.isHidden(poly)) {
				Color polyColor = Pipeline.getShading(poly, scene.getLight(), new Color(100,100,100), new Color(getAmbientLight()[0], getAmbientLight()[1], getAmbientLight()[2]));
				EdgeList EL = Pipeline.computeEdgeList(poly);
				Pipeline.computeZBuffer(zbuffer, zdepth, EL, polyColor);
			}
		}
		return convertBitmapToImage(zbuffer);
	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static void main(String[] args) {
		new Renderer();
	}
}

// code for comp261 assignments

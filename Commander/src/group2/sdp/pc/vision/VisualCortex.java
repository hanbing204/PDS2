package group2.sdp.pc.vision;

import group2.sdp.pc.breadbin.StaticRobotInfo;
import group2.sdp.pc.globalinfo.GlobalInfo;
import group2.sdp.pc.globalinfo.LCHColourSettings.ColourClass;
import group2.sdp.pc.vision.skeleton.ImageConsumer;
import group2.sdp.pc.vision.skeleton.StaticInfoConsumer;
import group2.sdp.pc.vision.skeleton.VisualCortexSkeleton;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * 
 *<p><b>VisualCortex:</b> An Image Consumer</br>
 *<p><b>Description:</b></br>
 *The visual cortex of Alfie. Does main processing on the image. 
 *<p><b>Main client:</b> Bakery
 *<p><b>Responsibilities:</b></br>
 *Extracts image features: position of the ball and the 
 *robots, and orientation of the T-shapes on the top of the 
 *robots. Passes that information to a Static Info Consumer 
 *that is supplied on construction of the VisualCortex.
 *<p><b> According to Alfie:</b></br>
 *"This is how I process what I see on the pitch.
 *I first remove the background to determine what pixels have
 *changed, and therefore what pixels I need to consider when 
 *looking for myself, the other robot and the ball.
 *To find the robots I look for the largest connected area 
 *of that colour. I then use repeated regression to ensure 
 *the angle is correct.
 * @author Alfie
 *
 */
public class VisualCortex extends VisualCortexSkeleton {

	/**
	 * The mode of the output from the processor: MATCH is the default, CHROMA
	 * and LUMA are used during setup.
	 */
	public enum OutputMode {
		MATCH,
		CHROMA,
		LUMA
	}
	
	private OutputMode currentMode = OutputMode.MATCH;

	/**
	* Shows whether the background has to be updated or not.
	*/
	private boolean extractBackground;

	/**
	* An image of the background - image in each frame is 
	* 							compared to the background
	*/
	private BufferedImage backgroundImage;

	private static final boolean VERBOSE = true;
	
	private String backgroundFileName = "background.png";
	private boolean saveBackground = false;

	/**
	 * New pixels in the last frame that was processed.
	 */
	private ArrayList<Point> newPixels;

	// values to return
	private Point blueCentroid, yellowCentroid, ballCentroid;
	// private Point plateCentroidYellowRobot;
	private double blueDir, yellowDir;
	
	// TODO: think of a better name/way of doing this
	// private boolean isYellowRobotRightGoal = true;

	private final int EXPECTED_ROBOT_SIZE = 400;
	private final int EXPECTED_BALL_SIZE = 120;

	/**
	 * See parent's comment.
	 */
	public VisualCortex(GlobalInfo globalInfo, StaticInfoConsumer consumer) {
		super(globalInfo, consumer);
		extractBackground = true;
		newPixels = new ArrayList<Point>();
	}

	/**
	 * See parent's comment.
	 */
	public VisualCortex(GlobalInfo globalInfo, Bakery bakery,
			ImageConsumer imageConsumer) {
		super(globalInfo, bakery, imageConsumer);
		extractBackground = true;
		newPixels = new ArrayList<Point>();
	}

	/**
	 * In addition to processing the frame extracts the background if needed.
	 */
	@Override
	public void process(BufferedImage image) {
		if (extractBackground) {
			if (!saveBackground) {
				backgroundImage = loadBackgroundImage();
				if (backgroundImage == null) {
					backgroundImage = image;
				}
			} else {
				backgroundImage = image;
				saveBackgroundImage(backgroundImage);
			}
			extractBackground = false;
			// Note that the super.process(image) is not called.
			// This is the case, since we expect that the background
			// contains no objects worth of detection.
		} else {
			newPixels = getDifferentPixels(image);
			internalImage = drawPixels(image, newPixels);
			drawStuff(internalImage);
			super.process(image);
		}
		detectRobotsAndBall(image, newPixels);
	}

	/**
	 * Grabs a new background image and saves it.
	 */
	public void grabNewBackgroundImage() {
		extractBackground = true;
		saveBackground = true;
	}

	/**
	 * Loads the background image from the file that is known to contain it and
	 * returns it.
	 * 
	 * @return The background image.
	 */
	private BufferedImage loadBackgroundImage() {
		File inputfile = new File(backgroundFileName);
		BufferedImage image = null;
		try {
			image = ImageIO.read(inputfile);
			System.out.println("Background image loaded.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * Saves the given image as a background image to the file used for the
	 * purpose.
	 * 
	 * @param image
	 *            The image to save as a background image.
	 */
	private void saveBackgroundImage(BufferedImage image) {
		File outputfile = new File(backgroundFileName);
		try {
			ImageIO.write(image, "png", outputfile);
			System.out.println("Background image saved.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The pixels that are sufficiently different from the background are added
	 * to a list that is returned.
	 * 
	 * @param image
	 *            The image to subtract from the background image.
	 * @return A list of the _different_ points.
	 * @see isDifferent
	 */
	private ArrayList<Point> getDifferentPixels(BufferedImage image) {
		Rectangle pitchCrop = globalInfo.getPitch().getCamera().getPitchCrop();
		int minX = Math.max(pitchCrop.x, image.getMinX());
		int minY = Math.max(pitchCrop.y, image.getMinY());
		int w = Math.min(pitchCrop.width, image.getWidth());
		int h = Math.min(pitchCrop.height, image.getHeight());

		int maxX = minX + w;
		int maxY = minY + h;

		ArrayList<Point> result = new ArrayList<Point>();
		for (int y = minY; y < maxY; ++y) {
			for (int x = minX; x < maxX; ++x) {
				if (isDifferent(image, x, y)) {
					result.add(new Point(x, y));
				}
			}
		}
		return result;
	}

	/**
	 * Compares if the given pixel in the given image is different from the
	 * corresponding pixel in the background image. The current implementation
	 * subtracts the background from the given image and checks if the result
	 * passes a certain threshold.
	 * 
	 * @param image
	 *            The pixel to compare is taken from this picture.
	 * @param x
	 *            The x coordinate of the pixel.
	 * @param y
	 *            The y coordinate of the pixel.
	 * @return True if the specified pixel is different, false otherwise.
	 */
	private boolean isDifferent(BufferedImage image, int x, int y) {
		int threshold = 90;

		Color imagePixel = new Color(image.getRGB(x, y));
		Color backPixel = new Color(backgroundImage.getRGB(x, y));

		int[] delta = new int[] {
				Math.abs(imagePixel.getRed() - backPixel.getRed()),
				Math.abs(imagePixel.getGreen() - backPixel.getGreen()),
				Math.abs(imagePixel.getBlue() - backPixel.getBlue()) };

		return delta[0] + delta[1] + delta[2] > threshold;
	}

	// TODO: probably better to split this up.
	/**
	 * This function is where everything except background removal is done. The
	 * robot 'T's are detected, processed and have their centroids and
	 * orientations calculated. These values are stored in the respective global
	 * variables.
	 * 
	 * @param image
	 * @param newPixels
	 *            The pixels which are "different" (calculated by background
	 *            removal).
	 * @see #getDifferentPixels(BufferedImage)
	 * @see #getGreatestArea(ArrayList)
	 * @see #regressionAndDirection(BufferedImage, ArrayList, boolean)
	 */
	public void detectRobotsAndBall(BufferedImage image,
			ArrayList<Point> newPixels) {
		ArrayList<Point> yellowPoints = new ArrayList<Point>();
		ArrayList<Point> bluePoints = new ArrayList<Point>();
		ArrayList<Point> ballPoints = new ArrayList<Point>();
		ArrayList<Point> platePoints = new ArrayList<Point>();
		for (Point p : newPixels) {
			Color c = new Color(image.getRGB(p.x, p.y));
			LCHColour lch = new LCHColour(c);
			ColourClass cc = globalInfo.getColourSettings().getColourClass(lch);

			switch (cc) {
			case RED:
				ballPoints.add(p);
				break;
			case GREEN_PLATE:
				platePoints.add(p);
				break;
			case BLUE:
				bluePoints.add(p);
				break;
			case YELLOW:
				yellowPoints.add(p);
				break;
			}
		}
		ArrayList<Point> yellowPointsClean = new ArrayList<Point>(0);
		ArrayList<Point> bluePointsClean = new ArrayList<Point>(0);
		ArrayList<Point> ballPointsClean = new ArrayList<Point>(0);

		if (sizeCheck(bluePoints, EXPECTED_ROBOT_SIZE)) {

			bluePointsClean = getGreatestArea(bluePoints);
		}
		if (sizeCheck(yellowPoints, EXPECTED_ROBOT_SIZE)) {

			yellowPointsClean = getGreatestArea(yellowPoints);
		}
		if (sizeCheck(ballPoints, EXPECTED_BALL_SIZE)) {
			ballPointsClean = getGreatestArea(ballPoints);
		}

		this.ballCentroid = calcCentroid(ballPointsClean);
		this.blueCentroid = calcCentroid(bluePointsClean);
		this.yellowCentroid = calcCentroid(yellowPointsClean);

		this.blueDir = regressionAndDirection(image, bluePointsClean, false) % 360;
		this.yellowDir = regressionAndDirection(image, yellowPointsClean, true) % 360;

	}

	// TODO: Fix comments, extract constants.
	/**
	 * Checks if the array list is within some bounds of the expected size.
	 * 
	 * @param points
	 * @param expectedSize
	 * @return
	 */
	private boolean sizeCheck(ArrayList<Point> points, int expectedSize) {
		if (points.size() > expectedSize * 4
				&& points.size() < expectedSize * 0.4) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Loops through allPoints calling
	 * {@link #mindFlower(ArrayList,ArrayList,Point)} on pixels in it.
	 * mindFlower removes pixels from allPoints if it determines they are
	 * connected to another pixel so this will almost never cycle through the
	 * *whole* ArrayList.
	 * @param allPoints
	 *            the list of points of the same colour
	 *            (yellowPoints/bluePoints)
	 * @return the ArrayList of connected pixels in allPoints which form the
	 *         largest area
	 */
	public ArrayList<Point> getGreatestArea(ArrayList<Point> allPoints) {
		ArrayList<Point> bestArea = new ArrayList<Point>();
		// this while loop should end because mindFlower
		// removes points from allPoints
		while (allPoints.size() != 0) {
			ArrayList<Point> newArea = new ArrayList<Point>();

			newArea.add(allPoints.get(0));
			allPoints.remove(0);
			newArea = mindFlower(newArea, allPoints, newArea.get(0));
			if (newArea.size() > bestArea.size()) {
				bestArea = newArea;
			}
		}
		return bestArea;
	}

	// TODO: fix comments (arguments), names
	/**
	 * Uses {@link #findFacingDirection(BufferedImage, Point, boolean)} to get a
	 * starting direction and then refines it using
	 * {@link #regression(ArrayList, double, boolean)}.
	 * 
	 * @param image
	 * @param fixels
	 * @param isYellow
	 * @return the direction 0 < x < 360 degrees.
	 */
	public double regressionAndDirection(BufferedImage image,
			ArrayList<Point> fixels, boolean isYellow) {

		// for regression
		double end_angle = 0;

		Point fixelsCentroid = calcCentroid(fixels);

		double actualDir;
		if (isYellow) {
			actualDir = (findFacingDirection(image, fixelsCentroid, true));
		} else {
			actualDir = (findFacingDirection(image, fixelsCentroid, false));
		}

		double m = 0;
		double newangle = actualDir;
		for (int i = 0; i < 5; i++) {
			m = regression(fixels, newangle, isYellow);
			newangle = (newangle) - Math.toDegrees(Math.atan(m));

		}
		end_angle = newangle;
		return end_angle;
	}

	// TODO: figure out if it is actually slow, then act accordingly.
	/**
	 * This function is supposed to be *slow*. Do not use apart from testing.
	 * 
	 * @param pixels
	 *            The pixels to draw on a new image.
	 * @return
	 */
	private BufferedImage drawPixels(BufferedImage image, List<Point> pixels) {
		int w = backgroundImage.getWidth();
		int h = backgroundImage.getHeight();
		BufferedImage result = new BufferedImage(w, h,
				BufferedImage.TYPE_3BYTE_BGR);
		for (Point p : newPixels) {
			Color c = new Color(image.getRGB(p.x, p.y));
			LCHColour lch = new LCHColour(c);
			ColourClass cc = globalInfo.getColourSettings().getColourClass(lch);

			Color dc = null;
			switch (cc) {
			case RED:
				dc = Color.RED;
				break;
			case GREEN_PLATE:
				dc = Color.GREEN;
				break;
			case GREEN_PITCH:
				dc = Color.LIGHT_GRAY;
				break;
			case BLUE:
				dc = Color.BLUE;
				break;
			case YELLOW:
				dc = Color.YELLOW;
				break;
			case GRAY:
				dc = Color.DARK_GRAY;
				break;
			default:
				dc = Color.CYAN;
				break;
			}
			int v;
			switch (currentMode) {
			case MATCH:
				result.setRGB(p.x, p.y, dc.getRGB());
				break;
			case CHROMA:
				v = lch.getChroma();
				result.setRGB(p.x, p.y, new Color(v, v, v).getRGB());
				break;
			case LUMA:
				v = lch.getLuma();
				result.setRGB(p.x, p.y, new Color(v, v, v).getRGB());
				break;
			}
		}
		return result;
	}

	/**
	 * Called by {@link #getGreatestArea(ArrayList)}. Looks for adjacent points
	 * to determine connected areas
	 * 
	 * @param newArea
	 *            connected pixels are stored in here
	 * @param allPoints
	 *            the list of all possible points (e.g. all yellow/blue points)
	 * @param pixel
	 *            the pixel to which all other points should be connected
	 * @return all points connected to pixel which are present in allPoints.
	 */

	public ArrayList<Point> mindFlower(ArrayList<Point> newArea,
			ArrayList<Point> allPoints, Point pixel) {
		Point north = new Point(pixel.x, pixel.y - 1);
		Point east = new Point(pixel.x + 1, pixel.y);
		Point south = new Point(pixel.x, pixel.y + 1);
		Point west = new Point(pixel.x - 1, pixel.y);

		if (allPoints.remove(north)) {
			newArea.add(north);
			mindFlower(newArea, allPoints, north);
		}
		if (allPoints.remove(east)) {
			newArea.add(east);
			mindFlower(newArea, allPoints, east);
		}
		if (allPoints.remove(south)) {
			newArea.add(south);
			mindFlower(newArea, allPoints, south);
		}
		if (allPoints.remove(west)) {
			newArea.add(west);
			mindFlower(newArea, allPoints, west);
		}

		return newArea;
	}

	// TODO: fix comments (almost perfect), names, code structure redesign
	/**
	 * Cycles through all (360) possible angles and finds the longest unbroken
	 * line from the centroid. The angle at which this line was found is the
	 * angle which is returned.
	 * 
	 * @param image
	 *            The image to draw on
	 * @param centroid
	 *            The centroid of the robot
	 * @param isYellow
	 *            If the robot is yellow
	 * @return Angle of robot in degrees w.r.t x-axis. Increases CCW.
	 */
	public int findFacingDirection(BufferedImage image, Point centroid,
			boolean isYellow) {
		if (centroid == null) {
			return -1;
		}
		int cur_score = 0;
		int cur_score2 = 0;
		int best_score = 0;
		int best_angle = 0;
		if (centroid.x != 0) {

			for (int i = 0; i < 360; i++) {
				cur_score = 0;
				cur_score2 = 0;
				Point nextPixel = new Point();
				nextPixel.x = centroid.x;
				nextPixel.y = centroid.y;
				Point rot_pixel = rotatePoint(centroid, new Point(nextPixel.x,
						nextPixel.y), i);
				/**
				 * Do not stop until the next pixel colour is not the colour we
				 * are looking for. The next pixel is determined by travelling
				 * in the negative x direction and then rotating the point i
				 * degrees around the centroid.
				 */

				while (isBlueYellow(image, rot_pixel, isYellow)) {

					cur_score++; // Since we sort in ascending order, lower
					// score is longer segments

					nextPixel = new Point(centroid.x + cur_score, centroid.y);
					rot_pixel = rotatePoint(centroid, new Point(nextPixel.x,
							nextPixel.y), i);
				}
				while (isBlueYellow(image, rot_pixel, isYellow)) {

					cur_score2++; // Since we sort in ascending order, lower
					// score is longer segments

					nextPixel = new Point(centroid.x + cur_score, centroid.y);
					rot_pixel = rotatePoint(centroid, new Point(nextPixel.x,
							nextPixel.y), i + 180);
				}

				if (cur_score + cur_score2 > best_score) {

					if (cur_score > cur_score2) {
						best_angle = i;
					} else {
						best_angle = (i + 180) % 360;
					}

					best_score = cur_score + cur_score2;
				}

			}
		}

		return 360 - best_angle;
	}

	/**
	 * Performs regression on the pixels, using the angle that
	 * {@link #findFacingDirection(BufferedImage, Point, boolean)}
	 * returns  
	 * @param fixels
	 * @param angle
	 * @return angle
	 */
	protected double regression(ArrayList<Point> fixels, double angle, boolean isYellow) {

		Point actualCentroid = blueCentroid;
		if (isYellow) {
			actualCentroid = yellowCentroid;
		}

		double sumX = 0;
		double sumY = 0;
		double productXY = 0;
		double productXsquared = 0;
		double productYsquared = 0;
		int size = fixels.size();

		for (int i = 0; i < fixels.size(); i++) {

			//translate the point onto the main axes
			int normalisedX = fixels.get(i).x - actualCentroid.x;
			int normalisedY = fixels.get(i).y - actualCentroid.y;

			//rotate the point while it's still on the main axes
			double cosAngle = Math.cos(Math.toRadians(angle));
			double sinAngle = Math.sin(Math.toRadians(angle));
			double rotatedX = normalisedX * cosAngle
			- normalisedY * sinAngle;
			double rotatedY = normalisedX * sinAngle
			+ normalisedY * cosAngle;

			//translate the point back to its original position
			rotatedX += actualCentroid.x;
			rotatedY += actualCentroid.y;

			//sum together the x and y coordinates of every point
			//in the ArrayList of pixels
			sumX += rotatedX;
			sumY += rotatedY;

			productXY += rotatedX * rotatedY;

			productXsquared += Math.pow(rotatedX, 2);
			productYsquared += Math.pow(rotatedY, 2);

		}

		return (size * productXY - sumX * sumY) / (size * productXsquared - sumX * sumX);
	}

	/**
	 * Checks if a given pixel on the image is blue or is yellow  
	 * depending on the parameter isYellow
	 * @param colour The colour you are checking
	 * @param isYellow If you are looking for yellow (the other option is blue)
	 * @return --
	 */
	private boolean isBlueYellow(BufferedImage image, Point pixel, boolean isYellow) {
		boolean returnValue = false;
		int width = 640;
		int height = 480;
		if (!(pixel.x >= 0 && pixel.x < width && pixel.y >= 0 && pixel.y < height)) {
			return false;
		}
		Color c = new Color(image.getRGB(pixel.x, pixel.y));
		LCHColour lch = new LCHColour(c);
		ColourClass cc = globalInfo.getColourSettings().getColourClass(lch);
		switch (cc) {
		case BLUE:
			if (!isYellow) {
				returnValue = true;
			}
			break;
		case YELLOW:
			if (isYellow) {
				returnValue = true;
			}
			break;
		}
		return returnValue;
	}

	/**
	 * For every ArrayList<Point> (robot/ball), calculate its centroid
	 * @param pixels robot/ball pixels
	 * @return Point centroid
	 */
	public Point calcCentroid(ArrayList<Point> fixels){
		if (fixels.size() == 0) {
			return null;
		}
		Point centroid = new Point(0,0);
		Point fixelsInArrayList = new Point(0,0);
		for (int i = 0; i < fixels.size(); i++){
			Point current = fixels.get(i);
			fixelsInArrayList.x += current.x;
			fixelsInArrayList.y += current.y;
		}
		if (!fixels.isEmpty()){
			centroid.x = fixelsInArrayList.x / fixels.size();
			centroid.y = fixelsInArrayList.y / fixels.size();}
		else {
			if (VERBOSE) {
				System.out.println("Robot's missing.");
			}
		}
		return centroid;
	}

	/**
	 * This function will change the values of p2. Use the returned point 
	 * and create a copy of p2 if you want to use it. This will also perform 
	 * very badly if continually used to rotate by 1 degrees.
	 * @return coordinates of the *rounded* and rotated point
	 */
	public Point rotatePoint(Point pivot, Point rotate, int deg) {
		Point point = new Point(rotate.x,rotate.y);
		point.x -= pivot.x;
		point.y -= pivot.y;
		double rad = Math.toRadians(deg);
		double cosAngle = Math.cos(rad);
		double sinAngle = Math.sin(rad); 
		int xtemp = (int) Math.round((point.x * cosAngle) - (point.y * sinAngle));
		point.y = (int) Math.round((point.x * sinAngle) + (point.y * cosAngle));
		point.x = xtemp;
		return new Point (point.x+pivot.x, point.y+pivot.y);
	}

	/**
	 * Converts from the image coordinate system (in pixels) to a coordinate system
	 * centred at the physical centre of the pitch (in cm), where y grows upwards and 
	 * x to the right.
	 * @param point The point to convert.
	 * @return The point, converted.
	 */
	private Point2D convertPixelsToCm(Point2D point) {
		Point2D p = new Point2D.Float();
		Rectangle2D pitchPhysicalRectangle = globalInfo.getPitch()
				.getMinimumEnclosingRectangle();
		Rectangle pitchImageRectangle = globalInfo.getCamera().getPitchCrop();

		double x = linearRemap(point.getX(), pitchImageRectangle.getMinX(),
				pitchImageRectangle.getWidth(),
				pitchPhysicalRectangle.getMinX(),
				pitchPhysicalRectangle.getWidth());
		double y = linearRemap(point.getY(), pitchImageRectangle.getMinY(),
				pitchImageRectangle.getHeight(),
				pitchPhysicalRectangle.getMinY(),
				pitchPhysicalRectangle.getHeight());
		p.setLocation(x, -y);

		return p;
	}

	/**
	 * Corrects the position of a robot. The converted position is our initial
	 * estimation of the position, assuming the plate lies on the pitch. There 
	 * is an error proportional to the distance of the robot from the centre
	 * of the pitch. This is corrected in this function. Here is a rough 
	 * sketch:
	 * Camera
	 *   .
	 *   |\
	 *   | \
	 *   |  \
	 * H |   \
	 *   |____\  <- robot centroid.
	 *   |   h|\ 
	 *   |____|_\
	 *       delta  
	 *      d
	 *      
	 * @param robotPosition The pixel position of the robot. 
	 * @param convertedPosition The converted position of the robot, before correction.
	 * @param height The height of the robot.
	 * @return The corrected position of the robot.
	 */
	private Point2D correctRobotPosition(Point robotPosition,
			Point2D convertedPosition, double height) {
		
		Rectangle pitchImageRectangle = 
			globalInfo.getCamera().getPitchCrop();
		
		// Correction
		double H = globalInfo.getCamera().getDistanceFromPitch();
		double h = height;
		double d = 
			robotPosition.distance(
					pitchImageRectangle.getCenterX(), 
					pitchImageRectangle.getCenterY()
			);
		double delta = d * h / H;
		double quotient = (d - delta) / d;
		Point2D result = 
			new Point2D.Float(
					(float) (convertedPosition.getX() * quotient),
					(float) (convertedPosition.getY() * quotient)
			);
		return result;
	}

	/**
	 * Changes the coordinate system of a 1-dimensional variable.
	 * E.g. if you want to move your starting point from 1 to 3, and to
	 * dilate your range from 3 to 6, this would move 2 to 5:
	 * 
	 *         1 2 3 4 5 6 7 8 9
	 * Input : > .   <
	 * Output:     >   .       <
	 * 
	 * There is a side-effect of 'flipping' the image if you give negative range
	 * to either of the range arguments:
	 * 
	 *         1 2 3 4 5 6 7 8 9
	 * Input : < .   >
	 * Output:     >       .   <
	 * 
	 * @param x
	 * @param x0
	 * @param domainRange
	 * @param y0
	 * @param targetRange
	 * @return
	 */
	private double linearRemap(double x, double x0, double domainRange,
			double y0, double targetRange) {
		return (x - x0) * (targetRange / domainRange) + y0;
	}

	// TODO: use a method for the check, see earlier todo-s.
	/**
	 * A safe way to draw a pixel
	 * 
	 * @param raster
	 *            draw on writable raster
	 * @param p1
	 *            point coordinates
	 * @param colour
	 *            colour
	 */
	private void drawPixel(WritableRaster raster, Point p1, int[] colour) {
		int width = 640;
		int height = 480;
		if (p1.x >= 0 && p1.x < width && p1.y >= 0 && p1.y < height)
			raster.setPixel(p1.x, p1.y, colour);
	}

	// TODO: fix comments, nice function
	/**
	 * This function is used to keep all drawing/printing in one place.
	 * 
	 * @param internalImage
	 */
	private void drawStuff(BufferedImage internalImage) {
		WritableRaster raster = internalImage.getRaster();
		if (blueCentroid != null) {
			drawLine_Robot_Facing(raster, this.blueCentroid, this.blueDir);
			drawCentroidCircle(raster, blueCentroid, new int[] { 0, 0, 255 },
					50);
		}
		if (yellowCentroid != null) {
			drawCentroidCircle(raster, yellowCentroid,
					new int[] { 255, 255, 0 }, 50);
			drawLine_Robot_Facing(raster, this.yellowCentroid, this.yellowDir);
		}
		if (ballCentroid != null) {
			drawCentroidCircle(raster, ballCentroid, new int[] { 255, 0, 0 },
					25);
		}
	}

	// TODO: simple, but add comments, should be disable-able :] (but not here).
	/**
	 * Simply used to draw a circle around the point centroid.
	 * 
	 * @param raster
	 * @param centroid
	 * @param colour
	 * @param radius
	 * @see #rotatePoint(Point, Point, int)
	 */
	private void drawCentroidCircle(WritableRaster raster, Point centroid,
			int[] colour, int radius) {
		Point rotPoint = new Point(centroid.x + radius, centroid.y);
		for (int i = 0; i < 360; i++) {
			Point tempPoint = new Point(centroid.x + radius, centroid.y);
			rotPoint = rotatePoint(centroid, tempPoint, i);
			drawPixel(raster, rotPoint, colour);
		}
	}

	/**
	 * Used to draw the facing direction of robots.
	 * 
	 * @param raster
	 * @param c
	 * @param angle
	 */
	private void drawRobotFacingDirection(WritableRaster raster, Point c,
			double angle) {
		
		angle = 360 - angle;
		double tanAngle = Math.tan(Math.toRadians(angle));
		int[] colour = {255, 255, 255};
		
		if (angle < 270 && angle > 90) {
			int counter = c.x - 100;
			double b = c.y - c.x * tanAngle;
			while (c.x > counter) {
				Point pointToDraw = new Point(c.x, (int)(b + c.x * tanAngle));
				drawPixel(raster, pointToDraw, colour);
				counter++;
			}
		} else {
			int counter = c.x + 100;
			double b = c.y - c.x * tanAngle;
			while (c.x < counter) {
				Point pointToDraw = new Point(c.x,(int)(b + c.x * tanAngle));
				drawPixel(raster, pointToDraw, colour);
				counter--;
			}
		}
	}

	/**
	 * Set the mode of output.
	 * 
	 * @param currentMode
	 *            The mode of output.
	 */
	public void setCurrentMode(OutputMode currentMode) {
		this.currentMode = currentMode;
	}

	/**
	 * Get the mode of output.
	 * 
	 * @return The mode of output.
	 */
	public OutputMode getCurrentMode() {
		return currentMode;
	}

	/**
	 * @return ball position
	 */
	protected Point2D extractBallPosition(BufferedImage image) {
		if (ballCentroid == null || ballCentroid.getLocation() == null) {
			return null;
		}
		return convertPixelsToCm(ballCentroid);
	}

	/**
	 * @return robot positions
	 */
	protected Point2D extractRobotPosition(BufferedImage image, boolean yellow) {
		Point robotPosition = yellow 
			? yellowCentroid
			: blueCentroid;
		if (robotPosition == null) {
			return null;
		}
		Point2D convertedPosition = convertPixelsToCm(robotPosition);
		return 
			correctRobotPosition(
					robotPosition, 
					convertedPosition, 
					StaticRobotInfo.getHeight()
			);
	}

	/**
	 * @return angle of the robot facing direction
	 */
	protected double extractRobotFacingDirection(BufferedImage image,
			boolean yellow) {
		if (yellow) {
			return yellowDir;
		} else {
			return blueDir;
		}
	}
}

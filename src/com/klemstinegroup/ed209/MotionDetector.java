package com.klemstinegroup.ed209;

// JCVMotionDetector.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, May 2011

/* Motion detections with JavaCV (http://code.google.com/p/javacv/).
 Compare the current image with the previous one to find the differences,
 then calculate the center-of-gravity (COG) of the difference image.

 Based on my CVMotionDetector class, but the constructor and calcMove()
 now take BufferedImage inputs, and smoothing is used to calculate the
 returned COG point.
 */

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvAbsDiff;
import static com.googlecode.javacv.cpp.opencv_core.cvCountNonZero;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BLUR;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetSpatialMoment;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMoments;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvMoments;

public class MotionDetector extends Camera {
	private static final int MIN_PIXELS = 50;
	// minimum number of non-black pixels needed for COG calculation
	private static final int LOW_THRESHOLD = 32;

	private static final int MAX_PTS = 5;

	private IplImage prevImg, currImg, diffImg; // grayscale images (diffImg is
												// bi-level)
	private Dimension imDim = null; // image dimensions

	private Point[] cogPoints; // array for smoothing COG points
	private int ptIdx, totalPts;

	int lastX, lastY;

	boolean firing=false;
	public MotionDetector(int camera) {
		super(camera);

		// prevImg = convertFrame(new BufferedImage(width,height,
		// BufferedImage.TYPE_INT_RGB));
		try {
			prevImg = grabber.grab();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int width = prevImg.width();
		int height = prevImg.height();
		System.out.println(width + "," + height);

		imDim = new Dimension(width, height);
		//

		cogPoints = new Point[MAX_PTS];
		ptIdx = 0;
		totalPts = 0;

		prevImg = convertFrame(new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB));
		currImg = null;

		diffImg = IplImage.create(prevImg.width(), prevImg.height(),
				IPL_DEPTH_8U, 1);

	} // end of JCVMotionDetector()

	public void calcMove(IplImage currFrame)
	// use a new image to create a new COG point
	{
		if (currFrame == null) {

			return;
		}

		if (currImg != null) // store old current as the previous image
			prevImg = currImg;

		currImg = currFrame;

		cvAbsDiff(currImg, prevImg, diffImg);
		// calculate absolute difference between curr & previous images;
		// large value means movement; small value means no movement

		/*
		 * threshold to convert grayscale --> two-level binary: small diffs (0
		 * -- LOW_THRESHOLD) --> 0 large diffs (LOW_THRESHOLD+1 -- 255) --> 255
		 */
		cvThreshold(diffImg, diffImg, LOW_THRESHOLD, 255, CV_THRESH_BINARY);

		Point cogPoint = findCOG(diffImg);
		if (cogPoint != null) { // store in points array
			cogPoints[ptIdx] = cogPoint;
			ptIdx = (ptIdx + 1) % MAX_PTS; // the index cycles around the array
			if (totalPts < MAX_PTS)
				totalPts++;
		}
	} // end of calcMove()

	public IplImage getCurrImg() {
		return currImg;
	}

	public IplImage getDiffImg() {
		return diffImg;
	}

	public Dimension getSize() {
		return imDim;
	}

	private IplImage convertFrame(BufferedImage buffIm)
	/*
	 * Conversion involves: changing the BufferedImage into an IplImage object,
	 * blurring, converting color to grayscale, and equalization
	 */
	{
		BufferedImage indexedImage = new BufferedImage(buffIm.getWidth(),
				buffIm.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D g = indexedImage.createGraphics();
		g.drawImage(buffIm, 0, 0, null);
		IplImage img = IplImage.createFrom(indexedImage);

		// blur image to get reduce camera noise
		cvSmooth(img, img, CV_BLUR, 30);

		return img;
	} // end of convertFrame()

	private Point findCOG(IplImage diffImg)
	/*
	 * If there are enough non-black pixels in the difference image (non-black
	 * means a difference, i.e. movement), then calculate the moments, and use
	 * them to calculate the (x,y) center of the white areas. These values are
	 * returned as a Point object.
	 */
	{
		Point pt = null;

		int numPixels = cvCountNonZero(diffImg); // non-zero (non-black) means
													// motion
		//
		if (numPixels > MIN_PIXELS) {
			CvMoments moments = new CvMoments();
			cvMoments(diffImg, moments, 1); // 1 == treat image as binary
											// (0,255) --> (0,1)
			double m00 = cvGetSpatialMoment(moments, 0, 0);
			double m10 = cvGetSpatialMoment(moments, 1, 0);
			double m01 = cvGetSpatialMoment(moments, 0, 1);

			if (m00 != 0) { // create COG Point
				int xCenter = (int) Math.round(m10 / m00);
				int yCenter = (int) Math.round(m01 / m00);
				//
				pt = new Point(xCenter, yCenter);
			}
		}
		return pt;
	} // end of findCOG()

	public Point getCOG()
	/*
	 * return average of points stored in cogPoints[], to smooth the position
	 */
	{
		if (totalPts == 0)
			return null;

		int xTot = 0;
		int yTot = 0;
		for (int i = 0; i < totalPts; i++) {
			xTot += cogPoints[i].x;
			yTot += cogPoints[i].y;
		}

		return new Point((int) (xTot / totalPts), (int) (yTot / totalPts));
	} // end of getCOG()

	public static IplImage convertToGrayscale(IplImage image) {
		// BufferedImageOp op = new ColorConvertOp(
		// ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		// return op.filter(image, null);
		// cvCvtColor( image, image, CV_RGB2GRAY );
		// convert to grayscale
		// System.out.println(img+" "+img.width()+" "+img.height());
		IplImage grayImg = IplImage.create(image.width(), image.height(),
				IPL_DEPTH_8U, 1);
		cvCvtColor(image, grayImg,
				com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY);
		//
		// cvEqualizeHist(grayImg, grayImg); // spread out the grayscale range

		return grayImg;
	}

	@Override
	public BufferedImage processImage(IplImage image) {
		if (image == null)
			return null;
		calcMove(convertToGrayscale(image));
		Point p = getCOG();
		BufferedImage out = image.getBufferedImage();
		Graphics g = out.getGraphics();
		if (g != null) {
			// g.setColor(Color.red);
			// for (Point p1 : cogPoints) {
			// if (p1 != null) {
			// g.fillOval(p1.x - 3, p1.y - 3, 7, 7);
			// }
			// }
			g.setColor(Color.yellow);
			g.drawLine(p.x, p.y, lastX, lastY);

			g.setColor(Color.red);
			g.fillOval(p.x - 3, p.y - 3, 7, 7);

			g.setColor(Color.green);
			g.fillOval(p.x + (p.x - lastX) - 3, p.y + (p.y - lastY) - 3, 7, 7);
		}
		double dist = Math.sqrt((p.x - lastX) * (p.x - lastX) + (p.y - lastY)
				* (p.y - lastY));
		// System.out.println(dist);
		if (dist > 15)
			fire();
		lastX = p.x;
		lastY = p.y;

		if (Main.slider_x != null && !Main.manualAim)
			Main.slider_x.setValue((p.x * 100) / out.getWidth());
		if (Main.slider_y != null && !Main.manualAim)
			Main.slider_y.setValue(100 - (p.y * 100) / out.getHeight());

		return out;

	}

	public synchronized void fire() {
		if (Main.fireDisabled)return;
		if (firing)return;
		firing=true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (Main.serialOn)Main.serial.add(2, 20);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (Main.serialOn)Main.serial.add(2, 50);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				firing=false;
			}

		}).start();
	}

} // end of JCVMotionDetector class

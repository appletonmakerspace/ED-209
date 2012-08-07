package com.klemstinegroup.ed209;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.VideoInputFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class Camera extends Canvas implements Runnable {

	FrameGrabber grabber;
	IplImage image = null;
	Thread runner;
	boolean running = false;

	public Camera(){
		this(0);
	}
	
	public Camera(int b) {
		grabber = new VideoInputFrameGrabber(b);
		grabber.setImageWidth(640);
		grabber.setImageHeight(480);
		try {
			grabber.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void start() {
		running = true;
		runner = new Thread(this);
		runner.start();
	}

	public void stop() {
		running = false;
	}

	@Override
	public void run() {
		while (running) {

			try {
				image = grabber.grab();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			BufferedImage newimage=processImage(image);
			if (newimage != null) {
				Graphics g = this.getGraphics();
				if (g!=null)
				g.drawImage(newimage, 0, 0, this.getWidth(), this.getHeight(), null);
			}
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

		}
	}

	public BufferedImage processImage(IplImage image) {
		return image.getBufferedImage();
	}

}

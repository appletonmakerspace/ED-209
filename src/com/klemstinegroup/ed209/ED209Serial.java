package com.klemstinegroup.ed209;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.serial.Serial;

public class ED209Serial extends PApplet implements Runnable {
	Serial serial;
	int port = 0;
	Thread runner = new Thread(this);
	ArrayList<Movement> move = new ArrayList<Movement>();

	public ED209Serial() {
		this.init();
		for (String bb : Serial.list()) {
			System.out.println(bb);
		}
		serial = new Serial(this, Serial.list()[port], 2400);
		serial.bufferUntil(10);
		System.out.println("Started on port " + Serial.list()[port]);
		add(0, 50);
		add(1, 50);
		add(1, 100);
	}

	public void serialEvent(Serial p) {
		System.out.println(p.readString());
	}

	public void run() {
		while (true) {
			if (move.size() > 1) {
				moveServo(move.get(0).servo, move.get(0).move);
				move.remove(0);
			}
		}
	}

	public void add(int servo, int percent) {
		move.add(new Movement(servo, percent));
	}

	public void moveServo(int servonum, double percent) {
		int pos = (int) (percent * 50 + 500);
		byte[] out = new byte[6];
		out[0] = (byte) 0x80;
		out[1] = (byte) 0x01;
		out[2] = (byte) 0x04;
		out[3] = (byte) servonum;
		out[4] = (byte) (pos / 128);
		out[5] = (byte) (pos % 128);
		// System.out.println(percent + " " + pos + " " + out[4] + " " +
		// out[5]);
		serial.write(out);
	}
}
package com.klemstinegroup.ed209;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Main extends JFrame {

	public static ED209Serial serial;
	public static JSlider slider_x;
	public static JSlider slider_y;
	static boolean manualAim=false;
	static boolean serialOn=true;
	public static boolean fireDisabled=true;
	int minx = 0;
	int maxx = 100;
	int miny = 0;
	int maxy = 100;

	public Main() {

		if (serialOn)serial=new ED209Serial();
		
		final MotionDetector canvas1 = new MotionDetector(0);
		canvas1.setSize(640, 480);
		canvas1.start();
		getContentPane().add(canvas1, BorderLayout.CENTER);

		
		//Panel setup
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		slider_x = new JSlider();
		panel.add(slider_x, BorderLayout.NORTH);
		slider_x.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int fps = (int) source.getValue();
					int a=minx;
					int b=maxx;
					if (minx>maxx){
						a=maxx;
						b=minx;
					}
					fps=(((b-a)*fps)/100)+a;
					if (serialOn)serial.add(0, fps);
				}
			}
		});

		JSlider slider_minx = new JSlider();
		slider_minx.setValue(0);
		panel.add(slider_minx, BorderLayout.CENTER);
		slider_minx.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					minx=(int) source.getValue();
				}
			}
		});

		JSlider slider_maxx = new JSlider();
		slider_maxx.setValue(100);
		panel.add(slider_maxx, BorderLayout.SOUTH);
		slider_maxx.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					maxx=(int) source.getValue();
				}
			}
		});

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new BorderLayout(0, 0));

		slider_y = new JSlider();
		panel_1.add(slider_y, BorderLayout.WEST);
		slider_y.setOrientation(SwingConstants.VERTICAL);
		slider_y.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int fps = (int) source.getValue();
					int a=miny;
					int b=maxy;
					if (miny>maxy){
						a=maxy;
						b=miny;
					}
					fps=(((b-a)*fps)/100)+a;
					if (serialOn)serial.add(1, 100-fps);
				}
			}
		});

		JSlider slider_maxy = new JSlider();
		slider_maxy.setValue(100);
		slider_maxy.setOrientation(SwingConstants.VERTICAL);
		panel_1.add(slider_maxy, BorderLayout.EAST);
		slider_maxy.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					maxy=(int) source.getValue();
				}
			}
		});

		JSlider slider_miny = new JSlider();
		slider_miny.setValue(0);
		slider_miny.setOrientation(SwingConstants.VERTICAL);
		panel_1.add(slider_miny, BorderLayout.CENTER);
		
		JPanel panel_2 = new JPanel();
		getContentPane().add(panel_2, BorderLayout.NORTH);
		
		final JCheckBox chckbxNewCheckBox = new JCheckBox("Manual Aim");
		chckbxNewCheckBox.addChangeListener(new ChangeListener() {
			 public void stateChanged(ChangeEvent arg0) {
				 manualAim=chckbxNewCheckBox.isSelected();
			}
		});
		panel_2.add(chckbxNewCheckBox);
		
		JButton btnNewButton = new JButton("Fire!");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("fire");
				canvas1.fire();
			}
		});
		panel_2.add(btnNewButton);
		slider_miny.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					miny=(int) source.getValue();
				}
			}
		});
		
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {
		new Main();
	}

	// cvSaveImage((i++) + "-capture.jpg", img);

}

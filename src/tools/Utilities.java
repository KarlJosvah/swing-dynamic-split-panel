package tools;

import java.util.Random;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;

public class Utilities {
	public static Dimension getScreenResolution() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}
	
	public static int randInt(int min, int max) {
		return new Random().nextInt(max - min + 1) + min;
	}

	public static Color randColor() {
		Random rand = new Random();
		int r = Utilities.randInt(0, 255);
		int g = Utilities.randInt(0, 255);
		int b = Utilities.randInt(0, 255);

		return new Color(r, g, b);
	}

	public static void replaceComponent(java.awt.Container parent, java.awt.Component oldComp, java.awt.Component newComp, boolean wasLeftInParentSplit) {
		if (parent instanceof javax.swing.JSplitPane) {
			javax.swing.JSplitPane split = (javax.swing.JSplitPane) parent;
			if (wasLeftInParentSplit) {
				split.setLeftComponent(newComp);
			} else {
				split.setRightComponent(newComp);
			}
		} else if (parent != null) {
			parent.remove(oldComp);
			parent.add(newComp, java.awt.BorderLayout.CENTER);
		}
	}
}
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

	public static void setupDivider(javax.swing.JSplitPane splitPane) {
		java.awt.Component divider = null;
		if (splitPane.getUI() instanceof javax.swing.plaf.basic.BasicSplitPaneUI) {
			divider = ((javax.swing.plaf.basic.BasicSplitPaneUI) splitPane.getUI()).getDivider();
		}
		if (divider == null) {
			for (java.awt.Component c : splitPane.getComponents()) {
				if (c instanceof javax.swing.plaf.basic.BasicSplitPaneDivider || c.getClass().getName().contains("Divider")) {
					divider = c;
					break;
				}
			}
		}

		if (divider != null) {
			int orientation = splitPane.getOrientation();
			if (orientation == javax.swing.JSplitPane.HORIZONTAL_SPLIT) {
				divider.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.E_RESIZE_CURSOR));
			} else {
				divider.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.N_RESIZE_CURSOR));
			}

			divider.addMouseListener(new java.awt.event.MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger() || javax.swing.SwingUtilities.isRightMouseButton(e)) {
						showDividerMenu(e, splitPane);
					}
				}

				@Override
				public void mouseReleased(java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger() || javax.swing.SwingUtilities.isRightMouseButton(e)) {
						showDividerMenu(e, splitPane);
					}
				}
			});
		}
	}

	private static void showDividerMenu(java.awt.event.MouseEvent e, javax.swing.JSplitPane splitPane) {
		javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
		int orientation = splitPane.getOrientation();

		if (orientation == javax.swing.JSplitPane.HORIZONTAL_SPLIT) {
			javax.swing.JMenuItem joinLeft = new javax.swing.JMenuItem("Join Left");
			joinLeft.addActionListener(al -> joinSplit(splitPane, false));
			javax.swing.JMenuItem joinRight = new javax.swing.JMenuItem("Join Right");
			joinRight.addActionListener(al -> joinSplit(splitPane, true));

			menu.add(joinLeft);
			menu.add(joinRight);
		} else {
			javax.swing.JMenuItem joinUp = new javax.swing.JMenuItem("Join Up");
			joinUp.addActionListener(al -> joinSplit(splitPane, false));
			javax.swing.JMenuItem joinDown = new javax.swing.JMenuItem("Join Down");
			joinDown.addActionListener(al -> joinSplit(splitPane, true));

			menu.add(joinUp);
			menu.add(joinDown);
		}

		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	public static void joinSplit(javax.swing.JSplitPane splitPane, boolean keepFirstChild) {
		java.awt.Container parent = splitPane.getParent();
		if (parent == null) return;

		java.awt.Component keptChild = keepFirstChild ? splitPane.getLeftComponent() : splitPane.getRightComponent();
		if (keptChild == null) return;

		boolean wasLeftInParentSplit = false;
		if (parent instanceof javax.swing.JSplitPane) {
			javax.swing.JSplitPane parentSplit = (javax.swing.JSplitPane) parent;
			wasLeftInParentSplit = (parentSplit.getLeftComponent() == splitPane);
		}

		replaceComponent(parent, splitPane, keptChild, wasLeftInParentSplit);

		java.awt.Container root = parent;
		while (root.getParent() != null) {
			root = root.getParent();
		}
		root.revalidate();
		root.repaint();
	}
}
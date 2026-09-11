package root;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import tools.Utilities;

public class DefaultPanel extends JPanel {

	private static final int CORNER_SIZE = 16;
	private static final int MIN_DRAG_THRESHOLD = 15;

	public enum Corner {
		NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
	}

	private Corner hoveredCorner = Corner.NONE;
	private Corner activeCorner = Corner.NONE;
	private Point dragStartPoint = null;
	private Point currentDragPoint = null;
	private boolean isDragging = false;

	public DefaultPanel() {
		this.setBackground(Utilities.randColor());
		initMouseListeners();
	}

	private void initMouseListeners() {
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Corner corner = getCornerAt(e.getPoint());
				if (corner != hoveredCorner) {
					hoveredCorner = corner;
					if (hoveredCorner != Corner.NONE) {
						setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					} else {
						setCursor(Cursor.getDefaultCursor());
					}
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if (!isDragging) {
					hoveredCorner = Corner.NONE;
					setCursor(Cursor.getDefaultCursor());
					repaint();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				Corner corner = getCornerAt(e.getPoint());
				if (corner != Corner.NONE) {
					activeCorner = corner;
					dragStartPoint = e.getPoint();
					currentDragPoint = e.getPoint();
					isDragging = true;
					repaint();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (isDragging) {
					currentDragPoint = e.getPoint();
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (isDragging) {
					currentDragPoint = e.getPoint();
					int dx = currentDragPoint.x - dragStartPoint.x;
					int dy = currentDragPoint.y - dragStartPoint.y;
					double dist = Math.hypot(dx, dy);

					if (dist >= MIN_DRAG_THRESHOLD) {
						int w = getWidth();
						int h = getHeight();
						int orientation;
						boolean newPanelFirst;
						double splitRatio;

						if (Math.abs(dx) > Math.abs(dy)) {
							// Horizontal split (vertical line divides left & right)
							orientation = JSplitPane.HORIZONTAL_SPLIT;
							newPanelFirst = (dx < 0);
							int splitX = Math.max(10, Math.min(w - 10, currentDragPoint.x));
							splitRatio = (double) splitX / (w > 0 ? w : 1);
						} else {
							// Vertical split (horizontal line divides top & bottom)
							orientation = JSplitPane.VERTICAL_SPLIT;
							newPanelFirst = (dy < 0);
							int splitY = Math.max(10, Math.min(h - 10, currentDragPoint.y));
							splitRatio = (double) splitY / (h > 0 ? h : 1);
						}

						splitRatio = Math.max(0.05, Math.min(0.95, splitRatio));
						performSplit(orientation, newPanelFirst, splitRatio);
					}

					isDragging = false;
					activeCorner = Corner.NONE;
					hoveredCorner = getCornerAt(e.getPoint());
					if (hoveredCorner == Corner.NONE) {
						setCursor(Cursor.getDefaultCursor());
					}
					repaint();
				}
			}
		};

		addMouseListener(adapter);
		addMouseMotionListener(adapter);
	}

	private Corner getCornerAt(Point p) {
		int w = getWidth();
		int h = getHeight();
		int cs = Math.min(CORNER_SIZE, Math.min(w / 3, h / 3));
		if (cs <= 0) return Corner.NONE;

		if (p.x <= cs && p.y <= cs) return Corner.TOP_LEFT;
		if (p.x >= w - cs && p.y <= cs) return Corner.TOP_RIGHT;
		if (p.x <= cs && p.y >= h - cs) return Corner.BOTTOM_LEFT;
		if (p.x >= w - cs && p.y >= h - cs) return Corner.BOTTOM_RIGHT;

		return Corner.NONE;
	}

	public void performSplit(int orientation, boolean newPanelFirst, double splitRatio) {
		Container parent = getParent();
		if (parent == null) return;

		boolean wasLeftInParentSplit = false;
		if (parent instanceof JSplitPane) {
			JSplitPane parentSplit = (JSplitPane) parent;
			wasLeftInParentSplit = (parentSplit.getLeftComponent() == this);
		}

		DefaultPanel newPanel = new DefaultPanel();

		JSplitPane splitPane = new JSplitPane(orientation);
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(splitRatio);
		splitPane.setDividerSize(5);
		splitPane.setBorder(null);

		if (newPanelFirst) {
			splitPane.setLeftComponent(newPanel);
			splitPane.setRightComponent(this);
		} else {
			splitPane.setLeftComponent(this);
			splitPane.setRightComponent(newPanel);
		}

		Utilities.replaceComponent(parent, this, splitPane, wasLeftInParentSplit);

		Container root = parent;
		while (root.getParent() != null) {
			root = root.getParent();
		}
		root.revalidate();
		root.repaint();

		SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(splitRatio));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();

		// Draw corner handles
		drawCornerGrip(g2, Corner.TOP_LEFT, 0, 0, w, h, hoveredCorner == Corner.TOP_LEFT || activeCorner == Corner.TOP_LEFT);
		drawCornerGrip(g2, Corner.TOP_RIGHT, 0, 0, w, h, hoveredCorner == Corner.TOP_RIGHT || activeCorner == Corner.TOP_RIGHT);
		drawCornerGrip(g2, Corner.BOTTOM_LEFT, 0, 0, w, h, hoveredCorner == Corner.BOTTOM_LEFT || activeCorner == Corner.BOTTOM_LEFT);
		drawCornerGrip(g2, Corner.BOTTOM_RIGHT, 0, 0, w, h, hoveredCorner == Corner.BOTTOM_RIGHT || activeCorner == Corner.BOTTOM_RIGHT);

		// Draw drag preview overlay line & highlighted region
		if (isDragging && dragStartPoint != null && currentDragPoint != null) {
			int dx = currentDragPoint.x - dragStartPoint.x;
			int dy = currentDragPoint.y - dragStartPoint.y;
			double dist = Math.hypot(dx, dy);

			if (dist >= MIN_DRAG_THRESHOLD) {
				if (Math.abs(dx) > Math.abs(dy)) {
					// Horizontal split (vertical preview line)
					int splitX = Math.max(10, Math.min(w - 10, currentDragPoint.x));

					// Highlight preview region for new panel
					g2.setColor(new Color(255, 255, 255, 40));
					if (dx < 0) {
						g2.fillRect(0, 0, splitX, h);
					} else {
						g2.fillRect(splitX, 0, w - splitX, h);
					}

					// Split line
					g2.setColor(new Color(255, 255, 255, 200));
					g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{6.0f, 4.0f}, 0.0f));
					g2.drawLine(splitX, 0, splitX, h);
				} else {
					// Vertical split (horizontal preview line)
					int splitY = Math.max(10, Math.min(h - 10, currentDragPoint.y));

					// Highlight preview region for new panel
					g2.setColor(new Color(255, 255, 255, 40));
					if (dy < 0) {
						g2.fillRect(0, 0, w, splitY);
					} else {
						g2.fillRect(0, splitY, w, h - splitY);
					}

					// Split line
					g2.setColor(new Color(255, 255, 255, 200));
					g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{6.0f, 4.0f}, 0.0f));
					g2.drawLine(0, splitY, w, splitY);
				}
			}
		}

		g2.dispose();
	}

	private void drawCornerGrip(Graphics2D g2, Corner corner, int x, int y, int w, int h, boolean isHovered) {
		g2.setColor(isHovered ? new Color(255, 255, 255, 240) : new Color(220, 220, 220, 110));
		g2.setStroke(new BasicStroke(isHovered ? 1.5f : 1.0f));

		int[] offsets = {4, 8, 12};

		for (int offset : offsets) {
			switch (corner) {
				case TOP_LEFT:
					g2.drawLine(0, offset, offset, 0);
					break;
				case TOP_RIGHT:
					g2.drawLine(w - offset, 0, w, offset);
					break;
				case BOTTOM_LEFT:
					g2.drawLine(0, h - offset, offset, h);
					break;
				case BOTTOM_RIGHT:
					g2.drawLine(w - offset, h, w, h - offset);
					break;
				default:
					break;
			}
		}

		if (isHovered) {
			g2.setColor(new Color(255, 255, 255, 35));
			int cs = CORNER_SIZE;
			switch (corner) {
				case TOP_LEFT:
					g2.fillArc(-cs, -cs, cs * 2, cs * 2, 270, 90);
					break;
				case TOP_RIGHT:
					g2.fillArc(w - cs, -cs, cs * 2, cs * 2, 180, 90);
					break;
				case BOTTOM_LEFT:
					g2.fillArc(-cs, h - cs, cs * 2, cs * 2, 0, 90);
					break;
				case BOTTOM_RIGHT:
					g2.fillArc(w - cs, h - cs, cs * 2, cs * 2, 90, 90);
					break;
				default:
					break;
			}
		}
	}
}
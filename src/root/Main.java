package root;

import com.formdev.flatlaf.FlatDarkLaf;

import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import tools.Utilities;

public class Main extends JFrame {
	private static final String TITLE = "Dynamic split in java swing";
	private static final int WIDTH = (int) Utilities.getScreenResolution().getWidth();
	private static final int HEIGHT = (int) Utilities.getScreenResolution().getHeight();
	private static final int MIN_WIDTH = 800;
	private static final int MIN_HEIGHT = 600;

	public static void main(String[] args) {
		FlatDarkLaf.setup();
		javax.swing.UIManager.put("SplitPane.dividerSize", 5);
		javax.swing.UIManager.put("SplitPane.border", null);
		SwingUtilities.invokeLater( () -> new Main() );
	}

	public Main() {
		this.init();
		this.config();
		this.setVisible(true);
	}

	private void init() {
		this.setTitle(Main.TITLE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(Main.WIDTH, Main.HEIGHT);
		this.setMinimumSize(new Dimension(Main.MIN_WIDTH, Main.MIN_HEIGHT));
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
	}

	private void config() {
		this.setContentPane(new NestedSplitedPanel(new DefaultPanel()));
	}
}
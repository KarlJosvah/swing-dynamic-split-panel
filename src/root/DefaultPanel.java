package root;

import javax.swing.JPanel;
import tools.Utilities;

public class DefaultPanel extends JPanel {

	public DefaultPanel() {
		this.setBackground(Utilities.randColor());
	}
}
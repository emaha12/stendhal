package games.stendhal.client.gui.styled;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;

/**
 * PanelUI implementation for drawing in pixmap styles.
 */
public class StyledPanelUI extends PanelUI {
	private static StyledPanelUI instance;
	
	private final Style style;
	
	// Required by UIManager
	public static ComponentUI createUI(JComponent panel) {
		// Panel UI instances can be shared
		if (instance == null) {
			instance = new StyledPanelUI(StyleUtil.getStyle());
		}
		
		return instance;
	}
	
	/**
	 * Create a new pixmap style.
	 * 
	 * @param style {@link Style} to be used for drawing the panel
	 */
	public StyledPanelUI(Style style) {
		this.style = style;
	}
	
	@Override
	public void paint(Graphics graphics, JComponent panel) {
		StyleUtil.fillBackground(style, graphics, 0, 0, panel.getWidth(), panel.getHeight());
	}
	
	@Override
	public void installUI(JComponent panel) {
		super.installUI(panel);
		panel.setForeground(style.getForeground());
		panel.setBorder(style.getBorder());
	}
}

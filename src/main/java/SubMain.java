import gui.DimensionViewer;
import gui.DxfViewer;

import javax.swing.UIManager;

/**
 * 
 */

/**
 * @author fujiwara
 * 
 */
public class SubMain {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		DxfViewer.debug = true;
		DimensionViewer viewer = new DimensionViewer();
		if (args.length > 0) {
			StringBuilder sb = new StringBuilder(args[0]);
			for (int i = 1; i < args.length; i++) {
				sb.append(' ');
				sb.append(args[i]);
			}
			viewer.openFile(sb.toString());
		}
	}
}

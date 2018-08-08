import gui.DxfViewer;

import javax.swing.UIManager;

/**
 * 起動のためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class Main {

	/**
	 * テストのためのメインメソッド
	 * 
	 * @param args
	 *            ファイルの場所（スペースを含んでもよい）
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		DxfViewer viewer = new DxfViewer();
		if (args.length > 0) {
			StringBuilder sb = new StringBuilder(args[0]);
			for (int i = 1; i < args.length; i++) {
				sb.append(' ');
				sb.append(args[i]);
			}
			viewer.openDirectory(sb.toString());
		}
	}
}

package util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.JComponent;

/**
 * エクスポート可能なパネルクラス
 * 
 * @author ma38su
 */
public abstract class ExportableComponent extends JComponent implements Printable {

	@Override
	protected void paintComponent(Graphics g) {
		Image image = this.createImage(this.getWidth(), this.getHeight());
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		this.paintComponent(g2);
		g.drawImage(image, 0, 0, null);
	}

	/**
	 * 描画を行うメソッド
	 * 
	 * @param g
	 * @param width
	 * @param height
	 */
	protected abstract void paintComponent(Graphics2D g);

	/**
	 * Printable インターフェースの実装
	 * 
	 * @author KUMANO Tatsuo
	 * @param graphics
	 * @param pageFormat
	 * @param pageIndex
	 * @return 状態
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
		if (pageIndex == 0) {
			final Graphics2D g = (Graphics2D) graphics;
			GraphicsConfiguration config = g.getDeviceConfiguration();
			AffineTransform matrix = config.getDefaultTransform();
			int width = (int) (pageFormat.getImageableWidth() * matrix.getScaleX());
			int height = (int) (pageFormat.getImageableHeight() * matrix.getScaleY());
			int x = (int) (pageFormat.getImageableX() * matrix.getScaleX());
			int y = (int) ((pageFormat.getHeight() - pageFormat.getImageableY() - pageFormat.getImageableHeight()) * matrix.getScaleY());
			this.printComponent(g, x, y, width, height);
			return Printable.PAGE_EXISTS;
		} else {
			return Printable.NO_SUCH_PAGE;
		}
	}

	protected abstract void printComponent(Graphics2D g, int x, int y, int width, int height);
}

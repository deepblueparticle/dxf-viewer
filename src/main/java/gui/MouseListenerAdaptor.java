package gui;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * 各種リスナーをコンポーネントのメソッドに適応させるためのクラス
 * 
 * @author FUJIWARA Masayasu
 */
public class MouseListenerAdaptor implements MouseListener, MouseMotionListener, MouseWheelListener {

	/**
	 * 倍率
	 */
	/**
	 * 倍率の変化の感度
	 */
	private final static double SCALE_SENSE = 0.1;

	/**
	 * 操作されているマウスのボタン
	 */
	private int button;

	/**
	 * 表示環境を操作するパネル 座標系の問題によってコンポーネントの高さが必要になる。
	 */
	private final DxfComponent comp;

	/**
	 * 移動の基準となるX座標
	 */
	private int x0;

	/**
	 * 移動の基準となるY座標
	 */
	private int y0;

	public MouseListenerAdaptor(DxfComponent comp) {
		this.comp = comp;
		this.comp.addMouseListener(this);
		this.comp.addMouseMotionListener(this);
		this.comp.addMouseWheelListener(this);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (this.button == MouseEvent.BUTTON3) {
			this.comp.updateSelection(x, y);
		} else {
			int dx = x - this.x0;
			int dy = y - this.y0;
			this.x0 = x;
			this.y0 = y;
			this.comp.updatePosition(dx, dy);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		this.button = e.getButton();
		this.x0 = e.getX();
		this.y0 = e.getY();
		if (this.button == MouseEvent.BUTTON3) {
			this.comp.selectionMake(this.x0, this.y0);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (this.button == MouseEvent.BUTTON3) {
			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {
				this.comp.fixSelection();
			} else {
				this.comp.removeSelection();
			}
		}
		this.x0 = -1;
		this.y0 = -1;
		this.button = MouseEvent.NOBUTTON;
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		this.comp.updateScale(1 + e.getWheelRotation() * SCALE_SENSE, e.getX(), e.getY());
	}
}

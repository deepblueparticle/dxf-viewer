/**
 * 
 */
package gui;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Observable;

import dxf.section.entities.DxfEntity;

/**
 * 選択範囲を扱うクラス
 * 
 * @author FUJIWARA Masayasu
 */
public class MouseSelection extends Observable {

	/**
	 * 選択範囲
	 */
	private Rectangle2D.Double selection;

	/**
	 * 基準となるX座標
	 */
	private double x0;

	/**
	 * 基準となるY座標
	 */
	private double y0;

	/**
	 * 選択範囲を削除します。
	 */
	public void clear() {
		this.selection = null;
		this.setChanged();
		this.notifyObservers();
	}

	public boolean contains(DxfEntity entity) {
		return entity.isContained(this.selection);
	}

	public void draw(Graphics2D g) {
		if (this.selection != null) {
			g.draw(this.selection);
		}
	}

	public Rectangle2D getRectangle2D() {
		return this.selection;
	}

	/**
	 * 選択範囲との交差を判定するメソッド
	 * 
	 * @param rect
	 *            交差を判定する領域
	 * @return 交差していればtrue、交差していなければfalseを返す。
	 */
	public boolean intersects(Rectangle2D rect) {
		return this.selection.intersects(rect);
	}

	/**
	 * 選択範囲との交差を判定するメソッド
	 * 
	 * @param bound
	 *            交差を判定する領域
	 * @return 交差していればtrue、交差していなければfalseを返す。
	 */
	public boolean intersects(Shape bound) {
		return bound.intersects(this.selection);
	}

	/**
	 * 選択されているかどうか確認するメソッド
	 * 
	 * @return 選択されていればtrue、されていなければfalse
	 */
	public boolean isSelected() {
		return this.selection != null;
	}

	/**
	 * 選択範囲を作成するメソッド
	 * 
	 * @param x0
	 *            基準となるX座標
	 * @param y0
	 *            基準となるY座標
	 */
	public void make(double x0, double y0) {
		this.x0 = x0;
		this.y0 = y0;
		this.selection = new Rectangle2D.Double(x0, y0, 0, 0);
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * 選択範囲を更新するメソッド
	 * 
	 * @param x1
	 *            選択範囲の端点
	 * @param y1
	 *            選択範囲の端点
	 */
	public void update(double x1, double y1) {
		if (this.selection == null) {
			make(x1, y1);
		}
		if (this.x0 > x1) {
			this.selection.x = x1;
			this.selection.width = this.x0 - x1;
		} else {
			this.selection.x = this.x0;
			this.selection.width = x1 - this.x0;
		}
		if (this.y0 > y1) {
			this.selection.y = y1;
			this.selection.height = this.y0 - y1;
		} else {
			this.selection.y = this.y0;
			this.selection.height = y1 - this.y0;
		}
		this.setChanged();
		this.notifyObservers();
	}
}

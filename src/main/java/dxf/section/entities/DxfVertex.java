/**
 * 
 */
package dxf.section.entities;

import gui.ViewingEnvironment;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * @author FUJIWARA Masayasu
 */
public class DxfVertex implements DxfEntity {

	private double x;
	private double y;

	/**
	 * @param x
	 *            X座標
	 * @param y
	 *            Y座標
	 */
	public DxfVertex(Number x, Number y) {
		this.x = x.doubleValue();
		this.y = y.doubleValue();
	}

	/**
	 * @see dxf.section.entities.DxfEntity#clearError()
	 */

	public void clearError() {
	}

	/**
	 * @see dxf.section.entities.DxfEntity#draw(java.awt.Graphics2D,
	 *      gui.ViewingEnvironment)
	 */

	public void draw(Graphics2D g, ViewingEnvironment env) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 */

	public Rectangle2D getBounds2D() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection,
	 *      java.util.Collection)
	 */

	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#intersects(java.awt.geom.Rectangle2D)
	 */

	public boolean intersects(Rectangle2D rect) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isContained(java.awt.Shape)
	 */

	public boolean isContained(Shape shape) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isSelectable()
	 */

	public boolean isSelectable() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#link(dxf.DxfData)
	 */

	public boolean link(DxfData dxf) {
		// TODO 自動生成されたメソッド・スタブ
		return true;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#setError()
	 */

	public void setError() {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @see dxf.section.entities.DxfEntity#transform(double, double)
	 */

	public void transform(double dx, double dy) {
		// TODO 自動生成されたメソッド・スタブ

	}

}

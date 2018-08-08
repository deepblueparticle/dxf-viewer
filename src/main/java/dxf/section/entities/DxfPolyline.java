/**
 * 
 */
package dxf.section.entities;

import gui.ViewingEnvironment;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * @author FUJIWARA Masayasu
 * 
 */
public class DxfPolyline implements DxfEntity {

	private GeneralPath polyline;

	/**
	 * @param polyline
	 */
	public DxfPolyline(GeneralPath polyline) {
		this.polyline = polyline;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#clearError()
	 */

	public void clearError() {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @see dxf.section.entities.DxfEntity#draw(java.awt.Graphics2D,
	 *      gui.ViewingEnvironment)
	 */

	public void draw(Graphics2D g, ViewingEnvironment env) {
		g.draw(this.polyline);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 */

	public Rectangle2D getBounds2D() {
		return this.polyline.getBounds2D();
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection,
	 *      java.util.Collection)
	 */

	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * @see dxf.section.entities.DxfEntity#intersects(java.awt.geom.Rectangle2D)
	 */

	public boolean intersects(Rectangle2D rect) {
		return this.polyline.intersects(rect);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isContained(java.awt.Shape)
	 */

	public boolean isContained(Shape shape) {
		// TODO厳密には誤り
		return shape.contains(this.getBounds2D());
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
	}
}

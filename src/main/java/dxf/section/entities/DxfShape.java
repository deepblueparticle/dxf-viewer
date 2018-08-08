/**
 * 
 */
package dxf.section.entities;

import gui.ViewingEnvironment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * ShapeのWRAPPER
 * 
 * @author FUJIWARA Masayasu
 */
public class DxfShape implements DxfEntity {

	private boolean isError;

	/**
	 * 実体となる図形
	 */
	private Shape shape;

	public DxfShape(Shape shape) {
		this.shape = shape;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#clearError()
	 */

	public void clearError() {
		this.isError = false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#draw(java.awt.Graphics2D,
	 *      gui.ViewingEnvironment)
	 */

	public void draw(Graphics2D g, ViewingEnvironment env) {
		Color color = g.getColor();
		if (this.isError) {
			g.setColor(env.getErrorColor());
		}
		g.draw(this.shape);
		if (this.isError) {
			g.setColor(color);
		}
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 */

	public Rectangle2D getBounds2D() {
		return this.shape.getBounds2D();
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection,
	 *      java.util.Collection)
	 */

	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see dxf.section.entities.DxfEntity#intersects(java.awt.Rectangle)
	 */

	public boolean intersects(Rectangle2D rect) {
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isContained(java.awt.Shape)
	 */

	public boolean isContained(Shape shape) {
		return shape.contains(this.shape.getBounds2D());
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isSelectable()
	 */

	public boolean isSelectable() {
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#link(dxf.DxfData)
	 */

	public boolean link(DxfData dxf) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see dxf.section.entities.DxfEntity#setError()
	 */

	public void setError() {
		this.isError = true;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#transform(double, double)
	 */

	public void transform(double dx, double dy) {
		throw new UnsupportedOperationException();
	}
}

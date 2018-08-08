package dxf.section.entities;

import gui.ViewingEnvironment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import util.RoundNumber;

import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * DXFファイルのアークを扱うクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DxfArc implements DxfEntity {

	/**
	 * アーク
	 */
	private Arc2D.Double arc;

	private boolean isError;

	/**
	 * 線種
	 */
	private String lineType;

	public DxfArc(Arc2D.Double arc) {
		this.lineType = "CONTINUOUS";
		this.arc = arc;
	}

	public DxfArc(Number x, Number y, Number radius, Number startAngle, Number endAngle, String type) {
		this.lineType = type;
		this.arc = new Arc2D.Double(Arc2D.OPEN);
		this.arc.x = x.doubleValue() - radius.doubleValue();
		this.arc.y = y.doubleValue() - radius.doubleValue();
		this.arc.width = radius.doubleValue() * 2;
		this.arc.height = this.arc.width;
		this.arc.start = -endAngle.doubleValue();
		this.arc.extent = endAngle.doubleValue() - startAngle.doubleValue();
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
		Stroke stroke = env.getStroke(this.lineType);
		if (stroke != null) {
			Stroke defaultStroke = g.getStroke();
			g.setStroke(stroke);
			g.draw(this.arc);
			g.setStroke(defaultStroke);
			g.setStroke(defaultStroke);
		}
		if (this.isError) {
			g.setColor(color);
		}
	}

	public boolean equalsShape(DxfArc arc) {
		RoundNumber w1 = new RoundNumber(this.arc.getWidth());
		RoundNumber h1 = new RoundNumber(this.arc.getHeight());
		RoundNumber w2 = new RoundNumber(arc.arc.getWidth());
		RoundNumber h2 = new RoundNumber(arc.arc.getHeight());
		return (w1.equals(w2) && h1.equals(h2));
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 * @see java.awt.Shape#getBounds2D()
	 */

	public Rectangle2D getBounds2D() {
		return this.arc.getBounds2D();
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection,
	 *      java.util.Collection)
	 */

	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {
		if ("CONTINUOUS".equals(this.lineType)) {
			Point2D p1 = this.arc.getStartPoint();
			pointsX.add(new DimensionNode(p1.getX()));
			pointsY.add(new DimensionNode(p1.getY()));

			Point2D p2 = this.arc.getEndPoint();
			pointsX.add(new DimensionNode(p2.getX()));
			pointsY.add(new DimensionNode(p2.getY()));

		} else {
			Point2D p1 = this.arc.getStartPoint();
			pointsX.add(new DimensionNode(p1.getX(), false));
			pointsY.add(new DimensionNode(p1.getY(), false));

			Point2D p2 = this.arc.getEndPoint();
			pointsX.add(new DimensionNode(p2.getX(), false));
			pointsY.add(new DimensionNode(p2.getY(), false));

		}
		pointsX.add(new DimensionNode(this.arc.getMinX(), false));
		pointsY.add(new DimensionNode(this.arc.getMinY(), false));
		pointsX.add(new DimensionNode(this.arc.getMaxX(), false));
		pointsY.add(new DimensionNode(this.arc.getMaxY(), false));
	}

	public double getDiameter() {
		return this.arc.width;
	}

	public Point2D getEndPoint() {
		return this.arc.getEndPoint();
	}

	public double getMaxX() {
		return this.arc.getMaxX();
	}

	public double getMaxY() {
		return this.arc.getMaxY();
	}

	public double getMinX() {
		return this.arc.getMinX();
	}

	public double getMinY() {
		return this.arc.getMinY();
	}

	public Point2D getStartPoint() {
		return this.arc.getStartPoint();
	}

	public DxfArc getSymmetryX(double x) {
		Arc2D.Double arc = new Arc2D.Double();
		double dx = x * 2;
		arc.width = this.arc.width;
		arc.height = this.arc.height;
		arc.extent = this.arc.extent;
		arc.x = dx - this.arc.x - this.arc.width;
		arc.y = this.arc.y;
		arc.start = Math.PI - this.arc.start + this.arc.extent;
		while (arc.start < 0) {
			arc.start += Math.PI * 2;
		}
		return new DxfArc(arc);
	}

	public DxfArc getSymmetryY(double y) {
		Arc2D.Double arc = new Arc2D.Double();
		double dy = y * 2;
		arc.width = this.arc.width;
		arc.height = this.arc.height;
		arc.extent = this.arc.extent;
		arc.x = this.arc.x;
		arc.y = dy - this.arc.y - this.arc.height;
		arc.start = Math.PI * 2 - this.arc.start - this.arc.extent;
		return new DxfArc(arc);
	}

	public String getType() {
		return this.lineType;
	}

	public double getX() {
		return this.arc.getCenterX();
	}

	public double getY() {
		return this.arc.getCenterY();
	}

	public int hashCode() {
		return this.arc.hashCode();
	}

	/**
	 * @see dxf.section.entities.DxfEntity#intersects(java.awt.Rectangle2D)
	 */

	public boolean intersects(Rectangle2D rect) {
		return this.arc.intersects(rect);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isContained(java.awt.Shape)
	 */

	public boolean isContained(Shape shape) {
		// TODO 正確な包含関係を調べたほうがよい。
		return shape.contains(this.arc.getBounds2D());
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isSelectable()
	 */

	public boolean isSelectable() {
		return !"CENTER".equals(this.lineType);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#link(dxf.DxfData)
	 */

	public boolean link(DxfData dxf) {
		return true;
	}

	public boolean nearlyEquals(Object obj) {
		if (obj instanceof DxfArc) {
			DxfArc arc = (DxfArc) obj;
			if (RoundNumber.nearlyEquals(this.arc.x, arc.arc.x) && RoundNumber.nearlyEquals(this.arc.y, arc.arc.y)) {
				Point2D sp1 = this.arc.getStartPoint();
				Point2D sp2 = arc.getStartPoint();
				Point2D ep1 = this.arc.getEndPoint();
				Point2D ep2 = arc.getEndPoint();
				if (RoundNumber.nearlyEquals(sp1.getX(), sp2.getX()) && RoundNumber.nearlyEquals(sp1.getY(), sp2.getY())) {
				} else {
					System.out.println(sp1);
					System.out.println(sp2);
				}
				if (RoundNumber.nearlyEquals(ep1.getX(), ep2.getX()) && RoundNumber.nearlyEquals(ep1.getY(), ep2.getY())) {
				} else {
					System.out.println(ep1);
					System.out.println(ep2);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 円弧と指定した点の距離の2乗を計算するメソッド
	 * 
	 * @param x
	 *            距離を求める点のX座標
	 * @param y
	 *            距離を求める点のY座標
	 * @return
	 */
	public double ptSegDistSq(double x, double y) {
		double dx = x - this.arc.x;
		double dy = y - this.arc.y;
		double atan = Math.atan2(dy, dx);
		double start = this.arc.start % (2 * Math.PI);
		double end = start + this.arc.extent;
		double r = this.arc.width / 2;
		if (atan >= start && atan <= end) {
			return Math.abs(dx * dx + dy * dy - r * r);
		} else {
			Point2D s = this.getStartPoint();
			Point2D e = this.getStartPoint();
			double dist = Math.min(s.distance(x, y), e.distance(x, y));
			return Math.min(dist, dx * dx + dy * dy + r * r);
		}
	}

	/**
	 * 円弧と指定した点の距離の2乗を計算するメソッド
	 * 
	 * @param pt
	 *            距離を求める点
	 * @return
	 */
	public double ptSegDistSq(Point2D pt) {
		return this.ptSegDistSq(pt.getX(), pt.getY());
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
		this.arc.x += dx;
		this.arc.y += dy;
	}
}

package dxf.section.entities;

import gui.ViewingEnvironment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import util.RoundNumber;

import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * DXFファイルの円を扱うクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DxfCircle implements DxfEntity {

	private boolean checked = false;

	private Ellipse2D.Double circle;

	private boolean isError;

	/**
	 * 線種
	 */
	private String style;

	public DxfCircle(Number radius, Number x, Number y, String type) {
		double r = radius.doubleValue();
		double diameter = r * 2;
		this.circle = new Ellipse2D.Double(x.doubleValue() - r, y.doubleValue() - r, diameter, diameter);
		this.style = type;
	}

	public void checked() {
		this.checked = true;
	}

	private boolean checkSymmetry(Collection<DxfCircle> circleX, Collection<DxfCircle> circleY) {
		for (DxfCircle cx : circleX) {
			if (this != cx) {
				for (DxfCircle cy : circleY) {
					if (this != cy) {
						if (this.equalsCenter(cx.circle.getCenterX(), cy.circle.getCenterY())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#clearError()
	 */
	public void clearError() {
		this.isError = false;
	}

	public void draw(Graphics2D g, ViewingEnvironment env) {
		Color color = g.getColor();
		if (this.isError || !this.checked) {
			g.setColor(env.getErrorColor());
		}
		Stroke stroke = env.getStroke(this.style);
		if (stroke != null) {
			Stroke defaultStroke = g.getStroke();
			g.setStroke(stroke);
			g.draw(this.circle);
			g.setStroke(defaultStroke);
		}
		if (this.isError || !this.checked) {
			g.setColor(color);
		}
	}

	public boolean equals(Object obj) {
		if (this.hashCode() == obj.hashCode()) {
			if (obj instanceof DxfCircle) {
				DxfCircle circle = (DxfCircle) obj;
				return this.circle.equals(circle.circle);
			}
		}
		return false;
	}

	public boolean equalsCenter(double x, double y) {
		return RoundNumber.nearlyEquals(this.circle.getCenterX(), x) && RoundNumber.nearlyEquals(this.circle.getCenterY(), y);
	}

	private boolean equalsScrew(DxfCircle c) {
		return RoundNumber.nearlyEquals(c.circle.width, this.circle.width);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 * @see java.awt.Shape#getBounds2D()
	 */
	public Rectangle2D getBounds2D() {
		return this.circle.getBounds2D();
	}

	public void getCenterLinkCircles(Set<DxfCircle> result, DxfCircle circle, Collection<DxfCircle> circles, Collection<DxfLine> centers) {
		for (DxfLine line : centers) {
			if (RoundNumber.nearlyEqualsZero(line.ptSegDistSq(circle))) {
				for (DxfCircle c : circles) {
					if (RoundNumber.nearlyEqualsZero(line.ptSegDistSq(c))) {
						if (!result.contains(c)) {
							result.add(c);
							getCenterLinkCircles(result, c, circles, centers);
						}
					}
				}
			}
		}
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection, java.util.Collection)
	 */
	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {
		if (!"CENTER".equals(this.style)) {
			pointsX.add(new DimensionNode(this.circle.x + this.circle.width / 2));
			pointsY.add(new DimensionNode(this.circle.y + this.circle.height / 2));
			pointsX.add(new DimensionNode(this.circle.x, false));
			pointsX.add(new DimensionNode(this.circle.x + this.circle.width, false));
			pointsY.add(new DimensionNode(this.circle.y, false));
			pointsY.add(new DimensionNode(this.circle.y + this.circle.height, false));
		}
	}

	/**
	 * 一番近い同心円を取得する．
	 * 
	 * @param circles
	 * @return
	 */
	private DxfCircle getConcentric(Collection<DxfCircle> circles) {
		DxfCircle circle = null;
		double min = Double.POSITIVE_INFINITY;
		for (DxfCircle c : circles) {
			if (c != this) {
				if (RoundNumber.nearlyEquals(c.circle.getCenterX(), this.circle.getCenterX())
						&& RoundNumber.nearlyEquals(c.circle.getCenterY(), this.circle.getCenterY())) {
					double diff = Math.abs(c.circle.width - this.circle.width);
					if (Double.compare(min, diff) > 0) {
						min = diff;
						circle = c;
					}
				}
			}
		}
		return circle;
	}

	/**
	 * @return 直径
	 */
	public double getDiameter() {
		return this.circle.width;
	}

	/**
	 * 点と円の距離の二乗を倍精度で返します。
	 * 
	 * @param x
	 *            円との距離を求める点
	 * @param y
	 *            円との距離を求める点
	 * @return 点と円の距離
	 */
	public double getDistSq(double x, double y) {
		double dx = this.circle.x - x;
		double dy = this.circle.y - y;
		double radius = this.circle.width / 2;
		return Math.abs(dx * dx + dy * dy - radius * radius);
	}

	/**
	 * 点と円の距離の二乗を倍精度で返します。
	 * 
	 * @param p 円との距離を求める点
	 * @return 点と円の距離
	 */
	public double getDistSq(Point2D p) {
		return this.getDistSq(p.getX(), p.getY());
	}

	public double getMaxX() {
		return this.circle.x + this.circle.width;
	}

	public double getMaxY() {
		return this.circle.y + this.circle.height;
	}

	public double getMinX() {
		return this.circle.x;
	}

	public double getMinY() {
		return this.circle.y;
	}

	public double getRadius() {
		return this.circle.getWidth() / 2;
	}
	
	public double getRadiusSq() {
		double r = this.getRadius();
		return r * r;
	}

	public static final int ON_CIRCLE = 1;
	public static final int ON_CENTER_LINES = 2;
	
	/**
	 * 4-φ16のような寸法が与えられたとき，寸法が入っていると思われる円を返すメソッド
	 * 上下左右対称に対応．点パターンマッチングを利用するほうがいいのかもしれない．
	 * 
	 * @param circles
	 * @param n
	 * @return
	 */
	public int getSimilarPatternCircles(int n, Set<DxfCircle> similars, Collection<DxfCircle> circles, Collection<DxfLine> centers, Collection<DxfEntity> errors) {
		this.checked();
		similars.add(this);
		for (DxfCircle circle : circles) {
			if ("CENTER".equals(circle.getStyle())) {
				if (RoundNumber.nearlyEqualsZero(circle.ptSegDistSq(this))) {
					for (DxfCircle c : circles) {
						if (RoundNumber.nearlyEqualsZero(circle.ptSegDistSq(c))) {
							similars.add(c);
						}
					}
				}
			}
		}
		if (similars.size() > 1) {
			return ON_CIRCLE;
		}
		this.getCenterLinkCircles(similars, this, circles, centers);
		if (similars.size() > 0) {
			return ON_CENTER_LINES;
		}
		DxfCircle concentric = getConcentric(circles);
		if (concentric != null) {
			similars.add(concentric);
			n *= 2;
		}
		List<DxfCircle> patternX = new ArrayList<DxfCircle>();
		List<DxfCircle> patternY = new ArrayList<DxfCircle>();
		List<DxfCircle> patternXY = new ArrayList<DxfCircle>();
		for (DxfCircle c : circles) {
			if (c != this) {
				if (this.equalsScrew(c) || (concentric != null && concentric.equalsScrew(c))) {
					if (RoundNumber.nearlyEquals(c.circle.getCenterX(), this.circle.getCenterX())) {
						patternY.add(c);
						n--;
					} else if (RoundNumber.nearlyEquals(c.circle.getCenterY(), this.circle.getCenterY())) {
						patternX.add(c);
						n--;
					}
				}
			}
		}
		if (n >= 0) {
			for (DxfCircle c : circles) {
				if (this.equalsScrew(c) || (concentric != null && concentric.equalsScrew(c))) {
					if (c.checkSymmetry(patternX, patternY)) {
						similars.add(c);
					}
				}
			}
			if (n >= 0) {
				for (DxfCircle c : patternX)
					similars.add(c);
				for (DxfCircle c : patternY)
					similars.add(c);
				for (DxfCircle c : patternXY)
					similars.add(c);
			}
		}
		return 0;
	}

	/**
	 * @return 線種
	 */
	public String getStyle() {
		return this.style;
	}

	/**
	 * 中心のX座標を返すメソッド
	 * 
	 * @return 中心のX座標
	 */
	public double getX() {
		return this.circle.getCenterX();
	}

	/**
	 * 中心のY座標を返すメソッド
	 * 
	 * @return 中心のY座標
	 */
	public double getY() {
		return this.circle.getCenterY();
	}

	public int hashCode() {
		return this.circle.hashCode();
	}

	/**
	 * @see dxf.section.entities.DxfEntity#intersects(java.awt.Rectangle2D)
	 */
	public boolean intersects(Rectangle2D rect) {
		return this.circle.intersects(rect);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isContained(java.awt.Shape)
	 */
	public boolean isContained(Shape shape) {
		// TODO 正確に包含関係を調べる必要がある
		return shape.contains(this.circle.getBounds2D());
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isSelectable()
	 */
	public boolean isSelectable() {
		return true;
	}

	public boolean link(DxfData dxf) {
		return true;
	}

	public boolean onCircle(Point2D p) {
		double dx = this.circle.x - p.getX();
		double dy = this.circle.y - p.getY();
		double radius = this.circle.width / 2;
		return Math.floor((dx * dx + dy * dy) * 100000) == Math.floor(radius * 1000000);
	}

	/**
	 * 円との距離
	 * 中心からの距離 - 半径
	 * @param x
	 * @param y
	 * @return
	 */
	public double ptSegDistSq(double x, double y) {
		double dx = this.circle.getCenterX() - x;
		double dy = this.circle.getCenterY() - y;
		return (dx * dx + dy * dy) - this.getRadiusSq();
	}

	/**
	 * 円との距離
	 * 中心からの距離 - 半径
	 * @param x
	 * @param y
	 * @return
	 */
	public double ptSegDistSq(DxfCircle c) {
		return this.ptSegDistSq(c.getX(), c.getY());
	}

	/**
	 * @see dxf.section.entities.DxfEntity#setError()
	 */
	public void setError() {
		this.isError = true;
	}

	public void transform(double dx, double dy) {
		this.circle.x += dx;
		this.circle.y += dy;
	}
}
package dxf.section.entities;

import gui.ViewingEnvironment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import util.RoundNumber;
import util.RoundNumber2D;

import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * DXFファイルの線分を扱うクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DxfLine implements DxfEntity {

	public static final int DIRECTION_X = 1;

	public static Point2D getCrossPoint(DxfLine l1, DxfLine l2) {
		double x = 0;
		double y = 0;
		double a1 = (l1.line.y2 - l1.line.y1) / (l1.line.x2 - l1.line.x1);
		double a2 = (l2.line.y2 - l2.line.y1) / (l2.line.x2 - l2.line.x1);
		if (a1 == a2) return null; // 平行
	
		if( ( l1.line.x1 != l1.line.x2 ) && ( l2.line.x1 != l2.line.x2 ) ){
			x = ((l1.line.x1 * a1) - l1.line.y1 - (l2.line.x1 * a2) + l2.line.y1) / (a1 - a2);
			y = a1 * ( x-l1.line.x1 ) + l1.line.y1;
		// l1が垂直
		} else if( ( l1.line.x1 == l1.line.x2 ) && ( l2.line.x1 != l2.line.x2 ) ){
			x = l1.line.x1;
			y = a2 * ( x-l2.line.x1 ) + l2.line.y1;
		// l2が垂直
		} else if( ( l1.line.x1 != l1.line.x2 ) && ( l2.line.x1 == l2.line.x2 ) ){
			x = l2.line.x1;
			y = a1 * (x -l1.line.x1 ) + l1.line.y1;
		}
		return new Point2D.Double(x, y);
	}
	
	public Point2D getCrossPoint(DxfLine l) {
		return getCrossPoint(this, l);
	}

	public static final int DIRECTION_Y = -1;
	
	public boolean equalsLine(DxfLine line) {
		return RoundNumber.nearlyEqualsZero(this.line.ptLineDist(line.getX1(), line.getY1()))
		&& RoundNumber.nearlyEqualsZero(this.line.ptLineDist(line.getX2(), line.getY2()));
	}

	/**
	 * 線分の方向
	 */
	private byte direction;

	private boolean isError;

	/**
	 * 線分の実体
	 */
	private Line2D.Double line;

	private boolean selectable;

	/**
	 * 線種
	 */
	private String style;

	public DxfLine(double x1, double y1, double x2, double y2, String type) {
		this.line = new Line2D.Double(x1, y1, x2, y2);
		this.style = type;
		this.selectable = !"CENTER".equals(this.style);
		if (RoundNumber.nearlyEquals(x1, x2)) {
			this.direction += DIRECTION_Y;
		}
		if (RoundNumber.nearlyEquals(y1, y2)) {
			this.direction += DIRECTION_X;
		}
	}

	private DxfLine(Line2D.Double line) {
		this.line = line;
	}

	public DxfLine(Number x1, Number y1, Number x2, Number y2, String type) {
		this(x1.doubleValue(), y1.doubleValue(), x2.doubleValue(), y2.doubleValue(), type);
	}

	public DxfLine(Point2D p1, Point2D p2) {
		this(p1.getX(), p1.getY(), p2.getX(), p2.getY(), "CHECK");
	}

	public DxfLine(RoundNumber2D p1, RoundNumber2D p2) {
		this(p1.getX(), p1.getY(), p2.getX(), p2.getY(), "CHECK");
	}

	/**
	 * 角度をチェックする
	 * 
	 * @return 角度
	 */
	public boolean checkAngle(DxfLine line) {
		double dx1 = -this.line.y1 + this.line.y2;
		double dy1 = this.line.x1 - this.line.x2;

		double dx2 = line.line.x1 - line.line.x2;
		double dy2 = line.line.y1 - line.line.y2;
		double inner = dx1 * dx2 + dy1 * dy2;
		return RoundNumber.nearlyAbsEquals(inner, 0);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#clearError()
	 */

	public void clearError() {
		this.isError = false;
	}

	public void draw(Graphics2D g, ViewingEnvironment env) {
		Color color = g.getColor();
		if (this.isError) {
			g.setColor(env.getErrorColor());
		}
		Stroke stroke = env.getStroke(this.style);
		if (stroke != null) {
			Stroke defaultStroke = g.getStroke();
			g.setStroke(stroke);
			g.draw(this.line);
			g.setStroke(defaultStroke);
		}
		if (this.isError) {
			g.setColor(color);
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof DxfLine) {
			DxfLine l = (DxfLine) obj;
			return (RoundNumber.nearlyEquals(this.line.x1, l.line.x1) && RoundNumber.nearlyEquals(this.line.x2, l.line.x2)
					&& RoundNumber.nearlyEquals(this.line.y1, l.line.y1) && RoundNumber.nearlyEquals(this.line.y2, l.line.y2))
					|| (RoundNumber.nearlyEquals(this.line.x1, l.line.x2) && RoundNumber.nearlyEquals(this.line.x2, l.line.x1)
							&& RoundNumber.nearlyEquals(this.line.y1, l.line.y2) && RoundNumber.nearlyEquals(this.line.y2, l.line.y1));
		}
		return false;
	}

	/**
	 * 形が同じかどうかをチェックする 平行移動、軸対象移動させて重なる場合はtrueを返す。
	 * 
	 * @param line
	 *            比較する線分エンティティ
	 * @return
	 */
	public boolean equalsShape(DxfLine line) {
		// TODO もう少し厳密な対象性などの比較をしたほうがいいんだと思う。
		RoundNumber w1 = new RoundNumber(Math.abs(this.line.x1 - this.line.x2));
		RoundNumber h1 = new RoundNumber(Math.abs(this.line.y1 - this.line.y2));
		RoundNumber w2 = new RoundNumber(Math.abs(line.line.x1 - line.line.x2));
		RoundNumber h2 = new RoundNumber(Math.abs(line.line.y1 - line.line.y2));
		if (!w1.equals(w2) || !h1.equals(h2)) {
			return false;
		}
		return true;
	}

	/**
	 * @see java.awt.Shape#getBounds2D()
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 */

	public Rectangle2D getBounds2D() {
		return this.line.getBounds2D();
	}

	public double getCenterPointX() {
		return (this.line.x1 + this.line.x2) / 2;
	}

	public double getCenterPointY() {
		return (this.line.y1 + this.line.y2) / 2;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection,
	 *      java.util.Collection)
	 */

	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {
		if ("CONTINUOUS".equals(this.style) || "BYBLOCK".equals(this.style)) {
			if (this.direction == DIRECTION_Y) {
				pointsX.add(new DimensionNode(this.line.x1));
				pointsY.add(new DimensionNode(this.line.y1));
				pointsY.add(new DimensionNode(this.line.y2));
			} else if (this.direction == DIRECTION_X) {
				pointsX.add(new DimensionNode(this.line.x1));
				pointsX.add(new DimensionNode(this.line.x2));
				pointsY.add(new DimensionNode(this.line.y1));
			} else {
				pointsX.add(new DimensionNode(this.line.x1));
				pointsX.add(new DimensionNode(this.line.x2));
				pointsY.add(new DimensionNode(this.line.y1));
				pointsY.add(new DimensionNode(this.line.y2));
			}
		} else {
			if (!"CENTER".equals(this.style)) {
				if (this.direction == DIRECTION_Y) {
					pointsX.add(new DimensionNode(this.line.x1, false));
					pointsY.add(new DimensionNode(this.line.y1, false));
					pointsY.add(new DimensionNode(this.line.y2, false));
				} else if (this.direction == DIRECTION_X) {
					pointsX.add(new DimensionNode(this.line.x1, false));
					pointsX.add(new DimensionNode(this.line.x2, false));
					pointsY.add(new DimensionNode(this.line.y1, false));
				} else {

				}
			} else {
				pointsX.add(new DimensionNode(this.line.x1));
				pointsX.add(new DimensionNode(this.line.x2));
				pointsY.add(new DimensionNode(this.line.y1));
				pointsY.add(new DimensionNode(this.line.y2));
			}
		}
	}

	/**
	 * 寸法の方向を取得するメソッド
	 * 
	 * @return DIRECTION_X or DIRECTION_Y or 0
	 */
	public int getDirection() {
		return this.direction;
	}

	public Point2D.Double getG() {
		return new Point2D.Double((this.line.getX1() + this.line.getX2()) / 2, (this.line.getY1() + this.line.getY2()) / 2);
	}

	/**
	 * 線種を取得するメソッド
	 * 
	 * @return 線種
	 */
	public String getStyle() {
		return this.style;
	}

	public DxfLine getSymmetry(double x, double y) {
		Line2D.Double line = new Line2D.Double();
		double dx = x * 2;
		double dy = y * 2;
		line.x1 = dx - this.line.x1;
		line.x2 = dx - this.line.x2;
		line.y1 = dy - this.line.y1;
		line.y2 = dy - this.line.y2;
		return new DxfLine(line);
	}

	public DxfLine getSymmetryX(double x) {
		Line2D.Double line = new Line2D.Double();
		double dx = x * 2;
		line.x1 = dx - this.line.x1;
		line.x2 = dx - this.line.x2;
		line.y1 = this.line.y1;
		line.y2 = this.line.y2;
		return new DxfLine(line);
	}

	public DxfLine getSymmetryY(double y) {
		Line2D.Double line = new Line2D.Double();
		double dy = y * 2;
		line.x1 = this.line.x1;
		line.x2 = this.line.x2;
		line.y1 = dy - this.line.y1;
		line.y2 = dy - this.line.y2;
		return new DxfLine(line);
	}

	/**
	 * 始点のX座標を倍精度で返します。
	 * 
	 * @return
	 */
	public double getX1() {
		return this.line.x1;
	}

	/**
	 * 終点のX座標を倍精度で返します。
	 * 
	 * @return
	 */
	public double getX2() {
		return this.line.x2;
	}

	/**
	 * 始点のY座標を倍精度で返します。
	 * 
	 * @return
	 */
	public double getY1() {
		return this.line.y1;
	}

	/**
	 * 終点のY座標を倍精度で返します。
	 * 
	 * @return
	 */
	public double getY2() {
		return this.line.y2;
	}

	public int hashCode() {
		return this.line.hashCode();
	}

	public boolean isParallel(DxfLine l) {
		double dx1 = this.line.x1 - this.line.x2;
		double dy1 = this.line.y1 - this.line.y2;
		double dx2 = l.line.x1 - l.line.x2;
		double dy2 = l.line.y1 - l.line.y2;
		return RoundNumber.nearlyAbsEquals(dx1 * dy2, dx2 * dy1);
	}
	/**
	 * 内積計算
	 * 
	 * @param line 計算する線分
	 * @return 計算した内積の値
	 */
	public double innerProduct(DxfLine line) {
		double v1x = this.line.x1 - this.line.x2;
		double v2x = line.line.x1 - line.line.x2;
		double v1y = this.line.y1 - this.line.y2;
		double v2y = line.line.y1 - line.line.y2;
		return v1x * v2x + v1y * v2y;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#intersects(java.awt.Rectangle)
	 */

	public boolean intersects(Rectangle2D rect) {
		return this.isSelectable() && this.line.intersects(rect);
	}

	public boolean intersectsLine(double x1, double y1, double x2, double y2) {
		return this.line.intersectsLine(x1, y1, x2, y2);
	}

	/**
	 * 線分の交差を判定するメソッド
	 * 
	 * @param line
	 *            交差を判定する線分
	 * @return 交差していればtrue、交差していなければfalseを返す。
	 */
	public boolean intersectsLine(DxfLine line) {
		return this.line.intersectsLine(line.line);
	}

	public boolean intersectsLine(Line2D line) {
		return this.line.intersectsLine(line);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isContained(java.awt.Shape)
	 */

	public boolean isContained(Shape shape) {
		return shape.contains(this.line.x1, this.line.y1) && shape.contains(this.line.x2, this.line.y2);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isSelectable()
	 */

	public boolean isSelectable() {
		return this.selectable;
	}

	public double length() {
		double length = 0;
		if (this.direction == DxfLine.DIRECTION_X) {
			length = Math.abs(this.line.x1 - this.line.x2);
		} else if (this.direction == DxfLine.DIRECTION_Y) {
			length = Math.abs(this.line.y1 - this.line.y2);
		} else {
			double dx = this.line.x1 - this.line.x2;
			double dy = this.line.y1 - this.line.y2;
			length = Math.sqrt(dx * dx + dy * dy);
		}
		return length;
	}

	public double lineDistSq(DxfCircle circle) {
		return this.line.ptLineDistSq(circle.getX(), circle.getY());
	}

	public boolean link(DxfData dxf) {
		return true;
	}

	public double ptLineDistSq(double x, double y) {
		return this.line.ptLineDistSq(x, y);
	}

	/**
	 * 点とこの線分との距離を求めるメソッド
	 * 
	 * @param x
	 *            X座標
	 * @param y
	 *            Y座標
	 * @return 距離
	 */
	public double ptSegDistSq(double x, double y) {
		return this.line.ptSegDistSq(x, y);
	}

	/**
	 * 点とこの線分との距離を求めるメソッド
	 * 
	 * @param pt
	 *            点
	 * @return 距離
	 */
	public double ptSegDistSq(Point2D pt) {
		return this.line.ptSegDistSq(pt);
	}

	public double ptSegDistSq(DxfCircle c) {
		return this.line.ptSegDistSq(c.getX(), c.getY());
	}

	/**
	 * @see dxf.section.entities.DxfEntity#setError()
	 */

	public void setError() {
		this.isError = true;
	}

	public void setSelectable(boolean flag) {
		this.selectable = flag;
	}

	/**
	 * 線種を設定するメソッド
	 * 
	 * @param style
	 *            線種
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	public void transform(double dx, double dy) {
		this.line.x1 += dx;
		this.line.y1 += dy;
		this.line.x2 += dx;
		this.line.y2 += dy;
	}
}

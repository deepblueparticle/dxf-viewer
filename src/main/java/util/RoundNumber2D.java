package util;

import java.awt.geom.Point2D;

/**
 * @author FUJIWARA Masayasu
 */
public class RoundNumber2D {

	private RoundNumber x;
	private RoundNumber y;

	/**
	 * @param number
	 */
	public RoundNumber2D(double x, double y) {
		this(new RoundNumber(x), new RoundNumber(y));
	}

	public RoundNumber2D(RoundNumber x, RoundNumber y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.hashCode() == obj.hashCode()) {
			if (obj instanceof RoundNumber2D) {
				RoundNumber2D pt = (RoundNumber2D) obj;
				return this.x.equals(pt.x) && this.y.equals(pt.y);
			}
		}
		return false;
	}

	public Point2D getPoint2D() {
		return new Point2D.Double(this.x.getValue(), this.y.getValue());
	}

	public double getX() {
		return this.x.getValue();
	}

	public double getY() {
		return this.y.getValue();
	}

	public int hashCode() {
		return this.x.hashCode() + this.y.hashCode();
	}

	public String toString() {
		return this.x.toString() + ", " + this.y.toString();
	}
}

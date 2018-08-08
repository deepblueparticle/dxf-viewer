package util;

import java.awt.geom.Point2D;

/**
 * @author FUJIWARA Masayasu
 */
public class RoundNumber implements Comparable<RoundNumber> {

	/**
	 * 保証する精度
	 */
	public final static long ACCURACY = 1000L; // 10000Lではだめな図面があった．

	/**
	 * 浮動小数点の誤差を許して絶対値が同値であるか確認するメソッド
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean nearlyAbsEquals(double v1, double v2) {
		return nearlyEquals(Math.abs(v1), Math.abs(v2), ACCURACY);
	}

	/**
	 * 浮動小数点の誤差を許して0であるか確認するメソッド
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean nearlyEqualsZero(double v) {
		return nearlyEquals(v, 0, ACCURACY);
	}

	/**
	 * 浮動小数点の誤差を許して同値であるか確認するメソッド
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean nearlyEquals(double v1, double v2) {
		return nearlyEquals(v1, v2, ACCURACY);
	}

	public static boolean nearlyEquals(double v1, double v2, long accuracy) {
		return Math.round(v1 * accuracy) == Math.round(v2 * accuracy);
	}

	public static boolean nearlyEquals(Point2D p1, Point2D p2, long accuracy) {
		return nearlyEquals(p1.getX(), p2.getX(), accuracy) && nearlyEquals(p1.getY(), p2.getY(), accuracy);
	}

	private double number;

	/**
	 * @param number
	 */
	public RoundNumber(double number) {
		this.number = number;
	}

	public int compareTo(RoundNumber o) {
		return (int) (Math.round(this.number * ACCURACY) - Math.round(o.number * ACCURACY));
	}

	public boolean equals(Object obj) {
		if (obj instanceof RoundNumber) {
			RoundNumber n = (RoundNumber) obj;
			return nearlyEquals(n.number, this.number);
		}
		return false;
	}

	public double getValue() {
		return this.number;
	}

	public int hashCode() {
		return (int) (Math.round(this.number * ACCURACY) % Integer.MAX_VALUE);
	}

	public boolean nearlyEquals(RoundNumber obj, int accuracy) {
		RoundNumber n = (RoundNumber) obj;
		return nearlyEquals(n.number, this.number);
	}

	public void sub(double value) {
		this.number -= value;
	}

	public String toString() {
		return Double.toString(this.number);
	}
}

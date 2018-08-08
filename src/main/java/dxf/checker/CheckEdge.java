/**
 * 
 */
package dxf.checker;

/**
 * @author FUJIWARA Masayasu
 * 
 */
public class CheckEdge {
	private double v1;
	private double v2;

	public CheckEdge(double v1, double v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public double getValue1() {
		return this.v1;
	}

	public double getValue2() {
		return this.v2;
	}
}

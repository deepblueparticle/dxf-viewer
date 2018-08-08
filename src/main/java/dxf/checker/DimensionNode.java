/**
 * 
 */
package dxf.checker;

import util.RoundNumber;

/**
 * @author fujiwara
 * 
 */
public class DimensionNode extends RoundNumber {

	/**
	 * 寸法の要不要のフラグ
	 */
	private boolean requireDimension;

	/**
	 * @param number
	 *            値
	 * @param dimension
	 *            寸法の要不要
	 */
	public DimensionNode(double number) {
		this(number, true);
	}

	/**
	 * @param number
	 *            値
	 * @param dimension
	 *            寸法の要不要
	 */
	public DimensionNode(double number, boolean dimension) {
		super(number);
		this.requireDimension = dimension;
	}

	public boolean isReqDim() {
		return this.requireDimension;
	}
}

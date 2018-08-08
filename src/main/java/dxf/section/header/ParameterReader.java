package dxf.section.header;

/**
 * パラメータ読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public abstract class ParameterReader {

	/**
	 * DXFから読み込んだパラメータを消去するメソッド
	 */
	abstract void clear();

	/**
	 * DXFから図形を読み込むためのメソッド
	 * 
	 * @return DXFファイルの図形
	 */
	public Number[] getParameter() {
		Number[] figure = null;
		if (this.hasParameter()) {
			figure = this.makeParameter();
			this.clear();
		}
		return figure;
	}

	/**
	 * パラメータを構成できるだけのデータを読み込めているか確認するメソッド
	 * 
	 * @return パラメータを構成できればtrue、できなければfalseを返す。
	 */
	abstract boolean hasParameter();

	/**
	 * 読み込んだデータからパラメータを構成するメソッド 必要なデータを読み込めているかなどのチェックは不要
	 * 
	 * @return DXFファイルの図形
	 */
	abstract Number[] makeParameter();

	/**
	 * DXFからパラメータを読み込むためのメソッド
	 * 
	 * @param groupCode
	 *            グループコード
	 * @param line
	 *            対応するデータ
	 */
	public abstract void readParameter(int groupCode, String line);
}

package dxf.section.entities;

/**
 * DXFファイルから円を読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class CircleReader implements EntityReader {

	/**
	 * 半径
	 */
	private Number radius;

	private String type;

	/**
	 * Y座標値
	 */
	private Number x;

	/**
	 * X座標値
	 */
	private Number y;

	@Override
	public void clear() {
		this.radius = null;
		this.x = null;
		this.y = null;
		this.type = null;
	}

	@Override
	public boolean hasFigure() {
		return this.radius != null && this.x != null && this.y != null;
	}

	/**
	 * 読み込んだデータから図形ファイルを構成するメソッド
	 */
	@Override
	public DxfEntity makeFigure() {
		return new DxfCircle(this.radius.doubleValue(), this.x, this.y, this.type);
	}

	/**
	 * 図形データを読み込みます。
	 * 
	 * @param groupCode
	 *            グループコード
	 * @param line
	 *            データライン
	 */
	@Override
	public void readFigure(int groupCode, String line) {
		switch (groupCode) {
		case 6: // 線種
			this.type = line;
			break;
		case 10:
			this.x = Double.valueOf(line);
			break;
		case 20:
			this.y = Double.valueOf(line);
			break;
		// TODO Z座標、Z座標軸を考慮しなければならない。
		case 40:
			this.radius = Double.valueOf(line);
			break;
		}
	}
}

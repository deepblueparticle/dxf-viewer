package dxf.section.entities;

/**
 * DXFファイルから直線を読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class LineReader implements EntityReader {

	/**
	 * 線種
	 */
	private String lineType;

	/**
	 * 始点のX座標
	 */
	private Number x1;

	/**
	 * 終点のX座標
	 */
	private Number x2;

	/**
	 * 始点のY座標
	 */
	private Number y1;

	/**
	 * 終点のY座標
	 */
	private Number y2;

	/**
	 * DXFから読み込んだ図形を消去するメソッド
	 */
	@Override
	public void clear() {
		this.x1 = null;
		this.y1 = null;
		this.x2 = null;
		this.y2 = null;
		this.lineType = null;
	}

	@Override
	public boolean hasFigure() {
		return this.x1 != null && this.y1 != null && this.x2 != null & this.y2 != null;
	}

	@Override
	public DxfEntity makeFigure() {
		return new DxfLine(this.x1, this.y1, this.x2, this.y2, this.lineType);
	}

	/**
	 * DXFから図形を読み込むためのメソッド
	 */
	@Override
	public void readFigure(int groupCode, String line) {
		switch (groupCode) {
		case 6: // 線種
			this.lineType = line;
			break;
		case 10: // 始点のX座標
			this.x1 = Double.valueOf(line);
			break;
		case 20: // 始点のY座標
			this.y1 = Double.valueOf(line);
			break;
		case 11: // 終点のY座標
			this.x2 = Double.valueOf(line);
			break;
		case 21:// 終点のY座標
			this.y2 = Double.valueOf(line);
			break;
		// TODO Z座標、Z座標軸を考慮しなければならない。
		}
	}
}

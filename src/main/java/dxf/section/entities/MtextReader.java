package dxf.section.entities;

/**
 * DXFファイルからマルチテキストを読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class MtextReader implements EntityReader {

	/**
	 * 高さ
	 */
	private Number height;

	private Number position;

	/**
	 * テキスト
	 */
	private String text;

	/**
	 * X座標値
	 */
	private Number x;

	/**
	 * Y座標値
	 */
	private Number y;

	@Override
	public void clear() {
		this.x = null;
		this.y = null;
		this.text = null;
		this.position = null;
	}

	@Override
	public boolean hasFigure() {
		return this.x != null && this.y != null && this.text != null;
	}

	@Override
	public DxfEntity makeFigure() {
		return new DxfMtext(this.text, this.x, this.y, this.height, this.position);
	}

	@Override
	public void readFigure(int groupCode, String line) {
		switch (groupCode) {
		case 1: // 文字列
			this.text = line;
			break;
		case 10:
			this.x = Double.valueOf(line);
			break;
		case 20:
			this.y = Double.valueOf(line);
			break;
		// TODO Z座標、Z座標軸を考慮しなければならない。
		case 40: // 文字高さ
			this.height = Double.valueOf(line);
			break;
		case 71: // アタッチされる点
			this.position = Integer.valueOf(line.replaceFirst("\\s+", ""));
		}
	}

}

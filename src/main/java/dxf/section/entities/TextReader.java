package dxf.section.entities;

/**
 * DXFファイルからマルチテキストを読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.04
 */
public class TextReader implements EntityReader {

	private Number align;

	/**
	 * 高さ
	 */
	private Number height;
	/**
	 * テキスト
	 */
	private String text;

	private Number valign;

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
		this.align = null;
		this.valign = null;
	}

	@Override
	public boolean hasFigure() {
		return this.x != null && this.y != null && this.text != null;
	}

	@Override
	public DxfEntity makeFigure() {
		return new DxfText(this.text, this.x, this.y, this.height, this.align, this.valign);
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
		case 72: // アタッチされる点
			this.align = Byte.valueOf(line.replaceFirst("\\s+", ""));
			break;
		case 73: // 垂直方向の文字位置あわせ
			this.valign = Byte.valueOf(line.replaceFirst("\\s+", ""));
			break;
		}
	}

}

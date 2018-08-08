/**
 * 
 */
package dxf.section.entities;

/**
 * DXFファイルからINSERTを読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DimensionReader implements EntityReader {

	/**
	 * 挿入するブロック名
	 */
	private String block;

	/**
	 * ブロックの挿入位置のX座標
	 */
	private Number x1;

	/**
	 * ブロックの挿入位置のY座標
	 */
	private Number y1;

	/**
	 * @see dxf.section.entities.EntityReader#clear()
	 */
	@Override
	public void clear() {
		this.block = null;
		this.x1 = null;
		this.y1 = null;
	}

	/**
	 * @see dxf.section.entities.EntityReader#hasFigure()
	 */
	@Override
	public boolean hasFigure() {
		return this.block != null && this.x1 != null && this.y1 != null;
	}

	/**
	 * @see dxf.section.entities.EntityReader#makeFigure()
	 */
	@Override
	public DxfEntity makeFigure() {
		return new DxfDimension(this.block, this.x1, this.y1);
	}

	/**
	 * @see dxf.section.entities.EntityReader#readFigure(int, java.lang.String)
	 */
	@Override
	public void readFigure(int groupCode, String line) {
		switch (groupCode) {
			case 2: // 名前
				this.block = line;
				break;
			case 10: // X座標値
				this.x1 = Double.valueOf(line);
				break;
			case 20: // Y座標値
				this.y1 = Double.valueOf(line);
				break;
		}
	}
}

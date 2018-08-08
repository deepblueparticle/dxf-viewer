/**
 * 
 */
package dxf.section.entities;

/**
 * DXFファイルからARCを読み込むためのクラス
 * 
 * @author ma38su
 * @since 0.01
 */
public class ArcReader implements EntityReader {

	private Number endAngle;
	private Number radius;
	private Number startAngle;
	private String type;
	private Number x;

	private Number y;

	/**
	 * @see dxf.section.entities.EntityReader#clear()
	 */
	@Override
	public void clear() {
		this.x = null;
		this.y = null;
		this.radius = null;
		this.startAngle = null;
		this.endAngle = null;
		this.type = null;
	}

	/**
	 * @see dxf.section.entities.EntityReader#hasFigure()
	 */
	@Override
	public boolean hasFigure() {
		return this.x != null && this.y != null && this.radius != null && this.startAngle != null && this.endAngle != null;
	}

	/**
	 * @see dxf.section.entities.EntityReader#makeFigure()
	 */
	@Override
	public DxfEntity makeFigure() {
		return new DxfArc(this.x, this.y, this.radius, this.startAngle, this.endAngle, this.type);
	}

	/**
	 * @see dxf.section.entities.EntityReader#readFigure(int, java.lang.String)
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
		case 40:
			this.radius = Double.valueOf(line);
			break;
		case 50:
			this.startAngle = Double.valueOf(line);
			break;
		case 51:
			this.endAngle = Double.valueOf(line);
			break;
		}

	}

}

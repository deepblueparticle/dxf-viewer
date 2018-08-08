package dxf.section.entities;

/**
 * @author FUJIWARA Masayasu
 */
public class VertexReader implements EntityReader {

	private Number x;

	private Number y;

	/**
	 * @see dxf.section.entities.EntityReader#clear()
	 */
	@Override
	public void clear() {
		this.x = null;
		this.y = null;
	}

	/**
	 * @see dxf.section.entities.EntityReader#hasFigure()
	 */
	@Override
	public boolean hasFigure() {
		return this.x != null && this.y != null;
	}

	/**
	 * @see dxf.section.entities.EntityReader#makeFigure()
	 */
	@Override
	public DxfEntity makeFigure() {
		return new DxfVertex(this.x, this.y);
	}

	/**
	 * @see dxf.section.entities.EntityReader#readFigure(int, java.lang.String)
	 */
	@Override
	public void readFigure(int groupCode, String line) {
		switch (groupCode) {
		case 10:
			this.x = Double.valueOf(line);
			break;
		case 20:
			this.y = Double.valueOf(line);
			break;
		}
	}
}

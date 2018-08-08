/**
 * 
 */
package dxf.section.entities;

import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Map;

import dxf.section.SectionReader;

/**
 * DXFファイルの読み込みのためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class PolylineReader implements EntityReader {

	private Map<String, EntityReader> map;

	private GeneralPath polyline;

	private int polylineFlag;
	private EntityReader reader;

	public PolylineReader() {
		this.map = new HashMap<String, EntityReader>();
		this.map.put("VERTEX", new VertexReader());
	}

	/**
	 * @see dxf.section.entities.EntityReader#clear()
	 */
	public void clear() {
		this.polyline = null;
		this.reader = null;
	}

	private void close() {
		if (this.polyline != null) {
			if ((this.polylineFlag & 1) == 1) {
				this.polyline.closePath();
			}
		}
	}

	/**
	 * @see dxf.section.entities.EntityReader#hasFigure()
	 */
	public boolean hasFigure() {
		return this.polyline != null;
	}

	/**
	 * @see dxf.section.entities.EntityReader#makeFigure()
	 */
	public DxfEntity makeFigure() {
		return new DxfPolyline(this.polyline);
	}

	/**
	 * @see dxf.section.entities.EntityReader#readFigure(int, java.lang.String)
	 */
	public void readFigure(int groupCode, String line) {
		switch (groupCode) {
		case 0:
			if (this.reader != null) {
				DxfVertex vertex = (DxfVertex) this.reader.getEntity();
				if (this.polyline != null) {
					this.polyline.lineTo((float) vertex.getX(), (float) vertex.getY());
				} else {
					this.polyline = new GeneralPath();
					this.polyline.moveTo((float) vertex.getX(), (float) vertex.getY());
				}
			}
			if ("SEQEND".equals(line)) {
				this.close();
			}
			this.reader = this.map.get(line);
			break;
		case 70:
			if (this.reader == null) {
				this.polylineFlag = SectionReader.parseInt(line);
				break;
			}
		default:
			if (this.reader != null) {
				this.reader.readFigure(groupCode, line);
			}
			break;
		}
	}
}

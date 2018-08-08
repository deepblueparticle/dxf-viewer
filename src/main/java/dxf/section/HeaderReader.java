package dxf.section;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import dxf.DxfData;
import dxf.section.header.ParameterReader;
import dxf.section.header.PointReader;

/**
 * DXFファイルからヘッダセクションを読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class HeaderReader extends SectionReader {

	private String label;

	private final Map<String, ParameterReader> map;

	private final Map<String, Number[]> paramMap;

	private ParameterReader reader;

	public HeaderReader() {
		this.map = new HashMap<String, ParameterReader>();
		this.paramMap = new HashMap<String, Number[]>();
		PointReader pointReader = new PointReader();
		this.map.put("$EXTMIN", pointReader);
		this.map.put("$EXTMAX", pointReader);
	}

	/**
	 * @see dxf.section.SectionReader#close()
	 */
	@Override
	protected void close() {
		if (this.reader != null) {
			Number[] param = this.reader.getParameter();
			if (param != null) {
				this.paramMap.put(this.label, param);
			}
		}
	}

	@Override
	protected void linkData(DxfData dxf) {
		Number[] min = this.paramMap.get("$EXTMIN");
		Number[] max = this.paramMap.get("$EXTMAX");
		dxf.setDefaultScreen(new Rectangle(min[0].intValue(), min[1].intValue(), max[0].intValue() - min[0].intValue(), max[1].intValue() - min[1].intValue()));
		System.out.println(dxf.getDefaultScreen());
	}

	@Override
	protected void readData(int groupCode, String line) {
		switch (groupCode) {
		case 9: // 分離符号
			if (this.reader != null) {
				Number[] param = this.reader.getParameter();
				if (param != null) {
					this.paramMap.put(this.label, param);
				}
			}
			this.label = line;
			this.reader = this.map.get(line);
		default:
			if (this.reader != null) {
				this.reader.readParameter(groupCode, line);
			}
		}
	}
}

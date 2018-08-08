package dxf.section.header;

/**
 * DXFファイルからPointを読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class PointReader extends ParameterReader {
	private Number x;
	private Number y;

	@Override
	void clear() {
		this.x = null;
		this.y = null;
	}

	@Override
	boolean hasParameter() {
		return this.x != null && this.y != null;
	}

	@Override
	public Number[] makeParameter() {
		return new Number[] { this.x, this.y };
	}

	@Override
	public void readParameter(int groupCode, String line) {
		switch (groupCode) {
		case 10:
			this.x = Double.valueOf(line);
		case 20:
			this.y = Double.valueOf(line);
		}
	}
}

/**
 * 
 */
package dxf.section;

import gui.DxfViewer;

import java.util.HashMap;
import java.util.Map;

import util.MapList;

import dxf.DxfData;
import dxf.section.entities.ArcReader;
import dxf.section.entities.CircleReader;
import dxf.section.entities.DxfEntity;
import dxf.section.entities.EntityReader;
import dxf.section.entities.InsertReader;
import dxf.section.entities.LineReader;
import dxf.section.entities.MtextReader;
import dxf.section.entities.PolylineReader;
import dxf.section.entities.TextReader;

/**
 * DXFファイルからブロックセクションを読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class BlocksReader extends SectionReader {

	/**
	 * 図形のリスト
	 */
	private MapList<String, DxfEntity> blocks;

	private Map<String, EntityReader> map;

	/**
	 * ブロック名
	 */
	private String name;

	private EntityReader reader;

	private boolean subEntityFlag = false;

	public BlocksReader() {
		this.map = new HashMap<String, EntityReader>();
		this.map.put("LINE", new LineReader());
		this.map.put("CIRCLE", new CircleReader());
		this.map.put("MTEXT", new MtextReader());
		this.map.put("TEXT", new TextReader());
		this.map.put("ARC", new ArcReader());
		this.map.put("POLYLINE", new PolylineReader());
		this.map.put("INSERT", new InsertReader());
		this.blocks = new MapList<String, DxfEntity>();
	}

	/**
	 * @see dxf.section.SectionReader#close()
	 */
	@Override
	protected void close() {
		if (this.reader != null) {
			DxfEntity figure = this.reader.getEntity();
			if (figure != null) {
				this.blocks.put(this.name, figure);
				this.reader = null;
			}
		}
	}

	/**
	 * @see dxf.section.SectionReader#linkData(dxf.DxfData)
	 */
	@Override
	protected void linkData(DxfData dxf) {
		dxf.setBlocks(this.blocks);
		this.blocks.clear();
	}

	/**
	 * @see dxf.section.SectionReader#readData(int, java.lang.String)
	 */
	@Override
	protected void readData(int groupCode, String line) {
		switch (groupCode) {
		case 0: // 分離符号
			if (this.subEntityFlag) {
				if ("SEQEND".equals(line)) {
					this.subEntityFlag = false;
					break;
				}
				this.reader.readFigure(groupCode, line);
			} else {
				this.close();
				if ("BLOCK".equals(line)) {
					this.name = null;
					this.reader = null;
				} else {
					this.close();
					this.reader = this.map.get(line);
					if (DxfViewer.debug) {
						if (this.reader == null && !"ENDBLK".equals(line)) {
							System.out.println("Unknown Block Tag: " + line);
						}
					}
				}
			}
			break;
		case 2: // 名前
			if (this.name == null) {
				this.name = line;
				break;
			}
		case 66: // 後続図形フラグ
			if (this.reader != null) {
				this.subEntityFlag = SectionReader.parseBoolean(line);
			}
		default:
			if (this.reader != null) {
				this.reader.readFigure(groupCode, line);
			}
			break;
		}
	}
}

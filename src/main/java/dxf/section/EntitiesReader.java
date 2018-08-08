package dxf.section;

import gui.DxfViewer;

import java.util.HashMap;
import java.util.Map;

import util.MapList;

import dxf.DxfData;
import dxf.section.entities.ArcReader;
import dxf.section.entities.CircleReader;
import dxf.section.entities.DimensionReader;
import dxf.section.entities.DxfEntity;
import dxf.section.entities.EntityReader;
import dxf.section.entities.InsertReader;
import dxf.section.entities.LineReader;
import dxf.section.entities.MtextReader;
import dxf.section.entities.PolylineReader;
import dxf.section.entities.TextReader;

/**
 * DXFファイルからエンティティセクションを読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class EntitiesReader extends SectionReader {

	/**
	 * 図形のリスト
	 */
	private MapList<String, DxfEntity> map;

	/**
	 * 現在読み込んでいるセクションに対するReader
	 */
	private EntityReader reader;

	private Map<String, EntityReader> readerSwitcher;

	private boolean subEntityFlag;

	/**
	 * コンストラクタ 読み込むFigureReaderをMapに設定する。
	 */
	public EntitiesReader() {
		this.readerSwitcher = new HashMap<String, EntityReader>();
		this.readerSwitcher.put("LINE", new LineReader());
		this.readerSwitcher.put("CIRCLE", new CircleReader());
		this.readerSwitcher.put("MTEXT", new MtextReader());
		this.readerSwitcher.put("TEXT", new TextReader());
		this.readerSwitcher.put("INSERT", new InsertReader());
		this.readerSwitcher.put("DIMENSION", new DimensionReader());
		this.readerSwitcher.put("ARC", new ArcReader());
		this.readerSwitcher.put("POLYLINE", new PolylineReader());
		this.map = new MapList<String, DxfEntity>();
	}

	/**
	 * @see dxf.section.SectionReader#close()
	 */
	@Override
	protected void close() {
		if (this.reader != null) {
			DxfEntity entity = this.reader.getEntity();
			String name = entity.getClass().getName();
			this.map.put(name, entity);
			this.reader = null;
		}
	}

	/**
	 * DXFファイルから読み込んだリストをDXFファイルのデータに関連付ける。
	 * 
	 * @param dxf
	 *            DXFファイルのデータ
	 */
	@Override
	protected void linkData(DxfData dxf) {
		dxf.putAllEntities(this.map);
		this.map.clear();
	}

	@Override
	protected void readData(int groupCode, String line) {
		switch (groupCode) {
		case 0: // 分離符号
			if (this.subEntityFlag) {
				this.reader.readFigure(groupCode, line);
				if ("SEQEND".equals(line)) {
					this.subEntityFlag = false;
					break;
				}
			} else {
				this.close();
				this.reader = this.readerSwitcher.get(line);
				if (DxfViewer.debug) {
					if (this.reader == null) {
						System.out.println("Unknown Entity Tag: " + line);
					}
				}
			}
			break;
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

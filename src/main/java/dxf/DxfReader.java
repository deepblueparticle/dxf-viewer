package dxf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import dxf.section.BlocksReader;
import dxf.section.EntitiesReader;
import dxf.section.HeaderReader;
import dxf.section.SectionReader;

/**
 * DXF Reader
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DxfReader {

	private final static String CHARSET = "SJIS";
	private final static int TYPE_SECTION = 1;

	/**
	 * セクションリーダー
	 */
	private final Map<String, SectionReader> readerSwitcher;

	/**
	 * ファイルを指定してオブジェクトを生成します。
	 */
	public DxfReader() {
		this.readerSwitcher = new HashMap<String, SectionReader>();
		this.readerSwitcher.put("HEADER", new HeaderReader());
		this.readerSwitcher.put("BLOCKS", new BlocksReader());
		this.readerSwitcher.put("ENTITIES", new EntitiesReader());
	}

	/**
	 * DXFファイルを読み込むメソッド 分離符号により各セクションリーダへBufferedReaderを渡しています。
	 * 
	 * @param file
	 *            読み込むDXFファイル
	 * @return 読み込んだDXFファイルのデータ
	 * @throws IOException
	 *             入出力エラー
	 */
	public DxfData readDXF(File file) throws IOException {
		DxfData dxf = new DxfData();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET));
			String line1;
			String line2;
			int type = 0;
			while ((line1 = in.readLine()) != null && (line2 = in.readLine()) != null) {
				int groupCode = SectionReader.getGroupCode(line1);
				switch (groupCode) {
				case 0: // 分離符号
					if ("SECTION".equals(line2)) {
						type = DxfReader.TYPE_SECTION;
					} else if ("ENDSEC".equals(line2)) {
						type = 0;
					}
					break;
				case 2: // 名前
					if (type == DxfReader.TYPE_SECTION) {
						SectionReader reader = this.readerSwitcher.get(line2);
						if (reader != null) {
							reader.readSection(in, dxf);
						}
					}
					break;
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		dxf.optimization();
		return dxf;
	}

	/**
	 * DXFファイルを読み込むメソッド
	 * 
	 * @param path
	 *            読み込むDXFファイルのパス
	 * @return 読み込んだDXFファイルのデータ
	 * @throws IOException
	 *             入出力エラー
	 */
	public DxfData readDXF(String path) throws IOException {
		return this.readDXF(new File(path));
	}
}

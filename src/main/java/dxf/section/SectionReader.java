package dxf.section;

import java.io.BufferedReader;
import java.io.IOException;

import dxf.DxfData;

/**
 * セクションを読み込むための抽象クラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public abstract class SectionReader {

	/**
	 * グループコードを取得するメソッド
	 * 
	 * @param line
	 * @return
	 */
	public static int getGroupCode(String line) {
		return Integer.parseInt(line.substring(0, 3).replaceAll(" ", ""));
	}

	public static boolean parseBoolean(String line) {
		return line != null && SectionReader.parseInt(line) != 0;
	}

	public static int parseInt(String line) {
		int value;
		try {
			value = Integer.parseInt(line.replaceAll("\\s+", ""));
		} catch (Exception e) {
			value = 0;
		}
		return value;
	}

	/**
	 * 各セクションを閉じる前の処理
	 */
	protected abstract void close();

	/**
	 * DXFファイルから読み込んだデータを関連付けするメソッド 関連付けた後、次の読み込みに備えること必要がある。 関連付けするのは一度のみでよい。
	 * 
	 * @param dxf
	 *            DXFファイルのデータ
	 */
	protected abstract void linkData(DxfData dxf);

	/**
	 * DXFファイルからデータを読み込むためのメソッド
	 * 
	 * @param groupCode
	 *            グループコード
	 * @param line
	 *            データ
	 */
	protected abstract void readData(int groupCode, String line);

	/**
	 * 各セクションを読み込むためのメソッド
	 * 
	 * @param in
	 *            入力のリーダ
	 * @param dxf
	 *            DXFファイルのデータ
	 * @throws IOException
	 *             入出力データ
	 */
	public void readSection(BufferedReader in, DxfData dxf) throws IOException {
		String line1;
		String line2;
		int count = 0;
		try {
			while ((line1 = in.readLine()) != null && (line2 = in.readLine()) != null) {
				int groupCode = SectionReader.getGroupCode(line1);
				if (groupCode == 0 && "ENDSEC".equals(line2)) {
					this.close();
					break;
				}
				this.readData(groupCode, line2);
				count += 2;
			}
		} catch (Throwable e) {
			System.err.println("LINE: " + count);
			e.printStackTrace();
			throw new IOException();
		}
		this.linkData(dxf);
	}
}

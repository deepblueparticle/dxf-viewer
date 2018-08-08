package dxf.section.entities;

/**
 * 図形データを読み込むためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public interface EntityReader {

	/**
	 * DXFから読み込んだ図形を消去するメソッド
	 */
	public void clear();

	/**
	 * DXFから図形を読み込むためのメソッド
	 * 
	 * @return DXFファイルの図形
	 */
	public default DxfEntity getEntity() {
		DxfEntity figure = null;
		if (this.hasFigure()) {
			figure = this.makeFigure();
		} else {
			System.out.println("FAILURE TO MAKE FIGURE: " + this.getClass().getName());
		}
		this.clear();
		return figure;
	}

	/**
	 * 図形データを構成できるだけのデータを読み込めているか確認するメソッド
	 * 
	 * @return 図形データを構成できればtrue、できなければfalseを返す。
	 */
	public boolean hasFigure();

	/**
	 * 読み込んだデータから図形ファイルを構成するメソッド 必要なデータを読み込めているかなどのチェックは不要
	 * 
	 * @return DXFファイルの図形
	 */
	public DxfEntity makeFigure();

	/**
	 * DXFから図形を読み込むためのメソッド
	 * 
	 * @param groupCode
	 *            グループコード
	 * @param line
	 *            対応するデータ
	 */
	public void readFigure(int groupCode, String line);
}

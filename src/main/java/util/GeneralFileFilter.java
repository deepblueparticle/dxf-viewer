package util;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * 簡易に一般化したFileFilterクラス
 * 
 * @author ma38su
 */
public class GeneralFileFilter extends FileFilter implements java.io.FileFilter {

	/**
	 * JFileChooserにラスタ画像のファイルフィルターを追加します。 Javaで書き出せるファイルのみ許可しまするため、GIF形式は含みません
	 * 標準の拡張子はPNG形式として設定します。
	 * 
	 * @param chooser
	 *            ファイルフィルタを追加するJFileChooser
	 */
	public static void addRasterGraphicsFileFilter(JFileChooser chooser) {
		FileFilter filter = new GeneralFileFilter("png", "PNG (*.png)");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(new GeneralFileFilter(new String[] { "jpg", "jpeg" }, "JPEG (*.jpg; *.jpeg)"));
		chooser.addChoosableFileFilter(new GeneralFileFilter("bmp", "BMP (*.bmp)"));
		chooser.addChoosableFileFilter(new GeneralFileFilter("wbmp"));
		chooser.setFileFilter(filter);
	}

	/**
	 * ラスタ画像を読み込むためのファイルフィルタを取得します。 Javaで読み込める画像をすべて設定します。
	 * 
	 * @return ラスタ画像とディレクトリのみを許可するファイルフィルタ
	 */
	public static GeneralFileFilter getRasterGraphicsFileFilters() {
		return new GeneralFileFilter(new String[] { "jpg", "jpeg", "png", "bmp", "wbmp" }, "ラスタ画像 (*.jpg; *.jpeg; *.png; *.bmp; *.wbmp)");
	}

	/**
	 * FileFilterの説明
	 */
	private final String description;

	/**
	 * ディレクトリを許可するかどうか
	 */
	private boolean dir;

	/**
	 * 許可する拡張子
	 */
	private final String[] extensions;

	/**
	 * 許可する拡張子を与えてインスタンスを生成します。 許可する拡張子の最初の要素が標準の拡張子に設定されます。
	 * （JPEG,HTMLなど複数の拡張子が存在する場合）
	 * 
	 * @param extensions
	 *            許可する拡張子
	 * @param description
	 *            拡張子の説明
	 * @param dir
	 *            ディレクトリを許可するかどうか
	 */
	public GeneralFileFilter(List<String> extensions, String description, boolean dir) {
		this(extensions.toArray(new String[] {}), description, dir);
	}

	/**
	 * 許可する拡張子を与えてインスタンスを生成します。 ディレクトリは許可します。
	 * 
	 * @param extension
	 *            許可する拡張子
	 */
	public GeneralFileFilter(String extension) {
		this(new String[] { extension }, extension, true);
	}

	/**
	 * 許可する拡張子を与えてインスタンスを生成します。 ディレクトリは許可します。
	 * 
	 * @param extension
	 *            許可する拡張子
	 * @param description
	 *            拡張子の説明
	 */
	public GeneralFileFilter(String extension, String description) {
		this(new String[] { extension }, description, true);
	}

	/**
	 * 許可する拡張子を与えてインスタンスを生成します。 ディレクトリは許可します。
	 * 
	 * @param extension
	 *            許可する拡張子
	 * @param description
	 *            拡張子の説明
	 */
	public GeneralFileFilter(String extension, String description, boolean flag) {
		this(new String[] { extension }, description, flag);
	}

	/**
	 * 許可する拡張子を与えてインスタンスを生成します。 許可する拡張子の最初の要素が標準の拡張子に設定されます。
	 * （JPEG,HTMLなど複数の拡張子が存在する場合） ディレクトリは許可します。
	 * 
	 * @param extensions
	 *            許可する拡張子
	 * @param description
	 *            拡張子の説明
	 */
	public GeneralFileFilter(String[] extensions, String description) {
		this(extensions, description, true);
	}

	/**
	 * 許可する拡張子を与えてインスタンスを生成します。 許可する拡張子の最初の要素が標準の拡張子に設定されます。
	 * （JPEG,HTMLなど複数の拡張子が存在する場合）
	 * 
	 * @param extensions
	 *            許可する拡張子
	 * @param description
	 *            拡張子の説明
	 * @param dir
	 *            ディレクトリを許可するかどうか
	 */
	public GeneralFileFilter(String[] extensions, String description, boolean dir) {
		this.extensions = extensions;
		this.description = description;
		this.dir = dir;
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return this.dir;
		}
		String path = file.getPath();
		String format = path.substring(path.lastIndexOf('.') + 1, path.length());
		for (String extension : this.extensions) {
			if (extension.compareToIgnoreCase(format) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 標準の拡張子を返します
	 * 
	 * @return 標準の拡張子
	 */
	public String getDefaultExtension() {
		return this.extensions[0];
	}

	@Override
	public String getDescription() {
		return this.description;
	}
}

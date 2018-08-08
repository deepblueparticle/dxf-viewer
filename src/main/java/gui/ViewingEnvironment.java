package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * 表示関係の設定を扱うクラス
 * 
 * @author FUJIWARA Masayasu
 */
public class ViewingEnvironment extends Observable implements Cloneable {

	/**
	 * アンチエイリアスのラベル
	 */
	public static final String labelAntialiasing = "アンチエイリアス";

	/**
	 * アンチエイリアスのラベル
	 */
	public static final String labelCheckline = "チェック補助線";

	/**
	 * 寸法表示のラベル
	 */
	public static final String labelDimension = "寸法表示";
	/**
	 * エラー表示のラベル
	 */
	public static final String labelError = "エラー表示";

	/**
	 * 挿入表示のラベル
	 */
	public static final String labelInsert = "挿入表示";

	public static final String labelSelectionAlways = "選択範囲を常に表示";

	/**
	 * 表示の最大尺度
	 */
	public final static float MAX_SCALE = 1000f;

	/**
	 * 表示の最小尺度
	 */
	public final static float MIN_SCALE = 0.5f;

	/**
	 * 背景色
	 */
	private Color background = Color.WHITE;

	private Map<String, Boolean> drawFlag;

	private Color errorColor = Color.RED;

	/**
	 * フォント
	 */
	private Font font = new Font("ＭＳ ゴシック", Font.PLAIN, 8);

	/**
	 * 線種
	 */
	public Map<String, Integer> lineType = new HashMap<String, Integer>();

	/**
	 * フォント
	 */
	private Font naviFont = new Font("ＭＳ ゴシック", Font.PLAIN, 14);

	/**
	 * パラメータ
	 */
	public final Map<String, Integer> parameter;

	/**
	 * 表示倍率
	 */
	private double scale = 1;

	/**
	 * X座標
	 */
	private double x;

	/**
	 * Y座標
	 */
	private double y;

	public ViewingEnvironment() {
		this.parameter = new HashMap<String, Integer>();
		this.parameter.put(ViewingEnvironment.labelAntialiasing, 1);
		this.parameter.put(ViewingEnvironment.labelDimension, 1);
		this.parameter.put(ViewingEnvironment.labelInsert, 1);
		this.parameter.put(ViewingEnvironment.labelError, 1);
		this.parameter.put(ViewingEnvironment.labelCheckline, 1);
		this.lineType = new HashMap<String, Integer>();
		this.lineType.put("PHANTOM", 2);
		this.lineType.put("CONTINUOUS", 2);
		this.lineType.put("HIDDEN", 1);
		this.lineType.put("BYBLOCK", 0);
		this.lineType.put("CENTER", 1); // 中心線
		this.lineType.put("DASHED", 1);
		this.lineType.put("DIMENSION", 0); // 寸法線の線種を独自に設定
		this.lineType.put("INSERT", 0); // 引き出し線の線種を独自に設定
		this.lineType.put("CHECK", 3); // チェック用の線種を独自に設定
		this.lineType.put(null, 0);

		this.drawFlag = new HashMap<String, Boolean>();
		this.drawFlag.put("PHANTOM", true);
		this.drawFlag.put("CONTINUOUS", true);
		this.drawFlag.put("HIDDEN", true);
		this.drawFlag.put("BYBLOCK", true);
		this.drawFlag.put("CENTER", true); // 中心線
		this.drawFlag.put("DASHED", true);
		this.drawFlag.put("DIMENSION", true); // 寸法線の線種を独自に設定
		this.drawFlag.put("INSERT", true); // 引き出し線の線種を独自に設定
		this.drawFlag.put("CHECK", true); // チェック用の線種を独自に設定
		this.drawFlag.put(null, true);
	}

	public boolean checkLineVisuble(String style) {
		Boolean flag = this.drawFlag.get(style);
		return flag != null && flag;
	}

	/**
	 * 表示環境のクリア
	 */
	public void clear() {
		this.x = 0;
		this.y = 0;
		this.scale = 1;
	}

	@Override
	protected Object clone() {
		ViewingEnvironment env = new ViewingEnvironment();
		env.x = this.x;
		env.y = this.y;
		env.parameter.putAll(this.parameter);
		return env;
	}

	/**
	 * 背景色を取得するメソッド
	 * 
	 * @return 背景色
	 */
	public Color getBackground() {
		return this.background;
	}

	public Color getErrorColor() {
		return this.errorColor;
	}

	/**
	 * エラー表示を描画するためのストローク
	 * 
	 * @return ストローク
	 */
	public Stroke getErrorStroke() {
		return new BasicStroke((float) (2 / this.scale));
	}

	/**
	 * フォントを取得するメソッド
	 * 
	 * @return フォント
	 */
	public Font getFont() {
		return this.font;
	}

	public Stroke getNarrowDashStroke() {
		return new BasicStroke((float) (.2f / this.scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.5f, new float[] { 1, 1 }, 0f);
	}

	public Stroke getNarrowStroke() {
		return new BasicStroke((float) (.2f / this.scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	}

	/**
	 * ナビゲーションのためのフォントを返すメソッド
	 * 
	 * @return ナビゲーションのためのフォント
	 */
	public Font getNaviFont() {
		return this.naviFont;
	}

	/**
	 * パラメータを取得するメソッド
	 * 
	 * @param name
	 *            パラメータ名
	 * @return パラメータの値
	 */
	public Integer getParam(String name) {
		Integer value = this.parameter.get(name);
		return value != null ? value.intValue() : 0;
	}

	/**
	 * 表示倍率を取得するメソッド
	 * 
	 * @return 表示倍率
	 */
	public double getScale() {
		return this.scale;
	}

	public Stroke getSelectionStroke() {
		float dash = (float) (5 / this.scale);
		return new BasicStroke((float) (1f / this.scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.5f, new float[] { dash, dash }, 0);
	}

	/**
	 * ストロークを取得するメソッド
	 * 
	 * @param type
	 *            線種
	 * @return ストローク
	 */
	public Stroke getStroke(String type) {
		Boolean flag = this.drawFlag.get(type);
		if (flag == null) {
			System.out.println("Stroke Type Unsuppert: " + type);
		} else if (flag) {
			Integer stroke = this.lineType.get(type);
			if (stroke != null && stroke >= 0) {
				switch (stroke) {
				case 1:
					return new BasicStroke((float) (.5f / this.scale), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1, new float[] { 4f, 0.9f, 0.9f, 0.6f },
							2f);
				case 2:
					return new BasicStroke((float) (1.25f / this.scale));
				case 3:
					return new BasicStroke((float) (2f / this.scale));
				default:
					return new BasicStroke((float) (1f / this.scale));
				}
			}
		}
		return null;
	}

	/**
	 * 原点を表示するコンポーネント上のX座標を取得します
	 * 
	 * @return X座標
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * 原点を表示するコンポーネント上のY座標を取得します。
	 * 
	 * @return Y座標
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * 平行移動量を設定します。
	 * 
	 * @param dx
	 *            X軸方向の移動量
	 * @param dy
	 *            Y軸方向の移動量
	 */
	public void move(double dx, double dy) {
		this.x += dx;
		this.y += dy;
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * 背景色を設定するメソッド
	 * 
	 * @param color
	 *            背景色
	 */
	public void setBackground(Color color) {
		this.background = color;
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * 描画する文字のフォントを指定します。
	 * 
	 * @param font
	 */
	public void setFont(Font font) {
		this.font = font;
	}

	/**
	 * パラメータを設定するメソッド
	 * 
	 * @param name
	 *            名前
	 * @param value
	 *            値
	 */
	public void setParam(String name, int value) {
		this.parameter.put(name, value);
	}

	/**
	 * 表示倍率を設定するメソッド
	 * 
	 * @param scale
	 *            表示倍率
	 */
	public void setScale(double scale) {
		this.scale = scale;
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * X座標を設定する
	 * 
	 * @param x
	 *            X座標
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Y座標を設定する
	 * 
	 * @param y
	 *            Y座標
	 */
	public void setY(double y) {
		this.y = y;
	}

	public void switchLineVisible(String style, boolean flag) {
		this.drawFlag.put(style, flag);
	}

	/**
	 * 与えられた座標を基準として表示倍率を更新するメソッド
	 * 
	 * @param newScale
	 *            新しい表示倍率
	 * @param x
	 *            基準となるX座標
	 * @param y
	 *            基準となるY座標
	 */
	public void updateScale(double newScale, double x, double y) {
		this.x += x * (1 - newScale / this.scale);
		this.y += y * (1 - newScale / this.scale);
		this.scale = newScale;
		this.setChanged();
		this.notifyObservers();
	}
}

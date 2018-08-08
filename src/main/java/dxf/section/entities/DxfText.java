package dxf.section.entities;

import gui.ViewingEnvironment;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * DXFファイルの文字列を扱うクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.04
 */
public class DxfText implements DxfAbstText {

	/**
	 * 水平方向の配置
	 */
	private byte align;

	/**
	 * 文字高さ 実装はAscentと解釈して行っている。
	 */
	private double height;

	private boolean isError;

	/**
	 * 文字列
	 */
	private String text;

	/**
	 * 垂直方向の配置
	 */
	private byte valign;

	/**
	 * 挿入点のX座標
	 */
	private double x;

	/**
	 * 挿入点のY座標
	 */
	private double y;

	/**
	 * @param text
	 *            文字列
	 * @param x
	 *            挿入点のX座標
	 * @param y
	 *            挿入店のY座標
	 * @param height
	 *            文字高さ
	 * @param align
	 *            水平方向の文字位置あわせ
	 * @param valign
	 *            垂直方向の文字位置あわせ
	 */
	public DxfText(String text, Number x, Number y, Number height, Number align, Number valign) {
		text = text.replaceAll("\\\\U\\+00B1", "±");
		text = text.replaceAll("\\\\U\\+2205", "φ"); // 直径記号を便宜上ファイに置換
		text = text.replaceAll("\\\\U\\+00B0", "°");
		text = text.replaceAll("%%c", "φ");
		text = text.replaceAll("%%d", "°");
		text = text.replaceAll("%%p", "±");
		text = text.replaceAll("%%u", ""); // %%uは、囲った範囲に下線を引く意味を持つが実装を省略する。
		text = text.replaceAll("(:?\\\\A1;|\\{(:?\\\\L)?|(:?\\\\L)?\\})", "");
		text = text.replaceAll("（", "(");
		text = text.replaceAll("）", ")");
		if (text.contains("\\\\S")) {
			text = text.replaceAll("＋", "+");
			text = text.replaceAll("[－ー‐]", "-");
		}
		this.text = text;
		if (align != null) {
			this.align = align.byteValue();
		}
		if (valign != null) {
			this.valign = valign.byteValue();
		}
		this.x = x.doubleValue();
		this.y = y.doubleValue();
		this.height = height.doubleValue();
	}

	/**
	 * @see dxf.section.entities.DxfEntity#clearError()
	 */

	public void clearError() {
		this.isError = false;
	}

	public void draw(Graphics2D g, ViewingEnvironment env) {
		Color color = g.getColor();
		if (this.isError) {
			g.setColor(env.getErrorColor());
		}
		Font font = g.getFont();
		FontMetrics metrics = g.getFontMetrics(font);
		int size = (int) (font.getSize() * this.height / metrics.getAscent());
		Font newFont = size == font.getSize() ? font : new Font(font.getName(), font.getStyle(), size);
		g.setFont(newFont);
		metrics = g.getFontMetrics(newFont);

		float lineHeight = metrics.getHeight();
		float y = (float) this.y - metrics.getDescent();
		if (this.valign == 0) {
			y += lineHeight / 2;
		} else if (this.valign == 1) {
			y += lineHeight;
		}
		float x = (float) this.x;
		if (this.align == 1) {
			x -= metrics.stringWidth(this.text);
		}
		GlyphVector vector = newFont.createGlyphVector(g.getFontRenderContext(), this.text);
		AffineTransform matrix = new AffineTransform(1, 0, 0, -1, 0, 0);
		for (int j = 0; j < vector.getNumGlyphs(); j++) {
			vector.setGlyphTransform(j, matrix);
		}
		g.drawGlyphVector(vector, x, y);
		if (this.isError) {
			g.setColor(color);
		}
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 * @see java.awt.Shape#getBounds2D()
	 */

	public Rectangle2D getBounds2D() {
		return null;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection,
	 *      java.util.Collection)
	 */

	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {
	}

	/**
	 * @return テキスト
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * テキスト挿入位置のX座標を取得するメソッド
	 * 
	 * @return X座標
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * テキスト挿入位置のY座標を取得するメソッド
	 * 
	 * @return Y座標
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#intersects(java.awt.Rectangle)
	 */

	public boolean intersects(Rectangle2D rect) {
		return rect.contains(this.x, this.y);
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isContained(java.awt.Shape)
	 */

	public boolean isContained(Shape shape) {
		// TODO 正確に包含関係を調べたほうがいい
		return false;
	}

	public boolean isRequired() {
		return this.text.charAt(0) != '(';
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isSelectable()
	 */

	public boolean isSelectable() {
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#link(dxf.DxfData)
	 */

	public boolean link(DxfData dxf) {
		return true;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#setError()
	 */

	public void setError() {
		this.isError = true;
	}

	public String toString() {
		return this.text;
	}

	public void transform(double dx, double dy) {
		this.x += dx;
		this.y += dy;
	}
}

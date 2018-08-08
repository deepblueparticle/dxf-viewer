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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * DXFファイルの文字列を扱うクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DxfMtext implements DxfAbstText {

	private static Pattern PATTERN = Pattern.compile("(.+)\\\\S([^\\^]+)\\^\\s([^;]+);");

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
	private String[] mtext;

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
	 * @param position
	 *            アタッチされる点
	 */
	public DxfMtext(String text, Number x, Number y, Number height, Number position) {
		text = text.replaceAll("\\\\U\\+00B1", "±");
		text = text.replaceAll("\\\\U\\+2205", "φ"); // 直径記号を便宜上ファイに置換
		text = text.replaceAll("\\\\U\\+00B0", "°");
		text = text.replaceAll("(:?\\\\A1;|\\{(:?\\\\L)?|(:?\\\\L)?\\})", "");
		text = text.replaceAll("（", "(");
		text = text.replaceAll("）", ")");
		if (text.contains("\\\\S")) {
			text = text.replaceAll("＋", "+");
			text = text.replaceAll("[－ー‐]", "-");
		}
		this.mtext = text.split("\\\\P");
		this.align = -1;
		this.valign = -1;
		if (position != null) {
			this.valign = (byte) ((position.intValue() - 1) / 3 - 1);
			this.align = (byte) ((position.intValue() - 1) % 3 - 2);
			switch (position.intValue() % 3) {
			case 2:
				this.align = 0;
				break;
			case 0:
				this.align = 1;
				break;
			}
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
		float y = (float) this.y - metrics.getAscent();
		if (this.valign == 0) {
			y += lineHeight * this.mtext.length / 2;
		} else if (this.valign == 1) {
			y += lineHeight * this.mtext.length;
		}
		for (int i = 0; i < this.mtext.length; i++, y -= lineHeight) {
			Matcher match = DxfMtext.PATTERN.matcher(this.mtext[i]);
			if (match.matches()) {
				int smallSize = (int) (font.getSize() * this.height / metrics.getAscent() / 2f + 0.5f);
				Font smallFont = new Font(font.getName(), font.getStyle(), smallSize);
				FontMetrics smallMetrics = g.getFontMetrics(smallFont);

				String mainStr = match.group(1);
				String underStr = match.group(2);
				String upperStr = match.group(3);

				float x = (float) this.x;
				int underWidth = smallMetrics.stringWidth(underStr);
				int upperWidth = smallMetrics.stringWidth(upperStr);
				if (this.align == 0) {
					x -= (metrics.stringWidth(mainStr) + (upperWidth > underWidth ? upperWidth : underWidth)) / 2;
				} else if (this.align == 1) {
					x -= metrics.stringWidth(mainStr) + (upperWidth > underWidth ? upperWidth : underWidth);
				}
				AffineTransform matrix = new AffineTransform(1, 0, 0, -1, 0, 0);
				GlyphVector vector = newFont.createGlyphVector(g.getFontRenderContext(), mainStr);
				for (int j = 0; j < vector.getNumGlyphs(); j++) {
					vector.setGlyphTransform(j, matrix);
				}
				g.drawGlyphVector(vector, x, y);
				int subX = (int) (x + metrics.stringWidth(mainStr) + smallMetrics.charWidth(' '));
				int subY = (int) (y + metrics.getHeight() / 2f + 0.5f);
				vector = smallFont.createGlyphVector(g.getFontRenderContext(), underStr);
				for (int j = 0; j < vector.getNumGlyphs(); j++) {
					vector.setGlyphTransform(j, matrix);
				}
				g.drawGlyphVector(vector, subX, subY);
				vector = smallFont.createGlyphVector(g.getFontRenderContext(), upperStr);
				for (int j = 0; j < vector.getNumGlyphs(); j++) {
					vector.setGlyphTransform(j, matrix);
				}
				g.drawGlyphVector(vector, subX, y);
			} else {
				float x = (float) this.x;
				if (this.align == 0) {
					x -= metrics.stringWidth(this.mtext[i]) / 2f;
				} else if (this.align == 1) {
					x -= metrics.stringWidth(this.mtext[i]);
				}
				GlyphVector vector = newFont.createGlyphVector(g.getFontRenderContext(), this.mtext[i]);
				AffineTransform matrix = new AffineTransform(1, 0, 0, -1, 0, 0);
				for (int j = 0; j < vector.getNumGlyphs(); j++) {
					vector.setGlyphTransform(j, matrix);
				}
				g.drawGlyphVector(vector, x, y);
			}
		}
		if (this.isError) {
			g.setColor(color);
		}
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 * @see java.awt.Shape#getBounds2D()
	 */

	public Rectangle2D getBounds2D() {
		// TODO
		return null;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection,
	 *      java.util.Collection)
	 */

	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {

	}

	/**
	 * 複数行のテキストを配列で取得するメソッド
	 * 
	 * @return テキストの配列
	 */
	public String[] getMultiTexts() {
		return this.mtext;
	}

	@Override
	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (String text : this.mtext) {
			sb.append(text);
		}
		return sb.toString();
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
		return this.mtext[0].charAt(0) != '(';
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isSelectable()
	 */

	public boolean isSelectable() {
		return false;
	}

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
		StringBuilder sb = new StringBuilder();
		for (String text : this.mtext) {
			sb.append(text);
			sb.append('\n');
		}
		return sb.toString();
	}

	public void transform(double dx, double dy) {
		this.x += dx;
		this.y += dy;
	}
}

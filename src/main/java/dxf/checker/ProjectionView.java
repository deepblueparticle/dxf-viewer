/**
 * 
 */
package dxf.checker;

import gui.ViewingEnvironment;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import dxf.section.entities.DxfEntity;

/**
 * 投影図
 * 
 * @author FUJIWARA Masayasu
 * @since 0.09
 */
public class ProjectionView {

	public static int LAYOUT_X = 1;
	public static int LAYOUT_Y = 2;

	/**
	 * 投影図を包含する最小の長方形
	 */
	private Rectangle2D bounds;

	/**
	 * 投影図の構成要素
	 */
	private List<DxfEntity> entites;

	/**
	 * 投影図の種類
	 */
	private String label;

	private int layout;

	public ProjectionView() {
		this.entites = new ArrayList<DxfEntity>();
	}

	/**
	 * 図面の構成要素を追加します。
	 * 
	 * @param entity
	 *            図面の構成要素
	 */
	public void add(DxfEntity entity) {
		this.entites.add(entity);
		if (this.bounds != null) {
			this.bounds.add(entity.getBounds2D());
		} else {
			this.bounds = entity.getBounds2D();
		}
	}

	public void draw(Graphics2D g, ViewingEnvironment env) {
		if (this.bounds != null) {
			double scale = env.getScale();
			float margin = 1;
			g.draw(this.bounds);
			Font font = env.getNaviFont();
			GlyphVector vector = font.createGlyphVector(g.getFontRenderContext(), this.label);
			AffineTransform matrix = new AffineTransform(1 / scale, 0, 0, -1 / scale, 0, 0);
			for (int k = 0; k < vector.getNumGlyphs(); k++) {
				vector.setGlyphTransform(k, matrix);
			}
			float x = (float) this.bounds.getX();
			float y = (float) (this.bounds.getMaxY());
			g.drawGlyphVector(vector, x, y + margin);
		}
	}

	/**
	 * エンティティを包含する最小の長方形を取得するメソッド
	 * 
	 * @return エンティティを包含する最小の長方形
	 */
	public Rectangle2D getBounds() {
		return this.bounds;
	}

	public List<DxfEntity> getEntities() {
		return this.entites;
	}

	/**
	 * 投影図のラベルを取得するメソッド
	 * 
	 * @return 投影図のラベル
	 */
	public String getLabel() {
		return this.label;
	}

	public int getLayout() {
		return this.layout;
	}

	/**
	 * 図面の構成要素あるかどうか確認するメソッド
	 * 
	 * @return 図面の構成要素がなければtrue、あればtrueを返す。
	 */
	public boolean isEmpty() {
		return this.bounds == null;
	}

	/**
	 * 投影図の種類を設定するメソッド
	 * 
	 * @param label
	 *            投影図のラベル
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	public void setLayout(int layout) {
		this.layout = layout;
	}
}

/**
 * 
 */
package dxf.checker;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * 複数の選択範囲を管理するメソッド
 * 
 * @author FUJIWARA Masayasu
 * @since 0.04
 */
public class DxfSelections {

	/**
	 * 選択範囲
	 */
	private Collection<Area> selections;

	public DxfSelections() {
		this.selections = new ArrayList<Area>();
	}

	/**
	 * 領域を選択範囲に加えるメソッド 既存の選択範囲と重なる場合には、選択範囲を結合します。
	 * 
	 * @param rect
	 */
	public void add(Rectangle2D rect) {
		Area bound = new Area(rect);
		if (!this.selections.isEmpty()) {
			Iterator<Area> itr = this.selections.iterator();
			while (itr.hasNext()) {
				Area shape = itr.next();
				if (shape.intersects(rect)) {
					itr.remove();
					Area area = new Area(shape);
					bound.add(area);
				}
			}
		}
		this.selections.add(bound);
	}

	/**
	 * 選択範囲を削減するメソッド
	 */
	public void clear() {
		this.selections.clear();
	}

	/**
	 * 選択範囲を描画するメソッド
	 * 
	 * @param g
	 */
	public void draw(Graphics2D g) {
		for (Area bound : this.selections) {
			g.draw(bound);
		}
	}

	public Collection<Area> getSelections() {
		return this.selections;
	}

	/**
	 * 領域を選択範囲から削減するメソッド
	 * 
	 * @param rect
	 *            削減する領域
	 */
	public void remove(Rectangle2D rect) {
		if (!this.selections.isEmpty()) {
			Area bound = new Area(rect);
			Iterator<Area> itr = this.selections.iterator();
			while (itr.hasNext()) {
				Area shape = itr.next();
				if (rect.contains(shape.getBounds2D())) {
					itr.remove();
				} else if (shape.intersects(rect)) {
					shape.subtract(bound);
				}
			}
		}
	}

	public void setAll(Collection<Rectangle2D> bounds) {
		for (Rectangle2D rect : bounds) {
			this.selections.add(new Area(rect));
		}
	}
}

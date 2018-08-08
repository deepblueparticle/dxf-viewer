package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import util.ExportableComponent;
import dxf.DxfData;
import dxf.section.entities.DxfEntity;

/**
 * DXFを表示するためのコンポーネント
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DxfComponent extends ExportableComponent implements Observer {

	/**
	 * DXFファイルから読み込んだデータ
	 */
	private DxfData dxf;

	/**
	 * 表示環境設定
	 */
	private ViewingEnvironment env;

	/**
	 * 異常を検出したエンティティ
	 */
	private Collection<DxfEntity> errors;

	/**
	 * 選択範囲
	 */
	private MouseSelection selection;

	public DxfComponent(ViewingEnvironment env) {
		this.env = env;
		this.env.addObserver(this);

		this.selection = new MouseSelection();
		this.selection.addObserver(this);

		this.errors = new ArrayList<DxfEntity>();
	}

	/**
	 * 異常を検出したエンティティを追加するメソッド
	 * 
	 * @param entities
	 *            異常を検出したエンティティ
	 */
	public void addErrorEntities(Collection<DxfEntity> entities) {
		this.errors.addAll(entities);
	}

	/**
	 * 構成要素のエラーを解除するメソッド
	 */
	public void clearErrorEntities() {
		for (DxfEntity entity : this.errors) {
			entity.clearError();
		}
		this.errors.clear();
	}

	public void clearSelection() {
		this.dxf.clearBounds();
		this.clearErrorEntities();
	}

	/**
	 * 選択範囲から図面を包含する最小の外接長方形を求めるメソッド
	 * 
	 * 選択範囲に含まれる図面に対して、 その図面を包含する最小の外接長方形を求める。
	 */
	public void fixSelection() {
		this.dxf.addBounds(this.selection.getRectangle2D());
		this.selection.clear();
	}

	public DxfData getDXF() {
		return this.dxf;
	}

	/**
	 * マウスのX座標を図面上のX座標に変換するメソッド
	 * 
	 * @param mouseX
	 *            マウスのX座標
	 * @return 図面上のX座標
	 */
	public double getDxfX(int mouseX) {
		return (mouseX - this.env.getX()) / this.env.getScale();
	}

	/**
	 * マウスのY座標を図面上のY座標に変換するメソッド
	 * 
	 * @param mouseY
	 *            マウスのY座標
	 * @return 図面上のY座標
	 */
	public double getDxfY(int mouseY) {
		return (this.getHeight() + this.env.getY() - mouseY) / this.env.getScale();
	}

	protected void paintComponent(Graphics2D g) {
		this.paintComponent(g, this.env, this.getWidth(), this.getHeight());
	}

	protected void paintComponent(Graphics2D g, ViewingEnvironment env, int width, int height) {
		g.setFont(env.getFont());
		g.setColor(env.getBackground());
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);

		double scale = env.getScale();
		if (env.getParam(ViewingEnvironment.labelAntialiasing) > 0) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setStroke(new BasicStroke((float) (.5f / scale)));
		g.setTransform(new AffineTransform(scale, 0f, 0f, -scale, env.getX(), env.getY() + height));

		Stroke stroke = g.getStroke();
		if (this.dxf != null) {

			this.dxf.drawEntities(g, env);

			if (env.getParam(ViewingEnvironment.labelError) == 1) {
				Color color = g.getColor();
				g.setColor(Color.BLUE);
				g.setStroke(env.getErrorStroke());
				for (DxfEntity entity : this.errors) {
					if (entity != null) {
						entity.draw(g, env);
					}
				}
				g.setStroke(stroke);
				g.setColor(color);
			}
			if (env.getParam(ViewingEnvironment.labelCheckline) == 1) {
				this.dxf.drawCheckAssist(g, env);
			}

			g.setStroke(env.getSelectionStroke());
			if (this.selection.isSelected() || env.getParam(ViewingEnvironment.labelSelectionAlways) != 0) {
				this.selection.draw(g);
				this.dxf.drawSelections(g);
			}
			g.setColor(Color.RED);

			this.dxf.drawBounds(g, env);
		}

		g.setStroke(stroke);
		g.setColor(Color.BLACK);
	}

	/**
	 * @see util.ExportableComponent#printComponent(java.awt.Graphics2D, double,
	 *      double)
	 */

	protected void printComponent(Graphics2D g, int x, int y, int width, int height) {
		ViewingEnvironment env = (ViewingEnvironment) this.env.clone();
		Rectangle rect = this.dxf.getDefaultScreen();
		double scaleX = (double) width / rect.width;
		double scaleY = (double) height / rect.height;
		if (scaleY > scaleX) {
			env.setScale(scaleX * 0.95);
		} else {
			env.setScale(scaleY * 0.95);
		}
		env.setX(x);
		env.setY(-y);
		this.paintComponent(g, env, width, height);
	}

	/**
	 * 選択範囲に含まれている外接長方形から、 選択範囲に含まれるエンティティを除外して、 再度、外接長方形を求める。
	 */
	public void removeSelection() {
		this.dxf.removeBounds(this.selection.getRectangle2D());
		this.selection.clear();
	}

	/**
	 * 選択範囲を作成するメソッド
	 * 
	 * @param mouseX
	 *            基準となるX座標（マウス座標）
	 * @param mouseY
	 *            基準となるY座標（マウス座標）
	 */
	public void selectionMake(int mouseX, int mouseY) {
		double x = this.getDxfX(mouseX);
		double y = this.getDxfY(mouseY);
		this.selection.make(x, y);
	}

	/**
	 * DXFファイルから読み込んだデータを設定するメソッド。
	 * 
	 * @param dxf
	 *            DXFファイルから読み込んだデータ
	 */
	public void set(DxfData dxf) {
		this.errors.clear();
		this.env.clear();
		this.dxf = dxf;
		this.setDefaultView();
	}

	/**
	 * 初期表示の状態にするためのメソッド
	 */
	public void setDefaultView() {
		Rectangle rect = this.dxf.getDefaultScreen();
		if (rect != null) {
			this.env.setX(rect.x);
			this.env.setY(rect.y);
			if (this.getHeight() * rect.width > this.getWidth() * rect.height) {
				this.env.setScale(this.getWidth() / rect.width);
			} else {
				this.env.setScale(this.getHeight() / rect.height);
			}
		}
	}

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */

	public void update(Observable o, Object arg) {
		this.repaint();
	}

	public void updatePosition(int mouseDx, int mouseDy) {
		this.env.move(mouseDx, mouseDy);
	}

	/**
	 * 表示倍率を更新するメソッド
	 * 
	 * @param ratio
	 *            新しい表示倍率
	 * @param mouseX
	 *            基準とするマウスのX座標
	 * @param mouseY
	 *            基準とするマウスのY座標
	 */
	public void updateScale(double ratio, int mouseX, int mouseY) {
		double scale = this.env.getScale();
		double newScale = scale * ratio;
		if (newScale > ViewingEnvironment.MAX_SCALE) {
			newScale = ViewingEnvironment.MAX_SCALE;
		} else if (newScale < ViewingEnvironment.MIN_SCALE) {
			newScale = ViewingEnvironment.MIN_SCALE;
		}
		double x = mouseX - this.env.getX();
		double y = mouseY - this.getHeight() - this.env.getY();
		this.env.updateScale(newScale, x, y);
	}

	/**
	 * 選択範囲を更新するメソッド
	 * 
	 * @param mouseX
	 *            選択範囲の端点となるマウスのX座標
	 * @param mouseY
	 *            選択範囲の端点となるマウスのY座標
	 */
	public void updateSelection(int mouseX, int mouseY) {
		double x = this.getDxfX(mouseX);
		double y = this.getDxfY(mouseY);
		this.selection.update(x, y);
	}
}

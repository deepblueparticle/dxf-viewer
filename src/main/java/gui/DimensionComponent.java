package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;

import dxf.section.entities.DxfCircle;
import dxf.section.entities.DxfDimension;
import dxf.section.entities.DxfEntity;

import util.ExportableComponent;
import util.RoundNumber2D;

/**
 * DXFを表示するためのコンポーネント
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DimensionComponent extends ExportableComponent implements Observer {

	private DxfEntity entity;

	/**
	 * 表示環境設定
	 */
	private ViewingEnvironment env;

	public DimensionComponent(ViewingEnvironment env) {
		this.env = env;
		this.env.addObserver(this);
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
		if (this.entity != null) {
			this.paintComponent(g, this.env, this.getWidth(), this.getHeight());
		}
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

		this.entity.draw(g, env);
		if (this.entity instanceof DxfDimension) {
			DxfDimension dimension = (DxfDimension) this.entity;
			RoundNumber2D pt = dimension.getIndicated();
			if (pt != null) {
				System.out.println("OK");
				g.setColor(Color.RED);
				DxfCircle circle = new DxfCircle(15 / env.getScale(), pt.getX(), pt.getY(), "CHECK");
				circle.draw(g, env);
				circle = new DxfCircle(10 / env.getScale(), pt.getX(), pt.getY(), "CHECK");
				circle.draw(g, env);
			}
		}
	}

	/**
	 * @see util.ExportableComponent#printComponent(java.awt.Graphics2D, double,
	 *      double)
	 */
	protected void printComponent(Graphics2D g, int x, int y, int width, int height) {
		ViewingEnvironment env = (ViewingEnvironment) this.env.clone();
		Rectangle rect = this.getBounds(); // default screen
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
	 * DXFファイルから読み込んだデータを設定するメソッド。
	 * 
	 * @param entity
	 *            表示するエンティティ
	 */
	public void set(DxfEntity entity) {
		this.env.clear();
		this.entity = entity;
		this.setDefaultView();
	}

	/**
	 * @param entity
	 *            エンティティ
	 */
	public void setDefaultView() {
		Rectangle2D rect = this.entity.getBounds2D();
		if (rect != null) {
			if (this.getHeight() * rect.getWidth() > this.getWidth() * rect.getHeight()) {
				this.env.setScale(this.getWidth() / rect.getWidth() * 0.8);
			} else {
				this.env.setScale(this.getHeight() / rect.getHeight() * 0.8);
			}
			this.env.setX(-rect.getX() * this.env.getScale() + this.getWidth() * 0.1);
			this.env.setY(rect.getY() * this.env.getScale() - this.getHeight() * 0.1);
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
}

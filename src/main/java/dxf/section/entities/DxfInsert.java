package dxf.section.entities;

import gui.DxfViewer;
import gui.ViewingEnvironment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Counter;
import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * ブロックセクションのデータを挿入するためのクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DxfInsert extends DxfEntities {

	private static Pattern arcPattern = Pattern.compile("^(?:(?:(\\d+)×)?(\\d+)-)?(?:M|Ｍ|R|Ｒ|φ)(:?(\\d+(?:\\.\\d+)?)|[^\\d]+|最小)");

	private static Pattern chamferPattern = Pattern.compile("^(?:(\\d+)-)?(C|Ｃ)(\\d+)");

	private static Pattern circlePattern = Pattern.compile("^(?:(?:(\\d+)×)?(\\d+)-)?(\\d+(?:\\.\\d+)?)キリ");
	public static final int TYPE_ARC = 1;
	public static int TYPE_CHAMFER = 1;
	public static final int TYPE_CIRCLE = 2;
	public int amount;

	/**
	 * ブロック名
	 */
	private String block;

	private Collection<DxfEntity> entities;

	/**
	 * 挿入が指し示す位置
	 */
	private Point2D indicated;

	private boolean isError;

	private double size;

	/**
	 * テキスト
	 */
	private String text;

	/**
	 * 引き出し線の種類
	 */
	public int type;

	/**
	 * X座標
	 */
	private double x;

	/**
	 * Y座標
	 */
	private double y;

	/**
	 * @param block
	 *            ブロック名
	 * @param x
	 *            X座標
	 * @param y
	 *            Y座標
	 */
	public DxfInsert(String block, Number x, Number y) {
		this.block = block;
		this.x = x.doubleValue();
		this.y = y.doubleValue();
	}

	protected void addEntity(DxfEntity entity) {
		this.entities.add(entity);
	}

	private boolean checkArcMeta(String str) {
		Matcher matcher = arcPattern.matcher(str);
		if (matcher.find()) {
			if (this.amount == 0) {
				this.amount = 1;
			}
			this.type |= (TYPE_ARC | TYPE_CIRCLE);
			System.out.println("arc match: " + str);
			for (int i = 1; i <= 2; i++) {
				String match = matcher.group(i);
				if (match != null) {
					this.amount *= Integer.parseInt(match);
				}
			}
			this.size = Double.parseDouble(matcher.group(3));
			return true;
		}
		return false;
	}

	private boolean checkChamferMeta(String str) {
		Matcher matcher = chamferPattern.matcher(str);
		if (matcher.matches()) {
			this.type = TYPE_CHAMFER;
			String match = matcher.group(1);
			if (match != null) {
				this.amount = Integer.parseInt(match);
			} else {
				this.amount = 1;
			}
			this.size = Double.parseDouble(matcher.group(3));
			return true;
		}
		return false;
	}

	private boolean checkCircleMeta(String str) {
		Matcher matcher = circlePattern.matcher(str);
		if (matcher.find()) {
			if (this.amount == 0) {
				this.amount = 1;
			}
			System.out.println("circle match: " + str);
			this.type |= TYPE_CIRCLE;
			for (int i = 1; i <= 2; i++) {
				String match = matcher.group(i);
				if (match != null) {
					this.amount *= Integer.parseInt(match);
				}
			}
			this.size = Double.parseDouble(matcher.group(3));
			return true;
		}
		return false;
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
		if (this.entities != null) {
			for (DxfEntity entity : this.entities) {
				entity.draw(g, env);
			}
			if (DxfViewer.debug && this.indicated != null) {
				double size = 5 / env.getScale();
				g.draw(new Ellipse2D.Double(this.indicated.getX() - size, this.indicated.getY() - size, size * 2, size * 2));
			}
		}
		if (this.isError) {
			g.setColor(color);
		}
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 */
	public Rectangle2D getBounds2D() {
		Rectangle2D bounds = null;
		for (DxfEntity entity : this.entities) {
			Rectangle2D rect = entity.getBounds2D();
			if (rect != null) {
				if (bounds == null) {
					bounds = (Rectangle2D) rect.clone();
				} else {
					bounds.add(rect);
				}
			}
		}
		return bounds;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection,
	 *      java.util.Collection)
	 */
	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {
	}

	protected Collection<DxfEntity> getEntities() {
		return this.entities;
	}

	/**
	 * 引き出し線の示す位置を返すメソッド ねじに関係ありそうなところをピックアップ
	 * 
	 * @return 引き出し線の示す位置
	 */
	public Point2D getIndicatedPoint() {
		return this.indicated;
	}

	/**
	 * 描画する文字列を取得するメソッド
	 * 
	 * @return 文字列
	 */
	public String getText() {
		return this.text;
	}

	public int getType() {
		return this.type;
	}

	public int getTypeAmount() {
		return this.amount;
	}

	public double getTypeSize() {
		return this.size;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#intersects(java.awt.Rectangle)
	 */
	public boolean intersects(Rectangle2D rect) {
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isContained(java.awt.geom.Rectangle2D)
	 */
	public boolean isContained(Shape shape) {
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isSelectable()
	 */
	public boolean isSelectable() {
		return false;
	}

	public boolean link(DxfData dxf) {
		this.entities = dxf.getBlock(this.block);
		assert this.entities != null : "BLOCK: " + this.block;
		Map<Point2D, Counter> map = new HashMap<Point2D, Counter>();
		StringBuilder sb = new StringBuilder();
		for (DxfEntity entity : this.entities) {
			entity.transform(this.x, this.y);
			entity.link(dxf);
			if (entity instanceof DxfMtext) {
				DxfMtext mtext = (DxfMtext) entity;
				for (String str : mtext.getMultiTexts()) {
					sb.append(str);
					this.checkChamferMeta(str);
					this.checkArcMeta(str);
					this.checkCircleMeta(str);
				}
			} else if (entity instanceof DxfText) {
				DxfText text = (DxfText) entity;
				sb.append(text.getText());
				this.checkChamferMeta(text.getText());
				this.checkArcMeta(text.getText());
				this.checkCircleMeta(text.getText());
			} else if (entity instanceof DxfLine) {
				DxfLine line = (DxfLine) entity;
				line.setStyle("INSERT");
				Point2D.Double p1 = new Point2D.Double(line.getX1(), line.getY1());
				Counter counter = map.get(p1);
				if (counter == null) {
					counter = new Counter();
					map.put(p1, counter);
				}
				counter.up();
				Point2D.Double p2 = new Point2D.Double(line.getX2(), line.getY2());
				counter = map.get(p2);
				if (counter == null) {
					counter = new Counter();
					map.put(p2, counter);
				}
				counter.up();
			}
		}
		this.text = sb.toString();
		int maxCount = 0;
		for (Map.Entry<Point2D, Counter> entry : map.entrySet()) {
			Counter counter = entry.getValue();
			int count = counter.getCount();
			if (maxCount < count && count >= 3) {
				maxCount = count;
				this.indicated = entry.getKey();
			}
		}
		return true;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#setError()
	 */
	public void setError() {
		this.isError = true;
	}

	public void transform(double dx, double dy) {
		this.x += dx;
		this.y += dy;
	}
}

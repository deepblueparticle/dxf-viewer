package dxf.section.entities;

import gui.DxfViewer;
import gui.ViewingEnvironment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Counter;
import util.RoundNumber;
import util.RoundNumber2D;
import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * 寸法を描画するためのクラス
 * 内部に保持する寸法線、寸法のインスタンスによって描画を行う。
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DxfDimension extends DxfEntities {

	/**
	 * 寸法を取得するためのパターン
	 */
	private static final Pattern arcPattern = Pattern.compile("^(R|\\(?約?Ｒ(\\d+)?\\)?)(\\d+|[^\\d]+|最小)?");

	private static final Pattern[] circlePattern = new Pattern[] { Pattern.compile("(?:(\\d+)-)?φ(\\d+(?:\\.\\d+)?)"),
			Pattern.compile("(?:(\\d+)-)?(\\d+(?:\\.\\d+)?)キリ") };

	private static final Pattern scalePattern = Pattern.compile("^(\\d\\.)?\\d+[^°]?$");

	public static final int TYPE_ARC = 1;
	public static final int TYPE_CIRCLE = 2;

	private int amount;

	/**
	 * 対応するブロック名
	 */
	private String block;

	/**
	 * 寸法に対応する直線
	 */
	private DxfLine dimension;

	/**
	 * 寸法線を指し示す直線 一応，this.dimensionと垂直になる線（this.dimensionが円弧だったりする可能性があるよね？）
	 */
	private List<DxfLine> dimensionAssist;

	private Collection<DxfEntity> entities;

	private RoundNumber2D indicated;

	private boolean isError;

	private Pattern numericPattern = Pattern.compile("\\(?(\\d+(?:\\.\\d)?)\\)?");

	private double scale;

	private double size;

	/**
	 * 寸法のテキスト
	 */
	private DxfAbstText text;

	private int type = 0;

	/** X座標 */
	private double x;

	/** Y座標 */
	private double y;

	public DxfDimension(String block, Number x, Number y) {
		this.block = block;
		this.x = x.doubleValue();
		this.y = y.doubleValue();
		this.dimensionAssist = new ArrayList<DxfLine>();
	}

	private boolean checkArcMeta(String str) {
		Matcher matcher = arcPattern.matcher(str);
		if (matcher.find()) {
			this.type |= TYPE_ARC;
			return true;
		}
		return false;
	}

	public DxfCircle[] checkCircle(Collection<DxfCircle> circles) {
		DxfCircle[] result = null;
		if (!this.dimensionAssist.isEmpty()) {
			result = new DxfCircle[this.dimensionAssist.size()];
			double[] table = new double[this.dimensionAssist.size()];
			for (int i = 0; i < table.length; i++) {
				table[i] = Double.POSITIVE_INFINITY;
			}
			for (DxfCircle c : circles) {
				for (int i = 0; i < this.dimensionAssist.size(); i++) {
					DxfLine l = this.dimensionAssist.get(i);
					double dist = l.lineDistSq(c);
					if (table[i] > dist) {
						table[i] = dist;
						result[i] = c;
					}
				}
			}
		}
		return result;
	}

	private boolean checkCircleMeta(String str) {
		for (Pattern ptr : circlePattern) {
			Matcher matcher = ptr.matcher(str);
			if (matcher.find()) {
				if (this.amount == 0) {
					this.amount = 1;
				}
				this.type |= (TYPE_ARC | TYPE_CIRCLE);
				String match = matcher.group(1);
				if (match != null) {
					this.amount *= Integer.parseInt(match);
				}
				this.size = Double.parseDouble(matcher.group(2));
				return true;
			}
		}
		return false;
	}

	private boolean checkNumeric(String str, double scale) {
		Matcher matcher = numericPattern.matcher(str);
		if (matcher.matches()) {
			double size = Double.parseDouble(matcher.group(1));
			System.out.println("size: " + size);
			for (DxfEntity entity : this.entities) {
				if (entity instanceof DxfLine) {
					DxfLine line = (DxfLine) entity;
					if (line.getDirection() == DxfLine.DIRECTION_X) {
						if (RoundNumber.nearlyEquals(Math.abs(line.getX1() - this.indicated.getX()) * scale, size)) {
							this.dimension = new DxfLine(line.getX1(), line.getY1(), this.indicated.getX(), this.indicated.getY(), "CHECK");
						} else if (RoundNumber.nearlyEquals(Math.abs(line.getX2() - this.indicated.getX()) * scale, size)) {
							this.dimension = new DxfLine(line.getX2(), line.getY2(), this.indicated.getX(), this.indicated.getY(), "CHECK");
						}
					}
					if (line.getDirection() == DxfLine.DIRECTION_Y) {
						if (RoundNumber.nearlyEquals(Math.abs(line.getY1() - this.indicated.getY() * scale), size)) {
							this.dimension = new DxfLine(line.getX1(), line.getY1(), this.indicated.getX(), this.indicated.getY(), "CHECK");
						} else if (RoundNumber.nearlyEquals(Math.abs(line.getY2() - this.indicated.getY() * scale), size)) {
							this.dimension = new DxfLine(line.getX2(), line.getY2(), this.indicated.getX(), this.indicated.getY(), "CHECK");
						}
					}
				}
			}
			System.out.println("dim: " + (this.dimension != null));
			return this.dimension != null;
		}
		return false;
	}

	private boolean checkScaleMeta(String str) {
		Matcher matcher = scalePattern.matcher(str);
		if (matcher.find()) {
			double value = Double.parseDouble(matcher.group());
			this.scale = Math.round(value / this.dimension.length() * 1000000) / 1000000.0;
			return true;
		}
		this.scale = Double.NaN;
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
		}
		if (this.isError) {
			g.setColor(color);
		}
		if (DxfViewer.debug && this.indicated != null) {
			double size = 5 / env.getScale();
			g.draw(new Ellipse2D.Double(this.indicated.getX() - size, this.indicated.getY() - size, size * 2, size * 2));
		}
		if (DxfViewer.debug) {
			if (this.dimension != null) {
				g.setColor(Color.BLUE);
				int radius = 3;
				if (this.dimension.getDirection() == DxfLine.DIRECTION_X) {
					g.draw(new Ellipse2D.Double(this.dimension.getX1() - radius / 2D, this.dimension.getY1() - radius, radius, radius * 2));
					g.draw(new Ellipse2D.Double(this.dimension.getX2() - radius / 2D, this.dimension.getY2() - radius, radius, radius * 2));
				} else {
					g.draw(new Ellipse2D.Double(this.dimension.getX1() - radius, this.dimension.getY1() - radius / 2D, radius * 2, radius));
					g.draw(new Ellipse2D.Double(this.dimension.getX2() - radius, this.dimension.getY2() - radius / 2D, radius * 2, radius));
				}
				this.dimension.draw(g, env);
				g.setColor(color);
			}
			if (!this.dimensionAssist.isEmpty()) {
				g.setColor(Color.RED);
				for (DxfLine l : this.dimensionAssist) {
					l.draw(g, env);
				}
				g.setColor(color);
			}
		}

	}

	/**
	 * @see dxf.section.entities.DxfEntity#getBounds2D()
	 * @see java.awt.Shape#getBounds2D()
	 */
	public Rectangle2D getBounds2D() {
		Rectangle2D bounds = null;
		for (DxfEntity entity : this.entities) {
			Rectangle2D rect = entity.getBounds2D();
			if (rect != null) {
				if (bounds == null) {
					bounds = new Rectangle2D.Double(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
				} else {
					bounds.add(rect);
				}
			}
		}
		return bounds;
	}

	public double getCenterX() {
		return (this.dimension.getX1() + this.dimension.getX2()) / 2;
	}

	public double getCenterY() {
		return (this.dimension.getY1() + this.dimension.getY2()) / 2;
	}

	/**
	 * 寸法線の端点を誤差を丸めて取得する
	 * 
	 * @return 寸法線の端点
	 */
	public RoundNumber getCheckPoint1() {
		RoundNumber node = null;
		if (this.dimension != null) {
			if (this.dimension.getDirection() == DxfLine.DIRECTION_X) {
				node = new RoundNumber(this.dimension.getX1());
			} else if (this.dimension.getDirection() == DxfLine.DIRECTION_Y) {
				node = new RoundNumber(this.dimension.getY1());
			}
		}
		return node;
	}

	/**
	 * 寸法線の端点を誤差を丸めて取得する
	 * 
	 * @return 寸法線の端点
	 */
	public RoundNumber getCheckPoint2() {
		RoundNumber node = null;
		if (this.dimension != null) {
			if (this.dimension.getDirection() == DxfLine.DIRECTION_X) {
				node = new RoundNumber(this.dimension.getX2());
			} else if (this.dimension.getDirection() == DxfLine.DIRECTION_Y) {
				node = new RoundNumber(this.dimension.getY2());
			}
		}
		return node;
	}

	public RoundNumber getCheckPointCenter() {
		RoundNumber node = null;
		if (this.dimension != null) {
			if (this.dimension.getDirection() == DxfLine.DIRECTION_X) {
				node = new RoundNumber((this.dimension.getX1() + this.dimension.getX2()) / 2);
			} else if (this.dimension.getDirection() == DxfLine.DIRECTION_Y) {
				node = new RoundNumber((this.dimension.getY1() + this.dimension.getY2()) / 2);
			}
		}
		return node;
	}

	public List<RoundNumber> getCheckPoints() {
		List<RoundNumber> list = new ArrayList<RoundNumber>();
		if (this.dimension != null) {
			if (this.dimension.getDirection() == DxfLine.DIRECTION_X) {
				for (DxfLine l : this.dimensionAssist) {
					RoundNumber node = new RoundNumber(l.getX1());
					list.add(node);
				}
			} else if (this.dimension.getDirection() == DxfLine.DIRECTION_Y) {
				for (DxfLine l : this.dimensionAssist) {
					RoundNumber node = new RoundNumber(l.getY1());
					list.add(node);
				}
			}
		}
		return list;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#getCheckPoints(java.util.Collection,
	 *      java.util.Collection)
	 */
	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY) {
	}

	public DxfEntity getDebug() {
		return this.dimension;
	}

	/**
	 * 寸法線の直線データを取得するメソッド
	 * 
	 * @return Line2D
	 */
	private DxfLine getDimension() {
		if (this.text.isRequired() && this.entities != null) {
			Map<RoundNumber2D, Counter> map = new HashMap<RoundNumber2D, Counter>();
			RoundNumber2D p1 = null;
			RoundNumber2D p2 = null;
			for (DxfEntity entity : this.entities) {
				if (entity instanceof DxfLine) {
					DxfLine line = (DxfLine) entity;
					line.setStyle("DIMENSION");
					p1 = new RoundNumber2D(line.getX1(), line.getY1());
					Counter counter = map.get(p1);
					if (counter == null) {
						counter = new Counter();
						map.put(p1, counter);
					}
					counter.up();
					p2 = new RoundNumber2D(line.getX2(), line.getY2());
					counter = map.get(p2);
					if (counter == null) {
						counter = new Counter();
						map.put(p2, counter);
					}
					counter.up();
				}
			}
			p1 = null;
			p2 = null;
			int c1 = 1;
			int c2 = 1;
			for (Map.Entry<RoundNumber2D, Counter> entry : map.entrySet()) {
				Counter counter = entry.getValue();
				int count = counter.getCount();
				if (c1 < count) {
					c2 = c1;
					p2 = p1;
					c1 = count;
					p1 = entry.getKey();
				} else if (c2 < count) {
					c2 = count;
					p2 = entry.getKey();
				}
			}
			if (p1 != null && p2 != null) {
				return new DxfLine(p1, p2);
			} else if (p1 != null) {
				this.indicated = p1;
			}
		}
		return null;
	}

	public List<DxfLine> getDimensionAssist() {
		return this.dimensionAssist;
	}

	public int getDirection() {
		return this.dimension.getDirection();
	}

	protected Collection<DxfEntity> getEntities() {
		return this.entities;
	}

	/**
	 * 引き出し線の示す位置を返すメソッド ねじに関係ありそうなところをピックアップ
	 * 
	 * @return 引き出し線の示す位置
	 */
	public RoundNumber2D getIndicated() {
		return this.indicated;
	}

	public double getScale() {
		return this.scale;
	}

	public double getSize() {
		return this.size;
	}

	public String getText() {
		return this.text.getText();
	}

	public int getType() {
		return this.type;
	}

	public int getTypeAmount() {
		return this.amount;
	}

	public boolean hasDimension() {
		return this.dimension != null;
	}

	public boolean hasIndicated() {
		return this.indicated != null;
	}

	public boolean hasScale() {
		return this.scale < Double.POSITIVE_INFINITY;
	}

	public boolean hasText() {
		for (DxfEntity entity : this.entities) {
			if (entity instanceof DxfText || entity instanceof DxfMtext) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#intersects(java.awt.Rectangle)
	 */
	public boolean intersects(Rectangle2D rect) {
		return false;
	}

	/**
	 * @see dxf.section.entities.DxfEntity#isContained(java.awt.Shape)
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

	public double length() {
		return this.dimension != null ? this.dimension.length() : 0;
	}

	public boolean link(DxfData dxf) {
		this.entities = dxf.getBlock(this.block);
		if (this.entities == null) {
			throw new RuntimeException("BLOCK: " + this.block);
		}
		for (DxfEntity entity : this.entities) {
			if (entity instanceof DxfMtext) {
				DxfMtext mtext = (DxfMtext) entity;
				// TODO 複数のTEXTが存在することがある
				//assert this.text == null;
				this.text = mtext;
				for (String str : mtext.getMultiTexts()) {
					this.checkCircleMeta(str);
					this.checkArcMeta(str);
				}
			} else if (entity instanceof DxfText) {
				DxfText text = (DxfText) entity;
				// TODO 複数のTEXTが存在することがある
				this.text = text;
				this.checkCircleMeta(text.getText());
				this.checkArcMeta(text.getText());
			}
		}
		this.dimension = this.getDimension();
		setDimensionAssist();
		int count = 0;
		// 円弧を含む寸法線の処理
		for (DxfEntity entity : this.entities) {
			if (entity instanceof DxfArc) {
				DxfArc arc = (DxfArc) entity;
				for (DxfEntity e2 : this.entities) {
					if (e2 instanceof DxfLine) {
						DxfLine line = (DxfLine) e2;
						double distSq = line.ptLineDistSq(arc.getX(), arc.getY());
						if (RoundNumber.nearlyEquals(distSq, 0)) {
							this.dimensionAssist.add(line);
						}
					}
				}
				assert count++ < 1; // 円弧が2つ以上含まれているのは想定外
				this.indicated = new RoundNumber2D(arc.getX(), arc.getY());
			}
		}
		if (this.text != null && this.dimension != null) {
			if (this.text instanceof DxfMtext) {
				DxfMtext mtext = (DxfMtext) this.text;
				for (String str : mtext.getMultiTexts()) {
					this.checkScaleMeta(str);
				}
			} else if (this.text instanceof DxfText) {
				DxfText text = (DxfText) this.text;
				this.checkScaleMeta(text.getText());
			}
		}
		return this.dimension != null;
	}

	/**
	 * スケールがないとデータを用意できない場合
	 * 
	 * @param dxf
	 * @param scale
	 */
	public void link(DxfData dxf, double scale) {
		this.entities = dxf.getBlock(this.block);
		if (this.entities == null) {
			throw new RuntimeException("BLOCK: " + this.block);
		}
		if (this.text instanceof DxfText) {
			DxfText txt = (DxfText) this.text;
			this.checkNumeric(txt.getText(), scale);
		} else if (this.text instanceof DxfMtext) {
			DxfMtext mtxt = (DxfMtext) this.text;
			for (String str : mtxt.getMultiTexts()) {
				if (this.checkNumeric(str, scale)) {
					break;
				}
			}
		}
		setDimensionAssist();
	}

	private void setDimensionAssist() {
		if (this.dimension != null) {
			for (DxfEntity entity : this.entities) {
				if (entity instanceof DxfLine) {
					DxfLine line = (DxfLine) entity;
					double inner = this.dimension.innerProduct(line);
					if (RoundNumber.nearlyEquals(0, inner)) {
						this.dimensionAssist.add(line);
					}
				}
			}
		}
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
		if (this.dimension != null) {
			this.dimension.transform(dx, dy);
		}
		for (DxfEntity entity : this.entities) {
			entity.transform(dx, dy);
		}
	}
}

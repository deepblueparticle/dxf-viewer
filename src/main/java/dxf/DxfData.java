package dxf;

import gui.MouseSelection;
import gui.ViewingEnvironment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Counter;
import util.MapList;
import util.NodePair;
import util.RoundNumber;

import dxf.checker.DimensionNode;
import dxf.checker.DxfSelections;
import dxf.checker.ProjectionView;
import dxf.section.entities.DxfEntities;
import dxf.section.entities.DxfAbstText;
import dxf.section.entities.DxfArc;
import dxf.section.entities.DxfCircle;
import dxf.section.entities.DxfDimension;
import dxf.section.entities.DxfInsert;
import dxf.section.entities.DxfLine;
import dxf.section.entities.DxfEntity;
import dxf.section.entities.DxfMtext;
import dxf.section.entities.DxfText;

/**
 * DXFファイルのデータを扱うクラス 描画のための変数も保持します。
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public class DxfData {

	/**
	 * 正面図のラベル
	 */
	public static final String LABEL_BACK_VIEW = "背面図";

	/**
	 * 
	 */
	public static final String LABEL_BOTTOM_VIEW = "下面図";

	/**
	 * 正面図のラベル
	 */
	public static final String LABEL_FRONT_VIEW = "正面図";

	/**
	 * 左側面図のラベル
	 */
	public static final String LABEL_LEFT_SIDE_VIEW = "左側面図";

	/**
	 * 
	 */
	public static final String LABEL_PLANE_VIEW = "平面図";

	/**
	 * 右側面図のラベル
	 */
	public static final String LABEL_RIGHT_SIDE_VIEW = "右側面図";

	private static final int SPLIT_PROJECTION = 50;

	/**
	 * ブロックセクションのデータ
	 */
	private MapList<String, DxfEntity> blocks;

	private boolean[][] edgesX;

	private boolean[][] edgesY;

	/**
	 * DXFファイルの図形リスト
	 */
	private MapList<String, DxfEntity> entityMap;

	/**
	 * 異常を検出した寸法など
	 */
	private Collection<DxfEntity> error;

	private List<DxfLine> lines;

	private Set<NodePair> loaX;

	private Set<NodePair> loaY;

	private List<DimensionNode> nodesX;

	private List<DimensionNode> nodesY;

	private double scale;

	/**
	 * 表示範囲またはオブジェクト範囲
	 */
	private Rectangle screen;

	/**
	 * 図面を包含する選択範囲
	 */
	private DxfSelections selections;

	private Map<String, ProjectionView> views;

	private boolean[] vpX;

	private boolean[] vpY;

	public DxfData() {
		this.entityMap = new MapList<String, DxfEntity>();
		this.blocks = new MapList<String, DxfEntity>();
		this.error = new ArrayList<DxfEntity>();
		this.selections = new DxfSelections();
	}
	public void addBounds(Rectangle2D rect) {
		this.selections.add(rect);
		this.updateBounds();
	}

	public void addCheck(List<DimensionNode> nodesX, List<DimensionNode> nodesY, boolean[][] edgesX, boolean[][] edgesY, boolean[] vpX, boolean[] vpY,
			Set<NodePair> loaX, Set<NodePair> loaY) {
		this.nodesX = nodesX;
		this.nodesY = nodesY;
		this.edgesX = edgesX;
		this.edgesY = edgesY;
		this.vpX = vpX;
		this.vpY = vpY;
		this.loaX = loaX;
		this.loaY = loaY;
	}

	public void clearBounds() {
		this.selections.clear();
		this.updateBounds();
	}

	public void clearSelections() {
		this.selections.clear();
	}

	/**
	 * 2つの範囲が重なるかどうかを判定するメソッド
	 * 
	 * @param min1
	 *            範囲1の最小値
	 * @param max1
	 *            範囲1の最大値
	 * @param min2
	 *            範囲2の最小値
	 * @param max2
	 *            範囲2の最大値
	 * @return 重なれば0、範囲1が範囲2よりも大きければ正の値, 小さければ負の値を返す。
	 */
	int compare(double min1, double max1, double min2, double max2) {
		if (max1 < min2 || max2 < min1) {
			return Double.compare(min1 + max1, min2 + max2);
		}
		return 0;
	}

	/**
	 * X座標を基準として長方形を比較するメソッド
	 * 
	 * @param rect1
	 *            比較する長方形
	 * @param rect2
	 *            比較する長方形
	 * @return 比較結果
	 */
	private int compareX(Rectangle2D rect1, Rectangle2D rect2) {
		return this.compare(rect1.getMinX(), rect1.getMaxX(), rect2.getMinX(), rect2.getMaxX());
	}

	/**
	 * Y座標を基準として長方形を比較するメソッド
	 * 
	 * @param rect1
	 *            比較する長方形
	 * @param rect2
	 *            比較する長方形
	 * @return 比較結果
	 */
	private int compareY(Rectangle2D rect1, Rectangle2D rect2) {
		return this.compare(rect1.getMinY(), rect1.getMaxY(), rect2.getMinY(), rect2.getMaxY());
	}

	public void drawBounds(Graphics2D g, ViewingEnvironment env) {
		if (this.views != null) {
			for (ProjectionView view : this.views.values()) {
				if (view != null) {
					view.draw(g, env);
				}
			}
		}
	}

	public void drawCheckAssist(Graphics2D g, ViewingEnvironment env) {
		Stroke stroke = g.getStroke();
		double radius = 0.5f;
		g.setStroke(env.getNarrowDashStroke());
		if (this.nodesX != null) {
			for (int i = 0; i < this.nodesX.size(); i++) {
				DimensionNode node = this.nodesX.get(i);
				Ellipse2D.Double n = new Ellipse2D.Double(node.getValue() - radius, -radius, radius * 2, radius * 2);
				Color color = g.getColor();
				boolean req = node.isReqDim();
				if (this.vpX[i] || !req) {
					g.setColor(Color.RED);
				}
				g.fill(n);
				g.setColor(color);
				if (req) {
					if (!this.vpX[i]) {
						g.draw(new Line2D.Double(node.getValue(), 0, node.getValue(), this.screen.height));
					}
				}
			}
		}
		if (this.nodesY != null) {
			for (int i = 0; i < this.nodesY.size(); i++) {
				DimensionNode node = this.nodesY.get(i);
				Ellipse2D.Double n = new Ellipse2D.Double(-radius, node.getValue() - radius, radius * 2, radius * 2);
				Color color = g.getColor();
				boolean req = node.isReqDim();
				if (this.vpY[i] || !req) {
					g.setColor(Color.RED);
					g.fill(n);
					g.setColor(color);
				}
			}
			for (int i = 0; i < this.nodesY.size(); i++) {
				DimensionNode node = this.nodesY.get(i);
				Ellipse2D.Double n = new Ellipse2D.Double(-radius, node.getValue() - radius, radius * 2, radius * 2);
				boolean req = node.isReqDim();
				if (!this.vpY[i] && req) {
					g.fill(n);
				}
				if (req) {
					if (!this.vpY[i]) {
						g.draw(new Line2D.Double(0, node.getValue(), this.screen.width, node.getValue()));
					}
				}
			}
		}

		g.setColor(Color.RED);
		g.setStroke(env.getErrorStroke());
		if (this.loaX != null && this.loaY != null) {
			if (!this.loaY.isEmpty()) {
				double posX = screen.width / 15;
				for (NodePair pair : loaY) {
					g.draw(new Line2D.Double(posX, pair.getValue1().getValue(), posX, pair.getValue2().getValue()));
				}
			}
			if (!this.loaX.isEmpty()) {
				double posY = screen.height / 15;
				for (NodePair pair : loaX) {
					g.draw(new Line2D.Double(pair.getValue1().getValue(), posY, pair.getValue2().getValue(), posY));
				}
			}
		}

		g.setColor(Color.BLACK);
		g.setStroke(env.getNarrowStroke());
		if (this.edgesX != null) {
			for (int i = 0; i < this.edgesX.length; i++) {
				for (int j = i + 1; j < this.edgesX.length; j++) {
					if (this.edgesX[i][j]) {
						RoundNumber node1 = this.nodesX.get(i);
						RoundNumber node2 = this.nodesX.get(j);
						g.draw(new QuadCurve2D.Double(node1.getValue(), 0, (node1.getValue() + node2.getValue()) / 2, -Math.abs(node1.getValue()
								- node2.getValue()) / 1.5, node2.getValue(), 0));
					}
				}
			}
		}
		if (this.edgesY != null) {
			for (int i = 0; i < this.edgesY.length; i++) {
				for (int j = i + 1; j < this.edgesY.length; j++) {
					if (this.edgesY[i][j]) {
						RoundNumber node1 = this.nodesY.get(i);
						RoundNumber node2 = this.nodesY.get(j);
						g.draw(new QuadCurve2D.Double(0, node1.getValue(), -Math.abs(node1.getValue() - node2.getValue()) / 1.5, (node1.getValue() + node2
								.getValue()) / 2, 0, node2.getValue()));
					}
				}
			}
		}
		g.setStroke(stroke);
	}

	/**
	 * 図面の構成要素を描画するメソッド
	 * 
	 * @param g
	 * @param env
	 *            描画する環境
	 */
	public void drawEntities(Graphics2D g, ViewingEnvironment env) {
		for (Collection<DxfEntity> entities : this.entityMap.values()) {
			for (DxfEntity entity : entities) {
				if (entity instanceof DxfDimension) {
					if (env.getParam(ViewingEnvironment.labelDimension) == 0) {
						continue;
					}
				} else if (entity instanceof DxfInsert) {
					if (env.getParam(ViewingEnvironment.labelInsert) == 0) {
						continue;
					}
				}
				entity.draw(g, env);
			}
		}
	}

	public void drawSelections(Graphics2D g) {
		this.selections.draw(g);
	}

	/**
	 * ブロックエントリーを取得するメソッド
	 * 
	 * @param name
	 *            ブロック名
	 * @return ブロックエントリー
	 */
	public Collection<DxfEntity> getBlock(String name) {
		return this.blocks.get(name);
	}

	public Collection<Rectangle2D> getBounds() {
		Collection<Rectangle2D> bounds = new ArrayList<Rectangle2D>();
		if (this.views != null) {
			for (ProjectionView view : this.views.values()) {
				bounds.add(view.getBounds());
			}
		}
		return bounds;
	}

	/**
	 * 表示環境を取得するメソッド
	 * 
	 * @return 表示環境
	 */
	public Rectangle getDefaultScreen() {
		return this.screen;
	}

	/**
	 * 表示領域内の線分データを取得するメソッド
	 * 
	 * @return 表示領域内の線分データ
	 */
	public List<DxfArc> getDxfArcs() {
		Collection<DxfEntity> entities = this.entityMap.get(DxfArc.class.getName());
		List<DxfArc> arcs = new ArrayList<DxfArc>();
		if (entities != null) {
			for (DxfEntity entity : entities) {
				if (entity instanceof DxfArc) {
					DxfArc arc = (DxfArc) entity;
					if (arc.isSelectable() && arc.isContained(this.screen)) {
						arcs.add(arc);
					}
				}
			}
		}
		return arcs;
	}

	/**
	 * DXFファイルの円を取得するメソッド
	 * 
	 * @return DXFファイルの円を取得するメソッド
	 */
	public Collection<DxfCircle> getDxfCircles() {
		Collection<DxfEntity> entities = this.entityMap.get(DxfCircle.class.getName());
		Collection<DxfCircle> circles = new ArrayList<DxfCircle>();
		if (entities != null) {
			for (DxfEntity entity : entities) {
				if (entity instanceof DxfCircle) {
					circles.add((DxfCircle) entity);
				}
			}
		}
		return circles;
	}

	/**
	 * DXFファイルのデータを取得するメソッド
	 * 
	 * @return DXFファイルのデータ
	 */
	public MapList<String, DxfEntity> getDxfData() {
		return this.entityMap;
	}

	/**
	 * 表示領域内の線分データを取得するメソッド
	 * 
	 * @return 表示領域内の線分データ
	 */
	public List<DxfLine> getDxfLines() {
		return this.lines;
	}

	/**
	 * 選択範囲に含まれる最小の外接長方形を求めるメソッド
	 * 
	 * @param selection
	 *            選択範囲
	 * @return 最小の外接長方形
	 */
	public Collection<DxfEntity> getEntities(MouseSelection selection) {
		Collection<DxfEntity> contains = new ArrayList<DxfEntity>();
		for (Collection<DxfEntity> entities : this.entityMap.values()) {
			for (DxfEntity entity : entities) {
				if (entity instanceof DxfInsert || entity instanceof DxfDimension || entity instanceof DxfMtext) {
					continue;
				}
				if (selection.contains(entity)) {
					contains.add(entity);
				}
			}
		}
		return contains;
	}

	/**
	 * 異常を検出した寸法などを返します。
	 * 
	 * @return 異常を検出した寸法など
	 */
	public Collection<DxfEntity> getErrorEntities() {
		return this.error;
	}

	public Collection<DxfEntity> getIntersectsTexts(Rectangle2D rect) {
		List<DxfEntity> texts = new ArrayList<DxfEntity>();
		List<DxfEntity> entities = this.entityMap.get(DxfText.class.getName());
		if (entities != null) {
			for (DxfEntity entity : entities) {
				if (entity.intersects(rect)) {
					texts.add(entity);
				}
			}
		}
		entities = this.entityMap.get(DxfMtext.class.getName());
		if (entities != null) {
			for (DxfEntity entity : entities) {
				if (entity.intersects(rect)) {
					texts.add(entity);
				}
			}
		}
		return texts;
	}

	/**
	 * 図面に対応する名前を返します。
	 * 
	 * @param indexX
	 *            X軸方向のインデックス
	 * @param indexY
	 *            Y軸方向のインデックス
	 * @param centerX
	 *            正面図のX軸方向のインデックス
	 * @param centerY
	 *            正面図のY軸方向のインデックス
	 * @return 対応する名前
	 */
	private String getLabel(int indexX, int indexY, int centerX, int centerY) {
		if (indexY == centerY) {
			if (indexX == centerX) {
				return LABEL_FRONT_VIEW;
			} else if (indexX == centerX + 1) {
				return LABEL_RIGHT_SIDE_VIEW;
			} else if (indexX == centerX - 1) {
				return LABEL_LEFT_SIDE_VIEW;
			} else if (Math.abs(indexX - centerX) == 2) {
				return LABEL_BACK_VIEW;
			}
		} else if (indexX == centerX) {
			if (indexY == centerY + 1) {
				return LABEL_PLANE_VIEW;
			} else if (indexY == centerY - 1) {
				return LABEL_BOTTOM_VIEW;
			} else if (Math.abs(indexY - centerY) == 2) {
				return LABEL_BACK_VIEW;
			}
		}
		return indexX + ", " + indexY;
	}

	/**
	 * 投影図に含まれる線分の系列を取得するメソッド
	 * 
	 * @param bounds
	 * @return 投影図に含まれる線分
	 */
	private List<DxfLine> getProjectionLines(Rectangle2D bounds) {

		Collection<DxfEntity> entities = this.entityMap.get(DxfLine.class.getName());

		List<DxfLine> borders = new ArrayList<DxfLine>();
		List<DxfLine> lines = new ArrayList<DxfLine>(entities.size());

		for (DxfEntity entity : entities) {
			DxfLine line = (DxfLine) entity;
			if (this.isContact(bounds, line)) {
				if (!this.isOverLine(bounds, line)) {
					borders.add(line);
					line.setSelectable(false);
				}
			} else if (line.isContained(this.screen)) {
				lines.add(line);
			}
		}

		double x = bounds.getMinX();
		double y = bounds.getMaxY();
		double minDist = Double.POSITIVE_INFINITY;
		int index = -1;
		for (int i = 0; i < lines.size(); i++) {
			DxfLine line = lines.get(i);
			double dist = line.ptSegDistSq(x, y);
			if (dist < minDist) {
				minDist = dist;
				index = i;
			}
		}
		if (index >= 0) {
			lines.remove(index).setSelectable(false);
		}

		return lines;
	}

	/**
	 * 投影図のマップを取得するメソッド
	 * 
	 * @param views
	 *            投影図のリスト
	 * @return 投影図のマップ
	 */
	public Map<String, ProjectionView> getProjectionMap(List<ProjectionView> views) {
		int[] scoreX = new int[views.size()];
		int[] scoreY = new int[views.size()];
		Collections.sort(views, new Comparator<ProjectionView>() {
			/**
			 * @see java.util.Comparator#compare(java.lang.Object,
			 *      java.lang.Object)
			 */

			public int compare(ProjectionView view1, ProjectionView view2) {
				Rectangle2D rect1 = view1.getBounds();
				Rectangle2D rect2 = view2.getBounds();
				int retX = DxfData.this.compare(rect1.getMinX(), rect1.getMaxX(), rect2.getMinX(), rect2.getMaxX());
				if (retX != 0) {
					return retX;
				}
				return DxfData.this.compare(rect1.getMinY(), rect1.getMaxY(), rect2.getMinY(), rect2.getMaxY());
			}
		}); // ソートしないとうまくスコアはつけられない。
		int maxX = 0;
		int maxY = 0;
		Map<Integer, Counter> mapX = new HashMap<Integer, Counter>();
		Map<Integer, Counter> mapY = new HashMap<Integer, Counter>();
		for (int i = 0; i < views.size(); i++) {
			for (int j = i + 1; j < views.size(); j++) {
				Rectangle2D rect1 = views.get(i).getBounds();
				Rectangle2D rect2 = views.get(j).getBounds();
				int ret = this.compareX(rect1, rect2);
				if (ret > 0) {
					scoreX[i] = scoreX[j] + 1;
					if (maxX < scoreX[i]) {
						maxX = scoreX[i];
					}
				} else if (ret < 0) {
					scoreX[j] = scoreX[i] + 1;
					if (maxX < scoreX[j]) {
						maxX = scoreX[j];
					}
				} else {
					if (scoreX[i] < scoreX[j]) {
						scoreX[i] = scoreX[j];
					} else {
						scoreX[j] = scoreX[i];
					}
				}
				ret = this.compareY(rect1, rect2);
				if (ret > 0) {
					scoreY[i] = scoreY[j] + 1;
					if (maxY < scoreY[i]) {
						maxY = scoreY[i];
					}
				} else if (ret < 0) {
					scoreY[j] = scoreY[i] + 1;
					if (maxY < scoreY[j]) {
						maxY = scoreY[j];
					}
				} else {
					if (scoreY[i] < scoreY[j]) {
						scoreY[i] = scoreY[j];
					} else {
						scoreY[j] = scoreY[i];
					}
				}
			}
		}
		ProjectionView[][] tmp = new ProjectionView[maxX + 1][maxY + 1];
		for (int i = 0; i < views.size(); i++) {
			tmp[scoreX[i]][scoreY[i]] = views.get(i);
			Counter counter = mapX.get(scoreX[i]);
			if (counter == null) {
				counter = new Counter();
				mapX.put(scoreX[i], counter);
			}
			counter.up();
			counter = mapY.get(scoreY[i]);
			if (counter == null) {
				counter = new Counter();
				mapY.put(scoreY[i], counter);
			}
			counter.up();
		}
		int maxCount = 1;
		int indexX = maxX / 2;
		for (Map.Entry<Integer, Counter> entry : mapX.entrySet()) {
			int count = entry.getValue().getCount();
			if (maxCount < count) {
				maxCount = count;
				indexX = entry.getKey();
			}
		}
		maxCount = 1;
		int indexY = maxY / 2;
		for (Map.Entry<Integer, Counter> entry : mapY.entrySet()) {
			int count = entry.getValue().getCount();
			if (maxCount < count) {
				maxCount = count;
				indexY = entry.getKey();
			}
		}
		Map<String, ProjectionView> map = new HashMap<String, ProjectionView>();
		for (int i = 0; i < tmp.length; i++) {
			for (int j = 0; j < tmp[i].length; j++) {
				ProjectionView view = tmp[i][j];
				if (view != null) {
					String label = this.getLabel(i, j, indexX, indexY);
					view.setLabel(label);
					map.put(label, view);
				}
			}
		}
		return map;
	}

	public Map<String, ProjectionView> getProjectionViewMap() {
		return this.views;
	}

	public double getScale() {
		return this.scale;
	}

	public Collection<Area> getSelections() {
		return this.selections.getSelections();
	}

	private void initScale() {
		List<DxfEntity> dimensions = this.entityMap.get(DxfDimension.class.getName());
		if (dimensions != null) {
			Map<Double, Counter> scaleMap = new HashMap<Double, Counter>();
			for (DxfEntity entity : dimensions) {
				if (entity instanceof DxfDimension) {
					DxfDimension d = (DxfDimension) entity;
					if (d.hasScale()) {
						double scale = d.getScale();
						if (Double.compare(scale, 0) > 0) {
							Counter count = scaleMap.get(scale);
							if (count == null) {
								count = new Counter();
								scaleMap.put(scale, count);
							}
							count.up();
						}
					}
				}
			}
			this.scale = 1;
			int vote = 0;
			for (Map.Entry<Double, Counter> entry : scaleMap.entrySet()) {
				if (vote < entry.getValue().getCount()) {
					vote = entry.getValue().getCount();
					this.scale = entry.getKey();
				}
				System.out.printf("SCALE: %f, %d\n", entry.getKey(), entry.getValue().getCount());
			}
			if (Double.compare(this.scale, 0) <= 0) {
				this.scale = 1;
			}
		}
	}

	/**
	 * 直線が長方形と接しているかどうかをチェックする
	 * 
	 * @param rect
	 * @param line
	 * @return
	 */
	private boolean isContact(Rectangle2D rect, DxfLine line) {
		return RoundNumber.nearlyEquals(rect.getMinX(), line.getX1()) || RoundNumber.nearlyEquals(rect.getMinX(), line.getX2())
				|| RoundNumber.nearlyEquals(rect.getMaxX(), line.getX1()) || RoundNumber.nearlyEquals(rect.getMaxX(), line.getX2())
				|| RoundNumber.nearlyEquals(rect.getMinY(), line.getY1()) || RoundNumber.nearlyEquals(rect.getMinY(), line.getY2())
				|| RoundNumber.nearlyEquals(rect.getMaxY(), line.getY1()) || RoundNumber.nearlyEquals(rect.getMaxY(), line.getY2())
				|| line.intersectsLine(rect.getMinX(), rect.getMinY(), rect.getMinX(), rect.getMaxY())
				|| line.intersectsLine(rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMinY())
				|| line.intersectsLine(rect.getMaxX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY())
				|| line.intersectsLine(rect.getMinX(), rect.getMaxY(), rect.getMaxX(), rect.getMaxY());
	}

	/**
	 * 直線が長方形を構成する線分どうかをチェックする
	 * 
	 * @param rect
	 * @param line
	 * @return
	 */
	private boolean isOverLine(Rectangle2D rect, DxfLine line) {
		return (RoundNumber.nearlyEquals(rect.getMinX(), line.getX1()) && RoundNumber.nearlyEquals(rect.getMinX(), line.getX2()))
				|| (RoundNumber.nearlyEquals(rect.getMaxX(), line.getX1()) && RoundNumber.nearlyEquals(rect.getMaxX(), line.getX2()))
				|| (RoundNumber.nearlyEquals(rect.getMinY(), line.getY1()) && RoundNumber.nearlyEquals(rect.getMinY(), line.getY2()))
				|| (RoundNumber.nearlyEquals(rect.getMaxY(), line.getY1()) && RoundNumber.nearlyEquals(rect.getMaxY(), line.getY2()));
	}

	/**
	 * 図面を最適化するためのメソッド
	 * 
	 * まず、図面の枠を除いて、X軸、Y軸方向へ図面を分割できる直線を探し、 その直線を元に、投影図を切り出します。
	 * 同時に，スケールを計算して寸法線などに適応させたり，寸法線にテキストがない場合は，最近傍のテキストを適用させます．
	 */
	public void optimization() {
		List<DxfEntity> list = new ArrayList<DxfEntity>();
		List<DxfAbstText> texts = new ArrayList<DxfAbstText>();
		for (Map.Entry<String, List<DxfEntity>> entry : this.entityMap.entrySet()) {
			for (DxfEntity entity : entry.getValue()) {
				if (entity instanceof DxfAbstText) {
					DxfAbstText text = (DxfAbstText) entity;
					texts.add(text);
				}
				if (!entity.link(this)) {
					list.add(entity);
				}
			}
		}
		initScale();
		for (List<DxfEntity> entities : this.entityMap.values()) {
			for (DxfEntity entity : entities) {
				if (entity instanceof DxfEntities) {
					DxfEntities dim = (DxfEntities) entity;
					if (!dim.hasText()) {
						// dim.joinText(texts);
					}
				}
			}
		}
		System.out.println("Scale: " + this.scale);
		for (DxfEntity entity : list) {
			if (entity instanceof DxfDimension) {
				DxfDimension dim = (DxfDimension) entity;
				if (dim.getIndicated() != null) {
					dim.link(this, this.scale);
				}
			}
		}

		Rectangle2D bounds = null;
		for (Collection<DxfEntity> entities : this.entityMap.values()) {
			for (DxfEntity entity : entities) {
				if (entity.isSelectable() && entity.isContained(this.screen)) {
					if (bounds == null) {
						bounds = entity.getBounds2D();
					} else {
						bounds.add(entity.getBounds2D());
					}
				}
			}
		}

		this.lines = this.getProjectionLines(bounds);

		boolean[] flagsX = new boolean[SPLIT_PROJECTION];
		boolean[] flagsY = new boolean[SPLIT_PROJECTION];
		for (int i = 0; i < SPLIT_PROJECTION; i++) {
			double x = this.screen.x + this.screen.width * i / (double) SPLIT_PROJECTION;
			double y = this.screen.y + this.screen.height * i / (double) SPLIT_PROJECTION;
			Line2D spliterX = new Line2D.Double(x, this.screen.getMinY(), x, this.screen.getMaxY());
			Line2D spliterY = new Line2D.Double(this.screen.getMinX(), y, this.screen.getMaxX(), y);
			for (DxfLine line : this.lines) {
				if (line.intersectsLine(spliterX)) {
					flagsX[i] = true;
					if (flagsY[i]) {
						break;
					}
				}
				if (line.intersectsLine(spliterY)) {
					flagsY[i] = true;
					if (flagsX[i]) {
						break;
					}
				}
			}
		}

		int[] startX = new int[10];
		int[] startY = new int[10];
		int[] endX = new int[10];
		int[] endY = new int[10];
		int indexX = 0;
		int indexY = 0;
		for (int i = 1; i < SPLIT_PROJECTION; i++) {
			if (flagsX[i]) {
				if (startX[indexX] == 0) {
					startX[indexX] = i;
				}
			} else if (startX[indexX] > 0) {
				endX[indexX] = i;
				indexX++;
			}
		}
		for (int i = 1; i < SPLIT_PROJECTION; i++) {
			if (flagsY[i]) {
				if (startY[indexY] == 0) {
					startY[indexY] = i;
				}
			} else if (startY[indexY] > 0) {
				endY[indexY] = i;
				indexY++;
			}
		}

		for (int i = 0; i < indexX; i++) {
			double x = this.screen.x + this.screen.width * (startX[i] - 1) / (double) SPLIT_PROJECTION;
			double width = (endX[i] - startX[i] + 1) * this.screen.width / (double) SPLIT_PROJECTION;
			for (int j = 0; j < indexY; j++) {
				double y = this.screen.y + this.screen.height * (startY[j] - 1) / (double) SPLIT_PROJECTION;
				double height = (endY[j] - startY[j] + 1) * this.screen.height / (double) SPLIT_PROJECTION;
				this.selections.add(new Rectangle2D.Double(x, y, width, height));
			}
		}
		this.updateBounds();
	}

	/**
	 * エンティティーセクション図形リストを設定するメソッド
	 * 
	 * @param entityMap
	 *            設定する図形リスト
	 */
	public void putAllEntities(MapList<String, DxfEntity> entityMap) {
		for (Map.Entry<String, List<DxfEntity>> entry : entityMap.entrySet()) {
			String key = entry.getKey();
			this.entityMap.putAll(key, entry.getValue());
		}
	}

	public void removeBounds(Rectangle2D rect) {
		this.selections.remove(rect);
		this.updateBounds();
	}

	/**
	 * 読み込んだブロックセクションのデータを設定するメソッド
	 * 
	 * @param blocks
	 *            読み込んだブロックセクションのデータ
	 */
	public void setBlocks(MapList<String, DxfEntity> blocks) {
		this.blocks.putAll(blocks);
	}

	/**
	 * 表示範囲またはオブジェクト範囲を設定するメソッド
	 * 
	 * @param screen
	 *            表示範囲またはオブジェクト範囲
	 */
	public void setDefaultScreen(Rectangle screen) {
		this.screen = screen;
	}

	/**
	 * 選択範囲に含まれる図面の構成要素に対して、 その構成要素を包含する最小の長方形を更新するメソッド
	 * 
	 * @param selections
	 *            選択範囲
	 */
	public void updateBounds() {
		Collection<Area> areas = this.selections.getSelections();
		List<ProjectionView> views = new ArrayList<ProjectionView>();
		Iterator<Area> itr = areas.iterator();
		while (itr.hasNext()) {
			Area area = itr.next();
			ProjectionView view = new ProjectionView();
			for (Collection<DxfEntity> entities : this.entityMap.values()) {
				for (DxfEntity entity : entities) {
					if (entity.isSelectable() && entity.isContained(area)) {
						view.add(entity);
					}
				}
			}
			if (!view.isEmpty()) {
				views.add(view);
			} else {
				itr.remove();
			}
		}
		this.views = this.getProjectionMap(views);
	}
}

package dxf.checker;

import gui.DxfViewer;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.ListSet;
import util.MapList;
import util.NodePair;
import util.RoundNumber;

import dxf.DxfData;
import dxf.section.entities.DxfArc;
import dxf.section.entities.DxfCircle;
import dxf.section.entities.DxfDimension;
import dxf.section.entities.DxfEntity;
import dxf.section.entities.DxfInsert;
import dxf.section.entities.DxfLine;

/**
 * 寸法抜けをチェックするクラス
 * 
 * @author FUJIWARA Masayasu
 * @since 0.03
 */
public class DimensionChecker implements DxfChecker {

	private Collection<DxfEntity> errors;

	public DimensionChecker() {
		this.errors = new ArrayList<DxfEntity>();
	}

	/**
	 * @see dxf.checker.DxfChecker#check(dxf.DxfData)
	 */
	public boolean check(DxfData dxf) {
		this.clearErrorEntities();
		this.errors.addAll(dxf.getErrorEntities());

		List<DxfLine> lines = dxf.getDxfLines();
		List<DxfLine> centerLines = new ArrayList<DxfLine>(); // 中心線のみ
		for (DxfLine line : lines) {
			if ("CENTER".equals(line.getStyle())) {
				centerLines.add(line);
			}
		}
		MapList<String, DxfEntity> map = dxf.getDxfData();
		ListSet<DimensionNode> nodesX = new ListSet<DimensionNode>();
		ListSet<DimensionNode> nodesY = new ListSet<DimensionNode>();

		/**
		 * 投影図から寸法線が必要なノードを取得するための処理
		 */
		Map<String, ProjectionView> projectionMap = dxf.getProjectionViewMap();
		Set<NodePair> setLoaX = new HashSet<NodePair>();
		Set<NodePair> setLoaY = new HashSet<NodePair>();
		for (ProjectionView view : projectionMap.values()) {
			for (DxfEntity entity : view.getEntities()) {
				entity.getCheckPoints(nodesX, nodesY);
			}
			Rectangle2D rect = view.getBounds();
			setLoaX.add(new NodePair(rect.getMinX(), rect.getMaxX()));
			setLoaY.add(new NodePair(rect.getMinY(), rect.getMaxY()));
		}
		ProjectionView frontview = projectionMap.get(DxfData.LABEL_FRONT_VIEW);

		Rectangle2D frontviewBound = frontview.getBounds();

		/**
		 * 中心線からノード取得するための処理
		 */
		Set<RoundNumber> centersX = new HashSet<RoundNumber>();
		Set<RoundNumber> centersY = new HashSet<RoundNumber>();
		this.getCenterLinePoints(lines, centersX, centersY);
		for (RoundNumber n : centersX) {
			nodesX.add(new DimensionNode(n.getValue(), false));
		}
		for (RoundNumber n : centersY) {
			nodesY.add(new DimensionNode(n.getValue(), false));
		}
//		for (DxfLine cl1 : centerLines) {
//			for (DxfLine cl2 : centerLines) {
//				if (cl1.equals(cl2)) continue;
//				if (RoundNumber.nearlyEqualsZero(cl1.innerProduct(cl2))) {
//					Point2D p = cl1.getCrossPoint(cl2);
//					nodesX.add(new DimensionNode(p.getX(), false));
//					nodesY.add(new DimensionNode(p.getY(), false));
//				}
//			}
//		}

		Collection<DxfCircle> circles = dxf.getDxfCircles();
		List<DxfArc> arcs = dxf.getDxfArcs();

		boolean[][] edgesX = new boolean[nodesX.size()][nodesX.size()];
		boolean[][] edgesY = new boolean[nodesY.size()][nodesY.size()];

		/*
		 * 寸法線の端点にあたるノード間を辺で結ぶ 寸法線に対応する中心線が存在する場合、その中心線を経由してノード間に辺を結ぶ
		 */
		List<DxfDimension> dimensionsX = new ArrayList<DxfDimension>();
		List<DxfDimension> dimensionsY = new ArrayList<DxfDimension>();
		List<DxfDimension> dimArc = new ArrayList<DxfDimension>();
		List<DxfDimension> dimCircle = new ArrayList<DxfDimension>();
		List<DxfDimension> dimOthers = new ArrayList<DxfDimension>();
		MapList<RoundNumber, DxfDimension> centerMapX = new MapList<RoundNumber, DxfDimension>();
		MapList<RoundNumber, DxfDimension> centerMapY = new MapList<RoundNumber, DxfDimension>();
		for (List<DxfEntity> entities : map.values()) {
			for (DxfEntity entity : entities) {
				if (!(entity instanceof DxfDimension)) {
					break;
				}
				DxfDimension dim = (DxfDimension) entity;
				if ((dim.getType() & DxfDimension.TYPE_ARC) > 0) {
					dimArc.add(dim);
				}
				if ((dim.getType() & DxfDimension.TYPE_CIRCLE) > 0) {
					dimCircle.add(dim);
				}
				if (dim.hasDimension()) {
					RoundNumber node1 = dim.getCheckPoint1();
					RoundNumber node2 = dim.getCheckPoint2();
					if (node1 != null && node2 != null) {
						boolean[][] edges = null;
						ListSet<DimensionNode> nodes = null;
						Set<RoundNumber> centers = null;
						RoundNumber center = new RoundNumber((node1.getValue() + node2.getValue()) / 2);
						MapList<RoundNumber, DxfDimension> centerMap = null;
						if (dim.getDirection() == DxfLine.DIRECTION_X) {
							edges = edgesX;
							nodes = nodesX;
							centers = centersX;
							centerMap = centerMapX;
							dimensionsX.add(dim);
							// 全長の有無のチェック
							setLoaX.remove(new NodePair(dim));
						} else if (dim.getDirection() == DxfLine.DIRECTION_Y) {
							edges = edgesY;
							nodes = nodesY;
							centers = centersY;
							centerMap = centerMapY;
							dimensionsY.add(dim);
							// 全長の有無のチェック
							setLoaY.remove(new NodePair(dim));
						} else {
							new RuntimeException();
						}
						if (edges != null && nodes != null) {
							int index1 = nodes.indexOf(node1);
							int index2 = nodes.indexOf(node2);
							if (index1 >= 0 && index2 >= 0) {
								if (centers.contains(center)) {
									centerMap.put(center, dim);
									int index3 = nodes.indexOf(center);
									if (index3 >= 0) {
										this.linkEdge(edges, index1, index2, index3);
									} else {
										dim.setError();
										this.errors.add(dim);
									}
								} else {
									this.linkEdge(edges, index1, index2);
								}
							} else {
								dim.setError();
								this.errors.add(dim);
							}
						}
					} else {
						dimOthers.add(dim);
					}
				}
			}
		}

		/**
		 * 中心線
		 */
		for (DxfDimension dimension : dimensionsX) {
			RoundNumber node1 = dimension.getCheckPoint1();
			RoundNumber node2 = dimension.getCheckPoint2();
			RoundNumber node3 = dimension.getCheckPointCenter();
			if (centerMapX.containsKey(node3)) {
				List<DxfDimension> dimensions1 = centerMapX.get(node1);
				List<DxfDimension> dimensions2 = centerMapX.get(node2);
				if (dimensions1 != null) {
					for (DxfDimension dim1 : dimensions1) {
						double length = dim1.length() / 2;
						RoundNumber node4 = new RoundNumber(node2.getValue() - length);
						RoundNumber node5 = new RoundNumber(node2.getValue() + length);
						int index3 = nodesX.indexOf(node4);
						int index4 = nodesX.indexOf(node5);
						int index5 = nodesX.indexOf(node2);
						if (index3 >= 0 && index4 >= 0) {
							this.linkEdge(edgesX, index3, index4, index5);
						}
					}
				}
				if (dimensions2 != null) {
					for (DxfDimension dim2 : dimensions2) {
						double length = dim2.length() / 2;
						RoundNumber node4 = new RoundNumber(node1.getValue() - length);
						RoundNumber node5 = new RoundNumber(node1.getValue() + length);
						int index3 = nodesX.indexOf(node4);
						int index4 = nodesX.indexOf(node5);
						int index5 = nodesX.indexOf(node1);
						if (index3 >= 0 && index4 >= 0) {
							this.linkEdge(edgesX, index3, index4, index5);
						}
					}
				}
			}
		}

		for (DxfDimension dimension : dimensionsY) {
			RoundNumber node1 = dimension.getCheckPoint1();
			RoundNumber node2 = dimension.getCheckPoint2();
			RoundNumber node3 = dimension.getCheckPointCenter();
			if (centerMapY.containsKey(node3)) {
				List<DxfDimension> dimensions1 = centerMapY.get(node1);
				List<DxfDimension> dimensions2 = centerMapY.get(node2);
				if (dimensions1 != null) {
					for (DxfDimension dim1 : dimensions1) {
						double length = dim1.length() / 2;
						RoundNumber node4 = new RoundNumber(node2.getValue() - length);
						RoundNumber node5 = new RoundNumber(node2.getValue() + length);
						int index3 = nodesY.indexOf(node4);
						int index4 = nodesY.indexOf(node5);
						int index5 = nodesY.indexOf(node2);
						if (index3 >= 0 && index4 >= 0) {
							this.linkEdge(edgesY, index3, index4, index5);
						}
					}
				}
				if (dimensions2 != null) {
					for (DxfDimension dim2 : dimensions2) {
						double length = dim2.length() / 2;
						RoundNumber node4 = new RoundNumber(node1.getValue() - length);
						RoundNumber node5 = new RoundNumber(node1.getValue() + length);
						int index3 = nodesY.indexOf(node4);
						int index4 = nodesY.indexOf(node5);
						int index5 = nodesY.indexOf(node1);
						if (index3 >= 0 && index4 >= 0) {
							this.linkEdge(edgesY, index3, index4, index5);
						}
					}
				}
			}
		}
		for (DxfDimension dim : dimCircle) {
			if (!dim.hasDimension()) {
				System.out.println("dim-circle: " + dim.getText());
				continue;
			}
			double centerX = dim.getCenterX();
			double centerY = dim.getCenterY();
			int indexX = nodesX.indexOf(new RoundNumber(centerX));
			int indexY = nodesY.indexOf(new RoundNumber(centerY));
			double length = dim.length();

			// TODO この辺何してるのかいまいち忘れてしまった．
			DxfCircle[] cs = dim.checkCircle(circles);
			DxfCircle c0 = null;
			int indexX0 = -1;
			int indexY0 = -1;
			if (cs != null) {
				for (int i = 0; i < cs.length; i++) {
					DxfCircle c1 = cs[i];
					int indexX1 = nodesX.indexOf(new RoundNumber(c1.getX()));
					int indexY1 = nodesY.indexOf(new RoundNumber(c1.getY()));
					if (c0 != null) {
						if (indexX1 > 0) {
							this.linkEdge(edgesX, indexX0, indexX1);
						} else {
							this.errors.add(c1);
							continue;
						}
						if (indexY1 >= 0) {
							this.linkEdge(edgesY, indexY0, indexY1);
						} else {
							this.errors.add(c1);
							continue;
						}
						c0 = c1;
						indexX0 = indexX1;
						indexY0 = indexY1;
					}
				}
			}

			/**
			 * 円の直径から寸法を確認する
			 */
			for (DxfCircle circle : circles) {
				if (RoundNumber.nearlyEquals(circle.getDiameter(), length)) {
					if (circle.equalsCenter(centerX, centerY)) {
						this.linkCircle(nodesX, nodesY, edgesX, edgesY, circle);
						circle.checked();
						// 円に対する寸法線が間接的に入っている場合のチェックを行なう．
					} else if ((RoundNumber.nearlyEquals(circle.getX(), centerX) && dim.getDirection() == DxfLine.DIRECTION_X)
							|| (RoundNumber.nearlyEquals(circle.getY(), centerY) && dim.getDirection() == DxfLine.DIRECTION_Y)) {
						// TODO 三面図対応を考えないと
						if (dim.getTypeAmount() > 1) {
							Set<DxfCircle> list = new HashSet<DxfCircle>();
							if (circle.getSimilarPatternCircles(dim.getTypeAmount(), list, circles, centerLines, this.errors) == DxfCircle.ON_CIRCLE) {
								checkEqualsInterval(nodesX, nodesY, edgesX, edgesY, list);
							}
							for (DxfCircle c : list) {
								c.checked();
								this.linkCircle(nodesX, nodesY, edgesX, edgesY, circle);
							}
						}
						circle.checked();
						this.linkCircle(nodesX, nodesY, edgesX, edgesY, circle);
					}
				}
			}

			/**
			 * 円弧の直径から寸法を確認する
			 */
			for (DxfArc arc : arcs) {
				if (RoundNumber.nearlyEquals(centerX, arc.getX()) && RoundNumber.nearlyEquals(centerY, arc.getY())
						&& RoundNumber.nearlyEquals(dim.length(), arc.getDiameter())) {
					Point2D pt1 = arc.getStartPoint();
					Point2D pt2 = arc.getEndPoint();
					int indexX1 = nodesX.indexOf(new RoundNumber(pt1.getX()));
					int indexX2 = nodesX.indexOf(new RoundNumber(pt2.getX()));
					if (indexX1 >= 0 && indexX2 >= 0) {
						this.linkEdge(edgesX, indexX1, indexX2, indexX);
					}
					int indexY1 = nodesY.indexOf(new RoundNumber(pt1.getY()));
					int indexY2 = nodesY.indexOf(new RoundNumber(pt2.getY()));
					if (indexY1 >= 0 && indexY2 >= 0) {
						this.linkEdge(edgesY, indexY1, indexY2, indexY);
					}
				}
			}
		}

		/**
		 * 面取りと円弧の処理
		 */
		List<DxfEntity> entities = map.get(DxfInsert.class.getName());
		if (entities != null) {
			// 引き出し線によるおねじの寸法解釈
			for (DxfEntity entity : entities) {
				if (entity instanceof DxfInsert) {
					DxfInsert insert = (DxfInsert) entity;
					// TODO 面取りのサイズの誤り検出
					if (insert.getType() == DxfInsert.TYPE_CHAMFER) {
						System.out.println("INSERT-TEXT: " + insert.getText());
						int amount = insert.getTypeAmount();
						Point2D pt = insert.getIndicatedPoint();
						assert pt != null;
						double minDist = Double.POSITIVE_INFINITY;
						double size = insert.getTypeSize() * dxf.getScale();
						if (size > 0) {
							int indexX = nodesX.indexOf(new RoundNumber(pt.getX()));
							int indexY = nodesY.indexOf(new RoundNumber(pt.getY()));
							if (indexX >= 0) {
								int indexX1 = nodesX.indexOf(new RoundNumber(pt.getX() + size));
								int indexX2 = nodesX.indexOf(new RoundNumber(pt.getX() - size));
								if (indexX1 >= 0) {
									linkEdge(edgesX, indexX, indexX1);
								}
								if (indexX2 >= 0) {
									linkEdge(edgesX, indexX, indexX2);
								}
							}
							if (indexY >= 0) {
								int indexY1 = nodesY.indexOf(new RoundNumber(pt.getY() + size));
								int indexY2 = nodesY.indexOf(new RoundNumber(pt.getY() - size));
								if (indexY1 >= 0) {
									linkEdge(edgesY, indexY, indexY1);
								}
								if (indexY2 >= 0) {
									linkEdge(edgesY, indexY, indexY2);
								}
							}
						}
						DxfLine nearestLine = null;
						System.out.println("面取り:" + amount);
						for (DxfLine line : lines) {
							double distSq = line.ptSegDistSq(pt);
							if (distSq < minDist) {
								minDist = distSq;
								nearestLine = line;
							}
						}
						if (nearestLine != null) {
							this.linkChamfer(nodesX, nodesY, edgesX, edgesY, nearestLine);
							amount--;
							if (amount > 0) {
								for (DxfLine l : lines) {
									if (nearestLine != l && nearestLine.equalsShape(l)) {
										this.linkChamfer(nodesX, nodesY, edgesX, edgesY, l);
										amount--;
									}
								}
							}
						}
						if (amount != 0) {
							if (DxfViewer.debug) {
								System.out.println("面取り数" + amount);
							}
							this.errors.add(insert);
						}
					} else if (insert.getType() > 0) {
						Point2D pt = insert.getIndicatedPoint();
						if (pt == null)
							continue;
						int amount = insert.getTypeAmount();
						if ((insert.getType() & DxfInsert.TYPE_CIRCLE) > 0) {
							double minDist = Double.POSITIVE_INFINITY;
							DxfCircle nearestCircle = null;
							for (DxfCircle circle : circles) {
								double distSq = circle.getDistSq(pt);
								if (distSq < minDist) {
									if (RoundNumber.nearlyEquals((circle.getDiameter() * dxf.getScale()), insert.getTypeSize())) {
										minDist = distSq;
										nearestCircle = circle;
									}
								}
							}
							System.out.println("Debug ねじ: " + insert.getText() + ", ねじ: " + amount + " / " + nearestCircle);
							if (nearestCircle != null) {
								System.out.println("Debug ねじ: " + insert.getText() + " nearest");
								Set<DxfCircle> list = new HashSet<DxfCircle>();
								if (nearestCircle.getSimilarPatternCircles(amount, list, circles, centerLines, this.errors) == DxfCircle.ON_CIRCLE) {
									checkEqualsInterval(nodesX, nodesY, edgesX, edgesY, list);
								}
								for (DxfCircle c : list) {
									c.checked();
									this.linkCircle(nodesX, nodesY, edgesX, edgesY, c);
								}
								continue;
							}
						}
						if ((insert.getType() & DxfInsert.TYPE_ARC) > 0) {
							double minDist = Double.POSITIVE_INFINITY;
							DxfArc nearestArc = null;
							for (DxfArc arc : arcs) {
								double distSq = arc.ptSegDistSq(pt);
								if (distSq < minDist) {
									minDist = distSq;
									nearestArc = arc;
								}
							}
							if (nearestArc != null) {
								for (DxfArc arc : arcs) {
									if (arc.equalsShape(nearestArc)) {
										this.linkArc(nodesX, nodesY, edgesX, edgesY, arc);
									}
								}
								if (!this.linkArc(nodesX, nodesY, edgesX, edgesY, nearestArc)) {
									System.out.println("ARC-MISS1: " + insert.getText());
									this.errors.add(insert);
								}
								continue;
							}
						}

						// (出っ張った)おねじだと仮定する
						double size = insert.getTypeSize() * dxf.getScale();
						if (size > 0) {
							int indexX = nodesX.indexOf(new RoundNumber(pt.getX()));
							int indexY = nodesY.indexOf(new RoundNumber(pt.getY()));
							if (indexX >= 0) {
								int indexX1 = nodesX.indexOf(new RoundNumber(pt.getX() + size));
								int indexX2 = nodesX.indexOf(new RoundNumber(pt.getX() - size));
								if (indexX1 >= 0) {
									linkEdge(edgesX, indexX, indexX1);
								}
								if (indexX2 >= 0) {
									linkEdge(edgesX, indexX, indexX2);
								}
							}
							if (indexY >= 0) {
								int indexY1 = nodesY.indexOf(new RoundNumber(pt.getY() + size));
								int indexY2 = nodesY.indexOf(new RoundNumber(pt.getY() - size));
								if (indexY1 >= 0) {
									linkEdge(edgesY, indexY, indexY1);
								}
								if (indexY2 >= 0) {
									linkEdge(edgesY, indexY, indexY2);
								}
							}
						}
					}
				}
			}
		}

		/**
		 * 円弧に対する処理
		 */
		for (DxfDimension dimension : dimArc) {
			assert (dimension.getType() & DxfDimension.TYPE_ARC) > 0;
			if (dimension.hasIndicated()) {
				Point2D pt = dimension.getIndicated().getPoint2D();
				double minDist = Double.POSITIVE_INFINITY;
				DxfArc nearestArc = null;
				for (DxfArc arc : arcs) {
					double distSq = arc.ptSegDistSq(pt);
					if (distSq < minDist) {
						minDist = distSq;
						nearestArc = arc;
					}
				}
				if (nearestArc == null) {
					this.errors.add(dimension);
				} else {
					if (this.linkArc(nodesX, nodesY, edgesX, edgesY, nearestArc)) {
						for (RoundNumber num : centersX) {
							DxfArc symmetryX = nearestArc.getSymmetryX(num.getValue());
							for (DxfArc arc : arcs) {
								if (arc.nearlyEquals(symmetryX)) {
									this.linkArc(nodesX, nodesY, edgesX, edgesY, arc);
								}
							}
						}
						for (RoundNumber num : centersY) {
							DxfArc symmetryY = nearestArc.getSymmetryY(num.getValue());
							for (DxfArc arc : arcs) {
								if (arc.nearlyEquals(symmetryY)) {
									this.linkArc(nodesX, nodesY, edgesX, edgesY, arc);
								}
							}
						}
					}
				}
			}
		}

		int[] tableX = new int[nodesX.size()];
		int[] tableY = new int[nodesY.size()];
		int[] tableLP = new int[nodesX.size()];
		int[] tableRP = new int[nodesX.size()];
		int[] tablePL = new int[nodesY.size()];
		int[] tablePR = new int[nodesY.size()];
		int[] tableLB = new int[nodesX.size()];
		int[] tableRB = new int[nodesX.size()];
		int[] tableBL = new int[nodesY.size()];
		int[] tableBR = new int[nodesY.size()];
		for (int i = 0; i < tableX.length; i++) {
			tableX[i] = -1;
			tableLP[i] = -1;
			tableRP[i] = -1;
			tableLB[i] = -1;
			tableRB[i] = -1;
		}
		for (int i = 0; i < tableY.length; i++) {
			tableY[i] = -1;
			tablePL[i] = -1;
			tablePR[i] = -1;
			tableBL[i] = -1;
			tableBR[i] = -1;
		}

		double planeMin = Double.POSITIVE_INFINITY;
		double bottomMax = Double.NEGATIVE_INFINITY;
		Map<RoundNumber, Integer> planeNodesY = new HashMap<RoundNumber, Integer>();
		Map<RoundNumber, Integer> bottomNodesY = new HashMap<RoundNumber, Integer>();
		for (int i = 0; i < nodesY.size(); i++) {
			RoundNumber node = nodesY.get(i);
			if (Double.compare(node.getValue(), frontviewBound.getMaxY()) > 0) {
				planeNodesY.put(new RoundNumber(node.getValue()), i);
				if (planeMin > node.getValue()) {
					planeMin = node.getValue();
				}
			} else if (Double.compare(node.getValue(), frontviewBound.getMinY()) < 0) {
				bottomNodesY.put(new RoundNumber(node.getValue()), i);
				if (bottomMax < node.getValue()) {
					bottomMax = node.getValue();
				}
			}
		}
		for (RoundNumber node : planeNodesY.keySet()) {
			node.sub(planeMin);
		}
		for (RoundNumber node : bottomNodesY.keySet()) {
			node.sub(bottomMax);
		}
		for (Map.Entry<RoundNumber, Integer> entry1 : planeNodesY.entrySet()) {
			RoundNumber planeNode = entry1.getKey();
			for (Map.Entry<RoundNumber, Integer> entry2 : bottomNodesY.entrySet()) {
				RoundNumber bottomNode = entry2.getKey();
				if (RoundNumber.nearlyAbsEquals(planeNode.getValue(), bottomNode.getValue())) {
					tableY[entry1.getValue()] = entry2.getValue();
					tableY[entry2.getValue()] = entry1.getValue();
					break;
				}
			}
		}

		double rightMin = Double.POSITIVE_INFINITY;
		double leftMax = Double.NEGATIVE_INFINITY;
		Map<RoundNumber, Integer> rightNodesX = new HashMap<RoundNumber, Integer>();
		Map<RoundNumber, Integer> leftNodesX = new HashMap<RoundNumber, Integer>();
		for (int i = 0; i < nodesX.size(); i++) {
			RoundNumber node = nodesX.get(i);
			if (Double.compare(node.getValue(), frontviewBound.getMaxX()) > 0) {
				rightNodesX.put(new RoundNumber(node.getValue()), i);
				if (rightMin > node.getValue()) {
					rightMin = node.getValue();
				}
			} else if (Double.compare(node.getValue(), frontviewBound.getMinX()) < 0) {
				leftNodesX.put(new RoundNumber(node.getValue()), i);
				if (leftMax < node.getValue()) {
					leftMax = node.getValue();
				}
			}
		}
		for (RoundNumber node : rightNodesX.keySet()) {
			node.sub(rightMin);
		}
		for (RoundNumber node : leftNodesX.keySet()) {
			node.sub(leftMax);
		}

		for (Map.Entry<RoundNumber, Integer> entry1 : rightNodesX.entrySet()) {
			RoundNumber planeNode = entry1.getKey();
			for (Map.Entry<RoundNumber, Integer> entry2 : leftNodesX.entrySet()) {
				RoundNumber bottomNode = entry2.getKey();
				if (RoundNumber.nearlyAbsEquals(planeNode.getValue(), bottomNode.getValue())) {
					tableX[entry1.getValue()] = entry2.getValue();
					tableX[entry2.getValue()] = entry1.getValue();
					break;
				}
			}
		}

		for (Map.Entry<RoundNumber, Integer> entry1 : planeNodesY.entrySet()) {
			RoundNumber planeNode = entry1.getKey();
			for (Map.Entry<RoundNumber, Integer> entry2 : leftNodesX.entrySet()) {
				RoundNumber leftNode = entry2.getKey();
				if (RoundNumber.nearlyAbsEquals(planeNode.getValue(), leftNode.getValue())) {
					tablePL[entry1.getValue()] = entry2.getValue();
					tableLP[entry2.getValue()] = entry1.getValue();
					break;
				}
			}
			for (Map.Entry<RoundNumber, Integer> entry2 : rightNodesX.entrySet()) {
				RoundNumber rightNode = entry2.getKey();
				if (RoundNumber.nearlyAbsEquals(planeNode.getValue(), rightNode.getValue())) {
					tablePR[entry1.getValue()] = entry2.getValue();
					tableRP[entry2.getValue()] = entry1.getValue();
					break;
				}
			}
		}

		for (Map.Entry<RoundNumber, Integer> entry1 : bottomNodesY.entrySet()) {
			RoundNumber bottomNode = entry1.getKey();
			for (Map.Entry<RoundNumber, Integer> entry2 : leftNodesX.entrySet()) {
				RoundNumber leftNode = entry2.getKey();
				if (RoundNumber.nearlyAbsEquals(bottomNode.getValue(), leftNode.getValue())) {
					tableBL[entry1.getValue()] = entry2.getValue();
					tableLB[entry2.getValue()] = entry1.getValue();
					break;
				}
			}
			for (Map.Entry<RoundNumber, Integer> entry2 : rightNodesX.entrySet()) {
				RoundNumber rightNode = entry2.getKey();
				if (RoundNumber.nearlyAbsEquals(bottomNode.getValue(), rightNode.getValue())) {
					tableBR[entry1.getValue()] = entry2.getValue();
					tableRB[entry2.getValue()] = entry1.getValue();
					break;
				}
			}
		}

		for (int i = 0; i < edgesX.length; i++) {
			for (int j = 0; j < edgesX[i].length; j++) {
				if (edgesX[i][j]) {
					if (tableX[i] >= 0 && tableX[j] >= 0) {
						edgesX[tableX[i]][tableX[j]] = true;
						edgesX[tableX[j]][tableX[i]] = true;
					}
					if (tableLP[i] >= 0 && tableLP[j] >= 0) {
						edgesY[tableLP[i]][tableLP[j]] = true;
						edgesY[tableLP[j]][tableLP[i]] = true;
					}
					if (tableRP[i] >= 0 && tableRP[j] >= 0) {
						edgesY[tableRP[i]][tableRP[j]] = true;
						edgesY[tableRP[j]][tableRP[i]] = true;
					}
					if (tableLB[i] >= 0 && tableLB[j] >= 0) {
						edgesY[tableLB[i]][tableLB[j]] = true;
						edgesY[tableLB[j]][tableLB[i]] = true;
					}
					if (tableRB[i] >= 0 && tableRB[j] >= 0) {
						edgesY[tableRB[i]][tableRB[j]] = true;
						edgesY[tableRB[j]][tableRB[i]] = true;
					}
				}
			}
		}
		for (int i = 0; i < edgesY.length; i++) {
			for (int j = 0; j < edgesY[i].length; j++) {
				if (edgesY[i][j]) {
					if (tableY[i] >= 0 && tableY[j] >= 0) {
						edgesY[tableY[i]][tableY[j]] = true;
						edgesY[tableY[j]][tableY[i]] = true;
					}
					if (tablePL[i] >= 0 && tablePL[j] >= 0) {
						edgesX[tablePL[i]][tablePL[j]] = true;
						edgesX[tablePL[j]][tablePL[i]] = true;
					}
					if (tablePR[i] >= 0 && tablePR[j] >= 0) {
						edgesX[tablePR[i]][tablePR[j]] = true;
						edgesX[tablePR[j]][tablePR[i]] = true;
					}
					if (tableBL[i] >= 0 && tableBL[j] >= 0) {
						edgesX[tableBL[i]][tableBL[j]] = true;
						edgesX[tableBL[j]][tableBL[i]] = true;
					}
					if (tableBR[i] >= 0 && tableBR[j] >= 0) {
						edgesX[tableBR[i]][tableBR[j]] = true;
						edgesX[tableBR[j]][tableBR[i]] = true;
					}
				}
			}
		}
		
		Map<DxfLine, Point2D> skewDimsAssist = new HashMap<DxfLine, Point2D>();
		for (DxfDimension dim : dimOthers) {
			if (dim.hasDimension()) {
				List<DxfLine> assist = dim.getDimensionAssist();
				if (assist.size() == 2) {
					DxfLine l1 = assist.get(0);
					DxfLine l2 = assist.get(1);
					double dx1 = l1.getX1() - l1.getX2();
					double dy1 = l1.getY1() - l1.getY2();
					double dx2 = l2.getX1() - l2.getX2();
					double dy2 = l2.getY1() - l2.getY2();
					if (!RoundNumber.nearlyAbsEquals(dx1 * dy2, dx2 *dy1)) {
						Point2D pt = l1.getCrossPoint(l2);
						if (l1.getDirection() != DxfLine.DIRECTION_X && l1.getDirection() != DxfLine.DIRECTION_Y) {
							this.errors.add(l1);
							skewDimsAssist.put(l1, pt);
						}
						if (l2.getDirection() != DxfLine.DIRECTION_X && l2.getDirection() != DxfLine.DIRECTION_Y) {
							this.errors.add(l2);
							skewDimsAssist.put(l2, pt);
						}
					}
				}
			}
		}

		// 円弧を含む寸法線の処理
		for (DxfDimension dim : dimOthers) {
			if (dim.hasDimension()) {
				for (DxfLine l : dim.getDimensionAssist()) {
					List<Point2D> pts = new ArrayList<Point2D>(); 
					for (DxfDimension dim2 : dimOthers) {
						if (dim.equals(dim2)) continue;
						for (DxfLine l2 : dim2.getDimensionAssist()) {
							if (RoundNumber.nearlyEqualsZero(l.innerProduct(l2))) {
								Point2D p = l.getCrossPoint(l2);
								pts.add(p);
							}
						}
						if (!pts.isEmpty()) {
							Iterator<Point2D> itr = pts.iterator();
							Point2D p1 = itr.next();
							while (itr.hasNext()) {
								Point2D p2 = itr.next();
								double x = (p1.getX() + p2.getX()) / 2;
								double y = (p1.getY() + p2.getY()) / 2;
								List<Point2D> checkPts = new ArrayList<Point2D>();
								for (DxfLine cl : centerLines) {
									if (RoundNumber.nearlyEqualsZero(cl.ptLineDistSq(x, y))) {
										for (Map.Entry<DxfLine, Point2D> entry : skewDimsAssist.entrySet()) {
											DxfLine skewline = entry.getKey();
											if (skewline.equalsLine(cl)) {
												checkPts.add(entry.getValue());
											}
										}
									}
								}
								this.linkEdge(nodesX, edgesX, p1.getX(), p2.getX());
								this.linkEdge(nodesY, edgesY, p1.getY(), p2.getY());
								for (Point2D pt : checkPts) {
									this.linkEdge(nodesX, edgesX, pt.getX(), p1.getX());
									this.linkEdge(nodesX, edgesX, pt.getX(), p2.getX());
									this.linkEdge(nodesY, edgesY, pt.getY(), p1.getY());
									this.linkEdge(nodesY, edgesY, pt.getY(), p2.getY());
								}
								p1 = p2;
							}
							pts.clear();
						}
					}
				}
			}
			if (dim.hasIndicated()) {
				Point2D pt = dim.getIndicated().getPoint2D();
				for (DxfLine line : lines) {
					double segDistSq = line.ptSegDistSq(pt);
					for (DxfLine assist : dim.getDimensionAssist()) {
						if (RoundNumber.nearlyEquals(segDistSq, 0) && assist.checkAngle(line) && line.getDirection() == 0) {
							// 角度が与えられた面取りに対する処理 ex.Y9906-A-302.DXF
							this.linkChamfer(nodesX, nodesY, edgesX, edgesY, line);
						}
					}
				}
			}
		}

		/**
		 * 寸法線が存在しないノードをチェックする
		 */
		boolean[] vpX = new boolean[edgesX.length];
		boolean[] vpY = new boolean[edgesY.length];

		Map<RoundNumber, RoundNumber> rectX = new HashMap<RoundNumber, RoundNumber>();
		Map<RoundNumber, RoundNumber> rectY = new HashMap<RoundNumber, RoundNumber>();
		for (Rectangle2D bounds : dxf.getBounds()) {
			rectX.put(new RoundNumber(bounds.getMinX()), new RoundNumber(bounds.getMaxX()));
			rectY.put(new RoundNumber(bounds.getMinY()), new RoundNumber(bounds.getMaxY()));
		}

		for (Map.Entry<RoundNumber, RoundNumber> entry : rectX.entrySet()) {
			RoundNumber min = entry.getKey();
			RoundNumber max = entry.getKey();
			for (int i = 0; i < nodesX.size(); i++) {
				RoundNumber num = nodesX.get(i);
				if (num.compareTo(min) >= 0 && num.compareTo(max) <= 0) {
					this.search(edgesX, vpX, i);
					break;
				}
			}
		}

		for (Map.Entry<RoundNumber, RoundNumber> entry : rectY.entrySet()) {
			RoundNumber min = entry.getKey();
			RoundNumber max = entry.getKey();
			for (RoundNumber num : nodesY) {
				if (num.compareTo(min) >= 0 && num.compareTo(max) <= 0) {
					int index = nodesY.indexOf(num);
					if (index >= 0) {
						this.search(edgesY, vpY, index);
						break;
					}
				}
			}
		}

		dxf.addCheck(nodesX, nodesY, edgesX, edgesY, vpX, vpY, setLoaX, setLoaY);

		int error = this.getErrors(nodesX, nodesY, vpX, vpY) + this.errors.size();

		if (error == 0) {
			return true;
		} else {
			System.out.println("DIMENSION CHECKER: " + error);
			return false;
		}
	}

	private void checkEqualsInterval(ListSet<DimensionNode> nodesX, ListSet<DimensionNode> nodesY, boolean[][] edgesX, boolean[][] edgesY, Set<DxfCircle> list) {
		DxfCircle[] dest = list.toArray(new DxfCircle[]{});
		System.out.println("EQL" + dest.length);
		for (int j = 1; j < dest.length - 1; j++) {
			DxfCircle base = dest[j - 1];
			int minI = -1;
			double min = Double.POSITIVE_INFINITY;
			for (int i = j; i < dest.length; i++) {
				DxfCircle c = dest[i];
				double dist = Point2D.distanceSq(c.getX(), c.getY(), base.getX(), base.getY());
				if (dist < min) {
					min = dist;
					minI = i;
				}
			}
			assert minI >= 0;
			DxfCircle tmp = dest[minI];
			dest[minI] = dest[j];
			dest[j] = tmp;
		}
		DxfCircle c1 = dest[dest.length - 2];
		DxfCircle c2 = dest[dest.length - 1];
		double d1 = Point2D.distanceSq(c1.getX(), c1.getY(), c2.getX(), c2.getY());
		boolean flag = true;
		for (int i = 0; i < dest.length; i++) {
			DxfCircle c3 = dest[i];
			double d2 = Point2D.distanceSq(c2.getX(), c2.getY(), c3.getX(), c3.getY());
			if (!RoundNumber.nearlyEquals(d1, d2)) {
				flag = false;
				break;
			}
			d1 = d2;
			c1 = c2;
			c2 = c3;
		}
		if (flag) {
			c1 = dest[0];
			for (int i = 1; i < dest.length; i++) {
				c2 = dest[i];
				this.linkEdge(nodesX, edgesX, c1.getX(), c2.getX());
				this.linkEdge(nodesY, edgesY, c1.getY(), c2.getY());
				c1 = c2;
			}
		}
	}

	private void clearErrorEntities() {
		for (DxfEntity entity : this.errors) {
			if (entity != null) {
				entity.clearError();
			}
		}
		this.errors.clear();
	}

	/**
	 * 寸法線チェックのためのグラフの辺の接続（曲面）
	 * 
	 * @param nodesX
	 * @param nodesY
	 * @param edgesX
	 * @param edgesY
	 * @param line
	 * @return
	 */
	private boolean linkArc(ListSet<DimensionNode> nodesX, ListSet<DimensionNode> nodesY, boolean[][] edgesX, boolean[][] edgesY, DxfArc arc) {
		Point2D pt1 = arc.getStartPoint();
		Point2D pt2 = arc.getEndPoint();
		int indexX1 = nodesX.indexOf(new RoundNumber(pt1.getX()));
		int indexX2 = nodesX.indexOf(new RoundNumber(pt2.getX()));
		int indexX3 = nodesX.indexOf(new RoundNumber(arc.getMinX()));
		int indexX4 = nodesX.indexOf(new RoundNumber(arc.getMaxX()));
		int indexY1 = nodesY.indexOf(new RoundNumber(pt1.getY()));
		int indexY2 = nodesY.indexOf(new RoundNumber(pt2.getY()));
		int indexY3 = nodesY.indexOf(new RoundNumber(arc.getMinY()));
		int indexY4 = nodesY.indexOf(new RoundNumber(arc.getMaxY()));

		if (indexX1 >= 0 && indexX2 >= 0 && indexY1 >= 0 && indexY2 >= 0) {
			this.linkEdge(edgesX, indexX1, indexX2);
			this.linkEdge(edgesY, indexY1, indexY2);
			if (indexX3 >= 0 && indexY3 >= 0) {
				this.linkEdge(edgesX, indexX1, indexX3);
				this.linkEdge(edgesY, indexY1, indexY3);
			}
			if (indexX4 >= 0 && indexY4 >= 0) {
				this.linkEdge(edgesX, indexX2, indexX4);
				this.linkEdge(edgesY, indexY2, indexY4);
			}
			return true;
		}
		return false;
	}

	/**
	 * 寸法線チェックのためのグラフの辺の接続（面取り）
	 * 
	 * @param nodesX
	 * @param nodesY
	 * @param edgesX
	 * @param edgesY
	 * @param line
	 * @return
	 */
	private boolean linkChamfer(ListSet<DimensionNode> nodesX, ListSet<DimensionNode> nodesY, boolean[][] edgesX, boolean[][] edgesY, DxfLine line) {
		int indexX1 = nodesX.indexOf(new RoundNumber(line.getX1()));
		int indexX2 = nodesX.indexOf(new RoundNumber(line.getX2()));
		int indexY1 = nodesY.indexOf(new RoundNumber(line.getY1()));
		int indexY2 = nodesY.indexOf(new RoundNumber(line.getY2()));
		if (indexX1 >= 0 && indexX2 >= 0 && indexY1 >= 0 && indexY2 >= 0) {
			this.linkEdge(edgesX, indexX1, indexX2);
			this.linkEdge(edgesY, indexY1, indexY2);
			return true;
		}
		return false;
	}

	/**
	 * 寸法線チェックのためのグラフの辺の接続（円）
	 * 
	 * @param nodesX
	 * @param nodesY
	 * @param edgesX
	 * @param edgesY
	 * @param circle
	 * @return
	 */
	private boolean linkCircle(ListSet<DimensionNode> nodesX, ListSet<DimensionNode> nodesY, boolean[][] edgesX, boolean[][] edgesY, DxfCircle circle) {
		int indexX1 = nodesX.indexOf(new RoundNumber(circle.getX()));
		int indexX2 = nodesX.indexOf(new RoundNumber(circle.getMaxX()));
		int indexX3 = nodesX.indexOf(new RoundNumber(circle.getMinX()));
		int indexY1 = nodesY.indexOf(new RoundNumber(circle.getY()));
		int indexY2 = nodesY.indexOf(new RoundNumber(circle.getMaxY()));
		int indexY3 = nodesY.indexOf(new RoundNumber(circle.getMinY()));
		if (indexX1 >= 0 && indexY1 >= 0) {
			if (indexX2 >= 0 && indexY2 >= 0) {
				this.linkEdge(edgesX, indexX1, indexX2);
				this.linkEdge(edgesY, indexY1, indexY2);
			}
			if (indexX3 >= 0 && indexY3 >= 0) {
				this.linkEdge(edgesX, indexX1, indexX3);
				this.linkEdge(edgesY, indexY1, indexY3);
			}
			return true;
		}
		return false;
	}

	/**
	 * 寸法線チェックのためのグラフの辺の接続
	 * 
	 * @param edge
	 * @param index1
	 * @param index2
	 */
	private void linkEdge(boolean[][] edge, int index1, int index2) {
		edge[index1][index2] = true;
		edge[index2][index1] = true;
	}

	private void linkEdge(ListSet<DimensionNode> nodes, boolean[][] edge, double v1, double v2) {
		RoundNumber r1 = new RoundNumber(v1);
		RoundNumber r2 = new RoundNumber(v2);
		int index1 = nodes.indexOf(r1);
		int index2 = nodes.indexOf(r2);
		if (index2 > 0 && index1 > 0) {
			edge[index1][index2] = true;
			edge[index2][index1] = true;
		}
	}

	/**
	 * 寸法線チェックのためのグラフの辺の接続
	 * 
	 * @param edge
	 * @param index1
	 * @param index2
	 * @param index3
	 */
	private void linkEdge(boolean[][] edge, int index1, int index2, int index3) {
		edge[index1][index3] = true;
		edge[index3][index1] = true;
		edge[index2][index3] = true;
		edge[index3][index2] = true;
	}

	/**
	 * 中心線の位置を座標別に取得するメソッド
	 * @param lines 直線のリスト
	 * @param centersX X座標の中心線の位置をいれる集合
	 * @param centersY Y座標の中心線の位置をいれる集合
	 */
	private void getCenterLinePoints(List<DxfLine> lines, Set<RoundNumber> centersX, Set<RoundNumber> centersY) {
		for (DxfLine line : lines) {
			if ("CENTER".equals(line.getStyle())) {
				if (line.getDirection() == DxfLine.DIRECTION_X) { // X軸方向の中心線をY軸方向では利用
					centersY.add(new RoundNumber(line.getY1()));
				} else if (line.getDirection() == DxfLine.DIRECTION_Y) { // Y軸方向の中心線をX軸方向では利用
					centersX.add(new RoundNumber(line.getX2()));
				}
			}
		}
	}

	/**
	 * @see dxf.checker.DxfChecker#getErrorEntities()
	 */
	public Collection<DxfEntity> getErrorEntities() {
		return this.errors;
	}

	/**
	 * @see dxf.checker.DxfChecker#getErrorMessage()
	 */

	public String getErrorMessage() {
		return "寸法抜けを確認してください。";
	}

	/**
	 * 寸法抜けエラーの数をチェックする
	 * 
	 * @param nodesX
	 *            寸法線チェックのためのノード(横)
	 * @param nodesY
	 *            寸法線チェックのためのノード(縦)
	 * @param vpX
	 *            チェック済みの寸法線のためのノード(横)
	 * @param vpY
	 *            チェック済みの寸法線のためのノード(縦)
	 * @return 寸法抜けエラーの数
	 */
	private int getErrors(List<DimensionNode> nodesX, List<DimensionNode> nodesY, boolean[] vpX, boolean[] vpY) {
		int error = 0;
		for (int i = 0; i < nodesX.size(); i++) {
			DimensionNode node = nodesX.get(i);
			if (node.isReqDim() && !vpX[i]) {
				error++;
			}
		}
		for (int i = 0; i < nodesY.size(); i++) {
			DimensionNode node = nodesY.get(i);
			if (node.isReqDim() && !vpY[i]) {
				error++;
			}
		}
		return error;
	}

	public void search(boolean[][] edges, boolean[] vp, int index) {
		for (int i = 0; i < edges.length; i++) {
			if (i != index && !vp[i] && edges[index][i]) {
				vp[i] = true;
				this.search(edges, vp, i);
			}
		}
	}

	public void search(boolean[][] edges, boolean[] vp, int[] start) {
		for (int i = 0; i < start.length; i++) {
			int index = start[i];
			vp[index] = true;
			this.search(edges, vp, index);
		}
	}

	@Override
	public String toString() {
		return "寸法抜けのチェック";
	}
}

package dxf.checker;

import gui.DxfViewer;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.MapList;

import dxf.DxfData;
import dxf.section.entities.DxfCircle;
import dxf.section.entities.DxfEntity;
import dxf.section.entities.DxfInsert;

/**
 * ねじの数をチェックするクラス もはやDimensionCheckerで済ませられる．
 * 
 * @author FUJIWARA Masayasu
 * @since 0.02
 */
public class ScrewChecker implements DxfChecker {

	/*
	 * public static void main(String[] args) { String str = "5-φ7"; Matcher
	 * matcher = screwPattern.matcher(str); if (matcher.find()) {
	 * System.out.println("match"); } }
	 */

	private class ScrewEntry {
		int count = 0;
		double diameter;
		double dimension;
		Collection<DxfInsert> inserts;

		ScrewEntry(double diameter, double radius) {
			this.dimension = diameter;
			this.diameter = radius;
			this.inserts = new ArrayList<DxfInsert>();
		}

		double getScale() {
			return this.diameter / this.dimension;
		}
	}

	/**
	 * ねじ検出のための正規表現パターン
	 */
	private final static Pattern screwPattern = Pattern.compile("^(?:(\\d+)(?:×(\\d+))?-)?(?:φ|M)?(\\d+(?:\\.\\d+)?)(?:$|[^-])");

	/**
	 * 異常を検出したエンティティ
	 */
	private Collection<DxfEntity> errors = new ArrayList<DxfEntity>();

	/**
	 * ねじのチェックを行うためのメソッド
	 * 
	 * @param dxf
	 *            チェックを行うDXFファイルのデータ
	 * @return ねじに問題が見つからなければtrue、問題がみつかればfalseを返す。
	 */
	public boolean check(DxfData dxf) {
		this.clearErrorEntities();
		boolean flag = true;
		Map<Double, ScrewEntry> entryMap = new HashMap<Double, ScrewEntry>();
		MapList<String, DxfEntity> dxfMap = dxf.getDxfData();
		Rectangle2D screen = dxf.getDefaultScreen();
		Collection<DxfCircle> circles = dxf.getDxfCircles();
		List<DxfEntity> entities = dxfMap.get(DxfInsert.class.getName());
		if (entities != null) {
			for (DxfEntity entity : entities) {
				DxfInsert insert = (DxfInsert) entity;
				String text = insert.getText();
				if (text != null) {
					Matcher matcher = ScrewChecker.screwPattern.matcher(text);
					if (matcher.find()) {
						String match1 = matcher.group(1);
						String match2 = matcher.group(2);
						int count1 = match1 != null ? Integer.parseInt(match1) : 1;
						int count2 = match2 != null ? Integer.parseInt(match2) : 1;
						double dimension = Double.parseDouble(matcher.group(3));
						Point2D indicated = insert.getIndicatedPoint();
						if (indicated != null) {
							DxfCircle circle = this.getNearestNeighbor(indicated, circles);
							if (circle != null) {
								double diameter = circle.getDiameter();
								ScrewEntry entry = entryMap.get(diameter);
								if (entry == null) {
									entry = new ScrewEntry(dimension, diameter);
									entryMap.put(diameter, entry);
								}
								entry.count += count1 * count2;
								entry.inserts.add(insert);
							}
						}
					}
				}
			}
		}
		for (ScrewEntry entry : entryMap.values()) {
			int countCircle = this.countCircles(entry.diameter, circles, screen);
			if (countCircle != entry.count) {
				this.errors.addAll(this.getCircles(entry.diameter, circles, screen));
				this.errors.addAll(entry.inserts);
				System.out.println(DxfData.class.getName() + " # errored Circle(" + entry.dimension + "): " + countCircle);
				flag = false;
			} else {
				System.out.println(DxfData.class.getName() + " # checked Circle(" + entry.dimension + "): " + countCircle);
			}
			if (DxfViewer.debug) {
				System.out.println("scale: " + entry.getScale());
			}
		}
		for (DxfEntity entity : this.errors) {
			entity.setError();
		}
		return flag;
	}

	private void clearErrorEntities() {
		for (DxfEntity entity : this.errors) {
			entity.clearError();
		}
		this.errors.clear();
	}

	/**
	 * 指定した半径の円をカウントするメソッド
	 * 
	 * @param diameter
	 *            カウントする円の直径
	 * @param circles
	 *            対象となる円の集合
	 * @param screen
	 *            検出範囲
	 * @return カウントされた円の数
	 */
	private int countCircles(double diameter, Collection<DxfCircle> circles, Rectangle2D screen) {
		int count = 0;
		for (DxfCircle circle : circles) {
			if (screen.contains(circle.getX(), circle.getY())) {
				if (Double.compare(circle.getDiameter(), diameter) == 0) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * 指定した半径の円を取得するメソッド
	 * 
	 * @param diameter
	 *            取得する円の直径
	 * @param circles
	 *            対象となる円集合
	 * @param screen
	 *            検出範囲
	 * @return 取得した円のインスタンス
	 */
	private Collection<DxfCircle> getCircles(double diameter, Collection<DxfCircle> circles, Rectangle2D screen) {
		Collection<DxfCircle> ret = new ArrayList<DxfCircle>();
		for (DxfCircle circle : circles) {
			if (screen.contains(circle.getX(), circle.getY())) {
				if (Double.compare(circle.getDiameter(), diameter) == 0) {
					ret.add(circle);
				}
			}
		}
		return ret;
	}

	/**
	 * 異常を検出したエンティティを取得するメソッド
	 * 
	 * @return 異常を検出したエンティティ
	 */
	public Collection<DxfEntity> getErrorEntities() {
		return this.errors;
	}

	/**
	 * エラーメッセージを取得するメソッド
	 * 
	 * @return エラーメッセージ
	 */
	public String getErrorMessage() {
		return "ねじの数を確認してください。";
	}

	/**
	 * 最近傍の円を取得します。
	 * 
	 * @param p
	 *            最近傍の円を求める頂点
	 * @param entities
	 *            最近傍の円を求める円集合
	 * @return 最近傍の円
	 */
	public DxfCircle getNearestNeighbor(Point2D p, Collection<DxfCircle> entities) {
		double minDistSq = 2; // 閾値
		DxfCircle nearestCircle = null;
		if (entities != null) {
			for (DxfCircle circle : entities) {
				double distSq = circle.getDistSq(p);
				if (minDistSq > distSq) {
					minDistSq = distSq;
					nearestCircle = circle;
				}
			}
		}
		return nearestCircle;
	}

	@Override
	public String toString() {
		return "ねじ穴のチェック";
	}
}

package dxf.section.entities;

import gui.ViewingEnvironment;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import dxf.DxfData;
import dxf.checker.DimensionNode;

/**
 * DXFファイルの図形インターフェース
 * 
 * @author FUJIWARA Masayasu
 * @since 0.01
 */
public interface DxfEntity {
	/**
	 * 異常のある構成要素としての設定を解除するメソッド
	 */
	public void clearError();

	/**
	 * 図形を描画するメソッド
	 * 
	 * @param g
	 *            図形を描画するGraphics2D
	 * @param env
	 *            表示環境設定
	 */
	public void draw(Graphics2D g, ViewingEnvironment env);

	/**
	 * 図形データを包含する最小の長方形を返すメソッド
	 * 
	 * @return 図形データを包含する最小の長方形
	 */
	public Rectangle2D getBounds2D();

	public void getCheckPoints(Collection<DimensionNode> pointsX, Collection<DimensionNode> pointsY);

	public boolean intersects(Rectangle2D rect);

	/**
	 * 構成要素が図形に含まれているかどうか確認するメソッド
	 * 
	 * @param shape
	 *            図形
	 * @return 含まれていればtrue、含まれていなければfalseを返します。
	 */
	public boolean isContained(Shape shape);

	/**
	 * 選択可能であるかどうか確認するメソッド 一般に、テキスト、寸法線、中心線などは選択不可とする
	 * 
	 * @return 選択可能であればtrue、選択できなければfalseを返します。
	 */
	public boolean isSelectable();

	/**
	 * パラメータを他のセクションのデータにより補完するためのメソッド
	 * 
	 * @param dxf
	 *            DXFファイルのデータ
	 */
	public boolean link(DxfData dxf);

	/**
	 * 異常のある構成要素として設定するメソッド
	 */
	public void setError();

	/**
	 * 図形データを位置を変更するメソッド
	 * 
	 * @param dx
	 *            X座標軸の移動量
	 * @param dy
	 *            Y座標軸の移動量
	 */
	public void transform(double dx, double dy);
}

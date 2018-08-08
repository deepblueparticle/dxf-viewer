/**
 * 
 */
package dxf.checker;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import util.RoundNumber;

import dxf.DxfData;
import dxf.section.entities.DxfEntity;
import dxf.section.entities.DxfShape;

/**
 * 使用していない
 * 
 * @author FUJIWARA Masayasu
 */
public class PositionChecker implements DxfChecker {

	private Collection<DxfEntity> errors;

	/**
	 * 
	 */
	public PositionChecker() {
		this.errors = new ArrayList<DxfEntity>();
	}

	/**
	 * 各図面の位置の確認をするメソッド
	 * 
	 * @param dxf
	 *            DXFファイル
	 * @return 異常のあるエンティティ
	 * @see dxf.checker.DxfChecker#check(dxf.DxfData)
	 */

	public boolean check(DxfData dxf) {
		this.clearErrorEntities();

		Map<String, ProjectionView> map = dxf.getProjectionViewMap();
		List<DxfEntity> entities = new ArrayList<DxfEntity>();
		if (map != null) {
			ProjectionView front = map.get(DxfData.LABEL_FRONT_VIEW);
			Rectangle2D frontBounds = front.getBounds();
			for (ProjectionView view : map.values()) {
				if (view.getLayout() == ProjectionView.LAYOUT_X) {
					Rectangle2D b1 = view.getBounds();
					if (frontBounds != null && b1 != null) {
						if (!RoundNumber.nearlyEquals(frontBounds.getMinX(), b1.getMinX())) {
							System.out.println("--");
							System.out.println("Front minX = " + frontBounds.getMinX());
							System.out.println(view.getLabel() + " minX = " + b1.getMinX());
							DxfShape entity = new DxfShape(view.getBounds());
							entity.setError();
							entities.add(entity);
						} else if (!RoundNumber.nearlyEquals(frontBounds.getMaxX(), b1.getMaxX())) {
							System.out.println("--");
							System.out.println("Front maxX = " + frontBounds.getMaxX());
							System.out.println(view.getLabel() + "maxX = " + b1.getMaxX());
							DxfShape entity = new DxfShape(view.getBounds());
							entity.setError();
							entities.add(entity);
						}
					}
				}
			}
			for (ProjectionView view : map.values()) {
				if (view.getLayout() == ProjectionView.LAYOUT_X) {
					Rectangle2D b1 = view.getBounds();
					if (frontBounds != null && b1 != null) {
						if (!RoundNumber.nearlyEquals(frontBounds.getMinY(), b1.getMinY())) {
							System.out.println("--");
							System.out.println("Front minY = " + frontBounds.getMinY());
							System.out.println(view.getLabel() + "minY = " + b1.getMinY());
							DxfShape entity = new DxfShape(view.getBounds());
							entity.setError();
							entities.add(entity);
						} else if (!RoundNumber.nearlyEquals(frontBounds.getMaxY(), b1.getMaxY())) {
							System.out.println("--");
							System.out.println("Front maxY = " + frontBounds.getMaxY());
							System.out.println(view.getLabel() + "maxY = " + b1.getMaxY());
							DxfShape entity = new DxfShape(view.getBounds());
							entity.setError();
							entities.add(entity);
						}
					}
				}
			}
		}
		if (this.errors.size() > 0) {
			System.out.println(this.getClass().getSimpleName() + " : Error " + this.errors.size());
		}
		return this.errors.isEmpty();
	}

	private void clearErrorEntities() {
		for (DxfEntity entity : this.errors) {
			entity.clearError();
		}
		this.errors.clear();
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
		return "図面の位置がズレていないか確認してください。";
	}

	@Override
	public String toString() {
		return "図面の位置のチェック";
	}
}

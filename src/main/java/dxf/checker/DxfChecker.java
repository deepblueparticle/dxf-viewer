/**
 * 
 */
package dxf.checker;

import java.util.Collection;

import dxf.DxfData;
import dxf.section.entities.DxfEntity;

/**
 * DXFファイルの製図のチェックのためのインターフェース
 * 
 * @author FUJIWARA Masayasu
 * @since 0.02
 */
public interface DxfChecker {

	/**
	 * DXFファイルをチェックするメソッド
	 * 
	 * @param dxf
	 *            チェックするDXFファイルのデータ
	 * @return 異常がなければtrue、あればfalse
	 */
	public boolean check(DxfData dxf);

	/**
	 * 異常を検出したエンティティを取得するメソッド
	 * 
	 * @return 異常を検出したエンティティ
	 */
	public Collection<DxfEntity> getErrorEntities();

	/**
	 * エラーメッセージを取得するメソッド
	 * 
	 * @return エラーメッセージ
	 */
	public String getErrorMessage();
}

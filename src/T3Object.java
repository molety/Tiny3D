/**
 * オブジェクト
 * (モデリング-ビューイング変換を受けるものの基底)
 */
public class T3Object
{
	/** モデリング変換行列 */
	public T3Matrix transform;

	/**
	 * コンストラクタ
	 */
	public T3Object()
	{
		transform = new T3Matrix();
	}

	/**
	 * モデリング変換行列を設定する。
	 * @param m (IN ) 変換行列
	 */
	public void setTransform(T3Matrix m)
	{
		T3Matrix.copy(transform, m);
	}

	/**
	 * 既存のモデリング変換の末尾に変換を追加する。
	 * 新しい変換行列を左から掛ける。
	 * @param m (IN ) 新しい変換行列
	 */
	public void addTransform(T3Matrix m)
	{
		transform.multFromLeft(m);
	}

	/**
	 * 既存のモデリング変換の先頭に変換を追加する。
	 * 新しい変換行列を右から掛ける。
	 * OpenGLの考え方はこちら
	 * @param m (IN ) 新しい変換行列
	 */
	public void addTransformToHead(T3Matrix m)
	{
		transform.multFromRight(m);
	}

	/**
	 * ワールド内に配置する。
	 * @param world (IN ) ワールドオブジェクト
	 */
	public void place(T3World world)
	{
		T3Matrix.multMatrix(world.modelView, world.camera.transform, transform);
	}
}

/**
 * カメラ (視点)
 */
public class T3Camera extends T3Object
{
	/** x方向の単位ベクトルの変換先 */
	private static T3Vector tx = new T3Vector();
	/** y方向の単位ベクトルの変換先 */
	private static T3Vector ty = new T3Vector();
	/** z方向の単位ベクトルの変換先 */
	private static T3Vector tz = new T3Vector();
	/** ビューイング変換の回転成分 */
	private static T3Matrix rot = new T3Matrix();
	/** ビューイング変換の平行移動成分 */
	private static T3Matrix trans = new T3Matrix();
	/** ビューイング変換のroll成分 */
	private static T3Matrix rollM = new T3Matrix();
	/** ビューイング変換のpitch成分 */
	private static T3Matrix pitchM = new T3Matrix();
	/** ビューイング変換のyaw成分 */
	private static T3Matrix yawM = new T3Matrix();
	/** 目標位置を表すテンポラリ */
	private static T3Vector tempTarget = new T3Vector();
	/** 目標位置を表すテンポラリ */
	private static T3Vector tempTarget2 = new T3Vector();

	/**
	 * 視点を設定する。
	 * @param eye    (IN ) 視点位置
	 * @param target (IN ) 目標位置
	 * @param up     (IN ) 上方向
	 */
	public void lookAt(T3Vector eye, T3Vector target, T3Vector up)
	{
		// ※このアルゴリズムは http://3dinc.blog45.fc2.com/blog-entry-392.html を参考にしました。

		// 原点から-z方向を見て、+y方向を上とした時の視点を基準とする。
		// この基準視点から与えられた視点に移動するための変換を考える。
		// この変換の逆変換が、求めるビューイング変換になる。

		// 回転成分を求める
		T3Vector.diff(tz, eye, target);
		T3Vector.outerProduct(tx, up, tz);
		tz.normalize();
		tx.normalize();
		T3Vector.outerProduct(ty, tz, tx);	// tyのノルムは1になるので正規化不要
		rot.setRotateWithUnitVector(tx, ty, tz);

		// 平行移動成分はeyeそのもの

		// 基準視点からrotだけ回転してeyeだけ平行移動すれば与えられた視点になるから、
		// 求めるビューイング変換は：-eyeだけ平行移動→rotの逆に回転
		rot.transpose();					// 回転行列は転置すれば逆行列になる
		trans.setTranslate(-eye.x(), -eye.y(), -eye.z());
		T3Matrix.multMatrix(transform, rot, trans);
	}

	/**
	 * 角度(roll-pitch-yaw)と視点位置で視点を設定する。
	 * @param roll  (IN ) -z軸周りの回転角 (時計回りに傾く方が正)
	 * @param pitch (IN ) -x軸周りの回転角 (下を向く方が正)
	 * @param yaw   (IN ) y軸周りの回転角 (左を向く方が正)
	 * @param eye   (IN ) 視点位置
	 */
	public void lookAtByAngleAndEye(float roll, float pitch, float yaw, T3Vector eye)
	{
		rollM.setRotateZ(-roll);
		pitchM.setRotateX(-pitch);
		yawM.setRotateY(yaw);
		T3Matrix.multMatrix(rot, pitchM, rollM);
		rot.multFromLeft(yawM);
		rot.transpose();

		trans.setTranslate(-eye.x(), -eye.y(), -eye.z());
		T3Matrix.multMatrix(transform, rot, trans);
	}

	/**
	 * 角度(roll-pitch-yaw)と目標位置と距離で視点を設定する。
	 * @param roll     (IN ) -z軸周りの回転角 (時計回りに傾く方が正)
	 * @param pitch    (IN ) -x軸周りの回転角 (下を向く方が正)
	 * @param yaw      (IN ) y軸周りの回転角 (左を向く方が正)
	 * @param target   (IN ) 目標位置
	 * @param distance (IN ) 目標までの距離
	 */
	public void lookAtByAngleAndTarget(float roll, float pitch, float yaw, T3Vector target, float distance)
	{
		rollM.setRotateZ(-roll);
		pitchM.setRotateX(-pitch);
		yawM.setRotateY(yaw);
		T3Matrix.multMatrix(rot, pitchM, rollM);
		rot.multFromLeft(yawM);

		tempTarget.set(0.0F, 0.0F, -distance);
		T3Matrix.multVector(tempTarget2, rot, tempTarget);
		T3Vector.diff(tempTarget, tempTarget2, target);		// ※tempTargetを使い回した
		trans.setTranslate(tempTarget);

		rot.transpose();
		T3Matrix.multMatrix(transform, rot, trans);
	}
}

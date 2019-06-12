/**
 * 補間
 */
public class T3Interpolation
{
	/** 開始姿勢 */
	private T3Matrix startPosture;
	/** 終了姿勢 */
	private T3Matrix endPosture;
	/** 開始時 平行移動成分 */
	private T3Matrix startTranslation;
	/** 終了時 平行移動成分 */
	private T3Matrix endTranslation;
	/** 開始時 スケーリング成分 */
	private T3Matrix startScaling;
	/** 終了時 スケーリング成分 */
	private T3Matrix endScaling;
	/** 開始時 回転成分 */
	private T3Matrix startRotation;
	/** 終了時 回転成分 */
	private T3Matrix endRotation;
	/** 開始時 回転角 */
	private float startAngle;
	/** 終了時 回転角 */
	private float endAngle;
	/** 開始時 回転軸 */
	private T3Vector startAxis;
	/** 終了時 回転軸 */
	private T3Vector endAxis;
	/** 回転成分 合成用テンポラリ */
	private T3Matrix temp1Rotation;
	/** 回転成分 合成用テンポラリ */
	private T3Matrix temp2Rotation;
	/** 補間 平行移動成分 */
	private T3Matrix interTranslation;
	/** 補間 スケーリング成分 */
	private T3Matrix interScaling;
	/** 補間 回転成分 */
	private T3Matrix interRotation;
	/** 補間 */
//	private T3Matrix interpolate;
	/** 継続時間 */
	private float duration;
	/** 補間開始時からの経過時間 */
	private float currTime;

	/**
	 * コンストラクタ
	 */
	public T3Interpolation()
	{
		startTranslation = new T3Matrix();
		endTranslation = new T3Matrix();
		startScaling = new T3Matrix();
		endScaling = new T3Matrix();
		startRotation = new T3Matrix();
		endRotation = new T3Matrix();
		startAxis = new T3Vector();
		endAxis = new T3Vector();
		temp1Rotation = new T3Matrix();
		temp2Rotation = new T3Matrix();
		interTranslation = new T3Matrix();
		interScaling = new T3Matrix();
		interRotation = new T3Matrix();
	}

	/**
	 * 初期化。
	 * 1つの補間オブジェクトを使い回すことを想定しているため、
	 * コンストラクタとは別にしている。
	 * @param startPosture (IN ) 開始姿勢
	 * @param endPosture   (IN ) 終了姿勢
	 * @param duration     (IN ) 継続時間 (単位は問わない)
	 */
	public void init(T3Matrix startPosture, T3Matrix endPosture, float duration)
	{
		this.startPosture = startPosture;
		this.endPosture = endPosture;
		this.duration = duration;
		currTime = 0.0F;

		decompose(startTranslation, startScaling, startRotation, this.startPosture);
		decompose(endTranslation, endScaling, endRotation, this.endPosture);
		startAngle = rotateMatrixToAngleAndAxis(startAxis, startRotation);
		endAngle = rotateMatrixToAngleAndAxis(endAxis, endRotation);
	}

	/**
	 * 補間姿勢を得る。
	 * @param inter (OUT) 補間姿勢
	 * @param delta (IN ) 前回からの経過時間
	 */
	public void interpolate(T3Matrix inter, float delta)
	{
		currTime += delta;
		if (currTime > duration) {
			currTime = duration;
		}

		interpolateTranslation();
		interpolateScaling();
		interpolateRotation();

		T3Matrix.multMatrix(inter, interScaling, interRotation);
		inter.multFromLeft(interTranslation);
	}

	/**
	 * 補間動作が完了したかどうかを返す。
	 * @return 完了していたら真
	 */
	public boolean isEnd()
	{
		return (currTime >= duration);
	}

	/**
	 * 平行移動／スケーリング／回転が合成された変換行列を分解する。
	 * M = T・S・Rの形にした時のT / S / Rを求める。
	 * @param translate (OUT) 平行移動成分
	 * @param scale     (OUT) スケーリング成分
	 * @param rotate    (OUT) 回転成分
	 * @param m         (IN ) 元の変換行列
	 */
	private void decompose(T3Matrix translate, T3Matrix scale, T3Matrix rotate, T3Matrix m)
	{
		T3Matrix.copy(rotate, m);
		float[][] e = rotate.elem;

		float translateX = e[0][3];
		float translateY = e[1][3];
		float translateZ = e[2][3];
		translate.setTranslate(translateX, translateY, translateZ);
		e[0][3] = 0.0F;
		e[1][3] = 0.0F;
		e[2][3] = 0.0F;

		float scaleX = (float)Math.sqrt(e[0][0] * e[0][0] + e[0][1] * e[0][1] + e[0][2] * e[0][2]);
		float scaleY = (float)Math.sqrt(e[1][0] * e[1][0] + e[1][1] * e[1][1] + e[1][2] * e[1][2]);
		float scaleZ = (float)Math.sqrt(e[2][0] * e[2][0] + e[2][1] * e[2][1] + e[2][2] * e[2][2]);
		scale.setScale(scaleX, scaleY, scaleZ);
		e[0][0] /= scaleX;
		e[0][1] /= scaleX;
		e[0][2] /= scaleX;
		e[1][0] /= scaleY;
		e[1][1] /= scaleY;
		e[1][2] /= scaleY;
		e[2][0] /= scaleZ;
		e[2][1] /= scaleZ;
		e[2][2] /= scaleZ;

		if ((e[1][0] * e[2][1] - e[2][0] * e[1][1]) * e[0][2] < 0.0F
			|| (e[2][0] * e[0][1] - e[0][0] * e[2][1]) * e[1][2] < 0.0F
			|| (e[0][0] * e[1][1] - e[1][0] * e[0][1]) * e[2][2] < 0.0F) {
			// 左手系になっている場合、スケーリングを調整して右手系に直す
			/* (e[1][0] * e[2][1] - e[2][0] * e[1][1]) == e[0][2] かつ
			 * (e[2][0] * e[0][1] - e[0][0] * e[2][1]) == e[1][2] かつ
			 * (e[0][0] * e[1][1] - e[1][0] * e[0][1]) == e[2][2] であれば
			 * 右手系と確認できるのだが、演算誤差を考慮して符号のチェックにとどめている。
			 */
			scaleZ = -scaleZ;
			e[2][0] = -e[2][0];
			e[2][1] = -e[2][1];
			e[2][2] = -e[2][2];
		}
	}

	/**
	 * 平行移動を線形補間する。
	 */
	private void interpolateTranslation()
	{
		float ratio = currTime / duration;
		float interX = startTranslation.elem[0][3] * (1.0F - ratio)
		  + endTranslation.elem[0][3] * ratio;
		float interY = startTranslation.elem[1][3] * (1.0F - ratio)
		  + endTranslation.elem[1][3] * ratio;
		float interZ = startTranslation.elem[2][3] * (1.0F - ratio)
		  + endTranslation.elem[2][3] * ratio;

		interTranslation.setTranslate(interX, interY, interZ);
	}

	/**
	 * スケーリングを補間する。
	 */
	private void interpolateScaling()
	{
		float ratio = currTime / duration;
//		float interScaleX = Math.pow(startScaling.elem[0][0], (1.0F - ratio))
//		  * Math.pow(endScaling.elem[0][0], ratio);
//		float interScaleY = Math.pow(startScaling.elem[1][1], (1.0F - ratio))
//		  * Math.pow(endScaling.elem[1][1], ratio);
//		float interScaleZ = Math.pow(startScaling.elem[2][2], (1.0F - ratio))
//		  * Math.pow(endScaling.elem[2][2], ratio);
		// 本来は上式のようになるが、線形補間で代用する。
		float interScaleX = startScaling.elem[0][0] * (1.0F - ratio)
		  + endScaling.elem[0][0] * ratio;
		float interScaleY = startScaling.elem[1][1] * (1.0F - ratio)
		  + endScaling.elem[1][1] * ratio;
		float interScaleZ = startScaling.elem[2][2] * (1.0F - ratio)
		  + endScaling.elem[2][2] * ratio;

		interScaling.setScale(interScaleX, interScaleY, interScaleZ);
	}

	/**
	 * 回転を球面線形補間する。
	 * slerp (Spherical Linear intERPolation)
	 */
	private void interpolateRotation()
	{
		float ratio = currTime / duration;

		temp1Rotation.setRotate(startAngle * (1.0F - ratio), startAxis);
		temp2Rotation.setRotate(endAngle * ratio, endAxis);
		T3Matrix.multMatrix(interRotation, temp2Rotation, temp1Rotation);
	}

	/**
	 * 回転行列から回転角と回転軸を求める。
	 * @param axis   (OUT) 回転軸
	 * @param rotate (IN ) 回転行列
	 * @return 回転角 (ラジアン)
	 */
	private float rotateMatrixToAngleAndAxis(T3Vector axis, T3Matrix rotate)
	{
		float[][] e = rotate.elem;
		float angle = 0.0F;
		float s, t, u, v;
		float q_s2 = e[0][0] + e[1][1] + e[2][2] + e[3][3];
		float abs_t = 0.5F * (float)Math.sqrt(Math.max((1.0F + e[0][0] - e[1][1] - e[2][2]), 0.0F));
		float abs_u = 0.5F * (float)Math.sqrt(Math.max((1.0F - e[0][0] + e[1][1] - e[2][2]), 0.0F));
		float abs_v = 0.5F * (float)Math.sqrt(Math.max((1.0F - e[0][0] - e[1][1] + e[2][2]), 0.0F));

		if (q_s2 > 0.0F) {
			// 0 < s=cos(θ/2) <= 1のとき (0 <= θ < πのとき)
			s = Math.min(0.5F * (float)Math.sqrt(q_s2), 1.0F);	// 計算誤差を考慮して1.0で抑えておく
			t = (e[2][1] - e[1][2] >= 0.0F) ? abs_t : -abs_t;
			u = (e[0][2] - e[2][0] >= 0.0F) ? abs_u : -abs_u;
			v = (e[1][0] - e[0][1] >= 0.0F) ? abs_v : -abs_v;
//			angle = 2.0F * (float)Math.acos(s);
			angle = 2.0F * T3Math.facos(s);
			if (s < 1.0F) {
				// 0 < s=cos(θ/2) < 1のとき (0 < θ < πのとき)
				float factor = (float)(1.0 / Math.sqrt(1.0 - s * s));
				axis.set(t * factor, u * factor, v * factor);
			} else {
				// s=cos(θ/2) = 1のとき (θ = 0のとき)
				// 回転角=0なので、適当に軸を決めてよい
				axis.set(0.0F, 1.0F, 0.0F);
			}
		} else {
			// s=cos(θ/2) = 0のとき (θ = πのとき)
			angle = (float)Math.PI;
			u = abs_u;					// u >= 0と決める
			if (u > 0.0F) {
				t = (e[0][1] >= 0.0F) ? abs_t : -abs_t;
				v = (e[1][2] >= 0.0F) ? abs_v : -abs_v;
			} else {
				t = abs_t;
				v = (e[2][0] >= 0.0F) ? abs_v : -abs_v;
			}
			axis.set(t, u, v);
		}

		return angle;
	}
}

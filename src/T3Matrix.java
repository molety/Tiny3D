/**
 * 行列
 */
public class T3Matrix
{
	/** 計算用バッファ */
	private static T3Matrix tempM = new T3Matrix();

	/** 要素 */
	public float[][] elem = new float[4][4];

	/**
	 * コンストラクタ
	 */
	public T3Matrix()
	{
		setIdentity();
	}

	/**
	 * 単位行列を設定する。
	 * @return できた単位行列
	 */
	public T3Matrix setIdentity()
	{
		elem[0][0] = 1.0F; elem[0][1] = 0.0F; elem[0][2] = 0.0F; elem[0][3] = 0.0F;
		elem[1][0] = 0.0F; elem[1][1] = 1.0F; elem[1][2] = 0.0F; elem[1][3] = 0.0F;
		elem[2][0] = 0.0F; elem[2][1] = 0.0F; elem[2][2] = 1.0F; elem[2][3] = 0.0F;
		elem[3][0] = 0.0F; elem[3][1] = 0.0F; elem[3][2] = 0.0F; elem[3][3] = 1.0F;

		return this;
	}

	/**
	 * 行列の全要素をコピーする。
	 * @param dst (OUT) コピー先
	 * @param src (IN ) コピー元
	 * @return コピー先
	 */
	public static T3Matrix copy(T3Matrix dst, T3Matrix src)
	{
		float[][] a = src.elem;
		float[][] b = dst.elem;
		int i, j;

		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				b[i][j] = a[i][j];
			}
		}

		return dst;
	}

	/**
	 * 転置する。
	 * @return 転置結果
	 */
	public T3Matrix transpose()
	{
		float[][] a = this.elem;
		int i, j;
		float temp;

		for (i = 1; i < 4; i++) {
			for (j = 0; j < i; j++) {
				temp = a[i][j];
				a[i][j] = a[j][i];
				a[j][i] = temp;
			}
		}

		return this;
	}

	/**
	 * 転置行列を求める。
	 * @param transposed (OUT) 転置結果
	 * @param src        (IN ) 元の行列
	 * @return 転置結果
	 */
	public static T3Matrix makeTransposed(T3Matrix transposed, T3Matrix src)
	{
		float[][] a = src.elem;
		float[][] b = transposed.elem;
		int i, j;

		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				b[j][i] = a[i][j];
			}
		}

		return transposed;
	}

	/**
	 * 2つの行列を交換する。
	 * @param a (I/O) 1つ目の行列
	 * @param b (I/O) 2つ目の行列
	 */
	public static void swap(T3Matrix a, T3Matrix b)
	{
		// フィールド(メンバ変数)の中身を入れ替えるという、あまり綺麗でない方法で実現している。
		// もしフィールドが増えた場合は、それらも入れ替えが必要。
		float[][] tempElem = a.elem;
		a.elem = b.elem;
		b.elem = tempElem;
	}

	/**
	 * 2つの行列の積を求める。
	 * @param product      (OUT) 積
	 * @param multiplicand (IN ) 掛けられる行列 (左)
	 * @param multiplier   (IN ) 掛ける行列 (右)
	 * @return 積
	 */
	public static T3Matrix multMatrix(T3Matrix product, T3Matrix multiplicand, T3Matrix multiplier)
	{
		float[][] a = multiplicand.elem;
		float[][] b = multiplier.elem;
		float[][] c = product.elem;
		float sum;
		int i, j, k;

		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				sum = 0.0F;
				for (k = 0; k < 4; k++) {
					sum += a[i][k] * b[k][j];
				}
				c[i][j] = sum;
			}
		}

		return product;
	}

	/**
	 * 行列に左から別の行列を掛ける。
	 * @param other (IN ) 左から掛ける行列
	 * @return 積 (自分自身)
	 */
	public T3Matrix multFromLeft(T3Matrix other)
	{
		T3Matrix.multMatrix(tempM, other, this);
		T3Matrix.swap(this, tempM);

		return this;
	}

	/**
	 * 行列に右から別の行列を掛ける。
	 * @param other (IN ) 右から掛ける行列
	 * @return 積 (自分自身)
	 */
	public T3Matrix multFromRight(T3Matrix other)
	{
		T3Matrix.multMatrix(tempM, this, other);
		T3Matrix.swap(this, tempM);

		return this;
	}

	/**
	 * 行列とベクトルの積を求める。
	 * @param product      (OUT) 積のベクトル
	 * @param multiplicand (IN ) 行列
	 * @param multiplier   (IN ) 掛けるベクトル
	 * @return 積のベクトル
	 */
	public static T3Vector multVector(T3Vector product, T3Matrix multiplicand, T3Vector multiplier)
	{
		float[][] a = multiplicand.elem;
		float[] b = multiplier.elem;
		float[] c = product.elem;
		float sum;
		int i, j;

		for (i = 0; i < 4; i++) {
			sum = 0.0F;
			for (j = 0; j < 4; j++) {
				sum += a[i][j] * b[j];
			}
			c[i] = sum;
		}

		return product;
	}

	/**
	 * 平行移動行列にする。
	 * @param v (IN ) 平行移動成分を表すベクトル
	 * @return 結果 (自分自身)
	 */
	public T3Matrix setTranslate(T3Vector v)
	{
		setIdentity();

		elem[0][3] = v.x();
		elem[1][3] = v.y();
		elem[2][3] = v.z();

		return this;
	}

	/**
	 * 平行移動行列にする。
	 * @param x (IN ) x成分
	 * @param y (IN ) y成分
	 * @param z (IN ) z成分
	 * @return 結果 (自分自身)
	 */
	public T3Matrix setTranslate(float x, float y, float z)
	{
		setIdentity();

		elem[0][3] = x;
		elem[1][3] = y;
		elem[2][3] = z;

		return this;
	}

	/**
	 * スケーリング行列にする。
	 * @param v (IN ) スケーリング倍率を表すベクトル
	 * @return 結果 (自分自身)
	 */
	public T3Matrix setScale(T3Vector v)
	{
		setIdentity();

		elem[0][0] = v.x();
		elem[1][1] = v.y();
		elem[2][2] = v.z();

		return this;
	}

	/**
	 * スケーリング行列にする。
	 * @param scaleX (IN ) x方向の倍率
	 * @param scaleY (IN ) y方向の倍率
	 * @param scaleZ (IN ) z方向の倍率
	 * @return 結果 (自分自身)
	 */
	public T3Matrix setScale(float scaleX, float scaleY, float scaleZ)
	{
		setIdentity();

		elem[0][0] = scaleX;
		elem[1][1] = scaleY;
		elem[2][2] = scaleZ;

		return this;
	}

	/**
	 * x軸周りの回転行列にする。
	 * @param angle (IN ) 回転角 (ラジアン)
	 * @return 結果 (自分自身)
	 */
	public T3Matrix setRotateX(float angle)
	{
//		float cosine = (float)Math.cos(angle);
//		float sine = (float)Math.sin(angle);
		float cosine = T3Math.fcos(angle);
		float sine = T3Math.fsin(angle);

		setIdentity();

		elem[1][1] = cosine;
		elem[1][2] = -sine;
		elem[2][1] = sine;
		elem[2][2] = cosine;

		return this;
	}

	/**
	 * y軸周りの回転行列にする。
	 * @param angle (IN ) 回転角 (ラジアン)
	 * @return 結果 (自分自身)
	 */
	public T3Matrix setRotateY(float angle)
	{
//		float cosine = (float)Math.cos(angle);
//		float sine = (float)Math.sin(angle);
		float cosine = T3Math.fcos(angle);
		float sine = T3Math.fsin(angle);

		setIdentity();

		elem[2][2] = cosine;
		elem[2][0] = -sine;
		elem[0][2] = sine;
		elem[0][0] = cosine;

		return this;
	}

	/**
	 * z軸周りの回転行列にする。
	 * @param angle (IN ) 回転角 (ラジアン)
	 * @return 結果 (自分自身)
	 */
	public T3Matrix setRotateZ(float angle)
	{
//		float cosine = (float)Math.cos(angle);
//		float sine = (float)Math.sin(angle);
		float cosine = T3Math.fcos(angle);
		float sine = T3Math.fsin(angle);

		setIdentity();

		elem[0][0] = cosine;
		elem[0][1] = -sine;
		elem[1][0] = sine;
		elem[1][1] = cosine;

		return this;
	}

	/**
	 * 回転行列にする。
	 * ※axisは正規化されている必要がある。
	 * @param angle (IN ) 回転角(ラジアン)
	 * @param axis  (IN ) 回転軸ベクトル
	 * @return 結果 (自分自身)
	 */
	public T3Matrix setRotate(float angle, T3Vector axis)
	{
		// ※クォータニオンから回転行列を作るところは
		//   「3D-CGプログラマーのための実践クォータニオン」を参考にしていますが、
		//   そのままだと回転の向きが逆になったので修正しています。

//		float cos_half_angle = (float)Math.cos(angle * 0.5F);
//		float sin_half_angle = (float)Math.sin(angle * 0.5F);
		float cos_half_angle = T3Math.fcos(angle * 0.5F);
		float sin_half_angle = T3Math.fsin(angle * 0.5F);

		float s = cos_half_angle;
		float t = axis.x() * sin_half_angle;
		float u = axis.y() * sin_half_angle;
		float v = axis.z() * sin_half_angle;

		float d_t2 = 2.0F * t * t;
		float d_u2 = 2.0F * u * u;
		float d_v2 = 2.0F * v * v;
		float d_tu = 2.0F * t * u;
		float d_vs = 2.0F * v * s;
		float d_vt = 2.0F * v * t;
		float d_su = 2.0F * s * u;
		float d_uv = 2.0F * u * v;
		float d_st = 2.0F * s * t;

		elem[0][0] = 1.0F - d_u2 - d_v2;
		elem[0][1] = d_tu - d_vs;
		elem[0][2] = d_vt + d_su;
		elem[0][3] = 0.0F;
		elem[1][0] = d_tu + d_vs;
		elem[1][1] = 1.0F - d_v2 - d_t2;
		elem[1][2] = d_uv - d_st;
		elem[1][3] = 0.0F;
		elem[2][0] = d_vt - d_su;
		elem[2][1] = d_uv + d_st;
		elem[2][2] = 1.0F - d_t2 - d_u2;
		elem[2][3] = 0.0F;
		elem[3][0] = 0.0F;
		elem[3][1] = 0.0F;
		elem[3][2] = 0.0F;
		elem[3][3] = 1.0F;

		return this;
	}

	/**
	 * x/y/z軸方向の単位ベクトルの変換先を与えて回転行列を作る。
	 * ※与える3つのベクトルは直交し、かつ正規化されている必要がある。
	 * @param tx (IN ) x軸方向の単位ベクトル(1, 0, 0)の変換先
	 * @param ty (IN ) y軸方向の単位ベクトル(0, 1, 0)の変換先
	 * @param tz (IN ) z軸方向の単位ベクトル(0, 0, 1)の変換先
	 * @return 結果 (自分自身)
	 */
	public T3Matrix setRotateWithUnitVector(T3Vector tx, T3Vector ty, T3Vector tz)
	{
		elem[0][0] = tx.x();
		elem[1][0] = tx.y();
		elem[2][0] = tx.z();
		elem[3][0] = 0.0F;
		elem[0][1] = ty.x();
		elem[1][1] = ty.y();
		elem[2][1] = ty.z();
		elem[3][1] = 0.0F;
		elem[0][2] = tz.x();
		elem[1][2] = tz.y();
		elem[2][2] = tz.z();
		elem[3][2] = 0.0F;
		elem[0][3] = 0.0F;
		elem[1][3] = 0.0F;
		elem[2][3] = 0.0F;
		elem[3][3] = 1.0F;

		return this;
	}
}

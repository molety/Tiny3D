/**
 * ベクトル (位置または方向を表す)
 */
public class T3Vector
{
	/** 要素 */
	public float[] elem = {0.0F, 0.0F, 0.0F, 1.0F};

	/**
	 * デフォルトコンストラクタ
	 */
	public T3Vector()
	{
	}

	/**
	 * コンストラクタ
	 * @param x (IN ) x成分
	 * @param y (IN ) y成分
	 * @param z (IN ) z成分
	 */
	public T3Vector(float x, float y, float z)
	{
		elem[0] = x;
		elem[1] = y;
		elem[2] = z;
	}

	/**
	 * x成分を返す。
	 * @return x成分
	 */
	public float x()
	{
		return elem[0];
	}

	/**
	 * y成分を返す。
	 * @return y成分
	 */
	public float y()
	{
		return elem[1];
	}

	/**
	 * z成分を返す。
	 * @return z成分
	 */
	public float z()
	{
		return elem[2];
	}

	/**
	 * 成分を設定する。
	 * @param x (IN ) x成分
	 * @param y (IN ) y成分
	 * @param z (IN ) z成分
	 * @return 結果 (自分自身)
	 */
	public T3Vector set(float x, float y, float z)
	{
		elem[0] = x;
		elem[1] = y;
		elem[2] = z;

		return this;
	}

	/**
	 * ベクトルのノルム(長さ)を求める。
	 * @return ノルム
	 */
	public float norm()
	{
		float x = elem[0];
		float y = elem[1];
		float z = elem[2];
		float n2 = x * x + y * y + z * z;

		return (n2 * T3Math.invSqrt(n2));
	}

	/**
	 * ノルムの2乗を求める。
	 * @return ノルムの2乗
	 */
	public float norm2()
	{
		float x = elem[0];
		float y = elem[1];
		float z = elem[2];

		return (x * x + y * y + z * z);
	}

	/**
	 * 正規化する。(ノルムを1にする)
	 * @return 結果 (自分自身)
	 */
	public T3Vector normalize()
	{
		float x = elem[0];
		float y = elem[1];
		float z = elem[2];
//		float invNorm = 1.0F / (float)Math.sqrt(x * x + y * y + z * z);
		float invNorm = T3Math.invSqrt(x * x + y * y + z * z);

		elem[0] *= invNorm;
		elem[1] *= invNorm;
		elem[2] *= invNorm;

		return this;
	}

	/**
	 * 逆ベクトルにする。
	 * @return 結果 (自分自身)
	 */
	public T3Vector invert()
	{
		elem[0] = -elem[0];
		elem[1] = -elem[1];
		elem[2] = -elem[2];

		return this;
	}

	/**
	 * 逆ベクトルを作る。
	 * @param inverse (OUT) 逆ベクトル
	 * @param src     (IN ) 元のベクトル
	 * @return 逆ベクトル
	 */
	public static T3Vector makeInverse(T3Vector inverse, T3Vector src)
	{
		float[] a = src.elem;
		float[] b = inverse.elem;

		b[0] = -a[0];
		b[1] = -a[1];
		b[2] = -a[2];

		return inverse;
	}

	/**
	 * スカラー倍する。
	 * @param n (IN ) スカラー
	 * @return 結果 (自分自身)
	 */
	public T3Vector multScalar(float n)
	{
		elem[0] *= n;
		elem[1] *= n;
		elem[2] *= n;

		return this;
	}

	/**
	 * ベクトルを加算する。
	 * @param addend (IN ) 加算するベクトル
	 * @return 結果 (自分自身)
	 */
	public T3Vector add(T3Vector addend)
	{
		float[] b = addend.elem;

		elem[0] += b[0];
		elem[1] += b[1];
		elem[2] += b[2];

		return this;
	}

	/**
	 * ベクトルを減算する。
	 * @param subtrahend (IN ) 減算するベクトル
	 * @return 結果 (自分自身)
	 */
	public T3Vector sub(T3Vector subtrahend)
	{
		float[] b = subtrahend.elem;

		elem[0] -= b[0];
		elem[1] -= b[1];
		elem[2] -= b[2];

		return this;
	}

	/**
	 * 和ベクトルを求める。
	 * @param s      (OUT) 和ベクトル
	 * @param augend (IN ) 足されるベクトル
	 * @param addend (IN ) 足すベクトル
	 * @return 和ベクトル
	 */
	public static T3Vector sum(T3Vector s, T3Vector augend, T3Vector addend)
	{
		float[] a = augend.elem;
		float[] b = addend.elem;
		float[] c = s.elem;

		c[0] = a[0] + b[0];
		c[1] = a[1] + b[1];
		c[2] = a[2] + b[2];

		return s;
	}

	/**
	 * 差ベクトルを求める。
	 * @param d          (OUT) 差ベクトル
	 * @param minuend    (IN ) 引かれるベクトル
	 * @param subtrahend (IN ) 引くベクトル
	 * @return 差ベクトル
	 */
	public static T3Vector diff(T3Vector d, T3Vector minuend, T3Vector subtrahend)
	{
		float[] a = minuend.elem;
		float[] b = subtrahend.elem;
		float[] c = d.elem;

		c[0] = a[0] - b[0];
		c[1] = a[1] - b[1];
		c[2] = a[2] - b[2];

		return d;
	}

	/**
	 * 内積を求める。
	 * @param multiplicand (IN ) 1つ目のベクトル
	 * @param multiplier   (IN ) 2つ目のベクトル
	 * @return 内積
	 */
	public static float innerProduct(T3Vector multiplicand, T3Vector multiplier)
	{
		float[] a = multiplicand.elem;
		float[] b = multiplier.elem;

		return (a[0] * b[0] + a[1] * b[1] + a[2] * b[2]);
	}

	/**
	 * 外積を求める。
	 * @param product      (OUT) 外積
	 * @param multiplicand (IN ) 1つ目のベクトル
	 * @param multiplier   (IN ) 2つ目のベクトル
	 * @return 外積
	 */
	public static T3Vector outerProduct(T3Vector product, T3Vector multiplicand, T3Vector multiplier)
	{
		float[] a = multiplicand.elem;
		float[] b = multiplier.elem;
		float[] c = product.elem;

		c[0] = a[1] * b[2] - a[2] * b[1];
		c[1] = a[2] * b[0] - a[0] * b[2];
		c[2] = a[0] * b[1] - a[1] * b[0];

		return product;
	}
}

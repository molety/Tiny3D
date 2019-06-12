/**
 * 数学関数
 */
public class T3Math
{
	// ※float-->intの高速な変換、sin,cosのテーブル化は
	//   http://www.yamagami-planning.com/soft/optimize/optimize01/ を参考にしました。
	//   平方根の逆数の高速化は
	//   http://garugari.jugem.jp/?eid=433
	//   http://niffy-you-nats.hp.infoseek.co.jp/iappli/LunaMath.java を参考にしました。

	/** 2π */
	public static final float PI2 = (float)(2.0 * Math.PI);
	/** sinテーブルのサイズ */
	private static final int SIN_TABLE_SIZE = 2048;
	/** cosを求める時のオフセット */
	private static final int COS_OFFSET = SIN_TABLE_SIZE / 4;
	/** 角度→インデックス変換時のスケーリング値 */
	private static final float TWO_PI_SCALE = (float)SIN_TABLE_SIZE / PI2;
	/** float-->int変換時にfloatに足すバイアス */
	private static final float FTOI_BIAS_F = 12582912.0F;
	/** float-->int変換時にintから引くバイアス */
	private static final int FTOI_BIAS_I = 0x4b400000;
	/** sinテーブル */
	private static float sinTable[] = new float[SIN_TABLE_SIZE];
	/** acosテーブルのサイズ */
	private static final int ACOS_TABLE_SIZE = 2048;
	/** acosの分解能 */
	private static final int ACOS_RESOLUTION = 8192;
	/** acosテーブル */
	private static float acosTable[] = new float[ACOS_TABLE_SIZE];

	/**
	 * staticイニシャライザ
	 */
	static
	{
		double angle;

		// sinテーブルの初期化
		for (int i = 0; i < SIN_TABLE_SIZE; i++) {
			angle = (double)i / TWO_PI_SCALE;
			sinTable[i] = (float)Math.sin(angle);
		}

		// acosテーブルの初期化
		angle = Math.PI;
		double targetCos;
		for (int i = 0, j = 0; i < ACOS_TABLE_SIZE; i++) {
			targetCos = (double)i / (double)(ACOS_TABLE_SIZE - 1) * 2.0 - 1.0;
			while (Math.cos(angle) < targetCos) {
				angle = (1.0 - (double)j / (double)ACOS_RESOLUTION) * Math.PI;
				if (j >= ACOS_RESOLUTION) {
					break;			// 万が一にも演算誤差で無限ループにならないようにしておく
				}
				j++;
			}
			acosTable[i] = (float)angle;
//			System.out.println(angle);
		}
	}

	/**
	 * sine
	 * @param angle (IN ) 角度 (ラジアン)
	 * @return sine値
	 */
	public static float fsin(float angle)
	{
		return sinTable[Float.floatToIntBits(angle * TWO_PI_SCALE + FTOI_BIAS_F)
						& (SIN_TABLE_SIZE - 1)];
	}

	/**
	 * cosine
	 * @param angle (IN ) 角度 (ラジアン)
	 * @return cosine値
	 */
	public static float fcos(float angle)
	{
		return sinTable[(Float.floatToIntBits(angle * TWO_PI_SCALE + FTOI_BIAS_F) + COS_OFFSET)
						& (SIN_TABLE_SIZE - 1)];
	}

	/**
	 * arc cosine
	 * @param x (IN ) cosine値 (-1 <= x <= 1)
	 * @return 角度 (0～πラジアン)
	 */
	public static float facos(float x)
	{
		return acosTable[Float.floatToIntBits((x + 1.0F) * 0.5F * (ACOS_TABLE_SIZE - 1) + FTOI_BIAS_F)
						 & (ACOS_TABLE_SIZE - 1)];
	}

	/**
	 * floatからintへの変換
	 * @param x (IN ) float値 (-2^22 <= x <= 2^22)
	 * @return int値
	 */
	public static int floatToInt(float x)
	{
		return (Float.floatToIntBits(x + FTOI_BIAS_F) - FTOI_BIAS_I);
	}

	/**
	 * べき乗
	 * @param base     (IN ) 底
	 * @param exponent (IN ) 指数
	 * @return べき乗値
	 */
	public static float power(float base, int exponent)
	{
		float pow = 1.0F;
		boolean doInvert = false;

		if (exponent < 0) {
			exponent = -exponent;
			doInvert = true;
		}

		while (exponent > 0) {
			if ((exponent & 1) == 1) {
				pow *= base;
			}
			base *= base;
			exponent >>= 1;
		}

		if (doInvert) {
			pow = 1.0F / pow;
		}

		return pow;
	}

	/**
	 * 平方根の逆数
	 * @param x (IN ) 元の値
	 * @return 平方根の逆数
	 */
	public static float invSqrt(float x)
	{
		float xhalf = 0.5F * x;

		x = Float.intBitsToFloat(0x5f3759df - (Float.floatToIntBits(x) >> 1));

		return x * (1.5F - xhalf * x * x);
	}
}

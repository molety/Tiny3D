/**
 * 光源
 */
public class T3Light extends T3Object
{
	/** 光源ベクトル */
	private static T3Vector lightV = new T3Vector();
	/** 視線ベクトル */
	private static T3Vector viewV = new T3Vector();
	/** ハーフウェイベクトル */
	private static T3Vector halfwayV = new T3Vector();

	/** 平行光源 */
	public static final int DIRECTIONAL = 0;
	/** 点光源 */
	public static final int POINT = 1;
	/** モード */
	private int mode = DIRECTIONAL;

	/** 照射方向 (平行光源) */
	private T3Vector direction;
	/** 位置 (点光源) / 仮の原点 (平行光源) */
	private T3Vector position;
	/** モデリング-ビューイング変換後の照射方向 */
	private T3Vector transformedDirection;
	/** モデリング-ビューイング変換後の位置 */
	private T3Vector transformedPosition;

	/** 環境光成分 */
	private float[] ambient;
	/** 拡散反射成分 */
	private float[] diffuse;
	/** 鏡面反射成分 */
	private float[] specular;
	/** 減衰率の定数項 */
	private float constantAttenuation;
	/** 減衰率の1次の項 */
	private float linearAttenuation;
	/** 減衰率の2次の項 */
	private float quadraticAttenuation;
	/** 環境光を計算するか */
	private boolean doAmbient;
	/** 拡散反射成分を計算するか */
	private boolean doDiffuse;
	/** 鏡面反射成分を計算するか */
	private boolean doSpecular;

	/**
	 * コンストラクタ
	 * @param mode   (IN ) モード
	 * @param vector (IN ) 照射方向 (平行光源) / 位置 (点光源)
	 * @param color  (IN ) 色情報
	 */
	public T3Light(int mode, float[] vector, float[] color)
	{
		int i;

		this.mode = mode;

		direction = new T3Vector();
		position = new T3Vector();
		transformedDirection = new T3Vector();
		transformedPosition = new T3Vector();

		switch (this.mode) {
		  case DIRECTIONAL:
			direction.set(vector[0], vector[1], vector[2]);
			position.set(0.0F, 0.0F, 0.0F);
			break;
		  case POINT:
			position.set(vector[0], vector[1], vector[2]);
			break;
		}

		specular = new float[3];
		diffuse = new float[3];
		ambient = new float[3];
		ambient[0] = color[0];
		ambient[1] = color[1];
		ambient[2] = color[2];
		diffuse[0] = color[3];
		diffuse[1] = color[4];
		diffuse[2] = color[5];
		specular[0] = color[6];
		specular[1] = color[7];
		specular[2] = color[8];
		constantAttenuation = color[9];
		linearAttenuation = color[10];
		quadraticAttenuation = color[11];
		doAmbient = (ambient[0] + ambient[1] + ambient[2] > 0.0F);
		doDiffuse = (diffuse[0] + diffuse[1] + diffuse[2] > 0.0F);
		doSpecular = (specular[0] + specular[1] + specular[2] > 0.0F);
	}

	/**
	 * ワールド内に配置する。
	 * @param world (IN ) ワールドオブジェクト
	 */
	public void place(T3World world)
	{
		super.place(world);

		T3Matrix.multVector(transformedDirection, world.modelView, direction);
		T3Matrix.multVector(transformedPosition, world.modelView, position);
	}

	/**
	 * 色を求める。
	 * @param color (I/O) 色(入力値に加算して出力する)
	 * @param face  (IN ) 面
	 */
	public void calcColor(float[] color, T3Face face)
	{
		// ※ http://marina.sys.wakayama-u.ac.jp/~tokoi/?date=20051007
		//    http://marupeke296.com/FBX_No7_UV.html を参考にしました。

		int i;
		float ratio;
		float attenuation = 1.0F;
		float distance;

		switch (mode) {
		  case DIRECTIONAL:
			// 光源ベクトルは光源へ向かうベクトルとする。
			T3Vector.diff(lightV, transformedPosition, transformedDirection);
			lightV.normalize();
			attenuation = 1.0F;
			break;
		  case POINT:
			// 光源ベクトルは面の重心から光源へ向かうベクトルとする。
			T3Vector.diff(lightV, transformedPosition, face.center);
			distance = lightV.norm();
			lightV.normalize();
			attenuation = 1.0F / (constantAttenuation
								  + linearAttenuation * distance
								  + quadraticAttenuation * distance * distance);
			break;
		}

		// 環境光
		if (doAmbient) {
			for (i = 0; i < 3; i++) {
				color[i] += ambient[i] * face.ambient[i] * attenuation;
			}
		}

		// 拡散反射光
		if (doDiffuse) {
			ratio = Math.max(T3Vector.innerProduct(lightV, face.normal), 0.0F);
			for (i = 0; i < 3; i++) {
				color[i] += diffuse[i] * face.diffuse[i] * ratio * attenuation;
			}
		}

		// 鏡面反射光
		if (doSpecular) {
			// 視線ベクトルは面の重心から視点(=原点)へ向かうベクトルとする。
			T3Vector.makeInverse(viewV, face.center);
			viewV.normalize();
			T3Vector.sum(halfwayV, lightV, viewV);
			halfwayV.normalize();
			ratio = Math.max(T3Vector.innerProduct(halfwayV, face.normal), 0.0F);
			ratio = T3Math.power(ratio, face.shininess);
			for (i = 0; i < 3; i++) {
				color[i] += specular[i] * face.specular[i] * ratio * attenuation;
			}
		}
	}
}

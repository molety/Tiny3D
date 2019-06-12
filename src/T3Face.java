/**
 * 面 (ポリゴン)
 */
public class T3Face
{
	/** 面法線ベクトル計算用 */
	private static T3Vector tempV01 = new T3Vector();
	/** 面法線ベクトル計算用 */
	private static T3Vector tempV12 = new T3Vector();

	/** 頂点数 */
	public int numOfVertex;
	/** 頂点数の逆数 */
	public float invNum;

	/** 頂点配列 */
	public T3Vector[] vertex;

	/** 面法線ベクトル */
	public T3Vector normal;
	/** 重心 */
	public T3Vector center;
	/** 重心のz座標 */
	public float centerZ;

	/** 環境光に対する反射係数 */
	public float[] ambient;
	/** 拡散反射係数 */
	public float[] diffuse;
	/** 鏡面反射係数 */
	public float[] specular;
	/** 輝き係数 */
	public int shininess;
	/** レンダリングされた色 */
	public int[] renderedColor;

	/**
	 * コンストラクタ
	 * @param vertex (IN ) 頂点配列
	 * @param index  (IN ) 頂点配列の中でこの面を構成する頂点のインデックス
	 * @param color  (IN ) 色情報
	 */
	public T3Face(T3Vector[] vertex, int[] index, float[] color)
	{
		int i;

		numOfVertex = index.length;
		invNum = 1.0F / (float)numOfVertex;

		this.vertex = new T3Vector[numOfVertex];
		for (i = 0; i < numOfVertex; i++) {
			this.vertex[i] = vertex[index[i]];
		}

		normal = new T3Vector();
		center = new T3Vector();

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
		shininess = (int)color[9];
		renderedColor = new int[3];
	}

	/**
	 * ワールド内に配置する。
	 * @param world      (IN ) ワールドオブジェクト
	 * @param doCullBack (IN ) 裏向きの面をカリングするか
	 * @param doCullNear (IN ) 投影面より近くにある面をカリングするか
	 */
	public void place(T3World world, boolean doCullBack, boolean doCullNear)
	{
		boolean visible = true;

		calcNormal();
		calcCenter();

		if (doCullBack) {
			// 面法線ベクトルの向きによるカリング
			// 面法線ベクトルのz成分が負の面は見えないとする。
			if (normal.z() < 0.0F) {
				visible = false;
			}
		}

		if (doCullNear) {
			// z座標によるカリング
			// centerZ > windowDepthの面は見えないとする。
			if (centerZ > world.projection.windowDepth) {
				visible = false;
			}
		}

		if (visible) {
			world.addFace(this);
		}
	}

	/**
	 * 面法線ベクトルを求める。
	 */
	private void calcNormal()
	{
		// 三角形の2辺を求める
		T3Vector.diff(tempV01, vertex[1], vertex[0]);
		T3Vector.diff(tempV12, vertex[2], vertex[1]);

		// 面法線ベクトルを求め、正規化する
		T3Vector.outerProduct(normal, tempV01, tempV12);
		normal.normalize();
	}

	/**
	 * 重心を求める。
	 */
	private void calcCenter()
	{
		float x = 0;
		float y = 0;
		float z = 0;
		int i;

		for (i = 0; i < numOfVertex; i++) {
			x += vertex[i].x();
			y += vertex[i].y();
			z += vertex[i].z();
		}
		x *= invNum;
		y *= invNum;
		z *= invNum;

		center.set(x, y, z);
		centerZ = z;
	}
}

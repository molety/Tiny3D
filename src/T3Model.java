/**
 * モデル (物体)
 */
public class T3Model extends T3Object
{
	/** adjustCenter()用テンポラリ */
	private static T3Vector tempCenter = new T3Vector();

	/** 頂点数 */
	private int numOfVertex;
	/** 頂点配列 */
	private T3Vector[] vertex;
	/** モデリング-ビューイング変換後の頂点配列 */
	private T3Vector[] transformedVertex;

	/** 面数 */
	private int numOfFace;
	/** 面 */
	private T3Face[] face;

	/**
	 * コンストラクタ (個別色指定)
	 * @param nVertex (IN ) 頂点数
	 * @param nFace   (IN ) 面数
	 * @param vertex  (IN ) 頂点情報
	 * @param index   (IN ) 各面を構成する頂点のインデックス
	 * @param color   (IN ) 色情報
	 */
	public T3Model(int nVertex, int nFace, float[][] vertex, int[][] index, float[][] color)
	{
		int i;

		numOfVertex = nVertex;
		numOfFace = nFace;

		setVertex(vertex);

		face = new T3Face[numOfFace];
		for (i = 0; i < numOfFace; i++) {
			face[i] = new T3Face(transformedVertex, index[i], color[i]);
		}
	}

	/**
	 * コンストラクタ (一括色指定)
	 * @param nVertex (IN ) 頂点数
	 * @param nFace   (IN ) 面数
	 * @param vertex  (IN ) 頂点情報
	 * @param index   (IN ) 各面を構成する頂点のインデックス
	 * @param color   (IN ) 色情報
	 */
	public T3Model(int nVertex, int nFace, float[][] vertex, int[][] index, float[] color)
	{
		int i;

		numOfVertex = nVertex;
		numOfFace = nFace;

		setVertex(vertex);

		face = new T3Face[numOfFace];
		for (i = 0; i < numOfFace; i++) {
			face[i] = new T3Face(transformedVertex, index[i], color);
		}
	}

	/**
	 * 頂点配列を設定する。
	 * コンストラクタの下請け
	 * @param vertex (IN ) 頂点情報
	 */
	private void setVertex(float[][] vertex)
	{
		int i;
		float[] xyz;

		this.vertex = new T3Vector[numOfVertex];
		this.transformedVertex = new T3Vector[numOfVertex];
		for (i = 0; i < numOfVertex; i++) {
			xyz = vertex[i];
			this.vertex[i] = new T3Vector(xyz[0], xyz[1], xyz[2]);
			this.transformedVertex[i] = new T3Vector();
		}
	}

	/**
	 * ワールド内に配置する。
	 * @param world      (IN ) ワールドオブジェクト
	 * @param doCullBack (IN ) 裏向きの面をカリングするか
	 * @param doCullNear (IN ) 投影面より近くにある面をカリングするか
	 */
	public void place(T3World world, boolean doCullBack, boolean doCullNear)
	{
		int i;

		super.place(world);

		for (i = 0; i < numOfVertex; i++) {
			T3Matrix.multVector(transformedVertex[i], world.modelView, vertex[i]);
		}

		for (i = 0; i < numOfFace; i++) {
			face[i].place(world, doCullBack, doCullNear);
		}
	}

	/**
	 * 物体の重心が原点に来るように補正する。
	 */
	public void adjustCenter()
	{
		int i;

		tempCenter.set(0.0F, 0.0F, 0.0F);
		for (i = 0; i < numOfVertex; i++) {
			tempCenter.add(vertex[i]);
		}
		for (i = 0; i < 3; i++) {
			tempCenter.multScalar(1.0F / (float)numOfVertex);
		}
		for (i = 0; i < numOfVertex; i++) {
			vertex[i].sub(tempCenter);
		}
	}
}

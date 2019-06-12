/**
 * ワールド
 */
public class T3World
{
	/** render()内部で使用するテンポラリ */
	private static float[] tempColor = new float[3];

	/** モデリング-ビューイング変換行列 */
	public T3Matrix modelView;

	/** モデル */
	private T3Model[] modelBuffer;
	/** 登録されているモデル数 */
	private int numOfModel;

	/** 光源 */
	private T3Light[] lightBuffer;
	/** 登録されている光源数 */
	private int numOfLight;

	/** カメラ(視点) */
	public T3Camera camera;

	/** 投影オブジェクト */
	public T3Projection projection;

	/** 面バッファ */
	private T3Face[] faceBuffer;
	/** 面バッファ(ソート用領域) */
	private T3Face[] faceBufferTemp;
	/** ソート済み面バッファ */
	public T3Face[] sortedFace;
	/** バッファに入っている面数 */
	public int numOfFace;

	/**
	 * コンストラクタ
	 * @param maxNumOfModel (IN ) モデルの最大数
	 * @param maxNumOfLight (IN ) 光源の最大数
	 * @param maxNumOfFace  (IN ) 面の最大数
	 */
	public T3World(int maxNumOfModel, int maxNumOfLight, int maxNumOfFace)
	{
		modelView = new T3Matrix();

		modelBuffer = new T3Model[maxNumOfModel];
		numOfModel = 0;

		lightBuffer = new T3Light[maxNumOfLight];
		numOfLight = 0;

		faceBuffer = new T3Face[maxNumOfFace];
		faceBufferTemp = new T3Face[maxNumOfFace];
		numOfFace = 0;
	}

	/**
	 * 登録されているモデルと光源をクリアする。
	 */
	public void clearModelAndLight()
	{
		numOfModel = 0;
		numOfLight = 0;
		numOfFace = 0;
	}

	/**
	 * モデルを追加する。
	 * @param model (IN ) モデル
	 */
	public void addModel(T3Model model)
	{
		modelBuffer[numOfModel++] = model;
	}

	/**
	 * 光源を追加する。
	 * @param light (IN ) 光源
	 */
	public void addLight(T3Light light)
	{
		lightBuffer[numOfLight++] = light;
	}

	/**
	 * 面を追加する。
	 * @param face (IN ) 面
	 */
	public void addFace(T3Face face)
	{
		faceBuffer[numOfFace++] = face;
	}

	/**
	 * カメラ(視点)を設定する。
	 * @param camera (IN ) カメラ
	 */
	public void setCamera(T3Camera camera)
	{
		this.camera = camera;
	}

	/**
	 * 投影オブジェクトを設定する。
	 * @param projection (IN ) 投影オブジェクト
	 */
	public void setProjection(T3Projection projection)
	{
		this.projection = projection;
	}

	/**
	 * レンダリングする。
	 * @param doCullBack (IN ) 裏向きの面をカリングするか
	 * @param doCullNear (IN ) 投影面より近くにある面をカリングするか
	 */
	public void render(boolean doCullBack, boolean doCullNear)
	{
		int i, j;

		for (i = 0; i < numOfModel; i++) {
			modelBuffer[i].place(this, doCullBack, doCullNear);
		}

		for (i = 0; i < numOfLight; i++) {
			lightBuffer[i].place(this);
		}

		// Zソート
		sortedFace = sortFace(faceBuffer, faceBufferTemp, numOfFace);

		// ライティング
		for (i = 0; i < numOfFace; i++) {
			tempColor[0] = 0.0F;
			tempColor[1] = 0.0F;
			tempColor[2] = 0.0F;
			for (j = 0; j < numOfLight; j++) {
				lightBuffer[j].calcColor(tempColor, sortedFace[i]);
			}
//			sortedFace[i].renderedColor[0] = Math.min((int)tempColor[0], 255);
//			sortedFace[i].renderedColor[1] = Math.min((int)tempColor[1], 255);
//			sortedFace[i].renderedColor[2] = Math.min((int)tempColor[2], 255);
			sortedFace[i].renderedColor[0] = Math.min(T3Math.floatToInt(tempColor[0]), 255);
			sortedFace[i].renderedColor[1] = Math.min(T3Math.floatToInt(tempColor[1]), 255);
			sortedFace[i].renderedColor[2] = Math.min(T3Math.floatToInt(tempColor[2]), 255);
		}

		// 投影
		projection.project(this);
	}

	/**
	 * 面をZソートする。
	 * @param data      (I/O) ソート対象のデータ
	 * @param work      (I/O) ワークエリア
	 * @param numOfData (IN ) データの個数
	 * @return ソート済みのデータ (data/workのどちらかが返る)
	 */
	private T3Face[] sortFace(T3Face[] data, T3Face[] work, int numOfData)
	{
		// ※ http://f4.aaa.livedoor.jp/~pointc/203/No.7397.htm で議論されている
		//   非再帰版マージソートのアルゴリズムを参考にしました。

		T3Face[] src = data;
		T3Face[] dst = work;
		T3Face[] p;
		int pieceHalfSize;
		int pieceTop;
		int pieceBottom;
		int pieceHalf;
		int left;
		int right;
		int dstIndex;
		// このループがO(logN)
		for (pieceHalfSize = 1; pieceHalfSize < numOfData; pieceHalfSize *= 2) {
			dstIndex = 0;
			// O(N^2)に見えてただのO(N)
			for (pieceTop = 0; pieceTop < numOfData; pieceTop += pieceHalfSize * 2) {
				pieceBottom = pieceTop + pieceHalfSize * 2;
				pieceHalf = pieceTop + pieceHalfSize;
				left = pieceTop;
				right = pieceHalf;
				if (pieceHalf >= numOfData) {				// 断片の右半分が無い時
					while (left < numOfData) {
						dst[dstIndex++] = src[left++];
					}
					break;
				}
				if (pieceBottom > numOfData) {				// 断片の右半分の数調整
					pieceBottom = numOfData;
				}
				for (;;) {
					if (src[left].centerZ <= src[right].centerZ) {	// 重心のz座標でソート
						dst[dstIndex++] = src[left++];
						if (left >= pieceHalf) {			// 断片の左半分が出尽くした時
							while (right < pieceBottom) {
								dst[dstIndex++] = src[right++];
							}
							break;
						}
					} else {
						dst[dstIndex++] = src[right++];
						if (right >= pieceBottom) {			// 断片の右半分が出尽くした時
							while (left < pieceHalf) {
								dst[dstIndex++] = src[left++];
							}
							break;
						}
					}
				}
			}

			p = src;
			src = dst;
			dst = p;
		}

		return src;			// data[]かwork[]かどちらかがソート済みになって返る
	}
}

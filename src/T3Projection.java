import javax.microedition.lcdui.*;

/**
 * 投影
 */
public class T3Projection
{
	/** Graphicsオブジェクト */
	private Graphics graphics;

	/** 透視投影モード */
	public static final int PERSPECTIVE = 0;
	/** 正投影モード */
	public static final int ORTHOGRAPHIC = 1;
	/** 投影モード */
	private int mode = PERSPECTIVE;

	/** ウィンドウ幅 */
	private float windowWidth;
	/** ウィンドウ高さ */
	private float windowHeight;
	/** ウィンドウ深さ */
	public  float windowDepth;

	/** ビューポート左端x座標 */
	private float viewportX;
	/** ビューポート上端y座標 */
	private float viewportY;
	/** ビューポート幅 */
	private float viewportWidth;
	/** ビューポート高さ */
	private float viewportHeight;

	/** 1つの面を構成する頂点数の最大値 */
	private static final int MAX_NUM_OF_VERTEX = 128;
	/** 描画位置x座標 */
	private static int[] drawX = new int[MAX_NUM_OF_VERTEX];
	/** 描画位置y座標 */
	private static int[] drawY = new int[MAX_NUM_OF_VERTEX];

	/**
	 * コンストラクタ
	 * @param g (IN ) Graphicsオブジェクト
	 */
	public T3Projection(Graphics g)
	{
		graphics = g;
	}

	/**
	 * 透視投影モードに設定する。
	 * @param width  (IN ) ウィンドウ幅
	 * @param height (IN ) ウィンドウ高さ
	 * @param depth  (IN ) ウィンドウ深さ (投影面のz座標、負の値)
	 */
	public void setPerspective(float width, float height, float depth)
	{
		mode = PERSPECTIVE;
		windowWidth = width;
		windowHeight = height;
		windowDepth = depth;
	}

	/**
	 * 画角と縦横比を指定して透視投影モードに設定する。
	 * @param fovY   (IN ) 画角(field of view Y) (ラジアンで指定)
	 * @param aspect (IN ) 縦横比 (横 / 縦)
	 * @param depth  (IN ) 投影面のz座標 (< 0)
	 */
	public void setPerspectiveByFovY(float fovY, float aspect, float depth)
	{
		mode = PERSPECTIVE;
		windowHeight = -depth * (float)Math.tan(fovY * 0.5F) * 2.0F;
		windowWidth = windowHeight * aspect;
		windowDepth = depth;
	}

	/**
	 * 正投影モードに設定する。
	 * @param width  (IN ) ウィンドウ幅
	 * @param height (IN ) ウィンドウ高さ
	 * @param depth  (IN ) ウィンドウ深さ (投影面のz座標、負の値)
	 */
	public void setOrthographic(float width, float height, float depth)
	{
		mode = ORTHOGRAPHIC;
		windowWidth = width;
		windowHeight = height;
		windowDepth = depth;
	}

	/**
	 * ビューポートを設定する。
	 * @param x      (IN ) 左端のx座標
	 * @param y      (IN ) 上端のy座標
	 * @param width  (IN ) 幅
	 * @param height (IN ) 高さ
	 */
	public void setViewport(int x, int y, int width, int height)
	{
		viewportX = (float)x;
		viewportY = (float)y;
		viewportWidth = (float)(width - 1);
		viewportHeight = (float)(height - 1);

		graphics.setClip(x, y, width, height);
	}

	/**
	 * 投影する。
	 * @param world (IN ) ワールドオブジェクト
	 */
	public void project(T3World world)
	{
		// ※透視投影モードの計算式は
		//   http://www.manpagez.com/man/3/glFrustum/ を参考にしました。

		float factorX, factorY;
		float offsetX, offsetY;
		int i, j;
		T3Face face;
		T3Vector tempV;
		int[] color;

		switch (mode) {
		  case PERSPECTIVE:
			factorX = viewportWidth / windowWidth * windowDepth;
			factorY = -1.0F * viewportHeight / windowHeight * windowDepth;
			offsetX = viewportX + viewportWidth * 0.5F;
			offsetY = viewportY + viewportHeight * 0.5F;
			for (i = 0; i < world.numOfFace; i++) {
				face = world.sortedFace[i];
				for (j = 0; j < face.numOfVertex; j++) {
					tempV = face.vertex[j];
//					drawX[j] = (int)(factorX * tempV.x() / tempV.z() + offsetX);
//					drawY[j] = (int)(factorY * tempV.y() / tempV.z() + offsetY);
					drawX[j] = T3Math.floatToInt(factorX * tempV.x() / tempV.z() + offsetX);
					drawY[j] = T3Math.floatToInt(factorY * tempV.y() / tempV.z() + offsetY);
				}
				color = face.renderedColor;
				graphics.setColor(color[0], color[1], color[2]);
				for (j = 0; j < face.numOfVertex - 2; j++) {
					graphics.fillTriangle(drawX[0], drawY[0],
										  drawX[j + 1], drawY[j + 1],
										  drawX[j + 2], drawY[j + 2]);
				}
			}
			break;
		  case ORTHOGRAPHIC:
			factorX = viewportWidth / windowWidth;
			factorY = -1.0F * viewportHeight / windowHeight;
			offsetX = viewportX + viewportWidth * 0.5F;
			offsetY = viewportY + viewportHeight * 0.5F;
			for (i = 0; i < world.numOfFace; i++) {
				face = world.sortedFace[i];
				for (j = 0; j < face.numOfVertex; j++) {
					tempV = face.vertex[j];
//					drawX[j] = (int)(factorX * tempV.x() + offsetX);
//					drawY[j] = (int)(factorY * tempV.y() + offsetY);
					drawX[j] = T3Math.floatToInt(factorX * tempV.x() + offsetX);
					drawY[j] = T3Math.floatToInt(factorY * tempV.y() + offsetY);
				}
				color = face.renderedColor;
				graphics.setColor(color[0], color[1], color[2]);
				for (j = 0; j < face.numOfVertex - 2; j++) {
					graphics.fillTriangle(drawX[0], drawY[0],
										  drawX[j + 1], drawY[j + 1],
										  drawX[j + 2], drawY[j + 2]);
				}
			}
			break;
		}
	}
}

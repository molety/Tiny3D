import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * キャンバス
 */
public class Tiny3DCanvas extends GameCanvas implements Runnable, CommandListener
{
	/** キャンバス幅 */
	public final int CANVAS_WIDTH = getWidth();
	/** キャンバス高さ */
	public final int CANVAS_HEIGHT = getHeight();

	/** 終了コマンド */
	private static final Command cmdQuit = new Command("終了", Command.SCREEN, 1);
	/** 説明(マニュアル表示)コマンド */
	private static final Command cmdManual = new Command("説明", Command.SCREEN, 1);
	/** 戻るコマンド */
	private static final Command cmdBack = new Command("戻る", Command.SCREEN, 1);

	/** メインループを継続するかどうか */
	private volatile boolean threadLoop = true;
	/** 強制終了されたかどうか */
	private volatile boolean isKilled = false;
	/** マニュアル表示モード */
	private volatile boolean manualMode = false;
	/** 押されたキーコード */
	private volatile int pressedKeyCode = 0;

	/**
	 * コンストラクタ
	 */
	public Tiny3DCanvas()
	{
		super(false);

		addCommand(cmdQuit);
		addCommand(cmdManual);
		setCommandListener(this);
	}

	/**
	 * スレッド開始時に呼ばれる。
	 */
	public void run()
	{
		try {
			// 画面初期化
			Graphics g = getGraphics();
			final int LEFT_TOP = g.LEFT|g.TOP;
//			Display d = Display.getDisplay(Tiny3DMIDlet.getInstance());

			Font mainFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
			g.setFont(mainFont);
			g.setColor(255, 255, 255);
			g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
			g.setColor(0, 0, 0);
			g.drawString("くるくる3Dデモ", 60, 0, LEFT_TOP);
			for (int i = 0; i <= 60; i += 15) {
				g.drawLine(i * 3, 44, i * 3, 46);
			}

			// ワールド
			T3World world = new T3World(10, 5, 1024);

			// モデルと光源の生成
			Tiny3DData data = new Tiny3DData();
			T3Model model1 = data.generateModel1();
			T3Model model2 = data.generateModel2();
			T3Model model3 = data.generateModel3();
			model3.adjustCenter();
			T3Light light1 = data.generateLight1();
			T3Light light2 = data.generateLight2();

			// モデリング変換
			T3Matrix ident = new T3Matrix();

			T3Vector rotateAxis1a = (new T3Vector(-0.9F, 0.0F, 0.1F)).normalize();
			T3Matrix rotateDelta1a = new T3Matrix();
			T3Matrix rotate1a = new T3Matrix();

			T3Matrix translate1 = new T3Matrix();
			translate1.setTranslate(-1.0F, 0.2F, 0.0F);

			T3Vector rotateAxis1b = (new T3Vector(0.2F, 0.5F, -1.0F)).normalize();
			T3Matrix rotateDelta1b = new T3Matrix();
			T3Matrix rotate1b = new T3Matrix();

			T3Matrix rotateDelta2 = new T3Matrix();
			T3Matrix rotate2 = new T3Matrix();

			T3Matrix scale2 = new T3Matrix();
			scale2.setScale(0.8F, 0.8F, 0.8F);

			T3Matrix translate3 = new T3Matrix();
			translate3.setTranslate(0.0F, -0.3F, 1.5F);

			T3Vector rotateAxis3 = (new T3Vector(-0.1F, 1.0F, -0.1F)).normalize();
			T3Matrix rotateDelta3 = new T3Matrix();
			T3Matrix rotate3 = new T3Matrix();

			T3Matrix translateAll = new T3Matrix();
			translateAll.setTranslate(0.0F, 0.0F, -3.0F);

			// カメラ
			T3Camera camera1 = new T3Camera();
			T3Camera camera2 = new T3Camera();
			T3Camera camera1Init = new T3Camera();
			T3Camera camera2Init = new T3Camera();
			T3Vector eye = new T3Vector(0.0F, 0.0F, 1.0F);
			T3Vector target1 = new T3Vector(0.0F, 0.0F, -2.0F);
			T3Vector target2 = new T3Vector(-1.0F, 0.0F, -3.0F);
			T3Vector up = new T3Vector(0.0F, 1.0F, 0.0F);
			final float INIT_DISTANCE = 3.0F;
			final float INIT_ROLL = 0.0F;
			final float INIT_PITCH = 0.3F;
			final float INIT_YAW = 0.0F;
			float distance = INIT_DISTANCE;
			float roll = INIT_ROLL;
			float pitch = INIT_PITCH;
			float yaw = INIT_YAW;
			camera1Init.lookAtByAngleAndTarget(roll, pitch, yaw, target1, distance);
			camera2Init.lookAtByAngleAndTarget(roll, pitch, yaw - 0.25F * T3Math.PI2, target2, distance);

			// 投影
			// T3Projectionオブジェクト内部でsetClip()するので、個別にGraphicsを渡す
			T3Projection proj1 = new T3Projection(getGraphics());
//			proj1.setOrthographic(1.7F, 1.7F, -1.94F);
//			proj1.setPerspective(1.5F, 1.5F, -1.94F);
			proj1.setPerspectiveByFovY(45.0F / 360.0F * T3Math.PI2, 1.0F, -1.94F);
			proj1.setViewport(20, 50, 200, 200);
			T3Projection proj2 = new T3Projection(getGraphics());
			proj2.setPerspectiveByFovY(45.0F / 360.0F * T3Math.PI2, 2.105F, -1.94F);
			proj2.setViewport(20, 50, 200, 95);
			T3Projection proj3 = new T3Projection(getGraphics());
			proj3.setPerspectiveByFovY(45.0F / 360.0F * T3Math.PI2, 2.105F, -1.94F);
			proj3.setViewport(20, 155, 200, 95);
			int projectMode = 1;

			// 時間管理
			T3TimeManager timeMan = new T3TimeManager(60.0F, 10, 300.0F);
			float delta;

			// 補間
			T3Interpolation erp1 = new T3Interpolation();
			T3Interpolation erp2 = new T3Interpolation();
			boolean erpMode = false;
			T3Matrix erpM = new T3Matrix();
			final float ERP_DURATION = 1.0F;		// 秒数

			// メインループ
			while (threadLoop) {
				// 単位時間の取得
				delta = timeMan.getDelta() * 0.001F;		// 秒単位にする

				// 画面クリア
				g.setColor(255, 255, 255);
				g.fillRect(0, 20, 80, 20);
				g.setColor(0, 0, 0);
				g.fillRect(20, 50, 200, 200);
				if (projectMode == 2) {
					g.setColor(255, 255, 255);
					g.fillRect(20, 145, 200, 10);
				}

				// フレームレートの表示
				float fps = timeMan.getFrameRate();
				g.setColor(0, 0, 0);
				g.setFont(mainFont);
				g.drawString(Integer.toString(T3Math.floatToInt(fps)), 0, 20, LEFT_TOP);
				g.drawString("FPS", 30, 20, LEFT_TOP);
				int x = T3Math.floatToInt(fps * 3.0F);
				g.setColor(0, 0, 255);
				g.fillRect(0, 41, x, 3);
				g.setColor(255, 255, 255);
				g.fillRect((x + 1), 41, 240, 3);

				// キー入力
				if (pressedKeyCode != 0 && manualMode == false) {
					switch (pressedKeyCode) {
					  case KEY_NUM0:
						erp1.init(camera1.transform, camera1Init.transform, ERP_DURATION);
						erp2.init(camera2.transform, camera2Init.transform, ERP_DURATION);
						erpMode = true;
						break;
					  case KEY_NUM1:
						roll += -0.08 * T3Math.PI2 * delta;
						break;
					  case KEY_NUM2:
						distance += -0.12 * T3Math.PI2 * delta;
						break;
					  case KEY_NUM3:
						roll += 0.08 * T3Math.PI2 * delta;
						break;
					  case KEY_NUM8:
						distance += 0.12 * T3Math.PI2 * delta;
						break;
					  case KEY_STAR:
						projectMode = 1;
						break;
					  case KEY_POUND:
						projectMode = 2;
						break;
					  case KEY_NUM4:
					  case KEY_NUM5:
					  case KEY_NUM6:
						break;
					  default:
						switch (getGameAction(pressedKeyCode)) {
						  case UP:
							pitch += -0.08 * T3Math.PI2 * delta;
							break;
						  case DOWN:
							pitch += 0.08 * T3Math.PI2 * delta;
							break;
						  case LEFT:
							yaw += 0.08 * T3Math.PI2 * delta;
							break;
						  case RIGHT:
							yaw += -0.08 * T3Math.PI2 * delta;
							break;
						  case FIRE:
							break;
						}
					}
				}

				// マニュアル表示モード
				if (manualMode) {
					g.setColor(255, 255, 255);
					g.fillRect(30, 60, 180, 180);
					g.setColor(0, 0, 0);
					Font manualFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
					g.setFont(manualFont);
					g.drawString("↑キー：上を向く", 50, 65, LEFT_TOP);
					g.drawString("↓キー：下を向く", 50, 80, LEFT_TOP);
					g.drawString("←キー：左を向く", 50, 95, LEFT_TOP);
					g.drawString("→キー：右を向く", 50, 110, LEFT_TOP);
					g.drawString(" 1キー：左に傾く", 50, 125, LEFT_TOP);
					g.drawString(" 3キー：右に傾く", 50, 140, LEFT_TOP);
					g.drawString(" 2キー：近づく", 50, 155, LEFT_TOP);
					g.drawString(" 8キー：遠ざかる", 50, 170, LEFT_TOP);
					g.drawString(" 0キー：元の位置に戻る", 50, 185, LEFT_TOP);
					g.drawString(" *キー：1画面モード", 50, 205, LEFT_TOP);
					g.drawString(" #キー：2画面モード", 50, 220, LEFT_TOP);
				} else {

					// カメラの移動
					if (!erpMode) {
//						camera1.lookAt(eye, target, up);
//						camera1.lookAtByAngleAndEye(roll, pitch, yaw, eye);
						camera1.lookAtByAngleAndTarget(roll, pitch, yaw, target1, distance);
						camera2.lookAtByAngleAndTarget(roll, pitch, yaw - 0.25F * T3Math.PI2, target2, distance);
					} else {
						erp1.interpolate(erpM, delta);
						camera1.setTransform(erpM);
						erp2.interpolate(erpM, delta);
						camera2.setTransform(erpM);
						if (erp1.isEnd()) {
							distance = INIT_DISTANCE;
							roll = INIT_ROLL;
							pitch = INIT_PITCH;
							yaw = INIT_YAW;
							erpMode = false;
						}
					}

					// モデルと光源の移動
					rotateDelta1a.setRotate(0.5F * T3Math.PI2 * delta, rotateAxis1a);
					rotateDelta1b.setRotate(0.17F * T3Math.PI2 * delta, rotateAxis1b);
					model1.setTransform(ident);
					model1.addTransform(rotate1a);
					model1.addTransform(translate1);
					model1.addTransform(rotate1b);
					model1.addTransform(translateAll);

					rotateDelta2.setRotateY(-0.03F * T3Math.PI2 * delta);
					model2.setTransform(ident);
					model2.addTransform(rotate2);
					model2.addTransform(scale2);
					model2.addTransform(translateAll);

					rotateDelta3.setRotate(0.1F * T3Math.PI2 * delta, rotateAxis3);
					model3.setTransform(ident);
					model3.addTransform(translate3);
					model3.addTransform(rotate3);
					model3.addTransform(translateAll);

					light2.setTransform(translateAll);

					// ワールドへの配置とレンダリング
					world.clearModelAndLight();
					world.addModel(model1);
					world.addModel(model2);
					world.addModel(model3);
					world.addLight(light1);
					world.addLight(light2);
					switch (projectMode) {
					  case 1:
						world.setCamera(camera1);
						world.setProjection(proj1);
						world.render(true, true);
						break;
					  case 2:
						world.setCamera(camera1);
						world.setProjection(proj2);
						world.render(true, true);
						world.setCamera(camera2);
						world.setProjection(proj3);
						world.render(true, true);
						break;
					}

					// 回転変換の更新
					rotate1a.multFromRight(rotateDelta1a);
					rotate1b.multFromRight(rotateDelta1b);
					rotate2.multFromRight(rotateDelta2);
					rotate3.multFromRight(rotateDelta3);
				}

				flushGraphics();

				// 時間計測とスリープ
				timeMan.measureAndSleep();
			}

			if (isKilled == false) {
				Tiny3DMIDlet.getInstance().terminate();
			}
		} catch (Exception e) {
//			System.out.println(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * コマンド実行時に呼ばれる。
	 * @param c (IN ) コマンド
	 * @param d (IN ) 表示対象
	 */
	public void commandAction(Command c, Displayable d)
	{
		if (c == cmdQuit) {
			threadLoop = false;
		} else if (c == cmdManual) {
			removeCommand(cmdManual);
			addCommand(cmdBack);
			manualMode = true;
		} else if (c == cmdBack) {
			removeCommand(cmdBack);
			addCommand(cmdManual);
			manualMode = false;
		}
	}

	/**
	 * キーが押された時に呼ばれる。
	 * @param keyCode (IN ) キーコード
	 */
	public void keyPressed(int keyCode)
	{
		pressedKeyCode = keyCode;
	}

	/**
	 * キーが離された時に呼ばれる。
	 * @param keyCode (IN ) キーコード
	 */
	public void keyReleased(int keyCode)
	{
		pressedKeyCode = 0;
	}

	/**
	 * キャンバスを強制終了する。
	 */
	public void kill()
	{
		isKilled = true;
		threadLoop = false;
	}
}

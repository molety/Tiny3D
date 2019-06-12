import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * MIDlet (アプリケーション)
 */
public class Tiny3DMIDlet extends MIDlet
{
	/** 自分自身を保持する */
	private static Tiny3DMIDlet instance;
	/** Canvasオブジェクト */
	private Tiny3DCanvas t3dCanvas;
	/** スレッド */
	private static Thread th = null;

	/**
	 * コンストラクタ
	 */
	public Tiny3DMIDlet()
	{
		instance = this;

		t3dCanvas = new Tiny3DCanvas();

		Display.getDisplay(this).setCurrent(t3dCanvas);

//		if (th != null) {
//			System.out.println("th != null !!");
//			if (th.isAlive()) {
//				System.out.println("th.isAlive() !!");
//			}
//		}
		th = new Thread(t3dCanvas);
		th.start();
//		System.out.println("Tiny3DMIDlet() !!");
	}

	/**
	 * アプリケーション開始時に呼ばれる。
	 */
	public void startApp()
	{
//		System.out.println("startApp() !!");
	}

	/**
	 * アプリケーション一時停止時に呼ばれる。
	 */
	public void pauseApp()
	{
//		System.out.println("pauseApp() !!");
	}

	/**
	 * アプリケーション終了時に呼ばれる。
	 * @param unconditional (IN ) 無条件に終了する必要がある場合は真
	 */
	public void destroyApp(boolean unconditional)
	{
		try {
			if (th != Thread.currentThread()) {
				t3dCanvas.kill();
				th.join();
//				System.out.println("joined !!");
			}
		} catch (InterruptedException e) {
		}
//		th = null;
//		System.out.println("destroyApp() !!");
	}

	/**
	 * MIDletオブジェクトを取得する。
	 * @return MIDletオブジェクト
	 */
	public static Tiny3DMIDlet getInstance()
	{
		return instance;
	}

	/**
	 * アプリケーションを終了する。
	 */
	public void terminate()
	{
		destroyApp(true);
		notifyDestroyed();
	}
}


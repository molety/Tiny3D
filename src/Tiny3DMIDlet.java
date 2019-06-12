import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * MIDlet (�A�v���P�[�V����)
 */
public class Tiny3DMIDlet extends MIDlet
{
	/** �������g��ێ����� */
	private static Tiny3DMIDlet instance;
	/** Canvas�I�u�W�F�N�g */
	private Tiny3DCanvas t3dCanvas;
	/** �X���b�h */
	private static Thread th = null;

	/**
	 * �R���X�g���N�^
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
	 * �A�v���P�[�V�����J�n���ɌĂ΂��B
	 */
	public void startApp()
	{
//		System.out.println("startApp() !!");
	}

	/**
	 * �A�v���P�[�V�����ꎞ��~���ɌĂ΂��B
	 */
	public void pauseApp()
	{
//		System.out.println("pauseApp() !!");
	}

	/**
	 * �A�v���P�[�V�����I�����ɌĂ΂��B
	 * @param unconditional (IN ) �������ɏI������K�v������ꍇ�͐^
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
	 * MIDlet�I�u�W�F�N�g���擾����B
	 * @return MIDlet�I�u�W�F�N�g
	 */
	public static Tiny3DMIDlet getInstance()
	{
		return instance;
	}

	/**
	 * �A�v���P�[�V�������I������B
	 */
	public void terminate()
	{
		destroyApp(true);
		notifyDestroyed();
	}
}


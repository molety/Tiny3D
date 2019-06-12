import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

/**
 * �L�����o�X
 */
public class Tiny3DCanvas extends GameCanvas implements Runnable, CommandListener
{
	/** �L�����o�X�� */
	public final int CANVAS_WIDTH = getWidth();
	/** �L�����o�X���� */
	public final int CANVAS_HEIGHT = getHeight();

	/** �I���R�}���h */
	private static final Command cmdQuit = new Command("�I��", Command.SCREEN, 1);
	/** ����(�}�j���A���\��)�R�}���h */
	private static final Command cmdManual = new Command("����", Command.SCREEN, 1);
	/** �߂�R�}���h */
	private static final Command cmdBack = new Command("�߂�", Command.SCREEN, 1);

	/** ���C�����[�v���p�����邩�ǂ��� */
	private volatile boolean threadLoop = true;
	/** �����I�����ꂽ���ǂ��� */
	private volatile boolean isKilled = false;
	/** �}�j���A���\�����[�h */
	private volatile boolean manualMode = false;
	/** �����ꂽ�L�[�R�[�h */
	private volatile int pressedKeyCode = 0;

	/**
	 * �R���X�g���N�^
	 */
	public Tiny3DCanvas()
	{
		super(false);

		addCommand(cmdQuit);
		addCommand(cmdManual);
		setCommandListener(this);
	}

	/**
	 * �X���b�h�J�n���ɌĂ΂��B
	 */
	public void run()
	{
		try {
			// ��ʏ�����
			Graphics g = getGraphics();
			final int LEFT_TOP = g.LEFT|g.TOP;
//			Display d = Display.getDisplay(Tiny3DMIDlet.getInstance());

			Font mainFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
			g.setFont(mainFont);
			g.setColor(255, 255, 255);
			g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
			g.setColor(0, 0, 0);
			g.drawString("���邭��3D�f��", 60, 0, LEFT_TOP);
			for (int i = 0; i <= 60; i += 15) {
				g.drawLine(i * 3, 44, i * 3, 46);
			}

			// ���[���h
			T3World world = new T3World(10, 5, 1024);

			// ���f���ƌ����̐���
			Tiny3DData data = new Tiny3DData();
			T3Model model1 = data.generateModel1();
			T3Model model2 = data.generateModel2();
			T3Model model3 = data.generateModel3();
			model3.adjustCenter();
			T3Light light1 = data.generateLight1();
			T3Light light2 = data.generateLight2();

			// ���f�����O�ϊ�
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

			// �J����
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

			// ���e
			// T3Projection�I�u�W�F�N�g������setClip()����̂ŁA�ʂ�Graphics��n��
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

			// ���ԊǗ�
			T3TimeManager timeMan = new T3TimeManager(60.0F, 10, 300.0F);
			float delta;

			// ���
			T3Interpolation erp1 = new T3Interpolation();
			T3Interpolation erp2 = new T3Interpolation();
			boolean erpMode = false;
			T3Matrix erpM = new T3Matrix();
			final float ERP_DURATION = 1.0F;		// �b��

			// ���C�����[�v
			while (threadLoop) {
				// �P�ʎ��Ԃ̎擾
				delta = timeMan.getDelta() * 0.001F;		// �b�P�ʂɂ���

				// ��ʃN���A
				g.setColor(255, 255, 255);
				g.fillRect(0, 20, 80, 20);
				g.setColor(0, 0, 0);
				g.fillRect(20, 50, 200, 200);
				if (projectMode == 2) {
					g.setColor(255, 255, 255);
					g.fillRect(20, 145, 200, 10);
				}

				// �t���[�����[�g�̕\��
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

				// �L�[����
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

				// �}�j���A���\�����[�h
				if (manualMode) {
					g.setColor(255, 255, 255);
					g.fillRect(30, 60, 180, 180);
					g.setColor(0, 0, 0);
					Font manualFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
					g.setFont(manualFont);
					g.drawString("���L�[�F�������", 50, 65, LEFT_TOP);
					g.drawString("���L�[�F��������", 50, 80, LEFT_TOP);
					g.drawString("���L�[�F��������", 50, 95, LEFT_TOP);
					g.drawString("���L�[�F�E������", 50, 110, LEFT_TOP);
					g.drawString(" 1�L�[�F���ɌX��", 50, 125, LEFT_TOP);
					g.drawString(" 3�L�[�F�E�ɌX��", 50, 140, LEFT_TOP);
					g.drawString(" 2�L�[�F�߂Â�", 50, 155, LEFT_TOP);
					g.drawString(" 8�L�[�F��������", 50, 170, LEFT_TOP);
					g.drawString(" 0�L�[�F���̈ʒu�ɖ߂�", 50, 185, LEFT_TOP);
					g.drawString(" *�L�[�F1��ʃ��[�h", 50, 205, LEFT_TOP);
					g.drawString(" #�L�[�F2��ʃ��[�h", 50, 220, LEFT_TOP);
				} else {

					// �J�����̈ړ�
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

					// ���f���ƌ����̈ړ�
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

					// ���[���h�ւ̔z�u�ƃ����_�����O
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

					// ��]�ϊ��̍X�V
					rotate1a.multFromRight(rotateDelta1a);
					rotate1b.multFromRight(rotateDelta1b);
					rotate2.multFromRight(rotateDelta2);
					rotate3.multFromRight(rotateDelta3);
				}

				flushGraphics();

				// ���Ԍv���ƃX���[�v
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
	 * �R�}���h���s���ɌĂ΂��B
	 * @param c (IN ) �R�}���h
	 * @param d (IN ) �\���Ώ�
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
	 * �L�[�������ꂽ���ɌĂ΂��B
	 * @param keyCode (IN ) �L�[�R�[�h
	 */
	public void keyPressed(int keyCode)
	{
		pressedKeyCode = keyCode;
	}

	/**
	 * �L�[�������ꂽ���ɌĂ΂��B
	 * @param keyCode (IN ) �L�[�R�[�h
	 */
	public void keyReleased(int keyCode)
	{
		pressedKeyCode = 0;
	}

	/**
	 * �L�����o�X�������I������B
	 */
	public void kill()
	{
		isKilled = true;
		threadLoop = false;
	}
}

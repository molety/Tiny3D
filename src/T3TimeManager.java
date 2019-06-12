/**
 * 時間管理
 */
public class T3TimeManager
{
	/** フレームレート(FPS) */
	private float frameRate;
	/** 最大フレームレート */
	private float maxFrameRate;
	/** 単位時間(δt) */
	private float delta;
	/** 理想の単位時間 */
	private float idealDelta;
	/** 単位時間の理想からの誤差 */
	private float errorDelta;
	/** 見かけ上の単位時間の最大値 */
	private float maxVirtualDelta;
	/** 最小スリープ時間 */
	private long minSleepTime;
	/** 通算経過時間 */
	private float elapsedTime;
	/** 前回の時刻 */
	private long prevTime;

	/**
	 * コンストラクタ
	 * @param maxFrameRate    (IN ) 最大フレームレート
	 * @param minSleepTime    (IN ) 最小スリープ時間
	 * @param maxVirtualDelta (IN ) 見かけ上の単位時間の最大値 (最大値を決めなくてよければ0以下にしておく)
	 */
	public T3TimeManager(float maxFrameRate, long minSleepTime, float maxVirtualDelta)
	{
		this.maxFrameRate = maxFrameRate;
		this.minSleepTime = minSleepTime;
		this.maxVirtualDelta = maxVirtualDelta;

		idealDelta = 1000.0F / maxFrameRate;
		delta = idealDelta;
		errorDelta = 0.0F;
		elapsedTime = 0.0F;
		frameRate = this.maxFrameRate;

		prevTime = System.currentTimeMillis();
	}

	/**
	 * 時間を計測し、スリープする。
	 */
	public void measureAndSleep()
	{
		long currTime = System.currentTimeMillis();
		long workTime = Math.max((currTime - prevTime), 0);
		long sleepTime = Math.max(((long)(idealDelta - errorDelta) - workTime), minSleepTime);

		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
		}

		long wakedTime = System.currentTimeMillis();
		long actualSleepTime = Math.max((wakedTime - currTime), 0);
		delta = (float)(workTime + actualSleepTime);
		errorDelta += delta - idealDelta;
		if (errorDelta > 10000.0F) {
			errorDelta = 0.0F;				// errorDeltaがたまりすぎたら適当なところでリセット
		}
		frameRate = 1000.0F / delta;
		elapsedTime += delta;

		prevTime = wakedTime;

//		System.out.println("W"+Long.toString(workTime)+"S"+Long.toString(sleepTime)+"A"+Long.toString(actualSleepTime)+"E"+Float.toString(errorDelta));
	}

	/**
	 * 見かけ上の単位時間を得る。
	 * ※処理負荷が高すぎるときに、値が大きくなりすぎないようにするため、
	 *   リミットを設けている。(見かけ上の時間の進み方が遅くなる)
	 * @return 見かけ上の単位時間
	 */
	public float getDelta()
	{
		float virtualDelta;

		if (maxVirtualDelta > 0.0F) {
			virtualDelta = Math.min(delta, maxVirtualDelta);
		} else {
			virtualDelta = delta;
		}
		return virtualDelta;
	}

	/**
	 * 通算経過時間を得る。
	 * ※単位時間の方は、値が大きくなりすぎないように小細工しているが、
	 *   通算経過時間は本来の値そのままなので注意。
	 * @return 通算経過時間
	 */
	public float getElapsedTime()
	{
		return elapsedTime;
	}

	/**
	 * フレームレートを得る。
	 * @return フレームレート
	 */
	public float getFrameRate()
	{
		return frameRate;
	}
}

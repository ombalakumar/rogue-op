// GameProc.java
// Game main logic class
//
// Copyright ©2010 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode;


import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;


public class GameProc extends Activity implements Runnable
{
	public static final long UPDATE_FREQ = 30;
	public static final long UPDATE_PERIOD = 1000 / UPDATE_FREQ;
	public static final String TAG = "ionoclast";

	protected static GameProc sOnly;
	protected static Thread sUpdateThread;

	// stats
	protected long mElapsedTime;
	protected static int sSeconds;
	protected static long sFPS;

	protected boolean mTouching;
	protected XYf mTouchPos;

	protected boolean mRunning;
	protected boolean mRestarting;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedState)
	{
		Log.d(TAG, "onCreate()");

		// these things need to be recreated every time //

		super.onCreate(savedState);
		sOnly = this;

		// set up graphics
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window tWin = getWindow();
		tWin.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		tWin.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		//		// initialize input data
		//		mTouching = false;
		//		mTouchPos = new XYf();
		//		mTouchArea = new Rect();
		//
		//		// initialize stats
		//		mElapsedTime = 0;
		//		sSeconds = 0;
		//		sFPS = 0;
		//
		//		mRestarting = false;
		//
		//		if(savedState != null)
		//		{
		//			Log.d(TAG, "  Restoring state");
		//			super.onRestoreInstanceState(savedState);
		//			ActionElement.sAllAEs = (Array<ActionElement>)savedState
		//					.getSerializable("sAllAEs");
		//			AudioResource.sAllARs = (HashMap<Integer, AudioResource>)savedState
		//					.getSerializable("sAllARs");
		//		}
		//
		//		// user-provided init code
		//		InitializeOnce();
	}

	//	@Override
	//	public void onSaveInstanceState(Bundle outState)
	//	{
	//		Log.d(TAG, "onSaveInstanceState()");
	//		super.onSaveInstanceState(outState);
	//		outState.putSerializable("sAllAEs", ActionElement.sAllAEs);
	//		outState.putSerializable("sAllARs", AudioResource.sAllARs);
	//	}

	//	/** Called on app shutdown. */
	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy()");
		super.onDestroy();

		Shutdown();

		// clean some up now
		Runtime r = Runtime.getRuntime();
		r.gc();
	}

	//	/** Called when the activity becomes visible. */
	//	@Override
	//	public void onStart()
	//	{
	//		Log.d(TAG, "onStart()");
	//		super.onStart();
	//		if(!mRestarting)
	//		{
	//			Log.d(TAG, "  not restarting");
	//		}
	//		else
	//		{
	//			Log.d(TAG, "  restarting");
	//			mRestarting = false;
	//		}
	//		if(AnimatedView.sOnly == null)
	//			AnimatedView.sOnly = new AnimatedView(this);
	//		(AnimatedView.sRenderThread = new Thread(AnimatedView.sOnly)).start();
	//	}

	//	/** Called when the activity becomes visible again after being hidden. */
	//	@Override
	//	public void onRestart()
	//	{
	//		Log.d(TAG, "onRestart()");
	//		super.onRestart();
	//		mRestarting = true;
	//	}

	//	/** Called when the activity becomes hidden. */
	//	@Override
	//	public void onStop()
	//	{
	//		Log.d(TAG, "onStop()");
	//		super.onStop();
	//		AnimatedView.sOnly.Die();
	//	}

	/**
	 * Called when app becomes active.
	 */
	@Override
	public void onResume()
	{
		Log.d(TAG, "onResume()");
		super.onResume();

		//		if(AnimatedView.sOnly == null)
		new AnimatedView(this);

		// input data
		mTouching = false;
		mTouchPos = new XYf();

		// initialize stats
		mElapsedTime = 0;
		sSeconds = 0;
		sFPS = 0;

		//		mRestarting = false;

		// user-provided init code
		//		InitializeOnce();

		Log.d(TAG, "  creating static arrays");
		ActionElement.Init();
		ScreenElement.Init();
		SoundEffect.Init();

		// user initialization code
		InitializeOnResume();

		// start the render and update threads
		(sUpdateThread = new Thread(this)).start();
		(AnimatedView.sRenderThread = new Thread(AnimatedView.sOnly)).start();

		// clean some up now to avoid latency later
		Runtime r = Runtime.getRuntime();
		r.gc();
	}

	/** Called when app is backgrounded, but may still be visible. */
	@Override
	public void onPause()
	{
		Log.d(TAG, "onPause()");
		super.onPause();
		Die();
		AnimatedView.sOnly.Die();
		AudioResource.Die(); // stop and free all audio resources
		SoundEffect.Die(); // free the sound pool
	}

	/** Thread to run logic updates */
	@Override
	public void run()
	{
		Log.d(TAG, "Entering update thread");
		mRunning = true;
		while(mRunning)
		{
			long start = SystemClock.uptimeMillis();

			// calc stats
			if(mElapsedTime >= 999)
			{
				synchronized(this)
				{
					sSeconds++;
					mElapsedTime -= 1000;
					sFPS = AnimatedView.sOnly.FPS();
				}
			}

			Update();

			long end = SystemClock.uptimeMillis();
			long tLastUpdate = end - start;
			mElapsedTime += UPDATE_PERIOD;
			try
			{
				long tSleep = UPDATE_PERIOD - tLastUpdate;
				if(tSleep < 1)
					tSleep = 1;
				Thread.sleep(tSleep);
			}
			catch(InterruptedException e)
			{
			}
		}
		Log.d(TAG, "Exiting update thread");
	}

	// public interfaces //

	/** Calls update on all extant ActionElements. */
	public void Update()
	{
		for(int i = 0; i < ActionElement.sAllAEs.size; i++)
		{
			try
			{
				ActionElement tAE = ActionElement.sAllAEs.At(i);
				if(tAE.Active())
					tAE.Update();
			}
			catch(Exception e)
			{
			}
		}
	}

	/** Query performance statistics based on update timing. */
	// TODO: does this belong here? Move to AnimatedView
	long FPS()
	{
		synchronized(this)
		{
			return sFPS;
		}
	}

	/** Query uptime statistics for update thread. */
	long Seconds()
	{
		synchronized(this)
		{
			return sSeconds;
		}
	}

	/** Stops the update thread. */
	void Die()
	{
		synchronized(this)
		{
			mRunning = false;
			sUpdateThread = null;
		}
	}

	/** Public accessor for static singleton instance. */
	public static GameProc Singleton()
	{
		return sOnly;
	}

	/** Override this in your derived class to make stuff. */
	public void InitializeOnce()
	{
	}

	/** Override this to hook the resume event. */
	public void InitializeOnResume()
	{
	}

	/** Override this to hook the shutdown event. */
	public void Shutdown()
	{
	}

	// user input callbacks //

	@Override
	public boolean onTouchEvent(MotionEvent pEvent)
	{
		// handle touch events
		int tAction = pEvent.getAction();
		if(tAction == MotionEvent.ACTION_DOWN
				|| tAction == MotionEvent.ACTION_MOVE)
		{
			mTouching = true;
			mTouchPos.x = pEvent.getX();
			mTouchPos.y = pEvent.getY();
		}
		else if(pEvent.getAction() == MotionEvent.ACTION_UP)
		{
			mTouching = false;
		}

		// slow down incoming touch events
		try
		{
			Thread.sleep(UPDATE_PERIOD);
		}
		catch(InterruptedException e)
		{
		}
		return true;
	}

	public boolean Touching()
	{
		return mTouching;
	}

	public XYf TouchPos()
	{
		return mTouchPos;
	}
}
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


import rogue_opcode.geometrics.XYf;
import rogue_opcode.soundy.SoundEffect;
import android.app.Activity;
import android.content.SharedPreferences;
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

	public static GameProc sOnly;
	protected static Thread sUpdateThread;

	// stats
	protected long mElapsedTime;
	protected static int sSeconds;
	protected static long sFPS;

	protected boolean mTouching;
	protected XYf mTouchPos;

	protected boolean mRunning;
	protected boolean mRestarting;
	protected boolean mExiting;

	// app lifecycle ///////////////////////////////////////////////////////////

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedState)
	{
		Log.d(TAG, "onCreate()");
		super.onCreate(savedState);
		sOnly = this;

		// set up graphics
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window tWin = getWindow();
		tWin.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		tWin.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		new AnimatedView(this);

		// initialize stats
		mElapsedTime = 0;
		sSeconds = 0;
		sFPS = 0;

		//		if(savedState != null)
		//		{
		//			Log.d(TAG, "  Restoring state");
		//			super.onRestoreInstanceState(savedState);
		//			ActionElement.sAllAEs = (Array<ActionElement>)savedState
		//					.getSerializable("sAllAEs");
		//			AudioResource.sAllARs = (HashMap<Integer, AudioResource>)savedState
		//					.getSerializable("sAllARs");
		//		}

		// user-provided init code
		InitializeOnce();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		Log.d(TAG, "onSaveInstanceState()");
		super.onSaveInstanceState(outState);
		//			outState.putSerializable("sAllAEs", ActionElement.sAllAEs);
		//			outState.putSerializable("sAllARs", AudioResource.sAllARs);
	}

	/**
	 * Called when app becomes active.
	 */
	@Override
	protected void onResume()
	{
		Log.d(TAG, "onResume()");
		super.onResume();

		// input data
		mTouching = false;
		mTouchPos = new XYf();

		// restore user settings
		SharedPreferences tPrefs = getPreferences(0);
		if(tPrefs.getBoolean("saved", false))
		{
			// TODO: restore settings to SettingsDB object in loop
		}
		else
		{
			// TODO: load SettingsDB with default values
		}

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
	protected void onPause()
	{
		Log.d(TAG, "onPause()");
		super.onPause();

		// save user settings
		SharedPreferences.Editor tEditor = getPreferences(0).edit();
		// TODO: save user settings in a loop from a static SettingsDB;
		// each entry is { "name", setting_type, setting_data }.
		tEditor.putBoolean("saved", true);
		tEditor.commit();

		// shut down
		Die();
		AnimatedView.sOnly.Die();
		AudioResource.Die(); // stop and free all audio resources
		SoundEffect.Die(); // free the sound pool

		// clean some up now to avoid latency later
		Runtime r = Runtime.getRuntime();
		r.gc();
	}

	//	/** Called on app shutdown. */
	@Override
	protected void onDestroy()
	{
		Log.d(TAG, "onDestroy()");
		super.onDestroy();

		Shutdown();
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

	// game update loop ////////////////////////////////////////////////////////

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

	// runtime stats ///////////////////////////////////////////////////////////

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

	// user input callbacks ////////////////////////////////////////////////////

	// TODO: Event callback/interface for listeners; sort on Z?
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
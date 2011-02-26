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
// import rogue_opcode.soundy.SoundEffect;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;


public class GameProc extends Activity implements Runnable, OnGestureListener,
		OnDoubleTapListener
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

	protected boolean mMousing;
	protected XYf mMousePos;

	private GestureDetector detector;
	public TouchState mTouchState;

	int mCurrentKey;
	public boolean[] mKeys; //holds the state of the keys on the keyboard


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

		mKeys = new boolean[525];

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
		detector = new GestureDetector(this, this);
		mTouchState = new TouchState();

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
		mMousing = false;
		mMousePos = new XYf();

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
		//SoundEffect.Init();

		// user initialization code
		InitializeOnResume();

		// start the render and update threads
		(sUpdateThread = new Thread(this)).start();
		(AnimatedView.sRenderThread = new Thread(AnimatedView.sOnly)).start();

		AnimatedView.sOnly.requestFocus();


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

		Shutdown();

		// save user settings
		SharedPreferences.Editor tEditor = getPreferences(0).edit();
		// TODO: save user settings in a loop from a static SettingsDB;
		// each entry is { "name", setting_type, setting_data }.
		tEditor.putBoolean("saved", true);
		tEditor.commit();

		// shut down
		Die();
		AnimatedView.sOnly.Die();
		//AudioResource.Die(); // stop and free all audio resources
		//SoundEffect.Die(); // free the sound pool

		// clean some up now to avoid latency later
		Runtime r = Runtime.getRuntime();
		r.gc();
	}

	//	/** Called on app shutdown. */
	//@Override
	/*
	 * protected void onDestroy()
	 * {
	 * Log.d(TAG, "onDestroy()");
	 * super.onDestroy();
	 * 
	 * Shutdown();
	 * }
	 */

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
		
		mTouchState.Clear();
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

	@Override
	public boolean onTrackballEvent(MotionEvent pEvent)
	{
		mMousePos.x = (int)(pEvent.getX() * 100);
		mMousePos.y = (int)(pEvent.getY() * 100);

		mMousing = true;
		return true;
	}

	public boolean Mousing()
	{
		return mMousing;
	}

	public XYf MousePos()
	{
		mMousing = false;
		return mMousePos;
	}


	@Override
	public boolean onTouchEvent(MotionEvent me)
	{
		this.detector.onTouchEvent(me);
		return super.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		//Toast.makeText(GameProc.sOnly, "down", 0).show();
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY)
	{
		mTouchState.SetState(TouchState.FLING, e1, e2);
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
		mTouchState.SetState(TouchState.LONG_TOUCH, e, null);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY)
	{
		//Toast.makeText(GameProc.sOnly, "scroll", 0).show();
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
		//Toast.makeText(GameProc.sOnly, "show press", 0).show();
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		//Toast.makeText(GameProc.sOnly, "single tap up", 0).show();
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e)
	{
		mTouchState.SetState(TouchState.DOUBLE_TAP, e, null);
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		//Toast.makeText(GameProc.sOnly, "double tap event", 0).show();
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e)
	{
		mTouchState.SetState(TouchState.SINGLE_TAP, e, null);
		return false;
	}


	/**
	 * TouchState is an inner class that holds state information about touch
	 * events.
	 * It is designed to be polled instead of event-driven.
	 * 
	 * @author Christopher R. Tooley
	 * 
	 */
	protected class TouchState
	{
		public static final int SINGLE_TAP = 1;
		public static final int DOUBLE_TAP = 2;
		public static final int FLING = 4;
		public static final int SCROLL = 8;
		public static final int LONG_TOUCH = 16;

		int mState;

		MotionEvent mMainMotionEvent;
		MotionEvent mSecondaryMotionEvent;

		TouchState()
		{
			Clear();
		}

		public void SetState(int pState, MotionEvent pMainMotionEvent,
				MotionEvent pSecondaryMotionEvent)
		{
			synchronized(this)
			{
				mState |= pState;
				mMainMotionEvent = pMainMotionEvent;
				mSecondaryMotionEvent = pSecondaryMotionEvent;
			}
		}

		public void Clear()
		{
			synchronized(this)
			{
				mState = 0;
			}
		}

		public XYf TouchPos() {
			return new XYf(mMainMotionEvent.getX(), mMainMotionEvent.getY());
		}
		
		public XYf SecondaryTouchPos() {
			return new XYf(mSecondaryMotionEvent.getX(), mSecondaryMotionEvent.getY());
		}
		
		public float GetMainX()
		{
			float tX = 0;
			synchronized(this)
			{
				tX = mMainMotionEvent.getX();
			}
			return tX;
		}

		public float GetMainY()
		{
			float tY = 0;
			synchronized(this)
			{
				tY = mMainMotionEvent.getY();
			}
			return tY;
		}

		public float GetSecondaryX()
		{
			float tX = 0;
			synchronized(this)
			{
				tX = mSecondaryMotionEvent.getX();
			}
			return tX;
		}

		public float GetSecondaryY()
		{
			float tY = 0;
			synchronized(this)
			{
				tY = mSecondaryMotionEvent.getY();
			}
			return tY;
		}

		public boolean Is(int pState)
		{
			synchronized(this)
			{
				return 0 != (mTouchState.mState & pState);
			}
		}
	}

}
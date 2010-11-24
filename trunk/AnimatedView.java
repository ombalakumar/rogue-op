// AnimatedView.java
// AnimatedView class, Android version.
//
// Copyright ©2010 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class AnimatedView
	extends SurfaceView implements SurfaceHolder.Callback, Runnable
{
	// TODO: make these static for performance when possible? //

	// self-refs
	protected static AnimatedView sOnly;
	protected static Thread sRenderThread;

	// physical screen stuff
	protected SurfaceHolder mHolder;
	protected Canvas mCurrentCanvas;
	protected boolean mHasSurface = false;
	protected int mScreenWidth, mScreenHeight;
	protected boolean mSized = false;

	// Base refers to desired dimensions and aspect ratio of the ideal machine,
	// used for reference when scaling virtual dimensions to physical dimensions
	protected int mBaseWidth, mBaseHeight;
	protected float mBaseAspectRatio;
	public float mPreScaler = 1.0f;

	// scrolling proterties
	// TODO: this gets too jerky—need something that jumps as the player gets
	// too close to being offscreen. Maybe scale based on distance from center?
	protected float mBaseHScroll = 0;
	static XYf mScrollSpeed = new XYf(2, 2);
	ScreenElement mKeepCenteredSE = null;

	// debug stats
	protected Paint mDebugPaint;
	protected static int sFramesDrawn;
	protected static boolean sDebug;

	boolean mRunning;

	// c'tor ///////////////////////////////////////////////////////////////////

	public AnimatedView(Context pContext)
	{
		super(pContext);
		sOnly = this;

		// init debug stats
		mDebugPaint = new Paint();
		mDebugPaint.setColor(Color.WHITE);
		sFramesDrawn = 0;
		sDebug = false;

		// init screen/rendering
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

		((Activity)pContext).setContentView(this);

		DisplayMetrics tDM = new DisplayMetrics();
		GameProc.sOnly.getWindowManager().getDefaultDisplay().getMetrics(tDM);
		mScreenWidth = tDM.widthPixels;
		mScreenHeight = tDM.heightPixels;
	}

	public static AnimatedView Singleton()
	{
		return sOnly;
	}

	// screen and drawing properties ///////////////////////////////////////////

	/**
	 * Set up a display with specified virtual resolution and aspect ratio. This
	 * method is used to tell {@code AnimatedView} the logical screen size and
	 * aspect ratio we are programming to, allowing it to pre-scale all graphics
	 * and draw positions appropriately.
	 *
	 * @param pWidth virtual width of display area.
	 * @param pHeight virtual height of display area.
	 */
	public void NormailzeResolution(int pWidth, int pHeight)
	{
		mBaseWidth = pWidth;
		mBaseHeight = pHeight;

		mBaseAspectRatio = (float)pWidth / (float)pHeight;
		float tScreenAspectRatio = (float)mScreenWidth / (float)mScreenHeight;
		if(mBaseAspectRatio < tScreenAspectRatio)
			mPreScaler = (float)mScreenHeight / (float)mBaseHeight;
		else
			mPreScaler = (float)mScreenWidth / (float)mBaseWidth;
	}

	public Canvas CurrentCanvas()
	{
		return mCurrentCanvas;
	}

	// XXX: is this still relevant?
	public void SetKeepCenteredSE(ScreenElement pKeepCenteredSE)
	{
		mKeepCenteredSE = pKeepCenteredSE;
	}

	public int VirtualWidth()
	{
		return mBaseWidth;
	}

	public int VirtualHeight()
	{
		return mBaseHeight;
	}

	public int ScreenWidth()
	{
		synchronized(this)
		{
			return mScreenWidth;
		}
	}

	public int ScreenHeight()
	{
		synchronized(this)
		{
			return mScreenHeight;
		}
	}

	public int FPS()
	{
		synchronized(this)
		{
			int tFrames = sFramesDrawn;
			sFramesDrawn = 0;
			return tFrames;
		}
	}

	public boolean Debug()
	{
		synchronized(this)
		{
			return sDebug;
		}
	}

	public void Debug(boolean pDebug)
	{
		synchronized(this)
		{
			sDebug = pDebug;
		}
	}

	// render thread ///////////////////////////////////////////////////////////

	public void Die()
	{
		mRunning = false;
		sRenderThread = null;
	}

	/** render thread loop */
	@Override
	public void run()
	{
		Log.d(GameProc.TAG, "Entering render thread");
		mRunning = true;
		while(mRunning)
		{
			synchronized(this)
			{
				while(!mHasSurface)
				{
					try
					{
						wait(); // wait for a valid surface
					}
					catch(InterruptedException e)
					{
					}
				}
				sFramesDrawn++;
			}
			draw();
		}
		Log.d(GameProc.TAG, "Exiting render thread");
	}

	/** draws all ScreenElements to the screen */
	protected void draw()
	{
		Canvas tCanvas = mHolder.lockCanvas();
		if(tCanvas == null)
			return;

		// Draw the "keep-centered" graphic
		// (You are not expected to understand this—I don't!)
		if(mKeepCenteredSE != null)
		{
			if(((mKeepCenteredSE.mPos.x - (mBaseWidth / 6)) + mBaseHScroll)
					< (mBaseWidth / 2))
				mBaseHScroll += (mScrollSpeed.x * mPreScaler);
			if(((mKeepCenteredSE.mPos.x + (mBaseWidth / 6)) + mBaseHScroll)
					> (mBaseWidth / 2))
				mBaseHScroll -= (mScrollSpeed.x * mPreScaler);
			if(((mKeepCenteredSE.mPos.x - (mBaseWidth / 6)) + mBaseHScroll)
					< (mBaseWidth / 2))
				mBaseHScroll += (mScrollSpeed.x * mPreScaler);
			if(((mKeepCenteredSE.mPos.x + (mBaseWidth / 6)) + mBaseHScroll)
					> (mBaseWidth / 2))
				mBaseHScroll -= (mScrollSpeed.x * mPreScaler);

			// TODO - need to limit how much we allow mBaseHScroll to go - when
			// we get to the edge of a background graphic we want to pin the
			// graphic to the edge of the screen and move the keep-centered SE
			// to the edge. Problem is that the size of the background graphic
			// we want to contain may not be the same as mBaseWidth and height
		}

		// draw world
		mCurrentCanvas = tCanvas;
		tCanvas.drawRGB(0, 0, 0); // clear with black
		ScreenElement.sAllSEs.SortIfDirty();
		for(int i = 0; i < ScreenElement.sAllSEs.size; i++)
		{
			ScreenElement tSE;
			try
			{
				tSE = ScreenElement.sAllSEs.At(i);
				if(tSE.Visible())
					tSE.Draw();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		// draw stats
		if(sDebug)
		{
			tCanvas.drawText("Time: " + GameProc.sOnly.Seconds(), 10, 10, mDebugPaint);
			tCanvas.drawText("FPS:  " + GameProc.sOnly.FPS(), 10, 20, mDebugPaint);
		}
		mCurrentCanvas = null;
		mHolder.unlockCanvasAndPost(tCanvas);
	}

	// screen update callbacks /////////////////////////////////////////////////

	@Override
	public void surfaceChanged(SurfaceHolder sh, int fmt, int w, int h)
	{
		Log.d(GameProc.TAG, "surfaceChanged()");
		synchronized(this)
		{
			mSized = true;
			mScreenWidth = w;
			mScreenHeight = h;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.d(GameProc.TAG, "surfaceCreated()");
		synchronized(this)
		{
			mHasSurface = true;
			notify();
		}
		Log.d(GameProc.TAG, "  completed");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.d(GameProc.TAG, "surfaceDestroyed()");
		synchronized(this)
		{
			mHasSurface = false;
		}
		Log.d(GameProc.TAG, "  completed");
	}
}
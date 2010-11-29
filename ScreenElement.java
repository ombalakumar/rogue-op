// ScreenElement.java
// Updatable active sprite class.
//
// Copyright ©2010 Christopher Tooley
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode;


import java.util.Comparator;

import rogue_opcode.containers.LazySortedArray;
import rogue_opcode.geometrics.XYZf;
import rogue_opcode.geometrics.XYf;

import android.graphics.Canvas;
import android.util.Log;


/** Generic updatable graphical object class.
 * <br /><br />
 * Override {@code Update()} to perform custom movement and animation,
 * or handle user input.
 * @see GraphicResource
 * @see ActionElement
 */
public class ScreenElement extends ActionElement
{
	private static final long serialVersionUID = 8512900123394987036L;

	protected GraphicResource mGR;

	public XYZf mPos;
	public XYZf mVel;

	protected boolean mVisible;
	public boolean mSelfGuided;

	protected String mText;

	protected boolean mDrawCentered;
	protected boolean mDrawAbsolute;

	// sorted on Z depth
	// XXX: may be inefficient; keep an eye on performance
	public static LazySortedArray<ScreenElement> sAllSEs;
	public static void Init()
	{
		try
		{
			if(sAllSEs == null)
				sAllSEs = new LazySortedArray<ScreenElement>(
					new Comparator<ScreenElement>()
					{
						@Override
						public int compare(ScreenElement a, ScreenElement b)
						{
							int tCmp = (int)Math.signum(b.mPos.z - a.mPos.z);
							//	if(tCmp == 0 && a != b)
							//		tCmp = 1; // bump
							return tCmp;
						}
					});
		}
		catch(Exception e)
		{
			Log.d(GameProc.TAG, e.toString());
		}
	}

	// c'tors //////////////////////////////////////////////////////////////////

	public ScreenElement(int pResourceID)
	{
		init(pResourceID, null, 0, 0);
	}

	public ScreenElement(String pText)
	{
		init(0, pText, 0, 0);
	}

	public ScreenElement(int pResourceID, int pX, int pY)
	{
		init(pResourceID, null, pX, pY);
	}

	public ScreenElement(String pText, int pX, int pY)
	{
		init(0, pText, pX, pY);
	}

	public ScreenElement(int pResourceID, String pText, int pX, int pY)
	{
		init(pResourceID, pText, pX, pY);
		mDrawCentered = false;
		mDrawAbsolute = false;
	}

	protected void init(int pResourceID, String pText, int pX, int pY)
	{
		if(pResourceID != 0)
			mGR = GraphicResource.FindGR(pResourceID);
		else
			mGR = null;

		mPos = new XYZf(pX, pY, 0);
		mVel = new XYZf();

		mText = pText;

		mDrawCentered = true;
		mVisible = true;
		mSelfGuided = false;

		try
		{
			sAllSEs.Append(this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	// public interfaces ///////////////////////////////////////////////////////

	public void Hibernate()
	{
		mVisible = false; // Turn off drawing
		mActive = false; // stop calling update function
	}

	public void Wake()
	{
		mVisible = true;
		mActive = true;
	}

	public boolean Visible()
	{
		return mVisible;
	}

	public void SetCurrentGR(int pResourceID)
	{
		mGR = GraphicResource.FindGR(pResourceID);
	}

	public GraphicResource getCurrentGR()
	{
		return mGR;
	}

	public Boolean WithinRange(ScreenElement pTargetSE, int pRadius)
	{
		if( (mPos.x < (pTargetSE.mPos.x + pRadius)) &&
				(mPos.x > (pTargetSE.mPos.x - pRadius)) &&
				(mPos.y < (pTargetSE.mPos.y + pRadius)) &&
				(mPos.y > (pTargetSE.mPos.y - pRadius)) )
			return true;
		return false;

	}

	public void DrawCentered(boolean pDrawCentered)
	{
		mDrawCentered = pDrawCentered;
	}

	public void DrawAbsolute(boolean pDrawAbsolute)
	{
		mDrawAbsolute = pDrawAbsolute;
	}

	public int Width()
	{
		return mGR.VirtualWidth();
	}

	public int Height()
	{
		return mGR.VirtualHeight();
	}

	public String Text()
	{
		return mText;
	}

	public void Text(String pText)
	{
		mText = pText;
	}

	public XYf Pos()
	{
		return mPos;
	}

	public void Pos(float pX, float pY)
	{
		mPos.x = pX;
		mPos.y = pY;
	}

	public float ZDepth()
	{
		return mPos.z;
	}

	public void ZDepth(float pZ)
	{
		mPos.z = pZ;
		sAllSEs.mDirty = true;
	}

	/**
	 * AnimatedView will call this repeatedly during the program's lifetime
	 * automatically. Override in your derived class to do something exciting.
	 *
	 * @see rogue_opcode.ActionElement#Update()
	 */
	@Override
	public void Update()
	{
		if(mSelfGuided)
			mPos.add(mVel);
	}

	/**
	 * AnimatedView will call this interface at draw time. Override in your
	 * derived class to do something more than draw the currentGR at it's
	 * current {@code mPos} coordinates.
	 */
	public void Draw()
	{
		Canvas tCanvas = AnimatedView.sCurrentCanvas;
		if(mGR != null)
			tCanvas.drawBitmap(mGR.mImage, mPos.x, mPos.y, null);
		if(mText != null && mText.length() > 0)
			tCanvas.drawText(mText, mPos.x, mPos.y,
					AnimatedView.sOnly.mPaint);
	}
}


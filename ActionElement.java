// ActionElement.java
// Updatable interface for active entities.
//
// Copyright ©2010 Christopher Tooley
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode;

import java.io.Serializable;

import android.util.Log;


/**
 * {@code ActionElement} is an abstract class that is intended to be extended
 * by the implementing program. Its sole function is to give objects a shot at
 * the processor at regular intervals.
 * <br /><br />
 * All extant ActionElements may be accessed through the static member {@code
 * sAllAEs}.
 * <br /><br />
 * Example usage: extend this class to create a coconut factory which has no
 * graphical component, but still needs to determine on every game tick if it
 * should spawn a new coconut.
 */
public abstract class ActionElement implements Serializable
{
	private static final long serialVersionUID = 3959649135411049295L;

	public static Array<ActionElement> sAllAEs;
	public static void Init()
	{
		try
		{
			if(sAllAEs == null)
				sAllAEs = new Array<ActionElement>(128);
		}
		catch(Exception e)
		{
			Log.d(GameProc.TAG, e.toString());
		}
	}

	// only active items will have their update routine called
	protected boolean mActive = true;

	public ActionElement()
	{
		try
		{
			sAllAEs.Append(this);
		}
		catch(Exception e)
		{
		}
	}

	// override in your derived class to do something exciting
	public void Update()
	{
	}

	// override in your derived class to do something exciting
	public void Reset()
	{
	}

	public void Active(boolean mActive)
	{
		this.mActive = mActive;
	}

	public boolean Active()
	{
		return mActive;
	}

}

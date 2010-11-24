// AudioResource.java
// Common audio interface
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

import java.io.Serializable;
import java.util.HashMap;


/**
 * Defines a common interface for audio objects.
 *
 * @author Brigham Toskin
 */
public abstract class AudioResource implements Serializable
{
	private static final long serialVersionUID = -4656188223786705530L;

	/**
	 * Maps {@code resource} IDs to {@code AudioResource}s. <b>Note: This may
	 * be inefficient with memory for some operations; use carefully!</b>
	 */
	public static HashMap<Integer, AudioResource> sAllARs;
	static
	{
		sAllARs = new HashMap<Integer, AudioResource>();
	}

	protected int mResID;
	protected float mGain;
	protected float mPan;

	// c'tor //
	protected AudioResource(int pResID)
	{
		sAllARs.put(pResID, this);
		mResID = pResID;
		mGain = 0.5f;
		mPan = 0.0f;
	}

	abstract protected void die();

	public static void Die()
	{
		// we are shutting down, so we don't care about performance too much
		for(AudioResource tAR : sAllARs.values())
		{
			tAR.die();
		}
	}

	// audio interfaces ////////////////////////////////////////////////////////

	public abstract void Play();
	public abstract void Stop();
	public abstract void Pause();
	public abstract void Resume();

	public float Gain()
	{
		return mGain;
	}
	public void Gain(float pGain)
	{
		if(pGain > 1.0f)
			pGain = 1.0f;
		if(pGain < 0)
			pGain = 0;
		mGain = pGain;
	}
	public void Mute()
	{
		Gain(0);
	}

	public abstract boolean Loop();
	public abstract void Loop(boolean pLoop);

	public float Pan()
	{
		return mPan;
	}
	public void Pan(float pPan)
	{
		if(pPan > 1.0f)
			pPan = 1.0f;
		if(pPan < -1.0f)
			pPan = -1.0f;
		mPan = pPan;

	}

	// static access ///////////////////////////////////////////////////////////

	/**
	 * Finds the {@code AudioResource} associated with the specified {@code
	 * resource} ID, if it exists. You may optionally override this method in
	 * class implementations to create an appropriate instance if the resource
	 * hasn't been previously loaded.
	 * 
	 * @param pResID the {@code resource} ID to retrieve.
	 * @return the found {@code AudioResource} instance, or {@code null} if it
	 *         hasn't been loaded.
	 */
	public static AudioResource FindByResID(int pResID)
	{
		return sAllARs.get(pResID);
	}
}

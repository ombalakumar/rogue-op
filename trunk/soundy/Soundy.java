// Soundy.java
// Soundy synthesizer framework
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


package rogue_opcode.soundy;

import rogue_opcode.AudioResource;
import android.media.AudioTrack;


/**
 * Soundy has up to 4 WaveSources, which will be instances of WaveTables or
 * WaveOscilators.
 *
 * @author Brigham Toskin
 */
public class Soundy extends AudioResource
{
	// These are the audio player guts.
	// AudioTracks should be generated on the fly and played in dynamic mode.
	protected AudioTrack mChannel1;
	protected AudioTrack mChannel2;
	protected AudioTrack mChannel3;
	protected AudioTrack mChannel4;
	protected WaveSource mSynth1, mLFO1;
	protected WaveSource mSynth2, mLFO2;
	protected WaveSource mSynth3, mLFO3;
	protected WaveSource mSynth4, mLFO4;

	/**
	 * @param pResID
	 */
	public Soundy(int pResID)
	{
		super(pResID);
		// TODO: allocate the above audio classes
	}

	/**
	 * @return
	 * @see rogue_opcode.AudioResource#Loop()
	 */
	@Override
	public boolean Loop()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param pLoop
	 * @see rogue_opcode.AudioResource#Loop(boolean)
	 */
	@Override
	public void Loop(boolean pLoop)
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * @see rogue_opcode.AudioResource#Pause()
	 */
	@Override
	public void Pause()
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * @see rogue_opcode.AudioResource#Play()
	 */
	@Override
	public void Play()
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * @see rogue_opcode.AudioResource#Resume()
	 */
	@Override
	public void Resume()
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * @see rogue_opcode.AudioResource#Stop()
	 */
	@Override
	public void Stop()
	{
		// TODO Auto-generated method stub

	}

	/**
	 *
	 * @see rogue_opcode.AudioResource#die()
	 */
	@Override
	protected void die()
	{
		// TODO Auto-generated method stub

	}

}
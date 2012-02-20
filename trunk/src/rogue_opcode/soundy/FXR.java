// FXR.java
// SFXR file loader and playback class
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


/**
 * This class is for loading and playing back synthesis parameter files created
 * by the open source sound effect generator, SFXR. It supports file formats up
 * through version 102, in addition to some more advanced/standard envelope
 * functionality.
 * <br /><br />
 * FXR can load and pre-render the synthesized audio once for fast, efficient
 * playback at runtime. Or, it can be configured to apply a small amount of
 * randomness to the synthesis parameters and regenerate the sound every time it
 * is played; this will lead to a more diverse soundscape, delivering a more
 * interesting and realistic experience to players at the expense of increased
 * processing overhead.
 *
 * @author Brigham Toskin
 */
public class FXR extends AudioResource
{
	protected WaveSource mSynth;
	protected boolean mRandomize;

	// c'tors //

	/**
	 * @param pResID
	 */
	public FXR(int pResID)
	{
		this(pResID, false);
	}

	public FXR(int pResID, boolean pRandomize)
	{
		super(pResID);
		mRandomize = pRandomize;
		int tLength = 0; // TODO: load file and calculate length
		// TODO: deduce the kind of WaveSource from file
		/*mOutStream = new AudioTrack(AudioManager.STREAM_MUSIC,
			WaveSource.sSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
			WaveSource.sFormat, tLength,
			(pRandomize ? AudioTrack.MODE_STREAM : AudioTrack.MODE_STATIC));*/
	}

	// playback interfaces /////////////////////////////////////////////////////

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

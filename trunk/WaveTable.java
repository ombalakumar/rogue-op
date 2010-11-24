// WaveTable.java
// WaveTable class
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

/**
 * Abstract base class for generating and buffering 1 cycle of a waveform at 1Hz
 * in normalized floating point format (amplitude over the range [-1.0,1.0]
 * inclusive). Extending classes will implement the {@code Generate()} method to
 * fill the internal buffer with floating point PCM data for the waveform.
 * <br /><br />
 * This class is part of the Soundy library.
 *
 * @author Brigham Toskin
 */
public abstract class WaveTable
{
	public float[] mWaveData;
	public int mSampleRate;
	public float mPeriod;

	/**
	 * Constructs an instance with an internal wavetable buffer of an
	 * appropriate size to hold one cycle at 1 Hz at the specified sample rate.
	 *
	 * @param pSampleRate the sample rate of the wavetable; 11025, 22050, and
	 *        44100 are probably appropriate values in most environments.
	 */
	public WaveTable(int pSampleRate)
	{
		mWaveData = new float[pSampleRate];
		mSampleRate = pSampleRate;
		mPeriod = 1.0f / pSampleRate;
	}

	/**
	 * Implement this class to generate the appropriate wave function values.
	 * This method should have an internal loop to fill the entire array based
	 * on the current member parameters.
	 */
	public abstract void Generate();

	/**
	 * Returns the correct sample at the requested frequency and phase location.
	 * @param pPhase the current phase of the generated waveform.
	 * @param pFreq tone frequency in Hz.
	 */
	public float Sample(int pPhase, float pFreq)
	{
		return 0;
	}
}

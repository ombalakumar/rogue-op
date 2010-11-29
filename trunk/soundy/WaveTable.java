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


package rogue_opcode.soundy;

import java.util.Arrays;


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
	public float mPeriod, mPhase;
	public ADSHR mEnvelope;

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
		generate_table();
	}

	/**
	 * Implement this class to generate the appropriate wave function values.
	 * This method should have an internal loop to fill the entire
	 * <code>mWaveData</code>array based on the current member parameters.
	 */
	protected abstract void generate_table();

	// synthesis interfaces ////////////////////////////////////////////////////

	/**
	 * Generates an output waveform with the requested properties. This is a
	 * very complicated method to use; normally you won't want to call this
	 * directly, instead using one of the interfaces with a simpler call
	 * signature which in turn calls <code>SynthSamplesMaster()</code>. However,
	 * it is being made public for advanced users and for interfacing external
	 * classes and adaptors that in turn make synthesis programming simpler.
	 *
	 * @param oStream reference to output audio stream buffer. This buffer can
	 *        be any size, and does not need to comply to any limitations
	 *        imposed by the underlying audio output system. It is assumed that
	 *        this buffer will be mixed down later before being pushed to sound
	 *        hardware, so buffers may be as large or small as is convenient.
	 * @param pFreq the output tone frequency, in Hz.
	 * @param phase_offset starting phase offset, in samples.
	 * @param duty waveform dutycycle in the range (0 , 1.0).
	 * @param vol output volume in the range [0 , 1.0].
	 * @param waveMod waveform modulation mode. This parameter affects how the
	 *        generated waveform is affected by the signal <code>lfo</code>.
	 * @param lfo low-frequency oscillator signal, used to modulate the
	 *        generated waveform.
	 * @param modarg ???
	 * @param note_on flag to turn the output on or off.
	 */
	public void SynthSamplesMaster(float[] oStream, float pFreq,
		float phase_offset, float duty, float vol, WaveModulation waveMod,
		float lfo[], float modarg, boolean note_on)
	{
		// precalc envelope to see if we need to turn pFreq back on
		float amp = 1.0f;
		if(mEnvelope != null)
		{
			pFreq = mEnvelope.Calculate(pFreq, note_on);
			amp = mEnvelope.mAmp;
		}

		// ensure a reasonable "no signal" value and bail
		if(pFreq == 0.0 || (mEnvelope == null && !note_on))
		{
			Arrays.fill(oStream, 0.0f);
			return;
		}

		// precalc period and mPhase data
		float period = period(mSampleRate, pFreq);
		float portion, lookup_phase;

		// generate each sample
		for(int i = 0; i < oStream.length; i++)
		{
			// predomulate dutycycle
			if(waveMod == WaveModulation.DM)
				duty = MIX_WAVEFORM_MEAN(duty, MOD_WAVEFORM_AMP(1.0f, lfo[i]));
			WRAP_AROUND(duty);

			// premodulate phaseshift
			WRAP_AROUND(mPhase);
			if(waveMod == WaveModulation.PM)
				lookup_phase = mPhase + (mSampleRate * lfo[i]);
			else
				lookup_phase = mPhase + (mSampleRate * phase_offset);
			WRAP_AROUND(lookup_phase);

			// grab sample from wavetable
			oStream[i] = mWaveData[Math.round(lookup_phase)] * vol * amp;

			// handle envelope calculations
			if(mEnvelope != null)
			{
				mEnvelope.Calculate(pFreq, note_on);
				amp = mEnvelope.mAmp;
			}


			// step to next sample, based on dutycycle
			if(mPhase < mSampleRate / 2.0) // in first halfperiod
			{
				if(duty == 0.0) //we shouldn't be here; wrap
				{
					//mPhase = mSampleRate / 2.0; // jump forward
					WRAP_SECOND(mPhase);
					portion = 1.0f - duty;
				}
				else
					portion = duty;
			}
			else // in second halfperiod
			{
				if(duty == 1.0) // we shouldn't be here; wrap
				{
					//mPhase = 0;	// jump to start
					WRAP_FIRST(mPhase);
					portion = duty;
				}
				else
					portion = 1.0f - duty;
			}
			mPhase += mSampleRate / (2 * portion * period); // == pFreq when duty == .5

			// handle modulation
			switch(waveMod)
			{
			case AM:
				oStream[i] = MOD_WAVEFORM_AMP(oStream[i], lfo[i] + (2 * modarg));
				break;
			case FM:
				mPhase += lfo[i] * modarg;
				break;
			default: // PM, DM handled above
				break;
			}
		}
	}

	/**
	 * Utility method to calculate the wavelength period in samples, based on
	 * output sample rate and requested tone frequency.
	 *
	 * @param pSampleRate current signal output sample rate.
	 * @param pFreq frequency of tone context.
	 * @return
	 */
	protected static float period(int pSampleRate, float pFreq)
	{
		return pSampleRate / pFreq;
	}

	// XXX: these might be better off in a static mixer class //////////////////

	protected static float MOD_WAVEFORM_AMP(float w, float lfo)
	{
		return w * ((lfo + 1.0f) / 2.0f);
	}

	protected static float MIX_WAVEFORM_MEAN(float w1, float w2)
	{
		return (w1 + w2) / 2.0f;
	}

	protected float WRAP_AROUND(float phase)
	{
		float tChunk = mSampleRate;
		while(phase >= tChunk)
			phase -= tChunk;
		while(phase < 0.0001f)
			phase += tChunk;
		return phase;
	}

	protected float WRAP_FIRST(float phase)
	{
		float tChunk = mSampleRate / 2.0f;
		while(phase >= tChunk)
			phase -= tChunk;
		while(phase < 0.0001f)
			phase += tChunk;
		return phase;
	}

	protected float WRAP_SECOND(float phase)
	{
		float tChunk = mSampleRate / 2.0f;
		while(phase >= mSampleRate)
			phase -= tChunk;
		while(phase < tChunk)
			phase += tChunk;
		return phase;
	}

	// inner types and classes /////////////////////////////////////////////////

	public enum WaveModulation
	{
		NONE, AM, FM, PM, DM
	}
}

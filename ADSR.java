// ADSR.java
// ADSR amplitude envelope class
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
 * Represents an ADSR envelope. These stand for Attack, Decay, Sustain and
 * Release, respectively. By setting these parameters, you can control the
 * behavior of how note events are triggered.
 * <br /></br />
 * In more detail, Attack specifies how long it takes for a newly generated
 * tone to go from 0 to full amplitude. Decay, as you might suspect,
 * specified how long it takes the tone to fall off from full amplitude to
 * the sustain level. Sustain, then, specifies what this amplitude is for
 * the generated tone as the note is held. Finally, Release once again
 * specifies a time period, indicating how long it takes for the tone to
 * decay form the sustain amplitude back down to zero when a frequency
 * change or note-off event is triggered.
 */
public class ADSR
{
	public int A, D, R;
	public double S;
	public float mAmp;
	public float mFreq;
	public EnvelopeState mState;

	ADSR()
	{
		A = D = R = 0;
		S = 0.0;
		mAmp = mFreq = 0;
		mState = EnvelopeState.OFF;
	}

	ADSR(int pA, int pD, float pS, int pR)
	{
		A = pA;
		D = pD;
		S = pS;
		R = pR;
		mAmp = mFreq = 0;
		mState = EnvelopeState.OFF;
	}

	public float Calculate(float pFreq, boolean pOn)
	{
		EnvelopeState tState = mState;

		if(pOn) // oscillator on
		{
			// reset envelope on pFreq change
			if(pFreq != mFreq || tState == EnvelopeState.OFF
					|| tState == EnvelopeState.RELEASE)
			{
				mState = EnvelopeState.ATTACK;
			}

			if(tState == EnvelopeState.ATTACK)
			{
				if(mAmp >= 1.0)
					mState = EnvelopeState.DECAY;
				else
					mAmp += 1.0 / A;
			}

			if(tState == EnvelopeState.DECAY)
			{
				if(mAmp <= S)
					mState = EnvelopeState.SUSTAIN;
				else
					mAmp -= (1.0 - S) / D;
			}
		}
		else
		{
			if(tState != EnvelopeState.OFF) // note ended
			{
				mState = EnvelopeState.RELEASE;
			}

			if(tState == EnvelopeState.RELEASE)
			{
				if(mAmp <= 0.0001f)
				{
					mAmp = 0.0f;
					mState = EnvelopeState.OFF;
					pFreq = 0; // signal the gen functions to shortcircuit 0's
				}
				else
					mAmp -= S / R;
			}
		}
		return (mFreq = pFreq);
	}

	// inner types and classes /////////////////////////////////////////////////

	public enum EnvelopeState
	{
		OFF, ATTACK, DECAY, SUSTAIN, RELEASE
	}
}

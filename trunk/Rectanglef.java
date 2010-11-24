// Rectanglef.java
// Floating point rectangle class
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

import android.graphics.RectF;


/**
 *
 * @author Brigham Toskin
 */
public class Rectanglef implements Serializable
{
	private static final long serialVersionUID = 1988188856826226217L;

	public Rectanglef()
	{
		x = y = w = h = 0;
	}

	public Rectanglef(float pX, float pY, float pW, float pH)
	{
		x = pX;
		y = pY;
		w = pW;
		h = pH;
	}

	public RectF toRectF()
	{
		return new RectF(x, y, x + w, y + h);
	}

	public float x, y, w, h;
}

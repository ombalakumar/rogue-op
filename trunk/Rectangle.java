// Rectangle.java
// Rectangle class
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

import android.graphics.Rect;


/**
 * 
 * @author Brigham Toskin
 */
public class Rectangle implements Serializable
{
	private static final long serialVersionUID = -7071451744257630068L;

	public Rectangle()
	{
		x = y = w = h = 0;
	}

	public Rectangle(int pX, int pY, int pW, int pH)
	{
		x = pX;
		y = pY;
		w = pW;
		h = pH;
	}

	public Rect toRect()
	{
		return new Rect(x, y, x + w, y + h);
	}

	public int x, y, w, h;
}

// XYf.java
// floating point 2D point/vector class
//
// Copyright ©2010 Brigham Toskin, Leea Harlan
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode.geometrics;

import java.io.Serializable;

import android.graphics.Point;


public class XYf implements Serializable
{
	private static final long serialVersionUID = -3490122379205325810L;

	// c'tors //

	public XYf()
	{
		this(0.0f, 0.0f);
	}

	public XYf(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public XYf(XYf xy)
	{
		this(xy.x, xy.y);
	}

	// blah
	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}

	public Point toPoint()
	{
		return new Point(Math.round(x), Math.round(y));
	}

	public void set(XYf xy)
	{
		x = xy.x;
		y = xy.y;
	}

	// arithmetic operators ////////////////////////////////////////////////////

	public XYf plus(XYf xy)
	{
		return new XYf(xy.x + x, xy.y + y);
	}

	public XYf minus(XYf xy)
	{
		return new XYf(x - xy.x, y - xy.y);
	}

	public XYf add(XYf xy)
	{
		x += xy.x;
		y += xy.y;
		return this;
	}

	public XYf sub(XYf xy)
	{
		x -= xy.x;
		y -= xy.y;
		return this;
	}

	public XYf times(float scalar)
	{
		return new XYf(x*scalar, y*scalar);
	}

	public XYf dividedBy(float scalar)
	{
		return new XYf(x/scalar, y/scalar);
	}

	public XYf mul(float scalar)
	{
		x *= scalar;
		y *= scalar;
		return this;
	}

	public XYf div(float scalar)
	{
		x /= scalar;
		y /= scalar;
		return this;
	}

	// linear algebra operators ////////////////////////////////////////////////

	public float Dot(XYf b)
	{
		return x * b.x + y * b.y;
	};

	public float Magnitude()
	{
		return (float) Math.sqrt(x * x + y * y);
	}

	public XYf Normalize()
	{
		float tMag = Magnitude();
		x /= tMag;
		y /= tMag;
		return this;
	}

	// data ////////////////////////////////////////////////////////////////////

	// cartesian (x, y) or vector components <i, j>
	public float x;
	public float y;
}

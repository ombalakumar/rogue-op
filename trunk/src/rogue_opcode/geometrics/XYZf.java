// XYZf.java
// Three dimensional point/vector class
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


package rogue_opcode.geometrics;



/**
 *
 * @author Brigham Toskin
 */
public class XYZf extends XYf
{
	private static final long serialVersionUID = 4186038949433460479L;

	// c'tors //

	public XYZf()
	{
		this(0.0f, 0.0f, 0.0f);
	}

	public XYZf(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public XYZf(XYZf xyz)
	{
		this(xyz.x, xyz.y, xyz.z);
	}

	// blah
	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public void set(XYZf xyz)
	{
		x = xyz.x;
		y = xyz.y;
		z = xyz.z;
	}

	// arithmetic operators ////////////////////////////////////////////////////

	public XYZf plus(XYZf xyz)
	{
		return new XYZf(xyz.x + x, xyz.y + y, xyz.z + z);
	}

	public XYZf minus(XYZf xyz)
	{
		return new XYZf(x - xyz.x, y - xyz.y, z - xyz.z);
	}

	public XYZf add(XYZf xyz)
	{
		x += xyz.x;
		y += xyz.y;
		z += xyz.z;
		return this;
	}

	public XYZf sub(XYZf xyz)
	{
		x -= xyz.x;
		y -= xyz.y;
		z -= xyz.z;
		return this;
	}

	@Override
	public XYZf times(float scalar)
	{
		return new XYZf(x * scalar, y * scalar, z * scalar);
	}

	@Override
	public XYZf dividedBy(float scalar)
	{
		return new XYZf(x / scalar, y / scalar, z / scalar);
	}

	@Override
	public XYZf mul(float scalar)
	{
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	@Override
	public XYZf div(float scalar)
	{
		x /= scalar;
		y /= scalar;
		z /= scalar;
		return this;
	}

	// linear algebra operators ////////////////////////////////////////////////

	// TODO: cross-product

	public float Dot(XYZf b)
	{
		return x * b.x + y * b.y + z * b.z;
	}

	@Override
	public float Magnitude()
	{
		return (float)Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public XYZf Normalize()
	{
		float tMag = Magnitude();
		x /= tMag;
		y /= tMag;
		z /= tMag;
		return this;
	}

	// data ////////////////////////////////////////////////////////////////////

	// cartesian z or vector component k
	public float z;
}

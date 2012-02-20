// GraphicElement.java
// Represents a loaded graphics resource (image)
//
// Copyright ©2010 Christopher Tooley, Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
// 80 cols ; tabwidth 4
// //////////////////////////////////////////////////////////////////////////////


package rogue_opcode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class GraphicResource implements Serializable
{
	private static final long serialVersionUID = 7533930775077206592L;

	protected static Map<Integer, GraphicResource> sAllGRs = new HashMap<Integer, GraphicResource>();

	protected static int sUID = 1000;

	public Bitmap mImage;
	public int mResID;

	// these are the original/logical size of the image
	public int mBaseWidth;
	public int mBaseHeight;

	protected static BitmapFactory.Options sBitmapOptions;
	static
	{
		sBitmapOptions = new BitmapFactory.Options();
		sBitmapOptions.inPreferredConfig = Bitmap.Config.ALPHA_8;
	}

	// c'tor ///////////////////////////////////////////////////////////////////


	/**
	 * Construct a {@code GraphicResource} and load the specified image
	 * resource.
	 *
	 * @param pResID the image's resource ID.
	 */
	public GraphicResource(int pResID)
	{
		mResID = pResID;
		load(pResID);
	}

	/**
	 * Construct a {@code GraphicResource} from an existing bitmap.
	 *
	 * @param pBitmap the Bitmap to clone.
	 */
	public GraphicResource(Bitmap pBitmap)
	{
		mResID = sUID;
		mBaseWidth = pBitmap.getWidth();
		mBaseHeight = pBitmap.getHeight();
		//TODO - I'm not sure about the filter here - in the case of Congo Bongo
		//we definitely don't want it, but in sudoku we do.  I'd make it a parameter
		//but I'd like to keep parameters like this to a minimum.
		mImage = Bitmap.createScaledBitmap(mImage,
				(int)(mBaseWidth * AnimatedView.sOnly.mPreScaler),
				(int)(mBaseHeight * AnimatedView.sOnly.mPreScaler), true);
		sAllGRs.put(mResID, this);
		sUID++;
	}

	protected void load(int pResourceID)
	{
		// load image
		if(pResourceID == 0)
		{
			mImage = null;
			return;
		}
		Resources r = GameProc.sOnly.getResources();
		mImage = BitmapFactory.decodeResource(r, pResourceID, sBitmapOptions);
		mBaseWidth = mImage.getWidth();
		mBaseHeight = mImage.getHeight();

		// scale image
		if(AnimatedView.sOnly.mPreScaler != 1.0f)
		{
			mImage = Bitmap.createScaledBitmap(mImage,
					(int)(mBaseWidth * AnimatedView.sOnly.mPreScaler),
					(int)(mBaseHeight * AnimatedView.sOnly.mPreScaler), true);
		}

		// store image
		sAllGRs.put(pResourceID, this);
	}

	// resource management /////////////////////////////////////////////////////

	public boolean Valid()
	{
		return mImage != null;
	}

	// image properties ////////////////////////////////////////////////////////

	// Returns the width and height in VIRTUAL units (the units used when
	// placing objects on the screen)
	public int VirtualWidth()
	{
		return mBaseWidth;
	}

	public int VirtualHeight()
	{
		return mBaseHeight;
	}

	public int PhysicalWidth()
	{
		return mImage.getWidth();
	}

	public int PhysicalHeight()
	{
		return mImage.getHeight();
	}

	// static access ///////////////////////////////////////////////////////////

	public static GraphicResource FindGR(int pResourceID)
	{
		GraphicResource tGR = sAllGRs.get(pResourceID);
		return (tGR != null ? tGR : new GraphicResource(pResourceID));
	}

	// serialization protocol //////////////////////////////////////////////////

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeInt(mResID);
	}

	private void readObject(ObjectInputStream pIn) throws IOException,
			ClassNotFoundException
	{
		pIn.defaultReadObject(); // read everything except the Bitmap field
		load(mResID); // loads the image resource
	}
}

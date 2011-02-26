// LazyPQueue.java
// LazyPQueue class
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


package rogue_opcode.containers;

import java.util.Arrays;
import java.util.Comparator;


/**
 * {@code LazySortedArray} is an efficient, array-based, sorted structure. It is
 * designed for situations which require a lot of runtime manipulation of sorted
 * contents. It avoids doing fix-ups in tight loops, instead simply marking the
 * structure as "dirty". Because the underlying structure is an array, it avoids
 * activating garbage collection when manipulating the contents as a node-based
 * structure might.
 * <br /><br />
 * The target use case involves one or more loops that
 * will modify the contents and/or structure of the underlying array, with a
 * single point where the structure truly needs to be sorted. Upon reaching this
 * point, the program can execute a single sort action to resynchronize the
 * structure.
 *
 * @param E the storage type parameter.
 * @author Brigham Toskin
 */
public class LazySortedArray<E> extends Array<E>
{
	private static final long serialVersionUID = 4854620935053392136L;

	public boolean mDirty = false;
	protected Comparator<E> mComparator;

	// c'tors //

	/**
	 * @param pCmp
	 * @throws Exception
	 */
	public LazySortedArray(Comparator<E> pCmp) throws Exception
	{
		super();
		mComparator = pCmp;
	}

	/**
	 * @param pCmp
	 * @param pCapacity
	 * @throws Exception
	 */
	public LazySortedArray(Comparator<E> pCmp, int pCapacity) throws Exception
	{
		super(pCapacity);
		mComparator = pCmp;
	}

	// container interfaces ////////////////////////////////////////////////////

	/**
	 * @param pVal
	 * @throws Exception
	 * @see rogue_opcode.containers.Array#Append(java.lang.Object)
	 */
	@Override
	public void Append(E pVal) //throws Exception
	{
		synchronized(this)
		{
			super.Append(pVal);
			mDirty = true;
		}
	}

	/**
	 *
	 * @see rogue_opcode.containers.Container#Clear()
	 */
	@Override
	public void Clear()
	{
		synchronized(this)
		{
			super.Clear();
			mDirty = false;
		}
	}

	/**
	 * @return
	 * @throws Exception
	 * @see rogue_opcode.containers.Array#First()
	 */
	@Override
	public E First() //throws Exception
	{
		SortIfDirty();
		return super.First();
	}

	/**
	 * @return
	 * @throws Exception
	 * @see rogue_opcode.containers.Array#Last()
	 */
	@Override
	public E Last() //throws Exception
	{
		SortIfDirty();
		return super.Last();
	}

	/**
	 * Retrieve element at offset <code>pIndex</code>. Asserts dirty flag.
	 *
	 * @param pIndex offset from beginning of array to retrieve element.
	 * @return the element at the requested index.
	 * @throws Exception if <code>pIndex</code> is out of bounds.
	 * @see rogue_opcode.containers.Array#At(int)
	 */
	@Override
	public E At(int pIndex) //throws Exception
	{
		mDirty = true;
		return super.At(pIndex);
	}

	// sorting interfaces //////////////////////////////////////////////////////

	/**
	 * Sorts the array.
	 */
	public void Sort()
	{
		synchronized(this)
		{
			if(size < 1)
				return;
			// place min at head
			/*
			 * int tMinIndex = 0;
			 * for(int i = 1; i < data.length; i++)
			 * {
			 * if(mComparator.compare(data[tMinIndex], data[i]) > 0)
			 * tMinIndex = i;
			 * }
			 * if(tMinIndex > 0)
			 * {
			 * E tVal = data[0];
			 * data[0] = data[tMinIndex];
			 * data[tMinIndex] = tVal;
			 * }
			 *
			 * // sort remaining array
			 * for(int i = 2; i <= data.length; i++)
			 * {
			 * E tVal = data[i];
			 * int j;
			 * for(j = i; mComparator.compare(tVal, data[j - 1]) < 0; j--)
			 * data[j] = data[j - 1];
			 * data[j] = tVal;
			 * }
			 */
			try
			{
				Arrays.sort(data, mComparator);
			}
			catch(Exception e)
			{
			}
			mDirty = false;
		}
	}

	/**
	 * Sorts the array if in a dirty state.
	 */
	public void SortIfDirty()
	{
		if(mDirty)
			Sort();
	}
}

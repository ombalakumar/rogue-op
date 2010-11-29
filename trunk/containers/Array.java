// Array.java
// Dynamical array container class
//
// Copyright ©2009 - 2010 Brigham Toskin
// This software is part of the Rogue-Opcode game framework. It is distributable
// under the terms of a modified MIT License. You should have received a copy of
// the license in the file LICENSE. If not, see:
// <http://code.google.com/p/rogue-op/wiki/LICENSE>
//
// Formatting:
//	80 cols ; tabwidth 4
////////////////////////////////////////////////////////////////////////////////


package rogue_opcode.containers;


/** When directly accessing the underlying {@code data} array, indices {@code
 * [0, size)} are valid.
 * @param E Generic storage type parameter.
 * @see rogue_opcode.containers.Container
 * @author Brigham Toskin
 */
public class Array<E> extends Container<E>
{
	private static final long serialVersionUID = -90938033040808162L;

	// c'tor //

	/** Default constructor preallocates space for up to 32 elements.
	 * @throws Exception on allocation failure.
	 */
	public Array() throws Exception
	{
		super(32);
	}

	/** Preallocates space for up to {@code pCapacity} elements.
	 * @param pCapacity number of elements to allocate space for.
	 * @throws Exception on allocation failure.
	 */
	public Array(int pCapacity) throws Exception
	{
		super(pCapacity);
	}

	// data access /////////////////////////////////////////////////////////////

	/**
	 * @throws Exception if empty.
	 * @see rogue_opcode.containers.Container#First()
	 */
	@Override
	public E First() throws Exception
	{
		validate_nonempty();
		return data[0];
	}

	/**
	 * @throws Exception if empty.
	 * @see rogue_opcode.containers.Container#Last()
	 */
	@Override
	public E Last() throws Exception
	{
		validate_nonempty();
		return data[size - 1];
	}

	/** Retrieve element at index {@code pOffset}.
	 * @param pIndex element index to retrieve.
	 * @throws Exception on invalid index.
	 * @see rogue_opcode.containers.Container#at(int)
	 */
	@Override
	public E At(int pIndex) throws Exception
	{
		validate_index(pIndex);
		return data[pIndex];
	}

	/** Appends a new element to the end of the array, reallocating the buffer
	 * if necessary.
	 * @param pVal the item to append.
	 * @throws Exception on allocation failure.
	 */
	public void Append(E pVal) throws Exception
	{
		resize_inc();
		data[size-1] = pVal;
	}
}

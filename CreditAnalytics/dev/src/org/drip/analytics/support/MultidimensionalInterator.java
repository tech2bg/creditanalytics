
package org.drip.analytics.support;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * MultidimensionalInterator contains the Functionality to iterate through a Multidimensional Array.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class MultidimensionalInterator {
	private int[] _aiMax = null;
	private int[] _aiCursor = null;
	private boolean _bCycle = false;

	/**
	 * MultidimensionalInterator Constructor
	 * 
	 * @param aiMax The Array of Dimension Maximum
	 * @param bCycle TRUE => Cycle through Start
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public MultidimensionalInterator (
		final int[] aiMax,
		final boolean bCycle)
		throws java.lang.Exception
	{
		if (null == (_aiMax = aiMax))
			throw new java.lang.Exception ("MultidimensionalInterator ctr: Invalid Input");

		_bCycle = bCycle;
		int iDimension = _aiMax.length;
		_aiCursor = new int[iDimension];

		if (0 == iDimension) throw new java.lang.Exception ("MultidimensionalInterator ctr: Invalid Input");

		for (int i = 0; i < iDimension; ++i) {
			if (0 >= _aiMax[i])
				throw new java.lang.Exception ("MultidimensionalInterator ctr: Invalid Input");

			_aiCursor[i] = 0;
		}
	}

	/**
	 * Retrieve the Array of Dimension Maximum
	 * 
	 * @return The Array of Dimension Maximum
	 */

	public int[] multiDimArray()
	{
		return _aiMax;
	}

	/**
	 * Retrieve the Cursor
	 * 
	 * @return The Cursor
	 */

	public int[] cursor()
	{
		return _aiCursor;
	}

	/**
	 * Reset and retrieve the Cursor
	 * 
	 * @return The Reset Cursor
	 */

	public int[] reset()
	{
		int iDimension = _aiMax.length;

		for (int i = 0; i < iDimension; ++i)
			_aiCursor[i] = 0;

		return _aiCursor;
	}

	/**
	 * Move to the adjacent Index Cursor
	 * 
	 * @return The adjacent Index Cursor
	 */

	public int[] next()
	{
		int iUpdateIndex = -1;
		int iDimension = _aiMax.length;

		for (int i = iDimension - 1; i >= 0; --i) {
			if (_aiCursor[i] != _aiMax[i] - 1) {
				iUpdateIndex = i;
				break;
			}
		}

		if (-1 == iUpdateIndex) return _bCycle ? reset() : null;

		_aiCursor[iUpdateIndex] = _aiCursor[iUpdateIndex] + 1;

		for (int i = iUpdateIndex + 1; i < iDimension; ++i)
			_aiCursor[i] = 0;

		return _aiCursor;
	}
}


package org.drip.spaces.tensor;

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
 * CombinatorialRealMultidimensionalInterator contains the Functionality to iterate through a Combinatorial
 *  Real Multidimensional Vector Space.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CombinatorialRealMultidimensionalIterator extends
	org.drip.analytics.support.MultidimensionalInterator {
	private org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] _aCRUV = null;

	/**
	 * Retrieve the Multidimensional Iterator associated with the Underlying Vector Space
	 * 
	 * @param aCRUV Array of the Combinatorial Real Unidimensional Vectors
	 * 
	 * @return The Multidimensional Iterator associated with the Underlying Vector Space
	 */

	public static final CombinatorialRealMultidimensionalIterator Standard (
		final org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] aCRUV)
	{
		if (null == aCRUV) return null;

		int iDimension = aCRUV.length;
		int[] aiMax = new int[iDimension];

		if (0 == iDimension) return null;

		for (int i = 0; i < iDimension; ++i)
			aiMax[i] = (int) aCRUV[i].cardinality().number();

		try {
			return new CombinatorialRealMultidimensionalIterator (aCRUV, aiMax);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * CombinatorialRealMultidimensionalIterator Constructor
	 * 
	 * @param aCRUV Array of the Combinatorial Real Unidimensional Vectors
	 * @param aiMax The Array of Dimension Maximum
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CombinatorialRealMultidimensionalIterator (
		final org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] aCRUV,
		final int[] aiMax)
		throws java.lang.Exception
	{
		super (aiMax, false);

		if (null == (_aCRUV = aCRUV) || _aCRUV.length != aiMax.length)
			throw new java.lang.Exception ("CombinatorialRealMultidimensionalIterator ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of the Combinatorial Real Unidimensional Vectors
	 * 
	 * @return The Array of the Combinatorial Real Unidimensional Vectors
	 */

	public org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] cruv()
	{
		return _aCRUV;
	}

	/**
	 * Convert the Vector Space Index Array to the Variate Array
	 * 
	 * @param aiIndex Vector Space Index Array
	 * 
	 * @return Variate Array
	 */

	public double[] vectorSpaceIndexToVariate (
		final int[] aiIndex)
	{
		if (null == aiIndex) return null;

		org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] aCRUV = cruv();

		int iDimension = aCRUV.length;
		double[] adblVariate = new double[iDimension];

		if (iDimension != aiIndex.length) return null;

		for (int i = 0; i < iDimension; ++i)
			adblVariate[i] = aCRUV[i].elementSpace().get (aiIndex[i]);

		return adblVariate;
	}

	/**
	 * Retrieve the Cursor Variate Array
	 * 
	 * @return The Cursor Variate Array
	 */

	public double[] cursorVariates()
	{
		return vectorSpaceIndexToVariate (cursor());
	}

	/**
	 * Retrieve the Subsequent Variate Array
	 * 
	 * @return The Subsequent Variate Array
	 */

	public double[] nextVariates()
	{
		return vectorSpaceIndexToVariate (next());
	}
}

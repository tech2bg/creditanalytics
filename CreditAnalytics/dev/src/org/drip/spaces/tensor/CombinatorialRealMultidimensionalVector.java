
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
 * CombinatorialRealMultidimensionalVector exposes the normed/non-normed Discrete Spaces with
 *  Multidimensional Real-valued Combinatorial Vector Elements.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CombinatorialRealMultidimensionalVector implements
	org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace {
	private org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] _aCRUV = null;

	/**
	 * CombinatorialRealMultidimensionalVector Constructor
	 * 
	 * @param aCRUV Array of the Underlying Unidimensional Combinatorial Real Vector Spaces
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CombinatorialRealMultidimensionalVector (
		final org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] aCRUV)
		throws java.lang.Exception
	{
		if (null == (_aCRUV = aCRUV) || 0 == _aCRUV.length)
			throw new java.lang.Exception ("CombinatorialRealUnidimensionalVector ctr: Invalid Inputs");
	}

	@Override public int dimension()
	{
		return _aCRUV.length;
	}

	@Override public org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] vectorSpaces()
	{
		return _aCRUV;
	}

	@Override public boolean validateInstance (
		final double[] adblInstance)
	{
		if (null == adblInstance) return false;

		int iDimension = _aCRUV.length;

		if (adblInstance.length != iDimension) return false;

		for (int i = 0; i < iDimension; ++i) {
			if (!_aCRUV[i].validateInstance (adblInstance[i])) return false;
		}

		return true;
	}

	@Override public org.drip.spaces.tensor.Cardinality cardinality()
	{
		double dblCardinalNumber = 1.;
		int iDimension = _aCRUV.length;

		for (int i = 0; i < iDimension; ++i)
			dblCardinalNumber *= _aCRUV[i].cardinality().number();

		return org.drip.spaces.tensor.Cardinality.CountablyFinite (dblCardinalNumber);
	}

	@Override public boolean match (
		final org.drip.spaces.tensor.GeneralizedVectorSpace gvsOther)
	{
		if (null == gvsOther || !(gvsOther instanceof CombinatorialRealMultidimensionalVector)) return false;

		CombinatorialRealMultidimensionalVector crmvOther = (CombinatorialRealMultidimensionalVector)
			gvsOther;

		int iDimensionOther = crmvOther.dimension();

		if (iDimensionOther != dimension()) return false;

		org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] aCRUVOther = crmvOther.vectorSpaces();

		for (int i = 0; i < iDimensionOther; ++i) {
			if (!aCRUVOther[i].match (_aCRUV[i])) return false;
		}

		return true;
	}

	/**
	 * Retrieve the Multidimensional Iterator associated with the Underlying Vector Space
	 * 
	 * @return The Multidimensional Iterator associated with the Underlying Vector Space
	 */

	public org.drip.spaces.tensor.CombinatorialRealMultidimensionalIterator iterator()
	{
		return org.drip.spaces.tensor.CombinatorialRealMultidimensionalIterator.Standard (_aCRUV);
	}
}

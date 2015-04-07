
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
 * ContinuousRealMultidimensional implements the normed/non-normed, bounded/unbounded Continuous
 * 	Multi-dimensional Real-valued R^d Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ContinuousRealMultidimensional implements org.drip.spaces.tensor.GeneralizedVectorSpace {
	private org.drip.spaces.tensor.ContinuousRealUnidimensional[] _aCRU = null;

	/**
	 * Construct the Standard R^d ContinuousRealMultidimensional Instance
	 * 
	 * @param iDimension The Space Dimension
	 * 
	 * @return The Standard R^d ContinuousRealMultidimensional Instance
	 */

	public static final ContinuousRealMultidimensional Standard (
		final int iDimension)
	{
		try {
			return 0 >= iDimension ? null : new ContinuousRealMultidimensional (new
				org.drip.spaces.tensor.ContinuousRealUnidimensional[iDimension]);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ContinuousRealMultidimensional Constructor
	 * 
	 * @param aCRU Array of the Continuous Uni-dimensional Real Valued Spaces
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ContinuousRealMultidimensional (
		final org.drip.spaces.tensor.ContinuousRealUnidimensional[] aCRU)
		throws java.lang.Exception
	{
		if (null == (_aCRU = aCRU))
			throw new java.lang.Exception ("ContinuousRealMultidimensional ctr: Invalid Inputs");

		int iDimension = _aCRU.length;

		if (0 == iDimension)
			throw new java.lang.Exception ("ContinuousRealMultidimensional ctr: Invalid Inputs");

		for (int i = 0; i < iDimension; ++i) {
			if (null == _aCRU[i])
				throw new java.lang.Exception ("ContinuousRealMultidimensional ctr: Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Dimension of the Space
	 *  
	 * @return The Dimension of the Space
	 */

	public int dimension()
	{
		return _aCRU.length;
	}

	/**
	 * Validate the specified R^d Input Instance
	 * 
	 * @param adblInstance The R^d Input Instance
	 * 
	 * @return TRUE => Input Instance Valid
	 */

	public boolean validateInstance (
		final double[] adblInstance)
	{
		if (null == adblInstance) return false;

		int iDimension = _aCRU.length;

		if (adblInstance.length != iDimension) return false;

		for (int i = 0; i < iDimension; ++i) {
			if (!_aCRU[i].validateInstance (adblInstance[i])) return false;
		}

		return true;
	}

	/**
	 * Retrieve the Array of the Variate Left Edges
	 * 
	 * @return The Array of the Variate Left Edges
	 */

	public double[] leftEdge()
	{
		int iDimension = _aCRU.length;
		double[] adblLeftEdge = new double[iDimension];

		for (int i = 0; i < iDimension; ++i)
			adblLeftEdge[i] = _aCRU[i].leftEdge();

		return adblLeftEdge;
	}

	/**
	 * Retrieve the Array of the Variate Right Edges
	 * 
	 * @return The Array of the Variate Right Edges
	 */

	public double[] rightEdge()
	{
		int iDimension = _aCRU.length;
		double[] adblRightEdge = new double[iDimension];

		for (int i = 0; i < iDimension; ++i)
			adblRightEdge[i] = _aCRU[i].rightEdge();

		return adblRightEdge;
	}

	@Override public org.drip.spaces.tensor.Cardinality cardinality()
	{
		return org.drip.spaces.tensor.Cardinality.UncountablyInfinite();
	}
}

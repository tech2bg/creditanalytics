
package org.drip.kernel.spaces;

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
 * MultidimensionalRealValuedSpace implements the normed/non-normed, bounded/unbounded Continuous
 * 	Multi-dimensional Real-valued R^d Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultidimensionalRealValuedSpace {
	private org.drip.kernel.spaces.UnidimensionalRealValuedSpace[] _aURVS = null;

	/**
	 * Construct the Standard R^d MultidimensionalRealValuedSpace Instance
	 * 
	 * @param iDimension The Space Dimension
	 * 
	 * @return The Standard R^d MultidimensionalRealValuedSpace Instance
	 */

	public static final MultidimensionalRealValuedSpace Standard (
		final int iDimension)
	{
		try {
			return 0 >= iDimension ? null : new MultidimensionalRealValuedSpace (new
				org.drip.kernel.spaces.UnidimensionalRealValuedSpace[iDimension]);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * MultidimensionalRealValuedSpace Constructor
	 * 
	 * @param aURVS Array of the Real Valued Spaces
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public MultidimensionalRealValuedSpace (
		final org.drip.kernel.spaces.UnidimensionalRealValuedSpace[] aURVS)
		throws java.lang.Exception
	{
		if (null == (_aURVS = aURVS))
			throw new java.lang.Exception ("MultidimensionalRealValuedSpace ctr: Invalid Inputs");

		int iDimension = _aURVS.length;

		if (0 == iDimension)
			throw new java.lang.Exception ("MultidimensionalRealValuedSpace ctr: Invalid Inputs");

		for (int i = 0; i < iDimension; ++i) {
			if (null == _aURVS[i])
				throw new java.lang.Exception ("MultidimensionalRealValuedSpace ctr: Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Dimension of the Space
	 *  
	 * @return The Dimension of the Space
	 */

	public int dimension()
	{
		return _aURVS.length;
	}

	/**
	 * Validate the specified R^d Input
	 * 
	 * @param adblX The R^d Input
	 * 
	 * @return TRUE => Input Space Valid
	 */

	public boolean validate (
		final double[] adblX)
	{
		if (null == adblX) return false;

		int iDimension = _aURVS.length;

		if (adblX.length != iDimension) return false;

		for (int i = 0; i < iDimension; ++i) {
			if (!_aURVS[i].validate (adblX[i])) return false;
		}

		return true;
	}
}

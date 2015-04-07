
package org.drip.spaces.metric;

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
 * ContinuousRealMultidimensionalHilbert implements the normed, bounded/unbounded, Continuous l^2 R^d Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ContinuousRealMultidimensionalHilbert extends
	org.drip.spaces.metric.ContinuousRealMultidimensionalBanach {

	/**
	 * Construct the Standard l^2 R^d Hilbert Space Instance
	 * 
	 * @param iDimension The Space Dimension
	 * 
	 * @return The Standard l^2 R^d Hilbert Space Instance
	 */

	public static final ContinuousRealMultidimensionalHilbert StandardHilbert (
		final int iDimension)
	{
		try {
			return 0 >= iDimension ? null : new ContinuousRealMultidimensionalHilbert (new
				org.drip.spaces.tensor.ContinuousRealUnidimensional[iDimension]);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ContinuousRealMultidimensionalHilbert Space Constructor
	 * 
	 * @param aCRU Array of Continuous Real Valued Vector Spaces
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ContinuousRealMultidimensionalHilbert (
		final org.drip.spaces.tensor.ContinuousRealUnidimensional[] aCRU)
		throws java.lang.Exception
	{
		super (aCRU, 2);
	}

	@Override public double sampleMetricNorm (
		final double[] adblX)
		throws java.lang.Exception
	{
		if (!validateInstance (adblX))
			throw new java.lang.Exception
				("ContinuousRealMultidimensionalHilbert::sampleMetricNorm => Invalid Inputs");

		double dblNorm = 0.;
		int iDimension = adblX.length;

		for (int i = 0; i < iDimension; ++i) {
			double dblAbsoluteX = java.lang.Math.abs (adblX[i]);

			dblNorm += dblAbsoluteX * dblAbsoluteX;
		}

		return dblNorm;
	}
}

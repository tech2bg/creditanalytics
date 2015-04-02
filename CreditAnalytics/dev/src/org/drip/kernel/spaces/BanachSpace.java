
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
 * BanachSpace implements the normed, bounded/unbounded Continuous Multi-dimensional Real-valued R^d Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BanachSpace extends org.drip.kernel.spaces.MultidimensionalRealValuedSpace {
	private int _iPNorm = -1;

	/**
	 * Construct the Standard R^d BanachSpace Instance
	 * 
	 * @param iDimension The Space Dimension
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @return The Standard R^d BanachSpace Instance
	 */

	public static final BanachSpace StandardBanach (
		final int iDimension,
		final int iPNorm)
	{
		try {
			return 0 >= iDimension ? null : new BanachSpace (new
				org.drip.kernel.spaces.UnidimensionalRealValuedSpace[iDimension], iPNorm);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * BanachSpace Constructor
	 * 
	 * @param aURVS Array of the Real Valued Spaces
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BanachSpace (
		final org.drip.kernel.spaces.UnidimensionalRealValuedSpace[] aURVS,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (aURVS);

		if (0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception ("BanachSpace Constructor: Invalid p-norm");
	}

	/**
	 * Retrieve the P-Norm of the Banach Space
	 * 
	 * @return The P-Norm of the Banach Space
	 */

	public int pnorm()
	{
		return _iPNorm;
	}

	/**
	 * Compute the P-Norm of the Banach Space Input
	 * 
	 * @param adblX The Banach Space Input
	 * 
	 * @return The P-Norm of the Banach Space Input
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double norm (
		final double[] adblX)
		throws java.lang.Exception
	{
		if (!validate (adblX)) throw new java.lang.Exception ("BanachSpace::norm => Cannot Validate Inputs");

		int iDimension = adblX.length;

		double dblNorm = 0 == _iPNorm ? java.lang.Math.abs (adblX[0]) : 0.;

		for (int i = 0; i < iDimension; ++i) {
			double dblAbsoluteX = java.lang.Math.abs (adblX[i]);

			if (0 == _iPNorm)
				dblNorm = dblNorm > dblAbsoluteX ? dblNorm : dblAbsoluteX;
			else
				dblNorm += java.lang.Math.pow (dblAbsoluteX, _iPNorm);
		}

		return 0 == _iPNorm ? dblNorm : java.lang.Math.pow (dblNorm, 1. / _iPNorm);
	}
}

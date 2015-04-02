
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
 * MultivariateFunctionBanachSpace implements the normed, bounded/unbounded Continuous Multivariate Functions
 *  on Real-valued R^d Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultivariateFunctionBanachSpace {
	private org.drip.kernel.spaces.BanachSpace _bs = null;
	private org.drip.function.deterministic.AbstractMultivariate _amFunc = null;

	/**
	 * MultivariateFunctionBanachSpace Constructor
	 * 
	 * @param amFunc Multivariate Function
	 * @param bs Underlying Banach Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public MultivariateFunctionBanachSpace (
		final org.drip.function.deterministic.AbstractMultivariate amFunc,
		final org.drip.kernel.spaces.BanachSpace bs)
		throws java.lang.Exception
	{
		if (null == (_bs = bs) || null == (_amFunc = amFunc))
			throw new java.lang.Exception ("MultivariateFunctionBanachSpace Constructor: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying Multivariate Function
	 * 
	 * @return The Underlying Multivariate Function
	 */

	public org.drip.function.deterministic.AbstractMultivariate multivariateFunction()
	{
		return _amFunc;
	}

	/**
	 * Retrieve the Underlying Banach Metric Space
	 * 
	 * @return The Underlying Banach Metric Space
	 */

	public org.drip.kernel.spaces.BanachSpace metricSpace()
	{
		return _bs;
	}

	/**
	 * Compute the L_p^m Norm of the Function Banach Space
	 * 
	 * @param aadblX Array of the Multidimensional Input Points
	 * 
	 * @return The L_p^m Norm of the Function Banach Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double norm (
		final double[][] aadblX)
		throws java.lang.Exception
	{
		if (null == aadblX)
			throw new java.lang.Exception ("MultivariateFunctionBanachSpace::norm => Invalid Inputs");

		int iNumVector = aadblX.length;
		double[] adblFunctionValue = new double[iNumVector];

		for (int i = 0; i < iNumVector; ++i)
			adblFunctionValue[i] = _amFunc.evaluate (aadblX[i]);

		return _bs.norm (adblFunctionValue);
	}
}

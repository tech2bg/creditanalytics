
package org.drip.sequence.functional;

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
 * IndependentBoundedVariateSum contains the Implementation of the Independent Sums of Bounded Variates.
 *
 * @author Lakshmi Krishnamurthy
 */

public class IndependentBoundedVariateSum extends
	org.drip.sequence.functional.SeparableBoundedMultivariateRandom {
	private double[] _adblVariateBound = null;

	/**
	 * IndependentBoundedVariateSum Constructor
	 * 
	 * @param au The Univariate Function
	 * @param adblWeight Array of Variable Weights
	 * @param adblVariateBound Array of Variable Bounds
	 * 
	 * @throws java.lang.Exception Thrown if IndependentBoundedVariateSum cannot be constructed
	 */

	public IndependentBoundedVariateSum (
		final org.drip.function.deterministic.AbstractUnivariate au,
		final double[] adblWeight,
		final double[] adblVariateBound)
		throws java.lang.Exception
	{
		super (au, adblWeight);

		if (null == (_adblVariateBound = adblVariateBound) || 0 == _adblVariateBound.length)
			throw new java.lang.Exception ("IndependentBoundedVariateSum ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of Target Variate Bounds
	 * 
	 * @return Array of Target Variate Bounds
	 */

	public double[] variateBounds()
	{
		return _adblVariateBound;
	}

	@Override public double targetVarianceBound (
		final int iTargetVariateIndex)
		throws java.lang.Exception
	{
		return _adblVariateBound[iTargetVariateIndex] * _adblVariateBound[iTargetVariateIndex];
	}
}


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
 * IndependentBinaryVariateSum contains the Implementation of the Weighted Sums of {0,1}-valued Independent
 * 	(but not identically distributed) Binary Random Variates.
 *
 * @author Lakshmi Krishnamurthy
 */

public class IndependentBinaryVariateSum extends
	org.drip.sequence.functional.SeparableBoundedMultivariateRandom {
	private double[] _adblWeight = null;
	private double[] _adblPositiveProbability = null;

	/**
	 * IndependentBinaryVariateSum Constructor
	 * 
	 * @param au The Univariate Function
	 * @param adblWeight Array of Variable Weights
	 * @param adblPositiveProbability Array of Probability of the Variable reaching 1
	 * 
	 * @throws java.lang.Exception Thrown if IndependentBinaryVariateSum cannot be constructed
	 */

	public IndependentBinaryVariateSum (
		final org.drip.function.deterministic.AbstractUnivariate au,
		final double[] adblWeight,
		final double[] adblPositiveProbability)
		throws java.lang.Exception
	{
		super (au, adblWeight);

		if (null == (_adblWeight = adblWeight) || null == (_adblPositiveProbability =
			adblPositiveProbability))
			throw new java.lang.Exception ("IndependentBinaryVariateSum ctr: Invalid Inputs");

		int iNumVariate = _adblWeight.length;

		if (_adblPositiveProbability.length != iNumVariate)
			throw new java.lang.Exception ("IndependentBinaryVariateSum ctr: Invalid Inputs");
	}

	@Override public double targetVarianceBound (
		final int iTargetVariateIndex)
		throws java.lang.Exception
	{
		return _adblWeight[iTargetVariateIndex] * _adblWeight[iTargetVariateIndex] *
			_adblPositiveProbability[iTargetVariateIndex] * (1. -
				_adblPositiveProbability[iTargetVariateIndex]);
	}
}

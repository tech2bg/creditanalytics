
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
 * SeparableBoundedMultivariateRandom contains the Implementation of the Bounded Objective Function dependent
 * 	on Multivariate Random Variables where the Multivariate Function is a Linear Combination of univariate
 *  Functions acting on each Variate.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class SeparableBoundedMultivariateRandom extends
	org.drip.sequence.functional.BoundedMultivariateRandom {
	private double[] _adblWeight = null;
	private org.drip.function.deterministic.AbstractUnivariate _au = null;

	protected SeparableBoundedMultivariateRandom (
		final org.drip.function.deterministic.AbstractUnivariate au,
		final double[] adblWeight)
		throws java.lang.Exception
	{
		if (null == (_adblWeight = adblWeight) || 0 == _adblWeight.length || null == (_au = au))
			throw new java.lang.Exception ("SeparableBoundedMultivariateRandom ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Separable Univariate Function
	 * 
	 * @return The Separable Univariate Function
	 */

	public org.drip.function.deterministic.AbstractUnivariate separableUnivariate()
	{
		return _au;
	}

	/**
	 * Retrieve the Weights
	 * 
	 * @return The Weights
	 */

	public double[] weights()
	{
		return _adblWeight;
	}

	@Override public double evaluate (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		double dblValue = 0.;
		int iNumVariate = adblVariate.length;

		if (_adblWeight.length < iNumVariate)
			throw new java.lang.Exception ("SeparableBoundedMultivariateRandom::evaluate => Invalid Inputs");

		for (int i = 0; i < iNumVariate; ++i)
			dblValue += _adblWeight[i] * _au.evaluate (adblVariate[i]);

		return dblValue;
	}
}

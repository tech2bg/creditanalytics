
package org.drip.sequence.random;

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
 * Poisson implements the Poisson Random Number Generator.
 *
 * @author Lakshmi Krishnamurthy
 */

public class Poisson extends org.drip.sequence.random.UnivariateSequenceGenerator {
	private double _dblLambda = java.lang.Double.NaN;
	private double _dblExponentialLambda = java.lang.Double.NaN;

	/**
	 * Construct a Poisson Random Number Generator
	 * 
	 * @param dblLambda Lambda
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public Poisson (
		final double dblLambda)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLambda = dblLambda) || 0. >= _dblLambda)
			throw new java.lang.Exception ("Poisson constructor: Invalid inputs");

		_dblExponentialLambda = java.lang.Math.exp (-1. * _dblLambda);
	}

	/**
	 * Retrieve Lambda
	 * 
	 * @return Lambda
	 */

	public double lambda()
	{
		return _dblLambda;
	}

	@Override public double random()
	{
		int i = 0;
		double dblPoissonProduct = 1.;

		java.util.Random r = new java.util.Random();

		while (dblPoissonProduct > _dblExponentialLambda) {
			++i;

			dblPoissonProduct *= r.nextDouble();
		}

		return i;
	}

	@Override public org.drip.sequence.metrics.SingleSequenceAgnosticMetrics sequence (
		final int iNumEntry,
		final org.drip.measure.continuous.UnivariateDistribution distPopulation)
	{
		double[] adblSequence = new double[iNumEntry];

		for (int i = 0; i < iNumEntry; ++i)
			adblSequence[i] = random();

		try {
			return new org.drip.sequence.metrics.PoissonSequenceAgnosticMetrics (adblSequence, null ==
				distPopulation ? java.lang.Double.NaN : distPopulation.mean());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

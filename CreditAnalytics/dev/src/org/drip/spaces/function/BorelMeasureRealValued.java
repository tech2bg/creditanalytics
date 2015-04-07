
package org.drip.spaces.function;

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
 * BorelMeasureRealValued exposes the normed Functional Spaces containing the Continuous, Real-Valued
 *  Elements and their associated Probability Measure.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BorelMeasureRealValued {
	private org.drip.function.deterministic.AbstractMultivariate _amFunc = null;
	private org.drip.spaces.measure.ContinuousRealValuedBorelSigma _crvbs = null;

	/**
	 * BorelMeasureRealValued Constructor
	 * 
	 * @param amFunc Multivariate Function
	 * @param crvbs Underlying Borel Measure Real-Valued Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BorelMeasureRealValued (
		final org.drip.function.deterministic.AbstractMultivariate amFunc,
		final org.drip.spaces.measure.ContinuousRealValuedBorelSigma crvbs)
		throws java.lang.Exception
	{
		if (null == (_crvbs = crvbs) || null == (_amFunc = amFunc))
			throw new java.lang.Exception ("BorelMeasureRealValued Constructor: Invalid Inputs");
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
	 * Retrieve the Underlying Borel-Algebra Real-Valued Metric Space
	 * 
	 * @return The Underlying Borel-Algebra Real-Valued Metric Space
	 */

	public org.drip.spaces.measure.ContinuousRealValuedBorelSigma metricSpace()
	{
		return _crvbs;
	}

	/**
	 * Compute the Population ESS (i.e., the Essential Spectrum) of the Spanning Space
	 * 
	 * @return The Population ESS
	 * 
	 * @throws java.lang.Exception Thrown if the ESS cannot be computed
	 */

	public double populationESS()
		throws java.lang.Exception
	{
		org.drip.function.deterministic.AbstractMultivariate amMultiDist = new
			org.drip.function.deterministic.AbstractMultivariate (null) {
			@Override public double evaluate (
				final double[] adblX)
				throws java.lang.Exception
			{
				return _amFunc.evaluate (adblX) * _crvbs.multivariateDistribution().density (adblX);
			}
		};

		org.drip.function.deterministic.VariateOutputPair vopMaxima = amMultiDist.maxima (_crvbs.leftEdge(),
			_crvbs.rightEdge());

		if (null == vopMaxima)
			throw new java.lang.Exception ("BorelMeasureRealValued::populationESS => Cannot compute VOP");

		return _amFunc.evaluate (vopMaxima.variates());
	}

	/**
	 * Compute the Population Metric Norm of the Spanning Space
	 * 
	 * @return The Population Metric Norm
	 * 
	 * @throws java.lang.Exception Thrown if the p-Norm cannot be computed
	 */

	public double populationMetricNorm()
		throws java.lang.Exception
	{
		final int iPNorm = _crvbs.pnorm();

		if (0 == iPNorm) return populationESS();

		org.drip.function.deterministic.AbstractMultivariate amMultiDistFunctionalPNorm = new
			org.drip.function.deterministic.AbstractMultivariate (null) {
			@Override public double evaluate (
				final double[] adblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (adblX) || _crvbs.dimension() != adblX.length)
					throw new java.lang.Exception
						("BorelMeasureRealValued::populationMetricNorm => Invalid Inputs");

				return java.lang.Math.abs (java.lang.Math.pow (_amFunc.evaluate (adblX), iPNorm)) *
					_crvbs.multivariateDistribution().density (adblX);
			}
		};

		return java.lang.Math.pow (amMultiDistFunctionalPNorm.integrate (_crvbs.leftEdge(),
			_crvbs.rightEdge()), 1. / iPNorm);
	}
}


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
 * BorelMeasureRealValuedFunction exposes the normed  Functional Spaces containing the Continuous,
 *  Real-Valued Elements and their associated Probability Measure.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BorelMeasureRealValuedFunction {
	private org.drip.kernel.spaces.BorelMeasureRealValuedSpace _bmrvs = null;
	private org.drip.function.deterministic.AbstractMultivariate _amFunc = null;

	/**
	 * BorelMeasureRealValuedFunction Constructor
	 * 
	 * @param amFunc Multivariate Function
	 * @param bmrvs Underlying Borel Measure Real-Valued Space
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BorelMeasureRealValuedFunction (
		final org.drip.function.deterministic.AbstractMultivariate amFunc,
		final org.drip.kernel.spaces.BorelMeasureRealValuedSpace bmrvs)
		throws java.lang.Exception
	{
		if (null == (_bmrvs = bmrvs) || null == (_amFunc = amFunc))
			throw new java.lang.Exception ("BorelMeasureRealValuedFunction Constructor: Invalid Inputs");
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

	public org.drip.kernel.spaces.BorelMeasureRealValuedSpace metricSpace()
	{
		return _bmrvs;
	}

	/**
	 * Compute the ESS (i.e., the Essential Spectrum) of the Spanning Space
	 * 
	 * @return The ESS
	 * 
	 * @throws java.lang.Exception Thrown if the ESS cannot be computed
	 */

	public double ess()
		throws java.lang.Exception
	{
		org.drip.function.deterministic.AbstractMultivariate amMultiDist = new
			org.drip.function.deterministic.AbstractMultivariate (null) {
			@Override public double evaluate (
				final double[] adblX)
				throws java.lang.Exception
			{
				return _amFunc.evaluate (adblX) * _bmrvs.multivariateDistribution().density (adblX);
			}
		};

		org.drip.function.deterministic.VariateOutputPair vopMaxima = amMultiDist.maxima (_bmrvs.leftEdge(),
			_bmrvs.rightEdge());

		if (null == vopMaxima)
			throw new java.lang.Exception ("BorelMeasureRealValuedFunction::ess => Cannot compute VOP");

		return _amFunc.evaluate (vopMaxima.variates());
	}

	/**
	 * Compute the P-Norm of the Spanning Space
	 * 
	 * @return The P-Norm
	 * 
	 * @throws java.lang.Exception Thrown if the p-Norm cannot be computed
	 */

	public double norm()
		throws java.lang.Exception
	{
		final int iPNorm = _bmrvs.pnorm();

		if (0 == iPNorm) return ess();

		org.drip.function.deterministic.AbstractMultivariate amMultiDistFunctionalPNorm = new
			org.drip.function.deterministic.AbstractMultivariate (null) {
			@Override public double evaluate (
				final double[] adblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (adblX) || _bmrvs.dimension() != adblX.length)
					throw new java.lang.Exception ("BorelMeasureRealValuedSpace::norm => Invalid Inputs");

				return java.lang.Math.abs (java.lang.Math.pow (_amFunc.evaluate (adblX), iPNorm)) *
					_bmrvs.multivariateDistribution().density (adblX);
			}
		};

		return java.lang.Math.pow (amMultiDistFunctionalPNorm.integrate (_bmrvs.leftEdge(),
			_bmrvs.rightEdge()), 1. / iPNorm);
	}
}


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
 * BorelMeasureRealValuedSpace exposes the normed Continuous Spaces containing the Real-Valued Element Ranges
 * 	and their associated Probability Measure.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BorelMeasureRealValuedSpace extends org.drip.kernel.spaces.MultidimensionalRealValuedSpace {
	private int _iPNorm = -1;
	private org.drip.quant.distribution.Multivariate _multiDist = null;

	/**
	 * BorelMeasureRealValuedSpace Constructor
	 * 
	 * @param aURVS Array of the Real Valued Spaces
	 * @param iPNorm p-Norm
	 * @param multiDist The Underlying Multivariate Probability Distribution Measure
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public BorelMeasureRealValuedSpace (
		final org.drip.kernel.spaces.UnidimensionalRealValuedSpace[] aURVS,
		final int iPNorm,
		final org.drip.quant.distribution.Multivariate multiDist)
		throws java.lang.Exception
	{
		super (aURVS);

		if (0 > (_iPNorm = iPNorm))
			throw new java.lang.Exception ("BorelMeasureRealValuedSpace ctr: Invalid Inputs");

		_multiDist = multiDist;
	}

	/**
	 * Retrieve the Borel Sigma Multivariate Probability Measure
	 * 
	 * @return The Borel Sigma Multivariate Probability Measure
	 */

	public org.drip.quant.distribution.Multivariate multivariateDistribution()
	{
		return _multiDist;
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
				return _multiDist.density (adblX);
			}
		};

		org.drip.function.deterministic.VariateOutputPair vopMaxima = amMultiDist.maxima (leftEdge(),
			rightEdge());

		if (null == vopMaxima)
			throw new java.lang.Exception ("BorelMeasureRealValuedSpace::ess => Cannot compute VOP");

		return vopMaxima.output();
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
		if (0 == _iPNorm) return ess();

		org.drip.function.deterministic.AbstractMultivariate amMultiDistPNorm = new
			org.drip.function.deterministic.AbstractMultivariate (null) {
			@Override public double evaluate (
				final double[] adblX)
				throws java.lang.Exception
			{
				if (!org.drip.quant.common.NumberUtil.IsValid (adblX))
					throw new java.lang.Exception ("BorelMeasureRealValuedSpace::norm => Invalid Inputs");

				double dblPNorm = 0.;
				int iDimension = adblX.length;

				if (dimension() != iDimension)
					throw new java.lang.Exception ("BorelMeasureRealValuedSpace::norm => Invalid Inputs");

				for (int i = 0; i < iDimension; ++i)
					dblPNorm += java.lang.Math.abs (java.lang.Math.pow (adblX[i], _iPNorm));

				return dblPNorm * _multiDist.density (adblX);
			}
		};

		return java.lang.Math.pow (amMultiDistPNorm.integrate (leftEdge(), rightEdge()), 1. / _iPNorm);
	}
}

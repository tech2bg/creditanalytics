
package org.drip.spaces.cover;

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
 * ScaleSensitiveCoveringNumber implements the Lower/Upper Bounds for the General Class of Functions in terms
 * 	of their scale-sensitive dimensions (i.e., the fat shattering coefficients).
 * 
 * The References are:
 * 
 * 	1) D. Pollard (1984): Convergence of Stochastic Processes, Springer, New York.
 * 
 * 	2) N. Alon, S. Ben-David, N. Cesa-Bianchi, and D. Haussler (1993): Scale-sensitive Dimensions, Uniform-
 * 		Convergence, and Learnability, Proceedings of the ACM Symposium on the Foundations of Computer
 * 		Science.
 * 
 * 	3) P. L. Bartlett, S. R. Kulkarni, and S. E. Posner (1997): Covering Numbers for Real-valued Function
 * 		Classes, IEEE Transactions on Information Theory 43 (5) 1721-1724.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ScaleSensitiveCoveringNumber implements org.drip.spaces.cover.CoveringNumber {
	private int _iSampleSize = -1;
	private org.drip.function.deterministic.R1ToR1 _auFatShatter = null;

	/**
	 * ScaleSensitiveCoveringNumber Constructor
	 * 
	 * @param auFatShatter The Cover Fat Shattering Coefficient Function
	 * @param iSampleSize Sample Size
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ScaleSensitiveCoveringNumber (
		final org.drip.function.deterministic.R1ToR1 auFatShatter,
		final int iSampleSize)
		throws java.lang.Exception
	{
		if (null == (_auFatShatter = auFatShatter) || 0 >= (_iSampleSize = iSampleSize))
			throw new java.lang.Exception ("ScaleSensitiveCoveringNumber ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Fat Shattering Coefficient Function
	 * 
	 * @return The Fat Shattering Coefficient Function
	 */

	public org.drip.function.deterministic.R1ToR1 fatShatteringFunction()
	{
		return _auFatShatter;
	}

	/**
	 * Retrieve the Sample Size
	 * 
	 * @return The Sample Size
	 */

	public int sampleSize()
	{
		return _iSampleSize;
	}

	/**
	 * Compute the Minimum Sample Size required to Estimate the Cardinality corresponding to the Specified
	 * 	Cover
	 * 
	 * @param dblCover The Cover
	 * 
	 * @return The Minimum Sample Size
	 * 
	 * @throws java.lang.Exception Thrown if the Minimum Sample Size Cannot be computed
	 */

	public double sampleSizeLowerBound (
		final double dblCover)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblCover) || 0. == dblCover)
			throw new java.lang.Exception
				("ScaleSensitiveCoveringNumber::sampleSizeLowerBound => Invalid Inputs");

		double dblLog2 = java.lang.Math.log (2.);

		return 2. * _auFatShatter.evaluate (0.25 * dblCover) * java.lang.Math.log (64. * java.lang.Math.E *
			java.lang.Math.E / (dblCover * dblLog2)) / dblLog2;
	}

	/**
	 * Compute the Cardinality for the Subset T (|x) that possesses the Specified Cover for the Restriction
	 * 	of the Input Function Class Family F (|x).
	 *  
	 * @param dblCover The Specified Cover
	 * 
	 * @return The Restricted Subset Cardinality
	 * 
	 * @throws java.lang.Exception Thrown if the Restricted Subset Cardinality cannot be computed
	 */

	public double restrictedSubsetCardinality (
		final double dblCover)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblCover) || 0. == dblCover)
			throw new java.lang.Exception
				("ScaleSensitiveCoveringNumber::restrictedSubsetCardinality => Invalid Inputs");

		double dblLog2 = java.lang.Math.log (2.);

		double dblFatShatteringCoefficient = _auFatShatter.evaluate (0.25 * dblCover);

		if (_iSampleSize < 2. * dblFatShatteringCoefficient * java.lang.Math.log (64. * java.lang.Math.E *
			java.lang.Math.E / (dblCover * dblLog2)) / dblLog2)
			throw new java.lang.Exception
				("ScaleSensitiveCoveringNumber::restrictedSubsetCardinality => Invalid Inputs");

		return 6. * dblFatShatteringCoefficient * java.lang.Math.log (16. / dblCover) * java.lang.Math.log
			(32. * java.lang.Math.E * _iSampleSize / (dblFatShatteringCoefficient * dblCover)) / dblLog2 +
				dblLog2;
	}

	/**
	 * Compute the Log of the Weight Loading Coefficient for the Maximum Cover Term in:
	 * 
	 * 	{Probability that the Empirical Error > Cover} <= 4 * exp (-m * Cover^2 / 128) *
	 * 		<Max Covering Number Over the Specified Sample>
	 * 
	 * Reference is:
	 *
	 *	- D. Haussler (1995): Sphere Packing Numbers for Subsets of the Boolean n-Cube with Bounded
	 *		Vapnik-Chervonenkis Dimension, Journal of the COmbinatorial Theory A 69 (2) 217.
	 *
	 * @param dblCover The Specified Cover
	 * 
	 * @return Log of the Weight Loading Coefficient for the Maximum Cover Term
	 * 
	 * @throws java.lang.Exception Thrown if the Log of the Weight Loading Coefficient cannot be computed
	 */

	public double upperProbabilityBoundWeight (
		final double dblCover)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblCover) || 0. == dblCover)
			throw new java.lang.Exception
				("ScaleSensitiveCoveringNumber::upperProbabilityBoundWeight => Invalid Inputs");

		return java.lang.Math.log (4.) - (dblCover * dblCover * _iSampleSize / 128.);
	}

	@Override public org.drip.spaces.function.GeneralizedNormedFunctionSpace functionSpace()
	{
		return null;
	}

	@Override public double logLowerBound (
		final double dblCover)
		throws java.lang.Exception
	{
		return restrictedSubsetCardinality (dblCover);
	}

	@Override public double logUpperBound (
		final double dblCover)
		throws java.lang.Exception
	{
		return _auFatShatter.evaluate (4. * dblCover) / 32.;
	}
}


package org.drip.sequence.custom;

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
 * LongestCommonSubsequence contains Variance Bounds on the Critical Measures of the Longest Common
 *  Subsequence between two Strings.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LongestCommonSubsequence extends org.drip.sequence.functional.BoundedMultivariateRandom {

	/**
	 * Lower Bound of the Conjecture of the Expected Value of the LCS Length
	 * 
	 * @param adblVariate Array of Input Variates
	 * 
	 * @return Lower Bound of the Conjecture of the Expected Value of the LCS Length
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double expectationConjectureLowerBound (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		if (null == adblVariate)
			throw new java.lang.Exception
				("LongestCommonSubsequence::expectationConjectureLowerBound => Invalid Inputs");

		return 0.37898;
	}

	/**
	 * Upper Bound of the Conjecture of the Expected Value of the LCS Length
	 * 
	 * @param adblVariate Array of Input Variates
	 * 
	 * @return Upper Bound of the Conjecture of the Expected Value of the LCS Length
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double expectationConjectureUpperBound (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		if (null == adblVariate)
			throw new java.lang.Exception
				("LongestCommonSubsequence::expectationConjectureUpperBound => Invalid Inputs");

		return 0.418815;
	}

	/**
	 * Conjecture of the Expected Value of the LCS Length
	 * 
	 * @param adblVariate Array of Input Variates
	 * 
	 * @return Conjecture of the Expected Value of the LCS Length
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double expectationConjecture (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		if (null == adblVariate)
			throw new java.lang.Exception
				("LongestCommonSubsequence::expectationConjecture => Invalid Inputs");

		return adblVariate.length / (1. + java.lang.Math.sqrt (2.));
	}

	@Override public double evaluate (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		return 0.25 * (expectationConjectureLowerBound (adblVariate) + expectationConjectureUpperBound
			(adblVariate)) + 0.5 * expectationConjecture (adblVariate);
	}

	@Override public double targetVariateVarianceBound (
		final int iTargetVariateIndex)
		throws java.lang.Exception
	{
		return 1.0;
	}
}

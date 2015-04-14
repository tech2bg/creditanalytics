
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
 * NormedRdToR1 is the abstract class underlying the f : Post-Validated R^d -> Post-Validated R^1 Normed
 *  Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class NormedRdToR1 extends org.drip.spaces.function.NormedRdInput {
	private org.drip.function.deterministic.RdToR1 _funcRdToR1 = null;
	private org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace _guvsOutput = null;

	protected NormedRdToR1 (
		final org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace gmvsInput,
		final org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace guvsOutput,
		final org.drip.function.deterministic.RdToR1 funcRdToR1,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (gmvsInput, iPNorm);

		if (null == (_guvsOutput = guvsOutput) || null == (_funcRdToR1 = funcRdToR1))
			throw new java.lang.Exception ("NormedRdToR1 ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying RdToR1 Function
	 * 
	 * @return The Underlying RdToR1 Function
	 */

	public org.drip.function.deterministic.RdToR1 function()
	{
		return _funcRdToR1;
	}

	@Override public double sampleSupremumNorm (
		final org.drip.spaces.instance.ValidatedCombinatorialRealMultidimensional vcrmInstance)
		throws java.lang.Exception
	{
		if (null == vcrmInstance || !vcrmInstance.tensorSpaceType().match (input()))
			throw new java.lang.Exception ("NormedRdToR1::sampleSupremumNorm => Invalid Input");

		double[][] aadblInstance = vcrmInstance.instance();

		int iNumSample = aadblInstance.length;

		double dblSupremumNorm = java.lang.Math.abs (_funcRdToR1.evaluate (aadblInstance[0]));

		for (int i = 1; i < iNumSample; ++i) {
			double dblResponse = java.lang.Math.abs (_funcRdToR1.evaluate (aadblInstance[i]));

			if (dblResponse > dblSupremumNorm) dblSupremumNorm = dblResponse;
		}

		return dblSupremumNorm;
	}

	@Override public double sampleMetricNorm (
		final org.drip.spaces.instance.ValidatedCombinatorialRealMultidimensional vcrmInstance)
		throws java.lang.Exception
	{
		if (null == vcrmInstance || !vcrmInstance.tensorSpaceType().match (input()))
			throw new java.lang.Exception ("NormedRdToR1::sampleMetricNorm => Invalid Input");

		double[][] aadblInstance = vcrmInstance.instance();

		int iNumSample = aadblInstance.length;
		double dblNorm = 0.;

		int iPNorm = pNorm();

		for (int i = 0; i < iNumSample; ++i)
			dblNorm += java.lang.Math.pow (java.lang.Math.abs (_funcRdToR1.evaluate (aadblInstance[i])),
				iPNorm);

		return java.lang.Math.pow (dblNorm, 1. / iPNorm);
	}

	@Override public double populationESS()
		throws java.lang.Exception
	{
		org.drip.spaces.tensor.GeneralizedMultidimensionalVectorSpace gmvsInput = input();

		if (!(gmvsInput instanceof org.drip.spaces.metric.ContinuousRealMultidimensionalBanach))
			throw new java.lang.Exception ("NormedRdToR1::populationESS => Incomptabile Input Vector Space");

		return _funcRdToR1.evaluate (((org.drip.spaces.metric.ContinuousRealMultidimensionalBanach)
			gmvsInput).populationMode());
	}

	@Override public org.drip.spaces.tensor.GeneralizedVectorSpace output()
	{
		return _guvsOutput;
	}
}

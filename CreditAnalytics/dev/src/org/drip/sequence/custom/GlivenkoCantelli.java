
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
 * GlivenkoCantelli contains Variance Bounds on the Critical Measures of Uniform Deviations of the
 * 	Glivenko-Cantelli Class.
 *
 * @author Lakshmi Krishnamurthy
 */

public class GlivenkoCantelli extends org.drip.sequence.functional.BoundedMultivariateRandom {
	private int _iSampleSize = -1;
	private double _dblPopulationProbability = java.lang.Double.NaN;

	/**
	 * GlivenkoCantelli Constructor
	 * 
	 * @param iSampleSize Size of the Observation Sample
	 * @param dblPopulationProbability The Population Probability
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public GlivenkoCantelli (
		final int iSampleSize,
		final double dblPopulationProbability)
		throws java.lang.Exception
	{
		if (0 >= (_iSampleSize = iSampleSize) || !org.drip.quant.common.NumberUtil.IsValid
			(_dblPopulationProbability = dblPopulationProbability))
			throw new java.lang.Exception ("GlivenkoCantelli ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Population Probability
	 * 
	 * @return The Population Probability
	 */

	public double populationProbability()
	{
		return _dblPopulationProbability;
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

	@Override public double evaluate (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		if (!org.drip.function.deterministic.AbstractMultivariate.ValidateInput (adblVariate))
			throw new java.lang.Exception ("GlivenkoCantelli::evaluate => Invalid Inputs");

		double dblUniformDeviation = 0.;
		int iNumVariate = adblVariate.length;

		for (int i = 0; i < iNumVariate; ++i) {
			if (adblVariate[i] != 0. && adblVariate[i] != 1.)
				throw new java.lang.Exception ("GlivenkoCantelli::evaluate => Invalid Variate");

			dblUniformDeviation += adblVariate[i] - _dblPopulationProbability;
		}

		return dblUniformDeviation / iNumVariate;
	}

	@Override public double targetVarianceBound (
		final int iTargetVariateIndex)
		throws java.lang.Exception
	{
		return 1. / _iSampleSize;
	}
}

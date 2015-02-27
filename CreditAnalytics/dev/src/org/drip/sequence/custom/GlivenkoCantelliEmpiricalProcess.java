
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
 * GlivenkoCantelliEmpiricalProcess contains Implementation of the Critical Measures of Uniform Deviations
 * 	of the Glivenko-Cantelli Class with Empirical Process Function Family.
 *
 * @author Lakshmi Krishnamurthy
 */

public class GlivenkoCantelliEmpiricalProcess extends org.drip.sequence.custom.GlivenkoCantelli {
	private org.drip.function.deterministic.AbstractUnivariate[] _aAUEmpirical = null;

	/**
	 * GlivenkoCantelliEmpiricalProcess Constructor
	 * 
	 * @param iSampleSize Size of the Observation Sample
	 * @param dblPopulationProbability The Population Probability
	 * @param aAUEmpirical Empirical Function Family
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public GlivenkoCantelliEmpiricalProcess (
		final int iSampleSize,
		final double dblPopulationProbability,
		final org.drip.function.deterministic.AbstractUnivariate[] aAUEmpirical)
		throws java.lang.Exception
	{
		super (iSampleSize, dblPopulationProbability);

		if (null == (_aAUEmpirical = aAUEmpirical) || 0 == _aAUEmpirical.length)
			throw new java.lang.Exception ("GlivenkoCantelliEmpiricalProcess ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Empirical Function Family
	 * 
	 * @return The Empirical Function Family
	 */

	public org.drip.function.deterministic.AbstractUnivariate[] empiricalFunctionFamily()
	{
		return _aAUEmpirical;
	}

	@Override public double evaluate (
		final double[] adblVariate)
		throws java.lang.Exception
	{
		if (!org.drip.function.deterministic.AbstractMultivariate.ValidateInput (adblVariate))
			throw new java.lang.Exception ("GlivenkoCantelliEmpiricalProcess::evaluate => Invalid Inputs");

		int iNumVariate = adblVariate.length;
		double dblUniformDeviationSupremum = 0.;
		int iEmpiricalFamilySize = _aAUEmpirical.length;

		double dblPopulationProbability = populationProbability();

		for (int iEmpiricalFamilyIndex = 0; iEmpiricalFamilyIndex < iEmpiricalFamilySize;
			++iEmpiricalFamilyIndex) {
			double dblUniformDeviation = 0.;

			for (int iVariateIndex = 0; iVariateIndex < iNumVariate; ++iVariateIndex) {
				if (adblVariate[iVariateIndex] != 0. && adblVariate[iVariateIndex] != 1.)
					throw new java.lang.Exception
						("GlivenkoCantelliEmpiricalProcess::evaluate => Invalid Variate");

				dblUniformDeviation += adblVariate[iVariateIndex] - dblPopulationProbability;
			}

			dblUniformDeviationSupremum = dblUniformDeviationSupremum > dblUniformDeviation ?
				dblUniformDeviationSupremum : dblUniformDeviation;
		}

		return dblUniformDeviationSupremum / iNumVariate;
	}

	@Override public double targetVarianceBound (
		final int iTargetVariateIndex)
		throws java.lang.Exception
	{
		return 4.;
	}
}

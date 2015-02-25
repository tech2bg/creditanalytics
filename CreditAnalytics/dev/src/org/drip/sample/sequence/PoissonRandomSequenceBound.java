
package org.drip.sample.sequence;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.distribution.*;
import org.drip.sequence.metrics.*;
import org.drip.sequence.random.Poisson;
import org.drip.sequence.random.RandomSequenceGenerator;
import org.drip.service.api.CreditAnalytics;

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
 * PoissonRandomSequenceBound demonstrates the Computation of the Probabilistic Bounds for a Sample Random
 * 	Poisson Sequence.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PoissonRandomSequenceBound {

	private static final void Head (
		final String strHeader)
	{
		System.out.println();

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		System.out.println (strHeader);

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		System.out.println ("\t| SIZE ||               <-               TOLERANCES               ->               |");

		System.out.println ("\t|----------------------------------------------------------------------------------|");
	}

	private static final void ChernoffStirlingBounds (
		final RandomSequenceGenerator iidsg,
		final Univariate dist,
		final int[] aiSampleSize,
		final double[] adblTolerance)
		throws Exception
	{
		for (int iSampleSize : aiSampleSize) {
			PoissonSequenceAgnosticMetrics ssamDist = (PoissonSequenceAgnosticMetrics) iidsg.sequence (
				iSampleSize,
				dist
			);

			String strDump = "\t| " + FormatUtil.FormatDouble (iSampleSize, 3, 0, 1) + " => ";

			for (double dblTolerance : adblTolerance)
				strDump += FormatUtil.FormatDouble (ssamDist.chernoffStirlingUpperBound (dblTolerance), 1, 9, 1.) + " | ";

			System.out.println (strDump);
		}
	}

	public static void main (
		final String[] args)
		throws Exception
	{
		CreditAnalytics.Init ("");

		Poisson poissonRandom = new Poisson (10.);

		UnivariatePoisson poissonDistribution = new UnivariatePoisson (10.);

		int[] aiSampleSize = new int[] {
			10, 20, 50, 100, 250
		};

		double[] adblTolerance = new double[] {
			5., 10., 15., 20., 25.
		};

		Head ("\t|        CHERNOFF-STIRLING BOUNDS    -     METRICS FROM UNDERLYING GENERATOR       |");

		ChernoffStirlingBounds (
			poissonRandom,
			poissonDistribution,
			aiSampleSize,
			adblTolerance
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		Head ("\t|      CHERNOFF-STIRLING BOUNDS    -     METRICS FROM EMPIRICAL DISTRIBUTION       |");

		ChernoffStirlingBounds (
			poissonRandom,
			null,
			aiSampleSize,
			adblTolerance
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");
	}
}

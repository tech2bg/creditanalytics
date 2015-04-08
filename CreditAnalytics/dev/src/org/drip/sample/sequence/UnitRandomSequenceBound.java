
package org.drip.sample.sequence;

import org.drip.measure.continuous.*;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.metrics.*;
import org.drip.sequence.random.*;
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
 * SingleRandomSequenceBound demonstrates the Computation of the Probabilistic Bounds for a Sample Random
 * 	Sequence.
 *
 * @author Lakshmi Krishnamurthy
 */

public class UnitRandomSequenceBound {

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

	private static final void ChernoffBinomialBounds (
		final UnivariateSequenceGenerator iidsg,
		final UnivariateDistribution dist,
		final int[] aiSampleSize,
		final double[] adblTolerance)
		throws Exception
	{
		for (int iSampleSize : aiSampleSize) {
			UnitSequenceAgnosticMetrics ssamDist = (UnitSequenceAgnosticMetrics) iidsg.sequence (
				iSampleSize,
				dist
			);

			String strDump = "\t| " + FormatUtil.FormatDouble (iSampleSize, 3, 0, 1) + " => ";

			for (double dblTolerance : adblTolerance)
				strDump += FormatUtil.FormatDouble (ssamDist.chernoffBinomialUpperBound (dblTolerance), 1, 9, 1.) + " | ";

			System.out.println (strDump);
		}
	}

	private static final void PoissonChernoffBinomialBounds (
		final UnivariateSequenceGenerator iidsg,
		final UnivariateDistribution dist,
		final int[] aiSampleSize,
		final double[] adblTolerance)
		throws Exception
	{
		for (int iSampleSize : aiSampleSize) {
			UnitSequenceAgnosticMetrics ssamDist = (UnitSequenceAgnosticMetrics) iidsg.sequence (
				iSampleSize,
				dist
			);

			String strDump = "\t| " + FormatUtil.FormatDouble (iSampleSize, 3, 0, 1) + " => ";

			for (double dblTolerance : adblTolerance)
				strDump += FormatUtil.FormatDouble (ssamDist.chernoffPoissonUpperBound (dblTolerance), 1, 9, 1.) + " | ";

			System.out.println (strDump);
		}
	}

	private static final void KarpHagerupRubUpperBounds (
		final UnivariateSequenceGenerator iidsg,
		final UnivariateDistribution dist,
		final int[] aiSampleSize,
		final double[] adblTolerance)
		throws Exception
	{
		for (int iSampleSize : aiSampleSize) {
			UnitSequenceAgnosticMetrics ssamDist = (UnitSequenceAgnosticMetrics) iidsg.sequence (
				iSampleSize,
				dist
			);

			String strDump = "\t| " + FormatUtil.FormatDouble (iSampleSize, 3, 0, 1) + " => ";

			for (double dblTolerance : adblTolerance)
				strDump += FormatUtil.FormatDouble (ssamDist.karpHagerupRubBounds (dblTolerance).upper(), 1, 9, 1.) + " | ";

			System.out.println (strDump);
		}
	}

	private static final void KarpHagerupRubLowerBounds (
		final UnivariateSequenceGenerator iidsg,
		final UnivariateDistribution dist,
		final int[] aiSampleSize,
		final double[] adblTolerance)
		throws Exception
	{
		for (int iSampleSize : aiSampleSize) {
			UnitSequenceAgnosticMetrics ssamDist = (UnitSequenceAgnosticMetrics) iidsg.sequence (
				iSampleSize,
				dist
			);

			String strDump = "\t| " + FormatUtil.FormatDouble (iSampleSize, 3, 0, 1) + " => ";

			for (double dblTolerance : adblTolerance)
				strDump += FormatUtil.FormatDouble (ssamDist.karpHagerupRubBounds (dblTolerance).lower(), 1, 9, 1.) + " | ";

			System.out.println (strDump);
		}
	}

	public static void main (
		final String[] args)
		throws Exception
	{
		CreditAnalytics.Init ("");

		BoundedUniform uniformRandom = new BoundedUniform (0., 1.);

		UnivariateBoundedUniformDistribution uniformDistribution = new UnivariateBoundedUniformDistribution (0., 1.);

		int[] aiSampleSize = new int[] {
			10, 20, 50, 100, 250
		};

		double[] adblTolerance = new double[] {
			0.01, 0.03, 0.05, 0.07, 0.10
		};

		Head ("\t|        CHERNOFF-BINOMIAL BOUNDS    -     METRICS FROM UNDERLYING GENERATOR       |");

		ChernoffBinomialBounds (
			uniformRandom,
			uniformDistribution,
			aiSampleSize,
			adblTolerance
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		Head ("\t|      CHERNOFF-BINOMIAL BOUNDS    -     METRICS FROM EMPIRICAL DISTRIBUTION       |");

		ChernoffBinomialBounds (
			uniformRandom,
			null,
			aiSampleSize,
			adblTolerance
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		Head ("\t|       POISSON CHERNOFF-BINOMIAL BOUNDS  -   METRICS FROM UNDERLYING GENERATOR    |");

		PoissonChernoffBinomialBounds (
			uniformRandom,
			uniformDistribution,
			aiSampleSize,
			adblTolerance
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		Head ("\t|       POISSON CHERNOFF-BINOMIAL BOUNDS  -  METRICS FROM EMPIRICAL DISTRIBUTION   |");

		PoissonChernoffBinomialBounds (
			uniformRandom,
			null,
			aiSampleSize,
			adblTolerance
		);

		aiSampleSize = new int[] {
			100, 200, 300, 500, 999
		};

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		Head ("\t|         KARP-HAGERUP-RUB UPPER BOUNDS  -   METRICS FROM UNDERLYING GENERATOR     |");

		KarpHagerupRubUpperBounds (
			uniformRandom,
			null,
			aiSampleSize,
			adblTolerance
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		Head ("\t|        KARP-HAGERUP-RUB UPPER BOUNDS  -   METRICS FROM EMPIRICAL DISTRIBUTION    |");

		KarpHagerupRubUpperBounds (
			uniformRandom,
			null,
			aiSampleSize,
			adblTolerance
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		Head ("\t|         KARP-HAGERUP-RUB LOWER BOUNDS  -   METRICS FROM UNDERLYING GENERATOR     |");

		KarpHagerupRubLowerBounds (
			uniformRandom,
			null,
			aiSampleSize,
			adblTolerance
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		Head ("\t|        KARP-HAGERUP-RUB LOWER BOUNDS  -   METRICS FROM EMPIRICAL DISTRIBUTION    |");

		KarpHagerupRubLowerBounds (
			uniformRandom,
			null,
			aiSampleSize,
			adblTolerance
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");
	}
}

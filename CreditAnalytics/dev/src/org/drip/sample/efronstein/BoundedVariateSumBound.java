
package org.drip.sample.efronstein;

import org.drip.quant.common.FormatUtil;
import org.drip.sequence.custom.GlivenkoCantelliUniformDeviation;
import org.drip.sequence.functional.*;
import org.drip.sequence.metrics.SingleSequenceAgnosticMetrics;
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
 * BoundedVariateSumBound demonstrates the Computation of the Probabilistic Bounds for the Realization of the
 *  Values of a Multivariate Function over Random Sequence Values (in this case, sum of the independent
 *  Random Variates) using Variants of the Efron-Stein Methodology.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BoundedVariateSumBound {

	private static final SingleSequenceAgnosticMetrics[] IIDDraw (
		final RandomSequenceGenerator rsg,
		final int iNumSample)
		throws Exception
	{
		SingleSequenceAgnosticMetrics[] aSSAM = new SingleSequenceAgnosticMetrics[iNumSample];

		for (int i = 0; i < iNumSample; ++i)
			aSSAM[i] = rsg.sequence (iNumSample, null);

		return aSSAM;
	}

	private static final GlivenkoCantelliUniformDeviation BoundedSumFunction (
		final BoundedUniform bu,
		final int iNumVariate)
		throws Exception
	{
		double[] adblWeight = new double[iNumVariate];

		for (int i = 0; i < iNumVariate; ++i)
			adblWeight[i] = 1.;

		return new GlivenkoCantelliUniformDeviation (
			new BoundedIdempotentUnivariateRandom (
				0.,
				null,
				bu.upperBound() - bu.lowerBound()
			),
			adblWeight
		);
	}

	private static final void MartingaleDifferencesRun (
		final BoundedUniform bu,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bu,
				iNumSample
			);

			EfronSteinMetrics esam = new EfronSteinMetrics (
				BoundedSumFunction (bu, iNumSample),
				aSSAM
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (esam.martingaleVarianceUpperBound(), 2, 2, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void GhostVariateVarianceRun (
		final BoundedUniform bu,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bu,
				iNumSample
			);

			EfronSteinMetrics esam = new EfronSteinMetrics (
				BoundedSumFunction (bu, iNumSample),
				aSSAM
			);

			SingleSequenceAgnosticMetrics[] aSSAMGhost = IIDDraw (
				bu,
				iNumSample
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (esam.ghostVarianceUpperBound (aSSAMGhost), 2, 2, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void EfronSteinSteeleRun (
		final BoundedUniform bu,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bu,
				iNumSample
			);

			EfronSteinMetrics esam = new EfronSteinMetrics (
				BoundedSumFunction (bu, iNumSample),
				aSSAM
			);

			SingleSequenceAgnosticMetrics[] aSSAMGhost = IIDDraw (
				bu,
				iNumSample
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (esam.efronSteinSteeleBound (aSSAMGhost), 2, 2, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void PivotDifferencesRun (
		final BoundedUniform bu,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bu,
				iNumSample
			);

			MultivariateRandom func = BoundedSumFunction (bu, iNumSample);

			EfronSteinMetrics esam = new EfronSteinMetrics (
				func,
				aSSAM
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (esam.pivotVarianceUpperBound (new FlatMultivariateRandom (0.)), 2, 2, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void BoundedDifferencesRun (
		final BoundedUniform bu,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bu,
				iNumSample
			);

			EfronSteinMetrics esam = new EfronSteinMetrics (
				BoundedSumFunction (bu, iNumSample),
				aSSAM
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (esam.boundedVarianceUpperBound(), 2, 2, 1.);
		}

		System.out.println (strDump + " |");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		int iNumSet = 5;

		int[] aiSampleSize = new int[] {
			3, 10, 25, 50
		};

		BoundedUniform bu = new BoundedUniform (0., 1.);

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|  Martingale Differences Variance Upper Bound  |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			MartingaleDifferencesRun (
				bu,
				iSampleSize,
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|   Symmetrized Variate Variance Upper Bound    |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			GhostVariateVarianceRun (
				bu,
				iSampleSize,
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");

		aiSampleSize = new int[] {
			3, 10, 25, 50, 75, 99
		};

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|    Efron-Stein-Steele Variance Upper Bound    |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			EfronSteinSteeleRun (
				bu,
				iSampleSize,
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|    Pivoted Differences Variance Upper Bound   |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			PivotDifferencesRun (
				bu,
				iSampleSize,
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");

		double[] adblVariateBound = new double[aiSampleSize[aiSampleSize.length - 1]];

		for (int i = 0; i < adblVariateBound.length; ++i)
			adblVariateBound[i] = bu.upperBound() - bu.lowerBound();

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|   Bounded Differences Variance Upper Bound    |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			BoundedDifferencesRun (
				bu,
				iSampleSize,
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");
	}
}

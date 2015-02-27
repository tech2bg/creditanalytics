
package org.drip.sample.efronstein;

import org.drip.quant.common.FormatUtil;
import org.drip.sequence.custom.LongestCommonSubsequence;
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
 * LongestCommonSubsequenceBound demonstrates the Computation of the Probabilistic Bounds for the Longest
 *  Common Subsequence across each half over the Random Sequence Values using Variants of the Efron-Stein
 *  Methodology.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LongestCommonSubsequenceBound {

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

	private static final void MartingaleDifferencesRun (
		final RandomSequenceGenerator rsg,
		final MultivariateRandom func,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				rsg,
				iNumSample
			);

			EfronSteinAgnosticMetrics esam = new EfronSteinAgnosticMetrics (
				func,
				aSSAM
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (esam.martingaleVarianceUpperBound(), 2, 2, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void GhostVariateVarianceRun (
		final RandomSequenceGenerator rsg,
		final MultivariateRandom func,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				rsg,
				iNumSample
			);

			EfronSteinAgnosticMetrics esam = new EfronSteinAgnosticMetrics (
				func,
				aSSAM
			);

			SingleSequenceAgnosticMetrics[] aSSAMGhost = IIDDraw (
				rsg,
				iNumSample
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (esam.ghostVarianceUpperBound (aSSAMGhost), 2, 2, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void EfronSteinSteeleRun (
		final RandomSequenceGenerator rsg,
		final MultivariateRandom func,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				rsg,
				iNumSample
			);

			EfronSteinAgnosticMetrics esam = new EfronSteinAgnosticMetrics (
				func,
				aSSAM
			);

			SingleSequenceAgnosticMetrics[] aSSAMGhost = IIDDraw (
				rsg,
				iNumSample
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (esam.efronSteinSteeleBound (aSSAMGhost), 2, 2, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void PivotDifferencesRun (
		final RandomSequenceGenerator rsg,
		final MultivariateRandom func,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				rsg,
				iNumSample
			);

			EfronSteinAgnosticMetrics esam = new EfronSteinAgnosticMetrics (
				func,
				aSSAM
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (esam.pivotVarianceUpperBound (func), 2, 2, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void BoundedDifferencesRun (
		final RandomSequenceGenerator rsg,
		final MultivariateRandom func,
		final int iNumSample,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (iNumSample, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				rsg,
				iNumSample
			);

			EfronSteinAgnosticMetrics esam = new EfronSteinAgnosticMetrics (
				func,
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
			3, 10, 25
		};

		BoundedUniform bu = new BoundedUniform (0., 1.);

		MultivariateRandom func = new LongestCommonSubsequence();

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|  Martingale Differences Variance Upper Bound  |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			MartingaleDifferencesRun (
				bu,
				func,
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
				func,
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
				func,
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
				func,
				iSampleSize,
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|   Bounded Differences Variance Upper Bound    |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			BoundedDifferencesRun (
				bu,
				func,
				iSampleSize,
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");
	}
}

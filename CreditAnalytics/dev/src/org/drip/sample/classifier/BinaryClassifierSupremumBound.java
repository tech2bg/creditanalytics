
package org.drip.sample.classifier;

import org.drip.function.classifier.*;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.classifier.*;
import org.drip.sequence.functional.FlatMultivariateRandom;
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
 * BinaryClassifierSupremumBound demonstrates the Computation of the Probabilistic Bounds for the Supremum
 * 	among the Class of Binary Classifier Functions for an Empirical Sample from its Population Mean using
 *  Variants of the Efron-Stein Methodology.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BinaryClassifierSupremumBound {

	private static final short[] EmpiricalOutcome (
		final int iNumOutcome)
		throws Exception
	{
		short[] asEmpiricalOutcome = new short[iNumOutcome];

		for (int i = 0; i < iNumOutcome; ++i)
			asEmpiricalOutcome[i] = (short) (Math.random() + 0.5);

		return asEmpiricalOutcome;
	}

	private static final SingleSequenceAgnosticMetrics[] IIDDraw (
		final UnivariateSequenceGenerator rsg,
		final int iNumSample)
		throws Exception
	{
		SingleSequenceAgnosticMetrics[] aSSAM = new SingleSequenceAgnosticMetrics[iNumSample];

		for (int i = 0; i < iNumSample; ++i)
			aSSAM[i] = rsg.sequence (iNumSample, null);

		return aSSAM;
	}

	private static final EmpiricalLossSupremum EmpiricalLossSupremumFunction (
		final short[] asEmpiricalOutcome)
		throws Exception
	{
		AbstractBinaryClassifier[] aClassifier = null;

		return new EmpiricalLossSupremum (
			new FunctionClass (
				aClassifier,
				new ClassAsymptoticSampleBound (
					0.01,
					-1.5
				)
			),
			asEmpiricalOutcome
		);
	}

	private static final void MartingaleDifferencesRun (
		final Binary bsg,
		final short[] asEmpiricalOutcome,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (asEmpiricalOutcome.length, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bsg,
				asEmpiricalOutcome.length
			);

			EmpiricalSupremumLossMetrics eslm = new EmpiricalSupremumLossMetrics (
				EmpiricalLossSupremumFunction (
					asEmpiricalOutcome
				),
				aSSAM
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (eslm.martingaleVarianceUpperBound(), 1, 3, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void GhostVariateVarianceRun (
		final Binary bsg,
		final short[] asEmpiricalOutcome,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (asEmpiricalOutcome.length, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bsg,
				asEmpiricalOutcome.length
			);

			EmpiricalSupremumLossMetrics eslm = new EmpiricalSupremumLossMetrics (
				EmpiricalLossSupremumFunction (
					asEmpiricalOutcome
				),
				aSSAM
			);

			SingleSequenceAgnosticMetrics[] aSSAMGhost = IIDDraw (
				bsg,
				asEmpiricalOutcome.length
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (eslm.ghostVarianceUpperBound (aSSAMGhost), 1, 3, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void EfronSteinSteeleRun (
		final Binary bsg,
		final short[] asEmpiricalOutcome,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (asEmpiricalOutcome.length, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bsg,
				asEmpiricalOutcome.length
			);

			EmpiricalSupremumLossMetrics eslm = new EmpiricalSupremumLossMetrics (
				EmpiricalLossSupremumFunction (
					asEmpiricalOutcome
				),
				aSSAM
			);

			SingleSequenceAgnosticMetrics[] aSSAMGhost = IIDDraw (
				bsg,
				asEmpiricalOutcome.length
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (eslm.efronSteinSteeleBound (aSSAMGhost), 1, 3, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void PivotDifferencesRun (
		final Binary bsg,
		final short[] asEmpiricalOutcome,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (asEmpiricalOutcome.length, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bsg,
				asEmpiricalOutcome.length
			);

			EmpiricalSupremumLossMetrics eslm = new EmpiricalSupremumLossMetrics (
				EmpiricalLossSupremumFunction (
					asEmpiricalOutcome
				),
				aSSAM
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (eslm.pivotVarianceUpperBound (new FlatMultivariateRandom (0.)), 1, 3, 1.);
		}

		System.out.println (strDump + " |");
	}

	private static final void LugosiVarianceRun (
		final Binary bsg,
		final short[] asEmpiricalOutcome,
		final int iNumSet)
		throws Exception
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (asEmpiricalOutcome.length, 2, 0, 1.) + " => ";

		for (int j = 0; j < iNumSet; ++j) {
			SingleSequenceAgnosticMetrics[] aSSAM = IIDDraw (
				bsg,
				asEmpiricalOutcome.length
			);

			EmpiricalSupremumLossMetrics eslm = new EmpiricalSupremumLossMetrics (
				EmpiricalLossSupremumFunction (
					asEmpiricalOutcome
				),
				aSSAM
			);

			SingleSequenceAgnosticMetrics[] aSSAMGhost = IIDDraw (
				bsg,
				1
			);

			if (0 != j) strDump += " |";

			strDump += FormatUtil.FormatDouble (eslm.lugosiVarianceBound (aSSAMGhost[0].sequence()), 1, 3, 1.);
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

		Binary bin = new Binary (0.7);

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|  Martingale Differences Variance Upper Bound  |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			MartingaleDifferencesRun (
				bin,
				EmpiricalOutcome (iSampleSize),
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|   Symmetrized Variate Variance Upper Bound    |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			GhostVariateVarianceRun (
				bin,
				EmpiricalOutcome (iSampleSize),
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");

		aiSampleSize = new int[] {
			3, 10, 25, 50, 75, 99
		};

		System.out.println ("\t|    Efron-Stein-Steele Variance Upper Bound    |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			EfronSteinSteeleRun (
				bin,
				EmpiricalOutcome (iSampleSize),
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|    Pivoted Differences Variance Upper Bound   |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			PivotDifferencesRun (
				bin,
				EmpiricalOutcome (iSampleSize),
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");

		System.out.println ("\n\t|-----------------------------------------------|");

		System.out.println ("\t|       Lugosi Bounded Variance Upper Bound       |");

		System.out.println ("\t|-----------------------------------------------|");

		for (int iSampleSize : aiSampleSize)
			LugosiVarianceRun (
				bin,
				EmpiricalOutcome (iSampleSize),
				iNumSet
			);

		System.out.println ("\t|-----------------------------------------------|");
	}
}

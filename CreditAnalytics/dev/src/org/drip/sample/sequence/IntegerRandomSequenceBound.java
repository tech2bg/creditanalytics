
package org.drip.sample.sequence;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.distribution.*;
import org.drip.quant.random.BoundedUniformInteger;
import org.drip.quant.random.RandomSequenceGenerator;
import org.drip.sequence.bounds.*;
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
 * IntegerRandomSequenceBound demonstrates the Computation of the Probabilistic Bounds for a Sample Random
 * 	Integer Sequence.
 *
 * @author Lakshmi Krishnamurthy
 */

public class IntegerRandomSequenceBound {

	private static final void IntegerBounds (
		final RandomSequenceGenerator iidsg,
		final Univariate dist,
		final int[] aiSampleSize)
		throws Exception
	{
		for (int iSampleSize : aiSampleSize) {
			IntegerSequenceAgnosticMetrics ssamDist = (IntegerSequenceAgnosticMetrics) iidsg.sequence (
				iSampleSize,
				dist
			);

			String strDump = "\t| " + FormatUtil.FormatDouble (iSampleSize, 3, 0, 1) + " => ";

			strDump += FormatUtil.FormatDouble (ssamDist.probGreaterThanZeroUpperBound(), 1, 9, 1.) + " | " +
				FormatUtil.FormatDouble (ssamDist.probEqualToZeroUpperBound(), 1, 9, 1.) + " | ";

			System.out.println (strDump);
		}
	}

	public static void main (
		final String[] args)
		throws Exception
	{
		CreditAnalytics.Init ("");

		BoundedUniformInteger bui = new BoundedUniformInteger (0, 100);

		UnivariateBoundedUniformInteger integerDistribution = new UnivariateBoundedUniformInteger (0, 100);

		int[] aiSampleSize = new int[] {
			10, 20, 50, 100, 250
		};

		System.out.println();

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		System.out.println ("\t| Generating Integer Random Sequence Metrics");

		System.out.println ("\t| \tL -> R:");

		System.out.println ("\t| \t\tSample Size");

		System.out.println ("\t| \t\tUpper Probability Bound for X != 0");

		System.out.println ("\t| \t\tUpper Probability Bound for X = 0");

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		System.out.println ("\t| Generating Metrics off of Underlying Distribution");

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		IntegerBounds (
			bui,
			integerDistribution,
			aiSampleSize
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		System.out.println();

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		System.out.println ("\t| Generating Metrics off of Empirical Distribution");

		System.out.println ("\t|----------------------------------------------------------------------------------|");

		IntegerBounds (
			bui,
			null,
			aiSampleSize
		);

		System.out.println ("\t|----------------------------------------------------------------------------------|");
	}
}

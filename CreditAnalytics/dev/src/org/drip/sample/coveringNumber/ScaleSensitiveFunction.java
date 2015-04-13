
package org.drip.sample.coveringNumber;

import org.drip.function.deterministic.R1ToR1;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.cover.ScaleSensitiveCoveringNumber;
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
 * ScaleSensitiveFunction demonstrates Computation of the Restricted Covers, Restricted Probability Bounds,
 * 	the Lower Bounds, and the Upper Bounds for Functions that are absolutely Bounded.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ScaleSensitiveFunction {

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		final int iSampleSize = 10;

		R1ToR1 auFatShatter = new R1ToR1 (null) {
			@Override public double evaluate (
				final double dblX)
				throws Exception
			{
				return iSampleSize;
			}
		};

		ScaleSensitiveCoveringNumber sscn = new ScaleSensitiveCoveringNumber (
			auFatShatter,
			iSampleSize
		);

		double[] adblCover = new double[] {
			500., 600., 700., 800., 900., 960.
		};

		System.out.println ("\n\t||------------------------------------------------||");

		System.out.println ("\t||    Scale    Sensitive    Covering   Numbers    ||");

		System.out.println ("\t||    -----    ---------    --------   Numbers    ||");

		System.out.println ("\t|| L -> R:                                        ||");

		System.out.println ("\t||   Sample Size Lower Bound                      ||");

		System.out.println ("\t||   Restricted Subset Cardinality                ||");

		System.out.println ("\t||   Probability of the Cover Weight Upper Bound  ||");

		System.out.println ("\t||   Log Log Covering Number Lower Bound          ||");

		System.out.println ("\t||   Log Log Covering Number Upper Bound          ||");

		System.out.println ("\t||------------------------------------------------||");

		for (double dblCover : adblCover)
			System.out.println ("\t|| [" + FormatUtil.FormatDouble (dblCover, 3, 0, 1.) + "] => " +
				FormatUtil.FormatDouble (sscn.sampleSizeLowerBound (dblCover), 1, 1, 1.) + " |" +
				FormatUtil.FormatDouble (sscn.restrictedSubsetCardinality (dblCover), 3, 0, 1.) + " | " +
				FormatUtil.FormatDouble (sscn.upperProbabilityBoundWeight (dblCover), 5, 0, 1.) + " | " +
				FormatUtil.FormatDouble (Math.log (sscn.logLowerBound (dblCover)), 1, 2, 1.) + " -> " +
				FormatUtil.FormatDouble (Math.log (sscn.logUpperBound (dblCover)), 1, 2, 1.) + " ||"
			);

		System.out.println ("\t||------------------------------------------------||");
	}
}

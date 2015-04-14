
package org.drip.sample.coveringNumber;

import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.spaces.cover.BoundedFunctionCoveringNumber;

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
 * BoundedFunction demonstrates Computation of the Lower and the Upper Bounds for Functions that are
 *  absolutely Bounded.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BoundedFunction {

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		BoundedFunctionCoveringNumber bfcnVariation = new BoundedFunctionCoveringNumber (
			1.,
			1.,
			Double.NaN
		);

		BoundedFunctionCoveringNumber bfcnBounded = new BoundedFunctionCoveringNumber (
			1.,
			1.,
			1.
		);

		double[] adblCover = new double[] {
			0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08
		};

		System.out.println ("\n\t||------------------------------------------||");

		System.out.println ("\t||    Bounded  Function  Covering Number    ||");

		System.out.println ("\t||    -------  --------  -------- ------    ||");

		System.out.println ("\t|| L -> R:                                  ||");

		System.out.println ("\t||   Variation Bound Covering Number Lower  ||");

		System.out.println ("\t||   Variation Bound Covering Number Upper  ||");

		System.out.println ("\t||    Absolute Bound Covering Number Lower  ||");

		System.out.println ("\t||    Absolute Bound Covering Number Upper  ||");

		System.out.println ("\t||------------------------------------------||");

		for (double dblCover : adblCover)
			System.out.println ("\t|| [" + FormatUtil.FormatDouble (dblCover, 1, 2, 1.) + "] => " +
				FormatUtil.FormatDouble (Math.log (bfcnVariation.logLowerBound (dblCover)), 1, 2, 1.) + " ->" +
				FormatUtil.FormatDouble (Math.log (bfcnVariation.logUpperBound (dblCover)), 1, 2, 1.) + " | " +
				FormatUtil.FormatDouble (Math.log (bfcnBounded.logLowerBound (dblCover)), 1, 2, 1.) + " ->" +
				FormatUtil.FormatDouble (Math.log (bfcnBounded.logUpperBound (dblCover)), 1, 2, 1.) + " ||"
			);

		System.out.println ("\t||------------------------------------------||");
	}
}

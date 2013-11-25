
package org.drip.sample.spline;

import org.drip.quant.common.FormatUtil;
import org.drip.spline.bspline.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * BSplineSequence implements Samples for the Construction and the usage of various monic basis B Spline
 * 	Sequences. It demonstrates the following:
 * 	- Construction and Usage of segment Monic B Spline Sequence.
 * 	- Construction and Usage of segment Multic B Spline Sequence.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BSplineSequence {
	public static final void ComputeResponseMetric (
		final SegmentBasisFunction me,
		final String strComment)
		throws Exception
	{
		int iOrder = 1;
		double dblXIncrement = 0.25;

		double dblX = me.leading() - dblXIncrement;

		double dblXEnd = me.trailing() + dblXIncrement;

		System.out.println ("\n\t---------------------------------------------------------------");

		System.out.println ("\t-------------------------" + strComment + "---------------------------");

		System.out.println ("\t---------------------------------------------------------------\n");

		while (dblX <= dblXEnd) {
			System.out.println (
				"\t\tResponse[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (me.evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (me.normalizedCumulative (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (me.calcDerivative (dblX, iOrder), 1, 5, 1.));

			dblX += dblXIncrement;
		}
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double[] adblPredictorOrdinate = new double[] {1., 2., 3., 4., 5.};

		SegmentBasisFunction[] aMonic = BasisFunctionGenerator.MonicSequence (
			BasisHatPairGenerator.TENSION_HYPERBOLIC,
			adblPredictorOrdinate,
			1.);

		for (int i = 0; i < aMonic.length; ++i)
			ComputeResponseMetric (aMonic[i], "   MONIC   ");

		SegmentBasisFunction[] aQuadratic = BasisFunctionGenerator.MulticSequence (
			3,
			1.,
			aMonic);

		for (int i = 0; i < aQuadratic.length; ++i)
			ComputeResponseMetric (aQuadratic[i], " QUADRATIC ");

		SegmentBasisFunction[] aCubic = BasisFunctionGenerator.MulticSequence (
			4,
			1.,
			aQuadratic);

		for (int i = 0; i < aCubic.length; ++i)
			ComputeResponseMetric (aCubic[i], "   CUBIC   ");
	}
}


package org.drip.sample.spline;

import org.drip.spline.bspline.*;
import org.drip.quant.common.FormatUtil;

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
 * BasisMulticBSpline implements Samples for the Construction and the usage of various multic basis B Splines.
 *  It demonstrates the following:
 * 	- Construction of segment higher order B Spline from the corresponding Hat Basis Functions.
 * 	- Estimation of the derivatives and the basis envelope cumulative integrands.
 * 	- Estimation of the normalizer and the basis envelope cumulative normalized integrands.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisMulticBSpline {
	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double[] adblPredictorOrdinateLeft = new double[] {1., 2., 3.};
		double[] adblPredictorOrdinateRight = new double[] {2., 3., 4.};

		TensionBasisHat[] aTBHLeft = BasisHatPairGenerator.HyperbolicTensionHatPair (
			adblPredictorOrdinateLeft[0],
			adblPredictorOrdinateLeft[1],
			adblPredictorOrdinateLeft[2],
			1.);

		TensionBasisHat[] aTBHRight = BasisHatPairGenerator.HyperbolicTensionHatPair (
			adblPredictorOrdinateLeft[0],
			adblPredictorOrdinateLeft[1],
			adblPredictorOrdinateLeft[2],
			1.);

		SegmentBasisFunction meLeft = BasisFunctionGenerator.Monic (
			BasisHatPairGenerator.TENSION_HYPERBOLIC,
			CubicRationalLeftRaw.SHAPE_CONTROL_NONE,
			adblPredictorOrdinateLeft,
			1.);

		SegmentBasisFunction meRight = BasisFunctionGenerator.Monic (
			BasisHatPairGenerator.TENSION_HYPERBOLIC,
			CubicRationalLeftRaw.SHAPE_CONTROL_NONE,
			adblPredictorOrdinateRight,
			1.);

		System.out.println ("\n\t-------------------------------------\n");

		double dblX = 0.50;
		double dblXIncrement = 0.25;

		while (dblX <= 4.50) {
			System.out.println (
				"\t\tResponse[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (aTBHLeft[0].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (aTBHLeft[1].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (meLeft.evaluate (dblX), 1, 5, 1.));

			dblX += dblXIncrement;
		}

		System.out.println ("\n\t-------------------------------------\n");

		dblX = 0.50;

		while (dblX <= 4.50) {
			System.out.println (
				"\t\tResponse[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (aTBHRight[0].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (aTBHRight[1].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (meRight.evaluate (dblX), 1, 5, 1.));

			dblX += dblXIncrement;
		}

		SegmentBasisFunction[] aMultic = BasisFunctionGenerator.MulticSequence (
			3,
			1.,
			new SegmentBasisFunction[] {meLeft, meRight});

		System.out.println ("\n\t-------------------------------------\n");

		dblX = 0.50;
		dblXIncrement = 0.125;

		while (dblX <= 4.50) {
			System.out.println (
				"\t\tMultic Response[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (aMultic[0].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (aMultic[0].normalizedCumulative (dblX), 1, 5, 1.));

			dblX += dblXIncrement;
		}

		System.out.println ("\n\t-------------------------------------\n");
	}
}

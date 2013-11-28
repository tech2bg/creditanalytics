
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
	public static final void RunMulticBSplineTest (
		final String strHatType,
		final String strShapeControlType,
		final double dblTension,
		final int iMulticBSplineOrder)
		throws Exception
	{
		double[] adblPredictorOrdinateLeft = new double[] {1., 2., 3.};
		double[] adblPredictorOrdinateRight = new double[] {2., 3., 4.};

		TensionBasisHat[] aTBHLeft = BasisHatPairGenerator.HyperbolicTensionHatPair (
			adblPredictorOrdinateLeft[0],
			adblPredictorOrdinateLeft[1],
			adblPredictorOrdinateLeft[2],
			dblTension);

		TensionBasisHat[] aTBHRight = BasisHatPairGenerator.HyperbolicTensionHatPair (
			adblPredictorOrdinateRight[0],
			adblPredictorOrdinateRight[1],
			adblPredictorOrdinateRight[2],
			dblTension);

		SegmentBasisFunction sbfMonicLeft = SegmentBasisFunctionGenerator.Monic (
			strHatType,
			strShapeControlType,
			adblPredictorOrdinateLeft,
			2,
			dblTension);

		SegmentBasisFunction sbfMonicRight = SegmentBasisFunctionGenerator.Monic (
			strHatType,
			strShapeControlType,
			adblPredictorOrdinateRight,
			2,
			dblTension);

		System.out.println ("\n\t-------------------------------------------------");

		System.out.println ("\t            X    |   LEFT   |   RIGHT  |   MONIC  ");

		System.out.println ("\t-------------------------------------------------");

		double dblX = 0.50;
		double dblXIncrement = 0.25;

		while (dblX <= 4.50) {
			System.out.println (
				"\tResponse[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (aTBHLeft[0].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (aTBHLeft[1].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (sbfMonicLeft.evaluate (dblX), 1, 5, 1.));

			dblX += dblXIncrement;
		}

		System.out.println ("\n\t-------------------------------------------------");

		System.out.println ("\t            X    |   LEFT   |   RIGHT  |   MONIC  ");

		System.out.println ("\t-------------------------------------------------");

		dblX = 0.50;

		while (dblX <= 4.50) {
			System.out.println (
				"\tResponse[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (aTBHRight[0].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (aTBHRight[1].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (sbfMonicRight.evaluate (dblX), 1, 5, 1.));

			dblX += dblXIncrement;
		}

		SegmentBasisFunction[] sbfMultic = SegmentBasisFunctionGenerator.MulticSequence (
			iMulticBSplineOrder,
			new SegmentBasisFunction[] {sbfMonicLeft, sbfMonicRight});

		System.out.println ("\n\t-------------------------------------------------");

		System.out.println ("\t          PREDICTOR    | RESPONSE | CUMULATIVE  ");

		System.out.println ("\t-------------------------------------------------");

		dblX = 0.50;
		dblXIncrement = 0.125;

		while (dblX <= 4.50) {
			System.out.println (
				"\t\tMultic[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (sbfMultic[0].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (sbfMultic[0].normalizedCumulative (dblX), 1, 5, 1.));

			dblX += dblXIncrement;
		}

		System.out.println ("\n\t-------------------------------------------------\n");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		System.out.println ("\n    RAW TENSION HYPERBOLIC | LINEAR SHAPE CONTROL | TENSION = 1.0 | CUBIC B SPLINE");

		RunMulticBSplineTest (
			BasisHatPairGenerator.RAW_TENSION_HYPERBOLIC,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			1.,
			3);

		System.out.println ("\n   PROC TENSION HYPERBOLIC | LINEAR SHAPE CONTROL | TENSION = 1.0 | CUBIC B SPLINE");

		RunMulticBSplineTest (
			BasisHatPairGenerator.PROCESSED_TENSION_HYPERBOLIC,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			1.,
			3);

		System.out.println ("\n   RAW CUBIC RATIONAL | LINEAR SHAPE CONTROL | TENSION = 0.0 | CUBIC B SPLINE");

		RunMulticBSplineTest (
			BasisHatPairGenerator.PROCESSED_CUBIC_RATIONAL,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			0.,
			3);

		System.out.println ("\n   RAW CUBIC RATIONAL | LINEAR SHAPE CONTROL | TENSION = 1.0 | CUBIC B SPLINE");

		RunMulticBSplineTest (
			BasisHatPairGenerator.PROCESSED_CUBIC_RATIONAL,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			1.,
			3);

		System.out.println ("\n   RAW CUBIC RATIONAL | QUADRATIC SHAPE CONTROL | TENSION = 1.0 | CUBIC B SPLINE");

		RunMulticBSplineTest (
			BasisHatPairGenerator.PROCESSED_CUBIC_RATIONAL,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_QUADRATIC,
			1.,
			3);

		System.out.println ("\n   RAW CUBIC RATIONAL | EXPONENTIAL SHAPE CONTROL | TENSION = 1.0 | CUBIC B SPLINE");

		RunMulticBSplineTest (
			BasisHatPairGenerator.PROCESSED_CUBIC_RATIONAL,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_EXPONENTIAL,
			1.,
			3);
	}
}

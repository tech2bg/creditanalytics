
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
 * BasisMonicBSpline implements Samples for the Construction and the usage of various monic basis B Splines.
 *  It demonstrates the following:
 * 	- Construction of segment B Spline Hat Basis Functions.
 * 	- Estimation of the derivatives and the basis envelope cumulative integrands.
 * 	- Estimation of the normalizer and the basis envelope cumulative normalized integrands.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BasisMonicBSpline {
	public static final void TestMonicHatBasis (
		final String strHatType,
		final String strShapeController,
		final TensionBasisHat[] aTBH,
		final double[] adblPredictorOrdinate,
		final String strTest)
		throws Exception
	{
		SegmentBasisFunction me = SegmentBasisFunctionGenerator.Monic (
			strHatType,
			strShapeController,
			adblPredictorOrdinate,
			2,
			aTBH[0].tension());

		double dblX = 1.0;
		double dblXIncrement = 0.25;

		System.out.println ("\n\t-------------------------------------------------");

		System.out.println ("\t--------------" + strTest + "-------------");

		System.out.println ("\t-------------------------------------------------\n");

		System.out.println ("\t-------------X---|---LEFT---|---RIGHT--|--MONIC--\n");

		while (dblX <= 3.0) {
			System.out.println (
				"\tResponse[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (aTBH[0].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (aTBH[1].evaluate (dblX), 1, 5, 1.) + " | " +
				FormatUtil.FormatDouble (me.evaluate (dblX), 1, 5, 1.));

			dblX += dblXIncrement;
		}

		System.out.println ("\n\t------------------------------------------------\n");

		dblX = 1.0;

		while (dblX <= 3.0) {
			System.out.println (
				"\t\tNormCumulative[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (me.normalizedCumulative (dblX), 1, 5, 1.));

			dblX += dblXIncrement;
		}

		System.out.println ("\n\t------------------------------------------------\n");

		dblX = 1.0;
		int iOrder = 1;

		while (dblX <= 3.0) {
			System.out.println (
				"\t\t\tDeriv[" + FormatUtil.FormatDouble (dblX, 1, 3, 1.) + "] : " +
				FormatUtil.FormatDouble (me.calcDerivative (dblX, iOrder), 1, 5, 1.));

			dblX += dblXIncrement;
		}

		System.out.println ("\n\t-----------------------------------------------\n");
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double[] adblPredictorOrdinate = new double[] {1., 2., 3.};

		TensionBasisHat[] aTBHProcessed = BasisHatPairGenerator.ProcessedHyperbolicTensionHatPair (
			adblPredictorOrdinate[0],
			adblPredictorOrdinate[1],
			adblPredictorOrdinate[2],
			2,
			1.);

		TestMonicHatBasis (
			BasisHatPairGenerator.PROCESSED_TENSION_HYPERBOLIC,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			aTBHProcessed,
			adblPredictorOrdinate,
			" PROCESSED HYPERBOLIC ");

		TensionBasisHat[] aTBHStraight = BasisHatPairGenerator.HyperbolicTensionHatPair (
			adblPredictorOrdinate[0],
			adblPredictorOrdinate[1],
			adblPredictorOrdinate[2],
			1.);

		TestMonicHatBasis (
			BasisHatPairGenerator.RAW_TENSION_HYPERBOLIC,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			aTBHStraight,
			adblPredictorOrdinate,
			" STRAIGHT  HYPERBOLIC ");

		TensionBasisHat[] aTBHCubicRationalPlain = BasisHatPairGenerator.ProcessedCubicRationalHatPair (
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			adblPredictorOrdinate[0],
			adblPredictorOrdinate[1],
			adblPredictorOrdinate[2],
			2,
			0.);

		TestMonicHatBasis (
			BasisHatPairGenerator.PROCESSED_CUBIC_RATIONAL,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			aTBHCubicRationalPlain,
			adblPredictorOrdinate,
			"     CUBIC     FLAT   ");

		TensionBasisHat[] aTBHCubicRationalLinear = BasisHatPairGenerator.ProcessedCubicRationalHatPair (
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			adblPredictorOrdinate[0],
			adblPredictorOrdinate[1],
			adblPredictorOrdinate[2],
			2,
			1.);

		TestMonicHatBasis (
			BasisHatPairGenerator.PROCESSED_CUBIC_RATIONAL,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_LINEAR,
			aTBHCubicRationalLinear,
			adblPredictorOrdinate,
			" CUBIC LINEAR RATIONAL ");

		TensionBasisHat[] aTBHCubicRationalQuadratic = BasisHatPairGenerator.ProcessedCubicRationalHatPair (
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_QUADRATIC,
			adblPredictorOrdinate[0],
			adblPredictorOrdinate[1],
			adblPredictorOrdinate[2],
			2,
			1.);

		TestMonicHatBasis (
			BasisHatPairGenerator.PROCESSED_CUBIC_RATIONAL,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_QUADRATIC,
			aTBHCubicRationalQuadratic,
			adblPredictorOrdinate,
			" CUBIC  QUAD  RATIONAL ");

		TensionBasisHat[] aTBHCubicRationalExponential = BasisHatPairGenerator.ProcessedCubicRationalHatPair (
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_EXPONENTIAL,
			adblPredictorOrdinate[0],
			adblPredictorOrdinate[1],
			adblPredictorOrdinate[2],
			2,
			1.);

		TestMonicHatBasis (
			BasisHatPairGenerator.PROCESSED_CUBIC_RATIONAL,
			BasisHatShapeControl.SHAPE_CONTROL_RATIONAL_EXPONENTIAL,
			aTBHCubicRationalExponential,
			adblPredictorOrdinate,
			" CUBIC  EXP  RATIONAL ");
	}
}

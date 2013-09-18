
package org.drip.sample.regime;

import org.drip.math.common.FormatUtil;
import org.drip.math.function.*;
import org.drip.math.regime.MultiSegmentRegime;
import org.drip.math.regime.RegimeBuilder;
import org.drip.math.regime.RegimeCalibrationSetting;
import org.drip.math.segment.*;
import org.drip.math.spline.PolynomialBasisSetParams;

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
 * RegimeAdjuster demonstrates the Regime Manipulation and Adjustment API. It shows the following:
 * 	- Construct a simple Base Regime.
 * 	- Clip a left Portion of the Regime to construct a left-clipped Regime.
 * 	- Clip a right Portion of the Regime to construct a tight-clipped Regime.
 *  - Compare the values across all the regimes to establish a) the continuity in the base smoothness is,
 *  	preserved, and b) Continuity across the predictor ordinate for the implied response value is also
 *  	preserved.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RegimeAdjuster {

	/**
	 * Build Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Polynomial Segment Control Parameters
	 */

	public static final PredictorResponseBuilderParams PolynomialSegmentControlParams (
		final int iNumBasis,
		final DesignInelasticParams segParams,
		final ResponseScalingShapeController rssc)
		throws Exception
	{
		return new PredictorResponseBuilderParams (
			RegimeBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialBasisSetParams (iNumBasis),
			segParams,
			rssc);
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final MultiSegmentRegime BasisSplineRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp)
		throws Exception
	{
		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = RegimeBuilder.CreateCalibratedRegimeEstimator (
			"SPLINE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			new RegimeCalibrationSetting // Boundary Condition - Natural + Calibrate the Regime predictors to the responses
				(RegimeCalibrationSetting.BOUNDARY_CONDITION_NATURAL, RegimeCalibrationSetting.CALIBRATE));

		return regime;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * X predictors
		 */

		double[] adblX = new double[] { 1.00,  1.50,  2.00, 3.00, 4.00, 5.00, 6.50, 8.00, 10.00};

		/*
		 * Y responses
		 */

		double[] adblY = new double[] {25.00, 20.25, 16.00, 9.00, 4.00, 1.00, 0.25, 4.00, 16.00};

		/*
		 * Construct a rational shape controller with the shape controller tension of 1.
		 */

		double dblShapeControllerTension = 1.;

		ResponseScalingShapeController rssc = new ResponseScalingShapeController (false, new RationalShapeControl (dblShapeControllerTension));

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 2;
		int iRoughnessPenaltyDerivativeOrder = 2;

		DesignInelasticParams segParams = new DesignInelasticParams (iK, iRoughnessPenaltyDerivativeOrder);

		System.out.println (" \n---------- \n POLYNOMIAL \n ---------- \n");

		int iPolyNumBasis = 4;

		PredictorResponseBuilderParams prbp = PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc);

		MultiSegmentRegime regimeBase = BasisSplineRegimeTest (adblX, adblY, prbp);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		double dblX = regimeBase.getLeftPredictorOrdinateEdge();

		double dblXMax = regimeBase.getRightPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeBase.response (dblX), 1, 2, 1.) + " | "
				+ regimeBase.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regimeBase.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		System.out.println (" \n---------- \n LEFT CLIPPED \n ---------- \n");

		MultiSegmentRegime regimeLeftClipped = regimeBase.clipLeft ("LEFT_CLIP", 1.66);

		dblX = regimeBase.getLeftPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			if (regimeLeftClipped.in (dblX)) {
				System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeLeftClipped.response (dblX), 1, 2, 1.) + " | "
					+ regimeLeftClipped.monotoneType (dblX));

				System.out.println ("Jacobian Y[" + dblX + "]=" + regimeLeftClipped.jackDResponseDResponseInput (dblX).displayString());
			}

			dblX += 1.;
		}

		System.out.println (" \n---------- \n RIGHT CLIPPED \n ---------- \n");

		MultiSegmentRegime regimeRightClipped = regimeBase.clipRight ("RIGHT_CLIP", 7.48);

		dblX = regimeBase.getLeftPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			if (regimeRightClipped.in (dblX)) {
				System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeRightClipped.response (dblX), 1, 2, 1.) + " | "
					+ regimeRightClipped.monotoneType (dblX));

				System.out.println ("Jacobian Y[" + dblX + "]=" + regimeRightClipped.jackDResponseDResponseInput (dblX).displayString());
			}

			dblX += 1.;
		}

		dblX = regimeBase.getLeftPredictorOrdinateEdge();

		dblXMax = regimeBase.getRightPredictorOrdinateEdge();

		System.out.println ("\n-----------------------------------------------------------------------------------------------------");

		System.out.println ("                           BASE         ||      LEFT CLIPPED           ||      RIGHT CLIPPED");

		System.out.println ("-----------------------------------------------------------------------------------------------------");

		while (dblX <= dblXMax) {
			java.lang.String strLeftClippedValue = "         ";
			java.lang.String strRightClippedValue = "         ";
			java.lang.String strLeftClippedMonotonocity = "             ";
			java.lang.String strRightClippedMonotonocity = "             ";

			java.lang.String strDisplay = "Y[" + FormatUtil.FormatDouble (dblX, 2, 3, 1.) + "] => "
				+ FormatUtil.FormatDouble (regimeBase.response (dblX), 2, 6, 1.) + " | "
				+ regimeBase.monotoneType (dblX);

			if (regimeLeftClipped.in (dblX)) {
				strLeftClippedValue = FormatUtil.FormatDouble (regimeLeftClipped.response (dblX), 2, 6, 1.);

				strLeftClippedMonotonocity = regimeLeftClipped.monotoneType (dblX).toString();
			}

			if (regimeRightClipped.in (dblX)) {
				strRightClippedValue = FormatUtil.FormatDouble (regimeRightClipped.response (dblX), 2, 6, 1.);

				strRightClippedMonotonocity = regimeRightClipped.monotoneType (dblX).toString();
			}

			System.out.println (strDisplay + "  ||  " + strLeftClippedValue + " | " + strLeftClippedMonotonocity +
				"  ||  " + strRightClippedValue + " | " + strRightClippedMonotonocity);

			dblX += 0.5;
		}
	}
}

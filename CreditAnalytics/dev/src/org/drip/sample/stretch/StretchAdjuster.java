
package org.drip.sample.stretch;

import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.*;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;

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
 * StretchAdjuster demonstrates the Stretch Manipulation and Adjustment API. It shows the following:
 * 	- Construct a simple Base Stretch.
 * 	- Clip a left Portion of the Stretch to construct a left-clipped Stretch.
 * 	- Clip a right Portion of the Stretch to construct a tight-clipped Stretch.
 *  - Compare the values across all the stretches to establish a) the continuity in the base smoothness is,
 *  	preserved, and b) Continuity across the predictor ordinate for the implied response value is also
 *  	preserved.
 *
 * @author Lakshmi Krishnamurthy
 */

public class StretchAdjuster {

	/**
	 * Build Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param sdic Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Polynomial Segment Control Parameters
	 */

	public static final SegmentCustomBuilderControl PolynomialSegmentControlParams (
		final int iNumBasis,
		final SegmentDesignInelasticControl sdic,
		final ResponseScalingShapeControl rssc)
		throws Exception
	{
		return new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (iNumBasis),
			sdic,
			rssc);
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Stretch Jacobian
	 *  - Stretch knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param scbc The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final MultiSegmentSequence BasisSplineStretchTest (
		final double[] adblX,
		final double[] adblY,
		final SegmentCustomBuilderControl scbc)
		throws Exception
	{
		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		SegmentCustomBuilderControl[] aSCBC = new SegmentCustomBuilderControl[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSCBC[i] = scbc;

		/*
		 * Construct a Stretch instance 
		 */

		return MultiSegmentSequenceBuilder.CreateCalibratedStretchEstimator (
			"SPLINE_STRETCH",
			adblX, // predictors
			adblY, // responses
			aSCBC, // Basis Segment Builder parameters
			null,
			BoundarySettings.NaturalStandard(), // Boundary Condition - Natural
			MultiSegmentSequence.CALIBRATE); // Calibrate the Stretch predictors to the responses
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

		ResponseScalingShapeControl rssc = new ResponseScalingShapeControl (
			false,
			new QuadraticRationalShapeControl (dblShapeControllerTension));

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 2;
		int iRoughnessPenaltyDerivativeOrder = 2;

		SegmentDesignInelasticControl sdic = SegmentDesignInelasticControl.Create (
			iK,
			iRoughnessPenaltyDerivativeOrder);

		System.out.println (" \n---------- \n POLYNOMIAL \n ---------- \n");

		int iPolyNumBasis = 4;

		SegmentCustomBuilderControl scbc = PolynomialSegmentControlParams (iPolyNumBasis, sdic, rssc);

		MultiSegmentSequence mssBase = BasisSplineStretchTest (adblX, adblY, scbc);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Stretch Jacobian
		 */

		double dblX = mssBase.getLeftPredictorOrdinateEdge();

		double dblXMax = mssBase.getRightPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (mssBase.responseValue (dblX), 1, 2, 1.) + " | "
				+ mssBase.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + mssBase.jackDResponseDCalibrationInput (dblX, 1).displayString());

			dblX += 1.;
		}

		System.out.println ("\tSPLINE_STRETCH_BASE DPE: " + mssBase.curvatureDPE());

		System.out.println (" \n---------- \n LEFT CLIPPED \n ---------- \n");

		MultiSegmentSequence mssLeftClipped = mssBase.clipLeft ("LEFT_CLIP", 1.66);

		dblX = mssBase.getLeftPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			if (mssLeftClipped.in (dblX)) {
				System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (mssLeftClipped.responseValue (dblX), 1, 2, 1.) + " | "
					+ mssLeftClipped.monotoneType (dblX));

				System.out.println ("Jacobian Y[" + dblX + "]=" + mssLeftClipped.jackDResponseDCalibrationInput (dblX, 1).displayString());
			}

			dblX += 1.;
		}

		System.out.println ("\tSPLINE_STRETCH_LEFT DPE: " + mssLeftClipped.curvatureDPE());

		System.out.println (" \n---------- \n RIGHT CLIPPED \n ---------- \n");

		MultiSegmentSequence mssRightClipped = mssBase.clipRight ("RIGHT_CLIP", 7.48);

		dblX = mssBase.getLeftPredictorOrdinateEdge();

		while (dblX <= dblXMax) {
			if (mssRightClipped.in (dblX)) {
				System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (mssRightClipped.responseValue (dblX), 1, 2, 1.) + " | "
					+ mssRightClipped.monotoneType (dblX));

				System.out.println ("Jacobian Y[" + dblX + "]=" + mssRightClipped.jackDResponseDCalibrationInput (dblX, 1).displayString());
			}

			dblX += 1.;
		}

		System.out.println ("\tSPLINE_STRETCH_RIGHT DPE: " + mssRightClipped.curvatureDPE());

		dblX = mssBase.getLeftPredictorOrdinateEdge();

		dblXMax = mssBase.getRightPredictorOrdinateEdge();

		System.out.println ("\n-----------------------------------------------------------------------------------------------------");

		System.out.println ("                           BASE         ||      LEFT CLIPPED           ||      RIGHT CLIPPED");

		System.out.println ("-----------------------------------------------------------------------------------------------------");

		while (dblX <= dblXMax) {
			java.lang.String strLeftClippedValue = "         ";
			java.lang.String strRightClippedValue = "         ";
			java.lang.String strLeftClippedMonotonocity = "             ";
			java.lang.String strRightClippedMonotonocity = "             ";

			java.lang.String strDisplay = "Y[" + FormatUtil.FormatDouble (dblX, 2, 3, 1.) + "] => "
				+ FormatUtil.FormatDouble (mssBase.responseValue (dblX), 2, 6, 1.) + " | "
				+ mssBase.monotoneType (dblX);

			if (mssLeftClipped.in (dblX)) {
				strLeftClippedValue = FormatUtil.FormatDouble (mssLeftClipped.responseValue (dblX), 2, 6, 1.);

				strLeftClippedMonotonocity = mssLeftClipped.monotoneType (dblX).toString();
			}

			if (mssRightClipped.in (dblX)) {
				strRightClippedValue = FormatUtil.FormatDouble (mssRightClipped.responseValue (dblX), 2, 6, 1.);

				strRightClippedMonotonocity = mssRightClipped.monotoneType (dblX).toString();
			}

			System.out.println (strDisplay + "  ||  " + strLeftClippedValue + " | " + strLeftClippedMonotonocity +
				"  ||  " + strRightClippedValue + " | " + strRightClippedMonotonocity);

			dblX += 0.5;
		}
	}
}

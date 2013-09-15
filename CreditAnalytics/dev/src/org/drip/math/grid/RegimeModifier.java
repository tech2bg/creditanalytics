
package org.drip.math.grid;

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
 * RegimeModifier exports Regime modification/alteration methods to generate customized basis splines, with
 *  customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class RegimeModifier {

	/**
	 * Insert the specified Predictor Ordinate Knot into the specified Regime, using the specified Response
	 * 	Value
	 * 
	 * @param regimeIn Input Regime
	 * @param dblPredictorOrdinate Predictor Ordinate Knot
	 * @param dblResponseValue Response Value
	 * @param rcs Regime Calibration Parameters
	 * 
	 * @return The Regime with the Knot inserted
	 */

	public static final org.drip.math.grid.MultiSegmentRegime InsertKnot (
		final org.drip.math.grid.MultiSegmentRegime regimeIn,
		final double dblPredictorOrdinate,
		final double dblResponseValue,
		final org.drip.math.grid.RegimeCalibrationSetting rcs)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblPredictorOrdinate) ||
			!org.drip.math.common.NumberUtil.IsValid (dblResponseValue) || null == regimeIn ||
				regimeIn.isKnot (dblPredictorOrdinate))
			return null;

		org.drip.math.segment.PredictorResponse[] aSeg = regimeIn.getSegments();

		int iNewIndex = 0;
		int iNumSegmentIn = aSeg.length;
		double[] adblResponseValue = new double[iNumSegmentIn + 2];
		double[] adblPredictorOrdinate = new double[iNumSegmentIn + 2];
		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBPOut = new
			org.drip.math.segment.PredictorResponseBuilderParams[iNumSegmentIn + 1];

		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBPIn = regimeIn.getSegmentBuilderParams();

		if (dblPredictorOrdinate < aSeg[0].left()) {
			adblPredictorOrdinate[iNewIndex] = dblPredictorOrdinate;
			adblResponseValue[iNewIndex] = dblResponseValue;
			aPRBPOut[iNewIndex++] = aPRBPIn[0];
		}

		for (int i = 0; i < iNumSegmentIn; ++i) {
			aPRBPOut[iNewIndex] = aPRBPIn[i];

			adblPredictorOrdinate[iNewIndex] = aSeg[i].left();

			try {
				adblResponseValue[iNewIndex++] = regimeIn.response (aSeg[i].left());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (dblPredictorOrdinate > aSeg[i].left() && dblPredictorOrdinate < aSeg[i].right()) {
				adblPredictorOrdinate[iNewIndex] = dblPredictorOrdinate;
				adblResponseValue[iNewIndex] = dblResponseValue;
				aPRBPOut[iNewIndex++] = aPRBPIn[i];
			}
		}

		adblPredictorOrdinate[iNewIndex] = aSeg[iNumSegmentIn - 1].right();

		try {
			adblResponseValue[iNewIndex++] = regimeIn.response (aSeg[iNumSegmentIn - 1].right());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (dblPredictorOrdinate > aSeg[iNumSegmentIn - 1].right()) {
			adblResponseValue[iNewIndex] = dblResponseValue;
			adblPredictorOrdinate[iNewIndex] = dblPredictorOrdinate;
			aPRBPOut[aPRBPOut.length - 1] = aPRBPIn[aPRBPIn.length - 1];
		}

		return org.drip.math.grid.RegimeBuilder.CreateCalibratedRegimeEstimator (regimeIn.name(),
			adblPredictorOrdinate, adblResponseValue, aPRBPOut, rcs);
	}

	/**
	 * Append a Segment to the Right of the Specified Regime using the Supplied Constraint
	 * 
	 * @param regimeIn Input Regime
	 * @param dblPredictorOrdinateAppendRight The Predictor Ordinate at the Right Edge of the Segment to be
	 * 	appended
	 * @param rvc The Segment Response Value Constraint
	 * @param prbp Segment Builder Parameters
	 * @param rcs Regime Setup Parameters
	 * 
	 * @return The Regime with the Segment Appended
	 */

	public static final org.drip.math.grid.MultiSegmentRegime AppendSegment (
		final org.drip.math.grid.MultiSegmentRegime regimeIn,
		final double dblPredictorOrdinateAppendRight,
		final org.drip.math.segment.ResponseValueConstraint rvc,
		final org.drip.math.segment.PredictorResponseBuilderParams prbp,
		final org.drip.math.grid.RegimeCalibrationSetting rcs)
	{
		if (null == regimeIn || null == rvc || null == prbp || !org.drip.math.common.NumberUtil.IsValid
			(dblPredictorOrdinateAppendRight))
			return null;

		double dblRegimePredictorOrdinateRight = regimeIn.getRightPredictorOrdinateEdge();

		double[] adblConstraintOrdinate = rvc.predictorOrdinates();

		for (int i = 0; i < adblConstraintOrdinate.length; ++i) {
			if (adblConstraintOrdinate[i] <= dblRegimePredictorOrdinateRight) return null;
		}

		org.drip.math.segment.PredictorResponse[] aSegment = regimeIn.getSegments();

		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBPIn = regimeIn.getSegmentBuilderParams();

		int iNumSegmentIn = aSegment.length;
		double dblRegimeResponseValueLeft = java.lang.Double.NaN;
		double[] adblPredictorOrdinateOut = new double[iNumSegmentIn + 2];
		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBPOut = new
			org.drip.math.segment.PredictorResponseBuilderParams[iNumSegmentIn + 1];
		org.drip.math.segment.ResponseValueConstraint[] aRVCOut = new
			org.drip.math.segment.ResponseValueConstraint[iNumSegmentIn + 1];

		try {
			dblRegimeResponseValueLeft = regimeIn.response (regimeIn.getLeftPredictorOrdinateEdge());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumSegmentIn; ++i) {
			aPRBPOut[i] = aPRBPIn[i];

			adblPredictorOrdinateOut[i] = aSegment[i].left();

			double dblPredictorOrdinateRight = aSegment[i].right();

			try {
				aRVCOut[i] = new org.drip.math.segment.ResponseValueConstraint (new double[]
					{dblPredictorOrdinateRight}, new double[] {1.}, regimeIn.response
						(dblPredictorOrdinateRight));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		aRVCOut[iNumSegmentIn] = rvc;
		aPRBPOut[iNumSegmentIn] = prbp;
		adblPredictorOrdinateOut[iNumSegmentIn + 1] = dblPredictorOrdinateAppendRight;

		adblPredictorOrdinateOut[iNumSegmentIn] = aSegment[iNumSegmentIn - 1].right();

		return org.drip.math.grid.RegimeBuilder.CreateCalibratedRegimeEstimator (regimeIn.name(),
			adblPredictorOrdinateOut, dblRegimeResponseValueLeft, aRVCOut, aPRBPOut, rcs);
	}

	/**
	 * Insert the Predictor Ordinate Knot into the specified Regime
	 * 
	 * @param regimeIn Input Regime
	 * @param dblPredictorOrdinate Knot Predictor Ordinate
	 * @param pordLeftSegmentRightEdge Response Values for the Right Edge of the Left Segment
	 * @param pordRightSegmentLeftEdge Response Values for the Left Edge of the Right segment
	 * 
	 * @return The Regime with the Predictor Ordinate Knot inserted
	 */

	public static final org.drip.math.grid.MultiSegmentRegime InsertKnot (
		final org.drip.math.grid.MultiSegmentRegime regimeIn,
		final double dblPredictorOrdinate,
		final org.drip.math.segment.PredictorOrdinateResponseDerivative pordLeftSegmentRightEdge,
		final org.drip.math.segment.PredictorOrdinateResponseDerivative pordRightSegmentLeftEdge)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblPredictorOrdinate) || null == regimeIn ||
			regimeIn.isKnot (dblPredictorOrdinate) || null == pordLeftSegmentRightEdge || null ==
				pordRightSegmentLeftEdge)
			return null;

		org.drip.math.segment.PredictorResponse[] aSegIn = regimeIn.getSegments();

		int iOutSegmentIndex = 1;
		int iNumSegmentIn = aSegIn.length;
		double[] adblPredictorOrdinateOut = new double[iNumSegmentIn + 2];
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDOutLeft = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumSegmentIn + 1];
		org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDOutRight = new
			org.drip.math.segment.PredictorOrdinateResponseDerivative[iNumSegmentIn + 1];
		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBPOut = new
			org.drip.math.segment.PredictorResponseBuilderParams[iNumSegmentIn + 1];

		if (dblPredictorOrdinate < aSegIn[0].left() || dblPredictorOrdinate >
			aSegIn[iNumSegmentIn - 1].right())
			return null;

		adblPredictorOrdinateOut[0] = aSegIn[0].left();

		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBPIn = regimeIn.getSegmentBuilderParams();

		for (int i = 0; i < iNumSegmentIn; ++i) {
			aPRBPOut[iOutSegmentIndex - 1] = aPRBPIn[i];

			aPORDOutLeft[iOutSegmentIndex - 1] = regimeIn.calcPORD (aSegIn[i].left());

			if (dblPredictorOrdinate > aSegIn[i].left() && dblPredictorOrdinate < aSegIn[i].right()) {
				aPORDOutRight[iOutSegmentIndex - 1] = pordLeftSegmentRightEdge;
				adblPredictorOrdinateOut[iOutSegmentIndex++] = dblPredictorOrdinate;
				aPRBPOut[iOutSegmentIndex - 1] = aPRBPIn[i];
				aPORDOutLeft[iOutSegmentIndex - 1] = pordRightSegmentLeftEdge;
			}

			aPORDOutRight[iOutSegmentIndex - 1] = regimeIn.calcPORD (aSegIn[i].right());

			adblPredictorOrdinateOut[iOutSegmentIndex++] = aSegIn[i].right();
		}

		org.drip.math.grid.MultiSegmentRegime regimeOut =
			org.drip.math.grid.RegimeBuilder.CreateUncalibratedRegimeEstimator (regimeIn.name(),
				adblPredictorOrdinateOut, aPRBPOut);

		if (null == regimeOut) return null;

		return regimeOut.setupHermite (aPORDOutLeft, aPORDOutRight, null,
			org.drip.math.grid.RegimeCalibrationSetting.CALIBRATE) ? regimeOut : null;
	}

	/**
	 * Insert a Cardinal Knot into the specified Regime at the specified Predictor Ordinate Location
	 * 
	 * @param regimeIn Input Regime
	 * @param dblPredictorOrdinate Knot Predictor Ordinate
	 * @param dblCardinalTension Cardinal Tension Parameter
	 * 
	 * @return The Regime with the Knot inserted
	 */

	public static final org.drip.math.grid.MultiSegmentRegime InsertCardinalKnot (
		final org.drip.math.grid.MultiSegmentRegime regimeIn,
		final double dblPredictorOrdinate,
		final double dblCardinalTension)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblPredictorOrdinate) ||
			!org.drip.math.common.NumberUtil.IsValid (dblCardinalTension) || null == regimeIn ||
				regimeIn.isKnot (dblPredictorOrdinate))
			return null;

		org.drip.math.segment.PredictorResponse[] aSegIn = regimeIn.getSegments();

		int iOutSegmentIndex = 0;
		int iNumSegmentIn = aSegIn.length;

		if (dblPredictorOrdinate < aSegIn[0].left() || dblPredictorOrdinate >
			aSegIn[iNumSegmentIn - 1].right())
			return null;

		for (; iOutSegmentIndex < iNumSegmentIn; ++iOutSegmentIndex) {
			if (dblPredictorOrdinate > aSegIn[iOutSegmentIndex].left() && dblPredictorOrdinate <
				aSegIn[iOutSegmentIndex].right())
				break;
		}

		org.drip.math.segment.PredictorOrdinateResponseDerivative pordCardinalOut =
			org.drip.math.segment.PredictorOrdinateResponseDerivative.CardinalEdgeAggregate
				(regimeIn.calcPORD (aSegIn[iOutSegmentIndex].left()), regimeIn.calcPORD
					(aSegIn[iOutSegmentIndex].right()), dblCardinalTension);

		return null == pordCardinalOut ? null : InsertKnot (regimeIn, dblPredictorOrdinate, pordCardinalOut,
			pordCardinalOut);
	}

	/**
	 * Insert a Catmull-Rom Knot into the specified Regime at the specified Predictor Ordinate Location
	 * 
	 * @param regimeIn Input Regime
	 * @param dblPredictorOrdinate Knot Predictor Ordinate
	 * 
	 * @return The Regime with the Knot inserted
	 */

	public static final org.drip.math.grid.MultiSegmentRegime InsertCatmullRomKnot (
		final org.drip.math.grid.MultiSegmentRegime regimeIn,
		final double dblPredictorOrdinate)
	{
		return InsertCardinalKnot (regimeIn, dblPredictorOrdinate, 0.);
	}
}

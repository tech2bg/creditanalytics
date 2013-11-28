
package org.drip.spline.regime;

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
 * MultiSegmentSequenceModifier exports Regime modification/alteration methods to generate customized basis
 *  splines, with customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultiSegmentSequenceModifier {

	/**
	 * Insert the specified Predictor Ordinate Knot into the specified Regime, using the specified Response
	 * 	Value
	 * 
	 * @param mssIn Input Regime
	 * @param dblPredictorOrdinate Predictor Ordinate Knot
	 * @param dblResponseValue Response Value
	 * @param bs The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return The Regime with the Knot inserted
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence InsertKnot (
		final org.drip.spline.regime.MultiSegmentSequence mssIn,
		final double dblPredictorOrdinate,
		final double dblResponseValue,
		final org.drip.spline.regime.BoundarySettings bs,
		final int iCalibrationDetail)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblResponseValue) || null == mssIn || mssIn.isKnot
				(dblPredictorOrdinate))
			return null;

		org.drip.spline.segment.ConstitutiveState[] aSeg = mssIn.segments();

		int iNewIndex = 0;
		int iNumSegmentIn = aSeg.length;
		double[] adblResponseValue = new double[iNumSegmentIn + 2];
		double[] adblPredictorOrdinate = new double[iNumSegmentIn + 2];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBCOut = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumSegmentIn + 1];

		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBCIn = mssIn.segmentBuilderControl();

		if (dblPredictorOrdinate < aSeg[0].left()) {
			adblPredictorOrdinate[iNewIndex] = dblPredictorOrdinate;
			adblResponseValue[iNewIndex] = dblResponseValue;
			aSCBCOut[iNewIndex++] = aSCBCIn[0];
		}

		for (int i = 0; i < iNumSegmentIn; ++i) {
			aSCBCOut[iNewIndex] = aSCBCIn[i];

			adblPredictorOrdinate[iNewIndex] = aSeg[i].left();

			try {
				adblResponseValue[iNewIndex++] = mssIn.responseValue (aSeg[i].left());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (dblPredictorOrdinate > aSeg[i].left() && dblPredictorOrdinate < aSeg[i].right()) {
				adblPredictorOrdinate[iNewIndex] = dblPredictorOrdinate;
				adblResponseValue[iNewIndex] = dblResponseValue;
				aSCBCOut[iNewIndex++] = aSCBCIn[i];
			}
		}

		adblPredictorOrdinate[iNewIndex] = aSeg[iNumSegmentIn - 1].right();

		try {
			adblResponseValue[iNewIndex++] = mssIn.responseValue (aSeg[iNumSegmentIn - 1].right());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (dblPredictorOrdinate > aSeg[iNumSegmentIn - 1].right()) {
			adblResponseValue[iNewIndex] = dblResponseValue;
			adblPredictorOrdinate[iNewIndex] = dblPredictorOrdinate;
			aSCBCOut[aSCBCOut.length - 1] = aSCBCIn[aSCBCIn.length - 1];
		}

		return org.drip.spline.regime.MultiSegmentSequenceBuilder.CreateCalibratedRegimeEstimator
			(mssIn.name(), adblPredictorOrdinate, adblResponseValue, aSCBCOut, null, bs, iCalibrationDetail);
	}

	/**
	 * Append a Segment to the Right of the Specified Regime using the Supplied Constraint
	 * 
	 * @param mssIn Input Regime
	 * @param dblPredictorOrdinateAppendRight The Predictor Ordinate at the Right Edge of the Segment to be
	 * 	appended
	 * @param srvc The Segment Response Value Constraint
	 * @param scbc Segment Builder Parameters
	 * @param bs The Calibration Boundary Condition
	 * @param iCalibrationDetail The Calibration Detail
	 * 
	 * @return The Regime with the Segment Appended
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence AppendSegment (
		final org.drip.spline.regime.MultiSegmentSequence mssIn,
		final double dblPredictorOrdinateAppendRight,
		final org.drip.spline.params.SegmentResponseValueConstraint srvc,
		final org.drip.spline.params.SegmentCustomBuilderControl scbc,
		final org.drip.spline.regime.BoundarySettings bs,
		final int iCalibrationDetail)
	{
		if (null == mssIn || null == srvc || null == scbc || !org.drip.quant.common.NumberUtil.IsValid
			(dblPredictorOrdinateAppendRight))
			return null;

		double dblRegimePredictorOrdinateRight = mssIn.getRightPredictorOrdinateEdge();

		double[] adblConstraintOrdinate = srvc.predictorOrdinates();

		for (int i = 0; i < adblConstraintOrdinate.length; ++i) {
			if (adblConstraintOrdinate[i] <= dblRegimePredictorOrdinateRight) return null;
		}

		org.drip.spline.segment.ConstitutiveState[] aCS = mssIn.segments();

		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBCIn = mssIn.segmentBuilderControl();

		int iNumSegmentIn = aCS.length;
		double dblRegimeResponseValueLeft = java.lang.Double.NaN;
		double[] adblPredictorOrdinateOut = new double[iNumSegmentIn + 2];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBCOut = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumSegmentIn + 1];
		org.drip.spline.params.SegmentResponseValueConstraint[] aSRVCOut = new
			org.drip.spline.params.SegmentResponseValueConstraint[iNumSegmentIn + 1];

		try {
			dblRegimeResponseValueLeft = mssIn.responseValue (mssIn.getLeftPredictorOrdinateEdge());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumSegmentIn; ++i) {
			aSCBCOut[i] = aSCBCIn[i];

			adblPredictorOrdinateOut[i] = aCS[i].left();

			double dblPredictorOrdinateRight = aCS[i].right();

			try {
				aSRVCOut[i] = new org.drip.spline.params.SegmentResponseValueConstraint (new double[]
					{dblPredictorOrdinateRight}, new double[] {1.}, mssIn.responseValue
						(dblPredictorOrdinateRight));
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		aSRVCOut[iNumSegmentIn] = srvc;
		aSCBCOut[iNumSegmentIn] = scbc;
		adblPredictorOrdinateOut[iNumSegmentIn + 1] = dblPredictorOrdinateAppendRight;

		adblPredictorOrdinateOut[iNumSegmentIn] = aCS[iNumSegmentIn - 1].right();

		return org.drip.spline.regime.MultiSegmentSequenceBuilder.CreateCalibratedRegimeEstimator
			(mssIn.name(), adblPredictorOrdinateOut, dblRegimeResponseValueLeft, aSRVCOut, aSCBCOut, null,
				bs, iCalibrationDetail);
	}

	/**
	 * Insert the Predictor Ordinate Knot into the specified Regime
	 * 
	 * @param mssIn Input Regime
	 * @param dblPredictorOrdinate Knot Predictor Ordinate
	 * @param sprdLeftSegmentRightEdge Response Values for the Right Edge of the Left Segment
	 * @param sprdRightSegmentLeftEdge Response Values for the Left Edge of the Right segment
	 * 
	 * @return The Regime with the Predictor Ordinate Knot inserted
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence InsertKnot (
		final org.drip.spline.regime.MultiSegmentSequence mssIn,
		final double dblPredictorOrdinate,
		final org.drip.spline.params.SegmentPredictorResponseDerivative sprdLeftSegmentRightEdge,
		final org.drip.spline.params.SegmentPredictorResponseDerivative sprdRightSegmentLeftEdge)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate) || null == mssIn ||
			mssIn.isKnot (dblPredictorOrdinate) || null == sprdLeftSegmentRightEdge || null ==
				sprdRightSegmentLeftEdge)
			return null;

		org.drip.spline.segment.ConstitutiveState[] aCSIn = mssIn.segments();

		int iOutSegmentIndex = 1;
		int iNumSegmentIn = aCSIn.length;
		double[] adblPredictorOrdinateOut = new double[iNumSegmentIn + 2];
		org.drip.spline.params.SegmentPredictorResponseDerivative[] aSPRDOutLeft = new
			org.drip.spline.params.SegmentPredictorResponseDerivative[iNumSegmentIn + 1];
		org.drip.spline.params.SegmentPredictorResponseDerivative[] aSPRDOutRight = new
			org.drip.spline.params.SegmentPredictorResponseDerivative[iNumSegmentIn + 1];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBCOut = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumSegmentIn + 1];

		if (dblPredictorOrdinate < aCSIn[0].left() || dblPredictorOrdinate >
			aCSIn[iNumSegmentIn - 1].right())
			return null;

		adblPredictorOrdinateOut[0] = aCSIn[0].left();

		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBCIn = mssIn.segmentBuilderControl();

		for (int i = 0; i < iNumSegmentIn; ++i) {
			aSCBCOut[iOutSegmentIndex - 1] = aSCBCIn[i];

			aSPRDOutLeft[iOutSegmentIndex - 1] = mssIn.calcSPRD (aCSIn[i].left());

			if (dblPredictorOrdinate > aCSIn[i].left() && dblPredictorOrdinate < aCSIn[i].right()) {
				aSPRDOutRight[iOutSegmentIndex - 1] = sprdLeftSegmentRightEdge;
				adblPredictorOrdinateOut[iOutSegmentIndex++] = dblPredictorOrdinate;
				aSCBCOut[iOutSegmentIndex - 1] = aSCBCIn[i];
				aSPRDOutLeft[iOutSegmentIndex - 1] = sprdRightSegmentLeftEdge;
			}

			aSPRDOutRight[iOutSegmentIndex - 1] = mssIn.calcSPRD (aCSIn[i].right());

			adblPredictorOrdinateOut[iOutSegmentIndex++] = aCSIn[i].right();
		}

		org.drip.spline.regime.MultiSegmentSequence mssOut =
			org.drip.spline.regime.MultiSegmentSequenceBuilder.CreateUncalibratedRegimeEstimator
				(mssIn.name(), adblPredictorOrdinateOut, aSCBCOut);

		if (null == mssOut) return null;

		return mssOut.setupHermite (aSPRDOutLeft, aSPRDOutRight, null, null,
			org.drip.spline.regime.MultiSegmentSequence.CALIBRATE) ? mssOut : null;
	}

	/**
	 * Insert a Cardinal Knot into the specified Regime at the specified Predictor Ordinate Location
	 * 
	 * @param mssIn Input Regime
	 * @param dblPredictorOrdinate Knot Predictor Ordinate
	 * @param dblCardinalTension Cardinal Tension Parameter
	 * 
	 * @return The Regime with the Knot inserted
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence InsertCardinalKnot (
		final org.drip.spline.regime.MultiSegmentSequence mssIn,
		final double dblPredictorOrdinate,
		final double dblCardinalTension)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblCardinalTension) || null == mssIn || mssIn.isKnot
				(dblPredictorOrdinate))
			return null;

		org.drip.spline.segment.ConstitutiveState[] aCSIn = mssIn.segments();

		int iOutSegmentIndex = 0;
		int iNumSegmentIn = aCSIn.length;

		if (dblPredictorOrdinate < aCSIn[0].left() || dblPredictorOrdinate >
			aCSIn[iNumSegmentIn - 1].right())
			return null;

		for (; iOutSegmentIndex < iNumSegmentIn; ++iOutSegmentIndex) {
			if (dblPredictorOrdinate > aCSIn[iOutSegmentIndex].left() && dblPredictorOrdinate <
				aCSIn[iOutSegmentIndex].right())
				break;
		}

		org.drip.spline.params.SegmentPredictorResponseDerivative sprdCardinalOut =
			org.drip.spline.params.SegmentPredictorResponseDerivative.CardinalEdgeAggregate
				(mssIn.calcSPRD (aCSIn[iOutSegmentIndex].left()), mssIn.calcSPRD
					(aCSIn[iOutSegmentIndex].right()), dblCardinalTension);

		return null == sprdCardinalOut ? null : InsertKnot (mssIn, dblPredictorOrdinate, sprdCardinalOut,
			sprdCardinalOut);
	}

	/**
	 * Insert a Catmull-Rom Knot into the specified Regime at the specified Predictor Ordinate Location
	 * 
	 * @param mssIn Input Regime
	 * @param dblPredictorOrdinate Knot Predictor Ordinate
	 * 
	 * @return The Regime with the Knot inserted
	 */

	public static final org.drip.spline.regime.MultiSegmentSequence InsertCatmullRomKnot (
		final org.drip.spline.regime.MultiSegmentSequence mssIn,
		final double dblPredictorOrdinate)
	{
		return InsertCardinalKnot (mssIn, dblPredictorOrdinate, 0.);
	}
}

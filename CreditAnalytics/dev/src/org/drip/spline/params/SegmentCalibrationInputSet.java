
package org.drip.spline.params;

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
 * SegmentCalibrationInputSet holds the flexible set of fields needed for the calibration of the segment.
 * 	Specifically, it holds:
 * 	- The array of predictors and their corresponding responses.
 * 	- Left/Right node Ck derivatives for continuity transmission.
 * 	- Linear Constraints on the Combination of the Responses at their corresponding Predictor Ordinates.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SegmentCalibrationInputSet {
	private double[] _adblLeftDeriv = null;
	private double[] _adblRightDeriv = null;
	private double[] _adblResponseValue = null;
	private double[] _adblPredictorOrdinate = null;
	private org.drip.spline.params.SegmentBestFitResponse _sbfr = null;
	private org.drip.spline.params.SegmentResponseValueConstraint[] _aSRVC = null;

	/**
	 * SegmentCalibrationInputSet constructor
	 * 
	 * @param adblPredictorOrdinate Array of the Predictor Ordinates
	 * @param adblResponse ValueArray of the Corresponding Response Values
	 * @param adblLeftDeriv Array of the Left Derivative Values
	 * @param adblRightDeriv Array of the Right Derivative Values
	 * @param aSRVC Array of the Segment Response Value Constraints
	 * @param sbfr Segment Best Fit Weighted Response Values
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are not Valid
	 */

	public SegmentCalibrationInputSet (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final double[] adblLeftDeriv,
		final double[] adblRightDeriv,
		final org.drip.spline.params.SegmentResponseValueConstraint[] aSRVC,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
		throws java.lang.Exception
	{
		_sbfr = sbfr;
		_aSRVC = aSRVC;
		int iNumPredictorOrdinate = 0;
		_adblLeftDeriv = adblLeftDeriv;
		_adblRightDeriv = adblRightDeriv;
		_adblResponseValue = adblResponseValue;
		_adblPredictorOrdinate = adblPredictorOrdinate;

		if ((null == _adblPredictorOrdinate && null != _adblResponseValue) || (null != _adblPredictorOrdinate
			&& null == _adblResponseValue))
			throw new java.lang.Exception ("SegmentCalibrationInputSet ctr: Invalid Inputs!");

		if (null != _adblPredictorOrdinate) iNumPredictorOrdinate = _adblPredictorOrdinate.length;

		if (null != _adblResponseValue && iNumPredictorOrdinate != _adblResponseValue.length)
			throw new java.lang.Exception ("SegmentCalibrationInputSet ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Array of Predictor Ordinates
	 * 
	 * @return The Array of Predictor Ordinates
	 */

	public double[] predictorOrdinates()
	{
		return _adblPredictorOrdinate;
	}

	/**
	 * Retrieve the Array of Response Values
	 * 
	 * @return The Array of Response Values
	 */

	public double[] reponseValues()
	{
		return _adblResponseValue;
	}

	/**
	 * Retrieve the Array of the Segment Left Derivatives
	 * 
	 * @return The Array of the Segment Left Derivatives
	 */

	public double[] leftDeriv()
	{
		return _adblLeftDeriv;
	}

	/**
	 * Retrieve the Array of the Segment Right Derivatives
	 * 
	 * @return The Array of the Segment Right Derivatives
	 */

	public double[] rightDeriv()
	{
		return _adblRightDeriv;
	}

	/**
	 * Retrieve the Best Fit Weighted Response Values
	 * 
	 * @return The Best Fit Weighted Response Values
	 */

	public org.drip.spline.params.SegmentBestFitResponse bestFitWeightedResponse()
	{
		return _sbfr;
	}

	/**
	 * Generate the Array of Segment Constraint in terms of the local Predictor Ordinates and their
	 * 	corresponding Response Basis Function Realizations
	 * 
	 * @param aAUResponseBasis Array of the Response Basis Functions
	 * @param rssc Shape Controller
	 * @param ics Inelastics Transformer to convert the Predictor Ordinate Space to Local from Global
	 * 
	 * @return Array of the Segment Response Basis Constraints
	 */

	public org.drip.spline.params.SegmentIndexedBasisConstraint[] basisFunctionIndexConstraint (
		final org.drip.quant.function1D.AbstractUnivariate[] aAUResponseBasis,
		final org.drip.spline.params.ResponseScalingShapeControl rssc,
		final org.drip.spline.segment.InelasticConstitutiveState ics)
	{
		if (null == _aSRVC) return null;

		int iNumConstraint = _aSRVC.length;
		org.drip.spline.params.SegmentIndexedBasisConstraint[] aSIBC = new
			org.drip.spline.params.SegmentIndexedBasisConstraint[iNumConstraint];

		if (0 == iNumConstraint) return null;

		for (int i = 0; i < iNumConstraint; ++i) {
			if (null == _aSRVC[i] || null == (aSIBC[i] = _aSRVC[i].responseBasisIndexConstraint
				(aAUResponseBasis, rssc, ics)))
				return null;
		}

		return aSIBC;
	}

	/**
	 * Split the Segment Calibration Parameters across the Predictor Knots into an array containing the Left
	 *  and the Right Segment Calibration Parameters.
	 * 
	 * @param dblPredictor Predictor Knot
	 * @param dblResponse Response
	 * @param adblDeriv Derivatives array
	 * 
	 * @return Array containing the Left and the Right Segment Calibration Parameters.
	 */

	public SegmentCalibrationInputSet[] split (
		final double dblPredictor,
		final double dblResponse,
		final double[] adblDeriv)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictor) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblResponse))
			return null;

		double[] adblLeftResponse = null;
		double[] adblLeftPredictor = null;
		double[] adblRightResponse = null;
		double[] adblRightPredictor = null;
		org.drip.spline.params.SegmentResponseValueConstraint[] aSRVCLeft = null;
		org.drip.spline.params.SegmentResponseValueConstraint[] aSRVCRight = null;

		if (null == _adblPredictorOrdinate || 0 == _adblPredictorOrdinate.length) {
			adblLeftResponse = new double[] {dblResponse};
			adblRightResponse = new double[] {dblResponse};
			adblLeftPredictor = new double[] {dblPredictor};
			adblRightPredictor = new double[] {dblPredictor};
		} else {
			boolean bSplitPredictorOrdinateAdded = false;

			java.util.List<java.lang.Double> lsLeftPredictorOrdinate = new
				java.util.ArrayList<java.lang.Double>();

			java.util.List<java.lang.Double> lsRightPredictorOrdinate = new
				java.util.ArrayList<java.lang.Double>();

			java.util.List<java.lang.Double> lsLeftResponse = new java.util.ArrayList<java.lang.Double>();

			java.util.List<java.lang.Double> lsRightResponse = new java.util.ArrayList<java.lang.Double>();

			for (int i = 0; i < _adblPredictorOrdinate.length; ++i) {
				if (_adblPredictorOrdinate[i] < dblPredictor) {
					lsLeftPredictorOrdinate.add (_adblPredictorOrdinate[i]);

					lsLeftResponse.add (_adblResponseValue[i]);
				} else {
					if (!bSplitPredictorOrdinateAdded) {
						lsLeftPredictorOrdinate.add (dblPredictor);

						lsLeftResponse.add (dblResponse);

						lsRightPredictorOrdinate.add (dblPredictor);

						lsRightResponse.add (dblResponse);

						bSplitPredictorOrdinateAdded = true;
					}

					lsRightPredictorOrdinate.add (_adblPredictorOrdinate[i]);

					lsRightResponse.add (_adblResponseValue[i]);
				}
			}

			int iNumLeftPredictorOrdinate = lsLeftPredictorOrdinate.size();

			if (0 != iNumLeftPredictorOrdinate) {
				adblLeftResponse = new double[iNumLeftPredictorOrdinate];
				adblLeftPredictor = new double[iNumLeftPredictorOrdinate];

				for (int i = 0; i < iNumLeftPredictorOrdinate; ++i) {
					adblLeftPredictor[i] = lsLeftPredictorOrdinate.get (i);

					adblLeftResponse[i] = lsLeftResponse.get (i);
				}
			}

			int iNumRightPredictorOrdinate = lsRightPredictorOrdinate.size();

			if (0 != iNumRightPredictorOrdinate) {
				adblRightResponse = new double[iNumRightPredictorOrdinate];
				adblRightPredictor = new double[iNumRightPredictorOrdinate];

				for (int i = 0; i < iNumRightPredictorOrdinate; ++i) {
					adblRightPredictor[i] = lsRightPredictorOrdinate.get (i);

					adblRightResponse[i] = lsRightResponse.get (i);
				}
			}
		}

		if (null != _aSRVC && 0 != _aSRVC.length) {
			java.util.List<org.drip.spline.params.SegmentResponseValueConstraint> lsSPRCLeft = new
				java.util.ArrayList<org.drip.spline.params.SegmentResponseValueConstraint>();

			java.util.List<org.drip.spline.params.SegmentResponseValueConstraint> lsSPRCRight = new
				java.util.ArrayList<org.drip.spline.params.SegmentResponseValueConstraint>();

			for (org.drip.spline.params.SegmentResponseValueConstraint rvc : _aSRVC) {
				if (null == rvc) return null;

				try {
					if (org.drip.spline.params.SegmentResponseValueConstraint.RIGHT_OF_CONSTRAINT ==
						rvc.knotPosition (dblPredictor))
						lsSPRCLeft.add (rvc);
					else if (org.drip.spline.params.SegmentResponseValueConstraint.LEFT_OF_CONSTRAINT ==
						rvc.knotPosition (dblPredictor))
						lsSPRCRight.add (rvc);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			int iNumLeftConstraint = lsSPRCLeft.size();

			if (0 != iNumLeftConstraint) {
				aSRVCLeft = new org.drip.spline.params.SegmentResponseValueConstraint[iNumLeftConstraint];

				for (int i = 0; i < iNumLeftConstraint; ++i)
					aSRVCLeft[i] = lsSPRCLeft.get (i);
			}

			int iNumRightConstraint = lsSPRCRight.size();

			if (0 != iNumRightConstraint) {
				aSRVCRight = new
					org.drip.spline.params.SegmentResponseValueConstraint[iNumRightConstraint];

				for (int i = 0; i < iNumRightConstraint; ++i)
					aSRVCRight[i] = lsSPRCRight.get (i);
			}
		}

		try {
			return new SegmentCalibrationInputSet[] {new SegmentCalibrationInputSet (adblLeftPredictor,
				adblLeftResponse, _adblLeftDeriv, adblDeriv, aSRVCLeft, _sbfr), new
					SegmentCalibrationInputSet (adblRightPredictor, adblRightResponse, adblDeriv,
						_adblRightDeriv, aSRVCRight, _sbfr)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}


package org.drip.math.segment;

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
 * CalibrationParams holds the flexible set of fields needed for the calibration of the segment.
 * 	Specifically, it holds:
 * 	- The array of predictors and their corresponding responses.
 * 	- Left/Right node Ck derivatives for continuity transmission.
 * 	- Linear Constraints on the Combination of the Responses at their corresponding Predictor Ordinates.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CalibrationParams {
	private double[] _adblResponse = null;
	private double[] _adblLeftDeriv = null;
	private double[] _adblRightDeriv = null;
	private double[] _adblPredictorOrdinate = null;
	private org.drip.math.segment.ResponseValueConstraint[] _aRVC = null;

	/**
	 * CalibrationParams constructor
	 * 
	 * @param adblPredictorOrdinate Array of the Predictor Ordinates
	 * @param adblResponse Array of the Corresponding Responses
	 * @param adblLeftDeriv Array of the Left Derivative Values
	 * @param adblRightDeriv Array of the Right Derivative Values
	 * @param aRVC Array of the Response Value Constraints
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are not Valid
	 */

	public CalibrationParams (
		final double[] adblPredictorOrdinate,
		final double[] adblResponse,
		final double[] adblLeftDeriv,
		final double[] adblRightDeriv,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC)
		throws java.lang.Exception
	{
		_aRVC = aRVC;
		int iNumPredictorOrdinate = 0;
		_adblResponse = adblResponse;
		_adblLeftDeriv = adblLeftDeriv;
		_adblRightDeriv = adblRightDeriv;
		_adblPredictorOrdinate = adblPredictorOrdinate;

		if ((null == _adblPredictorOrdinate && null != _adblResponse) || (null != _adblPredictorOrdinate &&
			null == _adblResponse))
			throw new java.lang.Exception ("CalibrationParams ctr: Invalid Inputs!");

		if (null != _adblPredictorOrdinate) iNumPredictorOrdinate = _adblPredictorOrdinate.length;

		if (null != _adblResponse && iNumPredictorOrdinate != _adblResponse.length)
			throw new java.lang.Exception ("CalibrationParams ctr: Invalid Inputs!");
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
	 * Retrieve the Array of Responses
	 * 
	 * @return The Array of Responses
	 */

	public double[] reponses()
	{
		return _adblResponse;
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
	 * Generate the Array of Segment Constraint in terms of the local Predictor Ordinates and their
	 * 	corresponding Response Basis Function Realizations
	 * 
	 * @param aAUResponseBasis Array of the Response Basis Functions
	 * @param inel Inelastics Transformer to convert the Predictor Ordinate Space to Local from Global
	 * 
	 * @return Array of the Segment Response Basis Constraints
	 */

	public org.drip.math.segment.ResponseBasisConstraint[] getBasisFunctionConstraint (
		final org.drip.math.function.AbstractUnivariate[] aAUResponseBasis,
		final org.drip.math.segment.Inelastics inel)
	{
		if (null == _aRVC) return null;

		int iNumConstraint = _aRVC.length;
		org.drip.math.segment.ResponseBasisConstraint[] aSRBC = new
			org.drip.math.segment.ResponseBasisConstraint[iNumConstraint];

		if (0 == iNumConstraint) return null;

		for (int i = 0; i < iNumConstraint; ++i) {
			if (null == _aRVC[i] || null == (aSRBC[i] = _aRVC[i].responseBasisConstraint (aAUResponseBasis,
				inel)))
				return null;
		}

		return aSRBC;
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

	public CalibrationParams[] split (
		final double dblPredictor,
		final double dblResponse,
		final double[] adblDeriv)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblPredictor) ||
			!org.drip.math.common.NumberUtil.IsValid (dblResponse))
			return null;

		double[] adblLeftResponse = null;
		double[] adblLeftPredictor = null;
		double[] adblRightResponse = null;
		double[] adblRightPredictor = null;
		org.drip.math.segment.ResponseValueConstraint[] aSPRCLeft = null;
		org.drip.math.segment.ResponseValueConstraint[] aSPRCRight = null;

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

					lsLeftResponse.add (_adblResponse[i]);
				} else {
					if (!bSplitPredictorOrdinateAdded) {
						lsLeftPredictorOrdinate.add (dblPredictor);

						lsLeftResponse.add (dblResponse);

						lsRightPredictorOrdinate.add (dblPredictor);

						lsRightResponse.add (dblResponse);

						bSplitPredictorOrdinateAdded = true;
					}

					lsRightPredictorOrdinate.add (_adblPredictorOrdinate[i]);

					lsRightResponse.add (_adblResponse[i]);
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

		if (null != _aRVC && 0 != _aRVC.length) {
			java.util.List<org.drip.math.segment.ResponseValueConstraint> lsSPRCLeft = new
				java.util.ArrayList<org.drip.math.segment.ResponseValueConstraint>();

			java.util.List<org.drip.math.segment.ResponseValueConstraint> lsSPRCRight = new
				java.util.ArrayList<org.drip.math.segment.ResponseValueConstraint>();

			for (org.drip.math.segment.ResponseValueConstraint rvc : _aRVC) {
				if (null == rvc) return null;

				try {
					if (org.drip.math.segment.ResponseValueConstraint.RIGHT_OF_CONSTRAINT ==
						rvc.knotPosition (dblPredictor))
						lsSPRCLeft.add (rvc);
					else if (org.drip.math.segment.ResponseValueConstraint.LEFT_OF_CONSTRAINT ==
						rvc.knotPosition (dblPredictor))
						lsSPRCRight.add (rvc);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			int iNumLeftConstraint = lsSPRCLeft.size();

			if (0 != iNumLeftConstraint) {
				aSPRCLeft = new org.drip.math.segment.ResponseValueConstraint[iNumLeftConstraint];

				for (int i = 0; i < iNumLeftConstraint; ++i)
					aSPRCLeft[i] = lsSPRCLeft.get (i);
			}

			int iNumRightConstraint = lsSPRCRight.size();

			if (0 != iNumRightConstraint) {
				aSPRCRight = new
					org.drip.math.segment.ResponseValueConstraint[iNumRightConstraint];

				for (int i = 0; i < iNumRightConstraint; ++i)
					aSPRCRight[i] = lsSPRCRight.get (i);
			}
		}

		try {
			return new CalibrationParams[] {new CalibrationParams (adblLeftPredictor, adblLeftResponse,
				_adblLeftDeriv, adblDeriv, aSPRCLeft), new CalibrationParams (adblRightPredictor,
					adblRightResponse, adblDeriv, _adblRightDeriv, aSPRCRight)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

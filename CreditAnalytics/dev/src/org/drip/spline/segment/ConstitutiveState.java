
package org.drip.spline.segment;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
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
 * ConstitutiveState implements the single segment basis calibration and inference functionality. It exports
 * 	the following functionality:
 * 	- Build the ConstitutiveState instance from the Basis Function/Shape Controller Set.
 * 	- Build the ConstitutiveState instance from the Basis Evaluator Set.
 * 	- Retrieve the Number of Parameters, Basis Evaluator, Array of the Response Basis Coefficients, and
 * 		Segment Design Inelastic Control.
 * 	- Calibrate the Segment State from the Calibration Parameter Set.
 * 	- Sensitivity Calibrator: Calibrate the Segment Quote Jacobian from the Calibration Parameter Set.
 * 	- Calibrate the coefficients from the prior Predictor/Response Segment, the Constraint, and fitness
 *		Weights
 *	- Calibrate the coefficients from the prior Segment and the Response Value at the Right Predictor
 *		Ordinate.
 *	- Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Slope.
 *	- Calibrate the coefficients from the Left Edge Response Value Constraint, the Left Edge Response Value
 *		Slope, and the Right Edge Response Value Constraint.
 *	- Retrieve the Segment Curvature, Length, and the Best Fit DPE.
 *	- Calculate the Response Value and its Derivative at the given Predictor Ordinate.
 *	- Calculate the Ordered Derivative of the Coefficient to the Quote.
 *	- Calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Inputs.
 *	- Calculate the Jacobian of the Response to the Edge Inputs at the given Predictor Ordinate.
 *	- Calculate the Jacobian of the Response to the Basis Coefficients at the given Predictor Ordinate.
 *	- Calibrate the segment and calculate the Jacobian of the Segment's Response Basis Function Coefficients
 *		to the Edge Parameters.
 *	- Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Value Slope and
 *		calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters.
 *	- Calibrate the coefficients from the prior Segment and the Response Value at the Right Predictor
 *		Ordinate and calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge
 *  	Parameters.
 *  - Indicate whether the given segment is monotone. If monotone, may optionally indicate the nature of the
 *  	extrema contained inside (maxima/minima/infection).
 *  - Clip the part of the Segment to the Right of the specified Predictor Ordinate. Retain all other
 *  	constraints the same.
 *  - Clip the part of the Segment to the Left of the specified Predictor Ordinate. Retain all other
 *  	constraints the same.
 *  - Display the string representation for diagnostic purposes.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ConstitutiveState extends org.drip.spline.segment.InelasticConstitutiveState {

	/**
	 * LEFT NODE VALUE PARAMETER INDEX
	 */

	public static final int LEFT_NODE_VALUE_PARAMETER_INDEX = 0;

	/**
	 * RIGHT NODE VALUE PARAMETER INDEX
	 */

	public static final int RIGHT_NODE_VALUE_PARAMETER_INDEX = 1;

	private double[] _adblResponseBasisCoeff = null;
	private double[] _adblDBasisCoeffDLocalQuote = null;
	private double[] _adblDBasisCoeffDPreceedingQuote = null;
	private org.drip.spline.segment.BasisEvaluator _be = null;
	private double[][] _aadblDResponseBasisCoeffDConstraint = null;
	private double _dblDResponseDPreceedingQuote = java.lang.Double.NaN;
	private org.drip.spline.params.SegmentDesignInelasticControl _sdic = null;
	private org.drip.spline.params.PreceedingQuoteSensitivityControl _pqsc = null;
	private org.drip.quant.calculus.WengertJacobian _wjDBasisCoeffDEdgeValue = null;

	/**
	 * Build the ConstitutiveState instance from the Basis Function/Shape Controller Set
	 * 
	 * @param dblLeftPredictorOrdinate Left Predictor Ordinate
	 * @param dblRightPredictorOrdinate Right Predictor Ordinate
	 * @param fs Response Basis Function Set
	 * @param rssc Shape Controller
	 * @param sdic Design Inelastic Parameters
	 * @param pqsc Preceeding Quote Sensitivity Control Parameters
	 * 
	 * @return Instance of ConstitutiveState
	 */

	public static final org.drip.spline.segment.ConstitutiveState Create (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.spline.basis.FunctionSet fs,
		final org.drip.spline.params.ResponseScalingShapeControl rssc,
		final org.drip.spline.params.SegmentDesignInelasticControl sdic,
		final org.drip.spline.params.PreceedingQuoteSensitivityControl pqsc)
	{
		try {
			org.drip.spline.segment.SegmentBasisEvaluator sbe = new
				org.drip.spline.segment.SegmentBasisEvaluator (fs, rssc);

			org.drip.spline.segment.ConstitutiveState cs = new org.drip.spline.segment.ConstitutiveState
				(dblLeftPredictorOrdinate, dblRightPredictorOrdinate, sbe, sdic, pqsc);

			if (null != pqsc) {
				org.drip.spline.segment.BasisEvaluator sbePQSC = pqsc.basisEvaluator();

				if (null != sbePQSC && !sbePQSC.setContainingInelastics (cs)) return null;
			}

			return sbe.setContainingInelastics (cs) ? cs : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Build the ConstitutiveState instance from the Basis Evaluator Set
	 * 
	 * @param dblLeftPredictorOrdinate Left Predictor Ordinate
	 * @param dblRightPredictorOrdinate Right Predictor Ordinate
	 * @param be Basis Evaluator
	 * @param sdic Design Inelastic Parameters
	 * @param pqsc Preceeding Quote Sensitivity Control Parameters
	 * 
	 * @return Instance of ConstitutiveState
	 */

	public static final org.drip.spline.segment.ConstitutiveState Create (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.spline.segment.BasisEvaluator be,
		final org.drip.spline.params.SegmentDesignInelasticControl sdic,
		final org.drip.spline.params.PreceedingQuoteSensitivityControl pqsc)
	{
		try {
			org.drip.spline.segment.ConstitutiveState cs = new org.drip.spline.segment.ConstitutiveState
				(dblLeftPredictorOrdinate, dblRightPredictorOrdinate, be, sdic, pqsc);

			return be.setContainingInelastics (cs) ? cs : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private ConstitutiveState (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.spline.segment.BasisEvaluator be,
		final org.drip.spline.params.SegmentDesignInelasticControl sdic,
		final org.drip.spline.params.PreceedingQuoteSensitivityControl pqsc)
		throws java.lang.Exception
	{
		super (dblLeftPredictorOrdinate, dblRightPredictorOrdinate);

		if (null == (_be = be) || null == (_sdic = sdic))
			throw new java.lang.Exception ("ConstitutiveState ctr: Invalid Basis Functions!");

		int iNumBasis = _be.numBasis();

		_adblResponseBasisCoeff = new double[iNumBasis];

		if (0 >= iNumBasis || _sdic.getCk() > iNumBasis - 2)
			throw new java.lang.Exception ("ConstitutiveState ctr: Invalid inputs!");

		if (null == (_pqsc = pqsc))
			_pqsc = new org.drip.spline.params.PreceedingQuoteSensitivityControl (true, 0, null);
	}

	private double[] basisDResponseDBasisCoeff (
		final double dblPredictorOrdinate,
		final int iOrder)
	{
		if (0 == iOrder) return null;

		int iNumBasis = _be.numBasis();

		double[] adblDResponseDBasisCoeff = new double[iNumBasis];

		for (int i = 0; i < iNumBasis; ++i) {
			try {
				adblDResponseDBasisCoeff[i] = 1 == iOrder ? _be.shapedBasisFunctionResponse
					(dblPredictorOrdinate, i) : 0.;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return adblDResponseDBasisCoeff;
	}

	private double[] transmissionCk (
		final double dblPredictorOrdinate,
		final org.drip.spline.segment.ConstitutiveState csPrev,
		final int iCk)
	{
		double[] adblDeriv = new double[iCk];

		for (int i = 0; i < iCk; ++i) {
			try {
				adblDeriv[i] = csPrev.calcResponseValueDerivative (dblPredictorOrdinate, i + 1);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return adblDeriv;
	}

	private double[] CkDBasisCoeffDPreceedingQuoteTail()
	{
		int iCk = _pqsc.Ck();

		if (0 == iCk) return null;

		double[] adblDBasisCoeffDPreceedingQuoteTail = new double[iCk];

		for (int i = 0; i < iCk; ++i)
			adblDBasisCoeffDPreceedingQuoteTail[i] = 0.;

		return adblDBasisCoeffDPreceedingQuoteTail;
	}

	/**
	 * Retrieve the Number of Parameters
	 * 
	 * @return The Number of Parameters
	 */

	public int numParameters()
	{
		return _sdic.getCk() + 2;
	}

	/**
	 * Retrieve the Basis Evaluator
	 * 
	 * @return The Basis Evaluator
	 */

	public org.drip.spline.segment.BasisEvaluator basisEvaluator()
	{
		return _be;
	}

	/**
	 * Retrieve the Array of Response Basis Coefficients
	 * 
	 * @return The Array of Response Basis Coefficients
	 */

	public double[] responseBasisCoefficient()
	{
		return _adblResponseBasisCoeff;
	}

	/**
	 * Retrieve the Segment Design Inelastic Control
	 * 
	 * @return The Segment Design Inelastic Control
	 */

	public org.drip.spline.params.SegmentDesignInelasticControl designControl()
	{
		return _sdic;
	}

	/**
	 * Main Calibrator: Calibrate the Segment State from the Calibration Parameter Set
	 * 
	 * @param sscState The Segment State Calibration Parameter Set
	 * 
	 * @return TRUE => Calibration Successful
	 */

	public boolean calibrateState (
		final org.drip.spline.params.SegmentStateCalibration sscState)
	{
		if (null == sscState) return false;

		double[] adblPredictorOrdinate = sscState.predictorOrdinates();

		double[] adblResponseValue = sscState.responseValues();

		double[] adblLeftEdgeDeriv = sscState.leftEdgeDeriv();

		double[] adblRightEdgeDeriv = sscState.rightEdgeDeriv();

		org.drip.spline.params.SegmentBestFitResponse sbfr = sscState.bestFitResponse();

		org.drip.spline.params.SegmentBasisFlexureConstraint[] aSBFC = sscState.flexureConstraint();

		int iNumConstraint = null == aSBFC ? 0 : aSBFC.length;
		int iNumResponseBasisCoeff = _adblResponseBasisCoeff.length;
		int iNumLeftDeriv = null == adblLeftEdgeDeriv ? 0 : adblLeftEdgeDeriv.length;
		int iNumRightDeriv = null == adblRightEdgeDeriv ? 0 : adblRightEdgeDeriv.length;
		double[] adblPredictorResponseConstraintValue = new double[iNumResponseBasisCoeff];
		double[][] aadblResponseBasisCoeffConstraint = new
			double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];
		int iNumPredictorOrdinate = null == adblPredictorOrdinate ? 0 : adblPredictorOrdinate.length;

		if (iNumResponseBasisCoeff < iNumPredictorOrdinate + iNumLeftDeriv + iNumRightDeriv + iNumConstraint)
			return false;

		try {
			org.drip.spline.segment.BestFitFlexurePenalizer bffp = new
				org.drip.spline.segment.BestFitFlexurePenalizer (this, _sdic.curvaturePenaltyControl(),
					_sdic.lengthPenaltyControl(), sbfr, _be);

			for (int j = 0; j < iNumResponseBasisCoeff; ++j) {
				if (j < iNumPredictorOrdinate)
					adblPredictorResponseConstraintValue[j] = adblResponseValue[j];
				else if (j < iNumPredictorOrdinate + iNumConstraint)
					adblPredictorResponseConstraintValue[j] = aSBFC[j -
					    iNumPredictorOrdinate].contraintValue();
				else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv)
					adblPredictorResponseConstraintValue[j] = adblLeftEdgeDeriv[j - iNumPredictorOrdinate -
					    iNumConstraint];
				else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv + iNumRightDeriv)
					adblPredictorResponseConstraintValue[j] = adblRightEdgeDeriv[j - iNumPredictorOrdinate -
					    iNumConstraint - iNumLeftDeriv];
				else
					adblPredictorResponseConstraintValue[j] = bffp.basisPairPenaltyConstraint (j);
			}

			for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
				for (int l = 0; l < iNumResponseBasisCoeff; ++l) {
					double[] adblCalibBasisConstraintWeight = null;

					if (0 != iNumConstraint && (l >= iNumPredictorOrdinate && l < iNumPredictorOrdinate +
						iNumConstraint))
						adblCalibBasisConstraintWeight = aSBFC[l -
						    iNumPredictorOrdinate].responseBasisCoeffWeights();

					if (l < iNumPredictorOrdinate)
						aadblResponseBasisCoeffConstraint[l][i] = _be.shapedBasisFunctionResponse
							(adblPredictorOrdinate[l], i);
					else if (l < iNumPredictorOrdinate + iNumConstraint)
						aadblResponseBasisCoeffConstraint[l][i] = adblCalibBasisConstraintWeight[i];
					else if (l < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv)
						aadblResponseBasisCoeffConstraint[l][i] = _be.shapedBasisFunctionDerivative (left(),
							l - iNumPredictorOrdinate - iNumConstraint + 1, i);
					else if (l < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv + iNumRightDeriv)
						aadblResponseBasisCoeffConstraint[l][i] = _be.shapedBasisFunctionDerivative
							(right(), l - iNumPredictorOrdinate - iNumConstraint - iNumLeftDeriv + 1, i);
					else
						aadblResponseBasisCoeffConstraint[l][i] = bffp.basisPairConstraintCoefficient (i, l);
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		org.drip.quant.linearalgebra.LinearizationOutput lo =
			org.drip.quant.linearalgebra.LinearSystemSolver.SolveUsingMatrixInversion
				(aadblResponseBasisCoeffConstraint, adblPredictorResponseConstraintValue);

		if (null == lo) return false;

		double[] adblCalibResponseBasisCoeff = lo.getTransformedRHS();

		if (null == adblCalibResponseBasisCoeff || adblCalibResponseBasisCoeff.length !=
			iNumResponseBasisCoeff || null == (_aadblDResponseBasisCoeffDConstraint =
				lo.getTransformedMatrix()) || _aadblDResponseBasisCoeffDConstraint.length !=
					iNumResponseBasisCoeff || _aadblDResponseBasisCoeffDConstraint[0].length !=
						iNumResponseBasisCoeff)
			return false;

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (_adblResponseBasisCoeff[i] =
				adblCalibResponseBasisCoeff[i]))
				return false;
		}

		return true;
	}

	/**
	 * Sensitivity Calibrator: Calibrate the Segment Quote Jacobian from the Calibration Parameter Set
	 * 
	 * @param sscQuoteSensitivity The Segment Quote Calibration Parameter Sensitivity
	 * @param aSBFCState Array of Segment State Basis Flexure Constraints
	 * 
	 * @return The Quote Sensitivity Jacobian
	 */

	public double[] calibrateQuoteJacobian (
		final org.drip.spline.params.SegmentStateCalibration sscQuoteSensitivity,
		final org.drip.spline.params.SegmentBasisFlexureConstraint[] aSBFCState)
	{
		if (null == sscQuoteSensitivity) return null;

		double[] adblPredictorOrdinate = sscQuoteSensitivity.predictorOrdinates();

		double[] adblResponseValueQuoteSensitivity = sscQuoteSensitivity.responseValues();

		double[] adblLeftEdgeDerivQuoteSensitivity = sscQuoteSensitivity.leftEdgeDeriv();

		double[] adblRightEdgeDerivQuoteSensitivity = sscQuoteSensitivity.rightEdgeDeriv();

		org.drip.spline.params.SegmentBestFitResponse sbfrQuoteSensitivity =
			sscQuoteSensitivity.bestFitResponse();

		org.drip.spline.params.SegmentBasisFlexureConstraint[] aSBFCQuoteSensitivity =
			sscQuoteSensitivity.flexureConstraint();

		int iNumResponseBasisCoeff = _adblResponseBasisCoeff.length;
		int iNumConstraint = null == aSBFCQuoteSensitivity ? 0 : aSBFCQuoteSensitivity.length;
		int iNumPredictorOrdinate = null == adblPredictorOrdinate ? 0 : adblPredictorOrdinate.length;
		double[] adblPredictorResponseQuoteSensitivityConstraint = new double[iNumResponseBasisCoeff];
		int iNumLeftDerivQuoteSensitivity = null == adblLeftEdgeDerivQuoteSensitivity ? 0 :
			adblLeftEdgeDerivQuoteSensitivity.length;
		int iNumRightDerivQuoteSensitivity = null == adblRightEdgeDerivQuoteSensitivity ? 0 :
			adblRightEdgeDerivQuoteSensitivity.length;
		double[][] aadblResponseCoeffConstraintQuoteSensitivity = new
			double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];

		if ((null == aSBFCState && 0 != iNumConstraint) || (null != aSBFCState && iNumConstraint !=
			aSBFCState.length) || null == _be)
			return null;

		if (iNumResponseBasisCoeff < iNumPredictorOrdinate + iNumLeftDerivQuoteSensitivity +
			iNumRightDerivQuoteSensitivity + iNumConstraint)
			return null;

		try {
			org.drip.spline.segment.BestFitFlexurePenalizer bffpQuoteSensitivity = new
				org.drip.spline.segment.BestFitFlexurePenalizer (this, null == _sdic ? null :
					_sdic.curvaturePenaltyControl(), null == _sdic ? null : _sdic.lengthPenaltyControl(),
						sbfrQuoteSensitivity, _be);

			for (int j = 0; j < iNumResponseBasisCoeff; ++j) {
				if (j < iNumPredictorOrdinate)
					adblPredictorResponseQuoteSensitivityConstraint[j] =
						adblResponseValueQuoteSensitivity[j];
				else if (j < iNumPredictorOrdinate + iNumConstraint) {
					adblPredictorResponseQuoteSensitivityConstraint[j] = 0.;
					org.drip.spline.params.SegmentBasisFlexureConstraint sbfcQuoteSensitivity =
						aSBFCQuoteSensitivity[j - iNumPredictorOrdinate];

					if (null != sbfcQuoteSensitivity) {
						adblPredictorResponseQuoteSensitivityConstraint[j] =
							sbfcQuoteSensitivity.contraintValue();

						double[] adblCalibConstraintWeightQuoteSensitivity =
							sbfcQuoteSensitivity.responseBasisCoeffWeights();

						for (int i = 0; i < iNumResponseBasisCoeff; ++i)
							adblPredictorResponseQuoteSensitivityConstraint[j] -= _adblResponseBasisCoeff[i]
								* adblCalibConstraintWeightQuoteSensitivity[i];
					}
				} else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDerivQuoteSensitivity)
					adblPredictorResponseQuoteSensitivityConstraint[j] = adblLeftEdgeDerivQuoteSensitivity[j
					    - iNumPredictorOrdinate - iNumConstraint];
				else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDerivQuoteSensitivity +
					iNumRightDerivQuoteSensitivity)
					adblPredictorResponseQuoteSensitivityConstraint[j] = adblRightEdgeDerivQuoteSensitivity[j
					    - iNumPredictorOrdinate - iNumConstraint - iNumLeftDerivQuoteSensitivity];
				else
					adblPredictorResponseQuoteSensitivityConstraint[j] =
						bffpQuoteSensitivity.basisPairPenaltyConstraint (j);
			}

			for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
				for (int l = 0; l < iNumResponseBasisCoeff; ++l) {
					double[] adblCalibBasisConstraintWeight = null;

					if (0 != iNumConstraint && (l >= iNumPredictorOrdinate && l < iNumPredictorOrdinate +
						iNumConstraint))
						adblCalibBasisConstraintWeight = aSBFCState[l -
						    iNumPredictorOrdinate].responseBasisCoeffWeights();

					if (l < iNumPredictorOrdinate)
						aadblResponseCoeffConstraintQuoteSensitivity[l][i] = _be.shapedBasisFunctionResponse
							(adblPredictorOrdinate[l], i);
					else if (l < iNumPredictorOrdinate + iNumConstraint)
						aadblResponseCoeffConstraintQuoteSensitivity[l][i] =
							adblCalibBasisConstraintWeight[i];
					else if (l < iNumPredictorOrdinate + iNumConstraint + iNumLeftDerivQuoteSensitivity)
						aadblResponseCoeffConstraintQuoteSensitivity[l][i] =
							_be.shapedBasisFunctionDerivative (left(), l - iNumPredictorOrdinate -
								iNumConstraint + 1, i);
					else if (l < iNumPredictorOrdinate + iNumConstraint + iNumLeftDerivQuoteSensitivity +
						iNumRightDerivQuoteSensitivity)
						aadblResponseCoeffConstraintQuoteSensitivity[l][i] =
							_be.shapedBasisFunctionDerivative (right(), l - iNumPredictorOrdinate -
								iNumConstraint - iNumLeftDerivQuoteSensitivity + 1, i);
					else
						aadblResponseCoeffConstraintQuoteSensitivity[l][i] =
							bffpQuoteSensitivity.basisPairConstraintCoefficient (i, l);
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.quant.linearalgebra.LinearizationOutput lo =
			org.drip.quant.linearalgebra.LinearSystemSolver.SolveUsingMatrixInversion
				(aadblResponseCoeffConstraintQuoteSensitivity,
					adblPredictorResponseQuoteSensitivityConstraint);

		return null == lo ? null : lo.getTransformedRHS();
	}

	/**
	 * Sensitivity Calibrator: Calibrate the Segment Local Quote Jacobian from the Calibration Parameter Set
	 * 
	 * @param sscQuoteSensitivity The Segment Quote Calibration Parameter Sensitivity
	 * @param aSBFCState Array of Segment State Basis Flexure Constraints
	 * 
	 * @return TRUE => Local Quote Sensitivity Calibration Successful
	 */

	public boolean calibrateLocalQuoteJacobian (
		final org.drip.spline.params.SegmentStateCalibration sscQuoteSensitivity,
		final org.drip.spline.params.SegmentBasisFlexureConstraint[] aSBFCState)
	{
		if (null == (_adblDBasisCoeffDLocalQuote = calibrateQuoteJacobian (sscQuoteSensitivity, aSBFCState))
			|| _adblDBasisCoeffDLocalQuote.length != _adblResponseBasisCoeff.length)
			return false;

		return true;
	}

	/**
	 * Sensitivity Calibrator: Calibrate the Segment Preceeding Quote Jacobian from the Calibration Parameter Set
	 * 
	 * @param sscPreceedingQuoteSensitivity The Segment Preceeding Quote Calibration Parameter Sensitivity
	 * 
	 * @return TRUE => Preceeding Quote Sensitivity Calibration Successful
	 */

	public boolean calibratePreceedingQuoteJacobian (
		final org.drip.spline.params.SegmentStateCalibration sscPreceedingQuoteSensitivity)
	{
		if (null == (_adblDBasisCoeffDPreceedingQuote = calibrateQuoteJacobian (sscPreceedingQuoteSensitivity,
			null)) || _adblDBasisCoeffDPreceedingQuote.length != _adblResponseBasisCoeff.length)
			return false;

		return true;
	}

	/**
	 * Calibrate the coefficients from the prior Predictor/Response Segment, the Constraint, and fitness
	 * 	Weights
	 * 
	 * @param csPrev Preceeding Predictor/Response Segment
	 * @param srvcState The Segment State Response Value Constraint
	 * @param srvcQuoteSensitivity The Segment State Response Value Constraint Quote Sensitivity
	 * @param sbfrState Segment's Best Fit Weighted State Response Values
	 * @param sbfrQuoteSensitivity Segment's Best Fit Weighted State Response Value Quote Sensitivity
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final org.drip.spline.segment.ConstitutiveState csPrev,
		final org.drip.spline.params.SegmentResponseValueConstraint srvcState,
		final org.drip.spline.params.SegmentResponseValueConstraint srvcQuoteSensitivity,
		final org.drip.spline.params.SegmentBestFitResponse sbfrState,
		final org.drip.spline.params.SegmentBestFitResponse sbfrQuoteSensitivity)
	{
		int iCk = _sdic.getCk();

		if (null == srvcState && null != srvcQuoteSensitivity) return false;

		org.drip.spline.params.SegmentBasisFlexureConstraint[] aSBFCState = null == srvcState ? null : new
			org.drip.spline.params.SegmentBasisFlexureConstraint[] {srvcState.responseIndexedBasisConstraint
				(_be, this)};

		org.drip.spline.params.SegmentBasisFlexureConstraint[] aSBFCQuoteSensitivity = null ==
			srvcQuoteSensitivity ? null : new org.drip.spline.params.SegmentBasisFlexureConstraint[]
				{srvcQuoteSensitivity.responseIndexedBasisConstraint (_be, this)};

		double[] adblQuoteJacobianDerivAtLeftOrdinate = null;

		if (0 != iCk) {
			adblQuoteJacobianDerivAtLeftOrdinate = new double[iCk];

			for (int i = 0; i < iCk; ++i)
				adblQuoteJacobianDerivAtLeftOrdinate[i] = 0.;
		}

		if (null == csPrev) {
			try {
				double[] adblStateDerivAtLeftOrdinate = null;

				if (0 != iCk) {
					adblStateDerivAtLeftOrdinate = new double[iCk];

					for (int i = 0; i < iCk; ++i)
						adblStateDerivAtLeftOrdinate[i] = _be.responseValueDerivative
							(_adblResponseBasisCoeff, left(), i);
				}

				if (!calibrateState (new org.drip.spline.params.SegmentStateCalibration (new double[]
					{left()}, new double[] {_be.responseValue (_adblResponseBasisCoeff, left())},
						adblStateDerivAtLeftOrdinate, null, aSBFCState, sbfrState)))
					return false;

				return null == aSBFCQuoteSensitivity ? true : calibrateLocalQuoteJacobian (new
					org.drip.spline.params.SegmentStateCalibration (new double[] {left()}, new double[] {0.},
						adblQuoteJacobianDerivAtLeftOrdinate, null, aSBFCQuoteSensitivity,
							sbfrQuoteSensitivity), aSBFCState);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			return false;
		}

		try {
			if (!calibrateState (new org.drip.spline.params.SegmentStateCalibration (new double[] {left()},
				new double[] {csPrev.responseValue (left())}, 0 == iCk ? null : transmissionCk (left(),
					csPrev, iCk), null, aSBFCState, sbfrState)))
				return false;

			if (null == aSBFCQuoteSensitivity) return true;

			if (!calibrateLocalQuoteJacobian (new org.drip.spline.params.SegmentStateCalibration (new
				double[] {left()}, new double[] {0.}, adblQuoteJacobianDerivAtLeftOrdinate, null,
					aSBFCQuoteSensitivity, sbfrQuoteSensitivity), aSBFCState))
				return false;

			if (_pqsc.impactFade())
				return calibratePreceedingQuoteJacobian (new org.drip.spline.params.SegmentStateCalibration (new
					double[] {left(), right()}, new double[] {csPrev.calcDResponseDQuote (left(), 1), 0.},
						null, CkDBasisCoeffDPreceedingQuoteTail(), null, null));

			_dblDResponseDPreceedingQuote = csPrev.calcDResponseDQuote (left(), 1);

			return true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the prior Segment and the Response Value at the Right Predictor
	 *  Ordinate
	 * 
	 * @param csPrev Preceeding Predictor/Response Segment
	 * @param dblRightStateValue Response Value at the Right Predictor Ordinate
	 * @param dblRightStateQuoteSensitivity Response Value Quote Sensitivity at the Right Predictor Ordinate
	 * @param sbfrState Segment's Best Fit Weighted Response Values
	 * @param sbfrQuoteSensitivity Segment's Best Fit Weighted Response Value Quote Sensitivity
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final ConstitutiveState csPrev,
		final double dblRightStateValue,
		final double dblRightStateQuoteSensitivity,
		final org.drip.spline.params.SegmentBestFitResponse sbfrState,
		final org.drip.spline.params.SegmentBestFitResponse sbfrQuoteSensitivity)
	{
		if (null == csPrev) return false;

		int iCk = _sdic.getCk();

		try {
			if (!calibrateState (new org.drip.spline.params.SegmentStateCalibration (new double[] {left(),
				right()}, new double[] {csPrev.responseValue (left()), dblRightStateValue}, 0 != iCk ?
					csPrev.transmissionCk (left(), this, iCk) : null, null, null, sbfrState)))
				return false;

			double[] adblQuoteJacobianDerivAtLeftOrdinate = null;

			if (0 != iCk) {
				adblQuoteJacobianDerivAtLeftOrdinate = new double[iCk];

				for (int i = 0; i < iCk; ++i)
					adblQuoteJacobianDerivAtLeftOrdinate[i] = 0.;
			}

			if (!org.drip.quant.common.NumberUtil.IsValid (dblRightStateQuoteSensitivity)) return true;

			if (!calibrateLocalQuoteJacobian (new org.drip.spline.params.SegmentStateCalibration (new
				double[] {left(), right()}, new double[] {0., dblRightStateQuoteSensitivity}, 0 != iCk ?
					adblQuoteJacobianDerivAtLeftOrdinate : null, null, null, sbfrQuoteSensitivity), null))
				return false;

			if (_pqsc.impactFade())
				return calibratePreceedingQuoteJacobian (new org.drip.spline.params.SegmentStateCalibration (new
					double[] {left(), right()}, new double[] {csPrev.calcDResponseDQuote (left(), 1), 0.},
						null, CkDBasisCoeffDPreceedingQuoteTail(), null, null));

			_dblDResponseDPreceedingQuote = csPrev.calcDResponseDQuote (left(), 1);

			return true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Slope
	 * 
	 * @param dblLeftValue Left Edge Response Value
	 * @param dblLeftQuoteSensitivity Left Edge Response Value Quote Sensitivity
	 * @param dblLeftSlope Left Edge Response Slope
	 * @param dblLeftSlopeQuoteSensitivity Left Edge Response Slope Quote Sensitivity
	 * @param dblRightValue Right Edge Response Value
	 * @param dblRightQuoteSensitivity Right Edge Response Value Quote Sensitivity
	 * @param sbfrState Segment's Best Fit Weighted Response Values
	 * @param sbfrQuoteSensitivity Segment's Best Fit Weighted Response Values Quote Sensitivity
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final double dblLeftValue,
		final double dblLeftQuoteSensitivity,
		final double dblLeftSlope,
		final double dblLeftSlopeQuoteSensitivity,
		final double dblRightValue,
		final double dblRightQuoteSensitivity,
		final org.drip.spline.params.SegmentBestFitResponse sbfrState,
		final org.drip.spline.params.SegmentBestFitResponse sbfrQuoteSensitivity)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblLeftValue) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblLeftSlope) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblRightValue))
			return false;

		try {
			if (!calibrateState (new org.drip.spline.params.SegmentStateCalibration (new double[] {left(),
				right()}, new double[] {dblLeftValue, dblRightValue},
					org.drip.quant.common.CollectionUtil.DerivArrayFromSlope (numParameters() - 2,
						dblLeftSlope), null, null, sbfrState)))
				return false;

			return org.drip.quant.common.NumberUtil.IsValid (dblLeftQuoteSensitivity) &&
				org.drip.quant.common.NumberUtil.IsValid (dblLeftSlopeQuoteSensitivity) &&
					org.drip.quant.common.NumberUtil.IsValid (dblRightQuoteSensitivity) ?
						calibrateLocalQuoteJacobian (new org.drip.spline.params.SegmentStateCalibration (new
							double[] {left(), right()}, new double[] {dblLeftQuoteSensitivity,
								dblRightQuoteSensitivity},
									org.drip.quant.common.CollectionUtil.DerivArrayFromSlope (numParameters()
										- 2, dblLeftSlopeQuoteSensitivity), null, null,
											sbfrQuoteSensitivity), null) : true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the Left Edge Response Value Constraint, the Left Edge Response Value
	 *  Slope, and the Right Edge Response Value Constraint
	 * 
	 * @param wrvcStateLeft Left Edge Response Value Constraint
	 * @param wrvcStateLeftQuoteSensitivity Left Edge Response Value Constraint Quote Sensitivity
	 * @param dblLeftSlope Left Edge Response Value Slope
	 * @param dblLeftSlopeQuoteSensitivity Left Edge Response Value Slope Quote Sensitivity
	 * @param wrvcStateRight Right Edge Response Value Constraint
	 * @param wrvcStateRightQuoteSensitivity Right Edge Response Value Constraint Quote Sensitivity
	 * @param sbfrState Segment's Best Fit Weighted Response
	 * @param sbfrQuoteSensitivity Segment's Best Fit Weighted Response Quote Sensitivity
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final org.drip.spline.params.SegmentResponseValueConstraint wrvcStateLeft,
		final org.drip.spline.params.SegmentResponseValueConstraint wrvcStateLeftQuoteSensitivity,
		final double dblLeftSlope,
		final double dblLeftSlopeQuoteSensitivity,
		final org.drip.spline.params.SegmentResponseValueConstraint wrvcStateRight,
		final org.drip.spline.params.SegmentResponseValueConstraint wrvcStateRightQuoteSensitivity,
		final org.drip.spline.params.SegmentBestFitResponse sbfrState,
		final org.drip.spline.params.SegmentBestFitResponse sbfrQuoteSensitivity)
	{
		org.drip.spline.params.SegmentBasisFlexureConstraint[] aSBFCState = null;
		org.drip.spline.params.SegmentBasisFlexureConstraint[] aSBFCQuoteSensitivity = null;

		try {
			if (null != wrvcStateLeft || null != wrvcStateRight)
				aSBFCState = new org.drip.spline.params.SegmentBasisFlexureConstraint[] {null ==
					wrvcStateLeft ? null : wrvcStateLeft.responseIndexedBasisConstraint (_be, this), null ==
						wrvcStateRight ? null : wrvcStateRight.responseIndexedBasisConstraint (_be, this)};

			if (null != wrvcStateLeftQuoteSensitivity || null != wrvcStateRightQuoteSensitivity)
				aSBFCQuoteSensitivity = new org.drip.spline.params.SegmentBasisFlexureConstraint[] {null ==
					wrvcStateLeftQuoteSensitivity ? null :
						wrvcStateLeftQuoteSensitivity.responseIndexedBasisConstraint (_be, this), null ==
							wrvcStateRightQuoteSensitivity ? null :
								wrvcStateRightQuoteSensitivity.responseIndexedBasisConstraint (_be, this)};

			if (!calibrateState (new org.drip.spline.params.SegmentStateCalibration (null, null,
				org.drip.quant.common.CollectionUtil.DerivArrayFromSlope (numParameters() - 2, dblLeftSlope),
					null, aSBFCState, sbfrState)))
				return false;

			return null == aSBFCQuoteSensitivity ? true : calibrateLocalQuoteJacobian (new
				org.drip.spline.params.SegmentStateCalibration (null, null,
					org.drip.quant.common.CollectionUtil.DerivArrayFromSlope (numParameters() - 2,
						dblLeftSlopeQuoteSensitivity), null, aSBFCQuoteSensitivity, sbfrQuoteSensitivity),
							aSBFCState);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Retrieve the Segment Curvature DPE
	 * 
	 * @return The Segment Curvature DPE
	 * 
	 * @throws java.lang.Exception Thrown if the Segment Curvature DPE cannot be computed
	 */

	public double curvatureDPE()
		throws java.lang.Exception
	{
		double dblDPE = 0.;

		int iNumBasis = _be.numBasis();

		org.drip.spline.params.SegmentFlexurePenaltyControl sfpc = _sdic.curvaturePenaltyControl();

		if (null == sfpc) sfpc = new org.drip.spline.params.SegmentFlexurePenaltyControl (2, 1.);

		org.drip.spline.segment.BestFitFlexurePenalizer bffp = new
			org.drip.spline.segment.BestFitFlexurePenalizer (this, sfpc, null, null, _be);

		for (int i = 0; i < iNumBasis; ++i) {
			for (int j = 0; j < iNumBasis; ++j)
				dblDPE += _adblResponseBasisCoeff[i] * _adblResponseBasisCoeff[j] *
					bffp.basisPairCurvaturePenalty (i, j);
		}

		return sfpc.amplitude() * dblDPE;
	}

	/**
	 * Retrieve the Segment Length DPE
	 * 
	 * @return The Segment Length DPE
	 * 
	 * @throws java.lang.Exception Thrown if the Segment Length DPE cannot be computed
	 */

	public double lengthDPE()
		throws java.lang.Exception
	{
		double dblDPE = 0.;

		int iNumBasis = _be.numBasis();

		org.drip.spline.params.SegmentFlexurePenaltyControl sfpcLength = _sdic.lengthPenaltyControl();

		if (null == sfpcLength) sfpcLength = new org.drip.spline.params.SegmentFlexurePenaltyControl (1, 1.);

		org.drip.spline.segment.BestFitFlexurePenalizer bffp = new
			org.drip.spline.segment.BestFitFlexurePenalizer (this, null, sfpcLength, null, _be);

		for (int i = 0; i < iNumBasis; ++i) {
			for (int j = 0; j < iNumBasis; ++j)
				dblDPE += _adblResponseBasisCoeff[i] * _adblResponseBasisCoeff[j] *
					bffp.basisPairLengthPenalty (i, j);
		}

		return sfpcLength.amplitude() * dblDPE;
	}

	/**
	 * Retrieve the Segment Best Fit DPE
	 * 
	 * @param sbfr The Segment's Best Fit Response Inputs
	 * 
	 * @return The Segment Best Fit DPE
	 * 
	 * @throws java.lang.Exception Thrown if the Segment Best Fit DPE cannot be computed
	 */

	public double bestFitDPE (
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
		throws java.lang.Exception
	{
		if (null == sbfr) return 0.;

		double dblDPE = 0.;

		int iNumBasis = _be.numBasis();

		org.drip.spline.segment.BestFitFlexurePenalizer bffp = new
			org.drip.spline.segment.BestFitFlexurePenalizer (this, null, null, sbfr, _be);

		for (int i = 0; i < iNumBasis; ++i) {
			for (int j = 0; j < iNumBasis; ++j)
				dblDPE += _adblResponseBasisCoeff[i] * _adblResponseBasisCoeff[j] * bffp.basisBestFitPenalty
					(i, j);
		}

		return dblDPE;
	}

	/**
	 * Calculate the Response Value at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * 
	 * @return The Response Value
	 * 
	 * @throws java.lang.Exception Thrown if the calculation did not succeed
	 */

	public double responseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		return _be.responseValue (_adblResponseBasisCoeff, dblPredictorOrdinate);
	}

	/**
	 * Calculate the Ordered Response Value Derivative at the Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate at which the ordered Response Derivative is to be
	 * 	calculated
	 * @param iOrder Derivative Order
	 * 
	 * @throws Thrown if the Ordered Response Value Derivative cannot be calculated
	 * 
	 * @return Retrieve the Ordered Response Value Derivative
	 */

	public double calcResponseValueDerivative (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (0 == iOrder) return responseValue (dblPredictorOrdinate);

		return _be.responseValueDerivative (_adblResponseBasisCoeff, dblPredictorOrdinate, iOrder);
	}

	/**
	 * Calculate the Ordered Derivative of the Response to the Quote
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate at which the ordered Derivative of the Response to the
	 * 	Quote is to be calculated
	 * @param iOrder Derivative Order
	 * 
	 * @throws Thrown if the Ordered Derivative of the Response to the Quote cannot be calculated
	 * 
	 * @return Retrieve the Ordered Derivative of the Response to the Quote
	 */

	public double calcDResponseDQuote (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (0 == iOrder)
			throw new java.lang.Exception ("ConstitutiveState::calcDResponseDQuote => Invalid Inputs");

		return _be.responseValue (_adblDBasisCoeffDLocalQuote, dblPredictorOrdinate);
	}

	/**
	 * Calculate the Ordered Derivative of the Response to the Preceeding Quote
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate at which the ordered Derivative of the Response to the
	 * 	Quote is to be calculated
	 * @param iOrder Derivative Order
	 * 
	 * @throws Thrown if the Ordered Derivative of the Response to the Quote cannot be calculated
	 * 
	 * @return Retrieve the Ordered Derivative of the Response to the Preceeding Quote
	 */

	public double calcDResponseDPreceedingQuote (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (0 == iOrder)
			throw new java.lang.Exception ("ConstitutiveState::calcDResponseDPreceedingQuote => Invalid Inputs");

		if (!_pqsc.impactFade())
			return org.drip.quant.common.NumberUtil.IsValid (_dblDResponseDPreceedingQuote) ?
				_dblDResponseDPreceedingQuote : 0.;

		org.drip.spline.segment.BasisEvaluator be = _pqsc.basisEvaluator();

		return null == _adblDBasisCoeffDPreceedingQuote ? 0. : (null == be ? _be : be).responseValue
			(_adblDBasisCoeffDPreceedingQuote, dblPredictorOrdinate);
	}

	/**
	 * Retrieve the Manifest Measure Preceeding Quote Impact Flag
	 * 
	 * @return The Manifest Measure Preceeding Quote Impact Flag
	 */

	public boolean impactFade()
	{
		return _pqsc.impactFade();
	}

	/**
	 * Calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Inputs
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Inputs
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeInputs()
	{
		if (null != _wjDBasisCoeffDEdgeValue) return _wjDBasisCoeffDEdgeValue;

		int iNumResponseBasisCoeff = _be.numBasis();

		try {
			_wjDBasisCoeffDEdgeValue = new org.drip.quant.calculus.WengertJacobian (iNumResponseBasisCoeff,
				iNumResponseBasisCoeff);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return _wjDBasisCoeffDEdgeValue = null;
		}

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			if (!_wjDBasisCoeffDEdgeValue.setWengert (i, _adblResponseBasisCoeff[i]))
				return _wjDBasisCoeffDEdgeValue = null;
		}

		if (null == _aadblDResponseBasisCoeffDConstraint) return null;

		int iSize = _aadblDResponseBasisCoeffDConstraint.length;

		for (int i = 0; i < iSize; ++i) {
			for (int j = 0; j < iSize; ++j) {
				if (!_wjDBasisCoeffDEdgeValue.accumulatePartialFirstDerivative (i, j,
					_aadblDResponseBasisCoeffDConstraint[i][j]))
					return null;
			}
		}

		return _wjDBasisCoeffDEdgeValue;
	}

	/**
	 * Calculate the Jacobian of the Response to the Edge Inputs at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * @param iOrder Order of the Derivative Desired
	 * 
	 * @return The Jacobian of the Response to the Edge Inputs at the given Predictor Ordinate
	 */

	public org.drip.quant.calculus.WengertJacobian jackDResponseDEdgeInputs (
		final double dblPredictorOrdinate,
		final int iOrder)
	{
		try {
			int iNumResponseBasisCoeff = _be.numBasis();

			org.drip.quant.calculus.WengertJacobian wjDResponseDEdgeParams = null;
			double[][] aadblDBasisCoeffDEdgeParams = new
				double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];

			double[] adblDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblPredictorOrdinate, iOrder);

			if (null == adblDResponseDBasisCoeff || iNumResponseBasisCoeff !=
				adblDResponseDBasisCoeff.length)
				return null;

			org.drip.quant.calculus.WengertJacobian wjDBasisCoeffDEdgeParams = (null ==
				_wjDBasisCoeffDEdgeValue) ? jackDCoeffDEdgeInputs() : _wjDBasisCoeffDEdgeValue;

			for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
				for (int j = 0; j < iNumResponseBasisCoeff; ++j)
					aadblDBasisCoeffDEdgeParams[j][i] = wjDBasisCoeffDEdgeParams.getFirstDerivative (j, i);
			}

			if (!(wjDResponseDEdgeParams = new org.drip.quant.calculus.WengertJacobian (1,
				iNumResponseBasisCoeff)).setWengert (0, responseValue (dblPredictorOrdinate)))
				return null;

			for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
				for (int j = 0; j < iNumResponseBasisCoeff; ++j) {
					if (!wjDResponseDEdgeParams.accumulatePartialFirstDerivative (0, i,
						adblDResponseDBasisCoeff[j] * aadblDBasisCoeffDEdgeParams[j][i]))
						return null;
				}
			}

			return wjDResponseDEdgeParams;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calculate the Jacobian of the Response to the Basis Coefficients at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * @param iOrder Order of the Derivative Desired
	 * 
	 * @return The Jacobian of the Response to the Basis Coefficients at the given Predictor Ordinate
	 */

	public org.drip.quant.calculus.WengertJacobian jackDResponseDBasisCoeff (
		final double dblPredictorOrdinate,
		final int iOrder)
	{
		try {
			int iNumResponseBasisCoeff = _be.numBasis();

			double[] adblBasisDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblPredictorOrdinate,
				iOrder);

			if (null == adblBasisDResponseDBasisCoeff || iNumResponseBasisCoeff !=
				adblBasisDResponseDBasisCoeff.length)
				return null;

			org.drip.quant.calculus.WengertJacobian wjDResponseDBasisCoeff = new
				org.drip.quant.calculus.WengertJacobian (1, iNumResponseBasisCoeff);

			for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
				if (!wjDResponseDBasisCoeff.accumulatePartialFirstDerivative (0, i,
					adblBasisDResponseDBasisCoeff[i]))
					return null;
			}

			return wjDResponseDBasisCoeff;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calibrate the segment and calculate the Jacobian of the Segment's Response Basis Function Coefficients
	 *  to the Edge Parameters
	 * 
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param adblLeftEdgeDeriv Array of Left Edge Derivatives
	 * @param adblRightEdgeDeriv Array of Right Edge Derivatives
	 * @param aSIBC Array of Segment Flexure Constraints, expressed as Basis Coefficients
	 * @param sbfr Segment Best Fit Response Instance
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final double[] adblLeftEdgeDeriv,
		final double[] adblRightEdgeDeriv,
		final org.drip.spline.params.SegmentBasisFlexureConstraint[] aSIBC,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		try {
			return calibrateState (new org.drip.spline.params.SegmentStateCalibration (adblPredictorOrdinate,
				adblResponseValue, adblLeftEdgeDeriv, adblRightEdgeDeriv, aSIBC, sbfr)) ?
					jackDCoeffDEdgeInputs() : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Value Slope and
	 *  calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 * 
	 * @param dblLeftValue Left Edge Response Value
	 * @param dblLeftQuoteSensitivity Left Edge Response Value Quote Sensitivity
	 * @param dblLeftSlope Left Edge Response Slope
	 * @param dblLeftSlopeQuoteSensitivity Left Edge Response Slope Quote Sensitivity
	 * @param dblRightValue Right Edge Response Value
	 * @param dblRightQuoteSensitivity Right Edge Response Value Quote Sensitivity
	 * @param sbfrState Segment's Best Fit Weighted Response Values
	 * @param sbfrQuoteSensitivity Segment's Best Fit Weighted Response Values Quote Sensitivity
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final double dblLeftValue,
		final double dblLeftQuoteSensitivity,
		final double dblLeftSlope,
		final double dblLeftSlopeQuoteSensitivity,
		final double dblRightValue,
		final double dblRightQuoteSensitivity,
		final org.drip.spline.params.SegmentBestFitResponse sbfrState,
		final org.drip.spline.params.SegmentBestFitResponse sbfrQuoteSensitivity)
	{
		return calibrate (dblLeftValue, dblLeftQuoteSensitivity, dblLeftSlope, dblLeftSlopeQuoteSensitivity,
			dblRightValue, dblRightQuoteSensitivity, sbfrState, sbfrQuoteSensitivity) ?
				jackDCoeffDEdgeInputs() : null;
	}

	/**
	 * Calibrate the coefficients from the prior Segment and the Response Value at the Right Predictor
	 *  Ordinate and calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge
	 *  Parameters
	 * 
	 * @param csPrev Previous Predictor/Response Segment
	 * @param dblRightStateValue Response Value at the Right Predictor Ordinate
	 * @param dblRightStateQuoteSensitivity Response Value Quote Sensitivity at the Right Predictor Ordinate
	 * @param sbfrState Segment's Best Fit Weighted Response Values
	 * @param sbfrQuoteSensitivity Segment's Best Fit Weighted Response Value Quote Sensitivity
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final ConstitutiveState csPrev,
		final double dblRightStateValue,
		final double dblRightStateQuoteSensitivity,
		final org.drip.spline.params.SegmentBestFitResponse sbfrState,
		final org.drip.spline.params.SegmentBestFitResponse sbfrQuoteSensitivity)
	{
		return !calibrate (csPrev, dblRightStateValue, dblRightStateQuoteSensitivity, sbfrState,
			sbfrQuoteSensitivity) ? null : jackDCoeffDEdgeInputs();
	}

	/**
	 * Indicate whether the given segment is monotone. If monotone, may optionally indicate the nature of
	 * 	the extrema contained inside (maxima/minima/infection).
	 *  
	 * @return The monotone Type
	 */

	public org.drip.spline.segment.Monotonocity monotoneType()
	{
		if (1 >= _sdic.getCk()) {
			try {
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MONOTONIC);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		org.drip.quant.function1D.AbstractUnivariate ofDeriv = new
			org.drip.quant.function1D.AbstractUnivariate (null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				return _be.responseValueDerivative (_adblResponseBasisCoeff, dblX, 1);
			}

			@Override public org.drip.quant.calculus.Differential calcDifferential (
				final double dblX,
				final double dblOFBase,
				final int iOrder)
			{
				try {
					double dblVariateInfinitesimal = _dc.getVariateInfinitesimal (dblX);

					return new org.drip.quant.calculus.Differential (dblVariateInfinitesimal,
						_be.responseValueDerivative (_adblResponseBasisCoeff, dblX, iOrder) *
							dblVariateInfinitesimal);
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override public double integrate (
				final double dblBegin,
				final double dblEnd)
				throws java.lang.Exception
			{
				return org.drip.quant.calculus.Integrator.Boole (this, dblBegin, dblEnd);
			}
		};

		try {
			org.drip.quant.solver1D.FixedPointFinderOutput fpop = new
				org.drip.quant.solver1D.FixedPointFinderBrent (0., ofDeriv, false).findRoot
					(org.drip.quant.solver1D.InitializationHeuristics.FromHardSearchEdges (0., 1.));

			if (null == fpop || !fpop.containsRoot())
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MONOTONIC);

			double dblExtremum = fpop.getRoot();

			if (!org.drip.quant.common.NumberUtil.IsValid (dblExtremum) || dblExtremum <= 0. || dblExtremum
				>= 1.)
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MONOTONIC);

			double dbl2ndDeriv = _be.responseValueDerivative (_adblResponseBasisCoeff, dblExtremum, 2);

			if (0. > dbl2ndDeriv)
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MAXIMA);

			if (0. < dbl2ndDeriv)
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MINIMA);

			if (0. == dbl2ndDeriv)
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.INFLECTION);

			return new org.drip.spline.segment.Monotonocity
				(org.drip.spline.segment.Monotonocity.NON_MONOTONIC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			return new org.drip.spline.segment.Monotonocity (org.drip.spline.segment.Monotonocity.MONOTONIC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Clip the part of the Segment to the Right of the specified Predictor Ordinate. Retain all other
	 * 	constraints the same.
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Clipped Segment
	 */

	public ConstitutiveState clipLeftOfPredictorOrdinate (
		final double dblPredictorOrdinate)
	{
		try {
			ConstitutiveState csLeftSnipped = ConstitutiveState.Create (dblPredictorOrdinate, right(),
				_be.replicate(), _sdic, _pqsc);

			int iCk = _sdic.getCk();

			double[] adblCalibLeftEdgeDeriv = 0 != iCk ? csLeftSnipped.transmissionCk (dblPredictorOrdinate,
				this, iCk) : null;

			return csLeftSnipped.calibrateState (new org.drip.spline.params.SegmentStateCalibration (new
				double[] {dblPredictorOrdinate, right()}, new double[] {responseValue (dblPredictorOrdinate),
					responseValue (right())}, adblCalibLeftEdgeDeriv, null, null, null)) ? csLeftSnipped :
						null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Clip the part of the Segment to the Left of the specified Predictor Ordinate. Retain all other
	 * 	constraints the same.
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Clipped Segment
	 */

	public ConstitutiveState clipRightOfPredictorOrdinate (
		final double dblPredictorOrdinate)
	{
		try {
			ConstitutiveState csRightSnipped = ConstitutiveState.Create (left(), dblPredictorOrdinate,
				_be.replicate(), _sdic, _pqsc);

			int iCk = _sdic.getCk();

			return csRightSnipped.calibrateState (new org.drip.spline.params.SegmentStateCalibration (new
				double[] {left(), dblPredictorOrdinate}, new double[] {responseValue (left()), responseValue
					(dblPredictorOrdinate)}, 0 != iCk ? csRightSnipped.transmissionCk (left(), this, iCk) :
						null, null, null, null)) ? csRightSnipped : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Display the string representation for diagnostic purposes
	 * 
	 * @return The string representation
	 */

	public java.lang.String displayString()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append ("\t\t\t[" + left() + " => " + right() + "\n");

		for (int i = 0; i < _adblResponseBasisCoeff.length; ++i) {
			if (0 != i) sb.append ("  |  ");

			sb.append (_adblResponseBasisCoeff[i] + "\n");
		}

		return sb.toString();
	}
}


package org.drip.spline.segment;

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
 * This abstract class contains the basis spline segment in-elastic ordinates. Estimate segment spline
 *  functions and their coefficients are implemented/calibrated in the overriding spline classes. It provides
 *  functionality for assessing the various segment attributes:
 *  - Segment Monotonicity
 *  - Estimate Function Value, the ordered derivative, and the corresponding Jacobian
 *  - Segment Local/Global Derivative
 *  - Evaluation of the Segment Micro-Jack
 *  - Head / Regular Segment calibration - both of the basis function coefficients and the Jacobian
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
	private org.drip.spline.segment.BasisEvaluator _be = null;
	private double[][] _aadblDResponseBasisCoeffDConstraint = null;
	private org.drip.spline.params.SegmentDesignInelasticControl _sdic = null;
	private org.drip.quant.calculus.WengertJacobian _wjDBasisCoeffDEdgeValue = null;

	/**
	 * Build the ConstitutiveState instance from the Basis Set
	 * 
	 * @param dblLeftPredictorOrdinate Left Predictor Ordinate
	 * @param dblRightPredictorOrdinate Right Predictor Ordinate
	 * @param fs Response Basis Function Set
	 * @param rssc Shape Controller
	 * @param sdic Design Inelastic Parameters
	 * 
	 * @return Instance of ConstitutiveState
	 */

	public static final org.drip.spline.segment.ConstitutiveState Create (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.spline.basis.FunctionSet fs,
		final org.drip.spline.params.ResponseScalingShapeControl rssc,
		final org.drip.spline.params.SegmentDesignInelasticControl sdic)
	{
		try {
			org.drip.spline.segment.SegmentBasisEvaluator sbe = new
				org.drip.spline.segment.SegmentBasisEvaluator (fs, rssc);

			org.drip.spline.segment.ConstitutiveState cs = new org.drip.spline.segment.ConstitutiveState
				(dblLeftPredictorOrdinate, dblRightPredictorOrdinate, sbe, sdic);

			return sbe.setContainingInelastics (cs) ? cs : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Build the ConstitutiveState instance from the Basis Set
	 * 
	 * @param dblLeftPredictorOrdinate Left Predictor Ordinate
	 * @param dblRightPredictorOrdinate Right Predictor Ordinate
	 * @param be Basis Evaluator
	 * @param sdic Design Inelastic Parameters
	 * 
	 * @return Instance of ConstitutiveState
	 */

	public static final org.drip.spline.segment.ConstitutiveState Create (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.spline.segment.BasisEvaluator be,
		final org.drip.spline.params.SegmentDesignInelasticControl sdic)
	{
		try {
			org.drip.spline.segment.ConstitutiveState cs = new org.drip.spline.segment.ConstitutiveState
				(dblLeftPredictorOrdinate, dblRightPredictorOrdinate, be, sdic);

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
		final org.drip.spline.params.SegmentDesignInelasticControl sdic)
		throws java.lang.Exception
	{
		super (dblLeftPredictorOrdinate, dblRightPredictorOrdinate);

		if (null == (_be = be) || null == (_sdic = sdic))
			throw new java.lang.Exception ("ConstitutiveState ctr: Invalid Basis Functions!");

		int iNumBasis = _be.numBasis();

		_adblResponseBasisCoeff = new double[iNumBasis];

		if (0 >= iNumBasis || _sdic.getCk() > iNumBasis - 2)
			throw new java.lang.Exception ("ConstitutiveState ctr: Invalid inputs!");
	}

	private int numParameters()
	{
		return _sdic.getCk() + 2;
	}

	private double[] basisDResponseDBasisCoeff (
		final double dblPredictorOrdinate)
	{
		int iNumBasis = _be.numBasis();

		double[] adblDResponseDBasisCoeff = new double[iNumBasis];

		for (int i = 0; i < iNumBasis; ++i) {
			try {
				adblDResponseDBasisCoeff[i] = _be.shapedBasisFunctionResponse (dblPredictorOrdinate, i);
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

	/**
	 * Retrieve the Basis Evaluator
	 * 
	 * @return The Basis Evaluator
	 */

	public org.drip.spline.segment.BasisEvaluator getBasisEvaluator()
	{
		return _be;
	}

	/**
	 * The Main Segment Calibrator Routine
	 * 
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * @param adblResponseValue Array of Response Values
	 * @param adblLeftEdgeDeriv Array of the Left Edge Derivatives
	 * @param adblRightEdgeDeriv Array of the Right Edge  Derivatives
	 * @param aSIBC Array of the Segment Indexed Basis Constraints
	 * @param sbfr Segment Basis Fit Response
	 * 
	 * @return TRUE => Calibration Successful
	 */

	public boolean calibrate (
		final double[] adblPredictorOrdinate,
		final double[] adblResponseValue,
		final double[] adblLeftEdgeDeriv,
		final double[] adblRightEdgeDeriv,
		final org.drip.spline.params.SegmentBasisFlexureConstraint[] aSIBC,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		int iNumConstraint = null == aSIBC ? 0 : aSIBC.length;
		int iNumResponseBasisCoeff = _adblResponseBasisCoeff.length;
		double[] adblPredictorResponseConstraintValue = new double[iNumResponseBasisCoeff];
		int iNumLeftDeriv = null == adblLeftEdgeDeriv ? 0 : adblLeftEdgeDeriv.length;
		int iNumRightDeriv = null == adblRightEdgeDeriv ? 0 : adblRightEdgeDeriv.length;
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
					adblPredictorResponseConstraintValue[j] =
						aSIBC[j - iNumPredictorOrdinate].contraintValue();
				else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv)
					adblPredictorResponseConstraintValue[j] =
						adblLeftEdgeDeriv[j - iNumPredictorOrdinate - iNumConstraint];
				else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv + iNumRightDeriv)
					adblPredictorResponseConstraintValue[j] =
						adblRightEdgeDeriv[j - iNumPredictorOrdinate - iNumConstraint - iNumLeftDeriv];
				else
					adblPredictorResponseConstraintValue[j] = bffp.basisPairPenaltyConstraint (j);
			}

			for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
				for (int l = 0; l < iNumResponseBasisCoeff; ++l) {
					double[] adblCalibBasisConstraintWeight = null;

					if (0 != iNumConstraint && (l >= iNumPredictorOrdinate && l < iNumPredictorOrdinate +
						iNumConstraint))
						adblCalibBasisConstraintWeight = aSIBC[l -
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
	 * Calibrate the coefficients from the prior Predictor/Response Segment, the Constraint, and fitness
	 * 	Weights
	 * 
	 * @param csPrev Prior Predictor/Response Segment
	 * @param srvc The Segment Response Value Constraint
	 * @param sbfr Segment's Best Fit Weighted Response Values
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final org.drip.spline.segment.ConstitutiveState csPrev,
		final org.drip.spline.params.SegmentResponseValueConstraint srvc,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		int iCk = _sdic.getCk();

		if (null == csPrev) {
			try {
				double[] adblDerivAtLeftOrdinate = null;

				if (0 != iCk) {
					adblDerivAtLeftOrdinate = new double[iCk];

					for (int i = 0; i < iCk; ++i)
						adblDerivAtLeftOrdinate[i] = _be.responseValueDerivative (_adblResponseBasisCoeff,
							left(), i);
				}

				return calibrate (new double[] {left()}, new double[] {_be.responseValue
					(_adblResponseBasisCoeff, left())}, adblDerivAtLeftOrdinate, null, null == srvc ? null :
						new org.drip.spline.params.SegmentBasisFlexureConstraint[]
							{srvc.responseIndexedBasisConstraint (_be, this)}, sbfr);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			return false;
		}

		try {
			return calibrate (new double[] {left()}, new double[] {csPrev.responseValue (left())}, 0 == iCk ?
				null : transmissionCk (left(), csPrev, iCk), null, null == srvc ? null : new
					org.drip.spline.params.SegmentBasisFlexureConstraint[]
						{srvc.responseIndexedBasisConstraint (_be, this)}, sbfr);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the prior Segment and the Response Value at the Right Predictor
	 *  Ordinate
	 * 
	 * @param csPrev Prior Predictor/Response Segment
	 * @param dblRightOrdinateResponseValue Response Value at the Right Predictor Ordinate
	 * @param sbfr Segment's Best Fit Weighted Response Values
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final ConstitutiveState csPrev,
		final double dblRightOrdinateResponseValue,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		int iCk = _sdic.getCk();

		try {
			return calibrate (new double[] {left(), right()}, new double[] {csPrev.responseValue (left()),
				dblRightOrdinateResponseValue}, 0 != iCk ? csPrev.transmissionCk (left(), this, iCk) : null,
					null, null, sbfr);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Slope
	 * 
	 * @param dblLeftEdgeResponseValue Left Edge Response Value
	 * @param dblLeftEdgeResponseSlope Left Edge Response Slope
	 * @param dblRightEdgeResponseValue Right Edge Response Value
	 * @param sbfr Segment's Best Fit Weighted Response Values
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final double dblLeftEdgeResponseValue,
		final double dblLeftEdgeResponseSlope,
		final double dblRightEdgeResponseValue,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblLeftEdgeResponseValue) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblLeftEdgeResponseSlope) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblRightEdgeResponseValue))
			return false;

		try {
			return calibrate (new double[] {left(), right()}, new double[] {dblLeftEdgeResponseValue,
				dblRightEdgeResponseValue}, org.drip.quant.common.CollectionUtil.DerivArrayFromSlope
					(numParameters() - 2, dblLeftEdgeResponseSlope), null, null, sbfr);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the Left Edge Response Value, the Left Edge Response Slope, and the
	 * 	Right Edge Response Value Constraint
	 * 
	 * @param dblLeftEdgeResponseValue Left Edge Response Value
	 * @param dblLeftEdgeResponseSlope Left Edge Response Slope
	 * @param srvcRight Segment's Right Edge Response Value Constraint
	 * @param bfwr Best Fit Weighted Response
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final double dblLeftEdgeResponseValue,
		final double dblLeftEdgeResponseSlope,
		final org.drip.spline.params.SegmentResponseValueConstraint srvcRight,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		try {
			org.drip.spline.params.SegmentBasisFlexureConstraint[] aSIBC = new
				org.drip.spline.params.SegmentBasisFlexureConstraint[]
					{org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
						(delocalize (0.), dblLeftEdgeResponseValue).responseIndexedBasisConstraint (_be,
							this), srvcRight.responseIndexedBasisConstraint (_be, this)};

			return calibrate (null, null, org.drip.quant.common.CollectionUtil.DerivArrayFromSlope
				(numParameters() - 2, dblLeftEdgeResponseSlope), null, aSIBC, sbfr);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the Left Edge Response Value Constraint, the Left Edge Response Value
	 *  Slope, and the Right Edge Response Value Constraint
	 * 
	 * @param wrvcLeft Left Edge Response Value Constraint
	 * @param dblLeftResponseValueSlope Left Edge Response ValueSlope
	 * @param wrvcRight Right Edge Response Value Constraint
	 * @param sbfr Segment's Best Fit Weighted Response
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final org.drip.spline.params.SegmentResponseValueConstraint wrvcLeft,
		final double dblLeftResponseValueSlope,
		final org.drip.spline.params.SegmentResponseValueConstraint wrvcRight,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		org.drip.spline.params.SegmentBasisFlexureConstraint[] aSIBC = null;

		try {
			if (null != wrvcLeft || null != wrvcRight)
				aSIBC = new org.drip.spline.params.SegmentBasisFlexureConstraint[] {null == wrvcLeft ? null :
					wrvcLeft.responseIndexedBasisConstraint (_be, this), null == wrvcRight ? null :
						wrvcRight.responseIndexedBasisConstraint (_be, this)};

			return calibrate (null, null, org.drip.quant.common.CollectionUtil.DerivArrayFromSlope
				(numParameters() - 2, dblLeftResponseValueSlope), null, aSIBC, sbfr);
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
	 * Calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams()
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
	 * Calculate the Jacobian of the Response to the Edge Parameters at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Jacobian of the Response to the Edge Parameters at the given Predictor Ordinate
	 */

	public org.drip.quant.calculus.WengertJacobian jackDResponseDEdgeParams (
		final double dblPredictorOrdinate)
	{
		try {
			int iNumResponseBasisCoeff = _be.numBasis();

			org.drip.quant.calculus.WengertJacobian wjDResponseDEdgeParams = null;
			double[][] aadblDBasisCoeffDEdgeParams = new
				double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];

			double[] adblDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblPredictorOrdinate);

			if (null == adblDResponseDBasisCoeff || iNumResponseBasisCoeff !=
				adblDResponseDBasisCoeff.length)
				return null;

			org.drip.quant.calculus.WengertJacobian wjDBasisCoeffDEdgeParams = (null ==
				_wjDBasisCoeffDEdgeValue) ? jackDCoeffDEdgeParams() : _wjDBasisCoeffDEdgeValue;

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
	 * 
	 * @return The Jacobian of the Response to the Basis Coefficients at the given Predictor Ordinate
	 */

	public org.drip.quant.calculus.WengertJacobian jackDResponseDBasisCoeff (
		final double dblPredictorOrdinate)
	{
		try {
			int iNumResponseBasisCoeff = _be.numBasis();

			double[] adblBasisDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblPredictorOrdinate);

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
	 * @param scis Segment Calibration Parameters
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
		return calibrate (adblPredictorOrdinate, adblResponseValue, adblLeftEdgeDeriv, adblRightEdgeDeriv,
			aSIBC, sbfr) ? jackDCoeffDEdgeParams() : null;
	}

	/**
	 * Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Value Slope and
	 *  calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 * 
	 * @param dblLeftEdgeResponseValue Left Edge Response Value
	 * @param dblLeftResponseValueSlope Left Edge Response Slope
	 * @param dblRightEdgeResponseValue Right Edge Response Value
	 * @param bfwr Fitness Weighted Response
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final double dblLeftEdgeResponseValue,
		final double dblLeftResponseValueSlope,
		final double dblRightEdgeResponseValue,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		if (!calibrate (dblLeftEdgeResponseValue, dblLeftResponseValueSlope, dblRightEdgeResponseValue,
			sbfr))
			return null;

		return jackDCoeffDEdgeParams();
	}

	/**
	 * Calibrate the coefficients from the prior Segment and the Response Value at the Right Predictor
	 *  Ordinate and calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge
	 *  Parameters
	 * 
	 * @param csPrev Previous Predictor/Response Segment
	 * @param dblRightEdgeResponseValue Right Edge Response Value
	 * @param sbfr Fitness Weighted Response
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final ConstitutiveState csPrev,
		final double dblRightEdgeResponseValue,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		if (!calibrate (csPrev, dblRightEdgeResponseValue, sbfr)) return null;

		return jackDCoeffDEdgeParams();
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

		org.drip.quant.function1D.AbstractUnivariate ofDeriv = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
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
				_be.replicate(), _sdic);

			int iCk = _sdic.getCk();

			double[] adblCalibLeftEdgeDeriv = 0 != iCk ? csLeftSnipped.transmissionCk (dblPredictorOrdinate,
				this, iCk) : null;

			return csLeftSnipped.calibrate (new double[] {dblPredictorOrdinate, right()}, new double[]
				{responseValue (dblPredictorOrdinate), responseValue (right())}, adblCalibLeftEdgeDeriv,
					null, null, null) ? csLeftSnipped : null;
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
				_be.replicate(), _sdic);

			int iCk = _sdic.getCk();

			return csRightSnipped.calibrate (new double[] {left(), dblPredictorOrdinate}, new double[]
				{responseValue (left()), responseValue (dblPredictorOrdinate)}, 0 != iCk ?
					csRightSnipped.transmissionCk (left(), this, iCk) : null, null, null, null) ?
						csRightSnipped : null;
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

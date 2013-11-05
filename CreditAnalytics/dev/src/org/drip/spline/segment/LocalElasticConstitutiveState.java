
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
 * This concrete class extends segment, and implements the segment's Ck based spline functionality. It
 * 	exports the following:
 * 	- Calibration: Head Calibration, Regular Calibration
 *  - Estimated Segment Elastics: The Basis Functions and their coefficients, Ck, the shape controller
 *  - Local Point Evaluation: Value, Ordered Derivative
 *  - Local Monotonicity
 *  - Local coefficient/derivative micro-Jack, and value/coefficient micro-Jack
 *  - Local Jacobians: Value Micro Jacobian, Value Elastic Jacobian, Composite Value Jacobian
 * 
 * @author Lakshmi Krishnamurthy
 */

public class LocalElasticConstitutiveState extends org.drip.spline.segment.ElasticConstitutiveState
	implements org.drip.spline.segment.LocalBasisEvaluator {
	private double[] _adblResponseBasisCoeff = null;
	private org.drip.spline.basis.FunctionSet _fs = null;
	private double[][] _aadblDResponseBasisCoeffDConstraint = null;
	private org.drip.spline.params.ResponseScalingShapeControl _rssc = null;
	private org.drip.spline.params.SegmentDesignInelasticControl _sdic = null;
	private org.drip.quant.calculus.WengertJacobian _wjDBasisCoeffDEdgeValue = null;

	/**
	 * Build the LocalElasticConstitutiveState instance from the Basis Set
	 * 
	 * @param dblLeftPredictorOrdinate Left Predictor Ordinate
	 * @param dblRightPredictorOrdinate Right Predictor Ordinate
	 * @param fs Response Basis Function Set
	 * @param rssc Shape Controller
	 * @param sdic Design Inelastic Parameters
	 * 
	 * @return Instance of LocalBasisConstitutiveState
	 */

	public static final org.drip.spline.segment.LocalElasticConstitutiveState Create (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.spline.basis.FunctionSet fs,
		final org.drip.spline.params.ResponseScalingShapeControl rssc,
		final org.drip.spline.params.SegmentDesignInelasticControl sdic)
	{
		try {
			return new org.drip.spline.segment.LocalElasticConstitutiveState (dblLeftPredictorOrdinate,
				dblRightPredictorOrdinate, fs, rssc, sdic);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean calibrate (
		final double[] adblLocalPredictorOrdinate,
		final double[] adblResponse,
		final double[] adblLeftEdgeLocalDeriv,
		final double[] adblRightEdgeLocalDeriv,
		final org.drip.spline.params.SegmentIndexedBasisConstraint[] aSIBC,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		int iNumConstraint = null == aSIBC ? 0 : aSIBC.length;
		int iNumResponseBasisCoeff = _adblResponseBasisCoeff.length;
		int iNumLeftDeriv = null == adblLeftEdgeLocalDeriv ? 0 : adblLeftEdgeLocalDeriv.length;
		int iNumRightDeriv = null == adblRightEdgeLocalDeriv ? 0 : adblRightEdgeLocalDeriv.length;
		double[] adblPredictorResponseConstraintValue = new double[iNumResponseBasisCoeff];
		double[][] aadblResponseBasisCoeffConstraint = new
			double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];
		int iNumPredictorOrdinate = null == adblLocalPredictorOrdinate ? 0 :
			adblLocalPredictorOrdinate.length;

		if (iNumResponseBasisCoeff < iNumPredictorOrdinate + iNumLeftDeriv + iNumRightDeriv + iNumConstraint)
			return false;

		try {
			org.drip.spline.segment.BestFitFlexurePenalizer bffp = new
				org.drip.spline.segment.BestFitFlexurePenalizer (_sdic.curvaturePenaltyControl(),
					_sdic.lengthPenaltyControl(), sbfr, this);

			for (int j = 0; j < iNumResponseBasisCoeff; ++j) {
				if (j < iNumPredictorOrdinate)
					adblPredictorResponseConstraintValue[j] = adblResponse[j];
				else if (j < iNumPredictorOrdinate + iNumConstraint)
					adblPredictorResponseConstraintValue[j] =
						aSIBC[j - iNumPredictorOrdinate].contraintValue();
				else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv)
					adblPredictorResponseConstraintValue[j] =
						adblLeftEdgeLocalDeriv[j - iNumPredictorOrdinate - iNumConstraint];
				else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv + iNumRightDeriv)
					adblPredictorResponseConstraintValue[j] =
						adblRightEdgeLocalDeriv[j - iNumPredictorOrdinate - iNumConstraint - iNumLeftDeriv];
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
						aadblResponseBasisCoeffConstraint[l][i] = _fs._aAUResponseBasis[i].evaluate
							(adblLocalPredictorOrdinate[l]) * (null == _rssc ? 1. :
								_rssc.shapeController().evaluate (_rssc.isLocal() ?
									adblLocalPredictorOrdinate[l] : delocalize
										(adblLocalPredictorOrdinate[l])));
					else if (l < iNumPredictorOrdinate + iNumConstraint)
						aadblResponseBasisCoeffConstraint[l][i] = adblCalibBasisConstraintWeight[i];
					else if (l < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv)
						aadblResponseBasisCoeffConstraint[l][i] = localSpecificBasisDerivative (0., l -
							iNumPredictorOrdinate - iNumConstraint + 1, i);
					else if (l < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv + iNumRightDeriv)
						aadblResponseBasisCoeffConstraint[l][i] = localSpecificBasisDerivative (1., l -
							iNumPredictorOrdinate - iNumConstraint - iNumLeftDeriv + 1, i);
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

	private boolean setJackDCoeffDEdge()
	{
		if (null == _aadblDResponseBasisCoeffDConstraint) return false;

		int iSize = _aadblDResponseBasisCoeffDConstraint.length;

		for (int i = 0; i < iSize; ++i) {
			for (int j = 0; j < iSize; ++j) {
				if (!_wjDBasisCoeffDEdgeValue.accumulatePartialFirstDerivative (i, j,
					_aadblDResponseBasisCoeffDConstraint[i][j]))
					return false;
			}
		}

		return true;
	}

	private double[] basisDResponseDBasisCoeff (
		final double dblLocalPredictorOrdinate)
	{
		int iNumBasis = _fs._aAUResponseBasis.length;
		double[] adblDResponseDBasisCoeff = new double[iNumBasis];

		for (int i = 0; i < iNumBasis; ++i) {
			try {
				adblDResponseDBasisCoeff[i] = _fs._aAUResponseBasis[i].evaluate (dblLocalPredictorOrdinate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return adblDResponseDBasisCoeff;
	}

	protected LocalElasticConstitutiveState (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.spline.basis.FunctionSet fs,
		final org.drip.spline.params.ResponseScalingShapeControl rssc,
		final org.drip.spline.params.SegmentDesignInelasticControl sdic)
		throws java.lang.Exception
	{
		super (dblLeftPredictorOrdinate, dblRightPredictorOrdinate);

		if (null == (_fs = fs) || null == (_sdic = sdic))
			throw new java.lang.Exception ("LocalElasticConstitutiveState ctr: Invalid Basis Functions!");

		_rssc = rssc;
		int iNumBasis = _fs._aAUResponseBasis.length;
		_adblResponseBasisCoeff = new double[iNumBasis];

		if (0 >= iNumBasis || _sdic.getCk() > iNumBasis - 2)
			throw new java.lang.Exception ("LocalElasticConstitutiveState ctr: Invalid inputs!");
	}

	@Override protected boolean isMonotone()
	{
		return 1 >= _sdic.getCk();
	}

	@Override protected double localResponseValue (
		final double dblLocalPredictorOrdinate)
		throws java.lang.Exception
	{
		if (null == _rssc) return basisFunctionResponseValue (dblLocalPredictorOrdinate);

		return basisFunctionResponseValue (dblLocalPredictorOrdinate) * _rssc.shapeController().evaluate
			(_rssc.isLocal() ? dblLocalPredictorOrdinate : delocalize (dblLocalPredictorOrdinate));
	}

	@Override protected ElasticConstitutiveState snipLeftOfLocalPredictorOrdinate (
		final double dblLocalPredictorOrdinate)
	{
		try {
			LocalElasticConstitutiveState lbprLeftSnipped = new LocalElasticConstitutiveState (delocalize
				(dblLocalPredictorOrdinate), right(), _fs, _rssc, _sdic);

			int iCk = _sdic.getCk();

			double[] adblCalibLeftEdgeDeriv = 0 != iCk ? lbprLeftSnipped.globalCk (delocalize
				(dblLocalPredictorOrdinate), this, iCk) : null;

			return lbprLeftSnipped.calibrate (new double[] {0., 1.}, new double[] {localResponseValue
				(dblLocalPredictorOrdinate), localResponseValue (1.)}, adblCalibLeftEdgeDeriv, null, null,
					null) ? lbprLeftSnipped : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override protected ElasticConstitutiveState snipRightOfLocalPredictorOrdinate (
		final double dblLocalPredictorOrdinate)
	{
		try {
			LocalElasticConstitutiveState lbprRightSnipped = new LocalElasticConstitutiveState (left(),
				delocalize (dblLocalPredictorOrdinate), _fs, _rssc, _sdic);

			int iCk = _sdic.getCk();

			double[] adblCalibLeftEdgeDeriv = 0 != iCk ? lbprRightSnipped.globalCk (left(), this, iCk) :
				null;

			return lbprRightSnipped.calibrate (new double[] {0., 1.}, new double[] {localResponseValue (0.),
				localResponseValue (dblLocalPredictorOrdinate)}, adblCalibLeftEdgeDeriv, null, null, null) ?
					lbprRightSnipped : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override protected double localResponseValueDerivative (
		final double dblLocalPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (null == _rssc) return basisFunctionResponseDerivative (dblLocalPredictorOrdinate, iOrder);

		double dblResponseDerivative = 0.;

		for (int i = 0; i <= iOrder; ++i) {
			double dblBasisFunctionDeriv = 0 == i ? basisFunctionResponseValue (dblLocalPredictorOrdinate):
				basisFunctionResponseDerivative (dblLocalPredictorOrdinate, i);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblBasisFunctionDeriv))
				throw new java.lang.Exception
					("LocalElasticConstitutiveState::localResponseValueDerivative => Cannot compute Basis Function Derivative");

			double dblShapeControllerPredictorOrdinate = _rssc.isLocal() ? dblLocalPredictorOrdinate :
				delocalize (dblLocalPredictorOrdinate);

			double dblShapeControlDeriv = iOrder == i ? _rssc.shapeController().evaluate
				(dblShapeControllerPredictorOrdinate) : _rssc.shapeController().calcDerivative
					(dblShapeControllerPredictorOrdinate, iOrder - i);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblShapeControlDeriv))
				throw new java.lang.Exception
					("LocalElasticConstitutiveState::localResponseDerivative => Cannot compute Shape Control Derivative");

			double dblShapeControllerDerivScale = 1.;

			if (!_rssc.isLocal()) {
				for (int j = 0; j < iOrder - i; ++j)
					dblShapeControllerDerivScale *= width();
			}

			dblResponseDerivative += (org.drip.quant.common.NumberUtil.NCK (iOrder, i) * dblBasisFunctionDeriv
				* dblShapeControllerDerivScale * dblShapeControlDeriv);
		}

		return dblResponseDerivative;
	}

	@Override protected org.drip.quant.calculus.WengertJacobian localDResponseDEdgeParams (
		final double dblLocalPredictorOrdinate)
	{
		int iNumResponseBasisCoeff = numBasis();

		org.drip.quant.calculus.WengertJacobian wjDResponseDEdgeParams = null;
		double[][] aadblDBasisCoeffDEdgeParams = new double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];

		double[] adblDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblLocalPredictorOrdinate);

		if (null == adblDResponseDBasisCoeff || iNumResponseBasisCoeff != adblDResponseDBasisCoeff.length)
			return null;

		org.drip.quant.calculus.WengertJacobian wjDBasisCoeffDEdgeParams = (null == _wjDBasisCoeffDEdgeValue)
			? jackDCoeffDEdgeParams() : _wjDBasisCoeffDEdgeValue;

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			for (int j = 0; j < iNumResponseBasisCoeff; ++j)
				aadblDBasisCoeffDEdgeParams[j][i] = wjDBasisCoeffDEdgeParams.getFirstDerivative (j, i);
		}

		try {
			if (!(wjDResponseDEdgeParams = new org.drip.quant.calculus.WengertJacobian (1,
				iNumResponseBasisCoeff)).setWengert (0, localResponseValue (dblLocalPredictorOrdinate)))
				return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			for (int j = 0; j < iNumResponseBasisCoeff; ++j) {
				if (!wjDResponseDEdgeParams.accumulatePartialFirstDerivative (0, i,
					adblDResponseDBasisCoeff[j] * aadblDBasisCoeffDEdgeParams[j][i]))
					return null;
			}
		}

		return wjDResponseDEdgeParams;
	}

	@Override protected org.drip.quant.calculus.WengertJacobian localDResponseDBasisCoeff (
		final double dblLocalPredictorOrdinate) {
		int iNumResponseBasisCoeff = numBasis();

		org.drip.quant.calculus.WengertJacobian wjDResponseDBasisCoeff = null;

		double[] adblBasisDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblLocalPredictorOrdinate);

		if (null == adblBasisDResponseDBasisCoeff || iNumResponseBasisCoeff !=
			adblBasisDResponseDBasisCoeff.length)
			return null;

		try {
			wjDResponseDBasisCoeff = new org.drip.quant.calculus.WengertJacobian (1, iNumResponseBasisCoeff);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			if (!wjDResponseDBasisCoeff.accumulatePartialFirstDerivative (0, i,
				adblBasisDResponseDBasisCoeff[i]))
				return null;
		}

		return wjDResponseDBasisCoeff;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams()
	{
		if (null != _wjDBasisCoeffDEdgeValue) return _wjDBasisCoeffDEdgeValue;

		int iNumResponseBasisCoeff = numBasis();

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

		return setJackDCoeffDEdge() ? _wjDBasisCoeffDEdgeValue : (_wjDBasisCoeffDEdgeValue = null);
	}

	@Override public int numBasis()
	{
		return _fs._aAUResponseBasis.length;
	}

	@Override public int numParameters()
	{
		return _sdic.getCk() + 2;
	}

	@Override public boolean calibrate (
		final org.drip.spline.params.SegmentCalibrationInputSet scis)
	{
		return null == scis ? false : calibrate (scis.predictorOrdinates(), scis.reponseValues(),
			scis.leftDeriv(), scis.rightDeriv(), scis.basisFunctionIndexConstraint (_fs._aAUResponseBasis,
				_rssc, this), scis.bestFitWeightedResponse());
	}

	@Override public boolean calibrate (
		final org.drip.spline.segment.ElasticConstitutiveState ecsPrev,
		final org.drip.spline.params.SegmentResponseValueConstraint srvc,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		if (null == srvc) return false;

		int iCk = _sdic.getCk();

		org.drip.spline.params.SegmentBestFitResponse sbfrSegment = null == sbfr ? null : sbfr.sizeToSegment
			(this);

		if (null == ecsPrev) {
			try {
				double[] adblLocalDerivAtLeftOrdinate = null;

				if (0 != iCk) {
					adblLocalDerivAtLeftOrdinate = new double[iCk];

					for (int i = 0; i < iCk; ++i)
						adblLocalDerivAtLeftOrdinate[i] = localResponseValueDerivative (0., i);
				}

				return calibrate (new double[] {0.}, new double[] {localResponseValue (0.)},
					adblLocalDerivAtLeftOrdinate, null, new
						org.drip.spline.params.SegmentIndexedBasisConstraint[]
							{srvc.responseBasisIndexConstraint (_fs._aAUResponseBasis, _rssc, this)},
								sbfrSegment);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			return false;
		}

		try {
			return calibrate (new double[] {0.}, new double[] {ecsPrev.localResponseValue (1.)}, 0 == iCk ?
				null : globalCk (left(), ecsPrev, iCk), null, new
					org.drip.spline.params.SegmentIndexedBasisConstraint[] {srvc.responseBasisIndexConstraint
						(_fs._aAUResponseBasis, _rssc, this)}, sbfrSegment);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public double dcpe()
		throws java.lang.Exception
	{
		double dblDCPE = 0.;
		int iNumBasis = _fs._aAUResponseBasis.length;

		org.drip.spline.params.SegmentFlexurePenaltyControl scpp = _sdic.curvaturePenaltyControl();

		if (null == scpp) scpp = new org.drip.spline.params.SegmentFlexurePenaltyControl (2, 1.);

		org.drip.spline.segment.BestFitFlexurePenalizer bfcp = new
			org.drip.spline.segment.BestFitFlexurePenalizer (scpp, null, null, this);

		for (int i = 0; i < iNumBasis; ++i) {
			for (int j = 0; j < iNumBasis; ++j)
				dblDCPE += _adblResponseBasisCoeff[i] * _adblResponseBasisCoeff[j] *
					bfcp.basisPairCurvaturePenalty (i, j);
		}

		return scpp.amplitude() * dblDCPE;
	}

	@Override public java.lang.String displayString()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append ("\t\t\t[" + left() + " => " + right() + "\n");

		for (int i = 0; i < _adblResponseBasisCoeff.length; ++i) {
			if (0 != i) sb.append ("  |  ");

			sb.append (_adblResponseBasisCoeff[i] + "\n");
		}

		return sb.toString();
	}

	@Override public double basisFunctionResponseDerivative (
		final double dblLocalPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		double dblDerivative = 0.;
		int iNumBasis = _fs._aAUResponseBasis.length;

		for (int i = 0; i < iNumBasis; ++i)
			dblDerivative += _adblResponseBasisCoeff[i] * _fs._aAUResponseBasis[i].calcDerivative
				(dblLocalPredictorOrdinate, iOrder);

		return dblDerivative;
	}

	@Override public double basisFunctionResponseValue (
		final double dblLocalPredictorOrdinate)
		throws java.lang.Exception
	{
		double dblResponse = 0.;
		int iNumBasis = _fs._aAUResponseBasis.length;

		for (int i = 0; i < iNumBasis; ++i)
			dblResponse += _adblResponseBasisCoeff[i] * _fs._aAUResponseBasis[i].evaluate
				(dblLocalPredictorOrdinate);

		return dblResponse;
	}

	@Override public double localSpecificBasisResponse (
		final double dblLocalPredictorOrdinate,
		final int iBasisFunctionIndex)
		throws java.lang.Exception
	{
		double dblResponse = _fs._aAUResponseBasis[iBasisFunctionIndex].evaluate (dblLocalPredictorOrdinate);

		return dblResponse * (null == _rssc ? 1. : _rssc.shapeController().evaluate (_rssc.isLocal() ?
			dblLocalPredictorOrdinate : delocalize (dblLocalPredictorOrdinate)));
	}

	@Override public double localSpecificBasisDerivative (
		final double dblLocalPredictorOrdinate,
		final int iOrder,
		final int iBasisFunctionIndex)
		throws java.lang.Exception
	{
		if (null == _rssc)
			return _fs._aAUResponseBasis[iBasisFunctionIndex].calcDerivative (dblLocalPredictorOrdinate,
				iOrder);

		double dblResponseDerivative = 0.;

		for (int i = 0; i <= iOrder; ++i) {
			double dblBasisFunctionDeriv = 0 == i ? _fs._aAUResponseBasis[iBasisFunctionIndex].evaluate
				(dblLocalPredictorOrdinate) : _fs._aAUResponseBasis[iBasisFunctionIndex].calcDerivative
					(dblLocalPredictorOrdinate, i);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblBasisFunctionDeriv))
				throw new java.lang.Exception
					("LocalElasticConstitutiveState::localSpecificBasisDerivative => Cannot compute Basis Function Derivative");

			double dblShapeControllerPredictorOrdinate = _rssc.isLocal() ? dblLocalPredictorOrdinate :
				delocalize (dblLocalPredictorOrdinate);

			double dblShapeControlDeriv = iOrder == i ? _rssc.shapeController().evaluate
				(dblShapeControllerPredictorOrdinate) : _rssc.shapeController().calcDerivative
					(dblShapeControllerPredictorOrdinate, iOrder - i);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblShapeControlDeriv))
				throw new java.lang.Exception
					("LocalElasticConstitutiveState::localSpecificBasisDerivative => Cannot compute Shape Control Derivative");

			double dblShapeControllerDerivScale = 1.;

			if (!_rssc.isLocal()) {
				for (int j = 0; j < iOrder - i; ++j)
					dblShapeControllerDerivScale *= width();
			}

			dblResponseDerivative += (org.drip.quant.common.NumberUtil.NCK (iOrder, i) *
				dblBasisFunctionDeriv * dblShapeControllerDerivScale * dblShapeControlDeriv);
		}

		return dblResponseDerivative;
	}
}

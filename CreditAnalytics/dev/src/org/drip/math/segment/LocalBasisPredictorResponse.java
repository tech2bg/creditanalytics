
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

public class LocalBasisPredictorResponse extends org.drip.math.segment.PredictorResponse {
	private static final int DISPLAY_SEGMENT_PREDICTOR_PARTITION = 5;

	private double[] _adblResponseBasisCoeff = null;
	private double[][] _aadblDResponseBasisCoeffDConstraint = null;
	private org.drip.math.segment.DesignInelasticParams _dip = null;
	private org.drip.math.function.AbstractUnivariate _auShapeControl = null;
	private org.drip.math.function.AbstractUnivariate[] _aAUResponseBasis = null;
	private org.drip.math.calculus.WengertJacobian _wjDBasisCoeffDEdgeParams = null;

	class CrossBasisDerivativeProduct extends org.drip.math.function.AbstractUnivariate {
		int _iOrder = -1;
		org.drip.math.function.AbstractUnivariate _aAU1 = null;
		org.drip.math.function.AbstractUnivariate _aAU2 = null;

		CrossBasisDerivativeProduct (
			final int iOrder,
			final org.drip.math.function.AbstractUnivariate aAU1,
			final org.drip.math.function.AbstractUnivariate aAU2)
		{
			super (null);

			_aAU1 = aAU1;
			_aAU2 = aAU2;
			_iOrder = iOrder;
		}

		public double evaluate (
			final double dblVariate)
			throws java.lang.Exception
		{
			return _aAU1.calcDerivative (dblVariate, _iOrder) * _aAU2.calcDerivative (dblVariate, _iOrder);
		}
	}

	/**
	 * Build the LocalBasisPredictorResponse instance from the Basis Set
	 * 
	 * @param dblLeftPredictorOrdinate Left Predictor Ordinate
	 * @param dblRightPredictorOrdinate Right Predictor Ordinate
	 * @param aAUResponseBasis Response Basis Set Functions
	 * @param auShapeControl Shape Control Basis Function
	 * @param dip Design Inelastic Parameters
	 * 
	 * @return Instance of PredictorResponseBasisSpline
	 */

	public static final org.drip.math.segment.LocalBasisPredictorResponse Create (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.math.function.AbstractUnivariate[] aAUResponseBasis,
		final org.drip.math.function.AbstractUnivariate auShapeControl,
		final org.drip.math.segment.DesignInelasticParams dip)
	{
		try {
			return new org.drip.math.segment.LocalBasisPredictorResponse (dblLeftPredictorOrdinate,
				dblRightPredictorOrdinate, aAUResponseBasis, auShapeControl, dip);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private double basisFunctionResponseDerivative (
		final double dblLocalPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		double dblDerivative = 0.;
		int iNumBasis = _aAUResponseBasis.length;

		for (int i = 0; i < iNumBasis; ++i)
			dblDerivative += _adblResponseBasisCoeff[i] * _aAUResponseBasis[i].calcDerivative
				(dblLocalPredictorOrdinate, iOrder);

		return dblDerivative;
	}

	private double basisFunctionResponse (
		final double dblLocalPredictorOrdinate)
		throws java.lang.Exception
	{
		double dblResponse = 0.;
		int iNumBasis = _aAUResponseBasis.length;

		for (int i = 0; i < iNumBasis; ++i)
			dblResponse += _adblResponseBasisCoeff[i] * _aAUResponseBasis[i].evaluate
				(dblLocalPredictorOrdinate);

		return dblResponse;
	}

	private double localSpecificBasisDerivative (
		final double dblLocalPredictorOrdinate,
		final int iOrder,
		final int iBasisIndex)
		throws java.lang.Exception
	{
		if (null == _auShapeControl)
			return _aAUResponseBasis[iBasisIndex].calcDerivative (dblLocalPredictorOrdinate, iOrder);

		double dblResponseDerivative = 0.;

		for (int i = 0; i <= iOrder; ++i) {
			double dblBasisFunctionDeriv = 0 == i ? _aAUResponseBasis[iBasisIndex].evaluate
				(dblLocalPredictorOrdinate) : _aAUResponseBasis[iBasisIndex].calcDerivative
					(dblLocalPredictorOrdinate, i);

			if (!org.drip.math.common.NumberUtil.IsValid (dblBasisFunctionDeriv))
				throw new java.lang.Exception
					("LocalBasisPredictorResponse::responseDerivative => Cannot compute Basis Function Derivative");

			double dblShapeControlDeriv = iOrder == i ? _auShapeControl.evaluate (dblLocalPredictorOrdinate) :
				_auShapeControl.calcDerivative (dblLocalPredictorOrdinate, iOrder - i);

			if (!org.drip.math.common.NumberUtil.IsValid (dblShapeControlDeriv))
				throw new java.lang.Exception
					("LocalBasisPredictorResponse::responseDerivative => Cannot compute Shape Control Derivative");

			dblResponseDerivative += (org.drip.math.common.NumberUtil.NCK (iOrder,  i) *
				dblBasisFunctionDeriv * dblShapeControlDeriv);
		}

		return dblResponseDerivative;
	}

	private boolean calibrate (
		final double[] adblPredictorOrdinate,
		final double[] adblResponse,
		final double[] adblLeftEdgeDeriv,
		final double[] adblRightEdgeDeriv,
		final org.drip.math.segment.ResponseBasisConstraint[] aRBC)
	{
		int iNumConstraint = null == aRBC ? 0 : aRBC.length;
		int iNumResponseBasisCoeff = _adblResponseBasisCoeff.length;
		int iNumLeftDeriv = null == adblLeftEdgeDeriv ? 0 : adblLeftEdgeDeriv.length;
		int iNumRightDeriv = null == adblRightEdgeDeriv ? 0 : adblRightEdgeDeriv.length;
		double[] adblPredictorResponseConstraintValue = new double[iNumResponseBasisCoeff];
		double[][] aadblResponseBasisCoeffConstraint = new
			double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];
		int iNumPredictorOrdinate = null == adblPredictorOrdinate ? 0 : adblPredictorOrdinate.length;

		if (iNumResponseBasisCoeff < iNumPredictorOrdinate + iNumLeftDeriv + iNumRightDeriv + iNumConstraint)
			return false;

		for (int j = 0; j < iNumResponseBasisCoeff; ++j) {
			if (j < iNumPredictorOrdinate)
				adblPredictorResponseConstraintValue[j] = adblResponse[j];
			else if (j < iNumPredictorOrdinate + iNumConstraint)
				adblPredictorResponseConstraintValue[j] = aRBC[j - iNumPredictorOrdinate].contraintValue();
			else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv)
				adblPredictorResponseConstraintValue[j] = adblLeftEdgeDeriv[j - iNumPredictorOrdinate -
				    iNumConstraint];
			else if (j < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv + iNumRightDeriv)
				adblPredictorResponseConstraintValue[j] = adblRightEdgeDeriv[j - iNumPredictorOrdinate -
				    iNumConstraint - iNumLeftDeriv];
			else
				adblPredictorResponseConstraintValue[j] = 0.;
		}

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			try {
				for (int l = 0; l < iNumResponseBasisCoeff; ++l) {
					double[] adblCalibBasisConstraintWeight = null;

					if (0 != iNumConstraint && (l >= iNumPredictorOrdinate && l < iNumPredictorOrdinate +
						iNumConstraint))
						adblCalibBasisConstraintWeight = aRBC[l -
						    iNumPredictorOrdinate].responseBasisCoeffWeights();

					if (l < iNumPredictorOrdinate)
						aadblResponseBasisCoeffConstraint[l][i] = _aAUResponseBasis[i].evaluate
							(adblPredictorOrdinate[l]);
					else if (l < iNumPredictorOrdinate + iNumConstraint)
						aadblResponseBasisCoeffConstraint[l][i] = adblCalibBasisConstraintWeight[i];
					else if (l < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv)
						aadblResponseBasisCoeffConstraint[l][i] = localSpecificBasisDerivative (0., l -
							iNumPredictorOrdinate - iNumConstraint + 1, i);
					else if (l < iNumPredictorOrdinate + iNumConstraint + iNumLeftDeriv + iNumRightDeriv)
						aadblResponseBasisCoeffConstraint[l][i] = localSpecificBasisDerivative (1., l -
							iNumPredictorOrdinate - iNumConstraint - iNumLeftDeriv + 1, i);
					else
						aadblResponseBasisCoeffConstraint[l][i] = org.drip.math.calculus.Integrator.Boole
							(new CrossBasisDerivativeProduct (_dip.getRoughnessPenaltyDerivativeOrder(),
								_aAUResponseBasis[i], _aAUResponseBasis[l]), 0., 1.);
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		org.drip.math.linearalgebra.LinearizationOutput lss =
			org.drip.math.linearalgebra.LinearSystemSolver.SolveUsingMatrixInversion
				(aadblResponseBasisCoeffConstraint, adblPredictorResponseConstraintValue);

		if (null == lss && null == (lss =
			org.drip.math.linearalgebra.LinearSystemSolver.SolveUsingGaussianElimination
				(aadblResponseBasisCoeffConstraint, adblPredictorResponseConstraintValue)))
			return false;

		double[] adblCalibResponseBasisCoeff = lss.getTransformedRHS();

		if (null == adblCalibResponseBasisCoeff || adblCalibResponseBasisCoeff.length !=
			iNumResponseBasisCoeff || null == (_aadblDResponseBasisCoeffDConstraint =
				lss.getTransformedMatrix()) || _aadblDResponseBasisCoeffDConstraint.length !=
					iNumResponseBasisCoeff || _aadblDResponseBasisCoeffDConstraint[0].length !=
						iNumResponseBasisCoeff)
			return false;

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			if (!org.drip.math.common.NumberUtil.IsValid (_adblResponseBasisCoeff[i] =
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
				if (!_wjDBasisCoeffDEdgeParams.accumulatePartialFirstDerivative (i, j,
					_aadblDResponseBasisCoeffDConstraint[i][j]))
					return false;
			}
		}

		return true;
	}

	private double[] basisDResponseDBasisCoeff (
		final double dblLocalPredictorOrdinate)
	{
		int iNumBasis = _aAUResponseBasis.length;
		double[] adblDResponseDBasisCoeff = new double[iNumBasis];

		for (int i = 0; i < iNumBasis; ++i) {
			try {
				adblDResponseDBasisCoeff[i] = _aAUResponseBasis[i].evaluate (dblLocalPredictorOrdinate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return adblDResponseDBasisCoeff;
	}

	protected LocalBasisPredictorResponse (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.math.function.AbstractUnivariate[] aAUResponseBasis,
		final org.drip.math.function.AbstractUnivariate auShapeControl,
		final org.drip.math.segment.DesignInelasticParams dip)
		throws java.lang.Exception
	{
		super (dblLeftPredictorOrdinate, dblRightPredictorOrdinate);

		if (null == (_aAUResponseBasis = aAUResponseBasis) || null == (_dip = dip))
			throw new java.lang.Exception ("LocalBasisPredictorResponse ctr: Invalid Basis Functions!");

		_auShapeControl = auShapeControl;
		int iNumBasis = _aAUResponseBasis.length;
		_adblResponseBasisCoeff = new double[iNumBasis];

		if (0 >= iNumBasis || _dip.getCk() > iNumBasis - 2)
			throw new java.lang.Exception ("LocalBasisPredictorResponse ctr: Invalid inputs!");
	}

	@Override protected boolean isMonotone()
	{
		return 1 >= _dip.getCk();
	}

	@Override protected double localResponse (
		final double dblLocalPredictorOrdinate)
		throws java.lang.Exception
	{
		return null == _auShapeControl ? basisFunctionResponse (dblLocalPredictorOrdinate) :
			basisFunctionResponse (dblLocalPredictorOrdinate) * _auShapeControl.evaluate
				(dblLocalPredictorOrdinate);
	}

	@Override protected PredictorResponse snipLeftOfLocalPredictorOrdinate (
		final double dblLocalPredictorOrdinate)
	{
		try {
			LocalBasisPredictorResponse lbpr = new LocalBasisPredictorResponse (delocalize
				(dblLocalPredictorOrdinate), right(), _aAUResponseBasis, _auShapeControl, _dip);

			int iCk = _dip.getCk();

			double[] adblCalibLeftEdgeDeriv = 0 != iCk ? lbpr.translateCkFromSegment (delocalize
				(dblLocalPredictorOrdinate), this, iCk) : null;

			return lbpr.calibrate (new double[] {0., 1.}, new double[] {localResponse
				(dblLocalPredictorOrdinate), localResponse (1.)}, adblCalibLeftEdgeDeriv, null, null) ? lbpr
					: null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override protected PredictorResponse snipRightOfLocalPredictorOrdinate (
		final double dblLocalPredictorOrdinate)
	{
		try {
			LocalBasisPredictorResponse lbpr = new LocalBasisPredictorResponse (left(), delocalize
				(dblLocalPredictorOrdinate), _aAUResponseBasis, _auShapeControl, _dip);

			int iCk = _dip.getCk();

			double[] adblCalibLeftEdgeDeriv = 0 != iCk ? lbpr.translateCkFromSegment (left(), this, iCk) :
				null;

			return lbpr.calibrate (new double[] {0., 1.}, new double[] {localResponse (0.), localResponse
				(dblLocalPredictorOrdinate)}, adblCalibLeftEdgeDeriv, null, null) ? lbpr : null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override protected double localResponseDerivative (
		final double dblLocalPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (null == _auShapeControl)
			return basisFunctionResponseDerivative (dblLocalPredictorOrdinate, iOrder);

		double dblResponseDerivative = 0.;

		for (int i = 0; i <= iOrder; ++i) {
			double dblBasisFunctionDeriv = 0 == i ? basisFunctionResponse (dblLocalPredictorOrdinate):
				basisFunctionResponseDerivative (dblLocalPredictorOrdinate, i);

			if (!org.drip.math.common.NumberUtil.IsValid (dblBasisFunctionDeriv))
				throw new java.lang.Exception
					("LocalBasisPredictorResponse::responseDerivative => Cannot compute Basis Function Derivative");

			double dblShapeControlDeriv = iOrder == i ? _auShapeControl.evaluate (dblLocalPredictorOrdinate) :
				_auShapeControl.calcDerivative (dblLocalPredictorOrdinate, iOrder - i);

			if (!org.drip.math.common.NumberUtil.IsValid (dblShapeControlDeriv))
				throw new java.lang.Exception
					("LocalBasisPredictorResponse::responseDerivative => Cannot compute Shape Control Derivative");

			dblResponseDerivative += (org.drip.math.common.NumberUtil.NCK (iOrder,  i) *
				dblBasisFunctionDeriv * dblShapeControlDeriv);
		}

		return dblResponseDerivative;
	}

	@Override protected org.drip.math.calculus.WengertJacobian localDResponseDEdgeParams (
		final double dblLocalPredictorOrdinate)
	{
		int iNumResponseBasisCoeff = numBasis();

		org.drip.math.calculus.WengertJacobian wjDResponseDEdgeParams = null;
		double[][] aadblDBasisCoeffDEdgeParams = new double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];

		double[] adblDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblLocalPredictorOrdinate);

		if (null == adblDResponseDBasisCoeff || iNumResponseBasisCoeff != adblDResponseDBasisCoeff.length)
			return null;

		org.drip.math.calculus.WengertJacobian wjDBasisCoeffDEdgeParams = (null == _wjDBasisCoeffDEdgeParams)
			? jackDCoeffDEdgeParams() : _wjDBasisCoeffDEdgeParams;

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			for (int j = 0; j < iNumResponseBasisCoeff; ++j)
				aadblDBasisCoeffDEdgeParams[j][i] = wjDBasisCoeffDEdgeParams.getFirstDerivative (j, i);
		}

		try {
			if (!(wjDResponseDEdgeParams = new org.drip.math.calculus.WengertJacobian (1,
				iNumResponseBasisCoeff)).setWengert (0, localResponse (dblLocalPredictorOrdinate)))
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

	@Override protected org.drip.math.calculus.WengertJacobian localDResponseDBasisCoeff (
		final double dblLocalPredictorOrdinate) {
		int iNumResponseBasisCoeff = numBasis();

		org.drip.math.calculus.WengertJacobian wjDResponseDBasisCoeff = null;

		double[] adblBasisDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblLocalPredictorOrdinate);

		if (null == adblBasisDResponseDBasisCoeff || iNumResponseBasisCoeff !=
			adblBasisDResponseDBasisCoeff.length)
			return null;

		try {
			wjDResponseDBasisCoeff = new org.drip.math.calculus.WengertJacobian (1, iNumResponseBasisCoeff);
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

	@Override public org.drip.math.calculus.WengertJacobian jackDCoeffDEdgeParams()
	{
		if (null != _wjDBasisCoeffDEdgeParams) return _wjDBasisCoeffDEdgeParams;

		int iNumResponseBasisCoeff = numBasis();

		try {
			_wjDBasisCoeffDEdgeParams = new org.drip.math.calculus.WengertJacobian (iNumResponseBasisCoeff,
				iNumResponseBasisCoeff);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return _wjDBasisCoeffDEdgeParams = null;
		}

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			if (!_wjDBasisCoeffDEdgeParams.setWengert (i, _adblResponseBasisCoeff[i]))
				return _wjDBasisCoeffDEdgeParams = null;
		}

		return setJackDCoeffDEdge() ? _wjDBasisCoeffDEdgeParams : (_wjDBasisCoeffDEdgeParams = null);
	}

	/**
	 * Get the Ck constraint number
	 * 
	 * @return The "k" in Ck
	 */

	public int getCk()
	{
		return _dip.getCk();
	}

	/**
	 * Retrieve the Shape Control
	 * 
	 * @return The Shape Control
	 */

	public org.drip.math.function.AbstractUnivariate getShapeControl()
	{
		return _auShapeControl;
	}

	@Override public int numBasis()
	{
		return _aAUResponseBasis.length;
	}

	@Override public int numParameters()
	{
		return _dip.getCk() + 2;
	}

	@Override public boolean calibrate (
		final org.drip.math.segment.CalibrationParams cp)
	{
		return null == cp ? false : calibrate (cp.predictorOrdinates(), cp.reponses(), cp.leftDeriv(),
			cp.rightDeriv(), cp.getBasisFunctionConstraint (_aAUResponseBasis, this));
	}

	@Override public boolean calibrate (
		final org.drip.math.segment.PredictorResponse prPrev,
		final org.drip.math.segment.ResponseValueConstraint rvc)
	{
		if (null == rvc) return false;

		int iCk = _dip.getCk();

		if (null == prPrev) {
			try {
				double[] adblLocalDerivAtLeftOrdinate = null;

				if (0 != iCk) {
					adblLocalDerivAtLeftOrdinate = new double[iCk];

					for (int i = 0; i < iCk; ++i)
						adblLocalDerivAtLeftOrdinate[i] = localResponseDerivative (0., i);
				}

				return calibrate (new double[] {0.}, new double[] {localResponse (0.)},
					adblLocalDerivAtLeftOrdinate, null, new org.drip.math.segment.ResponseBasisConstraint[]
						{rvc.responseBasisConstraint (_aAUResponseBasis, this)});
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			return false;
		}

		try {
			return calibrate (new double[] {0.}, new double[] {prPrev.localResponse (1.)}, 0 == iCk ? null :
				translateCkFromSegment (left(), prPrev, iCk), null, new
					org.drip.math.segment.ResponseBasisConstraint[] {rvc.responseBasisConstraint
						(_aAUResponseBasis, this)});
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public java.lang.String displayString()
	{
		double dblSegmentPartitionResponse = java.lang.Double.NaN;

		double dblGlobalPredictorOrdinateLeft = left();

		double dblGlobalSegmentPredictorWidth = width() / DISPLAY_SEGMENT_PREDICTOR_PARTITION;

		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		for (int i = 0; i <= DISPLAY_SEGMENT_PREDICTOR_PARTITION; ++i) {
			double dblGlobalPredictorOrdinate = dblGlobalPredictorOrdinateLeft + i *
				dblGlobalSegmentPredictorWidth;

			try {
				dblSegmentPartitionResponse = calcResponse (dblGlobalPredictorOrdinate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			sb.append ("\t\t\t" + dblGlobalPredictorOrdinate + " = " + dblSegmentPartitionResponse + "\n");
		}

		return sb.toString();
	}
}

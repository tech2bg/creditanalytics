
package org.drip.math.spline;

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

public class SegmentCk extends org.drip.math.grid.Segment {
	private double[][] _aadblF = null;
	private double[][] _aadblFInv = null;
	private org.drip.math.calculus.WengertJacobian _wjMicro = null;
	private org.drip.math.function.AbstractUnivariate[] _aAUBasis = null;
	private org.drip.math.spline.SegmentInelasticParams _segParams = null;
	private org.drip.math.function.AbstractUnivariate _auShapeControl = null;

	protected double[] _adblCoeff = null;

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

	private double derivativeBasis (
		final double dblX,
		final int iOrder)
		throws java.lang.Exception
	{
		double dblDerivative = 0.;

		for (int i = 0; i < _aAUBasis.length; ++i)
			dblDerivative += _adblCoeff[i] * _aAUBasis[i].calcDerivative (dblX, iOrder);

		return dblDerivative;
	}

	private double yBasis (
		final double dblX)
		throws java.lang.Exception
	{
		double dblY = 0.;

		for (int i = 0; i < _aAUBasis.length; ++i)
			dblY += _adblCoeff[i] * _aAUBasis[i].evaluate (dblX);

		return dblY;
	}

	protected SegmentCk (
		final double dblLeft,
		final double dblRight,
		final org.drip.math.function.AbstractUnivariate[] aAUBasis,
		final org.drip.math.function.AbstractUnivariate auShapeControl,
		final org.drip.math.spline.SegmentInelasticParams segParams)
		throws java.lang.Exception
	{
		super (dblLeft, dblRight);

		if (null == (_aAUBasis = aAUBasis) || null == (_segParams = segParams))
			throw new java.lang.Exception ("SegmentCk ctr: Invalid Basis Functions!");

		int iNumBasis = _aAUBasis.length;
		_auShapeControl = auShapeControl;
		_adblCoeff = new double[iNumBasis];

		if (0 >= iNumBasis || _segParams.getCk() > iNumBasis - 2)
			throw new java.lang.Exception ("SegmentCk ctr: Invalid inputs!");
	}

	protected boolean y1 (
		final double dblYRight)
	{
		try {
			if (0 == _segParams.getCk()) return calibrate (y (0.), null, dblYRight, null);

			double[] adblLeftDeriv = new double[_segParams.getCk()];

			for (int i = 0; i < _segParams.getCk(); ++i)
				adblLeftDeriv[i] = derivative (0., i);

			return calibrate (y (0.), adblLeftDeriv, dblYRight, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	protected boolean coeffCkMicroJack()
	{
		if (null == _aadblFInv) return false;

		int iSize = _aadblFInv.length;

		for (int i = 0; i < iSize; ++i) {
			for (int j = 0; j < iSize; ++j) {
				if (!accumulateJacobian (i, j,  _aadblFInv[i][j])) return false;
			}
		}

		return true;
	}

	protected double[] valueCoeffMicroJack (
		final double dblX)
	{
		double[] adblDValueDCoeff = new double[_aAUBasis.length];

		for (int i = 0; i < _aAUBasis.length; ++i) {
			try {
				adblDValueDCoeff[i] = _aAUBasis[i].evaluate (dblX);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return adblDValueDCoeff;
	}

	protected boolean accumulateJacobian (
		final int iCoeffIndex,
		final int iCkIndex,
		final double dblJacobianEntry)
	{
		return _wjMicro.accumulatePartialFirstDerivative (iCoeffIndex, iCkIndex, dblJacobianEntry);
	}

	@Override protected boolean isMonotone()
	{
		return 1 >= _segParams.getCk();
	}

	@Override public double y (
		final double dblX)
		throws java.lang.Exception
	{
		return null == _auShapeControl ? yBasis (dblX) : yBasis (dblX) * _auShapeControl.evaluate (dblX);
	}

	@Override public double derivative (
		final double dblX,
		final int iOrder)
		throws java.lang.Exception
	{
		if (null == _auShapeControl) return derivativeBasis (dblX, iOrder);

		double dblDerivative = 0.;

		for (int i = 0; i <= iOrder; ++i) {
			double dblInterpDeriv = 0 == i ? yBasis (dblX): derivativeBasis (dblX, i);

			double dblShapeControlDeriv = iOrder == i ? _auShapeControl.evaluate (dblX) :
				_auShapeControl.calcDerivative (dblX, iOrder - i);

			dblDerivative += (org.drip.math.common.NumberUtil.NCK (iOrder,  i) * dblInterpDeriv *
				dblShapeControlDeriv);
		}

		return dblDerivative;
	}

	/**
	 * Get the Ck constraint number
	 * 
	 * @return The "k" in Ck
	 */

	public int getCk()
	{
		return _segParams.getCk();
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
		return _aAUBasis.length;
	}

	@Override public int numParameters()
	{
		return _segParams.getCk() + 2;
	}

	@Override public double calcOrderedDerivative (
		final double dblPoint,
		final int iOrder,
		final boolean bLocal)
		throws java.lang.Exception
	{
		double dblX = calcNormalizedOrdinate (dblPoint);

		if (0 == iOrder) return y (dblX);

		if (_segParams.getCk() < iOrder && (0. == dblPoint || 1. == dblPoint))
			throw new java.lang.Exception ("SegmentCk::calcOrderedDerivative => Segment Discontinuous: C" +
				_segParams.getCk() + " less than deriv order " + iOrder + " at segment edges!");

		double dblDeriv = derivative (dblX, iOrder);

		if (bLocal) return dblDeriv;

		double dblSpan = getSpan();

		for (int i = 0; i < iOrder; ++i)
			dblDeriv /= dblSpan;

		return dblDeriv;
	}

	@Override public boolean calibrate (
		final double dblYLeft,
		final double[] adblLeftDeriv,
		final double dblYRight,
		final double[] adblRightDeriv)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblYLeft) || !org.drip.math.common.NumberUtil.IsValid
			(dblYRight))
			return false;

		int iNumLeftDeriv = 0;
		int iNumCoeff = _adblCoeff.length;
		double[] adblRHS = new double[iNumCoeff];
		_aadblF = new double[iNumCoeff][iNumCoeff];
		int iNumRightDeriv = null == adblRightDeriv ? 0 : adblRightDeriv.length;

		int iCk = _segParams.getCk();

		org.drip.math.spline.SegmentConstraint lc = _segParams.getLinearConstraint();

		int iNumConstraint = null == lc ? 0 : lc.size();

		if ((null == adblLeftDeriv || 0 == (iNumLeftDeriv = adblLeftDeriv.length)) && 2 == iNumCoeff) {
			if (0 != iCk) return false;

			_aadblF[0][0] = 1.;
			_aadblF[0][1] = 0.;
			_aadblF[1][0] = 1.;
			_aadblF[1][1] = 1.;
			_adblCoeff[1] = dblYRight - (_adblCoeff[0] = dblYLeft);

			org.drip.math.linearalgebra.LinearizationOutput lss =
				org.drip.math.linearalgebra.LinearSystemSolver.SolveUsingGaussianElimination (_aadblF,
					adblRHS);

			return null != lss && null != (_aadblFInv = lss.getTransformedMatrix());
		}

		if (iCk > iNumLeftDeriv || iNumCoeff < 2 + iNumConstraint + iCk + iNumRightDeriv) return false;

		for (int j = 0; j < iNumCoeff; ++j) {
			if (j < 2)
				adblRHS[j] = 0 == j ? dblYLeft : dblYRight;
			else if (j < 2 + iNumConstraint)
				adblRHS[j] = lc.getValue (j - 2);
			else if (j < 2 + iNumConstraint + iCk)
				adblRHS[j] = adblLeftDeriv[j - 2 - iNumConstraint];
			else if (j < 2 + iNumConstraint + iCk + iNumRightDeriv)
				adblRHS[j] = adblRightDeriv[j - 2 - iNumConstraint - iCk];
			else
				adblRHS[j] = 0.;
		}

		for (int i = 0; i < iNumCoeff; ++i) {
			try {
				for (int l = 0; l < iNumCoeff; ++l) {
					if (l < 2)
						_aadblF[l][i] = _aAUBasis[i].evaluate (l);
					else if (l < 2 + iNumConstraint)
						_aadblF[l][i] = lc.getValue (l - 2, i);
					else if (l < 2 + iNumConstraint + iCk)
						_aadblF[l][i] = _aAUBasis[i].calcDerivative (0., l - 1 - iNumConstraint);
					else if (l < 2 + iNumConstraint + iCk + iNumRightDeriv)
						_aadblF[l][i] = _aAUBasis[i].calcDerivative (1., l - 1 - iNumConstraint - iCk);
					else
						_aadblF[l][i] = org.drip.math.calculus.Integrator.Boole (new
							CrossBasisDerivativeProduct (_segParams.getRoughnessPenaltyDerivativeOrder(),
								_aAUBasis[i], _aAUBasis[l]), 0., 1.);
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		org.drip.math.linearalgebra.LinearizationOutput lss =
			org.drip.math.linearalgebra.LinearSystemSolver.SolveUsingMatrixInversion (_aadblF, adblRHS);

		if (null == lss && null == (lss =
			org.drip.math.linearalgebra.LinearSystemSolver.SolveUsingGaussianElimination (_aadblF, adblRHS)))
			return false;

		double[] adblCoeff = lss.getTransformedRHS();

		if (null == adblCoeff || adblCoeff.length != iNumCoeff || null == (_aadblFInv =
			lss.getTransformedMatrix()) || _aadblFInv.length != iNumCoeff || _aadblFInv[0].length !=
				iNumCoeff)
			return false;

		for (int i = 0; i < iNumCoeff; ++i) {
			if (!org.drip.math.common.NumberUtil.IsValid (_adblCoeff[i] = adblCoeff[i])) return false;
		}

		return true;
	}

	@Override public boolean calibrate (
		final org.drip.math.grid.Segment segPrev,
		final double dblY1)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblY1)) return false;

		if (null == segPrev) return y1 (dblY1);

		double dblXLeft = getLeft();

		double dblWidth = getSpan();

		try {
			double dblScale = 1.;
			double[] adblDeriv = null;

			int iCK = _segParams.getCk();

			if (0 != iCK) {
				adblDeriv = new double[iCK];

				for (int i = 0; i < iCK; ++i) {
					dblScale *= dblWidth;

					adblDeriv[i] = segPrev.calcOrderedDerivative (dblXLeft, i + 1, false) * dblScale;
				}
			}

			return calibrate (segPrev.calcValue (dblXLeft), adblDeriv, dblY1, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public org.drip.math.calculus.WengertJacobian calcJacobian()
	{
		if (null != _wjMicro) return _wjMicro;

		int iNumCoeffs = numBasis();

		try {
			_wjMicro = new org.drip.math.calculus.WengertJacobian (iNumCoeffs, iNumCoeffs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return _wjMicro = null;
		}

		for (int i = 0; i < iNumCoeffs; ++i) {
			if (!_wjMicro.setWengert (i, _adblCoeff[i])) return _wjMicro = null;
		}

		return coeffCkMicroJack() ? _wjMicro : (_wjMicro = null);
	}

	@Override public org.drip.math.calculus.WengertJacobian calcValueJacobian (
		final double dblPoint)
	{
		int iNumCoeff = numBasis();

		double dblX = java.lang.Double.NaN;
		org.drip.math.calculus.WengertJacobian wjValue = null;
		double[][] aadblDCoeffDCk = new double[iNumCoeff][iNumCoeff];

		try {
			dblX = calcNormalizedOrdinate (dblPoint);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double[] adblDVDCoeff = valueCoeffMicroJack (dblX);

		if (null == adblDVDCoeff || iNumCoeff != adblDVDCoeff.length) return null;

		org.drip.math.calculus.WengertJacobian wj = (null == _wjMicro) ? calcJacobian() : _wjMicro;

		for (int i = 0; i < iNumCoeff; ++i) {
			for (int j = 0; j < iNumCoeff; ++j)
				aadblDCoeffDCk[j][i] = wj.getFirstDerivative (j, i);
		}

		try {
			if (!(wjValue = new org.drip.math.calculus.WengertJacobian (1, iNumCoeff)).setWengert (0, y
				(dblX)))
				return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumCoeff; ++i) {
			for (int j = 0; j < iNumCoeff; ++j) {
				if (!wjValue.accumulatePartialFirstDerivative (0, i, adblDVDCoeff[j] * aadblDCoeffDCk[j][i]))
					return null;
			}
		}

		return wjValue;
	}

	@Override public org.drip.math.calculus.WengertJacobian calcValueElasticJacobian (
		final double dblPoint) {
		int iNumCoeff = numBasis();

		double dblX = java.lang.Double.NaN;
		org.drip.math.calculus.WengertJacobian wjElastic = null;

		try {
			dblX = calcNormalizedOrdinate (dblPoint);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double[] adblDVDCoeff = valueCoeffMicroJack (dblX);

		if (null == adblDVDCoeff || iNumCoeff != adblDVDCoeff.length) return null;

		try {
			wjElastic = new org.drip.math.calculus.WengertJacobian (1, iNumCoeff);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumCoeff; ++i) {
			if (!wjElastic.accumulatePartialFirstDerivative (0, i, adblDVDCoeff[i])) return null;
		}

		return wjElastic;
	}

	@Override public java.lang.String displayString()
	{
		int iNumPartitions = 5;
		double dblValue = java.lang.Double.NaN;

		double dblStart = getLeft();

		double dblStepWidth = getSpan() / iNumPartitions;

		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		for (int i = 0; i <= iNumPartitions; ++i) {
			double dblPoint = dblStart + i * dblStepWidth;

			try {
				dblValue = calcValue (dblPoint);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			sb.append ("\t\t\t" + dblPoint + " = " + dblValue + "\n");
		}

		return sb.toString();
	}
}

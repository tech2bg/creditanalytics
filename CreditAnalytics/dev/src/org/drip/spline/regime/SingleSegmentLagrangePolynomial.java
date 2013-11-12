	
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
 * This class implements the regime using the Lagrange Polynomial Estimator. It provides the following
 * 	functionality:
 * 	- At any location inside the regime calculate the estimated value and the Jacobian to the input.
 * 	- Estimate convexity, and if it is co-convex.
 * 	- Estimate monotonicity, if it is co-monotone, and it if is locally monotone.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SingleSegmentLagrangePolynomial implements org.drip.spline.regime.SingleSegmentSequence {
	private static final double DIFF_SCALE = 1.0e-06;
	private static final int MAXIMA_PREDICTOR_ORDINATE_NODE = 1;
	private static final int MINIMA_PREDICTOR_ORDINATE_NODE = 2;
	private static final int MONOTONE_PREDICTOR_ORDINATE_NODE = 4;

	private double[] _adblResponseValue = null;
	private double[] _adblPredictorOrdinate = null;

	private static final double CalcAbsoluteMin (
		final double[] adblY)
		throws java.lang.Exception
	{
		if (null == adblY)
			throw new java.lang.Exception
				("SingleSegmentLagrangePolynomial::CalcAbsoluteMin => Invalid Inputs");

		int iNumPoints = adblY.length;

		if (1 >= iNumPoints)
			throw new java.lang.Exception
				("SingleSegmentLagrangePolynomial::CalcAbsoluteMin => Invalid Inputs");

		double dblMin = java.lang.Math.abs (adblY[0]);

		for (int i = 0; i < iNumPoints; ++i) {
			double dblValue = java.lang.Math.abs (adblY[i]);

			dblMin = dblMin > dblValue ? dblValue : dblMin;
		}

		return dblMin;
	}

	private static final double CalcMinDifference (
		final double[] adblY)
		throws java.lang.Exception
	{
		if (null == adblY)
			throw new java.lang.Exception
				("SingleSegmentLagrangePolynomial::CalcMinDifference => Invalid Inputs");

		int iNumPoints = adblY.length;

		if (1 >= iNumPoints)
			throw new java.lang.Exception
				("SingleSegmentLagrangePolynomial::CalcMinDifference => Invalid Inputs");

		double dblMinDiff = java.lang.Math.abs (adblY[0] - adblY[1]);

		for (int i = 0; i < iNumPoints; ++i) {
			for (int j = i + 1; j < iNumPoints; ++j) {
				double dblDiff = java.lang.Math.abs (adblY[i] - adblY[j]);

				dblMinDiff = dblMinDiff > dblDiff ? dblDiff : dblMinDiff;
			}
		}

		return dblMinDiff;
	}

	private static final double EstimateBumpDelta (
		final double[] adblY)
		throws java.lang.Exception
	{
		double dblBumpDelta = CalcMinDifference (adblY);

		if (!org.drip.quant.common.NumberUtil.IsValid (dblBumpDelta) || 0. == dblBumpDelta)
			dblBumpDelta = CalcAbsoluteMin (adblY);

		return 0. == dblBumpDelta ? DIFF_SCALE : dblBumpDelta * DIFF_SCALE;
	}

	/**
	 * LagrangePolynomialRegime constructor
	 * 
	 * @param adblPredictorOrdinate Array of Predictor Ordinates
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public SingleSegmentLagrangePolynomial (
		final double[] adblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (null == (_adblPredictorOrdinate = adblPredictorOrdinate))
			throw new java.lang.Exception ("SingleSegmentLagrangePolynomial ctr: Invalid Inputs");

		int iNumPredictorOrdinate = _adblPredictorOrdinate.length;

		if (1 >= iNumPredictorOrdinate)
			throw new java.lang.Exception ("SingleSegmentLagrangePolynomial ctr: Invalid Inputs");

		for (int i = 0; i < iNumPredictorOrdinate; ++i) {
			for (int j = i + 1; j < iNumPredictorOrdinate; ++j) {
				if (_adblPredictorOrdinate[i] == _adblPredictorOrdinate[j])
					throw new java.lang.Exception ("SingleSegmentLagrangePolynomial ctr: Invalid Inputs");
			}
		}
	}

	@Override public boolean setup (
		final double dblYLeading,
		final double[] adblResponseValue,
		final org.drip.spline.params.RegimeBestFitResponse rbfr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		return null != (_adblResponseValue = adblResponseValue) && _adblResponseValue.length ==
			_adblPredictorOrdinate.length;
	}

	@Override public double responseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception
				("SingleSegmentLagrangePolynomial::responseValue => Invalid inputs!");

		int iNumPredictorOrdinate = _adblPredictorOrdinate.length;

		if (_adblPredictorOrdinate[0] > dblPredictorOrdinate ||
			_adblPredictorOrdinate[iNumPredictorOrdinate - 1] < dblPredictorOrdinate)
			throw new java.lang.Exception
				("SingleSegmentLagrangePolynomial::responseValue => Input out of range!");

		double dblResponse = 0;

		for (int i = 0; i < iNumPredictorOrdinate; ++i) {
			double dblResponsePredictorOrdinateContribution = _adblResponseValue[i];

			for (int j = 0; j < iNumPredictorOrdinate; ++j) {
				if (i != j)
					dblResponsePredictorOrdinateContribution = dblResponsePredictorOrdinateContribution *
						(dblPredictorOrdinate - _adblPredictorOrdinate[j]) / (_adblPredictorOrdinate[i] -
							_adblPredictorOrdinate[j]);
			}

			dblResponse += dblResponsePredictorOrdinateContribution;
		}

		return dblResponse;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDResponseDResponseInput (
		final double dblPredictorOrdinate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate)) return null;

		int iNumPredictorOrdinate = _adblPredictorOrdinate.length;
		double dblInputResponseSensitivityShift = java.lang.Double.NaN;
		double dblResponseWithUnadjustedResponseInput = java.lang.Double.NaN;
		org.drip.quant.calculus.WengertJacobian wjDResponseDResponseInput = null;

		if (_adblPredictorOrdinate[0] > dblPredictorOrdinate ||
			_adblPredictorOrdinate[iNumPredictorOrdinate - 1] < dblPredictorOrdinate)
			return null;

		try {
			if (!org.drip.quant.common.NumberUtil.IsValid (dblInputResponseSensitivityShift =
				EstimateBumpDelta (_adblResponseValue)) || !org.drip.quant.common.NumberUtil.IsValid
					(dblResponseWithUnadjustedResponseInput = responseValue (dblPredictorOrdinate)))
				return null;

			wjDResponseDResponseInput = new org.drip.quant.calculus.WengertJacobian (1,
				iNumPredictorOrdinate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumPredictorOrdinate; ++i) {
			double[] adblSensitivityShiftedInputResponse = new double[iNumPredictorOrdinate];

			for (int j = 0; j < iNumPredictorOrdinate; ++j)
				adblSensitivityShiftedInputResponse[j] = i == j ? _adblResponseValue[j] +
					dblInputResponseSensitivityShift : _adblResponseValue[j];

			try {
				SingleSegmentLagrangePolynomial lps = new SingleSegmentLagrangePolynomial (_adblPredictorOrdinate);

				if (!lps.setup (adblSensitivityShiftedInputResponse[0], adblSensitivityShiftedInputResponse,
					null, org.drip.spline.regime.MultiSegmentSequence.BOUNDARY_CONDITION_FLOATING,
						org.drip.spline.regime.MultiSegmentSequence.CALIBRATE) ||
							!wjDResponseDResponseInput.accumulatePartialFirstDerivative (0, i,
								(lps.responseValue (dblPredictorOrdinate) -
									dblResponseWithUnadjustedResponseInput) /
										dblInputResponseSensitivityShift))
					return null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return wjDResponseDResponseInput;
	}

	@Override public org.drip.spline.segment.Monotonocity monotoneType (
		final double dblPredictorOrdinate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate)) return null;

		int iNumPredictorOrdinate = _adblPredictorOrdinate.length;

		if (_adblPredictorOrdinate[0] > dblPredictorOrdinate ||
			_adblPredictorOrdinate[iNumPredictorOrdinate - 1] < dblPredictorOrdinate)
			return null;

		if (2 == iNumPredictorOrdinate) {
			try {
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MONOTONIC);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		org.drip.quant.function1D.AbstractUnivariate auDeriv = new
			org.drip.quant.function1D.AbstractUnivariate (null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				double dblDeltaX = CalcMinDifference (_adblPredictorOrdinate) * DIFF_SCALE;

				return (responseValue (dblX + dblDeltaX) - responseValue (dblX)) / dblDeltaX;
			}
		};

		try {
			org.drip.quant.solver1D.FixedPointFinderOutput fpop = new
				org.drip.quant.solver1D.FixedPointFinderBrent (0., auDeriv, true).findRoot
					(org.drip.quant.solver1D.InitializationHeuristics.FromHardSearchEdges (0., 1.));

			if (null == fpop || !fpop.containsRoot())
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MONOTONIC);

			double dblExtremum = fpop.getRoot();

			if (!org.drip.quant.common.NumberUtil.IsValid (dblExtremum) || dblExtremum <= 0. || dblExtremum
				>= 1.)
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MONOTONIC);

			double dblDeltaX = CalcMinDifference (_adblPredictorOrdinate) * DIFF_SCALE;

			double dbl2ndDeriv = responseValue (dblExtremum + dblDeltaX) + responseValue (dblExtremum -
				dblDeltaX) - 2. * responseValue (dblPredictorOrdinate);

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
			return new org.drip.spline.segment.Monotonocity
				(org.drip.spline.segment.Monotonocity.MONOTONIC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public boolean isLocallyMonotone()
		throws java.lang.Exception
	{
		org.drip.spline.segment.Monotonocity sm = monotoneType (0.5 * (_adblPredictorOrdinate[0] +
			_adblPredictorOrdinate[_adblPredictorOrdinate.length - 1]));

		return null != sm && org.drip.spline.segment.Monotonocity.MONOTONIC == sm.type();
	}

	@Override public boolean isCoMonotone (
		final double[] adblMeasuredResponse)
		throws java.lang.Exception
	{
		if (null == adblMeasuredResponse) return false;

		int iNumMeasuredResponse = adblMeasuredResponse.length;

		if (2 >= iNumMeasuredResponse) return false;

		int[] aiNodeMiniMax = new int[iNumMeasuredResponse];
		int[] aiMonotoneType = new int[iNumMeasuredResponse];

		for (int i = 0; i < iNumMeasuredResponse; ++i) {
			if (0 == i || iNumMeasuredResponse - 1 == i)
				aiNodeMiniMax[i] = 0;
			else {
				if (adblMeasuredResponse[i - 1] < adblMeasuredResponse[i] && adblMeasuredResponse[i + 1] <
					adblMeasuredResponse[i])
					aiNodeMiniMax[i] = MAXIMA_PREDICTOR_ORDINATE_NODE;
				else if (adblMeasuredResponse[i - 1] > adblMeasuredResponse[i] && adblMeasuredResponse[i + 1]
					> adblMeasuredResponse[i])
					aiNodeMiniMax[i] = MINIMA_PREDICTOR_ORDINATE_NODE;
				else
					aiNodeMiniMax[i] = MONOTONE_PREDICTOR_ORDINATE_NODE;
			}

			org.drip.spline.segment.Monotonocity sm = monotoneType (adblMeasuredResponse[i]);

			aiMonotoneType[i] = null != sm ? sm.type() :
				org.drip.spline.segment.Monotonocity.NON_MONOTONIC;
		}

		for (int i = 1; i < iNumMeasuredResponse - 1; ++i) {
			if (MAXIMA_PREDICTOR_ORDINATE_NODE == aiNodeMiniMax[i]) {
				if (org.drip.spline.segment.Monotonocity.MAXIMA != aiMonotoneType[i] &&
					org.drip.spline.segment.Monotonocity.MAXIMA != aiMonotoneType[i - 1])
					return false;
			} else if (MINIMA_PREDICTOR_ORDINATE_NODE == aiNodeMiniMax[i]) {
				if (org.drip.spline.segment.Monotonocity.MINIMA != aiMonotoneType[i] &&
					org.drip.spline.segment.Monotonocity.MINIMA != aiMonotoneType[i - 1])
					return false;
			}
		}

		return true;
	}

	@Override public boolean isKnot (
		final double dblPredictorOrdinate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate)) return false;

		int iNumPredictorOrdinate = _adblPredictorOrdinate.length;

		if (_adblPredictorOrdinate[0] > dblPredictorOrdinate ||
			_adblPredictorOrdinate[iNumPredictorOrdinate - 1] < dblPredictorOrdinate)
			return false;

		for (int i = 0; i < iNumPredictorOrdinate; ++i) {
			if (dblPredictorOrdinate == _adblPredictorOrdinate[i]) return true;
		}

		return false;
	}

	@Override public boolean resetNode (
		final int iPredictorOrdinateNodeIndex,
		final double dblResetResponse)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblResetResponse)) return false;

		if (iPredictorOrdinateNodeIndex > _adblPredictorOrdinate.length) return false;

		_adblResponseValue[iPredictorOrdinateNodeIndex] = dblResetResponse;
		return true;
	}

	@Override public boolean resetNode (
		final int iPredictorOrdinateNodeIndex,
		final org.drip.spline.params.SegmentResponseValueConstraint sprcReset)
	{
		return false;
	}

	@Override public double getLeftPredictorOrdinateEdge()
	{
		return _adblPredictorOrdinate[0];
	}

	@Override public double getRightPredictorOrdinateEdge()
	{
		return _adblPredictorOrdinate[_adblPredictorOrdinate.length - 1];
	}
}

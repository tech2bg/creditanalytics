	
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
 * This class implements the span using the Lagrange Polynomial Interpolator. It provides the following
 * 	functionality:
 * 	- At any location inside the span calculate the interpolated value and the Jacobian to the input.
 * 	- Estimate convexity, and if it is co-convex.
 * 	- Estimate monotonicity, if it is co-monotone, and it if is locally monotone.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LagrangePolynomialSpan implements org.drip.math.grid.SingleSegmentSpan {
	private static final double DIFF_SCALE = 1.0e-06;

	private double[] _adblX = null;
	private double[] _adblY = null;

	private static final double CalcAbsoluteMin (
		final double[] adblY)
		throws java.lang.Exception
	{
		if (null == adblY)
			throw new java.lang.Exception ("LagrangePolynomialSpan::CalcAbsoluteMin => Invalid Inputs");

		int iNumPoints = adblY.length;

		if (1 >= iNumPoints)
			throw new java.lang.Exception ("LagrangePolynomialSpan::CalcAbsoluteMin => Invalid Inputs");

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
			throw new java.lang.Exception ("LagrangePolynomialSpan::CalcMinDifference => Invalid Inputs");

		int iNumPoints = adblY.length;

		if (1 >= iNumPoints)
			throw new java.lang.Exception ("LagrangePolynomialSpan::CalcMinDifference => Invalid Inputs");

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

		if (!org.drip.math.common.NumberUtil.IsValid (dblBumpDelta) || 0. == dblBumpDelta)
			dblBumpDelta = CalcAbsoluteMin (adblY);

		return 0. == dblBumpDelta ? DIFF_SCALE : dblBumpDelta * DIFF_SCALE;
	}

	/**
	 * LagrangePolynomialSpan constructor
	 * 
	 * @param adblX Array of Ordinates
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public LagrangePolynomialSpan (
		final double[] adblX)
		throws java.lang.Exception
	{
		if (null == (_adblX = adblX))
			throw new java.lang.Exception ("LagrangePolynomialSpan ctr: Invalid Inputs");

		int iNumOrdinates = _adblX.length;

		if (1 >= iNumOrdinates) throw new java.lang.Exception ("LagrangePolynomialSpan ctr: Invalid Inputs");

		for (int i = 0; i < iNumOrdinates; ++i) {
			for (int j = i + 1; j < iNumOrdinates; ++j) {
				if (_adblX[i] == _adblX[j])
					throw new java.lang.Exception ("LagrangePolynomialSpan ctr: Invalid Inputs");
			}
		}
	}

	/**
	 * Retrieve the Span Builder Parameters
	 * 
	 * @return The Span Builder Parameters
	 */

	@Override public org.drip.math.grid.SpanBuilderParams getSpanBuilderParams()
	{
		return null;
	}

	/**
	 * Sets up (i.e., calibrates) the individual segment in the span to the target node values.
	 * 
	 * @param adblY Target Node values
	 * @param iSetupMode Set up Mode (Fully calibrate the Span, or calibrate Span plus compute Jacobian)
	 * 
	 * @return TRUE => Set up was successful
	 */

	@Override public boolean setup (
		final double[] adblY,
		final java.lang.String strCalibrationMode,
		final int iSetupMode)
	{
		return null != (_adblY = adblY) && _adblY.length == _adblX.length;
	}

	/**
	 * Calculates the interpolated value at the given input point
	 * 
	 * @param dblX Input point
	 * 
	 * @return Interpolated output
	 * 
	 * @throws java.lang.Exception Thrown if the interpolation did not succeed
	 */

	@Override public double calcValue (
		final double dblX)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX))
			throw new java.lang.Exception ("LagrangePolynomialSpan::calcValue => Invalid inputs!");

		int iNumPoints = _adblX.length;

		if (_adblX[0] > dblX || _adblX[iNumPoints - 1] < dblX)
			throw new java.lang.Exception ("LagrangePolynomialSpan::calcValue => Input out of range!");

		double dblValue = 0;

		for (int i = 0; i < iNumPoints; ++i) {
			double dblNodeValue = _adblY[i];

			for (int j = 0; j < iNumPoints; ++j) {
				if (i != j) dblNodeValue = dblNodeValue * (dblX - _adblX[j]) / (_adblX[i] - _adblX[j]);
			}

			dblValue += dblNodeValue;
		}

		return dblValue;
	}

	/**
	 * Calculates the Jacobian to the inputs at the given input point
	 * 
	 * @param dblX Input point
	 * 
	 * @return Jacobian to the inputs
	 */

	@Override public org.drip.math.calculus.WengertJacobian calcValueJacobian (
		final double dblX)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX)) return null;

		int iNumPoints = _adblX.length;
		double dblBase = java.lang.Double.NaN;
		double dblDeltaY = java.lang.Double.NaN;
		org.drip.math.calculus.WengertJacobian wj = null;

		if (_adblX[0] > dblX || _adblX[iNumPoints - 1] < dblX) return null;

		try {
			wj = new org.drip.math.calculus.WengertJacobian (1, iNumPoints);

			dblBase = calcValue (dblX);

			dblDeltaY = EstimateBumpDelta (_adblY);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumPoints; ++i) {
			double[] adblYBumped = new double[iNumPoints];

			for (int j = 0; j < iNumPoints; ++j)
				adblYBumped[j] = i == j ? _adblY[j] + dblDeltaY : _adblY[j];

			try {
				LagrangePolynomialSpan lps = new LagrangePolynomialSpan (_adblX);

				if (!lps.setup (adblYBumped, "", org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN) ||
					!wj.accumulatePartialFirstDerivative (0, i, (lps.calcValue (dblX) - dblBase) /
						dblDeltaY))
					return null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return wj;
	}

	/**
	 * Identifies the monotone type for the segment underlying the given input point
	 * 
	 * @param dblX Input point
	 * 
	 * @return Segment monotone Type
	 */

	@Override public org.drip.math.grid.SegmentMonotonocity monotoneType (
		final double dblX)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX)) return null;

		int iNumPoints = _adblX.length;

		if (_adblX[0] > dblX || _adblX[iNumPoints - 1] < dblX) return null;

		if (2 == iNumPoints) {
			try {
				return new org.drip.math.grid.SegmentMonotonocity
					(org.drip.math.grid.SegmentMonotonocity.MONOTONIC);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		org.drip.math.function.AbstractUnivariate ofDeriv = new org.drip.math.function.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				double dblDeltaX = CalcMinDifference (_adblX) * DIFF_SCALE;

				return (calcValue (dblX + dblDeltaX) - calcValue (dblX)) / dblDeltaX;
			}
		};

		try {
			org.drip.math.solver1D.FixedPointFinderOutput fpop = new
				org.drip.math.solver1D.FixedPointFinderBrent (0., ofDeriv).findRoot
					(org.drip.math.solver1D.InitializationHeuristics.FromHardSearchEdges (0., 1.));

			if (null == fpop || !fpop.containsRoot())
				return new org.drip.math.grid.SegmentMonotonocity
					(org.drip.math.grid.SegmentMonotonocity.MONOTONIC);

			double dblExtremum = fpop.getRoot();

			if (!org.drip.math.common.NumberUtil.IsValid (dblExtremum) || dblExtremum <= 0. || dblExtremum >=
				1.)
				return new org.drip.math.grid.SegmentMonotonocity
					(org.drip.math.grid.SegmentMonotonocity.MONOTONIC);

			double dblDeltaX = CalcMinDifference (_adblX) * DIFF_SCALE;

			double dbl2ndDeriv = calcValue (dblExtremum + dblDeltaX) + calcValue (dblExtremum - dblDeltaX) -
				2. * calcValue (dblX);

			if (0. > dbl2ndDeriv)
				return new org.drip.math.grid.SegmentMonotonocity
					(org.drip.math.grid.SegmentMonotonocity.MAXIMA);

			if (0. < dbl2ndDeriv)
				return new org.drip.math.grid.SegmentMonotonocity
					(org.drip.math.grid.SegmentMonotonocity.MINIMA);

			if (0. == dbl2ndDeriv)
				return new org.drip.math.grid.SegmentMonotonocity
					(org.drip.math.grid.SegmentMonotonocity.INFLECTION);

			return new org.drip.math.grid.SegmentMonotonocity
				(org.drip.math.grid.SegmentMonotonocity.NON_MONOTONIC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			return new org.drip.math.grid.SegmentMonotonocity
				(org.drip.math.grid.SegmentMonotonocity.MONOTONIC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Indicates if all the comprising segments are monotone
	 * 
	 * @return TRUE => Fully locally monotonic
	 * 
	 * @throws java.lang.Exception Thrown if the Segment monotone Type could not be estimated
	 */

	@Override public boolean isLocallyMonotone()
		throws java.lang.Exception
	{
		org.drip.math.grid.SegmentMonotonocity sm = monotoneType (0.5 * (_adblX[0] +
			_adblX[_adblX.length - 1]));

		return null != sm && org.drip.math.grid.SegmentMonotonocity.MONOTONIC == sm.type();
	}

	/**
	 * Verify whether the segment and spline mini-max behavior matches
	 * 
	 * @param adblYIn Input Y array points
	 * 
	 * @return TRUE => Span is co-monotonic with the input points
	 * 
	 * @throws java.lang.Exception Thrown if the Segment monotone Type could not be estimated
	 */

	@Override public boolean isCoMonotone (
		final double[] adblYIn)
		throws java.lang.Exception
	{
		if (null == adblYIn) return false;

		int iNumInputNodes = adblYIn.length;

		if (2 >= iNumInputNodes) return false;

		int iMaximaNode = 1;
		int iMinimaNode = 2;
		int[] aiNodeMiniMax = new int[iNumInputNodes];
		int[] aiMonotoneType = new int[iNumInputNodes];

		for (int i = 0; i < iNumInputNodes; ++i) {
			if (0 == i || iNumInputNodes - 1 == i)
				aiNodeMiniMax[i] = 0;
			else {
				if (adblYIn[i - 1] < adblYIn[i] && adblYIn[i + 1] < adblYIn[i])
					aiNodeMiniMax[i] = iMaximaNode;
				else if (adblYIn[i - 1] > adblYIn[i] && adblYIn[i + 1] > adblYIn[i])
					aiNodeMiniMax[i] = iMinimaNode;
				else
					aiNodeMiniMax[i] = 0;
			}

			org.drip.math.grid.SegmentMonotonocity sm = monotoneType (adblYIn[i]);

			aiMonotoneType[i] = null != sm ? sm.type() :
				org.drip.math.grid.SegmentMonotonocity.NON_MONOTONIC;
		}

		for (int i = 1; i < iNumInputNodes - 1; ++i) {
			if (iMaximaNode == aiNodeMiniMax[i]) {
				if (org.drip.math.grid.SegmentMonotonocity.MAXIMA != aiMonotoneType[i] &&
					org.drip.math.grid.SegmentMonotonocity.MAXIMA != aiMonotoneType[i - 1])
					return false;
			} else if (iMinimaNode == aiNodeMiniMax[i]) {
				if (org.drip.math.grid.SegmentMonotonocity.MINIMA != aiMonotoneType[i] &&
					org.drip.math.grid.SegmentMonotonocity.MINIMA != aiMonotoneType[i - 1])
					return false;
			}
		}

		return true;
	}

	/**
	 * Is the given X a knot location
	 * 
	 * @param dblX Knot X
	 * 
	 * @return TRUE => Given Location corresponds to a Knot
	 */

	@Override public boolean isKnot (
		final double dblX)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX)) return false;

		int iNumPoints = _adblX.length;

		if (_adblX[0] > dblX || _adblX[iNumPoints - 1] < dblX) return false;

		for (int i = 0; i < iNumPoints; ++i) {
			if (dblX == _adblX[i]) return true;
		}

		return false;
	}

	/**
	 * Reset the given node with the given value
	 * 
	 * @param iNodeIndex Node whose value is set
	 * @param dblY New Y
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	@Override public boolean resetNode (
		final int iNodeIndex,
		final double dblY)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblY)) return false;

		if (iNodeIndex > _adblX.length) return false;

		_adblY[iNodeIndex] = dblY;
		return true;
	}
}

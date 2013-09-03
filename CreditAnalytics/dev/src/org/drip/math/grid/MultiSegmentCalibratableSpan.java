	
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
 * This class implements the span that spans multiple segments. It holds the ordered segment sequence, the
 * 	segment control parameters, and, if available, the spanning Jacobian. It exports the following group of
 * 	functionality:
 * 	- Construct adjoining segment sequences in accordance with the segment control parameters
 * 	- Calibrate according to a varied set of (i.e., NATURAL/FINANCIAL) boundary conditions
 * 	- Interpolate both the value, the ordered derivatives, and the Jacobian at the given ordinate
 * 	- Compute the monotonicity details - segment/span level monotonicity, co-monotonicity, local
 * 		monotonicity.
 * 	- Insert knots
 * 
 * It also exports several static Span creation/calibration methods to generate customized basis splines,
 * 	with customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultiSegmentCalibratableSpan extends org.drip.math.function.AbstractUnivariate implements
	org.drip.math.grid.MultiSegmentSpan {
	class TargetEvalParams {
		java.lang.String _strSolverMode = "";
		double _dblYLeading = java.lang.Double.NaN;
		org.drip.math.spline.SegmentNodeWeightConstraint[] _aSNWC = null;

		/**
		 * TargetEvalParams constructor
		 * 
		 * @param dblYLeading Left most Y
		 * @param aSNWC Array of Segment Node Weight Constraints
		 * @param strSolverMode Solver Mode - NATURAL | FINANCIAL
		 * 
		 * @throws java.lang.Exception Thrown if the inputs are invalid
		 */

		public TargetEvalParams (
			final double dblYLeading,
			final org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWC,
			final java.lang.String strSolverMode)
			throws java.lang.Exception
		{
			if (!org.drip.math.common.NumberUtil.IsValid (_dblYLeading = dblYLeading) || null ==
				(_strSolverMode = strSolverMode) ||
					(!org.drip.math.grid.MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL.equalsIgnoreCase
						(_strSolverMode) &&
							!org.drip.math.grid.MultiSegmentSpan.SPLINE_BOUNDARY_MODE_FINANCIAL.equalsIgnoreCase
								(_strSolverMode)) || null == (_aSNWC = aSNWC) || 0 == _aSNWC.length)
				throw new java.lang.Exception ("TargetEvalParams ctr: Invalid inputs!");
		}
	}

	private TargetEvalParams _tep = null;
	private org.drip.math.grid.Segment[] _aCSS = null;
	private org.drip.math.calculus.WengertJacobian _wjSpan = null;
	private org.drip.math.grid.SegmentBuilderParams[] _aSBP = null;

	private boolean initStartingSegment (
		final double dblLeftSlope)
	{
		return _aCSS[0].calibrate (_tep._dblYLeading, dblLeftSlope, _tep._aSNWC[0]);
	}

	private boolean calibSegmentFromSMWC (
		final int iSegment,
		final org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWC)
	{
		return _aCSS[iSegment].calibrate (0 == iSegment ? null : _aCSS[iSegment - 1], aSNWC[iSegment]);
	}

	private boolean calibSegmentElastics (
		final int iStartingSegment,
		final org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWC)
	{
		for (int iSegment = iStartingSegment; iSegment < _aCSS.length; ++iSegment) {
			if (!calibSegmentFromSMWC (iSegment, aSNWC)) return false;
		}

		return true;
	}

	private boolean setSpanJacobian (
		final int iNodeIndex,
		final org.drip.math.calculus.WengertJacobian wjSegment)
	{
		if (null == wjSegment) return false;

		int iParameterIndex = 0 == iNodeIndex ? 0 : 2;

		if (!_wjSpan.accumulatePartialFirstDerivative (0, iNodeIndex, wjSegment.getFirstDerivative (0,
			iParameterIndex)))
			return false;

		if (!_wjSpan.accumulatePartialFirstDerivative (1, iNodeIndex, wjSegment.getFirstDerivative (1,
			iParameterIndex)))
			return false;

		if (!_wjSpan.accumulatePartialFirstDerivative (2, iNodeIndex, wjSegment.getFirstDerivative (2,
			iParameterIndex)))
			return false;

		return _wjSpan.accumulatePartialFirstDerivative (3, iNodeIndex, wjSegment.getFirstDerivative (3,
			iParameterIndex));
	}

	private final org.drip.math.calculus.WengertJacobian setJacobian (
		final int iNodeIndex,
		final org.drip.math.calculus.WengertJacobian wjSegment)
	{
		if (null == wjSegment) return null;

		int iNumSegment = _aCSS.length;
		org.drip.math.calculus.WengertJacobian wjSpan = null;

		try {
			wjSpan = new org.drip.math.calculus.WengertJacobian (1, iNumSegment + 1);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i <= iNumSegment; ++i) {
			if (i == iNodeIndex) {
				if (!wjSpan.accumulatePartialFirstDerivative (0, i, wjSegment.getFirstDerivative (0,
					org.drip.math.grid.Segment.LEFT_NODE_VALUE_PARAMETER_INDEX)) ||
						!wjSpan.accumulatePartialFirstDerivative (0, i + 1, wjSegment.getFirstDerivative (0,
							org.drip.math.grid.Segment.RIGHT_NODE_VALUE_PARAMETER_INDEX)))
					return null;
			}
		}

		return wjSpan;
	}

	/**
	 * Span constructor - Constructs a sequence of basis spline segments
	 * 
	 * @param aCSS Array of segments
	 * @param aSBP Array of Segment Builder Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public MultiSegmentCalibratableSpan (
		final org.drip.math.grid.Segment[] aCSS,
		final org.drip.math.grid.SegmentBuilderParams[] aSBP)
		throws java.lang.Exception
	{
		super (null);

		if (null == aCSS || null == aSBP)
			throw new java.lang.Exception ("MultiSegmentCalibratableSpan ctr => Invalid inputs!");

		int iNumSegment = aCSS.length;
		_aCSS = new org.drip.math.grid.Segment[iNumSegment];
		_aSBP = new org.drip.math.grid.SegmentBuilderParams[iNumSegment];

		if (0 == iNumSegment || iNumSegment != aSBP.length)
			throw new java.lang.Exception ("MultiSegmentCalibratableSpan ctr => Invalid inputs!");

		for (int i = 0; i < iNumSegment; ++i) {
			if (null == (_aCSS[i] = aCSS[i]) || null == (_aSBP[i] = aSBP[i]))
				throw new java.lang.Exception ("MultiSegmentCalibratableSpan ctr => Invalid inputs!");
		}
	}

	@Override public org.drip.math.grid.Segment[] getSegments()
	{
		return _aCSS;
	}

	@Override public org.drip.math.grid.SegmentBuilderParams[] getSegmentBuilderParams()
	{
		return _aSBP;
	}

	@Override public boolean setup (
		final double dblYLeading,
		final org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWC,
		final java.lang.String strCalibrationMode,
		final int iSetupMode)
	{
		try {
			_tep = new TargetEvalParams (dblYLeading, aSNWC, strCalibrationMode);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		if (0 != (org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN & iSetupMode)) {
			org.drip.math.solver1D.FixedPointFinderOutput rfopCalib = null;

			if (null == rfopCalib || !rfopCalib.containsRoot()) {
				try {
					rfopCalib = new org.drip.math.solver1D.FixedPointFinderBrent (0., this).findRoot();
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return false;
				}
			}

			if (null == rfopCalib || !org.drip.math.common.NumberUtil.IsValid (rfopCalib.getRoot()))
				return false;
		}

		if (0 != (org.drip.math.grid.SingleSegmentSpan.CALIBRATE_JACOBIAN & iSetupMode)) {
			int iNumSegment = _aCSS.length;

			try {
				if (null == (_wjSpan = new org.drip.math.calculus.WengertJacobian (_aCSS[0].numBasis(),
					iNumSegment + 1)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			org.drip.math.calculus.WengertJacobian wjHead = _aCSS[0].calcJacobian();

			if (!setSpanJacobian (0, wjHead) || !setSpanJacobian (1, wjHead)) return false;

			for (int i = 1; i < iNumSegment; ++i) {
				if (!setSpanJacobian (i + 1, _aCSS[i].calcJacobian())) return false;
			}
		}

		return true;
	}

	@Override public boolean setup (
		final double dblYLeading,
		final double[] adblYRight,
		final java.lang.String strCalibrationMode,
		final int iSetupMode)
	{
		int iNumSegment = _aCSS.length;
		org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWCRight = new
			org.drip.math.spline.SegmentNodeWeightConstraint[iNumSegment];

		if (0 == iNumSegment || iNumSegment != adblYRight.length) return false;

		try {
			for (int i = 0; i < iNumSegment; ++i)
				aSNWCRight[i] = new org.drip.math.spline.SegmentNodeWeightConstraint (new double[]
					{_aCSS[i].getRight()}, new double[] {1.}, adblYRight[i]);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return setup (dblYLeading, aSNWCRight, strCalibrationMode, iSetupMode);
	}

	@Override public boolean setup (
		final org.drip.math.grid.SegmentEdgeParams[] aSEPLeft,
		final org.drip.math.grid.SegmentEdgeParams[] aSEPRight,
		final org.drip.math.spline.SegmentNodeWeightConstraint[][] aaSNWC,
		final int iSetupMode)
	{
		if (null == aSEPLeft || null == aSEPRight) return false;

		int iNumSegment = _aCSS.length;

		if (iNumSegment != aSEPLeft.length || iNumSegment != aSEPRight.length || (null != aaSNWC &&
			iNumSegment != aaSNWC.length))
			return false;

		for (int i = 0; i < iNumSegment; ++i) {
			try {
				if (0 != (org.drip.math.grid.SingleSegmentSpan.CALIBRATE_SPAN & iSetupMode) &&
					!_aCSS[i].calibrate (new org.drip.math.spline.SegmentCalibrationParams (new double[] {0.,
						1.}, new double[] {aSEPLeft[i].getY(), aSEPRight[i].getY()}, aSEPLeft[i].getDeriv(),
							aSEPLeft[i].getDeriv(), null == aaSNWC ? null : aaSNWC[i])))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		if (0 != (org.drip.math.grid.SingleSegmentSpan.CALIBRATE_JACOBIAN & iSetupMode)) {
			try {
				if (null == (_wjSpan = new org.drip.math.calculus.WengertJacobian (_aCSS[0].numBasis(),
					iNumSegment + 1)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			org.drip.math.calculus.WengertJacobian wjHead = _aCSS[0].calcJacobian();

			if (!setSpanJacobian (0, wjHead) || !setSpanJacobian (1, wjHead)) return false;

			for (int i = 1; i < iNumSegment; ++i) {
				if (!setSpanJacobian (i + 1, _aCSS[i].calcJacobian())) return false;
			}
		}

		return true;
	}

	@Override public double evaluate (
		final double dblLeftSlope)
		throws java.lang.Exception
	{
		if (org.drip.math.grid.MultiSegmentSpan.SPLINE_BOUNDARY_MODE_NATURAL.equalsIgnoreCase
			(_tep._strSolverMode)) {
			if (!initStartingSegment (dblLeftSlope) || !calibSegmentElastics (1, _tep._aSNWC))
				throw new java.lang.Exception
					("MultiSegmentCalibratableSpan::evalTarget => cannot set segment elastics!");

			return calcTailDerivative (2);
		}

		throw new java.lang.Exception ("MultiSegmentCalibratableSpan::evalTarget => Unknown Solver Mode " +
			_tep._strSolverMode);
	}

	@Override public boolean setLeftNode (
		final double dblLeftValue,
		final double dblLeftSlope,
		final double dblRightValue)
	{
		return _aCSS[0].calibrate (dblLeftValue, dblLeftSlope, dblRightValue);
	}

	@Override public double calcValue (
		final double dblX)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX))
			throw new java.lang.Exception ("MultiSegmentCalibratableSpan::calcValue => Invalid inputs!");

		int iNumSegment = _aCSS.length;

		if (_aCSS[0].getLeft() > dblX || _aCSS[iNumSegment - 1].getRight() < dblX)
			throw new java.lang.Exception ("MultiSegmentCalibratableSpan::calcValue => Input out of range!");

		int iIndex = 0;

		for (int i = 0; i < iNumSegment; ++i) {
			if (_aCSS[i].getLeft() <= dblX && _aCSS[i].getRight() >= dblX) {
				iIndex = i;
				break;
			}
		}

		return _aCSS[iIndex].calcValue (dblX);
	}

	@Override public org.drip.math.grid.SegmentEdgeParams calcSEP (
		final double dblX)
	{
		int iIndex = 0;
		int iNumSegment = _aCSS.length;

		if (!org.drip.math.common.NumberUtil.IsValid (dblX) || (_aCSS[0].getLeft() > dblX ||
			_aCSS[iNumSegment - 1].getRight() < dblX))
			return null;

		for (int i = 0; i < iNumSegment; ++i) {
			if (_aCSS[i].getLeft() <= dblX && _aCSS[i].getRight() >= dblX) {
				iIndex = i;
				break;
			}
		}

		int iCk = _aSBP[iIndex].getSegmentInelasticParams().getCk();

		double adblDeriv[] = new double[iCk];

		try {
			for (int i = 0; i < iCk; ++i)
				adblDeriv[i] = _aCSS[iIndex].calcOrderedDerivative (dblX, i, false);

			return new org.drip.math.grid.SegmentEdgeParams (_aCSS[iIndex].calcValue (dblX), adblDeriv);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.math.calculus.WengertJacobian calcValueJacobian (
		final double dblX)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX)) return null;

		int iIndex = 0;
		int iNumSegment = _aCSS.length;

		if (_aCSS[0].getLeft() > dblX || _aCSS[iNumSegment - 1].getRight() < dblX) return null;

		for (int i = 0 ; i < iNumSegment; ++i) {
			if (_aCSS[i].getLeft() <= dblX && _aCSS[i].getRight() >= dblX) {
				iIndex = i;
				break;
			}
		}

		return setJacobian (iIndex, _aCSS[iIndex].calcValueJacobian (dblX));
	}

	@Override public org.drip.math.grid.SegmentMonotonocity monotoneType (
		final double dblX)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX)) return null;

		int iNumSegment = _aCSS.length;

		if (_aCSS[0].getLeft() > dblX || _aCSS[iNumSegment - 1].getRight() < dblX) return null;

		for (int i = 0; i < iNumSegment; ++i) {
			if (_aCSS[i].getLeft() <= dblX && _aCSS[i].getRight() >= dblX)
				return _aCSS[i].monotoneType();
		}

		return null;
	}

	@Override public boolean isLocallyMonotone()
		throws java.lang.Exception
	{
		for (int i = 0; i < _aCSS.length; ++i) {
			org.drip.math.grid.SegmentMonotonocity sm = null;

			try {
				sm = _aCSS[i].monotoneType();
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			if (null == sm || org.drip.math.grid.SegmentMonotonocity.MONOTONIC != sm.type()) return false;
		}

		return true;
	}

	@Override public boolean isCoMonotone (
		final double[] adblY)
		throws java.lang.Exception
	{
		int iNumSegment = _aCSS.length;
		int iMaximaNode = 1;
		int iMinimaNode = 2;
		int[] aiMonotoneType = new int[iNumSegment];
		int[] aiNodeMiniMax = new int[iNumSegment + 1];

		if (null == adblY || adblY.length != iNumSegment + 1)
			throw new java.lang.Exception
				("MultiSegmentCalibratableSpan::isCoMonotone => Data input inconsistent with the segment");

		for (int i = 0; i < iNumSegment + 1; ++i) {
			if (0 == i || iNumSegment == i)
				aiNodeMiniMax[i] = 0;
			else {
				if (adblY[i - 1] < adblY[i] && adblY[i + 1] < adblY[i])
					aiNodeMiniMax[i] = iMaximaNode;
				else if (adblY[i - 1] > adblY[i] && adblY[i + 1] > adblY[i])
					aiNodeMiniMax[i] = iMinimaNode;
				else
					aiNodeMiniMax[i] = 0;
			}

			if (i < iNumSegment) {
				org.drip.math.grid.SegmentMonotonocity sm = _aCSS[i].monotoneType();

				if (null != sm) aiMonotoneType[i] = sm.type();
			}
		}

		for (int i = 1; i < iNumSegment; ++i) {
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

	@Override public boolean isKnot (
		final double dblX)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX)) return false;

		int iNumSegment = _aCSS.length;

		for (int i = 0; i < iNumSegment; ++i) {
			if (dblX == _aCSS[i].getLeft()) return false;
		}

		return dblX == _aCSS[iNumSegment - 1].getLeft();
	}

	@Override public double calcTailDerivative (
		final int iOrder)
		throws java.lang.Exception
	{
		org.drip.math.grid.Segment css = _aCSS[_aCSS.length - 1];

		return css.calcOrderedDerivative (css.getRight(), iOrder, false);
	}

	@Override public boolean resetNode (
		final int iNodeIndex,
		final double dblNodeValue)
	{
		if (0 == iNodeIndex || 1 == iNodeIndex || _aCSS.length < iNodeIndex ||
			!org.drip.math.common.NumberUtil.IsValid (dblNodeValue))
			return false;

		return _aCSS[iNodeIndex - 1].calibrate (_aCSS[iNodeIndex - 2], dblNodeValue);
	}

	@Override public boolean resetNode (
		final int iNodeIndex,
		final org.drip.math.spline.SegmentNodeWeightConstraint snwc)
	{
		if (0 == iNodeIndex || 1 == iNodeIndex || _aCSS.length < iNodeIndex || null == snwc)
			return false;

		return _aCSS[iNodeIndex - 1].calibrate (_aCSS[iNodeIndex - 2], snwc);
	}

	@Override public boolean isInRange (
		final double dblX)
	{
		return org.drip.math.common.NumberUtil.IsValid (dblX) && dblX >= _aCSS[0].getLeft() && dblX <=
			_aCSS[_aCSS.length - 1].getRight();
	}

	@Override public double getLeftEdge()
	{
		return _aCSS[0].getLeft();
	}

	@Override public double getRightEdge()
	{
		return _aCSS[_aCSS.length - 1].getRight();
	}
}

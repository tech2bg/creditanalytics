	
package org.drip.math.regime;

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
 * 	- Calibrate according to a varied set of (i.e., FLOATING/NATURAL/FINANCIAL) boundary conditions
 * 	- Estimate both the value, the ordered derivatives, and the Jacobian at the given ordinate
 * 	- Compute the monotonicity details - segment/span level monotonicity, co-monotonicity, local
 * 		monotonicity.
 * 	- Insert knots
 * 
 * It also exports several static Regime creation/calibration methods to generate customized basis splines,
 * 	with customized segment behavior using the segment control.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MultiSegmentCalibratableRegime extends org.drip.math.function.AbstractUnivariate implements
	org.drip.math.regime.MultiSegmentRegime {
	private static final int MAXIMA_PREDICTOR_ORDINATE_NODE = 1;
	private static final int MINIMA_PREDICTOR_ORDINATE_NODE = 2;
	private static final int MONOTONE_PREDICTOR_ORDINATE_NODE = 4;

	private java.lang.String _strName = "";
	private org.drip.math.regime.SegmentSequenceBuilder _ssb = null;
	private org.drip.math.segment.PredictorResponse[] _aSegment = null;
	private org.drip.math.calculus.WengertJacobian _wjDCoeffDEdgeParams = null;
	private org.drip.math.segment.PredictorResponseBuilderParams[] _aSBP = null;

	private boolean setDCoeffDEdgeParams (
		final int iNodeIndex,
		final org.drip.math.calculus.WengertJacobian wjDCoeffDEdgeParams)
	{
		if (null == wjDCoeffDEdgeParams) return false;

		int iParameterIndex = 0 == iNodeIndex ? 0 : 2;

		if (!_wjDCoeffDEdgeParams.accumulatePartialFirstDerivative (0, iNodeIndex,
			wjDCoeffDEdgeParams.getFirstDerivative (0, iParameterIndex)))
			return false;

		if (!_wjDCoeffDEdgeParams.accumulatePartialFirstDerivative (1, iNodeIndex,
			wjDCoeffDEdgeParams.getFirstDerivative (1, iParameterIndex)))
			return false;

		if (!_wjDCoeffDEdgeParams.accumulatePartialFirstDerivative (2, iNodeIndex,
			wjDCoeffDEdgeParams.getFirstDerivative (2, iParameterIndex)))
			return false;

		return _wjDCoeffDEdgeParams.accumulatePartialFirstDerivative (3, iNodeIndex,
			wjDCoeffDEdgeParams.getFirstDerivative (3, iParameterIndex));
	}

	private final org.drip.math.calculus.WengertJacobian setDResponseDEdgeResponse (
		final int iNodeIndex,
		final org.drip.math.calculus.WengertJacobian wjDResponseDEdgeParams)
	{
		if (null == wjDResponseDEdgeParams) return null;

		int iNumSegment = _aSegment.length;
		org.drip.math.calculus.WengertJacobian wjDResponseDEdgeResponse = null;

		try {
			wjDResponseDEdgeResponse = new org.drip.math.calculus.WengertJacobian (1, iNumSegment + 1);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i <= iNumSegment; ++i) {
			if (i == iNodeIndex) {
				if (!wjDResponseDEdgeResponse.accumulatePartialFirstDerivative (0, i,
					wjDResponseDEdgeParams.getFirstDerivative (0,
						org.drip.math.segment.PredictorResponse.LEFT_NODE_VALUE_PARAMETER_INDEX)) ||
							!wjDResponseDEdgeResponse.accumulatePartialFirstDerivative (0, i + 1,
								wjDResponseDEdgeParams.getFirstDerivative (0,
									org.drip.math.segment.PredictorResponse.RIGHT_NODE_VALUE_PARAMETER_INDEX)))
					return null;
			}
		}

		return wjDResponseDEdgeResponse;
	}

	/**
	 * MultiSegmentCalibratableRegime constructor - Construct a sequence of Basis Spline Segments
	 * 
	 * @param strName Name of the Regime
	 * @param aSegment Array of Segments
	 * @param aSBP Array of Segment Builder Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public MultiSegmentCalibratableRegime (
		final java.lang.String strName,
		final org.drip.math.segment.PredictorResponse[] aSegment,
		final org.drip.math.segment.PredictorResponseBuilderParams[] aSBP)
		throws java.lang.Exception
	{
		super (null);

		if (null == aSegment || null == aSBP || null == (_strName = strName) || _strName.isEmpty())
			throw new java.lang.Exception ("MultiSegmentCalibratableRegime ctr => Invalid inputs!");

		int iNumSegment = aSegment.length;
		_aSegment = new org.drip.math.segment.PredictorResponse[iNumSegment];
		_aSBP = new org.drip.math.segment.PredictorResponseBuilderParams[iNumSegment];

		if (0 == iNumSegment || iNumSegment != aSBP.length)
			throw new java.lang.Exception ("MultiSegmentCalibratableRegime ctr => Invalid inputs!");

		for (int i = 0; i < iNumSegment; ++i) {
			if (null == (_aSegment[i] = aSegment[i]) || null == (_aSBP[i] = aSBP[i]))
				throw new java.lang.Exception ("MultiSegmentCalibratableRegime ctr => Invalid inputs!");
		}
	}

	@Override public java.lang.String name()
	{
		return _strName;
	}

	@Override public org.drip.math.segment.PredictorResponse[] getSegments()
	{
		return _aSegment;
	}

	@Override public org.drip.math.segment.PredictorResponseBuilderParams[] getSegmentBuilderParams()
	{
		return _aSBP;
	}

	@Override public boolean setup (
		final org.drip.math.regime.SegmentSequenceBuilder ssb,
		final int iCalibrationDetail)
	{
		if (null == (_ssb = ssb)) return false;

		if (!_ssb.setRegime (this)) return false;

		if (org.drip.math.regime.MultiSegmentRegime.BOUNDARY_CONDITION_FLOATING ==
			_ssb.getCalibrationBoundaryCondition()) {
			if (!_ssb.calibStartingSegment (0.) || !_ssb.calibSegmentSequence (1)) return false;
		} else if (0 != (org.drip.math.regime.MultiSegmentRegime.CALIBRATE & iCalibrationDetail)) {
			org.drip.math.solver1D.FixedPointFinderOutput fpop = null;

			if (null == fpop || !fpop.containsRoot()) {
				try {
					fpop = new org.drip.math.solver1D.FixedPointFinderNewton (0., this, true).findRoot();
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return false;
				}
			}

			if (null == fpop || !org.drip.math.common.NumberUtil.IsValid (fpop.getRoot())) return false;
		}

		if (0 != (org.drip.math.regime.MultiSegmentRegime.CALIBRATE_JACOBIAN &iCalibrationDetail)) {
			int iNumSegment = _aSegment.length;

			try {
				if (null == (_wjDCoeffDEdgeParams = new org.drip.math.calculus.WengertJacobian
					(_aSegment[0].numBasis(), iNumSegment + 1)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			org.drip.math.calculus.WengertJacobian wjHead = _aSegment[0].jackDCoeffDEdgeParams();

			if (!setDCoeffDEdgeParams (0, wjHead) || !setDCoeffDEdgeParams (1, wjHead)) return false;

			for (int i = 1; i < iNumSegment; ++i) {
				if (!setDCoeffDEdgeParams (i + 1, _aSegment[i].jackDCoeffDEdgeParams())) return false;
			}
		}

		return true;
	}

	@Override public boolean setup (
		final org.drip.math.segment.ResponseValueConstraint rvcLeading,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC,
		final org.drip.math.segment.BestFitWeightedResponse bfwr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		try {
			return setup (new org.drip.math.regime.CkSegmentSequenceBuilder (rvcLeading, aRVC, bfwr,
				iCalibrationBoundaryCondition), iCalibrationDetail);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public boolean setup (
		final double dblLeftRegimeResponseValue,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC,
		final org.drip.math.segment.BestFitWeightedResponse bfwr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		return setup (org.drip.math.segment.ResponseValueConstraint.FromPredictorResponse
			(getLeftPredictorOrdinateEdge(), dblLeftRegimeResponseValue), aRVC, bfwr,
				iCalibrationBoundaryCondition, iCalibrationDetail);
	}

	@Override public boolean setup (
		final double dblLeftRegimeResponseValue,
		final double[] adblSegmentRightResponseValue,
		final org.drip.math.segment.BestFitWeightedResponse bfwr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		int iNumSegment = _aSegment.length;
		org.drip.math.segment.ResponseValueConstraint[] aSNWCRight = new
			org.drip.math.segment.ResponseValueConstraint[iNumSegment];

		if (0 == iNumSegment || iNumSegment != adblSegmentRightResponseValue.length) return false;

		try {
			for (int i = 0; i < iNumSegment; ++i)
				aSNWCRight[i] = new org.drip.math.segment.ResponseValueConstraint (new double[]
					{_aSegment[i].right()}, new double[] {1.}, adblSegmentRightResponseValue[i]);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return setup (dblLeftRegimeResponseValue, aSNWCRight, bfwr, iCalibrationBoundaryCondition,
			iCalibrationDetail);
	}

	@Override public boolean setupHermite (
		final org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDLeft,
		final org.drip.math.segment.PredictorOrdinateResponseDerivative[] aPORDRight,
		final org.drip.math.segment.ResponseValueConstraint[][] aaRVC,
		final org.drip.math.segment.BestFitWeightedResponse bfwr,
		final int iSetupMode)
	{
		if (null == aPORDLeft || null == aPORDRight) return false;

		int iNumSegment = _aSegment.length;

		if (iNumSegment != aPORDLeft.length || iNumSegment != aPORDRight.length || (null != aaRVC &&
			iNumSegment != aaRVC.length))
			return false;

		for (int i = 0; i < iNumSegment; ++i) {
			try {
				if (0 != (org.drip.math.regime.MultiSegmentRegime.CALIBRATE & iSetupMode) &&
					!_aSegment[i].calibrate (new org.drip.math.segment.CalibrationParams (new double[] {0.,
						1.}, new double[] {aPORDLeft[i].responseValue(), aPORDRight[i].responseValue()},
							aPORDLeft[i].getDResponseDPredictorOrdinate(),
								aPORDLeft[i].getDResponseDPredictorOrdinate(), null == aaRVC ? null :
									aaRVC[i], bfwr)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		if (0 != (org.drip.math.regime.MultiSegmentRegime.CALIBRATE_JACOBIAN & iSetupMode)) {
			try {
				if (null == (_wjDCoeffDEdgeParams = new org.drip.math.calculus.WengertJacobian
					(_aSegment[0].numBasis(), iNumSegment + 1)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			org.drip.math.calculus.WengertJacobian wjDCoeffDEdgeParamsHead =
				_aSegment[0].jackDCoeffDEdgeParams();

			if (!setDCoeffDEdgeParams (0, wjDCoeffDEdgeParamsHead) || !setDCoeffDEdgeParams (1,
				wjDCoeffDEdgeParamsHead))
				return false;

			for (int i = 1; i < iNumSegment; ++i) {
				if (!setDCoeffDEdgeParams (i + 1, _aSegment[i].jackDCoeffDEdgeParams())) return false;
			}
		}

		return true;
	}

	@Override public double evaluate (
		final double dblLeftSlope)
		throws java.lang.Exception
	{
		if (null == _ssb || !_ssb.calibStartingSegment (dblLeftSlope) || !_ssb.calibSegmentSequence (1))
			throw new java.lang.Exception
				("MultiSegmentCalibratableRegime::evaluate => cannot set up segments!");

		int iBC = _ssb.getCalibrationBoundaryCondition();

		if (org.drip.math.regime.MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL == iBC)
			return calcRightEdgeDerivative (2);
		else if (org.drip.math.regime.MultiSegmentRegime.BOUNDARY_CONDITION_FINANCIAL == iBC)
			return calcRightEdgeDerivative (1);

		throw new java.lang.Exception ("MultiSegmentCalibratableRegime::evaluate => Boundary Condition " +
			iBC + " unknown");
	}

	@Override public boolean setLeftNode (
		final double dblRegimeLeftResponse,
		final double dblRegimeLeftResponseSlope,
		final double dblRegimeRightResponse,
		final org.drip.math.segment.BestFitWeightedResponse bfwr)
	{
		return _aSegment[0].calibrate (org.drip.math.segment.ResponseValueConstraint.FromPredictorResponse
			(getLeftPredictorOrdinateEdge(), dblRegimeLeftResponse), dblRegimeLeftResponseSlope,
				org.drip.math.segment.ResponseValueConstraint.FromPredictorResponse
					(getRightPredictorOrdinateEdge(), dblRegimeRightResponse), bfwr);
	}

	@Override public double responseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		return _aSegment[containingIndex (dblPredictorOrdinate, true, true)].responseValue
			(dblPredictorOrdinate);
	}

	@Override public org.drip.math.segment.PredictorOrdinateResponseDerivative calcPORD (
		final double dblPredictorOrdinate)
	{
		int iIndex = -1;

		try {
			iIndex = containingIndex (dblPredictorOrdinate, true, true);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		int iCk = _aSBP[iIndex].getSegmentElasticParams().getCk();

		double adblDeriv[] = new double[iCk];

		try {
			for (int i = 0; i < iCk; ++i)
				adblDeriv[i] = _aSegment[iIndex].calcResponseValueDerivative (dblPredictorOrdinate, i,
					false);

			return new org.drip.math.segment.PredictorOrdinateResponseDerivative
				(_aSegment[iIndex].responseValue (dblPredictorOrdinate), adblDeriv);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.math.calculus.WengertJacobian jackDResponseDResponseInput (
		final double dblPredictorOrdinate)
	{
		int iIndex = -1;

		try {
			iIndex = containingIndex (dblPredictorOrdinate, true, true);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return setDResponseDEdgeResponse (iIndex, _aSegment[iIndex].jackDResponseDEdgeParams
			(dblPredictorOrdinate));
	}

	@Override public org.drip.math.segment.Monotonocity monotoneType (
		final double dblPredictorOrdinate)
	{
		int iIndex = -1;

		try {
			iIndex = containingIndex (dblPredictorOrdinate, true, true);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return _aSegment[iIndex].monotoneType();
	}

	@Override public boolean isLocallyMonotone()
		throws java.lang.Exception
	{
		int iNumSegment = _aSegment.length;

		for (int i = 0; i < iNumSegment; ++i) {
			org.drip.math.segment.Monotonocity sm = null;

			try {
				sm = _aSegment[i].monotoneType();
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			if (null == sm || org.drip.math.segment.Monotonocity.MONOTONIC != sm.type()) return false;
		}

		return true;
	}

	@Override public boolean isCoMonotone (
		final double[] adblMeasuredResponse)
		throws java.lang.Exception
	{
		int iNumSegment = _aSegment.length;
		int[] aiMonotoneType = new int[iNumSegment];
		int[] aiNodeMiniMax = new int[iNumSegment + 1];

		if (null == adblMeasuredResponse || adblMeasuredResponse.length != iNumSegment + 1)
			throw new java.lang.Exception
				("MultiSegmentCalibratableRegime::isCoMonotone => Data input inconsistent with the segment");

		for (int i = 0; i < iNumSegment + 1; ++i) {
			if (0 == i || iNumSegment == i)
				aiNodeMiniMax[i] = MONOTONE_PREDICTOR_ORDINATE_NODE;
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

			if (i < iNumSegment) {
				org.drip.math.segment.Monotonocity sm = _aSegment[i].monotoneType();

				if (null != sm) aiMonotoneType[i] = sm.type();
			}
		}

		for (int i = 1; i < iNumSegment; ++i) {
			if (MAXIMA_PREDICTOR_ORDINATE_NODE == aiNodeMiniMax[i]) {
				if (org.drip.math.segment.Monotonocity.MAXIMA != aiMonotoneType[i] &&
					org.drip.math.segment.Monotonocity.MAXIMA != aiMonotoneType[i - 1])
					return false;
			} else if (MINIMA_PREDICTOR_ORDINATE_NODE == aiNodeMiniMax[i]) {
				if (org.drip.math.segment.Monotonocity.MINIMA != aiMonotoneType[i] &&
					org.drip.math.segment.Monotonocity.MINIMA != aiMonotoneType[i - 1])
					return false;
			}
		}

		return true;
	}

	@Override public boolean isKnot (
		final double dblPredictorOrdinate)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblPredictorOrdinate)) return false;

		int iNumSegment = _aSegment.length;

		for (int i = 0; i < iNumSegment; ++i) {
			if (dblPredictorOrdinate == _aSegment[i].left()) return false;
		}

		return dblPredictorOrdinate == _aSegment[iNumSegment - 1].left();
	}

	@Override public double calcRightEdgeDerivative (
		final int iOrder)
		throws java.lang.Exception
	{
		org.drip.math.segment.PredictorResponse seg = _aSegment[_aSegment.length - 1];

		return seg.calcResponseValueDerivative (seg.right(), iOrder, false);
	}

	@Override public boolean resetNode (
		final int iPredictorOrdinateIndex,
		final double dblResponseReset)
	{
		if (0 == iPredictorOrdinateIndex || 1 == iPredictorOrdinateIndex || _aSegment.length <
			iPredictorOrdinateIndex || !org.drip.math.common.NumberUtil.IsValid (dblResponseReset))
			return false;

		return _aSegment[iPredictorOrdinateIndex - 1].calibrate (_aSegment[iPredictorOrdinateIndex - 2],
			dblResponseReset, null);
	}

	@Override public boolean resetNode (
		final int iPredictorOrdinateIndex,
		final org.drip.math.segment.ResponseValueConstraint sprcReset)
	{
		if (0 == iPredictorOrdinateIndex || 1 == iPredictorOrdinateIndex || _aSegment.length <
			iPredictorOrdinateIndex || null == sprcReset)
			return false;

		return _aSegment[iPredictorOrdinateIndex - 1].calibrate (_aSegment[iPredictorOrdinateIndex - 2],
			sprcReset, null);
	}

	@Override public boolean in (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception ("MultiSegmentCalibratableRegime::in => Invalid inputs");

		return dblPredictorOrdinate >= getLeftPredictorOrdinateEdge() && dblPredictorOrdinate <=
			getRightPredictorOrdinateEdge();
	}

	@Override public double getLeftPredictorOrdinateEdge()
	{
		return _aSegment[0].left();
	}

	@Override public double getRightPredictorOrdinateEdge()
	{
		return _aSegment[_aSegment.length - 1].right();
	}

	@Override public int containingIndex (
		final double dblPredictorOrdinate,
		final boolean bIncludeLeft,
		final boolean bIncludeRight)
		throws java.lang.Exception
	{
		if (!in (dblPredictorOrdinate))
			throw new java.lang.Exception
				("MultiSegmentCalibratableRegime::containingIndex => Predictor Ordinate not in the Regime Range");

		int iNumSegment = _aSegment.length;

		for (int i = 0 ; i < iNumSegment; ++i) {
			boolean bLeftValid = bIncludeLeft ? _aSegment[i].left() <= dblPredictorOrdinate :
				_aSegment[i].left() < dblPredictorOrdinate;

			boolean bRightValid = bIncludeRight ? _aSegment[i].right() >= dblPredictorOrdinate :
				_aSegment[i].right() > dblPredictorOrdinate;

			if (bLeftValid && bRightValid) return i;
		}

		throw new java.lang.Exception
			("MultiSegmentCalibratableRegime::containingIndex => Cannot locate Containing Index");
	}

	@Override public MultiSegmentCalibratableRegime clipLeft (
		final java.lang.String strName,
		final double dblPredictorOrdinate)
	{
		int iNumSegment = _aSegment.length;
		int iContainingPredictorOrdinateIndex = 0;

		try {
			iContainingPredictorOrdinateIndex = containingIndex (dblPredictorOrdinate, true, false);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		int iNumClippedSegment = iNumSegment - iContainingPredictorOrdinateIndex;
		org.drip.math.segment.PredictorResponse[] aPR = new
			org.drip.math.segment.PredictorResponse[iNumClippedSegment];
		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[iNumClippedSegment];

		for (int i = 0; i < iNumClippedSegment; ++i) {
			if (null == (aPR[i] = 0 == i ?
				_aSegment[iContainingPredictorOrdinateIndex].clipLeftOfPredictorOrdinate
					(dblPredictorOrdinate) : _aSegment[i + iContainingPredictorOrdinateIndex]))
				return null;

			aPRBP[i] = _aSBP[i + iContainingPredictorOrdinateIndex];
		}

		try {
			return new MultiSegmentCalibratableRegime (strName, aPR, aPRBP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public MultiSegmentCalibratableRegime clipRight (
		final java.lang.String strName,
		final double dblPredictorOrdinate)
	{
		int iContainingPredictorOrdinateIndex = 0;

		try {
			iContainingPredictorOrdinateIndex = containingIndex (dblPredictorOrdinate, false, true);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.math.segment.PredictorResponse[] aPR = new
			org.drip.math.segment.PredictorResponse[iContainingPredictorOrdinateIndex + 1];
		org.drip.math.segment.PredictorResponseBuilderParams[] aPRBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[iContainingPredictorOrdinateIndex + 1];

		for (int i = 0; i <= iContainingPredictorOrdinateIndex; ++i) {
			if (null == (aPR[i] = iContainingPredictorOrdinateIndex == i ?
				_aSegment[iContainingPredictorOrdinateIndex].clipRightOfPredictorOrdinate
					(dblPredictorOrdinate) : _aSegment[i]))
				return null;

			aPRBP[i] = _aSBP[i];
		}

		try {
			return new MultiSegmentCalibratableRegime (strName, aPR, aPRBP);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public double dcpe()
		throws java.lang.Exception
	{
		double dblDCPE = 0.;

		for (org.drip.math.segment.PredictorResponse seg : _aSegment)
			dblDCPE += seg.dcpe();

		return dblDCPE;
	}

	@Override public java.lang.String displayString()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		for (int i = 0; i < _aSegment.length; ++i)
			sb.append (_aSegment[i].displayString() + " \n");

		return sb.toString();
	}
}

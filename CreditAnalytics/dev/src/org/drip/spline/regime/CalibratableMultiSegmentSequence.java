
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

public class CalibratableMultiSegmentSequence extends org.drip.quant.function1D.AbstractUnivariate implements
	org.drip.spline.regime.MultiSegmentSequence {
	private static final int MAXIMA_PREDICTOR_ORDINATE_NODE = 1;
	private static final int MINIMA_PREDICTOR_ORDINATE_NODE = 2;
	private static final int MONOTONE_PREDICTOR_ORDINATE_NODE = 4;

	private java.lang.String _strName = "";
	private org.drip.spline.regime.SegmentSequenceBuilder _ssb = null;
	private org.drip.spline.segment.ElasticConstitutiveState[] _aECS = null;
	private org.drip.spline.params.SegmentCustomBuilderControl[] _aSCBC = null;
	private org.drip.quant.calculus.WengertJacobian _wjDCoeffDEdgeParams = null;

	private boolean setDCoeffDEdgeParams (
		final int iNodeIndex,
		final org.drip.quant.calculus.WengertJacobian wjDCoeffDEdgeParams)
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

	private final org.drip.quant.calculus.WengertJacobian setDResponseDEdgeResponse (
		final int iNodeIndex,
		final org.drip.quant.calculus.WengertJacobian wjDResponseDEdgeParams)
	{
		if (null == wjDResponseDEdgeParams) return null;

		int iNumSegment = _aECS.length;
		org.drip.quant.calculus.WengertJacobian wjDResponseDEdgeResponse = null;

		try {
			wjDResponseDEdgeResponse = new org.drip.quant.calculus.WengertJacobian (1, iNumSegment + 1);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i <= iNumSegment; ++i) {
			if (i == iNodeIndex) {
				if (!wjDResponseDEdgeResponse.accumulatePartialFirstDerivative (0, i,
					wjDResponseDEdgeParams.getFirstDerivative (0,
						org.drip.spline.segment.ElasticConstitutiveState.LEFT_NODE_VALUE_PARAMETER_INDEX)) ||
							!wjDResponseDEdgeResponse.accumulatePartialFirstDerivative (0, i + 1,
								wjDResponseDEdgeParams.getFirstDerivative (0,
									org.drip.spline.segment.ElasticConstitutiveState.RIGHT_NODE_VALUE_PARAMETER_INDEX)))
					return null;
			}
		}

		return wjDResponseDEdgeResponse;
	}

	/**
	 * CalibratableMultiSegmentSequence constructor - Construct a sequence of Basis Spline Segments
	 * 
	 * @param strName Name of the Regime
	 * @param aECS Array of Segments
	 * @param aSCBC Array of Segment Builder Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CalibratableMultiSegmentSequence (
		final java.lang.String strName,
		final org.drip.spline.segment.ElasticConstitutiveState[] aECS,
		final org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC)
		throws java.lang.Exception
	{
		super (null);

		if (null == aECS || null == aSCBC || null == (_strName = strName) || _strName.isEmpty())
			throw new java.lang.Exception ("CalibratableMultiSegmentSequence ctr => Invalid inputs!");

		int iNumSegment = aECS.length;
		_aECS = new org.drip.spline.segment.ElasticConstitutiveState[iNumSegment];
		_aSCBC = new org.drip.spline.params.SegmentCustomBuilderControl[iNumSegment];

		if (0 == iNumSegment || iNumSegment != aSCBC.length)
			throw new java.lang.Exception ("CalibratableMultiSegmentSequence ctr => Invalid inputs!");

		for (int i = 0; i < iNumSegment; ++i) {
			if (null == (_aECS[i] = aECS[i]) || null == (_aSCBC[i] = aSCBC[i]))
				throw new java.lang.Exception ("CalibratableMultiSegmentSequence ctr => Invalid inputs!");
		}
	}

	@Override public java.lang.String name()
	{
		return _strName;
	}

	@Override public org.drip.spline.segment.ElasticConstitutiveState[] segments()
	{
		return _aECS;
	}

	@Override public org.drip.spline.params.SegmentCustomBuilderControl[] segmentBuilderControl()
	{
		return _aSCBC;
	}

	@Override public boolean setup (
		final org.drip.spline.regime.SegmentSequenceBuilder ssb,
		final int iCalibrationDetail)
	{
		if (null == (_ssb = ssb)) return false;

		if (!_ssb.setRegime (this)) return false;

		if (org.drip.spline.regime.MultiSegmentSequence.BOUNDARY_CONDITION_FLOATING ==
			_ssb.getCalibrationBoundaryCondition()) {
			if (!_ssb.calibStartingSegment (0.) || !_ssb.calibSegmentSequence (1)) return false;
		} else if (0 != (org.drip.spline.regime.MultiSegmentSequence.CALIBRATE & iCalibrationDetail)) {
			org.drip.quant.solver1D.FixedPointFinderOutput fpop = null;

			if (null == fpop || !fpop.containsRoot()) {
				try {
					fpop = new org.drip.quant.solver1D.FixedPointFinderNewton (0., this, true).findRoot();
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return false;
				}
			}

			if (null == fpop || !org.drip.quant.common.NumberUtil.IsValid (fpop.getRoot())) return false;
		}

		if (0 != (org.drip.spline.regime.MultiSegmentSequence.CALIBRATE_JACOBIAN &iCalibrationDetail)) {
			int iNumSegment = _aECS.length;

			try {
				if (null == (_wjDCoeffDEdgeParams = new org.drip.quant.calculus.WengertJacobian
					(_aECS[0].numBasis(), iNumSegment + 1)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			org.drip.quant.calculus.WengertJacobian wjHead = _aECS[0].jackDCoeffDEdgeParams();

			if (!setDCoeffDEdgeParams (0, wjHead) || !setDCoeffDEdgeParams (1, wjHead)) return false;

			for (int i = 1; i < iNumSegment; ++i) {
				if (!setDCoeffDEdgeParams (i + 1, _aECS[i].jackDCoeffDEdgeParams())) return false;
			}
		}

		return true;
	}

	@Override public boolean setup (
		final org.drip.spline.params.SegmentResponseValueConstraint srvcLeading,
		final org.drip.spline.params.SegmentResponseValueConstraint[] aSRVC,
		final org.drip.spline.params.SegmentBestFitResponse sbfr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		try {
			return setup (new org.drip.spline.regime.CkSegmentSequenceBuilder (srvcLeading, aSRVC, sbfr,
				iCalibrationBoundaryCondition), iCalibrationDetail);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public boolean setup (
		final double dblLeftRegimeResponseValue,
		final org.drip.spline.params.SegmentResponseValueConstraint[] aSRVC,
		final org.drip.spline.params.SegmentBestFitResponse sbfr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		return setup (org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
			(getLeftPredictorOrdinateEdge(), dblLeftRegimeResponseValue), aSRVC, sbfr,
				iCalibrationBoundaryCondition, iCalibrationDetail);
	}

	@Override public boolean setup (
		final double dblLeftRegimeResponseValue,
		final double[] adblSegmentRightResponseValue,
		final org.drip.spline.params.SegmentBestFitResponse sbfr,
		final int iCalibrationBoundaryCondition,
		final int iCalibrationDetail)
	{
		int iNumSegment = _aECS.length;
		org.drip.spline.params.SegmentResponseValueConstraint[] aSNVCRight = new
			org.drip.spline.params.SegmentResponseValueConstraint[iNumSegment];

		if (0 == iNumSegment || iNumSegment != adblSegmentRightResponseValue.length) return false;

		try {
			for (int i = 0; i < iNumSegment; ++i)
				aSNVCRight[i] = new org.drip.spline.params.SegmentResponseValueConstraint (new double[]
					{_aECS[i].right()}, new double[] {1.}, adblSegmentRightResponseValue[i]);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return setup (dblLeftRegimeResponseValue, aSNVCRight, sbfr, iCalibrationBoundaryCondition,
			iCalibrationDetail);
	}

	@Override public boolean setupHermite (
		final org.drip.spline.params.SegmentPredictorResponseDerivative[] aSPRDLeft,
		final org.drip.spline.params.SegmentPredictorResponseDerivative[] aSPRDRight,
		final org.drip.spline.params.SegmentResponseValueConstraint[][] aaSRVC,
		final org.drip.spline.params.SegmentBestFitResponse sbfr,
		final int iSetupMode)
	{
		if (null == aSPRDLeft || null == aSPRDRight) return false;

		int iNumSegment = _aECS.length;

		if (iNumSegment != aSPRDLeft.length || iNumSegment != aSPRDRight.length || (null != aaSRVC &&
			iNumSegment != aaSRVC.length))
			return false;

		for (int i = 0; i < iNumSegment; ++i) {
			try {
				if (0 != (org.drip.spline.regime.MultiSegmentSequence.CALIBRATE & iSetupMode) &&
					!_aECS[i].calibrate (new org.drip.spline.params.SegmentCalibrationInputSet (new double[]
						{0., 1.}, new double[] {aSPRDLeft[i].responseValue(), aSPRDRight[i].responseValue()},
							aSPRDLeft[i].getDResponseDPredictorOrdinate(),
								aSPRDLeft[i].getDResponseDPredictorOrdinate(), null == aaSRVC ? null :
									aaSRVC[i], sbfr)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		if (0 != (org.drip.spline.regime.MultiSegmentSequence.CALIBRATE_JACOBIAN & iSetupMode)) {
			try {
				if (null == (_wjDCoeffDEdgeParams = new org.drip.quant.calculus.WengertJacobian
					(_aECS[0].numBasis(), iNumSegment + 1)))
					return false;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}

			org.drip.quant.calculus.WengertJacobian wjDCoeffDEdgeParamsHead =
				_aECS[0].jackDCoeffDEdgeParams();

			if (!setDCoeffDEdgeParams (0, wjDCoeffDEdgeParamsHead) || !setDCoeffDEdgeParams (1,
				wjDCoeffDEdgeParamsHead))
				return false;

			for (int i = 1; i < iNumSegment; ++i) {
				if (!setDCoeffDEdgeParams (i + 1, _aECS[i].jackDCoeffDEdgeParams())) return false;
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
				("CalibratableMultiSegmentSequence::evaluate => cannot set up segments!");

		int iBC = _ssb.getCalibrationBoundaryCondition();

		if (org.drip.spline.regime.MultiSegmentSequence.BOUNDARY_CONDITION_NATURAL == iBC)
			return calcRightEdgeDerivative (2);
		else if (org.drip.spline.regime.MultiSegmentSequence.BOUNDARY_CONDITION_FINANCIAL == iBC)
			return calcRightEdgeDerivative (1);

		throw new java.lang.Exception ("CalibratableMultiSegmentSequence::evaluate => Boundary Condition " +
			iBC + " unknown");
	}

	@Override public boolean setLeftNode (
		final double dblRegimeLeftResponse,
		final double dblRegimeLeftResponseSlope,
		final double dblRegimeRightResponse,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		return _aECS[0].calibrate
			(org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
				(getLeftPredictorOrdinateEdge(), dblRegimeLeftResponse), dblRegimeLeftResponseSlope,
					org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
						(getRightPredictorOrdinateEdge(), dblRegimeRightResponse), sbfr);
	}

	@Override public double responseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		return _aECS[containingIndex (dblPredictorOrdinate, true, true)].responseValue
			(dblPredictorOrdinate);
	}

	@Override public org.drip.spline.params.SegmentPredictorResponseDerivative calcSPRD (
		final double dblPredictorOrdinate)
	{
		int iIndex = -1;

		try {
			iIndex = containingIndex (dblPredictorOrdinate, true, true);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		int iCk = _aSCBC[iIndex].inelasticParams().getCk();

		double adblDeriv[] = new double[iCk];

		try {
			for (int i = 0; i < iCk; ++i)
				adblDeriv[i] = _aECS[iIndex].calcResponseValueDerivative (dblPredictorOrdinate, i, false);

			return new org.drip.spline.params.SegmentPredictorResponseDerivative (_aECS[iIndex].responseValue
				(dblPredictorOrdinate), adblDeriv);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDResponseDResponseInput (
		final double dblPredictorOrdinate)
	{
		int iIndex = -1;

		try {
			iIndex = containingIndex (dblPredictorOrdinate, true, true);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return setDResponseDEdgeResponse (iIndex, _aECS[iIndex].jackDResponseDEdgeParams
			(dblPredictorOrdinate));
	}

	@Override public org.drip.spline.segment.Monotonocity monotoneType (
		final double dblPredictorOrdinate)
	{
		int iIndex = -1;

		try {
			iIndex = containingIndex (dblPredictorOrdinate, true, true);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return _aECS[iIndex].monotoneType();
	}

	@Override public boolean isLocallyMonotone()
		throws java.lang.Exception
	{
		int iNumSegment = _aECS.length;

		for (int i = 0; i < iNumSegment; ++i) {
			org.drip.spline.segment.Monotonocity mono = null;

			try {
				mono = _aECS[i].monotoneType();
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			if (null == mono || org.drip.spline.segment.Monotonocity.MONOTONIC != mono.type()) return false;
		}

		return true;
	}

	@Override public boolean isCoMonotone (
		final double[] adblMeasuredResponse)
		throws java.lang.Exception
	{
		int iNumSegment = _aECS.length;
		int[] aiMonotoneType = new int[iNumSegment];
		int[] aiNodeMiniMax = new int[iNumSegment + 1];

		if (null == adblMeasuredResponse || adblMeasuredResponse.length != iNumSegment + 1)
			throw new java.lang.Exception
				("CalibratableMultiSegmentSequence::isCoMonotone => Data input inconsistent with the segment");

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
				org.drip.spline.segment.Monotonocity mono = _aECS[i].monotoneType();

				if (null != mono) aiMonotoneType[i] = mono.type();
			}
		}

		for (int i = 1; i < iNumSegment; ++i) {
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

		int iNumSegment = _aECS.length;

		for (int i = 0; i < iNumSegment; ++i) {
			if (dblPredictorOrdinate == _aECS[i].left()) return false;
		}

		return dblPredictorOrdinate == _aECS[iNumSegment - 1].left();
	}

	@Override public double calcRightEdgeDerivative (
		final int iOrder)
		throws java.lang.Exception
	{
		org.drip.spline.segment.ElasticConstitutiveState ecs = _aECS[_aECS.length - 1];

		return ecs.calcResponseValueDerivative (ecs.right(), iOrder, false);
	}

	@Override public boolean resetNode (
		final int iPredictorOrdinateIndex,
		final double dblResponseReset)
	{
		if (0 == iPredictorOrdinateIndex || 1 == iPredictorOrdinateIndex || _aECS.length <
			iPredictorOrdinateIndex || !org.drip.quant.common.NumberUtil.IsValid (dblResponseReset))
			return false;

		return _aECS[iPredictorOrdinateIndex - 1].calibrate (_aECS[iPredictorOrdinateIndex - 2],
			dblResponseReset, null);
	}

	@Override public boolean resetNode (
		final int iPredictorOrdinateIndex,
		final org.drip.spline.params.SegmentResponseValueConstraint srvcReset)
	{
		if (0 == iPredictorOrdinateIndex || 1 == iPredictorOrdinateIndex || _aECS.length <
			iPredictorOrdinateIndex || null == srvcReset)
			return false;

		return _aECS[iPredictorOrdinateIndex - 1].calibrate (_aECS[iPredictorOrdinateIndex - 2], srvcReset,
			null);
	}

	@Override public boolean in (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception ("CalibratableMultiSegmentSequence::in => Invalid inputs");

		return dblPredictorOrdinate >= getLeftPredictorOrdinateEdge() && dblPredictorOrdinate <=
			getRightPredictorOrdinateEdge();
	}

	@Override public double getLeftPredictorOrdinateEdge()
	{
		return _aECS[0].left();
	}

	@Override public double getRightPredictorOrdinateEdge()
	{
		return _aECS[_aECS.length - 1].right();
	}

	@Override public int containingIndex (
		final double dblPredictorOrdinate,
		final boolean bIncludeLeft,
		final boolean bIncludeRight)
		throws java.lang.Exception
	{
		if (!in (dblPredictorOrdinate))
			throw new java.lang.Exception
				("CalibratableMultiSegmentSequence::containingIndex => Predictor Ordinate not in the Regime Range");

		int iNumSegment = _aECS.length;

		for (int i = 0 ; i < iNumSegment; ++i) {
			boolean bLeftValid = bIncludeLeft ? _aECS[i].left() <= dblPredictorOrdinate : _aECS[i].left() <
				dblPredictorOrdinate;

			boolean bRightValid = bIncludeRight ? _aECS[i].right() >= dblPredictorOrdinate : _aECS[i].right()
				> dblPredictorOrdinate;

			if (bLeftValid && bRightValid) return i;
		}

		throw new java.lang.Exception
			("CalibratableMultiSegmentSequence::containingIndex => Cannot locate Containing Index");
	}

	@Override public CalibratableMultiSegmentSequence clipLeft (
		final java.lang.String strName,
		final double dblPredictorOrdinate)
	{
		int iNumSegment = _aECS.length;
		int iContainingPredictorOrdinateIndex = 0;

		try {
			iContainingPredictorOrdinateIndex = containingIndex (dblPredictorOrdinate, true, false);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		int iNumClippedSegment = iNumSegment - iContainingPredictorOrdinateIndex;
		org.drip.spline.segment.ElasticConstitutiveState[] aECS = new
			org.drip.spline.segment.ElasticConstitutiveState[iNumClippedSegment];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC = new
			org.drip.spline.params.SegmentCustomBuilderControl[iNumClippedSegment];

		for (int i = 0; i < iNumClippedSegment; ++i) {
			if (null == (aECS[i] = 0 == i ?
				_aECS[iContainingPredictorOrdinateIndex].clipLeftOfPredictorOrdinate (dblPredictorOrdinate) :
					_aECS[i + iContainingPredictorOrdinateIndex]))
				return null;

			aSCBC[i] = _aSCBC[i + iContainingPredictorOrdinateIndex];
		}

		try {
			return new CalibratableMultiSegmentSequence (strName, aECS, aSCBC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public CalibratableMultiSegmentSequence clipRight (
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

		org.drip.spline.segment.ElasticConstitutiveState[] aECS = new
			org.drip.spline.segment.ElasticConstitutiveState[iContainingPredictorOrdinateIndex + 1];
		org.drip.spline.params.SegmentCustomBuilderControl[] aSCBC = new
			org.drip.spline.params.SegmentCustomBuilderControl[iContainingPredictorOrdinateIndex + 1];

		for (int i = 0; i <= iContainingPredictorOrdinateIndex; ++i) {
			if (null == (aECS[i] = iContainingPredictorOrdinateIndex == i ?
				_aECS[iContainingPredictorOrdinateIndex].clipRightOfPredictorOrdinate
					(dblPredictorOrdinate) : _aECS[i]))
				return null;

			aSCBC[i] = _aSCBC[i];
		}

		try {
			return new CalibratableMultiSegmentSequence (strName, aECS, aSCBC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public double dcpe()
		throws java.lang.Exception
	{
		double dblDCPE = 0.;

		for (org.drip.spline.segment.ElasticConstitutiveState ecs : _aECS)
			dblDCPE += ecs.dcpe();

		return dblDCPE;
	}

	@Override public java.lang.String displayString()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		for (int i = 0; i < _aECS.length; ++i)
			sb.append (_aECS[i].displayString() + " \n");

		return sb.toString();
	}
}

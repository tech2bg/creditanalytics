	
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

public class CkSegmentSequenceBuilder implements org.drip.math.regime.SegmentSequenceBuilder {
	private int _iCalibrationBoundaryCondition = -1;
	private org.drip.math.regime.MultiSegmentRegime _msr = null;
	private org.drip.math.segment.ResponseValueConstraint[] _aRVC = null;
	private org.drip.math.segment.ResponseValueConstraint _rvcLeading = null;

	/**
	 * CkSegmentSequenceBuilder constructor
	 * 
	 * @param rvcLeading Leading Segment Response Value Constraint
	 * @param aRVC Array of Segment Response Value Constraints
	 * @param iCalibrationBoundaryCondition Solver Mode - FLOATING | NATURAL | FINANCIAL
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CkSegmentSequenceBuilder (
		final org.drip.math.segment.ResponseValueConstraint rvcLeading,
		final org.drip.math.segment.ResponseValueConstraint[] aRVC,
		final int iCalibrationBoundaryCondition)
		throws java.lang.Exception
	{
		if (null == (_rvcLeading = rvcLeading) || null == (_aRVC = aRVC) || 0 == _aRVC.length)
			throw new java.lang.Exception ("CkSegmentSequenceBuilder ctr: Invalid inputs!");

		_iCalibrationBoundaryCondition = iCalibrationBoundaryCondition;
	}

	@Override public boolean setRegime (
		final org.drip.math.regime.MultiSegmentRegime msr)
	{
		if (null == msr) return false;

		_msr = msr;
		return true;
	}

	@Override public int getCalibrationBoundaryCondition()
	{
		return _iCalibrationBoundaryCondition;
	}

	@Override public boolean calibStartingSegment (
		final double dblLeftSlope)
	{
		if (null == _msr) return false;

		org.drip.math.segment.PredictorResponse[] aPR = _msr.getSegments();

		return null != aPR && 1 <= aPR.length ? aPR[0].calibrate (_rvcLeading, dblLeftSlope, _aRVC[0]) :
			false;
	}

	@Override public boolean calibSegmentSequence (
		final int iStartingSegment)
	{
		if (null == _msr) return false;

		org.drip.math.segment.PredictorResponse[] aPR = _msr.getSegments();

		int iNumSegment = aPR.length;

		for (int iSegment = iStartingSegment; iSegment < iNumSegment; ++iSegment) {
			if (!aPR[iSegment].calibrate (0 == iSegment ? null : aPR[iSegment - 1], _aRVC[iSegment]))
				return false;
		}

		return true;
	}
}

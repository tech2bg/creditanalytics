	
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

public class CkSegmentSequenceBuilder implements org.drip.spline.regime.SegmentSequenceBuilder {
	private int _iCalibrationBoundaryCondition = -1;
	private org.drip.spline.regime.MultiSegmentSequence _mss = null;
	private org.drip.spline.params.SegmentBestFitResponse _sbfr = null;
	private org.drip.spline.params.SegmentResponseValueConstraint[] _aSRVC = null;
	private org.drip.spline.params.SegmentResponseValueConstraint _srvcLeading = null;

	/**
	 * CkSegmentSequenceBuilder constructor
	 * 
	 * @param srvcLeading Leading Segment Response Value Constraint
	 * @param aSRVC Array of Segment Response Value Constraints
	 * @param iCalibrationBoundaryCondition Solver Mode - FLOATING | NATURAL | FINANCIAL
	 * @param sbfr Best Fit Weighted Response
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CkSegmentSequenceBuilder (
		final org.drip.spline.params.SegmentResponseValueConstraint srvcLeading,
		final org.drip.spline.params.SegmentResponseValueConstraint[] aSRVC,
		final org.drip.spline.params.SegmentBestFitResponse sbfr,
		final int iCalibrationBoundaryCondition)
		throws java.lang.Exception
	{
		if (null == (_srvcLeading = srvcLeading) || null == (_aSRVC = aSRVC) || 0 == _aSRVC.length)
			throw new java.lang.Exception ("CkSegmentSequenceBuilder ctr: Invalid inputs!");

		_sbfr = sbfr;
		_iCalibrationBoundaryCondition = iCalibrationBoundaryCondition;
	}

	@Override public boolean setRegime (
		final org.drip.spline.regime.MultiSegmentSequence mss)
	{
		if (null == mss) return false;

		_mss = mss;
		return true;
	}

	@Override public int getCalibrationBoundaryCondition()
	{
		return _iCalibrationBoundaryCondition;
	}

	@Override public boolean calibStartingSegment (
		final double dblLeftSlope)
	{
		if (null == _mss) return false;

		org.drip.spline.segment.ElasticConstitutiveState[] aECS = _mss.segments();

		return null != aECS && 1 <= aECS.length ? aECS[0].calibrate (_srvcLeading, dblLeftSlope, _aSRVC[0],
			_sbfr) : false;
	}

	@Override public boolean calibSegmentSequence (
		final int iStartingSegment)
	{
		if (null == _mss) return false;

		org.drip.spline.segment.ElasticConstitutiveState[] aECS = _mss.segments();

		int iNumSegment = aECS.length;

		for (int iSegment = iStartingSegment; iSegment < iNumSegment; ++iSegment) {
			if (!aECS[iSegment].calibrate (0 == iSegment ? null : aECS[iSegment - 1], _aSRVC[iSegment],
				_sbfr))
				return false;
		}

		return true;
	}
}

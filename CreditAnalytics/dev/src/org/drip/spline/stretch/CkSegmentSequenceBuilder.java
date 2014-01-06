	
package org.drip.spline.stretch;

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

public class CkSegmentSequenceBuilder implements org.drip.spline.stretch.SegmentSequenceBuilder {
	private org.drip.spline.stretch.BoundarySettings _bs = null;
	private org.drip.spline.stretch.MultiSegmentSequence _mss = null;
	private org.drip.spline.params.StretchBestFitResponse _rbfr = null;
	private org.drip.spline.params.SegmentResponseValueConstraint[] _aSRVC = null;
	private org.drip.spline.params.SegmentResponseValueConstraint _srvcLeading = null;

	/**
	 * CkSegmentSequenceBuilder constructor
	 * 
	 * @param srvcLeading Leading Segment Response Value Constraint
	 * @param aSRVC Array of Segment Response Value Constraints
	 * @param rbfr Sequence Best Fit Weighted Response
	 * @param bs The Calibration Boundary Condition
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CkSegmentSequenceBuilder (
		final org.drip.spline.params.SegmentResponseValueConstraint srvcLeading,
		final org.drip.spline.params.SegmentResponseValueConstraint[] aSRVC,
		final org.drip.spline.params.StretchBestFitResponse rbfr,
		final org.drip.spline.stretch.BoundarySettings bs)
		throws java.lang.Exception
	{
		_rbfr = rbfr;
		_aSRVC = aSRVC;
		_srvcLeading = srvcLeading;

		if (null == _srvcLeading && (null == _aSRVC || 0 == _aSRVC.length) && null == _rbfr)
			throw new java.lang.Exception ("CkSegmentSequenceBuilder ctr: Invalid inputs!");

		if (null == (_bs = bs))
			throw new java.lang.Exception ("CkSegmentSequenceBuilder ctr: Invalid inputs!");
	}

	@Override public boolean setStretch (
		final org.drip.spline.stretch.MultiSegmentSequence mss)
	{
		if (null == mss) return false;

		_mss = mss;
		return true;
	}

	@Override public org.drip.spline.stretch.BoundarySettings getCalibrationBoundaryCondition()
	{
		return _bs;
	}

	@Override public boolean calibStartingSegment (
		final double dblLeftSlope,
		final double dblLeftSlopeSensitivity)
	{
		if (null == _mss) return false;

		org.drip.spline.segment.ConstitutiveState[] aCS = _mss.segments();

		return null != aCS && 1 <= aCS.length ? aCS[0].calibrate (_srvcLeading, null, dblLeftSlope,
			dblLeftSlopeSensitivity, null == _aSRVC ? null : _aSRVC[0], null, null == _rbfr ? null :
				_rbfr.sizeToSegment (aCS[0]), null) : false;
	}

	@Override public boolean calibSegmentSequence (
		final int iStartingSegment)
	{
		if (null == _mss) return false;

		org.drip.spline.segment.ConstitutiveState[] aCS = _mss.segments();

		int iNumSegment = aCS.length;

		for (int iSegment = iStartingSegment; iSegment < iNumSegment; ++iSegment) {
			if (!aCS[iSegment].calibrate (0 == iSegment ? null : aCS[iSegment - 1], null == _aSRVC ? null :
				_aSRVC[iSegment], null, null == _rbfr ? null : _rbfr.sizeToSegment (aCS[iSegment]), null))
				return false;
		}

		return true;
	}
}

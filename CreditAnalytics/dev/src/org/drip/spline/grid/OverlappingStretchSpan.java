
package org.drip.spline.grid;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
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
 * OverlappingStretchSpan implements the Span interface, and the collection functionality of overlapping
 *  Stretches. In addition to providing a custom implementation of all the Span interface stubs, it also
 *  converts the Overlapping Stretch Span to a non-overlapping Stretch Span. Overlapping Stretches are
 *  clipped from the Left.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OverlappingStretchSpan implements org.drip.spline.grid.Span {
	private java.util.List<org.drip.spline.stretch.MultiSegmentSequence> _lsMSS = new
		java.util.ArrayList<org.drip.spline.stretch.MultiSegmentSequence>();

	/**
	 * OverlappingStretchSpan constructor
	 * 
	 * @param mss The Initial Stretch in the Span
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public OverlappingStretchSpan (
		final org.drip.spline.stretch.MultiSegmentSequence mss)
		throws java.lang.Exception
	{
		if (null == mss) throw new java.lang.Exception ("OverlappingStretchSpan ctr: Invalid Inputs");

		_lsMSS.add (mss);
	}

	@Override public boolean addStretch (
		final org.drip.spline.stretch.MultiSegmentSequence mss)
	{
		if (null == mss) return false;

		_lsMSS.add (mss);

		return true;
	}

	@Override public org.drip.spline.stretch.MultiSegmentSequence getContainingStretch (
		final double dblPredictorOrdinate)
	{
		if (null == _lsMSS || 0 == _lsMSS.size()) return null;

		for (org.drip.spline.stretch.MultiSegmentSequence mss : _lsMSS) {
			try {
				if (mss.in (dblPredictorOrdinate)) return mss;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return null;
	}

	@Override public org.drip.spline.stretch.MultiSegmentSequence getStretch (
		final java.lang.String strName)
	{
		if (null == strName) return null;

		for (org.drip.spline.stretch.MultiSegmentSequence mss : _lsMSS) {
			if (strName.equalsIgnoreCase (mss.name())) return mss;
		}

		return null;
	}

	@Override public double left()
		throws java.lang.Exception
	{
		if (0 == _lsMSS.size())
			throw new java.lang.Exception ("OverlappingStretchSpan::left => No valid Stretches found");

		return _lsMSS.get (0).getLeftPredictorOrdinateEdge();
	}

	@Override public double right()
		throws java.lang.Exception
	{
		if (0 == _lsMSS.size())
			throw new java.lang.Exception ("OverlappingStretchSpan::right => No valid Stretches found");

		return _lsMSS.get (_lsMSS.size() - 1).getRightPredictorOrdinateEdge();
	}

	@Override public double calcResponseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		for (org.drip.spline.stretch.MultiSegmentSequence mss : _lsMSS) {
			if (mss.in (dblPredictorOrdinate)) return mss.responseValue (dblPredictorOrdinate);
		}

		throw new java.lang.Exception ("OverlappingStretchSpan::calcResponseValue => Cannot Calculate!");
	}

	@Override public boolean isMergeState (
		final double dblPredictorOrdinate,
		final org.drip.state.representation.LatentStateLabel lsl)
	{
		try {
			for (org.drip.spline.stretch.MultiSegmentSequence mss : _lsMSS) {
				if (mss.in (dblPredictorOrdinate)) {
					org.drip.state.representation.MergeSubStretchManager msm = mss.msm();

					return null == msm ? false : msm.partOfMergeState (dblPredictorOrdinate, lsl);
				}
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDResponseDQuote (
		final double dblPredictorOrdinate,
		final int iOrder)
	{
		if (0 == _lsMSS.size()) return null;

		java.util.List<org.drip.quant.calculus.WengertJacobian> lsWJ = new
			java.util.ArrayList<org.drip.quant.calculus.WengertJacobian>();

		boolean bPredictorOrdinateCovered = false;

		for (org.drip.spline.stretch.MultiSegmentSequence mss : _lsMSS) {
			if (null == mss) continue;

			try {
				org.drip.quant.calculus.WengertJacobian wj = null;

				if (!bPredictorOrdinateCovered && mss.in (dblPredictorOrdinate)) {
					wj = mss.jackDResponseDQuote (dblPredictorOrdinate, iOrder);

					bPredictorOrdinateCovered = true;
				} else
					wj = new org.drip.quant.calculus.WengertJacobian (1, mss.segments().length);

				if (null != wj) lsWJ.add (wj);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return org.drip.quant.common.CollectionUtil.AppendWengert (lsWJ);
	}

	/**
	 * Convert the Overlapping Stretch Span to a non-overlapping Stretch Span. Overlapping Stretches are
	 *  clipped from the Left.
	 *  
	 * @return The Non-overlapping Stretch Span Instance
	 */

	public org.drip.spline.grid.Span toNonOverlapping()
	{
		if (0 == _lsMSS.size()) return null;

		org.drip.spline.grid.OverlappingStretchSpan oss = null;
		org.drip.spline.stretch.MultiSegmentSequence mssPrev = null;

		for (org.drip.spline.stretch.MultiSegmentSequence mss : _lsMSS) {
			if (null == mss) continue;

			if (null == oss) {
				try {
					oss = new org.drip.spline.grid.OverlappingStretchSpan (mssPrev = mss);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			} else {
				double dblPrevRightPredictorOrdinateEdge = mssPrev.getRightPredictorOrdinateEdge();

				double dblCurrentLeftPredictorOrdinateEdge = mss.getLeftPredictorOrdinateEdge();

				if (dblCurrentLeftPredictorOrdinateEdge >= dblPrevRightPredictorOrdinateEdge)
					oss.addStretch (mss);
				else
					oss.addStretch (mss.clipLeft (mss.name(), dblPrevRightPredictorOrdinateEdge));
			}
		}

		return oss;
	}

	@Override public void displayString()
	{
		for (org.drip.spline.stretch.MultiSegmentSequence mss : _lsMSS) {
			try {
				System.out.println (new org.drip.analytics.date.JulianDate
					(mss.getLeftPredictorOrdinateEdge()) + " => " + new org.drip.analytics.date.JulianDate
						(mss.getRightPredictorOrdinateEdge()));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}
	}
}

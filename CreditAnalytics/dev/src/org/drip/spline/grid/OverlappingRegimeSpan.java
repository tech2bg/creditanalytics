
package org.drip.spline.grid;

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
 * OverlappingRegimeSpan implements a collection of overlapping Regimes.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OverlappingRegimeSpan implements org.drip.spline.grid.Span {
	private java.util.List<org.drip.spline.regime.MultiSegmentSequence> _lsMSS = new
		java.util.ArrayList<org.drip.spline.regime.MultiSegmentSequence>();

	/**
	 * OverlappingRegimeSpan constructor
	 * 
	 * @param regime The Initial Regime in the Span
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public OverlappingRegimeSpan (
		final org.drip.spline.regime.MultiSegmentSequence mss)
		throws java.lang.Exception
	{
		if (null == mss) throw new java.lang.Exception ("OverlappingRegimeSpan ctr: Invalid Inputs");

		_lsMSS.add (mss);
	}

	@Override public boolean addRegime (
		final org.drip.spline.regime.MultiSegmentSequence mss)
	{
		if (null == mss) return false;

		_lsMSS.add (mss);

		return true;
	}

	@Override public org.drip.spline.regime.MultiSegmentSequence getContainingRegime (
		final double dblPredictorOrdinate)
	{
		for (org.drip.spline.regime.MultiSegmentSequence mss : _lsMSS) {
			try {
				if (mss.in (dblPredictorOrdinate)) return mss;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return null;
	}

	@Override public org.drip.spline.regime.MultiSegmentSequence getRegime (
		final java.lang.String strName)
	{
		if (null == strName) return null;

		for (org.drip.spline.regime.MultiSegmentSequence mss : _lsMSS) {
			if (strName.equalsIgnoreCase (mss.name())) return mss;
		}

		return null;
	}

	@Override public double calcResponseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		for (org.drip.spline.regime.MultiSegmentSequence mss : _lsMSS) {
			if (mss.in (dblPredictorOrdinate)) return mss.responseValue (dblPredictorOrdinate);
		}

		throw new java.lang.Exception ("OverlappingRegimeSpan::calcResponseValue => Cannot Calculate!");
	}

	@Override public double left() throws java.lang.Exception
	{
		if (0 == _lsMSS.size())
			throw new java.lang.Exception ("OverlappingRegimeSpan::left => No valid Regimes found");

		return _lsMSS.get (0).getLeftPredictorOrdinateEdge();
	}

	@Override public double right() throws java.lang.Exception
	{
		if (0 == _lsMSS.size())
			throw new java.lang.Exception ("OverlappingRegimeSpan::right => No valid Regimes found");

		return _lsMSS.get (_lsMSS.size() - 1).getRightPredictorOrdinateEdge();
	}

	/**
	 * Convert the Overlapping Regime Span to a non-overlapping Regime Span. Overlapping Regimes are clipped
	 * 	from the Left.
	 *  
	 * @return The Non-overlapping Regime Span Instance
	 */

	public org.drip.spline.grid.Span toNonOverlapping()
	{
		if (0 == _lsMSS.size()) return null;

		org.drip.spline.grid.OverlappingRegimeSpan ors = null;
		org.drip.spline.regime.MultiSegmentSequence mssPrev = null;

		for (org.drip.spline.regime.MultiSegmentSequence mss : _lsMSS) {
			if (null == mss) continue;

			if (null == ors) {
				try {
					ors = new org.drip.spline.grid.OverlappingRegimeSpan (mssPrev = mss);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			} else {
				double dblPrevRightPredictorOrdinateEdge = mssPrev.getRightPredictorOrdinateEdge();

				double dblCurrentLeftPredictorOrdinateEdge = mss.getLeftPredictorOrdinateEdge();

				if (dblCurrentLeftPredictorOrdinateEdge >= dblPrevRightPredictorOrdinateEdge)
					ors.addRegime (mss);
				else
					ors.addRegime (mss.clipLeft (mss.name(), dblPrevRightPredictorOrdinateEdge));
			}
		}

		return ors;
	}
}


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
 * NonOverlappingRegimeSpan implements a collection of regimes whose predictor ordinate ranges are strictly
 * 	non-overlapping (adjacency is allowed). Typical NonOverlappingRegimeSpan consist of functional regimes
 *  coupled together by transition regimes.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NonOverlappingRegimeSpan implements org.drip.math.grid.Span {
	private java.util.List<org.drip.math.regime.MultiSegmentRegime> _lsRegime = new
		java.util.ArrayList<org.drip.math.regime.MultiSegmentRegime>();

	/**
	 * NonOverlappingRegimeSpan constructor
	 * 
	 * @param regime The Initial Regime in the Span
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public NonOverlappingRegimeSpan (
		final org.drip.math.regime.MultiSegmentRegime regime)
		throws java.lang.Exception
	{
		if (null == regime) throw new java.lang.Exception ("NonOverlappingRegimeSpan ctr: Invalid Inputs");

		_lsRegime.add (regime);
	}

	@Override public boolean addRegime (
		final org.drip.math.regime.MultiSegmentRegime regime)
	{
		if (null == regime) return false;

		_lsRegime.add (regime);

		return true;
	}

	@Override public org.drip.math.regime.MultiSegmentRegime getContainingRegime (
		final double dblPredictorOrdinate)
	{
		for (org.drip.math.regime.MultiSegmentRegime regime : _lsRegime) {
			try {
				if (regime.in (dblPredictorOrdinate)) return regime;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return null;
	}

	@Override public org.drip.math.regime.MultiSegmentRegime getRegime (
		final java.lang.String strName)
	{
		if (null == strName) return null;

		for (org.drip.math.regime.MultiSegmentRegime regime : _lsRegime) {
			if (strName.equalsIgnoreCase (regime.name())) return regime;
		}

		return null;
	}

	@Override public double calcResponseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		for (org.drip.math.regime.MultiSegmentRegime regime : _lsRegime) {
			if (regime.in (dblPredictorOrdinate)) return regime.response (dblPredictorOrdinate);
		}

		throw new java.lang.Exception ("NonOverlappingRegimeSpan::calcResponseValue => Cannot Calculate!");
	}

	@Override public double left() throws java.lang.Exception
	{
		if (0 == _lsRegime.size())
			throw new java.lang.Exception ("NonOverlappingRegimeSpan::left => No valid Regimes found");

		return _lsRegime.get (0).getLeftPredictorOrdinateEdge();
	}

	@Override public double right() throws java.lang.Exception
	{
		if (0 == _lsRegime.size())
			throw new java.lang.Exception ("NonOverlappingRegimeSpan::right => No valid Regimes found");

		return _lsRegime.get (_lsRegime.size() - 1).getRightPredictorOrdinateEdge();
	}
}
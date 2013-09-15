
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
 * OverlappingRegimeSpan implements a collection of overlapping Regimes.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OverlappingRegimeSpan {
	private java.util.List<org.drip.math.grid.MultiSegmentRegime> _lsRegime = new
		java.util.ArrayList<org.drip.math.grid.MultiSegmentRegime>();

	/**
	 * OverlappingRegimeSpan constructor
	 * 
	 * @param regime The Initial Regime in the Span
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public OverlappingRegimeSpan (
		final org.drip.math.grid.MultiSegmentRegime regime)
		throws java.lang.Exception
	{
		if (null == regime) throw new java.lang.Exception ("OverlappingRegimeSpan ctr: Invalid Inputs");

		_lsRegime.add (regime);
	}

	/**
	 * Add a Regime to the Span
	 * 
	 * @param regime Regime to be added
	 * 
	 * @return TRUE => Regime added successfully
	 */

	public boolean addRegime (
		final org.drip.math.grid.MultiSegmentRegime regime)
	{
		if (null == regime) return false;

		_lsRegime.add (regime);

		return true;
	}

	/**
	 * Retrieve the first Regime that contains the Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The containing Regime
	 */

	public org.drip.math.grid.MultiSegmentRegime getContainingRegime (
		final double dblPredictorOrdinate)
	{
		for (org.drip.math.grid.MultiSegmentRegime regime : _lsRegime) {
			try {
				if (regime.in (dblPredictorOrdinate)) return regime;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return null;
	}

	/**
	 * Retrieve the Regime by Name
	 * 
	 * @param strName The Regime Name
	 * 
	 * @return The Regime
	 */

	public org.drip.math.grid.MultiSegmentRegime getRegime (
		final java.lang.String strName)
	{
		if (null == strName) return null;

		for (org.drip.math.grid.MultiSegmentRegime regime : _lsRegime) {
			if (strName.equalsIgnoreCase (regime.name())) return regime;
		}

		return null;
	}
}

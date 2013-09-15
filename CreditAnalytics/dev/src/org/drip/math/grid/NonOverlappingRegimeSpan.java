
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

public class NonOverlappingRegimeSpan {
	private java.util.List<org.drip.math.grid.MultiSegmentRegime> _lsRegime = new
		java.util.ArrayList<org.drip.math.grid.MultiSegmentRegime>();

	/**
	 * NonOverlappingRegimeSpan constructor
	 * 
	 * @param regime The Initial Regime in the Span
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public NonOverlappingRegimeSpan (
		final org.drip.math.grid.MultiSegmentRegime regime)
		throws java.lang.Exception
	{
		if (null == regime) throw new java.lang.Exception ("NonOverlappingRegimeSpan ctr: Invalid Inputs");

		_lsRegime.add (regime);
	}
}


package org.drip.kernel.spaces;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * MetricCardinality contains the Type and the Measure of the Metric Cardinality.
 *
 * @author Lakshmi Krishnamurthy
 */

public class MetricCardinality {

	/**
	 * Cardinality Type - Countably Finite
	 */

	public static final int CARD_COUNTABLY_FINITE = 1;

	/**
	 * Cardinality Type - Countably Infinite
	 */

	public static final int CARD_COUNTABLY_INFINITE = 2;

	/**
	 * Cardinality Type - Uncountably Infinite
	 */

	public static final int CARD_UNCOUNTABLY_INFINITE = 3;

	private int _iType = -1;
	private double _dblNumber = java.lang.Double.NaN;

	/**
	 * Countably Finite Metric Cardinality
	 * 
	 * @param dblNumber The Cardinality Number
	 * 
	 * @return The Metric Cardinality Instance
	 */

	public static final MetricCardinality CountablyFinite (
		final double dblNumber)
	{
		try {
			return new MetricCardinality (CARD_COUNTABLY_FINITE, dblNumber);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Countably Infinite Metric Cardinality
	 * 
	 * @return The Metric Cardinality Instance
	 */

	public static final MetricCardinality CountablyInfinite()
	{
		try {
			return new MetricCardinality (CARD_COUNTABLY_INFINITE, java.lang.Double.POSITIVE_INFINITY);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Uncountably Infinite Metric Cardinality
	 * 
	 * @return The Metric Cardinality Instance
	 */

	public static final MetricCardinality UncountablyInfinite()
	{
		try {
			return new MetricCardinality (CARD_UNCOUNTABLY_INFINITE, java.lang.Double.POSITIVE_INFINITY);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * MetricCardinality Constructor
	 * 
	 * @param iType Cardinality Type
	 * @param dblNumber Cardinality Number
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public MetricCardinality (
		final int iType,
		final double dblNumber)
		throws java.lang.Exception
	{
		if ((CARD_COUNTABLY_FINITE != (_iType = iType) && CARD_COUNTABLY_INFINITE != _iType &&
			CARD_UNCOUNTABLY_INFINITE != _iType) || java.lang.Double.isNaN (_dblNumber = dblNumber))
			throw new java.lang.Exception ("MetricCardinality ctr => Invalid Inputs");
	}

	/**
	 * Retrieve the Cardinality Type
	 * 
	 * @return The Cardinality Type
	 */

	public int type()
	{
		return _iType;
	}

	/**
	 * Retrieve the Cardinality Number
	 * 
	 * @return The Cardinality Number
	 */

	public double number()
	{
		return _dblNumber;
	}
}


package org.drip.param.valuation;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * WorkoutInfo is the place-holder for the work-out parameters. It contains the date, the factor, the type,
 *  and the yield of the work-out.
 *
 * @author Lakshmi Krishnamurthy
 */

public class WorkoutInfo {

	/**
	 * Work out type Call
	 */

	public static final int WO_TYPE_CALL = 1;

	/**
	 * Work out type Put
	 */

	public static final int WO_TYPE_PUT = 2;

	/**
	 * Work out type Maturity
	 */

	public static final int WO_TYPE_MATURITY = 3;

	private int _iWOType = WO_TYPE_MATURITY;
	private double _dblDate = java.lang.Double.NaN;
	private double _dblYield = java.lang.Double.NaN;
	private double _dblExerciseFactor = java.lang.Double.NaN;

	/**
	 * Constructor: Construct the class from the work-out date, yield, exercise factor, and type
	 * 
	 * @param dblDate Work-out Date
	 * @param dblYield Work-out Yield
	 * @param dblExerciseFactor Work-out Factor
	 * @param iWOType Work out Type
	 * 
	 * @throws java.lang.Exception Thrown if input is invalid
	 */

	public WorkoutInfo (
		final double dblDate,
		final double dblYield,
		final double dblExerciseFactor,
		final int iWOType)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblDate = dblDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblYield = dblYield) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblExerciseFactor = dblExerciseFactor))
			throw new java.lang.Exception ("WorkoutInfo ctr: One of wkout dat/yld/ex factor came out NaN!");

		_iWOType= iWOType;
	}

	/**
	 * Retrieve the Work-out Date
	 * 
	 * @return The Work-out Date
	 */

	public double date()
	{
		return _dblDate;
	}

	/**
	 * Retrieve the Work-out Yield
	 * 
	 * @return The Work-out Yield
	 */

	public double yield()
	{
		return _dblYield;
	}

	/**
	 * Retrieve the Work-out Factor
	 * 
	 * @return The Work-out Factor
	 */

	public double factor()
	{
		return _dblExerciseFactor;
	}

	/**
	 * Retrieve the Work-out Type
	 * 
	 * @return The Work-out Type
	 */

	public int type()
	{
		return _iWOType;
	}
}

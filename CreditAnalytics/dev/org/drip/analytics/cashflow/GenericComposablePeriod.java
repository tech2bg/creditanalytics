
package org.drip.analytics.cashflow;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * ComposablePeriod contains the cash flow periods' composable sub period details. Currently it holds the
 *  start date, the end date, the fixing date, and the reference floating index if any.
 *
 * @author Lakshmi Krishnamurthy
 */

public class GenericComposablePeriod {

	/**
	 * Node is to the Left of the Period
	 */

	public static final int NODE_LEFT_OF_SEGMENT = 1;

	/**
	 * Node is Inside the Period
	 */

	public static final int NODE_INSIDE_SEGMENT = 2;

	/**
	 * Node is to the Right of the Period
	 */

	public static final int NODE_RIGHT_OF_SEGMENT = 4;

	private double _dblEnd = java.lang.Double.NaN;
	private double _dblStart = java.lang.Double.NaN;
	private double _dblFixing = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _forwardLabel = null;

	/**
	 * The ComposablePeriod constructor
	 * 
	 * @param dblStart Period Start Date
	 * @param dblEnd Period End Date
	 * @param dblFixing Period Fixing Date
	 * @param forwardLabel The Period Forward Label
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public GenericComposablePeriod (
		final double dblStart,
		final double dblEnd,
		final double dblFixing,
		final org.drip.state.identifier.ForwardLabel forwardLabel)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStart = dblStart) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEnd = dblEnd) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblFixing = dblFixing))
			throw new java.lang.Exception ("ComposablePeriod ctr: Invalid Inputs");
	}

	/**
	 * Period Start Date
	 * 
	 * @return The Period Start Date
	 */

	public double start()
	{
		return _dblStart;
	}

	/**
	 * Period End Date
	 * 
	 * @return The Period End Date
	 */

	public double end()
	{
		return _dblEnd;
	}

	/**
	 * Period Fixing Date
	 * 
	 * @return The Period Fixing Date
	 */

	public double fixing()
	{
		return _dblFixing;
	}

	/**
	 * Retrieve the Forward Label
	 * 
	 * @return The Forward Label
	 */

	public org.drip.state.identifier.ForwardLabel forwardLabel()
	{
		return _forwardLabel;
	}

	/**
	 * Place the Node Location in relation to the segment Location
	 * 
	 * @param dblNode The Node Ordinate
	 * 
	 * @return One of NODE_LEFT_OF_SEGMENT, NODE_RIGHT_OF_SEGMENT, or NODE_INSIDE_SEGMENT
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public int nodeLocation (
		final double dblNode)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblNode))
			throw new java.lang.Exception ("ComposablePeriod::nodeLocation => Invalid Node");

		if (dblNode < _dblStart) return NODE_LEFT_OF_SEGMENT;

		if (dblNode > _dblEnd) return NODE_RIGHT_OF_SEGMENT;

		return NODE_INSIDE_SEGMENT;
	}
}

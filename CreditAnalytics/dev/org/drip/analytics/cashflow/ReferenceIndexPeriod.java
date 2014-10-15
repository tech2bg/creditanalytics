
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
 * ReferenceIndexPeriod contains the cash flow period details. Currently it holds the start date, the end
 * 	date, the fixing date, and the reference floating index if any.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ReferenceIndexPeriod {
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblFixingDate = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _forwardLabel = null;

	/**
	 * The ReferenceIndexPeriod constructor
	 * 
	 * @param dblStartDate The Reference Period Start Date
	 * @param dblEndDate The Reference Period End Date
	 * @param dblFixingDate The Reference Period Fixing Date
	 * @param forwardLabel The Period Forward Label
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ReferenceIndexPeriod (
		final double dblStartDate,
		final double dblEndDate,
		final double dblFixingDate,
		final org.drip.state.identifier.ForwardLabel forwardLabel)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) || _dblEndDate <=
				_dblStartDate || !org.drip.quant.common.NumberUtil.IsValid (_dblFixingDate = dblFixingDate)
					|| null == (_forwardLabel = forwardLabel))
			throw new java.lang.Exception ("ReferenceIndexPeriod ctr: Invalid Inputs");
	}

	/**
	 * Reference Period Start Date
	 * 
	 * @return The Reference Period Start Date
	 */

	public double startDate()
	{
		return _dblStartDate;
	}

	/**
	 * Reference Period End Date
	 * 
	 * @return The Reference Period End Date
	 */

	public double endDate()
	{
		return _dblEndDate;
	}

	/**
	 * Reference Period Fixing Date
	 * 
	 * @return The Reference Period Fixing Date
	 */

	public double fixingDate()
	{
		return _dblFixingDate;
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
}

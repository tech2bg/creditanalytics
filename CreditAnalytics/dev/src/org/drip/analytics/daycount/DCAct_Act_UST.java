
package org.drip.analytics.daycount;

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
 * This class implements the US Treasury Bond Act/Act Day Count Convention.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DCAct_Act_UST implements org.drip.analytics.daycount.DCFCalculator {

	/**
	 * Empty DCAct_Act_UST constructor
	 */

	public DCAct_Act_UST()
	{
	}

	@Override public java.lang.String baseCalculationType()
	{
		return "DCAct_Act_UST";
	}

	@Override public java.lang.String[] alternateNames()
	{
		return new java.lang.String[] {"Actual/Actual UST", "Act/Act UST", "DCAct_Act_UST"};
	}

	@Override public double yearFraction (
		final double dblStart,
		final double dblEnd,
		final boolean bApplyEOMAdj,
		final ActActDCParams actactParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == actactParams || !org.drip.quant.common.NumberUtil.IsValid (dblStart) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblEnd) || dblEnd < dblStart)
			throw new java.lang.Exception ("DCAct_Act_UST.daysAccrued: Invalid Inputs!");

		return (dblEnd - dblStart) / actactParams.days();
	}

	@Override public int daysAccrued (
		final double dblStart,
		final double dblEnd,
		final boolean bApplyEOMAdj,
		final ActActDCParams actactParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStart) || !org.drip.quant.common.NumberUtil.IsValid
			(dblEnd) || dblEnd < dblStart)
			throw new java.lang.Exception ("DCAct_Act_UST.daysAccrued: Invalid Inputs!");

		return (int) (dblEnd - dblStart);
	}
}

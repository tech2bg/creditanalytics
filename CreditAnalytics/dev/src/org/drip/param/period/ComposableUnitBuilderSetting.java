
package org.drip.param.period;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * ComposableUnitBuilderSetting contains the composable unit builder details. Currently it holds the coupon
 *  currency, the fixed coupon, and the basis.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComposableUnitBuilderSetting {
	private java.lang.String _strTenor = "";
	private int _iEdgeDateSequenceScheme = -1;
	private org.drip.analytics.daycount.DateAdjustParams _dapEdge = null;

	protected ComposableUnitBuilderSetting (
		final java.lang.String strTenor,
		final int iEdgeDateSequenceScheme,
		final org.drip.analytics.daycount.DateAdjustParams dapEdge)
		throws java.lang.Exception
	{
		if (null == (_strTenor = strTenor) || _strTenor.isEmpty())
			throw new java.lang.Exception ("ComposableUnitBuilderSetting ctr: Invalid Inputs");

		_dapEdge = dapEdge;
		_iEdgeDateSequenceScheme = iEdgeDateSequenceScheme;
	}

	/**
	 * Retrieve the Tenor
	 * 
	 * @return The Tenor
	 */

	public java.lang.String tenor()
	{
		return _strTenor;
	}

	/**
	 * Retrieve the Edge Date Generation Scheme
	 * 
	 * @return The Edge Date Generation Scheme
	 */

	public int edgeDateSequenceScheme()
	{
		return _iEdgeDateSequenceScheme;
	}

	/**
	 * Retrieve the Edge Date Adjust Parameters
	 * 
	 * @return The Edge Date Adjust Parameters
	 */

	public org.drip.analytics.daycount.DateAdjustParams dapEdge()
	{
		return _dapEdge;
	}
}

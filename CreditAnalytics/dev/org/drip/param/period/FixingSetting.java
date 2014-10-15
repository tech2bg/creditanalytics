
package org.drip.param.period;

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
 * FixingSetting implements the custom setting parameters for the Latent State Fixing Settings.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixingSetting {

	/**
	 * Fixing Based off of the Start of the Composite Period
	 */

	public static final int FIXING_COMPOSITE_PERIOD_START = 1;

	/**
	 * Fixing Based off of the End of the Composite Period
	 */

	public static final int FIXING_COMPOSITE_PERIOD_END = 2;

	/**
	 * Fixing Based off of the Start of a Pre-determined Static Date
	 */

	public static final int FIXING_PRESET_STATIC = 4;

	private int _iType = -1;
	private double _dblStaticDate = java.lang.Double.NaN;
	private org.drip.analytics.daycount.DateAdjustParams _dap = null;

	/**
	 * Validate the Type of FX Fixing
	 * 
	 * @param iType The FX Fixing Type
	 * 
	 * @return TRUE => FX Fixing is One of the Valid Types
	 */

	public static final boolean ValidateType (
		final int iType)
	{
		return FIXING_COMPOSITE_PERIOD_START == iType || FIXING_COMPOSITE_PERIOD_END == iType &&
			FIXING_PRESET_STATIC == iType;
	}

	/**
	 * FixingSetting Constructor
	 * 
	 * @param iType The Fixing Type
	 * @param dap The Fixing DAP
	 * @param dblStaticDate Static Fixing Date
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public FixingSetting (
		final int iType,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final double dblStaticDate)
		throws java.lang.Exception
	{
		if (!ValidateType (_iType = iType) || (!org.drip.quant.common.NumberUtil.IsValid (_dblStaticDate =
			dblStaticDate) && FIXING_PRESET_STATIC == _iType))
			throw new java.lang.Exception ("FixingSetting ctr: Invalid Inputs");

		_dap = dap;
	}

	/**
	 * Retrieve the Fixing Type
	 * 
	 * @return The Fixing Type
	 */

	public int type()
	{
		return _iType;
	}

	/**
	 * Retrieve the Fixing DAP
	 * 
	 * @return The Fixing DAP
	 */

	public org.drip.analytics.daycount.DateAdjustParams dap()
	{
		return _dap;
	}

	/**
	 * Retrieve the Static Fixing Date
	 * 
	 * @return The Static Fixing Date
	 */

	public double staticDate()
	{
		return _dblStaticDate;
	}
}

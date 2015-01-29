
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
 * ValuationCustomizationParams holds the parameters needed to interpret the input quotes. It contains the
 * 	quote day count, the quote frequency, the quote EOM Adjustment, the quote Act/Act parameters, the quote
 * 	Calendar, the Core Collateralization Parameters, and the Switchable Alternate Collateralization
 * 	Parameters. It also indicates if the native quote is spread based.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ValuationCustomizationParams {
	private int _iYieldFrequency = 0;
	private boolean _bSpreadQuoted = false;
	private java.lang.String _strYieldDC = "";
	private boolean _bYieldApplyEOMAdj = false;
	private java.lang.String _strYieldCalendar = "";
	private org.drip.analytics.daycount.ActActDCParams _aapYield = null;
	private org.drip.param.valuation.CollateralizationParams _collatParamsCore = null;
	private java.util.Set<org.drip.param.valuation.CollateralizationParams> _setSwitchableCollateralBasket =
		null;

	/**
	 * Construct ValuationCustomizationParams from the Day Count and the Frequency parameters
	 * 
	 * @param strDC Quoting Day Count
	 * @param iFrequency Quoting Frequency
	 * @param bApplyEOMAdj TRUE => Apply the EOM Adjustment
	 * @param aap => Quoting Act/Act Parameters
	 * @param strCalendar => Quoting Calendar
	 * @param bSpreadQuoted => TRUE => Market Quotes are Spread Quoted
	 * @param collatParamsCore => The Core Collateral Parameters using which the valuation is done
	 * @param setSwitchableCollateralBasket => Switchable Collateral Basket
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public ValuationCustomizationParams (
		final java.lang.String strDC,
		final int iFrequency,
		final boolean bApplyEOMAdj,
		final org.drip.analytics.daycount.ActActDCParams aap,
		final java.lang.String strCalendar,
		final boolean bSpreadQuoted,
		final org.drip.param.valuation.CollateralizationParams collatParamsCore,
		final java.util.Set<org.drip.param.valuation.CollateralizationParams> setSwitchableCollateralBasket)
		throws java.lang.Exception
	{
		if (null == strDC || strDC.isEmpty() || 0 == iFrequency)
			throw new java.lang.Exception ("ValuationCustomizationParams ctr: Invalid quoting params!");

		_aapYield = aap;
		_strYieldDC = strDC;
		_iYieldFrequency = iFrequency;
		_bSpreadQuoted = bSpreadQuoted;
		_strYieldCalendar = strCalendar;
		_bYieldApplyEOMAdj = bApplyEOMAdj;
		_collatParamsCore = collatParamsCore;
		_setSwitchableCollateralBasket = setSwitchableCollateralBasket;
	}

	/**
	 * Retrieve the Yield Act Act Day Count Parameters
	 * 
	 * @return The Yield Act Act Day Count Parameters
	 */

	public org.drip.analytics.daycount.ActActDCParams yieldAAP()
	{
		return _aapYield;
	}

	/**
	 * Retrieve the Yield Day Count
	 * 
	 * @return The Yield Day Count
	 */

	public java.lang.String yieldDayCount()
	{
		return _strYieldDC;
	}

	/**
	 * Retrieve the Yield Frequency
	 * 
	 * @return The Yield Frequency
	 */

	public int yieldFreq()
	{
		return _iYieldFrequency;
	}

	/**
	 * Indicate if spread Quoted
	 * 
	 * @return TRUE => Spread Quoted
	 */

	public boolean spreadQuoted()
	{
		return _bSpreadQuoted;
	}

	/**
	 * Retrieve the Yield Calendar
	 * 
	 * @return The Yield Calendar
	 */

	public java.lang.String yieldCalendar()
	{
		return _strYieldCalendar;
	}

	/**
	 * Indicate if EOM Adjustment is to be made for the Yield Calculation
	 * 
	 * @return TRUE => EOM Adjustment is to be made for the Yield Calculation
	 */

	public boolean applyYieldEOMAdj()
	{
		return _bYieldApplyEOMAdj;
	}

	/**
	 * Retrieve the Core Collateralization Parameters
	 * 
	 * @return The Core Collateralization Parameters
	 */

	public org.drip.param.valuation.CollateralizationParams coreCollateralizationParams()
	{
		return _collatParamsCore;
	}

	/**
	 * Retrieve the Switchable Collateralization Basket
	 * 
	 * @return The Switchable Collateralization Basket
	 */

	public java.util.Set<org.drip.param.valuation.CollateralizationParams> switchableCollateralBasket()
	{
		return _setSwitchableCollateralBasket;
	}
}

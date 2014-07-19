
package org.drip.product.params;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * FXMTMSetting captures the FX MTM related parameters for the given product. Crrently is contains the
 * 	currency pair and the FX MTM flag.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FXMTMSetting {
	private boolean _bMTMMode = false;
	private org.drip.product.params.CurrencyPair _cp = null;

	/**
	 * FXMTMSetting constructor
	 * 
	 * @param cp The Currency Pair
	 * @param bMTMMode The MTM Mode
	 */

	public FXMTMSetting (
		final org.drip.product.params.CurrencyPair cp,
		final boolean bMTMMode)
	{
		_cp = cp;
		_bMTMMode = bMTMMode;
	}

	/**
	 * Retrieve the Currency Pair
	 * 
	 * @return The Currency Pair
	 */

	public org.drip.product.params.CurrencyPair currencyPair()
	{
		return _cp;
	}

	/**
	 * Retrieve the MTM Mode
	 * 
	 * @return The MTM Mode
	 */

	public boolean mtmMode()
	{
		return _bMTMMode;
	}
}

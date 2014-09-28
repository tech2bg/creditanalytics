
package org.drip.product.params;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * FloaterSetting contains the component's floating rate parameters. It holds the rate index, floater day
 * 	count, and one of either the coupon spread or the full current coupon. It also provides for serialization
 *  into and de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FloaterSetting implements org.drip.product.params.Validatable {

	/**
	 * Floating Rate Index
	 */

	public org.drip.state.identifier.ForwardLabel _fri = null;

	/**
	 * Floating Day Count
	 */

	public java.lang.String _strFloatDayCount = "";

	/**
	 * Floating Spread
	 */

	public double _dblFloatSpread = java.lang.Double.NaN;

	/**
	 * Current Coupon
	 */

	public double _dblCurrentCoupon = java.lang.Double.NaN;

	/**
	 * Construct the FloaterSetting from rate index, floating day count, float spread, and current coupon
	 * 
	 * @param strRateIndex Fully Qualified Floating Rate Index
	 * @param strFloatDayCount Floating Day Count
	 * @param dblFloatSpread Floating Spread
	 * @param dblCurrentCoupon Current Coupon
	 */

	public FloaterSetting (
		final java.lang.String strRateIndex,
		final java.lang.String strFloatDayCount,
		final double dblFloatSpread,
		final double dblCurrentCoupon)
	{
		_dblFloatSpread = dblFloatSpread;
		_strFloatDayCount = strFloatDayCount;
		_dblCurrentCoupon = dblCurrentCoupon;

		_fri = org.drip.state.identifier.ForwardLabel.Standard (strRateIndex);
	}

	@Override public boolean validate()
	{
		return (org.drip.quant.common.NumberUtil.IsValid (_dblFloatSpread) ||
			org.drip.quant.common.NumberUtil.IsValid (_dblCurrentCoupon)) && null != _fri;
	}
}

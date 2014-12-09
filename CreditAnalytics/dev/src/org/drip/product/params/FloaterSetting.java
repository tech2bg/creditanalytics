
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
	private java.lang.String _strDayCount = "";
	private double _dblSpread = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _fri = null;
	private double _dblCurrentFullCoupon = java.lang.Double.NaN;

	/**
	 * Construct the FloaterSetting from rate index, floating day count, float spread, and current Full
	 * 	coupon
	 * 
	 * @param strRateIndex Fully Qualified Floating Rate Index
	 * @param strDayCount Floating Day Count
	 * @param dblSpread Floating Spread
	 * @param dblCurrentFullCoupon Current Full Coupon
	 */

	public FloaterSetting (
		final java.lang.String strRateIndex,
		final java.lang.String strDayCount,
		final double dblSpread,
		final double dblCurrentFullCoupon)
	{
		_dblSpread = dblSpread;
		_strDayCount = strDayCount;
		_dblCurrentFullCoupon = dblCurrentFullCoupon;

		_fri = org.drip.state.identifier.ForwardLabel.Standard (strRateIndex);
	}

	@Override public boolean validate()
	{
		return (org.drip.quant.common.NumberUtil.IsValid (_dblSpread) ||
			org.drip.quant.common.NumberUtil.IsValid (_dblCurrentFullCoupon)) && null != _fri;
	}

	/**
	 * Retrieve the Floating Rate Index
	 * 
	 * @return Tyhe Floating Rate Index
	 */

	public org.drip.state.identifier.ForwardLabel fri()
	{
		return _fri;
	}

	/**
	 * Retrieve the Floating Day Count
	 * 
	 * @return The Floating Day Count
	 */

	public java.lang.String dayCount()
	{
		return _strDayCount;
	}

	/**
	 * Retrieve the Floating Spread
	 * 
	 * @return The Floating Spread
	 */

	public double spread()
	{
		return _dblSpread;
	}

	/**
	 * Retrieve the Full Current Coupon
	 * 
	 * @return The Full Current Coupon
	 */

	public double currentFullCoupon()
	{
		return _dblCurrentFullCoupon;
	}
}

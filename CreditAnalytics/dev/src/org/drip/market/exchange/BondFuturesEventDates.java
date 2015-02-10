
package org.drip.market.exchange;

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
 * BondFuturesEventDates contains the actually realized Event Dates related to a Bond Futures Contract.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondFuturesEventDates {
	private org.drip.analytics.date.JulianDate _dtExpiry = null;
	private org.drip.analytics.date.JulianDate _dtLastTrading = null;
	private org.drip.analytics.date.JulianDate _dtFirstDelivery = null;
	private org.drip.analytics.date.JulianDate _dtFinalDelivery = null;
	private org.drip.analytics.date.JulianDate _dtDeliveryNotice = null;

	/**
	 * BondFuturesEventDates Constructor
	 * 
	 * @param dtExpiry The Expiry Date
	 * @param dtFirstDelivery The First Delivery Date
	 * @param dtFinalDelivery The Final Delivery Date
	 * @param dtDeliveryNotice The Delivery Notice Date
	 * @param dtLastTrading The Last Trading Date
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public BondFuturesEventDates (
		final org.drip.analytics.date.JulianDate dtExpiry,
		final org.drip.analytics.date.JulianDate dtFirstDelivery,
		final org.drip.analytics.date.JulianDate dtFinalDelivery,
		final org.drip.analytics.date.JulianDate dtDeliveryNotice,
		final org.drip.analytics.date.JulianDate dtLastTrading)
		throws java.lang.Exception
	{
		if (null == (_dtExpiry = dtExpiry) || null == (_dtFirstDelivery = dtFirstDelivery) || null ==
			(_dtFinalDelivery = dtFinalDelivery) || null == (_dtDeliveryNotice = dtDeliveryNotice) || null ==
				(_dtLastTrading = dtLastTrading))
			throw new java.lang.Exception ("BondFuturesEventDates ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Expiry Date
	 * 
	 * @return The Expiry Date
	 */

	public org.drip.analytics.date.JulianDate expiry()
	{
		return _dtExpiry;
	}

	/**
	 * Retrieve the First Delivery Date
	 * 
	 * @return The First Delivery Date
	 */

	public org.drip.analytics.date.JulianDate firstDelivery()
	{
		return _dtFirstDelivery;
	}

	/**
	 * Retrieve the Final Delivery Date
	 * 
	 * @return The Final Delivery Date
	 */

	public org.drip.analytics.date.JulianDate finalDelivery()
	{
		return _dtFinalDelivery;
	}

	/**
	 * Retrieve the Delivery Notice Date
	 * 
	 * @return The Delivery Notice Date
	 */

	public org.drip.analytics.date.JulianDate deliveryNotice()
	{
		return _dtDeliveryNotice;
	}

	/**
	 * Retrieve the Last Trading Date
	 * 
	 * @return The Last Trading Date
	 */

	public org.drip.analytics.date.JulianDate lastTrading()
	{
		return _dtLastTrading;
	}

	@Override public java.lang.String toString()
	{
		return _dtExpiry + " | " + _dtFirstDelivery + "  | " + _dtFirstDelivery + " |  " + _dtDeliveryNotice
			+ "  | " + _dtLastTrading;
	}
}

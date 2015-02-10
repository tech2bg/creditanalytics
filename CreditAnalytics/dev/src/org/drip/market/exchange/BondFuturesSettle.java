
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
 * BondFuturesSettle contains the Settlement Details for the Futures Basket of the Exchange-Traded Bond
 *  Futures Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondFuturesSettle {

	/**
	 * Cash Settled Futures
	 */

	public static final int SETTLE_TYPE_CASH = 1;

	/**
	 * Physically Settled Futures
	 */

	public static final int SETTLE_TYPE_PHYSICAL_DELIVERY = 2;

	/**
	 * Settle Quote Type - AUD Bank Bill Type - Uses a Flat Reference Index
	 */

	public static final int QUOTE_REFERENCE_INDEX_FLAT = 1;

	/**
	 * Settle Quote Type - Uses a Reference Index Based off of Conversion Factor
	 */

	public static final int QUOTE_REFERENCE_INDEX_CONVERSION_FACTOR = 2;

	/**
	 * Settle Quote Type - Uses a Reference Index Based off of Conversion Factor Computed AUD Bond Futures
	 *  Style
	 */

	public static final int QUOTE_REFERENCE_INDEX_AUD_BOND_FUTURES_STYLE = 3;

	private int _iSettleType = -1;
	private int _iSettleQuoteStyle = -1;
	private int _iExpiryLastTradingLag = -1;
	private boolean _bWildCardOption = false;
	private int _iExpiryFirstDeliveryLag = -1;
	private int _iExpiryFinalDeliveryLag = -1;
	private int _iExpiryDeliveryNoticeLag = -1;
	private double _dblReferenceYieldCurrent = java.lang.Double.NaN;
	private double _dblReferenceYieldOriginal = java.lang.Double.NaN;

	/**
	 * BondFuturesSettle Constructor
	 * 
	 * @param iExpiryFirstDeliveryLag Lag Between the Expiry and the First Delivery Dates
	 * @param iExpiryFinalDeliveryLag Lag Between the Expiry and the Final Delivery Dates
	 * @param iExpiryDeliveryNoticeLag Lag between the Expiry and the Delivery Notice
	 * @param iExpiryLastTradingLag Lag between the Expiry and the Last Trading Day
	 * @param iSettleType Settlement Type
	 * @param iSettleQuoteStyle Settlement Quote Style
	 * @param bWildCardOption TRUE => Turn ON the Wild Card Option
	 * @param dblReferenceYieldCurrent The Current Reference Yield
	 * @param dblReferenceYieldOriginal The Original Reference Yield
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public BondFuturesSettle (
		final int iExpiryFirstDeliveryLag,
		final int iExpiryFinalDeliveryLag,
		final int iExpiryDeliveryNoticeLag,
		final int iExpiryLastTradingLag,
		final int iSettleType,
		final int iSettleQuoteStyle,
		final boolean bWildCardOption,
		final double dblReferenceYieldCurrent,
		final double dblReferenceYieldOriginal)
		throws java.lang.Exception
	{
		if ((_iExpiryFinalDeliveryLag = iExpiryFinalDeliveryLag) < (_iExpiryFirstDeliveryLag =
			iExpiryFirstDeliveryLag) || _iExpiryFirstDeliveryLag < 0)
			throw new java.lang.Exception ("BondFuturesSettle ctr: Invalid Inputs");

		_iSettleType = iSettleType;
		_bWildCardOption = bWildCardOption;
		_iSettleQuoteStyle = iSettleQuoteStyle;
		_iExpiryLastTradingLag = iExpiryLastTradingLag;
		_iExpiryFirstDeliveryLag = iExpiryFirstDeliveryLag;
		_iExpiryFinalDeliveryLag = iExpiryFinalDeliveryLag;
		_iExpiryDeliveryNoticeLag = iExpiryDeliveryNoticeLag;
		_dblReferenceYieldCurrent = dblReferenceYieldCurrent;
		_dblReferenceYieldOriginal = dblReferenceYieldOriginal;
	}

	/**
	 * Retrieve the Lag Between the Expiry and the First Delivery Dates
	 * 
	 * @return The Lag Between the Expiry and the First Delivery Dates
	 */

	public int expiryFirstDeliveryLag()
	{
		return _iExpiryFirstDeliveryLag;
	}

	/**
	 * Retrieve the Lag Between the Expiry and the Final Delivery Dates
	 * 
	 * @return The Lag Between the Expiry and the Final Delivery Dates
	 */

	public int expiryFinalDeliveryLag()
	{
		return _iExpiryFinalDeliveryLag;
	}

	/**
	 * Retrieve the Lag Between the Expiry and the Delivery Notice Dates
	 * 
	 * @return The Lag Between the Expiry and the Delivery Notice Dates
	 */

	public int expiryDeliveryNoticeLag()
	{
		return _iExpiryDeliveryNoticeLag;
	}

	/**
	 * Retrieve the Lag Between the Expiry and the Last Trading Dates
	 * 
	 * @return The Lag Between the Expiry and the Last Trading Dates
	 */

	public int expiryLastTradingLag()
	{
		return _iExpiryLastTradingLag;
	}

	/**
	 * Retrieve the Settle Type
	 * 
	 * @return The Settle Type
	 */

	public int settleType()
	{
		return _iSettleType;
	}

	/**
	 * Retrieve the Settle Quote Style
	 * 
	 * @return The Settle Quote Style
	 */

	public int settleQuoteStyle()
	{
		return _iSettleQuoteStyle;
	}

	/**
	 * Retrieve the Bond Futures Wild Card Option Setting
	 * 
	 * @return Bond Futures Wild Card Option Setting
	 */

	public boolean wildCardOption()
	{
		return _bWildCardOption;
	}

	/**
	 * Retrieve the Current Reference Yield
	 * 
	 * @return The Current Reference Yield
	 */

	public double currentReferenceYield()
	{
		return _dblReferenceYieldCurrent;
	}

	/**
	 * Retrieve the Original Reference Yield
	 * 
	 * @return The Original Reference Yield
	 */

	public double originalReferenceYieldCeiling()
	{
		return _dblReferenceYieldOriginal;
	}

	@Override public java.lang.String toString()
	{
		return "[Futures Settle => Expiry To First Delivery Lag: " + _iExpiryFirstDeliveryLag +
			" | Expiry To Final Delivery Lag: " + _iExpiryFinalDeliveryLag +
				" | Expiry To Delivery Notice Lag: " + _iExpiryDeliveryNoticeLag +
					" | Expiry To Last Trading Lag: " + _iExpiryLastTradingLag + " | Settlement Type:  " +
						_iSettleType + " | Settlement Quote Style: " + _iSettleQuoteStyle + " | Wild Card: "
							+ _bWildCardOption + " | Reference Yield Floor: " + _dblReferenceYieldCurrent +
								" | Reference Yield Ceiling: " + _dblReferenceYieldOriginal + "]";
	}
}

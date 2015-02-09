
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
 * BondFuturesConvention contains the Details for the Futures Basket of the Exchange-Traded Bond Futures
 *  Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondFuturesConvention {
	private java.lang.String _strName = "";
	private java.lang.String _strCurrency = "";
	private java.lang.String[] _astrCode = null;
	private java.lang.String[] _astrExchange = null;
	private java.lang.String _strMaturityTenor = "";
	private java.lang.String _strUnderlierType = "";
	private java.lang.String _strUnderlierSubtype = "";
	private double _dblBasketNotional = java.lang.Double.NaN;
	private org.drip.market.exchange.BondFuturesSettle _bfs = null;
	private org.drip.analytics.eventday.DateInMonth _dimExpiry = null;
	private double _dblComponentNotionalMinimum = java.lang.Double.NaN;
	private org.drip.market.exchange.BondFuturesEligibility _bfe = null;

	/**
	 * BondFuturesConvention Constructor
	 * 
	 * @param strName The Futures Name
	 * @param astrCode The Array of the Futures Codes
	 * @param strCurrency The Futures Currency
	 * @param strMaturityTenor The Maturity Tenor
	 * @param dblBasketNotional Basket Notional
	 * @param dblComponentNotionalMinimum The Minimum Component Notional
	 * @param astrExchange Exchange Array
	 * @param strUnderlierType Underlier Type
	 * @param strUnderlierSubtype Underlier Sub-Type
	 * @param bfe Eligibility Settings
	 * @param bfs Settlement Settings
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public BondFuturesConvention (
		final java.lang.String strName,
		final java.lang.String[] astrCode,
		final java.lang.String strCurrency,
		final java.lang.String strMaturityTenor,
		final double dblBasketNotional,
		final double dblComponentNotionalMinimum,
		final java.lang.String[] astrExchange,
		final java.lang.String strUnderlierType,
		final java.lang.String strUnderlierSubtype,
		final org.drip.analytics.eventday.DateInMonth dimExpiry,
		final org.drip.market.exchange.BondFuturesEligibility bfe,
		final org.drip.market.exchange.BondFuturesSettle bfs)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_astrCode = astrCode) || 0 ==
			_astrCode.length || null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() || null ==
				(_strMaturityTenor = strMaturityTenor) || _strMaturityTenor.isEmpty() ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblBasketNotional = dblBasketNotional) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblComponentNotionalMinimum =
							dblComponentNotionalMinimum) || null == (_astrExchange = astrExchange) || 0 ==
								_astrExchange.length || null == (_strUnderlierType = strUnderlierType) ||
									_strUnderlierType.isEmpty() || null == (_strUnderlierSubtype =
										strUnderlierSubtype) || _strUnderlierSubtype.isEmpty() || null ==
											(_dimExpiry = dimExpiry) || null == (_bfe = bfe) || null == (_bfs
												= bfs))
			throw new java.lang.Exception ("BondFuturesConvention ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Bond Futures Name
	 * 
	 * @return The Bond Futures Name
	 */

	public java.lang.String name()
	{
		return _strName;
	}

	/**
	 * Retrieve the Bond Futures Code Array
	 * 
	 * @return The Bond Futures Code Array
	 */

	public java.lang.String[] codes()
	{
		return _astrCode;
	}

	/**
	 * Retrieve the Bond Futures Currency
	 * 
	 * @return The Bond Futures Currency
	 */

	public java.lang.String currency()
	{
		return _strCurrency;
	}

	/**
	 * Retrieve the Bond Futures Maturity Tenor
	 * 
	 * @return The Bond Futures Maturity Tenor
	 */

	public java.lang.String maturityTenor()
	{
		return _strMaturityTenor;
	}

	/**
	 * Retrieve the Bond Futures Basket Notional
	 * 
	 * @return The Bond Futures Basket Notional
	 */

	public double basketNotional()
	{
		return _dblBasketNotional;
	}

	/**
	 * Retrieve the Minimum Bond Futures Component Notional
	 * 
	 * @return The Minimum Bond Futures Component Notional
	 */

	public double minimumComponentNotional()
	{
		return _dblComponentNotionalMinimum;
	}

	/**
	 * Retrieve the Bond Futures Exchanges Array
	 * 
	 * @return The Bond Futures Exchanges Array
	 */

	public java.lang.String[] exchanges()
	{
		return _astrExchange;
	}

	/**
	 * Retrieve the Bond Futures Underlier Type
	 * 
	 * @return The Bond Futures Underlier Type
	 */

	public java.lang.String underlierType()
	{
		return _strUnderlierType;
	}

	/**
	 * Retrieve the Bond Futures Underlier Sub-type
	 * 
	 * @return The Bond Futures Underlier Sub-type
	 */

	public java.lang.String underlierSubtype()
	{
		return _strUnderlierSubtype;
	}

	/**
	 * Retrieve the Date In Month Expiry Settings
	 * 
	 * @return The Date In Month Expiry Settings
	 */

	public org.drip.analytics.eventday.DateInMonth dimExpiry()
	{
		return _dimExpiry;
	}

	/**
	 * Retrieve the Bond Futures Eligibility Settings
	 * 
	 * @return The Bond Futures Eligibility Settings
	 */

	public org.drip.market.exchange.BondFuturesEligibility eligibility()
	{
		return _bfe;
	}

	/**
	 * Retrieve the Bond Futures Settle Settings
	 * 
	 * @return The Bond Futures Settle Settings
	 */

	public org.drip.market.exchange.BondFuturesSettle settle()
	{
		return _bfs;
	}

	@Override public java.lang.String toString()
	{
		java.lang.String strDump = "Name: " + _strName + " | Currency: " + _strCurrency +
			" | Underlier Type: " + _strUnderlierType + " | Underlier Sub-type: " + _strUnderlierSubtype +
				" | Maturity Tenor: " + _strMaturityTenor + " | Basket Notional: " + _dblBasketNotional +
					" | Component Notional Minimum: " + _dblComponentNotionalMinimum;

		for (int i = 0; i < _astrCode.length; ++i) {
			if (0 == i)
				strDump += " | CODES => {";
			else
				strDump += ", ";

			strDump += _astrCode[i];

			if (_astrExchange.length - 1 == i) strDump += "}";
		}

		for (int i = 0; i < _astrExchange.length; ++i) {
			if (0 == i)
				strDump += " | EXCHANGES => (";
			else
				strDump += ", ";

			strDump += _astrExchange[i];

			if (_astrExchange.length - 1 == i) strDump += ") ";
		}

		return strDump + "\n\t\t" + _dimExpiry + "\n\t\t" + _bfe + "\n\t\t" + _bfs;
	}
}

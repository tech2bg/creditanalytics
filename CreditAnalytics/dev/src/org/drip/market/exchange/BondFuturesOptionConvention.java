
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
 * BondFuturesOptionConvention contains the Details for the Exchange-Traded Options of the Exchange-Traded
 *  Bond Futures Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondFuturesOptionConvention {
	private boolean _bPremiumType = false;
	private java.lang.String[] _astrCode = null;
	private double _dblNotional = java.lang.Double.NaN;
	private java.lang.String _strBondFuturesIndex = "";
	private org.drip.product.params.LastTradingDateSetting[] _aLTDS = null;

	/**
	 * BondFuturesOptionConvention Constructor
	 * 
	 * @param astrCode Array of Option Codes
	 * @param strBondFuturesIndex Underlying Futures Index
	 * @param dblNotional Exchange Notional
	 * @param bPremiumType TRUE => Premium Up-front Type; FALSE => Margin Type
	 * @param aLTDS Array of Last Trading Date Settings
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public BondFuturesOptionConvention (
		final java.lang.String[] astrCode,
		final java.lang.String strBondFuturesIndex,
		final double dblNotional,
		final boolean bPremiumType,
		final org.drip.product.params.LastTradingDateSetting[] aLTDS)
		throws java.lang.Exception
	{
		if (null == (_astrCode = astrCode) || null == (_strBondFuturesIndex = strBondFuturesIndex) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) || null == (_aLTDS =
				aLTDS))
			throw new java.lang.Exception ("BondFuturesOptionConvention ctr: Invalid Inputs!");

		_bPremiumType = bPremiumType;
		int iNumLTDS = _aLTDS.length;
		int iNumCode = _astrCode.length;

		if (0 == iNumLTDS || 0 == iNumCode)
			throw new java.lang.Exception ("BondFuturesOptionConvention ctr: Invalid Inputs!");

		for (java.lang.String strCode : _astrCode) {
			if (null == strCode || strCode.isEmpty())
				throw new java.lang.Exception ("BondFuturesOptionConvention ctr: Invalid Inputs!");
		}

		for (org.drip.product.params.LastTradingDateSetting ltds : _aLTDS) {
			if (null == ltds)
				throw new java.lang.Exception ("BondFuturesOptionConvention ctr: Invalid Inputs!");
		}
	}

	/**
	 * Retrieve the Array of the Exchange Codes
	 * 
	 * @return The Array of the Exchange Codes
	 */

	public java.lang.String[] codes()
	{
		return _astrCode;
	}

	/**
	 * Retrieve the Array of Last Trading Date Settings
	 * 
	 * @return The Array of Last Trading Date Settings
	 */

	public org.drip.product.params.LastTradingDateSetting[] ltds()
	{
		return _aLTDS;
	}

	/**
	 * Retrieve the Bond Futures Index
	 * 
	 * @return The Bond Futures Index
	 */

	public java.lang.String bondFuturesIndex()
	{
		return _strBondFuturesIndex;
	}

	/**
	 * Retrieve the Option Exchange Notional
	 * 
	 * @return The Option Exchange Notional
	 */

	public double notional()
	{
		return _dblNotional;
	}

	/**
	 * Retrieve the Trading Type PREMIUM/MARGIN
	 * 
	 * @return TRUE => Trading Type is PREMIUM
	 */

	public boolean premiumType()
	{
		return _bPremiumType;
	}

	@Override public java.lang.String toString()
	{
		java.lang.String strDump = "BondFuturesIndex: " + _strBondFuturesIndex + " | Premium Type: " +
			(_bPremiumType ? "PREMIUM" : "MARGIN ");

		for (int i = 0; i < _astrCode.length; ++i) {
			if (0 == i)
				strDump += " | CODES => {";
			else
				strDump += ", ";

			strDump += _astrCode[i];

			if (_astrCode.length - 1 == i) strDump += "}";
		}

		for (int i = 0; i < _aLTDS.length; ++i)
			strDump += "\n\t" + _aLTDS[i];

		return strDump;
	}
}

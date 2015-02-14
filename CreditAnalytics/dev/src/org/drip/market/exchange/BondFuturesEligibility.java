
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
 * BondFuturesEligibility contains the Eligibility Criterion for a Bond in the Futures Basket of the
 * 	Exchange-Traded Bond Futures Contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondFuturesEligibility {
	private java.lang.String[] _astrIssuer = null;
	private java.lang.String _strMaturityFloor = "";
	private java.lang.String _strMaturityCeiling = "";
	private double _dblMinimumOutstandingNotional = java.lang.Double.NaN;

	/**
	 * BondFuturesEligibility Constructor
	 * 
	 * @param strMaturityFloor Maturity Floor
	 * @param strMaturityCeiling Maturity Floor
	 * @param astrIssuer Array of Issuers
	 * @param dblMinimumOutstandingNotional Minimum Outstanding Notional
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public BondFuturesEligibility (
		final java.lang.String strMaturityFloor,
		final java.lang.String strMaturityCeiling,
		final java.lang.String[] astrIssuer,
		final double dblMinimumOutstandingNotional)
		throws java.lang.Exception
	{
		if (null == (_strMaturityFloor = strMaturityFloor) || _strMaturityFloor.isEmpty() || null ==
			(_strMaturityCeiling = strMaturityCeiling) || _strMaturityCeiling.isEmpty() ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblMinimumOutstandingNotional =
					dblMinimumOutstandingNotional))
			throw new java.lang.Exception ("BondFuturesEligibility ctr: Invalid Inputs");

		if (null != (_astrIssuer = astrIssuer)) {
			int iNumIssuer = _astrIssuer.length;

			for (int i = 0; i < iNumIssuer; ++i) {
				if (null == _astrIssuer[i] || _astrIssuer[i].isEmpty())
					throw new java.lang.Exception ("BondFuturesEligibility ctr: Invalid Issuer");
			}
		}
	}

	/**
	 * Retrieve the Eligible Maturity Floor
	 * 
	 * @return Array of Eligible Maturity Floor
	 */

	public java.lang.String maturityFloor()
	{
		return _strMaturityFloor;
	}

	/**
	 * Retrieve the Eligible Maturity Ceiling
	 * 
	 * @return Array of Eligible Maturity Ceiling
	 */

	public java.lang.String maturityCeiling()
	{
		return _strMaturityCeiling;
	}

	/**
	 * Retrieve the Array of Eligible Issuers
	 * 
	 * @return Array of Eligible Issuers
	 */

	public java.lang.String[] issuer()
	{
		return _astrIssuer;
	}

	/**
	 * Retrieve the Minimum Outstanding Notional
	 * 
	 * @return The Minimum Outstanding Notional
	 */

	public double minimumOutstandingNotional()
	{
		return _dblMinimumOutstandingNotional;
	}

	/**
	 * Indicate whether the given bond is eligible to be delivered
	 * 
	 * @param dtValue The Value Date
	 * @param bond The Bond whose Eligibility is to be evaluated
	 * @param dblOutstandingNotional The Outstanding Notional
	 * @param strIssuer The Issuer
	 * 
	 * @return TRUE => The given bond is eligible to be delivered
	 */

	public boolean isEligible (
		final org.drip.analytics.date.JulianDate dtValue,
		final org.drip.product.definition.Bond bond,
		final double dblOutstandingNotional,
		final java.lang.String strIssuer)
	{
		if (null == bond || null == dtValue) return false;

		org.drip.analytics.date.JulianDate dtFloorMaturity = dtValue.addTenor (_strMaturityFloor);

		org.drip.analytics.date.JulianDate dtCeilingMaturity = dtValue.addTenor (_strMaturityCeiling);

		if (null == dtFloorMaturity || null == dtFloorMaturity) return false;

		double dblValueDate = dtValue.julian();

		if (dblValueDate < dtFloorMaturity.julian() || dblValueDate > dtCeilingMaturity.julian())
			return false;

		if (0. != _dblMinimumOutstandingNotional && org.drip.quant.common.NumberUtil.IsValid
			(dblOutstandingNotional) && dblOutstandingNotional < _dblMinimumOutstandingNotional)
			return false;

		if (null == strIssuer || strIssuer.isEmpty() || null == _astrIssuer) return true;

		int iNumIssuer = _astrIssuer.length;

		if (0 == iNumIssuer) return true;

		for (int i = 0; i < iNumIssuer; ++i) {
			if (_astrIssuer[i].equalsIgnoreCase (strIssuer)) return true;
		}

		return false;
	}

	@Override public java.lang.String toString()
	{
		java.lang.String strDump = "[Futures Eligibility => Maturity Band: " + _strMaturityFloor + " -> " +
			_strMaturityCeiling + "] [Issuers: ";

		if (null == _astrIssuer) return strDump + "]";

		for (int i = 0; i < _astrIssuer.length; ++i) {
			if (0 != i) strDump += " | ";

			strDump += _astrIssuer[i];
		}

		return strDump + "] [Minimum Outstanding Notional: " + _dblMinimumOutstandingNotional + "]";
	}
}

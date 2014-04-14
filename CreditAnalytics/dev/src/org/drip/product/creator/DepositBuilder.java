
package org.drip.product.creator;

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
 * DepositBuilder contains the suite of helper functions for creating the Deposit product from the
 * 	parameters/codes/byte array streams.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DepositBuilder {

	/**
	 * Create a Deposit product from effective date, tenor, IR curve name, and code.
	 * 
	 * @param dtEffective JulianDate specifying the effective date
	 * @param strTenor String tenor
	 * @param strIR IR curve name
	 * @param strCode Product Code
	 * 
	 * @return Deposit Object
	 */

	public static final org.drip.product.definition.RatesComponent CreateDeposit (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strTenor,
		final java.lang.String strIR,
		final java.lang.String strCode)
	{
		if (null == dtEffective || null == strTenor || strTenor.isEmpty() || null == strIR ||
			strIR.isEmpty()) {
			System.out.println ("Invalid DepositBuilder.CreateDeposit params!");

			return null;
		}

		try {
			org.drip.product.definition.RatesComponent deposit = new org.drip.product.rates.DepositComponent
				(dtEffective, dtEffective.addTenor (strTenor), null, strIR, "Act/360", strIR);

			deposit.setPrimaryCode (strCode + "." + strTenor + "." + strIR);

			return deposit;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Deposit product from effective and maturity dates, and the IR curve
	 * 
	 * @param dtEffective Effective date
	 * @param dtMaturity Maturity
	 * @param fri The Floating Rate Index
	 * @param strIR IR Curve name
	 * 
	 * @return Deposit product
	 */

	public static final org.drip.product.definition.RatesComponent CreateDeposit (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strIR)
	{
		if (null == dtMaturity) {
			System.out.println ("Invalid DepositBuilder.CreateDeposit params!");

			return null;
		}

		try {
			org.drip.product.definition.RatesComponent deposit = new org.drip.product.rates.DepositComponent
				(dtEffective, dtMaturity, fri, strIR, "Act/360", strIR);

			deposit.setPrimaryCode ("CD." + dtMaturity + "." + strIR);

			return deposit;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create the Deposit product from the effective date, tenor, and the IR curve name.
	 * 
	 * @param dtEffective JulianDate Effective
	 * @param strTenor String tenor
	 * @param strIR IR Curve Name
	 * 
	 * @return Deposit object
	 */

	public static final org.drip.product.definition.RatesComponent CreateDeposit (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strTenor,
		final java.lang.String strIR)
	{
		return CreateDeposit (dtEffective, strTenor, strIR, "CD");
	}

	/**
	 * Create a Deposit Instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return Deposit Instance
	 */

	public static final org.drip.product.definition.RatesComponent FromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.product.rates.DepositComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

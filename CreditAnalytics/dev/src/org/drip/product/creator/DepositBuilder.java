
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

	public static final org.drip.product.definition.CalibratableFixedIncomeComponent CreateDeposit2 (
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
			org.drip.product.definition.CalibratableFixedIncomeComponent deposit = new
				org.drip.product.rates.GenericDepositComponent (dtEffective, dtEffective.addTenor (strTenor),
					null, strIR, "Act/360", strIR);

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

	public static final org.drip.product.rates.GenericDepositComponent CreateDeposit2 (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final org.drip.state.identifier.ForwardLabel fri,
		final java.lang.String strIR)
	{
		if (null == dtMaturity) {
			System.out.println ("Invalid DepositBuilder.CreateDeposit params!");

			return null;
		}

		try {
			org.drip.product.rates.GenericDepositComponent deposit = new
				org.drip.product.rates.GenericDepositComponent (dtEffective, dtMaturity, fri, strIR,
					"Act/360", strIR);

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

	public static final org.drip.product.definition.CalibratableFixedIncomeComponent CreateDeposit (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strTenor,
		final java.lang.String strIR)
	{
		return CreateDeposit2 (dtEffective, strTenor, strIR, "CD");
	}

	/**
	 * Create a Deposit product from effective and maturity dates, and the Currency
	 * 
	 * @param dtEffective Effective date
	 * @param dtMaturity Maturity
	 * @param fri The Floating Rate Index
	 * @param strCurrency Currency
	 * 
	 * @return Deposit product
	 */

	public static final org.drip.product.rates.SingleStreamComponent CreateDeposit (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final org.drip.state.identifier.ForwardLabel fri,
		final java.lang.String strCurrency)
	{
		org.drip.state.identifier.ForwardLabel friDeposit = null != fri ? fri :
			org.drip.state.identifier.ForwardLabel.Standard (strCurrency + "-LIBOR-3M");

		java.lang.String strTenor = friDeposit.tenor();

		java.lang.String strCode = "DEPOSIT::" + strTenor + "::{" + dtEffective + "->" + dtMaturity + "}";

		try {
			int iFreq = 12 / org.drip.analytics.support.AnalyticsHelper.TenorToMonths (strTenor);

			org.drip.param.period.UnitCouponAccrualSetting ucas = new
				org.drip.param.period.UnitCouponAccrualSetting (iFreq, "Act/360", false, "Act/360", false,
					strCurrency, false);

			org.drip.param.period.ComposableFloatingUnitSetting cfus = new
				org.drip.param.period.ComposableFloatingUnitSetting (strTenor,
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE, null,
						friDeposit,
							org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
								null, 0.);

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting (iFreq, strTenor, strCurrency, null,
					org.drip.analytics.support.CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC, 1.,
						null, null, null, null);

			org.drip.product.rates.SingleStreamComponent sscDeposit = new
				org.drip.product.rates.SingleStreamComponent (strCode, new org.drip.product.rates.Stream
					(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
						(org.drip.analytics.support.CompositePeriodBuilder.EdgePair (dtEffective,
							dtMaturity), cps, ucas, cfus)), new org.drip.param.valuation.CashSettleParams (0,
								strCurrency, 0));

			sscDeposit.setPrimaryCode (strCode);

			return sscDeposit;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

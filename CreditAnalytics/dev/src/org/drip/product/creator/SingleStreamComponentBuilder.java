
package org.drip.product.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * IRFutureBuilder contains the suite of helper functions for creating the Futures product and product pack
 *  from the parameters/codes/byte array streams. It also contains function to construct EDF codes and the
 *  EDF product from code.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SingleStreamComponentBuilder {

	/**
	 * Create the EDF Code given a effective date
	 * 
	 * @param dblEffective Double representing the Effective JulianDate
	 * 
	 * @return EDF Code String
	 */

	public static java.lang.String MakeBaseEDFCode (
		final double dblEffective)
	{
		try {
			return "ED" + org.drip.analytics.date.DateUtil.CodeFromMonth
				(org.drip.analytics.date.DateUtil.Month (dblEffective)) +
					(org.drip.analytics.date.DateUtil.Year (dblEffective) % 10);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a Futures Pack corresponding to the specified number of contracts
	 * 
	 * @param dt Spot date specifying the contract issue
	 * @param iNumContract Number of contracts
	 * @param strCurrency Contract currency string
	 * 
	 * @return Array of EDF product
	 */

	public static org.drip.product.rates.SingleStreamComponent[] FuturesPack (
		final org.drip.analytics.date.JulianDate dt,
		final int iNumContract,
		final java.lang.String strCurrency)
	{
		if (0 == iNumContract || null == dt) return null;

		org.drip.product.rates.SingleStreamComponent[] aSSC = new
			org.drip.product.rates.SingleStreamComponent[iNumContract];

		try {
			org.drip.param.period.ComposableFloatingUnitSetting cfus = new
				org.drip.param.period.ComposableFloatingUnitSetting ("3M",
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE, null,
						org.drip.state.identifier.ForwardLabel.Standard (strCurrency + "-3M"),
							org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
								0.);

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting (4, "3M", strCurrency, null, 1., null, null,
					null, null);

			org.drip.param.valuation.CashSettleParams csp = new org.drip.param.valuation.CashSettleParams (0,
				strCurrency, 0);

			org.drip.analytics.date.JulianDate dtStart = dt.firstIMMDate (3);

			for (int i = 0; i < iNumContract; ++i) {
				org.drip.analytics.date.JulianDate dtMaturity = dtStart.addMonths (3);

				aSSC[i] = new org.drip.product.rates.SingleStreamComponent ("FUTURE_" + i, new
					org.drip.product.rates.Stream
						(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
							(org.drip.analytics.support.CompositePeriodBuilder.EdgePair (dtStart,
								dtMaturity), cps, cfus)), csp);

				aSSC[i].setPrimaryCode (MakeBaseEDFCode (dtStart.julian()));

				dtStart = dtStart.addMonths (3);
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return aSSC;
	}

	/**
	 * Create a Deposit Product from the Effective and the Maturity Dates, and the Forward Label
	 * 
	 * @param dtEffective Effective date
	 * @param dtMaturity Maturity
	 * @param fri The Floating Rate Index
	 * 
	 * @return Deposit product
	 */

	public static final org.drip.product.rates.SingleStreamComponent Deposit (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final org.drip.state.identifier.ForwardLabel fri)
	{
		java.lang.String strTenor = fri.tenor();

		java.lang.String strCurrency = fri.currency();

		boolean bIsON = "ON".equalsIgnoreCase (strTenor);

		java.lang.String strCode = "DEPOSIT::" + fri.fullyQualifiedName() + "::{" + dtEffective + "->" +
			dtMaturity + "}";

		try {
			int iFreq = bIsON ? 360 : org.drip.analytics.support.AnalyticsHelper.TenorToFreq (strTenor);

			org.drip.param.period.ComposableFloatingUnitSetting cfus = new
				org.drip.param.period.ComposableFloatingUnitSetting (strTenor, bIsON ?
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_OVERNIGHT :
						org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE, null,
							fri,
								org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
				0.);

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting (iFreq, strTenor, strCurrency,
					fri.floaterIndex().spotLagDAPForward(), 1., null, null, null, null);

			org.drip.product.rates.SingleStreamComponent sscDeposit = new
				org.drip.product.rates.SingleStreamComponent (strCode, new org.drip.product.rates.Stream
					(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
						(org.drip.analytics.support.CompositePeriodBuilder.EdgePair (dtEffective,
							dtMaturity), cps, cfus)), new org.drip.param.valuation.CashSettleParams (0,
								strCurrency, 0));

			sscDeposit.setPrimaryCode (strCode);

			return sscDeposit;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Standard FRA from the Spot Date, the Forward Label, and the Strike
	 * 
	 * @param dtSpot Spot Date
	 * @param fri The Floating Rate Index
	 * @param dblStrike Futures Strike
	 * 
	 * @return The Standard FRA Instance
	 */

	public static final org.drip.product.fra.FRAStandardComponent FRAStandard (
		final org.drip.analytics.date.JulianDate dtSpot,
		final org.drip.state.identifier.ForwardLabel fri,
		final double dblStrike)
	{
		if (null == fri || null == dtSpot) return null;

		org.drip.analytics.date.JulianDate dtEffective = null;

		org.drip.analytics.daycount.DateAdjustParams dapEffective = fri.floaterIndex().spotLagDAPForward();

		try {
			dtEffective = null == dapEffective ? dtSpot : new org.drip.analytics.date.JulianDate
				(dapEffective.roll (dtSpot.julian()));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		java.lang.String strTenor = fri.tenor();

		java.lang.String strCurrency = fri.currency();

		boolean bIsON = "ON".equalsIgnoreCase (strTenor);

		org.drip.analytics.date.JulianDate dtMaturity = dtEffective.addTenor (strTenor);
		
		java.lang.String strCode = (0 == dblStrike ? "FUTURES::" : "FRA::") + fri.fullyQualifiedName() +
			"::{" + dtEffective + "->" + dtMaturity + "}";

		try {
			int iFreq = bIsON ? 360 : 12 / org.drip.analytics.support.AnalyticsHelper.TenorToMonths
				(strTenor);

			org.drip.param.period.ComposableFloatingUnitSetting cfus = new
				org.drip.param.period.ComposableFloatingUnitSetting (strTenor, bIsON ?
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_OVERNIGHT :
						org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE, null,
							fri,
								org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
				0.);

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting (iFreq, strTenor, strCurrency, null, 1., null,
					null, null, null);

			org.drip.product.fra.FRAStandardComponent sscDeposit = new org.drip.product.fra.FRAStandardComponent (strCode, new
				org.drip.product.rates.Stream
					(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
						(org.drip.analytics.support.CompositePeriodBuilder.EdgePair (dtEffective,
							dtMaturity), cps, cfus)), dblStrike, new
								org.drip.param.valuation.CashSettleParams (0, strCurrency, 0));

			sscDeposit.setPrimaryCode (strCode);

			return sscDeposit;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a FRA Market Component Instance from the Spot Date, the Forward Label, and the Strike
	 * 
	 * @param dtSpot Spot Date
	 * @param fri The Floating Rate Index
	 * @param dblStrike Futures Strike
	 * 
	 * @return The Futures Product
	 */

	public static final org.drip.product.fra.FRAMarketComponent FRAMarket (
		final org.drip.analytics.date.JulianDate dtSpot,
		final org.drip.state.identifier.ForwardLabel fri,
		final double dblStrike)
	{
		if (null == fri) return null;

		org.drip.analytics.date.JulianDate dtEffective = dtSpot;

		java.lang.String strTenor = fri.tenor();

		java.lang.String strCurrency = fri.currency();

		boolean bIsON = "ON".equalsIgnoreCase (strTenor);

		org.drip.analytics.date.JulianDate dtMaturity = dtEffective.addTenor (strTenor);
		
		java.lang.String strCode = "FUTURES::" + fri.fullyQualifiedName() + "::{" + dtEffective + "->" +
			dtMaturity + "}";

		try {
			int iFreq = org.drip.analytics.support.AnalyticsHelper.TenorToFreq (strTenor);

			org.drip.param.period.ComposableFloatingUnitSetting cfus = new
				org.drip.param.period.ComposableFloatingUnitSetting (strTenor, bIsON ?
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_OVERNIGHT :
						org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE, null,
							fri,
								org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
				0.);

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting (iFreq, strTenor, strCurrency, null, 1., null,
					null, null, null);

			org.drip.product.fra.FRAMarketComponent sscDeposit = new org.drip.product.fra.FRAMarketComponent
				(strCode, new org.drip.product.rates.Stream
					(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
						(org.drip.analytics.support.CompositePeriodBuilder.EdgePair (dtEffective,
							dtMaturity), cps, cfus)), dblStrike, new
								org.drip.param.valuation.CashSettleParams (0, strCurrency, 0));

			sscDeposit.setPrimaryCode (strCode);

			return sscDeposit;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Futures Product Instance from the Spot Date, the Forward Label, and the Strike
	 * 
	 * @param dtSpot Spot Date
	 * @param fri The Floating Rate Index
	 * 
	 * @return The Futures Product Instance
	 */

	public static final org.drip.product.fra.FRAStandardComponent Futures (
		final org.drip.analytics.date.JulianDate dtSpot,
		final org.drip.state.identifier.ForwardLabel fri)
	{
		return FRAStandard (dtSpot, fri, 0.);
	}
}

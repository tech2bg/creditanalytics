
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
		int iMonth = 0;
		java.lang.String strEDFCode = "ED";

		try {
			iMonth = org.drip.analytics.date.JulianDate.Month (dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (org.drip.analytics.date.JulianDate.MARCH == iMonth)
			strEDFCode = strEDFCode + "H";
		else if (org.drip.analytics.date.JulianDate.JUNE == iMonth)
			strEDFCode = strEDFCode + "M";
		else if (org.drip.analytics.date.JulianDate.SEPTEMBER == iMonth)
			strEDFCode = strEDFCode + "U";
		else if (org.drip.analytics.date.JulianDate.DECEMBER == iMonth)
			strEDFCode = strEDFCode + "Z";
		else
			return null;

		try {
			return strEDFCode + (org.drip.analytics.date.JulianDate.Year (dblEffective) % 10);
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

	public static org.drip.product.rates.SingleStreamComponent[] GenerateFuturesPack (
		final org.drip.analytics.date.JulianDate dt,
		final int iNumContract,
		final java.lang.String strCurrency)
	{
		if (0 == iNumContract || null == dt) return null;

		org.drip.product.rates.SingleStreamComponent[] aSSC = new
			org.drip.product.rates.SingleStreamComponent[iNumContract];

		try {
			org.drip.param.period.UnitCouponAccrualSetting ucas = new
				org.drip.param.period.UnitCouponAccrualSetting (4, "Act/360", false, "Act/360", false,
					strCurrency, false);

			org.drip.param.period.ComposableFloatingUnitSetting cfus = new
				org.drip.param.period.ComposableFloatingUnitSetting ("3M",
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE, null,
						org.drip.state.identifier.ForwardLabel.Standard (strCurrency + "-LIBOR-3M"),
							org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
								null, 0.);

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting (4, "3M", strCurrency, null,
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC, 1.,
						null, null, null, null);

			org.drip.param.valuation.CashSettleParams csp = new org.drip.param.valuation.CashSettleParams (0,
				strCurrency, 0);

			org.drip.analytics.date.JulianDate dtStart = dt.firstEDFStartDate (3);

			for (int i = 0; i < iNumContract; ++i) {
				org.drip.analytics.date.JulianDate dtMaturity = dtStart.addMonths (3);

				aSSC[i] = new org.drip.product.rates.SingleStreamComponent ("FUTURE_" + i, new
					org.drip.product.rates.Stream
						(org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
							(org.drip.analytics.support.CompositePeriodBuilder.EdgePair (dtStart,
								dtMaturity), cps, ucas, cfus)), csp);

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

		boolean bIsON = "ON".equalsIgnoreCase (strTenor);

		java.lang.String strCode = "DEPOSIT::" + friDeposit.fullyQualifiedName() + "::{" + dtEffective + "->"
			+ dtMaturity + "}";

		try {
			int iFreq = bIsON ? 360 : 12 / org.drip.analytics.support.AnalyticsHelper.TenorToMonths
				(strTenor);

			org.drip.param.period.UnitCouponAccrualSetting ucas = new
				org.drip.param.period.UnitCouponAccrualSetting (iFreq, "Act/360", false, "Act/360", false,
					strCurrency, false);

			org.drip.param.period.ComposableFloatingUnitSetting cfus = new
				org.drip.param.period.ComposableFloatingUnitSetting (strTenor, bIsON ?
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_OVERNIGHT :
						org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE, null,
							friDeposit,
								org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
				null, 0.);

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting (iFreq, strTenor, strCurrency, null,
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC, 1.,
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

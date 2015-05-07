
package org.drip.sample.bond;

import org.drip.analytics.cashflow.CompositePeriod;
import org.drip.analytics.date.*;
import org.drip.analytics.daycount.Convention;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.BondBuilder;
import org.drip.product.definition.Bond;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;

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
 * FixedPaymentMortgageBond demonstrates the Construction and Valuation of a Custom Fixed Cash-flow Mortgage
 *  Bond.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixedPaymentMortgageBond {

	private static final void BondMetrics (
		final Bond bond,
		final double dblNotional,
		final JulianDate dtSettle,
		final CurveSurfaceQuoteSet mktParams,
		final double dblCleanPrice)
		throws Exception
	{
		double dblAccrued = bond.accrued (
			dtSettle.julian(),
			null
		);

		double dblYield = bond.yieldFromPrice (
			new ValuationParams (
				dtSettle,
				dtSettle,
				bond.currency()
			),
			mktParams,
			null,
			dblCleanPrice
		);

		double dblModifiedDuration = bond.modifiedDurationFromPrice (
			new ValuationParams (
				dtSettle,
				dtSettle,
				bond.currency()
			),
			mktParams,
			null,
			dblCleanPrice
		);

		double dblRisk = bond.yield01FromPrice (
			new ValuationParams (
				dtSettle,
				dtSettle,
				bond.currency()
			),
			mktParams,
			null,
			dblCleanPrice
		);

		double dblConvexity = bond.convexityFromPrice (
			new ValuationParams (
				dtSettle,
				dtSettle,
				bond.currency()
			),
			mktParams,
			null,
			dblCleanPrice
		);

		JulianDate dtPreviousCouponDate = bond.previousCouponDate (dtSettle);

		System.out.println ("\t-------------------------------------");

		System.out.println ("\tAnalytics Metrics for " + bond.name());

		System.out.println ("\t-------------------------------------");

		System.out.println ("\tPrice             : " + FormatUtil.FormatDouble (dblCleanPrice, 1, 4, 100.));

		System.out.println ("\tYield             : " + FormatUtil.FormatDouble (dblYield, 1, 4, 100.) + "%");

		System.out.println ("\tSettle            :  " + dtSettle);

		System.out.println();

		System.out.println ("\tModified Duration : " + FormatUtil.FormatDouble (dblModifiedDuration, 1, 4, 10000.));

		System.out.println ("\tRisk              : " + FormatUtil.FormatDouble (dblRisk, 1, 4, 10000.));

		System.out.println ("\tConvexity         : " + FormatUtil.FormatDouble (dblConvexity * dblNotional, 1, 4, 1.));

		System.out.println ("\tDV01              : " + FormatUtil.FormatDouble (dblRisk * dblNotional, 1, 2, 1.));

		System.out.println();

		System.out.println ("\tPrevious Coupon Date :  " + dtPreviousCouponDate);

		System.out.println ("\tFace                 : " + FormatUtil.FormatDouble (dblNotional, 1, 2, 1.));

		System.out.println ("\tPrincipal            : " + FormatUtil.FormatDouble (dblCleanPrice * dblNotional, 1, 2, 1.));

		System.out.println ("\tAccrued              : " + FormatUtil.FormatDouble (dblAccrued * dblNotional, 1, 2, 1.));

		System.out.println ("\tTotal                : " + FormatUtil.FormatDouble ((dblCleanPrice + dblAccrued) * dblNotional, 1, 2, 1.));

		System.out.println ("\tAccrual Days         : " + FormatUtil.FormatDouble (dtSettle.julian() - dtPreviousCouponDate.julian(), 1, 0, 1.));
	}

	private static final Bond FixedPaymentMortgageAmortizer (
		final String strName,
		final JulianDate dtEffective,
		final int iNumPayment,
		final String strDayCount,
		final int iPayFrequency,
		final double dblCouponRate,
		final double dblFixedMonthlyAmount,
		final double dblBondNotional)
		throws Exception
	{
		double dblOutstandingPrincipalPrev = 1.;
		JulianDate[] adt = new JulianDate[iNumPayment];
		double[] adblCouponAmount = new double[iNumPayment];
		double[] adblPrincipalAmount = new double[iNumPayment];
		double dblTotalMonthlyPayment = dblFixedMonthlyAmount / dblBondNotional;

		for (int i = 0; i < iNumPayment; ++i) {
			adt[i] = dtEffective.addMonths (i + 1);

			JulianDate dtPrev = 0 == i ? dtEffective : adt[i - 1];

			if (0 != i) dblOutstandingPrincipalPrev -= adblPrincipalAmount[i - 1];

			adblCouponAmount[i] = dblCouponRate;

			double dblPeriodDCF = Convention.YearFraction (
				dtPrev.julian(),
				adt[i].julian(),
				strDayCount,
				false,
				null,
				""
			);

			double dblPeriodCoupon = dblOutstandingPrincipalPrev * adblCouponAmount[i] * dblPeriodDCF;
			adblPrincipalAmount[i] = dblTotalMonthlyPayment - dblPeriodCoupon;
		}

		return BondBuilder.CreateBondFromCF (
			strName,				// Name
			dtEffective,			// Effective
			"USD",					// Currency
			"", 					// Credit Curve
			strDayCount,			// Day Count
			iPayFrequency,			// Frequency
			adt,					// Array of dates
			adblCouponAmount,		// Array of coupon amount
			adblPrincipalAmount,	// Array of principal amount
			true					// Principal is an outstanding notional
		);
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		double dblNotional = 1.;
		double dblCouponRate = 0.1299;
		double dblBondNotional = 6000.;
		double dblFixedMonthlyAmount = 202.13;
		String strDayCount = "Act/365";
		int iNumPayment = 36;
		int iPayFrequency = 12;

		JulianDate dtEffective = DateUtil.CreateFromYMD (
			2013,
			DateUtil.AUGUST,
			19
		);

		Bond bond = FixedPaymentMortgageAmortizer (
			"FPMA 12.99 2016",
			dtEffective,
			iNumPayment,
			strDayCount,
			iPayFrequency,
			dblCouponRate,
			dblFixedMonthlyAmount,
			dblBondNotional
		);

		System.out.println ("\n\n\t|---------------------------------------------------------------------------------------------------------------------||");

		System.out.println ("\t|                                         FIXED CASH-FLOW MORTGAGE BOND ANALYTICS                                     ||");

		System.out.println ("\t|                                         ----- --------- -------- ---- ---------                                     ||");

		System.out.println ("\t|    L -> R:                                                                                                          ||");

		System.out.println ("\t|            - Start Date                                                                                             ||");

		System.out.println ("\t|            - End Date                                                                                               ||");

		System.out.println ("\t|            - Pay Date                                                                                               ||");

		System.out.println ("\t|            - Principal Factor                                                                                       ||");

		System.out.println ("\t|            - Accrual Days                                                                                           ||");

		System.out.println ("\t|            - Accrual Fraction                                                                                       ||");

		System.out.println ("\t|            - Coupon Rate (%)                                                                                        ||");

		System.out.println ("\t|            - Coupon Amount                                                                                          ||");

		System.out.println ("\t|            - Principal Amount                                                                                       ||");

		System.out.println ("\t|            - Total Amount                                                                                           ||");

		System.out.println ("\t|---------------------------------------------------------------------------------------------------------------------||");

		for (CompositePeriod p : bond.couponPeriods()) {
			double dblPeriodCouponRate = p.couponMetrics (dtEffective.julian(), null).rate();

			double dblCouponDCF = p.couponDCF();

			double dblEndNotional = bond.notional (p.endDate());

			double dblNotionalAmount = (dblNotional - dblEndNotional) * dblBondNotional;

			double dblCouponAmount = dblNotional * dblPeriodCouponRate * dblCouponDCF * dblBondNotional;

			System.out.println ("\t| [" +
				DateUtil.FromJulian (p.startDate()) + " -> " +
				DateUtil.FromJulian (p.endDate()) + "] => " +
				DateUtil.FromJulian (p.payDate()) + " | " +
				FormatUtil.FormatDouble (dblNotional, 1, 8, 1.) + " | " +
				FormatUtil.FormatDouble (dblCouponDCF * 365, 1, 0, 1.) + " | " +
				FormatUtil.FormatDouble (dblCouponDCF, 1, 10, 1.) + " | " +
				FormatUtil.FormatDouble (dblPeriodCouponRate, 2, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (dblCouponAmount, 2, 2, 1.) + " | " +
				FormatUtil.FormatDouble (dblNotionalAmount, 2, 2, 1.) + " | " +
				FormatUtil.FormatDouble (dblNotionalAmount + dblCouponAmount, 2, 2, 1.) + " ||"
			);

			dblNotional = dblEndNotional;
		}

		System.out.println ("\t|---------------------------------------------------------------------------------------------------------------------||\n\n");

		JulianDate dtSettle = DateUtil.Today();

		double dblCleanPrice = 1.00; // PAR

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		BondMetrics (
			bond,
			dblBondNotional,
			dtSettle,
			mktParams,
			dblCleanPrice
		);
	}
}

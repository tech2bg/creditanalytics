
package org.drip.sample.bloomberg;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.period.Period;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.definition.ComponentMarketParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * SWPMOIS contains the sample demonstrating the replication of Bloomberg's SWPM functionality, using OIS
 * 	discounting.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SWPMOIS {
	private static final String FIELD_SEPARATOR = "    ";

	/*
	 * Construct the Array of Cash Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				null,
				strCurrency);

		CalibratableFixedIncomeComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, strCurrency);

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrTenor.length];

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			FloatingStream floatStream = FloatingStream.Create (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., true, FloatingRateIndex.Create (strCurrency + "-LIBOR-3M"),
					3, "Act/360", false, "Act/360", false, false, null, dap, dap, dap, dap, dap, dap,
						null, null, -1., strCurrency, strCurrency);

			FixedStream fixStream = new FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
				adblCoupon[i], 2, "30/360", "30/360", false, null, dap, dap, dap, dap, dap, null, null, 1.,
					strCurrency, strCurrency);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = irs;
		}

		return aCalibComp;
	}

	/*
	 * Construct the discount curve using the following steps:
	 * 	- Construct the array of cash instruments and their quotes.
	 * 	- Construct the array of swap instruments and their quotes.
	 * 	- Construct a shape preserving and smoothing KLK Hyperbolic Spline from the cash/swap instruments.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final DiscountCurve MakeDC (
		final JulianDate dtSpot,
		final String strCurrency,
		final double dblBump)
		throws Exception
	{
		/*
		 * Construct the array of cash instruments and their quotes.
		 */

		CalibratableFixedIncomeComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {},
			0,
			strCurrency);

		double[] adblCashQuote = new double[] {}; // Futures

		/*
		 * Construct the array of Swap instruments and their quotes.
		 */

		double[] adblSwapQuote = new double[] {
			0.00092 + dblBump,     //  6M
			0.0009875 + dblBump,   //  9M
			0.00122 + dblBump,     //  1Y
			0.00223 + dblBump,     // 18M
			0.00383 + dblBump,     //  2Y
			0.00827 + dblBump,     //  3Y
			0.01245 + dblBump,     //  4Y
			0.01605 + dblBump,     //  5Y
			0.02597 + dblBump      // 10Y
		};

		CalibratableFixedIncomeComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {"6M", "9M", "1Y", "18M", "2Y", "3Y", "4Y", "5Y", "10Y"},
			adblSwapQuote,
			strCurrency);

		/*
		 * Construct a shape preserving and smoothing KLK Hyperbolic Spline from the cash/swap instruments.
		 */

		return ScenarioDiscountCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SHAPE_TEMPLATE",
			new ValuationParams (dtSpot, dtSpot, "USD"),
			aCashComp,
			adblCashQuote,
			aSwapComp,
			adblSwapQuote,
			true);
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtValue = JulianDate.Today();

		JulianDate dtSettle = dtValue.addBusDays (2, "USD");

		System.out.println ("\n---- Valuation Details ----\n");

		System.out.println ("Trade Date  : " + dtValue);

		System.out.println ("Settle Date : " + dtSettle);

		double dblCoupon = 0.0187;
		double dblFixing = 0.00087;
		double dblNotional = 10.e+06;

		/*
		 * Model the discount curve instrument quotes. Best pulled from Curves #42 in the BBG SWPM "Curves" tab
		 */

		/*
		 * Build the Discount Curve
		 */

		DiscountCurve dc = MakeDC (dtValue, "USD", 0.);

		JulianDate dtEffective = dtValue.addBusDays (2, "USD");

		JulianDate dtMaturity = dtEffective.addTenor ("5Y");

		/*
		 * Build the Fixed Receive Stream
		 */

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, "USD");

		FixedStream fixStream = new FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
			dblCoupon, 2, "30/360", "30/360", false, null, null, dap, dap, dap, dap, dap, null, dblNotional, "USD", "USD");

		/*
		 * Build the Floating Pay Stream
		 */

		FloatingStream floatStream = FloatingStream.Create (dtEffective.getJulian(),
			dtMaturity.getJulian(), 0., true, org.drip.product.params.FloatingRateIndex.Create ("USD-LIBOR-3M"), 4,
				"Act/360", false, "Act/360", false, false, null, null,
					dap, dap, dap, dap, dap, null, null, -dblNotional, "USD", "USD");

		/*
		 * Build the Swap from the fixed and the floating streams
		 */

		IRSComponent swap = new IRSComponent (fixStream, floatStream);

		System.out.println ("\n---- Swap Details ----\n");

		System.out.println ("Effective: " + dtEffective);

		System.out.println ("Maturity:  " + dtMaturity);

		/*
		 * Set up the base market parameters, including base discount curves and the base fixings
		 */

		CaseInsensitiveTreeMap<Double> mapIndexFixing = new CaseInsensitiveTreeMap<Double>();

		mapIndexFixing.put ("USD-LIBOR-3M", dblFixing);

		Map<JulianDate, CaseInsensitiveTreeMap<Double>> mmFixings = new HashMap<JulianDate, CaseInsensitiveTreeMap<Double>>();

		mmFixings.put (dtEffective, mapIndexFixing);

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, mmFixings);

		/*
		 * Set up the valuation parameters
		 */

		ValuationParams valParams = new ValuationParams (dtValue, dtSettle, "USD");

		/*
		 * Generate the base scenario measures for the swap
		 */

		CaseInsensitiveTreeMap<Double> mapSwapCalc = swap.value (valParams, null, cmp, null);

		double dblBasePV = mapSwapCalc.get ("PV");

		double dblBaseFixedDV01 = mapSwapCalc.get ("FixedDV01");

		System.out.println ("\n---- Swap Output Measures ----\n");

		System.out.println ("Mkt Val       : " + FormatUtil.FormatDouble (dblBasePV, 0, 0, 1.));

		System.out.println ("Mkt Val Fixed : " + FormatUtil.FormatDouble (mapSwapCalc.get ("DirtyFixedPV"), 0, 0, 1.));

		System.out.println ("Par Cpn       : " + FormatUtil.FormatDouble (mapSwapCalc.get ("FairPremium"), 1, 5, 100.));

		System.out.println ("Fixed DV01    : " + FormatUtil.FormatDouble (dblBaseFixedDV01, 0, 0, 1.));

		/*
		 * Set up the fixings bumped market parameters - these use base discount curve and the bumped fixing
		 */

		mapIndexFixing.put ("USD-LIBOR-3M", dblFixing + 0.0001);

		mmFixings.put (dtEffective, mapIndexFixing);

		ComponentMarketParams cmpFixingsBumped = ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, mmFixings);

		/*
		 * Generate the fixing bumped scenario measures for the swap
		 */

		CaseInsensitiveTreeMap<Double> mapSwapFixingsBumpedCalc = swap.value (valParams, null, cmpFixingsBumped, null);

		double dblFixingsDV01 = mapSwapFixingsBumpedCalc.get ("PV") - dblBasePV;

		System.out.println ("Fixings DV01 : " + FormatUtil.FormatDouble (dblFixingsDV01, 0, 0, 1.));

		System.out.println ("Total DV01   : " + FormatUtil.FormatDouble (dblBaseFixedDV01 + dblFixingsDV01, 0, 0, 1.));

		/*
		 * Set up the rate flat bumped market parameters - these use the bumped base discount curve and the base fixing
		 */

		DiscountCurve dcBumped = MakeDC (dtValue, "USD", -0.0001);

		mapIndexFixing.put ("USD-LIBOR-3M", dblFixing - 0.0001);

		mmFixings.put (dtEffective, mapIndexFixing);

		ComponentMarketParams cmpRateBumped = ComponentMarketParamsBuilder.CreateComponentMarketParams (dcBumped, null, null, null, null, null, mmFixings);

		/*
		 * Generate the rate flat bumped scenario measures for the swap
		 */

		CaseInsensitiveTreeMap<Double> mapSwapRateBumpedCalc = swap.value (valParams, null, cmpRateBumped, null);

		System.out.println ("PV01         : " + FormatUtil.FormatDouble (mapSwapRateBumpedCalc.get ("PV") - dblBasePV, 0, 0, 1.));

		/*
		 * Generate the Swap's fixed cash flows
		 */

		System.out.println ("\n---- Fixed Cashflow ----\n");

		for (Period p : fixStream.cashFlowPeriod())
			System.out.println (
				JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualEndDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF() * 360, 0, 0, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF(), 0, 2, dblCoupon * dblNotional) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dc.df (p.getPayDate()), 1, 4, 1.)
			);

		/*
		 * Generate the Swap's floating cash flows
		 */

		System.out.println ("\n---- Floating Cashflow ----\n");

		for (Period p : floatStream.cashFlowPeriod())
			System.out.println (
				JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualStartDate()) + FIELD_SEPARATOR +
				JulianDate.fromJulian (p.getAccrualEndDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.getCouponDCF() * 360, 0, 0, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dc.df (p.getPayDate()), 1, 4, 1.)
			);
	}
}

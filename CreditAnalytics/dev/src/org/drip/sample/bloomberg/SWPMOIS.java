
package org.drip.sample.bloomberg;

import java.util.List;

import org.drip.analytics.cashflow.CompositePeriod;
import org.drip.analytics.date.DateUtil;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.*;
import org.drip.param.creator.*;
import org.drip.param.market.*;
import org.drip.param.period.*;
import org.drip.param.valuation.*;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.identifier.ForwardLabel;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * SWPMOIS contains the sample demonstrating the replication of Bloomberg's SWPM OIS functionality.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SWPMOIS {
	private static final String FIELD_SEPARATOR = "    ";

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFuture,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[aiDay.length + iNumFuture];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = SingleStreamComponentBuilder.Deposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				ForwardLabel.Create (strCurrency, aiDay[i] + "D")
			);

		CalibratableFixedIncomeComponent[] aEDF = SingleStreamComponentBuilder.FuturesPack (
			dtEffective,
			iNumFuture,
			strCurrency
		);

		for (int i = aiDay.length; i < aiDay.length + iNumFuture; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	private static final FixFloatComponent IRS (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strMaturityTenor,
		final double dblCoupon)
		throws Exception
	{
		UnitCouponAccrualSetting ucasFixed = new UnitCouponAccrualSetting (
			2,
			"Act/360",
			false,
			"Act/360",
			false,
			strCurrency,
			true,
			CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC
		);

		ComposableFloatingUnitSetting cfusFloating = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			ForwardLabel.Create (strCurrency, "3M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		ComposableFixedUnitSetting cfusFixed = new ComposableFixedUnitSetting (
			"6M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			dblCoupon,
			0.,
			strCurrency
		);

		CompositePeriodSetting cpsFloating = new CompositePeriodSetting (
			4,
			"3M",
			strCurrency,
			null,
			-1.,
			null,
			null,
			null,
			null
		);

		CompositePeriodSetting cpsFixed = new CompositePeriodSetting (
			2,
			"6M",
			strCurrency,
			null,
			1.,
			null,
			null,
			null,
			null
		);

		CashSettleParams csp = new CashSettleParams (
			0,
			strCurrency,
			0
		);

		List<Double> lsFixedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
			dtEffective,
			"6M",
			strMaturityTenor,
			null
		);

		List<Double> lsFloatingStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
			dtEffective,
			"3M",
			strMaturityTenor,
			null
		);

		Stream floatingStream = new Stream (
			CompositePeriodBuilder.FloatingCompositeUnit (
				lsFloatingStreamEdgeDate,
				cpsFloating,
				cfusFloating
			)
		);

		Stream fixedStream = new Stream (
			CompositePeriodBuilder.FixedCompositeUnit (
				lsFixedStreamEdgeDate,
				cpsFixed,
				ucasFixed,
				cfusFixed
			)
		);

		FixFloatComponent irs = new FixFloatComponent (
			fixedStream,
			floatingStream,
			csp
		);

		irs.setPrimaryCode ("IRS." + strMaturityTenor + "." + strCurrency);

		return irs;
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrTenor,
		final double[] adblCoupon)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = IRS (
				dtEffective,
				strCurrency,
				astrTenor[i],
				adblCoupon[i]
			);

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
		 * Construct the array of Deposit instruments and their quotes.
		 */

		CalibratableFixedIncomeComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {},
			0,
			strCurrency
		);

		double[] adblDepositQuote = new double[] {}; // Futures

		/*
		 * Construct the array of Swap instruments and their quotes.
		 */

		double[] adblSwapQuote = new double[] {
			0.0009875 + dblBump,   //  9M
			0.00122 + dblBump,     //  1Y
			0.00223 + dblBump,     // 18M
			0.00383 + dblBump,     //  2Y
			0.00827 + dblBump,     //  3Y
			0.01245 + dblBump,     //  4Y
			0.01605 + dblBump,     //  5Y
			0.02597 + dblBump      // 10Y
		};

		String[] astrSwapManifestMeasure = new String[] {
			"SwapRate",		//  9M
			"SwapRate",     //  1Y
			"SwapRate",     // 18M
			"SwapRate",     //  2Y
			"SwapRate",     //  3Y
			"SwapRate",     //  4Y
			"SwapRate",     //  5Y
			"SwapRate"      // 10Y
		};

		CalibratableFixedIncomeComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"9M", "1Y", "18M", "2Y", "3Y", "4Y", "5Y", "10Y"
			},
			new double[] {
				0.0009875, 0.00122, 0.00223, 0.00383, 0.00827, 0.01245, 0.01605, 0.02597
			}
		);

		/*
		 * Construct a shape preserving and smoothing KLK Hyperbolic Spline from the cash/swap instruments.
		 */

		return ScenarioDiscountCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SHAPE_TEMPLATE",
			new ValuationParams (dtSpot, dtSpot, "USD"),
			aDepositComp,
			adblDepositQuote,
			null,
			aSwapComp,
			adblSwapQuote,
			astrSwapManifestMeasure,
			true
		);
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		JulianDate dtValue = DateUtil.Today();

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

		FixFloatComponent swap = IRS (
			dtEffective,
			"USD",
			"5Y",
			0.
		);

		System.out.println ("\n---- Swap Details ----\n");

		System.out.println ("Effective: " + dtEffective);

		System.out.println ("Maturity:  " + dtMaturity);

		/*
		 * Set up the base market parameters, including base discount curves and the base fixings
		 */

		LatentStateFixingsContainer lsfc = new LatentStateFixingsContainer();

		lsfc.add (dtEffective, swap.derivedStream().forwardLabel(), dblFixing);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (dc, null, null, null, null, null, lsfc);

		/*
		 * Set up the valuation parameters
		 */

		ValuationParams valParams = new ValuationParams (dtValue, dtSettle, "USD");

		/*
		 * Generate the base scenario measures for the swap
		 */

		CaseInsensitiveTreeMap<Double> mapSwapCalc = swap.value (valParams, null, mktParams, null);

		double dblBasePV = mapSwapCalc.get ("PV");

		double dblBaseFixedDV01 = mapSwapCalc.get ("FixedDV01");

		System.out.println ("\n---- Swap Output Measures ----\n");

		System.out.println ("Mkt Val      : " + FormatUtil.FormatDouble (dblBasePV, 0, 0, dblNotional));

		System.out.println ("Par Cpn      : " + FormatUtil.FormatDouble (mapSwapCalc.get ("FairPremium"), 1, 5, 100.));

		System.out.println ("Fixed DV01   : " + FormatUtil.FormatDouble (dblBaseFixedDV01, 0, 0, dblNotional));

		/*
		 * Set up the fixings bumped market parameters - these use base discount curve and the bumped fixing
		 */

		lsfc.add (dtEffective, swap.derivedStream().forwardLabel(), dblFixing + 0.0001);

		CurveSurfaceQuoteSet mktParamsFixingsBumped = MarketParamsBuilder.Create (dc, null, null, null, null, null, lsfc);

		/*
		 * Generate the fixing bumped scenario measures for the swap
		 */

		CaseInsensitiveTreeMap<Double> mapSwapFixingsBumpedCalc = swap.value (valParams, null, mktParamsFixingsBumped, null);

		double dblFixingsDV01 = mapSwapFixingsBumpedCalc.get ("PV") - dblBasePV;

		System.out.println ("Fixings DV01 : " + FormatUtil.FormatDouble (dblFixingsDV01, 0, 0, dblNotional));

		System.out.println ("Total DV01   : " + FormatUtil.FormatDouble (dblBaseFixedDV01 + dblFixingsDV01, 0, 0, dblNotional));

		/*
		 * Set up the rate flat bumped market parameters - these use the bumped base discount curve and the base fixing
		 */

		DiscountCurve dcBumped = MakeDC (dtValue, "USD", -0.0001);

		lsfc.add (dtEffective, swap.derivedStream().forwardLabel(), dblFixing - 0.0001);

		CurveSurfaceQuoteSet mktParamsRateBumped = MarketParamsBuilder.Create (dcBumped, null, null, null, null, null, lsfc);

		/*
		 * Generate the rate flat bumped scenario measures for the swap
		 */

		CaseInsensitiveTreeMap<Double> mapSwapRateBumpedCalc = swap.value (valParams, null, mktParamsRateBumped, null);

		System.out.println ("PV01         : " + FormatUtil.FormatDouble (mapSwapRateBumpedCalc.get ("PV") - dblBasePV, 0, 0, dblNotional));

		/*
		 * Generate the Swap's fixed cash flows
		 */

		System.out.println ("\n---- Fixed Cashflow ----\n");

		for (CompositePeriod p : swap.referenceStream().cashFlowPeriod())
			System.out.println (
				DateUtil.FromJulian (p.payDate()) + FIELD_SEPARATOR +
				DateUtil.FromJulian (p.startDate()) + FIELD_SEPARATOR +
				DateUtil.FromJulian (p.endDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.couponDCF() * 360, 0, 0, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.couponDCF(), 0, 2, dblCoupon * dblNotional) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dc.df (p.payDate()), 1, 4, 1.)
			);

		/*
		 * Generate the Swap's floating cash flows
		 */

		System.out.println ("\n---- Floating Cashflow ----\n");

		for (CompositePeriod p : swap.derivedStream().cashFlowPeriod())
			System.out.println (
				DateUtil.FromJulian (p.payDate()) + FIELD_SEPARATOR +
				DateUtil.FromJulian (p.startDate()) + FIELD_SEPARATOR +
				DateUtil.FromJulian (p.endDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (p.couponDCF() * 360, 0, 0, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dc.df (p.payDate()), 1, 4, 1.)
			);
	}
}

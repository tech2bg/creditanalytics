
package org.drip.sample.option;

import java.util.*;

import org.drip.analytics.date.*;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.market.product.*;
import org.drip.param.creator.ScenarioDiscountCurveBuilder;
import org.drip.param.valuation.*;
import org.drip.pricer.option.BlackScholesAlgorithm;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.option.EuropeanCallPut;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.SABRLIBORCapVolatility;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.identifier.ForwardLabel;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * DeterministicVolBlackScholes contains an illustration of the Black Scholes based European Call and Put
 * 	Options Pricer that uses deterministic Volatility Function.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DeterministicVolBlackScholes {

	private static final FixFloatComponent OTCIRS (
		final JulianDate dtSpot,
		final String strCurrency,
		final String strMaturityTenor,
		final double dblCoupon)
	{
		FixFloatConvention ffConv = FixFloatContainer.ConventionFromJurisdiction (
			strCurrency,
			"ALL",
			strMaturityTenor,
			"MAIN"
		);

		return ffConv.createFixFloatComponent (
			dtSpot,
			strMaturityTenor,
			dblCoupon,
			0.,
			1.
		);
	}

	/*
	 * Construct the Array of Cash Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = SingleStreamComponentBuilder.Deposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				ForwardLabel.Create (strCurrency, "3M")
			);

		CalibratableFixedIncomeComponent[] aEDF = SingleStreamComponentBuilder.FuturesPack (
			dtEffective,
			iNumFutures,
			strCurrency
		);

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtSpot,
		final String strCurrency,
		final String[] astrMaturityTenor,
		final double[] adblCoupon)
		throws Exception
	{
		FixFloatComponent[] aIRS = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			FixFloatComponent irs = OTCIRS (
				dtSpot,
				strCurrency,
				astrMaturityTenor[i],
				adblCoupon[i]
			);

			irs.setPrimaryCode ("IRS." + astrMaturityTenor[i] + "." + strCurrency);

			aIRS[i] = irs;
		}

		return aIRS;
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
		final String strCurrency)
		throws Exception
	{
		/*
		 * Construct the array of Deposit instruments and their quotes.
		 */

		CalibratableFixedIncomeComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {1, 2, 3, 7, 14, 21, 30, 60},
			0,
			strCurrency
		);

		double[] adblDepositQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850
		};

		String[] astrDepositManifestMeasure = new String[] {
			"ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate", "ForwardRate"
		};

		/*
		 * Construct the array of Swap instruments and their quotes.
		 */

		double[] adblSwapQuote = new double[] {
			0.02604,    //  4Y
			0.02808,    //  5Y
			0.02983,    //  6Y
			0.03136,    //  7Y
			0.03268,    //  8Y
			0.03383,    //  9Y
			0.03488,    // 10Y
			0.03583,    // 11Y
			0.03668,    // 12Y
			0.03833,    // 15Y
			0.03854,    // 20Y
			0.03672,    // 25Y
			0.03510,    // 30Y
			0.03266,    // 40Y
			0.03145     // 50Y
		};

		String[] astrSwapManifestMeasure = new String[] {
			"SwapRate",    //  4Y
			"SwapRate",    //  5Y
			"SwapRate",    //  6Y
			"SwapRate",    //  7Y
			"SwapRate",    //  8Y
			"SwapRate",    //  9Y
			"SwapRate",    // 10Y
			"SwapRate",    // 11Y
			"SwapRate",    // 12Y
			"SwapRate",    // 15Y
			"SwapRate",    // 20Y
			"SwapRate",    // 25Y
			"SwapRate",    // 30Y
			"SwapRate",    // 40Y
			"SwapRate"     // 50Y
		};

		CalibratableFixedIncomeComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"
			},
			adblSwapQuote
		);

		/*
		 * Construct a shape preserving and smoothing KLK Hyperbolic Spline from the cash/swap instruments.
		 */

		return ScenarioDiscountCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SHAPE_TEMPLATE",
			new ValuationParams (dtSpot, dtSpot, "USD"),
			aDepositComp,
			adblDepositQuote,
			astrDepositManifestMeasure,
			aSwapComp,
			adblSwapQuote,
			astrSwapManifestMeasure,
			true
		);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = DateUtil.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		/*
		 * Construct the Discount Curve using its instruments and quotes
		 */

		DiscountCurve dc = MakeDC (dtToday, "USD");

		JulianDate dtMaturity = dtToday.addTenor ("6M");

		double dblStrike = 1.;

		EuropeanCallPut option = new EuropeanCallPut (
			dtMaturity,
			dblStrike);

		double dblSpot = 1.;

		SABRLIBORCapVolatility auSABRLIBORCapVol = new SABRLIBORCapVolatility (
				dtToday.julian(),
				0.2000,				// A
				0.0005 / 365.25, 	// B
				0.1000 / 365.25, 	// C
				0.0050); 			// D

		Map<String, Double> mapOptionCalc = option.value (
			valParams,
			dblSpot,
			false,
			dc,
			auSABRLIBORCapVol,
			new BlackScholesAlgorithm());

		for (Map.Entry<String, Double> me : mapOptionCalc.entrySet())
			System.out.println ("\t" + me.getKey() + " => " + me.getValue());

		System.out.println ("\n\tImplied Vol:" + FormatUtil.FormatDouble (
			option.implyVolatility (
				valParams,
				dblSpot,
				false,
				dc,
				mapOptionCalc.get ("CallPrice")
			), 2, 2, 100.) + "%");
	}
}

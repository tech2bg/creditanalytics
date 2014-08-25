
package org.drip.sample.multicurve;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CouponPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.PeriodHelper;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.pricer.PricerParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.cashflow.*;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.rates.*;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.stretch.MultiSegmentSequenceBuilder;
import org.drip.state.identifier.*;

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
 * STIR contains a full valuation run on the STIR Product.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class STIR {

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

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenor (astrTenor[i]);

			List<CouponPeriod> lsFloatPeriods = PeriodHelper.RegularPeriodSingleReset (
				dtEffective.julian(),
				astrTenor[i],
				null,
				4,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency,
				ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
				null
			);

			FloatingStream floatStream = new FloatingStream (
				strCurrency,
				null,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
				false
			);

			List<CouponPeriod> lsFixedPeriods = PeriodHelper.RegularPeriodSingleReset (
				dtEffective.julian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency,
				null,
				null
			);

			FixedStream fixStream = new FixedStream (
				strCurrency,
				null,
				adblCoupon[i],
				1.,
				null,
				lsFixedPeriods
			);

			org.drip.product.rates.FixFloatComponent irs = new org.drip.product.rates.FixFloatComponent (fixStream,
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
		final String strCurrency)
		throws Exception
	{
		/*
		 * Construct the array of cash instruments and their quotes.
		 */

		CalibratableFixedIncomeComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {1, 2, 3, 7, 14, 21, 30, 60},
			0,
			strCurrency);

		double[] adblCashQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850}; // Cash
			// 0.01612, 0.01580, 0.01589, 0.01598}; // Futures

		String[] astrCashManifestMeasure = new String[] {
			"Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate", "Rate"}; // Cash
			// "Rate", "Rate", "Rate", "Rate"}; // Futures

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
			new java.lang.String[] {"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"},
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
			astrCashManifestMeasure,
			aSwapComp,
			adblSwapQuote,
			astrSwapManifestMeasure,
			true);
	}

	/*
	 * Construct an array of float-float swaps from the corresponding reference (6M) and the derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FloatFloatComponent[] MakexM6MBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {

			/*
			 * The Reference 6M Leg
			 */

			List<CouponPeriod> lsReferenceFloatPeriods = PeriodHelper.RegularPeriodSingleReset (
				dtEffective.julian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency,
				ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
				null
			);

			FloatingStream fsReference = new FloatingStream (
				strCurrency,
				null,
				0.,
				-1.,
				null,
				lsReferenceFloatPeriods,
				ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
				false
			);

			/*
			 * The Derived Leg
			 */

			List<CouponPeriod> lsDerivedFloatPeriods = PeriodHelper.RegularPeriodSingleReset (
				dtEffective.julian(),
				astrTenor[i],
				null,
				12 / iTenorInMonths,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency,
				ForwardLabel.Standard (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
				null
			);

			FloatingStream fsDerived = new FloatingStream (
				strCurrency,
				null,
				0.,
				1.,
				null,
				lsDerivedFloatPeriods,
				ForwardLabel.Standard (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
				false
			);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (fsReference, fsDerived);
		}

		return aFFC;
	}

	private static final ForwardCurve MakeFC (
		final JulianDate dtSpot,
		final String strCurrency,
		final DiscountCurve dc,
		final int iTenorInMonths,
		final String[] astrxM6MFwdTenor,
		final double[] adblxM6MBasisSwapQuote)
		throws Exception
	{
		/*
		 * Construct the 6M-xM float-float basis swap.
		 */

		FloatFloatComponent[] aFFC = MakexM6MBasisSwap (dtSpot, strCurrency, astrxM6MFwdTenor, iTenorInMonths);

		String strBasisTenor = iTenorInMonths + "M";

		ValuationParams valParams = new ValuationParams (dtSpot, dtSpot, strCurrency);

		/*
		 * Calculate the starting forward rate off of the discount curve.
		 */

		double dblStartingFwd = dc.forward (dtSpot.julian(), dtSpot.addTenor (strBasisTenor).julian());

		/*
		 * Set the discount curve based component market parameters.
		 */

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (dc, null, null, null, null, null, null);

		/*
		 * Construct the shape preserving forward curve off of Quartic Polynomial Basis Spline.
		 */

		return ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			"QUARTIC_FWD" + strBasisTenor,
			ForwardLabel.Create (strCurrency, "LIBOR", strBasisTenor),
			valParams,
			null,
			mktParams,
			null,
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (5),
			aFFC,
			"DerivedParBasisSpread",
			adblxM6MBasisSwapQuote,
			dblStartingFwd);
	}

	private static final Map<String, ForwardCurve> MakeFC (
		final JulianDate dt,
		final String strCurrency,
		final DiscountCurve dc)
		throws Exception
	{
		Map<String, ForwardCurve> mapFC = new HashMap<String, ForwardCurve>();

		/*
		 * Build and run the sampling for the 1M-6M Tenor Basis Swap from its instruments and quotes.
		 */

		ForwardCurve fc1M = MakeFC (
			dt,
			strCurrency,
			dc,
			1,
			new String[] {"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
			new double[] {
				0.00551,    //  1Y
				0.00387,    //  2Y
				0.00298,    //  3Y
				0.00247,    //  4Y
				0.00211,    //  5Y
				0.00185,    //  6Y
				0.00165,    //  7Y
				0.00150,    //  8Y
				0.00137,    //  9Y
				0.00127,    // 10Y
				0.00119,    // 11Y
				0.00112,    // 12Y
				0.00096,    // 15Y
				0.00079,    // 20Y
				0.00069,    // 25Y
				0.00062     // 30Y
				}
			);

		mapFC.put ("1M", fc1M);

		/*
		 * Build and run the sampling for the 3M-6M Tenor Basis Swap from its instruments and quotes.
		 */

		ForwardCurve fc3M = MakeFC (
			dt,
			strCurrency,
			dc,
			3,
			new String[] {"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
			new double[] {
				0.00186,    //  1Y
				0.00127,    //  2Y
				0.00097,    //  3Y
				0.00080,    //  4Y
				0.00067,    //  5Y
				0.00058,    //  6Y
				0.00051,    //  7Y
				0.00046,    //  8Y
				0.00042,    //  9Y
				0.00038,    // 10Y
				0.00035,    // 11Y
				0.00033,    // 12Y
				0.00028,    // 15Y
				0.00022,    // 20Y
				0.00020,    // 25Y
				0.00018     // 30Y
				}
			);

		mapFC.put ("3M", fc3M);

		/*
		 * Build and run the sampling for the 12M-6M Tenor Basis Swap from its instruments and quotes.
		 */

		ForwardCurve fc12M = MakeFC (
			dt,
			strCurrency,
			dc,
			12,
			new String[] {"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y",
				"35Y", "40Y"}, // Extrapolated
			new double[] {
				-0.00212,    //  1Y
				-0.00152,    //  2Y
				-0.00117,    //  3Y
				-0.00097,    //  4Y
				-0.00082,    //  5Y
				-0.00072,    //  6Y
				-0.00063,    //  7Y
				-0.00057,    //  8Y
				-0.00051,    //  9Y
				-0.00047,    // 10Y
				-0.00044,    // 11Y
				-0.00041,    // 12Y
				-0.00035,    // 15Y
				-0.00028,    // 20Y
				-0.00025,    // 25Y
				-0.00022,    // 30Y
				-0.00022,    // 35Y Extrapolated
				-0.00022,    // 40Y Extrapolated
				}
			);

		mapFC.put ("12M", fc12M);

		return mapFC;
	}

	private static final STIRFutureComponent CreateSTIR (
		final JulianDate dtEffective,
		final String strTenor,
		final ForwardLabel fri,
		final double dblCoupon,
		final String strCurrency)
		throws Exception
	{
		JulianDate dtMaturity = dtEffective.addTenor (strTenor);

		List<CouponPeriod> lsFloatPeriods = PeriodHelper.RegularPeriodSingleReset (
			dtEffective.julian(),
			strTenor,
			null,
			4,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency,
			fri,
			null
		);

		FloatingStream floatStream = new FloatingStream (
			strCurrency,
			null,
			0.,
			-1.,
			null,
			lsFloatPeriods,
			fri,
			false
		);

		List<CouponPeriod> lsFixedPeriods = PeriodHelper.RegularPeriodSingleReset (
			dtEffective.julian(),
			strTenor,
			null,
			2,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency,
			null,
			null
		);

		FixedStream fixStream = new FixedStream (
			strCurrency,
			null,
			dblCoupon,
			1.,
			null,
			lsFixedPeriods
		);

		STIRFutureComponent stir = new STIRFutureComponent (fixStream, floatStream);

		stir.setPrimaryCode ("STIR." + dtMaturity.toString() + "." + strCurrency);

		return stir;
	}

	private static final void RunWithVolCorrSurface (
		final STIRFutureComponent stir,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel fri,
		final double dblSwapRateVolatility,
		final double dblSwapRateExchangeVolatility,
		final double dblSwapRateToSwapRateExchangeCorrelation)
		throws Exception
	{
		JulianDate dtEffective = stir.effective();

		String strComponentName = stir.name();

		mktParams.setCustomMetricVolSurface (
			CustomMetricLabel.Standard (strComponentName + "SwapRateVolatility"),
			dtEffective,
			new FlatUnivariate (dblSwapRateVolatility)
		);

		mktParams.setCustomMetricVolSurface (
			CustomMetricLabel.Standard (strComponentName + "SwapRateExchangeVolatility"),
			dtEffective,
			new FlatUnivariate (dblSwapRateExchangeVolatility)
		);

		mktParams.setCustomMetricVolSurface (
			CustomMetricLabel.Standard (strComponentName + "SwapRateToSwapRateExchangeCorrelation"),
			dtEffective,
			new FlatUnivariate (dblSwapRateToSwapRateExchangeCorrelation)
		);

		Map<String, Double> mapSTIROutput = stir.value (valParams, PricerParams.StandardAmetranoBianchetti(), mktParams, null);

		for (Map.Entry<String, Double> me : mapSTIROutput.entrySet())
			System.out.println ("\t" + me.getKey() + " => " + me.getValue());
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		String strTenor = "3M";
		String strCurrency = "EUR";
		double dblSwapRateVolatility = 0.3;
		double dblSwapRateExchangeVolatility = 0.1;
		double dblSwapRateToSwapRateExchangeCorrelation = 0.2;

		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		/*
		 * Construct the Discount Curve using its instruments and quotes
		 */

		DiscountCurve dc = MakeDC (dtToday, strCurrency);

		Map<String, ForwardCurve> mapFC = MakeFC (dtToday, strCurrency, dc);

		ForwardLabel fri = ForwardLabel.Standard (strCurrency + "-LIBOR-" + strTenor);

		STIRFutureComponent stir = CreateSTIR (dtToday.addTenor (strTenor), "5Y", fri, 0.05, strCurrency);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create
			(dc, mapFC.get (strTenor), null, null, null, null, null, null);

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strCurrency);

		RunWithVolCorrSurface (
			stir,
			valParams,
			mktParams,
			fri,
			dblSwapRateVolatility,
			dblSwapRateExchangeVolatility,
			dblSwapRateToSwapRateExchangeCorrelation);
	}
}


package org.drip.sample.sensitivity;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.*;
import org.drip.spline.stretch.MultiSegmentSequenceBuilder;

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
 * ForwardCurveReferenceBasis contains the sample demonstrating the full functionality behind creating highly
 * 	customized spline based forward curves.
 * 
 * The first sample illustrates the creation and usage of the xM-6M Tenor Basis Swap:
 * 	- Construct the 6M-xM float-float basis swap.
 * 	- Calculate the corresponding starting forward rate off of the discount curve.
 * 	- Construct the shape preserving forward curve off of Cubic Polynomial Basis Spline.
 * 	- Construct the shape preserving forward curve off of Quartic Polynomial Basis Spline.
 * 	- Construct the shape preserving forward curve off of Hyperbolic Tension Based Basis Spline.
 * 	- Set the discount curve based component market parameters.
 * 	- Set the discount curve + cubic polynomial forward curve based component market parameters.
 * 	- Set the discount curve + quartic polynomial forward curve based component market parameters.
 * 	- Set the discount curve + hyperbolic tension forward curve based component market parameters.
 * 	- Compute the following forward curve metrics for each of cubic polynomial forward, quartic
 * 		polynomial forward, and KLK Hyperbolic tension forward curves:
 * 		- Reference Basis Par Spread
 * 		- Derived Basis Par Spread
 * 	- Compare these with a) the forward rate off of the discount curve, b) The LIBOR rate, and c) The
 * 		Input Basis Swap Quote.
 * 
 * The second sample illustrates how to build and test the forward curves across various tenor basis. It
 * 	shows the following steps:
 * 	- Construct the Discount Curve using its instruments and quotes.
 * 	- Build and run the sampling for the 1M-6M Tenor Basis Swap from its instruments and quotes.
 * 	- Build and run the sampling for the 3M-6M Tenor Basis Swap from its instruments and quotes.
 * 	- Build and run the sampling for the 6M-6M Tenor Basis Swap from its instruments and quotes.
 * 	- Build and run the sampling for the 12M-6M Tenor Basis Swap from its instruments and quotes.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ForwardCurveReferenceBasis {

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

			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				"USD",
				"USD"
			);

			FloatingStream floatStream = new FloatingStream (
				"USD",
				null,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				FloatingRateIndex.Create ("USD-LIBOR-6M"),
				false
			);

			List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				"USD",
				"USD"
			);

			FixedStream fixStream = new FixedStream (
				"USD",
				null,
				0.,
				1.,
				null,
				lsFixedPeriods
			);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + dtMaturity.toString() + ".USD");

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
			4,
			strCurrency);

		double[] adblCashQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850, // Cash
			0.01612, 0.01580, 0.01589, 0.01598}; // Futures

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
			aSwapComp,
			adblSwapQuote,
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

			List<CashflowPeriod> lsReferenceFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream fsReference = new FloatingStream (
				strCurrency,
				null,
				0.,
				-1.,
				null,
				lsReferenceFloatPeriods,
				FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"),
				false
			);

			/*
			 * The Derived Leg
			 */

			List<CashflowPeriod> lsDerivedFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				12 / iTenorInMonths,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream fsDerived = new FloatingStream (
				strCurrency,
				null,
				0.,
				1.,
				null,
				lsDerivedFloatPeriods,
				FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
				false
			);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (fsReference, fsDerived);
		}

		return aFFC;
	}

	private static final void ForwardJack (
		final JulianDate dt,
		final Map<String, ForwardCurve> mapForward,
		final String strStartDateTenor)
	{
		for (Map.Entry<String, ForwardCurve> me : mapForward.entrySet())
			System.out.println (me.getKey() + " | " + strStartDateTenor + ": " +
				me.getValue().jackDForwardDManifestMeasure (
					"ReferenceParBasisSpread",
					dt.addTenor (strStartDateTenor)).displayString()
				);
	}

	private static final void ForwardJack (
		final JulianDate dt,
		final Map<String, ForwardCurve> mapForward)
	{
		ForwardJack (dt, mapForward, "1Y");

		ForwardJack (dt, mapForward, "2Y");

		ForwardJack (dt, mapForward, "3Y");

		ForwardJack (dt, mapForward, "5Y");

		ForwardJack (dt, mapForward, "7Y");
	}

	/*
	 * This sample illustrates the creation and usage of the xM-6M Tenor Basis Swap. It shows the following:
	 * 	- Construct the 6M-xM float-float basis swap.
	 * 	- Calculate the corresponding starting forward rate off of the discount curve.
	 * 	- Construct the shape preserving forward curve off of Cubic Polynomial Basis Spline.
	 * 	- Construct the shape preserving forward curve off of Quartic Polynomial Basis Spline.
	 * 	- Construct the shape preserving forward curve off of Hyperbolic Tension Based Basis Spline.
	 * 	- Set the discount curve based component market parameters.
	 * 	- Set the discount curve + cubic polynomial forward curve based component market parameters.
	 * 	- Set the discount curve + quartic polynomial forward curve based component market parameters.
	 * 	- Set the discount curve + hyperbolic tension forward curve based component market parameters.
	 * 	- Compute the following forward curve metrics for each of cubic polynomial forward, quartic
	 * 		polynomial forward, and KLK Hyperbolic tension forward curves:
	 * 		- Reference Basis Par Spread
	 * 		- Derived Basis Par Spread
	 * 	- Compare these with a) the forward rate off of the discount curve, b) The LIBOR rate, and c) The
	 * 		Input Basis Swap Quote.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final Map<String, ForwardCurve> xM6MBasisSample (
		final JulianDate dtSpot,
		final String strCurrency,
		final DiscountCurve dc,
		final int iTenorInMonths,
		final String[] astrxM6MFwdTenor,
		final double[] adblxM6MBasisSwapQuote)
		throws Exception
	{
		System.out.println ("------------------------------------------------------------");

		System.out.println (" SPL =>              n=4               |         |         |");

		System.out.println ("---------------------------------------|  LOG DF |  LIBOR  |");

		System.out.println (" MSR =>  RECALC  |  REFEREN |  DERIVED |         |         |");

		System.out.println ("------------------------------------------------------------");

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

		Map<String, ForwardCurve> mapForward = new HashMap<String, ForwardCurve>();

		/*
		 * Construct the shape preserving forward curve off of Quartic Polynomial Basis Spline.
		 */

		ForwardCurve fcxMQuartic = ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			"QUARTIC_FWD" + strBasisTenor,
			FloatingRateIndex.Create (strCurrency, "LIBOR", strBasisTenor),
			valParams,
			null,
			mktParams,
			null,
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (5),
			aFFC,
			"ReferenceParBasisSpread",
			adblxM6MBasisSwapQuote,
			dblStartingFwd);

		mapForward.put (" QUARTIC_FWD" + strBasisTenor, fcxMQuartic);

		/*
		 * Set the discount curve + quartic polynomial forward curve based component market parameters.
		 */

		CurveSurfaceQuoteSet mktParamsQuarticFwd = MarketParamsBuilder.Create
			(dc, fcxMQuartic, null, null, null, null, null, null);

		int i = 0;
		int iFreq = 12 / iTenorInMonths;

		/*
		 * Compute the following forward curve metrics for each of cubic polynomial forward, quartic
		 * 	polynomial forward, and KLK Hyperbolic tension forward curves:
		 * 	- Reference Basis Par Spread
		 * 	- Derived Basis Par Spread
		 * 
		 * Further compare these with a) the forward rate off of the discount curve, b) the LIBOR rate, and
		 * 	c) Input Basis Swap Quote.
		 */

		for (String strMaturityTenor : astrxM6MFwdTenor) {
			double dblFwdEndDate = dtSpot.addTenor (strMaturityTenor).julian();

			double dblFwdStartDate = dtSpot.addTenor (strMaturityTenor).subtractTenor (strBasisTenor).julian();

			FloatFloatComponent ffc = aFFC[i++];

			CaseInsensitiveTreeMap<Double> mapQuarticValue = ffc.value (valParams, null, mktParamsQuarticFwd, null);

			System.out.println (" " + strMaturityTenor + " =>  " +
				FormatUtil.FormatDouble (fcxMQuartic.forward (strMaturityTenor), 2, 2, 100.) + "  |  " +
				FormatUtil.FormatDouble (mapQuarticValue.get ("ReferenceParBasisSpread"), 2, 2, 1.) + "  |  " +
				FormatUtil.FormatDouble (mapQuarticValue.get ("DerivedParBasisSpread"), 2, 2, 1.) + "  |  " +
				FormatUtil.FormatDouble (iFreq * java.lang.Math.log (dc.df (dblFwdStartDate) / dc.df (dblFwdEndDate)), 1, 2, 100.) + "  |  " +
				FormatUtil.FormatDouble (dc.libor (dblFwdStartDate, dblFwdEndDate), 1, 2, 100.) + "  |  "
			);
		}

		return mapForward;
	}

	/*
	 * This sample illustrates how to build and test the forward curves across various tenor basis. It shows
	 * 	the following steps:
	 * 	- Construct the Discount Curve using its instruments and quotes.
	 * 	- Build and run the sampling for the 1M-6M Tenor Basis Swap from its instruments and quotes.
	 * 	- Build and run the sampling for the 3M-6M Tenor Basis Swap from its instruments and quotes.
	 * 	- Build and run the sampling for the 6M-6M Tenor Basis Swap from its instruments and quotes.
	 * 	- Build and run the sampling for the 12M-6M Tenor Basis Swap from its instruments and quotes.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void CustomForwardCurveBuilderSample()
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		String strCurrency = "EUR";

		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		/*
		 * Construct the Discount Curve using its instruments and quotes
		 */

		DiscountCurve dc = MakeDC (dtToday, strCurrency);

		System.out.println ("\n------------------------------------------------------------");

		System.out.println ("-------------------    1M-6M Basis Swap    -----------------");

		/*
		 * Build and run the sampling for the 1M-6M Tenor Basis Swap from its instruments and quotes.
		 */

		Map<String, ForwardCurve> mapForward1M6M = xM6MBasisSample (
			dtToday,
			strCurrency,
			dc,
			1,
			new String[] {"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
			// new String[] {"1Y"},
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

		/*
		 * Build and run the sampling for the 3M-6M Tenor Basis Swap from its instruments and quotes.
		 */

		System.out.println ("\n------------------------------------------------------------");

		System.out.println ("-------------------    3M-6M Basis Swap    -----------------");

		Map<String, ForwardCurve> mapForward3M6M = xM6MBasisSample (
			dtToday,
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

		/*
		 * Build and run the sampling for the 6M-6M Tenor Basis Swap from its instruments and quotes.
		 */

		System.out.println ("\n------------------------------------------------------------");

		System.out.println ("-------------------    6M-6M Basis Swap    -----------------");

		Map<String, ForwardCurve> mapForward6M6M = xM6MBasisSample (
			dtToday,
			strCurrency,
			dc,
			6,
			new String[] {"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
			new double[] {
				0.00000,    //  1Y
				0.00000,    //  2Y
				0.00000,    //  3Y
				0.00000,    //  4Y
				0.00000,    //  5Y
				0.00000,    //  6Y
				0.00000,    //  7Y
				0.00000,    //  8Y
				0.00000,    //  9Y
				0.00000,    // 10Y
				0.00000,    // 11Y
				0.00000,    // 12Y
				0.00000,    // 15Y
				0.00000,    // 20Y
				0.00000,    // 25Y
				0.00000     // 30Y
				}
			);

		/*
		 * Build and run the sampling for the 12M-6M Tenor Basis Swap from its instruments and quotes.
		 */

		System.out.println ("\n------------------------------------------------------------");

		System.out.println ("-------------------   12M-6M Basis Swap    -----------------");

		Map<String, ForwardCurve> mapForward12M6M = xM6MBasisSample (
			dtToday,
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

		System.out.println ("\n--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("------------------------------------------------------- 1M-6M Micro Jack -------------------------------------------------------------------");

		System.out.println ("--------------------------------------------------------------------------------------------------------------------------------------------\n");

		ForwardJack (dtToday, mapForward1M6M);

		System.out.println ("\n--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("------------------------------------------------------- 3M-6M Micro Jack -------------------------------------------------------------------");

		System.out.println ("--------------------------------------------------------------------------------------------------------------------------------------------\n");

		ForwardJack (dtToday, mapForward3M6M);

		System.out.println ("\n--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("------------------------------------------------------- 6M-6M Micro Jack -------------------------------------------------------------------");

		System.out.println ("--------------------------------------------------------------------------------------------------------------------------------------------\n");

		ForwardJack (dtToday, mapForward6M6M);

		System.out.println ("\n--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("------------------------------------------------------ 12M-6M Micro Jack -------------------------------------------------------------------");

		System.out.println ("--------------------------------------------------------------------------------------------------------------------------------------------\n");

		ForwardJack (dtToday, mapForward12M6M);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CustomForwardCurveBuilderSample();
	}
}

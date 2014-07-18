
package org.drip.sample.multicurve;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
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
 * FixFloatForwardCurve contains the sample demonstrating the full functionality behind creating highly
 * 	customized spline based forward curves from fix-float swaps and the discount curves.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatForwardCurve {

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
				4,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream floatStream = new FloatingStream (
				strCurrency,
				null,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				FloatingRateIndex.Create (strCurrency + "-LIBOR-3M"),
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
				strCurrency,
				strCurrency
			);

			FixedStream fixStream = new FixedStream (
				strCurrency,
				null,
				adblCoupon[i],
				1.,
				null,
				lsFixedPeriods
			);

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

	/*
	 * Construct an array of fix-float swaps from the fixed reference and the xM floater derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] MakeFixFloatxMSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrTenor,
		final double[] adblCoupon,
		final int iTenorInMonths)
		throws Exception
	{
		FixFloatComponent[] aFFC = new FixFloatComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {

			/*
			 * The Fixed Leg
			 */

			List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
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

			FixedStream fixStream = new FixedStream (
				strCurrency,
				null,
				adblCoupon[i],
				1.,
				null,
				lsFixedPeriods
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
				-1.,
				null,
				lsDerivedFloatPeriods,
				FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
				false
			);

			/*
			 * The fix-float swap instance
			 */

			aFFC[i] = new FixFloatComponent (fixStream, fsDerived);
		}

		return aFFC;
	}

	private static final Map<String, ForwardCurve> FixFloatxMBasisSample (
		final JulianDate dtSpot,
		final String strCurrency,
		final DiscountCurve dc,
		final int iTenorInMonths,
		final String[] astrxM6MFwdTenor,
		final String strManifestMeasure,
		final double[] adblxM6MBasisSwapQuote,
		final double[] adblSwapCoupon)
		throws Exception
	{
		System.out.println ("-----------------------------------------------------------------------------------------------------------------------------");

		System.out.println (" SPL =>              n=3              |              n=4               |              KLK               |         |         |");

		System.out.println ("--------------------------------------------------------------------------------------------------------|  LOG DF |  LIBOR  |");

		System.out.println (" MSR =>  RECALC |  REFEREN |  DERIVED |  RECALC  |  REFEREN |  DERIVED |  RECALC  |  REFEREN |  DERIVED |         |         |");

		System.out.println ("-----------------------------------------------------------------------------------------------------------------------------");

		/*
		 * Construct the 6M-xM float-float basis swap.
		 */

		FixFloatComponent[] aFFC = MakeFixFloatxMSwap (
			dtSpot,
			strCurrency,
			astrxM6MFwdTenor,
			adblSwapCoupon,
			iTenorInMonths);

		String strBasisTenor = iTenorInMonths + "M";

		ValuationParams valParams = new ValuationParams (dtSpot, dtSpot, strCurrency);

		/*
		 * Calculate the starting forward rate off of the discount curve.
		 */

		double dblStartingFwd = dc.forward (
			dtSpot.julian(),
			dtSpot.addTenor (strBasisTenor).julian()
		);

		/*
		 * Set the discount curve based component market parameters.
		 */

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (dc, null, null, null, null, null, null);

		Map<String, ForwardCurve> mapForward = new HashMap<String, ForwardCurve>();

		/*
		 * Construct the shape preserving forward curve off of Cubic Polynomial Basis Spline.
		 */

		ForwardCurve fcxMCubic = ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			"CUBIC_FWD" + strBasisTenor,
			FloatingRateIndex.Create (strCurrency, "LIBOR", strBasisTenor),
			valParams,
			null,
			mktParams,
			null,
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (4),
			aFFC,
			strManifestMeasure,
			adblxM6MBasisSwapQuote,
			dblStartingFwd);

		mapForward.put ("   CUBIC_FWD" + strBasisTenor, fcxMCubic);

		/*
		 * Set the discount curve + cubic polynomial forward curve based component market parameters.
		 */

		CurveSurfaceQuoteSet mktParamsCubicFwd = MarketParamsBuilder.Create
			(dc, fcxMCubic, null, null, null, null, null, null);

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
			strManifestMeasure,
			adblxM6MBasisSwapQuote,
			dblStartingFwd);

		mapForward.put (" QUARTIC_FWD" + strBasisTenor, fcxMQuartic);

		/*
		 * Set the discount curve + quartic polynomial forward curve based component market parameters.
		 */

		CurveSurfaceQuoteSet mktParamsQuarticFwd = MarketParamsBuilder.Create
			(dc, fcxMQuartic, null, null, null, null, null, null);

		/*
		 * Construct the shape preserving forward curve off of Hyperbolic Tension Based Basis Spline.
		 */

		ForwardCurve fcxMKLKHyper = ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			"KLKHYPER_FWD" + strBasisTenor,
			FloatingRateIndex.Create (strCurrency, "LIBOR", strBasisTenor),
			valParams,
			null,
			mktParams,
			null,
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
			new ExponentialTensionSetParams (1.),
			aFFC,
			strManifestMeasure,
			adblxM6MBasisSwapQuote,
			dblStartingFwd);

		mapForward.put ("KLKHYPER_FWD" + strBasisTenor, fcxMKLKHyper);

		/*
		 * Set the discount curve + hyperbolic tension forward curve based component market parameters.
		 */

		CurveSurfaceQuoteSet mktParamsKLKHyperFwd = MarketParamsBuilder.Create
			(dc, fcxMKLKHyper, null, null, null, null, null, null);

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

			FixFloatComponent ffc = aFFC[i++];

			CaseInsensitiveTreeMap<Double> mapCubicValue = ffc.value (valParams, null, mktParamsCubicFwd, null);

			CaseInsensitiveTreeMap<Double> mapQuarticValue = ffc.value (valParams, null, mktParamsQuarticFwd, null);

			CaseInsensitiveTreeMap<Double> mapKLKHyperValue = ffc.value (valParams, null, mktParamsKLKHyperFwd, null);

			System.out.println (" " + strMaturityTenor + " =>  " +
				FormatUtil.FormatDouble (fcxMCubic.forward (strMaturityTenor), 2, 2, 100.) + "  |  " +
				FormatUtil.FormatDouble (mapCubicValue.get ("ReferenceParBasisSpread"), 2, 2, 1.) + "  |  " +
				FormatUtil.FormatDouble (mapCubicValue.get ("DerivedParBasisSpread"), 2, 2, 1.) + "  |  " +
				FormatUtil.FormatDouble (fcxMQuartic.forward (strMaturityTenor), 2, 2, 100.) + "  |  " +
				FormatUtil.FormatDouble (mapQuarticValue.get ("ReferenceParBasisSpread"), 2, 2, 1.) + "  |  " +
				FormatUtil.FormatDouble (mapQuarticValue.get ("DerivedParBasisSpread"), 2, 2, 1.) + "  |  " +
				FormatUtil.FormatDouble (fcxMKLKHyper.forward (strMaturityTenor), 2, 2, 100.) + "  |  " +
				FormatUtil.FormatDouble (mapKLKHyperValue.get ("ReferenceParBasisSpread"), 2, 2, 1.) + "  |  " +
				FormatUtil.FormatDouble (mapKLKHyperValue.get ("DerivedParBasisSpread"), 2, 2, 1.) + "  |  " +
				FormatUtil.FormatDouble (iFreq * java.lang.Math.log (dc.df (dblFwdStartDate) / dc.df (dblFwdEndDate)), 1, 2, 100.) + "  |  " +
				FormatUtil.FormatDouble (dc.libor (dblFwdStartDate, dblFwdEndDate), 1, 2, 100.) + "  |  "
			);
		}

		return mapForward;
	}

	private static final Map<String, ForwardCurve> CustomFixFloatForwardCurveSample (
		final JulianDate dtValue,
		final String strCurrency,
		final DiscountCurve dc,
		final String strCalibMeasure,
		final int iTenorInMonths)
		throws Exception
	{
		return FixFloatxMBasisSample (
			dtValue,
			"USD",
			dc,
			iTenorInMonths,
			new java.lang.String[] {"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"},
			strCalibMeasure,
			new double[] {
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
				0.00000,    // 30Y
				0.00000,    // 40Y
				0.00000     // 50Y
			},
			new double[] {
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
				}
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

		String strCurrency = "USD";

		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		/*
		 * Construct the Discount Curve using its instruments and quotes
		 */

		DiscountCurve dc = MakeDC (dtToday, strCurrency, 0.);

		CustomFixFloatForwardCurveSample (
			dtToday,
			strCurrency,
			dc,
			"DerivedParBasisSpread",
			3);

		CustomFixFloatForwardCurveSample (
			dtToday,
			strCurrency,
			dc,
			"ReferenceParBasisSpread",
			3);
	}
}


package org.drip.sample.rates;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.rates.ForwardCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.definition.ComponentMarketParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableComponent;
import org.drip.product.definition.RatesComponent;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.ExponentialTensionSetParams;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.stretch.MultiSegmentSequenceBuilder;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * CustomForwardCurveBuilder contains the sample demonstrating the full functionality behind creating highly
 * 	customized spline based forward curves.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CustomForwardCurveBuilder {
	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures,
		final String strCurrency)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addBusDays (aiDay[i], strCurrency), strCurrency);

		CalibratableComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, strCurrency);

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	private static final CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			RatesComponent floatStream = new FloatingStream (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"),
					2, "Act/360", "Act/360", false, null, dap, dap, dap, dap, dap, dap,
						null, null, -1., strCurrency, strCurrency);

			RatesComponent fixStream = new FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
				adblCoupon[i], 2, "30/360", "30/360", false, null, dap, dap, dap, dap, dap, null, null, 100.,
					strCurrency, strCurrency);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = irs;
		}

		return aCalibComp;
	}

	private static final DiscountCurve MakeDC (
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{
		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {1, 2, 3, 7, 14, 21, 30, 60},
			4,
			strCurrency);

		double[] adblCashQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850, // Cash
			0.01612, 0.01580, 0.01589, 0.01598}; // Futures

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

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"},
			adblSwapQuote,
			strCurrency);

		return RatesScenarioCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SHAPE_TEMPLATE",
			new ValuationParams (dtSpot, dtSpot, "USD"),
			aCashComp,
			adblCashQuote,
			aSwapComp,
			adblSwapQuote,
			true);
	}

	private static final FloatFloatComponent[] MakexM6MBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			FloatingStream fsVisible = new FloatingStream (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"),
					2, "Act/360", "Act/360", false, null, dap, dap, dap, dap, dap, dap,
						null, null, -1., strCurrency, strCurrency);

			FloatingStream fsWorkout = new FloatingStream (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
					12 / iTenorInMonths, "Act/360", "Act/360", false, null, dap, dap, dap, dap, dap, dap,
						null, null, 1., strCurrency, strCurrency);

			aFFC[i] = new FloatFloatComponent (fsVisible, fsWorkout);
		}

		return aFFC;
	}

	private static final void TestxM6MBasis (
		final JulianDate dtSpot,
		final String strCurrency,
		final DiscountCurve dc,
		final int iTenorInMonths,
		final String[] astrxM6MFwdTenor,
		final double[] adblxM6MBasisSwapQuote)
		throws Exception
	{
		System.out.println ("\t------------------------------------------------------------------------------------------");

		System.out.println ("\t YR  =>    n=3    |    n=4    |    KLK    |    FWD    |   LIBOR   |  VISIBLE  |  WORKOUT  ");

		System.out.println ("\t------------------------------------------------------------------------------------------");

		FloatFloatComponent[] aFFC = MakexM6MBasisSwap (dtSpot, strCurrency, astrxM6MFwdTenor, iTenorInMonths);

		String strBasisTenor = iTenorInMonths + "M";

		ValuationParams valParams = new ValuationParams (dtSpot, dtSpot, strCurrency);

		double dblStartingFwd = dc.forward (dtSpot.getJulian(), dtSpot.addTenor (strBasisTenor).getJulian());

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null);

		ForwardCurve fcxMCubic = RatesScenarioCurveBuilder.ShapePreservingForwardCurve (
			"CUBIC_FWD" + strBasisTenor,
			strBasisTenor,
			valParams,
			null,
			cmp,
			null,
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (4),
			aFFC,
			adblxM6MBasisSwapQuote,
			dblStartingFwd);

		ForwardCurve fcxMQuintic = RatesScenarioCurveBuilder.ShapePreservingForwardCurve (
			"QUINTIC_FWD" + strBasisTenor,
			strBasisTenor,
			valParams,
			null,
			cmp,
			null,
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (5),
			aFFC,
			adblxM6MBasisSwapQuote,
			dblStartingFwd);

		ForwardCurve fcxMKLKHyperbolic = RatesScenarioCurveBuilder.ShapePreservingForwardCurve (
			"CUBIC_FWD" + strBasisTenor,
			strBasisTenor,
			valParams,
			null,
			cmp,
			null,
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
			new ExponentialTensionSetParams (1.),
			aFFC,
			adblxM6MBasisSwapQuote,
			dblStartingFwd);

		int i = 0;
		int iFreq = 12 / iTenorInMonths;

		for (String strMaturityTenor : astrxM6MFwdTenor) {
			double dblFwdEndDate = dtSpot.addTenor (strMaturityTenor).getJulian();

			double dblFwdStartDate = dtSpot.addTenor (strMaturityTenor).subtractTenor (strBasisTenor).getJulian();

			CaseInsensitiveTreeMap<Double> mapValue = aFFC[i++].value (valParams, null, cmp, null);

			System.out.println ("\t" + strMaturityTenor + "   =>   " +
				FormatUtil.FormatDouble (fcxMCubic.forward (strMaturityTenor), 1, 2, 100.) + "   |   " +
				FormatUtil.FormatDouble (fcxMQuintic.forward (strMaturityTenor), 1, 2, 100.) + "   |   " +
				FormatUtil.FormatDouble (fcxMKLKHyperbolic.forward (strMaturityTenor), 1, 2, 100.) + "   |   " +
				FormatUtil.FormatDouble (iFreq * java.lang.Math.log (dc.df (dblFwdStartDate) / dc.df (dblFwdEndDate)), 1, 2, 100.) + "   |   " +
				FormatUtil.FormatDouble (dc.libor (dblFwdStartDate, dblFwdEndDate), 1, 2, 100.) + "   |   " +
				FormatUtil.FormatDouble (mapValue.get ("VisibleParBasisSpread"), 1, 2, 1.) + "   |   " +
				FormatUtil.FormatDouble (mapValue.get ("WorkoutParBasisSpread"), 1, 2, 1.));
		}
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		String strCurrency = "EUR";

		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", strCurrency);

		DiscountCurve dc = MakeDC (dtToday, strCurrency);

		System.out.println ("\n\t------------------------------------------------------------------------------------------");

		System.out.println ("\t----------    1M-6M Basis Swap    --------------------------------------------------------");

		TestxM6MBasis (
			dtToday,
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

		System.out.println ("\n\t------------------------------------------------------------------------------------------");

		System.out.println ("\t----------    3M-6M Basis Swap    --------------------------------------------------------");

		TestxM6MBasis (
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

		System.out.println ("\n\t------------------------------------------------------------------------------------------");

		System.out.println ("\t----------   6M-12M Basis Swap    --------------------------------------------------------");

		TestxM6MBasis (
			dtToday,
			strCurrency,
			dc,
			12,
			new String[] {"1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
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
				-0.00022     // 30Y
				}
			);

		System.out.println ("\n\t------------------------------------------------------------------------------------------");

		System.out.println ("\t----------    6M-6M Basis Swap    --------------------------------------------------------");

		TestxM6MBasis (
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
	}
}

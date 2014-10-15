
package org.drip.sample.fra;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.fra.FRAStandardCapFloor;
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
 * FRAStdCapFloorVolAnalysis contains an analysis if the correlation and volatility impact on a Cap/Floor of
 * 	the standard FRA.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FRAStdCapFloorVolAnalysis {

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

			GenericStream floatStream = new GenericStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrTenor[i],
					Double.NaN,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					4,
					"Act/360",
					false,
					"Act/360",
					false,
					false,
					strCurrency,
					-1.,
					null,
					0.,
					strCurrency,
					strCurrency,
					ForwardLabel.Standard (strCurrency + "-LIBOR-3M"),
					null
				)
			);

			GenericStream fixStream = new GenericStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrTenor[i],
					Double.NaN,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					2,
					"Act/360",
					false,
					"Act/360",
					false,
					false,
					strCurrency,
					1.,
					null,
					adblCoupon[i],
					strCurrency,
					strCurrency,
					null,
					null
				)
			);

			GenericFixFloatComponent irs = new GenericFixFloatComponent (
				fixStream,
				floatStream,
				new CashSettleParams (0, strCurrency, 0)
			);

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

			GenericStream fsReference = new GenericStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrTenor[i],
					Double.NaN,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					2,
					"Act/360",
					false,
					"Act/360",
					false,
					false,
					strCurrency,
					-1.,
					null,
					0.,
					strCurrency,
					strCurrency,
					ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
					null
				)
			);

			/*
			 * The Derived Leg
			 */

			GenericStream fsDerived = new GenericStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrTenor[i],
					Double.NaN,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					12 / iTenorInMonths,
					"Act/360",
					false,
					"Act/360",
					false,
					false,
					strCurrency,
					1.,
					null,
					0.,
					strCurrency,
					strCurrency,
					ForwardLabel.Standard (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
					null
				)
			);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (
				fsReference,
				fsDerived,
				new CashSettleParams (0, strCurrency, 0)
			);
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

	private static final void SetVolCorrSurface (
		final GenericStream floatstream,
		final CurveSurfaceQuoteSet mktParams,
		final ForwardLabel fri,
		final double dblForwardVol,
		final double dblFundingVol,
		final double dblForwardFundingCorr)
		throws Exception
	{
		FundingLabel fundingLabel = FundingLabel.Standard (fri.currency());

		mktParams.setForwardCurveVolSurface (
			fri,
			new FlatUnivariate (dblForwardVol)
		);

		mktParams.setFundingCurveVolSurface (
			fundingLabel,
			new FlatUnivariate (dblFundingVol)
		);

		mktParams.setForwardFundingCorrSurface (
			fri,
			fundingLabel,
			new FlatUnivariate (dblForwardFundingCorr)
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

		double dblStrike = 0.05;
		String strTenor = "3M";
		String strCurrency = "EUR";
		String strManifestMeasure = "QuantoAdjustedParForward";

		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		/*
		 * Construct the Discount Curve using its instruments and quotes
		 */

		DiscountCurve dc = MakeDC (dtToday, strCurrency);

		Map<String, ForwardCurve> mapFC = MakeFC (dtToday, strCurrency, dc);

		ForwardLabel fri = ForwardLabel.Standard (strCurrency + "-LIBOR-" + strTenor);

		JulianDate dtEffective = dtToday.addTenor (strTenor);

		GenericStream floatStream = new GenericStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				"5Y",
				Double.NaN,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				4,
				"Act/360",
				false,
				"Act/360",
				false,
				false,
				strCurrency,
				1.,
				null,
				0.,
				strCurrency,
				strCurrency,
				fri,
				null
			)
		);

		FRAStandardCapFloor fraCap = new FRAStandardCapFloor (
			new GenericSingleStreamComponent ("FRA_CAP", floatStream),
			strManifestMeasure,
			true,
			dblStrike,
			1.,
			"Act/360",
			strCurrency
		);

		FRAStandardCapFloor fraFloor = new FRAStandardCapFloor (
			new GenericSingleStreamComponent ("FRA_FLOOR", floatStream),
			strManifestMeasure,
			false,
			dblStrike,
			1.,
			"Act/360",
			strCurrency
		);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (dc, mapFC.get (strTenor), null, null, null, null, null, null);

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strCurrency);

		double[] adblSigmaFwd = new double[] {0.1, 0.2, 0.3, 0.4, 0.5};
		double[] adblSigmaFwd2DomX = new double[] {0.10, 0.15, 0.20, 0.25, 0.30};
		double[] adblCorrFwdFwd2DomX = new double[] {-0.99, -0.50, 0.00, 0.50, 0.99};

		System.out.println ("\tPrinting the Cap/Floor Output in Order (Left -> Right):");

		System.out.println ("\t\tCap Price");

		System.out.println ("\t\tFloor Price");

		System.out.println ("\t-------------------------------------------------------------");

		System.out.println ("\t-------------------------------------------------------------");

		for (double dblSigmaFwd : adblSigmaFwd) {
			for (double dblSigmaFwd2DomX : adblSigmaFwd2DomX) {
				for (double dblCorrFwdFwd2DomX : adblCorrFwdFwd2DomX) {
					SetVolCorrSurface (
						floatStream,
						mktParams,
						fri,
						dblSigmaFwd,
						dblSigmaFwd2DomX,
						dblCorrFwdFwd2DomX);

					Map<String, Double> mapFRACapOutput = fraCap.value (valParams, null, mktParams, null);

					Map<String, Double> mapFRAFloorOutput = fraFloor.value (valParams, null, mktParams, null);

					System.out.println ("\t[" +
						org.drip.quant.common.FormatUtil.FormatDouble (dblSigmaFwd, 2, 0, 100.) + "%," +
						org.drip.quant.common.FormatUtil.FormatDouble (dblSigmaFwd2DomX, 2, 0, 100.) + "%," +
						org.drip.quant.common.FormatUtil.FormatDouble (dblCorrFwdFwd2DomX, 2, 0, 100.) + "%] =" +
						org.drip.quant.common.FormatUtil.FormatDouble (mapFRACapOutput.get ("Price"), 1, 2, 1.) + " | " +
						org.drip.quant.common.FormatUtil.FormatDouble (mapFRAFloorOutput.get ("Price"), 1, 2, 1.));
				}
			}
		}
	}
}

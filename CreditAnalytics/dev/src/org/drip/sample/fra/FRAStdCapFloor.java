
package org.drip.sample.fra;

import java.util.*;

import org.drip.analytics.date.DateUtil;
import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.period.*;
import org.drip.param.valuation.*;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.fra.FRAStandardCapFloor;
import org.drip.product.option.LastTradingDateSetting;
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
 * FRAStdCapFloor demonstrates the creation, invocation, usage, and valuation of the FRA Cap/Floor.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FRAStdCapFloor {

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
				ForwardLabel.Create (strCurrency, "3M")
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

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrMaturityTenor.length];

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
			"6M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			ForwardLabel.Create (strCurrency, "6M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		CompositePeriodSetting cpsFloating = new CompositePeriodSetting (
			2,
			"6M",
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

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenor (astrMaturityTenor[i]);

			ComposableFixedUnitSetting cfusFixed = new ComposableFixedUnitSetting (
				"6M",
				CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
				null,
				adblCoupon[i],
				0.,
				strCurrency
			);

			List<Double> lsFixedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				"6M",
				astrMaturityTenor[i],
				null
			);

			List<Double> lsFloatingStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				"6M",
				astrMaturityTenor[i],
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
		 * Construct the array of Deposit instruments and their quotes.
		 */

		CalibratableFixedIncomeComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {
				1, 2, 3, 7, 14, 21, 30, 60
			},
			0,
			strCurrency
		);

		double[] adblDepositQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850
		};

		String[] astrDepositManifestMeasure = new String[] {
			"ForwardRate",
			"ForwardRate",
			"ForwardRate",
			"ForwardRate",
			"ForwardRate",
			"ForwardRate",
			"ForwardRate",
			"ForwardRate"
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
			new java.lang.String[] {
				"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"
			},
			adblSwapQuote,
			strCurrency
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
			false
		);
	}

	/*
	 * Construct an array of float-float swaps from the corresponding reference (6M) and the derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FloatFloatComponent[] MakexM6MBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrMaturityTenor,
		final int iTenorInMonths)
		throws Exception
	{
		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrMaturityTenor.length];

		ComposableFloatingUnitSetting cfusReference = new ComposableFloatingUnitSetting (
			"6M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			ForwardLabel.Create (strCurrency, "6M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		CompositePeriodSetting cpsReference = new CompositePeriodSetting (
			2,
			"6M",
			strCurrency,
			null,
			-1.,
			null,
			null,
			null,
			null
		);

		ComposableFloatingUnitSetting cfusDerived = new ComposableFloatingUnitSetting (
			iTenorInMonths + "M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR,
			null,
			ForwardLabel.Create (strCurrency, iTenorInMonths + "M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		CompositePeriodSetting cpsDerived = new CompositePeriodSetting (
			12 / iTenorInMonths,
			iTenorInMonths + "M",
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

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			List<Double> lsReferenceStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				"6M",
				astrMaturityTenor[i],
				null
			);

			List<Double> lsDerivedStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
				dtEffective,
				iTenorInMonths + "M",
				astrMaturityTenor[i],
				null
			);

			Stream referenceStream = new Stream (
				CompositePeriodBuilder.FloatingCompositeUnit (
					lsReferenceStreamEdgeDate,
					cpsReference,
					cfusReference
				)
			);

			Stream derivedStream = new Stream (
				CompositePeriodBuilder.FloatingCompositeUnit (
					lsDerivedStreamEdgeDate,
					cpsDerived,
					cfusDerived
				)
			);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (
				referenceStream,
				derivedStream,
				csp
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

		FloatFloatComponent[] aFFC = MakexM6MBasisSwap (
			dtSpot,
			strCurrency,
			astrxM6MFwdTenor,
			iTenorInMonths
		);

		String strBasisTenor = iTenorInMonths + "M";

		ValuationParams valParams = new ValuationParams (
			dtSpot,
			dtSpot,
			strCurrency
		);

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

		/*
		 * Construct the shape preserving forward curve off of Quartic Polynomial Basis Spline.
		 */

		return ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			"QUARTIC_FWD" + strBasisTenor,
			ForwardLabel.Create (strCurrency, strBasisTenor),
			valParams,
			null,
			mktParams,
			null,
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (5),
			aFFC,
			"DerivedParBasisSpread",
			adblxM6MBasisSwapQuote,
			dblStartingFwd
		);
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

		double dblStrike = 0.02;
		String strTenor = "3M";
		String strCurrency = "EUR";
		String strManifestMeasure = "ParForward";

		JulianDate dtToday = DateUtil.Today().addTenorAndAdjust (
			"0D",
			strCurrency
		);

		/*
		 * Construct the Discount Curve using its instruments and quotes
		 */

		DiscountCurve dc = MakeDC (dtToday, strCurrency);

		Map<String, ForwardCurve> mapFC = MakeFC (dtToday, strCurrency, dc);

		ForwardLabel fri = ForwardLabel.Create (strCurrency, strTenor);

		JulianDate dtEffective = dtToday.addTenor (strTenor);

		ComposableFloatingUnitSetting cfus = new ComposableFloatingUnitSetting (
			strTenor,
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			fri,
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		CompositePeriodSetting cps = new CompositePeriodSetting (
			4,
			strTenor,
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

		Stream floatStream = new Stream (
			CompositePeriodBuilder.FloatingCompositeUnit (
				CompositePeriodBuilder.EdgePair (
					dtEffective,
					dtEffective.addTenorAndAdjust (strTenor, strCurrency)
				),
				cps,
				cfus
			)
		);

		FRAStandardCapFloor fraCap = new FRAStandardCapFloor (
			new SingleStreamComponent ("FRA_CAP", floatStream, csp),
			strManifestMeasure,
			true,
			dblStrike,
			1.,
			new LastTradingDateSetting (LastTradingDateSetting.MID_CURVE_OPTION_QUARTERLY, "", Double.NaN),
			"Act/360",
			strCurrency
		);

		FRAStandardCapFloor fraFloor = new FRAStandardCapFloor (
			new SingleStreamComponent ("FRA_FLOOR", floatStream, csp),
			strManifestMeasure,
			false,
			dblStrike,
			1.,
			new LastTradingDateSetting (LastTradingDateSetting.MID_CURVE_OPTION_QUARTERLY, "", Double.NaN),
			"Act/360",
			strCurrency
		);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (dc, mapFC.get (strTenor), null, null, null, null, null, null);

		double dblForwardVol = 0.50;
		double dblFundingVol = 0.50;
		double dblForwardFundingCorr = 0.50;

		SetVolCorrSurface (
			mktParams,
			fri,
			dblForwardVol,
			dblFundingVol,
			dblForwardFundingCorr
		);

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strCurrency);

		Map<String, Double> mapFRACapOutput = fraCap.value (valParams, null, mktParams, null);

		for (Map.Entry<String, Double> me : mapFRACapOutput.entrySet())
			System.out.println ("\t" + me.getKey() + " => " + me.getValue());

		System.out.println ("\t-------------------------------------------------------------");

		System.out.println ("\t-------------------------------------------------------------");

		Map<String, Double> mapFRAFloorOutput = fraFloor.value (valParams, null, mktParams, null);

		for (Map.Entry<String, Double> me : mapFRAFloorOutput.entrySet())
			System.out.println ("\t" + me.getKey() + " => " + me.getValue());
	}
}

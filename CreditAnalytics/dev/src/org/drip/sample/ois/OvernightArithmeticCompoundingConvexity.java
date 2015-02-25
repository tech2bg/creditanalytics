
package org.drip.sample.ois;

import java.util.*;

import org.drip.analytics.cashflow.CompositePeriod;
import org.drip.analytics.date.*;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.function.deterministic1D.*;
import org.drip.market.otc.*;
import org.drip.param.creator.*;
import org.drip.param.market.*;
import org.drip.param.period.*;
import org.drip.param.valuation.*;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.estimator.LatentStateStretchBuilder;
import org.drip.state.identifier.*;
import org.drip.state.inference.*;

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
 * OvernightArithmeticCompoundingConvexity contains an assessment of the impact of the Overnight Index
 *  Volatility, the Funding Numeraire Volatility, and the ON Index/Funding Correlation on the Overnight
 *  Floating Stream.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OvernightArithmeticCompoundingConvexity {

	private static final FixFloatComponent OTCOISFixFloat (
		final JulianDate dtSpot,
		final String strCurrency,
		final String strMaturityTenor,
		final double dblCoupon)
	{
		FixedFloatSwapConvention ffConv = OvernightFixedFloatContainer.ConventionFromJurisdiction (
			strCurrency
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
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final SingleStreamComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final String strCurrency,
		final int[] aiDay)
		throws Exception
	{
		SingleStreamComponent[] aDeposit = new SingleStreamComponent[aiDay.length];

		for (int i = 0; i < aiDay.length; ++i)
			aDeposit[i] = SingleStreamComponentBuilder.Deposit (
				dtEffective,
				dtEffective.addBusDays (
					aiDay[i],
					strCurrency
				),
				ForwardLabel.Create (strCurrency, "ON")
			);

		return aDeposit;
	}

	private static final FixFloatComponent[] OISFromMaturityTenor (
		final JulianDate dtSpot,
		final String strCurrency,
		final String[] astrMaturityTenor,
		final double[] adblCoupon)
		throws Exception
	{
		FixFloatComponent[] aOIS = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aOIS[i] = OTCOISFixFloat (
				dtSpot,
				strCurrency,
				astrMaturityTenor[i],
				adblCoupon[i]
			);

		return aOIS;
	}

	private static final FixFloatComponent[] OISFuturesFromMaturityTenor (
		final JulianDate dtSpot,
		final String strCurrency,
		final String[] astrStartTenor,
		final String[] astrMaturityTenor,
		final double[] adblCoupon)
		throws Exception
	{
		FixFloatComponent[] aOISFutures = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aOISFutures[i] = OTCOISFixFloat (
				dtSpot.addTenor (astrStartTenor[i]),
				strCurrency,
				astrMaturityTenor[i],
				adblCoupon[i]
			);

		return aOISFutures;
	}

	private static final DiscountCurve CustomOISCurveBuilderSample (
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{
		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		SingleStreamComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			strCurrency,
			new int[] {
				1, 2, 3
			}
		);

		double[] adblDepositQuote = new double[] {
			0.0004, 0.0004, 0.0004		 // Deposit
		};

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec depositStretch = LatentStateStretchBuilder.ForwardFundingStretchSpec (
			"   DEPOSIT   ",
			aDepositComp,
			"ForwardRate",
			adblDepositQuote
		);

		/*
		 * Construct the Array of Short End OIS Instruments and their Quotes from the given set of parameters
		 */

		double[] adblShortEndOISQuote = new double[] {
			0.00070,    //   1W
			0.00069,    //   2W
			0.00078,    //   3W
			0.00074     //   1M
		};

		CalibratableFixedIncomeComponent[] aShortEndOISComp = OISFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"1W", "2W", "3W", "1M"
			},
			adblShortEndOISQuote
		);

		/*
		 * Construct the Short End OIS Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisShortEndStretch = LatentStateStretchBuilder.ForwardFundingStretchSpec (
			"SHORT END OIS",
			aShortEndOISComp,
			"SwapRate",
			adblShortEndOISQuote
		);

		/*
		 * Construct the Array of OIS Futures Instruments and their Quotes from the given set of parameters
		 */

		double[] adblOISFutureQuote = new double[] {
			 0.00046,    //   1M x 1M
			 0.00016,    //   2M x 1M
			-0.00007,    //   3M x 1M
			-0.00013,    //   4M x 1M
			-0.00014     //   5M x 1M
		};

		CalibratableFixedIncomeComponent[] aOISFutureComp = OISFuturesFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"1M", "2M", "3M", "4M", "5M"
			},
			new java.lang.String[] {
				"1M", "1M", "1M", "1M", "1M"
			},
			adblOISFutureQuote
		);

		/*
		 * Construct the OIS Future Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisFutureStretch = LatentStateStretchBuilder.ForwardFundingStretchSpec (
			" OIS FUTURE  ",
			aOISFutureComp,
			"SwapRate",
			adblOISFutureQuote
		);

		/*
		 * Construct the Array of Long End OIS Instruments and their Quotes from the given set of parameters
		 */

		double[] adblLongEndOISQuote = new double[] {
			0.00002,    //  15M
			0.00008,    //  18M
			0.00021,    //  21M
			0.00036,    //   2Y
			0.00127,    //   3Y
			0.00274,    //   4Y
			0.00456,    //   5Y
			0.00647,    //   6Y
			0.00827,    //   7Y
			0.00996,    //   8Y
			0.01147,    //   9Y
			0.01280,    //  10Y
			0.01404,    //  11Y
			0.01516,    //  12Y
			0.01764,    //  15Y
			0.01939,    //  20Y
			0.02003,    //  25Y
			0.02038     //  30Y
		};

		CalibratableFixedIncomeComponent[] aLongEndOISComp = OISFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"
			},
			adblLongEndOISQuote
		);

		/*
		 * Construct the Long End OIS Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisLongEndStretch = LatentStateStretchBuilder.ForwardFundingStretchSpec (
			"LONG END OIS ",
			aLongEndOISComp,
			"SwapRate",
			adblLongEndOISQuote
		);

		LatentStateStretchSpec[] aStretchSpec = new LatentStateStretchSpec[] {
			depositStretch,
			oisShortEndStretch,
			oisFutureStretch,
			oisLongEndStretch
		};

		/*
		 * Set up the Linear Curve Calibrator using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LinearLatentStateCalibrator lcc = new LinearLatentStateCalibrator (
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null
		);

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Cash and Swap Stretches.
		 */

		return ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aStretchSpec,
			new ValuationParams (dtSpot, dtSpot, strCurrency),
			null,
			null,
			null,
			1.
		);
	}

	private static final LatentStateFixingsContainer SetFlatOvernightFixings (
		final JulianDate dtStart,
		final JulianDate dtEnd,
		final JulianDate dtValue,
		final ForwardLabel fri,
		final double dblFlatFixing,
		final double dblNotional)
		throws Exception
	{
		LatentStateFixingsContainer lsfc = new LatentStateFixingsContainer();

		JulianDate dt = dtStart.addDays (1);

		while (dt.julian() <= dtEnd.julian()) {
			lsfc.add (dt, fri, dblFlatFixing);

			dt = dt.addBusDays (1, "USD");
		}

		return lsfc;
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final String strCurrency,
		final ForwardLabel fri,
		final double dblOISVol,
		final double dblUSDFundingVol,
		final double dblUSDFundingUSDOISCorrelation)
		throws Exception
	{
		FundingLabel fundingLabel = FundingLabel.Standard (strCurrency);

		mktParams.setFundingCurveVolSurface (fundingLabel, new FlatUnivariate (dblUSDFundingVol));

		mktParams.setForwardCurveVolSurface (fri, new FlatUnivariate (dblOISVol));

		mktParams.setForwardFundingCorrSurface (fri, fundingLabel, new FlatUnivariate (dblUSDFundingUSDOISCorrelation));
	}

	private static final void VolCorrScenario (
		final Stream[] aFloatStream,
		final String strCurrency,
		final ForwardLabel fri,
		final double dblAccrualEndDate,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final double dblOISVol,
		final double dblUSDFundingVol,
		final double dblUSDFundingUSDOISCorrelation)
		throws Exception
	{
		SetMarketParams (
			mktParams,
			strCurrency,
			fri,
			dblOISVol,
			dblUSDFundingVol,
			dblUSDFundingUSDOISCorrelation
		);

		String strDump = "\t[" +
			FormatUtil.FormatDouble (dblOISVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSDFundingVol, 2, 0, 100.) + "%," +
			FormatUtil.FormatDouble (dblUSDFundingUSDOISCorrelation, 2, 0, 100.) + "%] = ";

		for (int i = 0; i < aFloatStream.length; ++i) {
			Map<String, Double> mapValue = aFloatStream[i].value (
				valParams,
				null,
				mktParams,
				null
			);

			if (0 != i) strDump += " || ";

			strDump +=
				FormatUtil.FormatDouble (mapValue.get ("UnadjustedFairPremium"), 1, 4, 100.) + "% | " +
				FormatUtil.FormatDouble (mapValue.get ("CompoundingAdjustmentFactor") - 1, 1, 2, 100.) + "% | " +
				FormatUtil.FormatDouble (mapValue.get ("CumulativeConvexityAdjustmentFactor") - 1, 1, 2, 100.) + "%";
		}

		System.out.println (strDump);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = DateUtil.Today().addTenor ("0D");

		String strCurrency = "USD";

		DiscountCurve dc = CustomOISCurveBuilderSample (
			dtToday,
			strCurrency
		);

		JulianDate dtCustomOISStart = dtToday.subtractTenor ("2M");

		JulianDate dtCustomOISMaturity = dtToday.addTenor ("4M");

		ForwardLabel fri = ForwardLabel.Create (strCurrency, "ON");

		ComposableFloatingUnitSetting cfusFloating = new ComposableFloatingUnitSetting (
			"ON",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_OVERNIGHT,
			null,
			ForwardLabel.Create (strCurrency, "ON"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		CompositePeriodSetting cpsFloating = new CompositePeriodSetting (
			360,
			"ON",
			strCurrency,
			null,
			-1.,
			null,
			null,
			null,
			null
		);

		List<Double> lsFloatingStreamEdgeDate = CompositePeriodBuilder.RegularEdgeDates (
			dtCustomOISStart,
			"6M",
			"6M",
			null
		);

		List<CompositePeriod> lsCP = CompositePeriodBuilder.FloatingCompositeUnit (
			lsFloatingStreamEdgeDate,
			cpsFloating,
			cfusFloating
		);

		Stream floatStream = new Stream (lsCP);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (
			dc,
			null,
			null,
			null,
			null,
			null,
			SetFlatOvernightFixings (
				dtCustomOISStart,
				dtCustomOISMaturity,
				dtToday,
				fri,
				0.003,
				-1.
			)
		);

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strCurrency);

		CompositePeriod period = lsCP.get (0);

		double[] adblOISVol = new double [] {0.1, 0.3, 0.5};
		double[] adblUSDFundingVol = new double [] {0.1, 0.3, 0.5};
		double[] adblUSDFundingUSDOISCorrelation = new double [] {-0.3, 0.0, 0.3};

		System.out.println ("\n\t-------------------------------------------------------------------------------------");

		System.out.println ("\tInput Order (LHS) L->R:");

		System.out.println ("\t\tOIS Volatility, Funding Volatility, OIS/Funding Correlation\n");

		System.out.println ("\tOutput Order (RHS) L->R:");

		System.out.println ("\t\tUnadjusted Fair Premium, Compounding Adjustment Factor (% - Relative), Convexity Adjustment Factor (% - Relative)\n");

		System.out.println ("\t-------------------------------------------------------------------------------------");

		for (double dblOISVol : adblOISVol) {
			for (double dblUSDFundingVol : adblUSDFundingVol) {
				for (double dblUSDFundingUSDOISCorrelation : adblUSDFundingUSDOISCorrelation)
					VolCorrScenario (
						new Stream[] {floatStream},
						strCurrency,
						fri,
						period.endDate(),
						valParams,
						mktParams,
						dblOISVol,
						dblUSDFundingVol,
						dblUSDFundingUSDOISCorrelation
					);
			}
		}

		System.out.println ("\t-------------------------------------------------------------------------------------");
	}
}

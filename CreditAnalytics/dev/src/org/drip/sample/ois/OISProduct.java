
package org.drip.sample.ois;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.calib.*;
import org.drip.product.cashflow.*;
import org.drip.product.creator.*;
import org.drip.product.rates.*;
import org.drip.quant.common.*;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.identifier.*;
import org.drip.state.inference.*;
import org.drip.state.representation.LatentStateSpecification;

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
 * OISProduct demonstrates the Construction, Usage, Coupon Extraction and Measure Generation for an OIS
 * 	Product Sample.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OISProduct {

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final DepositComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final String strCurrency)
		throws Exception
	{
		DepositComponent[] aDeposit = new DepositComponent[aiDay.length];

		for (int i = 0; i < aiDay.length; ++i)
			aDeposit[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				strCurrency
			);

		return aDeposit;
	}

	private static final LatentStateStretchSpec DepositStretch (
		final DepositComponent[] aDeposit,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aDeposit.length];

		String strCurrency = aDeposit[0].couponCurrency()[0];

		for (int i = 0; i < aDeposit.length; ++i) {
			DepositComponentQuoteSet depositQuote = new DepositComponentQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						DiscountCurve.LATENT_STATE_DISCOUNT,
						DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						FundingLabel.Standard (strCurrency)
					)
				}
			);

			depositQuote.setRate (adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aDeposit[i],
				depositQuote
			);
		}

		return new LatentStateStretchSpec (
			"DEPOSIT",
			aSegmentSpec
		);
	}

	/*
	 * Construct the Array of Overnight Index Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final IRSComponent[] OvernightIndexFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		IRSComponent[] aOIS = new IRSComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
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
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				false
			);

			List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
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

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + astrMaturityTenor[i] + "." + strCurrency);

			aOIS[i] = ois;
		}

		return aOIS;
	}

	/*
	 * Construct the Array of Overnight Fund Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final IRSComponent[] OvernightFundFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		IRSComponent[] aOIS = new IRSComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GenerateDailyPeriod (
				dtEffective.julian(),
				dtEffective.addTenor (astrMaturityTenor[i]).julian(),
				null,
				null,
				"Act/360",
				strCurrency,
				strCurrency
			);

			FloatingStream floatStream = new FloatingStream (
				strCurrency,
				null,
				adblCoupon[i],
				-1.,
				null,
				lsFloatPeriods,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				false
			);

			List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
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

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + astrMaturityTenor[i] + "." + strCurrency);

			aOIS[i] = ois;
		}

		return aOIS;
	}

	/*
	 * Construct the Array of Overnight Index Future Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final IRSComponent[] OvernightIndexFutureFromMaturityTenor (
		final JulianDate dtSpot,
		final String[] astrStartTenor,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		IRSComponent[] aOIS = new IRSComponent[astrStartTenor.length];

		for (int i = 0; i < astrStartTenor.length; ++i) {
			JulianDate dtEffective = dtSpot.addTenor (astrStartTenor[i]);

			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
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
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				false
			);

			List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
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

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + astrMaturityTenor[i] + "." + strCurrency);

			aOIS[i] = ois;
		}

		return aOIS;
	}

	/*
	 * Construct the Array of Overnight Fund Future Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final IRSComponent[] OvernightFundFutureFromMaturityTenor (
		final JulianDate dtSpot,
		final String[] astrStartTenor,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		IRSComponent[] aOIS = new IRSComponent[astrStartTenor.length];

		for (int i = 0; i < astrStartTenor.length; ++i) {
			JulianDate dtEffective = dtSpot.addTenor (astrStartTenor[i]);

			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GenerateDailyPeriod (
				dtEffective.julian(),
				dtEffective.addTenor (astrMaturityTenor[i]).julian(),
				null,
				null,
				"Act/360",
				strCurrency,
				strCurrency
			);

			FloatingStream floatStream = new FloatingStream (
				strCurrency,
				null,
				adblCoupon[i],
				-1.,
				null,
				lsFloatPeriods,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				false
			);

			List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrMaturityTenor[i],
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

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + astrMaturityTenor[i] + "." + strCurrency);

			aOIS[i] = ois;
		}

		return aOIS;
	}

	private static final LatentStateStretchSpec OISStretch (
		final String strName,
		final IRSComponent[] aOIS,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aOIS.length];

		String strCurrency = aOIS[0].couponCurrency()[0];

		for (int i = 0; i < aOIS.length; ++i) {
			FixFloatQuoteSet oisQuote = new FixFloatQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						DiscountCurve.LATENT_STATE_DISCOUNT,
						DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						FundingLabel.Standard (strCurrency)
					),
					new LatentStateSpecification (
						ForwardCurve.LATENT_STATE_FORWARD,
						ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
						aOIS[i].forwardLabel()[0]
					)
				}
			);

			oisQuote.setSwapRate (adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aOIS[i],
				oisQuote
			);
		}

		return new LatentStateStretchSpec (
			strName,
			aSegmentSpec
		);
	}


	private static final DiscountCurve CustomOISCurveBuilderSample (
		final JulianDate dtToday,
		final String strHeaderComment,
		final String strCurrency,
		final boolean bOvernightIndex)
		throws Exception
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     " + strHeaderComment);

		System.out.println ("\t----------------------------------------------------------------");

		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		DepositComponent[] aDeposit = DepositInstrumentsFromMaturityDays (
			dtToday,
			new int[] {
				1, 2, 3
			},
			strCurrency
		);

		double[] adblDepositQuote = new double[] {
			0.0004, 0.0004, 0.0004		 // Deposit
		};

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec depositStretch = DepositStretch (
			aDeposit,
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

		IRSComponent[] aShortEndOISComp = bOvernightIndex ?
			OvernightIndexFromMaturityTenor (
				dtToday,
				new java.lang.String[]
					{"1W", "2W", "3W", "1M"},
				adblShortEndOISQuote,
				strCurrency) :
			OvernightFundFromMaturityTenor (
				dtToday,
				new java.lang.String[]
					{"1W", "2W", "3W", "1M"},
				adblShortEndOISQuote,
				strCurrency);

		/*
		 * Construct the Short End OIS Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisShortEndStretch = OISStretch (
			"SHORT_END_OIS",
			aShortEndOISComp,
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

		IRSComponent[] aOISFutureComp = bOvernightIndex ?
			OvernightIndexFutureFromMaturityTenor (
				dtToday,
				new java.lang.String[] {"1M", "2M", "3M", "4M", "5M"},
				new java.lang.String[] {"1M", "1M", "1M", "1M", "1M"},
				adblOISFutureQuote,
				strCurrency) :
			OvernightFundFutureFromMaturityTenor (
				dtToday,
				new java.lang.String[] {"1M", "2M", "3M", "4M", "5M"},
				new java.lang.String[] {"1M", "1M", "1M", "1M", "1M"},
				adblOISFutureQuote,
				strCurrency);

		/*
		 * Construct the OIS Future Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisFutureStretch = OISStretch (
			"OIS_FUTURE",
			aOISFutureComp,
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

		IRSComponent[] aLongEndOISComp = bOvernightIndex ?
			OvernightIndexFromMaturityTenor (
				dtToday,
				new java.lang.String[]
					{"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
				adblLongEndOISQuote,
				strCurrency) :
			OvernightFundFromMaturityTenor (
				dtToday,
				new java.lang.String[]
					{"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
				adblLongEndOISQuote,
				strCurrency);


		/*
		 * Construct the Long End OIS Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisLongEndStretch = OISStretch (
			"LONG_END_OIS",
			aLongEndOISComp,
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
			null);

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Deposit and Swap Stretches.
		 */

		DiscountCurve dc = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aStretchSpec,
			new ValuationParams (dtToday, dtToday, strCurrency),
			null,
			null,
			null,
			1.
		);

		/*
		 * Cross-Comparison of the Deposit Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDeposit.length; ++i)
			System.out.println ("\t[" + aDeposit[i].effective() + " => " + aDeposit[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aDeposit[i].measureValue (new ValuationParams (dtToday, dtToday, strCurrency), null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the Short End OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS SHORT END INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aShortEndOISComp.length; ++i)
			System.out.println ("\t[" + aShortEndOISComp[i].effective() + " => " + aShortEndOISComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aShortEndOISComp[i].measureValue (new ValuationParams (dtToday, dtToday, strCurrency), null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblShortEndOISQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (aShortEndOISComp[i].measureValue (new ValuationParams (dtToday, dtToday, strCurrency), null,
								MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
									null, "FairPremium"), 1, 6, 1.));

		/*
		 * Cross-Comparison of the OIS Future Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS FUTURE INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aOISFutureComp.length; ++i)
			System.out.println ("\t[" + aOISFutureComp[i].effective() + " => " + aOISFutureComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aOISFutureComp[i].measureValue (new ValuationParams (dtToday, dtToday, strCurrency), null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "SwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblOISFutureQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (aOISFutureComp[i].measureValue (new ValuationParams (dtToday, dtToday, strCurrency), null,
								MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
									null, "FairPremium"), 1, 6, 1.));

		/*
		 * Cross-Comparison of the Long End OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS LONG END INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aLongEndOISComp.length; ++i)
			System.out.println ("\t[" + aLongEndOISComp[i].effective() + " => " + aLongEndOISComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aLongEndOISComp[i].measureValue (new ValuationParams (dtToday, dtToday, strCurrency), null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblLongEndOISQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (aLongEndOISComp[i].measureValue (new ValuationParams (dtToday, dtToday, strCurrency), null,
								MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
									null, "FairPremium"), 1, 6, 1.));

		return dc;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		String strCurrency = "EUR";

		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		DiscountCurve dcIndex = CustomOISCurveBuilderSample (
			dtToday,
			"---- DISCOUNT CURVE WITH OVERNIGHT INDEX ---",
			strCurrency,
			true);

		DiscountCurve dcFund = CustomOISCurveBuilderSample (
			dtToday,
			"---- DISCOUNT CURVE WITH OVERNIGHT FUND ---",
			strCurrency,
			false);

		JulianDate dtCustomOISStart = dtToday.subtractTenor ("2M");

		List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtCustomOISStart.julian(),
			"4M",
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
			OvernightFRIBuilder.JurisdictionFRI (strCurrency),
			false
		);

		List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtCustomOISStart.julian(),
			"4M",
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
			0.003,
			1.,
			null,
			lsFixedPeriods
		);

		IRSComponent ois = new IRSComponent (fixStream, floatStream);

		CurveSurfaceQuoteSet mktParamsIndex = MarketParamsBuilder.Create (dcIndex, null, null, null, null, null, null);

		CurveSurfaceQuoteSet mktParamsFund = MarketParamsBuilder.Create (dcFund, null, null, null, null, null, null);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------\n");

		Map<String, Double> mapOISOutputIndex = ois.value (
			new ValuationParams (dtToday, dtToday, strCurrency),
			null,
			mktParamsIndex,
			null);

		Map<String, Double> mapOISOutputFund = ois.value (
			new ValuationParams (dtToday, dtToday, strCurrency),
			null,
			mktParamsFund,
			null);

		for (Map.Entry<String, Double> me : mapOISOutputIndex.entrySet()) {
			String strKey = me.getKey();

			double dblIndexMeasure = me.getValue();

			double dblFundMeasure = mapOISOutputFund.get (strKey);

			String strReconcile = NumberUtil.WithinTolerance (dblIndexMeasure, dblFundMeasure, 1.e-08, 1.e-04) ?
				"RECONCILES" :
				"DOES NOT RECONCILE";

			System.out.println ("\t" +
				FormatUtil.FormatDouble (dblIndexMeasure, 1, 8, 1.) + " | " +
				FormatUtil.FormatDouble (dblFundMeasure, 1, 8, 1.) + " | " +
				strReconcile + " <= " + strKey);
		}
	}
}

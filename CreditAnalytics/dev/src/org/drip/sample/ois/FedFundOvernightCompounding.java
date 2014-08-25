
package org.drip.sample.ois;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.output.CouponPeriodMetrics;
import org.drip.analytics.period.CouponPeriod;
import org.drip.analytics.period.ResetPeriodContainer;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.PeriodHelper;
import org.drip.param.creator.*;
import org.drip.param.market.*;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.calib.*;
import org.drip.product.cashflow.*;
import org.drip.product.creator.*;
import org.drip.product.rates.*;
import org.drip.quant.function1D.*;
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
 * FedFundOvernightCompounding demonstrates in detail the methodology behind the overnight compounding used
 * 	in the Overnight fund Floating Stream Accrual.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FedFundOvernightCompounding {

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

	private static final FixFloatComponent[] OvernightIndexFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		FixFloatComponent[] aOIS = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			List<CouponPeriod> lsFloatPeriods = PeriodHelper.RegularPeriodSingleReset (
				dtEffective.julian(),
				astrMaturityTenor[i],
				null,
				4,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				null
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

			List<CouponPeriod> lsFixedPeriods = PeriodHelper.RegularPeriodSingleReset (
				dtEffective.julian(),
				astrMaturityTenor[i],
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

			FixFloatComponent ois = new FixFloatComponent (fixStream, floatStream);

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

	private static final FixFloatComponent[] OvernightIndexFutureFromMaturityTenor (
		final JulianDate dtSpot,
		final String[] astrStartTenor,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		FixFloatComponent[] aOIS = new FixFloatComponent[astrStartTenor.length];

		for (int i = 0; i < astrStartTenor.length; ++i) {
			JulianDate dtEffective = dtSpot.addTenor (astrStartTenor[i]);

			List<CouponPeriod> lsFloatPeriods = PeriodHelper.RegularPeriodSingleReset (
				dtEffective.julian(),
				astrMaturityTenor[i],
				null,
				4,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				null
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

			List<CouponPeriod> lsFixedPeriods = PeriodHelper.RegularPeriodSingleReset (
				dtEffective.julian(),
				astrMaturityTenor[i],
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

			FixFloatComponent ois = new FixFloatComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + astrMaturityTenor[i] + "." + strCurrency);

			aOIS[i] = ois;
		}

		return aOIS;
	}

	private static final LatentStateStretchSpec OISStretch (
		final String strName,
		final FixFloatComponent[] aOIS,
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
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{
		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		DepositComponent[] aDeposit = DepositInstrumentsFromMaturityDays (
			dtSpot,
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

		FixFloatComponent[] aShortEndOISComp = OvernightIndexFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {
				"1W", "2W", "3W", "1M"
			},
			adblShortEndOISQuote,
			strCurrency
		);

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

		FixFloatComponent[] aOISFutureComp = OvernightIndexFutureFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {
				"1M", "2M", "3M", "4M", "5M"
			},
			new java.lang.String[] {
				"1M", "1M", "1M", "1M", "1M"
			},
			adblOISFutureQuote,
			strCurrency
		);

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

		FixFloatComponent[] aLongEndOISComp = OvernightIndexFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {
				"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"
			},
			adblLongEndOISQuote,
			strCurrency
		);

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

		double dblAccount = 1.;

		lsfc.add (dtStart, fri, dblFlatFixing);

		double dblPrevDate = dtStart.julian();

		JulianDate dt = dtStart.addDays (1);

		while (dt.julian() <= dtEnd.julian()) {
			lsfc.add (dt, fri, dblFlatFixing);

			if (dt.julian() <= dtValue.julian()) {
				double dblAccrualFraction = Convention.YearFraction (dblPrevDate, dt.julian(), "Act/360", false, Double.NaN, null, "USD");

				dblAccount *= (1. + dblFlatFixing * dblAccrualFraction);
			}

			dblPrevDate = dt.julian();

			dt = dt.addBusDays (1, "USD");
		}

		System.out.println ("\tManual Calc Float Accrued (Geometric Compounding): " + (dblAccount - 1.) * dblNotional);

		double dblDCF = (dtValue.julian() - dtStart.julian()) / 360.;

		System.out.println ("\tManual Calc Float Accrued (Arithmetic Compounding): " +
			(dblDCF * dblNotional * dblFlatFixing));

		System.out.println ("\tManual Calc Float Accrued DCF (Arithmetic Compounding): " + dblDCF);

		return lsfc;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		String strCurrency = "USD";

		DiscountCurve dc = CustomOISCurveBuilderSample (
			dtToday,
			strCurrency);

		JulianDate dtCustomOISStart = dtToday.subtractTenor ("2M");

		JulianDate dtCustomOISMaturity = dtToday.addTenor ("4M");

		ForwardLabel fri = OvernightFRIBuilder.JurisdictionFRI (strCurrency);

		FundingLabel fundingLabel = FundingLabel.Standard (strCurrency);

		List<CouponPeriod> lsFloatPeriods = PeriodHelper.RegularPeriodDailyReset (
			dtCustomOISStart.julian(),
			"6M",
			null,
			2,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency,
			ResetPeriodContainer.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC,
			OvernightFRIBuilder.JurisdictionFRI (strCurrency),
			null
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

		List<CouponPeriod> lsFixedPeriods = PeriodHelper.RegularPeriodSingleReset (
			dtCustomOISStart.julian(),
			"4M",
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
			0.,
			1.,
			null,
			lsFixedPeriods
		);

		FixFloatComponent ois = new FixFloatComponent (fixStream, floatStream);

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
				-1.)
			);

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strCurrency);

		Map<String, Double> mapOISOutput = ois.value (
			valParams,
			null,
			mktParams,
			null);

		System.out.println ("\tMachine Calc Float Accrued (Geometric Compounding): " + mapOISOutput.get ("FloatAccrued"));

		fri.setArithmeticCompounding (true);

		mapOISOutput = ois.value (
			new ValuationParams (dtToday, dtToday, strCurrency),
			null,
			mktParams,
			null);

		System.out.println ("\tMachine Calc Float Accrued (Arithmetic Compounding): " + mapOISOutput.get ("FloatAccrued"));

		CouponPeriod period = lsFloatPeriods.get (0);

		CouponPeriodMetrics pcm = floatStream.coupon (
			period.endDate(),
			valParams,
			mktParams
		);

		System.out.println ("\tMachine Calc Float Accrued DCF (Arithmetic Compounding): " + pcm.dcf());

		System.out.println ("\tPeriod #1 Coupon Without Convexity Adjustment: " + pcm.convexityAdjustedAccrualRate());

		double dblOISVol = 0.3;
		double dblUSDFundingVol = 0.3;
		double dblUSDFundingUSDOISCorrelation = 0.3;

		mktParams.setFundingCurveVolSurface (fundingLabel, new FlatUnivariate (dblUSDFundingVol));

		mktParams.setForwardCurveVolSurface (fri, new FlatUnivariate (dblOISVol));

		mktParams.setForwardFundingCorrSurface (fri, fundingLabel, new FlatUnivariate (dblUSDFundingUSDOISCorrelation));

		System.out.println (
			"\tPeriod #1 Coupon With Convexity Adjustment: " + floatStream.coupon (
				period.endDate(),
				valParams,
				mktParams
			).convexityAdjustedAccrualRate()
		);
	}
}


package org.drip.sample.sensitivity;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.creator.*;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.ois.*;
import org.drip.product.rates.*;
import org.drip.quant.calculus.WengertJacobian;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.ExponentialTensionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.estimator.*;

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
 * OISCurveQuoteSensitivity demonstrates the calculation of the OIS discount curve sensitivity to the
 * 	calibration instrument quotes. It does the following:
 * 	- Construct the Array of Cash/OIS Instruments and their Quotes from the given set of parameters.
 * 	- Construct the Cash/OIS Instrument Set Stretch Builder.
 * 	- Set up the Linear Curve Calibrator using the following parameters:
 * 		- Cubic Exponential Mixture Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Construct the Shape Preserving OIS Discount Curve by applying the linear curve calibrator to the array
 * 		of Cash and OIS Stretches.
 * 	- Cross-Comparison of the Cash/OIS Calibration Instrument "Rate" metric across the different curve
 * 		construction methodologies.
 * 	- Display of the Cash Instrument Discount Factor Quote Jacobian Sensitivities.
 * 	- Display of the OIS Instrument Discount Factor Quote Jacobian Sensitivities.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OISCurveQuoteSensitivity {

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aDepositComp = new CalibratableFixedIncomeComponent[aiDay.length];

		for (int i = 0; i < aiDay.length; ++i)
			aDepositComp[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				null,
				strCurrency);

		return aDepositComp;
	}

	/*
	 * Make an OIS Swap from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final IRSComponent MakeOIS (
		final JulianDate dtEffective,
		final String strTenor,
		final double dblCoupon,
		final String strCurrency)
		throws Exception
	{
		JulianDate dtMaturity = dtEffective.addTenor (strTenor);

		List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtEffective.julian(),
			strTenor,
			null,
			4,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		OvernightIndexFloatingStream floatStream = new OvernightIndexFloatingStream (
			strCurrency,
			0.,
			-1.,
			null,
			lsFloatPeriods,
			OvernightFRIBuilder.JurisdictionFRI (strCurrency),
			false
		);

		List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtEffective.julian(),
			strTenor,
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
			dblCoupon,
			1.,
			null,
			lsFixedPeriods
		);

		IRSComponent ois = new IRSComponent (fixStream, floatStream);

		ois.setPrimaryCode ("OIS." + dtMaturity.toString() + "." + strCurrency);

		return ois;
	}

	/*
	 * Construct the Array of Overnight Index Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] OvernightIndexFromMaturityTenor (
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

			OvernightIndexFloatingStream floatStream = new OvernightIndexFloatingStream (
				strCurrency,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
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
				adblCoupon[i],
				1.,
				null,
				lsFixedPeriods
			);

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = ois;
		}

		return aCalibComp;
	}

	/*
	 * Construct the Array of Overnight Fund Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] OvernightFundFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenor (astrTenor[i]);

			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GenerateDailyPeriod (
				dtEffective.julian(),
				dtMaturity.julian(),
				null,
				null,
				"Act/360",
				strCurrency,
				strCurrency
			);

			OvernightFundFloatingStream floatStream = new OvernightFundFloatingStream (
				strCurrency,
				adblCoupon[i],
				-1.,
				null,
				lsFloatPeriods,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
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
				adblCoupon[i],
				1.,
				null,
				lsFixedPeriods
			);

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = ois;
		}

		return aCalibComp;
	}

	/*
	 * Construct the Array of Overnight Index Future Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] OvernightIndexFutureFromMaturityTenor (
		final JulianDate dtSpot,
		final String[] astrStartTenor,
		final String[] astrTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrStartTenor.length];

		for (int i = 0; i < astrStartTenor.length; ++i) {
			JulianDate dtEffective = dtSpot.addTenor (astrStartTenor[i]);

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

			OvernightIndexFloatingStream floatStream = new OvernightIndexFloatingStream (
				strCurrency,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
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
				adblCoupon[i],
				1.,
				null,
				lsFixedPeriods
			);

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = ois;
		}

		return aCalibComp;
	}

	/*
	 * Construct the Array of Overnight Fund Future Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] OvernightFundFutureFromMaturityTenor (
		final JulianDate dtSpot,
		final String[] astrStartTenor,
		final String[] astrTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrStartTenor.length];

		for (int i = 0; i < astrStartTenor.length; ++i) {
			JulianDate dtEffective = dtSpot.addTenor (astrStartTenor[i]);

			JulianDate dtMaturity = dtEffective.addTenor (astrTenor[i]);

			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GenerateDailyPeriod (
				dtEffective.julian(),
				dtMaturity.julian(),
				null,
				null,
				"Act/360",
				strCurrency,
				strCurrency
			);

			OvernightFundFloatingStream floatStream = new OvernightFundFloatingStream (
				strCurrency,
				adblCoupon[i],
				-1.,
				null,
				lsFloatPeriods,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
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
				adblCoupon[i],
				1.,
				null,
				lsFixedPeriods
			);

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = ois;
		}

		return aCalibComp;
	}

	private static final void TenorJack (
		final JulianDate dtStart,
		final String strTenor,
		final DiscountCurve dc,
		final String strCurrency)
		throws Exception
	{
		IRSComponent oisBespoke = MakeOIS (
			dtStart,
			strTenor,
			0.01,
			strCurrency);

		WengertJacobian wjDFQuoteBespokeMat = dc.jackDDFDManifestMeasure (oisBespoke.maturity(), "Rate");

		System.out.println (strTenor + " => " + wjDFQuoteBespokeMat.displayString());
	}

	private static final void Forward6MRateJack (
		final JulianDate dtStart,
		final String strStartTenor,
		final DiscountCurve dc,
		final String strCurrency)
	{
		JulianDate dtBegin = dtStart.addTenor (strStartTenor);

		WengertJacobian wjForwardRate = dc.jackDForwardDManifestMeasure (dtBegin, "6M", "Rate", 0.5);

		System.out.println ("[" + dtBegin + " | 6M] => " + wjForwardRate.displayString());
	}

	/*
	 * This sample demonstrates the calculation of the discount curve sensitivity to the calibration
	 * 	instrument quotes. It does the following:
	 * 	- Construct the Array of Cash/OIS Instruments and their Quotes from the given set of parameters.
	 * 	- Construct the Cash/OIS Instrument Set Stretch Builder.
	 * 	- Set up the Linear Curve Calibrator using the following parameters:
	 * 		- Cubic Exponential Mixture Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
	 * 		of Cash and OIS Stretches.
	 * 	- Cross-Comparison of the Cash/OIS Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Display of the Cash Instrument Discount Factor Quote Jacobian Sensitivities.
	 * 	- Display of the OIS Instrument Discount Factor Quote Jacobian Sensitivities.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void OISCurveQuoteSensitivitySample (
		final JulianDate dtSpot,
		final String strHeaderComment,
		final String strCurrency,
		final boolean bOvernightIndex)
		throws Exception
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t" + strHeaderComment);

		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		CalibratableFixedIncomeComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {1, 2, 3},
			strCurrency
		);

		double[] adblDepositQuote = new double[] {
			0.0004, 0.0004, 0.0004		 // Deposit
		};

		/*
		 * Setup the Deposit instruments stretch latent state representation - this uses the discount factor
		 * 	quantification metric and the "rate" manifest measure.
		 */

		StretchRepresentationSpec srsDeposit = StretchRepresentationSpec.CreateStretchBuilderSet (
			"DEPOSITS",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aDepositComp,
			"Rate",
			adblDepositQuote,
			null);

		/*
		 * Construct the Array of Short End OIS Instruments and their Quotes from the given set of parameters
		 */

		double[] adblShortEndOISQuote = new double[] {
			0.00070,    //   1W
			0.00069,    //   2W
			0.00078,    //   3W
			0.00074     //   1M
		};

		CalibratableFixedIncomeComponent[] aShortEndOISComp = bOvernightIndex ?
			OvernightIndexFromMaturityTenor (
				dtSpot,
				new java.lang.String[]
					{"1W", "2W", "3W", "1M"},
				adblShortEndOISQuote,
				strCurrency) :
			OvernightFundFromMaturityTenor (
				dtSpot,
				new java.lang.String[]
					{"1W", "2W", "3W", "1M"},
				adblShortEndOISQuote,
				strCurrency);

		/*
		 * Construct the Short End OIS Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsShortEndOIS = StretchRepresentationSpec.CreateStretchBuilderSet (
			"OISSHORT",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aShortEndOISComp,
			"Rate",
			adblShortEndOISQuote,
			null);

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

		CalibratableFixedIncomeComponent[] aOISFutureComp = bOvernightIndex ?
			OvernightIndexFutureFromMaturityTenor (
				dtSpot,
				new java.lang.String[] {"1M", "2M", "3M", "4M", "5M"},
				new java.lang.String[] {"1M", "1M", "1M", "1M", "1M"},
				adblOISFutureQuote,
				"EUR") :
			OvernightFundFutureFromMaturityTenor (
				dtSpot,
				new java.lang.String[] {"1M", "2M", "3M", "4M", "5M"},
				new java.lang.String[] {"1M", "1M", "1M", "1M", "1M"},
				adblOISFutureQuote,
				"EUR");

		/*
		 * Construct the OIS Future Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsOISFuture = StretchRepresentationSpec.CreateStretchBuilderSet (
			"OIS FUTURE",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aOISFutureComp,
			"Rate",
			adblOISFutureQuote,
			null);

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

		CalibratableFixedIncomeComponent[] aLongEndOISComp = bOvernightIndex ?
			OvernightIndexFromMaturityTenor (
				dtSpot,
				new java.lang.String[]
					{"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
				adblLongEndOISQuote,
				strCurrency) :
			OvernightFundFromMaturityTenor (
				dtSpot,
				new java.lang.String[]
					{"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
				adblLongEndOISQuote,
				strCurrency);

		/*
		 * Construct the Long End OIS Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsLongEndOIS = StretchRepresentationSpec.CreateStretchBuilderSet (
			"OIS LONG",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aLongEndOISComp,
			"Rate",
			adblLongEndOISQuote,
			null);

		StretchRepresentationSpec[] aSRS = new StretchRepresentationSpec[] {
			srsDeposit,
			srsShortEndOIS,
			srsOISFuture,
			srsLongEndOIS
		};

		/*
		 * Set up the Linear Curve Calibrator using the following Default Segment Control parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null)),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		/*
		 * Set up the DEPOSIT Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			srsDeposit.getName(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null)));

		/*
		 * Set up the Short End OIS Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			srsShortEndOIS.getName(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null)));

		/*
		 * Set up the Long End OIS Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			srsLongEndOIS.getName(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null)));

		ValuationParams valParams = new ValuationParams (dtSpot, dtSpot, strCurrency);

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of DEPOSIT, OIS SHORT END, and OIS LONG END Stretches.
		 */

		DiscountCurve dc = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aSRS,
			valParams,
			null,
			null,
			null,
			1.);

		/*
		 * Cross-Comparison of the DEPOSIT Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i)
			System.out.println ("\t[" + aDepositComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aDepositComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the Short End OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SHORT END OIS INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aShortEndOISComp.length; ++i)
			System.out.println ("\t[" + aShortEndOISComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aShortEndOISComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblShortEndOISQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the OIS Future Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS FUTURE INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aOISFutureComp.length; ++i)
			System.out.println ("\t[" + aOISFutureComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aOISFutureComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblOISFutureQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the Long End OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     LONG END OIS INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aLongEndOISComp.length; ++i)
			System.out.println ("\t[" + aLongEndOISComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aLongEndOISComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblLongEndOISQuote[i], 1, 6, 1.));

		/*
		 * Display of the DEPOSIT Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wj = dc.jackDDFDManifestMeasure (aDepositComp[i].maturity(), "Rate");

			System.out.println (aDepositComp[i].maturity() + " => " + wj.displayString());
		}

		/*
		 * Display of the Short End OIS Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SHORT END OIS MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aShortEndOISComp.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wjDFQuote = dc.jackDDFDManifestMeasure (aShortEndOISComp[i].maturity(), "Rate");

			System.out.println (aShortEndOISComp[i].maturity() + " => " + wjDFQuote.displayString());
		}

		/*
		 * Display of the OIS Future Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS FUTURE MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aOISFutureComp.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wjDFQuote = dc.jackDDFDManifestMeasure (aOISFutureComp[i].maturity(), "Rate");

			System.out.println (aOISFutureComp[i].maturity() + " => " + wjDFQuote.displayString());
		}

		/*
		 * Display of the Long End OIS Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     LONG END OIS MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aLongEndOISComp.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wjDFQuote = dc.jackDDFDManifestMeasure (aLongEndOISComp[i].maturity(), "Rate");

			System.out.println (aLongEndOISComp[i].maturity() + " => " + wjDFQuote.displayString());
		}

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     COMPONENT-BY-COMPONENT QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		WengertJacobian wj = dc.compJackDPVDManifestMeasure (dtSpot);

		System.out.println (wj.displayString());

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     BESPOKE 35Y OIS QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		IRSComponent ois35Y = MakeOIS (
			dtSpot,
			"35Y",
			0.01,
			strCurrency);

		WengertJacobian wjOISBespokeQuoteJack = ois35Y.jackDDirtyPVDManifestMeasure (
			valParams,
			null,
			MarketParamsBuilder.Create (dc, null, null, null, null, null, null, null),
			null);

		System.out.println (wjOISBespokeQuoteJack.displayString());

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     BESPOKE OIS MATURITY QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		TenorJack (dtSpot, "30Y", dc, strCurrency);

		TenorJack (dtSpot, "32Y", dc, strCurrency);

		TenorJack (dtSpot, "34Y", dc, strCurrency);

		TenorJack (dtSpot, "36Y", dc, strCurrency);

		TenorJack (dtSpot, "38Y", dc, strCurrency);

		TenorJack (dtSpot, "40Y", dc, strCurrency);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS CURVE IMPLIED 6M FORWARD RATE QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		Forward6MRateJack (dtSpot, "1D", dc, strCurrency);

		Forward6MRateJack (dtSpot, "3M", dc, strCurrency);

		Forward6MRateJack (dtSpot, "6M", dc, strCurrency);

		Forward6MRateJack (dtSpot, "1Y", dc, strCurrency);

		Forward6MRateJack (dtSpot, "2Y", dc, strCurrency);

		Forward6MRateJack (dtSpot, "5Y", dc, strCurrency);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtSpot = JulianDate.Today();

		OISCurveQuoteSensitivitySample (
			dtSpot,
			"---- DISCOUNT CURVE WITH OVERNIGHT INDEX ---",
			"EUR",
			true);

		OISCurveQuoteSensitivitySample (
			dtSpot,
			"---- DISCOUNT CURVE WITH OVERNIGHT FUND ---",
			"EUR",
			false);
	}
}


package org.drip.sample.ois;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.creator.*;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.ois.*;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.*;
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
 * ShapeOISZeroLocalSmooth demonstrates the usage of different local smoothing techniques involved in the
 * 	discount curve creation. It shows the following:
 * 	- Construct the Array of Deposit/OIS Instruments and their Quotes from the given set of parameters.
 * 	- Construct the Deposit/OIS Instrument Set Stretch Builder.
 * 	- Set up the Linear Curve Calibrator using the following parameters:
 * 		- Cubic Exponential Mixture Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Akima Local Curve Control parameters as follows:
 * 		- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Harmonic Local Curve Control parameters as follows:
 * 		- C1 Harmonic Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Hyman 1983 Local Curve Control parameters as follows:
 * 		- C1 Hyman 1983 Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Hyman 1989 Local Curve Control parameters as follows:
 * 		- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Huynh-Le Floch Delimited Local Curve Control parameters as follows:
 * 		- C1 Huynh-Le Floch Delimited Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Kruger Local Curve Control parameters as follows:
 * 		- C1 Kruger Monotone Smoothener with spurious extrema elimination and monotone filtering applied
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Construct the Shape Preserving OIS Discount Curve by applying the linear curve calibrator to the array
 * 		of Deposit and OIS Stretches.
 * 	- Construct the Akima Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
 * 		the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Construct the Harmonic Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Construct the Hyman 1983 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Construct the Hyman 1989 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Construct the Huynh-Le Floch Delimiter Locally Smoothened OIS Discount Curve by applying the linear
 * 		curve calibrator and the Local Curve Control parameters to the array of Deposit and OIS Stretches
 * 		and the shape preserving discount curve.
 * 	- Construct the Kruger Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
 * 		the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
 * 		preserving discount curve.
 * 	- Cross-Comparison of the Deposit/OIS Calibration Instrument "Rate" metric across the different curve
 * 		construction methodologies.
 *  - Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
 *  	construction methodologies for a sequence of bespoke swap instruments.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ShapeOISZeroLocalSmooth {

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
				dtEffective.getJulian(),
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
				dtEffective.getJulian(),
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
				dtEffective.getJulian(),
				dtMaturity.getJulian(),
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
				dtEffective.getJulian(),
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
				dtEffective.getJulian(),
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
				dtEffective.getJulian(),
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
				dtEffective.getJulian(),
				dtMaturity.getJulian(),
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
				dtEffective.getJulian(),
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
	 * This sample demonstrates the usage of different local smoothing techniques involved in the OIS discount
	 * 	curve creation. It shows the following:
	 * 	- Construct the Array of Deposit/OIS Instruments and their Quotes from the given set of parameters.
	 * 	- Construct the Deposit/OIS Instrument Set Stretch Builder.
	 * 	- Set up the Linear Curve Calibrator using the following parameters:
	 * 		- Cubic Exponential Mixture Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Akima Local Curve Control parameters as follows:
	 * 		- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Harmonic Local Curve Control parameters as follows:
	 * 		- C1 Harmonic Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Hyman 1983 Local Curve Control parameters as follows:
	 * 		- C1 Hyman 1983 Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Hyman 1989 Local Curve Control parameters as follows:
	 * 		- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Huynh-Le Floch Delimited Local Curve Control parameters as follows:
	 * 		- C1 Huynh-Le Floch Delimited Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Kruger Local Curve Control parameters as follows:
	 * 		- C1 Kruger Monotone Smoothener with spurious extrema elimination and monotone filtering applied
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Construct the Shape Preserving OIS Discount Curve by applying the linear curve calibrator to the array
	 * 		of Deposit and OIS Stretches.
	 * 	- Construct the Akima Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
	 * 		the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Construct the Harmonic Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
	 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Construct the Hyman 1983 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
	 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Construct the Hyman 1989 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
	 * 		and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Construct the Huynh-Le Floch Delimiter Locally Smoothened OIS Discount Curve by applying the linear
	 * 		curve calibrator and the Local Curve Control parameters to the array of Deposit and OIS Stretches
	 * 		and the shape preserving discount curve.
	 * 	- Construct the Kruger Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
	 * 		the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
	 * 		preserving discount curve.
	 * 	- Cross-Comparison of the Deposit/OIS Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 *  - Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
	 *  	construction methodologies for a sequence of bespoke OIS instruments.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void ShapeOISDFZeroLocalSmoothSample (
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
		 * Set up the Linear Curve Calibrator using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_EXPONENTIAL_MIXTURE,
				new ExponentialMixtureSetParams (new double[] {0.01, 0.05, 0.25}),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		/*
		 * Set up the Akima Local Curve Control parameters as follows:
		 * 	- C1 Akima Monotone Smoothener with spurious extrema elimination and monotone filtering applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpAkima = new LocalControlCurveParams (
			org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_AKIMA,
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true);

		/*
		 * Set up the Harmonic Local Curve Control parameters as follows:
		 * 	- C1 Harmonic Monotone Smoothener with spurious extrema elimination and monotone filtering
		 * 		applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpHarmonic = new LocalControlCurveParams (
			org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_HARMONIC,
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true);

		/*
		 * Set up the Hyman 1983 Local Curve Control parameters as follows:
		 * 	- C1 Hyman 1983 Monotone Smoothener with spurious extrema elimination and monotone filtering
		 * 		applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpHyman83 = new LocalControlCurveParams (
			org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_HYMAN83,
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true);

		/*
		 * Set up the Hyman 1989 Local Curve Control parameters as follows:
		 * 	- C1 Hyman 1989 Monotone Smoothener with spurious extrema elimination and monotone filtering
		 * 		applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpHyman89 = new LocalControlCurveParams (
			org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_HYMAN89,
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true);

		/*
		 * Set up the Huynh-LeFloch Limiter Local Curve Control parameters as follows:
		 * 	- C1 Huynh-LeFloch Limiter Monotone Smoothener with spurious extrema elimination and monotone
		 * 		filtering applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpHuynhLeFloch = new LocalControlCurveParams (
			org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_HUYNH_LE_FLOCH,
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true);

		/*
		 * Set up the Kruger Local Curve Control parameters as follows:
		 * 	- C1 Kruger Monotone Smoothener with spurious extrema elimination and monotone filtering applied
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccpKruger = new LocalControlCurveParams (
			org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_KRUGER,
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			MultiSegmentSequence.CALIBRATE,
			null,
			null,
			true,
			true);

		ValuationParams valParams = new ValuationParams (dtSpot, dtSpot, strCurrency);

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Deposit and OIS Stretches.
		 */

		DiscountCurve dcShapePreserving = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aSRS,
			valParams,
			null,
			null,
			null,
			1.);

		/*
		 * Construct the Akima Locally Smoothened Discount Curve by applying the linear curve calibrator and
		 * 	the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalAkima = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpAkima,
			aSRS,
			valParams,
			null,
			null,
			null);

		/*
		 * Construct the Harmonic Locally Smoothened Discount Curve by applying the linear curve calibrator
		 * 	and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalHarmonic = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpHarmonic,
			aSRS,
			valParams,
			null,
			null,
			null);

		/*
		 * Construct the Hyman 1983 Locally Smoothened Discount Curve by applying the linear curve calibrator
		 * 	and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalHyman83 = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpHyman83,
			aSRS,
			valParams,
			null,
			null,
			null);

		/*
		 * Construct the Hyman 1989 Locally Smoothened OIS Discount Curve by applying the linear curve calibrator
		 * 	and the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalHyman89 = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpHyman89,
			aSRS,
			valParams,
			null,
			null,
			null);

		/*
		 * Construct the Huynh-Le Floch delimited Locally Smoothened OIS Discount Curve by applying the linear
		 * 	curve calibrator and the Local Curve Control parameters to the array of Deposit and OIS Stretches
		 * 	and the shape preserving discount curve.
		 */

		DiscountCurve dcLocalHuynhLeFloch = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpHuynhLeFloch,
			aSRS,
			valParams,
			null,
			null,
			null);

		/*
		 * Construct the Kruger Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and
		 *  the Local Curve Control parameters to the array of Deposit and OIS Stretches and the shape
		 * 	preserving discount curve.
		 */

		DiscountCurve dcLocalKruger = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccpKruger,
			aSRS,
			valParams,
			null,
			null,
			null);

		/*
		 * Cross-Comparison of the Deposit Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t-------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                DEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83 | LOCAL HYMAN89 | LOCAL HUYNHLF | LOCAL KRUGER  |  INPUT QUOTE  ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i)
			System.out.println ("\t[" + aDepositComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (
					aDepositComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDepositComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDepositComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aDepositComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDepositComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDepositComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aDepositComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.)
			);

		/*
		 * Cross-Comparison of the Short End OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                 SHORT END OIS INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83 | LOCAL HYMAN89 | LOCAL HUYNHLF | LOCAL KRUGER  |  INPUT QUOTE  ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aShortEndOISComp.length; ++i)
			System.out.println ("\t[" + aShortEndOISComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aShortEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblShortEndOISQuote[i], 1, 6, 1.)
			);

		/*
		 * Cross-Comparison of the OIS Future Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                 OIS FUTURE INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83 | LOCAL HYMAN89 | LOCAL HUYNHLF | LOCAL KRUGER  |  INPUT QUOTE  ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aOISFutureComp.length; ++i)
			System.out.println ("\t[" + aOISFutureComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aOISFutureComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblOISFutureQuote[i], 1, 6, 1.)
			);

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                 LONG END OIS INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83 | LOCAL HYMAN89 | LOCAL HUYNHLF | LOCAL KRUGER  |  INPUT QUOTE  ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aLongEndOISComp.length; ++i)
			System.out.println ("\t[" + aLongEndOISComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aLongEndOISComp[i].measureValue (
						valParams, null,
						MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblLongEndOISQuote[i], 1, 6, 1.)
			);

		/*
		 * Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies for a sequence of bespoke OIS instruments.
		 */

		CalibratableFixedIncomeComponent[] aCC = OvernightIndexFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {"3Y", "6Y", "9Y", "12Y", "15Y", "18Y", "21Y", "24Y", "27Y", "30Y"},
			new double[] {0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01},
			strCurrency
		);

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t                                                BESPOKE OIS PAR RATE");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  LOCAL AKIMA  | LOCAL HARMONIC | LOCAL HYMAN83  | LOCAL HYMAN89  | LOCAL HUYNHLF  | LOCAL KRUGER ");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");

		for (int i = 0; i < aCC.length; ++i)
			System.out.println ("\t[" + aCC[i].maturity() + "] = " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcShapePreserving, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalAkima, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalHarmonic, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalHyman83, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalHyman89, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalHuynhLeFloch, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
					1, 6, 1.) + "    |   " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dcLocalKruger, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.)
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

		JulianDate dtSpot = JulianDate.Today();

		ShapeOISDFZeroLocalSmoothSample (
			dtSpot,
			"---- DISCOUNT CURVE WITH OVERNIGHT INDEX ---",
			"EUR",
			true);

		ShapeOISDFZeroLocalSmoothSample (
			dtSpot,
			"---- DISCOUNT CURVE WITH OVERNIGHT FUND ---",
			"EUR",
			false);
	}
}

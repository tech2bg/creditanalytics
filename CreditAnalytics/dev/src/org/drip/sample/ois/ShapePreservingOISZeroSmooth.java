
package org.drip.sample.ois;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
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
 * ShapePreservingOISZeroSmooth demonstrates the usage of different shape preserving and smoothing techniques
 *  involved in the discount curve creation. It shows the following:
 * 	- Construct the Array of Cash/OIS Instruments and their Quotes from the given set of parameters.
 * 	- Construct the Cash/OIS Instrument Set Stretch Builder.
 * 	- Set up the Linear Curve Calibrator using the following parameters:
 * 		- Cubic Exponential Mixture Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Global Curve Control parameters as follows:
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Set up the Local Curve Control parameters as follows:
 * 		- C1 Bessel Monotone Smoothener with no spurious extrema elimination and no monotone filter
 * 		- Zero Rate Quantification Metric
 * 		- Cubic Polynomial Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Construct the Shape Preserving OIS Discount Curve by applying the linear curve calibrator to the array of
 * 		Cash and OIS Stretches.
 * 	- Construct the Globally Smoothened OIS Discount Curve by applying the linear curve calibrator and the Global
 * 		Curve Control parameters to the array of Cash and OIS Stretches and the shape preserving discount
 * 		curve.
 * 	- Construct the Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and the Local
 * 		Curve Control parameters to the array of Cash and OIS Stretches and the shape preserving discount
 *  	curve.
 * 	- Cross-Comparison of the Cash/OIS Calibration Instrument "Rate" metric across the different curve
 * 		construction methodologies.
 *  - Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
 *  	construction methodologies for a sequence of bespoke OIS instruments.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ShapePreservingOISZeroSmooth {

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
	 * Construct the Array of OIS Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] OISInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrTenor.length];

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			OvernightFundFloatingStream floatStream = OvernightFundFloatingStream.Create (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., OvernightFRIBuilder.JurisdictionFRI (strCurrency),
					"Act/360", dap, dap, null, -1., strCurrency, strCurrency, false);

			FixedStream fixStream = new FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
				adblCoupon[i], 2, "Act/360", "Act/360", false, null, dap, dap, dap, dap, dap, null, null, 1.,
					strCurrency, strCurrency);

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = ois;
		}

		return aCalibComp;
	}

	/*
	 * This sample demonstrates the usage of different shape preserving and smoothing techniques involved in
	 * 	the OIS discount curve creation. It shows the following:
	 * 	- Construct the Array of Cash/OIS Instruments and their Quotes from the given set of parameters.
	 * 	- Construct the Cash/OIS Instrument Set Stretch Builder.
	 * 	- Set up the Linear Curve Calibrator using the following parameters:
	 * 		- Cubic Exponential Mixture Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Global Curve Control parameters as follows:
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Set up the Local Curve Control parameters as follows:
	 * 		- C1 Bessel Monotone Smoothener with no spurious extrema elimination and no monotone filter
	 * 		- Zero Rate Quantification Metric
	 * 		- Cubic Polynomial Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Construct the Shape Preserving OIS Discount Curve by applying the linear curve calibrator to the array
	 * 		of Cash and OIS Stretches.
	 * 	- Construct the Globally Smoothened OIS Discount Curve by applying the linear curve calibrator and the
	 * 		Global Curve Control parameters to the array of Cash and OIS Stretches and the shape preserving
	 * 		discount curve.
	 * 	- Construct the Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and the
	 * 		Local Curve Control parameters to the array of Cash and OIS Stretches and the shape preserving
	 *  	discount curve.
	 * 	- Cross-Comparison of the Cash/OIS Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 *  - Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
	 *  	construction methodologies for a sequence of bespoke OIS instruments.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void ShapePreservingOISDFZeroSmoothSample()
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", "USD");

		/*
		 * Construct the Array of Cash Instruments and their Quotes from the given set of parameters
		 */

		CalibratableFixedIncomeComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtToday,
			new int[] {1, 2, 3, 7, 14, 21, 30, 60},
			4,
			"USD");

		double[] adblCashQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850, // Cash
			0.01612, 0.01580, 0.01589, 0.01598}; // Futures

		/*
		 * Construct the Cash Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec rrsCash = StretchRepresentationSpec.CreateStretchBuilderSet (
			"CASH",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCashComp,
			"Rate",
			adblCashQuote,
			null);

		/*
		 * Setup the OIS instruments and their quotes for calibrations
		 */

		double[] adblOISQuote = new double[] {
			0.02604,    //  4Y
			0.02808,    //  5Y
			0.02983,    //  6Y
			0.03136,    //  7Y
			0.03268,    //  8Y
			0.03383,    //  9Y
			0.03488     // 10Y
		};

		CalibratableFixedIncomeComponent[] aOISComp = OISInstrumentsFromMaturityTenor (
			dtToday,
			new java.lang.String[]
				{"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y"},
			adblOISQuote,
			"USD");

		/*
		 * Construct the OIS Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec rrsOIS = StretchRepresentationSpec.CreateStretchBuilderSet (
			"OIS",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aOISComp,
			"Rate",
			adblOISQuote,
			null);

		StretchRepresentationSpec[] aRRS = new StretchRepresentationSpec[] {rrsCash, rrsOIS};

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
		 * Set up the Global Curve Control parameters as follows:
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		/* GlobalControlCurveParams gccp = new GlobalControlCurveParams (
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_ZERO_RATE,
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (1.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null); */

		/*
		 * Set up the Local Curve Control parameters as follows:
		 * 	- C1 Bessel Monotone Smoothener with no spurious extrema elimination and no monotone filter
		 * 	- Zero Rate Quantification Metric
		 * 	- Cubic Polynomial Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LocalControlCurveParams lccp = new LocalControlCurveParams (
			org.drip.spline.pchip.LocalMonotoneCkGenerator.C1_BESSEL,
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
			false,
			false);

		/*
		 * Construct the Shape Preserving OIS Discount Curve by applying the linear curve calibrator to the array
		 *  of Cash and OIS Stretches.
		 */

		DiscountCurve dcShapePreserving = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aRRS,
			new ValuationParams (dtToday, dtToday, "USD"),
			null,
			null,
			null,
			1.);

		/*
		 * Construct the Globally Smoothened OIS Discount Curve by applying the linear curve calibrator and the
		 * 	Global Curve Control parameters to the array of Cash and OIS Stretches and the shape preserving
		 * 	discount curve.
		 */

		/* DiscountCurve dcGloballySmooth = ScenarioDiscountCurveBuilder.SmoothingGlobalControlBuild (
			dcShapePreserving,
			lcc,
			gccp,
			aRRS,
			new ValuationParams (dtToday, dtToday, "USD"),
			null,
			null,
			null); */

		/*
		 * Construct the Locally Smoothened OIS Discount Curve by applying the linear curve calibrator and the
		 * 	Local Curve Control parameters to the array of Cash and OIS Stretches and the shape preserving
		 *  discount curve.
		 */

		DiscountCurve dcLocallySmooth = ScenarioDiscountCurveBuilder.SmoothingLocalControlBuild (
			dcShapePreserving,
			lcc,
			lccp,
			aRRS,
			new ValuationParams (dtToday, dtToday, "USD"),
			null,
			null,
			null);

		/*
		 * Cross-Comparison of the Cash Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t               CASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   | SMOOTHING #1  | SMOOTHING #2  |  INPUT QUOTE  ");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i)
			System.out.println ("\t[" + aCashComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (
					aCashComp[i].measureValue (
						new ValuationParams (dtToday, dtToday, "USD"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcShapePreserving, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				/* FormatUtil.FormatDouble (
					aCashComp[i].measureValue (
						new ValuationParams (dtToday, dtToday, "USD"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcGloballySmooth, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " + */
				FormatUtil.FormatDouble (
					aCashComp[i].measureValue (
						new ValuationParams (dtToday, dtToday, "USD"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcLocallySmooth, null, null, null, null, null, null),
						null,
						"Rate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblCashQuote[i], 1, 6, 1.)
			);

		/*
		 * Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t               OIS INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   | SMOOTHING #1  | SMOOTHING #2  |  INPUT QUOTE  ");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aOISComp.length; ++i)
			System.out.println ("\t[" + aOISComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (
					aOISComp[i].measureValue (
						new ValuationParams (dtToday, dtToday, "USD"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcShapePreserving, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				/* FormatUtil.FormatDouble (
					aOISComp[i].measureValue (
						new ValuationParams (dtToday, dtToday, "USD"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcGloballySmooth, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " + */
				FormatUtil.FormatDouble (
					aOISComp[i].measureValue (
						new ValuationParams (dtToday, dtToday, "USD"), null,
						ComponentMarketParamsBuilder.CreateComponentMarketParams (dcLocallySmooth, null, null, null, null, null, null),
						null,
						"CalibSwapRate"),
					1, 6, 1.) + "   |   " +
				FormatUtil.FormatDouble (adblOISQuote[i], 1, 6, 1.)
			);

		/*
		 * Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies for a sequence of bespoke OIS instruments.
		 */

		CalibratableFixedIncomeComponent[] aCC = OISInstrumentsFromMaturityTenor (
			dtToday,
			new java.lang.String[] {"3Y", "6Y", "9Y", "12Y", "15Y", "18Y", "21Y", "24Y", "27Y", "30Y"},
			new double[] {0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01},
			"USD"
		);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t           BESPOKE OIS PAR RATE");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t        SHAPE PRESERVING   |  SMOOTHING #1 |  SMOOTHING #2");

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCC.length; ++i)
			System.out.println ("\t[" + aCC[i].maturity() + "] = " +
				FormatUtil.FormatDouble (
					aCC[i].measureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dcShapePreserving, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " +
				/* FormatUtil.FormatDouble (
					aCC[i].measureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dcGloballySmooth, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.) + "   |   " + */
				FormatUtil.FormatDouble (
					aCC[i].measureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dcLocallySmooth, null, null, null, null, null, null),
					null,
					"CalibSwapRate"),
				1, 6, 1.)
			);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		ShapePreservingOISDFZeroSmoothSample();
	}
}


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
import org.drip.spline.basis.PolynomialFunctionSetParams;
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
 * CustomOISCurveBuilder contains a sample of the construction and usage of the OIS Curve built using the
 * 	Overnight Indexed Swap Product Instruments. The Tenors/Quotes to replicate are taken from:
 * 
 *  - Ametrano, F., and M. Bianchetti (2013): Everything You Always Wanted to Know About Multiple Interest
 *  	Rate Curve Bootstrapping but Were Afraid to Ask,
 *  		http://papers.ssrn.com/sol3/papers.cfm?abstract_id=2219548
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CustomOISCurveBuilder {

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
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
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

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			OvernightIndexFloatingStream floatStream = OvernightIndexFloatingStream.Create (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., false, OvernightFRIBuilder.JurisdictionFRI (strCurrency), 4, "Act/360",
					false, "Act/360", false, false, null, dap, dap, dap, dap, dap, dap, null, null, -1., strCurrency,
						strCurrency);

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

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		for (int i = 0; i < astrStartTenor.length; ++i) {
			JulianDate dtEffective = dtSpot.addTenorAndAdjust (astrStartTenor[i], strCurrency);

			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			OvernightIndexFloatingStream floatStream = OvernightIndexFloatingStream.Create (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., false, OvernightFRIBuilder.JurisdictionFRI (strCurrency), 4, "Act/360",
					false, "Act/360", false, false, null, dap, dap, dap, dap, dap, dap, null, null, -1., strCurrency,
						strCurrency);

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

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		for (int i = 0; i < astrStartTenor.length; ++i) {
			JulianDate dtEffective = dtSpot.addTenorAndAdjust (astrStartTenor[i], strCurrency);

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

	private static final void CustomOISCurveBuilderSample (
		final String strHeaderComment,
		final boolean bOvernightIndex)
		throws Exception
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     " + strHeaderComment);

		System.out.println ("\t----------------------------------------------------------------");

		JulianDate dtToday = JulianDate.CreateFromYMD (2012, JulianDate.DECEMBER, 11);

		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		CalibratableFixedIncomeComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtToday,
			new int[] {1, 2, 3},
			"EUR"
		);

		double[] adblDepositQuote = new double[] {
			0.0004, 0.0004, 0.0004		 // Deposit
		};

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsDeposit = StretchRepresentationSpec.CreateStretchBuilderSet (
			"CASH",
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
				dtToday,
				new java.lang.String[]
					{"1W", "2W", "3W", "1M"},
				adblShortEndOISQuote,
				"EUR") :
			OvernightFundFromMaturityTenor (
				dtToday,
				new java.lang.String[]
					{"1W", "2W", "3W", "1M"},
				adblShortEndOISQuote,
				"EUR");

		/*
		 * Construct the Short End OIS Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsShortEndOIS = StretchRepresentationSpec.CreateStretchBuilderSet (
			"OIS SHORT END",
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
				dtToday,
				new java.lang.String[] {"1M", "2M", "3M", "4M", "5M"},
				new java.lang.String[] {"1M", "1M", "1M", "1M", "1M"},
				adblOISFutureQuote,
				"EUR") :
			OvernightFundFutureFromMaturityTenor (
				dtToday,
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
				dtToday,
				new java.lang.String[]
					{"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
				adblLongEndOISQuote,
				"EUR") :
			OvernightFundFromMaturityTenor (
				dtToday,
				new java.lang.String[]
					{"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"},
				adblLongEndOISQuote,
				"EUR");

		/*
		 * Construct the Long End OIS Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsLongEndOIS = StretchRepresentationSpec.CreateStretchBuilderSet (
			"OIS LONG END",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aLongEndOISComp,
			"Rate",
			adblLongEndOISQuote,
			null);

		StretchRepresentationSpec[] aRBS = new StretchRepresentationSpec[] {
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
			aRBS,
			new ValuationParams (dtToday, dtToday, "EUR"),
			null,
			null,
			null,
			1.);

		/*
		 * Cross-Comparison of the Deposit Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t     CASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i)
			System.out.println ("\t[" + aDepositComp[i].effective() + " => " + aDepositComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aDepositComp[i].measureValue (new ValuationParams (dtToday, dtToday, "EUR"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
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
				FormatUtil.FormatDouble (aShortEndOISComp[i].measureValue (new ValuationParams (dtToday, dtToday, "EUR"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblShortEndOISQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (aShortEndOISComp[i].measureValue (new ValuationParams (dtToday, dtToday, "EUR"), null,
								ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
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
				FormatUtil.FormatDouble (aOISFutureComp[i].measureValue (new ValuationParams (dtToday, dtToday, "EUR"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblOISFutureQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (aOISFutureComp[i].measureValue (new ValuationParams (dtToday, dtToday, "EUR"), null,
								ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
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
				FormatUtil.FormatDouble (aLongEndOISComp[i].measureValue (new ValuationParams (dtToday, dtToday, "EUR"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblLongEndOISQuote[i], 1, 6, 1.) + " | " +
							FormatUtil.FormatDouble (aLongEndOISComp[i].measureValue (new ValuationParams (dtToday, dtToday, "EUR"), null,
								ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
									null, "FairPremium"), 1, 6, 1.));
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		CustomOISCurveBuilderSample ("OVERNIGHT INDEX RUN RECONCILIATION", true);

		CustomOISCurveBuilderSample ("OVERNIGHT FUND RUN RECONCILIATION", false);
	}
}

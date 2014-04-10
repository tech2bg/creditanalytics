
package org.drip.sample.ois;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.rates.*;
import org.drip.param.creator.ComponentMarketParamsBuilder;
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
import org.drip.state.curve.DiscountFactorDiscountCurve;
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
 * CustomOISCurveReconciler demonstrates the multi-stretch transition custom OIS discount curve construction,
 * 	turns application, discount factor extraction, and calibration quote recovery.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CustomOISCurveReconciler {

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
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addBusDays (aiDay[i], strCurrency), strCurrency);

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
	 * Construct the Linear Curve Calibrator using the segment custom builder control parameters and
	 * 	natural boundary conditions.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final LinearCurveCalibrator MakeCalibrator (
		SegmentCustomBuilderControl prbp)
		throws Exception
	{
		return new LinearCurveCalibrator (
			prbp,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);
	}

	/*
	 * This sample demonstrates the multi-stretch transition custom discount curve construction, turns
	 * 	application, discount factor extraction, and calibration quote recovery. It shows the following
	 * 	steps:
	 * 	- Setup the linear curve calibrator.
	 * 	- Setup the cash instruments and their quotes for calibration.
	 * 	- Setup the cash instruments stretch latent state representation - this uses the discount factor
	 * 		quantification metric and the "rate" manifest measure.
	 * 	- Setup the OIS instruments and their quotes for calibration.
	 * 	- Setup the OIS instruments stretch latent state representation - this uses the discount factor
	 * 		quantification metric and the "rate" manifest measure.
	 * 	- Calibrate over the instrument set to generate a new overlapping latent state span instance.
	 * 	- Retrieve the "cash" stretch from the span.
	 * 	- Retrieve the "OIS" stretch from the span.
	 * 	- Create a discount curve instance by converting the overlapping stretch to an exclusive
	 * 		non-overlapping stretch.
	 * 	- Compare the discount factors and their monotonicity emitted from the discount curve, the
	 * 		non-overlapping span, and the "OIS" stretch across the range of tenor predictor ordinates.
	 * 	- Cross-Recovery of the Cash Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Cross-Recovery of the OIS Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Create a turn list instance and add new turn instances.
	 * 	- Update the discount curve with the turn list.
	 * 	- Compare the discount factor implied the discount curve with and without applying the turns
	 * 		adjustment.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void SplineLinearOISDiscountCurve (
		final SegmentCustomBuilderControl prbp)
		throws Exception
	{
		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", "USD");

		/*
		 * Setup the linear curve calibrator
		 */

		LinearCurveCalibrator lcc = MakeCalibrator (prbp);

		/*
		 * Setup the cash instruments and their quotes for calibrations
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
		 * Setup the cash instruments stretch latent state representation - this uses the discount factor
		 * 	quantification metric and the "rate" manifest measure.
		 */

		StretchRepresentationSpec rbsCash = StretchRepresentationSpec.CreateStretchBuilderSet (
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
		 * Setup the OIS instruments stretch latent state representation - this uses the discount factor
		 * 	quantification metric and the "rate" manifest measure.
		 */

		StretchRepresentationSpec rbsOIS = StretchRepresentationSpec.CreateStretchBuilderSet (
			"OIS",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aOISComp,
			"Rate",
			adblOISQuote,
			null);

		StretchRepresentationSpec[] aRBS = new StretchRepresentationSpec[] {rbsCash, rbsOIS};

		/*
		 * Calibrate over the instrument set to generate a new overlapping latent state span instance
		 */

		org.drip.spline.grid.OverlappingStretchSpan ors = lcc.calibrateSpan (
			aRBS,
			1.,
			new ValuationParams (dtToday, dtToday, "USD"),
			null,
			null,
			null);

		/*
		 * Retrieve the "cash" stretch from the span
		 */

		MultiSegmentSequence mssCash = ors.getStretch ("CASH");

		/*
		 * Retrieve the "OIS" stretch from the span
		 */

		MultiSegmentSequence mssOIS = ors.getStretch ("OIS");

		/*
		 * Create a discount curve instance by converting the overlapping stretch to an exclusive
		 * 	non-overlapping stretch.
		 */

		DiscountCurve dfdc = new DiscountFactorDiscountCurve ("USD", null, ors);

		/*
		 * Compare the discount factors and their monotonicity emitted from the discount curve, the
		 * non-overlapping span, and the "cash" stretch across the range of tenor predictor ordinates.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     CASH DF            DFDC     STRETCH           LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		for (double dblX = mssCash.getLeftPredictorOrdinateEdge(); dblX <= mssCash.getRightPredictorOrdinateEdge();
			dblX += 0.1 * (mssCash.getRightPredictorOrdinateEdge() - mssCash.getLeftPredictorOrdinateEdge())) {
			try {
				System.out.println ("\tCash [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssCash.responseValue (dblX), 1, 8, 1.) + " | " +
								mssCash.monotoneType (dblX));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * Compare the discount factors and their monotonicity emitted from the discount curve, the
		 * non-overlapping span, and the "OIS" stretch across the range of tenor predictor ordinates.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t      OIS DF            DFDC     STRETCH            LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		for (double dblX = mssOIS.getLeftPredictorOrdinateEdge(); dblX <= mssOIS.getRightPredictorOrdinateEdge();
			dblX += 0.05 * (mssOIS.getRightPredictorOrdinateEdge() - mssOIS.getLeftPredictorOrdinateEdge())) {
				System.out.println ("\tOIS [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssOIS.responseValue (dblX), 1, 8, 1.) + " | " +
								mssOIS.monotoneType (dblX));
		}

		System.out.println ("\tOIS [" + dtToday.addTenor ("60Y") + "] = " +
			FormatUtil.FormatDouble (dfdc.df (dtToday.addTenor ("60Y")), 1, 8, 1.));

		/*
		 * Cross-Recovery of the Cash Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     CASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i)
			System.out.println ("\t[" + aCashComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aCashComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dfdc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblCashQuote[i], 1, 6, 1.));

		/*
		 * Cross-Recovery of the OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t      OIS INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aOISComp.length; ++i)
			System.out.println ("\t[" + aOISComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aOISComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dfdc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblOISQuote[i], 1, 6, 1.));

		/*
		 * Create a turn list instance and add new turn instances
		 */

		TurnListDiscountFactor tldc = new TurnListDiscountFactor();

		tldc.addTurn (new Turn (
			dtToday.addTenor ("5Y").getJulian(),
			dtToday.addTenor ("40Y").getJulian(),
			0.001));

		/*
		 * Update the discount curve with the turn list.
		 */

		dfdc.setTurns (tldc);

		/*
		 * Compare the discount factor implied the discount curve with and without applying the turns
		 * 	adjustment.
		 */

		System.out.println ("\n\t-------------------------------");

		System.out.println ("\t      OIS DF            DFDC");

		System.out.println ("\t-------------------------------");

		for (double dblX = mssOIS.getLeftPredictorOrdinateEdge(); dblX <= mssOIS.getRightPredictorOrdinateEdge();
			dblX += 0.05 * (mssOIS.getRightPredictorOrdinateEdge() - mssOIS.getLeftPredictorOrdinateEdge())) {
				System.out.println ("\tOIS [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.));
		}
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		/*
		 * Construct the segment Custom builder using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 */

		SegmentCustomBuilderControl prbpPolynomial = new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (4),
			SegmentInelasticDesignControl.Create (2, 2),
			new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
			null);

		/*
		 * Runs the full spline linear discount curve builder sample.
		 */

		SplineLinearOISDiscountCurve (prbpPolynomial);
	}
}

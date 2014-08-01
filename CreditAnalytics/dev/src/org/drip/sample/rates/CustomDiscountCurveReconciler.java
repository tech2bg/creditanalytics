
package org.drip.sample.rates;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.rates.Turn;
import org.drip.analytics.rates.TurnListDiscountFactor;
import org.drip.param.creator.MarketParamsBuilder;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.cashflow.FixedStream;
import org.drip.product.cashflow.FloatingStream;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.curve.DiscountFactorDiscountCurve;
import org.drip.state.estimator.*;
import org.drip.state.identifier.ForwardLabel;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * CustomDiscountCurveReconciler demonstrates the multi-stretch transition custom discount curve
 *  construction, turns application, discount factor extraction, and calibration quote recovery. It shows the
 * 	following steps:
 * 	- Setup the linear curve calibrator.
 * 	- Setup the cash instruments and their quotes for calibration.
 * 	- Setup the cash instruments stretch latent state representation - this uses the discount factor
 * 		quantification metric and the "rate" manifest measure.
 * 	- Setup the swap instruments and their quotes for calibration.
 * 	- Setup the swap instruments stretch latent state representation - this uses the discount factor
 * 		quantification metric and the "rate" manifest measure.
 * 	- Calibrate over the instrument set to generate a new overlapping latent state span instance.
 * 	- Retrieve the "cash" stretch from the span.
 * 	- Retrieve the "swap" stretch from the span.
 * 	- Create a discount curve instance by converting the overlapping stretch to an exclusive
 * 		non-overlapping stretch.
 * 	- Compare the discount factors and their monotonicity emitted from the discount curve, the
 * 		non-overlapping span, and the "swap" stretch across the range of tenor predictor ordinates.
 * 	- Cross-Recovery of the Cash Calibration Instrument "Rate" metric across the different curve
 * 		construction methodologies.
 * 	- Cross-Recovery of the Swap Calibration Instrument "Rate" metric across the different curve
 * 		construction methodologies.
 * 	- Create a turn list instance and add new turn instances.
 * 	- Update the discount curve with the turn list.
 * 	- Compare the discount factor implied the discount curve with and without applying the turns
 * 		adjustment.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CustomDiscountCurveReconciler {

	/*
	 * Construct the Array of Cash Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = DepositBuilder.CreateDeposit (dtEffective, dtEffective.addBusDays (aiDay[i], "USD"), null, "USD");

		CalibratableFixedIncomeComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, "USD");

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
		final String[] astrTenor)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenor (astrTenor[i]);

			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				"USD",
				"USD"
			);

			FloatingStream floatStream = new FloatingStream (
				"USD",
				null,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				ForwardLabel.Create ("USD-LIBOR-6M"),
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
				"USD",
				"USD"
			);

			FixedStream fixStream = new FixedStream (
				"USD",
				null,
				0.,
				1.,
				null,
				lsFixedPeriods
			);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + dtMaturity.toString() + ".USD");

			aCalibComp[i] = irs;
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
	 * 	- Setup the swap instruments and their quotes for calibration.
	 * 	- Setup the swap instruments stretch latent state representation - this uses the discount factor
	 * 		quantification metric and the "rate" manifest measure.
	 * 	- Calibrate over the instrument set to generate a new overlapping latent state span instance.
	 * 	- Retrieve the "cash" stretch from the span.
	 * 	- Retrieve the "swap" stretch from the span.
	 * 	- Create a discount curve instance by converting the overlapping stretch to an exclusive
	 * 		non-overlapping stretch.
	 * 	- Compare the discount factors and their monotonicity emitted from the discount curve, the
	 * 		non-overlapping span, and the "swap" stretch across the range of tenor predictor ordinates.
	 * 	- Cross-Recovery of the Cash Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Cross-Recovery of the Swap Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Create a turn list instance and add new turn instances.
	 * 	- Update the discount curve with the turn list.
	 * 	- Compare the discount factor implied the discount curve with and without applying the turns
	 * 		adjustment.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void SplineLinearDiscountCurve (
		final SegmentCustomBuilderControl prbp)
		throws Exception
	{
		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		/*
		 * Setup the linear curve calibrator
		 */

		LinearCurveCalibrator lcc = MakeCalibrator (prbp);

		/*
		 * Setup the cash instruments and their quotes for calibrations
		 */

		CalibratableFixedIncomeComponent[] aCashComp = CashInstrumentsFromMaturityDays (dtToday, new int[] {1, 2, 7, 14, 30, 60}, 8);

		double[] adblCashQuote = new double[]
			{0.0013, 0.0017, 0.0017, 0.0018, 0.0020, 0.0023, // Cash Rate
			0.0027, 0.0032, 0.0041, 0.0054, 0.0077, 0.0104, 0.0134, 0.0160}; // EDF Rate;

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
		 * Setup the swap instruments and their quotes for calibrations
		 */

		CalibratableFixedIncomeComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"});

		double[] adblSwapQuote = new double[]
			{0.0166, 0.0206, 0.0241, 0.0269, 0.0292, 0.0311, 0.0326, 0.0340, 0.0351, 0.0375, 0.0393, 0.0402, 0.0407, 0.0409, 0.0409};

		/*
		 * Setup the Swap instruments stretch latent state representation - this uses the discount factor
		 * 	quantification metric and the "rate" manifest measure.
		 */

		StretchRepresentationSpec rbsSwap = StretchRepresentationSpec.CreateStretchBuilderSet (
			"SWAP",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aSwapComp,
			"Rate",
			adblSwapQuote,
			null);

		StretchRepresentationSpec[] aRBS = new StretchRepresentationSpec[] {rbsCash, rbsSwap};

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
		 * Retrieve the "swap" stretch from the span
		 */

		MultiSegmentSequence mssSwap = ors.getStretch ("SWAP");

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
		 * non-overlapping span, and the "swap" stretch across the range of tenor predictor ordinates.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP DF            DFDC     STRETCH            LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		for (double dblX = mssSwap.getLeftPredictorOrdinateEdge(); dblX <= mssSwap.getRightPredictorOrdinateEdge();
			dblX += 0.05 * (mssSwap.getRightPredictorOrdinateEdge() - mssSwap.getLeftPredictorOrdinateEdge())) {
				System.out.println ("\tSwap [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssSwap.responseValue (dblX), 1, 8, 1.) + " | " +
								mssSwap.monotoneType (dblX));
		}

		System.out.println ("\tSwap [" + dtToday.addTenor ("60Y") + "] = " +
			FormatUtil.FormatDouble (dfdc.df (dtToday.addTenor ("60Y")), 1, 8, 1.));

		/*
		 * Cross-Recovery of the Cash Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     CASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i)
			System.out.println ("\t[" + aCashComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aCashComp[i].measureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					MarketParamsBuilder.Create (dfdc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblCashQuote[i], 1, 6, 1.));

		/*
		 * Cross-Recovery of the Swap Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i)
			System.out.println ("\t[" + aSwapComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aSwapComp[i].measureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					MarketParamsBuilder.Create (dfdc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblSwapQuote[i], 1, 6, 1.));

		/*
		 * Create a turn list instance and add new turn instances
		 */

		TurnListDiscountFactor tldc = new TurnListDiscountFactor();

		tldc.addTurn (new Turn (
			dtToday.addTenor ("5Y").julian(),
			dtToday.addTenor ("40Y").julian(),
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

		System.out.println ("\t     SWAP DF            DFDC");

		System.out.println ("\t-------------------------------");

		for (double dblX = mssSwap.getLeftPredictorOrdinateEdge(); dblX <= mssSwap.getRightPredictorOrdinateEdge();
			dblX += 0.05 * (mssSwap.getRightPredictorOrdinateEdge() - mssSwap.getLeftPredictorOrdinateEdge())) {
				System.out.println ("\tSwap [" + new JulianDate (dblX) + "] = " +
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
		 * Runs the full spline linear discount curve buil;der sample.
		 */

		SplineLinearDiscountCurve (prbpPolynomial);
	}
}

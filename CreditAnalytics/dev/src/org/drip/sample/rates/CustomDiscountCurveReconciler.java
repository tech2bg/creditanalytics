
package org.drip.sample.rates;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.creator.MarketParamsBuilder;
import org.drip.param.valuation.CashSettleParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.calib.*;
import org.drip.product.cashflow.*;
import org.drip.product.creator.*;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.curve.DiscountFactorDiscountCurve;
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
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final DepositComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final String strCurrency,
		final int[] aiDay)
		throws Exception
	{
		DepositComponent[] aDeposit = new DepositComponent[aiDay.length];

		for (int i = 0; i < aiDay.length; ++i)
			aDeposit[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				null,
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

		for (int i = 0; i < aDeposit.length; ++i) {
			DepositComponentQuoteSet depositQuote = new DepositComponentQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						DiscountCurve.LATENT_STATE_DISCOUNT,
						DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						FundingLabel.Standard (aDeposit[i].payCurrency()[0])
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

	private static final LatentStateStretchSpec EDFStretch (
		final EDFComponent[] aEDF,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aEDF.length];

		for (int i = 0; i < aEDF.length; ++i) {
			EDFComponentQuoteSet edfQuote = new EDFComponentQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						DiscountCurve.LATENT_STATE_DISCOUNT,
						DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						FundingLabel.Standard (aEDF[i].payCurrency()[0])
					),
					new LatentStateSpecification (
						ForwardCurve.LATENT_STATE_FORWARD,
						ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
						aEDF[i].forwardLabel()[0]
					)
				}
			);

			edfQuote.setRate (adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aEDF[i],
				edfQuote
			);
		}

		return new LatentStateStretchSpec (
			"EDF",
			aSegmentSpec
		);
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrMaturityTenor)
		throws Exception
	{
		FixFloatComponent[] aIRS = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			FloatingStream floatStream = new FloatingStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrMaturityTenor[i],
					Double.NaN,
					null,
					2,
					"Act/360",
					false,
					false,
					strCurrency,
					-1.,
					null,
					0.,
					strCurrency,
					strCurrency,
					ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
					null
				)
			);

			FixedStream fixStream = new FixedStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrMaturityTenor[i],
					Double.NaN,
					null,
					2,
					"Act/360",
					false,
					false,
					strCurrency,
					1.,
					null,
					0.,
					strCurrency,
					strCurrency,
					null,
					null
				)
			);

			FixFloatComponent irs = new FixFloatComponent (
				fixStream,
				floatStream,
				new CashSettleParams (0, strCurrency, 0)
			);

			irs.setPrimaryCode ("IRS." + astrMaturityTenor[i] + "." + strCurrency);

			aIRS[i] = irs;
		}

		return aIRS;
	}

	private static final LatentStateStretchSpec SwapStretch (
		final FixFloatComponent[] aIRS,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aIRS.length];

		for (int i = 0; i < aIRS.length; ++i) {
			FixFloatQuoteSet fixFloatQuote = new FixFloatQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						DiscountCurve.LATENT_STATE_DISCOUNT,
						DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						FundingLabel.Standard (aIRS[i].payCurrency()[0])
					),
					new LatentStateSpecification (
						ForwardCurve.LATENT_STATE_FORWARD,
						ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
						aIRS[i].forwardLabel()[0]
					)
				}
			);

			fixFloatQuote.setPV (0.);

			fixFloatQuote.setSwapRate (adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aIRS[i],
				fixFloatQuote
			);
		}

		return new LatentStateStretchSpec (
			"SWAP",
			aSegmentSpec
		);
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
		final JulianDate dtSpot,
		final String strCurrency,
		final SegmentCustomBuilderControl scbc)
		throws Exception
	{
		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		DepositComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			strCurrency,
			new int[] {
				1, 2, 7, 14, 30, 60
			}
		);

		double[] adblDepositQuote = new double[] {
			0.0013, 0.0017, 0.0017, 0.0018, 0.0020, 0.0023
		};

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec depositStretch = DepositStretch (
			aDepositComp,
			adblDepositQuote
		);

		/*
		 * Construct the Array of EDF Instruments and their Quotes from the given set of parameters
		 */

		EDFComponent[] aEDFComp = EDFutureBuilder.GenerateEDPack (
			dtSpot,
			8,
			strCurrency
		);

		double[] adblEDFQuote = new double[] {
			0.0027, 0.0032, 0.0041, 0.0054, 0.0077, 0.0104, 0.0134, 0.0160
		};

		/*
		 * Construct the EDF Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec edfStretch = EDFStretch (
			aEDFComp,
			adblEDFQuote
		);

		/*
		 * Construct the Array of Swap Instruments and their Quotes from the given set of parameters
		 */

		FixFloatComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"
			}
		);

		double[] adblSwapQuote = new double[] {
			0.0166, 0.0206, 0.0241, 0.0269, 0.0292, 0.0311, 0.0326, 0.0340, 0.0351, 0.0375, 0.0393, 0.0402, 0.0407, 0.0409, 0.0409
		};

		/*
		 * Construct the Swap Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec swapStretch = SwapStretch (
			aSwapComp,
			adblSwapQuote
		);

		LatentStateStretchSpec[] aStretchSpec = new LatentStateStretchSpec[] {depositStretch, edfStretch, swapStretch};

		/*
		 * Set up the Linear Curve Calibrator using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LinearLatentStateCalibrator lcc = new LinearLatentStateCalibrator (
			scbc,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null
		);

		ValuationParams valParams = new ValuationParams (
			dtSpot,
			dtSpot,
			strCurrency
		);

		/*
		 * Calibrate over the instrument set to generate a new overlapping latent state span instance
		 */

		org.drip.spline.grid.OverlappingStretchSpan ors = lcc.calibrateSpan (
			aStretchSpec,
			1.,
			valParams,
			null,
			null,
			null);

		/*
		 * Retrieve the "Deposit" stretch from the span
		 */

		MultiSegmentSequence mssDeposit = ors.getStretch ("DEPOSIT");

		/*
		 * Retrieve the "swap" stretch from the span
		 */

		MultiSegmentSequence mssSwap = ors.getStretch ("SWAP");

		/*
		 * Create a discount curve instance by converting the overlapping stretch to an exclusive
		 * 	non-overlapping stretch.
		 */

		DiscountCurve dfdc = new DiscountFactorDiscountCurve (strCurrency, null, ors);

		/*
		 * Compare the discount factors and their monotonicity emitted from the discount curve, the
		 * non-overlapping span, and the Deposit stretch across the range of tenor predictor ordinates.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT DF            DFDC     STRETCH           LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		for (double dblX = mssDeposit.getLeftPredictorOrdinateEdge(); dblX <= mssDeposit.getRightPredictorOrdinateEdge();
			dblX += 0.1 * (mssDeposit.getRightPredictorOrdinateEdge() - mssDeposit.getLeftPredictorOrdinateEdge())) {
			try {
				System.out.println ("\tDeposit [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssDeposit.responseValue (dblX), 1, 8, 1.) + " | " +
								mssDeposit.monotoneType (dblX));
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

		System.out.println ("\tSwap [" + dtSpot.addTenor ("60Y") + "] = " +
			FormatUtil.FormatDouble (dfdc.df (dtSpot.addTenor ("60Y")), 1, 8, 1.));

		/*
		 * Cross-Recovery of the Deposit Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i)
			System.out.println ("\t[" + aDepositComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aDepositComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dfdc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.));

		/*
		 * Cross-Recovery of the Swap Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i)
			System.out.println ("\t[" + aSwapComp[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aSwapComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dfdc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblSwapQuote[i], 1, 6, 1.));

		/*
		 * Create a turn list instance and add new turn instances
		 */

		TurnListDiscountFactor tldc = new TurnListDiscountFactor();

		tldc.addTurn (new Turn (
			dtSpot.addTenor ("5Y").julian(),
			dtSpot.addTenor ("40Y").julian(),
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
		 * Run the full spline linear discount curve builder sample.
		 */

		SplineLinearDiscountCurve (JulianDate.Today(), "USD", prbpPolynomial);
	}
}

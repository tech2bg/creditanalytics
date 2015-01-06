
package org.drip.sample.swaps;

import java.util.Map;

import org.drip.analytics.date.*;
import org.drip.analytics.definition.LatentStateStatic;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.market.product.*;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.period.*;
import org.drip.param.valuation.*;
import org.drip.product.calib.*;
import org.drip.product.creator.*;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
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
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * OTCFixFloatSwaps contains curve construction and valuation of common OTC IRS.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OTCFixFloatSwaps {

	private static final FixFloatComponent OTCIRS (
		final JulianDate dtSpot,
		final String strCurrency,
		final String strLocation,
		final String strMaturityTenor,
		final String strIndex,
		final double dblCoupon)
	{
		FixFloatConvention ffConv = FixFloatContainer.ConventionFromJurisdiction (
			strCurrency,
			strLocation,
			strMaturityTenor,
			strIndex
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

		ComposableFloatingUnitSetting cfus = new ComposableFloatingUnitSetting (
			"3M",
			CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE,
			null,
			ForwardLabel.Create (strCurrency, "3M"),
			CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
			0.
		);

		CompositePeriodSetting cps = new CompositePeriodSetting (
			4,
			"3M",
			strCurrency,
			null,
			1.,
			null,
			null,
			null,
			null
		);

		CashSettleParams csp = new CashSettleParams (
			0,
			strCurrency,
			0
		);

		for (int i = 0; i < aiDay.length; ++i) {
			aDeposit[i] = new SingleStreamComponent (
				"DEPOSIT_" + aiDay[i],
				new Stream (
					CompositePeriodBuilder.FloatingCompositeUnit (
						CompositePeriodBuilder.EdgePair (
							dtEffective,
							dtEffective.addBusDays (aiDay[i], strCurrency)
						),
						cps,
						cfus
					)
				),
				csp
			);

			aDeposit[i].setPrimaryCode (aiDay[i] + "D");
		}

		return aDeposit;
	}

	private static final LatentStateStretchSpec DepositStretch (
		final SingleStreamComponent[] aDeposit,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aDeposit.length];

		for (int i = 0; i < aDeposit.length; ++i) {
			FloatingStreamQuoteSet depositQuote = new FloatingStreamQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FUNDING,
						LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						FundingLabel.Standard (aDeposit[i].payCurrency())
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
						aDeposit[i].forwardLabel().get ("DERIVED")
					)
				}
			);

			depositQuote.setForwardRate (adblQuote[i]);

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
		final SingleStreamComponent[] aEDF,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aEDF.length];

		for (int i = 0; i < aEDF.length; ++i) {
			FloatingStreamQuoteSet edfQuote = new FloatingStreamQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FUNDING,
						LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						FundingLabel.Standard (aEDF[i].payCurrency())
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
						aEDF[i].forwardLabel().get ("DERIVED")
					)
				}
			);

			edfQuote.setForwardRate (adblQuote[i]);

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
		final JulianDate dtSpot,
		final String strCurrency,
		final String strLocation,
		final String strIndex,
		final String[] astrMaturityTenor)
		throws Exception
	{
		FixFloatComponent[] aIRS = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aIRS[i] = OTCIRS (
				dtSpot,
				strCurrency,
				strLocation,
				astrMaturityTenor[i],
				strIndex,
				0.
			);

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
						LatentStateStatic.LATENT_STATE_FUNDING,
						LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						FundingLabel.Standard (aIRS[i].payCurrency())
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
						aIRS[i].forwardLabel().get ("DERIVED")
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
	 * This sample demonstrates discount curve calibration and input instrument calibration quote recovery.
	 * 	It shows the following:
	 * 	- Construct the Array of Cash/Swap Instruments and their Quotes from the given set of parameters.
	 * 	- Construct the Cash/Swap Instrument Set Stretch Builder.
	 * 	- Set up the Linear Curve Calibrator using the following parameters:
	 * 		- Cubic Exponential Mixture Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
	 * 		of Cash and Swap Stretches.
	 * 	- Cross-Comparison of the Cash/Swap Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void OTCRun (
		final JulianDate dtSpot,
		final String strCurrency,
		final String strLocation,
		final String[] astrOTCMaturityTenor,
		final String strIndex)
		throws Exception
	{
		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		SingleStreamComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
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

		SingleStreamComponent[] aEDFComp = SingleStreamComponentBuilder.FuturesPack (
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
			strLocation,
			strIndex,
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
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null
			),
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
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Deposit, Futures, and Swap Stretches.
		 */

		DiscountCurve dc = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aStretchSpec,
			valParams,
			null,
			null,
			null,
			1.
		);

		CurveSurfaceQuoteSet csqs = MarketParamsBuilder.Create (
			dc,
			null,
			null,
			null,
			null,
			null,
			null
		);

		System.out.print ("\t[" + strCurrency + " | " + strLocation + "] = ");

		for (int i = 0; i < astrOTCMaturityTenor.length; ++i) {
			FixFloatComponent swap = OTCIRS (
				dtSpot,
				strCurrency,
				strLocation,
				astrOTCMaturityTenor[i],
				strIndex,
				0.
			);

			Map<String, Double> mapOutput = swap.value (
				valParams,
				null,
				csqs,
				null
			);

			System.out.print (
				FormatUtil.FormatDouble (mapOutput.get ("CalibSwapRate"), 1, 4, 100.) + "% (" +
				FormatUtil.FormatDouble (mapOutput.get ("FairPremium"), 1, 4, 100.) + "%) || "
			);
		}

		System.out.println();
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

		String[] astrOTCMaturityTenor = new String[] {
			"1Y", "3Y", "5Y", "7Y", "10Y"
		};

		System.out.println ("\n\t--------------------------------------------------------------------------------------------------------------------------------");

		System.out.println ("\t JURISDICTION             1Y      ||          3Y         ||          5Y         ||          7Y         ||         10Y         ||");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");

		OTCRun (dtToday, "AUD", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "CAD", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "CHF", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "CNY", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "DKK", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "EUR", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "GBP", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "HKD", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "INR", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "JPY", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "JPY", "ALL", astrOTCMaturityTenor, "TIBOR");

		OTCRun (dtToday, "NOK", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "NZD", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "PLN", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "SEK", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "SGD", "ALL", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "USD", "LON", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "USD", "NYC", astrOTCMaturityTenor, "MAIN");

		OTCRun (dtToday, "ZAR", "ALL", astrOTCMaturityTenor, "MAIN");

		System.out.println ("\t--------------------------------------------------------------------------------------------------------------------------------");
	}
}

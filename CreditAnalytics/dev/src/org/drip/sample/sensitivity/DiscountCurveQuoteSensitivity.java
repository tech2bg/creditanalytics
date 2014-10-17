
package org.drip.sample.sensitivity;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.definition.LatentStateStatic;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.PeriodBuilder;
import org.drip.param.creator.*;
import org.drip.param.valuation.*;
import org.drip.product.calib.*;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.rates.*;
import org.drip.quant.calculus.WengertJacobian;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.*;
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
 * DiscountCurveQuoteSensitivity demonstrates the calculation of the discount curve sensitivity to the
 * 	calibration instrument quotes. It does the following:
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
 * 	- Display of the Cash Instrument Discount Factor Quote Jacobian Sensitivities.
 * 	- Display of the Swap Instrument Discount Factor Quote Jacobian Sensitivities.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DiscountCurveQuoteSensitivity {

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

		for (int i = 0; i < aiDay.length; ++i)
			aDeposit[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (
					aiDay[i],
					strCurrency
				),
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				strCurrency
			);

		return aDeposit;
	}

	private static final LatentStateStretchSpec DepositStretch (
		final SingleStreamComponent[] aDeposit,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aDeposit.length];

		String strCurrency = aDeposit[0].payCurrency()[0];

		for (int i = 0; i < aDeposit.length; ++i) {
			FloatingStreamQuoteSet depositQuote = new FloatingStreamQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FUNDING,
						LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						FundingLabel.Standard (strCurrency)
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
						aDeposit[i].forwardLabel()[0]
					)
				}
			);

			depositQuote.set ("ForwardRate", adblQuote[i]);

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
						FundingLabel.Standard (aEDF[i].payCurrency()[0])
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
						aEDF[i].forwardLabel()[0]
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

	private static final GenericFixFloatComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrMaturityTenor)
		throws Exception
	{
		GenericFixFloatComponent[] aIRS = new GenericFixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			GenericStream floatStream = new GenericStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrMaturityTenor[i],
					Double.NaN,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					2,
					"Act/360",
					false,
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

			GenericStream fixStream = new GenericStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrMaturityTenor[i],
					Double.NaN,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					2,
					"Act/360",
					false,
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

			GenericFixFloatComponent irs = new GenericFixFloatComponent (
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
		final GenericFixFloatComponent[] aIRS,
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
						FundingLabel.Standard (aIRS[i].payCurrency()[0])
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
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

	private static final GenericFixFloatComponent IRS (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strTenor,
		final double dblCoupon)
		throws Exception
	{
		GenericStream fixStream = new GenericStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strTenor,
				Double.NaN,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				2,
				"Act/360",
				false,
				"Act/360",
				false,
				true,
				strCurrency,
				1.,
				null,
				dblCoupon,
				strCurrency,
				strCurrency,
				null,
				null
			)
		);

		GenericStream floatStream = new GenericStream (
			PeriodBuilder.RegularPeriodSingleReset (
				dtEffective.julian(),
				strTenor,
				Double.NaN,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				4,
				"Act/360",
				false,
				"Act/360",
				false,
				true,
				strCurrency,
				-1.,
				null,
				0.,
				strCurrency,
				strCurrency,
				ForwardLabel.Create (strCurrency, "LIBOR", "3M"),
				null
			)
		);

		GenericFixFloatComponent irs = new GenericFixFloatComponent (
			fixStream,
			floatStream,
			null
		);

		irs.setPrimaryCode ("IRS." + strTenor + "." + strCurrency);

		return irs;
	}

	private static final void TenorJack (
		final JulianDate dtStart,
		final String strTenor,
		final String strCurrency,
		final String strManifestMeasure,
		final DiscountCurve dc)
		throws Exception
	{
		CalibratableFixedIncomeComponent irsBespoke = IRS (
			dtStart,
			strCurrency,
			strTenor,
			0.
		);

		WengertJacobian wjDFQuoteBespokeMat = dc.jackDDFDManifestMeasure (
			irsBespoke.maturity(),
			strManifestMeasure
		);

		System.out.println (strTenor + " => " + wjDFQuoteBespokeMat.displayString());
	}

	private static final void Forward6MRateJack (
		final JulianDate dtStart,
		final String strStartTenor,
		final String strManifestMeasure,
		final DiscountCurve dc)
	{
		JulianDate dtBegin = dtStart.addTenor (strStartTenor);

		WengertJacobian wjForwardRate = dc.jackDForwardDManifestMeasure (dtBegin, "6M", strManifestMeasure, 0.5);

		System.out.println ("[" + dtBegin + " | 6M] => " + wjForwardRate.displayString());
	}

	/*
	 * This sample demonstrates the calculation of the discount curve sensitivity to the calibration
	 * 	instrument quotes. It does the following:
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
	 * 	- Display of the Cash Instrument Discount Factor Quote Jacobian Sensitivities.
	 * 	- Display of the Swap Instrument Discount Factor Quote Jacobian Sensitivities.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void DiscountCurveQuoteSensitivitySample (
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{
		/*
		 * Construct the Array of DEPOSIT Instruments and their Quotes from the given set of parameters
		 */

		SingleStreamComponent[] aDeposit = DepositInstrumentsFromMaturityDays (
			dtSpot,
			strCurrency,
			new int[] {
				1, 2, 7, 14, 30, 60
			}
		);

		double[] adblDepositQuote = new double[] {
			0.0013, 0.0017, 0.0017, 0.0018, 0.0020, 0.0023}; // Cash Rate

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec depositStretch = DepositStretch (
			aDeposit,
			adblDepositQuote
		);

		/*
		 * Construct the Array of FUTURE Instruments and their Quotes from the given set of parameters
		 */

		SingleStreamComponent[] aEDF = IRFutureBuilder.GenerateFuturesPack (
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
			aEDF,
			adblEDFQuote
		);

		/*
		 * Construct the Array of SWAP Instruments and their Quotes from the given set of parameters
		 */

		GenericFixFloatComponent[] aSwap = SwapInstrumentsFromMaturityTenor (
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
			aSwap,
			adblSwapQuote
		);

		LatentStateStretchSpec[] aStretchSpec = new LatentStateStretchSpec[] {
			depositStretch,
			edfStretch,
			swapStretch
		};

		/*
		 * Set up the Linear Curve Calibrator using the following Default Segment Control parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		LinearLatentStateCalibrator lcc = new LinearLatentStateCalibrator (
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null)),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null
		);

		/*
		 * Set up the DEPOSIT Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			depositStretch.name(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null))
		);

		/*
		 * Set up the FUTURE Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE OFF, RETAIN ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			edfStretch.name(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (false, 1, null)
			)
		);

		/*
		 * Set up the SWAP Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			swapStretch.name(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null))
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

		/*
		 * Cross-Comparison of the DEPOSIT Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDeposit.length; ++i)
			System.out.println ("\t[" + aDeposit[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aDeposit[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the FUTURE Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     FUTURE INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aEDF.length; ++i)
			System.out.println ("\t[" + aEDF[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aEDF[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblEDFQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the SWAP Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwap.length; ++i)
			System.out.println ("\t[" + aSwap[i].maturity() + "] = " +
				FormatUtil.FormatDouble (aSwap[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblSwapQuote[i], 1, 6, 1.));

		/*
		 * Display of the DEPOSIT Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDeposit.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wj = dc.jackDDFDManifestMeasure (aDeposit[i].maturity(), "PV");

			System.out.println (aDeposit[i].maturity() + " => " + wj.displayString());
		}

		/*
		 * Display of the FUTURE Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     FUTURE MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aEDF.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wj = dc.jackDDFDManifestMeasure (aEDF[i].maturity(), "PV");

			System.out.println (aEDF[i].maturity() + " => " + wj.displayString());
		}

		/*
		 * Display of the SWAP Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwap.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wjDFQuote = dc.jackDDFDManifestMeasure (aSwap[i].maturity(), "PV");

			System.out.println (aSwap[i].maturity() + " => " + wjDFQuote.displayString());
		}

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     COMPONENT-BY-COMPONENT QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		WengertJacobian wj = dc.compJackDPVDManifestMeasure (dtSpot);

		System.out.println (wj.displayString());

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     BESPOKE 35Y SWAP QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		CalibratableFixedIncomeComponent irs35Y = IRS (
			dtSpot,
			strCurrency,
			"35Y",
			0.
		);

		WengertJacobian wjIRSBespokeQuoteJack = irs35Y.jackDDirtyPVDManifestMeasure (
			valParams,
			null,
			MarketParamsBuilder.Create (dc, null, null, null, null, null, null, null),
			null
		);

		System.out.println (wjIRSBespokeQuoteJack.displayString());

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     BESPOKE SWAP MATURITY QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		TenorJack (dtSpot, "30Y", strCurrency, "PV", dc);

		TenorJack (dtSpot, "32Y", strCurrency, "PV", dc);

		TenorJack (dtSpot, "34Y", strCurrency, "PV", dc);

		TenorJack (dtSpot, "36Y", strCurrency, "PV", dc);

		TenorJack (dtSpot, "38Y", strCurrency, "PV", dc);

		TenorJack (dtSpot, "40Y", strCurrency, "PV", dc);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DISCOUNT CURVE IMPLIED 6M FORWARD RATE QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		Forward6MRateJack (dtSpot, "1D", "PV", dc);

		Forward6MRateJack (dtSpot, "3M", "PV", dc);

		Forward6MRateJack (dtSpot, "6M", "PV", dc);

		Forward6MRateJack (dtSpot, "1Y", "PV", dc);

		Forward6MRateJack (dtSpot, "2Y", "PV", dc);

		Forward6MRateJack (dtSpot, "5Y", "PV", dc);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		String strCurrency = "USD";

		DiscountCurveQuoteSensitivitySample (JulianDate.Today(), strCurrency);
	}
}


package org.drip.sample.rates;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.creator.*;
import org.drip.param.market.ComponentMarketParamSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.quant.calculus.WengertJacobian;
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
	 * Construct the Array of Cash Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addBusDays (aiDay[i], "USD"), "USD");

		CalibratableComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, "USD");

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor)
		throws Exception
	{
		CalibratableComponent[] aCalibComp = new CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = RatesStreamBuilder.CreateIRS (dtEffective,
				dtEffective.addTenorAndAdjust (astrTenor[i], "USD"), 0., "USD", "USD-LIBOR-6M", "USD");

		return aCalibComp;
	}

	private static final void TenorJack (
		final JulianDate dtStart,
		final String strTenor,
		final DiscountCurve dc)
	{
		RatesComponent irsBespoke = RatesStreamBuilder.CreateIRS (
			dtStart, dtStart.addTenorAndAdjust (strTenor, "USD"),
			0.,
			"USD",
			"USD-LIBOR-6M",
			"USD");

		WengertJacobian wjDFQuoteBespokeMat = dc.jackDDFDQuote (irsBespoke.getMaturityDate());

		System.out.println (strTenor + " => " + wjDFQuoteBespokeMat.displayString());
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

	private static final void DiscountCurveQuoteSensitivitySample()
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

		CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtToday,
			new int[] {1, 2, 7, 14, 30, 60},
			8);

		double[] adblCashQuote = new double[] {
			0.0013, 0.0017, 0.0017, 0.0018, 0.0020, 0.0023, // Cash Rate
			0.0027, 0.0032, 0.0041, 0.0054, 0.0077, 0.0104, 0.0134, 0.0160}; // EDF Rate;

		/*
		 * Construct the Cash Instrument Set Stretch Builder
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
		 * Construct the Array of Swap Instruments and their Quotes from the given set of parameters
		 */

		CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (dtToday, new java.lang.String[]
			{"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"});

		double[] adblSwapQuote = new double[]
			{0.0166, 0.0206, 0.0241, 0.0269, 0.0292, 0.0311, 0.0326, 0.0340, 0.0351, 0.0375, 0.0393, 0.0402, 0.0407, 0.0409, 0.0409};

		/*
		 * Construct the Swap Instrument Set Stretch Builder
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
				SegmentDesignInelasticControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingQuoteSensitivityControl (true, 1, null)),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		/*
		 * Set up the CASH Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			rbsCash.getName(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentDesignInelasticControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingQuoteSensitivityControl (true, 1, null)));

		/*
		 * Set up the SWAP Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			rbsSwap.getName(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentDesignInelasticControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingQuoteSensitivityControl (true, 1, null)));

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Cash and Swap Stretches.
		 */

		DiscountCurve dc = RatesScenarioCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aRBS,
			new ValuationParams (dtToday, dtToday, "USD"),
			null,
			null,
			null,
			1.);

		/*
		 * Cross-Comparison of the Cash Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     CASH INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i)
			System.out.println ("\t[" + aCashComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aCashComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblCashQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the Swap Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i)
			System.out.println ("\t[" + aSwapComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aSwapComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblSwapQuote[i], 1, 6, 1.));

		/*
		 * Display of the Cash Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     CASH/FUTURE MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCashComp.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wj = dc.jackDDFDQuote (aCashComp[i].getMaturityDate());

			System.out.println (aCashComp[i].getMaturityDate() + " => " + wj.displayString());
		}

		/*
		 * Display of the Swap Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     SWAP MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSwapComp.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wjDFQuote = dc.jackDDFDQuote (aSwapComp[i].getMaturityDate());

			System.out.println (aSwapComp[i].getMaturityDate() + " => " + wjDFQuote.displayString());
		}

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     COMPONENT-BY-COMPONENT QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		WengertJacobian wj = dc.compJackDPVDQuote (dtToday);

		System.out.println (wj.displayString());

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     BESPOKE 35Y SWAP QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		RatesComponent irs35Y = RatesStreamBuilder.CreateIRS (
			dtToday, dtToday.addTenorAndAdjust ("35Y", "USD"),
			0.,
			"USD",
			"USD-LIBOR-6M",
			"USD");

		WengertJacobian wjIRSBespokeQuoteJack = irs35Y.jackDDirtyPVDQuote (
			new ValuationParams (dtToday, dtToday, "USD"),
			null,
			new ComponentMarketParamSet (dc, null, null, null, null, null, null, null),
			null);

		System.out.println (wjIRSBespokeQuoteJack.displayString());

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     BESPOKE SWAP MATURITY QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		TenorJack (dtToday, "30Y", dc);

		TenorJack (dtToday, "32Y", dc);

		TenorJack (dtToday, "34Y", dc);

		TenorJack (dtToday, "36Y", dc);

		TenorJack (dtToday, "38Y", dc);

		TenorJack (dtToday, "40Y", dc);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		DiscountCurveQuoteSensitivitySample();
	}
}

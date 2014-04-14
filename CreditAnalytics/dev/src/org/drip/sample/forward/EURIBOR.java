
package org.drip.sample.forward;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.rates.*;
import org.drip.param.creator.*;
import org.drip.param.definition.ComponentMarketParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.DepositBuilder;
import org.drip.product.definition.*;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
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
 * EURIBOR illustrates the Construction and Usage of the EURIBOR Forward Curve.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EURIBOR {

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final RatesComponent[] DepositFromMaturityDays (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final FloatingRateIndex fri)
		throws Exception
	{
		RatesComponent[] aDeposit = new RatesComponent[astrMaturityTenor.length];

		String strCurrency = fri.currency();

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aDeposit[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addTenorAndAdjust (astrMaturityTenor[i], strCurrency),
				fri,
				strCurrency);

		return aDeposit;
	}

	/*
	 * Construct an array of fix-float swaps from the fixed reference and the xM floater derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] FixFloatSwap (
		final JulianDate dtEffective,
		final FloatingRateIndex fri,
		final int iForwardTenorFreq,
		final String[] astrMaturityTenor,
		final double[] adblCoupon)
		throws Exception
	{
		String strCurrency = fri.currency();

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		FixFloatComponent[] aFFC = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrMaturityTenor[i], strCurrency);

			/*
			 * The Fixed Leg
			 */

			FixedStream fixStream = new FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
				adblCoupon[i], 2, "30/360", "30/360", false, null, dap, dap, dap, dap, dap, null, null, -1.,
					strCurrency, strCurrency);

			/*
			 * The Derived Leg
			 */

			FloatingStream fsDerived = FloatingStream.Create (dtEffective.getJulian(), dtMaturity.getJulian(), 0.,
				false, fri, iForwardTenorFreq, "Act/360", false, "Act/360", false, false, null, dap, dap, dap, dap,
					dap, dap, null, null, 1., strCurrency, strCurrency);

			/*
			 * The fix-float swap instance
			 */

			aFFC[i] = new FixFloatComponent (fixStream, fsDerived);
		}

		return aFFC;
	}

	/*
	 * Construct an array of float-float swaps from the corresponding reference (6M) and the derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FloatFloatComponent[] FloatFloatSwap (
		final JulianDate dtEffective,
		final FloatingRateIndex fri,
		final int iForwardTenorFreq,
		final String[] astrMaturityTenor)
		throws Exception
	{
		String strCurrency = fri.currency();

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrMaturityTenor[i], strCurrency);

			/*
			 * The Reference 6M Leg
			 */

			FloatingStream fsReference = FloatingStream.Create (dtEffective.getJulian(), dtMaturity.getJulian(),
				0., true, FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"), 2, "Act/360", false, "Act/360",
					false, false, null, dap, dap, dap, dap, dap, dap, null, null, -1., strCurrency, strCurrency);

			/*
			 * The Derived Leg
			 */

			FloatingStream fsDerived = FloatingStream.Create (dtEffective.getJulian(), dtMaturity.getJulian(),
				0., false, fri, iForwardTenorFreq, "Act/360", false, "Act/360", false, false, null, dap, dap,
					dap, dap, dap, dap, null, null, 1., strCurrency, strCurrency);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (fsReference, fsDerived);
		}

		return aFFC;
	}

	public static final ForwardCurve CustomEURIBORBuilderSample (
		final DiscountCurve dc,
		final FloatingRateIndex fri,
		final SegmentCustomBuilderControl scbc,
		final String[] astrDepositTenor,
		final double[] adblDepositQuote,
		final String[] astrFixFloatTenor,
		final double[] adblFixFloatQuote,
		final String[] astrFloatFloatTenor,
		final double[] adblFloatFloatQuote,
		final String[] astrSyntheticFloatFloatTenor,
		final double[] adblSyntheticFloatFloatQuote,
		final String strHeaderComment)
		throws Exception
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     " + strHeaderComment);

		System.out.println ("\t----------------------------------------------------------------");

		JulianDate dtValue = dc.epoch();

		CalibratableFixedIncomeComponent[] aDepositComp = DepositFromMaturityDays (
			dtValue,
			astrDepositTenor,
			fri
		);

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec srsDeposit = StretchRepresentationSpec.CreateStretchBuilderSet (
			"DEPOSIT",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aDepositComp,
			"ForwardRate",
			adblDepositQuote,
			null);

		FixFloatComponent[] aFixFloat = FixFloatSwap (
			dtValue,
			fri,
			12,
			astrFixFloatTenor,
			adblFixFloatQuote);

		/*
		 * Construct the Fix-Float Component Set Stretch Builder
		 */

		StretchRepresentationSpec srsFixFloat = StretchRepresentationSpec.CreateStretchBuilderSet (
			"FIXFLOAT",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aFixFloat,
			"DerivedParBasisSpread",
			adblFixFloatQuote,
			null);

		FloatFloatComponent[] aFloatFloat = FloatFloatSwap (
			dtValue,
			fri,
			12,
			astrFloatFloatTenor
		);

		/*
		 * Construct the Float-Float Component Set Stretch Builder
		 */

		StretchRepresentationSpec srsFloatFloat = StretchRepresentationSpec.CreateStretchBuilderSet (
			"FLOATFLOAT",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aFloatFloat,
			"DerivedParBasisSpread",
			adblFloatFloatQuote,
			null);

		FloatFloatComponent[] aSyntheticFloatFloat = FloatFloatSwap (
			dtValue,
			fri,
			12,
			astrSyntheticFloatFloatTenor
		);

		/*
		 * Construct the Synthetic Float-Float Component Set Stretch Builder
		 */

		StretchRepresentationSpec srsSyntheticFloatFloat = StretchRepresentationSpec.CreateStretchBuilderSet (
			"SYNTHETICFLOATFLOAT",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aSyntheticFloatFloat,
			"DerivedParBasisSpread",
			adblSyntheticFloatFloatQuote,
			null);

		StretchRepresentationSpec[] aSRS = new StretchRepresentationSpec[] {
			srsDeposit,
			srsFixFloat,
			srsFloatFloat,
			srsSyntheticFloatFloat
		};

		/*
		 * Set up the Linear Curve Calibrator using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			scbc,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		ValuationParams valParams = new ValuationParams (dtValue, dtValue, fri.currency());

		/*
		 * Set the discount curve based component market parameters.
		 */

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null);

		/*
		 * Construct the Shape Preserving Forward Curve by applying the linear curve calibrator to the array
		 *  of Deposit and Swap Stretches.
		 */

		ForwardCurve fc = ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			lcc,
			aSRS,
			fri,
			valParams,
			null,
			cmp,
			null,
			adblDepositQuote[0]);

		/*
		 * Set the discount curve + cubic polynomial forward curve based component market parameters.
		 */

		ComponentMarketParams cmpFwd = ComponentMarketParamsBuilder.CreateComponentMarketParams
			(dc, fc, null, null, null, null, null, null);

		/*
		 * Cross-Comparison of the Deposit Calibration Instrument "Forward" metric.
		 */

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT INSTRUMENTS QUOTE RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i)
			System.out.println ("\t[" + aDepositComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aDepositComp[i].calcMeasureValue (valParams, null, cmpFwd, null, "Forward"), 1, 6, 1.) +
					" | " + FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the Fix-Float Calibration Instrument "DerivedParBasisSpread" metric.
		 */

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t     FIX-FLOAT INSTRUMENTS QUOTE RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aFixFloat.length; ++i)
			System.out.println ("\t[" + aFixFloat[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aFixFloat[i].calcMeasureValue (valParams, null, cmpFwd, null, "DerivedParBasisSpread"), 1, 2, 1.) +
					" | " + FormatUtil.FormatDouble (adblFixFloatQuote[i], 1, 2, 10000.));

		/*
		 * Cross-Comparison of the Float-Float Calibration Instrument "DerivedParBasisSpread" metric.
		 */

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t     FLOAT-FLOAT INSTRUMENTS QUOTE RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aFloatFloat.length; ++i)
			System.out.println ("\t[" + aFloatFloat[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aFloatFloat[i].calcMeasureValue (valParams, null, cmpFwd, null, "DerivedParBasisSpread"), 1, 2, 1.) +
					" | " + FormatUtil.FormatDouble (adblFloatFloatQuote[i], 1, 2, 10000.));

		/*
		 * Cross-Comparison of the Synthetic Float-Float Calibration Instrument "DerivedParBasisSpread" metric.
		 */

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t     SYNTHETIC FLOAT-FLOAT INSTRUMENTS QUOTE RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aSyntheticFloatFloat.length; ++i)
			System.out.println ("\t[" + aSyntheticFloatFloat[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aSyntheticFloatFloat[i].calcMeasureValue (valParams, null, cmpFwd, null, "DerivedParBasisSpread"), 1, 2, 1.) +
					" | " + FormatUtil.FormatDouble (adblSyntheticFloatFloatQuote[i], 1, 2, 10000.));

		return fc;
	}

	private static final void ForwardJack (
		final JulianDate dt,
		final ForwardCurve fc,
		final String strStartDateTenor)
	{
		JulianDate dtJack = dt.addTenor (strStartDateTenor);

		System.out.println ("\t" + 
			dtJack + " | " +
			strStartDateTenor + ": " +
			fc.jackDForwardDManifestMeasure (
				"DerivedParBasisSpread",
				dtJack).displayString()
			);
	}

	public static final void ForwardJack (
		final JulianDate dt,
		final String strHeaderComment,
		final ForwardCurve fc)
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t" + strHeaderComment);

		System.out.println ("\t----------------------------------------------------------------");

		ForwardJack (dt, fc, "1Y");

		ForwardJack (dt, fc, "2Y");

		ForwardJack (dt, fc, "3Y");

		ForwardJack (dt, fc, "5Y");

		ForwardJack (dt, fc, "7Y");
	}
}

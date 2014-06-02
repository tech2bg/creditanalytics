
package org.drip.sample.fx;

import java.util.ArrayList;
import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.definition.BasketMarketParams;
import org.drip.param.definition.ComponentMarketParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.fx.CrossCurrencySwapPair;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.sample.forward.*;
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
 * ForwardCurveFromCCBS demonstrates the setup and construction of the Forward Curve from the CCBS Quotes.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ForwardCurveFromCCBS {

	/*
	 * Construct an array of float-float swaps from the corresponding reference (6M) and the derived legs.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FloatFloatComponent[] MakexM6MBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			/*
			 * The Reference 6M Leg
			 */

			FloatingStream fsReference = FloatingStream.Create (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., true, FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"),
					2, "Act/360", false, "Act/360", false, false, null, dap, dap, dap, dap, dap, dap,
						null, null, -1., strCurrency, strCurrency);

			/*
			 * The Derived Leg
			 */

			FloatingStream fsDerived = FloatingStream.Create (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., false, FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
					12 / iTenorInMonths, "Act/360", false, "Act/360", false, false, null, dap, dap, dap, dap, dap, dap,
						null, null, 1., strCurrency, strCurrency);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (fsReference, fsDerived);

			aFFC[i].setPrimaryCode (strCurrency + "_6M::" + iTenorInMonths + "M::" + astrTenor[i]);
		}

		return aFFC;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtValue = JulianDate.CreateFromYMD (2012, JulianDate.DECEMBER, 11);

		String strReferenceCurrency = "EUR";
		String strDerivedCurrency = "USD";
		String[] astrTenor = new String[] {"1Y", "2Y", "3Y", "4Y", "5Y"};

		FloatFloatComponent[] aFFCReference = MakexM6MBasisSwap (
			dtValue,
			strReferenceCurrency,
			astrTenor,
			3);

		FloatFloatComponent[] aFFCDerived = MakexM6MBasisSwap (
			dtValue,
			"USD",
			astrTenor,
			3);

		CrossCurrencySwapPair[] aCCSP = new CrossCurrencySwapPair[astrTenor.length];

		for (int i = 0; i < aCCSP.length; ++i)
			aCCSP[i] = new CrossCurrencySwapPair ("EURUSD_" + astrTenor[i], aFFCReference[i], aFFCDerived[i]);

		SegmentCustomBuilderControl scbcCubic = new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (4),
			SegmentInelasticDesignControl.Create (2, 2),
			new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
			null);

		DiscountCurve dcReference = EONIA.MakeDC (
			dtValue,
			strReferenceCurrency,
			false);

		ForwardCurve fc6MReference = EURIBOR6MCubicPolyVanilla.Make6MForward (
			dtValue,
			strReferenceCurrency,
			"6M");

		ForwardCurve fc3MReference = EURIBOR3MCubicPolyVanilla.MakeForward3M (
			"3M",
			strReferenceCurrency,
			dtValue,
			dcReference,
			fc6MReference,
			scbcCubic);

		DiscountCurve dcDerived = EONIA.MakeDC (
			dtValue,
			strDerivedCurrency,
			false);

		ForwardCurve fc6MDerived = EURIBOR6MCubicPolyVanilla.Make6MForward (
			dtValue,
			strDerivedCurrency,
			"6M");

		BasketMarketParams bmp = BasketMarketParamsBuilder.CreateBasketMarketParams();

		bmp.addDiscountCurve (strReferenceCurrency, dcReference);

		bmp.addDiscountCurve (strDerivedCurrency, dcDerived);

		bmp.addForwardCurve (fc6MReference.index().fullyQualifiedName(), fc6MReference);

		bmp.addForwardCurve (fc3MReference.index().fullyQualifiedName(), fc3MReference);

		System.out.println ("\n--------------------\n");

		ValuationParams valParams = new ValuationParams (dtValue, dtValue, strReferenceCurrency);

		List<CaseInsensitiveTreeMap<java.lang.Double>> lsMapManifestMeasureQuote = new ArrayList<CaseInsensitiveTreeMap<Double>>();

		for (int i = 0; i < astrTenor.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapOP = aCCSP[i].value (valParams, null, bmp, null);

			double dblPVReference = mapOP.get (aFFCReference[i].componentName() + "[pv]");

			System.out.println (dblPVReference);

			CaseInsensitiveTreeMap<Double> mapManifestQuote = new CaseInsensitiveTreeMap<Double>();

			mapManifestQuote.put ("PV", dblPVReference);

			lsMapManifestMeasureQuote.add (mapManifestQuote);
		}

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			scbcCubic,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		StretchRepresentationSpec srsFloatFloat = new StretchRepresentationSpec (
			"FLOATFLOAT",
			ForwardCurve.LATENT_STATE_FORWARD,
			ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
			aFFCDerived,
			lsMapManifestMeasureQuote,
			null);

		ForwardCurve fc3MDerived = ScenarioForwardCurveBuilder.ShapePreservingForwardCurve (
			lcc,
			new StretchRepresentationSpec[] {srsFloatFloat},
			FloatingRateIndex.Create (strDerivedCurrency + "-LIBOR-3M"),
			valParams,
			null,
			ComponentMarketParamsBuilder.CreateComponentMarketParams (dcDerived, fc6MDerived, null, null, null, null, null),
			null,
			dcDerived.forward (dtValue.getJulian(), dtValue.addTenor ("3M").getJulian()));

		ComponentMarketParams cmpDerived = ComponentMarketParamsBuilder.CreateComponentMarketParams
			(dcDerived, fc3MDerived, null, null, null, null, null);

		cmpDerived.setForwardCurve (fc6MDerived);

		double[] adblPVDerived = new double[astrTenor.length];

		System.out.println ("\n--------------------\n");

		for (int i = 0; i < astrTenor.length; ++i) {
			adblPVDerived[i] = aFFCDerived[i].measureValue (valParams, null, cmpDerived, null, "PV");

			System.out.println (adblPVDerived[i]);
		}
	}
}

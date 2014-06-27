
package org.drip.sample.xccy;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.market.MarketParamSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.RatesStreamBuilder;
import org.drip.product.definition.RatesComponent;
import org.drip.product.fx.CrossCurrencyComponentPair;
import org.drip.product.params.*;
import org.drip.product.rates.*;
import org.drip.quant.calculus.WengertJacobian;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.spline.params.SegmentCustomBuilderControl;
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
 * CCBSDiscountCurve demonstrates the setup and construction of the Forward Curve from the CCBS Quotes.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CCBSDiscountCurve {

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
		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {

			/*
			 * The Reference 6M Leg
			 */

			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.getJulian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream fsReference = new FloatingStream (
				strCurrency,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"),
				false
			);

			/*
			 * The Derived Leg
			 */

			List<CashflowPeriod> lsDerivedFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.getJulian(),
				astrTenor[i],
				null,
				12 / iTenorInMonths,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream fsDerived = new FloatingStream (
				strCurrency,
				0.,
				1.,
				null,
				lsDerivedFloatPeriods,
				FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
				false
			);

			/*
			 * The float-float swap instance
			 */

			aFFC[i] = new FloatFloatComponent (fsReference, fsDerived);

			aFFC[i].setPrimaryCode (strCurrency + "_6M::" + iTenorInMonths + "M::" + astrTenor[i]);
		}

		return aFFC;
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final IRSComponent[] MakeIRS (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrTenor)
		throws Exception
	{
		IRSComponent[] aCalibComp = new IRSComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = (IRSComponent) RatesStreamBuilder.CreateIRS (
				dtEffective,
				astrTenor[i],
				0.,
				2,
				"Act/360",
				0.,
				4,
				"Act/360",
				strCurrency,
				strCurrency
			);

		return aCalibComp;
	}

	private static final CrossCurrencyComponentPair[] MakeCCSP (
		final JulianDate dtValue,
		final String strReferenceCurrency,
		final String strDerivedCurrency,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		FloatFloatComponent[] aFFCReference = MakexM6MBasisSwap (
			dtValue,
			strReferenceCurrency,
			astrTenor,
			3);

		IRSComponent[] aIRS = MakeIRS (
			dtValue,
			strDerivedCurrency,
			astrTenor);

		CrossCurrencyComponentPair[] aCCSP = new CrossCurrencyComponentPair[astrTenor.length];

		for (int i = 0; i < aCCSP.length; ++i)
			aCCSP[i] = new CrossCurrencyComponentPair ("EURUSD_" + astrTenor[i], aFFCReference[i], aIRS[i]);

		return aCCSP;
	}

	private static final void TenorJack (
		final JulianDate dtStart,
		final String strTenor,
		final String strManifestMeasure,
		final DiscountCurve dc)
	{
		String strCurrency = dc.currency();

		RatesComponent irsBespoke = RatesStreamBuilder.CreateIRS (
			dtStart,
			strTenor,
			0.,
			2,
			"Act/360",
			0.,
			4,
			"Act/360",
			strCurrency,
			strCurrency
		);

		WengertJacobian wjDFQuoteBespokeMat = dc.jackDDFDManifestMeasure (
			irsBespoke.maturity(),
			strManifestMeasure
		);

		System.out.println ("\t" + strTenor + " => " + wjDFQuoteBespokeMat.displayString());
	}

	public static final void MakeDiscountCurve (
		final String strReferenceCurrency,
		final String strDerivedCurrency,
		final JulianDate dtValue,
		final DiscountCurve dcReference,
		final ForwardCurve fc6MReference,
		final ForwardCurve fc3MReference,
		final double dblRefDerFX,
		final SegmentCustomBuilderControl scbc,
		final String[] astrTenor,
		final double[] adblCrossCurrencyBasis,
		final double[] adblSwapRate,
		final boolean bBasisOnDerivedLeg)
		throws Exception
	{
		List<CaseInsensitiveTreeMap<Double>> lsCCBSMapManifestQuote = new ArrayList<CaseInsensitiveTreeMap<Double>>();

		List<CaseInsensitiveTreeMap<Double>> lsIRSMapManifestQuote = new ArrayList<CaseInsensitiveTreeMap<Double>>();

		for (int i = 0; i < astrTenor.length; ++i) {
			CaseInsensitiveTreeMap<Double> mapIRSManifestQuote = new CaseInsensitiveTreeMap<Double>();

			mapIRSManifestQuote.put ("Rate", adblSwapRate[i]);

			lsIRSMapManifestQuote.add (mapIRSManifestQuote);

			CaseInsensitiveTreeMap<Double> mapCCBSManifestQuote = new CaseInsensitiveTreeMap<Double>();

			mapCCBSManifestQuote.put ("DerivedParBasisSpread", adblCrossCurrencyBasis[i]);

			lsCCBSMapManifestQuote.add (mapCCBSManifestQuote);
		}

		CrossCurrencyComponentPair[] aCCSP = MakeCCSP (
			dtValue,
			strReferenceCurrency,
			strDerivedCurrency,
			astrTenor,
			3);

		MarketParamSet bmp = new MarketParamSet();

		bmp.setFundingCurve (dcReference);

		bmp.setForwardCurve (fc3MReference);

		bmp.setForwardCurve (fc6MReference);

		bmp.setFXCurve (CurrencyPair.FromCode (strDerivedCurrency + "/" + strReferenceCurrency), new FlatUnivariate (1. / dblRefDerFX));

		bmp.setFXCurve (CurrencyPair.FromCode (strReferenceCurrency + "/" + strDerivedCurrency), new FlatUnivariate (dblRefDerFX));

		ValuationParams valParams = new ValuationParams (dtValue, dtValue, strReferenceCurrency);

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			scbc,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		StretchRepresentationSpec srsIRS = CCBSStretchRepresentationBuilder.DiscountCurveSRS (
			"FIXFLOAT",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aCCSP,
			valParams,
			bmp,
			adblCrossCurrencyBasis,
			adblSwapRate,
			bBasisOnDerivedLeg
		);

		DiscountCurve dcDerived = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			new StretchRepresentationSpec[] {srsIRS},
			valParams,
			null,
			null,
			null,
			1.
		);

		bmp.setFundingCurve (dcDerived);

		System.out.println ("\t----------------------------------------------------------------");

		if (bBasisOnDerivedLeg)
			System.out.println ("\t     IRS INSTRUMENTS QUOTE REVISION FROM CCBS DERIVED BASIS INPUTS");
		else
			System.out.println ("\t     IRS INSTRUMENTS QUOTE REVISION FROM CCBS REFERENCE BASIS INPUTS");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCCSP.length; ++i) {
			RatesComponent rcDerived = aCCSP[i].derivedComponent();

			CaseInsensitiveTreeMap<Double> mapOP = aCCSP[i].value (valParams, null, bmp, null);

			double dblCalibSwapRate = mapOP.get (rcDerived.name() + "[CalibSwapRate]");

			System.out.println ("\t[" + rcDerived.effective() + " - " + rcDerived.maturity() + "] = " +
				FormatUtil.FormatDouble (dblCalibSwapRate, 1, 3, 100.) +
					"% | " + FormatUtil.FormatDouble (adblSwapRate[i], 1, 3, 100.) + "% | " +
						FormatUtil.FormatDouble (adblSwapRate[i] - dblCalibSwapRate, 2, 0, 10000.) + " | " +
							FormatUtil.FormatDouble (dcDerived.df (rcDerived.maturity()), 1, 4, 1.));
		}

		System.out.println ("\t----------------------------------------------------------------------");

		if (bBasisOnDerivedLeg)
			System.out.println ("\t     CCBS DERIVED BASIS TENOR JACOBIAN");
		else
			System.out.println ("\t     CCBS REFERENCE BASIS TENOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------------");

		for (int i = 0; i < aCCSP.length; ++i)
			TenorJack (
				dtValue,
				astrTenor[i],
				"Rate",
				dcDerived
			);
	}
}


package org.drip.sample.ccbs;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.cashflow.FloatingStream;
import org.drip.product.creator.RatesStreamBuilder;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.fx.ComponentPair;
import org.drip.product.params.*;
import org.drip.product.rates.*;
import org.drip.quant.calculus.WengertJacobian;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.spline.params.SegmentCustomBuilderControl;
import org.drip.spline.stretch.*;
import org.drip.state.estimator.*;
import org.drip.state.identifier.*;
import org.drip.state.inference.*;

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
		final CurrencyPair cp,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		FloatFloatComponent[] aFFC = new FloatFloatComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {

			/*
			 * The Reference 6M Leg
			 */

			FloatingStream fsReference = new FloatingStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrTenor[i],
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
					ForwardLabel.Standard (strCurrency + "-LIBOR-6M"),
					null
				)
			);

			/*
			 * The Derived Leg
			 */

			FloatingStream fsDerived = new FloatingStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrTenor[i],
					Double.NaN,
					null,
					12 / iTenorInMonths,
					"Act/360",
					false,
					false,
					strCurrency,
					1.,
					null,
					0.,
					strCurrency,
					ForwardLabel.Standard (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
					null
				)
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

	private static final FixFloatComponent[] MakeIRS (
		final JulianDate dtEffective,
		final String strCurrency,
		final String[] astrTenor)
		throws Exception
	{
		FixFloatComponent[] aCalibComp = new FixFloatComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i)
			aCalibComp[i] = RatesStreamBuilder.CreateFixFloat (
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

	private static final ComponentPair[] MakeCCSP (
		final JulianDate dtValue,
		final String strReferenceCurrency,
		final String strDerivedCurrency,
		final CurrencyPair cp,
		final String[] astrTenor,
		final int iTenorInMonths)
		throws Exception
	{
		FloatFloatComponent[] aFFCReference = MakexM6MBasisSwap (
			dtValue,
			strReferenceCurrency,
			cp,
			astrTenor,
			3);

		FixFloatComponent[] aIRS = MakeIRS (
			dtValue,
			strDerivedCurrency,
			astrTenor);

		ComponentPair[] aCCSP = new ComponentPair[astrTenor.length];

		for (int i = 0; i < aCCSP.length; ++i)
			aCCSP[i] = new ComponentPair ("EURUSD_" + astrTenor[i], aFFCReference[i], aIRS[i]);

		return aCCSP;
	}

	private static final void TenorJack (
		final JulianDate dtStart,
		final String strTenor,
		final String strManifestMeasure,
		final DiscountCurve dc)
	{
		String strCurrency = dc.currency();

		CalibratableFixedIncomeComponent irsBespoke = RatesStreamBuilder.CreateFixFloat (
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

		ComponentPair[] aCCSP = MakeCCSP (
			dtValue,
			strReferenceCurrency,
			strDerivedCurrency,
			CurrencyPair.FromCode (strDerivedCurrency + "/" + strReferenceCurrency),
			astrTenor,
			3
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcReference);

		mktParams.setForwardCurve (fc3MReference);

		mktParams.setForwardCurve (fc6MReference);

		FXLabel fxLabelBase = FXLabel.Standard (CurrencyPair.FromCode (strDerivedCurrency + "/" + strReferenceCurrency));

		FXLabel fxLabelInverse = FXLabel.Standard (CurrencyPair.FromCode (strReferenceCurrency + "/" + strDerivedCurrency));

		mktParams.setFXCurve (fxLabelBase, new FlatUnivariate (1. / dblRefDerFX));

		mktParams.setFXCurve (fxLabelInverse, new FlatUnivariate (dblRefDerFX));

		mktParams.setFixing (aCCSP[0].effective(), fxLabelBase, dblRefDerFX);

		mktParams.setFixing (aCCSP[0].effective(), fxLabelInverse, 1. / dblRefDerFX);

		ValuationParams valParams = new ValuationParams (dtValue, dtValue, strReferenceCurrency);

		LinearLatentStateCalibrator llsc = new LinearLatentStateCalibrator (
			scbc,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		LatentStateStretchSpec stretchSpec = CCBSStretchBuilder.DiscountStretch (
			"FIXFLOAT",
			aCCSP,
			valParams,
			mktParams,
			adblCrossCurrencyBasis,
			adblSwapRate,
			bBasisOnDerivedLeg
		);

		DiscountCurve dcDerived = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			llsc,
			new LatentStateStretchSpec[] {stretchSpec},
			valParams,
			null,
			null,
			null,
			1.
		);

		mktParams.setFundingCurve (dcDerived);

		System.out.println ("\t----------------------------------------------------------------");

		if (bBasisOnDerivedLeg)
			System.out.println ("\t     IRS INSTRUMENTS QUOTE REVISION FROM CCBS DERIVED BASIS INPUTS");
		else
			System.out.println ("\t     IRS INSTRUMENTS QUOTE REVISION FROM CCBS REFERENCE BASIS INPUTS");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aCCSP.length; ++i) {
			CalibratableFixedIncomeComponent rcDerived = aCCSP[i].derivedComponent();

			CaseInsensitiveTreeMap<Double> mapOP = aCCSP[i].value (valParams, null, mktParams, null);

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
				"PV",
				dcDerived
			);
	}
}

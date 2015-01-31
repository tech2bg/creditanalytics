
package org.drip.sample.xccy;

import org.drip.analytics.date.*;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.market.otc.*;
import org.drip.param.creator.ScenarioForwardCurveBuilder;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.params.CurrencyPair;
import org.drip.product.rates.FloatFloatComponent;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;
import org.drip.state.identifier.*;

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
 * OTCCrossCurrencySwaps demonstrates the Construction and Valuation of the Cross-Currency Floating Swap of
 *  OTC contracts.
 *
 * @author Lakshmi Krishnamurthy
 */

public class OTCCrossCurrencySwaps {

	private static final FloatFloatComponent OTCCrossCurrencyFloatFloat (
		final String strReferenceCurrency,
		final String strDerivedCurrency,
		final JulianDate dtSpot,
		final String strMaturityTenor,
		final double dblBasis,
		final double dblDerivedNotionalScaler)
	{
		CrossFloatSwapConvention ccfc = CrossFloatConventionContainer.ConventionFromJurisdiction (
			strReferenceCurrency,
			strDerivedCurrency
		);

		return ccfc.createFloatFloatComponent (
			dtSpot,
			strMaturityTenor,
			dblBasis,
			1.,
			-1. * dblDerivedNotionalScaler
		);
	}

	private static final void OTCCrossCurrencyRun (
		final JulianDate dtSpot,
		final String strReferenceCurrency,
		final String strDerivedCurrency,
		final String strMaturityTenor,
		final double dblBasis,
		final double dblReferenceDerivedFXRate)
		throws Exception
	{
		double dblReferenceFundingRate = 0.02;
		double dblDerived3MForwardRate = 0.02;

		double dblReferenceFundingVol = 0.3;
		double dblDerivedForward3MVol = 0.3;
		double dblReferenceDerivedFXVol = 0.3;

		double dblDerived3MReferenceDerivedFXCorr = 0.1;
		double dblReferenceFundingDerived3MCorr = 0.1;
		double dblReferenceFundingReferenceDerivedFXCorr = 0.1;

		DiscountCurve dcReferenceFunding = DiscountCurveBuilder.CreateFromFlatRate (
			dtSpot,
			strReferenceCurrency,
			new CollateralizationParams (
				"OVERNIGHT_INDEX",
				strReferenceCurrency
			),
			dblReferenceFundingRate
		);

		ForwardLabel friDerived3M = ForwardLabel.Create (
			strDerivedCurrency,
			"3M"
		);

		ForwardCurve fcDerived3M = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtSpot,
			friDerived3M,
			dblDerived3MForwardRate,
			new CollateralizationParams (
				"OVERNIGHT_INDEX",
				strDerivedCurrency
			)
		);

		CurrencyPair cp = CurrencyPair.FromCode (
			strReferenceCurrency + "/" + strDerivedCurrency
		);

		FXLabel fxLabel = FXLabel.Standard (cp);

		FundingLabel fundingLabelReference = org.drip.state.identifier.FundingLabel.Standard (
			strReferenceCurrency
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setForwardCurve (
			fcDerived3M
		);

		mktParams.setFundingCurve (
			dcReferenceFunding
		);

		mktParams.setFXCurve (
			fxLabel,
			new FlatUnivariate (
				dblReferenceDerivedFXRate
			)
		);

		mktParams.setForwardCurveVolSurface (
			friDerived3M,
			new FlatUnivariate (
				dblDerivedForward3MVol
			)
		);

		mktParams.setFundingCurveVolSurface (
			fundingLabelReference,
			new FlatUnivariate (
				dblReferenceFundingVol
			)
		);

		mktParams.setFXCurveVolSurface (
			fxLabel,
			new FlatUnivariate (
				dblReferenceDerivedFXVol
			)
		);

		mktParams.setForwardFundingCorrSurface (
			friDerived3M,
			fundingLabelReference,
			new FlatUnivariate (
				dblReferenceFundingDerived3MCorr
			)
		);

		mktParams.setForwardFXCorrSurface (
			friDerived3M,
			fxLabel,
			new FlatUnivariate (
				dblDerived3MReferenceDerivedFXCorr
			)
		);

		mktParams.setFundingFXCorrSurface (
			fundingLabelReference,
			fxLabel,
			new FlatUnivariate (
				dblReferenceFundingReferenceDerivedFXCorr
			)
		);

		FloatFloatComponent xccySwap = OTCCrossCurrencyFloatFloat (
			strReferenceCurrency,
			strDerivedCurrency,
			dtSpot,
			strMaturityTenor,
			dblBasis,
			1. / dblReferenceDerivedFXRate
		);

		xccySwap.setPrimaryCode (
			strDerivedCurrency + "_" + strReferenceCurrency + "_OTC::FLOATFLOAT::" + strMaturityTenor
		);

		mktParams.setFixing (
			xccySwap.effectiveDate(),
			fxLabel,
			dblReferenceDerivedFXRate
		);

		ValuationParams valParams = new ValuationParams (
			dtSpot,
			dtSpot,
			strReferenceCurrency + "," + strDerivedCurrency
		);

		CaseInsensitiveTreeMap<Double> mapXCcyOutput = xccySwap.value (
			valParams,
			null,
			mktParams,
			null
		);

		System.out.println (
			"\t| " + xccySwap.name() + "  [" + xccySwap.effectiveDate() + " -> " + xccySwap.maturityDate() + "]  =>  " +
			FormatUtil.FormatDouble (mapXCcyOutput.get ("Price"), 1, 2, 1.) + "  |  " +
			FormatUtil.FormatDouble (mapXCcyOutput.get ("DerivedParBasisSpread"), 1, 2, 1.) + "  |  " +
			FormatUtil.FormatDouble (mapXCcyOutput.get ("ReferenceParBasisSpread"), 1, 2, 1.) + "  |  " +
			FormatUtil.FormatDouble (mapXCcyOutput.get ("DerivedCleanDV01"), 1, 2, 10000.) + "  |  " +
			FormatUtil.FormatDouble (mapXCcyOutput.get ("ReferenceCleanDV01"), 1, 2, 10000.) + "  |"
		);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtSpot = DateUtil.Today();

		System.out.println ("\t---------------------------------------------------------");

		System.out.println ("\t\tCROSS-CURRENCY FLOAT-FLOAT COMPONENT RUNS");

		System.out.println ("\t---------------------------------------------------------");

		System.out.println ("\tL -> R:");

		System.out.println ("\t\tCross Currency Swap Name");

		System.out.println ("\t\tFloat-Float Effective");

		System.out.println ("\t\tFloat-Float Maturity");

		System.out.println ("\t\tPrice");

		System.out.println ("\t\tDerived Stream Par Basis Spread");

		System.out.println ("\t\tReference Stream Par Basis Spread");

		System.out.println ("\t\tAnnualized Derived Stream Duration");

		System.out.println ("\t\tAnnualized Reference Stream Duration");

		System.out.println ("\t------------------------------------------------------------------------------------------------------------------");

		OTCCrossCurrencyRun (dtSpot, "USD", "AUD", "2Y", 0.0003, 0.7769);

		OTCCrossCurrencyRun (dtSpot, "USD", "CAD", "2Y", 0.0003, 0.7861);

		OTCCrossCurrencyRun (dtSpot, "USD", "CHF", "2Y", 0.0003, 1.0811);

		OTCCrossCurrencyRun (dtSpot, "USD", "CLP", "2Y", 0.0003, 0.0016);

		OTCCrossCurrencyRun (dtSpot, "USD", "DKK", "2Y", 0.0003, 0.1517);

		OTCCrossCurrencyRun (dtSpot, "USD", "EUR", "2Y", 0.0003, 1.1294);

		OTCCrossCurrencyRun (dtSpot, "USD", "GBP", "2Y", 0.0003, 1.5004);

		OTCCrossCurrencyRun (dtSpot, "USD", "JPY", "2Y", 0.0003, 0.0085);

		OTCCrossCurrencyRun (dtSpot, "USD", "MXN", "2Y", 0.0003, 0.0666);

		OTCCrossCurrencyRun (dtSpot, "USD", "NOK", "2Y", 0.0003, 0.1288);

		OTCCrossCurrencyRun (dtSpot, "USD", "PLN", "2Y", 0.0003, 0.2701);

		OTCCrossCurrencyRun (dtSpot, "USD", "SEK", "2Y", 0.0003, 0.1211);

		System.out.println ("\t------------------------------------------------------------------------------------------------------------------");
	}
}

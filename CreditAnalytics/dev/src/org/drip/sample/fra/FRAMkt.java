
package org.drip.sample.fra;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.fra.FRAMarketComponent;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.sample.forward.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.identifier.*;

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
 * FRAMkt contains the demonstration of the Market Multi-Curve FRA product sample.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FRAMkt {
	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		String strTenor = "6M";
		String strCurrency = "EUR";
		double dblEURIBOR6MVol = 0.37;
		double dblMultiplicativeQuantoExchangeVol = 0.1;
		double dblFRIQuantoExchangeCorr = 0.2;
		double dblEONIAVol = 0.37;
		double dblEONIAEURIBOR6MCorrelation = 0.8;

		JulianDate dtToday = JulianDate.Today().addTenor ("0D");

		DiscountCurve dcEONIA = OvernightIndexCurve.MakeDC (
			dtToday,
			strCurrency
		);

		ForwardCurve fcEURIBOR6M = IBOR6MQuarticPolyVanilla.Make6MForward (
			dtToday,
			strCurrency,
			strTenor);

		ForwardLabel fri = ForwardLabel.Create (strCurrency + "-LIBOR-" + strTenor);

		FundingLabel fundingLabel = FundingLabel.Standard (strCurrency);

		JulianDate dtForward = dtToday.addTenor (strTenor);

		FRAMarketComponent fra = new FRAMarketComponent (
			1.,
			strCurrency,
			fri.fullyQualifiedName(),
			strCurrency,
			dtForward.julian(),
			fri,
			0.006,
			"Act/360");

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create
			(dcEONIA, fcEURIBOR6M, null, null, null, null, null, null);

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strCurrency);

		mktParams.setForwardCurveVolSurface (
			fri,
			new FlatUnivariate (dblEURIBOR6MVol)
		);

		mktParams.setCustomMetricVolSurface (
			CustomMetricLabel.Standard ("ForwardToDomesticExchangeVolatility"),
			dtForward,
			new FlatUnivariate (dblMultiplicativeQuantoExchangeVol)
		);

		mktParams.setCustomMetricVolSurface (
			CustomMetricLabel.Standard ("FRIForwardToDomesticExchangeCorrelation"),
			dtForward,
			new FlatUnivariate (dblFRIQuantoExchangeCorr)
		);

		mktParams.setFundingCurveVolSurface (
			fundingLabel,
			new FlatUnivariate (dblEONIAVol)
		);

		mktParams.setForwardFundingCorrSurface (
			fri,
			fundingLabel,
			new FlatUnivariate (dblEONIAEURIBOR6MCorrelation)
		);

		Map<String, Double> mapFRAOutput = fra.value (valParams, null, mktParams, null);

		for (Map.Entry<String, Double> me : mapFRAOutput.entrySet())
			System.out.println ("\t" + me.getKey() + " => " + me.getValue());
	}
}

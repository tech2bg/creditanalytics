
package org.drip.sample.fra;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.param.creator.*;
import org.drip.param.definition.ComponentMarketParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.fra.FRAMarketComponent;
import org.drip.product.params.FloatingRateIndex;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.sample.forward.*;
import org.drip.service.api.CreditAnalytics;

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

		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", strCurrency);

		DiscountCurve dcEONIA = OvernightIndexCurve.MakeDC (
			dtToday,
			strCurrency,
			false);

		ForwardCurve fcEURIBOR6M = IBOR6MQuarticPolyVanilla.Make6MForward (
			dtToday,
			strCurrency,
			strTenor);

		FloatingRateIndex fri = FloatingRateIndex.Create (strCurrency + "-LIBOR-" + strTenor);

		JulianDate dtForward = dtToday.addTenor (strTenor);

		FRAMarketComponent fra = new FRAMarketComponent (
			1.,
			strCurrency,
			fri.fullyQualifiedName(),
			strCurrency,
			dtForward.getJulian(),
			fri,
			0.006,
			"Act/360");

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams
			(dcEONIA, fcEURIBOR6M, null, null, null, null, null);

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strCurrency);

		cmp.setVolSurface (
			fri.fullyQualifiedName(),
			dtForward,
			new FlatUnivariate (dblEURIBOR6MVol)
		);

		cmp.setVolSurface (
			"ForwardToDomesticExchangeVolatility",
			dtForward,
			new FlatUnivariate (dblMultiplicativeQuantoExchangeVol)
		);

		cmp.setVolSurface (
			"FRIForwardToDomesticExchangeCorrelation",
			dtForward,
			new FlatUnivariate (dblFRIQuantoExchangeCorr)
		);

		cmp.setVolSurface (
			dcEONIA.name() + "_VOL_TS",
			dtForward,
			new FlatUnivariate (dblEONIAVol)
		);

		cmp.setVolSurface (
			fri.fullyQualifiedName() + "_VOL_TS",
			dtForward,
			new FlatUnivariate (dblEURIBOR6MVol)
		);

		cmp.setVolSurface (
			dcEONIA.name() + "::" + fri.fullyQualifiedName() + "_VOL_TS",
			dtForward,
			new FlatUnivariate (dblEONIAEURIBOR6MCorrelation)
		);

		Map<String, Double> mapFRAOutput = fra.value (valParams, null, cmp, null);

		for (Map.Entry<String, Double> me : mapFRAOutput.entrySet())
			System.out.println ("\t" + me.getKey() + " => " + me.getValue());
	}
}

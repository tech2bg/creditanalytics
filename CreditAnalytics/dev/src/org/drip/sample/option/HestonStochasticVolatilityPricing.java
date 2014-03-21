
package org.drip.sample.option;

import org.drip.analytics.date.JulianDate;
import org.drip.param.valuation.ValuationParams;
import org.drip.pricer.option.*;
import org.drip.product.option.EuropeanCallPut;

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
 * HestonStochasticVolatilityPricing contains an illustration of the Stochastic Volatility based Pricing
 *  Algorithm of an European Call Using the Heston Algorithm.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class HestonStochasticVolatilityPricing {
	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		JulianDate dtMaturity = dtToday.addTenor ("6M");

		double dblStrike = 1.;

		EuropeanCallPut call = new EuropeanCallPut (
			dtMaturity,
			dblStrike,
			false);

		EuropeanCallPut put = new EuropeanCallPut (
			dtMaturity,
			dblStrike,
			true);

		double dblSpot = 1.;
		double dblRiskFreeRate = 0.05;

		double dblRho = 0.3;
		double dblKappa = 1.;
		double dblSigma = 0.5;
		double dblTheta = 0.2;
		double dblLambda = 0.;
		double dblSpotVolatility = 0.2;

		FPHestonParams fphp = new FPHestonParams (
			dblRho, 			// Rho
			dblKappa,			// Kappa
			dblSigma,			// Sigma
			dblTheta,			// Theta
			dblLambda);			// Lambda

		FokkerPlanckGenerator fpg = new HestonStochasticVolatilityAlgorithm (
			fphp);				// FP Heston Parameters

		System.out.println (call.value (valParams, dblSpot, dblRiskFreeRate, dblSpotVolatility, fpg));

		System.out.println (put.value (valParams, dblSpot, dblRiskFreeRate, dblSpotVolatility, fpg));
	}
}

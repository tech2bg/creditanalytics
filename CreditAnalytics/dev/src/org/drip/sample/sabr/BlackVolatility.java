
package org.drip.sample.sabr;

import org.drip.dynamics.sabr.*;
import org.drip.quant.common.FormatUtil;
import org.drip.sequence.random.BoxMullerGaussian;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.identifier.ForwardLabel;

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
 * BlackVolatility demonstrates the Construction and Usage of the SABR Model to Imply the Black Volatility of
 *  a given Contract.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BlackVolatility {

	private static StochasticVolatilityStateEvolver SABREvolver (
		final double dblBeta,
		final double dblRho,
		final double dblVolatilityOfVolatility)
		throws Exception
	{
		return new StochasticVolatilityStateEvolver (
			ForwardLabel.Create ("USD", "6M"),
			dblBeta,
			dblRho,
			dblVolatilityOfVolatility,
			new BoxMullerGaussian (0., 1.),
			new BoxMullerGaussian (0., 1.)
		);
	}

	private static void VolatilitySurface (
		final StochasticVolatilityStateEvolver seSABR,
		final double[] adblStrike,
		final double dblATMForwardRate,
		final double dblTTE,
		final double dblSigma0)
	{
		String strDump = "\t| " + FormatUtil.FormatDouble (dblTTE, 1, 2, 1.) + " => ";

		for (int i = 0; i < adblStrike.length; ++i) {
			ImpliedBlackVolatility ibv = seSABR.computeBlackVolatility (
				adblStrike[i],
				dblATMForwardRate,
				dblTTE,
				dblSigma0
			);

			strDump += FormatUtil.FormatDouble (ibv.impliedVolatility(), 2, 1, 100.) + " | ";
		}

		System.out.println (strDump);
	}

	public static void main (
		final String[] astrArgs)
		throws Exception
	{
		CreditAnalytics.Init ("");

		double dblRho = 0.;
		double dblBeta = 0.7;
		double dblATMForwardRate = 0.04;
		double dblVolatilityOfVolatility = 0.5;
		double adblForwardRateVolatility = 0.10;
		double[] adblStrike = {0.30, 0.35, 0.40, 0.45, 0.50};
		double[] adblTTE = {0.25, 0.50, 0.75, 1.00, 2.00, 3.00, 4.00, 5.00, 7.00, 9.99};

		StochasticVolatilityStateEvolver seSABR = SABREvolver (
			dblBeta,
			dblRho,
			dblVolatilityOfVolatility
		);

		System.out.println ("\n\t|------------------------------------------------|");

		System.out.println ("\t|         SABR IMPLIED BLACK VOLATILITY          |");

		System.out.println ("\t|------------------------------------------------|");

		for (double dblTTE : adblTTE)
			VolatilitySurface (
				seSABR,
				adblStrike,
				dblATMForwardRate,
				dblTTE,
				adblForwardRateVolatility
			);

		System.out.println ("\t|------------------------------------------------|");
	}
}

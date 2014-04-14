
package org.drip.sample.forward;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.product.params.FloatingRateIndex;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.MultiSegmentSequenceBuilder;

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
 * EURIBOR 1M illustrates the Construction and Usage of the EURIBOR 1M Forward Curve Using Vanilla Quartic
 * 	Polynomial.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EURIBOR1MQuarticPolyVanilla {
	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtValue = JulianDate.CreateFromYMD (2012, JulianDate.DECEMBER, 11);

		String strTenor = "1M";
		String strCurrency = "EUR";

		FloatingRateIndex fri = FloatingRateIndex.Create (strCurrency + "-LIBOR-" + strTenor);

		DiscountCurve dcEONIA = EONIA.MakeDC (
			dtValue,
			strCurrency,
			false);

		SegmentCustomBuilderControl scbcQuartic = new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (5),
			SegmentInelasticDesignControl.Create (2, 2),
			new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
			null);

		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		double[] adblDepositQuote = new double[] {
			0.000661,
			0.000980,
			0.000993
		};

		String[] astrDepositTenor = new String[] {
			"1D",
			"7D",
			"14D"
		};

		/*
		 * Construct the Array of Fix-Float Component and their Quotes from the given set of parameters
		 */

		double[] adblFixFloatQuote = new double[] {
			0.001100,
			0.001060,
			0.000960,
			0.000850,
			0.000790,
			0.000750,
			0.000710,
			0.000690,
			0.000660,
			0.000650,
			0.000640,
			0.000630
		};

		String[] astrFixFloatTenor = new String[] {
			 "1M",
			 "2M",
			 "3M",
			 "4M",
			 "5M",
			 "6M",
			 "7M",
			 "8M",
			 "9M",
			"10M",
			"11M",
			"12M"
		};

		/*
		 * Construct the Array of Float-Float Component and their Quotes from the given set of parameters
		 */

		double[] adblFloatFloatQuote = new double[] {
			0.000980,
			0.001860,
			0.003300,
			0.005120,
			0.007040,
			0.008870,
			0.010580,
			0.012110,
			0.013470,
			0.014700,
			0.015810,
			0.018260,
			0.019980,
			0.020590,
			0.020930
		};

		String[] astrFloatFloatTenor = new String[] {
			  "2Y",
			  "3Y",
			  "4Y",
			  "5Y",
			  "6Y",
			  "7Y",
			  "8Y",
			  "9Y",
			 "10Y",
			 "11Y",
			 "12Y",
			 "15Y",
			 "20Y",
			 "25Y",
			 "30Y"
		};

		/*
		 * Construct the Array of Terminal Synthetic Float-Float Components and their Quotes from the given set of parameters
		 */

		String[] astrSyntheticFloatFloatTenor = new String[] {
			"35Y",
			"40Y",
			"50Y",
			"60Y"
		};

		double[] adblSyntheticFloatFloatQuote = new double[] {
			0.021320,
			0.021850,
			0.022580,
			0.023000
		};

		ForwardCurve fc = EURIBOR.CustomEURIBORBuilderSample (
			dcEONIA,
			fri,
			scbcQuartic,
			astrDepositTenor,
			adblDepositQuote,
			astrFixFloatTenor,
			adblFixFloatQuote,
			astrFloatFloatTenor,
			adblFloatFloatQuote,
			astrSyntheticFloatFloatTenor,
			adblSyntheticFloatFloatQuote,
			"---- VANILLA QUARTIC POLYNOMIAL FORWARD CURVE ---");

		EURIBOR.ForwardJack (
			dtValue,
			"---- VANILLA QUARTIC POLYNOMIAL FORWARD CURVE SENSITIVITY ---",
			fc);
	}
}

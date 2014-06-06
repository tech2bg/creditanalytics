
package org.drip.sample.forward;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.product.params.FloatingRateIndex;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.ExponentialTensionSetParams;
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
 * This Sample illustrates the Construction and Usage of the EURIBOR 12M Forward Curve Using Vanilla Cubic
 * 	KLK Hyperbolic Tension B-Splines.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EURIBOR12MCubicKLKHyperbolic {
	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtValue = JulianDate.CreateFromYMD (2012, JulianDate.DECEMBER, 11);

		String strTenor = "12M";
		String strCurrency = "EUR";

		FloatingRateIndex fri = FloatingRateIndex.Create (strCurrency + "-LIBOR-" + strTenor);

		DiscountCurve dcEONIA = EONIA.MakeDC (
			dtValue,
			strCurrency,
			false);

		SegmentCustomBuilderControl scbcCubicKLKHyperbolic = new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
			new ExponentialTensionSetParams (1.),
			SegmentInelasticDesignControl.Create (2, 2),
			new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
			null);

		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		double[] adblDepositQuote = new double[] {
			0.006537,
			0.006187,
			0.005772,
			0.005563,
			0.005400
		};

		String[] astrDepositTenor = new String[] {
			 "1M",
			 "3M",
			 "6M",
			 "9M",
			"12M"
		};

		/*
		 * Construct the Array of Fix-Float Component and their Quotes from the given set of parameters
		 */

		double[] adblFRAQuote = new double[] {
			0.004974,
			0.004783,
			0.004822,
			0.005070,
			0.005481,
			0.006025
		};

		String[] astrFRATenor = new String[] {
			 "3M",
			 "6M",
			 "9M",
			"12M",
			"15M",
			"18M",
		};

		/*
		 * Construct the Array of Float-Float Component and their Quotes from the given set of parameters
		 */

		double[] adblFloatFloatQuote = new double[] {
			-0.002070,	//  3Y
			-0.001640,	//  4Y
			-0.001510,	//  5Y
			-0.001390,	//  6Y
			-0.001300,	//  7Y
			-0.001230,	//  8Y
			-0.001180,	//  9Y
			-0.001130,	// 10Y
			-0.001090,	// 11Y
			-0.001060,	// 12Y
			-0.000930,	// 15Y
			-0.000800,	// 20Y
			-0.000720,	// 25Y
			-0.000660	// 30Y
		};

		String[] astrFloatFloatTenor = new String[] {
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
			-0.000660,
			-0.000660,
			-0.000660,
			-0.000660
		};

		ForwardCurve fc6M = EURIBOR6MCubicPolyVanilla.Make6MForward (
			dtValue,
			strCurrency,
			"6M",
			true);

		ForwardCurve fc = IBOR.CustomEURIBORBuilderSample (
			dcEONIA,
			fc6M,
			fri,
			scbcCubicKLKHyperbolic,
			astrDepositTenor,
			adblDepositQuote,
			"ForwardRate",
			astrFRATenor,
			adblFRAQuote,
			"ParForwardRate",
			null,
			null,
			"ReferenceParBasisSpread",
			astrFloatFloatTenor,
			adblFloatFloatQuote,
			"ReferenceParBasisSpread",
			astrSyntheticFloatFloatTenor,
			adblSyntheticFloatFloatQuote,
			"ReferenceParBasisSpread",
			"---- VANILLA CUBIC KLK HYPERBOLIC TENSION B-SPLINE FORWARD CURVE ---",
			true);

			IBOR.ForwardJack (
				dtValue,
				"---- VANILLA CUBIC KLK HYPERBOLIC TENSION B-SPLINE FORWARD CURVE SENSITIVITY ---",
				fc);
	}
}

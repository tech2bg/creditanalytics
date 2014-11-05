
package org.drip.sample.forward;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.ExponentialTensionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.MultiSegmentSequenceBuilder;
import org.drip.state.identifier.ForwardLabel;

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
 * This Sample illustrates the Construction and Usage of the IBOR 1M Forward Curve Using Vanilla Cubic
 * 	KLK Hyperbolic Tension B-Splines.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class IBOR1MCubicKLKHyperbolic {
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

		ForwardLabel fri = ForwardLabel.Standard (strCurrency + "-LIBOR-" + strTenor);

		DiscountCurve dcEONIA = OvernightIndexCurve.MakeDC (
			dtValue,
			strCurrency
		);

		SegmentCustomBuilderControl scbcCubicKLKHyperbolic = new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
			new ExponentialTensionSetParams (1.),
			SegmentInelasticDesignControl.Create (2, 2),
			new ResponseScalingShapeControl (
				true,
				new QuadraticRationalShapeControl (0.)
			),
			null
		);

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
			0.002260,	//  2Y
			0.002380,	//  3Y
			0.002460,	//  4Y
			0.002500,	//  5Y
			0.002500,	//  6Y
			0.002480,	//  7Y
			0.002450,	//  8Y
			0.002410,	//  9Y
			0.002370,	// 10Y
			0.002330,	// 11Y
			0.002280,	// 12Y
			0.002110,	// 15Y
			0.001890,	// 20Y
			0.001750,	// 25Y
			0.001630	// 30Y
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
			0.001630,
			0.001630,
			0.001630,
			0.001630
		};

		ForwardCurve fc6M = IBOR6MCubicPolyVanilla.Make6MForward (
			dtValue,
			strCurrency,
			"6M",
			true
		);

		ForwardCurve fc = IBOR.CustomIBORBuilderSample (
			dcEONIA,
			fc6M,
			fri,
			scbcCubicKLKHyperbolic,
			astrDepositTenor,
			adblDepositQuote,
			"ForwardRate",
			null,
			null,
			"ParForwardRate",
			astrFixFloatTenor,
			adblFixFloatQuote,
			"SwapRate",
			astrFloatFloatTenor,
			adblFloatFloatQuote,
			"DerivedParBasisSpread",
			astrSyntheticFloatFloatTenor,
			adblSyntheticFloatFloatQuote,
			"DerivedParBasisSpread",
			"---- VANILLA CUBIC KLK HYPERBOLIC TENSION B-SPLINE FORWARD CURVE ---",
			true
		);

		IBOR.ForwardJack (
			dtValue,
			"---- VANILLA CUBIC KLK HYPERBOLIC TENSION B-SPLINE FORWARD CURVE SENSITIVITY ---",
			fc,
			"DerivedParBasisSpread"
		);
	}
}

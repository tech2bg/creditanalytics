
package org.drip.sample.fx;

/*
 * General imports
 */

import java.util.Random;

/*
 * Credit Product imports
 */


import org.drip.analytics.date.DateUtil;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.definition.*;
import org.drip.analytics.rates.ExplicitBootDiscountCurve;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;
import org.drip.product.params.*;
import org.drip.product.creator.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * FXAPI contains a demo of the FX API Sample. It shows the following:
 * 	- Create a currency pair, FX SPot, and FX Forward.
 * 	- Calculate the FX forward PIP/outright.
 * 	- Calculate the DC Basis on the domestic and the foreign curves.
 * 	- Create an FX curve from the spot, and the array of nodes, FX forward, as well as the PIP indicator.
 * 	- Calculate the array of the domestic/foreign basis.
 * 	- Calculate the array of bootstrapped domestic/foreign basis.
 * 	- Re-imply the array of FX Forward from domestic/foreign Basis Curve.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FXAPI {

	/*
	 * Sample demonstrating the creation/usage of the FX API
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void DisplayFXAPI()
		throws Exception
	{
		/*
		 * Create a currency pair
		 */

		CurrencyPair cp = new CurrencyPair ("EUR", "USD", "USD", 10000.);

		Random rand = new Random();

		/*
		 * Create a the USD and EUR discount curves
		 */

		ExplicitBootDiscountCurve dcUSD = DiscountCurveBuilder.CreateFromFlatRate (DateUtil.Today(), "USD", null, 0.05);

		ExplicitBootDiscountCurve dcEUR = DiscountCurveBuilder.CreateFromFlatRate (DateUtil.Today(), "EUR", null, 0.04);

		double dblFXSpot = 1.40;
		double dblFXFwdMarket = 1.40;
		double[] adblNodes = new double[5];
		double[] adblFXFwd = new double[5];
		boolean[] abIsPIP = new boolean[5];

		for (int i = 0; i < 5; ++i) {
			abIsPIP[i] = false;

			adblFXFwd[i] = dblFXSpot - (i + 1) * 0.01 * rand.nextDouble();

			adblNodes[i] = DateUtil.Today().addYears (i + 1).julian();

			System.out.println (cp.code() + "[" + (i + 1) + "]=" + FormatUtil.FormatDouble (adblFXFwd[i], 1, 3, 100.));
		}

		ValuationParams valParams = ValuationParams.Spot (
			DateUtil.Today(),
			0,
			"USD",
			Convention.DATE_ROLL_ACTUAL
		);

		/*
		 * Create the FX forward instrument
		 */

		FXForward fxfwd = FXForwardBuilder.CreateFXForward (cp, DateUtil.Today(), "1Y");

		/*
		 * Calculate the FX forward outright
		 */

		double dblFXFwd = fxfwd.imply (valParams, dcEUR, dcUSD, 1.4, false);

		System.out.println (cp.code() + "[1Y]= " + dblFXFwd);

		/*
		 * Calculate the FX forward PIP
		 */

		double dblFXFwdPIP = fxfwd.imply (valParams, dcEUR, dcUSD, 1.4, true);

		System.out.println (cp.code() + "[1Y](pip)= " + FormatUtil.FormatDouble (dblFXFwdPIP, 1, 3, 100.));

		/*
		 * Calculate the DC Basis on the EUR curve
		 */

		double dblDCEURBasis = fxfwd.discountCurveBasis (valParams, dcEUR, dcUSD, dblFXSpot, dblFXFwdMarket, false);

		System.out.println ("EUR Basis bp for " + cp.code() + "[1Y] = " + dblFXFwdMarket + ": " +
			FormatUtil.FormatDouble (dblDCEURBasis, 1, 3, 100.));

		/*
		 * Calculate the DC Basis on the USD curve
		 */

		double dblDCUSDBasis = fxfwd.discountCurveBasis (valParams, dcEUR, dcUSD, dblFXSpot, dblFXFwdMarket, true);

		System.out.println ("USD Basis bp for " + cp.code() + "[1Y] = " + dblFXFwdMarket + ": " +
			FormatUtil.FormatDouble (dblDCUSDBasis, 1, 3, 100.));

		/*
		 * Create an FX curve from the spot, and the array of nodes, FX forward, as well as the PIP indicator
		 */

		FXForwardCurve fxCurve = FXForwardCurveBuilder.CreateFXForwardCurve
			(cp, DateUtil.Today(), dblFXSpot, adblNodes, adblFXFwd, abIsPIP);

		/*
		 * Calculate the array of the USD basis
		 */

		double[] adblFullUSDBasis = fxCurve.zeroBasis (valParams, dcEUR, dcUSD, true);

		for (int i = 0; i < adblFullUSDBasis.length; ++i)
			System.out.println ("FullUSDBasis[" + (i + 1) + "Y]=" +
				FormatUtil.FormatDouble (adblFullUSDBasis[i], 1, 3, 100.));

		/*
		 * Calculate the array of the EUR basis
		 */

		double[] adblFullEURBasis = fxCurve.zeroBasis (valParams, dcEUR, dcUSD, false);

		for (int i = 0; i < adblFullEURBasis.length; ++i)
			System.out.println ("FullEURBasis[" + (i + 1) + "Y]=" +
				FormatUtil.FormatDouble (adblFullEURBasis[i], 1, 3, 100.));

		/*
		 * Calculate the array of bootstrapped USD basis
		 */

		double[] adblBootstrappedUSDBasis = fxCurve.bootstrapBasis (valParams, dcEUR, dcUSD, true);

		for (int i = 0; i < adblBootstrappedUSDBasis.length; ++i)
			System.out.println ("Bootstrapped USDBasis from FX fwd for " + cp.code() + "[" + (i + 1) + "Y]=" +
				FormatUtil.FormatDouble (adblBootstrappedUSDBasis[i], 1, 3, 100.));

		/*
		 * Calculate the array of bootstrapped EUR basis
		 */

		double[] adblBootstrappedEURBasis = fxCurve.bootstrapBasis (valParams, dcEUR, dcUSD, false);

		for (int i = 0; i < adblBootstrappedEURBasis.length; ++i)
			System.out.println ("Bootstrapped EURBasis from FX fwd for " + cp.code() + "[" + (i + 1) + "Y]=" +
				FormatUtil.FormatDouble (adblBootstrappedEURBasis[i], 1, 3, 100.));

		/*
		 * Create an USD FX Basis Curve from the spot, and the array of nodes, FX Basis
		 */

		FXBasisCurve fxUSDBasisCurve = FXBasisCurveBuilder.CreateFXBasisCurve
			(cp, DateUtil.Today(), dblFXSpot, adblNodes, adblFullUSDBasis, false);

		/*
		 * Re-calculate the array of FX Forward from USD Basis Curve
		 */

		double[] adblFXFwdFromUSDBasis = fxUSDBasisCurve.fxForward (valParams, dcEUR, dcUSD, true, false);

		for (int i = 0; i < adblFXFwdFromUSDBasis.length; ++i)
			System.out.println ("FX Fwd from Bootstrapped USD Basis: " + cp.code() + "[" + (i + 1) + "Y]=" +
				FormatUtil.FormatDouble (adblFXFwdFromUSDBasis[i], 1, 3, 100.));

		/*
		 * Create an EUR FX Basis Curve from the spot, and the array of nodes, FX Basis
		 */

		FXBasisCurve fxEURBasisCurve = FXBasisCurveBuilder.CreateFXBasisCurve
			(cp, DateUtil.Today(), dblFXSpot, adblNodes, adblFullEURBasis, false);

		/*
		 * Re-calculate the array of FX Forward from EUR Basis Curve
		 */

		double[] adblFXFwdFromEURBasis = fxEURBasisCurve.fxForward (valParams, dcEUR, dcUSD, false, false);

		for (int i = 0; i < adblFXFwdFromEURBasis.length; ++i)
			System.out.println ("FX Fwd from Bootstrapped EUR Basis: " + cp.code() + "[" + (i + 1) + "Y]=" +
				FormatUtil.FormatDouble (adblFXFwdFromEURBasis[i], 1, 3, 100.));
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		DisplayFXAPI();
	}
}

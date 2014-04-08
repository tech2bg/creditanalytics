
package org.drip.sample.rates;

/*
 * Credit Analytics Imports
 */

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.analytics.support.CaseInsensitiveTreeMap;

/*
 * Credit Product Imports
 */

import org.drip.param.creator.*;
import org.drip.param.definition.BasketMarketParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;

/*
 * Credit Analytics API Imports
 */

import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * MultiLegSwapAPI illustrates the creation, invocation, and usage of the MultiLegSwap. It shows how to:
 * 	- Create the Discount Curve from the rates instruments.
 *  - Set up the valuation and the market parameters.
 * 	- Create the Rates Basket from the fixed/float streams.
 * 	- Value the Rates Basket.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class MultiLegSwapAPI {

	/*
	 * Sample demonstrating building of rates curve from cash/future/swaps
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static DiscountCurve BuildRatesCurveFromInstruments (
		final JulianDate dtStart,
		final String[] astrCashTenor,
		final double[] adblCashRate,
		final String[] astrIRSTenor,
		final double[] adblIRSRate,
		final double dblBump,
		final String strCurrency)
		throws Exception
	{
		int iNumDCInstruments = astrCashTenor.length + adblIRSRate.length;
		double adblDate[] = new double[iNumDCInstruments];
		double adblRate[] = new double[iNumDCInstruments];
		String astrCalibMeasure[] = new String[iNumDCInstruments];
		double adblCompCalibValue[] = new double[iNumDCInstruments];
		CalibratableFixedIncomeComponent aCompCalib[] = new CalibratableFixedIncomeComponent[iNumDCInstruments];
		String strIndex = strCurrency + "-LIBOR-3M";

		// Cash Calibration

		JulianDate dtCashEffective = dtStart.addBusDays (1, strCurrency);

		for (int i = 0; i < astrCashTenor.length; ++i) {
			astrCalibMeasure[i] = "Rate";
			adblRate[i] = java.lang.Double.NaN;
			adblCompCalibValue[i] = adblCashRate[i] + dblBump;

			aCompCalib[i] = CashBuilder.CreateCash (dtCashEffective,
				new JulianDate (adblDate[i] = dtCashEffective.addTenor (astrCashTenor[i]).getJulian()),
				strCurrency);
		}

		// IRS Calibration

		JulianDate dtIRSEffective = dtStart.addBusDays (2, strCurrency);

		for (int i = 0; i < astrIRSTenor.length; ++i) {
			astrCalibMeasure[i + astrCashTenor.length] = "Rate";
			adblRate[i + astrCashTenor.length] = java.lang.Double.NaN;
			adblCompCalibValue[i + astrCashTenor.length] = adblIRSRate[i] + dblBump;

			aCompCalib[i + astrCashTenor.length] = RatesStreamBuilder.CreateIRS (dtIRSEffective,
				new JulianDate (adblDate[i + astrCashTenor.length] = dtIRSEffective.addTenor (astrIRSTenor[i]).getJulian()),
				0., strCurrency, strIndex, strCurrency);
		}

		/*
		 * Build the IR curve from the components, their calibration measures, and their calibration quotes.
		 */

		return ScenarioDiscountCurveBuilder.NonlinearBuild (dtStart, strCurrency,
			DiscountCurveBuilder.BOOTSTRAP_MODE_CONSTANT_FORWARD, aCompCalib, adblCompCalibValue, astrCalibMeasure, null);
	}

	/*
	 * Sample demonstrating creation of a rates basket instance from component fixed and floating streams
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final RatesBasket MakeRatesBasket (
		final JulianDate dtEffective)
		throws Exception
	{
		/*
		 * Create a sequence of Fixed Streams
		 */

		FixedStream[] aFixedStream = new FixedStream[3];

		org.drip.analytics.daycount.DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, "XYZ");

		aFixedStream[0] = new FixedStream (dtEffective.getJulian(), dtEffective.addTenor ("3Y").getJulian(),
			0.03, 2, "Act/360", "30/360", false, null, null, dap, dap, dap, dap, null, null, 100., "USD", "USD");

		aFixedStream[1] = new FixedStream (dtEffective.getJulian(), dtEffective.addTenor ("5Y").getJulian(),
			0.05, 2, "30/360", "30/360", false, null, null, dap, dap, dap, dap, null, null, 100., "USD", "USD");

		aFixedStream[2] = new FixedStream (dtEffective.getJulian(), dtEffective.addTenor ("7Y").getJulian(),
			0.07, 2, "30/360", "30/360", false, null, null, dap, dap, dap, dap, null, null, 100., "USD", "USD");

		/*
		 * Create a sequence of Float Streams
		 */

		FloatingStream[] aFloatStream = new FloatingStream[3];

		aFloatStream[0] = FloatingStream.Create (dtEffective.getJulian(), dtEffective.addTenor ("3Y").getJulian(),
			0.03, true, FloatingRateIndex.Create ("ABC-RI-3M"), 4, "Act/360", false, "Act/360", false, false, null, null,
				dap, dap, dap, dap, null, null, null, -100., "USD", "USD");

		aFloatStream[1] = FloatingStream.Create (dtEffective.getJulian(), dtEffective.addTenor ("5Y").getJulian(),
			0.05, true, FloatingRateIndex.Create ("ABC-RI-3M"), 4, "Act/360", false, "Act/360", false, false, null, null,
				dap, dap, dap, dap, null, null, null, -100., "USD", "USD");

		aFloatStream[2] = FloatingStream.Create (dtEffective.getJulian(), dtEffective.addTenor ("7Y").getJulian(),
			0.07, true, FloatingRateIndex.Create ("ABC-RI-12M"), 1, "Act/360", false, "Act/360", false, false, null, null,
				dap, dap, dap, dap, null, null, null, -100., "USD", "USD");

		/*
		 * Create a Rates Basket instance containing the fixed and floating streams
		 */

		return new RatesBasket ("RATESBASKET", aFixedStream, aFloatStream);
	}

	/*
	 * Sample demonstrating creation of discount curve from cash/futures/swaps
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void MultiLegSwapSample()
		throws Exception
	{
		JulianDate dtValue = JulianDate.Today();

		JulianDate dtSettle = dtValue.addBusDays (2, "USD");

		/*
		 * Create the Discount Curve from the rates instruments
		 */

		String[] astrCashTenor = new String[] {"3M"};
		double[] adblCashRate = new double[] {0.00276};
		String[] astrIRSTenor = new String[] {   "1Y",    "2Y",    "3Y",    "4Y",    "5Y",    "6Y",    "7Y",
			   "8Y",    "9Y",   "10Y",   "11Y",   "12Y",   "15Y",   "20Y",   "25Y",   "30Y",   "40Y",   "50Y"};
		double[] adblIRSRate = new double[]  {0.00367, 0.00533, 0.00843, 0.01238, 0.01609, 0.01926, 0.02191,
			0.02406, 0.02588, 0.02741, 0.02870, 0.02982, 0.03208, 0.03372, 0.03445, 0.03484, 0.03501, 0.03484};

		DiscountCurve dc = BuildRatesCurveFromInstruments (dtValue, astrCashTenor, adblCashRate, astrIRSTenor, adblIRSRate, 0., "USD");

		/*
		 * Set up the valuation and the market parameters
		 */

		ValuationParams valParams = ValuationParams.CreateValParams (dtSettle, 0, "", Convention.DR_ACTUAL);

		BasketMarketParams bmp = BasketMarketParamsBuilder.CreateBasketMarketParams();

		bmp.addDiscountCurve ("USD", dc);

		/*
		 * Create the Rates Basket from the streams
		 */

		RatesBasket rb = MakeRatesBasket (dtValue);

		/*
		 * Value the Rates Basket
		 */

		CaseInsensitiveTreeMap<Double> mapRBResults = rb.value (valParams, null, bmp, null);

		System.out.println (mapRBResults);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		MultiLegSwapSample();
	}
}

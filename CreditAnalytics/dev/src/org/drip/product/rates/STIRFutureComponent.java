
package org.drip.product.rates;

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
 * STIRFutureComponent contains the implementation of the Short Term Interest Rate Swap Future product
 * 	contract/valuation details. It exports the following functionality:
 *  - Standard/Custom Constructor for the STIRFuture
 *  - Dates: Effective, Maturity, Coupon dates and Product settlement Parameters
 *  - Coupon/Notional Outstanding as well as schedules
 *  - Retrieve the constituent fixed and floating streams
 *  - Market Parameters: Discount, Forward, Credit, Treasury Curves
 *  - Cash Flow Periods: Coupon flows and (Optionally) Loss Flows
 *  - Valuation: Named Measure Generation
 *  - Calibration: The codes and constraints generation
 *  - Jacobians: Quote/DF and PV/DF micro-Jacobian generation
 *  - Serialization into and de-serialization out of byte arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class STIRFutureComponent extends org.drip.product.rates.FixFloatComponent {

	/**
	 * Construct the STIRFutureComponent from the fixed and the floating streams
	 * 
	 * @param fixStream Fixed Stream
	 * @param floatStream Floating Stream
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public STIRFutureComponent (
		final org.drip.product.cashflow.FixedStream fixStream,
		final org.drip.product.cashflow.FloatingStream floatStream)
		throws java.lang.Exception
	{
		super (fixStream, floatStream);
	}

	@Override public java.lang.String name()
	{
		return "STIR=" + derivedStream().forwardLabel()[0] + " | " + effective();
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		long lStart = System.nanoTime();

		double dblValueDate = valParams.valueDate();

		double dblEffectiveDate = effective().julian();

		java.lang.String strComponentName = name();

		if (dblValueDate >= dblEffectiveDate) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = super.value
			(valParams, pricerParams, csqs, quotingParams);

		if (null == mapResult || 0 == mapResult.size()) return null;

		double dblMultiplicativeSwapRateQuantoAdjustment = java.lang.Double.NaN;

		try {
			dblMultiplicativeSwapRateQuantoAdjustment =
				org.drip.analytics.support.OptionHelper.MultiplicativeCrossVolQuanto (csqs,
					strComponentName + "SwapRateVolatility", strComponentName + "SwapRateExchangeVolatility",
						strComponentName + "SwapRateToSwapRateExchangeCorrelation", dblValueDate,
							dblEffectiveDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblSwapRate = mapResult.get ("SwapRate");

		double dblQuantoAdjustedSwapRate = dblSwapRate * dblMultiplicativeSwapRateQuantoAdjustment;

		mapResult.put ("AdditiveSwapRateQuantoAdjustment", dblQuantoAdjustedSwapRate - dblSwapRate);

		mapResult.put ("MultiplicativeSwapRateQuantoAdjustment", dblMultiplicativeSwapRateQuantoAdjustment);

		mapResult.put ("QuantoAdjustedSwapRate", dblQuantoAdjustedSwapRate);

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = super.measureNames();

		setstrMeasureNames.add ("AdditiveSwapRateQuantoAdjustment");

		setstrMeasureNames.add ("MultiplicativeSwapRateQuantoAdjustment");

		setstrMeasureNames.add ("QuantoAdjustedSwapRate");

		return setstrMeasureNames;
	}
}

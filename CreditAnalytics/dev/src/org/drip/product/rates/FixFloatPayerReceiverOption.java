
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
 * FixFloatPayerReceiverOption implements the Payer/Receiver Option on the STIR Futures.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatPayerReceiverOption extends org.drip.product.definition.FixedIncomeOptionComponent {
	private boolean _bIsReceiver = false;
	private org.drip.product.rates.FixFloatComponent _stir = null;

	/**
	 * FixFloatPayerReceiverOption constructor
	 * 
	 * @param stir The Underlying STIR Future Component
	 * @param strManifestMeasure Measure of the Underlying Component
	 * @param bIsReceiver Is the STIR Option a Receiver/Payer? TRUE => Receiver
	 * @param dblStrike Strike of the Underlying Component's Measure
	 * @param dblNotional Option Notional
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FixFloatPayerReceiverOption (
		final org.drip.product.rates.FixFloatComponent stir,
		final java.lang.String strManifestMeasure,
		final boolean bIsReceiver,
		final double dblStrike,
		final double dblNotional,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		super (stir, strManifestMeasure, dblStrike, dblNotional, strDayCount, strCalendar);

		_stir = stir;
		_bIsReceiver = bIsReceiver;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		return _stir.cashflowCurrencySet();
	}

	@Override public java.lang.String[] payCurrency()
	{
		return _stir.payCurrency();
	}

	@Override public java.lang.String[] principalCurrency()
	{
		return _stir.principalCurrency();
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams) return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= exercise().julian()) return null;

		long lStart = System.nanoTime();

		double dblExerciseDate = exercise().julian();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapSTIROutput = _stir.value
			(valParams, pricerParams, csqs, quotingParams);

		java.lang.String strManifestMeasure = manifestMeasure();

		if (null == mapSTIROutput || !mapSTIROutput.containsKey (strManifestMeasure)) return null;

		double dblFixedCleanDV01 = mapSTIROutput.get ("CleanFixedDV01");

		double dblATMManifestMeasure = mapSTIROutput.get (strManifestMeasure);

		if (!org.drip.quant.common.NumberUtil.IsValid (dblATMManifestMeasure)) return null;

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel()[0];

		org.drip.state.identifier.FundingLabel fundingLabel = org.drip.state.identifier.FundingLabel.Standard
			(payCurrency()[0]);

		try {
			double dblSTIRIntegratedQuantoDrift =
				org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (csqs.forwardCurveVolSurface
					(forwardLabel), csqs.fundingCurveVolSurface (fundingLabel),
						csqs.forwardFundingCorrSurface (forwardLabel, fundingLabel), dblValueDate,
							dblExerciseDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblSTIRIntegratedQuantoDrift)) return null;

			double dblSTIRIntegratedSurfaceVariance =
				org.drip.analytics.support.OptionHelper.IntegratedSurfaceVariance
					(csqs.forwardCurveVolSurface (forwardLabel), dblValueDate, dblExerciseDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblSTIRIntegratedSurfaceVariance)) return null;

			double dblSTIRIntegratedSurfaceVolatility = java.lang.Math.sqrt
				(dblSTIRIntegratedSurfaceVariance);

			double dblStrike = strike();

			double dblLogMoneynessFactor = java.lang.Math.log (dblATMManifestMeasure / dblStrike);

			double dblForwardIntrinsic = java.lang.Double.NaN;
			double dblForwardATMIntrinsic = java.lang.Double.NaN;
			double dblATMDPlus = (dblSTIRIntegratedQuantoDrift + 0.5 * dblSTIRIntegratedSurfaceVariance) /
				dblSTIRIntegratedSurfaceVolatility;
			double dblATMDMinus = (dblSTIRIntegratedQuantoDrift - 0.5 * dblSTIRIntegratedSurfaceVariance) /
				dblSTIRIntegratedSurfaceVolatility;
			double dblDPlus = (dblLogMoneynessFactor + dblSTIRIntegratedQuantoDrift + 0.5 *
				dblSTIRIntegratedSurfaceVariance) / dblSTIRIntegratedSurfaceVolatility;
			double dblDMinus = (dblLogMoneynessFactor + dblSTIRIntegratedQuantoDrift - 0.5 *
				dblSTIRIntegratedSurfaceVariance) / dblSTIRIntegratedSurfaceVolatility;

			if (_bIsReceiver) {
				dblForwardIntrinsic = dblATMManifestMeasure * org.drip.quant.distribution.Gaussian.InverseCDF
					(dblDPlus) - dblStrike * org.drip.quant.distribution.Gaussian.InverseCDF (dblDMinus);

				dblForwardATMIntrinsic = dblATMManifestMeasure *
					org.drip.quant.distribution.Gaussian.InverseCDF (dblATMDPlus) - dblStrike *
						org.drip.quant.distribution.Gaussian.InverseCDF (dblATMDMinus);
			} else {
				dblForwardIntrinsic = dblStrike * org.drip.quant.distribution.Gaussian.InverseCDF
					(-dblDMinus) - dblATMManifestMeasure * org.drip.quant.distribution.Gaussian.InverseCDF
						(-dblDPlus);

				dblForwardATMIntrinsic = dblStrike * org.drip.quant.distribution.Gaussian.InverseCDF
					(-dblATMDMinus) - dblATMManifestMeasure * org.drip.quant.distribution.Gaussian.InverseCDF
						(-dblATMDPlus);
			}

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

			double dblSpotPrice = dblForwardIntrinsic * dblFixedCleanDV01;

			mapResult.put ("ATMSwapRate", dblATMManifestMeasure);

			mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

			mapResult.put ("ForwardATMIntrinsic", dblForwardATMIntrinsic);

			mapResult.put ("ForwardIntrinsic", dblForwardIntrinsic);

			mapResult.put ("IntegratedQuantoDrift", dblSTIRIntegratedQuantoDrift);

			mapResult.put ("IntegratedSurfaceVariance", dblSTIRIntegratedSurfaceVariance);

			mapResult.put ("Price", dblSpotPrice);

			mapResult.put ("PV", dblSpotPrice);

			mapResult.put ("SpotPrice", dblSpotPrice);

			mapResult.put ("Upfront", dblSpotPrice);

			return mapResult;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("ATMSwapRate");

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("ForwardATMIntrinsic");

		setstrMeasureNames.add ("ForwardIntrinsic");

		setstrMeasureNames.add ("IntegratedQuantoDrift");

		setstrMeasureNames.add ("IntegratedSurfaceVariance");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("SpotPrice");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}
}

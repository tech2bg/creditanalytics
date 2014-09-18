
package org.drip.product.fra;

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
 * FRAStandardCapFloorlet implements the Standard FRA Caplet and Floorlet.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FRAStandardCapFloorlet extends org.drip.product.definition.FixedIncomeOptionComponent {
	private boolean _bIsCaplet = false;
	private org.drip.product.fra.FRAStandardComponent _fra = null;

	/**
	 * FRAStandardCapFloorlet constructor
	 * 
	 * @param fra The Underlying FRA Standard Component
	 * @param strManifestMeasure Measure of the Underlying Component
	 * @param bIsCaplet Is the FRA Option a Caplet? TRUE => YES
	 * @param dblStrike Strike of the Underlying Component's Measure
	 * @param dblNotional Option Notional
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FRAStandardCapFloorlet (
		final org.drip.product.fra.FRAStandardComponent fra,
		final java.lang.String strManifestMeasure,
		final boolean bIsCaplet,
		final double dblStrike,
		final double dblNotional,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		super (fra, strManifestMeasure, dblStrike, dblNotional, strDayCount, strCalendar);

		_fra = fra;
		_bIsCaplet = bIsCaplet;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		return _fra.cashflowCurrencySet();
	}

	@Override public java.lang.String[] payCurrency()
	{
		return _fra.payCurrency();
	}

	@Override public java.lang.String[] principalCurrency()
	{
		return _fra.principalCurrency();
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == csqs) return null;

		org.drip.state.identifier.FundingLabel fundingLabel = org.drip.state.identifier.FundingLabel.Standard
			(_fra.payCurrency()[0]);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel);

		if (null == dcFunding) return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= exercise().julian()) return null;

		long lStart = System.nanoTime();

		double dblExerciseDate = exercise().julian();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFRAOutput = _fra.value
			(valParams, pricerParams, csqs, quotingParams);

		java.lang.String strManifestMeasure = manifestMeasure();

		if (null == mapFRAOutput || !mapFRAOutput.containsKey (strManifestMeasure)) return null;

		double dblATMManifestMeasure = mapFRAOutput.get (strManifestMeasure);

		if (!org.drip.quant.common.NumberUtil.IsValid (dblATMManifestMeasure)) return null;

		org.drip.quant.function1D.AbstractUnivariate auForwardVolSurface = csqs.forwardCurveVolSurface
			(_fra.fri());

		if (null == auForwardVolSurface) return null;

		try {
			double dblIntegratedSurfaceVariance =
				org.drip.analytics.support.OptionHelper.IntegratedSurfaceVariance (auForwardVolSurface,
					dblValueDate, dblExerciseDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblIntegratedSurfaceVariance)) return null;

			double dblIntegratedSurfaceVolatility = java.lang.Math.sqrt (dblIntegratedSurfaceVariance);

			double dblStrike = strike();

			double dblNotional = notional();

			double dblLogMoneynessFactor = java.lang.Math.log (dblATMManifestMeasure / dblStrike);

			double dblForwardPrice = java.lang.Double.NaN;
			double dblForwardATMPrice = java.lang.Double.NaN;
			double dblATMDPlus = 0.5 * dblIntegratedSurfaceVariance / dblIntegratedSurfaceVolatility;
			double dblATMDMinus = -1. * dblATMDPlus;
			double dblDPlus = (dblLogMoneynessFactor + 0.5 * dblIntegratedSurfaceVariance) /
				dblIntegratedSurfaceVolatility;
			double dblDMinus = (dblLogMoneynessFactor - 0.5 * dblIntegratedSurfaceVariance) /
				dblIntegratedSurfaceVolatility;

			if (_bIsCaplet) {
				dblForwardPrice = dblATMManifestMeasure * org.drip.quant.distribution.Gaussian.InverseCDF
					(dblDPlus) - dblStrike * org.drip.quant.distribution.Gaussian.InverseCDF (dblDMinus);

				dblForwardATMPrice = dblATMManifestMeasure * org.drip.quant.distribution.Gaussian.InverseCDF
					(dblATMDPlus) - dblStrike * org.drip.quant.distribution.Gaussian.InverseCDF
						(dblATMDMinus);
			} else {
				dblForwardPrice = dblStrike * org.drip.quant.distribution.Gaussian.InverseCDF (-dblDMinus) -
					dblATMManifestMeasure * org.drip.quant.distribution.Gaussian.InverseCDF (-dblDPlus);

				dblForwardATMPrice = dblStrike * org.drip.quant.distribution.Gaussian.InverseCDF
					(-dblATMDMinus) - dblATMManifestMeasure * org.drip.quant.distribution.Gaussian.InverseCDF
						(-dblATMDPlus);
			}

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

			double dblSpotPrice = dblForwardPrice * dcFunding.df (dblExerciseDate);

			mapResult.put ("ATMFRA", dblATMManifestMeasure);

			mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

			mapResult.put ("ForwardATMPrice", dblForwardATMPrice);

			mapResult.put ("ForwardPrice", dblForwardPrice);

			mapResult.put ("IntegratedSurfaceVariance", dblIntegratedSurfaceVariance);

			mapResult.put ("Price", dblSpotPrice);

			mapResult.put ("PV", dblSpotPrice * dblNotional);

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

		setstrMeasureNames.add ("ATMFRA");

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("ForwardATMPrice");

		setstrMeasureNames.add ("ForwardPrice");

		setstrMeasureNames.add ("IntegratedSurfaceVariance");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("SpotPrice");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}
}

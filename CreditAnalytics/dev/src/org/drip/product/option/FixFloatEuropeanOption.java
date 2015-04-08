
package org.drip.product.option;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * FixFloatEuropeanOption implements the Payer/Receiver European Option on the Fix-Float Swap.
 *
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatEuropeanOption extends org.drip.product.option.FixedIncomeOptionComponent {
	private boolean _bIsReceiver = false;
	private org.drip.product.rates.FixFloatComponent _stir = null;

	/**
	 * FixFloatEuropeanOption constructor
	 * 
	 * @param stir The Underlying STIR Future Component
	 * @param strManifestMeasure Measure of the Underlying Component
	 * @param bIsReceiver Is the STIR Option a Receiver/Payer? TRUE => Receiver
	 * @param dblStrike Strike of the Underlying Component's Measure
	 * @param dblNotional Option Notional
	 * @param ltds Last Trading Date Setting
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FixFloatEuropeanOption (
		final org.drip.product.rates.FixFloatComponent stir,
		final java.lang.String strManifestMeasure,
		final boolean bIsReceiver,
		final double dblStrike,
		final double dblNotional,
		final org.drip.product.params.LastTradingDateSetting ltds,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		super (stir, strManifestMeasure, dblStrike, dblNotional, ltds, strDayCount, strCalendar);

		_stir = stir;
		_bIsReceiver = bIsReceiver;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String> couponCurrency()
	{
		return _stir.couponCurrency();
	}

	@Override public java.lang.String payCurrency()
	{
		return _stir.payCurrency();
	}

	@Override public java.lang.String principalCurrency()
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

		double dblExerciseDate = exerciseDate().julian();

		org.drip.analytics.date.JulianDate dtEffective = _stir.effectiveDate();

		org.drip.product.params.LastTradingDateSetting ltds = lastTradingDateSetting();

		try {
			if (null != ltds && dblValueDate >= ltds.lastTradingDate (dtEffective.julian(), calendar()))
				return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		java.lang.String strPayCurrency = _stir.payCurrency();

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (strPayCurrency));

		if (null == dcFunding) return null;

		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapSTIROutput = _stir.value
			(valParams, pricerParams, csqs, quotingParams);

		java.lang.String strManifestMeasure = manifestMeasure();

		if (null == mapSTIROutput || !mapSTIROutput.containsKey (strManifestMeasure)) return null;

		double dblFixedCleanDV01 = mapSTIROutput.get ("CleanFixedDV01");

		double dblATMManifestMeasure = mapSTIROutput.get (strManifestMeasure);

		if (!org.drip.quant.common.NumberUtil.IsValid (dblATMManifestMeasure)) return null;

		try {
			double dblSTIRIntegratedSurfaceVariance =
				org.drip.analytics.support.OptionHelper.IntegratedSurfaceVariance
					(csqs.customMetricVolSurface (org.drip.state.identifier.CustomMetricLabel.Standard
						(_stir.name() + "_" + strManifestMeasure)), dblValueDate, dblExerciseDate);

			if (!org.drip.quant.common.NumberUtil.IsValid (dblSTIRIntegratedSurfaceVariance)) return null;

			double dblSTIRIntegratedSurfaceVolatility = java.lang.Math.sqrt
				(dblSTIRIntegratedSurfaceVariance);

			double dblStrike = strike();

			double dblMoneynessFactor = dblATMManifestMeasure / dblStrike;

			double dblLogMoneynessFactor = java.lang.Math.log (dblMoneynessFactor);

			double dblForwardIntrinsic = java.lang.Double.NaN;
			double dblForwardATMIntrinsic = java.lang.Double.NaN;
			double dblManifestMeasurePriceTransformer = java.lang.Double.NaN;
			double dblManifestMeasureIntrinsic = _bIsReceiver ? dblATMManifestMeasure - dblStrike : dblStrike
				- dblATMManifestMeasure;
			double dblATMDPlus = 0.5 * dblSTIRIntegratedSurfaceVariance / dblSTIRIntegratedSurfaceVolatility;
			double dblATMDMinus = -1. * dblATMDPlus;
			double dblDPlus = (dblLogMoneynessFactor + 0.5 * dblSTIRIntegratedSurfaceVariance) /
				dblSTIRIntegratedSurfaceVolatility;
			double dblDMinus = (dblLogMoneynessFactor - 0.5 * dblSTIRIntegratedSurfaceVariance) /
				dblSTIRIntegratedSurfaceVolatility;

			if (strManifestMeasure.equalsIgnoreCase ("Price") || strManifestMeasure.equalsIgnoreCase ("PV"))
				dblManifestMeasurePriceTransformer = dcFunding.df (dblExerciseDate);
			else if (strManifestMeasure.equalsIgnoreCase ("FairPremium") ||
				strManifestMeasure.equalsIgnoreCase ("SwapRate") || strManifestMeasure.equalsIgnoreCase
					("Rate"))
				dblManifestMeasurePriceTransformer = 10000. * dblFixedCleanDV01;

			if (!org.drip.quant.common.NumberUtil.IsValid (dblManifestMeasurePriceTransformer)) return null;

			if (_bIsReceiver) {
				dblForwardIntrinsic = dblATMManifestMeasure * org.drip.measure.continuous.Gaussian.InverseCDF
					(dblDPlus) - dblStrike * org.drip.measure.continuous.Gaussian.InverseCDF (dblDMinus);

				dblForwardATMIntrinsic = dblATMManifestMeasure *
					org.drip.measure.continuous.Gaussian.InverseCDF (dblATMDPlus) - dblStrike *
						org.drip.measure.continuous.Gaussian.InverseCDF (dblATMDMinus);
			} else {
				dblForwardIntrinsic = dblStrike * org.drip.measure.continuous.Gaussian.InverseCDF
					(-dblDMinus) - dblATMManifestMeasure * org.drip.measure.continuous.Gaussian.InverseCDF
						(-dblDPlus);

				dblForwardATMIntrinsic = dblStrike * org.drip.measure.continuous.Gaussian.InverseCDF
					(-dblATMDMinus) - dblATMManifestMeasure * org.drip.measure.continuous.Gaussian.InverseCDF
						(-dblATMDPlus);
			}

			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

			double dblSpotPrice = dblForwardIntrinsic * dblManifestMeasurePriceTransformer;

			mapResult.put ("ATMSwapRate", dblATMManifestMeasure);

			mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

			mapResult.put ("ForwardATMIntrinsic", dblForwardATMIntrinsic);

			mapResult.put ("ForwardIntrinsic", dblForwardIntrinsic);

			mapResult.put ("IntegratedSurfaceVariance", dblSTIRIntegratedSurfaceVariance);

			mapResult.put ("ManifestMeasureIntrinsic", dblManifestMeasureIntrinsic);

			mapResult.put ("ManifestMeasureIntrinsicValue", dblManifestMeasureIntrinsic *
				dblManifestMeasurePriceTransformer);

			mapResult.put ("MoneynessFactor", dblMoneynessFactor);

			mapResult.put ("Price", dblSpotPrice);

			mapResult.put ("PV", dblSpotPrice);

			org.drip.market.otc.SwapOptionSettlement sos =
				org.drip.market.otc.SwapOptionSettlementContainer.ConventionFromJurisdiction
					(strPayCurrency);

			if (null != sos) {
				int iSettlementType = sos.settlementType();

				int iSettlementQuote = sos.settlementQuote();

				mapResult.put ("SettleType", (double) iSettlementType);

				mapResult.put ("SettleQuote", (double) iSettlementQuote);

				if (org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_TYPE_CASH_SETTLED == iSettlementType)
				{
					if (org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_QUOTE_EXACT_CURVE ==
						iSettlementQuote)
						mapResult.put ("SettleAmount", dblSpotPrice);
					else if (org.drip.market.otc.SwapOptionSettlement.SETTLEMENT_QUOTE_IRR ==
						iSettlementQuote && (strManifestMeasure.equalsIgnoreCase ("FairPremium") ||
							strManifestMeasure.equalsIgnoreCase ("SwapRate") ||
								strManifestMeasure.equalsIgnoreCase ("Rate"))) {
						org.drip.product.rates.Stream streamDerived = _stir.derivedStream();

						if (csqs.setFundingCurve
							(org.drip.state.creator.DiscountCurveBuilder.CreateFromFlatYield (dtEffective,
								strPayCurrency, dcFunding.collateralParams(), dblATMManifestMeasure,
									streamDerived.couponDC(), streamDerived.freq())) && null !=
										(mapSTIROutput = _stir.value (valParams, pricerParams, csqs,
											quotingParams)))
								mapResult.put ("SettleAmount", dblForwardIntrinsic * 10000. *
									mapSTIROutput.get ("CleanFixedDV01"));
					}
				}
			} else
				mapResult.put ("SettleAmount", dblSpotPrice);

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

		setstrMeasureNames.add ("IntegratedSurfaceVariance");

		setstrMeasureNames.add ("ManifestMeasureIntrinsic");

		setstrMeasureNames.add ("ManifestMeasureIntrinsicValue");

		setstrMeasureNames.add ("MoneynessFactor");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("SettleAmount");

		setstrMeasureNames.add ("SettleQuote");

		setstrMeasureNames.add ("SettleType");

		setstrMeasureNames.add ("SpotPrice");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}
}

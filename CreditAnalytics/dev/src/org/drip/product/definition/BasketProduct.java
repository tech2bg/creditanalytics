
package org.drip.product.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 *  BasketProduct abstract class extends MarketParamRef. It provides methods for getting the basket’s
 *   components, notional, coupon, effective date, maturity date, coupon amount, and list of coupon periods.
 *  
 * @author Lakshmi Krishnamurthy
 */

public abstract class BasketProduct implements org.drip.product.definition.MarketParamRef {
	protected static final int MEASURE_AGGREGATION_TYPE_CUMULATIVE = 1;
	protected static final int MEASURE_AGGREGATION_TYPE_WEIGHTED_CUMULATIVE = 2;
	protected static final int MEASURE_AGGREGATION_TYPE_UNIT_ACCUMULATE = 4;
	protected static final int MEASURE_AGGREGATION_TYPE_IGNORE = 4;

	class ComponentCurve {
		java.lang.String _strName = null;
		org.drip.analytics.definition.CreditCurve _cc = null;

		ComponentCurve (
			final java.lang.String strName,
			final org.drip.analytics.definition.CreditCurve cc)
		{
			_cc = cc;
			_strName = strName;
		}
	}

	class FlatDeltaGammaMeasureMap {
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mapDelta = null;
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mapGamma = null;

		FlatDeltaGammaMeasureMap (
			final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapDelta,
			final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapGamma)
		{
			_mapDelta = mapDelta;
			_mapGamma = mapGamma;
		}
	}

	class TenorDeltaGammaMeasureMap {
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmDelta = null;
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmGamma = null;

		TenorDeltaGammaMeasureMap (
			final
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
					mmDelta,
			final
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
					mmGamma)
		{
			_mmDelta = mmDelta;
			_mmGamma = mmGamma;
		}
	}

	class ComponentFactorTenorDeltaGammaMeasureMap {
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmDelta = null;
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmGamma = null;

		ComponentFactorTenorDeltaGammaMeasureMap (
			final
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
					mmmDelta,
			final
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
					mmmGamma)
		{
			_mmmDelta = mmmDelta;
			_mmmGamma = mmmGamma;
		}
	}

	private FlatDeltaGammaMeasureMap accumulateDeltaGammaMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqsUp,
		final org.drip.param.market.CurveSurfaceQuoteSet csqsDown,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapBaseMeasures)
	{
		if (null == csqsUp) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapUpMeasures = value (valParams,
			pricerParams, csqsUp, quotingParams);

		if (null == mapUpMeasures || 0 == mapUpMeasures.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.String, java.lang.Double>> mapUpMeasuresES =
			mapUpMeasures.entrySet();

		if (null == mapUpMeasuresES) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapDeltaMeasures = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> meUp : mapUpMeasuresES) {
			if (null == meUp) continue;

			java.lang.String strKey = meUp.getKey();

			if (null == strKey || strKey.isEmpty()) continue;

			java.lang.Double dblBase = mapBaseMeasures.get (strKey);

			java.lang.Double dblUp = meUp.getValue();

			mapDeltaMeasures.put (strKey, (null == dblUp ? 0. : dblUp) - (null == dblBase ? 0. : dblBase));
		}

		if (null == csqsDown) return new FlatDeltaGammaMeasureMap (mapDeltaMeasures, null);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapDownMeasures = value
			(valParams, pricerParams, csqsDown, quotingParams);

		if (null == mapDownMeasures || 0 == mapDownMeasures.size())
			return new FlatDeltaGammaMeasureMap (mapDeltaMeasures, null);

		java.util.Set<java.util.Map.Entry<java.lang.String, java.lang.Double>> mapDownMeasuresES =
			mapDownMeasures.entrySet();

		if (null == mapDownMeasuresES) return new FlatDeltaGammaMeasureMap (mapDeltaMeasures, null);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapGammaMeasures = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> meDown : mapDownMeasuresES) {
			if (null == meDown) continue;

			java.lang.String strKey = meDown.getKey();

			if (null == strKey || strKey.isEmpty()) continue;

			java.lang.Double dblBase = mapBaseMeasures.get (strKey);

			java.lang.Double dblUp = mapUpMeasures.get (strKey);

			java.lang.Double dblDown = meDown.getValue();

			mapGammaMeasures.put (strKey, (null == dblUp ? 0. : dblUp) + (null == dblDown ? 0. : dblDown) -
				(null == dblBase ? 0. : 2. * dblBase));
		}

		return new FlatDeltaGammaMeasureMap (mapDeltaMeasures, mapGammaMeasures);
	}

	private TenorDeltaGammaMeasureMap accumulateTenorDeltaGammaMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapTenorUpCSQS,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapTenorDownCSQS,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapBaseMeasures,
		final ComponentCurve compCurve)
	{
		if (null == mapTenorUpCSQS || 0 == mapTenorUpCSQS.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.String, org.drip.param.market.CurveSurfaceQuoteSet>>
			mapESTenorUpCSQS = mapTenorUpCSQS.entrySet();

		if (null == mapESTenorUpCSQS || 0 == mapESTenorUpCSQS.size()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<FlatDeltaGammaMeasureMap> mapTenorDGMM = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<FlatDeltaGammaMeasureMap>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.market.CurveSurfaceQuoteSet> meTenorUpCSQS
			: mapESTenorUpCSQS) {
			if (null == meTenorUpCSQS) continue;

			java.lang.String strTenorKey = meTenorUpCSQS.getKey();

			if (null == strTenorKey || strTenorKey.isEmpty()) continue;

			org.drip.param.market.CurveSurfaceQuoteSet csqsTenorUp = meTenorUpCSQS.getValue();

			org.drip.param.market.CurveSurfaceQuoteSet csqsTenorDown = mapTenorDownCSQS.get (strTenorKey);

			org.drip.analytics.definition.CreditCurve ccVirginUp = null;
			org.drip.analytics.definition.CreditCurve ccVirginDown = null;

			if (null != csqsTenorUp && null != compCurve && null != compCurve._cc && null !=
				compCurve._strName && !compCurve._strName.isEmpty()) {
				ccVirginUp = csqsTenorUp.creditCurve (org.drip.state.identifier.CreditLabel.Standard
					(compCurve._strName));

				csqsTenorUp.setCreditCurve (compCurve._cc);

				if (null != csqsTenorDown) {
					ccVirginDown = csqsTenorDown.creditCurve (org.drip.state.identifier.CreditLabel.Standard
						(compCurve._strName));

					csqsTenorDown.setCreditCurve (compCurve._cc);
				}
			}

			mapTenorDGMM.put (strTenorKey, accumulateDeltaGammaMeasures (valParams, pricerParams,
				csqsTenorUp, csqsTenorDown, quotingParams, mapBaseMeasures));

			if (null != csqsTenorUp && null != compCurve && null != compCurve._strName &&
				!compCurve._strName.isEmpty() && null != ccVirginUp)
				csqsTenorUp.setCreditCurve (ccVirginUp);

			if (null != csqsTenorDown && null != compCurve && null != compCurve._strName &&
				!compCurve._strName.isEmpty() && null != ccVirginDown)
				csqsTenorDown.setCreditCurve (ccVirginDown);
		}

		if (0 == mapTenorDGMM.size()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mmDelta = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mmGamma = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		for (java.util.Map.Entry<java.lang.String, FlatDeltaGammaMeasureMap> meTenorDGMM :
			mapTenorDGMM.entrySet()) {
			if (null == meTenorDGMM) continue;

			FlatDeltaGammaMeasureMap dgmmTenorDelta = meTenorDGMM.getValue();

			if (null != dgmmTenorDelta) {
				java.lang.String strKey = meTenorDGMM.getKey();

				mmDelta.put (strKey, dgmmTenorDelta._mapDelta);

				mmGamma.put (strKey, dgmmTenorDelta._mapGamma);
			}
		}

		return new TenorDeltaGammaMeasureMap (mmDelta, mmGamma);
	}

	private ComponentFactorTenorDeltaGammaMeasureMap accumulateComponentWiseTenorDeltaGammaMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapCSQS,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapTenorUpCSQS,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapTenorDownCSQS,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapBaseMeasures)
	{
		if (null == mapCSQS || 0 == mapCSQS.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.String, org.drip.param.market.CurveSurfaceQuoteSet>>
			mapESCSQS = mapCSQS.entrySet();

		if (null == mapESCSQS || 0 == mapESCSQS.size()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<TenorDeltaGammaMeasureMap> mapComponentTenorDGMM =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<TenorDeltaGammaMeasureMap>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.market.CurveSurfaceQuoteSet> meCSQS :
			mapESCSQS) {
			if (null == meCSQS) continue;

			java.lang.String strComponentName = meCSQS.getKey();

			if (null == strComponentName || strComponentName.isEmpty()) continue;

			org.drip.param.market.CurveSurfaceQuoteSet csqs = meCSQS.getValue();

			if (null != csqs)
				mapComponentTenorDGMM.put (strComponentName, accumulateTenorDeltaGammaMeasures (valParams,
					pricerParams, mapTenorUpCSQS, mapTenorDownCSQS, quotingParams, mapBaseMeasures, new
						ComponentCurve (strComponentName, csqs.creditCurve
							(org.drip.state.identifier.CreditLabel.Standard (strComponentName)))));
		}

		if (0 == mapComponentTenorDGMM.size()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			mmmCompRatesDelta = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>();

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			mmmCompRatesGamma = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>();

		for (java.util.Map.Entry<java.lang.String, TenorDeltaGammaMeasureMap> meCompTenorDGMM :
			mapComponentTenorDGMM.entrySet()) {
			if (null == meCompTenorDGMM) continue;

			TenorDeltaGammaMeasureMap dgmmCompTenorDeltaGamma = meCompTenorDGMM.getValue();

			if (null != dgmmCompTenorDeltaGamma) {
				java.lang.String strKey = meCompTenorDGMM.getKey();

				mmmCompRatesDelta.put (strKey, dgmmCompTenorDeltaGamma._mmDelta);

				mmmCompRatesGamma.put (strKey, dgmmCompTenorDeltaGamma._mmGamma);
			}
		}

		return new ComponentFactorTenorDeltaGammaMeasureMap (mmmCompRatesDelta, mmmCompRatesGamma);
	}

	protected double measureValue (
		final java.lang.String strMeasure,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCalc)
		throws java.lang.Exception
	{
		if (null == strMeasure || strMeasure.isEmpty() || null == mapCalc || null == mapCalc.entrySet())
			throw new java.lang.Exception ("BasketProduct::getMeasure => Invalid Params");

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapCalc.entrySet()) {
			if (null != me && null != me.getKey() && me.getKey().equalsIgnoreCase (strMeasure))
				return me.getValue();
		}

		throw new java.lang.Exception ("BasketProduct::getMeasure => " + strMeasure +
			" is an invalid measure!");
	}

	/**
	 * Return the basket name
	 * 
	 * @return Name of the basket product
	 */

	public abstract java.lang.String name();

	/**
	 * Return the Components in the Basket
	 * 
	 * @return Components in the Basket
	 */

	public abstract org.drip.product.definition.FixedIncomeComponent[] components();

	/**
	 * Retrieve the Aggregation Type for the specified Measure
	 * 
	 * @param strMeasureName The Specified Measure Name
	 * 
	 * @return The Aggregation Type
	 */

	public abstract int measureAggregationType (
		final java.lang.String strMeasureName);

	/**
	 * Retrieve the component Weights
	 * 
	 * @return Array Containing the Component Weights
	 */

	public double[] weights()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		double dblTotalWeight = 0.;
		int iNumComp = aComp.length;
		double[] adblWeight = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i) {
			try {
				dblTotalWeight += (adblWeight[i] = aComp[i].initialNotional());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (0. == dblTotalWeight) return null;

		for (int i = 0; i < iNumComp; ++i)
			adblWeight[i] /= dblTotalWeight;

		return adblWeight;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		if (null == aComp) return null;

		int iNumComp = aComp.length;

		if (0 == iNumComp) return null;

		java.util.Set<java.lang.String> setCashflowCurrency = new java.util.HashSet<java.lang.String>();

		for (int i = 0; i < iNumComp; ++i) {
			if (null == aComp[i]) return null;

			java.util.Set<java.lang.String> setCompCashflowCurrency = aComp[i].cashflowCurrencySet();

			if (null != setCompCashflowCurrency && 0 != setCompCashflowCurrency.size())
				setCashflowCurrency.addAll (setCompCashflowCurrency);
		}

		return setCashflowCurrency;
	}

	@Override public java.lang.String[] payCurrency()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		if (null == aComp) return null;

		int iNumComp = aComp.length;

		if (0 == iNumComp) return null;

		java.util.Set<java.lang.String> setCouponCurrency = new java.util.HashSet<java.lang.String>();

		for (int i = 0; i < iNumComp; ++i) {
			if (null == aComp[i]) return null;

			java.lang.String[] astrCompCouponCurrency = aComp[i].payCurrency();

			if (null != astrCompCouponCurrency && 0 != astrCompCouponCurrency.length) {
				for (java.lang.String strCompCouponCurrency : astrCompCouponCurrency) {
					if (null != strCompCouponCurrency && !strCompCouponCurrency.isEmpty())
						setCouponCurrency.add (strCompCouponCurrency);
				}
			}
		}

		int iNumCouponCurrency = setCouponCurrency.size();

		if (0 == iNumCouponCurrency) return null;

		int i = 0;
		java.lang.String[] astrCouponCurrency = new java.lang.String[iNumCouponCurrency];

		for (java.lang.String strCouponCurrency : astrCouponCurrency)
			astrCouponCurrency[i++] = strCouponCurrency;

		return astrCouponCurrency;
	}

	@Override public java.lang.String[] principalCurrency()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		if (null == aComp) return null;

		int iNumComp = aComp.length;

		if (0 == iNumComp) return null;

		java.util.Set<java.lang.String> setPrincipalCurrency = new java.util.HashSet<java.lang.String>();

		for (int i = 0; i < iNumComp; ++i) {
			if (null == aComp[i]) return null;

			java.lang.String[] astrCompPrincipalCurrency = aComp[i].principalCurrency();

			if (null != astrCompPrincipalCurrency && 0 != astrCompPrincipalCurrency.length) {
				for (java.lang.String strCompPrincipalCurrency : astrCompPrincipalCurrency) {
					if (null != strCompPrincipalCurrency && !strCompPrincipalCurrency.isEmpty())
						setPrincipalCurrency.add (strCompPrincipalCurrency);
				}
			}
		}

		int iNumPrincipalCurrency = setPrincipalCurrency.size();

		if (0 == iNumPrincipalCurrency) return null;

		int i = 0;
		java.lang.String[] astrPrincipalCurrency = new java.lang.String[iNumPrincipalCurrency];

		for (java.lang.String strPrincipalCurrency : astrPrincipalCurrency)
			astrPrincipalCurrency[i++] = strPrincipalCurrency;

		return astrPrincipalCurrency;
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		if (null == aComp) return null;

		int iNumComp = aComp.length;

		if (0 == iNumComp) return null;

		java.util.Set<org.drip.state.identifier.CreditLabel> sLSLCredit = new
			java.util.HashSet<org.drip.state.identifier.CreditLabel>();

		for (int i = 0; i < iNumComp; ++i) {
			if (null == aComp[i]) return null;

			org.drip.state.identifier.CreditLabel[] aLSLCredit = aComp[i].creditLabel();

			if (null == aLSLCredit) continue;

			int iNumCreditCurve = aLSLCredit.length;

			if (0 == iNumCreditCurve) continue;

			for (int j = 0; j < iNumCreditCurve; ++j) {
				org.drip.state.identifier.CreditLabel lslCredit = aLSLCredit[j];

				if (null != lslCredit) sLSLCredit.add (lslCredit);
			}
		}

		int iNumCreditCurve = sLSLCredit.size();

		if (0 == iNumCreditCurve) return null;

		int i = 0;
		org.drip.state.identifier.CreditLabel[] aLSLCredit = new
			org.drip.state.identifier.CreditLabel[iNumCreditCurve];

		for (org.drip.state.identifier.CreditLabel lslCredit : sLSLCredit)
			aLSLCredit[i++] = lslCredit;

		return aLSLCredit;
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		if (null == aComp) return null;

		int iNumComp = aComp.length;

		if (0 == iNumComp) return null;

		java.util.Set<org.drip.state.identifier.ForwardLabel> setLSLForward = new
			java.util.HashSet<org.drip.state.identifier.ForwardLabel>();

		for (int i = 0; i < iNumComp; ++i) {
			if (null == aComp[i]) return null;

			org.drip.state.identifier.ForwardLabel[] aLSLForward = aComp[i].forwardLabel();

			if (null == aLSLForward) continue;

			int iNumForwardCurve = aLSLForward.length;

			if (0 == iNumForwardCurve) continue;

			for (int j = 0; j < iNumForwardCurve; ++j) {
				org.drip.state.identifier.ForwardLabel lslForward = aLSLForward[j];

				if (null != lslForward) setLSLForward.add (lslForward);
			}
		}

		int iNumForward = setLSLForward.size();

		if (0 == iNumForward) return null;

		int i = 0;
		org.drip.state.identifier.ForwardLabel[] aLSLForward = new
			org.drip.state.identifier.ForwardLabel[iNumForward];

		for (org.drip.state.identifier.ForwardLabel lslForward : setLSLForward)
			aLSLForward[i++] = lslForward;

		return aLSLForward;
	}

	@Override public org.drip.state.identifier.FundingLabel[] fundingLabel()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		if (null == aComp) return null;

		int iNumComp = aComp.length;

		if (0 == iNumComp) return null;

		java.util.Set<org.drip.state.identifier.FundingLabel> sLSLFunding = new
			java.util.HashSet<org.drip.state.identifier.FundingLabel>();

		for (int i = 0; i < iNumComp; ++i) {
			if (null == aComp[i]) return null;

			org.drip.state.identifier.FundingLabel[] aLSLFunding = aComp[i].fundingLabel();

			if (null == aLSLFunding) continue;

			int iNumFundingCurve = aLSLFunding.length;

			if (0 == iNumFundingCurve) continue;

			for (int j = 0; j < iNumFundingCurve; ++j)
				sLSLFunding.add (aLSLFunding[j]);
		}

		int iNumFundingCurve = sLSLFunding.size();

		if (0 == iNumFundingCurve) return null;

		int i = 0;
		org.drip.state.identifier.FundingLabel[] aLSLFunding = new
			org.drip.state.identifier.FundingLabel[iNumFundingCurve];

		for (org.drip.state.identifier.FundingLabel lslFunding : sLSLFunding)
			aLSLFunding[i++] = lslFunding;

		return aLSLFunding;
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		if (null == aComp) return null;

		int iNumComp = aComp.length;

		if (0 == iNumComp) return null;

		java.util.Set<org.drip.state.identifier.FXLabel> setLabel = new
			java.util.HashSet<org.drip.state.identifier.FXLabel>();

		for (int i = 0; i < iNumComp; ++i) {
			if (null == aComp[i]) return null;

			org.drip.state.identifier.FXLabel[] aLabel = aComp[i].fxLabel();

			if (null == aLabel) continue;

			int iNumLabel = aLabel.length;

			if (0 == iNumLabel) continue;

			for (int j = 0; j < iNumLabel; ++j) {
				org.drip.state.identifier.FXLabel label = aLabel[j];

				if (null != label) setLabel.add (label);
			}
		}

		int iNumLabel = setLabel.size();

		if (0 == iNumLabel) return null;

		int i = 0;
		org.drip.state.identifier.FXLabel[] aLabel = new org.drip.state.identifier.FXLabel[iNumLabel];

		for (org.drip.state.identifier.FXLabel label : setLabel)
			aLabel[i++] = label;

		return aLabel;
	}

	/**
	 * Return the initial notional of the basket product
	 * 
	 * @return Initial notional of the basket product
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public double initialNotional()
		throws java.lang.Exception
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		int iNumComp = aComp.length;
		double dblInitialNotional = 0.;

		for (int i = 0; i < iNumComp; ++i)
			dblInitialNotional += aComp[i].initialNotional();

		return dblInitialNotional;
	}

	/**
	 * Retrieve the notional at the given date
	 * 
	 * @param dblDate Double JulianDate
	 * 
	 * @return Notional
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		double dblNotional = 0.;
		int iNumComp = aComp.length;

		for (int i = 0; i < iNumComp; ++i)
			dblNotional += aComp[i].notional (dblDate);

		return dblNotional;
	}

	/**
	 * Retrieve the time-weighted notional between 2 given dates
	 * 
	 * @param dblDate1 Double JulianDate first
	 * @param dblDate2 Double JulianDate second
	 * 
	 * @return Notional
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		double dblNotional = 0.;
		int iNumComp = aComp.length;

		for (int i = 0; i < iNumComp; ++i)
			dblNotional += aComp[i].notional (dblDate1, dblDate2);

		return dblNotional;
	}

	/**
	 * Retrieve the basket product's coupon amount at the given date
	 * 
	 * @param dblDate Double JulianDate
	 * @param csqs Market Parameters
	 * 
	 * @return Coupon Amount
	 * 
	 * @throws java.lang.Exception Thrown if coupon cannot be calculated
	 */

	public double coupon (
		final double dblDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		double dblNotional = notional (dblDate);

		if (null == csqs || 0. == dblNotional || !org.drip.quant.common.NumberUtil.IsValid (dblNotional))
			throw new java.lang.Exception ("BasketProduct::coupon => Cannot extract basket notional");

		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		double dblCoupon = 0.;
		int iNumComp = aComp.length;

		for (int i = 0; i < iNumComp; ++i)
			dblCoupon += aComp[i].coupon (dblDate, null, csqs).compoundedAccrualRate();

		return dblCoupon / dblNotional;
	}

	/**
	 * Returns the effective date of the basket product
	 * 
	 * @return Effective date of the basket product
	 */

	public org.drip.analytics.date.JulianDate effective()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		int iNumComp = aComp.length;

		org.drip.analytics.date.JulianDate dtEffective = aComp[0].effective();

		for (int i = 1; i < iNumComp; ++i) {
			org.drip.analytics.date.JulianDate dtCompEffective = aComp[i].effective();

			if (dtCompEffective.julian() < dtEffective.julian()) dtEffective = dtCompEffective;
		}

		return dtEffective;
	}

	/**
	 * Return the maturity date of the basket product
	 * 
	 * @return Maturity date of the basket product
	 */

	public org.drip.analytics.date.JulianDate maturity()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		int iNumComp = aComp.length;

		org.drip.analytics.date.JulianDate dtMaturity = aComp[0].maturity();

		for (int i = 1; i < iNumComp; ++i) {
			org.drip.analytics.date.JulianDate dtCompMaturity = aComp[i].maturity();

			if (dtCompMaturity.julian() < dtMaturity.julian()) dtMaturity = dtCompMaturity;
		}

		return dtMaturity;
	}

	/**
	 * Get the basket product's coupon periods
	 * 
	 * @return List of CouponPeriods
	 */

	public java.util.List<org.drip.analytics.cashflow.CouponPeriod> couponPeriod()
	{
		java.util.Set<org.drip.analytics.cashflow.CouponPeriod> setPeriod =
			org.drip.analytics.support.AnalyticsHelper.AggregateComponentPeriods (components());

		if (null == setPeriod || 0 == setPeriod.size()) return null;

		java.util.List<org.drip.analytics.cashflow.CouponPeriod> lsCouponPeriod = new
			java.util.ArrayList<org.drip.analytics.cashflow.CouponPeriod>();

		for (org.drip.analytics.cashflow.CouponPeriod p : setPeriod) {
			if (null != p) lsCouponPeriod.add (p);
		}

		return lsCouponPeriod;
	}

	/**
	 * Get the first coupon date
	 * 
	 * @return First Coupon Date
	 */

	public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		int iNumComp = aComp.length;

		org.drip.analytics.date.JulianDate dtFirstCoupon = aComp[0].firstCouponDate();

		for (int i = 1; i < iNumComp; ++i) {
			if (dtFirstCoupon.julian() > aComp[i].firstCouponDate().julian())
				dtFirstCoupon = aComp[i].firstCouponDate();
		}

		return dtFirstCoupon;
	}

	/**
	 * Generate a full list of the basket product measures for the full input set of market parameters
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param csqs Market Parameters
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Map of measure name and value
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapBasketOP = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		org.drip.product.definition.FixedIncomeComponent[] aComp = components();

		double[] adblWeight = weights();

		int iNumComp = aComp.length;

		for (int i = 0; i < iNumComp; ++i) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapCompOP = aComp[i].value
				(valParams, pricerParams, csqs, quotingParams);

			if (null == mapCompOP || 0 == mapCompOP.size()) continue;

			java.util.Set<java.util.Map.Entry<java.lang.String, java.lang.Double>> mapCompOPES =
				mapCompOP.entrySet();

			if (null == mapCompOPES) continue;

			for (java.util.Map.Entry<java.lang.String, java.lang.Double> meCompOP : mapCompOPES) {
				if (null == meCompOP) continue;

				java.lang.String strKey = meCompOP.getKey();

				if (null == strKey || strKey.isEmpty()) continue;

				java.lang.Double dblCompValue = mapCompOP.get (strKey);

				java.lang.Double dblBasketValue = mapBasketOP.get (strKey);

				if (MEASURE_AGGREGATION_TYPE_CUMULATIVE == measureAggregationType (strKey))
					mapBasketOP.put (strKey, (null == dblCompValue ? 0. : dblCompValue) + (null ==
						dblBasketValue ? 0. : dblBasketValue));
				else if (MEASURE_AGGREGATION_TYPE_WEIGHTED_CUMULATIVE == measureAggregationType (strKey) &&
					null != adblWeight)
					mapBasketOP.put (strKey, (null == dblCompValue ? 0. : adblWeight[i] * dblCompValue) +
						(null == dblBasketValue ? 0. : dblBasketValue));
				else if (MEASURE_AGGREGATION_TYPE_UNIT_ACCUMULATE == measureAggregationType (strKey))
					mapBasketOP.put (aComp[i].name() + "[" + strKey + "]", (null == dblCompValue ? 0. :
						dblCompValue));
			}
		}

		mapBasketOP.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapBasketOP;
	}

	/**
	 * Calculate the value of the given basket product measure
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param csqs Market Parameters
	 * @param quotingParams Quoting Parameters
	 * @param strMeasure Measure String
	 * 
	 * @return Double measure value
	 * 
	 * @throws java.lang.Exception Thrown if the measure cannot be calculated
	 */

	public double measureValue (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final java.lang.String strMeasure)
		throws java.lang.Exception
	{
		return measureValue (strMeasure, value (valParams, pricerParams, csqs, quotingParams));
	}

	/**
	 * Generate a full list of the basket product measures for the set of scenario market parameters present
	 * 	in the org.drip.param.definition.MarketParams
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mpc org.drip.param.definition.MarketParams
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return BasketOutput object
	 */

	public org.drip.analytics.output.BasketMeasures measures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ScenarioMarketParams mpc,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == mpc) return null;

		long lStart = System.nanoTime();

		org.drip.analytics.output.BasketMeasures bkop = new org.drip.analytics.output.BasketMeasures();

		if (null == (bkop._mBase = value (valParams, pricerParams, mpc.getScenMarketParams (this, "Base"),
			quotingParams)))
			return null;

		FlatDeltaGammaMeasureMap dgmmCredit = accumulateDeltaGammaMeasures (valParams, pricerParams,
			mpc.getScenMarketParams (this, "FlatCreditBumpUp"), mpc.getScenMarketParams (this,
				"FlatCreditBumpDn"), quotingParams, bkop._mBase);

		if (null != dgmmCredit && null != (bkop._mFlatCreditDelta = dgmmCredit._mapDelta))
			bkop._mFlatCreditGamma = dgmmCredit._mapGamma;

		FlatDeltaGammaMeasureMap dgmmRates = accumulateDeltaGammaMeasures (valParams, pricerParams,
			mpc.getScenMarketParams (this, "FlatIRBumpUp"), mpc.getScenMarketParams (this, "FlatIRBumpDn"),
				quotingParams, bkop._mBase);

		if (null != dgmmRates && null != (bkop._mFlatIRDelta = dgmmRates._mapDelta))
			bkop._mFlatIRGamma = dgmmRates._mapGamma;

		FlatDeltaGammaMeasureMap dgmmRecovery = accumulateDeltaGammaMeasures (valParams, pricerParams,
			mpc.getScenMarketParams (this, "FlatRRBumpUp"), mpc.getScenMarketParams (this, "FlatRRBumpDn"),
				quotingParams, bkop._mBase);

		if (null != dgmmRecovery && null != (bkop._mFlatRRDelta = dgmmRates._mapDelta))
			bkop._mFlatRRGamma = dgmmRates._mapGamma;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapCSQSIRTenorUp = mpc.dcFlatBump (this, true);

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapCSQSIRTenorDown = mpc.dcFlatBump (this, false);

		TenorDeltaGammaMeasureMap mapDGMMRatesTenor = accumulateTenorDeltaGammaMeasures (valParams,
			pricerParams, mapCSQSIRTenorUp, mapCSQSIRTenorDown, quotingParams, bkop._mBase, null);

		if (null != mapDGMMRatesTenor) {
			bkop._mmIRDelta = mapDGMMRatesTenor._mmDelta;
			bkop._mmIRGamma = mapDGMMRatesTenor._mmGamma;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapCSQSCreditTenorUp = mpc.creditFlatBump (this, true);

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.market.CurveSurfaceQuoteSet>
			mapCSQSCreditTenorDown = mpc.creditFlatBump (this, false);

		TenorDeltaGammaMeasureMap mapDGMMCreditComp = accumulateTenorDeltaGammaMeasures (valParams,
			pricerParams, mapCSQSCreditTenorUp, mapCSQSCreditTenorDown, quotingParams, bkop._mBase, null);

		if (null != mapDGMMCreditComp) {
			bkop._mmCreditDelta = mapDGMMCreditComp._mmDelta;
			bkop._mmCreditGamma = mapDGMMCreditComp._mmGamma;
		}

		TenorDeltaGammaMeasureMap mapDGMMRecoveryTenor = accumulateTenorDeltaGammaMeasures (valParams,
			pricerParams, mpc.recoveryFlatBump (this, true), mpc.recoveryFlatBump (this, false),
				quotingParams, bkop._mBase, null);

		if (null != mapDGMMRecoveryTenor) {
			bkop._mmRRDelta = mapDGMMRecoveryTenor._mmDelta;
			bkop._mmRRGamma = mapDGMMRecoveryTenor._mmGamma;
		}

		ComponentFactorTenorDeltaGammaMeasureMap mapCompRatesTenorDGMM =
			accumulateComponentWiseTenorDeltaGammaMeasures (valParams, pricerParams, mapCSQSCreditTenorUp,
				mapCSQSIRTenorUp, mapCSQSIRTenorDown, quotingParams, bkop._mBase);

		if (null != mapCompRatesTenorDGMM) {
			bkop._mmmIRTenorDelta = mapCompRatesTenorDGMM._mmmDelta;
			bkop._mmmIRTenorGamma = mapCompRatesTenorDGMM._mmmGamma;
		}

		ComponentFactorTenorDeltaGammaMeasureMap mapCompCreditTenorDGMM =
			accumulateComponentWiseTenorDeltaGammaMeasures (valParams, pricerParams, mapCSQSCreditTenorUp,
				mapCSQSCreditTenorUp, mapCSQSCreditTenorDown, quotingParams, bkop._mBase);

		if (null != mapCompCreditTenorDGMM) {
			bkop._mmmCreditTenorDelta = mapCompCreditTenorDGMM._mmmDelta;
			bkop._mmmCreditTenorGamma = mapCompCreditTenorDGMM._mmmGamma;
		}

		bkop._dblCalcTime = (System.nanoTime() - lStart) * 1.e-09;

		return bkop;
	}

	/**
	 * Compute Basket's Custom Scenario Measures
	 * 
	 * @param valParams Valuation Parameters
	 * @param pricerParams Pricer Parameters
	 * @param mpc Market Parameters Container
	 * @param strCustomScenName Custom Scenario Name
	 * @param quotingParams Quoting Parameters
	 * @param mapBase Map of Base Measures
	 * 
	 * @return Basket's Custom Scenario Measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> customScenarioMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ScenarioMarketParams mpc,
		final java.lang.String strCustomScenName,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapBase)
	{
		if (null == valParams || null == mpc) return null;

		if (null == mapBase && null == mpc.getScenMarketParams (this, "Base")) return null;

		if (null == mapBase) {
			org.drip.param.market.CurveSurfaceQuoteSet csqs = mpc.getScenMarketParams (this, "Base");

			if (null == (mapBase = value (valParams, pricerParams, csqs, quotingParams))) return null;
		}

		org.drip.param.market.CurveSurfaceQuoteSet csqsScen = mpc.getScenMarketParams (this,
			strCustomScenName);

		if (null == csqsScen) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapScenMeasures = value
			(valParams, pricerParams, csqsScen, quotingParams);

		if (null == mapScenMeasures || null != mapScenMeasures.entrySet()) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapOP = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapScenMeasures.entrySet()) {
			if (null == me || null == me.getKey()) continue;

			mapOP.put (me.getKey(), me.getValue() - mapBase.get (me.getKey()));
		}

		return mapOP;
	}
}

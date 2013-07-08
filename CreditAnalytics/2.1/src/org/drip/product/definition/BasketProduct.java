
package org.drip.product.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 *  This abstract class extends BasketMarketParamRef. Provides methods for getting the basket’s components,
 *  	notional, coupon, effective date, maturity date, coupon amount, and list of coupon periods.
 *  
 * @author Lakshmi Krishnamurthy
 */

public abstract class BasketProduct extends org.drip.service.stream.Serializer implements
	org.drip.product.definition.BasketMarketParamRef {
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
		java.util.Map<java.lang.String, java.lang.Double> _mapDelta = null;
		java.util.Map<java.lang.String, java.lang.Double> _mapGamma = null;

		FlatDeltaGammaMeasureMap (
			final java.util.Map<java.lang.String, java.lang.Double> mapDelta,
			final java.util.Map<java.lang.String, java.lang.Double> mapGamma)
		{
			_mapDelta = mapDelta;
			_mapGamma = mapGamma;
		}
	}

	class TenorDeltaGammaMeasureMap {
		java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>> _mmDelta = null;
		java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>> _mmGamma = null;

		TenorDeltaGammaMeasureMap (
			final java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>> mmDelta,
			final java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>> mmGamma)
		{
			_mmDelta = mmDelta;
			_mmGamma = mmGamma;
		}
	}

	class ComponentFactorTenorDeltaGammaMeasureMap {
		java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.util.Map<java.lang.String,
			java.lang.Double>>> _mmmDelta = null;
		java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.util.Map<java.lang.String,
			java.lang.Double>>> _mmmGamma = null;

		ComponentFactorTenorDeltaGammaMeasureMap (
			final java.util.Map<java.lang.String, java.util.Map<java.lang.String,
				java.util.Map<java.lang.String, java.lang.Double>>> mmmDelta,
			final java.util.Map<java.lang.String, java.util.Map<java.lang.String,
				java.util.Map<java.lang.String, java.lang.Double>>> mmmGamma)
		{
			_mmmDelta = mmmDelta;
			_mmmGamma = mmmGamma;
		}
	}

	private FlatDeltaGammaMeasureMap accumulateDeltaGammaMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.BasketMarketParams bmpUp,
		final org.drip.param.definition.BasketMarketParams bmpDown,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final java.util.Map<java.lang.String, java.lang.Double> mapBaseMeasures)
	{
		if (null == bmpUp) return null;

		java.util.Map<java.lang.String, java.lang.Double> mapUpMeasures = value (valParams, pricerParams,
			bmpUp, quotingParams);

		if (null == mapUpMeasures || 0 == mapUpMeasures.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.String, java.lang.Double>> mapUpMeasuresES =
			mapUpMeasures.entrySet();

		if (null == mapUpMeasuresES) return null;

		java.util.Map<java.lang.String, java.lang.Double> mapDeltaMeasures = new
			java.util.HashMap<java.lang.String, java.lang.Double>();

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> meUp : mapUpMeasuresES) {
			if (null == meUp) continue;

			java.lang.String strKey = meUp.getKey();

			if (null == strKey || strKey.isEmpty()) continue;

			java.lang.Double dblBase = mapBaseMeasures.get (strKey);

			java.lang.Double dblUp = meUp.getValue();

			mapDeltaMeasures.put (strKey, (null == dblUp ? 0. : dblUp) - (null == dblBase ? 0. : dblBase));
		}

		if (null == bmpDown) return new FlatDeltaGammaMeasureMap (mapDeltaMeasures, null);

		java.util.Map<java.lang.String, java.lang.Double> mapDownMeasures = value (valParams, pricerParams,
			bmpDown, quotingParams);

		if (null == mapDownMeasures || 0 == mapDownMeasures.size())
			return new FlatDeltaGammaMeasureMap (mapDeltaMeasures, null);

		java.util.Set<java.util.Map.Entry<java.lang.String, java.lang.Double>> mapDownMeasuresES =
			mapDownMeasures.entrySet();

		if (null == mapDownMeasuresES) return new FlatDeltaGammaMeasureMap (mapDeltaMeasures, null);

		java.util.Map<java.lang.String, java.lang.Double> mapGammaMeasures = new
			java.util.HashMap<java.lang.String, java.lang.Double>();

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
		final java.util.Map<java.lang.String, org.drip.param.definition.BasketMarketParams> mapTenorUpBMP,
		final java.util.Map<java.lang.String, org.drip.param.definition.BasketMarketParams> mapTenorDownBMP,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final java.util.Map<java.lang.String, java.lang.Double> mapBaseMeasures,
		final ComponentCurve compCurve)
	{
		if (null == mapTenorUpBMP || 0 == mapTenorUpBMP.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.String, org.drip.param.definition.BasketMarketParams>>
			mapESTenorUpBMP = mapTenorUpBMP.entrySet();

		if (null == mapESTenorUpBMP || 0 == mapESTenorUpBMP.size()) return null;

		java.util.Map<java.lang.String, FlatDeltaGammaMeasureMap> mapTenorDGMM = new
			java.util.HashMap<java.lang.String, FlatDeltaGammaMeasureMap>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.BasketMarketParams> meTenorUpBMP
			: mapESTenorUpBMP) {
			if (null == meTenorUpBMP) continue;

			java.lang.String strTenorKey = meTenorUpBMP.getKey();

			if (null == strTenorKey || strTenorKey.isEmpty()) continue;

			org.drip.param.definition.BasketMarketParams bmpTenorUp = meTenorUpBMP.getValue();

			org.drip.param.definition.BasketMarketParams bmpTenorDown = mapTenorDownBMP.get (strTenorKey);

			org.drip.analytics.definition.CreditCurve ccVirginUp = null;
			org.drip.analytics.definition.CreditCurve ccVirginDown = null;

			if (null != bmpTenorUp && null != compCurve && null != compCurve._cc && null !=
				compCurve._strName && !compCurve._strName.isEmpty()) {
				ccVirginUp = bmpTenorUp.getCC (compCurve._strName);

				bmpTenorUp.addCC (compCurve._strName, compCurve._cc);

				if (null != bmpTenorDown) {
					ccVirginDown = bmpTenorDown.getCC (compCurve._strName);

					bmpTenorDown.addCC (compCurve._strName, compCurve._cc);
				}
			}

			mapTenorDGMM.put (strTenorKey, accumulateDeltaGammaMeasures (valParams, pricerParams, bmpTenorUp,
				bmpTenorDown, quotingParams, mapBaseMeasures));

			if (null != bmpTenorUp && null != compCurve && null != compCurve._strName &&
				!compCurve._strName.isEmpty() && null != ccVirginUp)
				bmpTenorUp.addCC (compCurve._strName, ccVirginUp);

			if (null != bmpTenorDown && null != compCurve && null != compCurve._strName &&
				!compCurve._strName.isEmpty() && null != ccVirginDown)
				bmpTenorDown.addCC (compCurve._strName, ccVirginDown);
		}

		if (0 == mapTenorDGMM.size()) return null;

		java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>> mmDelta = new
			java.util.HashMap<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>>();

		java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>> mmGamma = new
			java.util.HashMap<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>>();

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
		final java.util.Map<java.lang.String, org.drip.param.definition.BasketMarketParams> mapComponentBMP,
		final java.util.Map<java.lang.String, org.drip.param.definition.BasketMarketParams> mapTenorUpBMP,
		final java.util.Map<java.lang.String, org.drip.param.definition.BasketMarketParams> mapTenorDownBMP,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final java.util.Map<java.lang.String, java.lang.Double> mapBaseMeasures)
	{
		if (null == mapComponentBMP || 0 == mapComponentBMP.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.String, org.drip.param.definition.BasketMarketParams>>
			mapESComponentBMP = mapComponentBMP.entrySet();

		if (null == mapESComponentBMP || 0 == mapESComponentBMP.size()) return null;

		java.util.Map<java.lang.String, TenorDeltaGammaMeasureMap> mapComponentTenorDGMM = new
			java.util.HashMap<java.lang.String, TenorDeltaGammaMeasureMap>();

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.BasketMarketParams>
			meComponentBMP : mapESComponentBMP) {
			if (null == meComponentBMP) continue;

			java.lang.String strComponentName = meComponentBMP.getKey();

			if (null == strComponentName || strComponentName.isEmpty()) continue;

			org.drip.param.definition.BasketMarketParams bmpComponent = meComponentBMP.getValue();

			if (null != bmpComponent)
				mapComponentTenorDGMM.put (strComponentName, accumulateTenorDeltaGammaMeasures (valParams,
					pricerParams, mapTenorUpBMP, mapTenorDownBMP, quotingParams, mapBaseMeasures, new
						ComponentCurve (strComponentName, bmpComponent.getCC (strComponentName))));
		}

		if (0 == mapComponentTenorDGMM.size()) return null;

		java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.util.Map<java.lang.String,
			java.lang.Double>>> mmmCompRatesDelta = new java.util.HashMap<java.lang.String,
				java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>>>();

		java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.util.Map<java.lang.String,
			java.lang.Double>>> mmmCompRatesGamma = new java.util.HashMap<java.lang.String,
				java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>>>();

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

	protected double getMeasure (
		final java.lang.String strMeasure,
		final java.util.Map<java.lang.String, java.lang.Double> mapCalc)
		throws java.lang.Exception
	{
		if (null == strMeasure || strMeasure.isEmpty() || null == mapCalc || null == mapCalc.entrySet())
			throw new java.lang.Exception ("BasketProduct::getMeasure => Invalid Params");

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapCalc.entrySet()) {
			if (null != me && null != me.getKey() && me.getKey().equals (strMeasure)) return me.getValue();
		}

		throw new java.lang.Exception ("BasketProduct::getMeasure => " + strMeasure +
			" is an invalid measure!");
	}

	/**
	 * Returns the basket name
	 * 
	 * @return Name of the basket product
	 */

	public abstract java.lang.String getName();

	/**
	 * Returns the Components in the Basket
	 * 
	 * @return Components in the Basket
	 */

	public abstract org.drip.product.definition.Component[] getComponents();

	@Override public java.util.Set<java.lang.String> getComponentIRCurveNames()
	{
		org.drip.product.definition.Component[] aComp = getComponents();

		int iNumComp = aComp.length;

		java.util.Set<java.lang.String> sIR = new java.util.HashSet<java.lang.String>();

		for (int i = 0; i < iNumComp; ++i) {
			sIR.add (aComp[i].getEDSFCurveName());

			sIR.add (aComp[i].getIRCurveName());

			sIR.add (aComp[i].getRatesForwardCurveName());

			sIR.add (aComp[i].getTreasuryCurveName());
		}

		return sIR;
	}

	@Override public java.util.Set<java.lang.String> getComponentCreditCurveNames()
	{
		org.drip.product.definition.Component[] aComp = getComponents();

		int iNumComp = aComp.length;

		java.util.Set<java.lang.String> sCC = new java.util.HashSet<java.lang.String>();

		for (int i = 0; i < iNumComp; ++i)
			sCC.add (aComp[i].getCreditCurveName());

		return sCC;
	}

	/**
	 * Returns the initial notional of the basket product
	 * 
	 * @return Initial notional of the basket product
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public double getInitialNotional()
		throws java.lang.Exception
	{
		org.drip.product.definition.Component[] aComp = getComponents();

		int iNumComp = aComp.length;
		double dblInitialNotional = 0.;

		for (int i = 0; i < iNumComp; ++i)
			dblInitialNotional += aComp[i].getInitialNotional();

		return dblInitialNotional;
	}

	/**
	 * Retrieves the notional at the given date
	 * 
	 * @param dblDate Double JulianDate
	 * 
	 * @return Notional
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public double getNotional (
		final double dblDate)
		throws java.lang.Exception
	{
		org.drip.product.definition.Component[] aComp = getComponents();

		double dblNotional = 0.;
		int iNumComp = aComp.length;

		for (int i = 0; i < iNumComp; ++i)
			dblNotional += aComp[i].getNotional (dblDate);

		return dblNotional;
	}

	/**
	 * Retrieves the time-weighted notional between 2 given dates
	 * 
	 * @param dblDate1 Double JulianDate first
	 * @param dblDate2 Double JulianDate second
	 * 
	 * @return Notional
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public double getNotional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		org.drip.product.definition.Component[] aComp = getComponents();

		double dblNotional = 0.;
		int iNumComp = aComp.length;

		for (int i = 0; i < iNumComp; ++i)
			dblNotional += aComp[i].getNotional (dblDate1, dblDate2);

		return dblNotional;
	}

	/**
	 * Retrieves the basket product's coupon amount at the given date
	 * 
	 * @param dblDate Double JulianDate
	 * @param bmp Basket Market Parameters
	 * 
	 * @return Coupon Amount
	 * 
	 * @throws java.lang.Exception Thrown if coupon cannot be calculated
	 */

	public double getCoupon (
		final double dblDate,
		final org.drip.param.definition.BasketMarketParams bmp)
		throws java.lang.Exception
	{
		double dblNotional = getNotional (dblDate);

		if (null == bmp || 0. == dblNotional || !org.drip.math.common.NumberUtil.IsValid (dblNotional))
			throw new java.lang.Exception ("BasketProduct::getCoupon => Cannot extract basket notional");

		org.drip.product.definition.Component[] aComp = getComponents();

		double dblCoupon = 0.;
		int iNumComp = aComp.length;

		for (int i = 0; i < iNumComp; ++i)
			dblCoupon += aComp[i].getCoupon (dblDate, bmp.getComponentMarketParams (aComp[i]));

		return dblCoupon / dblNotional;
	}

	/**
	 * Returns the effective date of the basket product
	 * 
	 * @return Effective date of the basket product
	 */

	public org.drip.analytics.date.JulianDate getEffectiveDate()
	{
		org.drip.product.definition.Component[] aComp = getComponents();

		int iNumComp = aComp.length;

		org.drip.analytics.date.JulianDate dtEffective = aComp[0].getEffectiveDate();

		for (int i = 1; i < iNumComp; ++i) {
			org.drip.analytics.date.JulianDate dtCompEffective = aComp[i].getEffectiveDate();

			if (dtCompEffective.getJulian() < dtEffective.getJulian()) dtEffective = dtCompEffective;
		}

		return dtEffective;
	}

	/**
	 * Returns the maturity date of the basket product
	 * 
	 * @return Maturity date of the basket product
	 */

	public org.drip.analytics.date.JulianDate getMaturityDate()
	{
		org.drip.product.definition.Component[] aComp = getComponents();

		int iNumComp = aComp.length;

		org.drip.analytics.date.JulianDate dtMaturity = aComp[0].getMaturityDate();

		for (int i = 1; i < iNumComp; ++i) {
			org.drip.analytics.date.JulianDate dtCompMaturity = aComp[i].getMaturityDate();

			if (dtCompMaturity.getJulian() < dtMaturity.getJulian()) dtMaturity = dtCompMaturity;
		}

		return dtMaturity;
	}

	/**
	 * Gets the basket product's coupon periods
	 * 
	 * @return List of CouponPeriods
	 */

	public java.util.List<org.drip.analytics.period.CouponPeriod> getCouponPeriod()
	{
		java.util.Set<org.drip.analytics.period.Period> setPeriod =
			org.drip.analytics.support.AnalyticsHelper.AggregateComponentPeriods (getComponents());

		if (null == setPeriod || 0 == setPeriod.size()) return null;

		java.util.List<org.drip.analytics.period.CouponPeriod> lsCouponPeriod = new
			java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

		for (org.drip.analytics.period.Period p : setPeriod) {
			if (null != p && p instanceof org.drip.analytics.period.CouponPeriod)
				lsCouponPeriod.add ((org.drip.analytics.period.CouponPeriod) p);
		}

		return lsCouponPeriod;
	}

	/**
	 * Gets the first coupon date
	 * 
	 * @return First Coupon Date
	 */

	public org.drip.analytics.date.JulianDate getFirstCouponDate()
	{
		org.drip.product.definition.Component[] aComp = getComponents();

		int iNumComp = aComp.length;

		org.drip.analytics.date.JulianDate dtFirstCoupon = aComp[0].getFirstCouponDate();

		for (int i = 1; i < iNumComp; ++i) {
			if (dtFirstCoupon.getJulian() > aComp[i].getFirstCouponDate().getJulian())
				dtFirstCoupon = aComp[i].getFirstCouponDate();
		}

		return dtFirstCoupon;
	}

	/**
	 * Generates a full list of the basket product measures for the full input set of market parameters
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param bmp BasketMarketParams
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return Map of measure name and value
	 */

	public java.util.Map<java.lang.String, java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.BasketMarketParams bmp,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == valParams || null == bmp) return null;

		java.util.Map<java.lang.String, java.lang.Double> mapBasketOP = new
			java.util.HashMap<java.lang.String, java.lang.Double>();

		org.drip.product.definition.Component[] aComp = getComponents();

		int iNumComp = aComp.length;

		for (int i = 0; i < iNumComp; ++i) {
			java.util.Map<java.lang.String, java.lang.Double> mapCompOP = aComp[i].value (valParams,
				pricerParams, bmp.getComponentMarketParams (aComp[i]), quotingParams);

			if (null != mapCompOP && 0 != mapCompOP.size()) {
				for (java.util.Map.Entry<java.lang.String, java.lang.Double> meCompOP : mapCompOP.entrySet())
				{
					if (null == meCompOP) continue;

					java.lang.String strKey = meCompOP.getKey();

					if (null == strKey || strKey.isEmpty()) continue;

					java.lang.Double dblCompValue = mapCompOP.get (strKey);

					java.lang.Double dblBasketValue = mapBasketOP.get (strKey);

					mapBasketOP.put (strKey, (null == dblCompValue ? 0. : dblCompValue) + (null ==
						dblBasketValue ? 0. : dblBasketValue));
				}
			}
		}

		return mapBasketOP;
	}

	/**
	 * Calculates the value of the given basket product measure
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param bmp BasketMarketParams
	 * @param quotingParams Quoting Parameters
	 * @param strMeasure Measure String
	 * 
	 * @return Double measure value
	 * 
	 * @throws java.lang.Exception Thrown if the measure cannot be calculated
	 */

	public double calcMeasureValue (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.BasketMarketParams bmp,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final java.lang.String strMeasure)
		throws java.lang.Exception
	{
		return getMeasure (strMeasure, value (valParams, pricerParams, bmp, quotingParams));
	}

	/**
	 * Generates a full list of the basket product measures for the set of scenario market parameters present
	 * 	in the org.drip.param.definition.MarketParams
	 * 
	 * @param valParams ValuationParams
	 * @param pricerParams PricerParams
	 * @param mpc org.drip.param.definition.MarketParams
	 * @param quotingParams Quoting Parameters
	 * 
	 * @return BasketOutput object
	 */

	public org.drip.analytics.output.BasketMeasures calcMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.MarketParams mpc,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == valParams || null == mpc) return null;

		long lStart = System.nanoTime();

		org.drip.analytics.output.BasketMeasures bkop = new org.drip.analytics.output.BasketMeasures();

		if (null == (bkop._mBase = value (valParams, pricerParams, mpc.getScenBMP (this, "Base"),
			quotingParams)))
			return null;

		FlatDeltaGammaMeasureMap dgmmCredit = accumulateDeltaGammaMeasures (valParams, pricerParams,
			mpc.getScenBMP (this, "FlatCreditBumpUp"), mpc.getScenBMP (this, "FlatCreditBumpDn"),
				quotingParams, bkop._mBase);

		if (null != dgmmCredit && null != (bkop._mFlatCreditDelta = dgmmCredit._mapDelta))
			bkop._mFlatCreditGamma = dgmmCredit._mapGamma;

		FlatDeltaGammaMeasureMap dgmmRates = accumulateDeltaGammaMeasures (valParams, pricerParams,
			mpc.getScenBMP (this, "FlatIRBumpUp"), mpc.getScenBMP (this, "FlatIRBumpDn"), quotingParams,
				bkop._mBase);

		if (null != dgmmRates && null != (bkop._mFlatIRDelta = dgmmRates._mapDelta))
			bkop._mFlatIRGamma = dgmmRates._mapGamma;

		FlatDeltaGammaMeasureMap dgmmRecovery = accumulateDeltaGammaMeasures (valParams, pricerParams,
			mpc.getScenBMP (this, "FlatRRBumpUp"), mpc.getScenBMP (this, "FlatRRBumpDn"), quotingParams,
				bkop._mBase);

		if (null != dgmmRecovery && null != (bkop._mFlatRRDelta = dgmmRates._mapDelta))
			bkop._mFlatRRGamma = dgmmRates._mapGamma;

		java.util.Map<java.lang.String, org.drip.param.definition.BasketMarketParams> mapBMPIRTenorUp =
			mpc.getIRBumpBMP (this, true);

		java.util.Map<java.lang.String, org.drip.param.definition.BasketMarketParams> mapBMPIRTenorDown =
			mpc.getIRBumpBMP (this, false);

		TenorDeltaGammaMeasureMap mapDGMMRatesTenor = accumulateTenorDeltaGammaMeasures (valParams,
			pricerParams, mapBMPIRTenorUp, mapBMPIRTenorDown, quotingParams, bkop._mBase, null);

		if (null != mapDGMMRatesTenor) {
			bkop._mmIRDelta = mapDGMMRatesTenor._mmDelta;
			bkop._mmIRGamma = mapDGMMRatesTenor._mmGamma;
		}

		java.util.Map<java.lang.String, org.drip.param.definition.BasketMarketParams> mapBMPCreditTenorUp =
			mpc.getCreditBumpBMP (this, true);

		java.util.Map<java.lang.String, org.drip.param.definition.BasketMarketParams> mapBMPCreditTenorDown =
			mpc.getCreditBumpBMP (this, false);

		TenorDeltaGammaMeasureMap mapDGMMCreditComp = accumulateTenorDeltaGammaMeasures (valParams,
			pricerParams, mapBMPCreditTenorUp, mapBMPCreditTenorDown, quotingParams, bkop._mBase, null);

		if (null != mapDGMMCreditComp) {
			bkop._mmCreditDelta = mapDGMMCreditComp._mmDelta;
			bkop._mmCreditGamma = mapDGMMCreditComp._mmGamma;
		}

		TenorDeltaGammaMeasureMap mapDGMMRecoveryTenor = accumulateTenorDeltaGammaMeasures (valParams,
			pricerParams, mpc.getRecoveryBumpBMP (this, true), mpc.getRecoveryBumpBMP (this, false),
				quotingParams, bkop._mBase, null);

		if (null != mapDGMMRecoveryTenor) {
			bkop._mmRRDelta = mapDGMMRecoveryTenor._mmDelta;
			bkop._mmRRGamma = mapDGMMRecoveryTenor._mmGamma;
		}

		ComponentFactorTenorDeltaGammaMeasureMap mapCompRatesTenorDGMM =
			accumulateComponentWiseTenorDeltaGammaMeasures (valParams, pricerParams, mapBMPCreditTenorUp,
				mapBMPIRTenorUp, mapBMPIRTenorDown, quotingParams, bkop._mBase);

		if (null != mapCompRatesTenorDGMM) {
			bkop._mmmIRTenorDelta = mapCompRatesTenorDGMM._mmmDelta;
			bkop._mmmIRTenorGamma = mapCompRatesTenorDGMM._mmmGamma;
		}

		ComponentFactorTenorDeltaGammaMeasureMap mapCompCreditTenorDGMM =
			accumulateComponentWiseTenorDeltaGammaMeasures (valParams, pricerParams, mapBMPCreditTenorUp,
				mapBMPCreditTenorUp, mapBMPCreditTenorDown, quotingParams, bkop._mBase);

		if (null != mapCompCreditTenorDGMM) {
			bkop._mmmCreditTenorDelta = mapCompCreditTenorDGMM._mmmDelta;
			bkop._mmmCreditTenorGamma = mapCompCreditTenorDGMM._mmmGamma;
		}

		bkop._dblCalcTime = (System.nanoTime() - lStart) * 1.e-09;

		return bkop;
	}

	public java.util.Map<java.lang.String, java.lang.Double> calcCustomScenarioMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.MarketParams mpc,
		final java.lang.String strCustomScenName,
		final org.drip.param.valuation.QuotingParams quotingParams,
		java.util.Map<java.lang.String, java.lang.Double> mapBase)
	{
		if (null == valParams || null == mpc) return null;

		if (null == mapBase && null == mpc.getScenBMP (this, "Base")) return null;

		if (null == mapBase) {
			org.drip.param.definition.BasketMarketParams bmp = mpc.getScenBMP (this, "Base");

			if (null == bmp || null == (mapBase = value (valParams, pricerParams, bmp, quotingParams)))
				return null;
		}

		org.drip.param.definition.BasketMarketParams bmpScen = mpc.getScenBMP (this, strCustomScenName);

		if (null == bmpScen) return null;

		java.util.Map<java.lang.String, java.lang.Double> mapScenMeasures = value (valParams, pricerParams,
			bmpScen, quotingParams);

		if (null == mapScenMeasures || null != mapScenMeasures.entrySet()) return null;

		java.util.Map<java.lang.String, java.lang.Double> mapOP = new java.util.HashMap<java.lang.String,
			java.lang.Double>();

		for (java.util.Map.Entry<java.lang.String, java.lang.Double> me : mapScenMeasures.entrySet()) {
			if (null == me || null == me.getKey()) continue;

			mapOP.put (me.getKey(), me.getValue() - mapBase.get (me.getKey()));
		}

		return mapOP;
	}
}


package org.drip.product.rates;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

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
 * RatesBasket contains the implementation of the Basket of Rates Component legs. RatesBasket is made from
 * 	zero/more fixed and floating streams. It exports the following functionality:
 *  - Standard/Custom Constructor for the RatesBasket
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

public class RatesBasket extends org.drip.product.definition.CalibratableFixedIncomeComponent {
	private java.lang.String _strName = "";
	private org.drip.product.rates.Stream[] _aCompFixedStream = null;
	private org.drip.product.rates.Stream[] _aCompFloatStream = null;

	/**
	 * RatesBasket constructor
	 * 
	 * @param strName Basket Name
	 * @param aCompFixedStream Array of Fixed Stream Components
	 * @param aCompFloatStream Array of Float Stream Components
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public RatesBasket (
		final java.lang.String strName,
		final org.drip.product.rates.Stream[] aCompFixedStream,
		final org.drip.product.rates.Stream[] aCompFloatStream)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_aCompFixedStream =
			aCompFixedStream) || 0 == _aCompFixedStream.length || null == (_aCompFloatStream =
				aCompFloatStream) || 0 == _aCompFloatStream.length)
			throw new java.lang.Exception ("RatesBasket ctr => Invalid Inputs");
	}

	@Override public java.lang.String name()
	{
		return _strName;
	}

	@Override public java.lang.String primaryCode()
	{
		return _strName;
	}


	/**
	 * Retrieve the array of the fixed stream components
	 * 
	 * @return The array of the fixed stream components
	 */

	public org.drip.product.rates.Stream[] getFixedStreamComponents()
	{
		return _aCompFixedStream;
	}

	/**
	 * Retrieve the array of the float stream components
	 * 
	 * @return The array of the float stream components
	 */

	public org.drip.product.rates.Stream[] getFloatStreamComponents()
	{
		return _aCompFloatStream;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCurrency = new java.util.TreeSet<java.lang.String>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.rates.Stream fixedStream : _aCompFixedStream)
				setCurrency.addAll (fixedStream.cashflowCurrencySet());
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.rates.Stream floatStream : _aCompFloatStream)
				setCurrency.addAll (floatStream.cashflowCurrencySet());
		}

		return null;
	}

	@Override public java.lang.String[] payCurrency()
	{
		java.util.Set<java.lang.String> setCouponCurrency = new java.util.TreeSet<java.lang.String>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.rates.Stream fixedStream : _aCompFixedStream)
				setCouponCurrency.add (fixedStream.couponCurrency());
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.rates.Stream floatStream : _aCompFloatStream)
				setCouponCurrency.add (floatStream.couponCurrency());
		}

		int iNumCouponCurrency = setCouponCurrency.size();

		int i = 0;
		java.lang.String[] astrCouponCurrency = new java.lang.String[iNumCouponCurrency];

		for (java.lang.String strCouponCurrency : setCouponCurrency)
			astrCouponCurrency[i++] = strCouponCurrency;

		return astrCouponCurrency;
	}

	@Override public String[] principalCurrency()
	{
		java.util.Set<java.lang.String> setPrincipalCurrency = new java.util.TreeSet<java.lang.String>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.rates.Stream fixedStream : _aCompFixedStream) {
				java.lang.String[] astrPrincipalCurrency = fixedStream.principalCurrency();

				if (null != astrPrincipalCurrency && 0 != astrPrincipalCurrency.length) {
					for (java.lang.String strPrincipalCurrency : astrPrincipalCurrency)
						setPrincipalCurrency.add (strPrincipalCurrency);
				}
			}
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.rates.Stream floatStream : _aCompFloatStream) {
				java.lang.String[] astrPrincipalCurrency = floatStream.principalCurrency();

				if (null != astrPrincipalCurrency && 0 != astrPrincipalCurrency.length) {
					for (java.lang.String strPrincipalCurrency : astrPrincipalCurrency)
						setPrincipalCurrency.add (strPrincipalCurrency);
				}
			}
		}

		int iNumPrincipalCurrency = setPrincipalCurrency.size();

		int i = 0;
		java.lang.String[] astrPrincipalCurrency = new java.lang.String[iNumPrincipalCurrency];

		for (java.lang.String strPrincipalCurrency : setPrincipalCurrency)
			astrPrincipalCurrency[i++] = strPrincipalCurrency;

		return astrPrincipalCurrency;
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		java.util.Set<java.lang.String> setstrCreditLabel = new java.util.TreeSet<java.lang.String>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.rates.Stream fixedStream : _aCompFixedStream) {
				org.drip.state.identifier.CreditLabel creditLabel = fixedStream.creditLabel();

				if (null != creditLabel) setstrCreditLabel.add (creditLabel.fullyQualifiedName());
			}
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.rates.Stream floatStream : _aCompFloatStream) {
				org.drip.state.identifier.CreditLabel creditLabel = floatStream.creditLabel();

				if (null != creditLabel) setstrCreditLabel.add (creditLabel.fullyQualifiedName());
			}
		}

		int iNumCreditLabel = setstrCreditLabel.size();

		int i = 0;
		org.drip.state.identifier.CreditLabel[] aCreditLabel = new
			org.drip.state.identifier.CreditLabel[iNumCreditLabel];

		for (java.lang.String strCreditLabel : setstrCreditLabel)
			aCreditLabel[i++] = org.drip.state.identifier.CreditLabel.Standard (strCreditLabel);

		return aCreditLabel;
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		java.util.Set<java.lang.String> setstrForwardLabel = new java.util.TreeSet<java.lang.String>();

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.rates.Stream floatStream : _aCompFloatStream) {
				org.drip.state.identifier.ForwardLabel forwardLabel = floatStream.forwardLabel();

				if (null != forwardLabel) setstrForwardLabel.add (forwardLabel.fullyQualifiedName());
			}
		}

		int iNumForwardLabel = setstrForwardLabel.size();

		int i = 0;
		org.drip.state.identifier.ForwardLabel[] aForwardLabel = new
			org.drip.state.identifier.ForwardLabel[iNumForwardLabel];

		for (java.lang.String strForwardLabel : setstrForwardLabel)
			aForwardLabel[i++] = org.drip.state.identifier.ForwardLabel.Standard (strForwardLabel);

		return aForwardLabel;
	}

	@Override public org.drip.state.identifier.FundingLabel[] fundingLabel()
	{
		java.util.Set<java.lang.String> setstrFundingLabel = new java.util.TreeSet<java.lang.String>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.rates.Stream fixedStream : _aCompFixedStream)
				setstrFundingLabel.add (fixedStream.fundingLabel().fullyQualifiedName());
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.rates.Stream floatStream : _aCompFloatStream)
				setstrFundingLabel.add (floatStream.fundingLabel().fullyQualifiedName());
		}

		int iNumFundingLabel = setstrFundingLabel.size();

		int i = 0;
		org.drip.state.identifier.FundingLabel[] aFundingLabel = new
			org.drip.state.identifier.FundingLabel[iNumFundingLabel];

		for (java.lang.String strFundingLabel : setstrFundingLabel)
			aFundingLabel[i++] = org.drip.state.identifier.FundingLabel.Standard (strFundingLabel);

		return aFundingLabel;
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		java.util.Set<java.lang.String> setstrFXLabel = new java.util.TreeSet<java.lang.String>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.rates.Stream fixedStream : _aCompFixedStream) {
				org.drip.state.identifier.FXLabel fxLabel = fixedStream.fxLabel();

				if (null != fxLabel) setstrFXLabel.add (fxLabel.fullyQualifiedName());
			}
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.rates.Stream floatStream : _aCompFloatStream) {
				org.drip.state.identifier.FXLabel fxLabel = floatStream.fxLabel();

				if (null != fxLabel) setstrFXLabel.add (fxLabel.fullyQualifiedName());
			}
		}

		int iNumFXLabel = setstrFXLabel.size();

		int i = 0;
		org.drip.state.identifier.FXLabel[] aFXLabel = new org.drip.state.identifier.FXLabel[iNumFXLabel];

		for (java.lang.String strFXLabel : setstrFXLabel)
			aFXLabel[i++] = org.drip.state.identifier.FXLabel.Standard (strFXLabel);

		return aFXLabel;
	}

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strMainfestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	@Override public org.drip.product.calib.ProductQuoteSet calibQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
	{
		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null;
	}

	@Override public double initialNotional()
		throws java.lang.Exception
	{
		return 0;
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		return 0;
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		return 0;
	}

	@Override public org.drip.analytics.output.CompositePeriodCouponMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		return null;
	}

	@Override public int freq()
	{
		return 0;
	}

	@Override public org.drip.analytics.date.JulianDate effective()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate maturity()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		return null;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.CompositePeriod> cashFlowPeriod()
	{
		java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCP = new
			java.util.ArrayList<org.drip.analytics.cashflow.CompositePeriod>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.rates.Stream fixedStream : _aCompFixedStream)
				lsCP.addAll (fixedStream.cashFlowPeriod());
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.rates.Stream floatStream : _aCompFloatStream)
				lsCP.addAll (floatStream.cashFlowPeriod());
		}

		return lsCP;
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return null;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.rates.Stream fixedStream : _aCompFixedStream) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>
					mapFixedStreamResult = fixedStream.value (valParams, pricerParams, csqs, quotingParams);

				if (!org.drip.analytics.support.AnalyticsHelper.AccumulateMeasures (mapResult,
					fixedStream.name(), mapFixedStreamResult))
					return null;
			}
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.rates.Stream floatStream : _aCompFloatStream) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>
					mapFixedStreamResult = floatStream.value (valParams, pricerParams, csqs, quotingParams);

				if (!org.drip.analytics.support.AnalyticsHelper.AccumulateMeasures (mapResult,
					floatStream.name(), mapFixedStreamResult))
					return null;
			}
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		return null;
	}
}


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
	private org.drip.product.cashflow.Stream[] _aCompFixedStream = null;
	private org.drip.product.cashflow.Stream[] _aCompFloatStream = null;

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
		final org.drip.product.cashflow.Stream[] aCompFixedStream,
		final org.drip.product.cashflow.Stream[] aCompFloatStream)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_aCompFixedStream =
			aCompFixedStream) || 0 == _aCompFixedStream.length || null == (_aCompFloatStream =
				aCompFloatStream) || 0 == _aCompFloatStream.length)
			throw new java.lang.Exception ("RatesBasket ctr => Invalid Inputs");
	}

	/**
	 * RatesBasket de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if RatesBasket cannot be properly de-serialized
	 */

	public RatesBasket (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("RatesBasket de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("RatesBasket de-serializer: Empty state");

		java.lang.String strSerializedRatesBasket = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedRatesBasket || strSerializedRatesBasket.isEmpty())
			throw new java.lang.Exception ("RatesBasket de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedRatesBasket,
			fieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception ("RatesBasket de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_strName = "";
		else
			_strName = astrField[1];

		java.lang.String[] astrCompFixedStream = org.drip.quant.common.StringUtil.Split (astrField[2],
			collectionRecordDelimiter());

		if (null == astrCompFixedStream || 0 == astrCompFixedStream.length)
			throw new java.lang.Exception
				("RatesBasket de-serializer: Cannot locate fixed stream component array");

		_aCompFixedStream = new org.drip.product.cashflow.Stream[astrCompFixedStream.length];

		for (int i = 0; i < astrCompFixedStream.length; ++i) {
			if (null == astrCompFixedStream[i] || astrCompFixedStream[i].isEmpty() ||
				org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrCompFixedStream[i]))
				throw new java.lang.Exception
					("RatesBasket de-serializer: Cannot locate fixed stream component #" + i);

			_aCompFixedStream[i] = new org.drip.product.cashflow.Stream (astrCompFixedStream[i].getBytes());
		}

		java.lang.String[] astrCompFloatStream = org.drip.quant.common.StringUtil.Split (astrField[3],
			collectionRecordDelimiter());

		if (null == astrCompFloatStream || 0 == astrCompFloatStream.length)
			throw new java.lang.Exception
				("RatesBasket de-serializer: Cannot locate float stream component array");

		_aCompFloatStream = new org.drip.product.cashflow.Stream[astrCompFloatStream.length];

		for (int i = 0; i < astrCompFloatStream.length; ++i) {
			if (null == astrCompFloatStream[i] || astrCompFloatStream[i].isEmpty() ||
				org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrCompFloatStream[i]))
				throw new java.lang.Exception
					("RatesBasket de-serializer: Cannot locate floating stream component #" + i);

			_aCompFloatStream[i] = new org.drip.product.cashflow.Stream (astrCompFloatStream[i].getBytes());
		}
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

	public org.drip.product.cashflow.Stream[] getFixedStreamComponents()
	{
		return _aCompFixedStream;
	}

	/**
	 * Retrieve the array of the float stream components
	 * 
	 * @return The array of the float stream components
	 */

	public org.drip.product.cashflow.Stream[] getFloatStreamComponents()
	{
		return _aCompFloatStream;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "#";
	}

	@Override public java.lang.String collectionRecordDelimiter()
	{
		return "@";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "^";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		if (null == _strName || _strName.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strName + fieldDelimiter());

		if (null == _aCompFixedStream || 0 == _aCompFixedStream.length)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbFixStream = new java.lang.StringBuffer();

			for (org.drip.product.cashflow.Stream fixStream : _aCompFixedStream) {
				if (null == fixStream) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbFixStream.append (collectionRecordDelimiter());

				sbFixStream.append (new java.lang.String (fixStream.serialize()));
			}

			if (sbFixStream.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
			else
				sb.append (sbFixStream.toString() + fieldDelimiter());
		}

		if (null == _aCompFloatStream || 0 == _aCompFloatStream.length)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbFloatStream = new java.lang.StringBuffer();

			for (org.drip.product.cashflow.Stream floatStream : _aCompFloatStream) {
				if (null == floatStream) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbFloatStream.append (collectionRecordDelimiter());

				sbFloatStream.append (new java.lang.String (floatStream.serialize()));
			}

			if (sbFloatStream.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
			else
				sb.append (sbFloatStream.toString() + fieldDelimiter());
		}

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new RatesBasket (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCurrency = new java.util.TreeSet<java.lang.String>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.cashflow.Stream fixedStream : _aCompFixedStream)
				setCurrency.addAll (fixedStream.cashflowCurrencySet());
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.cashflow.Stream floatStream : _aCompFloatStream)
				setCurrency.addAll (floatStream.cashflowCurrencySet());
		}

		return null;
	}

	@Override public java.lang.String[] payCurrency()
	{
		java.util.Set<java.lang.String> setCouponCurrency = new java.util.TreeSet<java.lang.String>();

		if (null != _aCompFixedStream && 0 != _aCompFixedStream.length) {
			for (org.drip.product.cashflow.Stream fixedStream : _aCompFixedStream)
				setCouponCurrency.add (fixedStream.couponCurrency());
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.cashflow.Stream floatStream : _aCompFloatStream)
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
			for (org.drip.product.cashflow.Stream fixedStream : _aCompFixedStream) {
				java.lang.String[] astrPrincipalCurrency = fixedStream.principalCurrency();

				if (null != astrPrincipalCurrency && 0 != astrPrincipalCurrency.length) {
					for (java.lang.String strPrincipalCurrency : astrPrincipalCurrency)
						setPrincipalCurrency.add (strPrincipalCurrency);
				}
			}
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.cashflow.Stream floatStream : _aCompFloatStream) {
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
			for (org.drip.product.cashflow.Stream fixedStream : _aCompFixedStream) {
				org.drip.state.identifier.CreditLabel creditLabel = fixedStream.creditLabel();

				if (null != creditLabel) setstrCreditLabel.add (creditLabel.fullyQualifiedName());
			}
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.cashflow.Stream floatStream : _aCompFloatStream) {
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
			for (org.drip.product.cashflow.Stream floatStream : _aCompFloatStream) {
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
			for (org.drip.product.cashflow.Stream fixedStream : _aCompFixedStream)
				setstrFundingLabel.add (fixedStream.fundingLabel().fullyQualifiedName());
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.cashflow.Stream floatStream : _aCompFloatStream)
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
			for (org.drip.product.cashflow.Stream fixedStream : _aCompFixedStream) {
				org.drip.state.identifier.FXLabel fxLabel = fixedStream.fxLabel();

				if (null != fxLabel) setstrFXLabel.add (fxLabel.fullyQualifiedName());
			}
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.cashflow.Stream floatStream : _aCompFloatStream) {
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

	@Override public org.drip.analytics.output.CouponPeriodMetrics coupon (
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

	@Override public java.util.List<org.drip.analytics.period.CouponPeriod> cashFlowPeriod()
	{
		return null;
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
			for (org.drip.product.cashflow.Stream fixedStream : _aCompFixedStream) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>
					mapFixedStreamResult = fixedStream.value (valParams, pricerParams, csqs, quotingParams);

				if (!org.drip.analytics.support.AnalyticsHelper.AccumulateMeasures (mapResult,
					fixedStream.name(), mapFixedStreamResult))
					return null;
			}
		}

		if (null != _aCompFloatStream && 0 != _aCompFloatStream.length) {
			for (org.drip.product.cashflow.Stream floatStream : _aCompFloatStream) {
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

	public static void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.analytics.daycount.Convention.Init ("c:\\Lakshmi\\BondAnal\\Config.xml");

		org.drip.analytics.date.JulianDate dtEffective = org.drip.analytics.date.JulianDate.Today();

		org.drip.product.cashflow.Stream[] aFixedStream = new org.drip.product.cashflow.Stream[3];
		org.drip.product.cashflow.Stream[] aFloatStream = new org.drip.product.cashflow.Stream[3];

		org.drip.state.identifier.ForwardLabel forwardLabel = org.drip.state.identifier.ForwardLabel.Standard
				("ABC-RI-3M");

		org.drip.analytics.daycount.DateAdjustParams dap = new org.drip.analytics.daycount.DateAdjustParams
			(org.drip.analytics.daycount.Convention.DR_FOLL, "XYZ");

		java.util.List<org.drip.analytics.period.CouponPeriod> lsFixedCouponPeriod3Y =
			org.drip.analytics.support.PeriodBuilder.RegularPeriodSingleReset (dtEffective.julian(), "3Y",
				java.lang.Double.NaN, dap, 2, "30/360", false, true, "DEF", 100., null, 0.03, "ABC", "ABC",
					null, null);

		aFixedStream[0] = new org.drip.product.cashflow.Stream (lsFixedCouponPeriod3Y);

		java.util.List<org.drip.analytics.period.CouponPeriod> lsFloatCouponPeriod3Y =
			org.drip.analytics.support.PeriodBuilder.RegularPeriodSingleReset (dtEffective.julian(), "3Y",
				java.lang.Double.NaN, dap, 2, "30/360", false, true, "DEF", 100., null, 0., "ABC", "ABC",
					forwardLabel, null);

		aFloatStream[0] = new org.drip.product.cashflow.Stream (lsFloatCouponPeriod3Y);

		java.util.List<org.drip.analytics.period.CouponPeriod> lsFixedCouponPeriod5Y =
			org.drip.analytics.support.PeriodBuilder.RegularPeriodSingleReset (dtEffective.julian(), "5Y",
				java.lang.Double.NaN, dap, 2, "30/360", false, true, "JKL", 100., null, 0.05, "GHI", "GHI",
					null, null);

		aFixedStream[1] = new org.drip.product.cashflow.Stream (lsFixedCouponPeriod5Y);

		java.util.List<org.drip.analytics.period.CouponPeriod> lsFloatCouponPeriod5Y =
			org.drip.analytics.support.PeriodBuilder.RegularPeriodSingleReset (dtEffective.julian(), "5Y",
				java.lang.Double.NaN, dap, 2, "30/360", false, true, "JKL", 100., null, 0.05, "GHI", "GHI",
					forwardLabel, null);

		aFloatStream[1] = new org.drip.product.cashflow.Stream (lsFloatCouponPeriod5Y);

		java.util.List<org.drip.analytics.period.CouponPeriod> lsFixedCouponPeriod7Y =
			org.drip.analytics.support.PeriodBuilder.RegularPeriodSingleReset (dtEffective.julian(), "7Y",
				java.lang.Double.NaN, dap, 2, "30/360", false, true, "PQR", 100., null, 0.05, "MNO", "MNO",
					null, null);

		aFixedStream[2] = new org.drip.product.cashflow.Stream (lsFixedCouponPeriod7Y);

		java.util.List<org.drip.analytics.period.CouponPeriod> lsFloatCouponPeriod7Y =
			org.drip.analytics.support.PeriodBuilder.RegularPeriodSingleReset (dtEffective.julian(), "7Y",
				java.lang.Double.NaN, dap, 2, "30/360", false, true, "PQR", 100., null, 0.05, "MNO", "MNO",
					forwardLabel, null);

		aFloatStream[2] = new org.drip.product.cashflow.Stream (lsFloatCouponPeriod7Y);

		RatesBasket rb = new RatesBasket ("SAMRB", aFixedStream, aFloatStream);

		byte[] abRB = rb.serialize();

		System.out.println (new java.lang.String (abRB));

		RatesBasket rbDeser = (RatesBasket) rb.deserialize (abRB);

		System.out.println (new java.lang.String (rbDeser.serialize()));
	}
}

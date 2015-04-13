
package org.drip.param.market;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * CurveSurfaceQuoteSet provides implementation of the set of the market curve parameters. It serves as a
 *  place holder for the market parameters needed to value the product – discount curve, forward curve,
 *  treasury curve, credit curve, product quote, treasury quote map, and fixings map.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CurveSurfaceQuoteSet {
	private
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>>
			_mapPayCurrencyForeignCollateralDC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapEquityCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		_mapCreditCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
		_mapForwardCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		_mapFundingCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFXCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
		_mapGovvieCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapPaydownCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapRecoveryCurve = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCustomMetricVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapEquityVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapForwardVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFundingVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFXVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapGovvieVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapPaydownVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapRecoveryVolatilitySurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralCollateralCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralCreditCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralCustomMetricCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralEquityCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralForwardCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralPaydownCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCollateralRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditCreditCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditCustomMetricCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditEquityCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditForwardCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditPaydownCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCreditRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCustomMetricCustomMetricCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCustomMetricEquityCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCustomMetricForwardCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCustomMetricFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCustomMetricFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCustomMetricGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCustomMetricPaydownCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapCustomMetricRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapEquityEquityCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapEquityForwardCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapEquityFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapEquityFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapEquityGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapEquityPaydownCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapEquityRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapForwardForwardCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapForwardFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapForwardFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapForwardGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapForwardPaydownCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapForwardRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFundingFundingCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFundingFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFundingGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFundingPaydownCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFundingRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFXFXCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFXGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFXPaydownCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapFXRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapGovvieGovvieCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapGovviePaydownCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapGovvieRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapPaydownPaydownCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapPaydownRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>
		_mapRecoveryRecoveryCorrelationSurface = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.function.deterministic.R1ToR1>();

	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
		_mapProductQuote = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>();

	private org.drip.param.market.LatentStateFixingsContainer _lsfc = new
		org.drip.param.market.LatentStateFixingsContainer();

	/**
	 * Empty CurveSurfaceQuoteSet Constructor
	 */

	public CurveSurfaceQuoteSet()
	{
	}

	/**
	 * Retrieve the Discount Curve associated with the Pay Cash-flow Collateralized using a different
	 * 	Collateral Currency Numeraire
	 * 
	 * @param strPayCurrency The Pay Currency
	 * @param strCollateralCurrency The Collateral Currency
	 * 
	 * @return The Discount Curve associated with the Pay Cash-flow Collateralized using a different
	 * 	Collateral Currency Numeraire
	 */

	public org.drip.analytics.rates.DiscountCurve payCurrencyCollateralCurrencyCurve (
		final java.lang.String strPayCurrency,
		final java.lang.String strCollateralCurrency)
	{
		if (null == strPayCurrency || !_mapPayCurrencyForeignCollateralDC.containsKey (strPayCurrency) ||
			null == strCollateralCurrency)
			return null;

		return _mapPayCurrencyForeignCollateralDC.get (strPayCurrency).get (strCollateralCurrency);
	}

	/**
	 * Set the Discount Curve associated with the Pay Cash-flow Collateralized using a different
	 * 	Collateral Currency Numeraire
	 * 
	 * @param strPayCurrency The Pay Currency
	 * @param strCollateralCurrency The Collateral Currency
	 * @param dcPayCurrencyCollateralCurrency The Discount Curve associated with the Pay Cash-flow
	 *  Collateralized using a different Collateral Currency Numeraire
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setPayCurrencyCollateralCurrencyCurve (
		final java.lang.String strPayCurrency,
		final java.lang.String strCollateralCurrency,
		final org.drip.analytics.rates.DiscountCurve dcPayCurrencyCollateralCurrency)
	{
		if (null == strPayCurrency || strPayCurrency.isEmpty() || null == strCollateralCurrency ||
			strCollateralCurrency.isEmpty() || null == dcPayCurrencyCollateralCurrency)
			return false;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapCollateralCurrencyDC = new
				org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>();

		mapCollateralCurrencyDC.put (strCollateralCurrency, dcPayCurrencyCollateralCurrency);

		_mapPayCurrencyForeignCollateralDC.put (strPayCurrency, mapCollateralCurrencyDC);

		return true;
	}

	/**
	 * Retrieve the Collateral Choice Discount Curve for the specified Pay Currency
	 * 
	 * @param strPayCurrency The Pay Currency
	 * 
	 * @return Collateral Choice Discount Curve
	 */

	public org.drip.analytics.rates.DiscountCurve collateralChoiceDiscountCurve (
		final java.lang.String strPayCurrency)
	{
		if (null == strPayCurrency || !_mapPayCurrencyForeignCollateralDC.containsKey (strPayCurrency))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapCollateralCurrencyDC = _mapPayCurrencyForeignCollateralDC.get (strPayCurrency);

		int iNumCollateralizer = mapCollateralCurrencyDC.size();

		org.drip.state.curve.ForeignCollateralizedDiscountCurve[] aFCDC = new
			org.drip.state.curve.ForeignCollateralizedDiscountCurve[iNumCollateralizer];

		int i = 0;

		for (java.util.Map.Entry<java.lang.String, org.drip.analytics.rates.DiscountCurve> me :
			mapCollateralCurrencyDC.entrySet()) {
			org.drip.analytics.rates.DiscountCurve fcdc = me.getValue();

			if (!(fcdc instanceof org.drip.state.curve.ForeignCollateralizedDiscountCurve)) return null;

			aFCDC[i++] = (org.drip.state.curve.ForeignCollateralizedDiscountCurve) fcdc;
		}

		try {
			return new org.drip.state.curve.DeterministicCollateralChoiceDiscountCurve
				(mapCollateralCurrencyDC.get (strPayCurrency), aFCDC, 30);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Credit Latent State from the Label
	 * 
	 * @param creditLabel The Credit Latent State Label
	 * 
	 * @return The Credit Latent State from the Label
	 */

	public org.drip.analytics.definition.CreditCurve creditCurve (
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (null == creditLabel) return null;

		java.lang.String strCreditLabel = creditLabel.fullyQualifiedName();

		return !_mapCreditCurve.containsKey (strCreditLabel) ? null : _mapCreditCurve.get (strCreditLabel);
	}

	/**
	 * (Re)-set the Credit Curve
	 * 
	 * @param cc The Credit Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCurve (
		final org.drip.analytics.definition.CreditCurve cc)
	{
		if (null == cc) return false;

		_mapCreditCurve.put (cc.label().fullyQualifiedName(), cc);

		return true;
	}

	/**
	 * Retrieve the Equity Curve for the specified Equity Latent State Label
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * 
	 * @return Equity Curve
	 */

	public org.drip.function.deterministic.R1ToR1 equityCurve (
		final org.drip.state.identifier.EquityLabel equityLabel)
	{
		if (null == equityLabel) return null;

		java.lang.String strCode = equityLabel.fullyQualifiedName();

		return _mapEquityCurve.containsKey (strCode) ? _mapEquityCurve.get (strCode) : null;
	}

	/**
	 * (Re)-set the Equity Curve for the specified Equity Latent State Label
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param auEquity The Equity Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setEquityCurve (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.function.deterministic.R1ToR1 auEquity)
	{
		if (null == equityLabel || null == auEquity) return false;

		_mapEquityCurve.put (equityLabel.fullyQualifiedName(), auEquity);

		return true;
	}

	/**
	 * Retrieve the Forward Curve corresponding to the Label
	 * 
	 * @param forwardLabel Forward Latent State Label
	 * 
	 * @return Forward Curve
	 */

	public org.drip.analytics.rates.ForwardCurve forwardCurve (
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == forwardLabel) return null;

		java.lang.String strForwardLabel = forwardLabel.fullyQualifiedName();

		return _mapForwardCurve.containsKey (strForwardLabel) ? _mapForwardCurve.get (strForwardLabel) :
			null;
	}

	/**
	 * (Re)-set the Forward Curve
	 * 
	 * @param fc Forward Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardCurve (
		final org.drip.analytics.rates.ForwardCurve fc)
	{
		if (null == fc) return false;

		_mapForwardCurve.put (fc.label().fullyQualifiedName(), fc);

		return true;
	}

	/**
	 * Retrieve the Funding Latent State Corresponding to the Label
	 * 
	 * @param fundingLabel Funding Latent State Label
	 * 
	 * @return The Funding Latent State
	 */

	public org.drip.analytics.rates.DiscountCurve fundingCurve (
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == fundingLabel) return null;

		java.lang.String strFundingLabel = fundingLabel.fullyQualifiedName();

		return _mapFundingCurve.containsKey (strFundingLabel) ? _mapFundingCurve.get (strFundingLabel) :
			null;
	}

	/**
	 * (Re)-set the Funding Curve
	 * 
	 * @param dcFunding Funding Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingCurve (
		final org.drip.analytics.rates.DiscountCurve dc)
	{
		if (null == dc) return false;

		_mapFundingCurve.put (dc.label().fullyQualifiedName(), dc);

		return true;
	}

	/**
	 * Retrieve the FX Curve for the specified FX Latent State Label
	 * 
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return FX Curve
	 */

	public org.drip.function.deterministic.R1ToR1 fxCurve (
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == fxLabel) return null;

		java.lang.String strCode = fxLabel.fullyQualifiedName();

		return _mapFXCurve.containsKey (strCode) ? _mapFXCurve.get (strCode) : null;
	}

	/**
	 * (Re)-set the FX Curve for the specified FX Latent State Label
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param auFX The FX Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXCurve (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.function.deterministic.R1ToR1 auFX)
	{
		if (null == fxLabel || null == auFX) return false;

		_mapFXCurve.put (fxLabel.fullyQualifiedName(), auFX);

		try {
			_mapFXCurve.put (fxLabel.inverse().fullyQualifiedName(), new
				org.drip.function.deterministic1D.UnivariateReciprocal (auFX));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	/**
	 * Retrieve the Government Curve for the specified Label
	 * 
	 * @param lslGovvie Govvie Curve Latent State Label
	 * 
	 * @return Government Curve for the specified Label
	 */

	public org.drip.analytics.rates.DiscountCurve govvieCurve (
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == govvieLabel) return null;

		java.lang.String strGovvieLabel = govvieLabel.fullyQualifiedName();

		return !_mapGovvieCurve.containsKey (strGovvieLabel) ? null : _mapGovvieCurve.get (strGovvieLabel);
	}

	/**
	 * (Re)-set the Government Discount Curve
	 * 
	 * @param dcGovvie Government Discount Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovvieCurve (
		final org.drip.analytics.rates.DiscountCurve dcGovvie)
	{
		if (null == dcGovvie) return false;

		_mapGovvieCurve.put (dcGovvie.label().fullyQualifiedName(), dcGovvie);

		return true;
	}

	/**
	 * Retrieve the Pay-down Curve for the specified Equity Latent State Label
	 * 
	 * @param paydownLabel The Pay-down Latent State Label
	 * 
	 * @return Pay-down Curve
	 */

	public org.drip.function.deterministic.R1ToR1 paydownCurve (
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == paydownLabel) return null;

		java.lang.String strCode = paydownLabel.fullyQualifiedName();

		return _mapPaydownCurve.containsKey (strCode) ? _mapPaydownCurve.get (strCode) : null;
	}

	/**
	 * (Re)-set the Pay-down Curve for the specified Pay-down Latent State Label
	 * 
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auPaydown The Pay-down Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setPaydownCurve (
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auPaydown)
	{
		if (null == paydownLabel || null == auPaydown) return false;

		_mapPaydownCurve.put (paydownLabel.fullyQualifiedName(), auPaydown);

		return true;
	}

	/**
	 * Retrieve the Recovery Latent State from the Label
	 * 
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Recovery Latent State from the Label
	 */

	public org.drip.function.deterministic.R1ToR1 recoveryCurve (
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == recoveryLabel) return null;

		java.lang.String strRecoveryLabel = recoveryLabel.fullyQualifiedName();

		return !_mapRecoveryCurve.containsKey (strRecoveryLabel) ? null : _mapRecoveryCurve.get
			(strRecoveryLabel);
	}

	/**
	 * (Re)-set the Recovery Curve for the specified Recovery Latent State Label
	 * 
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auRC The Recovery Curve
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setRecoveryCurve (
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auRC)
	{
		if (null == recoveryLabel || null == auRC) return false;

		_mapRecoveryCurve.put (recoveryLabel.fullyQualifiedName(), auRC);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Collateral Curve
	 * 
	 * @param strCurrency The Collateral Currency
	 * 
	 * @return The Volatility Surface for the Collateral Currency
	 */

	public org.drip.function.deterministic.R1ToR1 collateralCurveVolSurface (
		final java.lang.String strCurrency)
	{
		if (null == strCurrency || strCurrency.isEmpty() || !_mapCollateralVolatilitySurface.containsKey
			(strCurrency))
			return null;

		return _mapCollateralVolatilitySurface.get (strCurrency);
	}

	/**
	 * (Re)-set the Volatility Surface for the specified Collateral Curve
	 * 
	 * @param strCurrency The Collateral Currency
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCurveVolSurface (
		final java.lang.String strCurrency,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == strCurrency || strCurrency.isEmpty() || null == auVolatility) return false;

		_mapCollateralVolatilitySurface.put (strCurrency, auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the Credit Latent State
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * 
	 * @return The Volatility Surface for the Credit Latent State
	 */

	public org.drip.function.deterministic.R1ToR1 creditCurveVolSurface (
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (null == creditLabel) return null;

		java.lang.String strCreditLabel = creditLabel.fullyQualifiedName();

		return  !_mapCreditVolatilitySurface.containsKey (strCreditLabel) ? null :
			_mapCreditVolatilitySurface.get (strCreditLabel);
	}

	/**
	 * (Re)-set the Volatility Surface for the Credit Latent State
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCurveVolSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == creditLabel || null == auVolatility) return false;

		_mapCreditVolatilitySurface.put (creditLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the Custom Metric Latent State
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * 
	 * @return The Volatility Surface for the Custom Metric Latent State
	 */

	public org.drip.function.deterministic.R1ToR1 customMetricVolSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel)
	{
		if (null == customMetricLabel) return null;

		java.lang.String strCustomMetricLabel = customMetricLabel.fullyQualifiedName();

		return _mapCustomMetricVolatilitySurface.containsKey (strCustomMetricLabel) ?
			_mapCustomMetricVolatilitySurface.get (strCustomMetricLabel) : null;
	}

	/**
	 * (Re)-set the Custom Metric Volatility Surface
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param auVolatility The Custom Metric Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricVolSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == customMetricLabel || null == auVolatility) return false;

		_mapCustomMetricVolatilitySurface.put (customMetricLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the Equity Latent State
	 * 
	 * @param equityLabel The Equity Curve Latent State Label
	 * 
	 * @return The Volatility Surface for the Equity Latent State
	 */

	public org.drip.function.deterministic.R1ToR1 equityCurveVolSurface (
		final org.drip.state.identifier.EquityLabel equityLabel)
	{
		if (null == equityLabel) return null;

		java.lang.String strEquityLabel = equityLabel.fullyQualifiedName();

		return  !_mapEquityVolatilitySurface.containsKey (strEquityLabel) ? null :
			_mapEquityVolatilitySurface.get (strEquityLabel);
	}

	/**
	 * (Re)-set the Volatility Surface for the Equity Latent State
	 * 
	 * @param equityLabel The Equity Curve Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setEquityCurveVolSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == equityLabel || null == auVolatility) return false;

		_mapEquityVolatilitySurface.put (equityLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Forward Latent State Label
	 * 
	 * @param forwardLabel The Forward Latent State Label
	 * 
	 * @return The Volatility Surface for the Forward Curve
	 */

	public org.drip.function.deterministic.R1ToR1 forwardCurveVolSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == forwardLabel) return null;

		java.lang.String strForwardLabel = forwardLabel.fullyQualifiedName();

		return _mapForwardVolatilitySurface.containsKey (strForwardLabel) ? _mapForwardVolatilitySurface.get
			(strForwardLabel) : null;
	}

	/**
	 * (Re)-set the Volatility Surface for the specified Forward Latent State Label
	 * 
	 * @param forwardLabel The Forward Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardCurveVolSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == forwardLabel || null == auVolatility) return false;

		_mapForwardVolatilitySurface.put (forwardLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the Funding Latent State Label
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Volatility Surface for the Funding Currency
	 */

	public org.drip.function.deterministic.R1ToR1 fundingCurveVolSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == fundingLabel) return null;

		java.lang.String strFundingLabel = fundingLabel.fullyQualifiedName();

		return _mapFundingVolatilitySurface.containsKey (strFundingLabel) ? _mapFundingVolatilitySurface.get
			(strFundingLabel) : null;
	}

	/**
	 * (Re)-set the Volatility Surface for the Funding Latent State Label
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingCurveVolSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == fundingLabel || null == auVolatility) return false;

		_mapFundingVolatilitySurface.put (fundingLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified FX Latent State Label
	 * 
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Volatility Surface for the FX Latent State Label
	 */

	public org.drip.function.deterministic.R1ToR1 fxCurveVolSurface (
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == fxLabel) return null;

		java.lang.String strCode = fxLabel.fullyQualifiedName();

		return !_mapFXVolatilitySurface.containsKey (strCode) ? null : _mapFXVolatilitySurface.get
			(strCode);
	}

	/**
	 * (Re)-set the Volatility Surface for the specified FX Latent State
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXCurveVolSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == fxLabel || null == auVolatility) return false;

		_mapFXVolatilitySurface.put (fxLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Govvie Latent State
	 * 
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Volatility Surface for the Govvie Latent State
	 */

	public org.drip.function.deterministic.R1ToR1 govvieCurveVolSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == govvieLabel) return null;

		java.lang.String strGovvieLabel = govvieLabel.fullyQualifiedName();

		return !_mapGovvieVolatilitySurface.containsKey (strGovvieLabel) ? null :
			_mapGovvieVolatilitySurface.get (strGovvieLabel);
	}

	/**
	 * (Re)-set the Volatility Surface for the Govvie Latent State
	 * 
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovvieCurveVolSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == govvieLabel || null == auVolatility) return false;

		_mapGovvieVolatilitySurface.put (govvieLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Pay-down Latent State
	 * 
	 * @param paydownLabel The Recovery Latent State Label
	 * 
	 * @return The Volatility Surface for the Pay-down Latent State
	 */

	public org.drip.function.deterministic.R1ToR1 paydownCurveVolSurface (
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == paydownLabel) return null;

		java.lang.String strPaydownLabel = paydownLabel.fullyQualifiedName();

		return !_mapPaydownVolatilitySurface.containsKey (strPaydownLabel) ? null :
			_mapPaydownVolatilitySurface.get (strPaydownLabel);
	}

	/**
	 * (Re)-set the Volatility Surface for the Pay-down Latent State
	 * 
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setPaydownCurveVolSurface (
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == paydownLabel || null == auVolatility) return false;

		_mapPaydownVolatilitySurface.put (paydownLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Volatility Surface for the specified Recovery Latent State
	 * 
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Volatility Surface for the Recovery Latent State
	 */

	public org.drip.function.deterministic.R1ToR1 recoveryCurveVolSurface (
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == recoveryLabel) return null;

		java.lang.String strRecoveryLabel = recoveryLabel.fullyQualifiedName();

		return !_mapRecoveryVolatilitySurface.containsKey (strRecoveryLabel) ? null :
			_mapRecoveryVolatilitySurface.get (strRecoveryLabel);
	}

	/**
	 * (Re)-set the Volatility Surface for the Recovery Latent State
	 * 
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auVolatility The Volatility Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setRecoveryCurveVolSurface (
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auVolatility)
	{
		if (null == recoveryLabel || null == auVolatility) return false;

		_mapRecoveryVolatilitySurface.put (recoveryLabel.fullyQualifiedName(), auVolatility);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral Currency Pair
	 * 
	 * @param strCurrency1 Collateral Currency #1
	 * @param strCurrency2 Collateral Currency #2
	 * 
	 * @return The Correlation Surface for the specified Collateral Currency Pair
	 */

	public org.drip.function.deterministic.R1ToR1 collateralCollateralCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty())
			return null;

		java.lang.String strCode = strCurrency1 + "@#" + strCurrency2;

		if (!_mapCollateralCollateralCorrelationSurface.containsKey (strCode)) return null;

		return _mapCollateralCollateralCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral Currency Pair
	 * 
	 * @param strCurrency1 Collateral Currency #1
	 * @param strCurrency2 Collateral Currency #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCollateralCorrSurface (
		final java.lang.String strCurrency1,
		final java.lang.String strCurrency2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCurrency1 || strCurrency1.isEmpty() || null == strCurrency2 || strCurrency2.isEmpty()
			|| null == auCorrelation)
			return false;

		_mapCollateralCollateralCorrelationSurface.put (strCurrency1 + "@#" + strCurrency2, auCorrelation);

		_mapCollateralCollateralCorrelationSurface.put (strCurrency2 + "@#" + strCurrency1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Pair of Credit Latent States
	 * 
	 * @param creditLabel1 The Credit Curve Latent State Label #1
	 * @param creditLabel2 The Credit Curve Latent State Label #2
	 * 
	 * @return The Correlation Surface between the Pair of Credit Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 creditCreditCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel1,
		final org.drip.state.identifier.CreditLabel creditLabel2)
	{
		if (null == creditLabel1 || null == creditLabel2) return null;

		java.lang.String strCode12 = creditLabel1.fullyQualifiedName() + "@#" +
			creditLabel2.fullyQualifiedName();

		if (_mapCreditCreditCorrelationSurface.containsKey (strCode12))
			return _mapCreditCreditCorrelationSurface.get (strCode12);

		java.lang.String strCode21 = creditLabel2.fullyQualifiedName() + "@#" +
			creditLabel1.fullyQualifiedName();

		return !_mapCreditCreditCorrelationSurface.containsKey (strCode21) ? null :
			_mapCreditCreditCorrelationSurface.get (strCode21);
	}

	/**
	 * (Re)-set the Correlation Surface between the Pair of Credit Latent States
	 * 
	 * @param creditLabel1 The Credit Curve Latent State Label #1
	 * @param creditLabel2 The Credit Curve Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCreditCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel1,
		final org.drip.state.identifier.CreditLabel creditLabel2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == creditLabel1 || null == creditLabel2 || null == auCorrelation) return false;

		java.lang.String strCreditLabel1 = creditLabel1.fullyQualifiedName();

		java.lang.String strCreditLabel2 = creditLabel2.fullyQualifiedName();

		_mapCreditCreditCorrelationSurface.put (strCreditLabel1 + "@#" + strCreditLabel2, auCorrelation);

		_mapCreditCreditCorrelationSurface.put (strCreditLabel2 + "@#" + strCreditLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric Latent State Pair
	 * 
	 * @param customMetricLabel1 The Custom Metric Latent State Label #1
	 * @param customMetricLabel2 The Custom Metric Latent State Label #2
	 * 
	 * @return The Correlation Surface between the Custom Metric Latent State Pair
	 */

	public org.drip.function.deterministic.R1ToR1 customMetricCustomMetricCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel1,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel2)
	{
		if (null == customMetricLabel1 || null == customMetricLabel2) return null;

		java.lang.String strCode12 = customMetricLabel1.fullyQualifiedName() + "@#" +
			customMetricLabel2.fullyQualifiedName();

		if (_mapCustomMetricCustomMetricCorrelationSurface.containsKey (strCode12))
			return _mapCustomMetricCustomMetricCorrelationSurface.get (strCode12);

		java.lang.String strCode21 = customMetricLabel2.fullyQualifiedName() + "@#" +
			customMetricLabel1.fullyQualifiedName();

		return _mapCustomMetricCustomMetricCorrelationSurface.containsKey (strCode21) ?
			_mapCustomMetricCustomMetricCorrelationSurface.get (strCode21) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric Latent State Pair
	 * 
	 * @param customMetricLabel1 The Custom Metric Latent State Label #1
	 * @param customMetricLabel2 The Custom Metric Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricCustomMetricCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel1,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == customMetricLabel1 || null == customMetricLabel2 || customMetricLabel1.match
			(customMetricLabel2) || null == auCorrelation)
			return false;

		_mapCustomMetricCustomMetricCorrelationSurface.put (customMetricLabel1.fullyQualifiedName() + "@#" +
			customMetricLabel2.fullyQualifiedName(), auCorrelation);

		_mapCustomMetricCustomMetricCorrelationSurface.put (customMetricLabel2.fullyQualifiedName() + "@#" +
			customMetricLabel1.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Pair of Equity Latent States
	 * 
	 * @param equityLabel1 Equity Curve Latent State Label #1
	 * @param equityLabel2 EquityCurve Latent State Label #2
	 * 
	 * @return The Correlation Surface between the Pair of Equity Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 equityEquityCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel1,
		final org.drip.state.identifier.EquityLabel equityLabel2)
	{
		if (null == equityLabel1 || null == equityLabel2) return null;

		java.lang.String strCode = equityLabel1.fullyQualifiedName() + "@#" +
			equityLabel2.fullyQualifiedName();

		return _mapEquityEquityCorrelationSurface.containsKey (strCode) ?
			_mapEquityEquityCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Pair of Equity Latent States
	 * 
	 * @param equityLabel1 EquityCurve Latent State Label #1
	 * @param equityLabel2 EquityCurve Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setEquityEquityCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel1,
		final org.drip.state.identifier.EquityLabel equityLabel2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == equityLabel1 || null == equityLabel2 || equityLabel1.match (equityLabel2) || null ==
			auCorrelation)
			return false;

		java.lang.String strEquityLabel1 = equityLabel1.fullyQualifiedName();

		java.lang.String strEquityLabel2 = equityLabel2.fullyQualifiedName();

		_mapEquityEquityCorrelationSurface.put (strEquityLabel1 + "@#" + strEquityLabel2, auCorrelation);

		_mapEquityEquityCorrelationSurface.put (strEquityLabel2 + "@#" + strEquityLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Pair of Forward Latent States
	 * 
	 * @param forwardLabel1 Forward Curve Latent State Label #1
	 * @param forwardLabel2 Forward Curve Latent State Label #2
	 * 
	 * @return The Correlation Surface between the Pair of Forward Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 forwardForwardCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel1,
		final org.drip.state.identifier.ForwardLabel forwardLabel2)
	{
		if (null == forwardLabel1 || null == forwardLabel2) return null;

		java.lang.String strCode = forwardLabel1.fullyQualifiedName() + "@#" +
			forwardLabel2.fullyQualifiedName();

		return _mapForwardForwardCorrelationSurface.containsKey (strCode) ?
			_mapForwardForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Pair of Forward Latent States
	 * 
	 * @param forwardLabel1 Forward Curve Latent State Label #1
	 * @param forwardLabel2 Forward Curve Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardForwardCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel1,
		final org.drip.state.identifier.ForwardLabel forwardLabel2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == forwardLabel1 || null == forwardLabel2 || forwardLabel1.match (forwardLabel2) || null ==
			auCorrelation)
			return false;

		java.lang.String strForwardLabel1 = forwardLabel1.fullyQualifiedName();

		java.lang.String strForwardLabel2 = forwardLabel2.fullyQualifiedName();

		_mapForwardForwardCorrelationSurface.put (strForwardLabel1 + "@#" + strForwardLabel2, auCorrelation);

		_mapForwardForwardCorrelationSurface.put (strForwardLabel2 + "@#" + strForwardLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Pair of Funding Latent States
	 * 
	 * @param fundingLabel1 Funding Latent State Label #1
	 * @param fundingLabel2 Funding Latent State Label #2
	 * 
	 * @return The Correlation Surface between the Pair of Funding Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 fundingFundingCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel1,
		final org.drip.state.identifier.FundingLabel fundingLabel2)
	{
		if (null == fundingLabel1 || null == fundingLabel2 || fundingLabel1.match (fundingLabel2))
			return null;

		java.lang.String strCode = fundingLabel1.fullyQualifiedName() + "@#" +
			fundingLabel2.fullyQualifiedName();

		return _mapFundingFundingCorrelationSurface.containsKey (strCode) ?
			_mapFundingFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Pair of Funding Latent States
	 * 
	 * @param fundingLabel1 Funding Latent State Label #1
	 * @param fundingLabel2 Funding Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingFundingCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel1,
		final org.drip.state.identifier.FundingLabel fundingLabel2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == fundingLabel1 || null == fundingLabel2 || fundingLabel1.match (fundingLabel2) || null ==
			auCorrelation)
			return false;

		java.lang.String strFundingLabel1 = fundingLabel1.fullyQualifiedName();

		java.lang.String strFundingLabel2 = fundingLabel2.fullyQualifiedName();

		_mapFundingFundingCorrelationSurface.put (strFundingLabel1 + "@#" + strFundingLabel2, auCorrelation);

		_mapFundingFundingCorrelationSurface.put (strFundingLabel2 + "@#" + strFundingLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FX Latent State Label Set
	 * 
	 * @param fxLabel1 The FX Latent State Label #1
	 * @param fxLabel2 The FX Latent State Label #2
	 * 
	 * @return The Correlation Surface for the specified FX Latent State Label Set
	 */

	public org.drip.function.deterministic.R1ToR1 fxFXCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel1,
		final org.drip.state.identifier.FXLabel fxLabel2)
	{
		if (null == fxLabel1 || null == fxLabel2 || fxLabel1.match (fxLabel2)) return null;

		java.lang.String strCode = fxLabel1.fullyQualifiedName() + "@#" + fxLabel2.fullyQualifiedName();

		return !_mapFXFXCorrelationSurface.containsKey (strCode) ? null : _mapFXFXCorrelationSurface.get
			(strCode);
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FX Latent State Label Set
	 * 
	 * @param fxLabel1 The FX Latent State Label #1
	 * @param fxLabel2 The FX Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXFXCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel1,
		final org.drip.state.identifier.FXLabel fxLabel2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == fxLabel1 || null == fxLabel2 || fxLabel1.match (fxLabel2) || null == auCorrelation)
			return false;

		java.lang.String strCode1 = fxLabel1.fullyQualifiedName();

		java.lang.String strCode2 = fxLabel2.fullyQualifiedName();

		_mapFXFXCorrelationSurface.put (strCode1 + "@#" + strCode2, auCorrelation);

		_mapFXFXCorrelationSurface.put (strCode2 + "@#" + strCode1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Govvie Latent State Pair
	 * 
	 * @param govvieLabel1 The Govvie Curve Latent State Label #1
	 * @param govvieLabel2 The Govvie Curve Latent State Label #2
	 * 
	 * @return The Correlation Surface for the specified Govvie Latent State Pair
	 */

	public org.drip.function.deterministic.R1ToR1 govvieGovvieCorrSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel1,
		final org.drip.state.identifier.GovvieLabel govvieLabel2)
	{
		if (null == govvieLabel1 || null == govvieLabel2 || govvieLabel1.match (govvieLabel2)) return null;

		java.lang.String strCode12 = govvieLabel1.fullyQualifiedName() + "@#" +
			govvieLabel2.fullyQualifiedName();

		if (_mapGovvieGovvieCorrelationSurface.containsKey (strCode12))
			return _mapGovvieGovvieCorrelationSurface.get (strCode12);

		java.lang.String strCode21 = govvieLabel2.fullyQualifiedName() + "@#" +
			govvieLabel1.fullyQualifiedName();

		return _mapGovvieGovvieCorrelationSurface.containsKey (strCode21) ?
			_mapGovvieGovvieCorrelationSurface.get (strCode21) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the Govvie Latent State Pair
	 * 
	 * @param govvieLabel1 The Govvie Curve Latent State Label #1
	 * @param govvieLabel2 The Govvie Curve Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovvieGovvieCorrSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel1,
		final org.drip.state.identifier.GovvieLabel govvieLabel2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == govvieLabel1 || null == govvieLabel2 || govvieLabel1.match (govvieLabel2) || null ==
			auCorrelation)
			return false;

		java.lang.String strGovvieLabel1 = govvieLabel1.fullyQualifiedName();

		java.lang.String strGovvieLabel2 = govvieLabel2.fullyQualifiedName();

		_mapGovvieGovvieCorrelationSurface.put (strGovvieLabel1 + "@#" + strGovvieLabel2, auCorrelation);

		_mapGovvieGovvieCorrelationSurface.put (strGovvieLabel2 + "@#" + strGovvieLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Pay-down Latent State Pair
	 * 
	 * @param paydownLabel1 The Pay-down Curve Latent State Label #1
	 * @param paydownLabel2 The Pay-down Curve Latent State Label #2
	 * 
	 * @return The Correlation Surface for the specified Pay-down Latent State Pair
	 */

	public org.drip.function.deterministic.R1ToR1 paydownPaydownCorrSurface (
		final org.drip.state.identifier.PaydownLabel paydownLabel1,
		final org.drip.state.identifier.PaydownLabel paydownLabel2)
	{
		if (null == paydownLabel1 || null == paydownLabel2 || paydownLabel1.match (paydownLabel2))
			return null;

		java.lang.String strCode12 = paydownLabel1.fullyQualifiedName() + "@#" +
			paydownLabel2.fullyQualifiedName();

		if (_mapPaydownPaydownCorrelationSurface.containsKey (strCode12))
			return _mapPaydownPaydownCorrelationSurface.get (strCode12);

		java.lang.String strCode21 = paydownLabel2.fullyQualifiedName() + "@#" +
			paydownLabel1.fullyQualifiedName();

		return _mapPaydownPaydownCorrelationSurface.containsKey (strCode21) ?
			_mapPaydownPaydownCorrelationSurface.get (strCode21) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the Pay-down Latent State Pair
	 * 
	 * @param paydownLabel1 The Pay-down Curve Latent State Label #1
	 * @param paydownLabel2 The Pay-down Curve Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setPaydownPaydownCorrSurface (
		final org.drip.state.identifier.PaydownLabel paydownLabel1,
		final org.drip.state.identifier.PaydownLabel paydownLabel2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == paydownLabel1 || null == paydownLabel2 || paydownLabel1.match (paydownLabel2) || null ==
			auCorrelation)
			return false;

		java.lang.String strPaydownLabel1 = paydownLabel1.fullyQualifiedName();

		java.lang.String strPaydownLabel2 = paydownLabel2.fullyQualifiedName();

		_mapPaydownPaydownCorrelationSurface.put (strPaydownLabel1 + "@#" + strPaydownLabel2, auCorrelation);

		_mapPaydownPaydownCorrelationSurface.put (strPaydownLabel2 + "@#" + strPaydownLabel1, auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Recovery Latent State Pair
	 * 
	 * @param recoveryLabel1 The Recovery Curve Latent State Label #1
	 * @param recoveryLabel2 The Recovery Curve Latent State Label #2
	 * 
	 * @return The Correlation Surface for the specified Recovery Latent State Pair
	 */

	public org.drip.function.deterministic.R1ToR1 recoveryRecoveryCorrSurface (
		final org.drip.state.identifier.RecoveryLabel recoveryLabel1,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel2)
	{
		if (null == recoveryLabel1 || null == recoveryLabel2 || recoveryLabel1.match (recoveryLabel2))
			return null;

		java.lang.String strCode12 = recoveryLabel1.fullyQualifiedName() + "@#" +
			recoveryLabel2.fullyQualifiedName();

		if (_mapRecoveryRecoveryCorrelationSurface.containsKey (strCode12))
			return _mapRecoveryRecoveryCorrelationSurface.get (strCode12);

		java.lang.String strCode21 = recoveryLabel2.fullyQualifiedName() + "@#" +
			recoveryLabel1.fullyQualifiedName();

		return _mapRecoveryRecoveryCorrelationSurface.containsKey (strCode21) ?
			_mapRecoveryRecoveryCorrelationSurface.get (strCode21) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the Recovery Latent State Pair
	 * 
	 * @param recoveryLabel1 The Recovery Curve Latent State Label #1
	 * @param recoveryLabel2 The Recovery Curve Latent State Label #2
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setRecoveryRecoveryCorrSurface (
		final org.drip.state.identifier.RecoveryLabel recoveryLabel1,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel2,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == recoveryLabel1 || null == recoveryLabel2 || recoveryLabel1.match (recoveryLabel2) || null
			== auCorrelation)
			return false;

		java.lang.String strRecoveryLabel1 = recoveryLabel1.fullyQualifiedName();

		java.lang.String strRecoveryLabel2 = recoveryLabel2.fullyQualifiedName();

		_mapRecoveryRecoveryCorrelationSurface.put (strRecoveryLabel1 + "@#" + strRecoveryLabel2,
			auCorrelation);

		_mapRecoveryRecoveryCorrelationSurface.put (strRecoveryLabel2 + "@#" + strRecoveryLabel1,
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Collateral and the Credit Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param creditLabel The Credit Curve Latent State Label
	 * 
	 * @return The Correlation Surface between the Collateral and the Credit Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 collateralCreditCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == creditLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + creditLabel.fullyQualifiedName();

		return _mapCollateralCreditCorrelationSurface.containsKey (strCode) ? null :
			_mapCollateralCreditCorrelationSurface.get (strCode);
	}

	/**
	 * (Re)-set the Correlation Surface between the Collateral and the Credit Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCreditCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == creditLabel)
			return false;

		_mapCollateralCreditCorrelationSurface.put (strCollateralCurrency + "@#" +
			creditLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Collateral and the Custom Metric Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * 
	 * @return The Correlation Surface between the Collateral and the Custom Metric Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 collateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == customMetricLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + customMetricLabel.fullyQualifiedName();

		return _mapCollateralCustomMetricCorrelationSurface.containsKey (strCode) ?
			_mapCollateralCustomMetricCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Collateral and the Custom Metric Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralCustomMetricCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == customMetricLabel)
			return false;

		_mapCollateralCustomMetricCorrelationSurface.put (strCollateralCurrency + "@#" +
			customMetricLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Collateral and the Forward Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param forwardLabel The Forward Latent State Label
	 * 
	 * @return The Correlation Surface between the Collateral and the Forward Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 collateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == forwardLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + forwardLabel.fullyQualifiedName();

		return _mapCollateralForwardCorrelationSurface.containsKey (strCode) ?
			_mapCollateralForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Collateral and the Equity Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param equityLabel The Equity Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralEquityCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == equityLabel || null
			== auCorrelation)
			return false;

		_mapCollateralEquityCorrelationSurface.put (strCollateralCurrency + "@#" +
			equityLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Collateral and the Equity Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param equityLabel The Equity Latent State Label
	 * 
	 * @return The Correlation Surface between the Collateral and the Equity Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 collateralEquityCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.EquityLabel equityLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == equityLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + equityLabel.fullyQualifiedName();

		return _mapCollateralEquityCorrelationSurface.containsKey (strCode) ?
			_mapCollateralEquityCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Collateral and the Forward Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param forwardLabel The Forward Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralForwardCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == forwardLabel || null
			== auCorrelation)
			return false;

		_mapCollateralForwardCorrelationSurface.put (strCollateralCurrency + "@#" +
			forwardLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Collateral and the Funding Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Correlation Surface between the Collateral and the Funding Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 collateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fundingLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + fundingLabel.fullyQualifiedName();

		return _mapCollateralFundingCorrelationSurface.containsKey (strCode) ?
			_mapCollateralFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Collateral and the Funding Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fundingLabel The Funding Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralFundingCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fundingLabel || null
			== auCorrelation)
			return false;

		_mapCollateralFundingCorrelationSurface.put (strCollateralCurrency + "@#" +
			fundingLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and the FX Latent State Label
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface for the specified Collateral and the FX Latent State Label
	 */

	public org.drip.function.deterministic.R1ToR1 collateralFXCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fxLabel) return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + fxLabel.fullyQualifiedName();

		return _mapCollateralFXCorrelationSurface.containsKey (strCode) ?
			_mapCollateralFXCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and FX Latent States
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralFXCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == fxLabel || null ==
			auCorrelation)
			return false;

		_mapCollateralFXCorrelationSurface.put (strCollateralCurrency + "@#" + fxLabel.fullyQualifiedName(),
			auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and Govvie Latent State Labels
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface for the specified Collateral and Govvie Latent State Labels
	 */

	public org.drip.function.deterministic.R1ToR1 collateralGovvieCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == govvieLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + govvieLabel.fullyQualifiedName();

		return _mapCollateralGovvieCorrelationSurface.containsKey (strCode) ?
			_mapCollateralGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and Govvie Latent State Labels
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralGovvieCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == govvieLabel || null
			== auCorrelation)
			return false;

		_mapCollateralGovvieCorrelationSurface.put (strCollateralCurrency + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and Pay-down Latent State Labels
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param paydownLabel The Pay-down Latent State Label
	 * 
	 * @return The Correlation Surface for the specified Collateral and Pay-down Latent State Labels
	 */

	public org.drip.function.deterministic.R1ToR1 collateralPaydownCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == paydownLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + paydownLabel.fullyQualifiedName();

		return _mapCollateralPaydownCorrelationSurface.containsKey (strCode) ?
			_mapCollateralPaydownCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and Pay-down Latent State Labels
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralPaydownCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == paydownLabel || null
			== auCorrelation)
			return false;

		_mapCollateralPaydownCorrelationSurface.put (strCollateralCurrency + "@#" +
			paydownLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Collateral and Recovery Latent State Labels
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Correlation Surface for the specified Collateral and Recovery Latent State Labels
	 */

	public org.drip.function.deterministic.R1ToR1 collateralRecoveryCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == recoveryLabel)
			return null;

		java.lang.String strCode = strCollateralCurrency + "@#" + recoveryLabel.fullyQualifiedName();

		return _mapCollateralRecoveryCorrelationSurface.containsKey (strCode) ?
			_mapCollateralRecoveryCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Collateral and Recovery Latent State Labels
	 * 
	 * @param strCollateralCurrency The Collateral Currency
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCollateralRecoveryCorrSurface (
		final java.lang.String strCollateralCurrency,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == strCollateralCurrency || strCollateralCurrency.isEmpty() || null == recoveryLabel || null
			== auCorrelation)
			return false;

		_mapCollateralRecoveryCorrelationSurface.put (strCollateralCurrency + "@#" +
			recoveryLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Custom Metric Latent States
	 * 
	 * @param creditLabel The Credit Latent State Label
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Custom Metric Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 creditCustomMetricCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel)
	{
		if (null == creditLabel || null == customMetricLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			customMetricLabel.fullyQualifiedName();

		return _mapCreditCustomMetricCorrelationSurface.containsKey (strCode) ?
			_mapCreditCustomMetricCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Custom Metric Latent States
	 * 
	 * @param creditLabel The Credit Latent State Label
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditCustomMetricCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == creditLabel || null == customMetricLabel || null == auCorrelation) return false;

		_mapCreditCustomMetricCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			customMetricLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Equity Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param equityLabel The Equity Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Equity Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 creditEquityCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.EquityLabel equityLabel)
	{
		if (null == creditLabel || null == equityLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			equityLabel.fullyQualifiedName();

		return _mapCreditEquityCorrelationSurface.containsKey (strCode) ?
			_mapCreditEquityCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Equity Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param equityLabel The Equity Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditEquityCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == creditLabel || null == equityLabel || null == auCorrelation) return false;

		_mapCreditEquityCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			equityLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Forward Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param forwardLabel The Forward Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Forward Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 creditForwardCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == creditLabel || null == forwardLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName();

		return _mapCreditForwardCorrelationSurface.containsKey (strCode) ?
			_mapCreditForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Forward Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param forwardLabel The Forward Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditForwardCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == creditLabel || null == forwardLabel || null == auCorrelation) return false;

		_mapCreditForwardCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Funding Latent States
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Funding Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 creditFundingCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == creditLabel || null == fundingLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName();

		return _mapCreditFundingCorrelationSurface.containsKey (strCode) ?
			_mapCreditFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Funding Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param fundingLabel The Funding Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditFundingCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == creditLabel || null == fundingLabel || null == auCorrelation) return false;

		_mapCreditFundingCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the FX Latent State Labels
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the FX Latent State Labels
	 */

	public org.drip.function.deterministic.R1ToR1 creditFXCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == creditLabel || null == fxLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" + fxLabel.fullyQualifiedName();

		return _mapCreditFXCorrelationSurface.containsKey (strCode) ? _mapCreditFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the FX Latent States
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditFXCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == creditLabel || null == fxLabel || null == auCorrelation) return false;

		_mapCreditFXCorrelationSurface.get (creditLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Govvie Latent State Labels
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Govvie Latent State Labels
	 */

	public org.drip.function.deterministic.R1ToR1 creditGovvieCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == creditLabel || null == govvieLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName();

		return _mapCreditGovvieCorrelationSurface.containsKey (strCode) ?
			_mapCreditGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Govvie Latent States
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditGovvieCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == creditLabel || null == govvieLabel || null == auCorrelation) return false;

		_mapCreditGovvieCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Pay-down Latent State Labels
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Pay-down Latent State Labels
	 */

	public org.drip.function.deterministic.R1ToR1 creditPaydownCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == creditLabel || null == paydownLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName();

		return _mapCreditPaydownCorrelationSurface.containsKey (strCode) ?
			_mapCreditPaydownCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Pay-down Latent States
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditPaydownCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == creditLabel || null == paydownLabel || null == auCorrelation) return false;

		_mapCreditPaydownCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Credit and the Recovery Latent State Labels
	 * 
	 * @param creditLabel The Credit Curve Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Correlation Surface between the Credit and the Recovery Latent State Labels
	 */

	public org.drip.function.deterministic.R1ToR1 creditRecoveryCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == creditLabel || null == recoveryLabel) return null;

		java.lang.String strCode = creditLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName();

		return _mapCreditRecoveryCorrelationSurface.containsKey (strCode) ?
			_mapCreditRecoveryCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Credit and the Recovery Latent States
	 * 
	 * @param creditLabel The Credit Curve Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCreditRecoveryCorrSurface (
		final org.drip.state.identifier.CreditLabel creditLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == creditLabel || null == recoveryLabel || null == auCorrelation) return false;

		_mapCreditRecoveryCorrelationSurface.put (creditLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric and the Equity Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param equityLabel The Equity Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the Equity Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 customMetricEquityCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.EquityLabel equityLabel)
	{
		if (null == customMetricLabel || null == equityLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			equityLabel.fullyQualifiedName();

		return _mapCustomMetricEquityCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricEquityCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the Equity Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Label
	 * @param equityLabel The Equity Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricEquityCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == customMetricLabel || null == equityLabel || null == auCorrelation) return false;

		_mapCustomMetricEquityCorrelationSurface.put (customMetricLabel.fullyQualifiedName() + "@#" +
			equityLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric and the Forward Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param forwardLabel The Forward Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the Forward Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 customMetricForwardCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == customMetricLabel || null == forwardLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName();

		return _mapCustomMetricForwardCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the Forward Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Label
	 * @param forwardLabel The Forward Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricForwardCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == customMetricLabel || null == forwardLabel || null == auCorrelation) return false;

		_mapCustomMetricForwardCorrelationSurface.put (customMetricLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between Custom Metric and the Funding Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the Funding Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 customMetricFundingCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == customMetricLabel || null == fundingLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName();

		return _mapCustomMetricFundingCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the Funding Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricFundingCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == customMetricLabel || null == fundingLabel) return false;

		_mapCustomMetricFundingCorrelationSurface.put (customMetricLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric and the FX Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the FX Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 customMetricFXCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == customMetricLabel || null == fxLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName();

		return _mapCustomMetricFXCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricFXCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the FX Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricFXCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == customMetricLabel || null == fxLabel || null == auCorrelation) return false;

		_mapCustomMetricFXCorrelationSurface.get (customMetricLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric and the Govvie Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the Govvie Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 customMetricGovvieCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == customMetricLabel || null == govvieLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName();

		return _mapCustomMetricGovvieCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the Govvie Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricGovvieCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == customMetricLabel || null == govvieLabel) return false;

		_mapCustomMetricGovvieCorrelationSurface.put (customMetricLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric and the Pay-down Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the Pay-down Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 customMetricPaydownCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == customMetricLabel || null == paydownLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName();

		return _mapCustomMetricPaydownCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricPaydownCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the Pay-down Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricPaydownCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == customMetricLabel || null == paydownLabel) return false;

		_mapCustomMetricPaydownCorrelationSurface.put (customMetricLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Custom Metric and the Recovery Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Correlation Surface between the Custom Metric and the Recovery Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 customMetricRecoveryCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == customMetricLabel || null == recoveryLabel) return null;

		java.lang.String strCode = customMetricLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName();

		return _mapCustomMetricRecoveryCorrelationSurface.containsKey (strCode) ?
			_mapCustomMetricRecoveryCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Custom Metric and the Recovery Latent States
	 * 
	 * @param customMetricLabel The Custom Metric Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setCustomMetricRecoveryCorrSurface (
		final org.drip.state.identifier.CustomMetricLabel customMetricLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == customMetricLabel || null == recoveryLabel) return false;

		_mapCustomMetricRecoveryCorrelationSurface.put (customMetricLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Equity and the Forward Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param forwardLabel The Forward Latent State Label
	 * 
	 * @return The Correlation Surface between the Equity and the Forward Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 equityForwardCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel)
	{
		if (null == equityLabel || null == forwardLabel) return null;

		java.lang.String strCode = equityLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName();

		return _mapEquityForwardCorrelationSurface.containsKey (strCode) ?
			_mapEquityForwardCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Equity and the Forward Latent States
	 * 
	 * @param equityLabel The Equity Label
	 * @param forwardLabel The Forward Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setEquityForwardCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == equityLabel || null == forwardLabel || null == auCorrelation) return false;

		_mapEquityForwardCorrelationSurface.put (equityLabel.fullyQualifiedName() + "@#" +
			forwardLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between Equity and the Funding Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Correlation Surface between the Equity and the Funding Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 equityFundingCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == equityLabel || null == fundingLabel) return null;

		java.lang.String strCode = equityLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName();

		return _mapEquityFundingCorrelationSurface.containsKey (strCode) ?
			_mapEquityFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Equity and the Funding Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setEquityFundingCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == equityLabel || null == fundingLabel) return false;

		_mapEquityFundingCorrelationSurface.put (equityLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Equity and the FX Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface between the Equity and the FX Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 equityFXCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == equityLabel || null == fxLabel) return null;

		java.lang.String strCode = equityLabel.fullyQualifiedName() + "@#" + fxLabel.fullyQualifiedName();

		return _mapEquityFXCorrelationSurface.containsKey (strCode) ? _mapEquityFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Equity and the FX Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setEquityFXCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == equityLabel || null == fxLabel || null == auCorrelation) return false;

		_mapEquityFXCorrelationSurface.get (equityLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Equity and the Govvie Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface between the Equity and the Govvie Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 equityGovvieCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == equityLabel || null == govvieLabel) return null;

		java.lang.String strCode = equityLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName();

		return _mapEquityGovvieCorrelationSurface.containsKey (strCode) ?
			_mapEquityGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Equity and the Govvie Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setEquityGovvieCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == equityLabel || null == govvieLabel) return false;

		_mapEquityGovvieCorrelationSurface.put (equityLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Equity and the Pay-down Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * 
	 * @return The Correlation Surface between the Equity and the Pay-down Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 equityPaydownCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == equityLabel || null == paydownLabel) return null;

		java.lang.String strCode = equityLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName();

		return _mapEquityPaydownCorrelationSurface.containsKey (strCode) ?
			_mapEquityPaydownCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Equity and the Pay-down Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setEquityPaydownCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == equityLabel || null == paydownLabel) return false;

		_mapEquityPaydownCorrelationSurface.put (equityLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Equity and the Recovery Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Correlation Surface between the Equity and the Recovery Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 equityRecoveryCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == equityLabel || null == recoveryLabel) return null;

		java.lang.String strCode = equityLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName();

		return _mapEquityRecoveryCorrelationSurface.containsKey (strCode) ?
			_mapEquityRecoveryCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Equity and the Recovery Latent States
	 * 
	 * @param equityLabel The Equity Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setEquityRecoveryCorrSurface (
		final org.drip.state.identifier.EquityLabel equityLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == equityLabel || null == recoveryLabel) return false;

		_mapEquityRecoveryCorrelationSurface.put (equityLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Forward and the Funding Latent States
	 * 
	 * @param forwardLabel The Forward Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * 
	 * @return The Correlation Surface between the Forward and the Funding Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 forwardFundingCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel)
	{
		if (null == forwardLabel || null == fundingLabel) return null;

		java.lang.String strCode = forwardLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName();

		return _mapForwardFundingCorrelationSurface.containsKey (strCode) ?
			_mapForwardFundingCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Forward and the Funding Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param fundingLabel The Funding Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardFundingCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == forwardLabel || null == fundingLabel || null == auCorrelation) return false;

		_mapForwardFundingCorrelationSurface.put (forwardLabel.fullyQualifiedName() + "@#" +
			fundingLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Forward and the FX Latent State Labels
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface between the Forward and the FX Latent State Labels
	 */

	public org.drip.function.deterministic.R1ToR1 forwardFXCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == forwardLabel || null == fxLabel) return null;

		java.lang.String strCode = forwardLabel.fullyQualifiedName() + "@#" + fxLabel.fullyQualifiedName();

		return _mapForwardFXCorrelationSurface.containsKey (strCode) ? _mapForwardFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Forward and the FX Latent State Labels
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardFXCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == forwardLabel || null == fxLabel || null == auCorrelation) return false;

		_mapForwardFXCorrelationSurface.get (forwardLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName());

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Forward and the Govvie Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface between the Forward and the Govvie Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 forwardGovvieCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == forwardLabel || null == govvieLabel) return null;

		java.lang.String strCode = forwardLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName();

		return _mapForwardGovvieCorrelationSurface.containsKey (strCode) ?
			_mapForwardGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Forward and the Govvie Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardGovvieCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == forwardLabel || null == govvieLabel || null == auCorrelation) return false;

		_mapForwardGovvieCorrelationSurface.put (forwardLabel.fullyQualifiedName() + "@#" + 
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Forward and the Pay-down Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * 
	 * @return The Correlation Surface between the Forward and the Pay-down Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 forwardPaydownCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == forwardLabel || null == paydownLabel) return null;

		java.lang.String strCode = forwardLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName();

		return _mapForwardPaydownCorrelationSurface.containsKey (strCode) ?
			_mapForwardPaydownCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Forward and the Pay-down Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardPaydownCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == forwardLabel || null == paydownLabel || null == auCorrelation) return false;

		_mapForwardPaydownCorrelationSurface.put (forwardLabel.fullyQualifiedName() + "@#" + 
			paydownLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Forward and the Recovery Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Correlation Surface between the Forward and the Recovery Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 forwardRecoveryCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == forwardLabel || null == recoveryLabel) return null;

		java.lang.String strCode = forwardLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName();

		return _mapForwardRecoveryCorrelationSurface.containsKey (strCode) ?
			_mapForwardRecoveryCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Forward and the Recovery Latent States
	 * 
	 * @param forwardLabel The Forward Curve Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setForwardRecoveryCorrSurface (
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == forwardLabel || null == recoveryLabel || null == auCorrelation) return false;

		_mapForwardRecoveryCorrelationSurface.put (forwardLabel.fullyQualifiedName() + "@#" + 
			recoveryLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Funding and the FX Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * 
	 * @return The Correlation Surface between the Funding and the FX Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 fundingFXCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.FXLabel fxLabel)
	{
		if (null == fundingLabel || null == fxLabel) return null;

		java.lang.String strCode = fundingLabel.fullyQualifiedName() + "@#" + fxLabel.fullyQualifiedName();

		return _mapFundingFXCorrelationSurface.containsKey (strCode) ? _mapFundingFXCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Funding and the FX Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param fxLabel The FX Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingFXCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == fundingLabel || null == fxLabel || null == auCorrelation) return false;

		_mapFundingFXCorrelationSurface.put (fundingLabel.fullyQualifiedName() + "@#" +
			fxLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Funding and the Govvie Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface between the Funding and the Govvie Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 fundingGovvieCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == fundingLabel || null == govvieLabel) return null;

		java.lang.String strCode = fundingLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName();

		return _mapFundingGovvieCorrelationSurface.containsKey (strCode) ?
			_mapFundingGovvieCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Funding and the Govvie Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingGovvieCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == fundingLabel || null == govvieLabel || null == auCorrelation) return false;

		_mapFundingGovvieCorrelationSurface.put (fundingLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Funding and the Pay-down Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * 
	 * @return The Correlation Surface between the Funding and the Pay-down Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 fundingPaydownCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == fundingLabel || null == paydownLabel) return null;

		java.lang.String strCode = fundingLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName();

		return _mapFundingPaydownCorrelationSurface.containsKey (strCode) ?
			_mapFundingPaydownCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Funding and the Pay-down Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingPaydownCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == fundingLabel || null == paydownLabel || null == auCorrelation) return false;

		_mapFundingPaydownCorrelationSurface.put (fundingLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface between the Funding and the Recovery Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Correlation Surface between the Funding and the Recovery Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 fundingRecoveryCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == fundingLabel || null == recoveryLabel) return null;

		java.lang.String strCode = fundingLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName();

		return _mapFundingRecoveryCorrelationSurface.containsKey (strCode) ?
			_mapFundingRecoveryCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface between the Funding and the Recovery Latent States
	 * 
	 * @param fundingLabel The Funding Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFundingRecoveryCorrSurface (
		final org.drip.state.identifier.FundingLabel fundingLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == fundingLabel || null == recoveryLabel || null == auCorrelation) return false;

		_mapFundingRecoveryCorrelationSurface.put (fundingLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FX and the Govvie Latent States
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * 
	 * @return The Correlation Surface for the specified FX and the Govvie Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 fxGovvieCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel)
	{
		if (null == fxLabel || null == govvieLabel) return null;

		java.lang.String strCode = fxLabel.fullyQualifiedName() + "@#" + govvieLabel.fullyQualifiedName();

		return _mapFXGovvieCorrelationSurface.containsKey (strCode) ? _mapFXGovvieCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FX and the Govvie Latent States
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param govvieLabel The Govvie Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXGovvieCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == fxLabel || null == govvieLabel || null == auCorrelation) return false;

		_mapFXGovvieCorrelationSurface.put (fxLabel.fullyQualifiedName() + "@#" +
			govvieLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FX and the Pay-down Latent States
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * 
	 * @return The Correlation Surface for the specified FX and the Pay-down Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 fxPaydownCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == fxLabel || null == paydownLabel) return null;

		java.lang.String strCode = fxLabel.fullyQualifiedName() + "@#" + paydownLabel.fullyQualifiedName();

		return _mapFXPaydownCorrelationSurface.containsKey (strCode) ? _mapFXPaydownCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FX and the Pay-down Latent States
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXPaydownCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == fxLabel || null == paydownLabel || null == auCorrelation) return false;

		_mapFXPaydownCorrelationSurface.put (fxLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified FX and the Recovery Latent States
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Correlation Surface for the specified FX and the Recovery Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 fxRecoveryCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == fxLabel || null == recoveryLabel) return null;

		java.lang.String strCode = fxLabel.fullyQualifiedName() + "@#" + recoveryLabel.fullyQualifiedName();

		return _mapFXRecoveryCorrelationSurface.containsKey (strCode) ? _mapFXRecoveryCorrelationSurface.get
			(strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified FX and the Recovery Latent States
	 * 
	 * @param fxLabel The FX Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setFXRecoveryCorrSurface (
		final org.drip.state.identifier.FXLabel fxLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == fxLabel || null == recoveryLabel || null == auCorrelation) return false;

		_mapFXRecoveryCorrelationSurface.put (fxLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Govvie and the Pay-down Latent States
	 * 
	 * @param govvieLabel The Govvie Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * 
	 * @return The Correlation Surface for the specified Govvie and the Pay-down Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 govviePaydownCorrSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel)
	{
		if (null == govvieLabel || null == paydownLabel) return null;

		java.lang.String strCode = govvieLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName();

		return _mapGovviePaydownCorrelationSurface.containsKey (strCode) ?
			_mapGovviePaydownCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Govvie and the Pay-down Latent States
	 * 
	 * @param govvieLabel The Govvie Latent State Label
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovviePaydownCorrSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == govvieLabel || null == paydownLabel || null == auCorrelation) return false;

		_mapGovviePaydownCorrelationSurface.put (govvieLabel.fullyQualifiedName() + "@#" +
			paydownLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Govvie and the Recovery Latent States
	 * 
	 * @param govvieLabel The Govvie Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Correlation Surface for the specified Govvie and the Recovery Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 govvieRecoveryCorrSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == govvieLabel || null == recoveryLabel) return null;

		java.lang.String strCode = govvieLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName();

		return _mapGovvieRecoveryCorrelationSurface.containsKey (strCode) ?
			_mapGovvieRecoveryCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Govvie and the Recovery Latent States
	 * 
	 * @param govvieLabel The Govvie Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setGovvieRecoveryCorrSurface (
		final org.drip.state.identifier.GovvieLabel govvieLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == govvieLabel || null == recoveryLabel || null == auCorrelation) return false;

		_mapGovvieRecoveryCorrelationSurface.put (govvieLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Correlation Surface for the specified Pay-down and the Recovery Latent States
	 * 
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * 
	 * @return The Correlation Surface for the specified Pay-down and the Recovery Latent States
	 */

	public org.drip.function.deterministic.R1ToR1 paydownRecoveryCorrSurface (
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel)
	{
		if (null == paydownLabel || null == recoveryLabel) return null;

		java.lang.String strCode = paydownLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName();

		return _mapPaydownRecoveryCorrelationSurface.containsKey (strCode) ?
			_mapPaydownRecoveryCorrelationSurface.get (strCode) : null;
	}

	/**
	 * (Re)-set the Correlation Surface for the specified Pay-down and the Recovery Latent States
	 * 
	 * @param paydownLabel The Pay-down Latent State Label
	 * @param recoveryLabel The Recovery Latent State Label
	 * @param auCorrelation The Correlation Surface
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setPaydownRecoveryCorrSurface (
		final org.drip.state.identifier.PaydownLabel paydownLabel,
		final org.drip.state.identifier.RecoveryLabel recoveryLabel,
		final org.drip.function.deterministic.R1ToR1 auCorrelation)
	{
		if (null == paydownLabel || null == recoveryLabel || null == auCorrelation) return false;

		_mapPaydownRecoveryCorrelationSurface.put (paydownLabel.fullyQualifiedName() + "@#" +
			recoveryLabel.fullyQualifiedName(), auCorrelation);

		return true;
	}

	/**
	 * Retrieve the Product Quote
	 * 
	 * @param strProductCode Product Code
	 * 
	 * @return Product Quote
	 */

	public org.drip.param.definition.ProductQuote productQuote (
		final java.lang.String strProductCode)
	{
		if (null == strProductCode || strProductCode.isEmpty() || !_mapProductQuote.containsKey
			(strProductCode))
			return null;

		return _mapProductQuote.get (strProductCode);
	}

	/**
	 * (Re)-set the Product Quote
	 * 
	 * @param strProductCode Product Code
	 * @param pq Product Quote
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setProductQuote (
		final java.lang.String strProductCode,
		final org.drip.param.definition.ProductQuote pq)
	{
		if (null == strProductCode || strProductCode.isEmpty() || null == pq) return false;

		_mapProductQuote.put (strProductCode, pq);

		return true;
	}

	/**
	 * Retrieve the Full Set of Quotes
	 * 
	 * @return The Full Set of Quotes
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			quoteMap()
	{
		return _mapProductQuote;
	}

	/**
	 * (Re)-set the Map of Quote
	 * 
	 * @param mapQuote Map of Quotes
	 * 
	 * @return TRUE => Successfully set
	 */

	public boolean setQuoteMap (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ProductQuote>
			mapQuote)
	{
		if (null == mapQuote || 0 == mapQuote.size()) return false;

		for (java.util.Map.Entry<java.lang.String, org.drip.param.definition.ProductQuote> meCQ :
			mapQuote.entrySet()) {
			if (null == meCQ) continue;

			java.lang.String strKey = meCQ.getKey();

			org.drip.param.definition.ProductQuote cq = meCQ.getValue();

			if (null == strKey || strKey.isEmpty() || null == cq) continue;

			_mapProductQuote.put (strKey, cq);
		}

		return true;
	}

	/**
	 * Set the Fixing corresponding to the Date/Label Pair
	 * 
	 * @param dt The Fixing Date
	 * @param lsl The Fixing Label
	 * @param dblFixing The Fixing Amount
	 * 
	 * @return TRUE => Entry successfully added
	 */

	public boolean setFixing (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.state.identifier.LatentStateLabel lsl,
		final double dblFixing)
	{
		return _lsfc.add (dt, lsl, dblFixing);
	}

	/**
	 * Set the Fixing corresponding to the Date/Label Pair
	 * 
	 * @param dblDate The Fixing Date
	 * @param lsl The Fixing Label
	 * @param dblFixing The Fixing Amount
	 * 
	 * @return TRUE => Entry successfully added
	 */

	public boolean setFixing (
		final double dblDate,
		final org.drip.state.identifier.LatentStateLabel lsl,
		final double dblFixing)
	{
		return _lsfc.add (dblDate, lsl, dblFixing);
	}

	/**
	 * Remove the Fixing corresponding to the Date/Label Pair it if exists
	 * 
	 * @param dt The Fixing Date
	 * @param lsl The Fixing Label
	 * 
	 * @return TRUE => Entry successfully removed if it existed
	 */

	public boolean removeFixing (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		return _lsfc.remove (dt, lsl);
	}

	/**
	 * Remove the Fixing corresponding to the Date/Label Pair it if exists
	 * 
	 * @param dblDate The Fixing Date
	 * @param lsl The Fixing Label
	 * 
	 * @return TRUE => Entry successfully removed if it existed
	 */

	public boolean removeFixing (
		final double dblDate,
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		return _lsfc.remove (dblDate, lsl);
	}

	/**
	 * Retrieve the Fixing for the Specified Date/LSL Combination
	 * 
	 * @param dt Date
	 * @param lsl The Latent State Label
	 * 
	 * @return The Fixing for the Specified Date/LSL Combination
	 * 
	 * @throws java.lang.Exception Thrown if the Fixing cannot be found
	 */

	public double fixing (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.state.identifier.LatentStateLabel lsl)
		throws java.lang.Exception
	{
		return _lsfc.fixing (dt, lsl);
	}

	/**
	 * Retrieve the Fixing for the Specified Date/LSL Combination
	 * 
	 * @param dblDate Date
	 * @param lsl The Latent State Label
	 * 
	 * @return The Fixing for the Specified Date/LSL Combination
	 * 
	 * @throws java.lang.Exception Thrown if the Fixing cannot be found
	 */

	public double fixing (
		final double dblDate,
		final org.drip.state.identifier.LatentStateLabel lsl)
		throws java.lang.Exception
	{
		return _lsfc.fixing (dblDate, lsl);
	}

	/**
	 * Indicates the Availability of the Fixing for the Specified LSL Label on the specified Date
	 * 
	 * @param dt The Date
	 * @param lsl The Label
	 * 
	 * @return TRUE => The Fixing for the Specified LSL Label on the specified Date 
	 */

	public boolean available (
		final org.drip.analytics.date.JulianDate dt,
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		return _lsfc.available (dt, lsl);
	}

	/**
	 * Indicates the Availability of the Fixing for the Specified LSL Label on the specified Date
	 * 
	 * @param dblDate The Date
	 * @param lsl The Label
	 * 
	 * @return TRUE => The Fixing for the Specified LSL Label on the specified Date 
	 */

	public boolean available (
		final double dblDate,
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		return _lsfc.available (dblDate, lsl);
	}

	/**
	 * Retrieve the Latent State Fixings
	 * 
	 * @return The Latent State Fixings
	 */

	public org.drip.param.market.LatentStateFixingsContainer fixings()
	{
		return _lsfc;
	}

	/**
	 * Set the Latent State Fixings Container Instance
	 * 
	 * @param lsfc The Latent State Fixings Container Instance
	 * 
	 * @return The Latent State Fixings Container Instance successfully set
	 */

	public boolean setFixings (
		final org.drip.param.market.LatentStateFixingsContainer lsfc)
	{
		_lsfc = lsfc;
		return true;
	}
}

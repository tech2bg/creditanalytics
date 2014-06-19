
package org.drip.product.fx;

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
 * DomesticCollateralizedForeignForward contains the Domestic Currency Collateralized Foreign Payout FX
 * 	forward product contract details.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class DomesticCollateralizedForeignForward {
	private java.lang.String _strCode = "";
	private double _dblMaturity = java.lang.Double.NaN;
	private org.drip.product.params.CurrencyPair _ccyPair = null;
	private double _dblForexForwardStrike = java.lang.Double.NaN;

	/**
	 * Create an DomesticCollateralizedForeignForward from the currency pair, the strike, and the maturity
	 * 	dates
	 * 
	 * @param ccyPair Currency Pair
	 * @param dblForexForwardStrike Forex Forward Strike
	 * @param dtMaturity Maturity Date
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public DomesticCollateralizedForeignForward (
		final org.drip.product.params.CurrencyPair ccyPair,
		final double dblForexForwardStrike,
		final org.drip.analytics.date.JulianDate dtMaturity)
		throws java.lang.Exception
	{
		if (null == (_ccyPair = ccyPair) || !org.drip.quant.common.NumberUtil.IsValid (_dblForexForwardStrike
			= dblForexForwardStrike) || null == dtMaturity)
			throw new java.lang.Exception ("DomesticCollateralizedForeignForward ctr: Invalid Inputs");

		_dblMaturity = dtMaturity.getJulian();
	}

	public java.lang.String getPrimaryCode()
	{
		return _strCode;
	}

	public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	public java.lang.String[] getSecondaryCode()
	{
		java.lang.String strPrimaryCode = getPrimaryCode();

		int iNumTokens = 0;
		java.lang.String astrCodeTokens[] = new java.lang.String[2];

		java.util.StringTokenizer stCodeTokens = new java.util.StringTokenizer (strPrimaryCode, ".");

		while (stCodeTokens.hasMoreTokens())
			astrCodeTokens[iNumTokens++] = stCodeTokens.nextToken();

		System.out.println (astrCodeTokens[0]);

		return new java.lang.String[] {astrCodeTokens[0]};
	}

	public org.drip.analytics.date.JulianDate getMaturityDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public org.drip.product.params.CurrencyPair getCcyPair()
	{
		return _ccyPair;
	}

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == mktParams) return null;

		long lStart = System.nanoTime();

		double dblValueDate = valParams.valueDate();

		if (dblValueDate > _dblMaturity) return null;

		org.drip.quant.function1D.AbstractUnivariate auFX = mktParams.fxCurve (_ccyPair);

		if (null == auFX) return null;

		java.lang.String strDomesticCurrency = _ccyPair.denomCcy();

		org.drip.analytics.rates.DiscountCurve dcDomesticCollateral =
			mktParams.payCurrencyCollateralCurrencyCurve (strDomesticCurrency, strDomesticCurrency);

		if (null == dcDomesticCollateral) return null;

		java.lang.String strForeignCurrency = _ccyPair.numCcy();

		org.drip.analytics.rates.DiscountCurve dcForeignCurrencyDomesticCollateral =
			mktParams.payCurrencyCollateralCurrencyCurve (strForeignCurrency, strDomesticCurrency);

		if (null == dcForeignCurrencyDomesticCollateral) return null;

		double dblPrice = java.lang.Double.NaN;
		double dblSpotFX = java.lang.Double.NaN;
		double dblParForward = java.lang.Double.NaN;
		double dblDomesticCollateralDF = java.lang.Double.NaN;
		double dblForeignCurrencyDomesticCollateralDF = java.lang.Double.NaN;

		try {
			dblPrice = (dblForeignCurrencyDomesticCollateralDF = dcForeignCurrencyDomesticCollateral.df
				(_dblMaturity)) - (_dblForexForwardStrike * (dblDomesticCollateralDF =
					dcDomesticCollateral.df (_dblMaturity)) / (dblSpotFX = auFX.evaluate (dblValueDate)));

			dblParForward = dblSpotFX * dblForeignCurrencyDomesticCollateralDF / dblDomesticCollateralDF;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		mapResult.put ("DomesticCollateralDF", dblDomesticCollateralDF);

		mapResult.put ("ForeignCurrencyDomesticCollateralDF", dblForeignCurrencyDomesticCollateralDF);

		mapResult.put ("ParForward", dblParForward);

		mapResult.put ("Price", dblPrice);

		mapResult.put ("SpotFX", dblSpotFX);

		return mapResult;
	}

	public java.util.Set<java.lang.String> getMeasureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("DomesticCurrencyForeignCollateralDF");

		setstrMeasureNames.add ("ForeignCollateralDF");

		setstrMeasureNames.add ("ParForward");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("SpotFX");

		return setstrMeasureNames;
	}
}

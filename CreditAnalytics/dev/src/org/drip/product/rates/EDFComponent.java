
package org.drip.product.rates;

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
 * EDFComponent contains the implementation of the Euro-dollar future contract/valuation (EDF). It exports
 *  the following functionality:
 *  - Standard/Custom Constructor for the EDFComponent
 *  - Dates: Effective, Maturity, Coupon dates and Product settlement Parameters
 *  - Coupon/Notional Outstanding as well as schedules
 *  - Market Parameters: Discount, Forward, Credit, Treasury Curves
 *  - Cash Flow Periods: Coupon flows and (Optionally) Loss Flows
 *  - Valuation: Named Measure Generation
 *  - Calibration: The codes and constraints generation
 *  - Jacobians: Quote/DF and PV/DF micro-Jacobian generation
 *  - Serialization into and de-serialization out of byte arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class EDFComponent extends org.drip.product.definition.RatesComponent {
	private double _dblNotional = 100.;
	private java.lang.String _strEDCode = "";
	private java.lang.String _strCurrency = "";
	private java.lang.String _strDC = "Act/360";
	private java.lang.String _strCalendar = "USD";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * Construct an EDFComponent Instance
	 * 
	 * @param dtEffective Effective Date
	 * @param dtMaturity Maturity Date
	 * @param strCurrency The Currency
	 * @param strDC Day Count
	 * @param strCalendar Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public EDFComponent (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final java.lang.String strCurrency,
		final java.lang.String strDC,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == dtEffective || null == dtMaturity || null == (_strCurrency = strCurrency) ||
			_strCurrency.isEmpty() || (_dblMaturity = dtMaturity.getJulian()) <= (_dblEffective =
				dtEffective.getJulian()))
			throw new java.lang.Exception ("EDFComponent ctr:: Invalid Params!");

		_strDC = strDC;
		_strCalendar = strCalendar;

		_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();
	}

	/**
	 * Construct an EDFComponent Component
	 * 
	 * @param strFullEDCode EDF Component Code
	 * @param dt Start Date
	 * @param strCurrency EDF Currency
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public EDFComponent (
		final java.lang.String strFullEDCode,
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		if (null == dt || null == (_strCurrency = strCurrency) || _strCurrency.isEmpty())
			throw new java.lang.Exception ("EDFComponent ctr:: Invalid Params!");

		java.lang.String strEDCode = strFullEDCode;

		if (4 != strEDCode.length() || !strEDCode.toUpperCase().startsWith ("ED"))
			 throw new java.lang.Exception ("EDFComponent ctr:: Unknown EDF Code " + strEDCode);

		_strCalendar = _strCurrency;
		int iEffectiveMonth = -1;

		int iYearDigit = new java.lang.Integer ("" + strEDCode.charAt (3)).intValue();

		if (10 <= iYearDigit)
			throw new java.lang.Exception ("EDFComponent ctr:: Invalid ED year in " + strEDCode);

		char chMonth = strEDCode.charAt (2);

		if ('H' == chMonth)
			 iEffectiveMonth = org.drip.analytics.date.JulianDate.MARCH;
		else if ('M' == chMonth)
			 iEffectiveMonth = org.drip.analytics.date.JulianDate.JUNE;
		else if ('U' == chMonth)
			iEffectiveMonth = org.drip.analytics.date.JulianDate.SEPTEMBER;
		else if ('Z' == chMonth)
			 iEffectiveMonth = org.drip.analytics.date.JulianDate.DECEMBER;
		else
			 throw new java.lang.Exception ("EDFComponent ctr:: Unknown Month in " + strEDCode);

		org.drip.analytics.date.JulianDate dtEDEffective = dt.getFirstEDFStartDate (3);

		while (iYearDigit != (org.drip.analytics.date.JulianDate.Year (dtEDEffective.getJulian()) % 10))
			 dtEDEffective = dtEDEffective.addYears (1);

		org.drip.analytics.date.JulianDate dtEffective = org.drip.analytics.date.JulianDate.CreateFromYMD
			 (org.drip.analytics.date.JulianDate.Year (dtEDEffective.getJulian()), iEffectiveMonth, 15);

		_dblEffective = dtEffective.getJulian();

		_dblMaturity = dtEffective.addMonths (3).getJulian();

		_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();
	}

	/**
	 * EDFComponent de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if EDFComponent cannot be properly de-serialized
	 */

	public EDFComponent (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("EDFComponent de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("EDFComponent de-serializer: Empty state");

		java.lang.String strSerializedEDFuture = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedEDFuture || strSerializedEDFuture.isEmpty())
			throw new java.lang.Exception ("EDFComponent de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedEDFuture,
			fieldDelimiter());

		if (null == astrField || 7 > astrField.length)
			throw new java.lang.Exception ("EDFComponent de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("EDFComponent de-serializer: Cannot locate notional");

		_dblNotional = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("EDFComponent de-serializer: Cannot locate EDF Pay Currency");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_strCurrency = astrField[2];
		else
			_strCurrency = "";

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("EDFComponent de-serializer: Cannot locate EDF code");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strEDCode = astrField[3];
		else
			_strEDCode = "";

		if (null == astrField[4] || astrField[4].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			throw new java.lang.Exception ("EDFComponent de-serializer: Cannot locate maturity date");

		_dblMaturity = new java.lang.Double (astrField[4]);

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception ("EDFComponent de-serializer: Cannot locate effective date");

		_dblEffective = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty())
			throw new java.lang.Exception ("EDFComponent de-serializer: Cannot locate notional schedule");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			_notlSchedule = null;
		else
			_notlSchedule = new org.drip.product.params.FactorSchedule (astrField[6].getBytes());

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception ("EDFComponent de-serializer: Cannot locate cash settle params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			_settleParams = null;
		else
			_settleParams = new org.drip.param.valuation.CashSettleParams (astrField[7].getBytes());
	}

	@Override public java.lang.String primaryCode()
	{
		return _strEDCode + "." + _strCurrency;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strEDCode = strCode;
	}

	@Override public java.lang.String[] secondaryCode()
	{
		java.lang.String strPrimaryCode = primaryCode();

		int iNumTokens = 0;
		java.lang.String astrCodeTokens[] = new java.lang.String[2];

		java.util.StringTokenizer stCodeTokens = new java.util.StringTokenizer (strPrimaryCode, ".");

		while (stCodeTokens.hasMoreTokens())
			astrCodeTokens[iNumTokens++] = stCodeTokens.nextToken();

		return new java.lang.String[] {astrCodeTokens[0]};
	}

	@Override public java.lang.String name()
	{
		return _strEDCode;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCcy = new java.util.HashSet<java.lang.String>();

		setCcy.add (_strCurrency);

		return setCcy;
	}

	@Override public java.lang.String[] couponCurrency()
	{
		return new java.lang.String[] {_strCurrency};
	}

	@Override public java.lang.String[] principalCurrency()
	{
		return new java.lang.String[] {_strCurrency};
	}

	@Override public double initialNotional()
	{
		return _dblNotional;
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("EDFComponent::notional => Got NaN date");

		return _notlSchedule.getFactor (dblDate);
	}

	@Override public double coupon (
		final double dblValue,
		final org.drip.param.market.MarketParamSet mktParams)
		throws java.lang.Exception
	{
		return 0.;
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2))
			throw new java.lang.Exception ("EDFComponent::notional => Got NaN date");

		return _notlSchedule.getFactor (dblDate1, dblDate2);
	}

	@Override public java.lang.String[] forwardCurveName()
	{
		return null;
	}

	@Override public java.lang.String[] creditCurveName()
	{
		return null;
	}

	@Override public java.lang.String[] currencyPairCode()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate effective()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate maturity()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		return null;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> cashFlowPeriod()
	{
		return org.drip.analytics.period.CashflowPeriod.GenerateSinglePeriod (_dblEffective, _dblMaturity,
			_strDC, _strCalendar, _strCurrency);
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _settleParams;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == mktParams || valParams.valueDate() >= _dblMaturity) return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = mktParams.fundingCurve (couponCurrency()[0]);

		if (null == dcFunding) return null;

		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		try {
			double dblCashSettle = null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (valParams.valueDate());

			double dblUnadjustedAnnuity = dcFunding.df (_dblMaturity) / dcFunding.df (_dblEffective) /
				dcFunding.df (dblCashSettle);

			double dblAdjustedAnnuity = dblUnadjustedAnnuity / dcFunding.df (dblCashSettle);

			mapResult.put ("PV", dblAdjustedAnnuity * _dblNotional * 0.01 * notional (_dblEffective,
				_dblMaturity));

			mapResult.put ("Price", 100. * dblAdjustedAnnuity);

			mapResult.put ("Rate", ((1. / dblUnadjustedAnnuity) - 1.) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblEffective, _dblMaturity, _strDC,
					false, _dblMaturity, null, _strCalendar));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("Rate");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= maturity().getJulian() || null == mktParams)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = mktParams.fundingCurve (couponCurrency()[0]);

		if (null == dcFunding) return null;

		try {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, mktParams, quotingParams);

			if (null == mapMeasures) return null;

			double dblDFEffective = dcFunding.df (_dblEffective);

			double dblDFMaturity = dcFunding.df (maturity().getJulian());

			org.drip.quant.calculus.WengertJacobian wjDFEffective = dcFunding.jackDDFDManifestMeasure
				(_dblEffective, "Rate");

			org.drip.quant.calculus.WengertJacobian wjDFMaturity = dcFunding.jackDDFDManifestMeasure
				(maturity().getJulian(), "Rate");

			if (null == wjDFEffective || null == wjDFMaturity) return null;

			org.drip.quant.calculus.WengertJacobian wjPVDFMicroJack = new
				org.drip.quant.calculus.WengertJacobian (1, wjDFMaturity.numParameters());

			for (int i = 0; i < wjDFMaturity.numParameters(); ++i) {
				if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, i, wjDFMaturity.getFirstDerivative
					(0, i) / dblDFEffective))
					return null;

				if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, i,
					-wjDFEffective.getFirstDerivative (0, i) * dblDFMaturity / dblDFEffective /
						dblDFEffective))
					return null;
			}

			return wjPVDFMicroJack;

			/* return adjustPVDFMicroJackForCashSettle (valParams.cashPayDate(), dblPV, dc, wjPVDFMicroJack) ?
				wjPVDFMicroJack : null; */
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= maturity().getJulian() || null ==
			strManifestMeasure || strManifestMeasure.isEmpty() || null == mktParams)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = mktParams.fundingCurve (couponCurrency()[0]);

		if (null == dcFunding) return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure)) {
			try {
				double dblDFEffective = dcFunding.df (_dblEffective);

				double dblDFMaturity = dcFunding.df (maturity().getJulian());

				org.drip.quant.calculus.WengertJacobian wjDFEffective = dcFunding.jackDDFDManifestMeasure
					(_dblEffective, "Rate");

				org.drip.quant.calculus.WengertJacobian wjDFMaturity = dcFunding.jackDDFDManifestMeasure
					(maturity().getJulian(), "Rate");

				if (null == wjDFEffective || null == wjDFMaturity) return null;

				org.drip.quant.calculus.WengertJacobian wjDFMicroJack = new
					org.drip.quant.calculus.WengertJacobian (1, wjDFMaturity.numParameters());

				for (int i = 0; i < wjDFMaturity.numParameters(); ++i) {
					if (!wjDFMicroJack.accumulatePartialFirstDerivative (0, i,
						wjDFMaturity.getFirstDerivative (0, i) / dblDFEffective))
						return null;

					if (!wjDFMicroJack.accumulatePartialFirstDerivative (0, i, -1. *
						wjDFEffective.getFirstDerivative (0, i) * dblDFMaturity / dblDFEffective /
							dblDFEffective))
						return null;
				}

				return wjDFMicroJack;
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRLC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.MarketParamSet mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == lsmm || !(lsmm instanceof
			org.drip.analytics.rates.RatesLSMM) ||
				!org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT.equalsIgnoreCase (lsmm.id()))
			return null;

		org.drip.analytics.rates.RatesLSMM ratesLSMM = (org.drip.analytics.rates.RatesLSMM) lsmm;

		java.lang.String[] astrManifestMeasure = ratesLSMM.manifestMeasures();

		if (org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
			(ratesLSMM.quantificationMetric())) {
			try {
				org.drip.analytics.rates.TurnListDiscountFactor tldf = ratesLSMM.turnsDiscount();

				double dblTurnMaturityDF = null == tldf ? 1. : tldf.turnAdjust (valParams.valueDate(),
					_dblMaturity);

				if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
					java.lang.String[] {"Price"}, false)) {
					org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
						org.drip.state.estimator.PredictorResponseWeightConstraint();

					return prlc.addPredictorResponseWeight (_dblMaturity, -dblTurnMaturityDF) &&
						prlc.addPredictorResponseWeight (_dblEffective, 0.01 *
							ratesLSMM.measureQuoteValue ("Price")) && prlc.updateValue (0.) &&
								prlc.addDResponseWeightDManifestMeasure ("Price", _dblMaturity, 0.) &&
									prlc.addDResponseWeightDManifestMeasure ("Price", _dblEffective, 0.01) &&
										prlc.updateDValueDManifestMeasure ("Price", 0.) ? prlc : null;
				}

				if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
					java.lang.String[] {"PV"}, false)) {
					org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
						org.drip.state.estimator.PredictorResponseWeightConstraint();

					return prlc.addPredictorResponseWeight (_dblMaturity, -dblTurnMaturityDF) &&
						prlc.addPredictorResponseWeight (_dblEffective, ratesLSMM.measureQuoteValue ("PV"))
							&& prlc.updateValue (0.) && prlc.addDResponseWeightDManifestMeasure ("PV",
								_dblMaturity, 0.) && prlc.addDResponseWeightDManifestMeasure ("PV",
									_dblEffective, 1.) && prlc.updateDValueDManifestMeasure ("PV", 0.) ? prlc
										: null;
				}

				if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
					java.lang.String[] {"Rate"}, false)) {
					org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
						org.drip.state.estimator.PredictorResponseWeightConstraint();

					double dblTurnEffectiveDF = null == tldf ? 1. : tldf.turnAdjust (valParams.valueDate(),
						_dblMaturity);

					double dblDCF = org.drip.analytics.daycount.Convention.YearFraction (_dblEffective,
						_dblMaturity, _strDC, false, _dblMaturity, null, _strCalendar);

					double dblDF = 1. / (1. + (ratesLSMM.measureQuoteValue ("Rate") * dblDCF));

					return prlc.addPredictorResponseWeight (_dblMaturity, -dblTurnMaturityDF) &&
						prlc.addPredictorResponseWeight (_dblEffective, dblTurnEffectiveDF * dblDF) &&
							prlc.updateValue (0.) && prlc.addDResponseWeightDManifestMeasure ("Rate",
								_dblMaturity, 0.) && prlc.addDResponseWeightDManifestMeasure ("Rate",
									_dblEffective, -1. * dblDCF * dblTurnEffectiveDF * dblDF * dblDF) &&
										prlc.updateDValueDManifestMeasure ("Rate", 0.) ? prlc : null;
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		sb.append (_dblNotional + fieldDelimiter());

		if (null == _strCurrency || _strCurrency.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCurrency + fieldDelimiter());

		if (null == _strEDCode || _strEDCode.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strEDCode + fieldDelimiter());

		sb.append (_dblMaturity + fieldDelimiter());

		sb.append (_dblEffective + fieldDelimiter());

		if (null == _notlSchedule)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_notlSchedule.serialize()) + fieldDelimiter());

		if (null == _settleParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else
			sb.append (new java.lang.String (_settleParams.serialize()));

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new EDFComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		EDFComponent edf = new EDFComponent (org.drip.analytics.date.JulianDate.Today(),
			org.drip.analytics.date.JulianDate.Today().addTenor ("1Y"), "GBP", "Act/360", "GBP");

		byte[] abEDF = edf.serialize();

		System.out.println (new java.lang.String (abEDF));

		EDFComponent edfDeser = new EDFComponent (abEDF);

		System.out.println (new java.lang.String (edfDeser.serialize()));
	}
}

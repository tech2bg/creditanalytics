
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
 * CashComponent contains the implementation of the Cash IR product and its contract/valuation details. It
 *  exports the following functionality:
 *  - Standard/Custom Constructor for the Cash Component
 *  - Dates: Effective, Maturity, Coupon dates and Product settlement Parameters
 *  - Coupon/Notional Outstanding as well as schedules
 *  - Market Parameters: Discount, Forward, Credit, Treasury, EDSF Curves
 *  - Cash Flow Periods: Coupon flows and (Optionally) Loss Flows
 *  - Valuation: Named Measure Generation
 *  - Calibration: The codes and constraints generation
 *  - Jacobians: Quote/DF and PV/DF micro-Jacobian generation
 *  - Serialization into and de-serialization out of byte arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CashComponent extends org.drip.product.definition.RatesComponent {
	private double _dblNotional = 100.;
	private java.lang.String _strIR = "";
	private java.lang.String _strCode = "";
	private java.lang.String _strDC = "Act/360";
	private java.lang.String _strCalendar = "USD";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;

	/**
	 * Construct a CashComponent instance
	 * 
	 * @param dtEffective Effective Date
	 * @param dtMaturity Maturity Date
	 * @param strIR IR Curve
	 * @param strDC Day Count
	 * @param strCalendar Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CashComponent (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final java.lang.String strIR,
		final java.lang.String strDC,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == dtEffective || null == dtMaturity || null == (_strIR = strIR) || strIR.isEmpty() ||
			(_dblMaturity = dtMaturity.getJulian()) <= (_dblEffective = dtEffective.getJulian()))
			throw new java.lang.Exception ("CashComponent ctr: Invalid Inputs!");

		_strDC = strDC;
		_strCalendar = strCalendar;

		_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();
	}

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * CashComponent de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if CashComponent cannot be properly de-serialized
	 */

	public CashComponent (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("CashComponent de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("CashComponent de-serializer: Empty state");

		java.lang.String strSerializedCash = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedCash || strSerializedCash.isEmpty())
			throw new java.lang.Exception ("CashComponent de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedCash,
			getFieldDelimiter());

		if (null == astrField || 9 > astrField.length)
			throw new java.lang.Exception ("CashComponent de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("CashComponent de-serializer: Cannot locate notional");

		_dblNotional = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("CashComponent de-serializer: Cannot locate IR curve name");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_strIR = astrField[2];
		else
			_strIR = "";

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("CashComponent de-serializer: Cannot locate cash code");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strCode = astrField[3];
		else
			_strCode = "";

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception
				("CashComponent de-serializer: Cannot locate day count convention");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strDC = astrField[4];
		else
			_strDC = "";

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception ("CashComponent de-serializer: Cannot locate Calendar");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strCalendar = astrField[5];
		else
			_strCalendar = "";

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception ("CashComponent de-serializer: Cannot locate maturity date");

		_dblMaturity = new java.lang.Double (astrField[6]);

		if (null == astrField[7] || astrField[7].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			throw new java.lang.Exception ("CashComponent de-serializer: Cannot locate effective date");

		_dblEffective = new java.lang.Double (astrField[7]);

		if (null == astrField[8] || astrField[8].isEmpty())
			throw new java.lang.Exception ("CashComponent de-serializer: Cannot locate notional schedule");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			_notlSchedule = null;
		else
			_notlSchedule = new org.drip.product.params.FactorSchedule (astrField[8].getBytes());

		if (null == astrField[9] || astrField[9].isEmpty())
			throw new java.lang.Exception ("CashComponent de-serializer: Cannot locate cash settle params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[9]))
			_settleParams = null;
		else
			_settleParams = new org.drip.param.valuation.CashSettleParams (astrField[9].getBytes());
	}

	@Override public java.lang.String getPrimaryCode()
	{
		return _strCode;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	@Override public java.lang.String getComponentName()
	{
		return "CD=" + org.drip.analytics.date.JulianDate.fromJulian (_dblMaturity);
	}

	@Override public java.lang.String getTreasuryCurveName()
	{
		return "";
	}

	@Override public java.lang.String getEDSFCurveName()
	{
		return "";
	}

	@Override public double getInitialNotional()
	{
		return _dblNotional;
	}

	@Override public double getNotional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("CashComponent::getNotional => Bad date into getNotional");

		return _notlSchedule.getFactor (dblDate);
	}

	@Override public double getNotional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2))
			throw new java.lang.Exception ("CashComponent::getNotional => Bad date into getNotional");

		return _notlSchedule.getFactor (dblDate1, dblDate2);
	}

	@Override public double getCoupon (
		final double dblValue,
		final org.drip.param.definition.ComponentMarketParams mktParams)
		throws java.lang.Exception
	{
		return 0.;
	}

	@Override public boolean setCurves (
		final java.lang.String strIR,
		final java.lang.String strIRTSY,
		final java.lang.String strCC)
	{
		if (null == strIR || strIR.isEmpty()) return false;

		_strIR = strIR;
		return true;
	}

	@Override public java.lang.String getIRCurveName()
	{
		return _strIR;
	}

	@Override public java.lang.String getForwardCurveName()
	{
		return "";
	}

	@Override public java.lang.String getCreditCurveName()
	{
		return "";
	}

	@Override public org.drip.analytics.date.JulianDate getEffectiveDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate getMaturityDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate getFirstCouponDate()
	{
		return null;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> getCashFlowPeriod()
	{
		return org.drip.analytics.period.CashflowPeriod.GetSinglePeriod (_dblEffective, _dblMaturity,
			_strCalendar);
	}

	@Override public org.drip.param.valuation.CashSettleParams getCashSettleParams()
	{
		return _settleParams;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == mktParams) return null;

		org.drip.analytics.rates.DiscountCurve dc = mktParams.getFundingCurve();

		if (null == dc) return null;

		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		try {
			double dblCashSettle = null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (valParams.valueDate());

			double dblUnadjustedAnnuity = dc.df (_dblMaturity) / dc.df (_dblEffective) / dc.df
				(dblCashSettle);

			double dblAdjustedAnnuity = dblUnadjustedAnnuity / dc.df (dblCashSettle);

			mapResult.put ("pv", dblAdjustedAnnuity * _dblNotional * 0.01 * getNotional (_dblEffective,
				_dblMaturity));

			mapResult.put ("price", 100. * dblAdjustedAnnuity);

			mapResult.put ("rate", ((1. / dblUnadjustedAnnuity) - 1.) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblEffective, _dblMaturity, _strDC,
					false, _dblMaturity, null, _strCalendar));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		mapResult.put ("calctime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> getMeasureNames()
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
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == mktParams || null ==
			mktParams.getFundingCurve())
			return null;

		try {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, mktParams, quotingParams);

			if (null == mapMeasures) return null;

			org.drip.analytics.rates.DiscountCurve dc = mktParams.getFundingCurve();

			org.drip.quant.calculus.WengertJacobian wjDFDF = dc.jackDDFDManifestMeasure (_dblMaturity,
				"Rate");

			if (null == wjDFDF) return null;

			org.drip.quant.calculus.WengertJacobian wjPVDFMicroJack = new
				org.drip.quant.calculus.WengertJacobian (1, wjDFDF.numParameters());

			for (int k = 0; k < wjDFDF.numParameters(); ++k) {
				if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, k, wjDFDF.getFirstDerivative (0,
					k)))
					return null;
			}

			return wjPVDFMicroJack;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian calcQuoteDFMicroJack (
		final java.lang.String strQuote,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == strQuote || null ==
			mktParams || null == mktParams.getFundingCurve())
			return null;

		if ("Rate".equalsIgnoreCase (strQuote)) {
			try {
				org.drip.analytics.rates.DiscountCurve dc = mktParams.getFundingCurve();

				org.drip.quant.calculus.WengertJacobian wjDF = dc.jackDDFDManifestMeasure (_dblMaturity,
					"Rate");

				if (null == wjDF) return null;

				org.drip.quant.calculus.WengertJacobian wjDFMicroJack = new
					org.drip.quant.calculus.WengertJacobian (1, wjDF.numParameters());

				for (int k = 0; k < wjDF.numParameters(); ++k) {
					if (!wjDFMicroJack.accumulatePartialFirstDerivative (0, k, -365.25 / (_dblMaturity -
						_dblEffective) / dc.df (_dblMaturity) * wjDF.getFirstDerivative (0, k)))
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
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == lsmm || !(lsmm instanceof
			org.drip.analytics.rates.RatesLSMM) ||
				!org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT.equalsIgnoreCase
					(lsmm.getID()))
			return null;

		org.drip.analytics.rates.RatesLSMM ratesLSMM = (org.drip.analytics.rates.RatesLSMM) lsmm;

		java.lang.String[] astrManifestMeasure = ratesLSMM.getManifestMeasures();

		if (org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
			(ratesLSMM.getQuantificationMetric())) {
			try {
				org.drip.analytics.rates.TurnListDiscountFactor tldf = ratesLSMM.turnsDiscount();

				double dblTurnDF = null == tldf ? 1. : tldf.turnAdjust (valParams.valueDate(), _dblMaturity);

				if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
					java.lang.String[] {"Price"}, false)) {
					org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
						org.drip.state.estimator.PredictorResponseWeightConstraint();

					return prlc.addPredictorResponseWeight (_dblMaturity, dblTurnDF) && prlc.updateValue
						(0.01 * ratesLSMM.getMeasureQuoteValue()) && prlc.addDResponseWeightDManifestMeasure
							("Price", _dblMaturity, 0.) && prlc.updateDValueDManifestMeasure ("Price", 0.01)
								? prlc : null;
				}

				if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
					java.lang.String[] {"PV"}, false)) {
					org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
						org.drip.state.estimator.PredictorResponseWeightConstraint();

					return prlc.addPredictorResponseWeight (_dblMaturity, dblTurnDF) && prlc.updateValue
						(ratesLSMM.getMeasureQuoteValue()) && prlc.addDResponseWeightDManifestMeasure ("PV",
							_dblMaturity, 0.) && prlc.updateDValueDManifestMeasure ("PV", 1.) ? prlc : null;
				}

				if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
					java.lang.String[] {"Rate"}, false)) {
					org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
						org.drip.state.estimator.PredictorResponseWeightConstraint();

					double dblDCF = org.drip.analytics.daycount.Convention.YearFraction (_dblEffective,
						_dblMaturity, _strDC, false, _dblMaturity, null, _strCalendar);

					double dblDF = 1. / (1. + ratesLSMM.getMeasureQuoteValue() * dblDCF);

					return prlc.addPredictorResponseWeight (_dblMaturity, dblTurnDF) && prlc.updateValue
						(dblDF) && prlc.addDResponseWeightDManifestMeasure ("Rate", _dblMaturity, 0.) &&
							prlc.updateDValueDManifestMeasure ("Rate", -1. * dblDCF * dblDF * dblDF) ? prlc :
								null;
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

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		sb.append (_dblNotional + getFieldDelimiter());

		if (null == _strIR || _strIR.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strIR + getFieldDelimiter());

		if (null == _strCode || _strCode.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strCode + getFieldDelimiter());

		if (null == _strDC || _strDC.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strDC + getFieldDelimiter());

		if (null == _strCalendar || _strCalendar.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strCalendar + getFieldDelimiter());

		sb.append (_dblMaturity + getFieldDelimiter());

		sb.append (_dblEffective + getFieldDelimiter());

		if (null == _notlSchedule)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_notlSchedule.serialize()) + getFieldDelimiter());

		if (null == _settleParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else
			sb.append (new java.lang.String (_settleParams.serialize()));

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new CashComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		CashComponent cash = new CashComponent (org.drip.analytics.date.JulianDate.Today(),
			org.drip.analytics.date.JulianDate.Today().addTenor ("1Y"), "AUD", "Act/360", "BMA");

		byte[] abCash = cash.serialize();

		System.out.println (new java.lang.String (abCash));

		CashComponent cashDeser = new CashComponent (abCash);

		System.out.println (new java.lang.String (cashDeser.serialize()));
	}
}

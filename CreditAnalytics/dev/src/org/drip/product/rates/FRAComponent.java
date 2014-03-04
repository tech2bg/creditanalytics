
package org.drip.product.rates;

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
 * FRAComponent contains the implementation of the Multi-Curve FRA product.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FRAComponent extends org.drip.product.definition.RatesComponent {
	private double _dblNotional = 1.;
	private java.lang.String _strIR = "";
	private java.lang.String _strCode = "";
	private java.lang.String _strCalendar = "USD";
	private double _dblStrike = java.lang.Double.NaN;
	private double _dblEffectiveDate = java.lang.Double.NaN;
	private org.drip.product.params.FloatingRateIndex _fri = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;

	class ForwardExchangeQuantoProduct extends org.drip.quant.function1D.AbstractUnivariate {
		org.drip.quant.function1D.AbstractUnivariate _auFRIVolatility = null;
		org.drip.quant.function1D.AbstractUnivariate _auForwardToDomesticExchangeVolatility = null;
		org.drip.quant.function1D.AbstractUnivariate _auFRIForwardToDomesticExchangeCorrelation = null;

		ForwardExchangeQuantoProduct (
			final org.drip.quant.function1D.AbstractUnivariate auFRIVolatility,
			final org.drip.quant.function1D.AbstractUnivariate auForwardToDomesticExchangeVolatility,
			final org.drip.quant.function1D.AbstractUnivariate auFRIForwardToDomesticExchangeCorrelation)
		{
			super (null);

			_auFRIVolatility = auFRIVolatility;
			_auForwardToDomesticExchangeVolatility = auForwardToDomesticExchangeVolatility;
			_auFRIForwardToDomesticExchangeCorrelation = auFRIForwardToDomesticExchangeCorrelation;
		}

		@Override public double evaluate (
			final double dblVariate)
			throws java.lang.Exception
		{
			return _auFRIVolatility.evaluate (dblVariate) * _auForwardToDomesticExchangeVolatility.evaluate
				(dblVariate) * _auFRIForwardToDomesticExchangeCorrelation.evaluate (dblVariate);
		}
	}

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		return null;
	}

	/**
	 * FRAComponent constructor
	 * 
	 * @param dblNotional Component Notional
	 * @param strIR IR Curve
	 * @param strCode FRA Product Code
	 * @param strCalendar FRA Calendar
	 * @param dblEffectiveDate FRA Effective Date
	 * @param fri FRA Floating Rate Index
	 * @param dblStrike FRA Strike
	 * @param notlSchedule Notional Schedule
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public FRAComponent (
		final double dblNotional,
		final java.lang.String strIR,
		final java.lang.String strCode,
		final java.lang.String strCalendar,
		final double dblEffectiveDate,
		final org.drip.product.params.FloatingRateIndex fri,
		final double dblStrike,
		final org.drip.product.params.FactorSchedule notlSchedule)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) || 0. == _dblNotional ||
			null == (_strIR = strIR) || _strIR.isEmpty() || null == (_strCode = strCode) ||
				_strCode.isEmpty() || null == (_strCalendar = strCalendar) || _strCalendar.isEmpty() ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblEffectiveDate = dblEffectiveDate) || null
						== (_fri = fri) || !org.drip.quant.common.NumberUtil.IsValid (_dblStrike =
							dblStrike))
			throw new java.lang.Exception ("FRAComponent ctr => Invalid Inputs!");

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();
	}

	/**
	 * FRAComponent de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if FRAComponent cannot be properly de-serialized
	 */

	public FRAComponent (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("FRAComponent de-serializer: Invalid input Byte array");

		java.lang.String strFRAComponent = new java.lang.String (ab);

		if (null == strFRAComponent || strFRAComponent.isEmpty())
			throw new java.lang.Exception ("FRAComponent de-serializer: Empty state");

		java.lang.String strSerializedFRAComponent = strFRAComponent.substring (0, strFRAComponent.indexOf
			(getObjectTrailer()));

		if (null == strSerializedFRAComponent || strSerializedFRAComponent.isEmpty())
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedFRAComponent,
			getFieldDelimiter());

		if (null == astrField || 10 > astrField.length)
			throw new java.lang.Exception ("FRAComponent de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate notional");

		_dblNotional = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate IR curve name");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_strIR = astrField[2];
		else
			_strIR = "";

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate code");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strCode = astrField[3];
		else
			_strCode = "";

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate calendar");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strCalendar = astrField[4];
		else
			_strCalendar = "";

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate Effective Date");

		_dblEffectiveDate = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty())
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate rate index");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			_fri = null;
		else
			_fri = new org.drip.product.params.FloatingRateIndex (astrField[6].getBytes());

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate notional schedule");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			_notlSchedule = null;
		else
			_notlSchedule = new org.drip.product.params.FactorSchedule (astrField[7].getBytes());

		if (null == astrField[8] || astrField[8].isEmpty())
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate cash settle params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			_settleParams = null;
		else
			_settleParams = new org.drip.param.valuation.CashSettleParams (astrField[8].getBytes());

		if (null == astrField[9] || astrField[9].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[9]))
			throw new java.lang.Exception ("FRAComponent de-serializer: Cannot locate Strike");

		_dblStrike = new java.lang.Double (astrField[9]);
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
		return "FRA=" + _fri.fullyQualifiedName();
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
		return _fri.fullyQualifiedName();
	}

	@Override public java.lang.String getCreditCurveName()
	{
		return "";
	}

	@Override public org.drip.analytics.date.JulianDate getEffectiveDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffectiveDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate getMaturityDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffectiveDate).addTenor (_fri.tenor());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate getFirstCouponDate()
	{
		return getMaturityDate();
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> getCashFlowPeriod()
	{
		try {
			return org.drip.analytics.period.CashflowPeriod.GetSinglePeriod (_dblEffectiveDate, new
				org.drip.analytics.date.JulianDate (_dblEffectiveDate).addTenor (_fri.tenor()).getJulian(),
					_strCalendar);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.param.valuation.CashSettleParams getCashSettleParams()
	{
		return _settleParams;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == valParams || null == mktParams) return null;

		long lStart = System.nanoTime();

		double dblQuantoAdjustment = 1.;
		double dblMaturity = java.lang.Double.NaN;
		double dblParForward = java.lang.Double.NaN;
		org.drip.analytics.date.JulianDate dtMaturity = null;

		try {
			if (valParams.valueDate() >= (dblMaturity = (dtMaturity = new org.drip.analytics.date.JulianDate
				(_dblEffectiveDate).addTenor (_fri.tenor())).getJulian()))
					return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.analytics.rates.DiscountCurve dc = mktParams.getDiscountCurve();

		if (null == dc) return null;

		org.drip.analytics.rates.ForwardRateEstimator fc = mktParams.getForwardCurve();

		if (null == fc || !_fri.match (fc.index())) return null;

		java.lang.String strFRI = _fri.fullyQualifiedName();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		try {
			double dblCashSettle = null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (valParams.valueDate());

			java.util.Map<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mapFixings =
					mktParams.getFixings();

			if (null != mapFixings && mapFixings.containsKey (dtMaturity)) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFixing =
					mapFixings.get (dtMaturity);

				dblParForward = null != mapFixing && mapFixing.containsKey (strFRI) ? mapFixing.get (strFRI)
					: fc.forward (dblMaturity);
			} else
				dblParForward = fc.forward (dblMaturity);

			org.drip.quant.function1D.AbstractUnivariate auFRIVolatility = mktParams.getLatentStateVolSurface
				(strFRI);

			if (null != auFRIVolatility) {
				org.drip.quant.function1D.AbstractUnivariate auForwardToDomesticExchangeVolatility =
					mktParams.getLatentStateVolSurface ("ForwardToDomesticExchangeVolatility");

				if (null != auForwardToDomesticExchangeVolatility) {
					org.drip.quant.function1D.AbstractUnivariate auFRIForwardToDomesticExchangeCorrelation =
						mktParams.getLatentStateVolSurface ("FRIForwardToDomesticExchangeCorrelation");

					if (null != auFRIForwardToDomesticExchangeCorrelation)
						dblQuantoAdjustment = java.lang.Math.exp (-1. * new ForwardExchangeQuantoProduct
							(auFRIVolatility, auForwardToDomesticExchangeVolatility,
								auFRIForwardToDomesticExchangeCorrelation).integrate (valParams.valueDate(),
									_dblEffectiveDate) / 365.25);
				}
			}

			double dblQuantoAdjustedParForward = dblParForward * dblQuantoAdjustment;

			double dblPV = dc.df (dblMaturity) / dc.df (dblCashSettle) * _dblNotional *
				(dblQuantoAdjustedParForward - _dblStrike);

			mapResult.put ("parforward", dblParForward);

			mapResult.put ("price", dblPV);

			mapResult.put ("pv", dblPV);

			mapResult.put ("quantoadjustedparforward", dblQuantoAdjustedParForward);

			mapResult.put ("quantoadjustment", dblQuantoAdjustment);

			mapResult.put ("upfront", dblPV);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		mapResult.put ("calctime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> getMeasureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("ParForward");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("QuantoAdjustedParForward");

		setstrMeasureNames.add ("QuantoAdjustment");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == valParams || null == mktParams || null == mktParams.getDiscountCurve())
			return null;

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian calcQuoteDFMicroJack (
		final java.lang.String strQuote,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == valParams || null == strQuote || null == mktParams || null ==
			mktParams.getDiscountCurve())
			return null;

		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRLC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		if (null == valParams || null == lsmm) return null;

		return null;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		sb.append (_dblNotional + getFieldDelimiter());

		sb.append (_strIR + getFieldDelimiter());

		sb.append (_strCode + getFieldDelimiter());

		sb.append (_strCalendar + getFieldDelimiter());

		sb.append (_dblEffectiveDate + getFieldDelimiter());

		if (null == _fri)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_fri.serialize()) + getFieldDelimiter());

		if (null == _notlSchedule)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_notlSchedule.serialize()) + getFieldDelimiter());

		if (null == _settleParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_settleParams.serialize()) + getFieldDelimiter());

		return sb.append (_dblStrike + getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new FRAComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the FRA Strike
	 * 
	 * @return The FRA Strike
	 */

	public double strike()
	{
		return _dblStrike;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		FRAComponent fra = new FRAComponent (1., "JPY", "JPY-FRA-3M", "JPY",
			org.drip.analytics.date.JulianDate.Today().getJulian(),
				org.drip.product.params.FloatingRateIndex.Create ("JPY-LIBOR-6M"), 0.01, null);

		byte[] abFRA = fra.serialize();

		System.out.println (new java.lang.String (abFRA));

		FRAComponent fraDeser = new FRAComponent (abFRA);

		System.out.println (new java.lang.String (fraDeser.serialize()));
	}
}

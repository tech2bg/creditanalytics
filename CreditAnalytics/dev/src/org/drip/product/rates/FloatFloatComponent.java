
package org.drip.product.rates;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * FloatFloatComponent contains the implementation of the Float-Float Index Basis Swap product
 *  contract/valuation details. It is made off one Visible Floating stream and one Work-out floating stream.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FloatFloatComponent extends org.drip.product.definition.RatesComponent {
	private java.lang.String _strCode = "";
	private org.drip.product.rates.FloatingStream _floatVisible = null;
	private org.drip.product.rates.FloatingStream _floatWorkout = null;

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		return null;
	}

	/**
	 * Construct the FloatFloatComponent from the fixed and the floating streams
	 * 
	 * @param floatVisible The Visible Floating Stream (e.g., 6M LIBOR/EURIBOR Leg)
	 * @param floatWorkout The Visible Floating Stream (e.g., 3M LIBOR/EURIBOR Leg)
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public FloatFloatComponent (
		final org.drip.product.rates.FloatingStream floatVisible,
		final org.drip.product.rates.FloatingStream floatWorkout)
		throws java.lang.Exception
	{
		if (null == (_floatVisible = floatVisible) || null == (_floatWorkout = floatWorkout))
			throw new java.lang.Exception ("FloatFloatComponent ctr: Invalid Inputs");
	}

	/**
	 * De-serialize the FloatFloatComponent from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if the FloatFloatComponent cannot be de-serialized from the byte
	 *  array
	 */

	public FloatFloatComponent (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("FloatFloatComponent de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("FloatFloatComponent de-serializer: Empty state");

		java.lang.String strSerializedFloatFloatComponent = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedFloatFloatComponent || strSerializedFloatFloatComponent.isEmpty())
			throw new java.lang.Exception ("FloatFloatComponent de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split
			(strSerializedFloatFloatComponent, getFieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("FloatFloatComponent de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]).doubleValue();

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception
				("FloatFloatComponent de-serializer: Cannot locate visible floating stream");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_floatVisible = null;
		else
			_floatVisible = new org.drip.product.rates.FloatingStream (astrField[1].getBytes());

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception
				("FloatFloatComponent de-serializer: Cannot locate work-out floating stream");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_floatWorkout = null;
		else
			_floatWorkout = new org.drip.product.rates.FloatingStream (astrField[2].getBytes());
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	@Override public java.lang.String getPrimaryCode()
	{
		return _strCode;
	}

	@Override public java.lang.String getComponentName()
	{
		return "IBS=" + getMaturityDate();
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
		throws java.lang.Exception
	{
		return _floatVisible.getInitialNotional();
	}

	@Override public double getNotional (
		final double dblDate)
		throws java.lang.Exception
	{
		return _floatVisible.getNotional (dblDate);
	}

	@Override public double getNotional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		return _floatVisible.getNotional (dblDate1, dblDate2);
	}

	@Override public boolean setCurves (
		final java.lang.String strIR,
		final java.lang.String strIRTSY,
		final java.lang.String strCC)
	{
		return _floatVisible.setCurves (strIR, strIRTSY, strCC) && _floatWorkout.setCurves (strIR, strIRTSY,
			strCC);
	}

	@Override public double getCoupon (
		final double dblValue,
		final org.drip.param.definition.ComponentMarketParams mktParams)
		throws java.lang.Exception
	{
		return _floatVisible.getCoupon (dblValue, mktParams);
	}

	@Override public java.lang.String getIRCurveName()
	{
		return _floatVisible.getIRCurveName();
	}

	@Override public java.lang.String getRatesForwardCurveName()
	{
		return _floatWorkout.getRatesForwardCurveName();
	}

	@Override public java.lang.String getCreditCurveName()
	{
		return "";
	}

	@Override public org.drip.analytics.date.JulianDate getEffectiveDate()
	{
		org.drip.analytics.date.JulianDate dtFloatVisibleEffective = _floatVisible.getEffectiveDate();

		org.drip.analytics.date.JulianDate dtFloatWorkoutEffective = _floatWorkout.getEffectiveDate();

		if (null == dtFloatVisibleEffective || null == dtFloatWorkoutEffective) return null;

		return dtFloatVisibleEffective.getJulian() < dtFloatWorkoutEffective.getJulian() ?
			dtFloatVisibleEffective : dtFloatWorkoutEffective;
	}

	@Override public org.drip.analytics.date.JulianDate getMaturityDate()
	{
		org.drip.analytics.date.JulianDate dtFloatVisibleMaturity = _floatVisible.getMaturityDate();

		org.drip.analytics.date.JulianDate dtFloatWorkoutMaturity = _floatWorkout.getMaturityDate();

		if (null == dtFloatVisibleMaturity || null == dtFloatWorkoutMaturity) return null;

		return dtFloatVisibleMaturity.getJulian() > dtFloatWorkoutMaturity.getJulian() ?
			dtFloatVisibleMaturity : dtFloatWorkoutMaturity;
	}

	@Override public org.drip.analytics.date.JulianDate getFirstCouponDate()
	{
		org.drip.analytics.date.JulianDate dtFloatVisibleFirstCoupon = _floatVisible.getFirstCouponDate();

		org.drip.analytics.date.JulianDate dtFloatWorkoutFirstCoupon = _floatWorkout.getFirstCouponDate();

		if (null == dtFloatVisibleFirstCoupon || null == dtFloatWorkoutFirstCoupon) return null;

		return dtFloatVisibleFirstCoupon.getJulian() < dtFloatWorkoutFirstCoupon.getJulian() ?
			dtFloatVisibleFirstCoupon : dtFloatWorkoutFirstCoupon;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> getCashFlowPeriod()
	{
		return org.drip.analytics.support.AnalyticsHelper.MergePeriodLists
			(_floatVisible.getCashFlowPeriod(), _floatWorkout.getCashFlowPeriod());
	}

	@Override public org.drip.param.valuation.CashSettleParams getCashSettleParams()
	{
		return _floatVisible.getCashSettleParams();
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFloatVisibleStreamResult =
			_floatVisible.value (valParams, pricerParams, mktParams, quotingParams);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFloatWorkoutStreamResult =
			_floatWorkout.value (valParams, pricerParams, mktParams, quotingParams);

		if (null == mapFloatVisibleStreamResult || 0 == mapFloatVisibleStreamResult.size() || null ==
			mapFloatWorkoutStreamResult || 0 == mapFloatWorkoutStreamResult.size())
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put ("VisibleAccrued01", mapFloatVisibleStreamResult.get ("Accrued01"));

		mapResult.put ("VisibleAccrued", mapFloatVisibleStreamResult.get ("FloatAccrued"));

		double dblVisibleCleanDV01 = mapFloatVisibleStreamResult.get ("CleanDV01");

		mapResult.put ("VisibleCleanDV01", dblVisibleCleanDV01);

		double dblVisibleCleanPV = mapFloatVisibleStreamResult.get ("CleanPV");

		mapResult.put ("VisibleCleanPV", dblVisibleCleanPV);

		mapResult.put ("VisibleDirtyDV01", mapFloatVisibleStreamResult.get ("DirtyDV01"));

		double dblVisibleDirtyPV = mapFloatVisibleStreamResult.get ("DirtyPV");

		mapResult.put ("VisibleDirtyPV", dblVisibleDirtyPV);

		mapResult.put ("VisibleDV01", mapFloatVisibleStreamResult.get ("DV01"));

		mapResult.put ("VisibleFixing01", mapFloatVisibleStreamResult.get ("Fixing01"));

		double dblVisiblePV = mapFloatVisibleStreamResult.get ("PV");

		mapResult.put ("VisiblePV", dblVisiblePV);

		mapResult.put ("VisibleResetDate", mapFloatVisibleStreamResult.get ("ResetDate"));

		mapResult.put ("VisibleResetRate", mapFloatVisibleStreamResult.get ("ResetRate"));

		mapResult.put ("WorkoutAccrued01", mapFloatWorkoutStreamResult.get ("Accrued01"));

		mapResult.put ("WorkoutAccrued", mapFloatWorkoutStreamResult.get ("FloatAccrued"));

		double dblWorkoutCleanDV01 = mapFloatWorkoutStreamResult.get ("CleanDV01");

		mapResult.put ("WorkoutCleanDV01", dblWorkoutCleanDV01);

		double dblWorkoutCleanPV = mapFloatWorkoutStreamResult.get ("CleanPV");

		mapResult.put ("WorkoutCleanPV", dblWorkoutCleanPV);

		mapResult.put ("WorkoutDirtyDV01", mapFloatWorkoutStreamResult.get ("DirtyDV01"));

		double dblWorkoutDirtyPV = mapFloatWorkoutStreamResult.get ("DirtyPV");

		mapResult.put ("WorkoutDirtyPV", dblWorkoutDirtyPV);

		mapResult.put ("WorkoutDV01", mapFloatWorkoutStreamResult.get ("DV01"));

		mapResult.put ("WorkoutFixing01", mapFloatWorkoutStreamResult.get ("Fixing01"));

		double dblWorkoutPV = mapFloatWorkoutStreamResult.get ("PV");

		mapResult.put ("WorkoutPV", dblWorkoutPV);

		mapResult.put ("WorkoutResetDate", mapFloatWorkoutStreamResult.get ("ResetDate"));

		mapResult.put ("WorkoutResetRate", mapFloatWorkoutStreamResult.get ("ResetRate"));

		double dblCleanPV = dblVisibleCleanPV + dblWorkoutCleanPV;

		mapResult.put ("CleanPV", dblCleanPV);

		mapResult.put ("DirtyPV", dblWorkoutCleanPV + dblWorkoutDirtyPV);

		mapResult.put ("PV", dblVisiblePV + dblWorkoutPV);

		mapResult.put ("Upfront", mapFloatVisibleStreamResult.get ("Upfront") +
			mapFloatWorkoutStreamResult.get ("Upfront"));

		mapResult.put ("VisibleParBasisSpread", -1. * (dblVisibleCleanPV + dblWorkoutCleanPV) /
			dblVisibleCleanDV01);

		mapResult.put ("WorkoutParBasisSpread", -1. * (dblVisibleCleanPV + dblWorkoutCleanPV) /
			dblWorkoutCleanDV01);

		double dblValueNotional = java.lang.Double.NaN;

		try {
			dblValueNotional = getNotional (valParams._dblValue);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
				double dblCleanPrice = 100. * (1. + (dblCleanPV / getInitialNotional() / dblValueNotional));

				mapResult.put ("Price", dblCleanPrice);

				mapResult.put ("CleanPrice", dblCleanPrice);
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> getMeasureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("CleanPrice");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("Upfront");

		setstrMeasureNames.add ("VisibleAccrued01");

		setstrMeasureNames.add ("VisibleAccrued");

		setstrMeasureNames.add ("VisibleCleanDV01");

		setstrMeasureNames.add ("VisibleCleanPV");

		setstrMeasureNames.add ("VisibleDirtyDV01");

		setstrMeasureNames.add ("VisibleDirtyPV");

		setstrMeasureNames.add ("VisibleDV01");

		setstrMeasureNames.add ("VisibleFixing01");

		setstrMeasureNames.add ("VisibleParSpread");

		setstrMeasureNames.add ("VisiblePV");

		setstrMeasureNames.add ("VisibleResetDate");

		setstrMeasureNames.add ("VisibleResetRate");

		setstrMeasureNames.add ("WorkoutAccrued01");

		setstrMeasureNames.add ("WorkoutAccrued");

		setstrMeasureNames.add ("WorkoutCleanDV01");

		setstrMeasureNames.add ("WorkoutCleanPV");

		setstrMeasureNames.add ("WorkoutDirtyDV01");

		setstrMeasureNames.add ("WorkoutDirtyPV");

		setstrMeasureNames.add ("WorkoutDV01");

		setstrMeasureNames.add ("WorkoutFixing01");

		setstrMeasureNames.add ("WorkoutParSpread");

		setstrMeasureNames.add ("WorkoutPV");

		setstrMeasureNames.add ("WorkoutResetDate");

		setstrMeasureNames.add ("WorkoutResetRate");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian calcPVDFMicroJack (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian calcQuoteDFMicroJack (
		final java.lang.String strQuote,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRLC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = _floatWorkout.generateCalibPRLC
			(valParams, pricerParams, mktParams, quotingParams, lsmm);

		if (null == prwc) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapVisibleValue =
			_floatVisible.value (valParams, null, mktParams, quotingParams);

		if (null == mapVisibleValue || !mapVisibleValue.containsKey ("CleanPV")) return null;

		return prwc.updateValue (-1. * mapVisibleValue.get ("CleanPV")) ? prwc : null;
	}

	@Override public java.lang.String getFieldDelimiter()
	{
		return "{";
	}

	@Override public java.lang.String getObjectTrailer()
	{
		return "^";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		if (null == _floatVisible)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_floatVisible.serialize()) + getFieldDelimiter());

		if (null == _floatWorkout)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_floatWorkout.serialize()));

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new FloatFloatComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Visible Stream
	 * 
	 * @return The Visible Stream
	 */

	public org.drip.product.rates.FloatingStream getVisibleStream()
	{
		return _floatVisible;
	}

	/**
	 * Retrieve the Work-out Stream
	 * 
	 * @return The Work-out Stream
	 */

	public org.drip.product.rates.FloatingStream getWorkoutStream()
	{
		return _floatWorkout;
	}
}

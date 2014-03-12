
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
 * IRSComponent contains the implementation of the Interest Rate Swap product contract/valuation details. It
 *  exports the following functionality:
 *  - Standard/Custom Constructor for the IRSComponent
 *  - Dates: Effective, Maturity, Coupon dates and Product settlement Parameters
 *  - Coupon/Notional Outstanding as well as schedules
 *  - Retrieve the constituent fixed and floating streams
 *  - Market Parameters: Discount, Forward, Credit, Treasury, EDSF Curves
 *  - Cash Flow Periods: Coupon flows and (Optionally) Loss Flows
 *  - Valuation: Named Measure Generation
 *  - Calibration: The codes and constraints generation
 *  - Jacobians: Quote/DF and PV/DF micro-Jacobian generation
 *  - Serialization into and de-serialization out of byte arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class IRSComponent extends org.drip.product.definition.RatesComponent {
	private java.lang.String _strCode = "";
	private org.drip.product.rates.FixedStream _fixStream = null;
	private org.drip.product.rates.FloatingStream _floatStream = null;

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * Construct the IRSComponent from the fixed and the floating streams
	 * 
	 * @param fixStream Fixed Stream
	 * @param floatStream Floating Stream
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public IRSComponent (
		final org.drip.product.rates.FixedStream fixStream,
		final org.drip.product.rates.FloatingStream floatStream)
		throws java.lang.Exception
	{
		if (null == (_fixStream = fixStream) || null == (_floatStream = floatStream))
			throw new java.lang.Exception ("IRSComponent ctr: Invalid Inputs");
	}

	/**
	 * De-serialize the IRSComponent from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if the IRSComponent cannot be de-serialized from the byte array
	 */

	public IRSComponent (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Empty state");

		java.lang.String strSerializedInterestRateSwap = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedInterestRateSwap || strSerializedInterestRateSwap.isEmpty())
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedInterestRateSwap,
			getFieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]).doubleValue();

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Cannot locate fixed stream");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_fixStream = null;
		else
			_fixStream = new org.drip.product.rates.FixedStream (astrField[1].getBytes());

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("InterestRateSwap de-serializer: Cannot locate floating stream");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_floatStream = null;
		else
			_floatStream = new org.drip.product.rates.FloatingStream (astrField[2].getBytes());
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
		return "IRS=" + getMaturityDate();
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
		return _fixStream.getInitialNotional();
	}

	@Override public double getNotional (
		final double dblDate)
		throws java.lang.Exception
	{
		return _fixStream.getNotional (dblDate);
	}

	@Override public double getNotional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		return _fixStream.getNotional (dblDate1, dblDate2);
	}

	@Override public boolean setCurves (
		final java.lang.String strIR,
		final java.lang.String strIRTSY,
		final java.lang.String strCC)
	{
		return _fixStream.setCurves (strIR, strIRTSY, strCC) && _floatStream.setCurves (strIR, strIRTSY,
			strCC);
	}

	@Override public double getCoupon (
		final double dblValue,
		final org.drip.param.definition.ComponentMarketParams mktParams)
		throws java.lang.Exception
	{
		return _fixStream.getCoupon (dblValue, mktParams);
	}

	@Override public java.lang.String getIRCurveName()
	{
		return _floatStream.getIRCurveName();
	}

	@Override public java.lang.String getForwardCurveName()
	{
		return _floatStream.getForwardCurveName();
	}

	@Override public java.lang.String getCreditCurveName()
	{
		return "";
	}

	@Override public org.drip.analytics.date.JulianDate getEffectiveDate()
	{
		org.drip.analytics.date.JulianDate dtFixEffective = _fixStream.getEffectiveDate();

		org.drip.analytics.date.JulianDate dtFloatEffective = _floatStream.getEffectiveDate();

		if (null == dtFixEffective || null == dtFloatEffective) return null;

		return dtFixEffective.getJulian() < dtFloatEffective.getJulian() ? dtFixEffective : dtFloatEffective;
	}

	@Override public org.drip.analytics.date.JulianDate getMaturityDate()
	{
		org.drip.analytics.date.JulianDate dtFixMaturity = _fixStream.getMaturityDate();

		org.drip.analytics.date.JulianDate dtFloatMaturity = _floatStream.getMaturityDate();

		if (null == dtFixMaturity || null == dtFloatMaturity) return null;

		return dtFixMaturity.getJulian() > dtFloatMaturity.getJulian() ? dtFixMaturity : dtFloatMaturity;
	}

	@Override public org.drip.analytics.date.JulianDate getFirstCouponDate()
	{
		org.drip.analytics.date.JulianDate dtFixFirstCoupon = _fixStream.getFirstCouponDate();

		org.drip.analytics.date.JulianDate dtFloatFirstCoupon = _floatStream.getFirstCouponDate();

		if (null == dtFixFirstCoupon || null == dtFloatFirstCoupon) return null;

		return dtFixFirstCoupon.getJulian() < dtFloatFirstCoupon.getJulian() ? dtFixFirstCoupon :
			dtFloatFirstCoupon;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> getCashFlowPeriod()
	{
		return org.drip.analytics.support.AnalyticsHelper.MergePeriodLists (_fixStream.getCashFlowPeriod(),
			_floatStream.getCashFlowPeriod());
	}

	@Override public org.drip.param.valuation.CashSettleParams getCashSettleParams()
	{
		return _fixStream.getCashSettleParams();
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFixStreamResult =
			_fixStream.value (valParams, pricerParams, mktParams, quotingParams);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFloatStreamResult =
			_floatStream.value (valParams, pricerParams, mktParams, quotingParams);

		if (null == mapFixStreamResult || 0 == mapFixStreamResult.size() || null == mapFloatStreamResult || 0
			== mapFloatStreamResult.size())
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		double dblFixedCleanDV01 = mapFixStreamResult.get ("CleanDV01");

		double dblFloatingCleanPV = mapFloatStreamResult.get ("CleanPV");

		double dblCleanPV = mapFixStreamResult.get ("CleanPV") + dblFloatingCleanPV;

		double dblFairPremium = java.lang.Math.abs (0.0001 * dblFloatingCleanPV / dblFixedCleanDV01);

		mapResult.put ("CleanFixedDV01", dblFixedCleanDV01);

		mapResult.put ("CleanFixedPV", mapFixStreamResult.get ("CleanPV"));

		mapResult.put ("CleanFloatingDV01", mapFloatStreamResult.get ("CleanDV01"));

		mapResult.put ("CleanFloatingPV", dblFloatingCleanPV);

		mapResult.put ("CleanPV", dblCleanPV);

		mapResult.put ("DirtyFixedDV01", mapFixStreamResult.get ("DirtyDV01"));

		mapResult.put ("DirtyFixedPV", mapFixStreamResult.get ("DirtyPV"));

		mapResult.put ("DirtyFloatingDV01", mapFloatStreamResult.get ("DirtyDV01"));

		mapResult.put ("DirtyFloatingPV", mapFloatStreamResult.get ("DirtyPV"));

		mapResult.put ("DirtyPV", mapFixStreamResult.get ("DirtyPV") + mapFloatStreamResult.get ("DirtyPV"));

		mapResult.put ("FairPremium", dblFairPremium);

		mapResult.put ("FixedAccrued", mapFixStreamResult.get ("Accrued"));

		mapResult.put ("FixedAccrued01", mapFixStreamResult.get ("Accrued01"));

		mapResult.put ("FixedDV01", mapFixStreamResult.get ("DV01"));

		mapResult.put ("FloatAccrued", mapFloatStreamResult.get ("Accrued"));

		mapResult.put ("FloatAccrued01", mapFloatStreamResult.get ("Accrued01"));

		mapResult.put ("FloatDV01", mapFloatStreamResult.get ("DV01"));

		mapResult.put ("Fixing01", mapFloatStreamResult.get ("Fixing01"));

		mapResult.put ("ParRate", dblFairPremium);

		mapResult.put ("PV", mapFixStreamResult.get ("PV") + mapFloatStreamResult.get ("PV"));

		mapResult.put ("Rate", dblFairPremium);

		mapResult.put ("ResetDate", mapFloatStreamResult.get ("ResetDate"));

		mapResult.put ("ResetRate", mapFloatStreamResult.get ("ResetRate"));

		mapResult.put ("SwapRate", dblFairPremium);

		mapResult.put ("Upfront", mapFixStreamResult.get ("Upfront") + mapFloatStreamResult.get ("Upfront"));

		double dblValueNotional = java.lang.Double.NaN;

		try {
			dblValueNotional = getNotional (valParams.valueDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
				double dblCleanPrice = 100. * (1. + (dblCleanPV / getInitialNotional() / dblValueNotional));

				org.drip.analytics.rates.DiscountCurve dc = mktParams.getFundingCurve();

				mapResult.put ("Price", dblCleanPrice);

				mapResult.put ("CleanPrice", dblCleanPrice);

				double dblStartDate = getEffectiveDate().getJulian();

				mapResult.put ("CalibSwapRatePV", (dc.df (dblStartDate > valParams.valueDate() ? dblStartDate
					: valParams.valueDate()) - dc.df (getMaturityDate())));

				mapResult.put ("CalibSwapRate", (dc.df (dblStartDate > valParams.valueDate() ? dblStartDate :
					valParams.valueDate()) - dc.df (getMaturityDate())) / dblFixedCleanDV01 * getNotional
						(valParams.valueDate()) * 0.0001);
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

		setstrMeasureNames.add ("CleanFixedDV01");

		setstrMeasureNames.add ("CleanFixedPV");

		setstrMeasureNames.add ("CleanFloatingDV01");

		setstrMeasureNames.add ("CleanFloatingPV");

		setstrMeasureNames.add ("CleanPrice");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("DirtyFixedDV01");

		setstrMeasureNames.add ("DirtyFixedPV");

		setstrMeasureNames.add ("DirtyFloatingDV01");

		setstrMeasureNames.add ("DirtyFloatingPV");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("FairPremium");

		setstrMeasureNames.add ("FixAccrued");

		setstrMeasureNames.add ("FixAccrued01");

		setstrMeasureNames.add ("FixedDV01");

		setstrMeasureNames.add ("Fixing01");

		setstrMeasureNames.add ("FloatAccrued");

		setstrMeasureNames.add ("FloatAccrued01");

		setstrMeasureNames.add ("FloatDV01");

		setstrMeasureNames.add ("ParRate");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("Rate");

		setstrMeasureNames.add ("ResetDate");

		setstrMeasureNames.add ("ResetRate");

		setstrMeasureNames.add ("SwapRate");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value (valParams,
			pricerParams, mktParams, quotingParams);

		if (null == mapMeasures || !mapMeasures.containsKey ("SwapRate")) return null;

		double dblParSwapRate = mapMeasures.get ("SwapRate");

		org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasureFloating =
			_floatStream.jackDDirtyPVDManifestMeasure (valParams, pricerParams, mktParams, quotingParams);

		if (null == jackDDirtyPVDManifestMeasureFloating) return null;

		int iNumQuote = jackDDirtyPVDManifestMeasureFloating.numParameters();

		if (0 == iNumQuote) return null;

		org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasureFixed =
			_fixStream.jackDDirtyPVDManifestMeasure (valParams, pricerParams, mktParams, quotingParams);

		if (null == jackDDirtyPVDManifestMeasureFixed || iNumQuote !=
			jackDDirtyPVDManifestMeasureFixed.numParameters())
			return null;

		double dblNotionalScaleDown = java.lang.Double.NaN;
		org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasureIRS = null;

		if (null == jackDDirtyPVDManifestMeasureIRS) {
			try {
				dblNotionalScaleDown = 1. / getInitialNotional();

				jackDDirtyPVDManifestMeasureIRS = new org.drip.quant.calculus.WengertJacobian (1, iNumQuote);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		for (int i = 0; i < iNumQuote; ++i) {
			if (!jackDDirtyPVDManifestMeasureIRS.accumulatePartialFirstDerivative (0, i, dblNotionalScaleDown
				* (dblParSwapRate * jackDDirtyPVDManifestMeasureFixed.getFirstDerivative (0, i) +
					jackDDirtyPVDManifestMeasureFloating.getFirstDerivative (0, i))))
				return null;
		}

		return jackDDirtyPVDManifestMeasureIRS;
	}

	@Override public org.drip.quant.calculus.WengertJacobian calcQuoteDFMicroJack (
		final java.lang.String strQuote,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= getMaturityDate().getJulian() || null == strQuote ||
			null == mktParams || null == mktParams.getFundingCurve())
			return null;

		if ("Rate".equalsIgnoreCase (strQuote) || "SwapRate".equalsIgnoreCase (strQuote)) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value (valParams,
				pricerParams, mktParams, quotingParams);

			if (null == mapMeasures) return null;

			double dblDirtyDV01 = mapMeasures.get ("DirtyDV01");

			double dblParSwapRate = mapMeasures.get ("SwapRate");

			try {
				org.drip.quant.calculus.WengertJacobian wjSwapRateDFMicroJack = null;

				org.drip.analytics.rates.DiscountCurve dc = mktParams.getFundingCurve();

				for (org.drip.analytics.period.Period p : getCashFlowPeriod()) {
					double dblPeriodPayDate = p.getPayDate();

					if (dblPeriodPayDate < valParams.valueDate()) continue;

					org.drip.quant.calculus.WengertJacobian wjPeriodFwdRateDF =
						dc.jackDForwardDManifestMeasure (p.getStartDate(), p.getEndDate(), "Rate",
							p.getCouponDCF());

					org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF = dc.jackDDFDManifestMeasure
						(dblPeriodPayDate, "Rate");

					if (null == wjPeriodFwdRateDF || null == wjPeriodPayDFDF) continue;

					double dblForwardRate = dc.libor (p.getStartDate(), p.getEndDate());

					double dblPeriodPayDF = dc.df (dblPeriodPayDate);

					if (null == wjSwapRateDFMicroJack)
						wjSwapRateDFMicroJack = new org.drip.quant.calculus.WengertJacobian (1,
							wjPeriodFwdRateDF.numParameters());

					double dblPeriodNotional = getNotional (p.getStartDate(), p.getEndDate());

					double dblPeriodDCF = p.getCouponDCF();

					for (int k = 0; k < wjPeriodFwdRateDF.numParameters(); ++k) {
						double dblPeriodMicroJack = (dblForwardRate - dblParSwapRate) *
							wjPeriodPayDFDF.getFirstDerivative (0, k) + dblPeriodPayDF *
								wjPeriodFwdRateDF.getFirstDerivative (0, k);

						if (!wjSwapRateDFMicroJack.accumulatePartialFirstDerivative (0, k, dblPeriodNotional
							* dblPeriodDCF * dblPeriodMicroJack / dblDirtyDV01))
							return null;
					}
				}

				return wjSwapRateDFMicroJack;
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
		if (null == valParams || valParams.valueDate() >= getMaturityDate().getJulian() || null == lsmm ||
			!(lsmm instanceof org.drip.analytics.rates.RatesLSMM) ||
				!org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT.equalsIgnoreCase
					(lsmm.getID()))
			return null;

		org.drip.analytics.rates.RatesLSMM ratesLSMM = (org.drip.analytics.rates.RatesLSMM) lsmm;

		if (org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
			(ratesLSMM.getQuantificationMetric())) {
			if (org.drip.quant.common.StringUtil.MatchInStringArray (ratesLSMM.getManifestMeasures(), new
				java.lang.String[] {"Rate", "SwapRate", "ParRate", "ParSpread", "FairPremium"}, false)) {
				org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
					org.drip.state.estimator.PredictorResponseWeightConstraint();

				org.drip.analytics.rates.TurnListDiscountFactor tldf = ratesLSMM.turnsDiscount();

				try {
					for (org.drip.analytics.period.CashflowPeriod period : _fixStream.getCashFlowPeriod()) {
						double dblPeriodTurnDF = null == tldf ? 1. : tldf.turnAdjust (valParams.valueDate(),
							period.getPayDate());

						double dblPay01 = period.getCouponDCF() * dblPeriodTurnDF;

						if (null == period || !prlc.addPredictorResponseWeight (period.getPayDate(),
							ratesLSMM.getMeasureQuoteValue() * dblPay01) ||
								!prlc.addDResponseWeightDManifestMeasure ("Rate", period.getPayDate(),
									dblPay01))
							return null;
					}

					double dblMaturity = getMaturityDate().getJulian();

					double dblPeriodMaturityDF = null == tldf ? 1. : tldf.turnAdjust (valParams.valueDate(),
						dblMaturity);

					return prlc.addPredictorResponseWeight (valParams.valueDate(), -1.) &&
						prlc.addPredictorResponseWeight (dblMaturity, dblPeriodMaturityDF) &&
							prlc.addDResponseWeightDManifestMeasure ("Rate", valParams.valueDate(), 0.) &&
								prlc.addDResponseWeightDManifestMeasure ("Rate", dblMaturity, 0.) ? prlc :
									null;
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}
		}

		return null;
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

		if (null == _fixStream)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_fixStream.serialize()) + getFieldDelimiter());

		if (null == _floatStream)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_floatStream.serialize()));

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new IRSComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Fixed Stream
	 * 
	 * @return The Fixed Stream
	 */

	public org.drip.product.rates.FixedStream getFixedStream()
	{
		return _fixStream;
	}

	/**
	 * Retrieve the Floating Stream
	 * 
	 * @return The Floating Stream
	 */

	public org.drip.product.rates.FloatingStream getFloatStream()
	{
		return _floatStream;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.analytics.date.JulianDate dtEffective = org.drip.analytics.date.JulianDate.Today();

		org.drip.analytics.date.JulianDate dtMaturity = dtEffective.addTenor ("4Y");

		org.drip.product.rates.FixedStream fixStream = new org.drip.product.rates.FixedStream
			(dtEffective.getJulian(), dtMaturity.getJulian(), 0.05, 2, "30/360", "30/360", false, null, null,
				null, null, null, null, null, null, 100., "JPY", "JPY");

		org.drip.product.rates.FloatingStream floatStream = new org.drip.product.rates.FloatingStream
			(dtEffective.getJulian(), dtMaturity.getJulian(), 0.01, true,
				org.drip.product.params.FloatingRateIndex.Create ("JPY-LIBOR-3M"), 4, "Act/360", "Act/360",
					false, null, null, null, null, null, null, null, null, null, -100., "JPY", "JPY");

		IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream, floatStream);

		byte[] abIRS = irs.serialize();

		System.out.println (new java.lang.String (abIRS));

		IRSComponent irsDeser = new IRSComponent (abIRS);

		System.out.println (new java.lang.String (irsDeser.serialize()));
	}
}

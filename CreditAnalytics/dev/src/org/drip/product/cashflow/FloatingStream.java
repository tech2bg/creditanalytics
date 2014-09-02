
package org.drip.product.cashflow;

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
 * FloatingStream contains an implementation of the Floating leg cash flow stream. It exports the following
 *  functionality:
 *  - Standard/Custom Constructor for the FloatingStream Component
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

public class FloatingStream extends org.drip.product.definition.CalibratableFixedIncomeComponent {
	private static final boolean s_bBlog = false;

	private double _dblNotional = 1.;
	private double _dblSpread = 0.0001;
	private boolean _bIsReference = true;
	private java.lang.String _strCode = "";
	private java.lang.String _strCurrency = "";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.product.params.FXMTMSetting _fxmtm = null;
	private org.drip.state.identifier.ForwardLabel _fri = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;
	private java.util.List<org.drip.analytics.period.CouponPeriod> _lsCouponPeriod = null;

	private org.drip.state.estimator.PredictorResponseWeightConstraint unloadedPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == csqs || null == pqs || !(pqs instanceof
			org.drip.product.calib.FloatingStreamQuoteSet))
			return null;

		double dblValueDate = valParams.valueDate();

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (_strCurrency));

		if (dblValueDate >= _dblMaturity || null == dcFunding) return null;

		double dblPV = 0.;
		double dblSpread = _dblSpread;
		org.drip.product.calib.FloatingStreamQuoteSet fsqs = (org.drip.product.calib.FloatingStreamQuoteSet)
			pqs;

		try {
			if (fsqs.containsSpread()) dblSpread = fsqs.spread();

			if (fsqs.containsPV()) dblPV = fsqs.pv();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			try {
				double dblAccrued = period.contains (dblValueDate) ? period.accrualDCF (dblValueDate) : 0.;

				double dblPeriodCV100 = _dblNotional * notional (dblPeriodEndDate) * (period.couponDCF() -
					dblAccrued) * dcFunding.df (period.payDate());

				org.drip.analytics.output.CouponPeriodMetrics pcm = coupon (dblPeriodEndDate, valParams,
					csqs);

				if (null == pcm) return null;

				dblPV -= dblPeriodCV100 * (pcm.nominalAccrualRate() + dblSpread);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (!prwc.updateValue (dblPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * FloatingStream constructor
	 * 
	 * @param strCurrency Cash Flow Currency
	 * @param fxmtm FX MTM setting
	 * @param dblSpread Spread
	 * @param dblNotional Initial Notional Amount
	 * @param notlSchedule Notional Schedule
	 * @param lsCouponPeriod List of the Coupon Periods
	 * @param fri Floating Rate Index
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public FloatingStream (
		final java.lang.String strCurrency,
		final org.drip.product.params.FXMTMSetting fxmtm,
		final double dblSpread,
		final double dblNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final java.util.List<org.drip.analytics.period.CouponPeriod> lsCouponPeriod,
		final org.drip.state.identifier.ForwardLabel fri,
		final boolean bIsReference)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblSpread = dblSpread) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) || 0. == _dblNotional
					|| null == (_lsCouponPeriod = lsCouponPeriod) || null == (_fri = fri))
			throw new java.lang.Exception ("FloatingStream ctr => Invalid Input params!");

		int iNumPeriod = _lsCouponPeriod.size();

		if (0 == iNumPeriod) throw new java.lang.Exception ("FloatingStream ctr => Invalid Input params!");

		_fxmtm = fxmtm;
		_bIsReference = bIsReference;

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();

		_dblEffective = _lsCouponPeriod.get (0).startDate();

		_dblMaturity = _lsCouponPeriod.get (iNumPeriod - 1).endDate();

		for (org.drip.analytics.period.CouponPeriod cp : _lsCouponPeriod) {
			if (!cp.setBaseNotional (dblNotional) || !cp.setNotionalSchedule (_notlSchedule) || !cp.setSpread
				(_dblSpread) || (null != _fxmtm && !_fxmtm.mtmMode() && !cp.setFXFixingDate (_dblEffective)))
				throw new java.lang.Exception ("FixedStream ctr: Cannot set Spread!");
		}

		_strCode = _strCurrency + "::" + _fri.fullyQualifiedName() + "::" + new
			org.drip.analytics.date.JulianDate (_dblMaturity);
	}

	/**
	 * FloatingStream de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if FloatingStream cannot be properly de-serialized
	 */

	public FloatingStream (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("FloatingStream de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Empty state");

		java.lang.String strSerializedFloatingStream = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedFloatingStream || strSerializedFloatingStream.isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedFloatingStream,
			fieldDelimiter());

		if (null == astrField || 12 > astrField.length)
			throw new java.lang.Exception ("FloatingStream de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate notional");

		_dblNotional = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate coupon");

		_dblSpread = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate IR curve name");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strCurrency = astrField[3];
		else
			_strCurrency = "";

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate code");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strCode = astrField[4];
		else
			_strCode = "";

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate maturity date");

		_dblMaturity = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate effective date");

		_dblEffective = new java.lang.Double (astrField[6]);

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate rate index");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			_fri = null;
		else
			_fri = new org.drip.state.identifier.ForwardLabel (astrField[7].getBytes());

		if (null == astrField[8] || astrField[8].isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate notional schedule");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			_notlSchedule = null;
		else
			_notlSchedule = new org.drip.product.params.FactorSchedule (astrField[8].getBytes());

		if (null == astrField[9] || astrField[9].isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate cash settle params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[9]))
			_settleParams = null;
		else
			_settleParams = new org.drip.param.valuation.CashSettleParams (astrField[9].getBytes());

		if (null == astrField[10] || astrField[10].isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate the periods");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[10]))
			_lsCouponPeriod = null;
		else {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[10],
				collectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					if (null == _lsCouponPeriod)
						_lsCouponPeriod = new java.util.ArrayList<org.drip.analytics.period.CouponPeriod>();

					_lsCouponPeriod.add (new org.drip.analytics.period.CouponPeriod
						(astrRecord[i].getBytes()));
				}
			}
		}

		if (null == astrField[11] || astrField[11].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[11]))
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate the reference flag");

		_bIsReference = new java.lang.Boolean (astrField[11]);
	}

	@Override public java.lang.String primaryCode()
	{
		return _strCode;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	@Override public java.lang.String name()
	{
		return _strCode;
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
			throw new java.lang.Exception ("FloatingStream::notional => Bad date into getNotional");

		return _notlSchedule.getFactor (dblDate);
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2))
			throw new java.lang.Exception ("FloatingStream::notional => Bad date into getNotional");

		return _notlSchedule.getFactor (dblDate1, dblDate2);
	}

	@Override public org.drip.analytics.output.CouponPeriodMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualEndDate) || null == csqs) return null;

		org.drip.analytics.period.CouponPeriod currentPeriod = null;

		if (dblAccrualEndDate <= _dblEffective)
			currentPeriod = _lsCouponPeriod.get (0);
		else {
			for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
				if (null == period) continue;

				if (dblAccrualEndDate >= period.startDate() && dblAccrualEndDate <= period.endDate()) {
					currentPeriod = period;
					break;
				}
			}
		}

		return null == currentPeriod ? null : currentPeriod.baseMetrics (valParams.valueDate(), csqs);
	}

	@Override public int freq()
	{
		return cashFlowPeriod().get (0).freq();
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		return new org.drip.state.identifier.ForwardLabel[] {_fri};
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		return null;
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		if (null == _fxmtm) return null;

		org.drip.product.params.CurrencyPair cp = _fxmtm.currencyPair();

		return null == cp ? null : new org.drip.state.identifier.FXLabel[]
			{org.drip.state.identifier.FXLabel.Standard (cp)};
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
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).endDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.util.List<org.drip.analytics.period.CouponPeriod> cashFlowPeriod()
	{
		return _lsCouponPeriod;
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _settleParams;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == csqs) return null;

		java.lang.String strCurrency = couponCurrency()[0];

		double dblFixing01 = 0.;
		double dblAccrued01 = 0.;
		double dblTotalCoupon = 0.;
		boolean bFirstPeriod = true;
		double dblUnadjustedDirtyPV = 0.;
		double dblUnadjustedDirtyDV01 = 0.;
		double dblConvexityAdjustedDirtyPV = 0.;
		double dblConvexityAdjustedDirtyDV01 = 0.;
		double dblCompoundingAdjustedDirtyPV = 0.;
		double dblCompoundingAdjustedDirtyDV01 = 0.;
		double dblCashPayDF = java.lang.Double.NaN;
		double dblResetDate = java.lang.Double.NaN;
		double dblResetRate = java.lang.Double.NaN;
		double dblValueNotional = java.lang.Double.NaN;
		org.drip.quant.function1D.AbstractUnivariate auFX = null;

		org.drip.state.identifier.FundingLabel fundingLabel = org.drip.state.identifier.FundingLabel.Standard
			(strCurrency);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel);

		if (null == dcFunding) return null;

		long lStart = System.nanoTime();

		double dblValueDate = valParams.valueDate();

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblFloatingRate = java.lang.Double.NaN;
			double dblUnadjustedDirtyPeriodDV01 = java.lang.Double.NaN;
			double dblCompoundingAdjustedDirtyPeriodDV01 = java.lang.Double.NaN;

			double dblPeriodStartDate = period.startDate();

			double dblPeriodEndDate = period.endDate();

			double dblPeriodPayDate = period.payDate();

			double dblPeriodDCF = period.couponDCF();

			if (dblPeriodPayDate < dblValueDate) continue;

			org.drip.analytics.output.CouponPeriodMetrics pcm = period.baseMetrics (dblValueDate, csqs);

			if (null == pcm) return null;

			org.drip.analytics.output.ConvexityAdjustment convAdj = pcm.convexityAdjustment();

			if (null == convAdj) return null;

			try {
				dblFloatingRate = pcm.nominalAccrualRate();

				if (bFirstPeriod) {
					bFirstPeriod = false;
					dblResetRate = dblFloatingRate;

					dblResetDate = pcm.resetPeriodMetrics().get (0).fixing();

					org.drip.analytics.output.CouponAccrualMetrics cam = period.accrualMetrics (dblValueDate,
						csqs);

					if (null != cam) {
						dblResetDate = cam.currentResetDate();

						dblResetRate = cam.nominalAccrualRate();

						dblFixing01 = 0.0001 * cam.dcf() * cam.notionalFactor() * cam.fx();
					}

					if (dblPeriodStartDate < dblValueDate) dblAccrued01 = dblFixing01;
				}

				dblUnadjustedDirtyPeriodDV01 = 0.0001 * dblPeriodDCF * pcm.annuity();

				dblCompoundingAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
					pcm.compoundingConvexityFactor();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (s_bBlog) {
				try {
					System.out.println (new org.drip.analytics.date.JulianDate (dblResetDate) + " [" + new
						org.drip.analytics.date.JulianDate (dblPeriodStartDate) + "->" + new
							org.drip.analytics.date.JulianDate (dblPeriodEndDate) + "] => " +
								org.drip.quant.common.FormatUtil.FormatDouble (dblFloatingRate, 1, 4, 100.));
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
			}

			double dblConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 * convAdj.cumulative();

			dblTotalCoupon += dblFloatingRate;
			dblUnadjustedDirtyDV01 += dblUnadjustedDirtyPeriodDV01;
			dblUnadjustedDirtyPV += dblUnadjustedDirtyPeriodDV01 * 10000. * (dblFloatingRate + _dblSpread);
			dblConvexityAdjustedDirtyDV01 += dblConvexityAdjustedDirtyPeriodDV01;
			dblConvexityAdjustedDirtyPV += dblConvexityAdjustedDirtyPeriodDV01 * 10000. * (dblFloatingRate +
				_dblSpread);
			dblCompoundingAdjustedDirtyDV01 += dblCompoundingAdjustedDirtyPeriodDV01;
			dblCompoundingAdjustedDirtyPV += dblCompoundingAdjustedDirtyPeriodDV01 * 10000. *
				(dblFloatingRate + _dblSpread);
		}

		try {
			double dblCashSettle = valParams.cashPayDate();

			if (null != _settleParams) dblCashSettle = _settleParams.cashSettleDate (dblValueDate);

			dblCashPayDF = dcFunding.df (dblCashSettle);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		dblUnadjustedDirtyPV /= dblCashPayDF;
		dblUnadjustedDirtyDV01 /= dblCashPayDF;
		dblConvexityAdjustedDirtyPV /= dblCashPayDF;
		dblConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		dblCompoundingAdjustedDirtyPV /= dblCashPayDF;
		dblCompoundingAdjustedDirtyDV01 /= dblCashPayDF;
		double dblAccrued = dblAccrued01 * 10000. * (dblResetRate + _dblSpread);
		double dblUnadjustedCleanPV = dblUnadjustedDirtyPV - dblAccrued;
		double dblUnadjustedCleanDV01 = dblUnadjustedDirtyDV01 - dblAccrued01;
		double dblUnadjustedFairPremium = 0.0001 * dblUnadjustedCleanPV / dblUnadjustedCleanDV01;
		double dblCompoundingAdjustedCleanPV = dblCompoundingAdjustedDirtyPV - dblAccrued;
		double dblCompoundingAdjustedCleanDV01 = dblCompoundingAdjustedDirtyDV01 - dblAccrued01;
		double dblCompoundingAdjustedFairPremium = 0.0001 * dblCompoundingAdjustedCleanPV /
			dblCompoundingAdjustedCleanDV01;
		double dblConvexityAdjustedCleanPV = dblConvexityAdjustedDirtyPV - dblAccrued;
		double dblConvexityAdjustedCleanDV01 = dblConvexityAdjustedDirtyDV01 - dblAccrued01;
		double dblConvexityAdjustedFairPremium = 0.0001 * dblConvexityAdjustedCleanPV /
			dblConvexityAdjustedCleanDV01;

		try {
			dblValueNotional = notional (dblValueDate) * _dblNotional * (null != auFX && null != _fxmtm &&
				!_fxmtm.mtmMode() ? auFX.evaluate (dblValueDate) : 1.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put ("Accrued", dblAccrued);

		mapResult.put ("Accrued01", dblAccrued01);

		mapResult.put ("CleanDV01", dblConvexityAdjustedCleanDV01);

		mapResult.put ("CleanPV", dblConvexityAdjustedCleanPV);

		mapResult.put ("CompoundingAdjustedCleanDV01", dblCompoundingAdjustedCleanDV01);

		mapResult.put ("CompoundingAdjustedCleanPV", dblCompoundingAdjustedCleanPV);

		mapResult.put ("CompoundingAdjustedDirtyPV", dblCompoundingAdjustedDirtyPV);

		mapResult.put ("CompoundingAdjustedDirtyDV01", dblCompoundingAdjustedDirtyDV01);

		mapResult.put ("CompoundingAdjustedDirtyPV", dblCompoundingAdjustedDirtyPV);

		mapResult.put ("CompoundingAdjustedFairPremium", dblCompoundingAdjustedFairPremium);

		mapResult.put ("CompoundingAdjustedParRate", dblCompoundingAdjustedFairPremium);

		mapResult.put ("CompoundingAdjustedPV", dblCompoundingAdjustedCleanPV);

		mapResult.put ("CompoundingAdjustedRate", dblCompoundingAdjustedFairPremium);

		mapResult.put ("CompoundingAdjustedUpfront", dblCompoundingAdjustedCleanPV);

		mapResult.put ("CompoundingAdjustmentFactor", dblCompoundingAdjustedDirtyDV01 /
			dblUnadjustedDirtyDV01);

		mapResult.put ("CompoundingAdjustmentPremiumUpfront", (dblCompoundingAdjustedCleanPV -
			dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("ConvexityAdjustedCleanDV01", dblConvexityAdjustedCleanDV01);

		mapResult.put ("ConvexityAdjustedCleanPV", dblConvexityAdjustedCleanPV);

		mapResult.put ("ConvexityAdjustedDirtyPV", dblConvexityAdjustedDirtyPV);

		mapResult.put ("ConvexityAdjustedDirtyDV01", dblConvexityAdjustedDirtyDV01);

		mapResult.put ("ConvexityAdjustedDirtyPV", dblConvexityAdjustedDirtyPV);

		mapResult.put ("ConvexityAdjustedFairPremium", dblConvexityAdjustedFairPremium);

		mapResult.put ("ConvexityAdjustedParRate", dblConvexityAdjustedFairPremium);

		mapResult.put ("ConvexityAdjustedPV", dblConvexityAdjustedCleanPV);

		mapResult.put ("ConvexityAdjustedRate", dblConvexityAdjustedFairPremium);

		mapResult.put ("ConvexityAdjustedUpfront", dblConvexityAdjustedCleanPV);

		mapResult.put ("ConvexityAdjustmentFactor", dblConvexityAdjustedDirtyDV01 / dblUnadjustedDirtyDV01);

		mapResult.put ("ConvexityAdjustmentPremiumUpfront", (dblConvexityAdjustedCleanPV -
			dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("CV01", dblConvexityAdjustedCleanDV01);

		mapResult.put ("DirtyDV01", dblConvexityAdjustedDirtyDV01);

		mapResult.put ("DirtyPV", dblConvexityAdjustedDirtyPV);

		mapResult.put ("DV01", dblConvexityAdjustedCleanDV01);

		mapResult.put ("FairPremium", dblConvexityAdjustedFairPremium);

		mapResult.put ("Fixing01", dblFixing01 / dblCashPayDF);

		mapResult.put ("ParRate", dblConvexityAdjustedFairPremium);

		mapResult.put ("PV", dblConvexityAdjustedCleanPV);

		mapResult.put ("Rate", dblConvexityAdjustedFairPremium);

		mapResult.put ("ResetDate", dblResetDate);

		mapResult.put ("ResetRate", dblResetRate);

		mapResult.put ("TotalCoupon", dblTotalCoupon);

		mapResult.put ("UnadjustedCleanDV01", dblUnadjustedCleanDV01);

		mapResult.put ("UnadjustedCleanPV", dblUnadjustedCleanPV);

		mapResult.put ("UnadjustedDirtyDV01", dblUnadjustedDirtyDV01);

		mapResult.put ("UnadjustedDirtyPV", dblUnadjustedDirtyPV);

		mapResult.put ("UnadjustedFairPremium", dblUnadjustedFairPremium);

		mapResult.put ("UnadjustedParRate", dblUnadjustedFairPremium);

		mapResult.put ("UnadjustedPV", dblUnadjustedCleanPV);

		mapResult.put ("UnadjustedRate", dblUnadjustedFairPremium);

		mapResult.put ("UnadjustedUpfront", dblUnadjustedCleanPV);

		mapResult.put ("Upfront", dblConvexityAdjustedCleanPV);

		if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
			double dblUnadjustedPrice = 100. * (1. + (dblUnadjustedCleanPV / dblValueNotional));
			double dblCompoundingAdjustedPrice = 100. * (1. + (dblCompoundingAdjustedCleanPV /
				dblValueNotional));
			double dblConvexityAdjustedPrice = 100. * (1. + (dblConvexityAdjustedCleanPV /
				dblValueNotional));

			mapResult.put ("CleanPrice", dblConvexityAdjustedPrice);

			mapResult.put ("CompoundingAdjustedCleanPrice", dblCompoundingAdjustedPrice);

			mapResult.put ("CompoundingAdjustedDirtyPrice", 100. * (1. + (dblCompoundingAdjustedDirtyPV /
				dblValueNotional)));

			mapResult.put ("CompoundingAdjustedPrice", dblCompoundingAdjustedPrice);

			mapResult.put ("ConvexityAdjustedCleanPrice", dblConvexityAdjustedPrice);

			mapResult.put ("ConvexityAdjustedDirtyPrice", 100. * (1. + (dblConvexityAdjustedDirtyPV /
				dblValueNotional)));

			mapResult.put ("ConvexityAdjustedPrice", dblConvexityAdjustedPrice);

			mapResult.put ("DirtyPrice", 100. * (1. + (dblConvexityAdjustedDirtyPV / dblValueNotional)));

			mapResult.put ("Price", dblConvexityAdjustedPrice);

			mapResult.put ("UnadjustedCleanPrice", dblUnadjustedPrice);

			mapResult.put ("UnadjustedDirtyPrice", 100. * (1. + (dblUnadjustedDirtyPV / dblValueNotional)));

			mapResult.put ("UnadjustedPrice", dblUnadjustedPrice);
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("Accrued01");

		setstrMeasureNames.add ("Accrued");

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("CleanDV01");

		setstrMeasureNames.add ("CleanPrice");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("CouponConvexityPremiumUpfront");

		setstrMeasureNames.add ("CV01");

		setstrMeasureNames.add ("DirtyDV01");

		setstrMeasureNames.add ("DirtyPrice");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("DV01");

		setstrMeasureNames.add ("FairPremium");

		setstrMeasureNames.add ("Fixing01");

		setstrMeasureNames.add ("ParRate");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("QuantoAdjustedCleanDV01");

		setstrMeasureNames.add ("QuantoAdjustedCleanPrice");

		setstrMeasureNames.add ("QuantoAdjustedCleanPV");

		setstrMeasureNames.add ("QuantoAdjustedConvexCleanPrice");

		setstrMeasureNames.add ("QuantoAdjustedConvexCleanPV");

		setstrMeasureNames.add ("QuantoAdjustedConvexDirtyPrice");

		setstrMeasureNames.add ("QuantoAdjustedConvexDirtyPV");

		setstrMeasureNames.add ("QuantoAdjustedConvexFairPremium");

		setstrMeasureNames.add ("QuantoAdjustedConvexParRate");

		setstrMeasureNames.add ("QuantoAdjustedDirtyDV01");

		setstrMeasureNames.add ("QuantoAdjustedDirtyPrice");

		setstrMeasureNames.add ("QuantoAdjustedDirtyPV");

		setstrMeasureNames.add ("QuantoAdjustedFairPremium");

		setstrMeasureNames.add ("QuantoAdjustedParRate");

		setstrMeasureNames.add ("QuantoAdjustedPrice");

		setstrMeasureNames.add ("QuantoAdjustedPV");

		setstrMeasureNames.add ("QuantoAdjustedRate");

		setstrMeasureNames.add ("QuantoAdjustedUpfront");

		setstrMeasureNames.add ("QuantoAdjustmentFactor");

		setstrMeasureNames.add ("QuantoAdjustmentPremiumUpfront");

		setstrMeasureNames.add ("Rate");

		setstrMeasureNames.add ("ResetDate");

		setstrMeasureNames.add ("ResetRate");

		setstrMeasureNames.add ("UnadjustedCleanDV01");

		setstrMeasureNames.add ("UnadjustedCleanPrice");

		setstrMeasureNames.add ("UnadjustedCleanPV");

		setstrMeasureNames.add ("UnadjustedConvexCleanPrice");

		setstrMeasureNames.add ("UnadjustedConvexCleanPV");

		setstrMeasureNames.add ("UnadjustedConvexDirtyPrice");

		setstrMeasureNames.add ("UnadjustedConvexDirtyPV");

		setstrMeasureNames.add ("UnadjustedConvexFairPremium");

		setstrMeasureNames.add ("UnadjustedConvexParRate");

		setstrMeasureNames.add ("UnadjustedDirtyDV01");

		setstrMeasureNames.add ("UnadjustedDirtyPrice");

		setstrMeasureNames.add ("UnadjustedDirtyPV");

		setstrMeasureNames.add ("UnadjustedFairPremium");

		setstrMeasureNames.add ("UnadjustedParRate");

		setstrMeasureNames.add ("UnadjustedPrice");

		setstrMeasureNames.add ("UnadjustedPV");

		setstrMeasureNames.add ("UnadjustedRate");

		setstrMeasureNames.add ("UnadjustedUpfront");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	@Override public org.drip.product.calib.ProductQuoteSet calibQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
	{
		try {
			return new org.drip.product.calib.FloatingStreamQuoteSet (aLSS);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.FloatingStreamQuoteSet) || !pqs.contains
				(org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
					org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0])))
			return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblMaturity) return null;

		double dblPV = 0.;
		double dblSpread = _dblSpread;
		org.drip.product.calib.FloatingStreamQuoteSet fsqs = (org.drip.product.calib.FloatingStreamQuoteSet)
			pqs;

		try {
			if (fsqs.containsSpread()) dblSpread = fsqs.spread();

			if (fsqs.containsPV()) dblPV = fsqs.pv();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			try {
				org.drip.analytics.output.CouponPeriodMetrics pcm = coupon (dblPeriodEndDate, valParams,
					csqs);

				if (null == pcm) return null;

				double dblAccrued = period.contains (dblValueDate) ? period.accrualDCF (dblValueDate) : 0.;

				double dblPeriodCV100 = _dblNotional * notional (dblPeriodEndDate) * (period.couponDCF() -
					dblAccrued) * (pcm.nominalAccrualRate() + dblSpread);

				double dblPeriodPayDate = period.payDate();

				if (!prwc.addPredictorResponseWeight (dblPeriodPayDate, dblPeriodCV100)) return null;

				if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblPeriodPayDate, dblPeriodCV100))
					return null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (!prwc.updateValue (dblPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == csqs || null == pqs || !(pqs instanceof
			org.drip.product.calib.FloatingStreamQuoteSet))
			return null;

		if (!pqs.contains (org.drip.analytics.rates.ForwardCurve.LATENT_STATE_FORWARD,
			org.drip.analytics.rates.ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE, _fri))
			return unloadedPRWC (valParams, pricerParams, csqs, quotingParams, pqs);

		double dblValueDate = valParams.valueDate();

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (_strCurrency));

		if (dblValueDate >= _dblMaturity || null == dcFunding) return null;

		double dblPV = 0.;
		double dblSpread = _dblSpread;
		org.drip.product.calib.FloatingStreamQuoteSet fsqs = (org.drip.product.calib.FloatingStreamQuoteSet)
			pqs;

		try {
			if (fsqs.containsSpread()) dblSpread = fsqs.spread();

			if (fsqs.containsPV()) dblPV = fsqs.pv();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			try {
				double dblAccrued = period.contains (dblValueDate) ? period.accrualDCF (dblValueDate) : 0.;

				double dblPeriodCV100 = _dblNotional * notional (dblPeriodEndDate) * (period.couponDCF() -
					dblAccrued) * dcFunding.df (period.payDate());

				dblPV -= dblPeriodCV100 * dblSpread;

				if (!prwc.addPredictorResponseWeight (dblPeriodEndDate, dblPeriodCV100)) return null;

				if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblPeriodEndDate, dblPeriodCV100))
					return null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (!prwc.updateValue (dblPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.FloatingStreamQuoteSet) || !pqs.contains
				(org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
					org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						org.drip.state.identifier.FundingLabel.Standard (_strCurrency)) ||
							!pqs.contains (org.drip.analytics.rates.ForwardCurve.LATENT_STATE_FORWARD,
								org.drip.analytics.rates.ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE,
									_fri))
			return unloadedPRWC (valParams, pricerParams, csqs, quotingParams, pqs);

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblMaturity) return null;

		double dblPV = 0.;
		boolean bFirstPeriod = true;
		double dblSpread = _dblSpread;
		double dblTerminalNotional = java.lang.Double.NaN;
		org.drip.product.calib.FloatingStreamQuoteSet fsqs = (org.drip.product.calib.FloatingStreamQuoteSet)
			pqs;

		try {
			if (fsqs.containsSpread()) dblSpread = fsqs.spread();

			if (fsqs.containsPV()) dblPV = fsqs.pv();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			double dblPeriodStartDate = period.startDate();

			double dblAccrued = 0.;

			try {
				double dblPeriodNotional = _dblNotional * notional (dblPeriodEndDate);

				if (bFirstPeriod) {
					bFirstPeriod = false;
					double dblDFDate = dblPeriodStartDate > dblValueDate ? dblPeriodStartDate : dblValueDate;

					dblAccrued = period.accrualDCF (dblValueDate);

					if (!prwc.addPredictorResponseWeight (dblDFDate, dblPeriodNotional)) return null;

					if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblDFDate, dblPeriodNotional))
						return null;
				}

				dblPV -= _dblNotional * notional (dblPeriodEndDate) * (period.couponDCF() - dblAccrued) *
					dblSpread;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			dblTerminalNotional = _dblNotional * notional (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		if (!prwc.addPredictorResponseWeight (_dblMaturity, -1. * dblTerminalNotional)) return null;

		if (!prwc.addDResponseWeightDManifestMeasure ("PV", _dblMaturity, -1. * dblTerminalNotional))
			return null;

		if (!prwc.updateValue (dblPV))return null; 

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.))return null; 

		if (!prwc.addMergeLabel (_fri)) return null;

		return prwc;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == csqs) return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0]));

		if (null == dcFunding) return null;

		try {
			org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure = null;

			for (org.drip.analytics.period.CouponPeriod p : _lsCouponPeriod) {
				double dblPeriodPayDate = p.payDate();

				if (p.startDate() < valParams.valueDate()) continue;

				org.drip.quant.calculus.WengertJacobian wjDForwardDManifestMeasure =
					dcFunding.jackDForwardDManifestMeasure (p.startDate(), p.endDate(), "Rate",
						p.couponDCF());

				if (null == wjDForwardDManifestMeasure) continue;

				int iNumQuote = wjDForwardDManifestMeasure.numParameters();

				if (0 == iNumQuote) continue;

				org.drip.quant.calculus.WengertJacobian wjDPayDFDManifestMeasure =
					dcFunding.jackDDFDManifestMeasure (dblPeriodPayDate, "Rate");

				if (null == wjDPayDFDManifestMeasure || iNumQuote !=
					wjDPayDFDManifestMeasure.numParameters())
					continue;

				double dblForward = dcFunding.libor (p.startDate(), p.endDate());

				double dblPayDF = dcFunding.df (dblPeriodPayDate);

				if (null == jackDDirtyPVDManifestMeasure)
					jackDDirtyPVDManifestMeasure = new org.drip.quant.calculus.WengertJacobian (1,
						iNumQuote);

				double dblPeriodNotional = _dblNotional * notional (p.startDate(), p.endDate());

				double dblPeriodDCF = p.couponDCF();

				for (int i = 0; i < iNumQuote; ++i) {
					double dblDCashflowPVDManifestMeasurei = dblPeriodDCF * (dblForward *
						wjDPayDFDManifestMeasure.getFirstDerivative (0, i) + dblPayDF *
							wjDForwardDManifestMeasure.getFirstDerivative (0, i));

					if (!jackDDirtyPVDManifestMeasure.accumulatePartialFirstDerivative (0, i,
						dblPeriodNotional * dblDCashflowPVDManifestMeasurei))
						return null;
				}
			}

			return jackDDirtyPVDManifestMeasure;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == strManifestMeasure)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0]));

		if (null == dcFunding) return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure) || "SwapRate".equalsIgnoreCase (strManifestMeasure))
		{
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, csqs, quotingParams);

			if (null == mapMeasures) return null;

			double dblDirtyDV01 = mapMeasures.get ("DirtyDV01");

			double dblParSwapRate = mapMeasures.get ("SwapRate");

			try {
				org.drip.quant.calculus.WengertJacobian wjSwapRateDFMicroJack = null;

				for (org.drip.analytics.period.CouponPeriod p : _lsCouponPeriod) {
					double dblPeriodPayDate = p.payDate();

					if (dblPeriodPayDate < valParams.valueDate()) continue;

					org.drip.quant.calculus.WengertJacobian wjPeriodFwdRateDF =
						dcFunding.jackDForwardDManifestMeasure (p.startDate(), p.endDate(), "Rate",
							p.couponDCF());

					org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF =
						dcFunding.jackDDFDManifestMeasure (dblPeriodPayDate, "Rate");

					if (null == wjPeriodFwdRateDF || null == wjPeriodPayDFDF) continue;

					double dblForwardRate = dcFunding.libor (p.startDate(), p.endDate());

					double dblPeriodPayDF = dcFunding.df (dblPeriodPayDate);

					if (null == wjSwapRateDFMicroJack)
						wjSwapRateDFMicroJack = new org.drip.quant.calculus.WengertJacobian (1,
							wjPeriodFwdRateDF.numParameters());

					double dblPeriodNotional = notional (p.startDate(), p.endDate());

					double dblPeriodDCF = p.couponDCF();

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

	@Override public java.lang.String fieldDelimiter()
	{
		return "!";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "&";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		sb.append (_dblNotional + fieldDelimiter());

		sb.append (_dblSpread + fieldDelimiter());

		if (null == _strCurrency || _strCurrency.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCurrency + fieldDelimiter());

		if (null == _strCode || _strCode.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCode + fieldDelimiter());

		sb.append (_dblMaturity + fieldDelimiter());

		sb.append (_dblEffective + fieldDelimiter());

		if (null == _fri)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_fri.serialize()) + fieldDelimiter());

		if (null == _notlSchedule)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_notlSchedule.serialize()) + fieldDelimiter());

		if (null == _settleParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_settleParams.serialize()) + fieldDelimiter());

		if (null == _lsCouponPeriod)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbPeriods = new java.lang.StringBuffer();

			for (org.drip.analytics.period.CouponPeriod p : _lsCouponPeriod) {
				if (null == p) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbPeriods.append (collectionRecordDelimiter());

				sbPeriods.append (new java.lang.String (p.serialize()));
			}

			if (sbPeriods.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
			else
				sb.append (sbPeriods.toString());
		}

		sb.append (fieldDelimiter() + _bIsReference);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new FloatingStream (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Floating Rate Index
	 * 
	 * @return The Floating Rate Index
	 */

	public org.drip.state.identifier.ForwardLabel fri()
	{
		return _fri;
	}

	/**
	 * Retrieve the Stream Spread
	 *  
	 * @return The Spread
	 */

	public double spread()
	{
		return _dblSpread;
	}

	/**
	 * Is this a Reference Stream?
	 *  
	 * @return TRUE => This is a Reference Stream
	 */

	public boolean reference()
	{
		return _bIsReference;
	}
}

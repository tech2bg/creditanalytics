
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
 * FixedStream contains an implementation of the Fixed leg cash flow stream. It exports the following
 * functionality:
 *  - Standard/Custom Constructor for the FixedStream Component
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

public class FixedStream extends org.drip.product.definition.CalibratableFixedIncomeComponent {
	private org.drip.param.valuation.CashSettleParams _settleParams = null;
	private java.util.List<org.drip.analytics.period.CouponPeriod> _lsCouponPeriod = null;

	private org.drip.state.estimator.PredictorResponseWeightConstraint unloadedPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		double dblValueDate = valParams.valueDate();

		org.drip.analytics.period.CouponPeriod cpFinal = _lsCouponPeriod.get (_lsCouponPeriod.size() - 1);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (cpFinal.payCurrency()));

		if (dblValueDate >= cpFinal.endDate() || null == dcFunding) return null;

		double dblPV = 0.;

		org.drip.product.calib.FixedStreamQuoteSet fsqs = (org.drip.product.calib.FixedStreamQuoteSet) pqs;

		double dblCoupon = cpFinal.fixedCoupon();

		try {
			if (fsqs.containsPV()) dblPV = fsqs.pv();

			if (fsqs.containsCoupon()) dblCoupon = fsqs.coupon();

			if (fsqs.containsCouponBasis()) dblCoupon += fsqs.couponBasis();
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

				dblPV -= period.baseNotional() * notional (dblPeriodEndDate) * (period.couponDCF() -
					dblAccrued) * dblCoupon * dcFunding.df (period.payDate());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (!prwc.updateValue (dblPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	private org.drip.state.estimator.PredictorResponseWeightConstraint discountFactorPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		double dblValueDate = valParams.valueDate();

		org.drip.analytics.period.CouponPeriod cpFinal = _lsCouponPeriod.get (_lsCouponPeriod.size() - 1);

		if (dblValueDate >= cpFinal.endDate()) return null;

		double dblPV = 0.;
		org.drip.product.calib.FixedStreamQuoteSet fsqs = (org.drip.product.calib.FixedStreamQuoteSet) pqs;

		double dblCoupon = cpFinal.fixedCoupon();

		try {
			if (fsqs.containsPV()) dblPV = fsqs.pv();

			if (fsqs.containsCoupon()) dblCoupon = fsqs.coupon();

			if (fsqs.containsCouponBasis()) dblCoupon += fsqs.couponBasis();
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

				double dblPeriodCV100 = period.baseNotional() * notional (dblPeriodEndDate) *
					(period.couponDCF() - dblAccrued) * dblCoupon;

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

	private org.drip.analytics.period.CouponPeriod containingPeriod (
		final double dblDate)
	{
		try {
			for (org.drip.analytics.period.CouponPeriod cp : _lsCouponPeriod) {
				if (cp.contains (dblDate)) return cp;
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private double notional (
		final double dblDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		org.drip.analytics.period.CouponPeriod cpLeft = _lsCouponPeriod.get (0);

		if (dblDate <= cpLeft.startDate()) return cpLeft.notional (cpLeft.startDate()) * cpLeft.fx (csqs);

		for (org.drip.analytics.period.CouponPeriod cp : _lsCouponPeriod) {
			if (cp.contains (dblDate)) return cp.notional (dblDate) * cp.fx (csqs);
		}

		org.drip.analytics.period.CouponPeriod cp = _lsCouponPeriod.get (_lsCouponPeriod.size() - 1);

		return cp.notional (cp.endDate()) * cp.fx (csqs);
	}

	protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * Full-featured instantiation of the Fixed Stream instance
	 * 
	 * @param lsCouponPeriod => List of the Coupon Periods
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public FixedStream (
		final java.util.List<org.drip.analytics.period.CouponPeriod> lsCouponPeriod)
		throws java.lang.Exception
	{
		if (null == (_lsCouponPeriod = lsCouponPeriod) || 0 == _lsCouponPeriod.size())
			throw new java.lang.Exception ("FixedStream ctr: Invalid Params!");
	}

	/**
	 * FixedStream de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if FixedStream cannot be properly de-serialized
	 */

	public FixedStream (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("FixedStream de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Empty state");

		java.lang.String strSerializedFixedStream = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedFixedStream || strSerializedFixedStream.isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedFixedStream,
			fieldDelimiter());

		if (null == astrField || 3 > astrField.length)
			throw new java.lang.Exception ("FixedStream de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate cash settle params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_settleParams = null;
		else
			_settleParams = new org.drip.param.valuation.CashSettleParams (astrField[1].getBytes());

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate the periods");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_lsCouponPeriod = null;
		else {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[2],
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
	}

	public java.lang.String primaryCode()
	{
		try {
			return "FIXEDSTREAM::" + (12 / _lsCouponPeriod.get (0).freq()) + "M::" + new
				org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (_lsCouponPeriod.size() -
					1).endDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public void setPrimaryCode (
		final java.lang.String strCode)
	{
	}

	public java.lang.String name()
	{
		try {
			return "FIXEDSTREAM::" + (12 / _lsCouponPeriod.get (0).freq()) + "M::" + new
				org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (_lsCouponPeriod.size() -
					1).endDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCcy = new java.util.HashSet<java.lang.String>();

		setCcy.add (_lsCouponPeriod.get (_lsCouponPeriod.size() - 1).payCurrency());

		return setCcy;
	}

	public java.lang.String[] couponCurrency()
	{
		return new java.lang.String[] {_lsCouponPeriod.get (_lsCouponPeriod.size() - 1).payCurrency()};
	}

	public java.lang.String[] principalCurrency()
	{
		return new java.lang.String[] {_lsCouponPeriod.get (_lsCouponPeriod.size() - 1).payCurrency()};
	}

	public double initialNotional()
	{
		return _lsCouponPeriod.get (0).baseNotional();
	}

	public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("FixedStream::notional => Bad date into getNotional");

		org.drip.analytics.period.CouponPeriod cpLeft = _lsCouponPeriod.get (0);

		if (dblDate <= cpLeft.startDate()) return cpLeft.notional (cpLeft.startDate());

		org.drip.analytics.period.CouponPeriod cp = containingPeriod (dblDate);

		if (null == cp)
			throw new java.lang.Exception ("FixedStream::notional => Bad date into getNotional");

		org.drip.product.params.FactorSchedule notlSchedule = cp.notionalSchedule();

		return null == notlSchedule ? 1. : notlSchedule.getFactor (dblDate);
	}

	public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2))
			throw new java.lang.Exception ("FixedStream::notional => Bad date into getNotional");

		org.drip.analytics.period.CouponPeriod cp = containingPeriod (dblDate1);

		if (null == cp || !cp.contains (dblDate2))
			throw new java.lang.Exception ("FixedStream::notional => Bad date into getNotional");

		org.drip.product.params.FactorSchedule notlSchedule = cp.notionalSchedule();

		return null == notlSchedule ? 1. : notlSchedule.getFactor (dblDate1, dblDate2);
	}

	public org.drip.analytics.output.CouponPeriodMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualEndDate) || null == csqs || null ==
			valParams)
			return null;

		org.drip.analytics.period.CouponPeriod currentPeriod = null;

		org.drip.analytics.period.CouponPeriod cpLeft = _lsCouponPeriod.get (0);

		if (dblAccrualEndDate <= cpLeft.startDate())
			currentPeriod = cpLeft;
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

	public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		return null;
	}

	public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		return null;
	}

	public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		return new org.drip.state.identifier.FXLabel[] {_lsCouponPeriod.get (0).fxLabel()};
	}

	public org.drip.analytics.date.JulianDate effective()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).startDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public org.drip.analytics.date.JulianDate maturity()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (_lsCouponPeriod.size() -
				1).endDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).endDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public java.util.List<org.drip.analytics.period.CouponPeriod> cashFlowPeriod()
	{
		return _lsCouponPeriod;
	}

	public int freq()
	{
		return cashFlowPeriod().get (0).freq();
	}

	public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _settleParams;
	}

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || null == csqs) return null;

		org.drip.state.identifier.FundingLabel fundingLabel = org.drip.state.identifier.FundingLabel.Standard
			(couponCurrency()[0]);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel);

		if (null == dcFunding) return null;

		double dblValueDate = valParams.valueDate();

		long lStart = System.nanoTime();

		double dblAccrued01 = 0.;
		boolean bFirstPeriod = true;
		double dblUnadjustedDirtyDV01 = 0.;
		double dblConvexityAdjustedDirtyDV01 = 0.;
		double dblCashPayDF = java.lang.Double.NaN;
		double dblValueNotional = java.lang.Double.NaN;

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblUnadjustedDirtyPeriodDV01 = java.lang.Double.NaN;
			double dblConvexityAdjustedDirtyPeriodDV01 = java.lang.Double.NaN;

			double dblPeriodAccrualStartDate = period.accrualStartDate();

			double dblPeriodPayDate = period.payDate();

			if (dblPeriodPayDate < dblValueDate) continue;

			org.drip.analytics.output.CouponPeriodMetrics pcm = period.baseMetrics (dblValueDate, csqs);

			if (null == pcm) return null;

			org.drip.analytics.output.ConvexityAdjustment convAdj = pcm.convexityAdjustment();

			if (null == convAdj) return null;

			try {
				if (bFirstPeriod) {
					bFirstPeriod = false;

					if (period.startDate() < dblValueDate) {
						org.drip.analytics.output.CouponAccrualMetrics cam = period.accrualMetrics
							(dblValueDate, csqs);

						dblAccrued01 = (null == cam ? 0. : cam.dcf()) * 0.0001 * period.notional
							(dblPeriodAccrualStartDate, dblValueDate);
					}
				}

				dblUnadjustedDirtyPeriodDV01 = 0.0001 * pcm.dcf() * pcm.annuity();

				dblConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 * convAdj.cumulative();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblUnadjustedDirtyDV01 += dblUnadjustedDirtyPeriodDV01;
			dblConvexityAdjustedDirtyDV01 += dblConvexityAdjustedDirtyPeriodDV01;
		}

		try {
			double dblCashSettle = dblValueDate;

			if (null != _settleParams) dblCashSettle = _settleParams.cashSettleDate (dblValueDate);

			dblCashPayDF = dcFunding.df (dblCashSettle);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblCoupon = _lsCouponPeriod.get (0).fixedCoupon();

		double dblAccrued = dblAccrued01 * 10000. * dblCoupon;
		dblUnadjustedDirtyDV01 /= dblCashPayDF;
		dblConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		double dblUnadjustedCleanDV01 = dblUnadjustedDirtyDV01 - dblAccrued01;
		double dblUnadjustedCleanPV = dblUnadjustedCleanDV01 * 10000. * dblCoupon;
		double dblUnadjustedDirtyPV = dblUnadjustedDirtyDV01 * 10000. * dblCoupon;
		double dblConvexityAdjustedCleanDV01 = dblConvexityAdjustedDirtyDV01 - dblAccrued01;
		double dblConvexityAdjustedCleanPV = dblConvexityAdjustedCleanDV01 * 10000. * dblCoupon;
		double dblConvexityAdjustedDirtyPV = dblConvexityAdjustedDirtyDV01 * 10000. * dblCoupon;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put ("Accrued", dblAccrued);

		mapResult.put ("Accrued01", dblAccrued01);

		mapResult.put ("CleanDV01", dblConvexityAdjustedCleanDV01);

		mapResult.put ("CleanPV", dblConvexityAdjustedCleanPV);

		mapResult.put ("ConvexityAdjustedCleanDV01", dblConvexityAdjustedCleanDV01);

		mapResult.put ("ConvexityAdjustedCleanPV", dblConvexityAdjustedCleanPV);

		mapResult.put ("ConvexityAdjustedDirtyDV01", dblConvexityAdjustedDirtyDV01);

		mapResult.put ("ConvexityAdjustedDirtyPV", dblConvexityAdjustedDirtyPV);

		mapResult.put ("ConvexityAdjustedDV01", dblConvexityAdjustedCleanDV01);

		mapResult.put ("ConvexityAdjustedPV", dblConvexityAdjustedCleanPV);

		mapResult.put ("ConvexityAdjustedUpfront", dblConvexityAdjustedCleanPV);

		mapResult.put ("ConvexityAdjustmentFactor", dblConvexityAdjustedDirtyDV01 / dblUnadjustedDirtyDV01);

		mapResult.put ("ConvexityAdjustmentPremium", dblConvexityAdjustedCleanPV - dblUnadjustedCleanPV);

		mapResult.put ("CV01", dblConvexityAdjustedCleanDV01);

		mapResult.put ("DirtyDV01", dblConvexityAdjustedDirtyDV01);

		mapResult.put ("DirtyPV", dblConvexityAdjustedDirtyPV);

		mapResult.put ("DV01", dblConvexityAdjustedCleanDV01);

		mapResult.put ("PV", dblConvexityAdjustedCleanPV);

		mapResult.put ("UnadjustedCleanDV01", dblUnadjustedCleanDV01);

		mapResult.put ("UnadjustedCleanPV", dblUnadjustedCleanPV);

		mapResult.put ("UnadjustedDirtyDV01", dblUnadjustedDirtyDV01);

		mapResult.put ("UnadjustedDirtyPV", dblUnadjustedDirtyPV);

		mapResult.put ("UnadjustedDV01", dblUnadjustedCleanDV01);

		mapResult.put ("UnadjustedPV", dblUnadjustedCleanPV);

		mapResult.put ("UnadjustedUpfront", dblUnadjustedCleanPV);

		mapResult.put ("Upfront", dblConvexityAdjustedCleanPV);

		try {
			dblValueNotional = notional (dblValueDate,csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
			double dblUnadjustedCleanPrice = 100. * (1. + (dblUnadjustedCleanPV / dblValueNotional));

			double dblUnadjustedDirtyPrice = 100. * (1. + (dblUnadjustedDirtyPV / dblValueNotional));

			double dblConvexityAdjustedCleanPrice = 100. * (1. + (dblConvexityAdjustedCleanPV /
				dblValueNotional));

			double dblConvexityAdjustedDirtyPrice = 100. * (1. + (dblConvexityAdjustedDirtyPV /
				dblValueNotional));

			mapResult.put ("CleanPrice", dblConvexityAdjustedCleanPrice);

			mapResult.put ("ConvexityAdjustedCleanPrice", dblConvexityAdjustedCleanPrice);

			mapResult.put ("ConvexityAdjustedDirtyPrice", dblConvexityAdjustedDirtyPrice);

			mapResult.put ("ConvexityAdjustedPrice", dblConvexityAdjustedCleanPrice);

			mapResult.put ("DirtyPrice", dblConvexityAdjustedDirtyPrice);

			mapResult.put ("Price", dblConvexityAdjustedCleanPrice);

			mapResult.put ("UnadjustedCleanPrice", dblUnadjustedCleanPrice);

			mapResult.put ("UnadjustedDirtyPrice", dblUnadjustedDirtyPrice);

			mapResult.put ("UnadjustedPrice", dblUnadjustedCleanPrice);
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("Accrued");

		setstrMeasureNames.add ("Accrued01");

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("CleanDV01");

		setstrMeasureNames.add ("CleanPrice");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("CV01");

		setstrMeasureNames.add ("DirtyDV01");

		setstrMeasureNames.add ("DirtyPrice");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("DV01");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("ConvexityAdjustedCleanDV01");

		setstrMeasureNames.add ("ConvexityAdjustedCleanPrice");

		setstrMeasureNames.add ("ConvexityAdjustedCleanPV");

		setstrMeasureNames.add ("ConvexityAdjustedDirtyDV01");

		setstrMeasureNames.add ("ConvexityAdjustedDirtyPrice");

		setstrMeasureNames.add ("ConvexityAdjustedDirtyPV");

		setstrMeasureNames.add ("ConvexityAdjustedDV01");

		setstrMeasureNames.add ("ConvexityAdjustedPrice");

		setstrMeasureNames.add ("ConvexityAdjustedPV");

		setstrMeasureNames.add ("ConvexityAdjustedUpfront");

		setstrMeasureNames.add ("ConvexityAdjustmentFactor");

		setstrMeasureNames.add ("ConvexityAdjustmentPremium");

		setstrMeasureNames.add ("UnadjustedCleanDV01");

		setstrMeasureNames.add ("UnadjustedCleanPrice");

		setstrMeasureNames.add ("UnadjustedCleanPV");

		setstrMeasureNames.add ("UnadjustedDirtyDV01");

		setstrMeasureNames.add ("UnadjustedDirtyPrice");

		setstrMeasureNames.add ("UnadjustedDirtyPV");

		setstrMeasureNames.add ("UnadjustedDV01");

		setstrMeasureNames.add ("UnadjustedPrice");

		setstrMeasureNames.add ("UnadjustedPV");

		setstrMeasureNames.add ("UnadjustedUpfront");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	public org.drip.product.calib.ProductQuoteSet calibQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
	{
		try {
			return new org.drip.product.calib.FixedStreamQuoteSet (aLSS);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.FixedStreamQuoteSet) || !pqs.contains
				(org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
					org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0])) ? unloadedPRWC
							(valParams, pricerParams, csqs, quotingParams, pqs) : discountFactorPRWC
								(valParams, pricerParams, csqs, quotingParams, pqs);
	}

	public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return unloadedPRWC (valParams, pricerParams, csqs, quotingParams, pqs);
	}

	public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.FixedStreamQuoteSet) ? null : discountFactorPRWC (valParams, pricerParams,
				csqs, quotingParams, pqs);
	}

	public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _lsCouponPeriod.get (_lsCouponPeriod.size() -
			1).endDate() || null == csqs)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0]));

		if (null == dcFunding) return null;

		try {
			org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure = null;

			for (org.drip.analytics.period.CouponPeriod p : _lsCouponPeriod) {
				double dblPeriodPayDate = p.payDate();

				if (p.startDate() < valParams.valueDate()) continue;

				org.drip.quant.calculus.WengertJacobian jackDDFDManifestMeasure =
					dcFunding.jackDDFDManifestMeasure (dblPeriodPayDate, "Rate");

				if (null == jackDDFDManifestMeasure) continue;

				int iNumQuote = jackDDFDManifestMeasure.numParameters();

				if (0 == iNumQuote) continue;

				if (null == jackDDirtyPVDManifestMeasure)
					jackDDirtyPVDManifestMeasure = new org.drip.quant.calculus.WengertJacobian (1,
						iNumQuote);

				double dblPeriodNotional = p.baseNotional() * p.notional (p.startDate(), p.endDate());

				double dblPeriodDCF = p.couponDCF();

				for (int k = 0; k < iNumQuote; ++k) {
					if (!jackDDirtyPVDManifestMeasure.accumulatePartialFirstDerivative (0, k,
						dblPeriodNotional * dblPeriodDCF * jackDDFDManifestMeasure.getFirstDerivative (0,
							k)))
						return null;
				}
			}

			return jackDDirtyPVDManifestMeasure;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _lsCouponPeriod.get (_lsCouponPeriod.size() -
			1).endDate() || null == strManifestMeasure || null == csqs)
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

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new FixedStream (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.service.api.CreditAnalytics.Init ("");

		org.drip.analytics.date.JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

		java.util.List<org.drip.analytics.period.CouponPeriod> lsCouponPeriod =
			org.drip.analytics.support.PeriodBuilder.RegularPeriodSingleReset (dtToday.julian(), "4Y",
				java.lang.Double.NaN, null, 2, "30/360", false, true, "JPY", 100., null, 0.03, "JPY", null,
					null);

		FixedStream fs = new org.drip.product.cashflow.FixedStream (lsCouponPeriod);

		byte[] abFS = fs.serialize();

		System.out.println (new java.lang.String (abFS));

		FixedStream fsDeser = new FixedStream (abFS);

		System.out.println (new java.lang.String (fsDeser.serialize()));
	}
}

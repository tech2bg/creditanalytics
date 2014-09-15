
package org.drip.product.cashflow;

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
 * Stream is the base class on top which the fixed and the floating streams are implemented.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class Stream extends org.drip.service.stream.Serializer {
	protected java.util.List<org.drip.analytics.period.CouponPeriod> _lsCouponPeriod = null;

	protected double notional (
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

	/**
	 * Stream constructor
	 * 
	 * @param lsCouponPeriod List of the Coupon Periods
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public Stream (
		final java.util.List<org.drip.analytics.period.CouponPeriod> lsCouponPeriod)
		throws java.lang.Exception
	{
		if (null == (_lsCouponPeriod = lsCouponPeriod) || 0 == _lsCouponPeriod.size())
			throw new java.lang.Exception ("Stream ctr => Invalid Input params!");
	}

	/**
	 * Retrieve the Stream Name
	 * 
	 * @return The Stream Name
	 */

	public java.lang.String name()
	{
		org.drip.analytics.period.CouponPeriod cpFirst = _lsCouponPeriod.get (0);

		try {
			return "STREAM::" + cpFirst.payCurrency() + "/" + cpFirst.couponCurrency() + "::" + (12 / freq())
				+ "M::{" + new org.drip.analytics.date.JulianDate (cpFirst.startDate()) + "->" + new
					org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (_lsCouponPeriod.size() -
						1).endDate()) + "}";
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * Retrieve the Stream Frequency
	 * 
	 * @return The Stream Frequency
	 */

	public int freq()
	{
		return _lsCouponPeriod.get (0).freq();
	}

	/**
	 * Retrieve the Credit Label
	 * 
	 * @return The Credit Label
	 */

	public org.drip.state.identifier.CreditLabel creditLabel()
	{
		return _lsCouponPeriod.get (0).creditLabel();
	}

	/**
	 * Retrieve the Forward Label
	 * 
	 * @return The Forward Label
	 */

	public org.drip.state.identifier.ForwardLabel forwardLabel()
	{
		return _lsCouponPeriod.get (0).forwardLabel();
	}

	/**
	 * Retrieve the Funding Label
	 * 
	 * @return The Funding Label
	 */

	public org.drip.state.identifier.FundingLabel fundingLabel()
	{
		return _lsCouponPeriod.get (0).fundingLabel();
	}

	/**
	 * Retrieve the FX Label
	 * 
	 * @return The FX Label
	 */

	public org.drip.state.identifier.FXLabel fxLabel()
	{
		return _lsCouponPeriod.get (0).fxLabel();
	}

	/**
	 * Retrieve the Coupon Period List
	 * 
	 * @return The Coupon Period List
	 */

	public java.util.List<org.drip.analytics.period.CouponPeriod> cashFlowPeriod()
	{
		return _lsCouponPeriod;
	}

	/**
	 * Retrieve the Period Instance enveloping the specified Date
	 * 
	 * @param dblDate The Date
	 * 
	 * @return The Period Instance enveloping the specified Date
	 */

	public org.drip.analytics.period.CouponPeriod containingPeriod (
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

	/**
	 * Retrieve the Initial Notional
	 * 
	 * @return The Initial Notional
	 */

	public double initialNotional()
	{
		return _lsCouponPeriod.get (0).baseNotional();
	}

	/**
	 * Retrieve the Notional corresponding to the specified Date
	 * 
	 * @param dblDate The Date
	 * 
	 * @return The Notional corresponding to the specified Date
	 * 
	 * @throws java.lang.Exception Thrown if the Notional cannot be computed
	 */

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

	/**
	 * Retrieve the Notional aggregated over the Date Pairs
	 * 
	 * @param dblDate1 The Date #1
	 * @param dblDate2 The Date #2
	 * 
	 * @return The Notional aggregated over the Date Pairs
	 * 
	 * @throws java.lang.Exception Thrown if the Notional cannot be computed
	 */

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

	/**
	 * Retrieve the Effective Date
	 * 
	 * @return The Effective Date
	 */

	public org.drip.analytics.date.JulianDate effective()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).startDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Maturity Date
	 * 
	 * @return The Maturity Date
	 */

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

	/**
	 * Retrieve the First Coupon Pay Date
	 * 
	 * @return The First Coupon Pay Date
	 */

	public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).endDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Cash Flow Currency Set
	 * 
	 * @return The Cash Flow Currency Set
	 */

	public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCcy = new java.util.HashSet<java.lang.String>();

		org.drip.analytics.period.CouponPeriod cpFirst = _lsCouponPeriod.get (0);

		setCcy.add (cpFirst.payCurrency());

		setCcy.add (cpFirst.couponCurrency());

		return setCcy;
	}

	/**
	 * Retrieve the Coupon Currency
	 * 
	 * @return The Coupon Currency
	 */

	public java.lang.String couponCurrency()
	{
		return _lsCouponPeriod.get (_lsCouponPeriod.size() - 1).couponCurrency();
	}

	/**
	 * Retrieve the Pay Currency
	 * 
	 * @return The Pay Currency
	 */

	public java.lang.String payCurrency()
	{
		return _lsCouponPeriod.get (_lsCouponPeriod.size() - 1).payCurrency();
	}

	/**
	 * Retrieve the Principal Currency
	 * 
	 * @return The Principal Currency
	 */

	public java.lang.String[] principalCurrency()
	{
		return new java.lang.String[] {_lsCouponPeriod.get (_lsCouponPeriod.size() - 1).payCurrency()};
	}

	/**
	 * Get the Coupon Metrics for the period corresponding to the specified accrual end date
	 * 
	 * @param dblAccrualEndDate The Accrual End Date
	 * @param valParams Valuation parameters
	 * @param csqs Market Parameters
	 * 
	 * @return The Coupon Metrics for the period corresponding to the specified accrual end date
	 */

	public org.drip.analytics.output.CouponPeriodMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualEndDate) || null == csqs) return null;

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

	/**
	 * Generate a Value Map for the Stream
	 * 
	 * @param valParams The Valuation Parameters
	 * @param pricerParams The Pricer parameters
	 * @param csqs The Market Parameters
	 * @param vcp Valuation Customization Parameters
	 * 
	 * @return The Value Map for the Stream
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || null == csqs) return null;

		org.drip.state.identifier.FundingLabel fundingLabel = org.drip.state.identifier.FundingLabel.Standard
			(payCurrency());

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel);

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		if (null == dcFunding) return null;

		long lStart = System.nanoTime();

		double dblValueDate = valParams.valueDate();

		double dblFixing01 = 0.;
		double dblAccrued01 = 0.;
		double dblTotalCoupon = 0.;
		double dblUnadjustedDirtyPV = 0.;
		double dblUnadjustedDirtyDV01 = 0.;
		double dblCompoundingAdjustedDirtyPV = 0.;
		double dblCompoundingAdjustedDirtyDV01 = 0.;
		double dblCashPayDF = java.lang.Double.NaN;
		double dblResetDate = java.lang.Double.NaN;
		double dblResetRate = java.lang.Double.NaN;
		double dblValueNotional = java.lang.Double.NaN;
		double dblCumulativeConvexityAdjustedDirtyPV = 0.;
		double dblCumulativeConvexityAdjustedDirtyDV01 = 0.;

		double dblSpread = null != forwardLabel ? _lsCouponPeriod.get (0).floatSpread() : 0.;

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblUnadjustedDirtyPeriodDV01 = java.lang.Double.NaN;
			double dblCompoundingAdjustedDirtyPeriodDV01 = java.lang.Double.NaN;

			double dblPeriodPayDate = period.payDate();

			if (dblPeriodPayDate < dblValueDate) continue;

			double dblPeriodDCF = period.couponDCF();

			org.drip.analytics.output.CouponPeriodMetrics pcm = period.baseMetrics (dblValueDate, csqs);

			if (null == pcm) return null;

			org.drip.analytics.output.ConvexityAdjustment convAdj = pcm.convexityAdjustment();

			if (null == convAdj) return null;

			double dblPeriodBaseRate = pcm.compoundedAccrualRate();

			org.drip.analytics.output.CouponAccrualMetrics cam = period.accrualMetrics (dblValueDate, csqs);

			try {
				if (null != cam) {
					dblResetDate = cam.outstandingFixingDate();

					dblResetRate = cam.compoundedAccrualRate();

					dblAccrued01 = dblFixing01 = cam.accrual01();
				} else if (period.contains (dblValueDate)) {
					dblAccrued01 = 0.;
					dblResetRate = dblPeriodBaseRate;
					java.util.List<org.drip.analytics.period.ResetPeriod> lsRP = null;

					org.drip.analytics.period.ResetPeriodContainer rpc = period.rpc();

					if (null != rpc) lsRP = rpc.resetPeriods();

					dblResetDate = null != lsRP && 0 != lsRP.size() ? lsRP.get (0).fixing() :
						period.startDate();
				}

				dblUnadjustedDirtyPeriodDV01 = 0.0001 * dblPeriodDCF * pcm.annuity();

				dblCompoundingAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
					pcm.compoundingConvexityFactor();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			double dblCumulativeConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
				convAdj.cumulative();

			double dblPeriodFullRate = dblPeriodBaseRate + dblSpread;
			dblTotalCoupon += dblPeriodFullRate;
			dblUnadjustedDirtyDV01 += dblUnadjustedDirtyPeriodDV01;
			dblUnadjustedDirtyPV += dblUnadjustedDirtyPeriodDV01 * 10000. * dblPeriodFullRate;
			dblCumulativeConvexityAdjustedDirtyDV01 += dblCumulativeConvexityAdjustedDirtyPeriodDV01;
			dblCumulativeConvexityAdjustedDirtyPV += dblCumulativeConvexityAdjustedDirtyPeriodDV01 * 10000. *
				dblPeriodFullRate;
			dblCompoundingAdjustedDirtyDV01 += dblCompoundingAdjustedDirtyPeriodDV01;
			dblCompoundingAdjustedDirtyPV += dblCompoundingAdjustedDirtyPeriodDV01 * 10000. *
				dblPeriodFullRate;
		}

		try {
			dblCashPayDF = dcFunding.df (dblValueDate);

			dblValueNotional = notional (dblValueDate, csqs);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double dblAccrualCoupon = null == forwardLabel ?_lsCouponPeriod.get (0).fixedCoupon() : dblResetRate
			+ dblSpread;

		dblUnadjustedDirtyPV /= dblCashPayDF;
		dblUnadjustedDirtyDV01 /= dblCashPayDF;
		dblCompoundingAdjustedDirtyPV /= dblCashPayDF;
		dblCompoundingAdjustedDirtyDV01 /= dblCashPayDF;
		dblCumulativeConvexityAdjustedDirtyPV /= dblCashPayDF;
		dblCumulativeConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		double dblAccrued = 0. == dblAccrued01 ? 0. : dblAccrued01 * 10000. * dblAccrualCoupon;
		double dblUnadjustedCleanPV = dblUnadjustedDirtyPV - dblAccrued;
		double dblUnadjustedCleanDV01 = dblUnadjustedDirtyDV01 - dblAccrued01;
		double dblUnadjustedFairPremium = 0.0001 * dblUnadjustedCleanPV / dblUnadjustedCleanDV01;
		double dblCompoundingAdjustedCleanPV = dblCompoundingAdjustedDirtyPV - dblAccrued;
		double dblCompoundingAdjustedCleanDV01 = dblCompoundingAdjustedDirtyDV01 - dblAccrued01;
		double dblCompoundingAdjustedFairPremium = 0.0001 * dblCompoundingAdjustedCleanPV /
			dblCompoundingAdjustedCleanDV01;
		double dblCumulativeConvexityAdjustedCleanPV = dblCumulativeConvexityAdjustedDirtyPV - dblAccrued;
		double dblCumulativeConvexityAdjustedCleanDV01 = dblCumulativeConvexityAdjustedDirtyDV01 -
			dblAccrued01;
		double dblCumulativeConvexityAdjustedFairPremium = 0.0001 * dblCumulativeConvexityAdjustedCleanPV /
			dblCumulativeConvexityAdjustedCleanDV01;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put ("AccrualCoupon", dblAccrualCoupon);

		mapResult.put ("Accrued", dblAccrued);

		mapResult.put ("Accrued01", dblAccrued01);

		mapResult.put ("CleanDV01", dblCumulativeConvexityAdjustedCleanDV01);

		mapResult.put ("CleanPV", dblCumulativeConvexityAdjustedCleanPV);

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

		mapResult.put ("ConvexityAdjustmentPremium", dblCompoundingAdjustedCleanPV - dblUnadjustedCleanPV);

		mapResult.put ("CompoundingAdjustmentPremiumUpfront", (dblCompoundingAdjustedCleanPV -
			dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("CumulativeConvexityAdjustedCleanDV01", dblCumulativeConvexityAdjustedCleanDV01);

		mapResult.put ("CumulativeConvexityAdjustedCleanPV", dblCumulativeConvexityAdjustedCleanPV);

		mapResult.put ("ConvexityAdjustedDirtyDV01", dblCumulativeConvexityAdjustedDirtyDV01);

		mapResult.put ("ConvexityAdjustedDirtyPV", dblCumulativeConvexityAdjustedDirtyPV);

		mapResult.put ("ConvexityAdjustedDV01", dblCumulativeConvexityAdjustedDirtyDV01);

		mapResult.put ("CumulativeConvexityAdjustedFairPremium", dblCumulativeConvexityAdjustedFairPremium);

		mapResult.put ("CumulativeConvexityAdjustedParRate", dblCumulativeConvexityAdjustedFairPremium);

		mapResult.put ("CumulativeConvexityAdjustedPV", dblCumulativeConvexityAdjustedCleanPV);

		mapResult.put ("CumulativeConvexityAdjustedRate", dblCumulativeConvexityAdjustedFairPremium);

		mapResult.put ("CumulativeConvexityAdjustedUpfront", dblCumulativeConvexityAdjustedCleanPV);

		mapResult.put ("CumulativeConvexityAdjustmentFactor", dblCumulativeConvexityAdjustedDirtyDV01 /
			dblUnadjustedDirtyDV01);

		mapResult.put ("CumulativeConvexityAdjustmentPremium", dblCumulativeConvexityAdjustedCleanPV -
			dblUnadjustedCleanPV);

		mapResult.put ("CumulativeConvexityAdjustmentPremiumUpfront", (dblCumulativeConvexityAdjustedCleanPV
			- dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("CV01", dblCumulativeConvexityAdjustedCleanDV01);

		mapResult.put ("DirtyDV01", dblCumulativeConvexityAdjustedDirtyDV01);

		mapResult.put ("DirtyPV", dblCumulativeConvexityAdjustedDirtyPV);

		mapResult.put ("DV01", dblCumulativeConvexityAdjustedCleanDV01);

		mapResult.put ("FairPremium", dblCumulativeConvexityAdjustedFairPremium);

		mapResult.put ("Fixing01", dblFixing01 / dblCashPayDF);

		mapResult.put ("ParRate", dblCumulativeConvexityAdjustedFairPremium);

		mapResult.put ("PV", dblCumulativeConvexityAdjustedCleanPV);

		mapResult.put ("Rate", dblCumulativeConvexityAdjustedFairPremium);

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

		mapResult.put ("Upfront", dblCumulativeConvexityAdjustedCleanPV);

		if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
			double dblCompoundingAdjustedCleanPrice = 100. * (1. + (dblCompoundingAdjustedCleanPV /
				dblValueNotional));
			double dblCumulativeConvexityAdjustedCleanPrice = 100. * (1. +
				(dblCumulativeConvexityAdjustedCleanPV / dblValueNotional));
			double dblUnadjustedCleanPrice = 100. * (1. + (dblUnadjustedCleanPV / dblValueNotional));

			mapResult.put ("CleanPrice", dblCumulativeConvexityAdjustedCleanPrice);

			mapResult.put ("CompoundingAdjustedCleanPrice", dblCompoundingAdjustedCleanPrice);

			mapResult.put ("CompoundingAdjustedDirtyPrice", 100. * (1. + (dblCompoundingAdjustedDirtyPV /
				dblValueNotional)));

			mapResult.put ("CompoundingAdjustedPrice", dblCompoundingAdjustedCleanPrice);

			mapResult.put ("CumulativeConvexityAdjustedCleanPrice",
				dblCumulativeConvexityAdjustedCleanPrice);

			mapResult.put ("CumulativeConvexityAdjustedDirtyPrice", 100. * (1. +
				(dblCumulativeConvexityAdjustedDirtyPV / dblValueNotional)));

			mapResult.put ("CumulativeConvexityAdjustedPrice", dblCumulativeConvexityAdjustedCleanPrice);

			mapResult.put ("DirtyPrice", 100. * (1. + (dblCumulativeConvexityAdjustedDirtyPV /
				dblValueNotional)));

			mapResult.put ("Price", dblCumulativeConvexityAdjustedCleanPrice);

			mapResult.put ("UnadjustedCleanPrice", dblUnadjustedCleanPrice);

			mapResult.put ("UnadjustedDirtyPrice", 100. * (1. + (dblUnadjustedDirtyPV / dblValueNotional)));

			mapResult.put ("UnadjustedPrice", dblUnadjustedCleanPrice);
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	/**
	 * Generate the Calibration Quote Set corresponding to the specified Latent State Array
	 * 
	 * @param aLSS The Latent State Array
	 * 
	 * @return The Calibration Quote Set corresponding to the specified Latent State Array
	 */

	public org.drip.product.calib.ProductQuoteSet calibQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
	{
		try {
			return null == forwardLabel() ? new org.drip.product.calib.FixedStreamQuoteSet (aLSS) : new
				org.drip.product.calib.FloatingStreamQuoteSet (aLSS);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate the State Loading Constraints for the Forward Latent State
	 * 
	 * @param valParams The Valuation Parameters
	 * @param pricerParams The Pricer parameters
	 * @param csqs The Market Parameters
	 * @param vcp Valuation Customization Parameters
	 * @param pqs The Product Calibration Quote Set
	 * 
	 * @return The State Loading Constraints for the Forward Latent State
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs) return null;

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		if ((null != forwardLabel && !(pqs instanceof org.drip.product.calib.FloatingStreamQuoteSet)) ||
			(null == forwardLabel && !(pqs instanceof org.drip.product.calib.FixedStreamQuoteSet)))
			return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= maturity().julian()) return null;

		double dblCleanPV = 0.;

		try {
			if (pqs instanceof org.drip.product.calib.FloatingStreamQuoteSet) {
				org.drip.product.calib.FloatingStreamQuoteSet fsqs =
					(org.drip.product.calib.FloatingStreamQuoteSet) pqs;

				if (fsqs.containsPV()) dblCleanPV = fsqs.pv();
			} else if (pqs instanceof org.drip.product.calib.FixedStreamQuoteSet) {
				org.drip.product.calib.FixedStreamQuoteSet fsqs =
					(org.drip.product.calib.FixedStreamQuoteSet) pqs;

				if (fsqs.containsPV()) dblCleanPV = fsqs.pv();
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			org.drip.state.estimator.PredictorResponseWeightConstraint prwcPeriod = period.forwardPRWC
				(dblValueDate, csqs, pqs);

			if (null == prwcPeriod || !prwc.absorb (prwcPeriod)) return null;
		}

		if (!prwc.updateValue (dblCleanPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Generate the State Loading Constraints for the Funding Latent State
	 * 
	 * @param valParams The Valuation Parameters
	 * @param pricerParams The Pricer parameters
	 * @param csqs The Market Parameters
	 * @param vcp Valuation Customization Parameters
	 * @param pqs The Product Calibration Quote Set
	 * 
	 * @return The State Loading Constraints for the Funding Latent State
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs) return null;

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		if ((null != forwardLabel && !(pqs instanceof org.drip.product.calib.FloatingStreamQuoteSet)) ||
			(null == forwardLabel && !(pqs instanceof org.drip.product.calib.FixedStreamQuoteSet)))
			return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= maturity().julian()) return null;

		double dblCleanPV = 0.;

		try {
			if (pqs instanceof org.drip.product.calib.FloatingStreamQuoteSet) {
				org.drip.product.calib.FloatingStreamQuoteSet fsqs =
					(org.drip.product.calib.FloatingStreamQuoteSet) pqs;

				if (fsqs.containsPV()) dblCleanPV = fsqs.pv();
			} else if (pqs instanceof org.drip.product.calib.FixedStreamQuoteSet) {
				org.drip.product.calib.FixedStreamQuoteSet fsqs =
					(org.drip.product.calib.FixedStreamQuoteSet) pqs;

				if (fsqs.containsPV()) dblCleanPV = fsqs.pv();
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			org.drip.state.estimator.PredictorResponseWeightConstraint prwcPeriod = period.fundingPRWC
				(dblValueDate, csqs, pqs);

			if (null == prwcPeriod || !prwc.absorb (prwcPeriod)) return null;
		}

		if (!prwc.updateValue (dblCleanPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Generate the State Loading Constraints for the Merged Forward/Funding Latent State
	 * 
	 * @param valParams The Valuation Parameters
	 * @param pricerParams The Pricer parameters
	 * @param csqs The Market Parameters
	 * @param vcp Valuation Customization Parameters
	 * @param pqs The Product Calibration Quote Set
	 * 
	 * @return The State Loading Constraints for the Merged Forward/Funding Latent State
	 */

	public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs) return null;

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		if ((null != forwardLabel && !(pqs instanceof org.drip.product.calib.FloatingStreamQuoteSet)) ||
			(null == forwardLabel && !(pqs instanceof org.drip.product.calib.FixedStreamQuoteSet)))
			return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= maturity().julian()) return null;

		double dblCleanPV = 0.;

		try {
			if (pqs instanceof org.drip.product.calib.FloatingStreamQuoteSet) {
				org.drip.product.calib.FloatingStreamQuoteSet fsqs =
					(org.drip.product.calib.FloatingStreamQuoteSet) pqs;

				if (fsqs.containsPV()) dblCleanPV = fsqs.pv();
			} else if (pqs instanceof org.drip.product.calib.FixedStreamQuoteSet) {
				org.drip.product.calib.FixedStreamQuoteSet fsqs =
					(org.drip.product.calib.FixedStreamQuoteSet) pqs;

				if (fsqs.containsPV()) dblCleanPV = fsqs.pv();
			}
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CouponPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.endDate();

			if (dblPeriodEndDate < dblValueDate) continue;

			org.drip.state.estimator.PredictorResponseWeightConstraint prwcPeriod = period.forwardFundingPRWC
				(dblValueDate, csqs, pqs);

			if (null == prwcPeriod || !prwc.absorb (prwcPeriod)) return null;
		}

		if (!prwc.updateValue (dblCleanPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	/**
	 * Generate the Jacobian of the Dirty PV to the Manifest Measure
	 * 
	 * @param valParams The Valuation Parameters
	 * @param pricerParams The Pricer parameters
	 * @param csqs The Market Parameters
	 * @param vcp Valuation Customization Parameters
	 * 
	 * @return The Jacobian of the Dirty PV to the Manifest Measure
	 */

	public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || valParams.valueDate() >= _lsCouponPeriod.get (_lsCouponPeriod.size() -
			1).endDate() || null == csqs)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (couponCurrency()));

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

	/**
	 * Generate the micro-Jacobian of the Manifest Measure to the Discount Factor
	 * 
	 * @param valParams The Valuation Parameters
	 * @param pricerParams The Pricer parameters
	 * @param csqs The Market Parameters
	 * @param vcp Valuation Customization Parameters
	 * 
	 * @return The micro-Jacobian of the Manifest Measure to the Discount Factor
	 */

	public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || valParams.valueDate() >= _lsCouponPeriod.get (_lsCouponPeriod.size() -
			1).endDate() || null == strManifestMeasure)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (payCurrency()));

		if (null == dcFunding) return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure) || "SwapRate".equalsIgnoreCase (strManifestMeasure))
		{
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, csqs, vcp);

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

	/**
	 * Stream de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if Stream cannot be properly de-serialized
	 */

	public Stream (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("Stream de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("Stream de-serializer: Empty state");

		java.lang.String strSerializedStream = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedStream || strSerializedStream.isEmpty())
			throw new java.lang.Exception ("Stream de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedStream,
			fieldDelimiter());

		if (null == astrField || 2 > astrField.length)
			throw new java.lang.Exception ("Stream de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("Stream de-serializer: Cannot locate the periods");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			_lsCouponPeriod = null;
		else {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[1],
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
			return new Stream (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

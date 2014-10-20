
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
 * GenericStream is the base class on top which the fixed and the floating streams are implemented.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class GenericStream {
	protected java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> _lsCouponPeriod = null;

	protected double notional (
		final double dblDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		org.drip.analytics.cashflow.GenericCouponPeriod cpLeft = _lsCouponPeriod.get (0);

		if (dblDate <= cpLeft.startDate()) return cpLeft.notional (cpLeft.startDate()) * cpLeft.fx (csqs);

		for (org.drip.analytics.cashflow.GenericCouponPeriod cp : _lsCouponPeriod) {
			if (cp.contains (dblDate)) return cp.notional (dblDate) * cp.fx (csqs);
		}

		org.drip.analytics.cashflow.GenericCouponPeriod cp = _lsCouponPeriod.get (_lsCouponPeriod.size() - 1);

		return cp.notional (cp.endDate()) * cp.fx (csqs);
	}

	/**
	 * GenericStream constructor
	 * 
	 * @param lsCouponPeriod List of the Coupon Periods
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public GenericStream (
		final java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> lsCouponPeriod)
		throws java.lang.Exception
	{
		if (null == (_lsCouponPeriod = lsCouponPeriod) || 0 == _lsCouponPeriod.size())
			throw new java.lang.Exception ("GenericStream ctr => Invalid Input params!");
	}

	/**
	 * Retrieve the Stream Name
	 * 
	 * @return The Stream Name
	 */

	public java.lang.String name()
	{
		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		java.lang.String strTrailer = "::{" + effective() + "->" + maturity() + "}";

		if (null != forwardLabel)
			return "FLOATSTREAM::" + payCurrency() + "::" + forwardLabel.fullyQualifiedName() + strTrailer;

		return "FIXEDSTREAM::" + payCurrency() + "/" + couponCurrency() + "::" + (12 / freq()) + strTrailer;
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

	public java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> cashFlowPeriod()
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

	public org.drip.analytics.cashflow.GenericCouponPeriod containingPeriod (
		final double dblDate)
	{
		try {
			for (org.drip.analytics.cashflow.GenericCouponPeriod cp : _lsCouponPeriod) {
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
			throw new java.lang.Exception ("GenericStream::notional => Bad date into getNotional");

		org.drip.analytics.cashflow.GenericCouponPeriod cpLeft = _lsCouponPeriod.get (0);

		if (dblDate <= cpLeft.startDate()) return cpLeft.notional (cpLeft.startDate());

		org.drip.analytics.cashflow.GenericCouponPeriod cp = containingPeriod (dblDate);

		if (null == cp)
			throw new java.lang.Exception ("GenericStream::notional => Bad date into getNotional");

		org.drip.product.params.FactorSchedule notlSchedule = cp.notionalSchedule();

		return null == notlSchedule ? 1. : notlSchedule.factor (dblDate);
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
			throw new java.lang.Exception ("GenericStream::notional => Bad date into getNotional");

		org.drip.analytics.cashflow.GenericCouponPeriod cp = containingPeriod (dblDate1);

		if (null == cp || !cp.contains (dblDate2))
			throw new java.lang.Exception ("GenericStream::notional => Bad date into getNotional");

		org.drip.product.params.FactorSchedule notlSchedule = cp.notionalSchedule();

		return null == notlSchedule ? 1. : notlSchedule.factor (dblDate1, dblDate2);
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

		org.drip.analytics.cashflow.GenericCouponPeriod cpFirst = _lsCouponPeriod.get (0);

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

	public org.drip.analytics.output.CompositePeriodCouponMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualEndDate) || null == csqs) return null;

		org.drip.analytics.output.UnitPeriodMetrics upm = null;
		org.drip.analytics.cashflow.GenericCouponPeriod currentPeriod = null;

		org.drip.analytics.cashflow.GenericCouponPeriod cpLeft = _lsCouponPeriod.get (0);

		if (dblAccrualEndDate <= cpLeft.startDate())
			currentPeriod = cpLeft;
		else {
			for (org.drip.analytics.cashflow.GenericCouponPeriod period : _lsCouponPeriod) {
				if (null == period) continue;

				if (dblAccrualEndDate >= period.startDate() && dblAccrualEndDate <= period.endDate()) {
					currentPeriod = period;
					break;
				}
			}
		}

		if (null == currentPeriod) return null;

		org.drip.analytics.output.GenericCouponPeriodMetrics gcpm = currentPeriod.baseMetrics
			(valParams.valueDate(), csqs);

		if (null == gcpm) return null;

		try {
			upm = new org.drip.analytics.output.UnitPeriodMetrics
				(currentPeriod.accrualStartDate(), currentPeriod.accrualEndDate(), currentPeriod.couponDCF(),
					gcpm.compoundedAccrualRate(), new org.drip.analytics.output.ConvexityAdjustment());
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		java.util.List<org.drip.analytics.output.UnitPeriodMetrics> lsUPM = new
			java.util.ArrayList<org.drip.analytics.output.UnitPeriodMetrics>();

		lsUPM.add (upm);

		return org.drip.analytics.output.CompositePeriodCouponMetrics.Create (lsUPM);
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
		double dblCreditForwardConvexityAdjustedDirtyPV = 0.;
		double dblCreditForwardConvexityAdjustedDirtyDV01 = 0.;
		double dblCreditFundingConvexityAdjustedDirtyPV = 0.;
		double dblCreditFundingConvexityAdjustedDirtyDV01 = 0.;
		double dblCreditFXConvexityAdjustedDirtyPV = 0.;
		double dblCreditFXConvexityAdjustedDirtyDV01 = 0.;
		double dblCumulativeConvexityAdjustedDirtyPV = 0.;
		double dblCumulativeConvexityAdjustedDirtyDV01 = 0.;
		double dblForwardFundingConvexityAdjustedDirtyPV = 0.;
		double dblForwardFundingConvexityAdjustedDirtyDV01 = 0.;
		double dblForwardFXConvexityAdjustedDirtyPV = 0.;
		double dblForwardFXConvexityAdjustedDirtyDV01 = 0.;
		double dblFundingFXConvexityAdjustedDirtyPV = 0.;
		double dblFundingFXConvexityAdjustedDirtyDV01 = 0.;

		double dblSpread = null != forwardLabel ? _lsCouponPeriod.get (0).floatSpread() : 0.;

		for (org.drip.analytics.cashflow.GenericCouponPeriod period : _lsCouponPeriod) {
			double dblUnadjustedDirtyPeriodDV01 = java.lang.Double.NaN;
			double dblCompoundingAdjustedDirtyPeriodDV01 = java.lang.Double.NaN;

			double dblPeriodPayDate = period.payDate();

			if (dblPeriodPayDate < dblValueDate) continue;

			double dblPeriodDCF = period.couponDCF();

			org.drip.analytics.output.GenericCouponPeriodMetrics pcm = period.baseMetrics (dblValueDate, csqs);

			if (null == pcm) return null;

			org.drip.analytics.output.ConvexityAdjustment convAdj = pcm.convexityAdjustment();

			if (null == convAdj) return null;

			double dblPeriodBaseRate = pcm.compoundedAccrualRate();

			org.drip.analytics.output.GenericCouponAccrualMetrics cam = period.accrualMetrics (dblValueDate, csqs);

			try {
				if (null != cam) {
					dblResetDate = cam.outstandingFixingDate();

					dblResetRate = cam.compoundedAccrualRate();

					dblAccrued01 = dblFixing01 = cam.accrual01();
				} else if (period.contains (dblValueDate)) {
					dblAccrued01 = 0.;
					dblResetRate = dblPeriodBaseRate;
					java.util.List<org.drip.analytics.cashflow.GenericComposablePeriod> lsRP = null;

					org.drip.analytics.cashflow.ResetPeriodContainer rpc = period.rpc();

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

			double dblCreditForwardConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
				convAdj.creditForward();

			double dblCreditFundingConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
				convAdj.creditFunding();

			double dblCreditFXConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
				convAdj.creditFX();

			double dblCumulativeConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
				convAdj.cumulative();

			double dblForwardFundingConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
				convAdj.forwardFunding();

			double dblForwardFXConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
				convAdj.forwardFX();

			double dblFundingFXConvexityAdjustedDirtyPeriodDV01 = dblUnadjustedDirtyPeriodDV01 *
				convAdj.fundingFX();

			double dblPeriodFullRate = dblPeriodBaseRate + dblSpread;
			dblTotalCoupon += dblPeriodFullRate;
			dblUnadjustedDirtyDV01 += dblUnadjustedDirtyPeriodDV01;
			dblUnadjustedDirtyPV += dblUnadjustedDirtyPeriodDV01 * 10000. * dblPeriodFullRate;
			dblCompoundingAdjustedDirtyDV01 += dblCompoundingAdjustedDirtyPeriodDV01;
			dblCompoundingAdjustedDirtyPV += dblCompoundingAdjustedDirtyPeriodDV01 * 10000. *
				dblPeriodFullRate;
			dblCreditForwardConvexityAdjustedDirtyDV01 += dblCreditForwardConvexityAdjustedDirtyPeriodDV01;
			dblCreditForwardConvexityAdjustedDirtyPV += dblCreditForwardConvexityAdjustedDirtyPeriodDV01 *
				10000. * dblPeriodFullRate;
			dblCreditFundingConvexityAdjustedDirtyDV01 += dblCreditFundingConvexityAdjustedDirtyPeriodDV01;
			dblCreditFundingConvexityAdjustedDirtyPV += dblCreditFundingConvexityAdjustedDirtyPeriodDV01 *
				10000. * dblPeriodFullRate;
			dblCreditFXConvexityAdjustedDirtyDV01 += dblCreditFXConvexityAdjustedDirtyPeriodDV01;
			dblCreditFXConvexityAdjustedDirtyPV += dblCreditFXConvexityAdjustedDirtyPeriodDV01 * 10000. *
				dblPeriodFullRate;
			dblCumulativeConvexityAdjustedDirtyDV01 += dblCumulativeConvexityAdjustedDirtyPeriodDV01;
			dblCumulativeConvexityAdjustedDirtyPV += dblCumulativeConvexityAdjustedDirtyPeriodDV01 * 10000. *
				dblPeriodFullRate;
			dblForwardFundingConvexityAdjustedDirtyDV01 += dblForwardFundingConvexityAdjustedDirtyPeriodDV01;
			dblForwardFundingConvexityAdjustedDirtyPV += dblForwardFundingConvexityAdjustedDirtyPeriodDV01 *
				10000. * dblPeriodFullRate;
			dblForwardFXConvexityAdjustedDirtyDV01 += dblForwardFXConvexityAdjustedDirtyPeriodDV01;
			dblForwardFXConvexityAdjustedDirtyPV += dblForwardFXConvexityAdjustedDirtyPeriodDV01 * 10000. *
				dblPeriodFullRate;
			dblFundingFXConvexityAdjustedDirtyDV01 += dblFundingFXConvexityAdjustedDirtyPeriodDV01;
			dblFundingFXConvexityAdjustedDirtyPV += dblFundingFXConvexityAdjustedDirtyPeriodDV01 * 10000. *
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
		dblCreditForwardConvexityAdjustedDirtyPV /= dblCashPayDF;
		dblCreditForwardConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		dblCreditFundingConvexityAdjustedDirtyPV /= dblCashPayDF;
		dblCreditFundingConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		dblCreditFXConvexityAdjustedDirtyPV /= dblCashPayDF;
		dblCreditFXConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		dblCumulativeConvexityAdjustedDirtyPV /= dblCashPayDF;
		dblCumulativeConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		dblForwardFundingConvexityAdjustedDirtyPV /= dblCashPayDF;
		dblForwardFundingConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		dblForwardFXConvexityAdjustedDirtyPV /= dblCashPayDF;
		dblForwardFXConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		dblFundingFXConvexityAdjustedDirtyPV /= dblCashPayDF;
		dblFundingFXConvexityAdjustedDirtyDV01 /= dblCashPayDF;
		double dblAccrued = 0. == dblAccrued01 ? 0. : dblAccrued01 * 10000. * dblAccrualCoupon;
		double dblUnadjustedCleanPV = dblUnadjustedDirtyPV - dblAccrued;
		double dblUnadjustedCleanDV01 = dblUnadjustedDirtyDV01 - dblAccrued01;
		double dblUnadjustedFairPremium = 0.0001 * dblUnadjustedCleanPV / dblUnadjustedCleanDV01;
		double dblCompoundingAdjustedCleanPV = dblCompoundingAdjustedDirtyPV - dblAccrued;
		double dblCompoundingAdjustedCleanDV01 = dblCompoundingAdjustedDirtyDV01 - dblAccrued01;
		double dblCompoundingAdjustedFairPremium = 0.0001 * dblCompoundingAdjustedCleanPV /
			dblCompoundingAdjustedCleanDV01;
		double dblCreditForwardConvexityAdjustedCleanPV = dblCreditForwardConvexityAdjustedDirtyPV -
			dblAccrued;
		double dblCreditForwardConvexityAdjustedCleanDV01 = dblCreditForwardConvexityAdjustedDirtyDV01 -
			dblAccrued01;
		double dblCreditForwardConvexityAdjustedFairPremium = 0.0001 *
			dblCreditForwardConvexityAdjustedCleanPV / dblCreditForwardConvexityAdjustedCleanDV01;
		double dblCreditFundingConvexityAdjustedCleanPV = dblCreditFundingConvexityAdjustedDirtyPV -
			dblAccrued;
		double dblCreditFundingConvexityAdjustedCleanDV01 = dblCreditFundingConvexityAdjustedDirtyDV01 -
			dblAccrued01;
		double dblCreditFundingConvexityAdjustedFairPremium = 0.0001 *
			dblCreditFundingConvexityAdjustedCleanPV / dblCreditFundingConvexityAdjustedCleanDV01;
		double dblCreditFXConvexityAdjustedCleanPV = dblCreditFXConvexityAdjustedDirtyPV - dblAccrued;
		double dblCreditFXConvexityAdjustedCleanDV01 = dblCreditFXConvexityAdjustedDirtyDV01 - dblAccrued01;
		double dblCreditFXConvexityAdjustedFairPremium = 0.0001 * dblCreditFXConvexityAdjustedCleanPV /
			dblCreditFXConvexityAdjustedCleanDV01;
		double dblCumulativeConvexityAdjustedCleanPV = dblCumulativeConvexityAdjustedDirtyPV - dblAccrued;
		double dblCumulativeConvexityAdjustedCleanDV01 = dblCumulativeConvexityAdjustedDirtyDV01 -
			dblAccrued01;
		double dblCumulativeConvexityAdjustedFairPremium = 0.0001 * dblCumulativeConvexityAdjustedCleanPV /
			dblCumulativeConvexityAdjustedCleanDV01;
		double dblForwardFundingConvexityAdjustedCleanPV = dblForwardFundingConvexityAdjustedDirtyPV -
			dblAccrued;
		double dblForwardFundingConvexityAdjustedCleanDV01 = dblForwardFundingConvexityAdjustedDirtyDV01 -
			dblAccrued01;
		double dblForwardFundingConvexityAdjustedFairPremium = 0.0001 *
			dblForwardFundingConvexityAdjustedCleanPV / dblForwardFundingConvexityAdjustedCleanDV01;
		double dblForwardFXConvexityAdjustedCleanPV = dblForwardFXConvexityAdjustedDirtyPV - dblAccrued;
		double dblForwardFXConvexityAdjustedCleanDV01 = dblForwardFXConvexityAdjustedDirtyDV01 -
			dblAccrued01;
		double dblForwardFXConvexityAdjustedFairPremium = 0.0001 * dblForwardFXConvexityAdjustedCleanPV /
			dblForwardFXConvexityAdjustedCleanDV01;
		double dblFundingFXConvexityAdjustedCleanPV = dblFundingFXConvexityAdjustedDirtyPV - dblAccrued;
		double dblFundingFXConvexityAdjustedCleanDV01 = dblFundingFXConvexityAdjustedDirtyDV01 -
			dblAccrued01;
		double dblFundingFXConvexityAdjustedFairPremium = 0.0001 * dblFundingFXConvexityAdjustedCleanPV /
			dblFundingFXConvexityAdjustedCleanDV01;

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

		mapResult.put ("CompoundingAdjustmentPremium", dblCompoundingAdjustedCleanPV - dblUnadjustedCleanPV);

		mapResult.put ("CompoundingAdjustmentPremiumUpfront", (dblCompoundingAdjustedCleanPV -
			dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("CreditForwardConvexityAdjustedCleanDV01",
			dblCreditForwardConvexityAdjustedCleanDV01);

		mapResult.put ("CreditForwardConvexityAdjustedCleanPV", dblCreditForwardConvexityAdjustedCleanPV);

		mapResult.put ("CreditForwardConvexityAdjustedDirtyDV01",
			dblCreditForwardConvexityAdjustedDirtyDV01);

		mapResult.put ("CreditForwardConvexityAdjustedDirtyPV", dblCreditForwardConvexityAdjustedDirtyPV);

		mapResult.put ("CreditForwardConvexityAdjustedDV01", dblCreditForwardConvexityAdjustedDirtyDV01);

		mapResult.put ("CreditForwardConvexityAdjustedFairPremium",
			dblCreditForwardConvexityAdjustedFairPremium);

		mapResult.put ("CreditForwardConvexityAdjustedParRate",
			dblCreditForwardConvexityAdjustedFairPremium);

		mapResult.put ("CreditForwardConvexityAdjustedPV", dblCreditForwardConvexityAdjustedCleanPV);

		mapResult.put ("CreditForwardConvexityAdjustedRate", dblCreditForwardConvexityAdjustedFairPremium);

		mapResult.put ("CreditForwardConvexityAdjustedUpfront", dblCreditForwardConvexityAdjustedCleanPV);

		mapResult.put ("CreditForwardConvexityAdjustmentFactor", dblCreditForwardConvexityAdjustedDirtyDV01
			/ dblUnadjustedDirtyDV01);

		mapResult.put ("CreditForwardConvexityAdjustmentPremium", dblCreditForwardConvexityAdjustedCleanPV
			- dblUnadjustedCleanPV);

		mapResult.put ("CreditForwardConvexityAdjustmentPremiumUpfront",
			(dblCreditForwardConvexityAdjustedCleanPV - dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("CreditFundingConvexityAdjustedCleanDV01",
			dblCreditFundingConvexityAdjustedCleanDV01);

		mapResult.put ("CreditFundingConvexityAdjustedCleanPV", dblCreditFundingConvexityAdjustedCleanPV);

		mapResult.put ("CreditFundingConvexityAdjustedDirtyDV01",
			dblCreditFundingConvexityAdjustedDirtyDV01);

		mapResult.put ("CreditFundingConvexityAdjustedDirtyPV", dblCreditFundingConvexityAdjustedDirtyPV);

		mapResult.put ("CreditFundingConvexityAdjustedDV01", dblCreditFundingConvexityAdjustedDirtyDV01);

		mapResult.put ("CreditFundingConvexityAdjustedFairPremium",
			dblCreditFundingConvexityAdjustedFairPremium);

		mapResult.put ("CreditFundingConvexityAdjustedParRate",
			dblCreditFundingConvexityAdjustedFairPremium);

		mapResult.put ("CreditFundingConvexityAdjustedPV", dblCreditFundingConvexityAdjustedCleanPV);

		mapResult.put ("CreditFundingConvexityAdjustedRate", dblCreditFundingConvexityAdjustedFairPremium);

		mapResult.put ("CreditFundingConvexityAdjustedUpfront", dblCreditFundingConvexityAdjustedCleanPV);

		mapResult.put ("CreditFundingConvexityAdjustmentFactor", dblCreditFundingConvexityAdjustedDirtyDV01
			/ dblUnadjustedDirtyDV01);

		mapResult.put ("CreditFundingConvexityAdjustmentPremium", dblCreditFundingConvexityAdjustedCleanPV
			- dblUnadjustedCleanPV);

		mapResult.put ("CreditFundingConvexityAdjustmentPremiumUpfront",
			(dblCreditFundingConvexityAdjustedCleanPV - dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("CreditFXConvexityAdjustedCleanDV01", dblCreditFXConvexityAdjustedCleanDV01);

		mapResult.put ("CreditFXConvexityAdjustedCleanPV", dblCreditFXConvexityAdjustedCleanPV);

		mapResult.put ("CreditFXConvexityAdjustedDirtyDV01", dblCreditFXConvexityAdjustedDirtyDV01);

		mapResult.put ("CreditFXConvexityAdjustedDirtyPV", dblCreditFXConvexityAdjustedDirtyPV);

		mapResult.put ("CreditFXConvexityAdjustedDV01", dblCreditFXConvexityAdjustedDirtyDV01);

		mapResult.put ("CreditFXConvexityAdjustedFairPremium", dblCreditFXConvexityAdjustedFairPremium);

		mapResult.put ("CreditFXConvexityAdjustedParRate", dblCreditFXConvexityAdjustedFairPremium);

		mapResult.put ("CreditFXConvexityAdjustedPV", dblCreditFXConvexityAdjustedCleanPV);

		mapResult.put ("CreditFXConvexityAdjustedRate", dblCreditFXConvexityAdjustedFairPremium);

		mapResult.put ("CreditFXConvexityAdjustedUpfront", dblCreditFXConvexityAdjustedCleanPV);

		mapResult.put ("CreditFXConvexityAdjustmentFactor", dblCreditFXConvexityAdjustedDirtyDV01 /
			dblUnadjustedDirtyDV01);

		mapResult.put ("CreditFXConvexityAdjustmentPremium", dblCreditFXConvexityAdjustedCleanPV -
			dblUnadjustedCleanPV);

		mapResult.put ("CreditFXConvexityAdjustmentPremiumUpfront", (dblCreditFXConvexityAdjustedCleanPV -
			dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("CumulativeConvexityAdjustedCleanDV01", dblCumulativeConvexityAdjustedCleanDV01);

		mapResult.put ("CumulativeConvexityAdjustedCleanPV", dblCumulativeConvexityAdjustedCleanPV);

		mapResult.put ("CumulativeConvexityAdjustedDirtyDV01", dblCumulativeConvexityAdjustedDirtyDV01);

		mapResult.put ("CumulativeConvexityAdjustedDirtyPV", dblCumulativeConvexityAdjustedDirtyPV);

		mapResult.put ("CumulativeConvexityAdjustedDV01", dblCumulativeConvexityAdjustedDirtyDV01);

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

		mapResult.put ("ForwardFundingConvexityAdjustedCleanDV01",
			dblForwardFundingConvexityAdjustedCleanDV01);

		mapResult.put ("ForwardFundingConvexityAdjustedCleanPV", dblForwardFundingConvexityAdjustedCleanPV);

		mapResult.put ("ForwardFundingConvexityAdjustedDirtyDV01",
			dblForwardFundingConvexityAdjustedDirtyDV01);

		mapResult.put ("ForwardFundingConvexityAdjustedDirtyPV", dblForwardFundingConvexityAdjustedDirtyPV);

		mapResult.put ("ForwardFundingConvexityAdjustedDV01", dblForwardFundingConvexityAdjustedDirtyDV01);

		mapResult.put ("ForwardFundingConvexityAdjustedFairPremium",
			dblForwardFundingConvexityAdjustedFairPremium);

		mapResult.put ("ForwardFundingConvexityAdjustedParRate",
			dblForwardFundingConvexityAdjustedFairPremium);

		mapResult.put ("ForwardFundingConvexityAdjustedPV", dblForwardFundingConvexityAdjustedCleanPV);

		mapResult.put ("ForwardFundingConvexityAdjustedRate", dblForwardFundingConvexityAdjustedFairPremium);

		mapResult.put ("ForwardFundingConvexityAdjustedUpfront", dblForwardFundingConvexityAdjustedCleanPV);

		mapResult.put ("ForwardFundingConvexityAdjustmentFactor", dblForwardFundingConvexityAdjustedDirtyDV01
			/ dblUnadjustedDirtyDV01);

		mapResult.put ("ForwardFundingConvexityAdjustmentPremium", dblForwardFundingConvexityAdjustedCleanPV
			- dblUnadjustedCleanPV);

		mapResult.put ("ForwardFundingConvexityAdjustmentPremiumUpfront",
			(dblForwardFundingConvexityAdjustedCleanPV - dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("ForwardFXConvexityAdjustedCleanDV01", dblForwardFXConvexityAdjustedCleanDV01);

		mapResult.put ("ForwardFXConvexityAdjustedCleanPV", dblForwardFXConvexityAdjustedCleanPV);

		mapResult.put ("ForwardFXConvexityAdjustedDirtyDV01", dblForwardFXConvexityAdjustedDirtyDV01);

		mapResult.put ("ForwardFXConvexityAdjustedDirtyPV", dblForwardFXConvexityAdjustedDirtyPV);

		mapResult.put ("ForwardFXConvexityAdjustedDV01", dblForwardFXConvexityAdjustedDirtyDV01);

		mapResult.put ("ForwardFXConvexityAdjustedFairPremium", dblForwardFXConvexityAdjustedFairPremium);

		mapResult.put ("ForwardFXConvexityAdjustedParRate", dblForwardFXConvexityAdjustedFairPremium);

		mapResult.put ("ForwardFXConvexityAdjustedPV", dblForwardFXConvexityAdjustedCleanPV);

		mapResult.put ("ForwardFXConvexityAdjustedRate", dblForwardFXConvexityAdjustedFairPremium);

		mapResult.put ("ForwardFXConvexityAdjustedUpfront", dblForwardFXConvexityAdjustedCleanPV);

		mapResult.put ("ForwardFXConvexityAdjustmentFactor", dblForwardFXConvexityAdjustedDirtyDV01 /
			dblUnadjustedDirtyDV01);

		mapResult.put ("ForwardFXConvexityAdjustmentPremium", dblForwardFXConvexityAdjustedCleanPV -
			dblUnadjustedCleanPV);

		mapResult.put ("ForwardFXConvexityAdjustmentPremiumUpfront", (dblForwardFXConvexityAdjustedCleanPV -
			dblUnadjustedCleanPV) / dblValueNotional);

		mapResult.put ("FundingFXConvexityAdjustedCleanDV01", dblFundingFXConvexityAdjustedCleanDV01);

		mapResult.put ("FundingFXConvexityAdjustedCleanPV", dblFundingFXConvexityAdjustedCleanPV);

		mapResult.put ("FundingFXConvexityAdjustedDirtyDV01", dblFundingFXConvexityAdjustedDirtyDV01);

		mapResult.put ("FundingFXConvexityAdjustedDirtyPV", dblFundingFXConvexityAdjustedDirtyPV);

		mapResult.put ("FundingFXConvexityAdjustedDV01", dblFundingFXConvexityAdjustedDirtyDV01);

		mapResult.put ("FundingFXConvexityAdjustedFairPremium", dblFundingFXConvexityAdjustedFairPremium);

		mapResult.put ("FundingFXConvexityAdjustedParRate", dblFundingFXConvexityAdjustedFairPremium);

		mapResult.put ("FundingFXConvexityAdjustedPV", dblFundingFXConvexityAdjustedCleanPV);

		mapResult.put ("FundingFXConvexityAdjustedRate", dblFundingFXConvexityAdjustedFairPremium);

		mapResult.put ("FundingFXConvexityAdjustedUpfront", dblFundingFXConvexityAdjustedCleanPV);

		mapResult.put ("FundingFXConvexityAdjustmentFactor", dblFundingFXConvexityAdjustedDirtyDV01 /
			dblUnadjustedDirtyDV01);

		mapResult.put ("FundingFXConvexityAdjustmentPremium", dblFundingFXConvexityAdjustedCleanPV -
			dblUnadjustedCleanPV);

		mapResult.put ("FundingFXConvexityAdjustmentPremiumUpfront", (dblFundingFXConvexityAdjustedCleanPV -
			dblUnadjustedCleanPV) / dblValueNotional);

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
			double dblCreditForwardConvexityAdjustedCleanPrice = 100. * (1. +
				(dblCreditForwardConvexityAdjustedCleanPV / dblValueNotional));
			double dblCreditFundingConvexityAdjustedCleanPrice = 100. * (1. +
				(dblCreditFundingConvexityAdjustedCleanPV / dblValueNotional));
			double dblCreditFXConvexityAdjustedCleanPrice = 100. * (1. + (dblCreditFXConvexityAdjustedCleanPV
				/ dblValueNotional));
			double dblCumulativeConvexityAdjustedCleanPrice = 100. * (1. +
				(dblCumulativeConvexityAdjustedCleanPV / dblValueNotional));
			double dblForwardFundingConvexityAdjustedCleanPrice = 100. * (1. +
				(dblForwardFundingConvexityAdjustedCleanPV / dblValueNotional));
			double dblForwardFXConvexityAdjustedCleanPrice = 100. * (1. +
				(dblForwardFXConvexityAdjustedCleanPV / dblValueNotional));
			double dblFundingFXConvexityAdjustedCleanPrice = 100. * (1. +
				(dblFundingFXConvexityAdjustedCleanPV / dblValueNotional));
			double dblUnadjustedCleanPrice = 100. * (1. + (dblUnadjustedCleanPV / dblValueNotional));

			mapResult.put ("CleanPrice", dblCumulativeConvexityAdjustedCleanPrice);

			mapResult.put ("CompoundingAdjustedCleanPrice", dblCompoundingAdjustedCleanPrice);

			mapResult.put ("CompoundingAdjustedDirtyPrice", 100. * (1. + (dblCompoundingAdjustedDirtyPV /
				dblValueNotional)));

			mapResult.put ("CompoundingAdjustedPrice", dblCompoundingAdjustedCleanPrice);

			mapResult.put ("CreditForwardConvexityAdjustedCleanPrice",
				dblCreditForwardConvexityAdjustedCleanPrice);

			mapResult.put ("CreditForwardConvexityAdjustedDirtyPrice", 100. * (1. +
				(dblCreditForwardConvexityAdjustedDirtyPV / dblValueNotional)));

			mapResult.put ("CreditForwardConvexityAdjustedPrice",
				dblCreditForwardConvexityAdjustedCleanPrice);

			mapResult.put ("CreditFundingConvexityAdjustedCleanPrice",
				dblCreditFundingConvexityAdjustedCleanPrice);

			mapResult.put ("CreditFundingConvexityAdjustedDirtyPrice", 100. * (1. +
				(dblCreditFundingConvexityAdjustedDirtyPV / dblValueNotional)));

			mapResult.put ("CreditFundingConvexityAdjustedPrice",
				dblCreditFundingConvexityAdjustedCleanPrice);

			mapResult.put ("CreditFXConvexityAdjustedCleanPrice", dblCreditFXConvexityAdjustedCleanPrice);

			mapResult.put ("CreditFXConvexityAdjustedDirtyPrice", 100. * (1. +
				(dblCreditFXConvexityAdjustedDirtyPV / dblValueNotional)));

			mapResult.put ("CreditFXConvexityAdjustedPrice", dblCreditFXConvexityAdjustedCleanPrice);

			mapResult.put ("CumulativeConvexityAdjustedCleanPrice",
				dblCumulativeConvexityAdjustedCleanPrice);

			mapResult.put ("CumulativeConvexityAdjustedDirtyPrice", 100. * (1. +
				(dblCumulativeConvexityAdjustedDirtyPV / dblValueNotional)));

			mapResult.put ("CumulativeConvexityAdjustedPrice", dblCumulativeConvexityAdjustedCleanPrice);

			mapResult.put ("DirtyPrice", 100. * (1. + (dblCumulativeConvexityAdjustedDirtyPV /
				dblValueNotional)));

			mapResult.put ("ForwardFundingConvexityAdjustedCleanPrice",
				dblForwardFundingConvexityAdjustedCleanPrice);

			mapResult.put ("ForwardFundingConvexityAdjustedDirtyPrice", 100. * (1. +
				(dblForwardFundingConvexityAdjustedDirtyPV / dblValueNotional)));

			mapResult.put ("ForwardFundingConvexityAdjustedPrice",
				dblForwardFundingConvexityAdjustedCleanPrice);

			mapResult.put ("ForwardFXConvexityAdjustedCleanPrice", dblForwardFXConvexityAdjustedCleanPrice);

			mapResult.put ("ForwardFXConvexityAdjustedDirtyPrice", 100. * (1. +
				(dblForwardFXConvexityAdjustedDirtyPV / dblValueNotional)));

			mapResult.put ("ForwardFXConvexityAdjustedPrice", dblForwardFXConvexityAdjustedCleanPrice);

			mapResult.put ("FundingFXConvexityAdjustedCleanPrice", dblFundingFXConvexityAdjustedCleanPrice);

			mapResult.put ("FundingFXConvexityAdjustedDirtyPrice", 100. * (1. +
				(dblFundingFXConvexityAdjustedDirtyPV / dblValueNotional)));

			mapResult.put ("FundingFXConvexityAdjustedPrice", dblFundingFXConvexityAdjustedCleanPrice);

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
	 * Retrieve the set of the implemented measures
	 * 
	 * @return The set of the implemented measures
	 */

	public java.util.Set<java.lang.String> availableMeasures()
	{
		java.util.Set<java.lang.String> setstrMeasures = new java.util.TreeSet<java.lang.String>();

		setstrMeasures.add ("AccrualCoupon");

		setstrMeasures.add ("Accrued");

		setstrMeasures.add ("Accrued01");

		setstrMeasures.add ("CleanDV01");

		setstrMeasures.add ("CleanPV");

		setstrMeasures.add ("CompoundingAdjustedCleanDV01");

		setstrMeasures.add ("CompoundingAdjustedCleanPV");

		setstrMeasures.add ("CompoundingAdjustedDirtyPV");

		setstrMeasures.add ("CompoundingAdjustedDirtyDV01");

		setstrMeasures.add ("CompoundingAdjustedDirtyPV");

		setstrMeasures.add ("CompoundingAdjustedFairPremium");

		setstrMeasures.add ("CompoundingAdjustedParRate");

		setstrMeasures.add ("CompoundingAdjustedPV");

		setstrMeasures.add ("CompoundingAdjustedRate");

		setstrMeasures.add ("CompoundingAdjustedUpfront");

		setstrMeasures.add ("CompoundingAdjustmentFactor");

		setstrMeasures.add ("CompoundingAdjustmentPremium");

		setstrMeasures.add ("CompoundingAdjustmentPremiumUpfront");

		setstrMeasures.add ("CreditForwardConvexityAdjustedCleanDV01");

		setstrMeasures.add ("CreditForwardConvexityAdjustedCleanPV");

		setstrMeasures.add ("CreditForwardConvexityAdjustedDirtyDV01");

		setstrMeasures.add ("CreditForwardConvexityAdjustedDirtyPV");

		setstrMeasures.add ("CreditForwardConvexityAdjustedDV01");

		setstrMeasures.add ("CreditForwardConvexityAdjustedFairPremium");

		setstrMeasures.add ("CreditForwardConvexityAdjustedParRate");

		setstrMeasures.add ("CreditForwardConvexityAdjustedPV");

		setstrMeasures.add ("CreditForwardConvexityAdjustedRate");

		setstrMeasures.add ("CreditForwardConvexityAdjustedUpfront");

		setstrMeasures.add ("CreditForwardConvexityAdjustmentFactor");

		setstrMeasures.add ("CreditForwardConvexityAdjustmentPremium");

		setstrMeasures.add ("CreditForwardConvexityAdjustmentPremiumUpfront");

		setstrMeasures.add ("CreditFundingConvexityAdjustedCleanDV01");

		setstrMeasures.add ("CreditFundingConvexityAdjustedCleanPV");

		setstrMeasures.add ("CreditFundingConvexityAdjustedDirtyDV01");

		setstrMeasures.add ("CreditFundingConvexityAdjustedDirtyPV");

		setstrMeasures.add ("CreditFundingConvexityAdjustedDV01");

		setstrMeasures.add ("CreditFundingConvexityAdjustedFairPremium");

		setstrMeasures.add ("CreditFundingConvexityAdjustedParRate");

		setstrMeasures.add ("CreditFundingConvexityAdjustedPV");

		setstrMeasures.add ("CreditFundingConvexityAdjustedRate");

		setstrMeasures.add ("CreditFundingConvexityAdjustedUpfront");

		setstrMeasures.add ("CreditFundingConvexityAdjustmentFactor");

		setstrMeasures.add ("CreditFundingConvexityAdjustmentPremium");

		setstrMeasures.add ("CreditFundingConvexityAdjustmentPremiumUpfront");

		setstrMeasures.add ("CreditFXConvexityAdjustedCleanDV01");

		setstrMeasures.add ("CreditFXConvexityAdjustedCleanPV");

		setstrMeasures.add ("CreditFXConvexityAdjustedDirtyDV01");

		setstrMeasures.add ("CreditFXConvexityAdjustedDirtyPV");

		setstrMeasures.add ("CreditFXConvexityAdjustedDV01");

		setstrMeasures.add ("CreditFXConvexityAdjustedFairPremium");

		setstrMeasures.add ("CreditFXConvexityAdjustedParRate");

		setstrMeasures.add ("CreditFXConvexityAdjustedPV");

		setstrMeasures.add ("CreditFXConvexityAdjustedRate");

		setstrMeasures.add ("CreditFXConvexityAdjustedUpfront");

		setstrMeasures.add ("CreditFXConvexityAdjustmentFactor");

		setstrMeasures.add ("CreditFXConvexityAdjustmentPremium");

		setstrMeasures.add ("CreditFXConvexityAdjustmentPremiumUpfront");

		setstrMeasures.add ("CumulativeConvexityAdjustedCleanDV01");

		setstrMeasures.add ("CumulativeConvexityAdjustedCleanPV");

		setstrMeasures.add ("CumulativeConvexityAdjustedDirtyDV01");

		setstrMeasures.add ("CumulativeConvexityAdjustedDirtyPV");

		setstrMeasures.add ("CumulativeConvexityAdjustedDV01");

		setstrMeasures.add ("CumulativeConvexityAdjustedFairPremium");

		setstrMeasures.add ("CumulativeConvexityAdjustedParRate");

		setstrMeasures.add ("CumulativeConvexityAdjustedPV");

		setstrMeasures.add ("CumulativeConvexityAdjustedRate");

		setstrMeasures.add ("CumulativeConvexityAdjustedUpfront");

		setstrMeasures.add ("CumulativeConvexityAdjustmentFactor");

		setstrMeasures.add ("CumulativeConvexityAdjustmentPremium");

		setstrMeasures.add ("CumulativeConvexityAdjustmentPremiumUpfront");

		setstrMeasures.add ("CV01");

		setstrMeasures.add ("DirtyDV01");

		setstrMeasures.add ("DirtyPV");

		setstrMeasures.add ("DV01");

		setstrMeasures.add ("FairPremium");

		setstrMeasures.add ("Fixing01");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedCleanDV01");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedCleanPV");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedDirtyDV01");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedDirtyPV");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedDV01");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedFairPremium");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedParRate");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedPV");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedRate");

		setstrMeasures.add ("ForwardFundingConvexityAdjustedUpfront");

		setstrMeasures.add ("ForwardFundingConvexityAdjustmentFactor");

		setstrMeasures.add ("ForwardFundingConvexityAdjustmentPremium");

		setstrMeasures.add ("ForwardFundingConvexityAdjustmentPremiumUpfront");

		setstrMeasures.add ("ForwardFXConvexityAdjustedCleanDV01");

		setstrMeasures.add ("ForwardFXConvexityAdjustedCleanPV");

		setstrMeasures.add ("ForwardFXConvexityAdjustedDirtyDV01");

		setstrMeasures.add ("ForwardFXConvexityAdjustedDirtyPV");

		setstrMeasures.add ("ForwardFXConvexityAdjustedDV01");

		setstrMeasures.add ("ForwardFXConvexityAdjustedFairPremium");

		setstrMeasures.add ("ForwardFXConvexityAdjustedParRate");

		setstrMeasures.add ("ForwardFXConvexityAdjustedPV");

		setstrMeasures.add ("ForwardFXConvexityAdjustedRate");

		setstrMeasures.add ("ForwardFXConvexityAdjustedUpfront");

		setstrMeasures.add ("ForwardFXConvexityAdjustmentFactor");

		setstrMeasures.add ("ForwardFXConvexityAdjustmentPremium");

		setstrMeasures.add ("ForwardFXConvexityAdjustmentPremiumUpfront");

		setstrMeasures.add ("FundingFXConvexityAdjustedCleanDV01");

		setstrMeasures.add ("FundingFXConvexityAdjustedCleanPV");

		setstrMeasures.add ("FundingFXConvexityAdjustedDirtyDV01");

		setstrMeasures.add ("FundingFXConvexityAdjustedDirtyPV");

		setstrMeasures.add ("FundingFXConvexityAdjustedDV01");

		setstrMeasures.add ("FundingFXConvexityAdjustedFairPremium");

		setstrMeasures.add ("FundingFXConvexityAdjustedParRate");

		setstrMeasures.add ("FundingFXConvexityAdjustedPV");

		setstrMeasures.add ("FundingFXConvexityAdjustedRate");

		setstrMeasures.add ("FundingFXConvexityAdjustedUpfront");

		setstrMeasures.add ("FundingFXConvexityAdjustmentFactor");

		setstrMeasures.add ("FundingFXConvexityAdjustmentPremium");

		setstrMeasures.add ("FundingFXConvexityAdjustmentPremiumUpfront");

		setstrMeasures.add ("ParRate");

		setstrMeasures.add ("PV");

		setstrMeasures.add ("Rate");

		setstrMeasures.add ("ResetDate");

		setstrMeasures.add ("ResetRate");

		setstrMeasures.add ("TotalCoupon");

		setstrMeasures.add ("UnadjustedCleanDV01");

		setstrMeasures.add ("UnadjustedCleanPV");

		setstrMeasures.add ("UnadjustedDirtyDV01");

		setstrMeasures.add ("UnadjustedDirtyPV");

		setstrMeasures.add ("UnadjustedFairPremium");

		setstrMeasures.add ("UnadjustedParRate");

		setstrMeasures.add ("UnadjustedPV");

		setstrMeasures.add ("UnadjustedRate");

		setstrMeasures.add ("UnadjustedUpfront");

		setstrMeasures.add ("Upfront");

		return setstrMeasures;
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

		for (org.drip.analytics.cashflow.GenericCouponPeriod period : _lsCouponPeriod) {
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

		for (org.drip.analytics.cashflow.GenericCouponPeriod period : _lsCouponPeriod) {
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

		for (org.drip.analytics.cashflow.GenericCouponPeriod period : _lsCouponPeriod) {
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

			for (org.drip.analytics.cashflow.GenericCouponPeriod p : _lsCouponPeriod) {
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

				for (org.drip.analytics.cashflow.GenericCouponPeriod p : _lsCouponPeriod) {
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
}

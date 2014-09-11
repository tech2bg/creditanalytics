
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

public abstract class Stream extends org.drip.service.stream.Serializer {
	protected java.util.List<org.drip.analytics.period.CouponPeriod> _lsCouponPeriod = null;

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

		setCcy.add (_lsCouponPeriod.get (_lsCouponPeriod.size() - 1).payCurrency());

		return setCcy;
	}

	/**
	 * Retrieve the Coupon Currency
	 * 
	 * @return The Coupon Currency
	 */

	public java.lang.String couponCurrency()
	{
		return _lsCouponPeriod.get (_lsCouponPeriod.size() - 1).payCurrency();
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

	public abstract org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp);

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
			throw new java.lang.Exception ("FloatingStream de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate the periods");

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
}

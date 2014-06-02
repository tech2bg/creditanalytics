
package org.drip.product.rates;

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

public class FloatingStream extends org.drip.product.definition.RatesComponent {
	private static final boolean s_bBlog = false;

	protected double _dblNotional = 1.;
	protected double _dblSpread = 0.0001;
	protected boolean _bIsReference = true;
	protected java.lang.String _strCode = "";
	protected java.lang.String _strCurrency = "";
	protected double _dblMaturity = java.lang.Double.NaN;
	protected double _dblEffective = java.lang.Double.NaN;
	protected org.drip.product.params.FloatingRateIndex _fri = null;
	protected org.drip.product.params.FactorSchedule _notlSchedule = null;
	protected org.drip.param.valuation.CashSettleParams _settleParams = null;
	protected java.util.List<org.drip.analytics.period.CashflowPeriod> _lsCouponPeriod = null;

	/**
	 * Create an Instance of FloatingStream
	 * 
	 * @param dblEffective Effective Date
	 * @param dblMaturity Maturity Date
	 * @param dblSpread Spread
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * @param fri Floating Rate Index
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon Day Count
	 * @param bApplyCpnEOMAdj TRUE => Apply the Coupon EOM Adjustment
	 * @param strAccrualDC Accrual Day Count
	 * @param bApplyAccEOMAdj TRUE => Apply the Accrual EOM Adjustment
	 * @param bFullFirstPeriod TRUE => Generate full first-stub
	 * @param dapEffective Effective DAP
	 * @param dapMaturity Maturity DAP
	 * @param dapPeriodStart Period Start DAP
	 * @param dapPeriodEnd Period End DAP
	 * @param dapAccrualStart Accrual Start DAP
	 * @param dapAccrualEnd Accrual End DAP
	 * @param dapPay Pay DAP
	 * @param dapReset Reset DAP
	 * @param notlSchedule Notional Schedule
	 * @param dblNotional Initial Notional Amount
	 * @param strCurrency Pay Currency
	 * @param strCalendar Calendar
	 * 
	 * return Instance of FloatingStream
	 */

	public static FloatingStream Create (
		final double dblEffective,
		final double dblMaturity,
		final double dblSpread,
		final boolean bIsReference,
		final org.drip.product.params.FloatingRateIndex fri,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final boolean bFullFirstPeriod,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblNotional,
		final java.lang.String strCurrency,
		final java.lang.String strCalendar)
	{
		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsBackward (
				dblEffective, 		// Effective
				dblMaturity, 		// Maturity
				dapEffective, 		// Effective DAP
				dapMaturity, 		// Maturity DAP
				dapPeriodStart, 	// Period Start DAP
				dapPeriodEnd, 		// Period End DAP
				dapAccrualStart, 	// Accrual Start DAP
				dapAccrualEnd, 		// Accrual End DAP
				dapPay, 			// Pay DAP
				dapReset, 			// Reset DAP
				iFreq, 				// Coupon Freq
				strCouponDC, 		// Coupon Day Count
				bApplyCpnEOMAdj,
				strAccrualDC, 		// Accrual Day Count
				bApplyAccEOMAdj,
				bFullFirstPeriod,	// Full First Coupon Period?
				false, 				// Merge the first 2 Periods - create a long stub?
				false,
				strCalendar);

		try {
			return new FloatingStream (dblEffective, dblMaturity, dblSpread, bIsReference, fri, notlSchedule,
				dblNotional, strCurrency, lsCouponPeriod);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Instance of FloatingStream
	 * 
	 * @param dblEffective Effective Date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dblSpread Spread
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * @param fri Floating Rate Index
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon Day Count
	 * @param bApplyCpnEOMAdj TRUE => Apply the Coupon EOM Adjustment
	 * @param strAccrualDC Accrual Day Count
	 * @param bApplyAccEOMAdj TRUE => Apply the Accrual EOM Adjustment
	 * @param dapEffective Effective DAP
	 * @param dapMaturity Maturity DAP
	 * @param dapPeriodStart Period Start DAP
	 * @param dapPeriodEnd Period End DAP
	 * @param dapAccrualStart Accrual Start DAP
	 * @param dapAccrualEnd Accrual End DAP
	 * @param dapPay Pay DAP
	 * @param dapReset Reset DAP
	 * @param notlSchedule Notional Schedule
	 * @param dblNotional Initial Notional Amount
	 * @param strCurrency Pay Currency
	 * @param strCalendar Calendar
	 * 
	 * return Instance of FloatingStream
	 */

	public static FloatingStream Create (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final double dblSpread,
		final boolean bIsReference,
		final org.drip.product.params.FloatingRateIndex fri,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblNotional,
		final java.lang.String strCurrency,
		final java.lang.String strCalendar)
	{
		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriods (
				dblEffective, 			// Effective
				strMaturityTenor, 		// Maturity Tenor
				dapEffective, 			// Effective DAP
				dapMaturity, 			// Maturity DAP
				dapPeriodStart, 		// Period Start DAP
				dapPeriodEnd, 			// Period End DAP
				dapAccrualStart, 		// Accrual Start DAP
				dapAccrualEnd, 			// Accrual End DAP
				dapPay, 				// Pay DAP
				null, 					// Reset DAP
				iFreq, 					// Coupon Freq
				strCouponDC, 			// Coupon Day Count
				bApplyCpnEOMAdj,
				strAccrualDC, 			// Accrual Day Count
				bApplyAccEOMAdj,
				false,
				strCalendar);

		try {
			return new FloatingStream (dblEffective, new org.drip.analytics.date.JulianDate
				(dblEffective).addTenor (strMaturityTenor).getJulian(), dblSpread, bIsReference, fri,
					notlSchedule, dblNotional, strCurrency, lsCouponPeriod);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an Instance of FloatingStream
	 * 
	 * @param dblEffective Effective Date
	 * @param strMaturityTenor Maturity Tenor
	 * @param dblSpread Spread
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * @param fri Floating Rate Index
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon Day Count
	 * @param bApplyCpnEOMAdj TRUE => Apply the Coupon EOM Adjustment
	 * @param strAccrualDC Accrual Day Count
	 * @param bApplyAccEOMAdj TRUE => Apply the Accrual EOM Adjustment
	 * @param dap DAP
	 * @param notlSchedule Notional Schedule
	 * @param dblNotional Initial Notional Amount
	 * @param strCurrency Pay Currency
	 * @param strCalendar Calendar
	 * 
	 * return Instance of FloatingStream
	 */

	public static FloatingStream Create (
		final double dblEffective,
		final java.lang.String strMaturityTenor,
		final double dblSpread,
		final boolean bIsReference,
		final org.drip.product.params.FloatingRateIndex fri,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final org.drip.analytics.daycount.DateAdjustParams dap,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblNotional,
		final java.lang.String strCurrency,
		final java.lang.String strCalendar)
	{
		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriods (
				dblEffective, 			// Effective
				strMaturityTenor, 		// Maturity Tenor
				dap, 					// DAP
				iFreq, 					// Coupon Freq
				strCouponDC, 			// Coupon Day Count
				bApplyCpnEOMAdj,
				strAccrualDC, 			// Accrual Day Count
				bApplyAccEOMAdj,
				false,
				strCalendar);

		try {
			return new FloatingStream (dblEffective, new org.drip.analytics.date.JulianDate
				(dblEffective).addTenor (strMaturityTenor).getJulian(), dblSpread, bIsReference, fri,
					notlSchedule, dblNotional, strCurrency, lsCouponPeriod);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	protected double getFixing (
		final double dblValueDate,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.analytics.period.CashflowPeriod currentPeriod,
		final org.drip.param.definition.ComponentMarketParams mktParams)
		throws java.lang.Exception
	{
		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mapFixings =
				mktParams.fixings();

		if (null != mapFixings) {
			double dblCurrentResetDate = currentPeriod.getResetDate();

			if (org.drip.quant.common.NumberUtil.IsValid (dblCurrentResetDate)) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIndexFixing =
					mapFixings.get (new org.drip.analytics.date.JulianDate (dblCurrentResetDate));

				if (null != mapIndexFixing) {
					java.lang.Double dblFixing = mapIndexFixing.get (fri.fullyQualifiedName());

					if (null != dblFixing && org.drip.quant.common.NumberUtil.IsValid (dblFixing))
						return dblFixing + _dblSpread;
				}
			}
		}

		throw new java.lang.Exception ("FloatingStream::getRegularFixing => Cannot get Fixing");
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
	 * FloatingStream constructor
	 * 
	 * @param dblEffective Effective Date
	 * @param dblMaturity Maturity Date
	 * @param dblSpread Spread
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * @param fri Floating Rate Index
	 * @param notlSchedule Notional Schedule
	 * @param dblNotional Initial Notional Amount
	 * @param strCurrency Pay Currency
	 * @param lsCouponPeriod List of the Coupon Periods
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public FloatingStream (
		final double dblEffective,
		final double dblMaturity,
		final double dblSpread,
		final boolean bIsReference,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblNotional,
		final java.lang.String strCurrency,
		final java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblMaturity = dblMaturity) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblSpread = dblSpread) || null == (_fri = fri) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) || 0. ==
						_dblNotional || null == (_lsCouponPeriod = lsCouponPeriod) || 0 ==
							_lsCouponPeriod.size())
			throw new java.lang.Exception ("FloatingStream ctr => Invalid Input params! " + _fri);

		_bIsReference = bIsReference;

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();

		_dblEffective = _lsCouponPeriod.get (0).getStartDate();
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
			(getObjectTrailer()));

		if (null == strSerializedFloatingStream || strSerializedFloatingStream.isEmpty())
			throw new java.lang.Exception ("FloatingStream de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedFloatingStream,
			getFieldDelimiter());

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
			_fri = new org.drip.product.params.FloatingRateIndex (astrField[7].getBytes());

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
				getCollectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					if (null == _lsCouponPeriod)
						_lsCouponPeriod = new java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>();

					_lsCouponPeriod.add (new org.drip.analytics.period.CashflowPeriod
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

	@Override public java.lang.String componentName()
	{
		return "FloatingStream=" + org.drip.analytics.date.JulianDate.fromJulian (_dblMaturity);
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

	@Override public double coupon (
		final double dblValueDate,
		final org.drip.param.definition.ComponentMarketParams mktParams)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValueDate) || null == mktParams)
			throw new java.lang.Exception ("FloatingStream::coupon => Invalid Inputs");

		org.drip.analytics.period.CashflowPeriod currentPeriod = null;

		if (dblValueDate <= _dblEffective)
			currentPeriod = _lsCouponPeriod.get (0);
		else {
			for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
				if (null == period) continue;

				if (dblValueDate >= period.getStartDate() && dblValueDate <= period.getEndDate()) {
					currentPeriod = period;
					break;
				}
			}
		}

		if (null == currentPeriod)
			throw new java.lang.Exception ("FloatingStream::coupon => Invalid Inputs");

		try {
			return getFixing (dblValueDate, _fri, currentPeriod, mktParams);
		} catch (java.lang.Exception e) {
		}

		org.drip.analytics.rates.DiscountCurve dc = mktParams.fundingCurve();

		if (null == dc) throw new java.lang.Exception ("FloatingStream::getCoupon => cant determine index");

		double dblStartDate = currentPeriod.getStartDate();

		double dblEndDate = currentPeriod.getEndDate();

		double dblEpochDate = dc.epoch().getJulian();

		if (dblEpochDate > dblStartDate)
			dblEndDate = new org.drip.analytics.date.JulianDate (dblStartDate = dblEpochDate).addTenor
				(_fri.tenor()).getJulian();

		return dc.libor (dblStartDate, dblEndDate, currentPeriod.getCouponDCF()) + _dblSpread;
	}

	@Override public java.lang.String[] forwardCurveName()
	{
		return new java.lang.String[] {_fri.fullyQualifiedName()};
	}

	@Override public java.lang.String creditCurveName()
	{
		return "";
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
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).getEndDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> cashFlowPeriod()
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
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == mktParams) return null;

		org.drip.analytics.rates.DiscountCurve dc = mktParams.fundingCurve();

		if (null == dc) return null;

		long lStart = System.nanoTime();

		double dblDirtyPV = 0.;
		double dblFixing01 = 0.;
		double dblAccrued01 = 0.;
		double dblDirtyDV01 = 0.;
		boolean bFirstPeriod = true;
		double dblCashPayDF = java.lang.Double.NaN;
		double dblResetDate = java.lang.Double.NaN;
		double dblResetRate = java.lang.Double.NaN;

		java.lang.String strFRI = _fri.fullyQualifiedName();

		org.drip.analytics.rates.ForwardRateEstimator fc = mktParams.forwardCurve (_fri);

		for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
			double dblFloatingRate = 0.;
			double dblDirtyPeriodDV01 = java.lang.Double.NaN;

			double dblPeriodPayDate = period.getPayDate();

			if (dblPeriodPayDate < valParams.valueDate()) continue;

			try {
				if (bFirstPeriod) {
					bFirstPeriod = false;

					dblResetRate = dblFloatingRate = null == fc ? coupon (valParams.valueDate(),
						mktParams) : fc.forward (period.getPayDate());

					dblFixing01 = period.getAccrualDCF (valParams.valueDate()) * 0.0001 * notional
						(period.getAccrualStartDate(), valParams.valueDate());

					if (period.getStartDate() < valParams.valueDate()) dblAccrued01 = dblFixing01;

					dblResetDate = period.getResetDate();
				} else {
					double dblPeriodQuantoAdjust =
						org.drip.analytics.support.OptionHelper.MultiplicativeCrossVolQuanto (mktParams,
							strFRI, "ForwardToDomesticExchangeVolatility",
								"FRIForwardToDomesticExchangeCorrelation", valParams.valueDate(),
									period.getStartDate());

					dblFloatingRate = (null == fc ? dc.libor (period.getStartDate(), period.getPayDate(),
						period.getCouponDCF()) : fc.forward (period.getPayDate())) * dblPeriodQuantoAdjust;
				}

				dblDirtyPeriodDV01 = 0.0001 * period.getCouponDCF() * dc.df (dblPeriodPayDate) * notional
					(period.getAccrualStartDate(), period.getEndDate());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			if (s_bBlog) {
				try {
					System.out.println (new org.drip.analytics.date.JulianDate (period.getResetDate()) + " ["
						+ new org.drip.analytics.date.JulianDate (period.getStartDate()) + "->" + new
							org.drip.analytics.date.JulianDate (period.getEndDate()) + "] => " +
								org.drip.quant.common.FormatUtil.FormatDouble (dblFloatingRate, 1, 4, 100.));
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
			}

			dblDirtyDV01 += dblDirtyPeriodDV01;
			dblDirtyPV += dblDirtyPeriodDV01 * 10000. * (dblFloatingRate + _dblSpread);
		}

		try {
			double dblCashSettle = valParams.cashPayDate();

			if (null != _settleParams) dblCashSettle = _settleParams.cashSettleDate (valParams.valueDate());

			dblCashPayDF = dc.df (dblCashSettle);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		dblAccrued01 *= _dblNotional;
		dblDirtyDV01 *= (_dblNotional / dblCashPayDF);
		dblDirtyPV *= (_dblNotional / dblCashPayDF);
		double dblCleanDV01 = dblDirtyDV01 - dblAccrued01;
		double dblAccrued = dblAccrued01 * 10000. * (dblResetRate + _dblSpread);
		double dblCleanPV = dblDirtyPV - dblAccrued;
		double dblFairPremium = 0.0001 * dblCleanPV / dblCleanDV01;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put ("Accrued", dblAccrued);

		mapResult.put ("Accrued01", dblAccrued01);

		mapResult.put ("CleanDV01", dblCleanDV01);

		mapResult.put ("CleanPV", dblCleanPV);

		mapResult.put ("CV01", dblCleanDV01);

		mapResult.put ("DirtyDV01", dblDirtyDV01);

		mapResult.put ("DirtyPV", dblDirtyPV);

		mapResult.put ("DV01", dblCleanDV01);

		mapResult.put ("FairPremium", dblFairPremium);

		mapResult.put ("Fixing01", dblFixing01 * _dblNotional / dblCashPayDF);

		mapResult.put ("ParRate", dblFairPremium);

		mapResult.put ("PV", dblCleanPV);

		mapResult.put ("Rate", dblFairPremium);

		mapResult.put ("ResetDate", dblResetDate);

		mapResult.put ("ResetRate", dblResetRate);

		mapResult.put ("Upfront", dblCleanPV);

		double dblValueNotional = java.lang.Double.NaN;

		try {
			dblValueNotional = notional (valParams.valueDate());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
			double dblPrice = 100. * (1. + (dblCleanPV / _dblNotional / dblValueNotional));

			mapResult.put ("CleanPrice", dblPrice);

			mapResult.put ("DirtyPrice", 100. * (1. + (dblDirtyPV / _dblNotional / dblValueNotional)));

			mapResult.put ("Price", dblPrice);
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

		setstrMeasureNames.add ("Rate");

		setstrMeasureNames.add ("ResetDate");

		setstrMeasureNames.add ("ResetRate");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == mktParams || null ==
			mktParams.fundingCurve())
			return null;

		try {
			org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure = null;

			org.drip.analytics.rates.DiscountCurve dc = mktParams.fundingCurve();

			for (org.drip.analytics.period.CashflowPeriod p : _lsCouponPeriod) {
				double dblPeriodPayDate = p.getPayDate();

				if (p.getStartDate() < valParams.valueDate()) continue;

				org.drip.quant.calculus.WengertJacobian wjDForwardDManifestMeasure =
					dc.jackDForwardDManifestMeasure (p.getStartDate(), p.getEndDate(), "Rate",
						p.getCouponDCF());

				if (null == wjDForwardDManifestMeasure) continue;

				int iNumQuote = wjDForwardDManifestMeasure.numParameters();

				if (0 == iNumQuote) continue;

				org.drip.quant.calculus.WengertJacobian wjDPayDFDManifestMeasure = dc.jackDDFDManifestMeasure
					(dblPeriodPayDate, "Rate");

				if (null == wjDPayDFDManifestMeasure || iNumQuote !=
					wjDPayDFDManifestMeasure.numParameters())
					continue;

				double dblForward = dc.libor (p.getStartDate(), p.getEndDate());

				double dblPayDF = dc.df (dblPeriodPayDate);

				if (null == jackDDirtyPVDManifestMeasure)
					jackDDirtyPVDManifestMeasure = new org.drip.quant.calculus.WengertJacobian (1,
						iNumQuote);

				double dblPeriodNotional = _dblNotional * notional (p.getStartDate(), p.getEndDate());

				double dblPeriodDCF = p.getCouponDCF();

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
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == strManifestMeasure || null
			== mktParams || null == mktParams.fundingCurve())
			return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure) || "SwapRate".equalsIgnoreCase (strManifestMeasure))
		{
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, mktParams, quotingParams);

			if (null == mapMeasures) return null;

			double dblDirtyDV01 = mapMeasures.get ("DirtyDV01");

			double dblParSwapRate = mapMeasures.get ("SwapRate");

			try {
				org.drip.quant.calculus.WengertJacobian wjSwapRateDFMicroJack = null;

				org.drip.analytics.rates.DiscountCurve dc = mktParams.fundingCurve();

				for (org.drip.analytics.period.CashflowPeriod p : _lsCouponPeriod) {
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

					double dblPeriodNotional = notional (p.getStartDate(), p.getEndDate());

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

	private org.drip.state.estimator.PredictorResponseWeightConstraint forwardLatentStatePRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= maturity().getJulian()) return null;

		org.drip.analytics.rates.DiscountCurve dc = mktParams.fundingCurve();

		if (null == dc) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		double dblCleanCV100 = 0.;
		boolean bFirstPeriod = true;
		double dblDerivedParBasisSpread = 0.;

		java.lang.String[] astrManifestMeasure = lsmm.manifestMeasures();

		boolean bPV = org.drip.quant.common.StringUtil.MatchInStringArray ("PV", astrManifestMeasure, false);

		boolean bDerivedParBasisSpread = org.drip.quant.common.StringUtil.MatchInStringArray
			("DerivedParBasisSpread", astrManifestMeasure, false);

		boolean bReferenceParBasisSpread = org.drip.quant.common.StringUtil.MatchInStringArray
			("ReferenceParBasisSpread", astrManifestMeasure, false);

		try {
			if (bDerivedParBasisSpread)
				dblDerivedParBasisSpread = lsmm.measureQuoteValue ("DerivedParBasisSpread");

			for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
				if (null == period) continue;

				double dblPayDate = period.getPayDate();

				if (dblValueDate > dblPayDate) {
					bFirstPeriod = false;
					continue;
				}

				double dblPeriodDCF = period.getCouponDCF();

				if (bFirstPeriod) {
					bFirstPeriod = false;

					if (dblValueDate > period.getStartDate())
						dblPeriodDCF -= period.getAccrualDCF (dblValueDate);
				}

				double dblPeriodCV100 = dblPeriodDCF * dc.df (dblPayDate) * notional (dblPayDate);

				dblCleanCV100 += dblPeriodCV100;
				double dblNotionalPeriodCV100 = dblPeriodCV100 * _dblNotional;

				if (!_bIsReference) {
					if (!prwc.addPredictorResponseWeight (dblPayDate, dblNotionalPeriodCV100)) return null;

					if (bPV && !prwc.addDResponseWeightDManifestMeasure ("PV", dblPayDate,
						dblNotionalPeriodCV100))
						return null;

					if (bDerivedParBasisSpread && !prwc.addDResponseWeightDManifestMeasure
						("DerivedParBasisSpread", dblPayDate, dblNotionalPeriodCV100))
						return null;

					if (bReferenceParBasisSpread && !prwc.addDResponseWeightDManifestMeasure
						("ReferenceParBasisSpread", dblPayDate, dblNotionalPeriodCV100))
						return null;
				}
			}

			double dblNotionalCleanCV100 = dblCleanCV100 * _dblNotional;

			if (!_bIsReference) {
				if (!prwc.updateValue (-1. * dblNotionalCleanCV100 * dblDerivedParBasisSpread)) return null;

				if (bDerivedParBasisSpread && !prwc.updateDValueDManifestMeasure ("DerivedParBasisSpread",
					-1. * dblNotionalCleanCV100))
					return null;
			} else {
				if (bReferenceParBasisSpread && !prwc.updateDValueDManifestMeasure
					("ReferenceParBasisSpread", -1. * dblNotionalCleanCV100))
					return null;
			}

			return prwc;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private org.drip.state.estimator.PredictorResponseWeightConstraint discountLatentStatePRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		return valParams.valueDate() >= maturity().getJulian() ? null : new
			org.drip.state.estimator.PredictorResponseWeightConstraint();
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRLC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		if (null == valParams || null == lsmm || null == mktParams || !(lsmm instanceof
			org.drip.analytics.rates.RatesLSMM))
			return null;

		java.lang.String strQuantificationMetric = lsmm.quantificationMetric();

		if (null == strQuantificationMetric) return null;

		if (org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
			(strQuantificationMetric))
			return discountLatentStatePRWC (valParams, pricerParams, mktParams, quotingParams, lsmm);

		if (org.drip.analytics.rates.ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE.equalsIgnoreCase
			(strQuantificationMetric))
			return forwardLatentStatePRWC (valParams, pricerParams, mktParams, quotingParams, lsmm);

		return null;
	}

	/* @Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRLC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		if (null == valParams || null == lsmm || null == mktParams || !(lsmm instanceof
			org.drip.analytics.rates.RatesLSMM))
			return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= maturity().getJulian()) return null;

		java.lang.String strQuantificationMetric = lsmm.quantificationMetric();

		if (null == strQuantificationMetric) return null;

		if (!org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_FORWARD_RATE.equalsIgnoreCase
			(strQuantificationMetric) &&
				!org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
					(strQuantificationMetric))
			return null;

		org.drip.analytics.rates.DiscountCurve dc = mktParams.fundingCurve();

		if (null == dc) return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		double dblCleanCV100 = 0.;
		boolean bFirstPeriod = true;
		double dblDerivedParBasisSpread = 0.;

		java.lang.String[] astrManifestMeasure = lsmm.manifestMeasures();

		boolean bSwapRate = org.drip.quant.common.StringUtil.MatchInStringArray ("SwapRate",
			astrManifestMeasure, false);

		boolean bDerivedParBasisSpread = org.drip.quant.common.StringUtil.MatchInStringArray
			("DerivedParBasisSpread", astrManifestMeasure, false);

		boolean bReferenceParBasisSpread = org.drip.quant.common.StringUtil.MatchInStringArray
			("ReferenceParBasisSpread", astrManifestMeasure, false);

		try {
			if (bDerivedParBasisSpread)
				dblDerivedParBasisSpread = lsmm.measureQuoteValue ("DerivedParBasisSpread");

			for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
				if (null == period) continue;

				double dblPayDate = period.getPayDate();

				if (dblValueDate > dblPayDate) {
					bFirstPeriod = false;
					continue;
				}

				double dblPeriodDCF = period.getCouponDCF();

				if (bFirstPeriod) {
					bFirstPeriod = false;

					if (dblValueDate > period.getStartDate())
						dblPeriodDCF -= period.getAccrualDCF (dblValueDate);
				}

				double dblPeriodCV100 = dblPeriodDCF * dc.df (dblPayDate) * notional (dblPayDate);

				dblCleanCV100 += dblPeriodCV100;
				double dblNotionalPeriodCV100 = dblPeriodCV100 * _dblNotional;

				if (!_bIsReference) {
					if (!prwc.addPredictorResponseWeight (dblPayDate, dblNotionalPeriodCV100)) return null;

					if (bDerivedParBasisSpread && !prwc.addDResponseWeightDManifestMeasure
						("DerivedParBasisSpread", dblPayDate, dblNotionalPeriodCV100))
						return null;

					if (bReferenceParBasisSpread && !prwc.addDResponseWeightDManifestMeasure
						("ReferenceParBasisSpread", dblPayDate, dblNotionalPeriodCV100))
						return null;
				}

				if (bSwapRate && !prwc.addDResponseWeightDManifestMeasure ("SwapRate", dblPayDate,
					dblNotionalPeriodCV100))
					return null;
			}

			double dblNotionalCleanCV100 = dblCleanCV100 * _dblNotional;

			if (!_bIsReference) {
				if (!prwc.updateValue (-1. * dblNotionalCleanCV100 * dblDerivedParBasisSpread)) return null;

				if (bDerivedParBasisSpread && !prwc.updateDValueDManifestMeasure ("DerivedParBasisSpread",
					-1. * dblNotionalCleanCV100))
					return null;
			} else {
				if (bReferenceParBasisSpread && !prwc.updateDValueDManifestMeasure
					("ReferenceParBasisSpread", dblNotionalCleanCV100))
					return null;
			}

			return prwc;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	} */

	@Override public java.lang.String getFieldDelimiter()
	{
		return "!";
	}

	@Override public java.lang.String getObjectTrailer()
	{
		return "&";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		sb.append (_dblNotional + getFieldDelimiter());

		sb.append (_dblSpread + getFieldDelimiter());

		if (null == _strCurrency || _strCurrency.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strCurrency + getFieldDelimiter());

		if (null == _strCode || _strCode.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (_strCode + getFieldDelimiter());

		sb.append (_dblMaturity + getFieldDelimiter());

		sb.append (_dblEffective + getFieldDelimiter());

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

		if (null == _lsCouponPeriod)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbPeriods = new java.lang.StringBuffer();

			for (org.drip.analytics.period.CashflowPeriod p : _lsCouponPeriod) {
				if (null == p) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbPeriods.append (getCollectionRecordDelimiter());

				sbPeriods.append (new java.lang.String (p.serialize()));
			}

			if (sbPeriods.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
			else
				sb.append (sbPeriods.toString());
		}

		sb.append (getFieldDelimiter() + _bIsReference);

		return sb.append (getObjectTrailer()).toString().getBytes();
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

	public org.drip.product.params.FloatingRateIndex fri()
	{
		return _fri;
	}
}

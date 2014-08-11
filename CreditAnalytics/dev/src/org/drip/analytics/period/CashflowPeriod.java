
package org.drip.analytics.period;

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
 * CashflowPeriod extends the period class with the cash-flow specific fields. It exposes the following
 * 	functionality:
 * 
 * 	- Frequency, reset date, and accrual day-count convention
 * 	- Static methods to construct cash-flow period sets starting backwards/forwards, generate single period
 * 	 sets, as well as merge cash-flow periods.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CashflowPeriod extends org.drip.service.stream.Serializer implements
	java.lang.Comparable<CashflowPeriod> {
	private int _iFreq = 2;
	private boolean _bApplyAccEOMAdj = false;
	private boolean _bApplyCpnEOMAdj = false;
	private java.lang.String _strCalendar = "";
	private java.lang.String _strCurrency = "";
	private double _dblDCF = java.lang.Double.NaN;
	private double _dblEnd = java.lang.Double.NaN;
	private double _dblPay = java.lang.Double.NaN;
	private double _dblReset = java.lang.Double.NaN;
	private double _dblStart = java.lang.Double.NaN;
	private java.lang.String _strCouponDC = "30/360";
	private java.lang.String _strAccrualDC = "30/360";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblAccrualEnd = java.lang.Double.NaN;
	private double _dblAccrualStart = java.lang.Double.NaN;
	private double _dblEndDF = java.lang.Double.NaN;
	private double _dblSpread = java.lang.Double.NaN;
	private double _dblIndexRate = java.lang.Double.NaN;
	private double _dblEndNotional = java.lang.Double.NaN;
	private double _dblEndSurvival = java.lang.Double.NaN;
	private double _dblStartNotional = java.lang.Double.NaN;
	private double _dblFullCouponRate = java.lang.Double.NaN;

	/**
	 * Construct a CashflowPeriod instance from the specified dates
	 * 
	 * @param dblStart Period Start Date
	 * @param dblEnd Period End Date
	 * @param dblAccrualStart Period Accrual Start Date
	 * @param dblAccrualEnd Period Accrual End Date
	 * @param dblPay Period Pay Date
	 * @param dblReset Period Reset Date
	 * @param iFreq Frequency
	 * @param dblDCF Full Period Day Count Fraction
	 * @param strCouponDC Coupon day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param strAccrualDC Accrual Day count
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param dblMaturity Maturity date
	 * @param strCalendar Holiday Calendar
	 * @param strCurrency Cash Flow Currency
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CashflowPeriod (
		final double dblStart,
		final double dblEnd,
		final double dblAccrualStart,
		final double dblAccrualEnd,
		final double dblPay,
		final double dblReset,
		final int iFreq,
		final double dblDCF,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final double dblMaturity,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStart = dblStart) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEnd = dblEnd) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualStart = dblAccrualStart) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualEnd = dblAccrualEnd) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblPay = dblPay) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) || dblStart > dblEnd
								|| dblAccrualStart > dblAccrualEnd || null == (_strCurrency = strCurrency) ||
									_strCurrency.isEmpty())
			throw new java.lang.Exception ("CashflowPeriod ctr: Invalid inputs");

		_iFreq = iFreq;
		_dblReset = dblReset;
		_dblMaturity = dblMaturity;
		_strCalendar = strCalendar;
		_strCouponDC = strCouponDC;
		_strAccrualDC = strAccrualDC;
		_bApplyAccEOMAdj = bApplyAccEOMAdj;
		_bApplyCpnEOMAdj = bApplyCpnEOMAdj;
	}

	/**
	 * De-serialization of CashflowPeriod from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize
	 */

	public CashflowPeriod (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("CashflowPeriod de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Empty state");

		java.lang.String strPeriod = strRawString.substring (0, strRawString.indexOf
			(super.objectTrailer()));

		if (null == strPeriod || strPeriod.isEmpty())
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strPeriod,
			super.fieldDelimiter());

		if (null == astrField || 23 > astrField.length)
			throw new java.lang.Exception ("CashflowPeriod de-serialize: Invalid number of fields");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate start date");

		_dblStart = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate end date");

		_dblEnd = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate accrual start date");

		_dblAccrualStart = new java.lang.Double (astrField[3]);

		if (null == astrField[4] || astrField[4].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate accrual end date");

		_dblAccrualEnd = new java.lang.Double (astrField[4]);

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate pay date");

		_dblPay = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate Reset Date");

		_dblReset = new java.lang.Double (astrField[6]);

		if (null == astrField[7] || astrField[7].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate Coupon Day Count");

		_strCouponDC = new java.lang.String (astrField[7]);

		if (null == astrField[8] || astrField[8].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			throw new java.lang.Exception
				("CashflowPeriod de-serializer: Cannot locate Coupon EOM Adjustment");

		_bApplyCpnEOMAdj = new java.lang.Boolean (astrField[8]);

		if (null == astrField[9] || astrField[9].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[9]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate Accrual Day Count");

		_strAccrualDC = new java.lang.String (astrField[9]);

		if (null == astrField[10] || astrField[10].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[10]))
			throw new java.lang.Exception
				("CashflowPeriod de-serializer: Cannot locate Accrual EOM Adjustment");

		_bApplyAccEOMAdj = new java.lang.Boolean (astrField[10]);

		if (null == astrField[11] || astrField[11].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[11]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate Period DCF");

		_dblDCF = new java.lang.Double (astrField[11]);

		if (null == astrField[12] || astrField[12].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[12]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate end date");

		_iFreq = new java.lang.Integer (astrField[12]);

		if (null == astrField[13] || astrField[13].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[13]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate Pay Currency");

		_strCurrency = new java.lang.String (astrField[13]);

		if (null == astrField[14] || astrField[14].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[14]))
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate Calendar");

		_strCalendar = new java.lang.String (astrField[14]);

		if (null == astrField[15] || astrField[15].isEmpty())
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate Maturity");

		_dblMaturity = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[15]) ?
			java.lang.Double.NaN : new java.lang.Double (astrField[15]);

		if (null == astrField[16] || astrField[16].isEmpty())
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate Period End DF");

		_dblEndDF = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[16]) ?
			java.lang.Double.NaN : new java.lang.Double (astrField[16]);

		if (null == astrField[17] || astrField[17].isEmpty())
			throw new java.lang.Exception
				("CashflowPeriod de-serializer: Cannot locate Period Coupon Spread");

		_dblSpread = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[17]) ?
			java.lang.Double.NaN : new java.lang.Double (astrField[17]);

		if (null == astrField[18] || astrField[18].isEmpty())
			throw new java.lang.Exception ("CashflowPeriod de-serializer: Cannot locate Period Index Rate");

		_dblIndexRate = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[18]) ?
			java.lang.Double.NaN : new java.lang.Double (astrField[18]);

		if (null == astrField[19] || astrField[19].isEmpty())
			throw new java.lang.Exception
				("CashflowPeriod de-serializer: Cannot locate Period End Notional");

		_dblEndNotional = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[19])
			? java.lang.Double.NaN : new java.lang.Double (astrField[19]);

		if (null == astrField[20] || astrField[20].isEmpty())
			throw new java.lang.Exception
				("CashflowPeriod de-serializer: Cannot locate Period End Survival");

		_dblEndSurvival = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[20])
			? java.lang.Double.NaN : new java.lang.Double (astrField[20]);

		if (null == astrField[21] || astrField[21].isEmpty())
			throw new java.lang.Exception
				("CashflowPeriod de-serializer: Cannot locate Period Start Notional");

		_dblStartNotional = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase
			(astrField[21]) ? java.lang.Double.NaN : new java.lang.Double (astrField[21]);

		if (null == astrField[22] || astrField[22].isEmpty())
			throw new java.lang.Exception
				("CashflowPeriod de-serializer: Cannot locate Period Full Coupon Rate");

		_dblFullCouponRate = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[22]) ?
			java.lang.Double.NaN : new java.lang.Double (astrField[22]);
	}

	/**
	 * Return the period Start Date
	 * 
	 * @return Period Start Date
	 */

	public double start()
	{
		return _dblStart;
	}

	/**
	 * Return the period End Date
	 * 
	 * @return Period End Date
	 */

	public double end()
	{
		return _dblEnd;
	}

	/**
	 * Return the period Accrual Start Date
	 * 
	 * @return Period Accrual Start Date
	 */

	public double accrualStart()
	{
		return _dblAccrualStart;
	}

	/**
	 * Set the period Accrual Start Date
	 * 
	 * @param dblAccrualStart Period Accrual Start Date
	 */

	public boolean setAccrualStart (
		final double dblAccrualStart)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualStart)) return false;

		_dblAccrualStart = dblAccrualStart;
		return true;
	}

	/**
	 * Return the period Accrual End Date
	 * 
	 * @return Period Accrual End Date
	 */

	public double accrualEnd()
	{
		return _dblAccrualEnd;
	}

	/**
	 * Return the period Reset Date
	 * 
	 * @return Period Reset Date
	 */

	public double reset()
	{
		return _dblReset;
	}

	/**
	 * Return the period Pay Date
	 * 
	 * @return Period Pay Date
	 */

	public double pay()
	{
		return _dblPay;
	}

	/**
	 * Set the period Pay Date
	 * 
	 * @param dblPay Period Pay Date
	 */

	public boolean setPay (
		final double dblPay)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPay)) return false;

		_dblPay = dblPay;
		return true;
	}

	/**
	 * Get the period Accrual Day Count Fraction to an accrual end date
	 * 
	 * @param dblAccrualEnd Accrual End Date
	 * 
	 * @exception Throws if inputs are invalid, or if the date does not lie within the period
	 */

	public double accrualDCF (
		final double dblAccrualEnd)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualEnd))
			throw new java.lang.Exception ("CashflowPeriod::accrualDCF => Accrual end is NaN!");

		if (_dblAccrualStart > dblAccrualEnd && dblAccrualEnd > _dblAccrualEnd)
			throw new java.lang.Exception ("CashflowPeriod::accrualDCF => Invalid in-period accrual date!");

		org.drip.analytics.daycount.ActActDCParams actactDCParams = new
			org.drip.analytics.daycount.ActActDCParams (_iFreq, _dblAccrualStart, _dblAccrualEnd);

		return org.drip.analytics.daycount.Convention.YearFraction (_dblAccrualStart, dblAccrualEnd,
			_strAccrualDC, _bApplyAccEOMAdj, _dblMaturity, actactDCParams, _strCalendar) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblAccrualStart, _dblAccrualEnd,
					_strAccrualDC, _bApplyAccEOMAdj, _dblMaturity, actactDCParams, _strCalendar) * _dblDCF;
	}

	/**
	 * Get the coupon DCF
	 * 
	 * @return The coupon DCF
	 */

	public double couponDCF()
	{
		return _dblDCF;
	}
	
	/**
	 * Check whether the supplied date is inside the period specified
	 * 
	 * @param dblDate Date input
	 * 
	 * @return True indicates the specified date is inside the period
	 * 
	 * @throws java.lang.Exception Thrown if input is invalid
	 */

	public boolean contains (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("CashflowPeriod::contains => Invalid Inputs");

		if (_dblStart > dblDate || dblDate > _dblEnd) return false;

		return true;
	}

	/**
	 * Retrieve the Calendar
	 * 
	 * @return The Calendar
	 */

	public java.lang.String calendar()
	{
		return _strCalendar;
	}

	/**
	 * Retrieve the Currency
	 * 
	 * @return The Currency
	 */

	public java.lang.String currency()
	{
		return _strCurrency;
	}

	/**
	 * Retrieve the Accrual Day Count
	 * 
	 * @return The Accrual Day Count
	 */

	public java.lang.String accrualDC()
	{
		return _strAccrualDC;
	}

	/**
	 * Retrieve the Coupon Day Count
	 * 
	 * @return The Coupon Day Count
	 */

	public java.lang.String couponDC()
	{
		return _strCouponDC;
	}

	/**
	 * Retrieve the Coupon Frequency
	 * 
	 * @return The Coupon Frequency
	 */

	public int freq()
	{
		return _iFreq;
	}

	/**
	 * Retrieve the Coupon EOM Adjustment Flag
	 * 
	 * @return The Coupon EOM Adjustment Flag
	 */

	public boolean couponEODAdjustment()
	{
		return _bApplyCpnEOMAdj;
	}

	/**
	 * Retrieve the Accrual EOM Adjustment Flag
	 * 
	 * @return The Accrual EOM Adjustment Flag
	 */

	public boolean accrualEODAdjustment()
	{
		return _bApplyAccEOMAdj;
	}

	/**
	 * Get the period full coupon rate (annualized quote)
	 * 
	 * @return Period Full Coupon Rate
	 */

	public double fullCouponRate()
	{
		return _dblFullCouponRate;
	}

	/**
	 * Set the Full Coupon Rate
	 * 
	 * @param dblFullCouponRate The Full Coupon Rate
	 * 
	 * @return The Full Coupon Rate
	 */

	public boolean setFullCouponRate (
		final double dblFullCouponRate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblFullCouponRate)) return false;

		_dblFullCouponRate = dblFullCouponRate;
		return true;
	}

	/**
	 * Get the period spread over the floating index
	 * 
	 * @return Period Spread
	 */

	public double spread()
	{
		return _dblSpread;
	}

	/**
	 * Set the Coupon Spread
	 * 
	 * @param dblSpread The Coupon Spread
	 * 
	 * @return The Full Coupon Spread
	 */

	public boolean setSpread (
		final double dblSpread)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSpread)) return false;

		_dblSpread = dblSpread;
		return true;
	}

	/**
	 * Get the period index rate
	 * 
	 * @return Period Index Reference Rate
	 */

	public double indexRate()
	{
		return _dblIndexRate;
	}

	/**
	 * Set the Index Rate
	 * 
	 * @param dblIndexRate The Index Rate
	 * 
	 * @return The Index Rate
	 */

	public boolean setIndexRate (
		final double dblIndexRate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblIndexRate)) return false;

		_dblIndexRate = dblIndexRate;
		return true;
	}

	/**
	 * Get the period start Notional
	 * 
	 * @return Period Start Notional
	 */

	public double startNotional()
	{
		return _dblStartNotional;
	}

	/**
	 * Set the Starting Notional
	 * 
	 * @param dblStartNotional The Starting Notional
	 * 
	 * @return The Starting Notional
	 */

	public boolean setStartNotional (
		final double dblStartNotional)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblStartNotional)) return false;

		_dblStartNotional = dblStartNotional;
		return true;
	}

	/**
	 * Get the period end Notional
	 * 
	 * @return Period end Notional
	 */

	public double endNotional()
	{
		return _dblEndNotional;
	}

	/**
	 * Set the End Notional
	 * 
	 * @param dblEndNotional The End Notional
	 * 
	 * @return The End Notional
	 */

	public boolean setEndNotional (
		final double dblEndNotional)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEndNotional)) return false;

		_dblEndNotional = dblEndNotional;
		return true;
	}

	/**
	 * Get the period end discount factor
	 * 
	 * @return Period end discount factor
	 */

	public double endDF()
	{
		return _dblEndDF;
	}

	/**
	 * Set the End Discount Factor
	 * 
	 * @param dblEndDF The End Discount Factor
	 * 
	 * @return The End Discount Factor
	 */

	public boolean setEndDF (
		final double dblEndDF)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEndDF)) return false;

		_dblEndDF = dblEndDF;
		return true;
	}

	/**
	 * Get the period end survival probability
	 * 
	 * @return Period end survival probability
	 */

	public double endSurvival()
	{
		return _dblEndSurvival;
	}

	/**
	 * Set the End Survival Probability
	 * 
	 * @param dblEndDF The End Survival Probability
	 * 
	 * @return The End Survival Probability
	 */

	public boolean setEndSurvival (
		final double dblEndSurvival)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEndSurvival)) return false;

		_dblEndSurvival = dblEndSurvival;
		return true;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter() + _dblStart +
			fieldDelimiter() + _dblEnd + fieldDelimiter() + _dblAccrualStart + fieldDelimiter() +
				_dblAccrualEnd + fieldDelimiter() + _dblPay + fieldDelimiter() + _dblReset + fieldDelimiter()
					+ _strCouponDC + fieldDelimiter() + _bApplyCpnEOMAdj + fieldDelimiter() + _strAccrualDC +
						fieldDelimiter() + _bApplyAccEOMAdj + fieldDelimiter() + _dblDCF + fieldDelimiter() +
							_iFreq + fieldDelimiter() + _strCurrency + fieldDelimiter() + _strCalendar +
								fieldDelimiter() + _dblMaturity + fieldDelimiter() + _dblEndDF +
									fieldDelimiter() + _dblSpread + fieldDelimiter() + _dblIndexRate +
										fieldDelimiter() + _dblEndNotional + fieldDelimiter() +
											_dblEndSurvival + fieldDelimiter() + _dblStartNotional +
												fieldDelimiter() + _dblFullCouponRate);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new CashflowPeriod (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public int hashCode()
	{
		long lBits = java.lang.Double.doubleToLongBits ((int) _dblPay);

		return (int) (lBits ^ (lBits >>> 32));
	}

	@Override public int compareTo (
		final CashflowPeriod periodOther)
	{
		if ((int) _dblPay > (int) (periodOther._dblPay)) return 1;

		if ((int) _dblPay < (int) (periodOther._dblPay)) return -1;

		return 0;
	}
}

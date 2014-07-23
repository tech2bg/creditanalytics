
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
 * 
 * Period serves as a holder for the period dates. It implements the following functionality:
 * 	- API for period start/end, period accrual start/end, pay, and period day count fraction/containment
 *  - Comparison with the Other, equals/hash-code/comparator
 *  - Serialization/De-serialization to and from Byte Arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class Period extends org.drip.service.stream.Serializer implements java.lang.Comparable<Period> {
	protected double _dblDCF = java.lang.Double.NaN;
	protected double _dblEnd = java.lang.Double.NaN;
	protected double _dblPay = java.lang.Double.NaN;
	protected double _dblStart = java.lang.Double.NaN;
	protected double _dblAccrualEnd = java.lang.Double.NaN;
	protected double _dblAccrualStart = java.lang.Double.NaN;

	/**
	 * Construct a period object instance from the corresponding date parameters
	 * 
	 * @param dblStart Period Start Date
	 * @param dblEnd Period End Date
	 * @param dblAccrualStart Period Accrual Start Date
	 * @param dblAccrualEnd Period Accrual End Date
	 * @param dblPay Period Pay Date
	 * @param dblDCF Period Day count fraction
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public Period (
		final double dblStart,
		final double dblEnd,
		final double dblAccrualStart,
		final double dblAccrualEnd,
		final double dblPay,
		final double dblDCF)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStart = dblStart) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEnd = dblEnd) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualStart = dblAccrualStart) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualEnd = dblAccrualEnd) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) || dblStart > dblEnd ||
							dblAccrualStart > dblAccrualEnd) {
			System.out.println (org.drip.analytics.date.JulianDate.fromJulian (dblStart) + "=>" +
				org.drip.analytics.date.JulianDate.fromJulian (dblEnd));

			throw new java.lang.Exception ("Period ctr: Invalid inputs");
		}

		_dblPay = dblPay;
	}

	/**
	 * De-serialization of Period from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize
	 */

	public Period (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("Period de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("Period de-serializer: Empty state");

		java.lang.String strPeriod = strRawString.substring (0, strRawString.indexOf
			(super.objectTrailer()));

		if (null == strPeriod || strPeriod.isEmpty())
			throw new java.lang.Exception ("Period de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strPeriod,
			super.fieldDelimiter());

		if (null == astrField || 7 > astrField.length)
			throw new java.lang.Exception ("Period de-serialize: Invalid number of fields");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("Period de-serializer: Cannot locate start date");

		_dblStart = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("Period de-serializer: Cannot locate end date");

		_dblEnd = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception ("Period de-serializer: Cannot locate accrual start date");

		_dblAccrualStart = new java.lang.Double (astrField[3]);

		if (null == astrField[4] || astrField[4].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			throw new java.lang.Exception ("Period de-serializer: Cannot locate accrual end date");

		_dblAccrualEnd = new java.lang.Double (astrField[4]);

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception ("Period de-serializer: Cannot locate pay date");

		_dblPay = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception ("Period de-serializer: Cannot locate DCF");

		_dblDCF = new java.lang.Double (astrField[6]);
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
		return _dblStart;
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
			throw new java.lang.Exception ("Period::accrualDCF => Invalid Inputs");

		if (_dblAccrualStart > dblAccrualEnd || dblAccrualEnd > _dblAccrualEnd)
			throw new java.lang.Exception ("Period::accrualDCF => Invalid in-period accrual date!");

		return (dblAccrualEnd - _dblAccrualStart) * _dblDCF / (_dblAccrualEnd - _dblAccrualStart);
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
			throw new java.lang.Exception ("Period::contains => Invalid Inputs");

		if (_dblStart > dblDate || dblDate > _dblEnd) return false;

		return true;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "!";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "@";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + super.fieldDelimiter() + _dblStart +
			super.fieldDelimiter() + _dblEnd + super.fieldDelimiter() + _dblAccrualStart +
				super.fieldDelimiter() + _dblAccrualEnd + super.fieldDelimiter() + _dblPay +
					super.fieldDelimiter() + _dblDCF);

		return sb.append (super.objectTrailer()).toString().getBytes();
	}

	@Override public int hashCode()
	{
		long lBits = java.lang.Double.doubleToLongBits ((int) _dblPay);

		return (int) (lBits ^ (lBits >>> 32));
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new Period (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public int compareTo (
		final Period periodOther)
	{
		if ((int) _dblPay > (int) (periodOther._dblPay)) return 1;

		if ((int) _dblPay < (int) (periodOther._dblPay)) return -1;

		return 0;
	}
}

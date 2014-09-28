
package org.drip.product.params;

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
 * PeriodSet is the place-holder for the component’s period generation parameters. Contains the component's
 * 	date adjustment parameters for period start/end, period accrual start/end, effective, maturity, pay and
 * 	reset, first coupon date, and interest accrual start date. It exports serialization into and
 *  de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PeriodSet implements org.drip.product.params.Validatable {

	/**
	 * Coupon Frequency
	 */

	public int _iFreq = 2;

	/**
	 * Apply Coupon end-of-month adjustment
	 */

	public boolean _bApplyCpnEOMAdj = false;

	/**
	 * Coupon day count
	 */

	public java.lang.String _strCouponDC = "";

	/**
	 * Accrual day count
	 */

	public java.lang.String _strAccrualDC = "";

	/**
	 * Maturity Type
	 */

	public java.lang.String _strMaturityType = "";

	/**
	 * Maturity Date
	 */

	public double _dblMaturity = java.lang.Double.NaN;

	/**
	 * Effective Date
	 */

	public double _dblEffective = java.lang.Double.NaN;

	/**
	 * Final Maturity Date
	 */

	public double _dblFinalMaturity = java.lang.Double.NaN;

	protected java.util.List<org.drip.analytics.cashflow.CouponPeriod> _lsCouponPeriod = null;

	/**
	 * Construct PeriodSet from the effective date, day count, frequency, and the list of coupon periods
	 * 
	 * @param dblEffective Effective Date
	 * @param strDC Day count
	 * @param iFreq Frequency
	 * @param lsCouponPeriod List of Coupon Period
	 */

	public PeriodSet (
		final double dblEffective,
		final java.lang.String strDC,
		final int iFreq,
		final java.util.List<org.drip.analytics.cashflow.CouponPeriod> lsCouponPeriod)
	{
		_iFreq = iFreq;
		_strCouponDC = strDC;
		_strAccrualDC = strDC;
		_dblEffective = dblEffective;
		_lsCouponPeriod = lsCouponPeriod;
	}

	@Override public boolean validate()
	{
		if (null == _lsCouponPeriod || 0 == _lsCouponPeriod.size() ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEffective) || 0 == _iFreq)
			return false;

		for (org.drip.analytics.cashflow.CouponPeriod fp : _lsCouponPeriod) {
			if (null == fp || !org.drip.quant.common.NumberUtil.IsValid (_dblMaturity = fp.endDate()))
				return false;
		}

		_dblFinalMaturity = _dblMaturity;
		return true;
	}

	/**
	 * Retrieve a list of the component's coupon periods
	 * 
	 * @return List of Coupon Period
	 */

	public java.util.List<org.drip.analytics.cashflow.CouponPeriod> getPeriods()
	{
		return _lsCouponPeriod;
	}

	/**
	 * Return the first Coupon period
	 * 
	 * @return The first Coupon period
	 */

	public org.drip.analytics.cashflow.CouponPeriod getFirstPeriod()
	{
		return _lsCouponPeriod.get (0);
	}

	/**
	 * Returns the final Coupon period
	 * 
	 * @return The final Coupon period
	 */

	public org.drip.analytics.cashflow.CouponPeriod getLastPeriod()
	{
		return _lsCouponPeriod.get (_lsCouponPeriod.size() - 1);
	}

	/**
	 * Return the period index containing the specified date
	 * 
	 * @param dblDate Date input
	 * 
	 * @return Period index containing the date
	 * 
	 * @throws java.lang.Exception Thrown if the input date not in the period set range
	 */

	public int getPeriodIndex (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("PeriodSet::getPeriodIndex => Input date is NaN!");

		int i = 0;

		for (org.drip.analytics.cashflow.CouponPeriod period : _lsCouponPeriod) {
			if (period.contains (dblDate)) return i;

			++i;
		}

		throw new java.lang.Exception
			("PeriodSet::getPeriodIndex => Input date not in the period set range!");
	}
	
	/**
	 * Retrieve the period corresponding to the given index
	 * 
	 * @param iIndex Period index
	 * 
	 * @return Period object corresponding to the input index
	 */

	public org.drip.analytics.cashflow.CouponPeriod getPeriod (
		final int iIndex)
	{
		try {
			return _lsCouponPeriod.get (iIndex);
		} catch (java.lang.Exception e) {
		}

		return null;
	}
}

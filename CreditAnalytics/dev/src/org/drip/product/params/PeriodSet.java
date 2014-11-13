
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
	private int _iFreq = 2;
	private boolean _bApplyAccEOMAdj = false;
	private boolean _bApplyCpnEOMAdj = false;
	private java.lang.String _strCouponDC = "";
	private java.lang.String _strCurrency = "";
	private java.lang.String _strAccrualDC = "";
	private boolean _bPeriodsFromForward = false;
	private java.lang.String _strMaturityType = "";
	private double _dblCoupon = java.lang.Double.NaN;
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private double _dblFinalMaturity = java.lang.Double.NaN;
	private org.drip.analytics.daycount.DateAdjustParams _dapPay = null;
	private org.drip.analytics.daycount.DateAdjustParams _dapAccrual = null;
	private java.util.List<org.drip.analytics.cashflow.CompositePeriod> _lsCouponPeriod = null;

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
		final java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCouponPeriod)
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

		for (org.drip.analytics.cashflow.CompositePeriod fp : _lsCouponPeriod) {
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

	public java.util.List<org.drip.analytics.cashflow.CompositePeriod> getPeriods()
	{
		return _lsCouponPeriod;
	}

	/**
	 * Return the first Coupon period
	 * 
	 * @return The first Coupon period
	 */

	public org.drip.analytics.cashflow.CompositePeriod getFirstPeriod()
	{
		return _lsCouponPeriod.get (0);
	}

	/**
	 * Returns the final Coupon period
	 * 
	 * @return The final Coupon period
	 */

	public org.drip.analytics.cashflow.CompositePeriod getLastPeriod()
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

		for (org.drip.analytics.cashflow.CompositePeriod period : _lsCouponPeriod) {
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

	public org.drip.analytics.cashflow.CompositePeriod getPeriod (
		final int iIndex)
	{
		try {
			return _lsCouponPeriod.get (iIndex);
		} catch (java.lang.Exception e) {
		}

		return null;
	}

	/**
	 * Retrieve the Frequency
	 * 
	 * @return The Frequency
	 */

	public int freq()
	{
		return _iFreq;
	}

	/**
	 * Retrieve the Coupon EOM Adjustment
	 * 
	 * @return The Coupon EOM Adjustment
	 */

	public boolean couponEOMAdjustment()
	{
		return _bApplyCpnEOMAdj;
	}

	/**
	 * Set the Coupon EOM Adjustment
	 * 
	 * @param bApplyCpnEOMAdj The Coupon EOM Adjustment Flag
	 * 
	 * @return TRUE => The Coupon EOM Adjustment Successfully Set
	 */

	public boolean setCouponEOMAdjustment (
		final boolean bApplyCpnEOMAdj)
	{
		_bApplyCpnEOMAdj = bApplyCpnEOMAdj;
		return true;
	}

	/**
	 * Retrieve the Accrual EOM Adjustment
	 * 
	 * @return The Accrual EOM Adjustment
	 */

	public boolean accrualEOMAdjustment()
	{
		return _bApplyAccEOMAdj;
	}

	/**
	 * Set the Accrual EOM Adjustment
	 * 
	 * @param bApplyAccEOMAdj The Accrual EOM Adjustment Flag
	 * 
	 * @return TRUE => The Accrual EOM Adjustment Successfully Set
	 */

	public boolean setAccrualEOMAdjustment (
		final boolean bApplyAccEOMAdj)
	{
		_bApplyAccEOMAdj = bApplyAccEOMAdj;
		return true;
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
	 * Set the Coupon Day Count
	 * 
	 * @param strCouponDC The Coupon Day Count
	 * 
	 * @return TRUE => Coupon Day Count Successfully set
	 */

	public boolean setCouponDC (
		final java.lang.String strCouponDC)
	{
		_strCouponDC = strCouponDC;
		return true;
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
	 * Set the Accrual Day Count
	 * 
	 * @param strAccrualDC The Accrual Day Count
	 * 
	 * @return TRUE => Accrual Day Count Successfully set
	 */

	public boolean setAccrualDC (
		final java.lang.String strAccrualDC)
	{
		_strAccrualDC = strAccrualDC;
		return true;
	}

	/**
	 * Retrieve the Maturity Type
	 * 
	 * @return The Maturity Type
	 */

	public java.lang.String maturityType()
	{
		return _strMaturityType;
	}

	/**
	 * Set the Maturity Type
	 * 
	 * @param strMaturityType The Maturity Type
	 * 
	 * @return TRUE => Maturity Type Successfully set
	 */

	public boolean setMaturityType (
		final java.lang.String strMaturityType)
	{
		_strMaturityType = strMaturityType;
		return true;
	}

	/**
	 * Retrieve the Maturity Date
	 * 
	 * @return The Maturity Date
	 */

	public double maturity()
	{
		return _dblMaturity;
	}

	/**
	 * Set the Maturity Date
	 * 
	 * @param dblMaturity The Maturity Date
	 * 
	 * @return TRUE => Maturity Date Successfully set
	 */

	public boolean setMaturity (
		final double dblMaturity)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblMaturity)) return false;

		_dblMaturity = dblMaturity;
		return true;
	}

	/**
	 * Retrieve the Effective Date
	 * 
	 * @return The Effective Date
	 */

	public double effective()
	{
		return _dblEffective;
	}

	/**
	 * Set the Effective Date
	 * 
	 * @param dblEffective The Effective Date
	 * 
	 * @return TRUE => Effective Date Successfully set
	 */

	public boolean setEffective (
		final double dblEffective)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEffective)) return false;

		_dblEffective = dblEffective;
		return true;
	}

	/**
	 * Retrieve the Final Maturity Date
	 * 
	 * @return The FinalMaturity Date
	 */

	public double finalMaturity()
	{
		return _dblFinalMaturity;
	}

	/**
	 * Set the Final Maturity Date
	 * 
	 * @param dblFinalMaturity The Final Maturity Date
	 * 
	 * @return TRUE => Final Maturity Date Successfully set
	 */

	public boolean setFinalMaturity (
		final double dblFinalMaturity)
	{
		_dblFinalMaturity = dblFinalMaturity;
		return true;
	}

	/**
	 * Retrieve the List of Coupon Periods
	 * 
	 * @return The List of Coupon Periods
	 */

	public java.util.List<org.drip.analytics.cashflow.CompositePeriod> couponPeriodList()
	{
		return _lsCouponPeriod;
	}

	/**
	 * Set the List of Coupon Periods
	 * 
	 * @param lsCouponPeriod List of Coupon Periods
	 * 
	 * @return TRUE => The Coupon Period List successfully set
	 */

	public boolean setCouponPeriodList (
		final java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCouponPeriod)
	{
		_lsCouponPeriod = lsCouponPeriod;
		return true;
	}

	/**
	 * Retrieve the Coupon Currency
	 * 
	 * @return The Coupon Currency
	 */

	public java.lang.String currency()
	{
		return _strCurrency;
	}

	/**
	 * Set the Coupon Currency
	 * 
	 * @param strCurrency The Currency
	 * 
	 * @return TRUE => Currency Successfully set
	 */

	public boolean setCurrency (
		final java.lang.String strCurrency)
	{
		if (null == strCurrency || strCurrency.isEmpty()) return false;

		_strCurrency = strCurrency;
		return true;
	}

	/**
	 * Retrieve the Periods From Forward Generator Flag
	 * 
	 * @return The Periods From Forward Generator Flag
	 */

	public boolean periodsFromForward()
	{
		return _bPeriodsFromForward;
	}

	/**
	 * Set the Periods From Forward Generator Flag
	 * 
	 * @param bPeriodsFromForward The Periods From Forward Generator Flag
	 * 
	 * @return TRUE => The Periods From Forward Generator Flag Successfully Set
	 */

	public boolean setPeriodsFromForward (
		final boolean bPeriodsFromForward)
	{
		_bPeriodsFromForward = bPeriodsFromForward;
		return true;
	}

	/**
	 * Retrieve the Coupon
	 * 
	 * @return The Coupon
	 */

	public double coupon()
	{
		return _dblCoupon;
	}

	/**
	 * Set the Coupon
	 * 
	 * @param dblCoupon The Coupon
	 * 
	 * @return TRUE => Coupon Successfully set
	 */

	public boolean setCoupon (
		final double dblCoupon)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblCoupon)) return false;

		_dblCoupon = dblCoupon;
		return true;
	}

	/**
	 * Retrieve the Pay Date Adjust Parameters
	 * 
	 * @return The Pay Date Adjust Parameters
	 */

	public org.drip.analytics.daycount.DateAdjustParams dapPay()
	{
		return _dapPay;
	}

	/**
	 * Set the Pay Date Adjust Parameters
	 * 
	 * @param dapPay The Pay Date Adjust Parameters
	 * 
	 * @return TRUE => The Pay DAP successfully set
	 */

	public boolean setDAPPay (
		final org.drip.analytics.daycount.DateAdjustParams dapPay)
	{
		_dapPay = dapPay;
		return true;
	}

	/**
	 * Retrieve the Accrual End Date Adjust Parameters
	 * 
	 * @return The Accrual End Date Adjust Parameters
	 */

	public org.drip.analytics.daycount.DateAdjustParams dapAccrual()
	{
		return _dapAccrual;
	}

	/**
	 * Set the Accrual End Date Adjust Parameters
	 * 
	 * @param dapAccrual The Accrual End Date Adjust Parameters
	 * 
	 * @return TRUE => The Accrual End DAP successfully set
	 */

	public boolean setDAPAccrual (
		final org.drip.analytics.daycount.DateAdjustParams dapAccrual)
	{
		_dapAccrual = dapAccrual;
		return true;
	}
}

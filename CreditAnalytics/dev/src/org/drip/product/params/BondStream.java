
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
 * BondStream is the place-holder for the bond's period generation parameters. Contains the bond's
 * 	date adjustment parameters for period start/end, period accrual start/end, effective, maturity, pay and
 * 	reset, first coupon date, and interest accrual start date. It exports serialization into and
 *  de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class BondStream extends org.drip.product.rates.Stream {
	private java.lang.String _strMaturityType = "";
	private double _dblFinalMaturity = java.lang.Double.NaN;

	/**
	 * Construct and Instance of PeriodSet from the specified Parameters
	 * 
	 * @param dblMaturity Maturity Date
	 * @param dblEffective Effective Date
	 * @param dblFinalMaturity Final Maturity Date
	 * @param dblFirstCouponDate First Coupon Date
	 * @param dblInterestAccrualStart Interest Accrual Start Date
	 * @param iFreq Coupon Frequency
	 * @param dblCoupon Coupon Rate
	 * @param strCouponDC Coupon day count convention
	 * @param strAccrualDC Accrual day count convention
	 * @param dapPay Pay Date Adjustment Parameters
	 * @param dapReset Reset Date Adjustment Parameters
	 * @param dapMaturity Maturity Date Adjustment Parameters
	 * @param dapEffective Effective Date Adjustment Parameters
	 * @param dapPeriodEnd Period End Date Adjustment Parameters
	 * @param dapAccrualEnd Accrual Date Adjustment Parameters
	 * @param dapPeriodStart Period Start Date Adjustment Parameters
	 * @param dapAccrualStart Accrual Start  Date Adjustment Parameters
	 * @param strMaturityType Maturity Type
	 * @param bPeriodsFromForward Generate Periods forward (True) or Backward (False)
	 * @param strCalendar Optional Holiday Calendar for accrual calculations
	 * @param strCurrency Coupon Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @return PeriodSet Instance
	 */

	public static final BondStream Create (
		final double dblMaturity,
		final double dblEffective,
		final double dblFinalMaturity,
		final double dblFirstCouponDate,
		final double dblInterestAccrualStart,
		final int iFreq,
		final double dblCoupon,
		final java.lang.String strCouponDC,
		final java.lang.String strAccrualDC,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final java.lang.String strMaturityType,
		final boolean bPeriodsFromForward,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
	{
		boolean bCouponEOMAdj = null == strCouponDC ? false : strCouponDC.toUpperCase().contains ("EOM");

		int iCouponDCIndex = null == strCouponDC ? -1 : strCouponDC.indexOf (" NON");

		java.lang.String strCouponDCAdj = -1 != iCouponDCIndex ? strCouponDC.substring (0, iCouponDCIndex) :
			strCouponDC;

		boolean bAccrualEOMAdj = null == strAccrualDC ? false : strAccrualDC.toUpperCase().contains ("EOM");

		int iAccrualDCIndex = null == strAccrualDC ? -1 : strAccrualDC.indexOf (" NON");

		java.lang.String strAccrualDCAdj = -1 != iAccrualDCIndex ? strAccrualDC.substring (0,
			iAccrualDCIndex) : strAccrualDC;

		try {
			org.drip.analytics.date.JulianDate dtEffective = new org.drip.analytics.date.JulianDate
				(dblEffective);

			org.drip.analytics.date.JulianDate dtMaturity = new org.drip.analytics.date.JulianDate
				(dblMaturity);

			org.drip.param.period.UnitCouponAccrualSetting ucas = new
				org.drip.param.period.UnitCouponAccrualSetting (iFreq, strCouponDCAdj, bCouponEOMAdj,
					strAccrualDCAdj, bAccrualEOMAdj, strCurrency, true);

			java.lang.String strTenor = (12 / iFreq) + "M";
			java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCouponPeriod = null;

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting (iFreq, strTenor, strCurrency, null,
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC, 1.,
						null, null, null, null);

			java.util.List<java.lang.Double> lsStreamEdgeDate = bPeriodsFromForward ?
				org.drip.analytics.support.CompositePeriodBuilder.ForwardEdgeDates (dtEffective, dtMaturity,
					strTenor, dapAccrualEnd, org.drip.analytics.support.CompositePeriodBuilder.SHORT_STUB) :
						org.drip.analytics.support.CompositePeriodBuilder.BackwardEdgeDates (dtEffective,
							dtMaturity, strTenor, dapAccrualEnd,
								org.drip.analytics.support.CompositePeriodBuilder.SHORT_STUB);

			if (null == forwardLabel) {
				org.drip.param.period.ComposableFixedUnitSetting cfus = new
					org.drip.param.period.ComposableFixedUnitSetting (strTenor,
						org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR, null,
							dblCoupon, 0., strCurrency);

				lsCouponPeriod = org.drip.analytics.support.CompositePeriodBuilder.FixedCompositeUnit
					(lsStreamEdgeDate, cps, ucas, cfus);
			} else {
				org.drip.param.period.ComposableFloatingUnitSetting cfus = new
					org.drip.param.period.ComposableFloatingUnitSetting (strTenor,
						org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_REGULAR, null,
							forwardLabel,
								org.drip.analytics.support.CompositePeriodBuilder.REFERENCE_PERIOD_IN_ADVANCE,
					dblCoupon);

				lsCouponPeriod = org.drip.analytics.support.CompositePeriodBuilder.FloatingCompositeUnit
					(lsStreamEdgeDate, cps, cfus);
			}

			return new BondStream (lsCouponPeriod, dblFinalMaturity, strMaturityType);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct the BondStream instance from the list of coupon periods
	 * 
	 * @param lsCouponPeriod List of Coupon Period
	 * @param dblFinalMaturity Final Maturity Date
	 * @param strMaturityType Maturity Type
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public BondStream (
		final java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCouponPeriod,
		final double dblFinalMaturity,
		final java.lang.String strMaturityType)
		throws java.lang.Exception
	{
		super (lsCouponPeriod);

		_strMaturityType = strMaturityType;
		_dblFinalMaturity = dblFinalMaturity;
	}

	/**
	 * Return the first Coupon period
	 * 
	 * @return The first Coupon period
	 */

	public org.drip.analytics.cashflow.CompositePeriod firstPeriod()
	{
		return periods().get (0);
	}

	/**
	 * Returns the final Coupon period
	 * 
	 * @return The final Coupon period
	 */

	public org.drip.analytics.cashflow.CompositePeriod lastPeriod()
	{
		java.util.List<org.drip.analytics.cashflow.CompositePeriod> lsCouponPeriod = periods();

		return lsCouponPeriod.get (lsCouponPeriod.size() - 1);
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

	public int periodIndex (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("BondStream::periodIndex => Input date is NaN!");

		int i = 0;

		for (org.drip.analytics.cashflow.CompositePeriod period : periods()) {
			if (period.contains (dblDate)) return i;

			++i;
		}

		throw new java.lang.Exception ("BondStream::periodIndex => Input date not in the period set range!");
	}
	
	/**
	 * Retrieve the period corresponding to the given index
	 * 
	 * @param iIndex Period index
	 * 
	 * @return Period object corresponding to the input index
	 */

	public org.drip.analytics.cashflow.CompositePeriod period (
		final int iIndex)
	{
		try {
			return periods().get (iIndex);
		} catch (java.lang.Exception e) {
		}

		return null;
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
	 * Retrieve the Final Maturity Date
	 * 
	 * @return The FinalMaturity Date
	 */

	public double finalMaturity()
	{
		return _dblFinalMaturity;
	}
}

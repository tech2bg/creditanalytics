
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
 * PeriodGenerator generates the component coupon periods from flexible inputs. Periods can be generated
 * 	forwards or backwards, with long/short stubs. For good customization, date adjustment parameters can be
 * 	applied to each cash flow date of the period - effective, maturity, period start start/end, accrual
 *  start/end, pay and reset can each be generated according to the date adjustment rule applied to nominal
 *  period start/end.
 *
 * @author Lakshmi Krishnamurthy
 */

public class PeriodGenerator extends PeriodSet {
	private static final boolean m_bBlog = false;

	private boolean _bApplyAccEOMAdj = false;
	private java.lang.String _strCurrency = "";
	private boolean _bPeriodsFromForward = false;
	private org.drip.analytics.daycount.DateAdjustParams _dapPay = null;
	private org.drip.analytics.daycount.DateAdjustParams _dapAccrualEnd = null;

	/**
	 * Generate the coupon periods from the date rules and the date adjustment rules for the different
	 * 	period dates
	 * 
	 * @param dblMaturity Maturity Date
	 * @param dblEffective Effective Date
	 * @param dblFinalMaturity Final Maturity Date
	 * @param dblFirstCouponDate First Coupon Date
	 * @param dblInterestAccrualStart Interest Accrual Start Date
	 * @param iFreq Coupon Frequency
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
	 */

	public PeriodGenerator (
		final double dblMaturity,
		final double dblEffective,
		final double dblFinalMaturity,
		final double dblFirstCouponDate,
		final double dblInterestAccrualStart,
		final int iFreq,
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
		super (dblEffective, strCouponDC, iFreq, null);

		_dapPay = dapPay;
		_dblMaturity = dblMaturity;
		_strCurrency = strCurrency;
		_strAccrualDC = strAccrualDC;
		_dapAccrualEnd = dapAccrualEnd;
		_strMaturityType = strMaturityType;
		_dblFinalMaturity = dblFinalMaturity;
		_bPeriodsFromForward = bPeriodsFromForward;

		if (strCouponDC.toUpperCase().contains ("EOM")) _bApplyCpnEOMAdj = true;

		int iCouponDCIndex = strCouponDC.indexOf (" NON");

		if (-1 != iCouponDCIndex)
			_strCouponDC = strCouponDC.substring (0, iCouponDCIndex);
		else
			_strCouponDC = strCouponDC;

		if (strAccrualDC.toUpperCase().contains ("EOM")) _bApplyAccEOMAdj = true;

		int iAccrualDCIndex = strAccrualDC.indexOf (" NON");

		if (-1 != iAccrualDCIndex)
			_strAccrualDC = strAccrualDC.substring (0, iAccrualDCIndex);
		else
			_strAccrualDC = strAccrualDC;
	}

	@Override public boolean validate()
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEffective) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblMaturity))
			return false;

		if (null == _dapPay)
			_dapPay = new org.drip.analytics.daycount.DateAdjustParams
				(org.drip.analytics.daycount.Convention.DR_FOLL, "USD");

		try {
			org.drip.analytics.date.JulianDate dtEffective = new org.drip.analytics.date.JulianDate
				(_dblEffective);

			org.drip.analytics.date.JulianDate dtMaturity = new org.drip.analytics.date.JulianDate
				(_dblMaturity);

			if (m_bBlog)
				System.out.println ("Starting " + dtEffective + "->" + dtMaturity + " with freq " + _iFreq +
					" ...");

			org.drip.param.period.UnitCouponAccrualSetting ucas = new
				org.drip.param.period.UnitCouponAccrualSetting (_iFreq, _strCouponDC, _bApplyCpnEOMAdj,
					_strAccrualDC, _bApplyAccEOMAdj, _strCurrency, true);

			java.lang.String strTenor = (12 / _iFreq) + "M";

			org.drip.param.period.ComposableFixedUnitSetting cfus = new
				org.drip.param.period.ComposableFixedUnitSetting (strTenor,
					org.drip.analytics.support.CompositePeriodBuilder.EDGE_DATE_SEQUENCE_SINGLE, null, 0.,
						0., _strCurrency);

			org.drip.param.period.CompositePeriodSetting cps = new
				org.drip.param.period.CompositePeriodSetting (_iFreq, strTenor, _strCurrency, null,
					org.drip.analytics.support.CompositePeriodBuilder.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC, 1.,
						null, null, null, null);

			java.util.List<java.lang.Double> lsStreamEdgeDate = _bPeriodsFromForward ?
				org.drip.analytics.support.CompositePeriodBuilder.ForwardEdgeDates (dtEffective, dtMaturity,
					strTenor, _dapAccrualEnd, org.drip.analytics.support.CompositePeriodBuilder.SHORT_STUB) :
						org.drip.analytics.support.CompositePeriodBuilder.BackwardEdgeDates (dtEffective,
							dtMaturity, strTenor, _dapAccrualEnd,
								org.drip.analytics.support.CompositePeriodBuilder.SHORT_STUB);

			org.drip.product.rates.Stream stream = new org.drip.product.rates.Stream
				(org.drip.analytics.support.CompositePeriodBuilder.FixedCompositeUnit (lsStreamEdgeDate, cps,
					ucas, cfus));

			return null != stream && null != (_lsCouponPeriod = stream.cashFlowPeriod());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public java.util.List<org.drip.analytics.cashflow.CompositePeriod> getPeriods()
	{
		return _lsCouponPeriod;
	}
}

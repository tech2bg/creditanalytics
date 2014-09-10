
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
 * CouponPeriod extends the period class with the cash-flow specific fields. It exposes the following
 * 	functionality:
 * 
 * 	- Frequency, reset date, and accrual day-count convention
 * 	- Static methods to construct cash-flow period sets starting backwards/forwards, generate single period
 * 	 sets, as well as merge cash-flow periods.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CouponPeriod extends org.drip.service.stream.Serializer implements
	java.lang.Comparable<CouponPeriod> {

	/*
	 * Period Date Fields
	 */

	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblPayDate = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblFXFixingDate = java.lang.Double.NaN;
	private double _dblAccrualEndDate = java.lang.Double.NaN;
	private double _dblAccrualStartDate = java.lang.Double.NaN;
	private org.drip.analytics.period.ResetPeriodContainer _rpc = null;

	/*
	 * Period Date Generation Fields
	 */

	private int _iFreq = 2;
	private boolean _bApplyAccEOMAdj = false;
	private boolean _bApplyCpnEOMAdj = false;
	private java.lang.String _strCalendar = "";
	private double _dblDCF = java.lang.Double.NaN;
	private java.lang.String _strCouponDC = "30/360";
	private java.lang.String _strAccrualDC = "30/360";

	/*
	 * Period Latent State Identification Support Fields
	 */

	private java.lang.String _strPayCurrency = "";
	private java.lang.String _strCouponCurrency = "";
	private org.drip.state.identifier.CreditLabel _creditLabel = null;
	private org.drip.state.identifier.ForwardLabel _forwardLabel = null;

	/*
	 * Period Cash Extensive Fields
	 */

	private double _dblFixedCoupon = java.lang.Double.NaN;
	private double _dblFloatSpread = java.lang.Double.NaN;
	private double _dblBaseNotional = java.lang.Double.NaN;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;

	private double resetPeriodRate (
		final org.drip.analytics.period.ResetPeriod rp,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == csqs)
			throw new java.lang.Exception ("CouponPeriod::resetPeriodRate => Cannot locate CSQS");

		double dblFixingDate = rp.fixing();

		if (csqs.available (dblFixingDate, _forwardLabel))
			return csqs.getFixing (dblFixingDate, _forwardLabel);

		double dblResetEndDate = rp.end();

		org.drip.analytics.rates.ForwardRateEstimator fc = csqs.forwardCurve (_forwardLabel);

		if (null != fc) return fc.forward (dblResetEndDate);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel());

		if (null == dcFunding)
			throw new java.lang.Exception ("CouponPeriod::resetPeriodRate => Cannot locate Discount Curve");

		double dblResetStartDate = rp.start();

		double dblEpochDate = dcFunding.epoch().julian();

		if (dblEpochDate > dblResetStartDate)
			dblResetEndDate = new org.drip.analytics.date.JulianDate (dblResetStartDate =
				dblEpochDate).addTenor (_forwardLabel.tenor()).julian();

		return dcFunding.libor (dblResetStartDate, dblResetEndDate,
			org.drip.analytics.daycount.Convention.YearFraction (dblResetStartDate, dblResetEndDate,
				_strAccrualDC, _bApplyAccEOMAdj, null, _strCalendar));
	}

	private org.drip.analytics.output.ConvexityAdjustment calcConvexityAdjustment (
		final double dblValueDate,
		final double dblFixingDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		org.drip.state.identifier.CreditLabel creditLabel = creditLabel();

		org.drip.state.identifier.ForwardLabel forwardLabel = forwardLabel();

		org.drip.state.identifier.FundingLabel fundingLabel = fundingLabel();

		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		org.drip.analytics.output.ConvexityAdjustment convAdj = new
			org.drip.analytics.output.ConvexityAdjustment();

		try {
			if (!convAdj.setCreditForward (null != csqs && dblFixingDate > dblValueDate ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (csqs.creditCurveVolSurface
					(creditLabel), csqs.forwardCurveVolSurface (forwardLabel), csqs.creditForwardCorrSurface
						(creditLabel, forwardLabel), dblValueDate, dblFixingDate)) : 1.))
				return null;

			if (!convAdj.setCreditFunding (null != csqs ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (csqs.creditCurveVolSurface
					(creditLabel), csqs.fundingCurveVolSurface (fundingLabel), csqs.creditFundingCorrSurface
						(creditLabel, fundingLabel), dblValueDate, _dblPayDate)) : 1.))
				return null;

			if (!convAdj.setCreditFX (null != csqs && isFXMTM() ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (csqs.creditCurveVolSurface
					(creditLabel), csqs.fxCurveVolSurface (fxLabel), csqs.creditFXCorrSurface (creditLabel,
						fxLabel), dblValueDate, _dblPayDate)) : 1.))
				return null;

			if (!convAdj.setForwardFunding (null != csqs && dblFixingDate > dblValueDate ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
					(csqs.forwardCurveVolSurface (forwardLabel), csqs.fundingCurveVolSurface
						(fundingLabel), csqs.forwardFundingCorrSurface (forwardLabel, fundingLabel),
							dblValueDate, dblFixingDate)) : 1.))
				return null;

			if (!convAdj.setForwardFX (null != csqs && isFXMTM() && dblFixingDate > dblValueDate ?
				java.lang.Math.exp (org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
					(csqs.forwardCurveVolSurface (forwardLabel), csqs.fxCurveVolSurface (fxLabel),
						csqs.forwardFXCorrSurface (forwardLabel, fxLabel), dblValueDate, dblFixingDate)) :
							1.))
				return null;

			if (!convAdj.setFundingFX (null != csqs && isFXMTM() ? java.lang.Math.exp
				(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
					(csqs.fundingCurveVolSurface (fundingLabel), csqs.fxCurveVolSurface (fxLabel),
						csqs.fundingFXCorrSurface (fundingLabel, fxLabel), dblValueDate, _dblPayDate)) : 1.))
				return null;

			return convAdj;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private org.drip.analytics.output.ResetPeriodMetrics resetPeriodMetrics (
		final org.drip.analytics.period.ResetPeriod rp,
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		double dblResetPeriodStartDate = rp.start();

		double dblResetPeriodEndDate = rp.end();

		double dblResetPeriodFixingDate = rp.end();

		try {
			org.drip.analytics.output.ResetPeriodMetrics rpm = new
				org.drip.analytics.output.ResetPeriodMetrics (dblResetPeriodStartDate, dblResetPeriodEndDate,
					dblResetPeriodFixingDate, resetPeriodRate (rp, csqs) + _dblFloatSpread,
						org.drip.analytics.daycount.Convention.YearFraction (dblResetPeriodStartDate,
							dblResetPeriodEndDate, _strCouponDC, _bApplyAccEOMAdj, null, _strCalendar));

			if (org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC ==
				_rpc.accrualCompoundingRule() && !rpm.setConvAdj (calcConvexityAdjustment (dblValueDate,
					dblResetPeriodFixingDate, csqs)))
				return null;

			return rpm;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct a CouponPeriod instance from the specified dates
	 * 
	 * @param dblStartDate Period Start Date
	 * @param dblEndDate Period End Date
	 * @param dblAccrualStartDate Period Accrual Start Date
	 * @param dblAccrualEndDate Period Accrual End Date
	 * @param dblPayDate Period Pay Date
	 * @param rpc Reset Period Container
	 * @param dblFXFixingDate The FX Fixing Date for non-MTM'ed Cash-flow
	 * @param iFreq Frequency
	 * @param dblDCF Full Period Day Count Fraction
	 * @param strCouponDC Coupon day count
	 * @param strAccrualDC Accrual Day count
	 * @param bApplyCpnEOMAdj Apply end-of-month adjustment to the coupon periods
	 * @param bApplyAccEOMAdj Apply end-of-month adjustment to the accrual periods
	 * @param strCalendar Holiday Calendar
	 * @param dblBaseNotional Coupon Period Base Notional
	 * @param notlSchedule Coupon Period Notional Schedule
	 * @param dblFixedCouponFloatSpread Fixed Coupon/Float Spread
	 * @param strPayCurrency Pay Currency
	 * @param strCouponCurrency Coupon Currency
	 * @param forwardLabel The Forward Label
	 * @param creditLabel The Credit Label
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CouponPeriod (
		final double dblStartDate,
		final double dblEndDate,
		final double dblAccrualStartDate,
		final double dblAccrualEndDate,
		final double dblPayDate,
		final org.drip.analytics.period.ResetPeriodContainer rpc,
		final double dblFXFixingDate,
		final int iFreq,
		final double dblDCF,
		final java.lang.String strCouponDC,
		final java.lang.String strAccrualDC,
		final boolean bApplyCpnEOMAdj,
		final boolean bApplyAccEOMAdj,
		final java.lang.String strCalendar,
		final double dblBaseNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblFixedCouponFloatSpread,
		final java.lang.String strPayCurrency,
		final java.lang.String strCouponCurrency,
		final org.drip.state.identifier.ForwardLabel forwardLabel,
		final org.drip.state.identifier.CreditLabel creditLabel)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblStartDate = dblStartDate) ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblEndDate = dblEndDate) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualStartDate = dblAccrualStartDate) ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblAccrualEndDate = dblAccrualEndDate) ||
						!org.drip.quant.common.NumberUtil.IsValid (_dblPayDate = dblPayDate) ||
							!org.drip.quant.common.NumberUtil.IsValid (_dblDCF = dblDCF) || _dblStartDate >=
								_dblEndDate || _dblAccrualStartDate >= _dblAccrualEndDate ||
									!org.drip.quant.common.NumberUtil.IsValid (_dblBaseNotional =
										dblBaseNotional) || null == (_strPayCurrency = strPayCurrency) ||
											_strPayCurrency.isEmpty())
			throw new java.lang.Exception ("CouponPeriod ctr: Invalid inputs");

		_iFreq = iFreq;
		_creditLabel = creditLabel;
		_strCalendar = strCalendar;
		_strCouponDC = strCouponDC;
		_strAccrualDC = strAccrualDC;
		_bApplyAccEOMAdj = bApplyAccEOMAdj;
		_bApplyCpnEOMAdj = bApplyCpnEOMAdj;
		_dblFXFixingDate = dblFXFixingDate;

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();

		if (null != (_forwardLabel = forwardLabel)) {
			if (null == (_rpc = rpc) || !org.drip.quant.common.NumberUtil.IsValid (_dblFloatSpread =
				dblFixedCouponFloatSpread))
				throw new java.lang.Exception
					("CouponPeriod ctr: Invalid Forward/Reset/Float Spread Combination");

			_strCouponCurrency = _forwardLabel.currency();
		} else {
			if (!org.drip.quant.common.NumberUtil.IsValid (_dblFixedCoupon = dblFixedCouponFloatSpread) ||
				null == (_strCouponCurrency = strCouponCurrency) || _strCouponCurrency.isEmpty())
				throw new java.lang.Exception
					("CouponPeriod ctr: Invalid Fixed Coupon/Coupon Currency Combination");
		}
	}

	/**
	 * De-serialization of CouponPeriod from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize
	 */

	public CouponPeriod (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("CouponPeriod de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("CouponPeriod de-serializer: Empty state");

		java.lang.String strPeriod = strRawString.substring (0, strRawString.indexOf (objectTrailer()));

		if (null == strPeriod || strPeriod.isEmpty())
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strPeriod,
			super.fieldDelimiter());

		if (null == astrField || 23 > astrField.length)
			throw new java.lang.Exception ("CouponPeriod de-serialize: Invalid number of fields");

		// double dblVersion = new java.lang.Double (astrField[0]);

		/*
		 * Period Dates
		 */

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate start date");

		_dblStartDate = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate end date");

		_dblEndDate = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate accrual start date");

		_dblAccrualStartDate = new java.lang.Double (astrField[3]);

		if (null == astrField[4] || astrField[4].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate accrual end date");

		_dblAccrualEndDate = new java.lang.Double (astrField[4]);

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate pay date");

		_dblPayDate = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty())
			throw new java.lang.Exception
				("CouponPeriod de-serializer: Cannot locate Reset Period Container");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			_rpc = null;
		else
			_rpc = new org.drip.analytics.period.ResetPeriodContainer (astrField[6].getBytes());

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate FX Fixing Date");

		_dblFXFixingDate = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7])
			? java.lang.Double.NaN : new java.lang.Double (astrField[7]);

		/*
		 * Period Date Parameters - End
		 */

		/*
		 * Period Accrual/Coupon Fraction Generation Parameters
		 */

		if (null == astrField[8] || astrField[8].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate Frequency");

		_iFreq = new java.lang.Integer (astrField[8]);

		if (null == astrField[9] || astrField[9].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[9]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate Full Period DCF");

		_dblDCF = new java.lang.Double (astrField[9]);

		if (null == astrField[10] || astrField[10].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[10]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate Coupon Day Count");

		_strCouponDC = new java.lang.String (astrField[10]);

		if (null == astrField[11] || astrField[11].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[11]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate Accrual Day Count");

		_strAccrualDC = new java.lang.String (astrField[11]);

		if (null == astrField[12] || astrField[12].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[12]))
			throw new java.lang.Exception
				("CouponPeriod de-serializer: Cannot locate Coupon EOM Adjustment");

		_bApplyCpnEOMAdj = new java.lang.Boolean (astrField[12]);

		if (null == astrField[13] || astrField[13].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[13]))
			throw new java.lang.Exception
				("CouponPeriod de-serializer: Cannot locate Accrual EOM Adjustment");

		_bApplyAccEOMAdj = new java.lang.Boolean (astrField[13]);

		if (null == astrField[14] || astrField[14].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[14]))
			_strCalendar = "";
		else
			_strCalendar = new java.lang.String (astrField[14]);

		/*
		 * Period Accrual/Coupon Fraction Generation Parameters - End
		 */

		/*
		 * Period Latent State Identification Settings
		 */

		if (null == astrField[15] || astrField[15].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[15]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate Coupon Currency");

		_strCouponCurrency = new java.lang.String (astrField[15]);

		if (null == astrField[16] || astrField[16].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[16]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate Pay Currency");

		_strPayCurrency = new java.lang.String (astrField[16]);

		if (null == astrField[17] || astrField[17].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[17]))
			_forwardLabel = null;
		else
			_forwardLabel = org.drip.state.identifier.ForwardLabel.Standard (astrField[17]);

		if (null != astrField[18] && !astrField[18].isEmpty() &&
			!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[18]))
			_creditLabel = org.drip.state.identifier.CreditLabel.Standard (astrField[18]);

		/*
		 * Period Latent State Identification Settings - End
		 */

		/*
		 * Period "Extensive Cash" Parameter Settings
		 */

		if (null == astrField[19] || astrField[19].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[19]))
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate Period Base Notional");

		_dblBaseNotional = new java.lang.Double (astrField[19]);

		if (null == astrField[20] || astrField[20].isEmpty())
			throw new java.lang.Exception
				("CouponPeriod de-serializer: Cannot locate Period Notional Schedule");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[20]))
			_notlSchedule = null;
		else
			_notlSchedule = new org.drip.product.params.FactorSchedule (astrField[20].getBytes());

		if (null == astrField[21] || astrField[21].isEmpty())
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate Period Fixed Coupon");

		_dblFixedCoupon = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[21])
			? java.lang.Double.NaN : new java.lang.Double (astrField[21]);

		if (null == astrField[22] || astrField[22].isEmpty())
			throw new java.lang.Exception ("CouponPeriod de-serializer: Cannot locate Period Float Spread");

		_dblFloatSpread = org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[22])
			? java.lang.Double.NaN : new java.lang.Double (astrField[22]);

		/*
		 * Period "Extensive Cash" Parameter Settings - End
		 */
	}

	/**
	 * Return the period Start Date
	 * 
	 * @return Period Start Date
	 */

	public double startDate()
	{
		return _dblStartDate;
	}

	/**
	 * Return the period End Date
	 * 
	 * @return Period End Date
	 */

	public double endDate()
	{
		return _dblEndDate;
	}

	/**
	 * Return the period Accrual Start Date
	 * 
	 * @return Period Accrual Start Date
	 */

	public double accrualStartDate()
	{
		return _dblAccrualStartDate;
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
			throw new java.lang.Exception ("CouponPeriod::contains => Invalid Inputs");

		if (_dblStartDate > dblDate || dblDate > _dblEndDate) return false;

		return true;
	}

	/**
	 * Set the period Accrual Start Date
	 * 
	 * @param dblAccrualStartDate Period Accrual Start Date
	 * 
	 * @return TRUE => Accrual Start Date Successfully Set
	 */

	public boolean setAccrualStartDate (
		final double dblAccrualStartDate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblAccrualStartDate)) return false;

		_dblAccrualStartDate = dblAccrualStartDate;
		return true;
	}

	/**
	 * Return the period Accrual End Date
	 * 
	 * @return Period Accrual End Date
	 */

	public double accrualEndDate()
	{
		return _dblAccrualEndDate;
	}

	/**
	 * Return the period Pay Date
	 * 
	 * @return Period Pay Date
	 */

	public double payDate()
	{
		return _dblPayDate;
	}

	/**
	 * Set the period Pay Date
	 * 
	 * @param dblPayDate Period Pay Date
	 * 
	 * @return TRUE => Period Pay Date Successfully set
	 */

	public boolean setPayDate (
		final double dblPayDate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPayDate)) return false;

		_dblPayDate = dblPayDate;
		return true;
	}

	/**
	 * Retrieve the Reset Period Container Instance
	 * 
	 * @return The Reset Period Container Instance
	 */

	public org.drip.analytics.period.ResetPeriodContainer rpc()
	{
		return _rpc;
	}

	/**
	 * Return the period FX Fixing Date
	 * 
	 * @return Period FX Fixing Date
	 */

	public double fxFixingDate()
	{
		return _dblFXFixingDate;
	}

	/**
	 * Coupon Period FX
	 * 
	 * @param csqs Market Parameters
	 * 
	 * @return The Period FX
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double fx (
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		org.drip.state.identifier.FXLabel fxLabel = fxLabel();

		if (null == fxLabel) return 1.;

		if (null == csqs) throw new java.lang.Exception ("CouponPeriod::fx => Invalid Inputs");

		if (!isFXMTM()) return csqs.getFixing (_dblFXFixingDate, fxLabel);

		org.drip.quant.function1D.AbstractUnivariate auFX = csqs.fxCurve (fxLabel);

		if (null == auFX)
			throw new java.lang.Exception ("CouponPeriod::fx => No Curve for " +
				fxLabel.fullyQualifiedName());

		return auFX.evaluate (_dblPayDate);
	}

	/**
	 * Is this Cash Flow FX MTM'ed?
	 * 
	 * @return TRUE => FX MTM is on (i.e., FX is not driven by fixing)
	 */

	public boolean isFXMTM()
	{
		return !org.drip.quant.common.NumberUtil.IsValid (_dblFXFixingDate);
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
	 * Retrieve the Coupon EOM Adjustment Flag
	 * 
	 * @return The Coupon EOM Adjustment Flag
	 */

	public boolean couponEODAdjustment()
	{
		return _bApplyCpnEOMAdj;
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
	 * Retrieve the Accrual EOM Adjustment Flag
	 * 
	 * @return The Accrual EOM Adjustment Flag
	 */

	public boolean accrualEODAdjustment()
	{
		return _bApplyAccEOMAdj;
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
			throw new java.lang.Exception ("CouponPeriod::accrualDCF => Accrual end is NaN!");

		if (_dblAccrualStartDate > dblAccrualEnd && dblAccrualEnd > _dblAccrualEndDate)
			throw new java.lang.Exception ("CouponPeriod::accrualDCF => Invalid in-period accrual date!");

		org.drip.analytics.daycount.ActActDCParams actactDCParams = new
			org.drip.analytics.daycount.ActActDCParams (_iFreq, _dblAccrualStartDate, _dblAccrualEndDate);

		return org.drip.analytics.daycount.Convention.YearFraction (_dblAccrualStartDate, dblAccrualEnd,
			_strAccrualDC, _bApplyAccEOMAdj, actactDCParams, _strCalendar) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblAccrualStartDate,
					_dblAccrualEndDate, _strAccrualDC, _bApplyAccEOMAdj, actactDCParams, _strCalendar) *
						_dblDCF;
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
	 * Retrieve the Coupon Frequency
	 * 
	 * @return The Coupon Frequency
	 */

	public int freq()
	{
		return _iFreq;
	}

	/**
	 * Convert the Coupon Frequency into a Tenor
	 * 
	 * @return The Coupon Frequency converted into a Tenor
	 */

	public java.lang.String tenor()
	{
		int iTenorInMonths = 12 / _iFreq ;

		return 1 == iTenorInMonths || 2 == iTenorInMonths || 3 == iTenorInMonths || 6 == iTenorInMonths || 12
			== iTenorInMonths ? iTenorInMonths + "M" : "ON";
	}

	/**
	 * Retrieve the Pay Currency
	 * 
	 * @return The Pay Currency
	 */

	public java.lang.String payCurrency()
	{
		return _strPayCurrency;
	}

	/**
	 * Retrieve the Coupon Currency
	 * 
	 * @return The Coupon Currency
	 */

	public java.lang.String couponCurrency()
	{
		return _strCouponCurrency;
	}

	/**
	 * Get the Period Fixed Coupon Rate
	 * 
	 * @return Period Fixed Coupon Rate
	 */

	public double fixedCoupon()
	{
		return _dblFixedCoupon;
	}

	/**
	 * Set the Fixed Coupon Rate
	 * 
	 * @param dblFixedCoupon The Fixed Coupon Rate
	 * 
	 * @return TRUE => The Fixed Coupon Rate Set
	 */

	public boolean setFixedCoupon (
		final double dblFixedCoupon)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblFixedCoupon)) return false;

		_dblFixedCoupon = dblFixedCoupon;
		return true;
	}

	/**
	 * Get the period spread over the floating index
	 * 
	 * @return Period Float Spread
	 */

	public double floatSpread()
	{
		return _dblFloatSpread;
	}

	/**
	 * Set the Period Floater Spread
	 * 
	 * @param dblFloatSpread The Period Floater Spread
	 * 
	 * @return TRUE => The Period Floater Spread successfully set
	 */

	public boolean setFloatSpread (
		final double dblFloatSpread)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblFloatSpread)) return false;

		_dblFloatSpread = dblFloatSpread;
		return true;
	}

	/**
	 * Get the Period Base Notional
	 * 
	 * @return Period Base Notional
	 */

	public double baseNotional()
	{
		return _dblBaseNotional;
	}

	/**
	 * Get the period Notional Schedule
	 * 
	 * @return Period Notional Schedule
	 */

	public org.drip.product.params.FactorSchedule notionalSchedule()
	{
		return _notlSchedule;
	}

	/**
	 * Coupon Period Notional Corresponding to the specified Date
	 * 
	 * @param dblDate The Specified Date
	 * 
	 * @return The Period Notional Corresponding to the specified Date
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || !contains (dblDate))
			throw new java.lang.Exception ("CouponPeriod::notional => Invalid Inputs");

		return _dblBaseNotional * (null == _notlSchedule ? 1. : _notlSchedule.getFactor (dblDate));
	}

	/**
	 * Coupon Period Notional Aggregated over the specified Dates
	 * 
	 * @param dblDate1 The Date #1
	 * @param dblDate2 The Date #2
	 * 
	 * @return The Period Notional Aggregated over the specified Dates
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2) || !contains (dblDate1) || !contains (dblDate2))
			throw new java.lang.Exception ("Coupon::notional => Invalid Dates");

		return _dblBaseNotional * (null == _notlSchedule ? 1. : _notlSchedule.getFactor (dblDate1,
			dblDate2));
	}

	/**
	 * Return the Collateral Label
	 * 
	 * @return The Collateral Label
	 */

	public org.drip.state.identifier.CollateralLabel collateralLabel()
	{
		return org.drip.state.identifier.CollateralLabel.Standard (_strPayCurrency);
	}

	/**
	 * Return the Credit Label
	 * 
	 * @return The Credit Label
	 */

	public org.drip.state.identifier.CreditLabel creditLabel()
	{
		return _creditLabel;
	}

	/**
	 * Return the Forward Label
	 * 
	 * @return The Forward Label
	 */

	public org.drip.state.identifier.ForwardLabel forwardLabel()
	{
		return _forwardLabel;
	}

	/**
	 * Return the Funding Label
	 * 
	 * @return The Funding Label
	 */

	public org.drip.state.identifier.FundingLabel fundingLabel()
	{
		return org.drip.state.identifier.FundingLabel.Standard (_strPayCurrency);
	}

	/**
	 * Return the FX Label
	 * 
	 * @return The FX Label
	 */

	public org.drip.state.identifier.FXLabel fxLabel()
	{
		java.lang.String strCouponCurrency = couponCurrency();

		return _strPayCurrency.equalsIgnoreCase (strCouponCurrency) ? null :
			org.drip.state.identifier.FXLabel.Standard (_strPayCurrency + "/" + strCouponCurrency);
	}

	/**
	 * Compute the Coupon Measures at the specified Accrual End Date
	 * 
	 * @param dblValueDate Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Coupon Measures at the specified Accrual End Date
	 */

	public org.drip.analytics.output.CouponPeriodMetrics baseMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValueDate)) return null;

		double dblDF = 1.;
		double dblSurvival = 1.;

		org.drip.analytics.definition.CreditCurve cc = null == csqs ? null : csqs.creditCurve
			(creditLabel());

		org.drip.analytics.rates.DiscountCurve dcFunding = null == csqs ? null : csqs.fundingCurve
			(fundingLabel());

		int iAccrualCompoundingRule = null == _rpc ?
			org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC :
				_rpc.accrualCompoundingRule();

		java.util.List<org.drip.analytics.output.ResetPeriodMetrics> lsRPM = new
			java.util.ArrayList<org.drip.analytics.output.ResetPeriodMetrics>();

		try {
			double dblFX = fx (csqs);

			if (null != dcFunding) dblDF = dcFunding.df (_dblPayDate);

			if (null != cc) dblSurvival = cc.survival (_dblPayDate);

			if (null == _forwardLabel) {
				lsRPM.add (new org.drip.analytics.output.ResetPeriodMetrics (_dblStartDate, _dblEndDate,
					java.lang.Double.NaN, _dblFixedCoupon, _dblDCF));

				return org.drip.analytics.output.CouponPeriodMetrics.Create (_dblStartDate, _dblEndDate,
					_dblPayDate, notional (_dblEndDate), iAccrualCompoundingRule, lsRPM, dblSurvival, dblDF,
						dblFX, calcConvexityAdjustment (dblValueDate, _dblStartDate, csqs));
			}

			for (org.drip.analytics.period.ResetPeriod rp : _rpc.resetPeriods()) {
				org.drip.analytics.output.ResetPeriodMetrics rpm = resetPeriodMetrics (rp, dblValueDate,
					csqs);

				if (null == rpm) return null;

				lsRPM.add (rpm);
			}

			return org.drip.analytics.output.CouponPeriodMetrics.Create (_dblStartDate, _dblEndDate,
				_dblPayDate, notional (_dblEndDate), iAccrualCompoundingRule, lsRPM, dblSurvival, dblDF,
					dblFX, org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC ==
						iAccrualCompoundingRule ? calcConvexityAdjustment (dblValueDate, _dblStartDate, csqs)
							: null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Compute the Accrual Measures to the specified Accrual End Date
	 * 
	 * @param dblValueDate The Valuation Date
	 * @param csqs The Market Curve Surface/Quote Set
	 * 
	 * @return The Accrual Measures to the specified Accrual End Date
	 */

	public org.drip.analytics.output.CouponAccrualMetrics accrualMetrics (
		final double dblValueDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValueDate) || _dblStartDate == dblValueDate)
			return null;

		int iAccrualCompoundingRule = null == _rpc ?
			org.drip.analytics.support.ResetUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC :
				_rpc.accrualCompoundingRule();

		java.util.List<org.drip.analytics.output.ResetPeriodMetrics> lsRPM = new
			java.util.ArrayList<org.drip.analytics.output.ResetPeriodMetrics>();

		try {
			 if (!contains (dblValueDate)) return null;

			 double dblFX = fx (csqs);

			 if (null == _forwardLabel) {
				lsRPM.add (new org.drip.analytics.output.ResetPeriodMetrics (_dblStartDate, dblValueDate,
					java.lang.Double.NaN, _dblFixedCoupon,
						org.drip.analytics.daycount.Convention.YearFraction (_dblStartDate, dblValueDate,
							_strAccrualDC, _bApplyAccEOMAdj, null, _strCalendar)));

				return new org.drip.analytics.output.CouponAccrualMetrics (_dblStartDate, dblValueDate,
					dblFX, notional (dblValueDate), iAccrualCompoundingRule, lsRPM);
			}

			for (org.drip.analytics.period.ResetPeriod rp : _rpc.resetPeriods()) {
				double dblResetPeriodStartDate = rp.start();

				int iNodeLocationIndicator = rp.nodeLocation (dblValueDate);

				if (org.drip.analytics.period.ResetPeriod.NODE_LEFT_OF_SEGMENT == iNodeLocationIndicator ||
					dblValueDate == dblResetPeriodStartDate)
					break;

				org.drip.analytics.output.ResetPeriodMetrics rpm = resetPeriodMetrics (rp, dblValueDate,
					csqs);

				if (null == rpm) return null;

				if (org.drip.analytics.period.ResetPeriod.NODE_INSIDE_SEGMENT == iNodeLocationIndicator)
					lsRPM.add (new org.drip.analytics.output.ResetPeriodMetrics (dblResetPeriodStartDate,
						dblValueDate, dblResetPeriodStartDate, rpm.nominalRate(),
							org.drip.analytics.daycount.Convention.YearFraction (dblResetPeriodStartDate,
								dblValueDate, _strAccrualDC, _bApplyAccEOMAdj, null, _strCalendar)));
				else
					lsRPM.add (rpm);
			}

			return new org.drip.analytics.output.CouponAccrualMetrics (_dblStartDate, dblValueDate, dblFX,
				notional (dblValueDate), iAccrualCompoundingRule, lsRPM);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a set of loss period measures
	 * 
	 * @param comp Component for which the measures are to be generated
	 * @param valParams ValuationParams from which the periods are generated
	 * @param pricerParams PricerParams that control the generation characteristics
	 * @param dblWorkoutDate Double JulianDate representing the absolute end of all the generated periods
	 * @param csqs Market Parameters
	 *  
	 * @return The Generated Loss Quadrature Metrics
	 */

	public java.util.List<org.drip.analytics.period.LossQuadratureMetrics> lossMetrics (
		final org.drip.product.definition.CreditComponent comp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final double dblWorkoutDate,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		if (null == comp || null == valParams || null == pricerParams || null == csqs || null ==
			csqs.creditCurve (comp.creditLabel()[0]) || !org.drip.quant.common.NumberUtil.IsValid
				(dblWorkoutDate) || _dblStartDate > dblWorkoutDate)
			return null;

		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (_strPayCurrency));

		if (null == dc) return null;

		int iDiscretizationScheme = pricerParams.discretizationScheme();

		java.util.List<org.drip.analytics.period.LossQuadratureMetrics> lsLQM = null;
		double dblPeriodEndDate = _dblEndDate < dblWorkoutDate ? _dblEndDate : dblWorkoutDate;

		if (org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_DAY_STEP == iDiscretizationScheme &&
			(null == (lsLQM = org.drip.analytics.support.LossQuadratureGenerator.GenerateDayStepLossPeriods
				(comp, valParams, this, dblPeriodEndDate, pricerParams.unitSize(), csqs)) || 0 ==
					lsLQM.size()))
				return null;

		if (org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_PERIOD_STEP == iDiscretizationScheme &&
			(null == (lsLQM =
				org.drip.analytics.support.LossQuadratureGenerator.GeneratePeriodUnitLossPeriods (comp,
					valParams, this, dblPeriodEndDate, pricerParams.unitSize(), csqs)) || 0 ==
						lsLQM.size()))
			return null;

		if (org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_FULL_COUPON == iDiscretizationScheme &&
			(null == (lsLQM = org.drip.analytics.support.LossQuadratureGenerator.GenerateWholeLossPeriods
				(comp, valParams, this, dblPeriodEndDate, csqs)) || 0 == lsLQM.size()))
			return null;

		return lsLQM;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		sb.append (_dblStartDate + fieldDelimiter());

		sb.append (_dblEndDate + fieldDelimiter());

		sb.append (_dblAccrualStartDate + fieldDelimiter());

		sb.append (_dblAccrualEndDate + fieldDelimiter());

		sb.append (_dblPayDate + fieldDelimiter());

		if (null == _rpc)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else
			sb.append (new java.lang.String (_rpc.serialize()));

		sb.append (fieldDelimiter());

		sb.append (_dblFXFixingDate + fieldDelimiter());

		sb.append (_iFreq + fieldDelimiter());

		sb.append (_dblDCF + fieldDelimiter());

		sb.append (_strCouponDC + fieldDelimiter());

		sb.append (_strAccrualDC + fieldDelimiter());

		sb.append (_bApplyCpnEOMAdj + fieldDelimiter());

		sb.append (_bApplyAccEOMAdj + fieldDelimiter());

		if (null == _strCalendar || _strCalendar.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCalendar + fieldDelimiter());

		sb.append (_strCouponCurrency + fieldDelimiter());

		sb.append (_strPayCurrency + fieldDelimiter());

		if (null == _forwardLabel)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_forwardLabel.fullyQualifiedName() + fieldDelimiter());

		if (null == _creditLabel)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_creditLabel.fullyQualifiedName() + fieldDelimiter());

		sb.append (_dblBaseNotional + fieldDelimiter());

		if (null == _notlSchedule)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_notlSchedule.serialize()) + fieldDelimiter());

		sb.append (_dblFixedCoupon + fieldDelimiter());

		sb.append (_dblFloatSpread);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new CouponPeriod (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public int hashCode()
	{
		long lBits = java.lang.Double.doubleToLongBits ((int) _dblPayDate);

		return (int) (lBits ^ (lBits >>> 32));
	}

	@Override public int compareTo (
		final CouponPeriod periodOther)
	{
		if ((int) _dblPayDate > (int) (periodOther._dblPayDate)) return 1;

		if ((int) _dblPayDate < (int) (periodOther._dblPayDate)) return -1;

		return 0;
	}
}


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
 * LossPeriodCurveFactors is an implementation of the period class enhanced by the loss period measures. It
 * 	exports the following functionality:
 * 
 * 	- Start/end survival probabilities, period effective notional/recovery/discount factor
 * 	- Serialization into and de-serialization out of byte arrays
 *
 * @author Lakshmi Krishnamurthy
 */

public class LossQuadratureMetrics extends org.drip.service.stream.Serializer {
	private double _dblEndDate = java.lang.Double.NaN;
	private double _dblStartDate = java.lang.Double.NaN;
	private double _dblAccrualDCF = java.lang.Double.NaN;
	private double _dblEffectiveDF = java.lang.Double.NaN;
	private double _dblEndSurvival = java.lang.Double.NaN;
	private double _dblStartSurvival = java.lang.Double.NaN;
	private double _dblEffectiveNotional = java.lang.Double.NaN;
	private double _dblEffectiveRecovery = java.lang.Double.NaN;

	/**
	 * Create an instance of the LossPeriodCurveFactors class using the period's dates and curves to
	 * 	generate the curve measures
	 * 
	 * @param dblStart Period Start Date
	 * @param dblEnd Period End Date
	 * @param dblAccrualDCF Period's accrual day count fraction
	 * @param dblEffectiveNotional Period's effective notional
	 * @param dblEffectiveRecovery Period's effective recovery
	 * @param dc Discount Curve
	 * @param cc Credit Curve
	 * @param iDefaultLag Default Pay Lag
	 * 
	 * @return LossPeriodCurveFactors instance
	 */

	public static final LossQuadratureMetrics MakeDefaultPeriod (
		final double dblStart,
		final double dblEnd,
		final double dblAccrualDCF,
		final double dblEffectiveNotional,
		final double dblEffectiveRecovery,
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.definition.CreditCurve cc,
		final int iDefaultLag)
	{
		if (java.lang.Double.isNaN (dblStart) || java.lang.Double.isNaN (dblEnd) || java.lang.Double.isNaN
			(dblAccrualDCF) || java.lang.Double.isNaN (dblEffectiveNotional) || java.lang.Double.isNaN
				(dblEffectiveRecovery) || null == dc || null == cc)
			return null;

		try {
			return new LossQuadratureMetrics (dblStart, dblEnd, cc.survival (dblStart), cc.survival
				(dblEnd), dblAccrualDCF, dblEffectiveNotional, dblEffectiveRecovery, dc.effectiveDF (dblStart
					+ iDefaultLag, dblEnd + iDefaultLag));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a LossPeriodCurveFactors instance from the period dates and the curve measures
	 * 
	 * @param dblStart Period Start Date
	 * @param dblEnd Period End Date
	 * @param dblAccrualDCF Period Accrual day count fraction
	 * @param dblEffectiveNotional Period effective notional
	 * @param dc Discount Curve
	 * @param cc Credit Curve
	 * @param iDefaultLag Default Pay Lag
	 * 
	 * @return LossPeriodCurveFactors instance
	 */

	public static final LossQuadratureMetrics MakeDefaultPeriod (
		final double dblStart,
		final double dblEnd,
		final double dblAccrualDCF,
		final double dblEffectiveNotional,
		final org.drip.analytics.rates.DiscountCurve dc,
		final org.drip.analytics.definition.CreditCurve cc,
		final int iDefaultLag)
	{
		if (java.lang.Double.isNaN (dblStart) || java.lang.Double.isNaN (dblEnd) || java.lang.Double.isNaN
			(dblAccrualDCF) || java.lang.Double.isNaN (dblEffectiveNotional) || null == dc || null == cc)
			return null;

		try {
			return new LossQuadratureMetrics (dblStart, dblEnd, cc.survival (dblStart), cc.survival (dblEnd),
				dblAccrualDCF, dblEffectiveNotional, cc.effectiveRecovery (dblStart + iDefaultLag, dblEnd +
					iDefaultLag), dc.effectiveDF (dblStart + iDefaultLag, dblEnd + iDefaultLag));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Elaborate LossPeriodCurveFactors constructor
	 * 
	 * @param dblStartDate Start Date
	 * @param dblEndDate End Date
	 * @param dblStartSurvival Period Start Survival
	 * @param dblEndSurvival Period End Survival
	 * @param dblAccrualDCF Period Accrual DCF
	 * @param dblEffectiveNotional Period Effective Notional
	 * @param dblEffectiveRecovery Period Effective Recovery
	 * @param dblEffectiveDF Period Effective Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public LossQuadratureMetrics (
		final double dblStartDate,
		final double dblEndDate,
		final double dblStartSurvival,
		final double dblEndSurvival,
		final double dblAccrualDCF,
		final double dblEffectiveNotional,
		final double dblEffectiveRecovery,
		final double dblEffectiveDF)
		throws java.lang.Exception
	{
		if (java.lang.Double.isNaN (_dblStartDate = dblStartDate) || java.lang.Double.isNaN (_dblEndDate =
			dblEndDate) || java.lang.Double.isNaN (_dblStartSurvival = dblStartSurvival) ||
				java.lang.Double.isNaN (_dblEndSurvival = dblEndSurvival) || java.lang.Double.isNaN 
					(_dblAccrualDCF = dblAccrualDCF) || java.lang.Double.isNaN (_dblEffectiveNotional =
						dblEffectiveNotional) || java.lang.Double.isNaN (_dblEffectiveRecovery =
							dblEffectiveRecovery) || java.lang.Double.isNaN (_dblEffectiveDF =
								dblEffectiveDF))
			throw new java.lang.Exception ("LossPeriodCurveFactors ctr: Invalid params");
	}

	/**
	 * De-serialization of LossPeriodCurveFactors from byte stream
	 * 
	 * @param ab Byte stream
	 * 
	 * @throws java.lang.Exception Thrown if cannot properly de-serialize LossPeriodCurveFactors
	 */

	public LossQuadratureMetrics (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("LossPeriodCurveFactors de-serialize: Invalid byte stream input");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("LossPeriodCurveFactors de-serializer: Empty state");

		java.lang.String strCP = strRawString.substring (0, strRawString.indexOf (objectTrailer()));

		if (null == strCP || strCP.isEmpty())
			throw new java.lang.Exception ("LossPeriodCurveFactors de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strCP, fieldDelimiter());

		if (null == astrField || 9 > astrField.length)
			throw new java.lang.Exception ("LossPeriodCurveFactors de-serialize: Invalid number of fields");

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception
				("LossPeriodCurveFactors de-serializer: Cannot locate Start Date");

		_dblStartDate = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("LossPeriodCurveFactors de-serializer: Cannot locate End Date");

		_dblEndDate = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception
				("LossPeriodCurveFactors de-serializer: Cannot locate start survival");

		_dblStartSurvival = new java.lang.Double (astrField[3]);

		if (null == astrField[4] || astrField[4].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			throw new java.lang.Exception
				("LossPeriodCurveFactors de-serializer: Cannot locate end survival");

		_dblEndSurvival = new java.lang.Double (astrField[4]);

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception
				("LossPeriodCurveFactors de-serializer: Cannot locate effective notional");

		_dblEffectiveNotional = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception
				("LossPeriodCurveFactors de-serializer: Cannot locate effective recovery");

		_dblEffectiveRecovery = new java.lang.Double (astrField[6]);

		if (null == astrField[7] || astrField[7].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			throw new java.lang.Exception
				("LossPeriodCurveFactors de-serializer: Cannot locate effective DF");

		_dblEffectiveDF = new java.lang.Double (astrField[7]);

		if (null == astrField[8] || astrField[8].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			throw new java.lang.Exception
				("LossPeriodCurveFactors de-serializer: Cannot locate Accrual DCF");

		_dblAccrualDCF = new java.lang.Double (astrField[8]);
	}

	/**
	 * Period Start Date
	 * 
	 * @return Period Start Date
	 */

	public double start()
	{
		return _dblStartDate;
	}

	/**
	 * Survival Probability at the period beginning
	 * 
	 * @return Survival Probability at the period beginning
	 */

	public double startSurvival()
	{
		return _dblStartSurvival;
	}

	/**
	 * Period End Date
	 * 
	 * @return Period End Date
	 */

	public double end()
	{
		return _dblEndDate;
	}

	/**
	 * Survival at the period end
	 * 
	 * @return Survival at the period end
	 */

	public double endSurvival()
	{
		return _dblEndSurvival;
	}

	/**
	 * Get the period's effective notional
	 * 
	 * @return Period's effective notional
	 */

	public double effectiveNotional()
	{
		return _dblEffectiveNotional;
	}

	/**
	 * Get the period's effective recovery
	 * 
	 * @return Period's effective recovery
	 */

	public double effectiveRecovery()
	{
		return _dblEffectiveRecovery;
	}

	/**
	 * Get the period's effective discount factor
	 * 
	 * @return Period's effective discount factor
	 */

	public double effectiveDF()
	{
		return _dblEffectiveDF;
	}

	/**
	 * Get the period's Accrual Day Count Fraction
	 * 
	 * @return Period's Accrual Day Count Fraction
	 */

	public double accrualDCF()
	{
		return _dblAccrualDCF;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "#";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "^";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter() + _dblStartDate +
			fieldDelimiter() + _dblEndDate + fieldDelimiter() + _dblStartSurvival + fieldDelimiter() +
				_dblEndSurvival + fieldDelimiter() + _dblEffectiveNotional + fieldDelimiter() +
					_dblEffectiveRecovery + fieldDelimiter() + _dblEffectiveDF + fieldDelimiter() +
						_dblAccrualDCF);

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new LossQuadratureMetrics (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

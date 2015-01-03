
package org.drip.state.identifier;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * ForwardLabel contains the Index Parameters referencing a payment on a Forward Index. It provides the
 *  following functionality:
 *  - Indicate if the Index is an Overnight Index
 *  - Retrieve Index, Tenor, Currency, and Fully Qualified Name.
 *  - Serialization into and de-serialization out of byte arrays.
 *  
 * @author Lakshmi Krishnamurthy
 */

public class ForwardLabel implements org.drip.state.identifier.LatentStateLabel {
	private java.lang.String _strTenor = "";
	private org.drip.market.definition.FloaterIndex _floaterIndex = null;

	/**
	 * Construct a ForwardLabel from the corresponding Fully Qualified Name
	 * 
	 * @param strFullyQualifiedName The Fully Qualified Name
	 * 
	 * @return ForwardLabel Instance
	 */

	public static final ForwardLabel Standard (
		final java.lang.String strFullyQualifiedName)
	{
		if (null == strFullyQualifiedName || strFullyQualifiedName.isEmpty()) return null;

		java.lang.String[] astr = strFullyQualifiedName.split ("-");

		if (null == astr || 2 != astr.length) return null;

		java.lang.String strTenor = astr[1];
		java.lang.String strCurrency = astr[0];

		org.drip.market.definition.FloaterIndex floaterIndex = "ON".equalsIgnoreCase (strTenor) ||
			"1D".equalsIgnoreCase (strTenor) ?
				org.drip.market.definition.OvernightIndexContainer.IndexFromJurisdiction (strCurrency) :
					org.drip.market.definition.IBORIndexContainer.IndexFromJurisdiction (strCurrency);

		try {
			return new ForwardLabel (floaterIndex, strTenor);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct a ForwardLabel from the tenor and the index
	 * 
	 * @param floaterIndex The Floater Index Details
	 * @param strTenor Tenor
	 * 
	 * @return ForwardLabel Instance
	 */

	public static final ForwardLabel Create (
		final org.drip.market.definition.FloaterIndex floaterIndex,
		final java.lang.String strTenor)
	{
		try {
			return new ForwardLabel (floaterIndex, strTenor);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create from the Currency and the Tenor
	 * 
	 * @param strCurrency Currency
	 * @param strTenor Tenor
	 * 
	 * @return ForwardLabel Instance
	 */

	public static final ForwardLabel Create (
		final java.lang.String strCurrency,
		final java.lang.String strTenor)
	{
		return Standard (strCurrency + "-" + strTenor);
	}

	/**
	 * ForwardLabel constructor
	 * 
	 * @param floaterIndex The Floater Index Details
	 * @param strTenor Tenor
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	private ForwardLabel (
		final org.drip.market.definition.FloaterIndex floaterIndex,
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == (_floaterIndex = floaterIndex) || null == (_strTenor = strTenor) || _strTenor.isEmpty())
			throw new java.lang.Exception ("ForwardLabel ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Currency
	 * 
	 * @return The Currency
	 */

	public java.lang.String currency()
	{
		return _floaterIndex.currency();
	}

	/**
	 * Retrieve the Family
	 * 
	 * @return The Family
	 */

	public java.lang.String family()
	{
		return _floaterIndex.family();
	}

	/**
	 * Retrieve the Tenor
	 * 
	 * @return The Tenor
	 */

	public java.lang.String tenor()
	{
		return _strTenor;
	}

	/**
	 * Indicate if the Index is an Overnight Index
	 * 
	 * @return TRUE => Overnight Index
	 */

	public boolean overnight()
	{
		return "ON".equalsIgnoreCase (_strTenor) || "1D".equalsIgnoreCase (_strTenor);
	}

	/**
	 * Retrieve the Floater Index
	 * 
	 * @return The Floater Index
	 */

	public org.drip.market.definition.FloaterIndex floaterIndex()
	{
		return _floaterIndex;
	}

	/**
	 * Retrieve a Unit Coupon Accrual Setting
	 * 
	 * @return Unit Coupon Accrual Setting
	 */

	public org.drip.param.period.UnitCouponAccrualSetting ucas()
	{
		java.lang.String strDayCount = _floaterIndex.dayCount();

		try {
			return new org.drip.param.period.UnitCouponAccrualSetting (overnight() ? 360 :
				org.drip.analytics.support.AnalyticsHelper.TenorToFreq (_strTenor), strDayCount, false,
					strDayCount, false, _floaterIndex.currency(), false,
						_floaterIndex.accrualCompoundingRule());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.lang.String fullyQualifiedName()
	{
		return _floaterIndex.currency() + "-" + _floaterIndex.family() + "-" + _strTenor;
	}

	@Override public boolean match (
		final org.drip.state.identifier.LatentStateLabel lslOther)
	{
		return null == lslOther || !(lslOther instanceof org.drip.state.identifier.ForwardLabel) ? false :
			fullyQualifiedName().equalsIgnoreCase (lslOther.fullyQualifiedName());
	}
}

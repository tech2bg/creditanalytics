
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
 * CouponSetting contains the coupon type, schedule, and the coupon amount for the component. If available
 *  floor and/or ceiling may also be applied to the coupon, in a pre-determined order of precedence. It
 *  exports serialization into and de-serialization out of byte arrays.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CouponSetting implements org.drip.product.params.Validatable {

	/**
	 * Coupon schedule
	 */

	public FactorSchedule _fsCoupon = null;

	/**
	 * Coupon Type
	 */

	public java.lang.String _strCouponType = "";

	/**
	 * Coupon Amount
	 */

	public double _dblCoupon = java.lang.Double.NaN;

	/**
	 * Coupon Floor
	 */

	public double _dblCouponFloor = java.lang.Double.NaN;

	/**
	 * Coupon Ceiling
	 */

	public double _dblCouponCeiling = java.lang.Double.NaN;

	/**
	 * Construct the CouponSetting from the coupon schedule, coupon type, and the coupon amount
	 * 
	 * @param fsCoupon Coupon schedule
	 * @param strCouponType Coupon Type
	 * @param dblCoupon Coupon Amount
	 * @param dblCouponCeiling Coupon Ceiling Amount
	 * @param dblCouponFloor Coupon Floor Amount
	 */

	public CouponSetting (
		final FactorSchedule fsCoupon,
		final java.lang.String strCouponType,
		final double dblCoupon,
		final double dblCouponCeiling,
		final double dblCouponFloor)
	{
		_fsCoupon = fsCoupon;
		_dblCoupon = dblCoupon;
		_strCouponType = strCouponType;
		_dblCouponFloor = dblCouponFloor;
		_dblCouponCeiling = dblCouponCeiling;
	}

	/**
	 * Trim the component coupon if it falls outside the (optionally) specified coupon window. Note that
	 * 	trimming the coupon ceiling takes precedence over hiking the coupon floor.
	 * 
	 * @param dblCoupon Input Coupon
	 * @param dblDate Input Date representing the period that the coupon belongs to
	 * 
	 * @return The "trimmed" coupon
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public double processCouponWindow (
		final double dblCoupon,
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblCoupon) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate))
			throw new java.lang.Exception ("CouponSetting::processCouponWindow => Invalid Inputs");

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblCouponCeiling) &&
			!org.drip.quant.common.NumberUtil.IsValid (_dblCouponFloor))
			return dblCoupon;

		if (!!org.drip.quant.common.NumberUtil.IsValid (_dblCouponCeiling) && dblCoupon > _dblCouponCeiling)
			return _dblCouponCeiling;

		if (!!org.drip.quant.common.NumberUtil.IsValid (_dblCouponFloor) && dblCoupon < _dblCouponFloor)
			return _dblCouponFloor;

		return dblCoupon;
	}

	@Override public boolean validate()
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblCoupon)) return false;

		if (null == _fsCoupon) _fsCoupon = FactorSchedule.CreateBulletSchedule();

		if (org.drip.quant.common.NumberUtil.IsValid (_dblCouponCeiling) &&
			org.drip.quant.common.NumberUtil.IsValid (_dblCouponFloor) && _dblCouponCeiling < _dblCouponFloor)
			return false;

		return true;
	}
}

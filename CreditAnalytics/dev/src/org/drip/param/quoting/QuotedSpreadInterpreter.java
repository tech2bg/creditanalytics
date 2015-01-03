
package org.drip.param.quoting;

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
 * QuotedSpreadInterpreter holds the fields needed to interpret a Quoted Spread Quote. It contains the
 * 	contract type and the coupon.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class QuotedSpreadInterpreter extends org.drip.param.quoting.MeasureInterpreter {

	/**
	 * SNAC CDS Contract
	 */

	public static final java.lang.String SNAC_CDS = "SNAC";

	/**
	 * Conventional CDS Contract
	 */

	public static final java.lang.String CONV_CDS = "CONV";

	/**
	 * STEM CDS Contract
	 */

	public static final java.lang.String STEM_CDS = "CONV";

	private java.lang.String _strCDSContractType = "";
	private double _dblCouponStrike = java.lang.Double.NaN;

	/**
	 * QuotedSpreadInterpreter constructor
	 * 
	 * @param strCDSContractType The CDS Contract Type
	 * @param dblCouponStrike The Coupon Strike
	 * 
	 * @throws java.lang.Exception
	 */

	public QuotedSpreadInterpreter (
		final java.lang.String strCDSContractType,
		final double dblCouponStrike)
		throws java.lang.Exception
	{
		if (null == (_strCDSContractType = strCDSContractType) || (!CONV_CDS.equalsIgnoreCase
			(_strCDSContractType) && !SNAC_CDS.equalsIgnoreCase (_strCDSContractType) &&
				!STEM_CDS.equalsIgnoreCase (_strCDSContractType)))
			throw new java.lang.Exception ("QuotedSpreadInterpreter ctr: Invalid Inputs");

		_dblCouponStrike = dblCouponStrike;
	}

	/**
	 * Retrieve the CDS Contract Type
	 * 
	 * @return The CDS Contract Type
	 */

	public java.lang.String cdsContractType()
	{
		return _strCDSContractType;
	}

	/**
	 * Retrieve the Coupon Strike
	 * 
	 * @return The Coupon Strike
	 */

	public double couponStrike()
	{
		return _dblCouponStrike;
	}
}

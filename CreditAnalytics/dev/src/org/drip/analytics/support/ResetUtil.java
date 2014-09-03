
package org.drip.analytics.support;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * ResetUtil contains the Reset Period Manipulation Functionality
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ResetUtil {

	/**
	 * Accrual Compounding Rule - Arithmetic
	 */

	public static final int ACCRUAL_COMPOUNDING_RULE_ARITHMETIC = 1;

	/**
	 * Accrual Compounding Rule - Geometric
	 */

	public static final int ACCRUAL_COMPOUNDING_RULE_GEOMETRIC = 2;

	/**
	 * Verify if the Specified Accrual Compounding Rule is a Valid One
	 * 
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * 
	 * @return TRUE => The Accrual Compounding Rule is valid
	 */

	public static final boolean ValidateCompoundingRule (
		final int iAccrualCompoundingRule)
	{
		return ACCRUAL_COMPOUNDING_RULE_ARITHMETIC == iAccrualCompoundingRule ||
			ACCRUAL_COMPOUNDING_RULE_GEOMETRIC == iAccrualCompoundingRule;
	}
}

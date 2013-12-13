
package org.drip.analytics.rates;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * ForwardRateEstimator is the interface that exposes the calculation of the Forward Rate for a specific
 *  Index.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface ForwardRateEstimator {

	/**
	 * Retrieve the Forward Rate Index
	 * 
	 * @return The Forward Rate Index
	 */

	public abstract org.drip.product.params.FloatingRateIndex index();

	/**
	 * Retrieve the Forward Rate Tenor
	 * 
	 * @return The Forward Rate Tenor
	 */

	public abstract java.lang.String tenor();

	/**
	 * Calculate the Forward Rate to the given Date
	 * 
	 * @param dblDate Date
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate cannot be calculated
	 */

	public abstract double forward (
		final double dblDate)
		throws java.lang.Exception;

	/**
	 * Calculate the Forward Rate to the given date
	 * 
	 * @param dt Date
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate cannot be calculated
	 */

	public abstract double forward (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception;

	/**
	 * Calculate the Forward Rate to the tenor implied by the given date
	 * 
	 * @param strTenor The Tenor
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate cannot be calculated
	 */

	public abstract double forward (
		final java.lang.String strTenor)
		throws java.lang.Exception;
}

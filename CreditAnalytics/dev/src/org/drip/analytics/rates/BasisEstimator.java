
package org.drip.analytics.rates;

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
 * BasisEstimator is the interface that exposes the calculation of the Basis between any two latent states.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface BasisEstimator {

	/**
	 * Retrieve the Reference Index
	 * 
	 * @return The Reference Index
	 */

	public abstract org.drip.product.params.FloatingRateIndex referenceIndex();

	/**
	 * Retrieve the Derived Index
	 * 
	 * @return The Derived Index
	 */

	public abstract org.drip.product.params.FloatingRateIndex derivedIndex();

	/**
	 * Calculate the Basis to the given Date
	 * 
	 * @param dblDate Date
	 * 
	 * @return The Basis
	 * 
	 * @throws java.lang.Exception Thrown if the Basis cannot be calculated
	 */

	public abstract double basis (
		final double dblDate)
		throws java.lang.Exception;

	/**
	 * Calculate the Basis to the given date
	 * 
	 * @param dt Date
	 * 
	 * @return The Basis
	 * 
	 * @throws java.lang.Exception Thrown if the Basis cannot be calculated
	 */

	public abstract double basis (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception;

	/**
	 * Calculate the Basis to the given date
	 * 
	 * @param strTenor The Tenor
	 * 
	 * @return The Basis
	 * 
	 * @throws java.lang.Exception Thrown if the Basis cannot be calculated
	 */

	public abstract double basis (
		final java.lang.String strTenor)
		throws java.lang.Exception;
}

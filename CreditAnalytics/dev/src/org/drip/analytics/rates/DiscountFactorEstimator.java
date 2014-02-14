
package org.drip.analytics.rates;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * DiscountFactorEstimator is the interface that exposes the calculation of the Discount Factor for a
 *  specific Sovereign/Jurisdiction Span. It exposes the following functionality:
 *  
 *  - Curve Epoch Date
 *  - Discount Factor Target/Effective Variants - to Specified Julian Dates and/or Tenors
 *  - Forward Rate Target/Effective Variants - to Specified Julian Dates and/or Tenors
 *  - Zero Rate Target/Effective Variants - to Specified Julian Dates and/or Tenors
 *  - LIBOR Rate and LIBOR01 Target/Effective Variants - to Specified Julian Dates and/or Tenors
 *  - Curve Implied Arbitrary Measure Estimates
 *
 * @author Lakshmi Krishnamurthy
 */

public interface DiscountFactorEstimator {

	/**
	 * Retrieve the Starting (Epoch) Date
	 * 
	 * @return The Starting Date
	 */

	public abstract org.drip.analytics.date.JulianDate epoch();

	/**
	 * Calculate the Discount Factor to the given Date
	 * 
	 * @param dblDate Date
	 * 
	 * @return Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the Discount Factor cannot be calculated
	 */

	public abstract double df (
		final double dblDate)
		throws java.lang.Exception;

	/**
	 * Calculate the discount factor to the given date
	 * 
	 * @param dt Date
	 * 
	 * @return Discount factor
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public abstract double df (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception;

	/**
	 * Calculate the Discount Factor to the given Tenor
	 * 
	 * @param strTenor Tenor
	 * 
	 * @return Discount factor
	 * 
	 * @throws java.lang.Exception Thrown if the Discount Factor cannot be calculated
	 */

	public abstract double df (
		final java.lang.String strTenor)
		throws java.lang.Exception;

	/**
	 * Compute the time-weighted discount factor between 2 dates
	 * 
	 * @param dblDate1 First Date
	 * @param dblDate2 Second Date
	 * 
	 * @return Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public abstract double effectiveDF (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception;

	/**
	 * Compute the time-weighted discount factor between 2 dates
	 * 
	 * @param dt1 First Date
	 * @param dt2 Second Date
	 * 
	 * @return Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public abstract double effectiveDF (
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2)
		throws java.lang.Exception;

	/**
	 * Compute the time-weighted discount factor between 2 tenors
	 * 
	 * @param strTenor1 First Date
	 * @param strTenor2 Second Date
	 * 
	 * @return Discount Factor
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public abstract double effectiveDF (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception;

	/**
	 * Compute the Forward Rate between two Dates
	 * 
	 * @param dblDate1 First Date
	 * @param dblDate2 Second Date
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate cannot be calculated
	 */

	public abstract double forward (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception;

	/**
	 * Compute the Forward Rate between two Tenors
	 * 
	 * @param strTenor1 Tenor Start
	 * @param strTenor2 Tenor End
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate cannot be calculated
	 */

	public abstract double forward (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception;

	/**
	 * Calculate the implied rate to the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return Implied rate
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public abstract double zero (
		final double dblDate)
		throws java.lang.Exception;

	/**
	 * Calculate the implied rate to the given tenor
	 * 
	 * @param strTenor Tenor
	 * 
	 * @return Implied rate
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public abstract double zero (
		final java.lang.String strTenor)
		throws java.lang.Exception;

	/**
	 * Compute the LIBOR between 2 dates
	 * 
	 * @param dblDt1 First Date
	 * @param dblDt2 Second Date
	 * 
	 * @return LIBOR
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public abstract double libor (
		final double dblDt1,
		final double dblDt2)
		throws java.lang.Exception;

	/**
	 * Compute the LIBOR between 2 dates given the Day Count
	 * 
	 * @param dblDt1 First Date
	 * @param dblDt2 Second Date
	 * @param dblDCF Day Count Fraction
	 * 
	 * @return LIBOR
	 * 
	 * @throws java.lang.Exception Thrown if the discount factor cannot be calculated
	 */

	public abstract double libor (
		final double dblDt1,
		final double dblDt2,
		final double dblDCF)
		throws java.lang.Exception;

	/**
	 * Calculate the LIBOR to the given tenor at the specified date
	 * 
	 * @param dblDate Date
	 * @param strTenor Tenor
	 * 
	 * @return LIBOR
	 * 
	 * @throws java.lang.Exception Thrown if LIBOR cannot be calculated
	 */

	public abstract double libor (
		final double dblDate,
		final java.lang.String strTenor)
		throws java.lang.Exception;

	/**
	 * Calculate the LIBOR to the given tenor at the specified Julian Date
	 * 
	 * @param dt Julian Date
	 * @param strTenor Tenor
	 * 
	 * @return LIBOR
	 * 
	 * @throws java.lang.Exception Thrown if LIBOR cannot be calculated
	 */

	public abstract double libor (
		final org.drip.analytics.date.JulianDate dt,
		final java.lang.String strTenor)
		throws java.lang.Exception;

	/**
	 * Calculate the LIBOR DV01 to the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return LIBOR DV01
	 * 
	 * @throws java.lang.Exception Thrown if LIBOR DV01 cannot be calculated
	 */

	public abstract double liborDV01 (
		final double dblDate)
		throws java.lang.Exception;

	/**
	 * Estimates the estimated calibrated measure value for the given date
	 * 
	 * @param dblDate Date
	 * 
	 * @return The estimated calibrated measure value
	 * 
	 * @throws java.lang.Exception Thrown if the estimated calibrated measure value cannot be computed
	 */

	public abstract double estimateMeasure (
		final double dblDate)
		throws java.lang.Exception;
}

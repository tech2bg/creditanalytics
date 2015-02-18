
package org.drip.quant.randomsequence;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * SequenceGenerator implements the Random Sequence Generator Functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class SequenceGenerator {

	/**
	 * Generate a Random Number according to the specified rule
	 * 
	 * @return The Random Number
	 */

	public abstract double random();

	/**
	 * Generate a Random Sequence along with its Metrics
	 * 
	 * @param iNumEntry Number of Entries in the Sequence
	 * @param distPopulation The True Underlying Generator Distribution of the Population
	 * 
	 * @return The Random Sequence (along with its Metrics)
	 */

	public org.drip.quant.randomsequence.SingleSequenceAgnosticMetrics sequence (
		final int iNumEntry,
		final org.drip.quant.distribution.Univariate distPopulation)
	{
		double[] adblSequence = new double[iNumEntry];

		for (int i = 0; i < iNumEntry; ++i)
			adblSequence[i] = random();

		try {
			return new org.drip.quant.randomsequence.SingleSequenceAgnosticMetrics (adblSequence,
				distPopulation);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a Bounded Random Sequence along with its Metrics
	 * 
	 * @param iNumEntry Number of Entries in the Sequence
	 * @param distPopulation The True Underlying Generator Distribution of the Population
	 * @param dblSupport Support of the Bounded Random Sequence
	 * 
	 * @return The Random Sequence (along with its Metrics)
	 */

	public org.drip.quant.randomsequence.BoundedSequenceAgnosticMetrics sequence (
		final int iNumEntry,
		final org.drip.quant.distribution.Univariate distPopulation,
		final double dblSupport)
	{
		double[] adblSequence = new double[iNumEntry];

		for (int i = 0; i < iNumEntry; ++i)
			adblSequence[i] = random();

		try {
			return new org.drip.quant.randomsequence.BoundedSequenceAgnosticMetrics (adblSequence,
				distPopulation, dblSupport);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate a Bounded Unit Random Sequence along with its Metrics
	 * 
	 * @param iNumEntry Number of Entries in the Sequence
	 * @param dblPopulationMean The Population Mean
	 * 
	 * @return The Bounded Unit Random Sequence (along with its Metrics)
	 */

	public org.drip.quant.randomsequence.UnitSequenceAgnosticMetrics unitSequence (
		final int iNumEntry,
		final double dblPopulationMean)
	{
		double[] adblSequence = new double[iNumEntry];

		for (int i = 0; i < iNumEntry; ++i)
			adblSequence[i] = random();

		try {
			return new org.drip.quant.randomsequence.UnitSequenceAgnosticMetrics (adblSequence,
				dblPopulationMean);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

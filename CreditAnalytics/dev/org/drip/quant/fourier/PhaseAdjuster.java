
package org.drip.quant.fourier;

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
 * PhaseAdjuster implements the functionality specifically meant for enhancing stability of the Fourier
 * 	numerical Routines.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class PhaseAdjuster {

	/**
	 * No Multi-Valued Principal Branch Tracking
	 */

	public static final int MULTI_VALUE_BRANCH_PHASE_TRACKER_NONE = 0;

	/**
	 * Multi-Valued Logarithm Principal Branch Tracking Using Rotating Counting
	 */

	public static final int MULTI_VALUE_BRANCH_PHASE_TRACKER_ROTATION_COUNT = 1;

	/**
	 * Multi-Valued Logarithm PLUS Power Principal Branch Tracking Using the Kahl-Jackel Algorithm
	 */

	public static final int MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL = 2;

	/**
	 * Handling the Branch Switching of the Complex Power Function according Kahl-Jackel algorithm:
	 * 	- http://www.pjaeckel.webspace.virginmedia.com/NotSoComplexLogarithmsInTheHestonModel.pdf
	 * 
	 * @param cnGNumerator The Log G Numerator
	 * @param cnGDenominator The Log G Denominator
	 * @param iN Number of Numerator Counted rotations
	 * @param iM Number of Numerator Counted rotations
	 * 
	 * @return The Branch Switching Log Adjustment
	 */

	public static final org.drip.quant.fourier.ComplexNumber PowerLogPhaseTracker (
		final org.drip.quant.fourier.ComplexNumber cnGNumerator,
		final org.drip.quant.fourier.ComplexNumber cnGDenominator,
		final int iN,
		final int iM)
	{
		if (null == cnGNumerator || null == cnGNumerator || iN < 0 || iM < 0) return null;

		double dblAbsDenominator = cnGDenominator.abs();

		if (0. == dblAbsDenominator) return null;

		try {
			return new org.drip.quant.fourier.ComplexNumber (java.lang.Math.log (cnGNumerator.abs() /
				dblAbsDenominator), cnGNumerator.argument() - cnGDenominator.argument() + 2. *
					java.lang.Math.PI * (iN - iM));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}


package org.drip.math.spline;

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
 * SegmentNodeWeightConstraint holds the following set of fields that characterize a single global linear
 * 	constraint, expressed linearly across the constituent nodes. Constraints are expressed as
 * 
 * 			C = Sigma_j [W_j * f (x_j)] = V where
 * 
 * 	x_j => Ordinate j
 * 	W_j => Weight at ordinate j
 * 	V => Value of the Constraint
 * 
 * SegmentNodeWeightConstraint can be viewed as the global response point value transpose of
 *  SegmentBasisFunctionConstraint.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SegmentNodeWeightConstraint {

	/**
	 * Indicator specifying that the knot is to the left of the constraint ordinates
	 */

	public static final int KNOT_LEFT_OF_CONSTRAINT = 1;

	/**
	 * Indicator specifying that the knot is to the right of the constraint ordinates
	 */

	public static final int KNOT_RIGHT_OF_CONSTRAINT = 2;

	/**
	 * Indicator specifying that the knot beaks the constraint ordinates
	 */

	public static final int KNOT_BREAKS_CONSTRAINT = 4;

	private double[] _adblOrdinate = null;
	private double[] _adblOrdinateWeight = null;
	private double _dblValue = java.lang.Double.NaN;

	/**
	 * SegmentNodeWeightConstraint constructor
	 * 
	 * @param adblOrdinate The Array of Global Constraint Ordinates
	 * @param adblOrdinateWeight The Array of the Weights to be applied to the value at each constraint
	 *  ordinate
	 * @param dblValue The Constraint Value
	 * 
	 * @throws java.lang.Exception
	 */

	public SegmentNodeWeightConstraint (
		final double[] adblOrdinate,
		final double[] adblOrdinateWeight,
		final double dblValue)
		throws java.lang.Exception
	{
		if (null == (_adblOrdinate = adblOrdinate) || null == (_adblOrdinateWeight = adblOrdinateWeight) ||
			!org.drip.math.common.NumberUtil.IsValid (_dblValue = dblValue))
			throw new java.lang.Exception ("SegmentNodeWeightConstraint ctr: Invalid Inputs");

		int iNumOrdinate = _adblOrdinate.length;

		if (0 == iNumOrdinate || _adblOrdinateWeight.length != iNumOrdinate)
			throw new java.lang.Exception ("SegmentNodeWeightConstraint ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Array of Constraint Ordinates
	 * 
	 * @return The Array of Constraint Ordinates
	 */

	public double[] ordinates()
	{
		return _adblOrdinate;
	}

	/**
	 * Retrieve the Array of Weights at each Ordinate
	 * 
	 * @return The Array of Weights at each Ordinate
	 */

	public double[] ordinateWeights()
	{
		return _adblOrdinateWeight;
	}

	/**
	 * Retrieve the Constraint Value
	 * 
	 * @return The Constraint Value
	 */

	public double getValue()
	{
		return _dblValue;
	}

	/**
	 * Generate the Segment Constraint in terms of the local Basis Function realizations
	 * 
	 * @param aAUBasis Array of the Basis Functions
	 * @param inel Inelastics transformer to convert coordinate space to Local from Global
	 * 
	 * @return The Segment Basis Function Constraint
	 */

	public org.drip.math.spline.SegmentBasisFunctionConstraint getSegmentBasisFunctionConstraint (
		final org.drip.math.function.AbstractUnivariate[] aAUBasis,
		final org.drip.math.grid.Inelastics inel)
	{
		if (null == aAUBasis || null == inel) return null;

		int iNumCoeff = aAUBasis.length;
		int iNumOrdinate = _adblOrdinate.length;
		double[] adblLocalOrdinate = new double[iNumOrdinate];
		double[] adblBasisCoeffWeight = new double[iNumCoeff];

		if (0 == iNumCoeff) return null;

		for (int i = 0; i < iNumOrdinate; ++i) {
			try {
				adblLocalOrdinate[i] = inel.calcNormalizedOrdinate (_adblOrdinate[i]);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			for (int i = 0; i < iNumCoeff; ++i) {
				adblBasisCoeffWeight[i] = 0.;

				for (int j = 0; j < iNumOrdinate; ++j) {
					adblBasisCoeffWeight[i] += _adblOrdinateWeight[j] * aAUBasis[i].evaluate
						(adblLocalOrdinate[j]);
				}
			}

			return new org.drip.math.spline.SegmentBasisFunctionConstraint (_dblValue, adblBasisCoeffWeight);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get the Position of the Knot relative to the Constraints
	 * 
	 * @param dblX The Knot Ordinate
	 * 
	 * @return One of KNOT_LEFT_OF_CONSTRAINT | KNOT_RIGHT_OF_CONSTRAINT | KNOT_BREAKS_CONSTRAINT
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public int knotPosition (
		final double dblX)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX))
			throw new java.lang.Exception ("SegmentNodeWeightConstraint::knotPosition => Invalid Inputs");

		if (dblX < _adblOrdinate[0]) return KNOT_LEFT_OF_CONSTRAINT;

		if (dblX > _adblOrdinate[_adblOrdinate.length - 1]) return KNOT_RIGHT_OF_CONSTRAINT;

		return KNOT_BREAKS_CONSTRAINT;
	}
}

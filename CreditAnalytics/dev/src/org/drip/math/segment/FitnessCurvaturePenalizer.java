
package org.drip.math.segment;

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
 * This Class implements the Segment's Fitness and Curvature Penalizers.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FitnessCurvaturePenalizer {
	private org.drip.math.segment.LocalBasisEvaluator _lbe = null;
	private org.drip.math.segment.FitnessPenaltyParams _fpp = null;
	private org.drip.math.segment.CurvaturePenaltyParams _cpp = null;

	private double basisPairCurvaturePenalty (
		final int iBasisIndexI,
		final int iBasisIndexR)
		throws java.lang.Exception
	{
		org.drip.math.function.AbstractUnivariate of = new org.drip.math.function.AbstractUnivariate (null) {
			@Override public double evaluate (
				final double dblVariate)
				throws Exception
			{
				int iOrder = _cpp.derivativeOrder();

				return _lbe.localSpecificBasisDerivative (dblVariate, iOrder, iBasisIndexI) *
					_lbe.localSpecificBasisDerivative (dblVariate, iOrder, iBasisIndexR);
			}
		};

		return _cpp.coefficient() * org.drip.math.calculus.Integrator.Boole (of, 0., 1.);
	}

	private double basisPairFitnessPenalty (
		final int iBasisIndexI,
		final int iBasisIndexR)
		throws java.lang.Exception
	{
		if (null == _fpp) return 0.;

		int iNumPoint = _fpp.numPoint();

		if (0 == iNumPoint) return 0.;

		double dblBasisPairFitnessPenalty = 0.;

		for (int i = 0; i < iNumPoint; ++i) {
			double dblPredictorOrdinate = _fpp.predictorOrdinate (i);

			dblBasisPairFitnessPenalty += _fpp.weight (i) * _lbe.localSpecificBasisResponse
				(dblPredictorOrdinate, iBasisIndexI) * _lbe.localSpecificBasisResponse (dblPredictorOrdinate,
					iBasisIndexR);
		}

		return dblBasisPairFitnessPenalty / iNumPoint;
	}

	/**
	 * FitnessCurvaturePenalizer constructor
	 * 
	 * @param cpp Curvature Penalty Parameters
	 * @param lbe The Local Basis Evaluator
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public FitnessCurvaturePenalizer (
		final org.drip.math.segment.CurvaturePenaltyParams cpp,
		final org.drip.math.segment.LocalBasisEvaluator lbe)
		throws java.lang.Exception
	{
		if (null == (_cpp = cpp) || null == (_lbe = lbe))
			throw new java.lang.Exception ("FitnessCurvaturePenalizer ctr: Invalid Inputs");
	}

	/**
	 * Compute the Basis Pair Penalty Coefficient for the Fitness and the Curvature Penalties
	 * 
	 * @param iBasisIndexI I Basis Index (I is the Summation Index)
	 * @param iBasisIndexR R Basis Index (R is the Separator Index)
	 * 
	 * @return The Basis Pair Penalty Coefficient for the Fitness and the Curvature Penalties
	 */

	public double basisPairConstraintCoefficient (
		final int iBasisIndexI,
		final int iBasisIndexR)
		throws java.lang.Exception
	{
		return basisPairCurvaturePenalty (iBasisIndexI, iBasisIndexR) + basisPairFitnessPenalty
			(iBasisIndexI, iBasisIndexR);
	}

	/**
	 * Compute the Constraint Value for the Fitness Penalty
	 * 
	 * @param iBasisIndexR R Basis Index (R is the Separator Index)
	 * 
	 * @return The Constraint Value for the Fitness Penalty
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double basisPairFitnessConstraint (
		final int iBasisIndexR)
		throws java.lang.Exception
	{
		if (null == _fpp) return 0.;

		int iNumPoint = _fpp.numPoint();

		if (0 == iNumPoint) return 0.;

		double dblBasisPairFitnessPenalty = 0.;

		for (int i = 0; i < iNumPoint; ++i) {
			double dblPredictorOrdinate = _fpp.predictorOrdinate (i);

			dblBasisPairFitnessPenalty += _fpp.weight (i) * _lbe.localSpecificBasisResponse
				(dblPredictorOrdinate, iBasisIndexR) * _fpp.response (i);
		}

		return dblBasisPairFitnessPenalty / iNumPoint;
	}
}

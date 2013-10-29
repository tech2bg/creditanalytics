
package org.drip.spline.segment;

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
 * This abstract class contains the basis spline segment in-elastic ordinates. Estimate segment spline
 *  functions and their coefficients are implemented/calibrated in the overriding spline classes. It provides
 *  functionality for assessing the various segment attributes:
 *  - Segment Monotonicity
 *  - Estimate Function Value, the ordered derivative, and the corresponding Jacobian
 *  - Segment Local/Global Derivative
 *  - Evaluation of the Segment Micro-Jack
 *  - Head / Regular Segment calibration - both of the basis function coefficients and the Jacobian
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ElasticConstitutiveState extends org.drip.spline.segment.InelasticConstitutiveState {

	/**
	 * LEFT NODE VALUE PARAMETER INDEX
	 */

	public static final int LEFT_NODE_VALUE_PARAMETER_INDEX = 0;

	/**
	 * RIGHT NODE VALUE PARAMETER INDEX
	 */

	public static final int RIGHT_NODE_VALUE_PARAMETER_INDEX = 1;

	protected ElasticConstitutiveState (
		final double dblPredictorOrdinateLeft,
		final double dblPredictorOrdinateRight)
		throws java.lang.Exception
	{
		super (dblPredictorOrdinateLeft, dblPredictorOrdinateRight);
	}

	protected abstract boolean isMonotone();

	protected double[] globalCk (
		final double dblPredictorOrdinate,
		final org.drip.spline.segment.ElasticConstitutiveState ecsPrev,
		final int iCk)
	{
		double dblLocalOrderedDerivScale = 1.;
		double[] adblLeftOrdinateLocalDeriv = new double[iCk];

		double dblSegmentPredictorGlobalWidth = width();

		for (int i = 0; i < iCk; ++i) {
			dblLocalOrderedDerivScale *= dblSegmentPredictorGlobalWidth;

			try {
				adblLeftOrdinateLocalDeriv[i] = ecsPrev.calcResponseValueDerivative (dblPredictorOrdinate, i
					+ 1, false) * dblLocalOrderedDerivScale;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return adblLeftOrdinateLocalDeriv;
	}

	/**
	 * Response Value given the Local Predictor Ordinate
	 * 
	 * @param dblLocalPredictorOrdinate Predictor Ordinate
	 * 
	 * @return Response Value
	 * 
	 * @throws java.lang.Exception Thrown if Response Cannot be computed.
	 */

	protected abstract double localResponseValue (
		final double dblLocalPredictorOrdinate)
		throws java.lang.Exception;

	/**
	 * nth order Response Value Derivative at the Local Predictor Ordinate
	 * 
	 * @param dblLocalPredictorOrdinate Local Predictor Ordinate
	 * @param iOrder Order of the Derivative
	 * 
	 * @return nth order Response Value Derivative at the Local Predictor Ordinate
	 * 
	 * @throws java.lang.Exception Thrown if the nth order Response Value Derivative at the Local Predictor
	 *  Ordinate cannot be computed.
	 */

	protected abstract double localResponseValueDerivative (
		final double dblLocalPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception;

	/**
	 * Calculate the Jacobian of the Response to the Edge Parameters at the given Local Predictor Ordinate
	 * 
	 * @param dblLocalPredictorOrdinate The Local Predictor Ordinate
	 * 
	 * @return The Jacobian of the Response to the Edge Parameters at the given Local Predictor Ordinate
	 */

	protected abstract org.drip.quant.calculus.WengertJacobian localDResponseDEdgeParams (
		final double dblLocalPredictorOrdinate);

	/**
	 * Calculate the Jacobian of the Response to the Basis Coefficients at the given Local Predictor Ordinate
	 * 
	 * @param dblLocalPredictorOrdinate The Local Predictor Ordinate
	 * 
	 * @return The Jacobian of the Response to the Basis Coefficients at the given Local Predictor Ordinate
	 */

	protected abstract org.drip.quant.calculus.WengertJacobian localDResponseDBasisCoeff (
		final double dblLocalPredictorOrdinate);

	protected abstract ElasticConstitutiveState snipLeftOfLocalPredictorOrdinate (
		final double dblLocalPredictorOrdinate);

	protected abstract ElasticConstitutiveState snipRightOfLocalPredictorOrdinate (
		final double dblLocalPredictorOrdinate);

	/**
	 * Retrieve the number of Segment's Basis Functions
	 * 
	 * @return The Number of Segment's Basis Functions
	 */

	public abstract int numBasis();

	/**
	 * Retrieve the Number of Segment's Parameters
	 * 
	 * @return The Number of Segment's Parameters
	 */

	public abstract int numParameters();

	/**
	 * Calibrate the Segment from the Calibration Parameters
	 * 
	 * @param scis Segment Calibration Parameters
	 * 
	 * @return TRUE => Segment Successfully Calibrated
	 */

	public abstract boolean calibrate (
		final org.drip.spline.params.SegmentCalibrationInputSet scis);

	/**
	 * Calibrate the coefficients from the prior Segment and the Response Value at the Right Predictor
	 *  Ordinate
	 * 
	 * @param ecsPrev Prior Predictor/Response Segment
	 * @param dblRightOrdinateResponseValue Response Value at the Right Predictor Ordinate
	 * @param sbfr Segment's Best Fit Weighted Response Values
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final ElasticConstitutiveState ecsPrev,
		final double dblRightOrdinateResponseValue,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		try {
			return calibrate (ecsPrev,
				org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair (delocalize
					(1.), dblRightOrdinateResponseValue), sbfr);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the prior Predictor/Response Segment, the Constraint, and fitness
	 * 	Weights
	 * 
	 * @param ecsPrev Prior Predictor/Response Segment
	 * @param srvc The Segment Response Value Constraint
	 * @param sbfr Segment's Best Fit Weighted Response Values
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public abstract boolean calibrate (
		final ElasticConstitutiveState ecsPrev,
		final org.drip.spline.params.SegmentResponseValueConstraint srvc,
		final org.drip.spline.params.SegmentBestFitResponse sbfr);

	/**
	 * Retrieve the Segment's DCPE
	 * 
	 * @return The Segment's DCPE
	 * 
	 * @throws java.lang.Exception Thrown if the Segment's DCPE cannot be computed
	 */

	public abstract double dcpe()
		throws java.lang.Exception;

	/**
	 * Calculate the Response Value at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * 
	 * @return The Response Value
	 * 
	 * @throws java.lang.Exception Thrown if the calculation did not succeed
	 */

	public double responseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		return localResponseValue (localize (dblPredictorOrdinate));
	}

	/**
	 * Calculate the Ordered Response Value Derivative at the Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate at which the ordered Response Derivative is to be
	 * 	calculated
	 * @param iOrder Derivative Order
	 * @param bLocal TRUE => Get the localized transform of the Derivative; FALSE => Get the untransformed
	 * 
	 * @throws Thrown if the Ordered Response Value Derivative cannot be calculated
	 * 
	 * @return Retrieve the Ordered Response Value Derivative
	 */

	public double calcResponseValueDerivative (
		final double dblPredictorOrdinate,
		final int iOrder,
		final boolean bLocal)
		throws java.lang.Exception
	{
		double dblLocalPredictorOrdinate = localize (dblPredictorOrdinate);

		if (0 == iOrder) return localResponseValue (dblLocalPredictorOrdinate);

		double dblOrderedResponseValueDerivative = localResponseValueDerivative (dblLocalPredictorOrdinate,
			iOrder);

		if (bLocal) return dblOrderedResponseValueDerivative;

		double dblSegmentWidth = width();

		for (int i = 0; i < iOrder; ++i)
			dblOrderedResponseValueDerivative /= dblSegmentWidth;

		return dblOrderedResponseValueDerivative;
	}

	/**
	 * Calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public abstract org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams();

	/**
	 * Calculate the Jacobian of the Response to the Edge Parameters at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Jacobian of the Response to the Edge Parameters at the given Predictor Ordinate
	 */

	public org.drip.quant.calculus.WengertJacobian jackDResponseDEdgeParams (
		final double dblPredictorOrdinate)
	{
		try {
			return localDResponseDEdgeParams (localize (dblPredictorOrdinate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calculate the Jacobian of the Response to the Basis Coefficients at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Jacobian of the Response to the Basis Coefficients at the given Predictor Ordinate
	 */

	public org.drip.quant.calculus.WengertJacobian jackDResponseDBasisCoeff (
		final double dblPredictorOrdinate)
	{
		try {
			return localDResponseDBasisCoeff (localize (dblPredictorOrdinate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Indicate whether the given segment is monotone. If monotone, may optionally indicate the nature of
	 * 	the extrema contained inside (maxima/minima/infection).
	 *  
	 * @return The monotone Type
	 */

	public org.drip.spline.segment.Monotonocity monotoneType()
	{
		if (isMonotone()) {
			try {
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MONOTONIC);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		org.drip.quant.function1D.AbstractUnivariate ofDeriv = new org.drip.quant.function1D.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				return localResponseValueDerivative (dblX, 1);
			}

			@Override public org.drip.quant.calculus.Differential calcDifferential (
				final double dblX,
				final double dblOFBase,
				final int iOrder)
			{
				try {
					double dblVariateInfinitesimal = _dc.getVariateInfinitesimal (dblX);

					return new org.drip.quant.calculus.Differential (dblVariateInfinitesimal,
						localResponseValueDerivative (dblX, iOrder) * dblVariateInfinitesimal);
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}

				return null;
			}
		};

		try {
			org.drip.quant.solver1D.FixedPointFinderOutput fpop = new
				org.drip.quant.solver1D.FixedPointFinderBrent (0., ofDeriv, false).findRoot
					(org.drip.quant.solver1D.InitializationHeuristics.FromHardSearchEdges (0., 1.));

			if (null == fpop || !fpop.containsRoot())
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MONOTONIC);

			double dblExtremum = fpop.getRoot();

			if (!org.drip.quant.common.NumberUtil.IsValid (dblExtremum) || dblExtremum <= 0. || dblExtremum >=
				1.)
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MONOTONIC);

			double dbl2ndDeriv = localResponseValueDerivative (dblExtremum, 2);

			if (0. > dbl2ndDeriv)
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MAXIMA);

			if (0. < dbl2ndDeriv)
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.MINIMA);

			if (0. == dbl2ndDeriv)
				return new org.drip.spline.segment.Monotonocity
					(org.drip.spline.segment.Monotonocity.INFLECTION);

			return new org.drip.spline.segment.Monotonocity
				(org.drip.spline.segment.Monotonocity.NON_MONOTONIC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			return new org.drip.spline.segment.Monotonocity (org.drip.spline.segment.Monotonocity.MONOTONIC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calibrate the segment and calculate the Jacobian of the Segment's Response Basis Function Coefficients
	 *  to the Edge Parameters
	 * 
	 * @param scis Segment Calibration Parameters
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final org.drip.spline.params.SegmentCalibrationInputSet scis)
	{
		return calibrate (scis) ? jackDCoeffDEdgeParams() : null;
	}

	/**
	 * Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Slope
	 * 
	 * @param dblLeftEdgeResponseValue Left Edge Response Value
	 * @param dblLeftEdgeResponseSlope Left Edge Response Slope
	 * @param dblRightEdgeResponseValue Right Edge Response Value
	 * @param sbfr Segment's Best Fit Weighted Response Values
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final double dblLeftEdgeResponseValue,
		final double dblLeftEdgeResponseSlope,
		final double dblRightEdgeResponseValue,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblLeftEdgeResponseValue) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblLeftEdgeResponseSlope) ||
				!org.drip.quant.common.NumberUtil.IsValid (dblRightEdgeResponseValue))
			return false;

		try {
			return calibrate (new org.drip.spline.params.SegmentCalibrationInputSet (new double[] {0., 1.},
				new double[] {dblLeftEdgeResponseValue, dblRightEdgeResponseValue},
					org.drip.quant.common.CollectionUtil.DerivArrayFromSlope (numParameters() - 2,
						dblLeftEdgeResponseSlope), null, null, sbfr));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the Left Edge Response Value, the Left Edge Response Slope, and the
	 * 	Right Edge Response Value Constraint
	 * 
	 * @param dblLeftEdgeResponseValue Left Edge Response Value
	 * @param dblLeftEdgeResponseSlope Left Edge Response Slope
	 * @param srvcRight Segment's Right Edge Response Value Constraint
	 * @param bfwr Best Fit Weighted Response
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final double dblLeftEdgeResponseValue,
		final double dblLeftEdgeResponseSlope,
		final org.drip.spline.params.SegmentResponseValueConstraint srvcRight,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		try {
			return calibrate (new org.drip.spline.params.SegmentCalibrationInputSet (null, null,
				org.drip.quant.common.CollectionUtil.DerivArrayFromSlope (numParameters() - 2,
					dblLeftEdgeResponseSlope), null, new
						org.drip.spline.params.SegmentResponseValueConstraint[]
							{org.drip.spline.params.SegmentResponseValueConstraint.FromPredictorResponsePair
				(delocalize (0.), dblLeftEdgeResponseValue), srvcRight}, sbfr));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the Left Edge Response Value Constraint, the Left Edge Response Value
	 *  Slope, and the Right Edge Response Value Constraint
	 * 
	 * @param wrvcLeft Left Edge Response Value Constraint
	 * @param dblLeftResponseValueSlope Left Edge Response ValueSlope
	 * @param wrvcRight Right Edge Response Value Constraint
	 * @param sbfr Segment's Best Fit Weighted Response
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final org.drip.spline.params.SegmentResponseValueConstraint wrvcLeft,
		final double dblLeftResponseValueSlope,
		final org.drip.spline.params.SegmentResponseValueConstraint wrvcRight,
		final org.drip.spline.params.SegmentBestFitResponse bfwr)
	{
		try {
			return calibrate (new org.drip.spline.params.SegmentCalibrationInputSet (null, null,
				org.drip.quant.common.CollectionUtil.DerivArrayFromSlope (numParameters() - 2,
					dblLeftResponseValueSlope), null, new
						org.drip.spline.params.SegmentResponseValueConstraint[] {wrvcLeft, wrvcRight},
							bfwr));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Clip the part of the Segment to the Right of the specified Predictor Ordinate. Retain all other
	 * 	constraints the same.
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Clipped Segment
	 */

	public ElasticConstitutiveState clipLeftOfPredictorOrdinate (
		final double dblPredictorOrdinate)
	{
		try {
			return snipLeftOfLocalPredictorOrdinate (localize (dblPredictorOrdinate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Clip the part of the Segment to the Left of the specified Predictor Ordinate. Retain all other
	 * 	constraints the same.
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Clipped Segment
	 */

	public ElasticConstitutiveState clipRightOfPredictorOrdinate (
		final double dblPredictorOrdinate)
	{
		try {
			return snipRightOfLocalPredictorOrdinate (localize (dblPredictorOrdinate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Value Slope and
	 *  calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 * 
	 * @param dblLeftEdgeResponseValue Left Edge Response Value
	 * @param dblLeftResponseValueSlope Left Edge Response Slope
	 * @param dblRightEdgeResponseValue Right Edge Response Value
	 * @param bfwr Fitness Weighted Response
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final double dblLeftEdgeResponseValue,
		final double dblLeftResponseValueSlope,
		final double dblRightEdgeResponseValue,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		if (!calibrate (dblLeftEdgeResponseValue, dblLeftResponseValueSlope, dblRightEdgeResponseValue,
			sbfr))
			return null;

		return jackDCoeffDEdgeParams();
	}

	/**
	 * Calibrate the coefficients from the prior Segment and the Response Value at the Right Predictor
	 *  Ordinate and calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge
	 *  Parameters
	 * 
	 * @param ecsPrev Previous Predictor/Response Segment
	 * @param dblRightEdgeResponseValue Right Edge Response Value
	 * @param sbfr Fitness Weighted Response
	 * 
	 * @return The Jacobian
	 */

	public org.drip.quant.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final ElasticConstitutiveState ecsPrev,
		final double dblRightEdgeResponseValue,
		final org.drip.spline.params.SegmentBestFitResponse sbfr)
	{
		if (!calibrate (ecsPrev, dblRightEdgeResponseValue, sbfr)) return null;

		return jackDCoeffDEdgeParams();
	}

	/**
	 * Display the string representation for diagnostic purposes
	 * 
	 * @return The string representation
	 */

	public abstract java.lang.String displayString();
}

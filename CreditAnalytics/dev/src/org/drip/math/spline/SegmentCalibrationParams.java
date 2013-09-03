
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
 * SegmentCalibrationParams holds the flexible set of fields needed for a segment calibration. Specifically,
 * 	it holds:
 * 	- The array of single valued nodes and their values.
 * 	- Left/Right node Ck derivatives for continuity transmission.
 * 	- Linear Combination of the Single Valued Nodes as Constraints.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SegmentCalibrationParams {
	private double[] _adblSoloNode = null;
	private double[] _adblLeftDeriv = null;
	private double[] _adblRightDeriv = null;
	private double[] _adblSoloNodeValue = null;
	private org.drip.math.spline.SegmentNodeWeightConstraint[] _aSNWC = null;

	/**
	 * SegmentCalibrationParams constructor
	 * 
	 * @param adblSoloNode Array of the Single Nodes
	 * @param adblSoloNodeValue Array of the Single Node Values
	 * @param adblLeftDeriv Array of the Left Derivative Values
	 * @param adblRightDeriv Array of the Right Derivative Values
	 * @param aSNWC Array of the Segment Constraints
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are not Valid
	 */

	public SegmentCalibrationParams (
		final double[] adblSoloNode,
		final double[] adblSoloNodeValue,
		final double[] adblLeftDeriv,
		final double[] adblRightDeriv,
		final org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWC)
		throws java.lang.Exception
	{
		if (null == (_adblSoloNode = adblSoloNode) || null == (_adblSoloNodeValue = adblSoloNodeValue))
			throw new java.lang.Exception ("SegmentCalibrationParams ctr: Invalid Inputs!");

		_aSNWC = aSNWC;
		_adblLeftDeriv = adblLeftDeriv;
		_adblRightDeriv = adblRightDeriv;
		int iNumNode = _adblSoloNode.length;

		if (0 == iNumNode || iNumNode != _adblSoloNodeValue.length)
			throw new java.lang.Exception ("SegmentCalibrationParams ctr: Invalid Inputs!");
	}

	/**
	 * Retrieve the Array of Solo Valued Nodes
	 * 
	 * @return The Array of Solo Valued Nodes
	 */

	public double[] soloNodes()
	{
		return _adblSoloNode;
	}

	/**
	 * Retrieve the Array of Solo Valued Node Values
	 * 
	 * @return The Array of Solo Valued Node Values
	 */

	public double[] soloNodeValues()
	{
		return _adblSoloNodeValue;
	}

	/**
	 * Retrieve the Array of the Segment Left Derivatives
	 * 
	 * @return The Array of the Segment Left Derivatives
	 */

	public double[] leftDeriv()
	{
		return _adblLeftDeriv;
	}

	/**
	 * Retrieve the Array of the Segment Right Derivatives
	 * 
	 * @return The Array of the Segment Right Derivatives
	 */

	public double[] rightDeriv()
	{
		return _adblRightDeriv;
	}

	/**
	 * Generate the Array of Segment Constraint in terms of the local Basis Function realizations
	 * 
	 * @param aAUBasis Array of the Basis Functions
	 * @param inel Inelastics transformer to convert coordinate space to Local from Global
	 * 
	 * @return Array of the Segment Basis Function Constraints
	 */

	public org.drip.math.spline.SegmentBasisFunctionConstraint[] getSegmentBasisFunctionConstraint (
		final org.drip.math.function.AbstractUnivariate[] aAUBasis,
		final org.drip.math.grid.Inelastics inel)
	{
		if (null == _aSNWC) return null;

		int iNumConstraint = _aSNWC.length;
		org.drip.math.spline.SegmentBasisFunctionConstraint[] aSBFC = new
			org.drip.math.spline.SegmentBasisFunctionConstraint[iNumConstraint];

		if (0 == iNumConstraint) return null;

		for (int i = 0; i < iNumConstraint; ++i) {
			if (null == _aSNWC[i] || null == (aSBFC[i] = _aSNWC[i].getSegmentBasisFunctionConstraint
				(aAUBasis, inel)))
				return null;
		}

		return aSBFC;
	}

	/**
	 * Split the Segment Calibration Parameters across the knots into the left and the right segment
	 * 	calibration parameter.
	 * 
	 * @param dblX Knot X
	 * @param dblY Knot Y
	 * @param adblDeriv Knot Derivatives array
	 * 
	 * @return Array containing the left and the right segment calibration parameters.
	 */

	public SegmentCalibrationParams[] split (
		final double dblX,
		final double dblY,
		final double[] adblDeriv)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblX) || !org.drip.math.common.NumberUtil.IsValid
			(dblY))
			return null;

		double[] adblLeftSoloNode = null;
		double[] adblRightSoloNode = null;
		double[] adblLeftSoloNodeValue = null;
		double[] adblRightSoloNodeValue = null;
		org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWCLeft = null;
		org.drip.math.spline.SegmentNodeWeightConstraint[] aSNWCRight = null;

		if (null == _adblSoloNode || 0 == _adblSoloNode.length) {
			adblLeftSoloNode = new double[] {dblX};
			adblRightSoloNode = new double[] {dblX};
			adblLeftSoloNodeValue = new double[] {dblY};
			adblRightSoloNodeValue = new double[] {dblY};
		} else {
			boolean bSplitNodeAdded = false;

			java.util.List<java.lang.Double> lsLeftNode = new java.util.ArrayList<java.lang.Double>();

			java.util.List<java.lang.Double> lsLeftNodeValue = new java.util.ArrayList<java.lang.Double>();

			java.util.List<java.lang.Double> lsRightNode = new java.util.ArrayList<java.lang.Double>();

			java.util.List<java.lang.Double> lsRightNodeValue = new java.util.ArrayList<java.lang.Double>();

			for (int i = 0; i < _adblSoloNode.length; ++i) {
				if (_adblSoloNode[i] < dblX) {
					lsLeftNode.add (_adblSoloNode[i]);

					lsLeftNodeValue.add (_adblSoloNodeValue[i]);
				} else {
					if (!bSplitNodeAdded) {
						lsLeftNode.add (dblX);

						lsLeftNodeValue.add (dblY);

						lsRightNode.add (dblX);

						lsRightNodeValue.add (dblY);

						bSplitNodeAdded = true;
					}

					lsRightNode.add (_adblSoloNode[i]);

					lsRightNodeValue.add (_adblSoloNodeValue[i]);
				}
			}

			int iNumLeftNode = lsLeftNode.size();

			if (0 != iNumLeftNode) {
				adblLeftSoloNode = new double[iNumLeftNode];
				adblLeftSoloNodeValue = new double[iNumLeftNode];

				for (int i = 0; i < iNumLeftNode; ++i) {
					adblLeftSoloNode[i] = lsLeftNode.get (i);

					adblLeftSoloNodeValue[i] = lsLeftNodeValue.get (i);
				}
			}

			int iNumRightNode = lsRightNode.size();

			if (0 != iNumRightNode) {
				adblRightSoloNode = new double[iNumRightNode];
				adblRightSoloNodeValue = new double[iNumRightNode];

				for (int i = 0; i < iNumRightNode; ++i) {
					adblRightSoloNode[i] = lsRightNode.get (i);

					adblRightSoloNodeValue[i] = lsRightNodeValue.get (i);
				}
			}
		}

		if (null != _aSNWC && 0 != _aSNWC.length) {
			java.util.List<org.drip.math.spline.SegmentNodeWeightConstraint> lsSNWCLeft = new
				java.util.ArrayList<org.drip.math.spline.SegmentNodeWeightConstraint>();

			java.util.List<org.drip.math.spline.SegmentNodeWeightConstraint> lsSNWCRight = new
				java.util.ArrayList<org.drip.math.spline.SegmentNodeWeightConstraint>();

			for (org.drip.math.spline.SegmentNodeWeightConstraint snwc : _aSNWC) {
				try {
					if (org.drip.math.spline.SegmentNodeWeightConstraint.KNOT_RIGHT_OF_CONSTRAINT ==
						snwc.knotPosition (dblX))
						lsSNWCLeft.add (snwc);
					else if (org.drip.math.spline.SegmentNodeWeightConstraint.KNOT_LEFT_OF_CONSTRAINT ==
						snwc.knotPosition (dblX))
						lsSNWCRight.add (snwc);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			int iNumLeftConstraint = lsSNWCLeft.size();

			if (0 != iNumLeftConstraint) {
				aSNWCLeft = new org.drip.math.spline.SegmentNodeWeightConstraint[iNumLeftConstraint];

				for (int i = 0; i < iNumLeftConstraint; ++i)
					aSNWCLeft[i] = lsSNWCLeft.get (i);
			}

			int iNumRightConstraint = lsSNWCRight.size();

			if (0 != iNumRightConstraint) {
				aSNWCRight = new org.drip.math.spline.SegmentNodeWeightConstraint[iNumRightConstraint];

				for (int i = 0; i < iNumRightConstraint; ++i)
					aSNWCRight[i] = lsSNWCRight.get (i);
			}
		}

		try {
			return new SegmentCalibrationParams[] {new SegmentCalibrationParams (adblLeftSoloNode,
				adblLeftSoloNodeValue, _adblLeftDeriv, adblDeriv, aSNWCLeft), new SegmentCalibrationParams
					(adblRightSoloNode, adblRightSoloNodeValue, adblDeriv, _adblRightDeriv, aSNWCRight)};
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

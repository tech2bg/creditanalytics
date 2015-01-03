
package org.drip.spline.basis;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * This class implements the parameter set for constructing the B Spline Sequence. It provides functionality
 * 	to:
 * 	- Retrieve the B Spline Order
 * 	- Retrieve the Number of Basis Functions
 * 	- Retrieve the Processed Basis Derivative Order
 * 	- Retrieve the Basis Hat Type
 * 	- Retrieve the Shape Control Type
 * 	- Retrieve the Tension
 * 	- Retrieve the Array of Predictor Ordinates
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BSplineSequenceParams {
	private int _iNumBasis = -1;
	private int _iBSplineOrder = -1;
	private int _iProcBasisDerivOrder = -1;
	private java.lang.String _strHatType = "";
	private double _dblTension = java.lang.Double.NaN;
	private java.lang.String _strShapeControlType = "";

	public BSplineSequenceParams (
		final java.lang.String strHatType,
		final java.lang.String strShapeControlType,
		final int iNumBasis,
		final int iBSplineOrder,
		final double dblTension,
		final int iProcBasisDerivOrder)
		throws java.lang.Exception
	{
		_iNumBasis = iNumBasis;
		_strHatType = strHatType;
		_dblTension = dblTension;
		_iBSplineOrder = iBSplineOrder;
		_strShapeControlType = strShapeControlType;
		_iProcBasisDerivOrder = iProcBasisDerivOrder;
	}

	/**
	 * Retrieve the B Spline Order
	 * 
	 * @return The B Spline Order
	 */

	public int bSplineOrder()
	{
		return _iBSplineOrder;
	}

	/**
	 * Retrieve the Number of Basis Functions
	 * 
	 * @return The Number of Basis Functions
	 */

	public int numBasis()
	{
		return _iNumBasis;
	}

	/**
	 * Retrieve the Processed Basis Derivative Order
	 * 
	 * @return The Processed Basis Derivative Order
	 */

	public int procBasisDerivOrder()
	{
		return _iProcBasisDerivOrder;
	}

	/**
	 * Retrieve the Basis Hat Type
	 * 
	 * @return The Basis Hat Type
	 */

	public java.lang.String hat()
	{
		return _strHatType;
	}

	/**
	 * Retrieve the Shape Control Type
	 * 
	 * @return The Shape Control Type
	 */

	public java.lang.String shapeControl()
	{
		return _strShapeControlType;
	}

	/**
	 * Retrieve the Tension
	 * 
	 * @return The Tension
	 */

	public double tension()
	{
		return _dblTension;
	}

	/**
	 * Retrieve the Array of Predictor Ordinates
	 * 
	 * @return The Array of Predictor Ordinates
	 */

	public double[] predictorOrdinates()
	{
		int iNumPredictorOrdinate = _iBSplineOrder + _iNumBasis;
		double[] adblPredictorOrdinate = new double[iNumPredictorOrdinate];
		double dblPredictorOrdinateIncrement = 1. / (_iBSplineOrder + _iNumBasis - 1);

		for (int i = 0; i < iNumPredictorOrdinate; ++i)
			adblPredictorOrdinate[i] = dblPredictorOrdinateIncrement * i;

		return adblPredictorOrdinate;
	}
}

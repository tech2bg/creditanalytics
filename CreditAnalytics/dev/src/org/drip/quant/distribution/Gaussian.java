
package org.drip.quant.distribution;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Robert Sedgewick
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
 * UnivariateGaussian implements the univariate normal distribution. It implements incremental, cumulative, and
 *  inverse cumulative distribution densities.
 *
 * @author Robert Sedgewick
 */

public class Gaussian {
    private static final double InverseCDF (
    	final double dblY,
    	final double dblTolerance,
    	final double dblLowCutoff,
    	final double dblHighCutoff)
    	throws java.lang.Exception
    {
        double dblMid = 0.5 * (dblHighCutoff + dblLowCutoff);

        if (dblHighCutoff - dblLowCutoff < dblTolerance) return dblMid;

        return CDF (dblMid) > dblY ? InverseCDF (dblY, dblTolerance, dblLowCutoff, dblMid) : InverseCDF
        	(dblY, dblTolerance, dblMid, dblHighCutoff);
    }

    /**
     * Retrieve the Density at the specified Point using Zero Mean and Unit Variance
     * 
     * @param dblX The Ordinate
     * 
     * @return The Density at the specified Point Zero Mean and Unit Variance
     * 
     * @throws java.lang.Exception Thrown if Inputs are Invalid
     */

    public static final double Density (
    	final double dblX)
    	throws java.lang.Exception
    {
    	if (!org.drip.quant.common.NumberUtil.IsValid (dblX))
    		throw new java.lang.Exception ("Gaussian::Density => Invalid Inputs");

    	return java.lang.Math.exp (-0.5 * dblX * dblX) / java.lang.Math.sqrt (2 * java.lang.Math.PI);
    }

    /**
     * Compute the Cumulative Distribution Function up to the specified variate
     * 
     * @param dblX The Variate
     * 
     * @return The Cumulative Distribution Function up to the specified variate
     * 
     * @throws java.lang.Exception thrown if the Inputs are Invalid
     */

    public static final double CDF (
    	final double dblX)
    	throws java.lang.Exception
    {
    	if (java.lang.Double.isNaN (dblX)) throw new java.lang.Exception ("Gaussian::CDF => Invalid Inputs");

        if (dblX < -8.) return 0.;

        if (dblX > 8.) return 1.;

        double dblSum = 0.;
        double dblTerm = dblX;

        for (int i = 3; dblSum + dblTerm != dblSum; i += 2) {
        	dblSum  = dblSum + dblTerm;
        	dblTerm = dblTerm * dblX * dblX / i;
        }

        return 0.5 + dblSum * Density (dblX);
    }

    /**
     * Compute the Inverse CDF of the Distribution up to the specified Y
     * 
     * @param dblY Y
     * 
     * @return The Inverse CDF of the Distribution up to the specified Y
     * 
     * @throws java.lang.Exception Thrown if Inputs are Invalid
     */

    public static final double InverseCDF (
    	final double dblY)
    	throws java.lang.Exception
    {
    	if (!org.drip.quant.common.NumberUtil.IsValid (dblY))
    		throw new java.lang.Exception ("Gaussian::InverseCDF => Invalid Inputs");

        return InverseCDF (dblY, .00000001, -8., 8.);
    } 
}

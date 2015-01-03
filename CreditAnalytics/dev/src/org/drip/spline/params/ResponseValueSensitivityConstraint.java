
package org.drip.spline.params;

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
 * SegmentResponseValueConstraint holds the SegmentBasisFlexureConstraint instances for the Base Calibration
 * 	and one for each Manifest Measure Sensitivity.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ResponseValueSensitivityConstraint {
	private org.drip.spline.params.SegmentResponseValueConstraint _srvcBase = null;

	private
		org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.spline.params.SegmentResponseValueConstraint>
			_mapSRVCManifestMeasure = new
				org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.spline.params.SegmentResponseValueConstraint>();

	/**
	 * ResponseValueSensitivityConstraint constructor
	 * 
	 * @param srvcBase The Base Calibration Instance of SRVC
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public ResponseValueSensitivityConstraint (
		final org.drip.spline.params.SegmentResponseValueConstraint srvcBase)
		throws java.lang.Exception
	{
		if (null == (_srvcBase = srvcBase))
			throw new java.lang.Exception ("ResponseValueSensitivityConstraint ctr: Invalid Inputs");
	}

	/**
	 * Add the SRVC Instance corresponding to the specified Manifest Measure
	 * 
	 * @param strManifestMeasure The Manifest Measure
	 * @param srvc The SRVC Instance
	 * 
	 * @return TRUE => The SRVC Instance was successfully added
	 */

	public boolean addManifestMeasureSensitivity (
		final java.lang.String strManifestMeasure,
		final org.drip.spline.params.SegmentResponseValueConstraint srvc)
	{
		if (null == strManifestMeasure || strManifestMeasure.isEmpty() || null == srvc) return false;

		_mapSRVCManifestMeasure.put (strManifestMeasure, srvc);

		return true;
	}

	/**
	 * Retrieve the base SRVC Instance
	 * 
	 * @return The Base SRVC Instance
	 */

	public org.drip.spline.params.SegmentResponseValueConstraint base()
	{
		return _srvcBase;
	}

	/**
	 * Retrieve the SRVC Instance Specified by the Manifest Measure
	 * 
	 * @param strManifestMeasure The Manifest Measure
	 * 
	 * @return The SRVC Instance Specified by the Manifest Measure
	 */

	public org.drip.spline.params.SegmentResponseValueConstraint manifestMeasureSensitivity (
		final java.lang.String strManifestMeasure)
	{
		return null != strManifestMeasure && _mapSRVCManifestMeasure.containsKey (strManifestMeasure) ?
			_mapSRVCManifestMeasure.get (strManifestMeasure) : null;
	}

	/**
	 * Return the Set of Available Manifest Measures (if any)
	 * 
	 * @return The Set of Available Manifest Measures
	 */

	public java.util.Set<java.lang.String> manifestMeasures()
	{
		return _mapSRVCManifestMeasure.keySet();
	}
}

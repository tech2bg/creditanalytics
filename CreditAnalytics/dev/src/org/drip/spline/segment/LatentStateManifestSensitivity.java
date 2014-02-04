
package org.drip.spline.segment;

/**
 * LatentStateManifestSensitivity contains the Manifest Sensitivity generation control parameters and outputs
 * 	related to the given Segment.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LatentStateManifestSensitivity {
	private double[] _adblDBasisCoeffDLocalManifest = null;
	private double[] _adblDBasisCoeffDPreceedingManifest = null;
	private double _dblDResponseDPreceedingManifest = java.lang.Double.NaN;
	private org.drip.spline.params.PreceedingManifestSensitivityControl _pmsc = null;

	/**
	 * LatentStateManifestSensitivity constructor
	 * 
	 * @param pmsc The Preceeding Manifest Measure Sensitivity Control Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public LatentStateManifestSensitivity (
		final org.drip.spline.params.PreceedingManifestSensitivityControl pmsc)
		throws java.lang.Exception
	{
		if (null == (_pmsc = pmsc))
			_pmsc = new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 0, null);
	}

	/**
	 * Set the Array containing the Sensitivities of the Basis Coefficients to the Local Manifest Measure
	 * 
	 * @param adblDBasisCoeffDLocalManifest The Array containing the Sensitivities of the Basis Coefficients
	 * 	to the Local Manifest Measure
	 * 
	 * @return TRUE => Basis Coefficient Manifest Measure Sensitivity Array Entries successfully set
	 */

	public boolean setDBasisCoeffDLocalManifest (
		final double[] adblDBasisCoeffDLocalManifest)
	{
		if (null == adblDBasisCoeffDLocalManifest) return false;

		int iNumCoeff = adblDBasisCoeffDLocalManifest.length;
		_adblDBasisCoeffDLocalManifest = new double[iNumCoeff];

		if (0 == iNumCoeff) return false;

		for (int i = 0; i < iNumCoeff; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (_adblDBasisCoeffDLocalManifest[i] =
				adblDBasisCoeffDLocalManifest[i]))
				return false;
		}

		return true;
	}

	/**
	 * Get the Array containing the Sensitivities of the Basis Coefficients to the Local Manifest Measure
	 * 
	 * @return The Array containing the Sensitivities of the Basis Coefficients to the Local Manifest Measure
	 */

	public double[] getDBasisCoeffDLocalManifest()
	{
		return _adblDBasisCoeffDLocalManifest;
	}

	/**
	 * Set the Array containing the Sensitivities of the Basis Coefficients to the Preceeding Manifest
	 * 	Measure
	 * 
	 * @param adblDBasisCoeffDLocalManifest The Array containing the Sensitivities of the Basis Coefficients to
	 * 	the Preceeding Manifest Measure
	 * 
	 * @return TRUE => Array Entries successfully set
	 */

	public boolean setDBasisCoeffDPreceedingManifest (
		final double[] adblDBasisCoeffDPreceedingManifest)
	{
		if (null == adblDBasisCoeffDPreceedingManifest) return false;

		int iNumCoeff = adblDBasisCoeffDPreceedingManifest.length;
		_adblDBasisCoeffDPreceedingManifest= new double[iNumCoeff];

		if (0 == iNumCoeff) return false;

		for (int i = 0; i < iNumCoeff; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (_adblDBasisCoeffDPreceedingManifest[i] =
				adblDBasisCoeffDPreceedingManifest[i]))
				return false;
		}

		return true;
	}

	/**
	 * Get the Array containing the Sensitivities of the Basis Coefficients to the Preceeding Manifest
	 *	Measure
	 * 
	 * @return The Array containing the Sensitivities of the Basis Coefficients to the Preceeding Manifest
	 * 	Measure
	 */

	public double[] getDBasisCoeffDPreceedingManifest()
	{
		return _adblDBasisCoeffDPreceedingManifest;
	}

	/**
	 * Set the Sensitivity of the Segment Response to the Preceeding Manifest Measure
	 * 
	 * @param dblDResponseDPreceedingQuote Sensitivity of the Segment Response to the Preceeding Manifest
	 * 	Measure
	 * 
	 * @return TRUE => Sensitivity of the Segment Response to the Preceeding Manifest Measure successfully
	 * 	set
	 */

	public boolean setDResponseDPreceedingManifest (
		final double dblDResponseDPreceedingManifest)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDResponseDPreceedingManifest)) return false;

		_dblDResponseDPreceedingManifest = dblDResponseDPreceedingManifest;
		return true;
	}

	/**
	 * Get the Sensitivity of the Segment Response to the Preceeding Manifest Measure
	 * 
	 * @return The Sensitivity of the Segment Response to the Preceeding Manifest Measure
	 */

	public double getDResponseDPreceedingManifest()
	{
		return _dblDResponseDPreceedingManifest;
	}

	/**
	 * Get the Preceeding Manifest Measure Sensitivity Control Parameters
	 * 
	 * @return The Preceeding Manifest Measure Sensitivity Control Parameters
	 */

	public org.drip.spline.params.PreceedingManifestSensitivityControl getPMSC()
	{
		return _pmsc;
	}
}

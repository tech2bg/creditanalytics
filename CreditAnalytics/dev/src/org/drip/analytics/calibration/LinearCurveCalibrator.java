
package org.drip.analytics.calibration;

public class LinearCurveCalibrator {
	private org.drip.math.grid.RegimeCalibrationSetting _rcs = null;
	private org.drip.math.segment.PredictorResponseBuilderParams _sbpRegular = null;

	private static final org.drip.math.segment.ResponseValueConstraint GenerateSegmentConstraint (
		final org.drip.analytics.calibration.PredictorResponseLinearConstraint prlc,
		final org.drip.math.grid.MultiSegmentRegime regimeCurrent,
		final org.drip.math.grid.MultiSegmentRegime regimePrev)
	{
		java.util.TreeMap<java.lang.Double, java.lang.Double> mapResponsePredictorWeight =
			prlc.getResponsePredictorWeight();

		if (null == mapResponsePredictorWeight || 0 == mapResponsePredictorWeight.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> setRP =
			mapResponsePredictorWeight.entrySet();

		if (null == setRP) return null;

		double dblValue = 0.;

		java.util.List<java.lang.Double> lsPredictor = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsResponseWeight = new java.util.ArrayList<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : setRP) {
			if (null == me) return null;

			double dblDate = me.getKey();

			try {
				if (null != regimePrev && regimePrev.in (dblDate)) {
					try {
						dblValue -= regimePrev.response (dblDate) * me.getValue();
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}
				} else if (null != regimeCurrent && regimeCurrent.in (dblDate)) {
					try {
						dblValue -= regimeCurrent.response (dblDate) * me.getValue();
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}
				} else {
					lsPredictor.add (dblDate);

					lsResponseWeight.add (me.getValue());
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		int iSize = lsPredictor.size();

		double[] adblPredictor = new double[iSize];
		double[] adblResponseWeight = new double[iSize];

		for (int i = 0; i < iSize; ++i) {
			adblPredictor[i] = lsPredictor.get (i);

			adblResponseWeight[i] = lsResponseWeight.get (i);
		}

		try {
			return new org.drip.math.segment.ResponseValueConstraint (adblPredictor, adblResponseWeight,
				prlc.getValue() + dblValue);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public LinearCurveCalibrator (
		final double dblTensionRegular,
		final org.drip.math.grid.RegimeCalibrationSetting rcs)
		throws java.lang.Exception
	{
		if (null == (_rcs = rcs))
			throw new java.lang.Exception ("LinearCurveCalibrator ctr: Invalid Inputs");

		_sbpRegular = new org.drip.math.segment.PredictorResponseBuilderParams
			(org.drip.math.grid.RegimeBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION, new
				org.drip.math.spline.ExponentialTensionBasisSetParams (dblTensionRegular), new
					org.drip.math.segment.DesignInelasticParams (2, 2), new
						org.drip.math.function.RationalShapeControl (0.));
	}

	public org.drip.math.grid.MultiSegmentRegime singleRegime (
		final org.drip.product.definition.CalibratableComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.analytics.calibration.LatentStateMetricMeasure[] aLSMM)
	{
		if (null == aCalibComp || null == valParams || null == aLSMM) return null;

		int iNumCalibComp = aCalibComp.length;
		org.drip.math.grid.MultiSegmentRegime regime = null;

		if (0 == iNumCalibComp || iNumCalibComp != aLSMM.length) return null;

		for (int i = 0; i < iNumCalibComp; ++i) {
			if (null == aCalibComp[i] || null == aLSMM[i]) return null;

			org.drip.analytics.calibration.PredictorResponseLinearConstraint prlc =
				aCalibComp[i].generateCalibPRLC (valParams, pricerParams, cmp, quotingParams, aLSMM[i]);

			if (null == prlc) return null;

			org.drip.math.segment.ResponseValueConstraint snwc = GenerateSegmentConstraint (prlc, regime,
				null);

			if (null == snwc) return null;

			try {
				if (null == regime) {
					if (null == (regime =
						org.drip.math.grid.RegimeBuilder.CreateUncalibratedRegimeEstimator ("SINGLE", new
							double[] {valParams._dblValue, aCalibComp[i].getMaturityDate().getJulian()}, new
								org.drip.math.segment.PredictorResponseBuilderParams[] {_sbpRegular})) ||
									!regime.setup (1., new org.drip.math.segment.ResponseValueConstraint[]
										{snwc}, _rcs))
						return null;
				} else {
					if (null == (regime = org.drip.math.grid.RegimeModifier.AppendSegment (regime,
						aCalibComp[i].getMaturityDate().getJulian(), snwc, _sbpRegular, _rcs)))
						return null;
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return regime;
	}

	public org.drip.math.grid.OverlappingRegimeSpan regimeFromCashInstruments (
		final org.drip.product.definition.CalibratableComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.analytics.calibration.LatentStateMetricMeasure[] aLSMM)
	{
		if (null == aCalibComp || null == valParams || null == aLSMM) return null;

		int iNumCalibComp = aCalibComp.length;
		org.drip.math.grid.MultiSegmentRegime regime = null;

		if (0 == iNumCalibComp || iNumCalibComp != aLSMM.length) return null;

		for (int i = 0; i < iNumCalibComp; ++i) {
			if (null == aCalibComp[i] || null == aLSMM[i]) return null;

			org.drip.analytics.calibration.PredictorResponseLinearConstraint prlc =
				aCalibComp[i].generateCalibPRLC (valParams, pricerParams, cmp, quotingParams, aLSMM[i]);

			if (null == prlc) return null;

			org.drip.math.segment.ResponseValueConstraint snwc = GenerateSegmentConstraint (prlc, regime,
				null);

			if (null == snwc) return null;

			try {
				if (null == regime) {
					if (null == (regime =
						org.drip.math.grid.RegimeBuilder.CreateUncalibratedRegimeEstimator ("CASH", new
							double[] {valParams._dblValue, aCalibComp[i].getMaturityDate().getJulian()}, new
								org.drip.math.segment.PredictorResponseBuilderParams[] {_sbpRegular})) ||
									!regime.setup (1., new org.drip.math.segment.ResponseValueConstraint[]
										{snwc}, _rcs))
						return null;
				} else {
					if (null == (regime = org.drip.math.grid.RegimeModifier.AppendSegment (regime,
						aCalibComp[i].getMaturityDate().getJulian(), snwc, _sbpRegular, _rcs)))
						return null;
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			return new org.drip.math.grid.OverlappingRegimeSpan (regime);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean regimeFromSwapInstruments (
		org.drip.math.grid.OverlappingRegimeSpan span,
		final org.drip.product.definition.CalibratableComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.analytics.calibration.LatentStateMetricMeasure[] aLSMM)
	{
		if (null == aCalibComp || null == valParams || null == aLSMM) return false;

		int iNumCalibComp = aCalibComp.length;
		org.drip.math.grid.MultiSegmentRegime regimeSwap = null;

		org.drip.math.grid.MultiSegmentRegime regimeCash = null == span ? null : span.getRegime ("CASH");

		if (0 == iNumCalibComp || iNumCalibComp != aLSMM.length) return false;

		for (int i = 0; i < iNumCalibComp; ++i) {
			if (null == aCalibComp[i] || null == aLSMM[i]) return false;

			org.drip.analytics.calibration.PredictorResponseLinearConstraint prlc =
				aCalibComp[i].generateCalibPRLC (valParams, pricerParams, cmp, quotingParams, aLSMM[i]);

			if (null == prlc) return false;

			org.drip.math.segment.ResponseValueConstraint rvc = GenerateSegmentConstraint (prlc, regimeSwap,
				regimeCash);

			if (null == rvc) return false;

			double dblMaturity = aCalibComp[i].getMaturityDate().getJulian();

			try {
				if (null == regimeSwap) {
					if (null == (regimeSwap =
						org.drip.math.grid.RegimeBuilder.CreateUncalibratedRegimeEstimator ("SWAP", new
							double[] {valParams._dblValue, dblMaturity}, new
								org.drip.math.segment.PredictorResponseBuilderParams[] {_sbpRegular})) ||
									!regimeSwap.setup (1., new org.drip.math.segment.ResponseValueConstraint[]
										{rvc}, _rcs))
						return false;
				} else {
					if (null == (regimeSwap = org.drip.math.grid.RegimeModifier.AppendSegment (regimeSwap,
						dblMaturity, rvc, _sbpRegular, _rcs)))
						return false;
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		if (null == span) {
			try {
				span = new org.drip.math.grid.OverlappingRegimeSpan (regimeSwap);

				return true;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		return span.addRegime (regimeSwap);
	}
}

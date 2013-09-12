
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

	public org.drip.math.grid.MultiSegmentRegime markovDiscountCurve (
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
						org.drip.math.grid.RegimeBuilder.CreateUncalibratedRegimeInterpolator ("FULL", new
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

	public org.drip.math.grid.MultiSegmentRegime regimeFromCashInstruments (
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
						org.drip.math.grid.RegimeBuilder.CreateUncalibratedRegimeInterpolator ("CASH", new
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

	public org.drip.math.grid.MultiSegmentRegime regimeFromSwapInstruments (
		final org.drip.math.grid.MultiSegmentRegime regimeCash,
		final org.drip.product.definition.CalibratableComponent[] aCalibComp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final org.drip.analytics.calibration.LatentStateMetricMeasure[] aLSMM)
	{
		if (null == aCalibComp || null == valParams || null == aLSMM) return null;

		int iNumCalibComp = aCalibComp.length;
		org.drip.math.grid.MultiSegmentRegime regimeSwap = null;

		if (0 == iNumCalibComp || iNumCalibComp != aLSMM.length) return null;

		for (int i = 0; i < iNumCalibComp; ++i) {
			if (null == aCalibComp[i] || null == aLSMM[i]) return null;

			org.drip.analytics.calibration.PredictorResponseLinearConstraint prlc =
				aCalibComp[i].generateCalibPRLC (valParams, pricerParams, cmp, quotingParams, aLSMM[i]);

			if (null == prlc) return null;

			org.drip.math.segment.ResponseValueConstraint rvc = GenerateSegmentConstraint (prlc, regimeSwap,
				regimeCash);

			if (null == rvc) return null;

			double dblMaturity = aCalibComp[i].getMaturityDate().getJulian();

			try {
				if (null == regimeSwap) {
					if (null == (regimeSwap =
						org.drip.math.grid.RegimeBuilder.CreateUncalibratedRegimeInterpolator ("SWAP", new
							double[] {valParams._dblValue, dblMaturity}, new
								org.drip.math.segment.PredictorResponseBuilderParams[] {_sbpRegular})) ||
									!regimeSwap.setup (1., new org.drip.math.segment.ResponseValueConstraint[]
										{rvc}, _rcs))
						return null;
				} else {
					if (null == (regimeSwap = org.drip.math.grid.RegimeModifier.AppendSegment (regimeSwap,
						dblMaturity, rvc, _sbpRegular, _rcs)))
						return null;
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return regimeSwap;
	}
}

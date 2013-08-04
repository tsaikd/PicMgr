package org.tsaikd.java.picmgr;

import net.semanticmetadata.lire.imageanalysis.ColorLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ColorLayoutext extends ColorLayout {

	static Log log = LogFactory.getLog(ColorLayoutext.class);

	public double getSumY() {
		double result = 0;
		for (int i=0 ; i<numYCoeff ; i++) {
			result += YCoeff[i];
		}
		return result;
	}

	public double getStdDiffPow2Y(double sum) {
		double result = 0;
		double avg = sum / numYCoeff;
		for (int i=0 ; i<numYCoeff ; i++) {
			result += (YCoeff[i] - avg) * (YCoeff[i] - avg);
		}
		return result;
	}

	public double getSumC() {
		double result = 0;
		for (int i=0 ; i<numCCoeff ; i++) {
			result += CbCoeff[i];
			result += CrCoeff[i];
		}
		return result;
	}

	public double getStdDiffPow2C(double sum) {
		double result = 0;
		double avg = sum / numCCoeff / 2;
		for (int i=0 ; i<numCCoeff ; i++) {
			result += (CbCoeff[i] - avg) * (CbCoeff[i] - avg);
			result += (CrCoeff[i] - avg) * (CrCoeff[i] - avg);
		}
		return result;
	}

}

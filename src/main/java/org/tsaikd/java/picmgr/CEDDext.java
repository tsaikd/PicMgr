package org.tsaikd.java.picmgr;

import net.semanticmetadata.lire.imageanalysis.CEDD;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CEDDext extends CEDD {

	static Log log = LogFactory.getLog(CEDDext.class);

	public double getSum() {
		double result = 0;
		for (double d : data) {
			result += d;
		}
		return result;
	}

	public double getStdDiffPow2(double sum) {
		double result = 0;
		double avg = sum / data.length;
		for (double d : data) {
			result += (d - avg) * (d - avg);
		}
		return result;
	}

}

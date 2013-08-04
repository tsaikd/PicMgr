package org.tsaikd.java.test;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.picmgr.PictureManager;

public class TestPicMgr {

	static Log log = LogFactory.getLog(TestPicMgr.class);

	public static void main(String[] args) throws Exception {

		if (TestPicMgr.class.getSimpleName().equals("TestPicMgr")) {
			ArrayList<String> testArgs = new ArrayList<String>();
			testArgs.add("--do.path");
			testArgs.add("C:\\picmgr\\misc");
			testArgs.add("--picdir.path");
			testArgs.add("C:\\picmgr\\store");
			testArgs.add("-h");
			PictureManager.main(testArgs.toArray(args));
		}

		log.debug("finished");
	}

}

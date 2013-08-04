package org.tsaikd.java.picmgr.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.utils.ConfigUtils;

/**
 * Servlet implementation class Solr
 */
@WebServlet(value="/__init__", loadOnStartup=1)
public class InitServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(InitServlet.class);

	static {
		ConfigUtils.setSearchBase(InitServlet.class);
	}

	public static void nothing() {}

}

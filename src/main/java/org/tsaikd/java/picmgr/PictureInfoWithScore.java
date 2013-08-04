package org.tsaikd.java.picmgr;

import org.apache.solr.client.solrj.beans.Field;

public class PictureInfoWithScore extends PictureInfo {

	@Field
	public double score;

}

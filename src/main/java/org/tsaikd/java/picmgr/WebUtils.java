package org.tsaikd.java.picmgr;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.tsaikd.java.utils.ConfigUtils;

public class WebUtils {

	static Log log = LogFactory.getLog(WebUtils.class);

	protected static String beauty_path = ConfigUtils.get("beauty.path");
	public static Path getImagePath(PictureInfo pic) {
		return Paths.get(beauty_path,
			PictureManager.getImageStorePath(pic.num, pic.fileType));
	}

	public static String image2base64(PictureInfo pic) throws IOException {
		Path picpath = getImagePath(pic);
		byte[] picblob = FileUtils.readFileToByteArray(picpath.toFile());
		return Base64.encodeBase64String(picblob);
	}

	protected static String beauty_solr_url = ConfigUtils.get("beauty.solr.url");
	protected static HttpSolrServer solrBeauty = new HttpSolrServer(beauty_solr_url);
	public static List<PictureInfoWithScore> queryBeauty(SolrQuery query) throws SolrServerException {
		QueryResponse solrRes = solrBeauty.query(query);
		return solrRes.getBeans(PictureInfoWithScore.class);
	}
}

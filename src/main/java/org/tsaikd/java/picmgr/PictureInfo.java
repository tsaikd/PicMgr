package org.tsaikd.java.picmgr;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.solr.client.solrj.beans.Field;

import com.mongodb.BasicDBObjectBuilder;

public class PictureInfo {

	@Field
	public long num;

	@Field
	public int fileSize;

	@Field
	public String sha512;

	@Field
	public int width;

	@Field
	public int height;

	@Field
	public double aspectRatio;

	@Field
	public String fileType;

	@Field
	public double sumCEED;

	@Field
	public double stdDiffCEED;

	@Field
	public byte[] featureCEDD;

	@Field
	public double sumColorLayoutY;

	@Field
	public double stdDiffColorLayoutY;

	@Field
	public double sumColorLayoutC;

	@Field
	public double stdDiffColorLayoutC;

	@Field
	public byte[] descriptorColorLayout;

	@Field
	public Date insertTime = new Date();

	@Override
	public boolean equals(Object info) {
		return EqualsBuilder.reflectionEquals(this, info);
	}

	@Override
	public String toString() {
		return BasicDBObjectBuilder
			.start()
			.add("num", num)
			.add("width", width)
			.add("height", height)
			.add("fileSize", fileSize)
			.add("fileType", fileType)
			.add("sha512", sha512)
			.add("insertTime", insertTime)
			.get()
			.toString();
	}

}

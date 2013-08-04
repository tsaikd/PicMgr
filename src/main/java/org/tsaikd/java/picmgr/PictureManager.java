package org.tsaikd.java.picmgr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.tsaikd.java.utils.ArgParser;
import org.tsaikd.java.utils.ConfigUtils;
import org.tsaikd.java.utils.HashUtils;

public class PictureManager {

	static Log log = LogFactory.getLog(PictureManager.class);

	public static final String config_pictureinfo_url = "pictureinfo.solr.url";
	public static final String config_pictureinfo_url_default = ConfigUtils.get(config_pictureinfo_url, "http://127.0.0.1:8983/solr/core0/");

	public static final String config_picdir_path = "picdir.path";
	public static final String config_picdir_path_default = ConfigUtils.get(config_picdir_path, "/tmp/picture/");

	public static final String config_do_path = "do.path";
	public static final String config_do_path_default = ConfigUtils.get(config_do_path, "/tmp/picmisc/");

	public static final ArgParser.Option[] opts = {
		new ArgParser.Option(null, config_pictureinfo_url, true, config_pictureinfo_url_default, "pictureinfo solr url"),
		new ArgParser.Option(null, config_picdir_path, true, config_picdir_path_default, "picture stored directory path"),
		new ArgParser.Option(null, config_do_path, true, config_do_path_default, "picture to process directory path"),
	};

	public static final Class<?>[] optDep = {
	};

	public static void main(String[] args) throws Exception {
		new PictureManager().mainEntry(args);
	}

	protected PictureManager() {}

	protected HttpSolrServer solr;
	public HttpSolrServer getSolr() {
		if (solr == null) {
			solr = new HttpSolrServer(argParser.getOptString(config_pictureinfo_url));
		}
		return solr;
	}

	protected ArgParser argParser;
	protected long num = 0;
	protected File storepath;
	protected boolean reindex = false;
	public void mainEntry(String[] args) throws Exception {
		argParser = new ArgParser(PictureManager.class);
		argParser.parse(args);

		storepath = new File(argParser.getOptString(config_picdir_path));
		File dopath = new File(argParser.getOptString(config_do_path));

		if (!dopath.isDirectory()) {
			throw new IOException(config_do_path + " is not a existed directory");
		}
		num = getSolrLastestNum();
		recordPictureFiles(dopath.listFiles());
	}

	protected void recordPictureFiles(File[] files) throws IOException, SolrServerException {
		List<File> list = Arrays.asList(files);
		recordPictureFiles(list);
	}

	protected void recordPictureFiles(List<File> files) throws IOException, SolrServerException {
		Collections.sort(files, new Comparator<File>(){
			@Override
			public int compare(File o1, File o2) {
				if (o1.isDirectory() && o2.isFile()) {
					return -1;
				}
				if (o1.isFile() && o2.isDirectory()) {
					return 1;
				}
				return o1.getName().compareTo(o2.getName());
			}
		});

		for (File file : files) {
			recordPictureFile(file);
		}
	}

	protected void recordPictureFile(File file) throws IOException, SolrServerException {
		if (file.isDirectory()) {
			recordPictureFiles(file.listFiles());
			file.delete();
		} else {
			// parse image info
			byte[] data = FileUtils.readFileToByteArray(file);
			BufferedImage image;
			try {
				image = ImageIO.read(new ByteArrayInputStream(data));
			} catch (IIOException e) {
				throw new IIOException(file.toString(), e);
			}
			ImageInputStream iis = ImageIO.createImageInputStream(file);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			ImageReader reader = readers.next();
			PictureInfo info = new PictureInfo();
			info.num = ++num;
			info.fileSize = (int) file.length();
			info.sha512 = HashUtils.sha512(data);
			info.width = image.getWidth();
			info.height = image.getHeight();
			info.aspectRatio = 1.0 * info.width / info.height;
			info.fileType = reader.getFormatName();
			iis.close();

			// extract image feature
			CEDDext ceedext = new CEDDext();
			ceedext.extract(image);
			info.featureCEDD = ceedext.getByteArrayRepresentation();
			info.sumCEED = ceedext.getSum();
			info.stdDiffCEED = ceedext.getStdDiffPow2(info.sumCEED);

			ColorLayoutext colorLayoutext = new ColorLayoutext();
			colorLayoutext.extract(image);
			info.descriptorColorLayout = colorLayoutext.getByteArrayRepresentation();
			info.sumColorLayoutY = colorLayoutext.getSumY();
			info.stdDiffColorLayoutY = colorLayoutext.getStdDiffPow2Y(info.sumColorLayoutY);
			info.sumColorLayoutC = colorLayoutext.getSumC();
			info.stdDiffColorLayoutC = colorLayoutext.getStdDiffPow2C(info.sumColorLayoutC);

			// check image info existed in solr
			File dstFile = getImageStoreFile(info);
			HttpSolrServer solr = getSolr();
			String q = String.format("sha512:\"%1$s\" AND fileSize:%2$d", info.sha512, info.fileSize);
			SolrQuery query = new SolrQuery(q)
				.setSort("num", ORDER.asc);
			QueryResponse solrRes = solr.query(query);
			List<PictureInfo> pics = solrRes.getBeans(PictureInfo.class);
			for (PictureInfo pic : pics) {
				num--;
				File picFile = getImageStoreFile(pic);
				if (picFile.exists()) {
					if (!file.delete()) {
						throw new IOException("delete file failed: " + file);
					}
					return;
				} else {
					dstFile = picFile;
					info.num = pic.num;
					break;
				}
			}

			File dstParentFile = dstFile.getParentFile();
			if (!dstParentFile.exists()) {
				if (!dstParentFile.mkdirs()) {
					throw new IOException("create directory failed: " + dstFile.getParentFile());
				}
			}
			savePictureInfo(info);
			if (!file.renameTo(dstFile)) {
				removePictureInfo(info);
				throw new IOException("move file failed, from:" + file + ", to:" + dstFile);
			}
			log.debug(info);
		}
	}

	protected long getSolrLastestNum() throws SolrServerException {
		HttpSolrServer solr = getSolr();
		SolrQuery query = new SolrQuery("*:*")
			.setSort("num", ORDER.desc)
			.setRows(1);
		QueryResponse solrRes = solr.query(query);
		List<PictureInfo> infos = solrRes.getBeans(PictureInfo.class);
		for (PictureInfo info : infos) {
			return info.num;
		}
		return 0;
	}

	@SuppressWarnings("serial")
	protected static HashMap<String, String> fileTypeMap = new HashMap<String, String>() {{
		put("JPEG", "jpg");
		put("bmp", "bmp");
		put("gif", "gif");
		put("png", "png");
	}};
	public static String getImageStorePath(long num, String fileType) {
		String ext = fileTypeMap.get(fileType);
		if (ext == null) {
			throw new IllegalArgumentException("unsupported fileType: " + fileType);
		}
		long dirnum = num / 1000;
		return String.format("%1$03d/%2$03d/%3$09d.%4$s", dirnum/1000, dirnum%1000, num, ext);
	}

	protected File getImageStoreFile(PictureInfo info) {
		String path = getImageStorePath(info.num, info.fileType);
		return storepath.toPath().resolve(path).toFile();
	}

	protected void savePictureInfo(PictureInfo info) throws IOException, SolrServerException {
		HttpSolrServer solr = getSolr();
		solr.addBean(info);
	}

	protected void removePictureInfo(PictureInfo info) throws IOException, SolrServerException {
		HttpSolrServer solr = getSolr();
		solr.deleteById(String.valueOf(info.num));
	}

}

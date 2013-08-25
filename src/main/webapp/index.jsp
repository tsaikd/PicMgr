<%@page import="org.tsaikd.java.picmgr.PictureInfoWithScore"%>
<%@page import="org.tsaikd.java.picmgr.WebUtils"%>
<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="org.apache.commons.codec.binary.Base64"%>
<%@page import="org.tsaikd.java.picmgr.PictureManager"%>
<%@page import="java.nio.file.Paths"%>
<%@page import="java.nio.file.Path"%>
<%@page import="java.util.List"%>
<%@page import="org.apache.solr.client.solrj.response.QueryResponse"%>
<%@page import="java.util.Random"%>
<%@page import="org.apache.solr.client.solrj.SolrQuery.ORDER"%>
<%@page import="org.apache.solr.client.solrj.SolrQuery"%>
<%@page import="org.apache.solr.client.solrj.impl.HttpSolrServer"%>
<%
SolrQuery query = new SolrQuery("*:*")
	.setRows(10)
	.setSort("random_" + new Random().nextInt(), ORDER.desc);
List<PictureInfoWithScore> pics = WebUtils.queryBeauty(query);
PictureInfoWithScore outpic = null;
for (PictureInfoWithScore pic : pics) {
	Path path = WebUtils.getImagePath(pic);
	if (path.toFile().exists()) {
		outpic = pic;
		break;
	}
}

String q = String.format("{!boost b=sum(%s,%s,%s,%s,%s,%s,%s)}" +
	"aspectRatio:[%f TO %f]" +
	" AND sumColorLayoutY:[%f TO %f]" +
	" AND sumColorLayoutC:[%f TO %f]" +
	" AND sumCEED:[%f TO %f]" +
	" AND stdDiffColorLayoutY:[%f TO %f]" +
	" AND stdDiffColorLayoutC:[%f TO %f]" +
	" AND stdDiffCEED:[%f TO %f]" +
	" AND -num:%d",
	String.format("product(abs(sub(aspectRatio,%f)),3)", outpic.aspectRatio),
	String.format("product(abs(sub(sumColorLayoutY,%f)),1)", outpic.sumColorLayoutY),
	String.format("product(abs(sub(sumColorLayoutC,%f)),0.8)", outpic.sumColorLayoutC),
	String.format("product(abs(sub(sumCEED,%f)),1)", outpic.sumCEED),
	String.format("product(abs(sub(stdDiffColorLayoutY,%f)),0.8)", outpic.stdDiffColorLayoutY),
	String.format("product(abs(sub(stdDiffColorLayoutC,%f)),0.5)", outpic.stdDiffColorLayoutC),
	String.format("product(abs(sub(stdDiffCEED,%f)),0.8)", outpic.stdDiffCEED),
	outpic.aspectRatio - 0.1, outpic.aspectRatio + 0.1,
	outpic.sumColorLayoutY * 0.9, outpic.sumColorLayoutY * 1.1,
	outpic.sumColorLayoutC * 0.9, outpic.sumColorLayoutC * 1.1,
	outpic.sumCEED * 0.9, outpic.sumCEED * 1.1,
	outpic.stdDiffColorLayoutY * 0.9, outpic.stdDiffColorLayoutY * 1.1,
	outpic.stdDiffColorLayoutC * 0.9, outpic.stdDiffColorLayoutC * 1.1,
	outpic.stdDiffCEED * 0.9, outpic.stdDiffCEED * 1.1,
	outpic.num);
query = new SolrQuery(q)
	.setRows(4)
	.setSort("score", ORDER.asc)
	.setFields("*", "score");
pics = WebUtils.queryBeauty(query);
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/include/html_preload.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<%@ include file="/WEB-INF/include/html_head_preload.jsp" %>
		<title>KD's Picture Manager</title>
		<style type="text/css">
body {
	white-space: nowrap;
}
img {
	max-width: 100%;
}
img {
	max-height: 88%;
}
.imageblock.original img {
	max-height: initial;
}
.imagebody {
	min-height: 88%;
}
.imageblock {
	max-width: 600px;
	display: inline-block;
	overflow: hidden;
}
.imageblock.original {
	max-width: initial;
}
.imageattr {
	text-decoration: underline;
	margin-right: 0.5em;
}
		</style>
		<script type="text/javascript">
$(function() {
	$("body")
		.on("click", "img", function() {
			$(this).closest(".imageblock").toggleClass("original");
		});
	$(document)
		.on("keypress", function(e) {
			switch (e.keyCode) {
			case 114: // 'r'
				location.href = location.href;
				break;
			}
		});
});
		</script>
	</head>
	<body style="overflow-x: scroll; margin: 0px;">
		<div style="position: fixed; z-index: 1; background-color: white; padding: 2px 1em;"><a href=".">Reload</a></div>
		<div style="height: 5px;"></div>
		<% { PictureInfoWithScore pic = outpic; %>
		<div class="imageblock">
			<div><%=WebUtils.getImagePath(pic).toString()%></div>
			<div class="imagebody"><img src="data:image/<%=pic.fileType%>;base64,<%=WebUtils.image2base64(pic)%>"/></div>
			<div>
				<span class="imageattr">
					<span><%=pic.width%>x<%=pic.height%></span>
				</span>
				<span class="imageattr">
					<span>fileSize</span>
					<span><%=pic.fileSize%></span>
				</span>
				<span class="imageattr">
					<span>fileType</span>
					<span><%=pic.fileType%></span>
				</span>
			</div>
		</div>
		<% } %>
		<% for (PictureInfoWithScore pic : pics) { %>
		<% if (pic.score > 150) { break; } %>
		<div class="imageblock">
			<div><%=String.format("%.2f", pic.score)%> <%=WebUtils.getImagePath(pic).toString()%></div>
			<div class="imagebody"><img src="data:image/<%=pic.fileType%>;base64,<%=WebUtils.image2base64(pic)%>"/></div>
			<div>
				<span class="imageattr">
					<span><%=pic.width%>x<%=pic.height%></span>
				</span>
				<span class="imageattr">
					<span>fileSize</span>
					<span><%=pic.fileSize%></span>
				</span>
				<span class="imageattr">
					<span>fileType</span>
					<span><%=pic.fileType%></span>
				</span>
			</div>
		</div>
		<% } %>
	</body>
</html>
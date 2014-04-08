package cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.util.common.StringConvertHelper.convertToInteger;
import cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.pojo.Inbox;
import cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.pojo.MailSummary;
import cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.util.common.ResponseStatus;
import cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.util.dom.DomParsingHelper;
import cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.util.http.HttpClientManager;
import cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.util.http.HttpParsingHelper;
import cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.util.http.ReusableHttpClient;
import cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.util.http.HttpParsingHelper.HttpContentType;

@Path("/mail")
public class MailManager {

private static Logger logger = LoggerFactory.getLogger(MailManager.class);
	
	@GET
	@Path("/new")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNewMails(@CookieParam("auth_code") String authCode) {
		
		logger.info(">>>>>>>>>>>>> Start getNewMails <<<<<<<<<<<<<<");
		
		List<MailSummary> newMails = null;
		
		try {
			newMails = getNewMailsFromServer(authCode);
		} catch (Exception e) {
			logger.error("Exception occurs in getNewMails!", e);
			return Response.status(ResponseStatus.SERVER_INTERNAL_ERROR_STATUS).build();
		}
		
		logger.info(">>>>>>>>>>>>> End getNewMails <<<<<<<<<<<<<<");
		return Response.ok().entity(newMails).build();
	}
	
	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMails(@CookieParam("auth_code") String authCode) {
		
		logger.info(">>>>>>>>>>>>> Start getAllMails <<<<<<<<<<<<<<");
		
		Inbox inbox = null;
		
		try {
			inbox = getAllMailsFromServer(authCode, 0);
		} catch (Exception e) {
			logger.error("Exception occurs in getAllMails!", e);
			return Response.status(ResponseStatus.SERVER_INTERNAL_ERROR_STATUS).build();
		}
		
		logger.info(">>>>>>>>>>>>> End getAllMails <<<<<<<<<<<<<<");
		return Response.ok().entity(inbox).build();
	}
	
	@GET
	@Path("/all/{start_num}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMailsWithStartNum(@CookieParam("auth_code") String authCode, @PathParam("start_num") int startNum) {
		
		logger.info(">>>>>>>>>>>>> Start getAllMailsWithStartNum <<<<<<<<<<<<<<");
		
		Inbox inbox = null;
		
		try {
			inbox = getAllMailsFromServer(authCode, startNum);
		} catch (Exception e) {
			logger.error("Exception occurs in getAllMailsWithStartNum!", e);
			return Response.status(ResponseStatus.SERVER_INTERNAL_ERROR_STATUS).build();
		}
		
		logger.info(">>>>>>>>>>>>> End getAllMailsWithStartNum <<<<<<<<<<<<<<");
		return Response.ok().entity(inbox).build();
	}
	
	
	private List<MailSummary> getNewMailsFromServer(String authCode) throws Exception {
		
		ReusableHttpClient reusableClient = HttpClientManager.getInstance().getReusableClient(authCode, false);
		
		URI uri = new URIBuilder().setScheme("http").setHost("bbs.fudan.edu.cn").setPath("/bbs/newmail").build();
		HttpGet httpGet = new HttpGet(uri);
		
		CloseableHttpResponse response = reusableClient.excuteGet(httpGet);
		
		HttpContentType httpContentType = HttpParsingHelper.getContentType(response);
		DomParsingHelper domParsingHelper = HttpParsingHelper.getDomParsingHelper(response, httpContentType);
		response.close();
		
		String xpathOfMail = "/bbsmail/mail";
		
		int nodeCount = domParsingHelper.getNumberOfNodes(xpathOfMail);
		List<MailSummary> newMails = new ArrayList<MailSummary>();
		
		for(int index = 0; index < nodeCount; index++) {
			MailSummary mail = constructNewMail(domParsingHelper, xpathOfMail, index);
			newMails.add(mail);
		}
		
		return newMails;
	}
	
	private MailSummary constructNewMail(DomParsingHelper domParsingHelper, String xpathExpression, int index) {
		
		String markSign = domParsingHelper.getAttributeTextValueOfNode("m", xpathExpression, index);
		String sender = domParsingHelper.getAttributeTextValueOfNode("from", xpathExpression, index);
		String name = domParsingHelper.getAttributeTextValueOfNode("name", xpathExpression, index);
		String number = domParsingHelper.getAttributeTextValueOfNode("n", xpathExpression, index);
		String date = domParsingHelper.getAttributeTextValueOfNode("date", xpathExpression, index);
		
		String title = domParsingHelper.getTextValueOfSingleNode(xpathExpression);
		
		boolean isNew = true;
		
		MailSummary mail = new MailSummary().withIsNew(isNew)
				.withMarkSign(markSign).withSender(sender)
				.withName(name).withDate(date.replace('T', ' '))
				.withTitle(title).withMailNumber(convertToInteger(number));
		
		return mail;
	}
	
	private Inbox getAllMailsFromServer(String authCode, int startNum) throws Exception {
		
		ReusableHttpClient reusableClient = HttpClientManager.getInstance().getReusableClient(authCode, false);
		
		
		URIBuilder uriBuilder = new URIBuilder().setScheme("http").setHost("bbs.fudan.edu.cn").setPath("/bbs/mail");
		if(startNum > 0) {
			uriBuilder.setParameter("start", ""+startNum);
		}
		
		URI uri = uriBuilder.build();
		HttpGet httpGet = new HttpGet(uri);
		
		CloseableHttpResponse response = reusableClient.excuteGet(httpGet);
		
		HttpContentType httpContentType = HttpParsingHelper.getContentType(response);
		DomParsingHelper domParsingHelper = HttpParsingHelper.getDomParsingHelper(response, httpContentType);
		response.close();
		
		String xpathOfMail = "/bbsmail/mail";
		
		
		
		return null;
	}
}

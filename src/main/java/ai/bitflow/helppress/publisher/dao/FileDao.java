
package ai.bitflow.helppress.publisher.dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.extend.HttpStreamFactory;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ai.bitflow.helppress.publisher.constant.ApplicationConstant;
import ai.bitflow.helppress.publisher.domain.Contents;
import ai.bitflow.helppress.publisher.domain.ContentsGroup;
import ai.bitflow.helppress.publisher.repository.ContentsGroupRepository;
import ai.bitflow.helppress.publisher.vo.req.ContentsReq;
import ai.bitflow.helppress.publisher.vo.tree.Node;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 
 * @author metho
 *
 */
@Component
public class FileDao {

	private final Logger logger = LoggerFactory.getLogger(FileDao.class);

	@Value("${app.upload.root.path}")
	private String UPLOAD_ROOT_PATH;
	
	@Value("${app.ext.template.path}")
	private String EXT_TEMPLATE_PATH;

	@Value("${app.history.root.path}")
	private String HISTORY_ROOT_PATH;
	
    @Autowired
    private SpringTemplateEngine tengine;

	@Autowired
	private ChangeHistoryDao chdao;
	
    @Autowired
	private ContentsGroupRepository grepo;
    
    @PostConstruct
    public void init() {
    	this.tengine.setTemplateResolver(templateResolver()); 
	}
	
    private FileTemplateResolver templateResolver() {
    	FileTemplateResolver resolver = new FileTemplateResolver();
    	resolver.setCharacterEncoding("UTF-8");
        resolver.setPrefix(EXT_TEMPLATE_PATH);
        resolver.setSuffix(ApplicationConstant.EXT_HTML);
        resolver.setCacheable(false);
        return resolver;
    }
    
    /**
     * 도움말 파일 생성
     * @param item
     * @param idstring
     * @return
     */
    public boolean newContentFile(Contents item, String idstring) {
		
		File dir = new File(UPLOAD_ROOT_PATH);
		if (!dir.exists()) {
			boolean success = dir.mkdirs();
		}
		 
		BufferedWriter writer = null;
		FileOutputStream fop = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					UPLOAD_ROOT_PATH + File.separator + idstring + ApplicationConstant.EXT_HTML), "UTF-8"));
			writer.write(getHeader(item.getTitle()));
			if (item.getContent()!=null) {
				writer.write(item.getContent());
			}
			writer.write(getFooter());
			//HtmlConverter.convertToPdf(html, new FileOutputStream(dest));
			//PdfRendererBuilder pdfBuilder = new PdfRendererBuilder();
			//File file = new File(UPLOAD_ROOT_PATH + File.separator + idstring + ".pdf");
			//fop = new FileOutputStream(file);
		    return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (writer!=null) {
				try {
					writer.close();
				} catch (IOException e) { }
			}
			//if (fop!=null) {
			//	try {
			//		fop.close();
			//	} catch (IOException e) { }
			//}
		}
	}

	/**
	 * 도움말 HTML 파일 생성
	 * @param item
	 * @return
	 * @throws IOException
	 */
	public boolean updateContentFile(Contents item) {
		
		File dir = new File(UPLOAD_ROOT_PATH);
		if (!dir.exists()) {
			boolean success = dir.mkdirs();
		}
		 
//		BufferedWriter writer = null;
		OutputStream  os = null;
		String destPdfFilename = UPLOAD_ROOT_PATH + item.getGroupId() + File.separator + item.getId() + ApplicationConstant.EXT_PDF;
		try {
//			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destPdfFilename), "UTF-8"));
			StringBuilder content = new StringBuilder();
			content.append(getHeader(item.getTitle()));
			content.append(item.getContent());
			content.append(getFooter());
			
			PdfRendererBuilder builder = new PdfRendererBuilder();
			os = new FileOutputStream(destPdfFilename);
			File fontMalgun = new File(FileDao.class.getResource("/static/fonts/MALGUN.TTF").getFile());
			builder.useFont(fontMalgun, "sans-serif");
			builder.useFont(fontMalgun, "맑은 고딕");
			builder.useFont(fontMalgun, "Arial");
			builder.useFont(fontMalgun, "함초롬바탕");
			builder.useFont(fontMalgun, "굴림");
			builder.useFont(fontMalgun, "굴림체");
			builder.useFont(fontMalgun, "돋움");
			builder.useFont(fontMalgun, "돋움체");
			builder.useFont(fontMalgun, "바탕");
			builder.useFont(fontMalgun, "휴먼명조");
			builder.useFont(fontMalgun, "궁서");
			builder.useFont(fontMalgun, "궁서체");
//			builder.useFont(new File(FileDao.class.getResource("/static/fonts/NanumGothic.ttf").getFile()), "나눔고딕");
//			builder.useFont(new File(FileDao.class.getResource("/static/fonts/H2HDRM.TTF").getFile()), "HY헤드라인M");
			
			W3CDom w3cDom = new W3CDom();
			String baseUri = "file:///" + (dir.getAbsolutePath() + "/export").replace("\\", "/");
			Document w3cDoc = w3cDom.fromJsoup(Jsoup.parse(content.toString(), baseUri));
			builder.withUri(destPdfFilename);
			builder.toStream(os);
			builder.withW3cDocument(w3cDoc, baseUri);
			builder.useHttpStreamImplementation(new OkHttpStreamFactory());
            builder.run();
		    return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (os!=null) {
				try {
					os.close();
				} catch (IOException e) { }
			}
//			if (fop!=null) {
//				try {
//					fop.close();
//				} catch (IOException e) { }
//			}
		}
	}
	
	/**
	 * PDF 업로드
	 * @param params
	 * @param item
	 * @return
	 */
	public boolean newPdfFile(ContentsReq params, Contents item, long now) {
		
		File dir = new File(UPLOAD_ROOT_PATH + item.getGroupId());
		if (!dir.exists()) {
			boolean success = dir.mkdirs();
		}
		dir = new File(HISTORY_ROOT_PATH + item.getGroupId());
		if (!dir.exists()) {
			boolean success = dir.mkdirs();
		}
		 
		String fileName = params.getMenuCode();
		if (fileName==null) {
			logger.error("Cannot find the menucode parameter");
			return false;
		}
		
		FileOutputStream writer1 = null;
		FileOutputStream writer2 = null;
		try {
			
			writer1 = new FileOutputStream(UPLOAD_ROOT_PATH + item.getGroupId() + File.separator + fileName + ApplicationConstant.EXT_PDF);
			writer1.write(params.getFile1().getBytes());

			writer2 = new FileOutputStream(HISTORY_ROOT_PATH + item.getGroupId() + File.separator + now + ApplicationConstant.EXT_PDF);
			writer2.write(params.getFile1().getBytes());
		    return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (writer1!=null) {
				try {
					writer1.close();
				} catch (IOException e) { }
			}
			if (writer2!=null) {
				try {
					writer2.close();
				} catch (IOException e) { }
			}
		}
	}
	
	/**
	 * 전체 도움말 그룹 HTML 재생성
	 * @param list
	 * @return
	 */
	public boolean makeAllContentGroupHTML(List<ContentsGroup> list, String method, String userid) {
		
		logger.debug("makeAllContentGroupHTML");
		// All contents group
		String type   = ApplicationConstant.TYPE_GROUP;
		long now = Calendar.getInstance().getTimeInMillis();
		
		for (int i=0; i<list.size(); i++) {
			ContentsGroup item1 = list.get(i);
			item1.setClassName("is-active");
			// Write to HTML file
			Context ctx = new Context();
			ctx.setVariable("group", list);
			ctx.setVariable("tree",  new Gson().fromJson(item1.getTree(), new TypeToken<List<Node>>(){}.getType()));
			logger.debug("ctx " + ctx.getVariable("tree"));
			String htmlCodes = this.tengine.process("hp-group-template.html", ctx);
			long fileTimeInMillis = now + i + 1;
			makeNewContentGroupTemplate(item1, htmlCodes, fileTimeInMillis);
			item1.setClassName("");
			if (i==0) {
				ctx.setVariable("targetHtml", item1.getGroupId() + ApplicationConstant.EXT_HTML);
				String indexHtmlCodes = this.tengine.process("hp-index-redirection.html", ctx);
				// 첫번째 도움말그룹으로 포워딩 할 index.html 생성
				makeNewIndexHtml(indexHtmlCodes, now);
				// 변경이력 저장
				chdao.addHistory(userid, type, method, "메인 인덱스 파일", "index" + ApplicationConstant.EXT_HTML
						, now + ApplicationConstant.EXT_HTML, "도움말그룹 추가");
			}
			// 변경이력 저장
			chdao.addHistory(userid, type, method, item1.getName(), item1.getGroupId() + ApplicationConstant.EXT_HTML
					, fileTimeInMillis + ApplicationConstant.EXT_HTML, "도움말그룹 추가");
		}
		return true;
	}
	
	public void makeOneContentGroupHTML(ContentsGroup item1, long now) {
		logger.debug("makeOneContentGroupHTML");
		List<ContentsGroup> list = grepo.findAll();
		item1.setClassName("is-active");
		// Write to HTML file
		Context ctx = new Context();
		ctx.setVariable("group", list);
		ctx.setVariable("tree",  new Gson().fromJson(item1.getTree(), new TypeToken<List<Node>>(){}.getType()));
		String htmlCodes = this.tengine.process("hp-group-template.html", ctx);
		makeNewContentGroupTemplate(item1, htmlCodes, now);
		item1.setClassName("");
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean deleteFile(String key) {
		File html = new File(UPLOAD_ROOT_PATH + key + ApplicationConstant.EXT_CONTENT);
		if (html.exists()) {
			return html.delete();
		}
		return true;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	/*
	public boolean deleteFileAndFolder(String key) {
		File html = new File(UPLOAD_ROOT_PATH + File.separator + key + ".html");
		if (html.exists()) {
			return html.delete();
		}
		File resDir = new File(UPLOAD_ROOT_PATH + ApplicationConstant.UPLOAD_REL_PATH + File.separator + key);
		if (resDir.exists() && resDir.isDirectory()) {
			logger.debug("deleting");
			resDir.delete();
			logger.debug("delete success");
		}
		return true;
	}
	 */
	
	/**
	 * 
	 * @param item
	 * @param htmlCodes
	 * @return
	 */
	public boolean makeNewContentGroupTemplate(ContentsGroup item, String htmlCodes, long now) {
		
		File dir1 = new File(UPLOAD_ROOT_PATH);
		if (!dir1.exists()) {
			boolean success = dir1.mkdirs();
		}
		
		File dir2 = new File(HISTORY_ROOT_PATH);
		if (!dir2.exists()) {
			boolean success = dir2.mkdirs();
		}
		
		BufferedWriter writer1 = null;
		BufferedWriter writer2 = null;
		try {
			writer1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					UPLOAD_ROOT_PATH + File.separator + item.getGroupId() + ApplicationConstant.EXT_HTML), "UTF-8"));
			
			writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					HISTORY_ROOT_PATH + File.separator + now + ApplicationConstant.EXT_HTML), "UTF-8"));
			/*
			String htmlpath = new File(UPLOAD_ROOT_PATH + File.separator + item.getGroupId() + ApplicationConstant.EXT_HTML
					).getAbsolutePath();
			*/
			writer1.write(htmlCodes);
			writer2.write(htmlCodes);
		    return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (writer1!=null) {
				try {
					writer1.close();
				} catch (IOException e) { }
			}
			if (writer2!=null) {
				try {
					writer2.close();
				} catch (IOException e) { }
			}
		}
	}
	
	/**
	 * 첫번째 도움말 그룹으로 포워딩 할 index.html 파일 생성
	 * @param htmlCodes
	 * @return
	 */
	private boolean makeNewIndexHtml(String htmlCodes, long now) {
		
		File dir1 = new File(UPLOAD_ROOT_PATH);
		if (!dir1.exists()) {
			boolean success = dir1.mkdirs();
		}
		
		File dir2 = new File(HISTORY_ROOT_PATH);
		if (!dir2.exists()) {
			boolean success = dir2.mkdirs();
		}
		
		BufferedWriter writer1 = null;
		BufferedWriter writer2 = null;
		try {
			writer1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					UPLOAD_ROOT_PATH + File.separator + "index.html"), "UTF-8"));
			writer1.write(htmlCodes);
			
			writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					HISTORY_ROOT_PATH + File.separator + now + ApplicationConstant.EXT_HTML), "UTF-8"));
			writer2.write(htmlCodes);
			
		    return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (writer1!=null) {
				try {
					writer1.close();
				} catch (IOException e) { }
			}
			if (writer2!=null) {
				try {
					writer2.close();
				} catch (IOException e) { }
			}
		}
	}

	private String getHeader(String title) {
		if (title==null) {
			title = "온라인도움말";
		}
		String style = "body { font-family: '맑은 고딕'; }";
//		String style = "";
		return "<!doctype html><html><head><meta charset=\"utf-8\">"
				 + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no, shrink-to-fit=no\">"
				 + "<title>" + title + "</title>"
//				 + "<link rel=\"stylesheet\" href=\"./resources/foundation-icons/foundation-icons.css\" />"
//				 + "<link rel=\"stylesheet\" href=\"./resources/css/page.css\" />"
				 + "<style>" + style + "</style></head><body>";
	}

//	private String getFooter() {
//		return "<div class=\"sticky no-print\" onclick=\"window.print()\"><i class=\"fi-print\"></i></div></body></html>";
//	}
	
	private String getFooter() {
		return "</body></html>";
	}
	
	public org.w3c.dom.Document html5ParseDocument(String urlStr, int timeoutMs) throws IOException {
		URL url = new URL(urlStr);
		org.jsoup.nodes.Document doc;
		
		if (url.getProtocol().equalsIgnoreCase("file")) {
			doc = Jsoup.parse(new File(url.getPath()), "UTF-8");
		}
		else {
			doc = Jsoup.parse(url, timeoutMs);	
		}
		// Should reuse W3CDom instance if converting multiple documents.
		return new W3CDom().fromJsoup(doc);
	}
	
    public static class OkHttpStreamFactory implements HttpStreamFactory {
    	
    	private final OkHttpClient client = new OkHttpClient();
		
	     @Override
	     public FSStream getUrl(String url) {
	        Request request = new Request.Builder()
	          .url(url)
	          .build();
	
	      try {
	       final Response response = client.newCall(request).execute();
	
	       return new FSStream() {
	           @Override
	           public InputStream getStream() {
	               return response.body().byteStream();
	           }
	
	           @Override
	           public Reader getReader() {
	               return response.body().charStream();
	           }
	      };
	    }
	    catch (IOException e) {
	        e.printStackTrace();
	    }
	
	    return null;
	  }
	}
	
}

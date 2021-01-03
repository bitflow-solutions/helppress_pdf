package ai.bitflow.helppress.publisher.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import ai.bitflow.helppress.publisher.constant.ApplicationConstant;

/**
 * 사이냅에디터 이미지 업로드 API
 * @author 김성준
 */
@Controller
public class SynapUploadController {
	
	private final Logger logger = LoggerFactory.getLogger(SynapUploadController.class);

	@Value("${app.upload.root.path}")
	private String UPLOAD_ROOT_PATH;
    
	@PostMapping("/uploadImage")
    @ResponseBody
    public Map<String, Object> uploadImage(HttpServletRequest request, @RequestParam("file") MultipartFile file)
            throws IOException {
		return uploadFile(request, file);
	}
	
    @PostMapping("/uploadFile")
    @ResponseBody
    public Map<String, Object> uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file)
            throws IOException {
    	
    	String key = (String) request.getParameter("key");
        String UPLOAD_DIR_ABS_PATH = UPLOAD_ROOT_PATH + File.separator + ApplicationConstant.UPLOAD_REL_PATH + File.separator + key;
        
        makeDirectory(UPLOAD_DIR_ABS_PATH);
     
        String fileName = file.getOriginalFilename();
        String ext = "";
        String contentType = file.getContentType();
        if(contentType != null) {
            ext = "." + contentType.substring(contentType.lastIndexOf('/') + 1);
        } else if (fileName.lastIndexOf('.') > 0) {
            ext = fileName.substring(fileName.lastIndexOf('.'));
        }
        if (ext.indexOf(".jpeg") > -1) { // jpg가 더많이쓰여서 jpeg는 jpg로 변환
            ext = ".jpg";
        }
        String saveFileName = UUID.randomUUID().toString() + ext;
        String saveFileAbsPath = UPLOAD_DIR_ABS_PATH + File.separator + saveFileName;
     
        writeFile(saveFileAbsPath, file.getBytes());
     
        Map<String, Object> map = new HashMap<String, Object>();
     
        // 브라우저에서 접근가능한 경로를 uploadPath에 담아서 넘겨줍니다.
        map.put("uploadPath", ApplicationConstant.UPLOAD_REL_PATH + "/" + key + "/" + saveFileName);
        map.put("orgFileName", fileName);
        return map;
    }
 
    /**
     * 파일을 씁니다.
     */
    private void writeFile(String path, byte[] bytes) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            os.write(bytes);
        } finally {
            if (os != null) os.close();
        }
    }
     
    /**
     * 디렉토리가 없는 경우 디렉토리를 생성합니다.
     */
    private void makeDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
package ai.bitflow.helppress.publisher.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.InflaterInputStream;

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
 * 사이냅에디터 문서 불러오기(변환) API
 * @author 김성준
 */
@Controller
public class SynapImportController {
	
	private final Logger logger = LoggerFactory.getLogger(SynapImportController.class);

	@Value("${app.upload.root.path}")
	private String UPLOAD_ROOT_PATH;
	@Value("${app.converter.path}")
	private String SEDOC_CONVERTER_REL_PATH;
	private final String TEMP_DOC_REL_PATH 	 = ApplicationConstant.UPLOAD_REL_PATH + File.separator + "tempDocs";
	private final String IMG_UPLOAD_REL_PATH = ApplicationConstant.UPLOAD_REL_PATH;
 
	
	/**
	 * 문서 불러오기
	 * @param request
	 * @param importFile
	 * @return
	 * @throws IOException
	 */
    @PostMapping("/importDoc")
    @ResponseBody
    public Map<String, Object> importDoc(HttpServletRequest request, @RequestParam("file") MultipartFile importFile)
            throws IOException {

    	try {
	    	String TEMP_DOC_UPLOAD_PATH = UPLOAD_ROOT_PATH + TEMP_DOC_REL_PATH;
	        makeDirectory(TEMP_DOC_UPLOAD_PATH);
	
	        Calendar now = Calendar.getInstance();
	        String fileName = String.valueOf(now.getTimeInMillis()); // FilenameUtils.getName(importFile.getOriginalFilename());
	        String inputFileAbsPath = TEMP_DOC_UPLOAD_PATH + File.separator + fileName;
	        logger.debug("inputFileAbsPath " + inputFileAbsPath);
	        writeFile(inputFileAbsPath, importFile.getBytes());
	     
	        // 파일별로 변환결과를 저장할 경로 생성
	        String key = (String) request.getParameter("key");
	        String worksDirAbsPath = UPLOAD_ROOT_PATH + IMG_UPLOAD_REL_PATH + File.separator + key;
	        makeDirectory(worksDirAbsPath);
	 
	        // 문서 변환
	        executeConverter(inputFileAbsPath, worksDirAbsPath);
	     
	        // 변환이 끝난 원본파일은 삭제한다.
	        deleteFile(inputFileAbsPath);
	        
	        // method76) 2020.11.21 김성준 추가
	        deleteFile(worksDirAbsPath + File.separator + "access.inf");
	        
	        // 변환된 pb파일을 읽어서 serialize
	        // v2.3.0 부터 파일명이 document.word.pb에서 document.pb로 변경됨
	        String pbAbsPath = worksDirAbsPath + File.separator + "document.pb";
	        Integer[] serializedData = serializePbData(pbAbsPath);
	     
	        // pb파일은 삭제
	        // v2.3.0 부터 파일명이 document.word.pb에서 document.pb로 변경됨
	        deleteFile(pbAbsPath);
	     
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put("serializedData", serializedData);
	        // 브라우저에서 접근가능한 경로를 importPath에 담아서 넘겨줍니다.
	        // OUTPUT_DIR_REL_PATH 경로에 맞춰서 수정해야 합니다.
	        map.put("importPath", ApplicationConstant.UPLOAD_REL_PATH + "/" + key);
	        
	        return map;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
    }
     
    /**
     * 문서 변환모듈 실행
     * @param inputFilePath
     * @param outputFilePath
     * @return
     */
    private int executeConverter(String inputFilePath, String outputFilePath) {
    	
        String FONT_DIR_ABS_PATH 		= SEDOC_CONVERTER_REL_PATH + "fonts";
        String TEMP_DIR_ABS_PATH 		= SEDOC_CONVERTER_REL_PATH + "temp";
        String SEDOC_CONVERTER_ABS_PATH = SEDOC_CONVERTER_REL_PATH + "sedocConverter.exe"; // window server용
     
        makeDirectory(TEMP_DIR_ABS_PATH);
        makeDirectory(FONT_DIR_ABS_PATH);
 
        // 변화 명령 구성
        String[] cmd = {SEDOC_CONVERTER_ABS_PATH, "-f", FONT_DIR_ABS_PATH, inputFilePath, outputFilePath, TEMP_DIR_ABS_PATH};
        try {
            Timer t = new Timer();
            Process proc = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while((line = br.readLine()) != null){
                logger.debug(line);
            }

            TimerTask killer = new TimeoutProcessKiller(proc);
            t.schedule(killer, 20000); // 20초 (변환이 20초 안에 완료되지 않으면 프로세스 종료)
         
            int exitValue = proc.waitFor();
            killer.cancel();
         
            return exitValue;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
     
    /**
     * 문서 모듈 실행 후 변환된 결과를 Serialize
     * @param pbFilePath
     * @return
     * @throws IOException
     */
    public Integer[] serializePbData(String pbFilePath) throws IOException {
        List<Integer> serializedData = new ArrayList<Integer>();
        FileInputStream fis = null;
        InflaterInputStream ifis = null;
        Integer[] data = null;
     
        try {
            fis = new FileInputStream(pbFilePath);
            fis.skip(16);
     
            ifis = new InflaterInputStream(fis);
            byte[] buffer = new byte[1024];
     
            int len;
            while ((len = ifis.read(buffer)) != -1) {
                for (int i = 0; i < len; i++) {
                    serializedData.add(buffer[i] & 0xFF);
                }
            }
     
            data = serializedData.toArray(new Integer[serializedData.size()]);
        } finally {
            if (ifis != null) ifis.close();
            if (fis != null) fis.close();
        }
     
        return data;
    }
     
    /**
     * 파일 쓰기
     * @param path
     * @param bytes
     * @throws IOException
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
     * 파일 삭제
     * @param path
     */
    private void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
     
    /**
     * 디렉토리가 없는 경우 디렉토리를 생성합니다.
     */
    private void makeDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
        	logger.debug("not exists");
        	logger.debug("making dir " + dir.getAbsolutePath());
            dir.mkdirs();
        }
    }
     
    private static class TimeoutProcessKiller extends TimerTask {
        private Process p;
     
        public TimeoutProcessKiller(Process p) {
            this.p = p;
        }
     
        @Override
        public void run() {
            p.destroy();
        }
    }
    
}

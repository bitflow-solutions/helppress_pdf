package ai.bitflow.helppress.publisher.constant;

/**
 * 전역 상수 설정
 * @author 김성준
 */
public interface ApplicationConstant {

	String METHOD_ADD 	 = "추가";
	String METHOD_MODIFY = "수정";
	String METHOD_DELETE = "삭제";
	String METHOD_RENAME = "REN";
	
	String EXT_HTML 	= ".html";
	String EXT_PDF 		= ".pdf";
	String EXT_CONTENT  = ".pdf"; // 설정 값
	
	String REASON_CHANGE_TREE = "도움말/폴더 순서 변경";
	String REASON_TREE_RENAME = "도움말/폴더 제목 변경";
	String REASON_TREE_ADD 	  = "도움말/폴더 추가";
	String REASON_DELETE_CONTENT_OR_FOLDER = "도움말/폴더 삭제";
	String REASON_DELETE_CONTENT = "도움말 삭제";
	
	String TYPE_RELEASE	 = "RELEASE";
	String TYPE_GROUP 	 = "GROUP";
	String TYPE_CONTENT  = "CONTENT";
	String TYPE_PDF  	 = "PDF";
	String TYPE_HTML  	 = "HTML";
	String TYPE_FOLDER 	 = "FOLDER";

	String RELEASE_ALL 	 = "ALL";
	String RELEASE_PART  = "PART";
	
	String UPLOAD_REL_PATH = "uploads";
	
}

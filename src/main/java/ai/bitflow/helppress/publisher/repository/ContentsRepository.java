package ai.bitflow.helppress.publisher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ai.bitflow.helppress.publisher.domain.Contents;
import ai.bitflow.helppress.publisher.domain.idclass.PkContents;


@Repository
public interface ContentsRepository extends JpaRepository <Contents, PkContents> {

//	List<Contents> findAllByTextContainsOrHtmlcontentContains(List<String> keyword, List<String> keyword2);
//	List<EsFile> findAllByTextOrSummary(String keyword1, String keyword2);
//	List<EsFile> findAllByTextOrSummaryOrAuthor(String keyword1, String keyword2, String keyword3);
//	List<EsFile> findAll();
	
}

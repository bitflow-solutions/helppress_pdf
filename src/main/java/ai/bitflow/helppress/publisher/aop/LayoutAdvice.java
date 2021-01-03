package ai.bitflow.helppress.publisher.aop;

import java.io.IOException;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.Lambda;
import com.samskivert.mustache.Template.Fragment;

import lombok.Setter;


/**
 * Mustach 템플릿 처리 엔진
 * @author 김성준
 *
 */
@ControllerAdvice
class LayoutAdvice {

	private final Compiler compiler;
	
	@Value("${app.title.default}")
	private String DEFAULT_HTML_TITLE;
	
	@Autowired
	public LayoutAdvice(Compiler compiler) {
		this.compiler = compiler;
	}
	
	@ModelAttribute("layout")
	public Lambda layout() {
		return new Layout(compiler);
	}
	
	@ModelAttribute("title")
	public Lambda title(@ModelAttribute Layout layout) {
		return (frag, out) -> {
			layout.setTitle(DEFAULT_HTML_TITLE);
		};
	}
	
	@ModelAttribute("style")
	public Lambda style(@ModelAttribute Layout layout) {
		return (frag, out) -> {
			layout.setStyle(frag.execute());
		};
	}
	
	@ModelAttribute("script")
	public Lambda script(@ModelAttribute Layout layout) {
		return (frag, out) -> {
			layout.setScript(frag.execute());
		};
	}
	
}

@Setter
class Layout implements Lambda {

	private Compiler compiler;
	private String title = "";
	private String style = "";
	private String body = "";
	private String contents = "";
	private String script = "";
	
	public Layout(Compiler compiler) {
		this.compiler = compiler;
	}

	@Override
	public void execute(Fragment frag, Writer out) throws IOException {
		body = frag.execute();
		compiler.compile("{{>layout}}").execute(frag.context(), out);
	}
  
}
 
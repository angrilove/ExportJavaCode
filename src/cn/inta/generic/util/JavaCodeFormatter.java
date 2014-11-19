package cn.inta.generic.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

/**
 * http://lqmh18.blog.163.com/blog/static/35004776201311611618394/
 * 
 * @author 标
 *
 */
public class JavaCodeFormatter {
	/**
	 * format java source by default rule
	 * 
	 * @param fileContent
	 * @exception Exception
	 * @return sourceCode
	 */
	public static String format(String fileContent) {
		String sourceCode = fileContent;
		// get default format for java
		Map<?, ?> options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		Document doc = new Document(sourceCode);

		try {
			Map<String, String> compilerOptions = new HashMap<String, String>();
			// confirm java source base on java 1.5
			compilerOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
			compilerOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
			compilerOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
			DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences, compilerOptions);
			// format
			TextEdit edits = codeFormatter.format(
					CodeFormatter.K_COMPILATION_UNIT, sourceCode, 0,
					sourceCode.length(), 0, null);
			edits.apply(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sourceCode = doc.get();
		return sourceCode;
	}

	public static void main(String[] arg) {
		String javaCodeBefore = "public class 挨拶{public void 挨拶(){System.out.println(\"挨拶\");}}";
		String javaCodeAfter = "";
		System.out.println("format before:" + "\n");
		System.out.println(javaCodeBefore);
		try {
			javaCodeAfter = JavaCodeFormatter.format(javaCodeBefore);
			System.out.println("format after:" + "\n");
			System.out.println(javaCodeAfter);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("format error");
		}
	}
}
package org.exigencecorp.aptutil;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import joist.sourcegen.GClass;

public class Util {

	public static void saveCode(ProcessingEnvironment env, GClass g, Element... originals) {
		try {
			JavaFileObject jfo = env.getFiler().createSourceFile(g.getFullName(), originals);
			Writer w = jfo.openWriter();
			w.write(g.toCode());
			w.close();
		} catch (IOException io) {
			Element hint = originals.length > 0 ? originals[0] : null;
			env.getMessager().printMessage(Kind.ERROR, io.getMessage(), hint);
		}
	}

	public static String lower(String name) {
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	public static String upper(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

}

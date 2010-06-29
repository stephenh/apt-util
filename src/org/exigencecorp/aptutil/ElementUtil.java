package org.exigencecorp.aptutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import joist.util.Join;

public class ElementUtil {

	public static List<String> getTypeParameters(ExecutableElement method) {
		List<String> params = new ArrayList<String>();
		if (method.getTypeParameters().size() != 0) {
			for (TypeParameterElement p : method.getTypeParameters()) {
				String base = p.toString();
				if (p.getBounds().size() > 0) {
					List<String> bounds = new ArrayList<String>();
					for (TypeMirror tm : p.getBounds()) {
						bounds.add(tm.toString());
					}
					base += " extends " + Join.join(bounds, " & ");
				}
				params.add(base);
			}
		}
		return params;
	}

	public static List<String> getArguments(ExecutableElement method) {
		List<String> args = new ArrayList<String>();
		for (VariableElement parameter : method.getParameters()) {
			args.add(parameter.asType().toString() + " " + parameter.getSimpleName());
		}
		return args;
	}

	public static List<? extends VariableElement> getFieldsSorted(TypeElement element) {
		// getAllMembers uses hash maps and so order is non-deterministic
		List<Element> copy = new ArrayList<Element>(element.getEnclosedElements());
		Collections.sort(copy, new Comparator<Element>() {
			@Override
			public int compare(Element o1, Element o2) {
				return o1.getSimpleName().toString().compareTo(o2.getSimpleName().toString());
			}
		});
		return ElementFilter.fieldsIn(copy);
	}

}

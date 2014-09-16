package org.exigencecorp.aptutil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Generated;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import joist.sourcegen.GClass;
import joist.sourcegen.GMethod;

public class PropUtil {

	public static void addGenerated(GClass gclass, Class<?> processorClass) {
		String value = processorClass.getName();
		String date = new SimpleDateFormat("dd MMM yyyy hh:mm").format(new Date());
		gclass.addImports(Generated.class);
		gclass.addAnnotation("@Generated(value = \"" + value + "\", date = \"" + date + "\")");
	}

	public static List<Prop> getProperties(TypeElement element, String prefix) {
		List<Prop> props = new ArrayList<Prop>();
		for (VariableElement field : ElementUtil.getFieldsSorted(element)) {
			String fieldName = field.getSimpleName().toString();
			if (fieldName.startsWith(prefix) && fieldName.length() > prefix.length()) {
				props.add(new Prop(Util.lower(PropUtil.stripPrefixAndIndex(fieldName, prefix)), field.asType().toString()));
			}
		}
		return props;
	}

	public static void addHashCode(GClass gclass, List<Prop> properties) {
		GMethod hashCode = gclass.getMethod("hashCode").addAnnotation("@Override").returnType("int");
		hashCode.body.line("int hashCode = 23;");
		hashCode.body.line("hashCode = (hashCode * 37) + getClass().hashCode();");
		for (Prop p : properties) {
			if (PrimitivesUtil.isPrimitive(p.type)) {
				hashCode.body.line("hashCode = (hashCode * 37) + new {}({}).hashCode();", PrimitivesUtil.getWrapper(p.type), p.name);
			} else if (p.type.endsWith("[]")) {
				hashCode.body.line("hashCode = (hashCode * 37) + java.util.Arrays.deepHashCode({});", p.name);
			} else {
				hashCode.body.line("hashCode = (hashCode * 37) + ({} == null ? 1 : {}.hashCode());", p.name, p.name);
			}
		}
		hashCode.body.line("return hashCode;");
	}

	public static void addToString(GClass gclass, List<Prop> properties) {
		GMethod tos = gclass.getMethod("toString").addAnnotation("@Override").returnType("String");
		tos.body.line("return \"{}[\"", gclass.getSimpleName());
		int i = 0;
		for (Prop p : properties) {
			if (p.type.endsWith("[]")) {
				tos.body.line("    + java.util.Arrays.toString({})", p.name);
			} else {
				tos.body.line("    + {}", p.name);
			}
			if (i++ < properties.size() - 1) {
				tos.body.line("    + \",\"");
			}
		}
		tos.body.line("    + \"]\";");
	}

	public static void addEquals(GClass gclass, GenericSuffix generics, List<Prop> properties) {
		GMethod equals = gclass.getMethod("equals").addAnnotation("@Override").argument("Object", "other").returnType("boolean");
		if (generics.vars.length() > 0) {
			equals.addAnnotation("@SuppressWarnings(\"unchecked\")");
		}
		equals.body.line("if (other != null && other.getClass().equals(this.getClass())) {");
		if (properties.size() == 0) {
			equals.body.line("    return true;");
		} else {
			equals.body.line("    {} o = ({}) other;",//
				gclass.getSimpleName() + generics.vars,//
				gclass.getSimpleName() + generics.vars);
			equals.body.line("    return true"); // leave open
			for (Prop p : properties) {
				if (PrimitivesUtil.isPrimitive(p.type)) {
					equals.body.line("        && o.{} == this.{}", p.name, p.name);
				} else if (p.type.endsWith("[]")) {
					equals.body.line("        && java.util.Arrays.deepEquals(o.{}, this.{})", p.name, p.name);
				} else {
					equals.body.line(
						"        && ((o.{} == null && this.{} == null) || (o.{} != null && o.{}.equals(this.{})))",
						p.name,
						p.name,
						p.name,
						p.name,
						p.name);
				}
			}
			equals.body.line("       ;");
		}
		equals.body.line("}");
		equals.body.line("return false;");
	}

	private static String stripPrefixAndIndex(String name, String prefix) {
		String withoutPrefix = name.substring(prefix.length());
		if (withoutPrefix.length() > 1 && withoutPrefix.substring(0, 1).matches("[0-9]")) {
			return withoutPrefix.substring(1);
		}
		return withoutPrefix;
	}

}

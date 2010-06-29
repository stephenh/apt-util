package org.exigencecorp.aptutil;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

public class TypeVarUtil {

	/**
	 * Given a {@link TypeMirror} {@code type} that could be a type variable,
	 * or have a type variable as generic, or as a bound, using the bound type
	 * arguments in {@code stubInterface}, replace the generics in {@code type}
	 * with the bound values from {@code stubInterface}.
	 *
	 * Extracted from stubgen. Doesn't look at superclasses, doesn't avoid
	 * java.util.Map.Entry bug.
	 */
	public static TypeMirror resolve(Types types, TypeMirror type, DeclaredType stubInterface) {
		TypeElement stubInterfaceType = (TypeElement) stubInterface.asElement();
		if (type == null) {
			return type;
		} else if (type.getKind() == TypeKind.TYPEVAR) {
			if (stubInterface.getTypeArguments().size() > 0) {
				boolean found = false;
				int i = 0;
				for (TypeParameterElement tpe : stubInterfaceType.getTypeParameters()) {
					if (types.isSameType(tpe.asType(), type)) {
						found = true;
						break;
					}
					i++;
				}
				if (found) {
					return stubInterface.getTypeArguments().get(i);
				}
			}
			return type;
		} else if (type.getKind() == TypeKind.WILDCARD) {
			WildcardType typew = (WildcardType) type;
			return types.getWildcardType(//
				resolve(types, typew.getExtendsBound(), stubInterface),//
				resolve(types, typew.getSuperBound(), stubInterface));
		} else if (type.getKind() == TypeKind.DECLARED) {
			DeclaredType typed = (DeclaredType) type;
			if (typed.getTypeArguments().size() == 0) {
				return type;
			}
			TypeMirror[] resolved = new TypeMirror[typed.getTypeArguments().size()];
			for (int i = 0; i < typed.getTypeArguments().size(); i++) {
				resolved[i] = resolve(types, typed.getTypeArguments().get(i), stubInterface);
			}
			return types.getDeclaredType((TypeElement) typed.asElement(), resolved);
		} else {
			return type;
		}
	}

}

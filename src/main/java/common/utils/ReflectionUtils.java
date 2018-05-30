package common.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Various helper methods for performing advanced reflection operations.
 * 
 */
public final class ReflectionUtils {

	// Set Private To Prevent Instantiation
	private ReflectionUtils() {
	}

	/**
	 * Find the class in a class hierarchy declaring the field whose value is
	 * the object provided. The hierarchy is searched starting with the upper
	 * bound until the Object class is reached for a field whose value is the
	 * SAME object as the object provided.
	 * 
	 * @param <T>	Class for upperBound
	 * @param upperBound
	 *            The upper bound of the hierarchy.
	 * @param fieldInstance
	 *            the object provided.
	 * @return Class The class declaring the field whose value is the object provided.
	 */
	public static <T> Class<T> findFieldContainingClassByValue(T upperBound, Object fieldInstance) {
		Field field = findField(upperBound, fieldInstance);
		if (field == null) {
			return null;
		}
		// local variable required to limit scope of suppress
		@SuppressWarnings("unchecked")
		Class<T> declaringClass = (Class<T>) field.getDeclaringClass(); // NOPMD
		return declaringClass;
	}

	/**
	 * Find the field in a class hierarchy whose value is the object provided.
	 * The hierarchy is searched starting with the upper bound and until the
	 * Object class is reached for a field whose value is the same object as the
	 * object provided.
	 * 
	 * @param upperBound
	 *            The upper bound of the hierarchy.
	 * @param fieldInstance
	 *            the object provided.
	 * @return The field whose value is the object provided.
	 */
	public static Field findField(Object upperBound, Object fieldInstance) {
		if (upperBound instanceof Class<?>) {
			throw new IllegalArgumentException(
					"upperBound is a class object instead of an instance object");
		}
		Class<?> clazz = upperBound.getClass();
		while (clazz != null) {

			Field[] fields = clazz.getDeclaredFields();
			for (int ix = 0; ix < fields.length; ix++) {
				Field field = fields[ix];
				try {
					if (!field.getType().isInstance(fieldInstance)) {
						continue;
					}
					field.setAccessible(true);
					Object value;
					value = field.get(upperBound);
					// We are testing for SAME object not equals object
					if (value == fieldInstance) { // NOPMD
						return field;
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	/**
	 * Search a class's hierarchy for an annotation. The hierarchy is searched
	 * starting with the upper bound and until the Object class is reached for
	 * an annotation of the specified type.
	 * 
	 * @param <A> Class of clazz
	 * @param clazz
	 * 			The class
	 * @param annotationClass
	 *            the Class object corresponding to the annotation type
	 * @return A 	this element's hierarchical annotation of the specified
	 *         annotation type if found, else null
	 */
	public static <A extends Annotation> A getAnnotation(Class<?> clazz, final Class<A> annotationClass) {
		if (annotationClass == null || clazz == null) {
			return null;
		}
		for (Class<?> currentClass = clazz; currentClass != null; currentClass = currentClass
				.getSuperclass()) {
			A annotation = currentClass.getAnnotation(annotationClass);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Verify at runtime the generic type of a class.
	 * <p>
	 * For example, to test if a field is a List&lt;String&gt;, invoke the
	 * method as <code>isDecorateType(List.class, String.class, field)</code>
	 * 
	 * @param type
	 *            the expected base type of the field
	 * @param decoratorType
	 *            the expected generic type of the field
	 * @param field
	 *            the field to test
	 * @return <code>true</code> if the field is of the base type and the
	 *         decorated type specified; Otherwise, <code>false</code>,
	 */
	public static boolean isDecoratedType(final Class<?> type,
			final Class<?> decoratorType, final Field field) {
		if (!type.isAssignableFrom(field.getType())) {
			return false;
		}

		// Type erasure in Java isn't complete. Attempt to discover the generic
		// type of the list.
		Type genericType = field.getGenericType();
		if (!(genericType instanceof ParameterizedType)) {
			return false;
		}

		Type declaredDecoratorType = ((ParameterizedType) genericType)
				.getActualTypeArguments()[0];

		if (!declaredDecoratorType.equals(decoratorType)) {
			return false;
		}

		return true;
	}

	private static <T> void addArrayToList(List<T> list, T[] array) {
		for (T t : array) {
			list.add(t);
		}
	}

	/**
	 * Get all the fields that would be visible inside a class.
	 * 
	 * @param clazz
	 *            the class to probe.
	 * @return a list of all the fields that would be visible inside the probed
	 *         class.
	 */
	public static List<Field> getFields(Class<?> clazz) {
		List<Field> fieldList = new ArrayList<Field>();
		for (Class<?> thisClass = clazz; thisClass != null; thisClass = thisClass
				.getSuperclass()) {
			addArrayToList(fieldList, thisClass.getDeclaredFields());
		}
		return fieldList;
	}

	/**
	 * Search for a field through the class hierarchy, a convenience method
	 * using getFields()
	 * 
	 * @param cls
	 *            class on which a field is searched, including the ancestors
	 * @param name
	 *            name of the field
	 * @return field found, or null
	 */
	public static Field getField(Class<?> cls, String name) {
		for (Field f : ReflectionUtils.getFields(cls)) {
			if (name.equals(f.getName())) {
				return f;
			}
		}
		return null;
	}

	/**
	 * a convenience function to get static field of a class
	 * 
	 * @param <T>
	 *            class type
	 * @param c
	 *            class type
	 * @param name
	 *            field name
	 * @param value
	 *            value to set
	 */
	public static <T> void setStaticField(Class<T> c, String name, Object value) {
		try {
			Field f = c.getDeclaredField(name);
			f.setAccessible(true);
			f.set(null, value);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Perform an immediate method invocation. The bean methods are scanned for
	 * the first method that may be invoked with the exact number of parameters
	 * as provided. <code>null</code> parameters are considered as matching any
	 * class.
	 * 
	 * @param methodClass
	 * 			the name of the class.
	 * @param methodName
	 *            the name of the method to invoke.
	 * @param parameters
	 *            the parameters of the method.
	 * @return Method 
	 * 			upon success, if the method has a return value, "RESULT: "
	 *         followed by a toString form of that value, or if it is a void
	 *         method the value "RESULT: VOID" is returned; Upon failure,
	 *         "EXCEPTION: " followed by the exception class name and the
	 *         exception message is returned. *
	 *  @throws NoSuchMethodException
	 *  		throws NoSuchMethodException
	 */
	public static Method findMethod(Class<?> methodClass, String methodName,
			Object... parameters) throws NoSuchMethodException {
		Class<?> searchType = methodClass;
		while (searchType != null) {
			System.out.println("Searching " + searchType.getCanonicalName());
			Method[] methods = (searchType.isInterface() ? searchType
					.getMethods() : searchType.getDeclaredMethods());

			for (Method method : methods) {
				System.out.println(method.getName());
				if (!methodName.equals(method.getName())) {
					continue;
				}
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length != parameters.length) {
					continue;
				}
				if (parametersEqual(parameterTypes, parameters)) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}

		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append("Unable to locate ");
		errorMessage.append(methodClass.getName());
		errorMessage.append(".");
		errorMessage.append(methodName);
		errorMessage.append("(");
		for (Object parameter : parameters) {
			if (parameter == null) {
				errorMessage.append("[null]");
			} else {
				errorMessage.append(parameter.getClass().getName());
			}
		}
		errorMessage.append(")");
		throw new NoSuchMethodException(errorMessage.toString());
	}

	private static Map<Class<?>,Class<?>> SPECIAL_TYPES = new HashMap<Class<?>, Class<?>>();
	static {
		SPECIAL_TYPES.put(Byte.TYPE, Byte.class);
		SPECIAL_TYPES.put(Short.TYPE, Short.class);
		SPECIAL_TYPES.put(Integer.TYPE, Integer.class);
		SPECIAL_TYPES.put(Long.TYPE, Long.class);
		SPECIAL_TYPES.put(Float.TYPE, Float.class);
		SPECIAL_TYPES.put(Double.TYPE, Double.class);
	}
	private static boolean parametersEqual(Class<?>[] parameterTypes,
			Object[] parameters) {
		for (int ix = 0; ix < parameters.length; ix++) {
			if (parameters[ix] == null) {
				continue;
			}
			if (parameterTypes[ix].isInstance(parameters[ix])) {
				continue;
			}
			Class<?> boxedClass = SPECIAL_TYPES.get(parameterTypes[ix]);
			if(boxedClass != null && boxedClass.isInstance(parameters[ix])) {
				continue;
			}
			return false;
		}
		return true;
	}
}
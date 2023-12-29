package com.config;

import static java.util.function.Predicate.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import feign.Param;
import feign.QueryMapEncoder;
import feign.codec.EncodeException;

public class CustomNestedObjectQueryMapEncoder implements QueryMapEncoder {

	private final Map<Class<?>, CustomNestedObjectQueryMapEncoder.ObjectParamMetadata> classToMetadata = new HashMap<>();


	@Override
	public Map<String, Object> encode(Object object) throws EncodeException {
		return encodeInternal(null, object, null);
	}

	private Map<String, Object> encodeInternal(String prefixName, Object object, Map<String, Object> fieldNameToValue) {
		if (null == fieldNameToValue) {
			fieldNameToValue = new HashMap<>();
		}

		try {
			CustomNestedObjectQueryMapEncoder.ObjectParamMetadata metadata = getMetadata(object.getClass());
			for (Field field : metadata.objectFields) {
				Object value = field.get(object);

				if (value != null && value != object) {
					Param alias = field.getAnnotation(Param.class);
					String name = alias != null ? alias.value() : field.getName();

					if (StringUtils.isNotBlank(prefixName)) {
						name = prefixName + "." + name;
					}

					Class<?> aClass = value.getClass();
					ClassLoader classLoader = aClass.getClassLoader();

					if (classLoader == null || aClass.isEnum()) {

						processNameAndValue(name, value, fieldNameToValue);

					} else {
						// Recursive call
						encodeInternal(name, value, fieldNameToValue);
					}
				}
			}
			return fieldNameToValue;
		} catch (IllegalAccessException e) {
			throw new EncodeException("Failure encoding object into query map", e);
		}
	}

	private void processNameAndValue(String name, Object value, Map<String, Object> fieldNameToValue) throws IllegalAccessException {
		// Determines whether it is a custom object collection
		if (isCustomObjectCollection(value)) {
			Collection<?> collection = (Collection<?>) value;

			int i = 0;
			for (Object element : collection) {
				ObjectParamMetadata metadata = getMetadata(element.getClass());

				for (Field field : metadata.objectFields) {
					Object elementValue = field.get(element);

					if (elementValue != null && elementValue != element) {
						Param alias1 = field.getAnnotation(Param.class);
						String elementName = alias1 != null ? alias1.value() : field.getName();

						elementName = name + "[" + i + "]." + elementName;

						ClassLoader classLoader = elementValue.getClass().getClassLoader();

						if (classLoader == null) {

							if (isCustomObjectCollection(elementValue)) {
								// Recursive call
								processNameAndValue(elementName, elementValue, fieldNameToValue);
							} else {
								fieldNameToValue.put(elementName, elementValue);
							}
						}
					}
				}
				i++;
			}
		} else {
			fieldNameToValue.put(name, value);
		}
	}

	private boolean isCustomObjectCollection(Object value) {
		return Optional.ofNullable(value)
			.filter(Collection.class::isInstance)
			.map(Collection.class::cast)
			.filter(not(Collection::isEmpty))
			.stream()
			.flatMap(collection -> (Stream<?>) collection.stream())
			.filter(not(Enum.class::isInstance))
			.map(Object::getClass)
			.map(Class::getClassLoader)
			.anyMatch(Objects::nonNull);
	}

	private CustomNestedObjectQueryMapEncoder.ObjectParamMetadata getMetadata(Class<?> objectType) {
		CustomNestedObjectQueryMapEncoder.ObjectParamMetadata metadata = classToMetadata.get(objectType);
		if (metadata == null) {
			metadata = CustomNestedObjectQueryMapEncoder.ObjectParamMetadata.parseObjectType(objectType);
			classToMetadata.put(objectType, metadata);
		}
		return metadata;
	}


	private static class ObjectParamMetadata {
		private final List<Field> objectFields;

		private ObjectParamMetadata(List<Field> objectFields) {
			this.objectFields = Collections.unmodifiableList(objectFields);
		}

		private static CustomNestedObjectQueryMapEncoder.ObjectParamMetadata parseObjectType(Class<?> type) {
			List<Field> allFields = new ArrayList<>();

			for (Class<?> currentClass = type; currentClass != null && !currentClass.isEnum(); currentClass =
				currentClass.getSuperclass()) {
				Collections.addAll(allFields, currentClass.getDeclaredFields());
			}

			return new CustomNestedObjectQueryMapEncoder.ObjectParamMetadata(allFields.stream()
				.filter(field -> !field.isSynthetic())
				.peek(field -> field.setAccessible(true))
				.collect(Collectors.toList()));
		}
	}

}

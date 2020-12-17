package com.github.TKnudsen.infoVis.view.interaction.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONLoader;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONWriter;
import com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects.BooleanParser;

public class FilterStatusHanders {

	public static <T> FilterStatusHander<T> create() {
		return new FilterStatusHander<T>();
	}

	/**
	 * returns a new collection of elements which are true according to the
	 * predicate. Other elements are filtered out.
	 * 
	 * @param <T>
	 * @param elements
	 * @param filterStatus
	 * @return
	 */
	public static <T> Collection<T> filter(Collection<T> elements, Predicate<T> filterStatus) {
		Collection<T> returnElements = new ArrayList<>();

		if (elements != null)
			for (T t : elements)
				if (filterStatus.test(t))
					returnElements.add(t);

		return returnElements;
	}

	/**
	 * @param <T>
	 * @param elements
	 * @param identifyer
	 * @param fileName
	 * @return
	 */
	public static <T> Predicate<T> loadFilterStatus(Collection<T> elements, Function<T, String> identifyer,
			String fileName) {
		ComplexDataObject cdo = JSONLoader.loadFromFile(fileName);
		BooleanParser booleanParser = new BooleanParser();

		return t -> {
			Object b = cdo.getAttribute(identifyer.apply(t));
			return (b == null) ? true : booleanParser.apply(b);
		};
	}

	/**
	 * @param <T>
	 * @param elements
	 * @param identifyer
	 * @param filterStatus
	 * @param fileName
	 */
	public static <T> void saveFilterStatus(Collection<T> elements, Function<T, String> identifyer,
			Predicate<T> filterStatus, String fileName) {
		ComplexDataObject cdo = new ComplexDataObject();
		for (T t : elements) {
			boolean test = filterStatus.test(t);
			cdo.add(identifyer.apply(t), test);
		}

		JSONWriter.writeToFile(cdo, fileName);
	}

	private FilterStatusHanders() {

	}
}

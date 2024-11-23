package com.github.TKnudsen.infoVis.frames;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.JFrame;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONLoader;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONWriter;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StringUtils;

/**
 * <p>
 * InfoVis
 * </p>
 * 
 * <p>
 * Non-visual components for a category labeling situation. Scenario: m raw
 * categories need to be mapped to a fixed alphabet of m categories.
 * </p>
 * 
 * <p>
 * Copyright: (c) 2023-2024 Juergen Bernard, https://github.com/TKnudsen/infoVis
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public abstract class CategoryLabelingModel2 implements Function<String, String> {

	private SortedMap<String, String> mappingHistory = null;

	private Set<String> labelAlphabet;

	/**
	 * Ask chatGPT for an opinion. Raw category, label alphabet => chatGPT output
	 * with probabilities
	 */
	private final BiFunction<String, Set<String>, String> chatGPTIntegration;

	private boolean fixedAlphabet;

	private boolean waitWhileFeedbackIsPending;

	private boolean rawCategoriesBacklogMode = false;

	/**
	 * 
	 * @param labelAlphabet              can be null/empty at the start. Will be
	 *                                   extended by the data from the file with
	 *                                   existing mappings when initialized.
	 * @param fixedAlphabet              determines if the given label alphabet can
	 *                                   be extended interactively, or if it shall
	 *                                   remain as given
	 * @param waitWhileFeedbackIsPending if the system waits until user feedback is
	 *                                   given (or time is over). Avoids that a)
	 *                                   multiple pop-ups occur and b) that the
	 *                                   system continues without the information
	 *                                   present, e.g., causing
	 *                                   ConcurrentModificationException. If the
	 *                                   feedback is planned for the next runtime,
	 *                                   then do not wait.
	 * @param chatGPTIntegration         ask chatGPT for an opinion. Raw category,
	 *                                   label alphabet => chatGPT output with
	 *                                   probabilities
	 */
	public CategoryLabelingModel2(Set<String> labelAlphabet, boolean fixedAlphabet, boolean waitWhileFeedbackIsPending,
			BiFunction<String, Set<String>, String> chatGPTIntegration) {
		this.labelAlphabet = labelAlphabet;
		this.fixedAlphabet = fixedAlphabet;

		if (fixedAlphabet && (labelAlphabet == null || labelAlphabet.isEmpty()))
			throw new IllegalArgumentException(
					this.getClass().getSimpleName() + ": label alphabet ill-defined (" + labelAlphabet + ")");

		this.waitWhileFeedbackIsPending = waitWhileFeedbackIsPending;
		this.chatGPTIntegration = chatGPTIntegration;
	}

	/**
	 * will be a JSON file. adding JSON as file ending for the String is supported,
	 * but is not necessary.
	 * 
	 * @return
	 */
	public abstract String logFileName();

	public abstract void reportInitializationProblem();

	public abstract String automaticLabelingAttempt(String rawCategory);

	private void initializeMapping() {
		mappingHistory = loadMapping();

		if (labelAlphabet == null)
			if (fixedAlphabet)
				throw new IllegalArgumentException(
						"CategoryLabelingModel: fixed alphabet with no given alphabet makes no sense.");
			else
				labelAlphabet = new TreeSet<>();
		if (!fixedAlphabet)
			labelAlphabet.addAll(mappingHistory.values());
	}

	private final SortedMap<String, String> getMappingHistory() {
		if (mappingHistory == null)
			initializeMapping();

		return mappingHistory;
	}

	@Override
	public String apply(String rawCategory) {
		if (rawCategory == null)
			return "";

		String raw = rawCategory.toString();

		// for lazy access
		SortedMap<String, String> mappingHistory = getMappingHistory();

		if (mappingHistory == null)
			return null;

		String item = mappingHistory.get(raw);
		if (item != null)
			return item;

		// try automatic labeling if possible
		item = automaticLabelingAttempt(rawCategory);
		if (item != null) {
			System.out.println("CategoryLabelingModel: found a very likely mapping automatically: " + rawCategory
					+ " to " + item + ", " + MathFunctions.round(guessCategoryLikelihood(rawCategory, item), 3)
					+ " likelihood and no other category nearly that likely.");
			addToMapping(rawCategory, item, false);
			return item;
		}

		BiFunction<String, String, Double> categoryGuesser = (r, t) -> guessCategoryLikelihood(r, t);

		CategoryLabelingFrame2 frame = new CategoryLabelingFrame2(raw, labelAlphabet, categoryGuesser, fixedAlphabet,
				waitWhileFeedbackIsPending, chatGPTIntegration);

		frameMechanics(frame);

		try {
			frame.waitUntilDone();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("Interrupted while waiting for feedback");
		}

		item = frame.getCategoryLabel();
		if (item == null)
			saveRawCategory(rawCategory);
		else if (!rawCategoriesBacklogMode) {
			rawCategoriesBacklogMode = true;

			// use the chance to ask for raw categories where human-in-the-loop failed
			ComplexDataObject rawCategoryies = loadRawCategory();
			// System.out.println(rawCategoryies);

			SortedSet<String> raws = new TreeSet<>(rawCategoryies.getAttributes());
			// raws.remove("ID");
			raws.remove("Description");
			raws.remove("Name");

			for (String k : getMappingHistory().keySet())
				if (raws.contains(k)) {
					raws.remove(k);
					removeRawCategory(k);
				}

			boolean humanIsInTheLoop = true;
			while (humanIsInTheLoop && !raws.isEmpty()) {
				String x = raws.first();

				System.out.println("CategoryLabelingModel: backlog mode, asking user for another category " + x);
				String result = apply(x);
				if (result == null || result.equals(""))
					humanIsInTheLoop = false;
				else
					removeRawCategory(x);
				raws.remove(x);
			}

			rawCategoriesBacklogMode = false;
		}

		if (item == null)
			item = "";
		else
			addToMapping(raw, item, false);
		item = frame.getCategoryLabel() == null ? "" : frame.getCategoryLabel();
		frame = null;

		System.out.println("CategoryLabelingModel: selected category " + item + " for raw category " + rawCategory
				+ " with" + MathFunctions.round(guessCategoryLikelihood(rawCategory, item), 3) + " likelihood");
		return item;
	}

	private void frameMechanics(CategoryLabelingFrame2 frame) {

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		int x = 800;
		int y = 400;
		int scale = Math.min(600, labelAlphabet.size() * 3);
		frame.setSize(Math.min(1600, x + 4 * scale), y + scale);
		frame.setLocation(50, 50);

		frame.setVisible(true);
		frame.setAlwaysOnTop(true);

		frame.revalidate();
		frame.repaint();
	}

	/**
	 * adds and saves the new key-value pair.
	 * 
	 * @param raw
	 * @param label
	 * @param skipWhenExists if true, keys that exist will not receive new values.
	 */
	public void addToMapping(String raw, String label, boolean skipWhenExists) {
		// for lazy access
		SortedMap<String, String> mapping = getMappingHistory();

		if (mapping == null)
			return;

		if (fixedAlphabet && !labelAlphabet.contains(label)) {
			System.err.println(this.getClass().getSimpleName() + ": ignoring raw tag [" + raw + "] because its label ["
					+ label + "] is not part of the fixed label alphabet");
			return;
		}

		if (skipWhenExists)
			if (mapping.containsKey(raw))
				return;
		mapping.put(raw, label);

		if (!skipWhenExists)
			if (!labelAlphabet.contains(label))
				labelAlphabet.add(label);

		saveMapping();
	}

	/**
	 * 
	 * @return must not be null
	 */
	private SortedMap<String, String> loadMapping() {
		if (logFileName() == null)
			reportInitializationProblem();

		SortedMap<String, String> mapping = new TreeMap<>();

		String file = new String(logFileName());
		if (!file.endsWith(".json"))
			file += ".json";

		if (!new File(file).exists())
			return mapping;

		ComplexDataObject cdo = null;
		try {
			cdo = JSONLoader.loadFromFile(file, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (cdo == null) {
			reportInitializationProblem();
			return null;
		}

		for (String attribute : cdo.getAttributes())
			if (attribute.equals("Name") || attribute.equals("Description"))// || attribute.equals("ID")
				continue;
			else {
				Object o = cdo.getAttribute(attribute);
				if (o != null)
					mapping.put(attribute, o.toString());
			}

		if (fixedAlphabet) {
			Set<String> removeSet = new HashSet<>();
			for (String raw : mapping.keySet())
				if (!labelAlphabet.contains(mapping.get(raw)))
					removeSet.add(raw);
			for (String raw : removeSet) {
				System.err.println(this.getClass().getSimpleName() + "loadMapping: removing raw tag [" + raw
						+ "] because its label [" + mapping.get(raw) + "] not part of the fixed alphabet");
				mapping.remove(raw);
			}
		}

		return mapping;
	}

	private void saveMapping() {
		if (logFileName() == null)
			reportInitializationProblem();

		// for lazy access
		SortedMap<String, String> mapping = getMappingHistory();

		String file = new String(logFileName());
		if (!file.endsWith(".json"))
			file += ".json";

		new File(file.substring(0, file.lastIndexOf("/"))).mkdirs();

		ComplexDataObject cdo = new ComplexDataObject(file.replace(".json", ""));
		for (String attribute : mapping.keySet())
			cdo.add(attribute, mapping.get(attribute));

		JSONWriter.writeToFile(cdo, file);
	}

	/**
	 * all the raw categories that did not yet make it through a human-in-the-loop
	 * approach
	 * 
	 * @return
	 */
	private String rawCategoryFileName() {
		if (logFileName() == null)
			reportInitializationProblem();

		String file = new String(logFileName());
		if (file.endsWith(".json"))
			file = file.substring(0, file.indexOf(".json"));
		file += "_Raw";
		if (!file.endsWith(".json"))
			file += ".json";

		return file;
	}

	/**
	 * all the raw categories that did not yet make it through a human-in-the-loop
	 * approach
	 * 
	 * @return
	 */
	private ComplexDataObject loadRawCategory() {
		String file = rawCategoryFileName();

		ComplexDataObject cdo = null;
		if (new File(file).exists()) {
			try {
				cdo = JSONLoader.loadFromFile(file, false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (cdo == null)
			cdo = new ComplexDataObject(file.replace(".json", ""));

		return cdo;
	}

	/**
	 * saves raw categories that did not yet make it through a human-in-the-loop
	 * approach
	 * 
	 * @param rawCategory
	 */
	private void saveRawCategory(String rawCategory) {
		String file = rawCategoryFileName();

		ComplexDataObject cdo = loadRawCategory();

		cdo.add(rawCategory, new Date());

		new File(file.substring(0, file.lastIndexOf("/"))).mkdirs();

		JSONWriter.writeToFile(cdo, file);
	}

	/**
	 * removes raw categories that now made it through a human-in-the-loop approach
	 * 
	 * @param rawCategory
	 */
	private void removeRawCategory(String rawCategory) {
		String file = rawCategoryFileName();

		ComplexDataObject cdo = loadRawCategory();

		cdo.removeAttribute(rawCategory);

		new File(file.substring(0, file.lastIndexOf("/"))).mkdirs();

		JSONWriter.writeToFile(cdo, file);
	}

	/**
	 * Default implementation of the guesser of the likelihood of a target category
	 * for a given raw category.
	 * 
	 * Basic: Checks the String similarity between the two as a basis.
	 * 
	 * Advanced Also uses the given mapping history to guess mapping candidates (by
	 * comparing other raw keys).
	 * 
	 * @param raw
	 * @param targetCategory
	 * @return
	 */
	public Double guessCategoryLikelihood(String raw, String targetCategory) {
		double d1 = MathFunctions.round(StringUtils.subStringSimilarity(raw, targetCategory, 3), 6);
		double d2 = MathFunctions.round(StringUtils.subStringSimilarity(targetCategory, raw, 3), 6);
		double dA = MathFunctions.round((d1 + d2) * 0.5, 3);

		// for lazy access
		SortedMap<String, String> mapping = getMappingHistory();
		double dB = 0.0;
		boolean occurred = false;
		for (String r : mapping.keySet()) {
			if (!targetCategory.equals(mapping.get(r)))
				continue;

			occurred = true;
			d1 = MathFunctions.round(StringUtils.subStringSimilarity(r, raw, 3), 6);
			d2 = MathFunctions.round(StringUtils.subStringSimilarity(raw, r, 3), 6);
			dB = Math.max(MathFunctions.round((d1 + d2) * 0.5, 3), dB);
		}

		if (occurred)
			return (dA + dB) * 0.5;
		else
			return dA;
	}

	/**
	 * uses the given mapping to guess mapping candidates (by comparing keys to find
	 * appropriate values)
	 * 
	 * @deprecated now the guessCategoryLikelihood method is used as a function for
	 *             more dynamic access, also working for dynamic label alphabets.
	 * @param rawCategory
	 * @return
	 */
	public SortedMap<String, Double> guessCategory(String rawCategory) {
		// for lazy access
		SortedMap<String, String> mapping = getMappingHistory();

		if (mapping == null)
			return null;

		SortedMap<String, Double> result = new TreeMap<>();
		for (String raw : mapping.keySet()) {
			double d = MathFunctions.round(StringUtils.subStringSimilarity(raw, rawCategory, 3), 6);

			String target = mapping.get(raw);

			if (!result.containsKey(target))
				result.put(target, d);
			else
				result.put(target, Math.max(result.get(target), d));
		}

		return result;
	}

	@Override
	public String toString() {
		return "CategoryLabelingModel with " + getMappingHistory().size() + " key-value pairs";
	}

	public Set<String> getLabelAlphabet() {
		return labelAlphabet;
	}

	public void setLabelAlphabet(Set<String> labelAlphabet) {
		this.labelAlphabet = labelAlphabet;
	}

	public boolean isWaitWhileFeedbackIsPending() {
		return waitWhileFeedbackIsPending;
	}

	public void setWaitWhileFeedbackIsPending(boolean waitWhileFeedbackIsPending) {
		this.waitWhileFeedbackIsPending = waitWhileFeedbackIsPending;
	}

}

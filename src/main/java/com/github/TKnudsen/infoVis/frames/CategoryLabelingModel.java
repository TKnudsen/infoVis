package com.github.TKnudsen.infoVis.frames;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.github.TKnudsen.ComplexDataObject.data.complexDataObject.ComplexDataObject;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONLoader;
import com.github.TKnudsen.ComplexDataObject.model.io.json.JSONWriter;
import com.github.TKnudsen.ComplexDataObject.model.tools.MathFunctions;
import com.github.TKnudsen.ComplexDataObject.model.tools.StringUtils;
import com.github.TKnudsen.ComplexDataObject.model.tools.Threads;

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
 * @version 1.02
 */
public abstract class CategoryLabelingModel implements Function<String, String> {

	private SortedMap<String, String> mappingHistory = null;

	private Set<String> labelAlphabet;

	/**
	 * Ask chatGPT for an opinion. Raw category, label alphabet => chatGPT output
	 * with probabilities
	 */
	private final BiFunction<String, Set<String>, String> chatGPTIntegration;

	private boolean fixedAlphabet;

	private boolean doNotContinueWhileWaitingForFeedback;

	/**
	 * 
	 * @param labelAlphabet                        can be null/empty at the start.
	 *                                             Will be extended by the data from
	 *                                             the file with existing mappings
	 *                                             when initialized.
	 * @param fixedAlphabet                        determines if the given label
	 *                                             alphabet can be extended
	 *                                             interactively, or if it shall
	 *                                             remain as given
	 * @param doNotContinueWhileWaitingForFeedback if the system waits until use
	 *                                             feedback is given (or time is
	 *                                             over). Avoids that a) multiple
	 *                                             pop-ups occur and b) that the
	 *                                             system continues without the
	 *                                             information present, e.g.,
	 *                                             causing
	 *                                             ConcurrentModificationException.
	 *                                             If the feedback is planned for
	 *                                             the next runtime, then do not
	 *                                             wait.
	 * @param chatGPTIntegration                   ask chatGPT for an opinion. Raw
	 *                                             category, label alphabet =>
	 *                                             chatGPT output with probabilities
	 */
	public CategoryLabelingModel(Set<String> labelAlphabet, boolean fixedAlphabet,
			boolean doNotContinueWhileWaitingForFeedback, BiFunction<String, Set<String>, String> chatGPTIntegration) {
		this.labelAlphabet = labelAlphabet;
		this.fixedAlphabet = fixedAlphabet;

		if (fixedAlphabet && (labelAlphabet == null || labelAlphabet.isEmpty()))
			throw new IllegalArgumentException(
					this.getClass().getSimpleName() + ": label alphabet ill-defined (" + labelAlphabet + ")");

		this.doNotContinueWhileWaitingForFeedback = doNotContinueWhileWaitingForFeedback;
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
		Consumer<String> resultConsumer = o -> addToMapping(raw, o, false);

		CategoryLabelingFrame frame = new CategoryLabelingFrame(raw, labelAlphabet, resultConsumer, categoryGuesser,
				fixedAlphabet, chatGPTIntegration);

		if (!doNotContinueWhileWaitingForFeedback) {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					frameMechanics(frame);
				}
			});
		} else {
			frameMechanics(frame);
		}

		item = "";

		// otherwise we give back "" this time and the value next time
		if (doNotContinueWhileWaitingForFeedback) {
			while (frame.isDisplayable())
				Threads.sleep(100);
			item = getMappingHistory().get(raw);
		}

		System.out.println("CategoryLabelingModel: selected category " + item + " for raw category " + rawCategory
				+ " with " + MathFunctions.round(guessCategoryLikelihood(rawCategory, item), 3) + " likelihood");
		return item;
	}

	private void frameMechanics(CategoryLabelingFrame frame) {

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		int x = 800;
		int y = 400;
		int scale = Math.min(600, labelAlphabet.size() * 3);
		frame.setSize(x + 2 * scale, y + scale);
		frame.setLocation(50, 50);

		frame.setVisible(true);
		frame.setAlwaysOnTop(true);

		Timer timer = new Timer(500000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();

				String rawCategoryUnlabeled = frame.getRawCategory();
				saveRawCategory(rawCategoryUnlabeled);
			}
		});
		timer.setRepeats(false);
		timer.start();

		frame.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				frame.setVisible(false);
				frame.dispose();
			}
		});
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

		//System.out.println("DEBUG: accessing JSONLoader.loadFromFile for file " + file);
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
			if (attribute.equals("Name") || attribute.equals("Description") || attribute.equals("ID"))
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
				System.err.println(this.getClass().getSimpleName() + ": removing raw tag [" + raw
						+ "] because its label [" + mapping.get(raw) + "] is not part of the fixed label alphabet");
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

	private void saveRawCategory(String rawCategory) {
		if (logFileName() == null)
			reportInitializationProblem();

		String file = new String(logFileName());
		if (file.endsWith(".json"))
			file = file.substring(0, file.indexOf(".json"));
		file += "_Raw";
		if (!file.endsWith(".json"))
			file += ".json";

		// load file, add raw category, save file
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

		cdo.add(rawCategory, new Date());

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

}

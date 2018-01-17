package de.mhus.cherry.vault.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.mongo.MoMetadata;

public class VaultTarget extends MoMetadata {

	private String name;
	private String processorName;
	private MProperties processorConfig;
	private String conditionNames;
	private HashMap<String, MProperties> conditionConfigs;
	private boolean enabled;
	
	public String getName() {
		return name;
	}
	public String getProcessorName() {
		return processorName;
	}
	public synchronized IReadProperties getProcessorConfig() {
		if (processorConfig == null) processorConfig = new MProperties();
		return processorConfig;
	}
	public String getConditionNames() {
		return conditionNames;
	}
	public synchronized IReadProperties getConditionConfig(String name) {
		if (conditionConfigs == null) conditionConfigs = new HashMap<>();
		MProperties out = conditionConfigs.get(name);
		if (out == null) {
			out = new MProperties();
			conditionConfigs.put(name, out);
		}
		return out;
	}
	public boolean isEnabled() {
		return enabled;
	}
	
}

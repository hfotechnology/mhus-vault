package de.mhus.cherry.vault.api.model;

import de.mhus.lib.mongo.MoMetadata;

public class VaultKey extends MoMetadata {

	private String value;
	private String description;
	private String type;

	public VaultKey() {}
	
	public VaultKey(String value, String description, String type) {
		this.value = value;
		this.description = description;
		this.type = type;
	}
	
	public String getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}
	
}

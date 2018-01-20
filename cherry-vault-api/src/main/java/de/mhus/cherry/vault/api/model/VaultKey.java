package de.mhus.cherry.vault.api.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

import de.mhus.lib.core.MSystem;
import de.mhus.lib.mongo.MoMetadata;

public class VaultKey extends MoMetadata {

	@Indexed(options = @IndexOptions(unique = true))
	private String ident;
	private String value;
	private String description;
	private String type;
	private LinkedList<String> readAcl;

	public VaultKey() {}
	
	public VaultKey(String ident, String value, String description, String type) {
		this.ident = ident;
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

	public String getIdent() {
		return ident;
	}
	
	@Override
	public String toString() {
		return MSystem.toString(this, ident, type);
	}
	
	public List<String> getReadAcl() {
		if (readAcl == null) return null;
		return Collections.unmodifiableList(readAcl);
 	}

}

package de.mhus.cherry.vault.api.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;

import de.mhus.lib.core.MSystem;
import de.mhus.lib.mongo.MoMetadata;

public class VaultGroup extends MoMetadata {

	@Indexed(options = @IndexOptions(unique = true))
	private String name;
	private LinkedList<String> targets;
	private String secretGeneratorName;
	private boolean allowUpdate;
	private boolean enabled;
	private LinkedList<String> writeAcl;
	private boolean allowUnencrypted;
	private boolean allowImports;
	private int maxImportLength;
	
	public String getName() {
		return name;
	}

	public List<String> getTargets() {
		if (targets == null) targets = new LinkedList<>();
		return Collections.unmodifiableList(targets);
	}

	public List<String> getWriteAcl() {
		if (writeAcl == null) writeAcl = new LinkedList<>();
		return Collections.unmodifiableList(writeAcl);
	}
	
	public String getSecretGeneratorName() {
		return secretGeneratorName;
	}

	public boolean isAllowUpdate() {
		return allowUpdate;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isAllowUnencrypted() {
		return allowUnencrypted;
	}

	public boolean isAllowImports() {
		return allowImports;
	}
	
	@Override
	public String toString() {
		return MSystem.toString(this, name);
	}

	public int getMaxImportLength() {
		return maxImportLength;
	}

}

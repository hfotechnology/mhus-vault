package de.mhus.cherry.vault.api.ifc;

import de.mhus.cherry.vault.api.model.WritableEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.errors.MException;

public interface TargetProcessor {

	void process(IProperties properties, IReadProperties processorConfig, SecretContent secret, WritableEntry entry) throws MException;

}

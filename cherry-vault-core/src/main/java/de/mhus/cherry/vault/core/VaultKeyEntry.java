package de.mhus.cherry.vault.core;

import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.lib.core.vault.DefaultEntry;

public class VaultKeyEntry extends DefaultEntry {

	public VaultKeyEntry(VaultKey key) {
		super(key.getId(), key.getType(),key.getDescription(),key.getValue());
	}

}

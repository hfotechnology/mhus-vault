package de.mhus.cherry.vault.api.ifc;

import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.lib.core.IProperties;

public interface SecretGenerator {

	SecretContent generateSecret(VaultGroup group, IProperties param);

}

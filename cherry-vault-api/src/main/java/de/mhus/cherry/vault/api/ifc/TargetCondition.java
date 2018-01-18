package de.mhus.cherry.vault.api.ifc;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;

public interface TargetCondition {

	boolean check(IProperties properties, IReadProperties conditionConfig);

}

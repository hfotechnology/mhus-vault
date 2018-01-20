package de.mhus.cherry.vault.core.services;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.ifc.TargetCondition;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;

@Component(properties="name=true")
public class TrueCondition implements TargetCondition {

	@Override
	public boolean check(IProperties properties, IReadProperties conditionConfig) {
		return true;
	}

}

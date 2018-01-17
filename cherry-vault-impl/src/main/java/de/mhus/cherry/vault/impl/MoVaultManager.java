package de.mhus.cherry.vault.impl;

import java.util.List;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.model.VaultArchive;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.cherry.vault.api.model.VaultTarget;
import de.mhus.karaf.mongo.MoManagerService;
import de.mhus.karaf.mongo.MoManagerServiceImpl;
import de.mhus.lib.adb.Persistable;

@Component(immediate=true,provide=MoManagerService.class)
public class MoVaultManager extends MoManagerServiceImpl {

	static MoVaultManager instance;

	@Override
	public void doInitialize() {
		instance = this;
	}

	@Override
	public String getServiceName() {
		return "test";
	}

	@Override
	public String getMongoDataSourceName() {
		return "local";
	}

	@Override
	protected void findObjectTypes(List<Class<? extends Persistable>> list) {
		list.add(VaultGroup.class);
		list.add(VaultTarget.class);
		list.add(VaultEntry.class);
		list.add(VaultArchive.class);
		list.add(VaultKey.class);
	}

}

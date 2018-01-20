package de.mhus.cherry.vault.core;

import java.util.List;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.cherry.vault.api.model.VaultArchive;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.cherry.vault.api.model.VaultTarget;
import de.mhus.cherry.vault.core.impl.StaticAccess;
import de.mhus.karaf.mongo.MoManagerService;
import de.mhus.karaf.mongo.MoManagerServiceImpl;
import de.mhus.lib.adb.Persistable;

@Component(immediate=true,provide=MoManagerService.class)
public class MoVaultManager extends MoManagerServiceImpl {

	@Activate
	public void doActivate(ComponentContext ctx) {
		StaticAccess.moManager = this;
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		StaticAccess.moManager = null;
	}
	
	@Override
	public void doInitialize() {
	}

	@Override
	public String getServiceName() {
		return "cherryvault";
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

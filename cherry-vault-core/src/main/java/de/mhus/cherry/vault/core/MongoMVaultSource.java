package de.mhus.cherry.vault.core;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.cherry.vault.core.impl.StaticAccess;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.vault.MutableVaultSource;
import de.mhus.lib.core.vault.VaultEntry;
import de.mhus.lib.core.vault.VaultSource;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AaaUtil;
import de.mhus.osgi.sop.api.aaa.AccessApi;

@Component(provide=VaultSource.class)
public class MongoMVaultSource extends MutableVaultSource {

	@Activate
	public void doActivate(ComponentContext ctx) {
		name = "CherryVaultLocalSource";
	}

	@Override
	public VaultEntry getEntry(UUID id) {
		try {
			VaultKey key = StaticAccess.moManager.getManager().createQuery(VaultKey.class).filter("ident", id.toString()).get();
			if (key == null) return null;
			List<String> readAcl = key.getReadAcl();
			if (readAcl != null) {
				AaaContext acc = MApi.lookup(AccessApi.class).getCurrentOrGuest();
				if (!AaaUtil.hasAccess(acc, readAcl))
					return null;
			}
			return new VaultKeyEntry(key);
		} catch (Exception e) {
			log().t(id,e);
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public UUID[] getEntryIds() {
		LinkedList<UUID> out = new LinkedList<>();
		AaaContext acc = MApi.lookup(AccessApi.class).getCurrentOrGuest();
		for ( VaultKey obj : StaticAccess.moManager.getManager().createQuery(VaultKey.class).limit(100).fetch()) {
			List<String> readAcl = obj.getReadAcl();
			if (readAcl != null) {
				if (!AaaUtil.hasAccess(acc, readAcl))
					continue;
			}
			out.add(UUID.fromString(obj.getIdent()));
		}
		return out.toArray(new UUID[out.size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T adaptTo(Class<? extends T> ifc) throws NotSupportedException {
		if (ifc.isInstance(this)) return (T) this;
		throw new NotSupportedException(this,ifc);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addEntry(VaultEntry entry) {
		VaultKey key = new VaultKey(entry.getId().toString(), entry.getValue(), entry.getDescription(), entry.getType());
		StaticAccess.moManager.getManager().save(key);
	}
	
	@Override
	public void removeEntry(UUID id) {
		VaultEntry obj = getEntry(id);
		AaaContext acc = MApi.lookup(AccessApi.class).getCurrentOrGuest();
		if (!acc.isAdminMode())
			throw new RuntimeException("only admin can delete entries");
		StaticAccess.moManager.getManager().delete(obj);
	}
	
	@Override
	public String toString() {
		return MSystem.toString(this, name);
	}

	@Override
	public void doLoad() throws IOException {
		
	}

	@Override
	public void doSave() throws IOException {
		
	}

}

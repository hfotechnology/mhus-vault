package de.mhus.cherry.vault.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.vault.DefaultEntry;
import de.mhus.lib.core.vault.MutableVaultSource;
import de.mhus.lib.core.vault.VaultEntry;
import de.mhus.lib.core.vault.VaultSource;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.lib.mongo.MoUtil;

@Component(provide=VaultSource.class)
public class MongoMVaultSource extends MutableVaultSource {

	@Activate
	public void doActivate(ComponentContext ctx) {
		name = "CherryVaultLocalSource";
	}

	@Override
	public VaultEntry getEntry(UUID id) {
		ObjectId oid = MoUtil.toObjectId(id);
		try {
			VaultKey key = MoVaultManager.instance.getManager().getObject(VaultKey.class, oid);
			return new VaultKeyEntry(key);
		} catch (MException e) {
			log().t(oid,id,e);
			return null;
		}
	}

	@Override
	public UUID[] getEntryIds() {
		LinkedList<UUID> out = new LinkedList<>();
		for ( Key<VaultKey> key : MoVaultManager.instance.getManager().createQuery(VaultKey.class).fetchKeys())
			out.add(MoUtil.toUUID((ObjectId)key.getId()));

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
		VaultKey key = new VaultKey(entry.getValue(), entry.getDescription(), entry.getType());
		MoVaultManager.instance.getManager().save(key);
	}
	
	@Override
	public void removeEntry(UUID id) {
		VaultEntry obj = getEntry(id);
		MoVaultManager.instance.getManager().delete(obj);
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

/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.core;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.basics.Ace;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.keychain.MutableVaultSource;
import de.mhus.lib.core.keychain.KeyEntry;
import de.mhus.lib.core.keychain.KeychainSource;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;

@Component(service = KeychainSource.class)
public class CherryMVaultSource extends MLog implements MutableVaultSource {

    private String name;

    @Activate
    public void doActivate(ComponentContext ctx) {
        name = "CherryVaultLocalSource";
    }

    @Override
    public KeyEntry getEntry(UUID id) {
        try {
            VaultKey key = getVaultKey(id);
            if (key == null) return null;
            return new VaultKeyEntry(key);
        } catch (Exception e) {
            log().t(id, e);
            return null;
        }
    }

    @Override
    public KeyEntry getEntry(String name) {
        try {
            VaultKey key = getVaultKey(name);
            if (key == null) return null;
            return new VaultKeyEntry(key);
        } catch (Exception e) {
            log().t(name, e);
            return null;
        }
    }

    public VaultKey getVaultKey(UUID id) {
        try {
            //          VaultKey key =
            // StaticAccess.moManager.getManager().createQuery(VaultKey.class).filter("ident",
            // id.toString()).get();
            VaultKey key =
                    StaticAccess.db
                            .getManager()
                            .getObjectByQualification(Db.query(VaultKey.class).eq("ident", id));
            if (key == null) return null;
            List<String> readAcl = key.getReadAcl();
            if (readAcl != null) {
                if (!AccessUtil.isPermitted(readAcl, VaultKey.class, Ace.READ, key.getIdent())) return null;
            }
            return key;
        } catch (Exception e) {
            log().t(id, e);
            return null;
        }
    }

    public VaultKey getVaultKey(String name) {
        try {
            //          VaultKey key =
            // StaticAccess.moManager.getManager().createQuery(VaultKey.class).filter("ident",
            // id.toString()).get();
            VaultKey key =
                    StaticAccess.db
                            .getManager()
                            .getObjectByQualification(Db.query(VaultKey.class).eq("name", name));
            if (key == null) return null;
            List<String> readAcl = key.getReadAcl();
            if (readAcl != null) {
                if (!AccessUtil.isPermitted(readAcl, VaultKey.class, Ace.READ, key.getIdent())) return null;
            }
            return key;
        } catch (Exception e) {
            log().t(name, e);
            return null;
        }
    }

    @Override
    public Iterable<UUID> getEntryIds() {
        LinkedList<UUID> out = new LinkedList<>();
        try {
            //		for ( VaultKey obj :
            // StaticAccess.moManager.getManager().createQuery(VaultKey.class).limit(100).fetch()) {
            for (VaultKey obj :
                    StaticAccess.db
                            .getManager()
                            .getByQualification(Db.query(VaultKey.class).limit(100))) {
                List<String> readAcl = obj.getReadAcl();
                if (readAcl != null) {
                    if (!AccessUtil.isPermitted(readAcl, VaultKey.class, Ace.READ, obj.getIdent())) return null;
                }
                out.add(UUID.fromString(obj.getIdent()));
            }
        } catch (MException e) {
            log().e(e);
        }
        return out;
    }

    //	@SuppressWarnings("unchecked")
    //	@Override
    //	public <T> T adaptTo(Class<? extends T> ifc) throws NotSupportedException {
    //		if (ifc.isInstance(this)) return (T) this;
    //		throw new NotSupportedException(this,ifc);
    //	}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addEntry(KeyEntry entry) throws MException {
        VaultKey key =
                new VaultKey(
                        entry.getId().toString(),
                        entry.getValue().value(),
                        entry.getDescription(),
                        entry.getType(),
                        entry.getName());
        StaticAccess.db.getManager().inject(key).save();
    }

    @Override
    public void updateEntry(KeyEntry entry) throws MException {
        VaultKey key = getVaultKey(entry.getId());
        if (key == null) throw new NotFoundException("entry not found", entry.getId());

        key.setType(entry.getType());
        key.setName(entry.getName());
        key.setDescription(entry.getDescription());
        key.save();
    }

    @Override
    public void removeEntry(UUID id) throws MException {
        VaultKey obj = getVaultKey(id);
        if (obj == null) return;
        if (!AccessUtil.isAdmin()) throw new RuntimeException("only admin can delete entries");
        //		StaticAccess.moManager.getManager().delete(obj);
        obj.delete();
    }

    @Override
    public String toString() {
        return MSystem.toString(this, name);
    }

    @Override
    public void doLoad() throws IOException {}

    @Override
    public void doSave() throws IOException {}

    @Override
    public boolean isMemoryBased() {
        return false;
    }

    @Override
    public MutableVaultSource getEditable() {
        return this;
    }
}

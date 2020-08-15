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

import java.util.List;

import org.osgi.service.component.annotations.Component;

import de.mhus.cherry.vault.api.model.VaultArchive;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.cherry.vault.api.model.VaultKey;
import de.mhus.cherry.vault.api.model.VaultTarget;
import de.mhus.db.osgi.api.adb.AbstractCommonAdbConsumer;
import de.mhus.db.osgi.api.adb.CommonDbConsumer;
import de.mhus.db.osgi.api.adb.ReferenceCollector;
import de.mhus.lib.errors.MException;
import de.mhus.lib.xdb.XdbService;

@Component(immediate = true, service = CommonDbConsumer.class)
public class CherryVaultManager extends AbstractCommonAdbConsumer {

    @Override
    public void registerObjectTypes(List<Class<?>> list) {
        list.add(VaultGroup.class);
        list.add(VaultTarget.class);
        list.add(VaultEntry.class);
        list.add(VaultArchive.class);
        list.add(VaultKey.class);
    }

    @Override
    public void doInitialize() {
        log().i("Init CherryVaultManager");
        StaticAccess.db = this;
    }

    @Override
    public void doDestroy() {
        log().i("Destroy CherryVaultManager");
        StaticAccess.db = null;
    }

    @Override
    public void collectReferences(Object object, ReferenceCollector collector) {}

    @Override
    public void doCleanup() {}

    @Override
    public boolean canCreate(Object obj) throws MException {
        return true;
    }

    @Override
    public void doPostInitialize(XdbService manager) throws Exception {}
}

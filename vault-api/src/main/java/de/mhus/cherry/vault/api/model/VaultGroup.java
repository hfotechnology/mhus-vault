/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.api.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;

public class VaultGroup extends DbMetadata {

    @DbIndex("u1")
    @DbPersistent
    private String name;

    @DbPersistent private LinkedList<String> targets;
    @DbPersistent private String secretGeneratorName;
    @DbPersistent private MProperties secretGeneratorConfig;
    @DbPersistent private boolean allowUpdate;
    @DbPersistent private boolean enabled;
    @DbPersistent private LinkedList<String> writeAcl;
    @DbPersistent private boolean allowUnencrypted;
    @DbPersistent private boolean allowImports;
    @DbPersistent private int maxImportLength;
    @DbPersistent private String description;

    public String getName() {
        return name;
    }

    public List<String> getTargets() {
        if (targets == null) targets = new LinkedList<>();
        return Collections.unmodifiableList(targets);
    }

    public List<String> getWriteAcl() {
        if (writeAcl == null) writeAcl = new LinkedList<>();
        return Collections.unmodifiableList(writeAcl);
    }

    public String getSecretGeneratorName() {
        return secretGeneratorName;
    }

    public boolean isAllowUpdate() {
        return allowUpdate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAllowUnencrypted() {
        return allowUnencrypted;
    }

    public boolean isAllowImports() {
        return allowImports;
    }

    @Override
    public String toString() {
        return MSystem.toString(this, name);
    }

    public int getMaxImportLength() {
        return maxImportLength;
    }

    @Override
    public DbMetadata findParentObject() throws MException {
        // TODO Auto-generated method stub
        return null;
    }

    public synchronized IReadProperties getSecretGeneratorConfig() {
        if (secretGeneratorConfig == null) secretGeneratorConfig = new MProperties();
        return secretGeneratorConfig;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

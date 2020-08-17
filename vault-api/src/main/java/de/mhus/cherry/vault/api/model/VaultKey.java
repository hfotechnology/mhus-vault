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
import de.mhus.lib.annotations.adb.DbType.TYPE;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.errors.MException;

public class VaultKey extends DbMetadata {

    @DbIndex("u1")
    @DbPersistent
    private String ident;

    @DbPersistent(type = TYPE.BLOB)
    private String value;

    @DbPersistent private String description;
    @DbPersistent private String name;
    @DbPersistent private String type;
    @DbPersistent private LinkedList<String> readAcl;

    public VaultKey() {}

    public VaultKey(String ident, String value, String description, String type, String name) {
        this.ident = ident;
        this.value = value;
        this.description = description;
        this.type = type;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getIdent() {
        return ident;
    }

    @Override
    public String toString() {
        return MSystem.toString(this, ident, type);
    }

    public List<String> getReadAcl() {
        if (readAcl == null) return null;
        return Collections.unmodifiableList(readAcl);
    }

    @Override
    public DbMetadata findParentObject() throws MException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }
}

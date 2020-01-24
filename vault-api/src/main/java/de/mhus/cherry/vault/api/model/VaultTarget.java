/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import de.mhus.lib.core.util.PropertiesSubset;
import de.mhus.lib.errors.MException;

public class VaultTarget extends DbMetadata {

	@DbIndex("u1")
	@DbPersistent
	private String name;
	@DbPersistent
	private String processorName;
	@DbPersistent
	private MProperties processorConfig;
	@DbPersistent
	private String conditionNames;
	@DbPersistent
	private MProperties conditionConfigs;
	@DbPersistent
	private boolean enabled;
	@DbPersistent
	private LinkedList<String> readAcl;
    @DbPersistent
    private String description;

	
	public String getName() {
		return name;
	}
	public String getProcessorName() {
		return processorName;
	}
	public synchronized IReadProperties getProcessorConfig() {
		if (processorConfig == null) processorConfig = new MProperties();
		return processorConfig;
	}
	public String getConditionNames() {
		return conditionNames;
	}
	public synchronized IReadProperties getConditionConfig(String name) {
		if (conditionConfigs == null) conditionConfigs = new MProperties();
		if (name == null) return conditionConfigs;
		return new PropertiesSubset(conditionConfigs,name + ".");
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
	    this.enabled = enabled;
	}
	public List<String> getReadAcl() {
		if (readAcl == null) readAcl = new LinkedList<>();
		return Collections.unmodifiableList(readAcl);
	}
	
	@Override
	public String toString() {
		return MSystem.toString(this, name);
	}
	@Override
	public DbMetadata findParentObject() throws MException {
		// TODO Auto-generated method stub
		return null;
	}
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

	
}

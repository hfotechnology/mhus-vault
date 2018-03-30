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

import de.mhus.lib.core.IProperties;

public class WritableEntry extends VaultEntry {

	public void setTarget(String target) {
		this.target = target;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public void setSecretKeyId(String secretKeyId) {
		this.secretKeyId = secretKeyId;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public void setSecretId(String secretId) {
		this.secretId = secretId;
	}
	@Override
	public IProperties getMeta() {
		return (IProperties) super.getMeta();
	}

	
}

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

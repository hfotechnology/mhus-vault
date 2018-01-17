package de.mhus.cherry.vault.api.model;

import java.util.Date;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.mongo.MoMetadata;

public class VaultEntry extends MoMetadata {

	protected String target;
	protected String group;
	protected String secretKeyId;
	protected String secret;
	protected String secretId;
	protected MProperties meta;
	protected Date validFrom;
	protected Date validTo;
	
	// constructor for morphia
	public VaultEntry() {}
	
	// constructor for VaultArchive
	public VaultEntry(VaultEntry clone) {
		target = clone.getTarget();
		group = clone.getGroup();
		secretKeyId = clone.getSecretKeyId();
		secretId = clone.getSecretId();
		secret = clone.getSecret();
		meta = new MProperties(clone.getMeta());
		validFrom = clone.getValidFrom();
		validTo = clone.getValidTo();
	}
	
	// Constructor for secret create
	public VaultEntry(String target, String group, String secretKeyId, String secret, String secretId, IReadProperties meta) {
		super();
		this.target = target;
		this.group = group;
		this.secretKeyId = secretKeyId;
		this.secret = secret;
		this.secretId = secretId;
		this.meta = new MProperties(meta);
	}

	public String getTarget() {
		return target;
	}

	public String getGroup() {
		return group;
	}

	public String getSecret() {
		return secret;
	}

	public IReadProperties getMeta() {
		if (meta == null) meta = new MProperties();
		return meta;
	}


	public String getSecretKeyId() {
		return secretKeyId;
	}


	public String getSecretId() {
		return secretId;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}
	
}

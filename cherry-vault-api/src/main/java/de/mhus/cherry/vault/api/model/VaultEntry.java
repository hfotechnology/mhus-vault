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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbType.TYPE;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.util.ReadOnlyException;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.sql.DbConnection;
import de.mhus.osgi.sop.api.aaa.AccessApi;

public class VaultEntry extends DbMetadata {

	@DbIndex("1")
	@DbPersistent
	protected String target;
	@DbPersistent
	protected String group;
	@DbPersistent
	protected String secretKeyId;
	@DbPersistent(type=TYPE.BLOB)
	protected String secret;
	@DbIndex({"1","2"})
	@DbPersistent
	protected String secretId;
	@DbPersistent
	protected MProperties meta;
	@DbPersistent
	protected Date validFrom;
	@DbPersistent
	protected Date validTo;
	@DbPersistent
	private String creator;
	
	@DbPersistent
	private String checksum;
	
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
		creator = clone.getCreator();
	}
	
	
	public void preChecksum() throws NoSuchAlgorithmException, UnsupportedEncodingException, ReadOnlyException {
		
		if (creator == null) {
			AccessApi aaa = MApi.lookup(AccessApi.class);
			creator = aaa.getCurrentOrGuest().getAccountId();
		}
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(String.valueOf(secret).getBytes("UTF-8"));
		md.update(String.valueOf(secretKeyId).getBytes("UTF-8"));
		md.update(String.valueOf(secretId).getBytes("UTF-8"));
		md.update(target.getBytes("UTF-8"));
		md.update(group.getBytes("UTF-8"));
		md.update(creator.getBytes("UTF-8"));

		byte[] digest = md.digest();
		String cs = Base64.getEncoder().encodeToString(digest);
		
		if (checksum == null)
			checksum = cs;
		else
		if (!cs.equals(checksum))
			throw new ReadOnlyException("VautlEntry data are read only",getId());
		
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

	public String getCreator() {
		return creator;
	}
	
	@Override
	public String toString() {
		return MSystem.toString(this, secretId, target);
	}

	@Override
	public DbMetadata findParentObject() throws MException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doPreCreate(DbConnection con) {
		try {
			preChecksum();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | ReadOnlyException e) {
			throw new MRuntimeException(e);
		}
		super.doPreCreate(con);
	}

	@Override
	public void doPreSave(DbConnection con) {
		try {
			preChecksum();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | ReadOnlyException e) {
			throw new MRuntimeException(e);
		}
		super.doPreSave(con);
	}
	
}

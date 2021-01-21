/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
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
package de.mhus.app.vault.api.model;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import org.apache.shiro.subject.Subject;

import de.mhus.lib.adb.DbMetadata;
import de.mhus.lib.annotations.adb.DbIndex;
import de.mhus.lib.annotations.adb.DbPersistent;
import de.mhus.lib.annotations.adb.DbType.TYPE;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MSystem;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.util.ReadOnlyException;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.sql.DbConnection;

public class VaultEntry extends DbMetadata {

    @DbIndex({"1", "v0", "v1", "v2", "v3", "v4"})
    @DbPersistent(ro = true)
    protected String target;

    @DbPersistent(ro = true)
    protected String group;

    @DbPersistent(type = TYPE.BLOB)
    protected String secret;

    @DbIndex({"1", "2"})
    @DbPersistent(ro = true)
    protected String secretId;

    @DbPersistent(ro = true)
    protected MProperties meta;

    @DbPersistent(ro = true)
    protected MProperties properties;

    @DbPersistent protected Date validFrom;
    @DbPersistent protected Date validTo;

    @DbPersistent(ro = true)
    private String creator;

    @DbPersistent(ro = true)
    private String checksum;

    @DbPersistent
    @DbIndex("v0")
    private String index0;

    @DbPersistent
    @DbIndex("v1")
    private String index1;

    @DbPersistent
    @DbIndex("v2")
    private String index2;

    @DbPersistent
    @DbIndex("v3")
    private String index3;

    @DbPersistent
    @DbIndex("v4")
    private String index4;

    public VaultEntry() {}

    // constructor for VaultArchive
    public VaultEntry(VaultEntry clone) {
        target = clone.getTarget();
        group = clone.getGroup();
        secretId = clone.getSecretId();
        secret = clone.getSecret();
        meta = new MProperties(clone.getMeta());
        validFrom = clone.getValidFrom();
        validTo = clone.getValidTo();
        creator = clone.getCreator();
    }

    public void preChecksum()
            throws NoSuchAlgorithmException, UnsupportedEncodingException, ReadOnlyException {

        if (creator == null) {
            Subject subject = AccessUtil.getSubject();
            creator = AccessUtil.getPrincipal(subject);
        }

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(String.valueOf(secret).getBytes("UTF-8"));
        md.update(String.valueOf(secretId).getBytes("UTF-8"));
        md.update(target.getBytes("UTF-8"));
        md.update(group.getBytes("UTF-8"));
        md.update(creator.getBytes("UTF-8"));

        byte[] digest = md.digest();
        String cs = Base64.getEncoder().encodeToString(digest);

        if (checksum == null) checksum = cs;
        else if (!cs.equals(checksum))
            throw new ReadOnlyException("VautlEntry data are read only", getId());
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

    public IReadProperties getProperties() {
        if (properties == null) properties = new MProperties();
        return properties;
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

    public String getIndex0() {
        return index0;
    }

    public void setIndex0(String index0) {
        this.index0 = index0;
    }

    public String getIndex1() {
        return index1;
    }

    public void setIndex1(String index1) {
        this.index1 = index1;
    }

    public String getIndex2() {
        return index2;
    }

    public void setIndex2(String index2) {
        this.index2 = index2;
    }

    public String getIndex3() {
        return index3;
    }

    public void setIndex3(String index3) {
        this.index3 = index3;
    }

    public String getIndex4() {
        return index4;
    }

    public void setIndex4(String index4) {
        this.index4 = index4;
    }
}

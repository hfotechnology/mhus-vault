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
package de.mhus.cherry.vault.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import de.mhus.cherry.vault.api.CherryVaultApi;
import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.ifc.SecretGenerator;
import de.mhus.cherry.vault.api.ifc.TargetCondition;
import de.mhus.cherry.vault.api.ifc.TargetProcessor;
import de.mhus.cherry.vault.api.model.VaultArchive;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.cherry.vault.api.model.VaultTarget;
import de.mhus.cherry.vault.api.model.WritableEntry;
import de.mhus.lib.adb.DbCollection;
import de.mhus.lib.adb.query.AQuery;
import de.mhus.lib.adb.query.Db;
import de.mhus.lib.basics.Ace;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.core.crypt.pem.PemBlockList;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.logging.ITracer;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.util.EmptyList;
import de.mhus.lib.core.util.SecureString;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.UsageException;
import de.mhus.lib.xdb.XdbService;
import de.mhus.osgi.api.MOsgi;
import de.mhus.osgi.crypt.api.CryptApi;
import io.opentracing.Scope;

@Component(immediate = true)
public class VaultApiImpl extends MLog implements CherryVaultApi {

    @SuppressWarnings("deprecation")
    public static final Date END_OF_DAYS = new Date(3000 - 1900, 0, 1);

    private static final String DEFAULT_GROUP_NAME = "default";
    private static final CfgString CFG_DEFAULT_GROUP_NAME =
            new CfgString(CherryVaultApi.class, "defaultGroup", DEFAULT_GROUP_NAME);
    private static final int INDEXES = 5;
    public static VaultApiImpl instance;

    @Activate
    public void doActivate(ComponentContext ctx) {
        instance = this;
    }

    @Deactivate
    public void doDeactivate(ComponentContext ctx) {
        instance = null;
    }

    @Override
    public String createSecret(
            String groupName, Date validFrom, Date validTo, IProperties properties, String[] index)
            throws MException {
        try (Scope scope =
                ITracer.get()
                        .enter(
                                "createSecret",
                                "group",
                                groupName,
                                "validFrom",
                                validFrom,
                                "validTo",
                                validTo,
                                "properties",
                                properties,
                                "index",
                                index)) {
            if (validFrom == null) validFrom = new Date();
            if (validTo == null) validTo = END_OF_DAYS;

            // get group
            VaultGroup group = getGroup(groupName);

            // check write access
            List<String> acl = group.getWriteAcl();
            if (!AccessUtil.isPermitted(acl, VaultGroup.class, Ace.UPDATE, group.getName()))
                throw new AccessDeniedException("Write access to group denied", groupName);

            // get and execute secret generation
            String generatorName = group.getSecretGeneratorName();
            if (MString.isEmpty(generatorName))
                throw new UsageException("Group can't generate secrets", groupName);

            SecretGenerator generator = getGenerator(generatorName);

            SecretContent secret = generator.generateSecret(group, properties);
            if (secret == null) throw new MException("Secret is null");

            // create entries by targets
            String secretId = UUID.randomUUID().toString();
            log().d("create secret", groupName, secretId);

            // -- cache entries to save. Save if everything target was ok
            LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
            processGroupTargets(group, properties, secretId, secret, entriesToSave);

            if (entriesToSave.size() == 0) return null;

            updateIndexes(entriesToSave, index, properties);

            // save entries
            saveEntries(groupName, entriesToSave, validFrom, validTo);

            scope.span().setTag("secretId", secretId);
            return secretId;
        }
    }

    public void updateIndexes(
            LinkedList<VaultEntry> entriesToSave, String[] index, IProperties properties) {
        // if (index == null || index.length == 0) return;

        entriesToSave.forEach(
                e -> {
                    if (index != null)
                        for (int i = 0; i < index.length; i++) {
                            String val = index[i];
                            if (MString.isEmpty(val)) continue;
                            switch (i) {
                                case 0:
                                    e.setIndex0(val);
                                    break;
                                case 1:
                                    e.setIndex1(val);
                                    break;
                                case 2:
                                    e.setIndex2(val);
                                    break;
                                case 3:
                                    e.setIndex3(val);
                                    break;
                                case 4:
                                    e.setIndex4(val);
                                    break;
                            }
                        }
                    if (properties != null)
                        for (Map.Entry<String, Object> entry : properties.entrySet())
                            if (!e.getProperties().containsKey(entry.getKey()))
                                ((MProperties) e.getProperties())
                                        .put(entry.getKey(), entry.getValue());
                });
    }

    @Override
    public void createUpdate(
            String secretId, Date validFrom, Date validTo, IProperties properties, String[] index)
            throws MException {

        try (Scope scope =
                ITracer.get()
                        .enter(
                                "createUpdate",
                                "secretId",
                                secretId,
                                "validFrom",
                                validFrom,
                                "validTo",
                                validTo,
                                "properties",
                                properties,
                                "index",
                                index)) {

            if (validFrom == null) validFrom = new Date();
            if (validTo == null) validTo = END_OF_DAYS;

            // get group
            String groupName = findGroupNameForSecretId(secretId);
            VaultGroup group = getGroup(groupName);

            if (properties == null) properties = new MProperties();
            List<VaultEntry> secrets = getSecrets(secretId);
            if (secrets.size() > 0) {
                for (Map.Entry<String, Object> entry : secrets.get(0).getProperties().entrySet())
                    if (!properties.containsKey(entry.getKey()))
                        properties.put(entry.getKey(), entry.getValue());
                index = fillIndex(index, secrets.get(0));
            }
            // check write access
            List<String> acl = group.getWriteAcl();
            if (!AccessUtil.isPermitted(acl, VaultGroup.class, Ace.UPDATE, group.getName()))
                throw new AccessDeniedException("Write access to group denied", groupName);

            if (!group.isAllowUpdate())
                throw new AccessDeniedException("The group dos not allow updates", groupName);

            // get and execute secret generation
            String generatorName = group.getSecretGeneratorName();
            if (MString.isEmpty(generatorName))
                throw new UsageException("Group can't generate secrets", groupName);

            SecretGenerator generator = getGenerator(generatorName);

            SecretContent secret = generator.generateSecret(group, properties);
            if (secret == null) throw new MException("Secret is null");

            log().d("create update", groupName, secretId);

            // -- cache entries to save. Save if everything target was ok
            LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
            processGroupTargets(group, properties, secretId, secret, entriesToSave);

            updateEntriesValidTo(secretId, validFrom);

            updateIndexes(entriesToSave, index, properties);

            // save entries
            saveEntries(groupName, entriesToSave, validFrom, validTo);
        }
    }

    private String[] fillIndex(String[] index, VaultEntry vaultEntry) {
        String[] out = new String[INDEXES];
        for (int i = 0; i < out.length; i++) {
            out[i] = null;
            if (index != null && index.length > i && MString.isSet(index[i])) out[i] = index[i];
            if (MString.isEmpty(out[i]))
                switch (i) {
                    case 0:
                        out[i] = vaultEntry.getIndex0();
                        break;
                    case 1:
                        out[i] = vaultEntry.getIndex1();
                        break;
                    case 2:
                        out[i] = vaultEntry.getIndex2();
                        break;
                    case 3:
                        out[i] = vaultEntry.getIndex3();
                        break;
                    case 4:
                        out[i] = vaultEntry.getIndex4();
                        break;
                }
        }
        return out;
    }

    @Override
    public String importSecret(
            String groupName,
            Date validFrom,
            Date validTo,
            SecretContent secret,
            IProperties properties,
            String[] index)
            throws MException {

        try (Scope scope =
                ITracer.get()
                        .enter(
                                "importSecret",
                                "group",
                                groupName,
                                "validFrom",
                                validFrom,
                                "validTo",
                                validTo,
                                "properties",
                                properties,
                                "index",
                                index)) {

            if (validFrom == null) validFrom = new Date();
            if (validTo == null) validTo = END_OF_DAYS;

            // get group
            VaultGroup group = getGroup(groupName);

            // check write access
            List<String> acl = group.getWriteAcl();
            if (!AccessUtil.isPermitted(acl, VaultGroup.class, Ace.UPDATE, group.getName()))
                throw new AccessDeniedException("Write access to group denied", groupName);

            if (secret == null || secret.getContent() == null || secret.getContent().isNull())
                throw new MException("Secret is null");
            if (group.getMaxImportLength() > 0
                    && secret.getContent().length() > group.getMaxImportLength())
                throw new MException("Secret out of bounds", group.getMaxImportLength());

            // create entries by targets
            String secretId = UUID.randomUUID().toString();
            log().d("import secret", groupName, secretId);

            // -- cache entries to save. Save if everything target was ok
            LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
            processGroupTargets(group, properties, secretId, secret, entriesToSave);

            updateIndexes(entriesToSave, index, properties);

            // save entries
            saveEntries(groupName, entriesToSave, validFrom, validTo);

            scope.span().setTag("secretId", secretId);
            return secretId;
        }
    }

    @Override
    public void importUpdate(
            String secretId,
            Date validFrom,
            Date validTo,
            SecretContent secret,
            IProperties properties,
            String[] index)
            throws MException {

        try (Scope scope =
                ITracer.get()
                        .enter(
                                "importUpdate",
                                "secretId",
                                secretId,
                                "validFrom",
                                validFrom,
                                "validTo",
                                validTo,
                                "properties",
                                properties,
                                "index",
                                index)) {

            if (validFrom == null) validFrom = new Date();
            if (validTo == null) validTo = END_OF_DAYS;

            // get group
            String groupName = findGroupNameForSecretId(secretId);
            VaultGroup group = getGroup(groupName);

            if (properties == null) properties = new MProperties();

            List<VaultEntry> secrets = getSecrets(secretId);
            if (secrets.size() > 0) {
                for (Map.Entry<String, Object> entry : secrets.get(0).getProperties().entrySet())
                    if (!properties.containsKey(entry.getKey()))
                        properties.put(entry.getKey(), entry.getValue());
                index = fillIndex(index, secrets.get(0));
            }
            System.out.println("Index: " + Arrays.toString(index) + " " + properties);

            // check write access
            List<String> acl = group.getWriteAcl();
            if (!AccessUtil.isPermitted(acl, VaultGroup.class, Ace.UPDATE, group.getName()))
                throw new AccessDeniedException("Write access to group denied", groupName);

            if (!group.isAllowUpdate())
                throw new AccessDeniedException("The group dos not allow updates", groupName);

            if (secret == null || secret.getContent() == null || secret.getContent().isNull())
                throw new MException("Secret is null");
            if (group.getMaxImportLength() > 0
                    && secret.getContent().length() > group.getMaxImportLength())
                throw new MException("Secret out of bounds", group.getMaxImportLength());

            log().d("import update", groupName, secretId);

            // -- cache entries to save. Save if everything target was ok
            LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
            processGroupTargets(group, properties, secretId, secret, entriesToSave);

            updateEntriesValidTo(secretId, validFrom);

            updateIndexes(entriesToSave, index, properties);

            // save entries
            saveEntries(groupName, entriesToSave, validFrom, validTo);
        }
    }

    @Override
    public void deleteSecret(String secretId) throws MException {

        try (Scope scope = ITracer.get().enter("deleteSecret", "secretId", secretId)) {
            // get group
            String groupName = findGroupNameForSecretId(secretId);
            VaultGroup group = getGroup(groupName);

            // check write access
            List<String> acl = group.getWriteAcl();
            if (!AccessUtil.isPermitted(acl, VaultGroup.class, Ace.UPDATE, group.getName()))
                throw new AccessDeniedException("Write access to group denied", groupName);

            log().d("delete secret", groupName, secretId);

            //		MorphiaIterator<VaultEntry, VaultEntry> res =
            // StaticAccess.moManager.getManager().createQuery(VaultEntry.class).field("secretId").equal(secretId).fetch();
            DbCollection<VaultEntry> res =
                    StaticAccess.db
                            .getManager()
                            .getByQualification(
                                    Db.query(VaultEntry.class).eq("secretid", secretId));
            for (VaultEntry entry : res) {
                VaultArchive archive = new VaultArchive(entry);
                StaticAccess.db.getManager().inject(archive).save();
                entry.delete();
            }
            res.close();
        }
    }

    @Override
    public void undeleteSecret(String secretId) throws MException {

        try (Scope scope = ITracer.get().enter("undeleteSecret", "secretId", secretId)) {
            //		List<VaultArchive> res =
            // StaticAccess.moManager.getManager().createQuery(VaultArchive.class).field("secretId").equal(secretId).limit(1).asList();

            VaultArchive res =
                    StaticAccess.db
                            .getManager()
                            .getObjectByQualification(
                                    Db.query(VaultArchive.class).eq("secretid", secretId));
            if (res == null) throw new NotFoundException("secretId not found", secretId);
            String groupName = res.getGroup();
            VaultGroup group = getGroup(groupName);

            // check write access
            List<String> acl = group.getWriteAcl();
            if (!AccessUtil.isPermitted(acl, VaultGroup.class, Ace.UPDATE, group.getName()))
                throw new AccessDeniedException("Write access to group denied", groupName);

            log().d("undelete secret", groupName, secretId);

            //		MorphiaIterator<VaultArchive, VaultArchive> res2 =
            // StaticAccess.moManager.getManager().createQuery(VaultArchive.class).field("secretId").equal(secretId).fetch();
            DbCollection<VaultArchive> res2 =
                    StaticAccess.db
                            .getManager()
                            .getByQualification(
                                    Db.query(VaultArchive.class).eq("secretid", secretId));
            for (VaultEntry archive : res2) {
                VaultEntry entry = new VaultEntry(archive);
                StaticAccess.db.getManager().inject(entry).save();
                archive.delete();
            }
            res2.close();
        }
    }

    @Override
    public VaultEntry getSecret(String secretId, String targetName) throws NotFoundException {

        try (Scope scope =
                ITracer.get().enter("getSecret", "secretId", secretId, "targetName", targetName)) {
            VaultTarget target = getTarget(targetName);
            // check read access
            List<String> acl = target.getReadAcl();
            if (!AccessUtil.isPermitted(acl, VaultTarget.class, Ace.READ, target.getName()))
                throw new AccessDeniedException("Read access to target denied", targetName);

            //		VaultEntry obj =
            // StaticAccess.moManager.getManager().createQuery(VaultEntry.class).field("secretId").equal(secretId).field("target").equal(targetName).get();
            try {
                VaultEntry obj =
                        StaticAccess.db
                                .getManager()
                                .getObjectByQualification(
                                        Db.query(VaultEntry.class)
                                                .eq("secretid", secretId)
                                                .eq("target", targetName));
                if (obj == null) throw new NotFoundException("secret not found", secretId, target);
                return obj;
            } catch (MException e) {
                throw new NotFoundException(secretId, target, e);
            }
        }
    }

    @Override
    public List<VaultEntry> getSecrets(String secretId) throws MException {

        try (Scope scope = ITracer.get().enter("getSecrets", "secretId", secretId)) {
            // check read access
            Date now = new Date();
            AQuery<VaultEntry> query =
                    Db.query(VaultEntry.class).le("validfrom", now).gt("validto", now);
            query.eq("secretid", secretId);

            LinkedList<VaultEntry> res = new LinkedList<>();
            for (VaultEntry entry : StaticAccess.db.getManager().getByQualification(query)) {
                String targetName = entry.getTarget();
                VaultTarget target = getTarget(targetName);
                List<String> acl = target.getReadAcl();
                if (AccessUtil.isPermitted(acl, VaultTarget.class, Ace.READ, target.getName()))
                    res.add(entry);
            }
            return res;
        }
    }

    public void saveEntries(
            String groupName, LinkedList<VaultEntry> entriesToSave, Date validFrom, Date validTo) {
        for (VaultEntry entry : entriesToSave) {
            try {
                entry.setValidFrom(validFrom);
                entry.setValidTo(validTo);
                entry.save();
            } catch (Throwable t) {
                log().w(groupName, entry, t);
            }
        }
    }

    private void processGroupTargets(
            VaultGroup group,
            IProperties properties,
            String secretId,
            SecretContent secret,
            LinkedList<VaultEntry> entriesToSave)
            throws MException {

        for (String targetName : group.getTargets()) {
            processTarget(group, targetName, properties, secretId, secret, entriesToSave);
        }

        if (entriesToSave.isEmpty()) return;

        VaultGroup ever = getMustHaveGroup(group.getName());
        if (ever != null) {
            for (String targetName : ever.getTargets()) {
                processTarget(group, targetName, properties, secretId, secret, entriesToSave);
            }
        }
    }

    public void processTarget(
            VaultGroup group,
            String targetName,
            IProperties properties,
            String secretId,
            SecretContent secret,
            LinkedList<VaultEntry> entriesToSave)
            throws NotFoundException, MException {
        VaultTarget target = getTarget(targetName);
        if (checkProcessConditions(group, properties, target)) {
            VaultEntry entry = processTarget(group, properties, target, secretId, secret);
            if (entry != null) entriesToSave.add(entry);
        }
    }

    private VaultGroup getMustHaveGroup(String groupName) throws NotFoundException {
        //		List<VaultGroup> res =
        // StaticAccess.moManager.getManager().createQuery(VaultGroup.class).field("name").equal(DEFAULT_GROUP_NAME).asList();
        //		if (res.size() < 1) return null;
        //		if (res.size() > 1) log().w("Not unique group name",DEFAULT_GROUP_NAME);
        //		VaultGroup group = res.get(0);
        //		if (!group.isEnabled()) return null;
        //		return group;
        try {
            VaultGroup group =
                    StaticAccess.db
                            .getManager()
                            .getObjectByQualification(
                                    Db.query(VaultGroup.class)
                                            .eq("name", CFG_DEFAULT_GROUP_NAME.value()));
            if (group == null) {
                log().w("Unique group not found", CFG_DEFAULT_GROUP_NAME.value());
                return null;
            }
            if (!group.isEnabled()) return null;
            return group;
        } catch (MException e) {
            throw new NotFoundException(CFG_DEFAULT_GROUP_NAME.value(), e);
        }
    }

    public VaultEntry processTarget(
            VaultGroup group,
            IProperties properties,
            VaultTarget target,
            String secretId,
            SecretContent secret)
            throws MException {
        try {
            String processorName = target.getProcessorName();
            TargetProcessor processor = getProcessor(processorName);
            WritableEntry entry = new WritableEntry();
            // copy properties into meta
            for (String mapping :
                    target.getProcessorConfig()
                            .getString("properties2meta.mapping", "")
                            .split(",")) {
                String from = mapping;
                String to = mapping;
                int p = mapping.indexOf('=');
                if (p > 0) {
                    from = mapping.substring(p + 1);
                    to = mapping.substring(0, p);
                }
                entry.getMeta().put(to, properties.get(from));
            }
            processor.process(properties, target.getProcessorConfig(), secret, entry);

            entry.setGroup(group.getName());
            entry.setTarget(target.getName());
            entry.setSecretId(secretId);

            return StaticAccess.db.getManager().inject(new VaultEntry(entry));
        } catch (Throwable t) {
            log().e("error executing target", group, target, secretId, t.toString());
            throw t;
        }
    }

    public TargetProcessor getProcessor(String processorName) throws NotFoundException {
        return MOsgi.getService(TargetProcessor.class, "(name=" + processorName + ")");
    }

    public boolean checkProcessConditions(
            VaultGroup group, IProperties properties, VaultTarget target) throws NotFoundException {
        if (!target.isEnabled()) return false;
        String conditions = target.getConditionNames();
        if (conditions == null) return false;
        String[] parts = conditions.split(",");
        for (String part : parts) {
            IReadProperties config = target.getConditionConfig(part);
            TargetCondition c = getConditionCheck(config.getString("service", part));
            if (!c.check(properties, config)) return false;
        }
        return true;
    }

    public TargetCondition getConditionCheck(String conditionName) throws NotFoundException {
        return MOsgi.getService(TargetCondition.class, "(name=" + conditionName + ")");
    }

    public SecretGenerator getGenerator(String generatorName) throws NotFoundException {
        return MOsgi.getService(SecretGenerator.class, "(name=" + generatorName + ")");
    }

    public VaultGroup getGroup(String name) throws NotFoundException {
        //		List<VaultGroup> res =
        // StaticAccess.moManager.getManager().createQuery(VaultGroup.class).field("name").equal(name).asList();
        //		if (res.size() < 1) throw new NotFoundException("Group not exists",name);
        //		if (res.size() > 1) log().w("Not unique group name",name);
        //		VaultGroup group = res.get(0);
        try {
            VaultGroup group =
                    StaticAccess.db
                            .getManager()
                            .getObjectByQualification(Db.query(VaultGroup.class).eq("name", name));
            if (!group.isEnabled()) throw new NotFoundException("Group is disabled", name);
            return group;
        } catch (MException e) {
            throw new NotFoundException(name, e);
        }
    }

    public VaultTarget getTarget(String name) throws NotFoundException {
        //		List<VaultTarget> res =
        // StaticAccess.moManager.getManager().createQuery(VaultTarget.class).field("name").equal(name).asList();
        //		if (res.size() < 1) throw new NotFoundException("Target not exists",name);
        //		if (res.size() > 1) log().w("Not unique target name",name);
        //		return res.get(0);
        try {
            VaultTarget out =
                    StaticAccess.db
                            .getManager()
                            .getObjectByQualification(Db.query(VaultTarget.class).eq("name", name));
            if (out == null) throw new NotFoundException("Target not exists", name);
            return out;
        } catch (MException e) {
            throw new NotFoundException(name, e);
        }
    }

    private String findGroupNameForSecretId(String secretId) throws NotFoundException {
        //		@SuppressWarnings("deprecation")
        //		List<VaultEntry> res =
        // StaticAccess.moManager.getManager().createQuery(VaultEntry.class).field("secretId").equal(secretId).limit(1).asList();
        //		if (res.size() == 0)
        //			throw new NotFoundException("secretId not found",secretId);
        //		return res.get(0).getGroup();
        try {
            VaultEntry out =
                    StaticAccess.db
                            .getManager()
                            .getObjectByQualification(
                                    Db.query(VaultEntry.class)
                                            .eq("secretid", secretId)
                                            .ne("group", CFG_DEFAULT_GROUP_NAME.value()));
            if (out == null) throw new NotFoundException("secretId not found", secretId);
            return out.getGroup();
        } catch (MException e) {
            throw new NotFoundException(secretId, e);
        }
    }

    private void updateEntriesValidTo(String secretId, Date validTo) throws MException {
        Date now = new Date();
        XdbService manager = StaticAccess.db.getManager();
        //		MorphiaIterator<VaultEntry, VaultEntry> res = manager.createQuery(VaultEntry.class)
        //			.field("secretId").equal(secretId)
        //			.field("validFrom").lessThanOrEq(now)
        //			.field("validTo").greaterThan(now)
        //			.fetch();
        DbCollection<VaultEntry> res =
                manager.getByQualification(
                        Db.query(VaultEntry.class)
                                .eq("secretid", secretId)
                                .le("validfrom", now)
                                .gt("validto", now));

        for (VaultEntry entry : res) {
            log().t("Update validTo", entry.getId(), entry.getValidTo(), validTo);
            entry.setValidTo(validTo);
            entry.save();
        }
        res.close();
    }

    @Override
    public String importSecret(
            String groupName,
            Date validFrom,
            Date validTo,
            String secret,
            IProperties properties,
            String[] index)
            throws MException {

        VaultGroup group = getGroup(groupName);
        if (properties == null) properties = new MProperties();

        SecretContent sec = null;
        if (PemUtil.isPemBlock(secret)) {
            // it's encoded
            CryptApi crypta = M.l(CryptApi.class);
            PemBlockList encoded = new PemBlockList(secret);

            CherryVaultProcessContext context = new CherryVaultProcessContext(properties);
            crypta.processPemBlocks(context, encoded);

            if (context.getLastSecret() == null) throw new MException("can't decode secret");

            sec = new SecretContent(context.getLastSecret(), new MProperties());
        } else {
            if (!group.isAllowUnencrypted())
                throw new AccessDeniedException("Need to encrypt secrets", groupName);
            sec = new SecretContent(new SecureString(secret), new MProperties());
        }

        return importSecret(groupName, validFrom, validTo, sec, properties, index);
    }

    @Override
    public void importUpdate(
            String secretId,
            Date validFrom,
            Date validTo,
            String secret,
            IProperties properties,
            String[] index)
            throws MException {

        String groupName = findGroupNameForSecretId(secretId);
        VaultGroup group = getGroup(groupName);
        if (properties == null) properties = new MProperties();

        SecretContent sec = null;
        if (PemUtil.isPemBlock(secret)) {
            // it's encoded
            CryptApi crypta = M.l(CryptApi.class);
            PemBlockList encoded = new PemBlockList(secret);

            CherryVaultProcessContext context = new CherryVaultProcessContext(properties);

            crypta.processPemBlocks(context, encoded);

            if (context.getLastSecret() == null) throw new MException("can't decode secret");

            sec = new SecretContent(context.getLastSecret(), new MProperties());

        } else {
            if (!group.isAllowUnencrypted())
                throw new AccessDeniedException("Need to encrypt secrets", groupName);
            sec = new SecretContent(new SecureString(secret), new MProperties());
        }

        importUpdate(secretId, validFrom, validTo, sec, properties, index);
    }

    @Override
    public void indexUpdate(String secretId, String[] index) throws MException {

        try (Scope scope =
                ITracer.get().enter("indexUpdate", "secretId", secretId, "index", index)) {

            Date now = new Date();
            XdbService manager = StaticAccess.db.getManager();
            DbCollection<VaultEntry> res =
                    manager.getByQualification(
                            Db.query(VaultEntry.class)
                                    .eq("secretid", secretId)
                                    .le("validfrom", now)
                                    .gt("validto", now));
            LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
            for (VaultEntry item : res) entriesToSave.add(item);
            res.close();

            updateIndexes(entriesToSave, index, null);

            for (VaultEntry entry : entriesToSave) {
                try {
                    entry.save();
                } catch (Throwable t) {
                    log().w(entry, t);
                }
            }
        }
    }

    @Override
    public List<VaultEntry> search(
            String group, String target, String[] index, int size, boolean all) throws MException {
        try (Scope scope =
                ITracer.get()
                        .enter(
                                "search", "group", group, "target", target, "index", index, "size",
                                size, "all", all)) {
            return search(group, target, index, size, all, true);
        }
    }

    public List<VaultEntry> search(
            String group, String target, String[] index, int size, boolean all, boolean secure)
            throws MException {
        if (secure && (index == null || index.length == 0)) return new EmptyList<VaultEntry>();

        Date now = new Date();
        AQuery<VaultEntry> query = Db.query(VaultEntry.class);
        if (!all) {
            query.le("validfrom", now).gt("validto", now);
        }
        if (group != null) query.eq("group", group);

        if (target != null) query.eq("target", target);

        boolean found = false;
        if (index != null)
            for (int i = 0; i < index.length; i++) {
                if (MString.isEmpty(index[i]) || i > 4) continue;
                found = true;
                query.eq("index" + i, index[i]);
            }
        if (secure && !found) return new EmptyList<VaultEntry>();

        XdbService manager = StaticAccess.db.getManager();
        DbCollection<VaultEntry> res = manager.getByQualification(query);

        LinkedList<VaultEntry> out = new LinkedList<>();
        for (VaultEntry item : res) {
            out.add(item);
            if (out.size() >= size) break;
        }
        res.close();

        return out;
    }

    @Override
    public String testGroup(String groupName, boolean execute, IProperties properties) {

        try (Scope scope =
                ITracer.get()
                        .enter(
                                "testGroup",
                                "groupName",
                                groupName,
                                "execute",
                                execute,
                                "properties",
                                properties)) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(os);
            try {
                // get group
                out.println("Group Name: " + groupName);
                VaultGroup group = getGroup(groupName);
                out.println("Group: " + group);
                List<String> acl = group.getWriteAcl();
                if (!AccessUtil.isPermitted(acl, VaultGroup.class, Ace.UPDATE, group.getName()))
                    out.println("Access Denied");
                else out.println("Access Granted");

                String generatorName = group.getSecretGeneratorName();
                out.println("Generator Name: " + generatorName);

                SecretContent secret = null;
                UUID secretId = UUID.randomUUID();
                if (generatorName != null) {
                    SecretGenerator generator = getGenerator(generatorName);
                    out.println("---------------------------");
                    out.println("Generator: " + generator);
                    out.println("---------------------------");
                    out.println(group.getSecretGeneratorConfig());
                    generator.test(out, group, properties);
                    if (execute) {
                        out.println(">>> Execute Generator");
                        secret = generator.generateSecret(group, properties);
                        out.println("=== Result:" + secret.getContent().value());
                        out.println("Properties: " + secret.getProperties());
                        out.println("<<< End Generator");
                    }
                }

                for (String targetName : group.getTargets()) {
                    out.println();
                    out.println("---------------------------");
                    out.println("Target: " + targetName);
                    out.println("---------------------------");
                    VaultTarget target = getTarget(targetName);
                    out.println("DB: " + target);
                    String processorName = target.getProcessorName();
                    out.println("Processor: " + processorName);
                    TargetProcessor processor = getProcessor(processorName);
                    out.println("Instance: " + processor);

                    out.println(target.getProcessorConfig());
                    boolean cond = checkProcessConditions(group, properties, target);
                    out.println("Condition: " + cond);
                    processor.test(out, properties, target.getProcessorConfig());
                    if (cond && execute) {
                        out.println(">>> Execute Target " + targetName);
                        WritableEntry entry = new WritableEntry();
                        entry.setSecretId(secretId.toString());
                        entry.setTarget(targetName);
                        entry.setGroup(groupName);
                        processor.process(properties, target.getProcessorConfig(), secret, entry);
                        out.println("=== Result:");
                        out.println("Secret:");
                        out.println(entry.getSecret());
                        out.println("Meta: " + entry.getMeta());
                        out.println("<<< End Target");
                    }
                }

                VaultGroup ever = getMustHaveGroup(group.getName());
                if (ever != null) {
                    out.println();
                    out.println("*****************************);");
                    out.println("Must Have Group: " + ever);
                    for (String targetName : ever.getTargets()) {
                        out.println();
                        out.println("---------------------------");
                        out.println("Target: " + targetName);
                        out.println("---------------------------");
                        VaultTarget target = getTarget(targetName);
                        out.println("DB: " + target);
                        String processorName = target.getProcessorName();
                        out.println("Processor: " + processorName);
                        TargetProcessor processor = getProcessor(processorName);
                        out.println("Instance: " + processor);

                        out.println(target.getProcessorConfig());
                        boolean cond = checkProcessConditions(group, properties, target);
                        out.println("Condition: " + cond);
                        processor.test(out, properties, target.getProcessorConfig());
                        if (cond && execute) {
                            out.println(">>> Execute Default Target " + targetName);
                            WritableEntry entry = new WritableEntry();
                            entry.setSecretId(secretId.toString());
                            entry.setTarget(targetName);
                            entry.setGroup(groupName);
                            processor.process(
                                    properties, target.getProcessorConfig(), secret, entry);
                            out.println("=== Result:");
                            out.println("Secret:");
                            out.println(entry.getSecret());
                            out.println("Meta: " + entry.getMeta());
                            out.println("<<< End Default Target");
                        }
                    }
                }
                out.append("############################################\n");

            } catch (Throwable t) {
                out.println(t.toString());
            }
            return new String(os.toByteArray());
        }
    }

    @Override
    public XdbService getManager() {
        return StaticAccess.db.getManager();
    }

    @Override
    public void cleanup(String group) {
        try (Scope scope = ITracer.get().enter("cleanup", "group", group)) {

            Date now = new Date();
            AQuery<VaultEntry> query = Db.query(VaultEntry.class).le("validto", now);
            try {
                for (VaultEntry entry : StaticAccess.db.getManager().getByQualification(query)) {
                    try {
                        log().i("cleanup", entry.getId());
                        entry.delete();
                    } catch (Throwable t) {
                        log().e(entry, t);
                    }
                }
            } catch (Throwable t) {
                log().e(t);
            }
        }
    }
}

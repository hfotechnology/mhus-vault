package de.mhus.cherry.vault.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.karaf.shell.api.action.lifecycle.Manager;
import org.mongodb.morphia.query.MorphiaIterator;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.CherryVaultApi;
import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.ifc.SecretGenerator;
import de.mhus.cherry.vault.api.ifc.TargetCondition;
import de.mhus.cherry.vault.api.ifc.TargetProcessor;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.cherry.vault.api.model.VaultTarget;
import de.mhus.cherry.vault.api.model.WritableEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MString;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.UsageException;
import de.mhus.lib.karaf.MOsgi;
import de.mhus.lib.mongo.MoManager;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AaaUtil;
import de.mhus.osgi.sop.api.aaa.AccessApi;

@Component(immediate=true)
public class VaultApiImpl extends MLog implements CherryVaultApi {

	private static final Date END_OF_DAYS = new Date(3000-1900,1,1);

	@Override
	public String createSecret(String groupName, Date validFrom, Date validTo, IProperties properties) throws MException {
		
		if (validFrom == null) validFrom = new Date();
		if (validTo == null) validTo = END_OF_DAYS;
		
		// get group
		VaultGroup group = getGroup(groupName);
		
		// check write access
		AccessApi aaa = MApi.lookup(AccessApi.class);
		List<String> acl = group.getWriteAcl();
		if (!AaaUtil.hasAccess(aaa.getCurrentOrGuest(), acl))
			throw new AccessDeniedException("Write access to group denied",groupName);
		
		// get and execute secret generation
		String generatorName = group.getSecretGeneratorName();
		if (MString.isEmpty(generatorName)) throw new UsageException("Group can't generate secrets",groupName);
		
		SecretGenerator generator = getGenerator(generatorName);
		
		SecretContent secret = generator.generateSecret(group, properties);
		if (secret == null) throw new MException("Secret is null");
		
		// create entries by targets
		String secretId = UUID.randomUUID().toString();
		
		// -- cache entries to save. Save if everything target was ok
		LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
		processGroupTargets(group, properties, secretId, secret, entriesToSave);
		
		if (entriesToSave.size() == 0) return null;
		
		// save entries
		saveEntries(groupName, entriesToSave, validFrom, validTo);
		
		return secretId;
	}

	@Override
	public void createUpdate(String secretId, Date validFrom, Date validTo, IProperties properties) throws MException {
		
		if (validFrom == null) validFrom = new Date();
		if (validTo == null) validTo = END_OF_DAYS;

		// get group
		String groupName = findGroupNameForSecretId(secretId);
		VaultGroup group = getGroup(groupName);
		
		// check write access
		AccessApi aaa = MApi.lookup(AccessApi.class);
		List<String> acl = group.getWriteAcl();
		if (!AaaUtil.hasAccess(aaa.getCurrentOrGuest(), acl))
			throw new AccessDeniedException("Write access to group denied",groupName);

		if (!group.isAllowUpdate()) throw new AccessDeniedException("The group dos not allow updates",groupName);
		
		// get and execute secret generation
		String generatorName = group.getSecretGeneratorName();
		if (MString.isEmpty(generatorName)) throw new UsageException("Group can't generate secrets",groupName);
		
		SecretGenerator generator = getGenerator(generatorName);
		
		SecretContent secret = generator.generateSecret(group, properties);
		if (secret == null) throw new MException("Secret is null");
		
		// -- cache entries to save. Save if everything target was ok
		LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
		processGroupTargets(group, properties, secretId, secret, entriesToSave);
		
		updateEntriesValidTo(secretId, validFrom);
		
		// save entries
		saveEntries(groupName, entriesToSave, validFrom, validTo);
		
	}

	@Override
	public String importSecret(String groupName, Date validFrom, Date validTo, SecretContent secret, IProperties properties) throws MException {
		
		if (validFrom == null) validFrom = new Date();
		if (validTo == null) validTo = END_OF_DAYS;

		// get group
		VaultGroup group = getGroup(groupName);

		// check write access
		AccessApi aaa = MApi.lookup(AccessApi.class);
		List<String> acl = group.getWriteAcl();
		if (!AaaUtil.hasAccess(aaa.getCurrentOrGuest(), acl))
			throw new AccessDeniedException("Write access to group denied",groupName);
		
		if (secret == null) throw new MException("Secret is null");
		
		// create entries by targets
		String secretId = UUID.randomUUID().toString();
		
		// -- cache entries to save. Save if everything target was ok
		LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
		processGroupTargets(group, properties, secretId, secret, entriesToSave);
		
		// save entries
		saveEntries(groupName, entriesToSave, validFrom, validTo);
		
		return secretId;
	}

	@Override
	public void importUpdate(String secretId, Date validFrom, Date validTo, SecretContent secret, IProperties properties) throws MException {
		
		if (validFrom == null) validFrom = new Date();
		if (validTo == null) validTo = END_OF_DAYS;

		// get group
		String groupName = findGroupNameForSecretId(secretId);
		VaultGroup group = getGroup(groupName);
		
		// check write access
		AccessApi aaa = MApi.lookup(AccessApi.class);
		List<String> acl = group.getWriteAcl();
		if (!AaaUtil.hasAccess(aaa.getCurrentOrGuest(), acl))
			throw new AccessDeniedException("Write access to group denied",groupName);

		if (!group.isAllowUpdate()) throw new AccessDeniedException("The group dos not allow updates",groupName);
		
		if (secret == null) throw new MException("Secret is null");
		
		// -- cache entries to save. Save if everything target was ok
		LinkedList<VaultEntry> entriesToSave = new LinkedList<>();
		processGroupTargets(group, properties, secretId, secret, entriesToSave);
		
		updateEntriesValidTo(secretId, validFrom);
		
		// save entries
		saveEntries(groupName, entriesToSave, validFrom, validTo);
		
	}

	@Override
	public void rollbackSecret(String secretId, Date creationDate) throws MException {
		
		// get group
		String groupName = findGroupNameForSecretId(secretId);
		VaultGroup group = getGroup(groupName);

		// check write access
		AccessApi aaa = MApi.lookup(AccessApi.class);
		List<String> acl = group.getWriteAcl();
		if (!AaaUtil.hasAccess(aaa.getCurrentOrGuest(), acl))
			throw new AccessDeniedException("Write access to group denied",groupName);

		// TODO Auto-generated method stub
	}

	@Override
	public VaultEntry getSecret(String secretId, String targetName) throws NotFoundException {
		
		VaultTarget target = getTarget(targetName);
		// check read access
		AccessApi aaa = MApi.lookup(AccessApi.class);
		List<String> acl = target.getReadAcl();
		if (!AaaUtil.hasAccess(aaa.getCurrentOrGuest(), acl))
			throw new AccessDeniedException("Read access to target denied",targetName);

		
		VaultEntry obj = MoVaultManager.instance.getManager().createQuery(VaultEntry.class).field("secretId").equal(secretId).field("target").equal(targetName).get();
		
		return obj;
	}

	private void saveEntries(String groupName, LinkedList<VaultEntry> entriesToSave, Date validFrom, Date validTo) {
		MoManager manager = MoVaultManager.instance.getManager();
		for (VaultEntry entry : entriesToSave) {
			try {
				entry.setValidFrom(validFrom);
				entry.setValidTo(validTo);
				manager.save(entry);
			} catch (Throwable t) {
				log().w(groupName,entry,t);
			}
		}
	}

	private void processGroupTargets(VaultGroup group, IProperties properties, String secretId, SecretContent secret,
	        LinkedList<VaultEntry> entriesToSave) throws MException {
		
		for (String targetName : group.getTargets()) {
			VaultTarget target = getTarget(targetName);
			if (checkProcessConditions(group, properties, target)) {
				VaultEntry entry = processTarget(group, properties, target, secretId, secret);
				if (entry != null)
					entriesToSave.add(entry);
			}
		}
		
		if (entriesToSave.isEmpty()) return;
		
		VaultGroup ever = getMustHaveGroup(group.getName());
		if (ever != null) {
			for (String targetName : group.getTargets()) {
				VaultTarget target = getTarget(targetName);
				if (checkProcessConditions(group, properties, target)) {
					VaultEntry entry = processTarget(ever, properties, target, secretId, secret);
					if (entry != null)
						entriesToSave.add(entry);
				}
			}
		}

		
	}

	private VaultGroup getMustHaveGroup(String groupName) {
		// TODO Auto-generated method stub
		return null;
	}

	private VaultEntry processTarget(VaultGroup group, IProperties properties, VaultTarget target, String secretId, SecretContent secret) throws MException {
		String processorName = target.getProcessorName();
		TargetProcessor processor = getProcessor(processorName);
		
		WritableEntry entry = new WritableEntry();
		// copy properties into meta
		for (String mapping : target.getProcessorConfig().getString("properties2meta.mapping", "").split(",")) {
			String from = mapping;
			String to = mapping;
			int p = mapping.indexOf('=');
			if (p > 0) {
				from = mapping.substring(p+1);
				to = mapping.substring(0, p);
			}
			entry.getMeta().put(to, properties.get(from));
		}
		processor.process(properties, target.getProcessorConfig(), secret, entry);
		
		entry.setGroup(group.getName());
		entry.setTarget(target.getName());
		entry.setSecretId(secretId);
		
		return new VaultEntry(entry);
	}

	public TargetProcessor getProcessor(String processorName) throws NotFoundException {
		return MOsgi.getService(TargetProcessor.class, "(name=" + processorName + ")");
	}

	public boolean checkProcessConditions(VaultGroup group, IProperties properties, VaultTarget target) throws NotFoundException {
		String conditions = target.getConditionNames();
		if (conditions == null) return false;
		String[] parts = conditions.split(",");
		for (String part : parts) {
			IReadProperties config = target.getConditionConfig(part);
			TargetCondition c = getConditionCheck(config.getString("service", part));
			if (!c.check(properties,config)) return false;
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
		List<VaultGroup> res = MoVaultManager.instance.getManager().createQuery(VaultGroup.class).field("name").equal(name).asList();
		if (res.size() < 1) throw new NotFoundException("Group not exists",name);
		if (res.size() > 1) log().w("Not unique group name",name);
		return res.get(0);
	}
	
	public VaultTarget getTarget(String name) throws NotFoundException {
		List<VaultTarget> res = MoVaultManager.instance.getManager().createQuery(VaultTarget.class).field("name").equal(name).asList();
		if (res.size() < 1) throw new NotFoundException("Target not exists",name);
		if (res.size() > 1) log().w("Not unique target name",name);
		return res.get(0);
	}

	private String findGroupNameForSecretId(String secretId) throws NotFoundException {
		List<VaultEntry> res = MoVaultManager.instance.getManager().createQuery(VaultEntry.class).field("secretId").equal(secretId).limit(1).asList();
		if (res.size() == 0)
			throw new NotFoundException("secretId not found",secretId);
		return res.get(0).getGroup();
	}

	private void updateEntriesValidTo(String secretId, Date validTo) throws MException {
		Date now = new Date();
		MoManager manager = MoVaultManager.instance.getManager();
		MorphiaIterator<VaultEntry, VaultEntry> res = manager.createQuery(VaultEntry.class)
			.field("secretId").equal(secretId)
			.field("validFrom").lessThanOrEq(now)
			.field("validTo").greaterThan(now)
			.fetch();
		
		for (VaultEntry entry : res) {
			log().t("Update validTo",entry.getObjectId(),entry.getValidTo(),validTo);
			entry.setValidTo(validTo);
			entry.save();
		}
		
	}


	
}

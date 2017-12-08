package de.mhus.cherry.vault.api;

import java.util.Collection;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.errors.MException;

public interface CherryVaultApi {

	String createEntry(IProperties meta, String[] groups, String secret) throws MException;

	void updateEntry(String id, String[] groups, String secret) throws MException;
	
	void deleteEntry(String id) throws MException;
	
	void removeEntryTargets(String id, String[] targets) throws MException;
	
	CherryVaultEntry getEntry(String id);
	
	Collection<CherryVaultEntry> searchEntries(String search, int page);
	
	Collection<CherryVaultEntry> getEntry(String target, String key, String value, int page);
	
	Collection<CherryVaultGroup> getGroups();
	
	Collection<CherryVaultTarget> getTargets();
	
	Collection<CherryVaultTarget> getTargets(String group);
		
}

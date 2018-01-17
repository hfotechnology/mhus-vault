package de.mhus.cherry.vault.api.model;

import java.util.Date;

public class VaultArchive extends VaultEntry {

	private Date entryCreationDate;
	
	public VaultArchive() {}
	
	public VaultArchive(VaultEntry entry) {
		super(entry);
		entryCreationDate = entry.getCreationDate();
	}

	public Date getEntryCreationDate() {
		return entryCreationDate;
	}

}

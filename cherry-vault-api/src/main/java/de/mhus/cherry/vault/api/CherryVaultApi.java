package de.mhus.cherry.vault.api;

import java.util.Date;

import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.errors.MException;

public interface CherryVaultApi {

	/**
	 * Creates a new secret using the group secret generator and process the secret
	 * by the target processors.
	 * 
	 * @param groupName Processing for this group of targets
	 * @param properties Processing configuration
	 * @return The new created secretId
	 * @throws MException
	 */
	String createSecret(String groupName, Date validFrom, Date validTo, IProperties properties) throws MException;

	/**
	 * Creates a new secret for an existing key (e.g. create new password).
	 * 
	 * @param secretId Secret to be updated
	 * @param properties Processing configuration
	 * @throws MException
	 */
	void createUpdate(String secretId, Date validFrom, Date validTo, IProperties properties) throws MException;
			
	/**
	 * Import an existing secret as new secret.
	 * @param groupName
	 * @param secret 
	 * @param properties
	 * @return
	 * @throws MException
	 */
	String importSecret(String groupName, Date validFrom, Date validTo, SecretContent secret, IProperties properties) throws MException;

	void importUpdate(String secretId, Date validFrom, Date validTo, SecretContent secret, IProperties properties) throws MException;
	
	void rollbackSecret(String secretId, Date creationDate) throws MException;
	
	VaultEntry getSecret(String secretId, String target);
	
}

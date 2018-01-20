package de.mhus.cherry.vault.api;

import java.util.Date;

import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.util.SecureString;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;

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

	default String createSecret(String groupName) throws MException {
		return createSecret(groupName, null, null, new MProperties());
	}

	/**
	 * Creates a new secret for an existing key (e.g. create new password).
	 * 
	 * @param secretId Secret to be updated
	 * @param properties Processing configuration
	 * @throws MException
	 */
	void createUpdate(String secretId, Date validFrom, Date validTo, IProperties properties) throws MException;

	default void createUpdate(String secretId) throws MException {
		createUpdate(secretId, null, null, new MProperties());
	}

	/**
	 * Import an existing secret as new secret.
	 * @param groupName
	 * @param secret 
	 * @param properties
	 * @return
	 * @throws MException
	 */
	String importSecret(String groupName, Date validFrom, Date validTo, SecretContent secret, IProperties properties) throws MException;

	default String importSecret(String groupName, String secret) throws MException {
		return importSecret(groupName, null, null, new SecretContent(new SecureString(secret), new MProperties()), new MProperties());
	}
	
	void importUpdate(String secretId, Date validFrom, Date validTo, SecretContent secret, IProperties properties) throws MException;

	default void importUpdate(String secretId, String secret) throws MException {
		importUpdate(secretId, null, null, new SecretContent(new SecureString(secret), new MProperties()), new MProperties());
	}

	void rollbackSecret(String secretId, Date creationDate) throws MException;
	
	default void rollbackSecret(String secretId, String creationDate) throws MException {
		rollbackSecret(secretId, MDate.toDate(creationDate, null));
	}
	
	VaultEntry getSecret(String secretId, String target) throws NotFoundException;
	
}

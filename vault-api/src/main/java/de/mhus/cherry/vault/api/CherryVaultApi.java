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
package de.mhus.cherry.vault.api;

import java.util.Date;
import java.util.List;

import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.util.SecureString;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.xdb.XdbService;

public interface CherryVaultApi {

	/**
	 * Creates a new secret using the group secret generator and process the secret
	 * by the target processors.
	 * 
	 * @param groupName Processing for this group of targets
	 * @param validFrom 
	 * @param validTo 
	 * @param properties Processing configuration
	 * @param index 
	 * @return The new created secretId
	 * @throws MException
	 */
	String createSecret(String groupName, Date validFrom, Date validTo, IProperties properties, String[] index) throws MException;

	default String createSecret(String groupName, String[] index) throws MException {
		return createSecret(groupName, null, null, new MProperties(), index);
	}

	/**
	 * Creates a new secret for an existing key (e.g. create new password).
	 * 
	 * @param secretId Secret to be updated
	 * @param validFrom 
	 * @param validTo 
	 * @param properties Processing configuration
	 * @param index 
	 * @throws MException
	 */
	void createUpdate(String secretId, Date validFrom, Date validTo, IProperties properties, String[] index) throws MException;

	default void createUpdate(String secretId, String[] index) throws MException {
		createUpdate(secretId, null, null, new MProperties(), index);
	}

	/**
	 * Import an existing secret as new secret.
	 * @param groupName
	 * @param validFrom 
	 * @param validTo 
	 * @param secret 
	 * @param properties
	 * @param index 
	 * @return secretId
	 * @throws MException
	 */
	String importSecret(String groupName, Date validFrom, Date validTo, SecretContent secret, IProperties properties, String[] index) throws MException;

	/**
	 * Import an existing secret as new secret. The secret is encoded with the users private key.
	 * 
	 * @param groupName
	 * @param validFrom
	 * @param validTo
	 * @param secret
	 * @param properties
	 * @param index 
	 * @return secretId
	 * @throws MException
	 */
	String importSecret(String groupName, Date validFrom, Date validTo, String secret, IProperties properties, String[] index) throws MException;

	default String importSecret(String groupName, String secret, String[] index) throws MException {
		return importSecret(groupName, null, null, new SecretContent(new SecureString(secret), new MProperties()), new MProperties(), index);
	}
	
	void importUpdate(String secretId, Date validFrom, Date validTo, SecretContent secret, IProperties properties, String[] index) throws MException;

	/**
	 * Import and update an secret. The secret is encoded with the users private key.
	 * 
	 * @param secretId
	 * @param validFrom
	 * @param validTo
	 * @param secret
	 * @param properties
	 * @param index 
	 * @throws MException
	 */
	void importUpdate(String secretId, Date validFrom, Date validTo, String secret, IProperties properties, String[] index) throws MException;
	
	default void importUpdate(String secretId, String secret, String[] index) throws MException {
		importUpdate(secretId, null, null, new SecretContent(new SecureString(secret), new MProperties()), new MProperties(), index);
	}

	/**
	 * Update the secret index values. Set null not to change the value.
	 * 
	 * @param secretId
	 * @param index 
	 * @throws MException
	 */
	void indexUpdate(String secretId, String[] index) throws MException;
	
	void deleteSecret(String secretId) throws MException;
		
	void undeleteSecret(String secretId) throws MException;
	
	VaultEntry getSecret(String secretId, String target) throws NotFoundException;

    List<VaultEntry> getSecrets(String secretId) throws MException;
    
    List<VaultEntry> search(String group, String target, String[] index, int size, boolean all) throws MException;

    String testGroup(String groupName, boolean execute,IProperties prop);
	
    public XdbService getManager();

}

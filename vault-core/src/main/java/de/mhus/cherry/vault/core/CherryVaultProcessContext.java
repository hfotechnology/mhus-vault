/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.core;

import java.util.UUID;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MCollection;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.core.shiro.AccessUtil;
import de.mhus.lib.core.util.SecureString;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.util.SimplePemProcessContext;

public class CherryVaultProcessContext extends SimplePemProcessContext {

    private IReadProperties properties;

    public CherryVaultProcessContext(IReadProperties properties) {
        this.properties = properties;
    }

    @Override
    public PemPriv getPrivateKey(String privId) throws MException {
        PemPriv privKey = super.getPrivateKey(privId);
        if (privKey != null) return privKey;

        String privateKey = AccessUtil.getSessionAttribute("privateKey", "");
        if (!MCollection.contains(privateKey, ',', privId))
            throw new AccessDeniedException(
                    "The private key is not owned by the current user",
                    AccessUtil.getPrincipal(),
                    privId);
        de.mhus.lib.core.keychain.KeyEntry privKeyObj =
                MKeychainUtil.loadDefault().getEntry(UUID.fromString(privId));
        if (privKeyObj == null) throw new NotFoundException("Private key not found", privId);

        privKey = PemUtil.toKey(privKeyObj.getValue().value());

        addPassphrase(privId, new SecureString(properties.getString("passphrase", null)));

        return privKey;
    }
}

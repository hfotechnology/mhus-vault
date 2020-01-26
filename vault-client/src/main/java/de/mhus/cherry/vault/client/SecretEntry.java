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
package de.mhus.cherry.vault.client;

import org.codehaus.jackson.JsonNode;

import de.mhus.lib.core.crypt.pem.PemBlockList;

public class SecretEntry {

    private long creationDate;
    private String creator;
    private String group;
    private String secretId;
    private String target;
    private long validFrom;
    private long validTo;
    private String secret;

    public SecretEntry(JsonNode json) {
        creationDate = json.get("creationdate").asLong();
        creator = json.get("creator").asText();
        group = json.get("group").asText();
        secretId = json.get("secretid").asText();
        target = json.get("target").asText();
        validFrom = json.get("validfrom").asLong();
        validTo = json.get("validto").asLong();
        secret = json.get("secret").asText();
    }

    public long getCreationDate() {
        return creationDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getGroup() {
        return group;
    }

    public String getSecretId() {
        return secretId;
    }

    public String getTarget() {
        return target;
    }

    public long getValidFrom() {
        return validFrom;
    }

    public long getValidTo() {
        return validTo;
    }

    public String getSecret() {
        return secret;
    }

    public PemBlockList getSecretBlock() {
        return new PemBlockList(secret);
    }

    @Override
    public String toString() {
        return secretId + " " + target;
    }
}

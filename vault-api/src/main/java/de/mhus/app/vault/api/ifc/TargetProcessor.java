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
package de.mhus.app.vault.api.ifc;

import java.io.PrintStream;

import de.mhus.app.vault.api.model.WritableEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.cfg.CfgString;
import de.mhus.lib.errors.MException;

public interface TargetProcessor {

    static final CfgString CFG_CIPHER_DEFAULT =
            new CfgString(TargetProcessor.class, "defaultCipher", "RSA-BC-01");
    static final CfgString CFG_SIGNER_DEFAULT =
            new CfgString(TargetProcessor.class, "defaultSigner", "DSA-BC-01");

    void process(
            IProperties properties,
            IReadProperties processorConfig,
            SecretContent secret,
            WritableEntry entry)
            throws MException;

    void test(PrintStream out, IProperties properties, IReadProperties processorConfig)
            throws Exception;
}

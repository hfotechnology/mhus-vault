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
package de.mhus.app.vault.manager;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.osgi.api.karaf.AbstractCmd;

@Command(scope = "vault", name = "vault-expire", description = "expire entries")
@Service
public class CmdVaultExpire extends AbstractCmd {

    @Argument(
            index = 0,
            name = "target",
            required = false,
            description = "target",
            multiValued = false)
    String target;

    @Argument(
            index = 1,
            name = "filter",
            required = false,
            description = "Filter",
            multiValued = false)
    String filter;

    @Override
    public Object execute2() throws Exception {

        new ExpireUtil().expire(target, filter);
        System.out.println("OK");

        return null;
    }
}

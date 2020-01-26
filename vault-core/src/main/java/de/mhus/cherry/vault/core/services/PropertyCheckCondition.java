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
package de.mhus.cherry.vault.core.services;

import org.osgi.service.component.annotations.Component;
import de.mhus.cherry.vault.api.ifc.TargetCondition;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.matcher.Condition;
import de.mhus.lib.errors.MException;

@Component(property = "name=properties")
public class PropertyCheckCondition extends MLog implements TargetCondition {

    @Override
    public boolean check(IProperties properties, IReadProperties conditionConfig) {
        String condition = conditionConfig.getString("condition", null);
        if (condition != null) {
            try {
                Condition c = new Condition(condition);
                return c.matches(properties);
            } catch (MException e) {
                log().e(conditionConfig, e);
            }
        }
        return false;
    }
}

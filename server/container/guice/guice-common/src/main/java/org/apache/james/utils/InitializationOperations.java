/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.utils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.james.lifecycle.api.Startable;

import com.github.fge.lambdas.Throwing;
import com.google.inject.Inject;

public class InitializationOperations {

    private final Set<InitialisationOperation> initialisationOperations;
    private final Startables configurables;

    @Inject
    public InitializationOperations(Set<InitialisationOperation> initialisationOperations, Startables configurables) {
        this.initialisationOperations = initialisationOperations;
        this.configurables = configurables;
    }

    public void initModules() throws Exception {
        Set<InitialisationOperation> processed = processConfigurables();
        
        processOthers(processed);
    }

    private Set<InitialisationOperation> processConfigurables() {
        return configurables.get().stream()
            .flatMap(this::configurationPerformerFor)
            .distinct()
            .peek(Throwing.consumer(InitialisationOperation::initModule).sneakyThrow())
            .collect(Collectors.toSet());
    }

    private Stream<InitialisationOperation> configurationPerformerFor(Class<? extends Startable> configurable) {
        return initialisationOperations.stream()
                .filter(x -> x.forClass().equals(configurable));
    }

    private void processOthers(Set<InitialisationOperation> processed) {
        initialisationOperations.stream()
            .filter(x -> !processed.contains(x))
            .forEach(Throwing.consumer(InitialisationOperation::initModule).sneakyThrow());
    }
}

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
package org.apache.james.data;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.james.lifecycle.api.Startable;
import org.apache.james.server.core.configuration.ConfigurationProvider;
import org.apache.james.user.api.UsersRepository;
import org.apache.james.user.ldap.LdapRepositoryConfiguration;
import org.apache.james.user.ldap.ReadOnlyUsersLDAPRepository;
import org.apache.james.utils.InitialisationOperation;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class LdapUsersRepositoryModule extends AbstractModule {

    @Override
    public void configure() {
        bind(ReadOnlyUsersLDAPRepository.class).in(Scopes.SINGLETON);
        bind(UsersRepository.class).to(ReadOnlyUsersLDAPRepository.class);

        Multibinder.newSetBinder(binder(), InitialisationOperation.class).addBinding().to(LdapUsersRepositoryInitialisationOperation.class);
    }

    @Provides
    @Singleton
    public LdapRepositoryConfiguration provideConfiguration(ConfigurationProvider configurationProvider) throws ConfigurationException {
        return LdapRepositoryConfiguration.from(
            configurationProvider.getConfiguration("usersrepository"));
    }

    @Singleton
    public static class LdapUsersRepositoryInitialisationOperation implements InitialisationOperation {
        private final LdapRepositoryConfiguration configuration;
        private final ReadOnlyUsersLDAPRepository usersRepository;

        @Inject
        public LdapUsersRepositoryInitialisationOperation(LdapRepositoryConfiguration configuration, ReadOnlyUsersLDAPRepository usersRepository) {
            this.configuration = configuration;
            this.usersRepository = usersRepository;
        }

        @Override
        public void initModule() throws Exception {
            usersRepository.configure(configuration);
            usersRepository.init();
        }

        @Override
        public Class<? extends Startable> forClass() {
            return ReadOnlyUsersLDAPRepository.class;
        }
    }
}

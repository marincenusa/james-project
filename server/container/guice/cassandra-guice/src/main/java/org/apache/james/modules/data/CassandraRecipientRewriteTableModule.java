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
package org.apache.james.modules.data;

import org.apache.james.backends.cassandra.components.CassandraModule;
import org.apache.james.lifecycle.api.Startable;
import org.apache.james.rrt.api.RecipientRewriteTable;
import org.apache.james.rrt.cassandra.CassandraMappingsSourcesDAO;
import org.apache.james.rrt.cassandra.CassandraRRTModule;
import org.apache.james.rrt.cassandra.CassandraRecipientRewriteTable;
import org.apache.james.rrt.cassandra.CassandraRecipientRewriteTableDAO;
import org.apache.james.server.core.configuration.ConfigurationProvider;
import org.apache.james.utils.InitialisationOperation;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

public class CassandraRecipientRewriteTableModule extends AbstractModule {

    @Override
    public void configure() {
        bind(CassandraRecipientRewriteTable.class).in(Scopes.SINGLETON);
        bind(CassandraRecipientRewriteTableDAO.class).in(Scopes.SINGLETON);
        bind(CassandraMappingsSourcesDAO.class).in(Scopes.SINGLETON);
        bind(RecipientRewriteTable.class).to(CassandraRecipientRewriteTable.class);
        Multibinder<CassandraModule> cassandraDataDefinitions = Multibinder.newSetBinder(binder(), CassandraModule.class);
        cassandraDataDefinitions.addBinding().toInstance(CassandraRRTModule.MODULE);
        Multibinder.newSetBinder(binder(), InitialisationOperation.class).addBinding().to(CassandraRecipientRewriteTablePerformer.class);
    }

    @Singleton
    public static class CassandraRecipientRewriteTablePerformer implements InitialisationOperation {
        private final ConfigurationProvider configurationProvider;
        private final CassandraRecipientRewriteTable recipientRewriteTable;

        @Inject
        public CassandraRecipientRewriteTablePerformer(ConfigurationProvider configurationProvider, CassandraRecipientRewriteTable recipientRewriteTable) {
            this.configurationProvider = configurationProvider;
            this.recipientRewriteTable = recipientRewriteTable;
        }

        @Override
        public void initModule() throws Exception {
            recipientRewriteTable.configure(configurationProvider.getConfiguration("recipientrewritetable"));
        }

        @Override
        public Class<? extends Startable> forClass() {
            return CassandraRecipientRewriteTable.class;
        }
    }

}

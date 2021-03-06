/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.jdbc.core.context;

import lombok.Getter;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata.CachedDatabaseMetaData;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaDataLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Runtime context for sharding.
 */
@Getter
public final class ShardingRuntimeContext extends MultipleDataSourcesRuntimeContext<ShardingRule> {
    
    private final CachedDatabaseMetaData cachedDatabaseMetaData;
    
    private final ShardingTransactionManagerEngine shardingTransactionManagerEngine;
    
    private final Map<String, DataSource> dataSourceMap;
    
    public ShardingRuntimeContext(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final Properties props, final DatabaseType databaseType) throws SQLException {
        super(dataSourceMap, shardingRule, props, databaseType);
        this.dataSourceMap = dataSourceMap;
        cachedDatabaseMetaData = createCachedDatabaseMetaData(dataSourceMap);
        shardingTransactionManagerEngine = new ShardingTransactionManagerEngine();
        shardingTransactionManagerEngine.init(databaseType, dataSourceMap);
    }
    
    private CachedDatabaseMetaData createCachedDatabaseMetaData(final Map<String, DataSource> dataSourceMap) throws SQLException {
        try (Connection connection = dataSourceMap.values().iterator().next().getConnection()) {
            return new CachedDatabaseMetaData(connection.getMetaData());
        }
    }
    
    @Override
    protected RuleSchemaMetaData loadRuleSchemaMetaData(final Map<String, DataSource> dataSourceMap) throws SQLException {
        return new RuleSchemaMetaDataLoader(getRule().toRules()).load(getDatabaseType(), dataSourceMap, getProperties());
    }
    
    @Override
    public void close() throws Exception {
        shardingTransactionManagerEngine.close();
        super.close();
    }
}

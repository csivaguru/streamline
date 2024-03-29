/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.hortonworks.iotas.streams.layout.component.impl.normalization;

import com.hortonworks.iotas.common.Schema;

/**
 * It represents normalization configuration which uses bulk script to normalize payload from input {@link com.hortonworks.iotas.common.Schema}
 * to output {@link com.hortonworks.iotas.common.Schema}.
 *
 */
public class BulkNormalizationConfig extends NormalizationConfig {
    public final String normalizationScript;

    private BulkNormalizationConfig() {
        this(null, null);
    }

    public BulkNormalizationConfig(Schema inputSchema, String normalizationScript) {
        super(inputSchema);
        this.normalizationScript = normalizationScript;
    }
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.trident.operation;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;
import org.apache.storm.trident.operation.ITridentResource;
import org.apache.storm.topology.ResourceDeclarer;

public class DelegateResourceDeclarer implements Serializable, ResourceDeclarer, ITridentResource {
    private Object _delegate;

    protected DelegateResourceDeclarer(Object delegate) {
        _delegate = delegate;
    }

    @Override
    public DelegateResourceDeclarer setMemoryLoad(Number onHeap) {
        if(_delegate instanceof ResourceDeclarer) {
            ((ResourceDeclarer)_delegate).setMemoryLoad(onHeap);
        }
        return this;
    }

    @Override
    public DelegateResourceDeclarer setMemoryLoad(Number onHeap, Number offHeap) {
        if(_delegate instanceof ResourceDeclarer) {
            ((ResourceDeclarer)_delegate).setMemoryLoad(onHeap, offHeap);
        }
        return this;
    }

    @Override
    public DelegateResourceDeclarer setCPULoad(Number amount) {
        if(_delegate instanceof ResourceDeclarer) {
            ((ResourceDeclarer)_delegate).setCPULoad(amount);
        }
        return this;
    }

    @Override
    public Map<String, Number> getResources() {
        if(_delegate instanceof ITridentResource) {
            return ((ITridentResource)_delegate).getResources();
        }
        return new HashMap<>();
    }
}

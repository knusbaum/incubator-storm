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

import org.apache.storm.topology.ResourceDeclarer;

public abstract class DelegateResourceDeclarer implements ResourceDeclarer, ITridentResource {
    private Object _delegate;

    private DelegateResourceDeclarer(Object delegate) {
        _delegate = delegate;
    }
    
    @Override
    public Negate setMemoryLoad(Number onHeap) {
        if(_delegate instanceof ResourceDeclarer) {
            _delegate.setMemoryLoad(onHeap);
        }
        return this;
    }

    @Override
    public Negate setMemoryLoad(Number onHeap, number offHeap) {
        if(_delegate instanceof ResourceDeclarer) {
            _delegate.setMemoryLoad(onHeap, offHeap);
        }
        return this;
    }

    @Override
    public Negate setCPULoad(Number amount) {
        if(_delegate instanceof ResourceDeclarer) {
            _delegate.setCPULoad(amount);
        }
        return this;
    }

    @Override
    public Map<String, Object> getResources() {
        if(_delegate instanceof ITridentResource) {
            return _delegate.getResources();
        }
        return new HashMap<>();
    }
}

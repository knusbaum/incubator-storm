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
package org.apache.storm.trident.spout;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.topology.TransactionAttempt;
import org.apache.storm.tuple.Fields;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This defines a transactional spout which does *not* necessarily
 * replay the same batch every time it emits a batch for a transaction id.
 * 
 */
public interface IOpaquePartitionedTridentSpout<Partitions, Partition extends ISpoutPartition, M>
    extends ITridentDataSource {

    interface Coordinator<Partitions> {
        boolean isReady(long txid);
        Partitions getPartitionsForBatch();
        void close();
    }
    
    interface Emitter<Partitions, Partition extends ISpoutPartition, M> {
        /**
         * Emit a batch of tuples for a partition/transaction. 
         * 
         * Return the metadata describing this batch that will be used as lastPartitionMeta
         * for defining the parameters of the next batch.
         */
        M emitPartitionBatch(TransactionAttempt tx, TridentCollector collector, Partition partition, M lastPartitionMeta);
        
        /**
         * This method is called when this task is responsible for a new set of partitions. Should be used
         * to manage things like connections to brokers.
         */        
        void refreshPartitions(List<Partition> partitionResponsibilities);

        /**
         * @return The oredered list of partitions being processed by all the tasks
         */
        List<Partition> getOrderedPartitions(Partitions allPartitionInfo);

        /**
         * @return The list of partitions that are to be processed by the task with id {@code taskId}
         */
        default List<Partition> getPartitionsForTask(int taskId, int numTasks, Partitions allPartitionInfo){
            final List<Partition> orderedPartitions = getOrderedPartitions(allPartitionInfo);
            final List<Partition> taskPartitions = new ArrayList<>(orderedPartitions == null ? 0 : orderedPartitions.size());
            if (orderedPartitions != null) {
                for (int i = taskId; i < orderedPartitions.size(); i += numTasks) {
                    taskPartitions.add(orderedPartitions.get(i));
                }
            }
            return taskPartitions;
        }

        void close();
    }
    
    Emitter<Partitions, Partition, M> getEmitter(Map conf, TopologyContext context);

    Coordinator getCoordinator(Map conf, TopologyContext context);

    Map<String, Object> getComponentConfiguration();

    Fields getOutputFields();
}

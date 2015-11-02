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
/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package backtype.storm.generated;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.2)", date = "2015-10-21")
public class WorkerResources implements org.apache.thrift.TBase<WorkerResources, WorkerResources._Fields>, java.io.Serializable, Cloneable, Comparable<WorkerResources> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("WorkerResources");

  private static final org.apache.thrift.protocol.TField MEM_ON_HEAP_FIELD_DESC = new org.apache.thrift.protocol.TField("mem_on_heap", org.apache.thrift.protocol.TType.DOUBLE, (short)1);
  private static final org.apache.thrift.protocol.TField MEM_OFF_HEAP_FIELD_DESC = new org.apache.thrift.protocol.TField("mem_off_heap", org.apache.thrift.protocol.TType.DOUBLE, (short)2);
  private static final org.apache.thrift.protocol.TField CPU_FIELD_DESC = new org.apache.thrift.protocol.TField("cpu", org.apache.thrift.protocol.TType.DOUBLE, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new WorkerResourcesStandardSchemeFactory());
    schemes.put(TupleScheme.class, new WorkerResourcesTupleSchemeFactory());
  }

  private double mem_on_heap; // optional
  private double mem_off_heap; // optional
  private double cpu; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    MEM_ON_HEAP((short)1, "mem_on_heap"),
    MEM_OFF_HEAP((short)2, "mem_off_heap"),
    CPU((short)3, "cpu");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // MEM_ON_HEAP
          return MEM_ON_HEAP;
        case 2: // MEM_OFF_HEAP
          return MEM_OFF_HEAP;
        case 3: // CPU
          return CPU;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __MEM_ON_HEAP_ISSET_ID = 0;
  private static final int __MEM_OFF_HEAP_ISSET_ID = 1;
  private static final int __CPU_ISSET_ID = 2;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.MEM_ON_HEAP,_Fields.MEM_OFF_HEAP,_Fields.CPU};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.MEM_ON_HEAP, new org.apache.thrift.meta_data.FieldMetaData("mem_on_heap", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    tmpMap.put(_Fields.MEM_OFF_HEAP, new org.apache.thrift.meta_data.FieldMetaData("mem_off_heap", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    tmpMap.put(_Fields.CPU, new org.apache.thrift.meta_data.FieldMetaData("cpu", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(WorkerResources.class, metaDataMap);
  }

  public WorkerResources() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public WorkerResources(WorkerResources other) {
    __isset_bitfield = other.__isset_bitfield;
    this.mem_on_heap = other.mem_on_heap;
    this.mem_off_heap = other.mem_off_heap;
    this.cpu = other.cpu;
  }

  public WorkerResources deepCopy() {
    return new WorkerResources(this);
  }

  @Override
  public void clear() {
    set_mem_on_heap_isSet(false);
    this.mem_on_heap = 0.0;
    set_mem_off_heap_isSet(false);
    this.mem_off_heap = 0.0;
    set_cpu_isSet(false);
    this.cpu = 0.0;
  }

  public double get_mem_on_heap() {
    return this.mem_on_heap;
  }

  public void set_mem_on_heap(double mem_on_heap) {
    this.mem_on_heap = mem_on_heap;
    set_mem_on_heap_isSet(true);
  }

  public void unset_mem_on_heap() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __MEM_ON_HEAP_ISSET_ID);
  }

  /** Returns true if field mem_on_heap is set (has been assigned a value) and false otherwise */
  public boolean is_set_mem_on_heap() {
    return EncodingUtils.testBit(__isset_bitfield, __MEM_ON_HEAP_ISSET_ID);
  }

  public void set_mem_on_heap_isSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __MEM_ON_HEAP_ISSET_ID, value);
  }

  public double get_mem_off_heap() {
    return this.mem_off_heap;
  }

  public void set_mem_off_heap(double mem_off_heap) {
    this.mem_off_heap = mem_off_heap;
    set_mem_off_heap_isSet(true);
  }

  public void unset_mem_off_heap() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __MEM_OFF_HEAP_ISSET_ID);
  }

  /** Returns true if field mem_off_heap is set (has been assigned a value) and false otherwise */
  public boolean is_set_mem_off_heap() {
    return EncodingUtils.testBit(__isset_bitfield, __MEM_OFF_HEAP_ISSET_ID);
  }

  public void set_mem_off_heap_isSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __MEM_OFF_HEAP_ISSET_ID, value);
  }

  public double get_cpu() {
    return this.cpu;
  }

  public void set_cpu(double cpu) {
    this.cpu = cpu;
    set_cpu_isSet(true);
  }

  public void unset_cpu() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __CPU_ISSET_ID);
  }

  /** Returns true if field cpu is set (has been assigned a value) and false otherwise */
  public boolean is_set_cpu() {
    return EncodingUtils.testBit(__isset_bitfield, __CPU_ISSET_ID);
  }

  public void set_cpu_isSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __CPU_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case MEM_ON_HEAP:
      if (value == null) {
        unset_mem_on_heap();
      } else {
        set_mem_on_heap((Double)value);
      }
      break;

    case MEM_OFF_HEAP:
      if (value == null) {
        unset_mem_off_heap();
      } else {
        set_mem_off_heap((Double)value);
      }
      break;

    case CPU:
      if (value == null) {
        unset_cpu();
      } else {
        set_cpu((Double)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case MEM_ON_HEAP:
      return Double.valueOf(get_mem_on_heap());

    case MEM_OFF_HEAP:
      return Double.valueOf(get_mem_off_heap());

    case CPU:
      return Double.valueOf(get_cpu());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case MEM_ON_HEAP:
      return is_set_mem_on_heap();
    case MEM_OFF_HEAP:
      return is_set_mem_off_heap();
    case CPU:
      return is_set_cpu();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof WorkerResources)
      return this.equals((WorkerResources)that);
    return false;
  }

  public boolean equals(WorkerResources that) {
    if (that == null)
      return false;

    boolean this_present_mem_on_heap = true && this.is_set_mem_on_heap();
    boolean that_present_mem_on_heap = true && that.is_set_mem_on_heap();
    if (this_present_mem_on_heap || that_present_mem_on_heap) {
      if (!(this_present_mem_on_heap && that_present_mem_on_heap))
        return false;
      if (this.mem_on_heap != that.mem_on_heap)
        return false;
    }

    boolean this_present_mem_off_heap = true && this.is_set_mem_off_heap();
    boolean that_present_mem_off_heap = true && that.is_set_mem_off_heap();
    if (this_present_mem_off_heap || that_present_mem_off_heap) {
      if (!(this_present_mem_off_heap && that_present_mem_off_heap))
        return false;
      if (this.mem_off_heap != that.mem_off_heap)
        return false;
    }

    boolean this_present_cpu = true && this.is_set_cpu();
    boolean that_present_cpu = true && that.is_set_cpu();
    if (this_present_cpu || that_present_cpu) {
      if (!(this_present_cpu && that_present_cpu))
        return false;
      if (this.cpu != that.cpu)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_mem_on_heap = true && (is_set_mem_on_heap());
    list.add(present_mem_on_heap);
    if (present_mem_on_heap)
      list.add(mem_on_heap);

    boolean present_mem_off_heap = true && (is_set_mem_off_heap());
    list.add(present_mem_off_heap);
    if (present_mem_off_heap)
      list.add(mem_off_heap);

    boolean present_cpu = true && (is_set_cpu());
    list.add(present_cpu);
    if (present_cpu)
      list.add(cpu);

    return list.hashCode();
  }

  @Override
  public int compareTo(WorkerResources other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(is_set_mem_on_heap()).compareTo(other.is_set_mem_on_heap());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_mem_on_heap()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.mem_on_heap, other.mem_on_heap);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_mem_off_heap()).compareTo(other.is_set_mem_off_heap());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_mem_off_heap()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.mem_off_heap, other.mem_off_heap);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(is_set_cpu()).compareTo(other.is_set_cpu());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (is_set_cpu()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.cpu, other.cpu);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("WorkerResources(");
    boolean first = true;

    if (is_set_mem_on_heap()) {
      sb.append("mem_on_heap:");
      sb.append(this.mem_on_heap);
      first = false;
    }
    if (is_set_mem_off_heap()) {
      if (!first) sb.append(", ");
      sb.append("mem_off_heap:");
      sb.append(this.mem_off_heap);
      first = false;
    }
    if (is_set_cpu()) {
      if (!first) sb.append(", ");
      sb.append("cpu:");
      sb.append(this.cpu);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class WorkerResourcesStandardSchemeFactory implements SchemeFactory {
    public WorkerResourcesStandardScheme getScheme() {
      return new WorkerResourcesStandardScheme();
    }
  }

  private static class WorkerResourcesStandardScheme extends StandardScheme<WorkerResources> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, WorkerResources struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // MEM_ON_HEAP
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.mem_on_heap = iprot.readDouble();
              struct.set_mem_on_heap_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // MEM_OFF_HEAP
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.mem_off_heap = iprot.readDouble();
              struct.set_mem_off_heap_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // CPU
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.cpu = iprot.readDouble();
              struct.set_cpu_isSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, WorkerResources struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.is_set_mem_on_heap()) {
        oprot.writeFieldBegin(MEM_ON_HEAP_FIELD_DESC);
        oprot.writeDouble(struct.mem_on_heap);
        oprot.writeFieldEnd();
      }
      if (struct.is_set_mem_off_heap()) {
        oprot.writeFieldBegin(MEM_OFF_HEAP_FIELD_DESC);
        oprot.writeDouble(struct.mem_off_heap);
        oprot.writeFieldEnd();
      }
      if (struct.is_set_cpu()) {
        oprot.writeFieldBegin(CPU_FIELD_DESC);
        oprot.writeDouble(struct.cpu);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class WorkerResourcesTupleSchemeFactory implements SchemeFactory {
    public WorkerResourcesTupleScheme getScheme() {
      return new WorkerResourcesTupleScheme();
    }
  }

  private static class WorkerResourcesTupleScheme extends TupleScheme<WorkerResources> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, WorkerResources struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.is_set_mem_on_heap()) {
        optionals.set(0);
      }
      if (struct.is_set_mem_off_heap()) {
        optionals.set(1);
      }
      if (struct.is_set_cpu()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.is_set_mem_on_heap()) {
        oprot.writeDouble(struct.mem_on_heap);
      }
      if (struct.is_set_mem_off_heap()) {
        oprot.writeDouble(struct.mem_off_heap);
      }
      if (struct.is_set_cpu()) {
        oprot.writeDouble(struct.cpu);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, WorkerResources struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.mem_on_heap = iprot.readDouble();
        struct.set_mem_on_heap_isSet(true);
      }
      if (incoming.get(1)) {
        struct.mem_off_heap = iprot.readDouble();
        struct.set_mem_off_heap_isSet(true);
      }
      if (incoming.get(2)) {
        struct.cpu = iprot.readDouble();
        struct.set_cpu_isSet(true);
      }
    }
  }

}


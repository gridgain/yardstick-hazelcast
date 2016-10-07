
package org.yardstickframework.hazelcast;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;



public class SampleValueSerializableFactory implements DataSerializableFactory{

    public static final int FACTORY_ID = 1;

    public static final int SAMPLE_ID = 1;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if ( typeId == SAMPLE_ID ) {
            return new SampleValue();
        }
        return null;
    }
}
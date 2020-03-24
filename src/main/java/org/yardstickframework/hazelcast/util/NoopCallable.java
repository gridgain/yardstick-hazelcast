/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstickframework.hazelcast.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.Callable;

/**
 *
 */
public class NoopCallable implements Callable<Object>, Externalizable {

    /** {@inheritDoc} */
    @Override public Object call() throws Exception {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput output) throws IOException {
        //No - op
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        //No - op
    }

}

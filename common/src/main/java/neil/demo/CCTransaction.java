/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neil.demo;

import java.io.IOException;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

/**
 * <p>A simplistic credit card transaction
 * </p>
 */
public class CCTransaction implements Portable {

    private String txnId;
    private double amount;
    private String where;
    private long when;

    @Override
    public int getClassId() {
        return 2;
    }
    @Override
    public int getFactoryId() {
        return 1;
    }
    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        this.txnId = portableReader.readString("txnId");
        this.amount = portableReader.readDouble("amount");
        this.where = portableReader.readString("where");
        this.when = portableReader.readLong("when");
    }
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeString("txnId", this.txnId);
        portableWriter.writeDouble("amount", this.amount);
        portableWriter.writeString("where", this.where);
        portableWriter.writeLong("when", this.when);
    }
    public String getTxnId() {
        return txnId;
    }
    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getWhere() {
        return where;
    }
    public void setWhere(String where) {
        this.where = where;
    }
    public long getWhen() {
        return when;
    }
    public void setWhen(long when) {
        this.when = when;
    }
    @Override
    public String toString() {
        return "CCTransaction [txnId=" + txnId + ", amount=" + amount + ", where=" + where + ", when=" + when + "]";
    }

}

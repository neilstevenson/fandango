/*
 * Copyright (c) 2008-2021, Hazelcast, Inc. All Rights Reserved.
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

import java.io.Serializable;
import java.util.List;

/**
 * <p>A credit card user account.
 * Has some authorizations and some transactions
 * </p>
 */
@SuppressWarnings("serial")
public class CCUser implements Serializable {

    private String userId;
    private String firstName;
    private String lastName;
    private List<String> authIds;
    private List<String> txnIds;
    private int creditLimit;
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public List<String> getAuthIds() {
        return authIds;
    }
    public void setAuthIds(List<String> authIds) {
        this.authIds = authIds;
    }
    public List<String> getTxnIds() {
        return txnIds;
    }
    public void setTxnIds(List<String> txnIds) {
        this.txnIds = txnIds;
    }
    public int getCreditLimit() {
        return creditLimit;
    }
    public void setCreditLimit(int creditLimit) {
        this.creditLimit = creditLimit;
    }
    @Override
    public String toString() {
        return "CCUser [userId=" + userId + ", firstName=" + firstName + ", lastName=" + lastName + ", authIds="
                + authIds + ", txnIds=" + txnIds + ", creditLimit=" + creditLimit + "]";
    }

}

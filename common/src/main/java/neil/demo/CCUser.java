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

import lombok.Data;

/**
 * <p>A credit card user account.
 * Has some authorizations and some transactions
 * </p>
 */
@SuppressWarnings("serial")
@Data
public class CCUser implements Serializable {

    private String userId;
    private String firstName;
    private String lastName;
    private List<String> authIds;
    private List<String> txnIds;
    private int creditLimit;

}

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

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.hazelcast.core.HazelcastJsonValue;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Business logic for the microservice, one or more REST URLs.
 * This microservice invokes others.
 * </p>
 */
@RestController
@Slf4j
public class MyController {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${my.ms2.app.name}")
    private String ms2AppName;
    @Value("${my.ms4.app.name}")
    private String ms4AppName;
    @Value("${my.ms5.app.name}")
    private String ms5AppName;

    /**
     * <p>Return the balance available to spend, credit limit less spending and
     * authorizations.
     * </p>
     */
    @GetMapping("/" + MyConstants.REST_CALL_BALANCE + "/" + "{userId}")
    public Double balance(@PathVariable String userId) {
        log.info("balance({})", userId);

        CCUser ccUser = this.getUser(userId);
        CCTransaction[] ccTransactions = this.getCCTransactions(ccUser.getTxnIds());
        HazelcastJsonValue[] ccAuthorisations = this.getCCAuthorisations(ccUser.getAuthIds());

        double balance = Double.valueOf(ccUser.getCreditLimit());

        for (CCTransaction ccTransaction : ccTransactions) {
            balance -= ccTransaction.getAmount();
        }
        for (HazelcastJsonValue ccAuthorisation : ccAuthorisations) {
            JSONObject json = new JSONObject(ccAuthorisation.toString());
            balance -= json.getDouble("amount");
        }

        // Do not allow to go further overdrawn
        if (balance < 0) {
            log.warn("balance() :: balance derived '{}'", balance);
            balance = 0d;
        }

        log.debug("balance() :: returning '{}'", balance);

        return balance;
    }

    /**
     * <p>Microservice 2 provides the user details.
     * </p>
     *
     * @param userId
     * @return
     */
    private CCUser getUser(String userId) {
        log.info("getUser({})", userId);

        String url2 = MyConstants.TURBINE_CALL_PREFIX + "/"
                + this.ms2AppName + "/"
                + MyConstants.REST_CALL_USER + "/"
                + userId;

        try {
            log.debug("getUser('{}') :: url2 '{}'", userId, url2);
            ResponseEntity<CCUser> response2 = this.restTemplate.getForEntity(url2, CCUser.class);

            CCUser ccUser = response2.getBody();

            log.debug("getUser() :: returning '{}'", ccUser);

            return ccUser;
        } catch (Exception e) {
            log.error("getUser('" + userId + "')", e);
            return null;
        }
    }

    /**
     * <p>Microservice 4 provides the transactions.
     * </p>
     *
     * @param txnIds
     * @return
     */
    private CCTransaction[] getCCTransactions(List<String> txnIds) {
        log.info("getCCTransactions('{}')", txnIds);

        String url4 = MyConstants.TURBINE_CALL_PREFIX + "/"
                + this.ms4AppName + "/"
                + MyConstants.REST_CALL_TXNS + "/"
                + txnIds;

        try {
            log.debug("getCCTransactions('{}') :: url4 '{}'", txnIds, url4);
            ResponseEntity<String[][]> response4
                = this.restTemplate.getForEntity(url4, String[][].class);

            String[][] ccTransactionsArrArr = response4.getBody();

            CCTransaction[] ccTransactions = new CCTransaction[ccTransactionsArrArr.length];
            for (int i = 0 ; i < ccTransactionsArrArr.length; i++) {
                ccTransactions[i] = new CCTransaction();
                ccTransactions[i].setAmount(Double.parseDouble(ccTransactionsArrArr[i][0]));
                ccTransactions[i].setTxnId(ccTransactionsArrArr[i][1]);
                ccTransactions[i].setWhen(Long.parseLong(ccTransactionsArrArr[i][2]));
                ccTransactions[i].setWhere(ccTransactionsArrArr[i][3]);
            }

            log.debug("getCCTransactions() :: returning '{}'", Arrays.asList(ccTransactions));

            return ccTransactions;
        } catch (Exception e) {
            log.error("getCCTransactions('" + txnIds + "')", e);
            return new CCTransaction[0];
        }
    }

    /**
     * <p>Microservice 5 provides the authorizations.
     * </p>
     *
     * @param authIds
     * @return
     */
    private HazelcastJsonValue[] getCCAuthorisations(List<String> authIds) {
        log.info("getCCAuthorisations('{}')", authIds);

        String url5 = MyConstants.TURBINE_CALL_PREFIX + "/"
                + this.ms5AppName + "/"
                + MyConstants.REST_CALL_AUTHS + "/"
                + authIds;

        try {
            log.debug("getCCAuthorisations('{}') :: url4 '{}'", authIds, url5);
            // DTO, domain model is HazelcastJSONValue
            ResponseEntity<String[][]> response5
                = this.restTemplate.getForEntity(url5, String[][].class);

            String[][] ccAuthorisationsArrArr = response5.getBody();

            HazelcastJsonValue[] ccAuthorisations =
                    new HazelcastJsonValue[ccAuthorisationsArrArr.length];
            for (int i = 0 ; i < ccAuthorisationsArrArr.length; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{");
                stringBuilder.append(" \"authId\" : \"" + ccAuthorisationsArrArr[i][1] + "\"");
                stringBuilder.append(",\"amount\" : " + ccAuthorisationsArrArr[i][0]);
                stringBuilder.append(",\"where\" : \"" + ccAuthorisationsArrArr[i][2] + "\"");
                stringBuilder.append("}");
                ccAuthorisations[i] = new HazelcastJsonValue(stringBuilder.toString());
            }

            log.debug("getCCAuthorisations() :: returning '{}'", Arrays.asList(ccAuthorisations));

            return ccAuthorisations;
        } catch (Exception e) {
            log.error("getCCAuthorisations('" + authIds + "')", e);
            return new HazelcastJsonValue[0];
        }
    }

}

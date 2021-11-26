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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>MVC style controller that invokes microservices to provide data
 * for pages returned.
 * </p>
 */
@Controller
@Slf4j
public class MyController {

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${my.ms1.app.name}")
    private String ms1AppName;
    @Value("${my.ms2.app.name}")
    private String ms2AppName;
    @Value("${my.ms3.app.name}")
    private String ms3AppName;
    @Value("${my.ms4.app.name}")
    private String ms4AppName;
    @Value("${my.ms5.app.name}")
    private String ms5AppName;

    /**
     * <p>This only needs a method so we can log it being called.
     * </p>
     */
    @GetMapping(value = "/")
    public ModelAndView index() {
        log.info("index()");
        return new ModelAndView("index");
    }

    /**
     * <p>Call microservice 1 to get users basic info.
     * </p>
     */
    @GetMapping("/" + MyConstants.REST_CALL_USERS)
    public ModelAndView users() {
        log.info("users()");
        ModelAndView modelAndView = new ModelAndView("users");

        String url1 = MyConstants.TURBINE_CALL_PREFIX + "/"
                + this.ms1AppName + "/"
                + MyConstants.REST_CALL_USERS;

        List<List<Object>> data = new ArrayList<>();
        try {
            log.debug("users() :: url1 '{}'", url1);
            ResponseEntity<Object[][]> response1 = this.restTemplate.getForEntity(url1, Object[][].class);
            Object[][] users = response1.getBody();

            // Reformat results for HTML
            for (Object[] user : users) {
                List<Object> datum = Arrays.asList(user);
                data.add(datum);
            }

            modelAndView.addObject("data", data);
        } catch (Exception e) {
            log.error("users()", e);
        }

        return modelAndView;
    }

    /**
     * <p>Call microservice 2 to get details for a specific user, and
     * microservice 3 to get balance for same user. Microservice 3
     * in turn invokes microservices 4 and 5.
     * </p>
     */
    @GetMapping("/" + MyConstants.REST_CALL_USER)
    public ModelAndView user(@RequestParam(name = "userId", required = true) String userId) {
        log.info("user({})", userId);
        ModelAndView modelAndView = new ModelAndView("user");

        String url2 = MyConstants.TURBINE_CALL_PREFIX + "/"
                + this.ms2AppName + "/"
                + MyConstants.REST_CALL_USER + "/"
                + userId;
        String url3 = MyConstants.TURBINE_CALL_PREFIX + "/"
                + this.ms3AppName + "/"
                + MyConstants.REST_CALL_BALANCE + "/"
                + userId;

        try {
            log.debug("user({}) :: url2 '{}'", userId, url2);
            ResponseEntity<CCUser> response2
                = this.restTemplate.getForEntity(url2, CCUser.class);
            CCUser ccUser = response2.getBody();

            log.debug("user({}) :: url3 '{}'", userId, url3);
            ResponseEntity<Double> response3
                = this.restTemplate.getForEntity(url3, Double.class);
            Double balance = response3.getBody();

            modelAndView.addObject("ccUser", ccUser);
            modelAndView.addObject("balance", balance);
        } catch (Exception e) {
            log.error("user(" + userId + ")", e);
        }

        return modelAndView;
    }

    /**
     * <p>Get the detailed information for a user. Unlike the previous call to microservice 3
     * that just summarizes transactions.
     * </p>
     *
     * @param userId
     * @return
     */
    @GetMapping("/" + MyConstants.REST_CALL_DETAIL)
    public ModelAndView detail(@RequestParam(name = "userId", required = true) String userId) {
        log.info("detail({})", userId);
        ModelAndView modelAndView = new ModelAndView("detail");

        String url2 = MyConstants.TURBINE_CALL_PREFIX + "/"
                + this.ms2AppName + "/"
                + MyConstants.REST_CALL_USER + "/"
                + userId;

        try {
            log.debug("user({}) :: url2 '{}'", userId, url2);
            ResponseEntity<CCUser> response2
                = this.restTemplate.getForEntity(url2, CCUser.class);
            CCUser ccUser = response2.getBody();

            String txnIds = String.join(",", ccUser.getTxnIds());
            String authIds = String.join(",", ccUser.getAuthIds());

            String url4 = MyConstants.TURBINE_CALL_PREFIX + "/"
                    + this.ms4AppName + "/"
                    + MyConstants.REST_CALL_TXNS + "/"
                    + txnIds;
            String url5 = MyConstants.TURBINE_CALL_PREFIX + "/"
                    + this.ms5AppName + "/"
                    + MyConstants.REST_CALL_AUTHS + "/"
                    + authIds;

            ResponseEntity<CCTransaction[]> response4
                = this.restTemplate.getForEntity(url4, CCTransaction[].class);
            CCTransaction[] ccTransactions = response4.getBody();

            // DTO, domain model is HazelcastJSONValue
            ResponseEntity<String[][]> response5
                = this.restTemplate.getForEntity(url5, String[][].class);
            String[][] ccAuthorisationsArrArr = response5.getBody();

            log.debug("detail({}) userId :: {}", userId, userId);
            log.debug("detail({}) ccTransactions :: {}", userId, ccTransactions.toString());
            log.debug("detail({}) ccAuthorisationsDTO :: {}", userId, ccAuthorisationsArrArr.toString());

            modelAndView.addObject("userId", userId);
            modelAndView.addObject("ccTransactions", ccTransactions);
            modelAndView.addObject("ccAuthorisations", ccAuthorisationsArrArr);
        } catch (Exception e) {
            log.error("detail(" + userId + ")", e);
        }

         return modelAndView;
    }

}

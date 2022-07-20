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

/**
 * <p>Test data</p>
 * <p>User may have recorded transaction or authorization ids that
 * are not currently loaded. Could be map loader if required.
 * </p>
 */
public class TestData {

    // Places where a transaction can occur
    public static final String[] PLACES = new String[] {
            "The Office",
            "The Airport",
            "Hotel Hazelcast",
            "The Coffee Shop",
            "A Taxi",
    };

    // User - id, first name, last name, authorization ids, transaction ids, credit limit
    public static final Object[][] USERS = new Object[][] {
        { 1, "Neil",        "Stevenson",    "-1",             "2,3",                100 },
        { 2, "Curly",       "Howard",         "",               "4",                100 },
        { 3, "Larry",       "Fine",           "",               "5",                100 },
        { 4, "Moe",         "Howard",       "-2",               "6",                500 },
        { 5, "Shemp",       "Howard",         "",           "7,8,9",                100 },
        { 6, "Joe",         "Besser",         "",     "10,11,12,13",                100 },
        { 7, "Joe",         "DeRita",         "",  "14,15,16,17,18",                100 },
    };

    // Authorizations - id, amount, place
    public static final Object[][] AUTHS = new Object[][] {
        { -1,  50d, "Hotel Hazelcast" },
        { -2, 150d, "Hotel Hazelcast" },
    };

    // Tranactions - id, amount, place, date
    public static final Object[][] TXNS = new Object[][] {
        { 2, 15,          "A Taxi", 1626775880000L + 3693L },
        { 3, 50, "The Coffee Shop", 1626775880000L + 6936L },
    };

}

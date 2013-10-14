/*
Copyright 2013, The Sporting Exchange Limited

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

package com.betfair.sre.statse.client;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.testng.AssertJUnit.assertEquals;

/**
 * User: mcintyret2
 * Date: 14/08/2013
 */
@Test
public class CachingTsdbCleanerTest {

    private TsdbCleaner cleaner = new CachingTsdbCleaner();
    private Random random = new Random();

    public void shouldCleanup() {
        String result = cleaner.clean("My Name With Spaces And @ Symbol");

        assertEquals("MyNameWithSpacesAndSymbol", result);
    }

    private String[] serviceNames = new String[]{
        "service1 with space",
        "service2 with space",
        "anotherService with space",
        "moreService with space",
        "yetAnotherService with space",
        "yetAnotherService2 with space",
        "yetAnotherService1 with space",
        "yetAnotherService3 with space",
        "yetAnotherService4 with space",
        "yetAnotherService5 with space",
        "yetAnotherService6 with space",
        "yetAnotherService7 with space",
        "yetAnotherService8 with space",
        "yetAnotherService9 with space"
    };

    @Test(description = "Driver method to test memory and CPU use of TSDBCleaner", enabled = false)
    public void shouldTestPerformance() {
        List<String> names = new ArrayList<String>(1000);
        for (String name1 : serviceNames) {
            for (String name2 : serviceNames) {
                for (String name3 : serviceNames) {
                    names.add(name1 + name2 + name3);
                }
            }
        }
        for (int i = 0; i < 10000; i++) {
            cleaner.clean(names.get(random.nextInt(names.size())));
        }

        long start = System.nanoTime();
        for (int i = 0; i < 100000; i++) {
            cleaner.clean(names.get(random.nextInt(names.size())));
        }
        long end = System.nanoTime();

        System.out.println("Run took " + (end - start) + "ns");
    }
}

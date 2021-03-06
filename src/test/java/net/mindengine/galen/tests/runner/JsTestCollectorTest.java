/*******************************************************************************
* Copyright 2014 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package net.mindengine.galen.tests.runner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.mindengine.galen.GalenMain;
import net.mindengine.galen.components.JsTestRegistry;
import net.mindengine.galen.runner.GalenArguments;
import net.mindengine.galen.runner.JsTestCollector;
import net.mindengine.galen.tests.GalenTest;

import org.testng.annotations.Test;

public class JsTestCollectorTest {

    
    @Test
    public void shouldExecuteJavascript_andCollectTests() throws Exception {
        JsTestCollector testCollector = new JsTestCollector();
        
        JsTestRegistry.get().clear();
        testCollector.execute(new File(getClass().getResource("/js-tests/simple.test.js").getFile()));
        
        List<GalenTest> tests = testCollector.getCollectedTests();
        
        assertThat("Amount of tests should be", tests.size(), is(3));
        assertThat("Name of #1 test should be", tests.get(0).getName(), is("Test number 1"));
        assertThat("Name of #1 test should be", tests.get(1).getName(), is("Test number 2"));
        assertThat("Name of #1 test should be", tests.get(2).getName(), is("Test number 3"));

        tests.get(0).execute(null, null);
        tests.get(2).execute(null, null);
        
        assertThat("Events should be", JsTestRegistry.get().getEvents(), contains("Test #1 was invoked", "Test #3 was invoked"));
    }


    @Test
    public void shouldAllow_toUse_testFilter() throws IOException {
        JsTestCollector testCollector = new JsTestCollector();
        JsTestRegistry.get().clear();
        testCollector.execute(new File(getClass().getResource("/js-tests/testfilter.test.js").getFile()));

        List<GalenTest> tests = testCollector.getCollectedTests();
        assertThat(tests.get(0).getName(), is("Test A"));
        assertThat(tests.get(1).getName(), is("Test B"));
        assertThat(tests.get(2).getName(), is("Test C"));
        assertThat(tests.get(3).getName(), is("Test D"));
    }


    @Test
    public void shouldLoadOtherScripts_onlyOnce() throws IOException {
        JsTestCollector testCollector = new JsTestCollector();
        JsTestRegistry.get().clear();
        testCollector.execute(new File(getClass().getResource("/js-tests/multilevel/main.test.js").getFile()));
        testCollector.execute(new File(getClass().getResource("/js-tests/multilevel/folder/second.test.js").getFile()));


        List<String> events = JsTestRegistry.get().getEvents();
        assertThat("Events amount should be", events.size(), is(3));

        assertThat("Events should be", events, contains("included.js was loaded", "From main name is visible as Included object", "From second name is visible as Included object"));
    }
}

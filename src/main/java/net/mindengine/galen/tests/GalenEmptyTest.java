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
package net.mindengine.galen.tests;

import net.mindengine.galen.reports.TestReport;
import net.mindengine.galen.runner.CompleteListener;

public class GalenEmptyTest implements GalenTest {
    private final String testName;

    public GalenEmptyTest(String testName) {
        this.testName = testName;
    }

    @Override
    public String getName() {
        return testName;
    }
    @Override
    public void execute(TestReport report, CompleteListener listener) throws Exception {
        throw new RuntimeException("Cannot execute test: " + testName);
    }
}

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
package net.mindengine.galen.tests.specs.reader;

import static java.util.Arrays.asList;
import static net.mindengine.galen.specs.Location.locations;
import static net.mindengine.galen.specs.Side.LEFT;
import static net.mindengine.galen.specs.Side.RIGHT;
import static net.mindengine.galen.specs.Side.TOP;
import static net.mindengine.galen.specs.Side.sides;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.mindengine.galen.browser.Browser;
import net.mindengine.galen.browser.SeleniumBrowser;
import net.mindengine.galen.components.JsTestRegistry;
import net.mindengine.galen.components.MockedBrowser;
import net.mindengine.galen.components.mocks.driver.MockedDriver;
import net.mindengine.galen.components.validation.MockedPage;
import net.mindengine.galen.page.Page;
import net.mindengine.galen.parser.FileSyntaxException;
import net.mindengine.galen.specs.Location;
import net.mindengine.galen.specs.Range;
import net.mindengine.galen.specs.Spec;
import net.mindengine.galen.specs.SpecAbsent;
import net.mindengine.galen.specs.SpecComponent;
import net.mindengine.galen.specs.SpecInside;
import net.mindengine.galen.specs.SpecNear;
import net.mindengine.galen.specs.SpecWidth;
import net.mindengine.galen.specs.page.ConditionalBlock;
import net.mindengine.galen.specs.page.ConditionalBlockStatement;
import net.mindengine.galen.specs.page.Locator;
import net.mindengine.galen.specs.page.ObjectSpecs;
import net.mindengine.galen.specs.page.PageSection;
import net.mindengine.galen.specs.reader.Place;
import net.mindengine.galen.specs.reader.page.PageSpec;
import net.mindengine.galen.specs.reader.page.PageSpecReader;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

public class PageSpecsReaderTest {
    
    private static final String BASE_TEST = "shouldLoadSpecSuccessfully";
    private static final Browser EMPTY_BROWSER = new SeleniumBrowser(new MockedDriver("/mocks/pages/base-page.json"));
    private static final Page EMPTY_PAGE = EMPTY_BROWSER.getPage();
    private static final Properties EMPTY_PROPERTIES = new Properties();
    PageSpecReader pageSpecReader = new PageSpecReader(EMPTY_PROPERTIES, EMPTY_PAGE);
    PageSpec pageSpec;
    
    @Test
    public void shouldBePossible_toReadSpec_fromInputStream() throws IOException {
        PageSpec pageSpec = new PageSpecReader(EMPTY_PROPERTIES, EMPTY_PAGE).read(getClass().getResourceAsStream("/specs.txt"));
        assertThat(pageSpec, is(notNullValue()));
    }
    
    @Test
    public void shouldBePossible_toReadSpec_fromFile() throws IOException {

        PageSpec pageSpec = new PageSpecReader(EMPTY_PROPERTIES, EMPTY_PAGE).read(getClass().getResource("/specs.txt").getFile());
        assertThat(pageSpec, is(notNullValue()));
    }
    
    @Test
    public void shouldLoadSpecSuccessfully() throws IOException {
        pageSpec = pageSpecReader.read(getClass().getResource("/specs.txt").getFile());
        assertThat(pageSpec, is(notNullValue()));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldRead_objectDefinitions() {
        Map<String, Locator> objects = pageSpec.getObjects();
        assertThat("Amount of objects should be", objects.size(), is(6));
        assertThat(objects, hasEntry("submit", new Locator("xpath", "//input[@name = 'submit']"))); 
        assertThat(objects, hasEntry("search-field", new Locator("css", "#search")));
        assertThat(objects, hasEntry("menu", new Locator("id", "menu")));
        assertThat(objects, hasEntry("big-box", new Locator("tag", "container")));
        assertThat(objects, hasEntry("anotherObject", new Locator("xpath", "//div")));
        assertThat(objects, hasEntry("menu-item-1", new Locator("css", "#menu .menu-item", 1)));
    }
    
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldRead_allSpecs_asSections_markedByTags() {
        List<PageSection> sections = pageSpec.getSections();

        assertThat("Amount of sections should be", sections.size(), is(6));
        assertThat(sections.get(0).getTags(), hasSize(0));
        
        assertThat(sections.get(1).getTags(), hasSize(2));
        assertThat(sections.get(1).getTags(), contains("tablet", "desktop"));
        
        assertThat(sections.get(2).getTags(), hasSize(0));
        
        assertThat(sections.get(3).getTags(), hasSize(1));
        assertThat(sections.get(3).getTags(), contains("mobile"));
        
        assertThat(sections.get(4).getTags(), hasSize(1));
        assertThat(sections.get(4).getTags(), contains("parameterized"));
        
        assertThat(sections.get(5).getTags(), hasSize(1));
        assertThat(sections.get(5).getTags(), contains("parameterized2"));
    }
    
    @Test(dependsOnMethods = BASE_TEST) 
    public void shouldStore_lineNumber_inSpecs() {
        List<PageSection> sections = pageSpec.getSections();
        Place place = sections.get(0).getObjects().get(0).getSpecs().get(0).getPlace();
        assertThat(place.getFilePath(), endsWith("/specs.txt"));
        assertThat(place.getLineNumber(), is(18));
        
        place = sections.get(1).getObjects().get(0).getSpecs().get(0).getPlace();
        assertThat(place.getFilePath(), endsWith("/specs.txt"));
        assertThat(place.getLineNumber(), is(24));
    }
    
    @Test
    public void shouldAlways_provideAsteriskTags_whenFiltering_byIncludedTags() throws IOException {
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/spec-asterisk-tags.spec").getFile());
        
        assertThat("Total amount of sections should be", pageSpec.getSections().size(), is(3));
        
        List<PageSection> filteredSections = pageSpec.findSections(asList("tag2"));
        assertThat("Amount of filtered sections should be", filteredSections.size(), is(2));
        assertThat("Tag for first filtered section should be", filteredSections.get(0).getTags(), contains("*"));
        assertThat("Tag for second filtered section should be", filteredSections.get(1).getTags(), contains("tag2"));
    }

    /**
     * Comes from https://github.com/galenframework/galen/issues/95
     * Incorrect imported spec file path resolution
     * @throws Exception
     */
    @Test
    public void shouldAvoidUsing_doubleSlashes_whenImporting_andNotGiveException() throws Exception {
        pageSpecReader.read("/specs/spec-issue-double-slash.spec");
    }

    @Test
    public void shouldAllow_toCountObject_byIvoking_JavascriptFunction() throws IOException {
        WebDriver driver = new MockedDriver();
        driver.get("/mocks/pages/count-via-js-page.json");
        PageSpecReader pageSpecReader = new PageSpecReader(EMPTY_PROPERTIES, new SeleniumBrowser(driver).getPage());
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/count-via-js.spec").getFile());

        List<ObjectSpecs> objectSpecs = pageSpec.getSections().get(0).getObjects();

        assertThat(objectSpecs.size(), is(3));
        assertThat(objectSpecs.get(0).getObjectName(), is("menu-item-1"));
        assertThat(objectSpecs.get(1).getObjectName(), is("menu-item-2"));
        assertThat(objectSpecs.get(2).getObjectName(), is("menu-item-3"));

        assertThat(objectSpecs.get(0).getSpecs().get(0).getOriginalText(), is("near: menu-item-2 0px left"));
        assertThat(objectSpecs.get(1).getSpecs().get(0).getOriginalText(), is("near: menu-item-3 0px left"));
        assertThat(objectSpecs.get(2).getSpecs().get(0).getOriginalText(), is("near: menu-item-4 0px left"));
    }

    /**
     * Comes from a bug report #134
     * The problem was that the count couldn't find all objects in case the page spec had an import of other spec
     * @throws IOException
     */
    @Test
    public void should_countObjects_evenWhenUsing_emptyImports() throws IOException {
        WebDriver driver = new MockedDriver();
        driver.get("/mocks/pages/count-via-js-page.json");
        PageSpecReader pageSpecReader = new PageSpecReader(EMPTY_PROPERTIES, new SeleniumBrowser(driver).getPage());
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/count-bug-134.spec").getFile());

        List<ObjectSpecs> objectSpecs = pageSpec.getSections().get(0).getObjects();
        assertThat(objectSpecs.get(0).getSpecs().get(0).getOriginalText(), is("text is: A count is 4"));
    }

    /** Comes from a bug in version 1.4.0
     */
    @Test
    public void should_countObject_insideSecondarySpec() throws IOException {
        WebDriver driver = new MockedDriver();
        driver.get("/mocks/pages/count-via-js-page.json");
        PageSpecReader pageSpecReader = new PageSpecReader(EMPTY_PROPERTIES, new SeleniumBrowser(driver).getPage());
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/count/main.spec").getFile());

        List<ObjectSpecs> objectSpecs = pageSpec.getSections().get(0).getObjects();
        assertThat(objectSpecs.get(0).getSpecs().get(0).getOriginalText(), is("text is: A count is 4"));
    }

    @Test
    public void shoulParameterize_withOneItem_whenTemplateIsGiven_2_to_2() throws IOException {
        PageSpec pageSpec = pageSpecReader.read("/specs/single-item-range-parameterization.spec");
        assertThat(pageSpec.getSections().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getObjects().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getObjectName(), is("menu-item-2"));
    }

    @Test
    public void shouldNotParameterize_ifTemplateIsGiven_from_1_to_0_or_from_1_to_minus1() throws IOException {
        PageSpec pageSpec = pageSpecReader.read("/specs/incorrect-parameterization.spec");
        assertThat(pageSpec.getSections().size(), is(0));
    }
    
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldRead_parameterizedSpecs() {
        List<PageSection> sections = pageSpec.findSections(asList("parameterized"));
        assertThat(sections.size(), is(1));
        
        PageSection section = sections.get(0);
        
        List<ObjectSpecs> objects = section.getObjects();
        
        assertThat(objects.size(), is(6));
        
        for (int index = 1; index <=6; index++) {
            ObjectSpecs objectSpecs = objects.get(index - 1);
            assertThat(objectSpecs.getObjectName(), is("box-" + index + "-link"));
            SpecInside spec = (SpecInside) objectSpecs.getSpecs().get(0);
            assertThat(spec.getObject(), is("box-" + index));
        }
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldRead_parameterizedSpecs_2() {
        List<PageSection> sections = pageSpec.findSections(asList("parameterized2"));
        assertThat(sections.size(), is(1));
        
        PageSection section = sections.get(0);
        
        List<ObjectSpecs> objects = section.getObjects();
        
        assertThat(objects.size(), is(6));
        
        //[ 1, 3, 6-8, 10]
        int[] indexes = {1, 3, 6, 7, 8, 10};
        
        for (int index = 0; index <6; index++) {
            ObjectSpecs objectSpecs = objects.get(index);
            int number = indexes[index];
            assertThat("Object name #" + index, objectSpecs.getObjectName(), is("box-" + number + "-link"));
            SpecInside spec = (SpecInside) objectSpecs.getSpecs().get(0);
            assertThat(spec.getObject(), is("box-" + number));
        }
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldRead_allSpecs_withinFirstSection() {
        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();
        assertThat(objects.size(), is(1));
        
        ObjectSpecs objectSpecs = objects.get(0);
        assertThat(objectSpecs.getObjectName(), is("menu"));
        
        List<Spec> specs = objectSpecs.getSpecs();
        assertThat(specs.size(), is(2));
        
        assertThat((SpecNear) specs.get(0), is(new SpecNear("button", locations(new Location(Range.exact(10), sides(LEFT))))));
        assertThat((SpecWidth) specs.get(1), is(new SpecWidth(Range.exact(70))));
    }

    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldRead_allSpecs_withinSecondSection() {
        List<ObjectSpecs> objects = pageSpec.getSections().get(1).getObjects();
        assertThat(objects.size(), is(2));
        
        assertThat(objects.get(0).getObjectName(), is("submit"));
        assertThat(objects.get(0).getSpecs().size(), is(2));
        assertThat((SpecInside) objects.get(0).getSpecs().get(0), is(new SpecInside("big-box", 
                locations(
                        new Location(Range.between(10, 30), sides(RIGHT)),
                        new Location(Range.between(20, 40), sides(TOP))
        ))));
        
        assertThat((SpecNear) objects.get(0).getSpecs().get(1), is(new SpecNear("menu", locations(new Location(Range.exact(20), sides(LEFT))))));
        
        assertThat(objects.get(1).getObjectName(), is("search-field"));
        assertThat(objects.get(1).getSpecs().size(), is(1));
        assertThat((SpecInside) objects.get(1).getSpecs().get(0), is(new SpecInside("big-box", locations(new Location(Range.exact(30), sides(RIGHT))))));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldRead_allSpecs_withinThirdSection() {
        List<ObjectSpecs> objects = pageSpec.getSections().get(2).getObjects();
        assertThat(objects.size(), is(1));
        
        ObjectSpecs objectSpecs = objects.get(0);
        assertThat(objectSpecs.getObjectName(), is("big-box"));
        
        List<Spec> specs = objectSpecs.getSpecs();
        assertThat(specs.size(), is(1));
        
        assertThat((SpecWidth) specs.get(0), is(new SpecWidth(Range.exact(900))));
    }
    
    @Test(dependsOnMethods = BASE_TEST)
    public void shouldRead_allSpecs_withinFourthSection() {
        List<ObjectSpecs> objects = pageSpec.getSections().get(3).getObjects();
        assertThat(objects.size(), is(1));
        
        ObjectSpecs objectSpecs = objects.get(0);
        assertThat(objectSpecs.getObjectName(), is("submit"));
        
        List<Spec> specs = objectSpecs.getSpecs();
        assertThat(specs.size(), is(1));
        
        assertThat((SpecAbsent) specs.get(0), is(new SpecAbsent()));
    }
    
    @Test
    public void shouldRead_andProcess_bashTemplateExpressions() throws IOException {
        PageSpec pageSpec = new PageSpecReader(EMPTY_PROPERTIES, EMPTY_PAGE).read(getClass().getResourceAsStream("/specs/spec-bash-template.spec"));
        assertThat(pageSpec, is(notNullValue()));
        
        List<PageSection> sections = pageSpec.getSections();
        assertThat(sections.size(), is(1));
        
        List<ObjectSpecs> objects = sections.get(0).getObjects();
        assertThat(objects.size(), is(3));
        
        assertThat(objects.get(0).getObjectName(), is("obj-1"));
        assertThat(objects.get(1).getObjectName(), is("obj-2"));
        assertThat(objects.get(2).getObjectName(), is("obj-3"));
    }
    
    @Test
    public void shouldRead_sectionsNames_withTags() throws IOException {
        PageSpec pageSpec = new PageSpecReader(EMPTY_PROPERTIES, EMPTY_PAGE).read(getClass().getResourceAsStream("/specs/spec-sections-advanced.spec"));
        assertThat(pageSpec, is(notNullValue()));
        
        List<PageSection> sections = pageSpec.getSections();
        assertThat(sections.size(), is(7));
        
        assertThat(sections.get(0).getName(), is("Section 1"));
        assertThat(sections.get(0).getTags(), contains("all", "mobile"));
        
        assertThat(sections.get(1).getName(), is("Section 2"));
        assertThat(sections.get(1).getTags().size(), is(0));
        
        assertThat(sections.get(2).getName(), is("Section 3"));
        assertThat(sections.get(2).getTags(), contains("mobile", "tablet"));
        
        assertThat(sections.get(3).getName(), is("Section 4"));
        assertThat(sections.get(3).getTags(), contains("mobile", "tablet"));
        
        assertThat(sections.get(4).getName(), is("Section 4"));
        assertThat(sections.get(4).getTags(), contains("mobile", "tablet"));
        
        assertThat(sections.get(5).getName(), is("mobile,tablet,  desktop"));
        assertThat(sections.get(5).getTags(), contains("mobile", "tablet", "desktop"));
        
        assertThat(sections.get(6).getName(), is(""));
        assertThat(sections.get(6).getTags().size(), is(0));
    }
    
    
    @Test
    public void givesError_ifThereAreSpecs_withNoObjectSpecified_inSection() throws IOException {
        FileSyntaxException exception = expectExceptionFromReading("/negative-specs/no-object-in-section.spec");
        
        String fullSpecPath = getClass().getResource("/negative-specs/no-object-in-section.spec").getFile();
        
        assertThat(exception.getMessage(), is("There is no object defined in section\n    in " + fullSpecPath + ":8"));
        assertThat(exception.getFilePath(), endsWith("/no-object-in-section.spec"));
        assertThat(exception.getLine(), is(8));
    }
    
    @Test
    public void givesError_ifThereAre_invalidSpecs() throws IOException {
        FileSyntaxException exception = expectExceptionFromReading("/negative-specs/invalid-spec.spec");
        String fullSpecPath = getClass().getResource("/negative-specs/invalid-spec.spec").getFile();
        
        assertThat(exception.getMessage(), is("There is no location defined\n    in " + fullSpecPath + ":10"));
        assertThat(exception.getFilePath(), endsWith("/invalid-spec.spec"));
        assertThat(exception.getLine(), is(10));
    }
    
    @Test 
    public void givesError_ifThereAre_invalidObjectLocators() throws Exception {
        FileSyntaxException exception = expectExceptionFromReading("/negative-specs/invalid-object-locator.spec");
        
        String fullSpecPath = getClass().getResource("/negative-specs/invalid-object-locator.spec").getFile();
        
        assertThat(exception.getMessage(), is("Missing locator for object \"bad-object\"\n    in " + fullSpecPath +":7"));
        assertThat(exception.getFilePath(), endsWith("/invalid-object-locator.spec"));
        assertThat(exception.getLine(), is(7));
    }
    
    @Test 
    public void givesError_ifThereAre_tooManySpacesInIndentation() throws Exception {
        FileSyntaxException exception = expectExceptionFromReading("/negative-specs/incorrect-indentation-1.spec");
        
        String fullSpecPath = getClass().getResource("/negative-specs/incorrect-indentation-1.spec").getFile();
        
        assertThat(exception.getMessage(), is("Incorrect indentation. Use from 1 to 8 spaces for indentation\n    in " + fullSpecPath +":9"));
        assertThat(exception.getFilePath(), endsWith("/incorrect-indentation-1.spec"));
        assertThat(exception.getLine(), is(9));
    }
    
    @Test 
    public void givesError_ifThereAre_differentIndentation() throws Exception {
        FileSyntaxException exception = expectExceptionFromReading("/negative-specs/incorrect-indentation-2.spec");
        
        String fullSpecPath = getClass().getResource("/negative-specs/incorrect-indentation-2.spec").getFile();
        
        assertThat(exception.getMessage(), is("Incorrect indentation. You should use same indentation within one spec\n    in " + fullSpecPath + ":10"));
        assertThat(exception.getFilePath(), endsWith("/incorrect-indentation-2.spec"));
        assertThat(exception.getLine(), is(10));
    }
    
    @Test 
    public void givesError_ifThereAre_tabsIndentations() throws Exception {
        FileSyntaxException exception = expectExceptionFromReading("/negative-specs/incorrect-indentation-3.spec");
        
        String fullSpecPath = getClass().getResource("/negative-specs/incorrect-indentation-3.spec").getFile();
        
        assertThat(exception.getMessage(), is("Incorrect indentation. Should not use tabs. Use spaces\n    in " + fullSpecPath + ":9"));
        assertThat(exception.getFilePath(), endsWith("/incorrect-indentation-3.spec"));
        assertThat(exception.getLine(), is(9));
    }
    
    @Test
    public void shouldImport_otherSpecs_fromOtherFiles() throws Exception {
    	PageSpec pageSpec = pageSpecReader.read("/spec-import-test/main.spec");
    	
    	assertThat(pageSpec.getObjects(), hasKey("content"));
    	assertThat(pageSpec.getObjects(), hasKey("header"));
    	assertThat(pageSpec.getObjects(), hasKey("header-text"));
    	
    	
    	List<PageSection> sections = pageSpec.getSections();
    	
    	assertThat(sections.size(), is(2));
    	
    	assertThat(sections.get(0).getTags(), contains("all"));
    	
    	List<ObjectSpecs> objects = sections.get(0).getObjects();
    	assertThat(objects.size(), is(2));
    	assertThat(objects.get(0).getObjectName(), is("header"));
    	assertThat(objects.get(1).getObjectName(), is("header-text"));
    	
    	assertThat(sections.get(1).getTags(), contains("all"));
    	objects = sections.get(1).getObjects();
    	assertThat(objects.size(), is(1));
    	assertThat(objects.get(0).getObjectName(), is("content"));
    	
    }
    
    @Test
    public void shouldProcess_simpleMathOperations_inParameterizedSpecs() throws IOException {
    	PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/spec-math.spec").getFile());
    	
        List<Spec> specs = pageSpec.getSections().get(0).getObjects().get(0).getSpecs();
    	
    	assertThat(specs.size(), is(2));
    	assertThat(specs.get(0).getOriginalText(), is("inside: box-2 10px left"));
    	assertThat(specs.get(1).getOriginalText(), is("inside: box-4 10px left"));
    	
    }
    
    @Test 
    public void shouldParse_conditionalSpecBlocks() throws Exception {
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/spec-conditional-simple.spec").getFile());
        
        List<PageSection> sections = pageSpec.getSections();
        assertThat(sections.size(), is(1));
        
        PageSection pageSection = sections.get(0);
        List<ConditionalBlock> blocks = pageSection.getConditionalBlocks();
        assertThat(blocks.size(), is(1));
        
        ConditionalBlock block = blocks.get(0);
        List<ConditionalBlockStatement> statements = block.getStatements();
        assertThat(statements.size(), is(1));
        ConditionalBlockStatement statement = statements.get(0);
        assertThat(statement.isInverted(), is(false));
        
        List<ObjectSpecs> statementObjects = statement.getObjects();
        assertThat(statementObjects.size(), is(2));
        assertThat(statementObjects.get(0).getObjectName(), is("button-1"));
        assertThat(statementObjects.get(0).getSpecs().get(0).getOriginalText(), is("width: > 100px"));
        assertThat(statementObjects.get(1).getObjectName(), is("button-2"));
        assertThat(statementObjects.get(1).getSpecs().get(0).getOriginalText(), is("height: < 50px"));
        assertThat(statementObjects.get(1).getSpecs().get(1).getOriginalText(), is("width: 200px"));
        
        List<ObjectSpecs> bodyObjects = block.getBodyObjects();
        assertThat(bodyObjects.size(), is(2));
        assertThat(bodyObjects.get(0).getObjectName(), is("button-1"));
        assertThat(bodyObjects.get(0).getSpecs().get(0).getOriginalText(), is("above: button-2 0px"));
        assertThat(bodyObjects.get(1).getObjectName(), is("button-2"));
        assertThat(bodyObjects.get(1).getSpecs().get(0).getOriginalText(), is("below: button-1 0px"));
        
        
        List<ObjectSpecs> sectionObjects = pageSection.getObjects();
        assertThat(sectionObjects.size(), is(2));
        assertThat(sectionObjects.get(0).getObjectName(), is("textfield"));
        assertThat(sectionObjects.get(0).getObjectName(), is("textfield"));
    }
    
    @Test 
    public void shouldParse_conditionalSpecBlocks_withOrStatement() throws Exception {
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/spec-conditional-or.spec").getFile());
        
        List<PageSection> sections = pageSpec.getSections();
        assertThat(sections.size(), is(1));
        
        PageSection pageSection = sections.get(0);
        List<ConditionalBlock> blocks = pageSection.getConditionalBlocks();
        assertThat(blocks.size(), is(1));
        
        ConditionalBlock block = blocks.get(0);
        List<ConditionalBlockStatement> statements = block.getStatements();
        assertThat(statements.size(), is(3));
        
        {
            ConditionalBlockStatement statement = statements.get(0);
            assertThat(statement.isInverted(), is(false));
            List<ObjectSpecs> statementObjects = statement.getObjects();
            assertThat(statementObjects.size(), is(1));
            assertThat(statementObjects.get(0).getObjectName(), is("button-1"));
            assertThat(statementObjects.get(0).getSpecs().get(0).getOriginalText(), is("width: > 100px"));
        }
        {
            ConditionalBlockStatement statement = statements.get(1);
            assertThat(statement.isInverted(), is(false));
            List<ObjectSpecs> statementObjects = statement.getObjects();
            assertThat(statementObjects.size(), is(1));
            assertThat(statementObjects.get(0).getObjectName(), is("button-2"));
            assertThat(statementObjects.get(0).getSpecs().get(0).getOriginalText(), is("width: 200px"));
        }
        {
            ConditionalBlockStatement statement = statements.get(2);
            assertThat(statement.isInverted(), is(false));
            List<ObjectSpecs> statementObjects = statement.getObjects();
            assertThat(statementObjects.size(), is(1));
            assertThat(statementObjects.get(0).getObjectName(), is("button-3"));
            assertThat(statementObjects.get(0).getSpecs().get(0).getOriginalText(), is("width: 200px"));
        }
        
        List<ObjectSpecs> bodyObjects = block.getBodyObjects();
        assertThat(bodyObjects.size(), is(2));
        assertThat(bodyObjects.get(0).getObjectName(), is("button-1"));
        assertThat(bodyObjects.get(0).getSpecs().get(0).getOriginalText(), is("text is: sample text"));
        assertThat(bodyObjects.get(1).getObjectName(), is("button-2"));
        assertThat(bodyObjects.get(1).getSpecs().get(0).getOriginalText(), is("text ends: something"));
        
        
    }
    
    @Test 
    public void shouldParse_conditionalSpecBlocks_inverted() throws Exception {
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/spec-conditional-inverted.spec").getFile());
        
        List<PageSection> sections = pageSpec.getSections();
        assertThat(sections.size(), is(1));
        
        PageSection pageSection = sections.get(0);
        List<ConditionalBlock> blocks = pageSection.getConditionalBlocks();
        assertThat(blocks.size(), is(1));
        
        ConditionalBlock block = blocks.get(0);
        List<ConditionalBlockStatement> statements = block.getStatements();
        assertThat(statements.size(), is(2));
        
        {
            ConditionalBlockStatement statement = statements.get(0);
            assertThat(statement.isInverted(), is(true));
            List<ObjectSpecs> statementObjects = statement.getObjects();
            assertThat(statementObjects.size(), is(1));
            assertThat(statementObjects.get(0).getObjectName(), is("button-1"));
            assertThat(statementObjects.get(0).getSpecs().get(0).getOriginalText(), is("width: > 100px"));
        }
        {
            ConditionalBlockStatement statement = statements.get(1);
            assertThat(statement.isInverted(), is(true));
            List<ObjectSpecs> statementObjects = statement.getObjects();
            assertThat(statementObjects.size(), is(1));
            assertThat(statementObjects.get(0).getObjectName(), is("button-2"));
            assertThat(statementObjects.get(0).getSpecs().get(0).getOriginalText(), is("width: 200px"));
        }
        
        List<ObjectSpecs> bodyObjects = block.getBodyObjects();
        assertThat(bodyObjects.size(), is(2));
        assertThat(bodyObjects.get(0).getObjectName(), is("button-1"));
        assertThat(bodyObjects.get(0).getSpecs().get(0).getOriginalText(), is("text is: sample text"));
        assertThat(bodyObjects.get(1).getObjectName(), is("button-2"));
        assertThat(bodyObjects.get(1).getSpecs().get(0).getOriginalText(), is("text ends: something"));
        
        
    }
    
    @Test 
    public void shouldParse_conditionalSpecBlocks_withOtherwise() throws Exception {
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/spec-conditional-otherwise.spec").getFile());
        
        List<PageSection> sections = pageSpec.getSections();
        assertThat(sections.size(), is(1));
        
        PageSection pageSection = sections.get(0);
        List<ConditionalBlock> blocks = pageSection.getConditionalBlocks();
        assertThat(blocks.size(), is(1));
        
        ConditionalBlock block = blocks.get(0);
        List<ConditionalBlockStatement> statements = block.getStatements();
        assertThat(statements.size(), is(1));
        
        {
            ConditionalBlockStatement statement = statements.get(0);
            assertThat(statement.isInverted(), is(false));
            List<ObjectSpecs> statementObjects = statement.getObjects();
            assertThat(statementObjects.size(), is(1));
            assertThat(statementObjects.get(0).getObjectName(), is("button-1"));
            assertThat(statementObjects.get(0).getSpecs().get(0).getOriginalText(), is("width: > 100px"));
        }
        
        List<ObjectSpecs> bodyObjects = block.getBodyObjects();
        assertThat(bodyObjects.size(), is(1));
        assertThat(bodyObjects.get(0).getObjectName(), is("button-1"));
        assertThat(bodyObjects.get(0).getSpecs().get(0).getOriginalText(), is("text is: sample text"));
        
        
        List<ObjectSpecs> otherwiseObjects = block.getOtherwiseObjects();
        assertThat(otherwiseObjects.size(), is(1));
        assertThat(otherwiseObjects.get(0).getObjectName(), is("button-2"));
        assertThat(otherwiseObjects.get(0).getSpecs().get(0).getOriginalText(), is("text ends: something"));
    }
    
    
    @Test
    public void shouldParse_componentSpecs() throws Exception {
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/components/spec-for-component-test-main.spec").getFile());
        List<PageSection> sections = pageSpec.getSections();
        assertThat(sections.size(), is(1));
        
        PageSection pageSection = sections.get(0);
        
        List<ObjectSpecs> objects = pageSection.getObjects();
        assertThat(objects.size(), is(3));
        
        assertThat(objects.get(0).getObjectName(), is("user-profile-1"));
        assertThat(objects.get(1).getObjectName(), is("user-profile-2"));
        assertThat(objects.get(2).getObjectName(), is("user-profile-3"));
        
        assertChildComponentSpec(objects.get(0).getSpecs());
        assertChildComponentSpec(objects.get(1).getSpecs());
        assertChildComponentSpec(objects.get(2).getSpecs());
    }
    
    
    @Test
    public void shouldParse_variablesDefinitions() throws Exception {
        
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/spec-variables.spec").getFile());
        List<PageSection> sections = pageSpec.getSections();
        assertThat(sections.size(), is(1));
        
        PageSection pageSection = sections.get(0);
        
        List<ObjectSpecs> objects = pageSection.getObjects();
        assertThat(objects.size(), is(2));
        
        
        ObjectSpecs headerObject = objects.get(0);
        ObjectSpecs containerObject = objects.get(1);
        
        
        assertThat(headerObject.getSpecs().get(0).getOriginalText(), is("text is: Hi, welcome"));
        assertThat(headerObject.getSpecs().get(1).getOriginalText(), is("inside: screen 10 to 20px top"));
        assertThat(containerObject.getSpecs().get(0).getOriginalText(), is("below: header ~ 10px"));
        
    }

    @Test
    public void shouldParse_warningLevels_forSpecs() throws IOException {
        PageSpec pageSpec = pageSpecReader.read(getClass().getResource("/specs/spec-warning-level.spec").getFile());
        List<PageSection> sections = pageSpec.getSections();
        assertThat(sections.size(), is(1));
        PageSection pageSection = sections.get(0);

        List<ObjectSpecs> objects = pageSection.getObjects();
        assertThat(objects.size(), is(3));

        {
            List<Spec> specs = objects.get(0).getSpecs();
            assertThat(specs.size(), is(2));

            Spec spec1 = specs.get(0);
            Spec spec2 = specs.get(1);

            assertThat(spec1.getOriginalText(), is("text is: Login"));
            assertThat(spec1.isOnlyWarn(), is(true));

            assertThat(spec2.getOriginalText(), is("width: 100px"));
            assertThat(spec2.isOnlyWarn(), is(false));
        }

        {
            List<Spec> specs = objects.get(1).getSpecs();
            assertThat(specs.size(), is(2));

            Spec spec1 = specs.get(0);
            Spec spec2 = specs.get(1);

            assertThat(spec1.getOriginalText(), is("width: 150px"));
            assertThat(spec1.isOnlyWarn(), is(true));

            assertThat(spec2.getOriginalText(), is("height: 50px"));
            assertThat(spec2.isOnlyWarn(), is(false));
        }
    }

    @Test
    public void shouldAllowTo_importJavascriptFiles() throws IOException {
        PageSpecReader specReader = new PageSpecReader(new Properties(), EMPTY_PAGE);
        PageSpec pageSpec = specReader.read(getClass().getResource("/specs/spec-with-javascript-import.spec").getFile());
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("text is: some value from-javascript function"));
    }

    @Test
    public void shouldAllowToUse_pageObjectProperties_inJsExpressions_via_findFunction() throws IOException {
        WebDriver driver = new MockedDriver();
        driver.get("/mocks/pages/object-values-in-js.json");
        PageSpecReader specReader = new PageSpecReader(new Properties(), new SeleniumBrowser(driver).getPage());
        PageSpec pageSpec = specReader.read(getClass().getResource("/specs/spec-with-object-values-in-js.spec").getFile());
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("near: menu-item-4 20px left"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(1).getOriginalText(), is("above: menu-item-4 30px"));
    }

    /** Discovered a bug in version 1.4.3
     * There was a bug that js function 'find' couldn't find all objects when the spec was invoked via import
     * @throws IOException
     */
    @Test
    public void shouldAllowToUse_pageObjectProperties_inJsExpressions_via_findFunction_withImportedSpecs() throws IOException {
        WebDriver driver = new MockedDriver();
        driver.get("/mocks/pages/object-values-in-js.json");
        PageSpecReader specReader = new PageSpecReader(new Properties(), new SeleniumBrowser(driver).getPage());
        PageSpec pageSpec = specReader.read(getClass().getResource("/specs/spec-with-object-values-in-js-via-import.spec").getFile());
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("near: menu-item-4 20px left"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(1).getOriginalText(), is("above: menu-item-4 30px"));
    }

    @Test
    public void shouldAllowTo_useParameterizationIndex_insideJsExpression() throws IOException {
        PageSpecReader specReader = new PageSpecReader(new Properties(), EMPTY_PAGE);
        PageSpec pageSpec = specReader.read(getClass().getResource("/specs/parameterization-index-in-js.spec").getFile());
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("above: menu-item-3"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(1).getSpecs().get(0).getOriginalText(), is("above: menu-item-5"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(2).getSpecs().get(0).getOriginalText(), is("above: menu-item-7"));
    }

    @Test
    public void shouldAllowTo_mixConditionalStatements_withParameterizations() throws IOException {
        PageSpecReader specReader = new PageSpecReader(new Properties(), EMPTY_PAGE);
        PageSpec pageSpec = specReader.read(getClass().getResource("/specs/mix-conditions-with-parameterizations.spec").getFile());
        ConditionalBlock conditionalBlock = pageSpec.getSections().get(0).getConditionalBlocks().get(0);
        assertThat(conditionalBlock.getBodyObjects().size(), is(3));
        assertThat(conditionalBlock.getBodyObjects().get(0).getObjectName(), is("banner-1"));
        assertThat(conditionalBlock.getBodyObjects().get(1).getObjectName(), is("banner-2"));
        assertThat(conditionalBlock.getBodyObjects().get(2).getObjectName(), is("banner-3"));
    }


    @Test
    public void shouldImport_sameJsScript_andSameSpec_onlyOnes_withinDifferentLevels_ofSpecs() throws IOException {
        JsTestRegistry.get().clear();
        PageSpec pageSpec = readSpec("/specs/same-import/main.spec");
        assertThat("Amount of events should be", JsTestRegistry.get().getEvents().size(), is(1));
        assertThat("Events should be", JsTestRegistry.get().getEvents(), contains("script is loaded"));

        assertThat("Amout of sections should be", pageSpec.getSections().size(), is(3));

        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getObjectName(), is("main-item"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("text is: name from script"));

        assertThat(pageSpec.getSections().get(1).getObjects().get(0).getObjectName(), is("common-object"));
        assertThat(pageSpec.getSections().get(1).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("width: 100px"));

        assertThat(pageSpec.getSections().get(2).getObjects().get(0).getObjectName(), is("sub-item"));
        assertThat(pageSpec.getSections().get(2).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("text is: name from script"));

    }

    private PageSpec readSpec(String path) throws IOException {
        PageSpecReader specReader = new PageSpecReader(new Properties(), EMPTY_PAGE);
        return specReader.read(getClass().getResource(path).getFile());
    }

    private void assertChildComponentSpec(List<Spec> specs) {
        assertThat(specs.size(), is(1));
        SpecComponent spec = (SpecComponent) specs.get(0);
        assertThat(spec.getOriginalText(), is("component: spec-for-component-test-component.spec"));
        
        assertThat(spec.getSpecPath(), endsWith("spec-for-component-test-component.spec"));
             
    }

    private FileSyntaxException expectExceptionFromReading(String file) throws IOException {
        try {
            pageSpecReader.read(getClass().getResource(file).getFile());
        }
        catch(FileSyntaxException exception) {
            return exception;
        }
        throw new RuntimeException("Expected exception was not caught when reading page spec: " + file);
    }  
}

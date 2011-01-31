/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.example.seambay.test.selenium;

import static org.testng.AssertJUnit.assertTrue;

import org.jboss.seam.example.common.test.selenium.SeamSelenium;
import org.testng.annotations.Test;

/**
 * This class tests a functionality of web service test page available directly
 * from SeamBay home page
 * 
 * @author Martin Gencur
 * @author kpiwko
 * 
 */
public class WebServiceTestPageTest extends SeleniumSeamBayTest
{
   protected static final String HERE_LINK = "xpath=//a[contains(text(),'here')]";
   protected static final String SERVICE_PAGE_HEADING = "seamBay Web Services - Test Page";

   protected static final String INVOKE_SERVICE_BUTTON = "xpath=//button[contains(@onclick,'sendRequest')]";
   protected static final String REQUEST_AREA = "id=serviceRequest";
   protected static final String RESPONSE_AREA = "id=serviceResponse";

   protected static final String LOGIN_LINK = "xpath=//a[contains(text(),'Login')]";
   protected static final String LIST_CATEGORIES_LINK = "xpath=//a[contains(text(),'List Categories')]";
   protected static final String CREATE_NEW_AUCTION_LINK = "xpath=//a[contains(text(),'Create new auction')]";
   protected static final String UPDATE_AUCTION_DETAILS_LINK = "xpath=//a[contains(text(),'Update auction details')]";
   protected static final String SET_AUCTION_DURATION_LINK = "xpath=//a[contains(text(),'Set auction duration')]";
   protected static final String SET_STARTING_PRICE_LINK = "xpath=//a[contains(text(),'Set starting price')]";
   protected static final String GET_AUCTION_DETAILS_LINK = "xpath=//a[contains(text(),'Get the auction details')]";
   protected static final String CONFIRM_AUCTION_LINK = "xpath=//a[contains(text(),'Confirm auction')]";
   protected static final String FIND_AUCTIONS_LINK = "xpath=//a[contains(text(),'Find Auctions')]";
   protected static final String LOGOUT_LINK = "xpath=//a[contains(text(),'Logout')]";

   /* login parameters */
   protected static final String LOGIN_INPUT_USERNAME = "id=username";
   protected static final String LOGIN_INPUT_PASSWORD = "id=password";

   /* create new auction parameters */
   protected static final String AUCTION_TITLE = "id=title";
   protected static final String AUCTION_DESCRIPTION = "id=description";
   protected static final String AUCTION_CATEGORY_ID = "id=categoryId";

   /* parameters for other tests */
   protected static final String SEARCH_TERM = "id=searchTerm";
   protected static final String AUCTION_DURATION = "id=duration";
   protected static final String STARTING_PRICE = "id=price";

   /**
    * Retrieves value of given element in Selenium. Since Selenium specification
    * differs from HTML standards, e.g. text within {@code
    * <textarea></textarea>} blocks must be retrieved with {@code getValue()},
    * this is a preffered way to get value of an element within DOM tree
    * 
    * @author kpiwko
    * 
    */
   protected static enum SeleniumValueRetriever
   {
      TEXTAREA("Value"), OTHER("Text");

      private String sa;

      private SeleniumValueRetriever(String seleniumAttribute)
      {
         this.sa = seleniumAttribute;
      }

      /**
       * Returns JavaScript selenium function, which can retrieve a value under
       * given locator
       * 
       * @param locator Selenium locator
       * @return Function call to be used in waitForCondition()
       */
      public String getSeleniumValueFunction(String locator)
      {
         return String.format("selenium.get%s(\"%s\")", sa, locator);
      }

   }

   /**
    * Modifies AJAX waits to match behavior of asynchronous services
    * 
    * @return Modified SeamSelenium browser
    */
   @Override
   public SeamSelenium startBrowser()
   {
      SeamSelenium newBrowser = new SeamSelenium(HOST, PORT, BROWSER, BROWSER_URL)
      {
         /**
          * Wait until service says it is executing a request
          */
         @Override
         public void waitForAJAXUpdate(String timeout)
         {
            waitForCondition("selenium.browserbot.getCurrentWindow().selectedService.active===false", timeout);
            waitForElementContent(SeleniumValueRetriever.TEXTAREA, RESPONSE_AREA, "</env:Envelope>", timeout);
         };

         /**
          * Check typed result in the request field
          */
         @Override
         public void type(String locator, String value)
         {
            super.type(locator, value);
            waitForElementContent(SeleniumValueRetriever.TEXTAREA, REQUEST_AREA, value, TIMEOUT);
         }
      };
      newBrowser.start();
      newBrowser.allowNativeXpath("false");
      newBrowser.setSpeed(SPEED);
      newBrowser.setTimeout(TIMEOUT);
      return newBrowser;
   }

   @Test
   public void simplePageContentTest()
   {
      browser.clickAndWait(HERE_LINK);
      waitForElementPresent(RESPONSE_AREA, TIMEOUT);
      assertTrue("Page should contain service page heading", browser.isTextPresent(SERVICE_PAGE_HEADING));
   }

   @Test(dependsOnMethods = { "simplePageContentTest" })
   public void loginTest()
   {
      loginService();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain \"true\"", x.contains(getProperty("LOGIN_RIGHT_RESPONSE")));
   }

   public void loginService()
   {
      String username = "demo";
      String password = "demo";
      browser.clickAndWait(HERE_LINK);
      browser.click(LOGIN_LINK);
      waitForElementPresent(LOGIN_INPUT_PASSWORD, TIMEOUT);
      browser.type(LOGIN_INPUT_USERNAME, username);
      // this wait is needed because we can't distinguish username from password
      waitForElementContent(SeleniumValueRetriever.TEXTAREA, REQUEST_AREA, "<arg0>" + username + "</arg0>", TIMEOUT);
      browser.type(LOGIN_INPUT_PASSWORD, password);
      waitForElementContent(SeleniumValueRetriever.TEXTAREA, REQUEST_AREA, "<arg1>" + password + "</arg1>", TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
   }

   @Test(dependsOnMethods = { "loginTest" })
   public void listCategoriesTest()
   {
      loginService();
      waitForElementPresent(LIST_CATEGORIES_LINK, TIMEOUT);
      browser.click(LIST_CATEGORIES_LINK);
      waitForElementPresent(INVOKE_SERVICE_BUTTON, TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain a list of categories.", x.contains(getProperty("LIST_CATEGORIES_RESPONSE")));
   }

   @Test(dependsOnMethods = { "loginTest" })
   public void createNewAuctionTest()
   {
      loginService();
      createNewAuctionService();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain information about creating the auction.", x.contains(getProperty("CREATE_NEW_AUCTION_RESPONSE")));
   }

   public void createNewAuctionService()
   {
      String title = "Animals";
      String description = "You can buy an animal here";
      String categoryId = "6";
      waitForElementPresent(CREATE_NEW_AUCTION_LINK, TIMEOUT);
      browser.click(CREATE_NEW_AUCTION_LINK);
      waitForElementPresent(AUCTION_TITLE, TIMEOUT);
      waitForElementPresent(AUCTION_DESCRIPTION, TIMEOUT);
      waitForElementPresent(AUCTION_CATEGORY_ID, TIMEOUT);
      browser.type(AUCTION_TITLE, title);
      browser.type(AUCTION_DESCRIPTION, description);
      browser.type(AUCTION_CATEGORY_ID, categoryId);
      waitForElementPresent(INVOKE_SERVICE_BUTTON, TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
   }

   @Test(dependsOnMethods = { "loginTest", "createNewAuctionTest" })
   public void findAuctionsTest()
   {
      String searchTerm = "Animals";
      loginService();
      createNewAuctionService();
      confirmAuctionService();
      waitForElementPresent(FIND_AUCTIONS_LINK, TIMEOUT);
      browser.click(FIND_AUCTIONS_LINK);
      waitForElementPresent(SEARCH_TERM, TIMEOUT);
      browser.type(SEARCH_TERM, searchTerm);
      waitForElementPresent(INVOKE_SERVICE_BUTTON, TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain information about finding auction.", x.contains(getProperty("FIND_AUCTIONS_RESPONSE")));
   }

   @Test(dependsOnMethods = { "loginTest", "createNewAuctionTest" })
   public void updateAuctionTest()
   {
      String title = "Animals";
      String description = "Another description";
      String categoryId = "5";
      loginService();
      createNewAuctionService();
      waitForElementPresent(UPDATE_AUCTION_DETAILS_LINK, TIMEOUT);
      browser.click(UPDATE_AUCTION_DETAILS_LINK);
      waitForElementPresent(AUCTION_TITLE, TIMEOUT);
      waitForElementPresent(AUCTION_DESCRIPTION, TIMEOUT);
      waitForElementPresent(AUCTION_CATEGORY_ID, TIMEOUT);
      browser.type(AUCTION_TITLE, title);
      browser.type(AUCTION_DESCRIPTION, description);
      browser.type(AUCTION_CATEGORY_ID, categoryId);
      waitForElementPresent(INVOKE_SERVICE_BUTTON, TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain information about updating the auction.", x.contains(getProperty("UPDATE_AUCTION_RESPONSE")));
   }

   @Test(dependsOnMethods = { "loginTest", "createNewAuctionTest" })
   public void setAuctionDurationTest()
   {
      String duration = "20";
      loginService();
      createNewAuctionService();
      waitForElementPresent(SET_AUCTION_DURATION_LINK, TIMEOUT);
      browser.click(SET_AUCTION_DURATION_LINK);
      waitForElementPresent(AUCTION_DURATION, TIMEOUT);
      browser.type(AUCTION_DURATION, duration);
      waitForElementPresent(INVOKE_SERVICE_BUTTON, TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain information about setting duration.", x.contains(getProperty("SET_DURATION_RESPONSE")));
   }

   @Test(dependsOnMethods = { "loginTest", "createNewAuctionTest" })
   public void setStartingPriceTest()
   {
      String price = "1000";
      loginService();
      createNewAuctionService();
      waitForElementPresent(SET_STARTING_PRICE_LINK, TIMEOUT);
      browser.click(SET_STARTING_PRICE_LINK);
      waitForElementPresent(STARTING_PRICE, TIMEOUT);
      browser.type(STARTING_PRICE, price);
      waitForElementPresent(INVOKE_SERVICE_BUTTON, TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain information about setting starting price.", x.contains(getProperty("SET_STARTING_PRICE_RESPONSE")));
   }

   @Test(dependsOnMethods = { "loginTest", "createNewAuctionTest" })
   public void getAuctionDetailsTest()
   {
      loginService();
      createNewAuctionService();
      waitForElementPresent(GET_AUCTION_DETAILS_LINK, TIMEOUT);
      browser.click(GET_AUCTION_DETAILS_LINK);
      waitForElementPresent(INVOKE_SERVICE_BUTTON, TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain auction details.", x.contains(getProperty("AUCTION_DETAILS_PRICE_RESPONSE")));
   }

   @Test(dependsOnMethods = { "loginTest" })
   public void logOutTest()
   {
      loginService();
      waitForElementPresent(LOGOUT_LINK, TIMEOUT);
      browser.click(LOGOUT_LINK);
      waitForElementPresent(INVOKE_SERVICE_BUTTON, TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain logout confirmation.", x.contains(getProperty("LOGOUT_RESPONSE")));
   }

   @Test(dependsOnMethods = { "loginTest", "createNewAuctionTest" })
   public void confirmAuctionTest()
   {
      loginService();
      createNewAuctionService();
      confirmAuctionService();
      String x = browser.getValue(RESPONSE_AREA);
      assertTrue("Response area should contain information about confirmation.", x.contains(getProperty("CONFIRMATION_RESPONSE")));
   }

   public void confirmAuctionService()
   {
      waitForElementPresent(CONFIRM_AUCTION_LINK, TIMEOUT);
      browser.click(CONFIRM_AUCTION_LINK);
      waitForElementPresent(INVOKE_SERVICE_BUTTON, TIMEOUT);
      browser.click(INVOKE_SERVICE_BUTTON);
      browser.waitForAJAXUpdate();
   }

   protected void waitForElementContent(final SeleniumValueRetriever type, final String locator, final String content, String timeout)
   {
      browser.waitForCondition(String.format("var value = %s; value.indexOf(\"%s\");", type.getSeleniumValueFunction(locator), content), timeout);
   }

   protected void waitForElementPresent(final String locator, String timeout)
   {
      browser.waitForCondition(String.format("selenium.isElementPresent(\"%s\")", locator), timeout);
   }

}

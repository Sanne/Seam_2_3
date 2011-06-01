package org.jboss.seam.example.restbay.test;

import static org.testng.Assert.assertEquals;

import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import static org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import static org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * 
 * @author Jozef Hartinger
 * 
 */
public class ResourceQueryTest extends SeamTest
{

   @DataProvider(name = "queryPaths")
   public Object[][] getData()
   {
      String[][] data = new String[2][1];
      data[0][0] = "/configuredCategory";
      data[1][0] = "/extendedCategory";
      return data;
   }

   @Test(dataProvider = "queryPaths")
   public void testResourceQuery(String path) throws Exception
   {
      new ResourceRequest(new ResourceRequestEnvironment(this), Method.GET, "/restv1" + path)
      {

         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            super.prepareRequest(request);
            request.addHeader("Accept", "application/xml");
            request.setQueryString("start=2&show=2");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><collection><category><categoryId>3</categoryId><name>Books</name></category><category><categoryId>4</categoryId><name>Cameras and Photography</name></category></collection>";
            assertEquals(response.getContentAsString(), expectedResponse, "Unexpected response.");
         }

      }.run();
   }

}

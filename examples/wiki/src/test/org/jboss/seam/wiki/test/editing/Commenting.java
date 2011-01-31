/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.wiki.test.editing;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.jboss.seam.wiki.core.action.CommentHome;
import org.jboss.seam.wiki.core.action.CommentQuery;
import org.jboss.seam.wiki.core.dao.WikiNodeDAO;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class Commenting extends DBUnitSeamTest {
   
    public static final String MEMBER_USERNAME = "member";
    public static final String MEMBER_PASSWORD = "member";

    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
            new DataSetOperation("org/jboss/seam/wiki/test/WikiBaseData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

    @Test
    public void postComment() throws Exception {

        loginMember();
       
        new FacesRequest("/docDisplay_d.xhtml") {

            protected void beforeRequest() {
                setParameter("documentId", "6");
            }

            protected void updateModelValues() throws Exception {
                assert getValue("#{documentHome.instance.id}").equals(6l);
            }

            protected void invokeApplication() throws Exception {

                CommentHome commentHome = (CommentHome)getInstance(CommentHome.class);

                commentHome.newComment();

                commentHome.getInstance().setFromUserName("Foo");
                commentHome.getInstance().setFromUserHomepage("http://foo.bar");
                commentHome.getInstance().setFromUserEmail("foo@bar.tld");
                commentHome.getInstance().setSubject("Some Subject");
                commentHome.getTextEditor().setValue("Some Content");

                invokeMethod("#{commentHome.persist}");

                CommentQuery commentQuery = (CommentQuery)getInstance(CommentQuery.class);
                assert commentQuery.getComments().size() == 7;

                assert commentQuery.getComments().get(0).getId().equals(10l);
                assert commentQuery.getComments().get(1).getId().equals(11l);
                assert commentQuery.getComments().get(2).getId().equals(12l);
                assert commentQuery.getComments().get(3).getId().equals(13l);
                assert commentQuery.getComments().get(4).getId().equals(14l);
                assert commentQuery.getComments().get(5).getId().equals(15l);

                assert commentQuery.getComments().get(6).getCreatedBy().getUsername().equals(MEMBER_USERNAME);
                assert commentQuery.getComments().get(6).getFromUserName().equals("Foo");
                assert commentQuery.getComments().get(6).getFromUserHomepage().equals("http://foo.bar");
                assert commentQuery.getComments().get(6).getFromUserEmail().equals("foo@bar.tld");
                assert commentQuery.getComments().get(6).getSubject().equals("Some Subject");
                assertEquals(commentQuery.getComments().get(6).getContent(), "Some Content");

                assert commentQuery.getComments().get(6).getName().matches("One\\.Comment[0-9]+");
                assert !commentQuery.getComments().get(6).getWikiname().contains(" ");
            }

        }.run();
    }

    @Test
    public void replyToComment() throws Exception {

        loginMember();
       
        new FacesRequest("/docDisplay_d.xhtml") {

            protected void beforeRequest() {
                setParameter("documentId", "6");
                setParameter("parentCommentId", "15");
            }

            protected void updateModelValues() throws Exception {
                assert getValue("#{documentHome.instance.id}").equals(6l);
            }

            protected void invokeApplication() throws Exception {

                CommentHome commentHome = (CommentHome)getInstance(CommentHome.class);

                commentHome.replyTo();

                commentHome.getInstance().setFromUserName("Foo");
                commentHome.getInstance().setFromUserHomepage("http://foo.bar");
                commentHome.getInstance().setFromUserEmail("foo@bar.tld");
                commentHome.getInstance().setSubject("Some Subject");
                commentHome.getTextEditor().setValue("Some Content");

                invokeMethod("#{commentHome.persist}");

                CommentQuery commentQuery = (CommentQuery)getInstance(CommentQuery.class);
                assert commentQuery.getComments().size() == 7;

                assert commentQuery.getComments().get(0).getId().equals(10l);
                assert commentQuery.getComments().get(1).getId().equals(11l);
                assert commentQuery.getComments().get(2).getId().equals(12l);
                assert commentQuery.getComments().get(3).getId().equals(13l);
                assert commentQuery.getComments().get(4).getId().equals(14l);
                assert commentQuery.getComments().get(5).getId().equals(15l);

                assert commentQuery.getComments().get(6).getCreatedBy().getUsername().equals(MEMBER_USERNAME);
                assert commentQuery.getComments().get(6).getFromUserName().equals("Foo");
                assert commentQuery.getComments().get(6).getFromUserHomepage().equals("http://foo.bar");
                assert commentQuery.getComments().get(6).getFromUserEmail().equals("foo@bar.tld");
                assertEquals(commentQuery.getComments().get(6).getSubject(), "Some Subject");
                assertEquals(commentQuery.getComments().get(6).getContent(), "Some Content");
                assertEquals(commentQuery.getComments().get(6).getParent().getId(), new Long(6));

                assert commentQuery.getComments().get(6).getName().matches("One\\.Comment[0-9]+");
                assert !commentQuery.getComments().get(6).getWikiname().contains(" ");
            }
            
        }.run();
    }

    @Test
    public void deleteComment() throws Exception {

        loginAdmin();

        new FacesRequest("/docDisplay_d.xhtml") {

            protected void beforeRequest() {
                setParameter("documentId", "6");
            }

            protected void updateModelValues() throws Exception {
                assert getValue("#{documentHome.instance.id}").equals(6l);
            }

            protected void invokeApplication() throws Exception {
                invokeMethod("#{commentHome.remove(14)}");

                CommentQuery commentQuery = (CommentQuery)getInstance(CommentQuery.class);
                assert commentQuery.getComments().size() == 5;
                assert commentQuery.getComments().get(0).getId().equals(10l);
                assert commentQuery.getComments().get(1).getId().equals(11l);
                assert commentQuery.getComments().get(2).getId().equals(12l);
                assert commentQuery.getComments().get(3).getId().equals(13l);
                assert commentQuery.getComments().get(4).getId().equals(15l);
            }

        }.run();
    }
    
    @Test
    public void rateComment() throws Exception {

        loginMember();

        new FacesRequest("/docDisplay_d.xhtml") {

            protected void beforeRequest() {
                setParameter("documentId", "6");
            }

            protected void updateModelValues() throws Exception {
                assert getValue("#{documentHome.instance.id}").equals(6l);
            }

            protected void invokeApplication() throws Exception {
                invokeMethod("#{commentHome.rate(12, 4)}");
            }

        }.run();

        new NonFacesRequest("/docDisplay_d.xhtml") {

            protected void beforeRequest() {
                setParameter("documentId", "6");
            }

            protected void renderResponse() throws Exception {
                WikiNodeDAO dao = (WikiNodeDAO)getInstance(WikiNodeDAO.class);
                assert dao.findWikiNode(12l).getRating() == 4;
            }

        }.run();

    }

    private void loginAdmin() throws Exception {
        new FacesRequest() {
           protected void invokeApplication() throws Exception {
              setValue("#{identity.username}", "admin");
              setValue("#{identity.password}", "admin");
              invokeAction("#{identity.login}");
              assert getValue("#{identity.loggedIn}").equals(true);
           }
        }.run();
    }

    private void loginMember() throws Exception {
        new FacesRequest() {
           protected void invokeApplication() throws Exception {
              setValue("#{identity.username}", MEMBER_USERNAME);
              setValue("#{identity.password}", MEMBER_PASSWORD);
              invokeAction("#{identity.login}");
              assert getValue("#{identity.loggedIn}").equals(true);
           }
        }.run();
    }

}
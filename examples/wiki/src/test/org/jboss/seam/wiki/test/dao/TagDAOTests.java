/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.wiki.test.dao;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.wiki.core.dao.TagDAO;
import org.jboss.seam.wiki.core.dao.WikiNodeDAO;
import org.jboss.seam.wiki.core.model.WikiDirectory;
import org.jboss.seam.wiki.core.model.DisplayTagCount;
import org.jboss.seam.wiki.core.model.WikiFile;
import org.jboss.seam.wiki.core.model.WikiNode;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.util.List;

public class TagDAOTests extends DBUnitSeamTest {

    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(
            new DataSetOperation("org/jboss/seam/wiki/test/WikiBaseData.dbunit.xml", DatabaseOperation.CLEAN_INSERT)
        );
    }

    @Test
    public void countTags() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiDirectory wikiRoot = (WikiDirectory)getInstance("wikiRoot");

                TagDAO dao = (TagDAO)getInstance(TagDAO.class);
                List<DisplayTagCount> tags = dao.findTagCounts(wikiRoot, null, 0, 1l);
                assert tags.size() == 3;
                assert tags.get(0).getTag().equals("Tag One");
                assert tags.get(0).getCount().equals(3l);
                assert tags.get(1).getTag().equals("Tag Two");
                assert tags.get(1).getCount().equals(2l);
                assert tags.get(2).getTag().equals("Tag Three");
                assert tags.get(2).getCount().equals(1l);

            }
        }.run();
    }

    @Test
    public void countTagsLimit() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiDirectory startDir = ((WikiNodeDAO)getInstance(WikiNodeDAO.class)).findWikiDirectory(4l);

                TagDAO dao = (TagDAO)getInstance(TagDAO.class);
                List<DisplayTagCount> tags = dao.findTagCounts(startDir, null, 2, 1l);

                assert tags.size() == 2;
                assert tags.get(0).getTag().equals("Tag One");
                assert tags.get(0).getCount().equals(1l);
                assert tags.get(1).getTag().equals("Tag Three");
                assert tags.get(1).getCount().equals(1l);

            }
        }.run();
    }

    @Test
    public void findTaggedFiles() throws Exception {
        new FacesRequest() {

            protected void invokeApplication() throws Exception {
                WikiDirectory startDir = ((WikiNodeDAO)getInstance(WikiNodeDAO.class)).findWikiDirectory(3l);

                TagDAO dao = (TagDAO)getInstance(TagDAO.class);
                List<WikiFile> taggedFiles = dao.findWikFiles(startDir, null, "Tag One", WikiNode.SortableProperty.name, true);

                Assert.assertEquals(taggedFiles.size(), 3);

                Assert.assertEquals(taggedFiles.get(0).getName(), "One");
                Assert.assertEquals(taggedFiles.get(1).getName(), "Three");
                Assert.assertEquals(taggedFiles.get(2).getName(), "Two");
            }
        }.run();
    }

}

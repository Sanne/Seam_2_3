//$Id$
package com.jboss.dvd.seam;

import java.util.Date;
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.FetchMode;
import org.hibernate.search.FullTextSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

/**
 * Re index the needed entities.
 * (Old version: see IndexerAction implementation in the
 * jboss6 additional source folder to see usage of new Search API)
 *
 * @author Emmanuel Bernard
 */
@Name("indexer")
@Stateful
@Scope(ScopeType.APPLICATION)
@Startup
@Deprecated
public class IndexerAction implements Indexer
{
   private Date lastIndexingTime;
   @PersistenceContext
   private EntityManager em;

   public Date getLastIndexingTime()
   {
      return lastIndexingTime;
   }

   @Create
   public void index()
   {
      indexAllClasses(Actor.class, Category.class);
      indexProducts();
      lastIndexingTime = new Date();
   }

   @SuppressWarnings("unchecked")
   private void indexProducts()
   {
       FullTextSession fullTextSession = getFullTextSession();
       List results = fullTextSession.createCriteria(Product.class)
            .setFetchMode("actors", FetchMode.JOIN)
            .setFetchMode("categories", FetchMode.JOIN)
            .list();
      for (Object obj : results)
      {
         fullTextSession.index(obj);
      }
   }

   private FullTextSession getFullTextSession()
   {
      return (FullTextSession) em.getDelegate();
   }

   @SuppressWarnings("unchecked")
   private void indexAllClasses(Class... entityTypes)
   {
      FullTextSession fullTextSession = getFullTextSession();
      for (Class entityType : entityTypes)
      {
         for (Object obj : fullTextSession.createCriteria(entityType).list())
         {
            fullTextSession.index(obj);
         }
      }
   }

   @Remove
   @Destroy
   public void stop() {}
   
}

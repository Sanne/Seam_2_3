//$Id$
package com.jboss.dvd.seam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.FullTextEntityManager;

/**
 * Hibernate Search version of the store querying mechanism.
 * (Old version: see FullTextSearchAction implementation in the
 * jboss6 additional source folder to see usage of new Search API)
 * 
 * @author Emmanuel Bernard
 */
@Deprecated
@Stateful
@Name("search")
public class FullTextSearchAction
    implements FullTextSearch,
               Serializable
{
    static final long serialVersionUID = -6536629890251170098L;
    
    @In(create=true)
    ShoppingCart cart;
    
    @PersistenceContext
    EntityManager em;

    //@RequestParameter
    Long id;

    int pageSize = 15;
    int currentPage = 0;
    boolean hasMore = false;
    int numberOfResults;
    
    String searchQuery;

    @DataModel
    List<Product> searchResults;

    //@DataModelSelection
    Product selectedProduct;

    @Out(required = false)
    Product dvd;

    @Out(scope=ScopeType.CONVERSATION, required=false)
    Map<Product, Boolean> searchSelections;


    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    
    public int getNumberOfResults() {
        return numberOfResults;
    }
    
    @Begin(join = true)
    public String doSearch() {
        currentPage = 0;
        updateResults();
        
        return "browse";
    }
    
    public void nextPage() {
        if (!isLastPage()) {
            currentPage++;
            updateResults();
        }
    }

    public void prevPage() {
        if (!isFirstPage()) {
            currentPage--;
            updateResults();
        }
    }
    
    @Begin(join = true)
    public void selectFromRequest() {
        if (id != null)  {
            dvd = em.find(Product.class, id);
        } else if (selectedProduct != null) {
            dvd = selectedProduct;
        }
    }

    public boolean isLastPage() {
        return ( searchResults != null ) && !hasMore;
    }

    public boolean isFirstPage() {
        return ( searchResults != null ) && ( currentPage == 0 );
    }

    @SuppressWarnings("unchecked")
    private void updateResults() {
        FullTextQuery query;
        try {
            query = searchQuery(searchQuery);
        } catch (ParseException pe) { 
            return; 
        }
      
        List<Product> items = query
            .setMaxResults(pageSize + 1)
            .setFirstResult(pageSize * currentPage)
            .getResultList();
        numberOfResults = query.getResultSize();
        
        if (items.size() > pageSize) {
            searchResults = new ArrayList(items.subList(0, pageSize));
            hasMore = true;
        } else {
            searchResults = items;
            hasMore = false;
        }

        searchSelections = new HashMap<Product, Boolean>();
    }

    private FullTextQuery searchQuery(String searchQuery) throws ParseException
    {
        Map<String,Float> boostPerField = new HashMap<String,Float>();
        boostPerField.put("title", 4f);
        boostPerField.put("description", 2f);
        boostPerField.put("actors.name", 2f);
        boostPerField.put("categories.name", 0.5f);

        String[] productFields = {"title", "description", "actors.name", "categories.name"};
        QueryParser parser = new MultiFieldQueryParser(productFields, new StandardAnalyzer(), boostPerField);
        parser.setAllowLeadingWildcard(true);
        org.apache.lucene.search.Query luceneQuery;
        luceneQuery = parser.parse(searchQuery);
        return ( (FullTextEntityManager) em ).createFullTextQuery(luceneQuery, Product.class);
    }
    
    /**
     * Add the selected DVD to the cart
     */
    public void addToCart()
    {
        cart.addProduct(dvd, 1);
    }
    
    /**
     * Add many items to cart
     */
    public void addAllToCart()
    {
        for (Product item : searchResults) {
            Boolean selected = searchSelections.get(item);
            if (selected != null && selected) {
                searchSelections.put(item, false);
                cart.addProduct(item, 1);
            }
        }
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Long getSelectedId() {
        return id;
    }

    public void setSelectedId(Long id) {
        this.id = id;
    }
    
    @End
    public void reset() { }

    @Destroy
    @Remove
    public void destroy() { }
}

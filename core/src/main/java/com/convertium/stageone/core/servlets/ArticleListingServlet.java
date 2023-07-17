package com.convertium.stageone.core.servlets;

import com.convertium.stageone.core.api.models.Highlights;
import com.convertium.stageone.core.api.models.HighlightsModel;
import com.convertium.stageone.core.helpers.ContentFragmentArticle;
import com.convertium.stageone.core.utils.CommonUtility;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.google.gson.Gson;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Component(service = {Servlet.class})
@SlingServletResourceTypes(resourceTypes = "cq:Page",
        methods = HttpConstants.METHOD_GET, selectors = "articlelisting", extensions = "json")
@ServiceDescription("Article Form Submission Servlet")
public class ArticleListingServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ArticleListingServlet.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder builder;

    private ResourceResolver resolver;

    private static final String ASSETS_CONTENT_ROOT = "/content/dam/convertium-article-content-fragments/us/en";

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException, IOException {
        resolver = CommonUtility.getWriterResourcerResolver(resolverFactory);
        Session session = resolver.adaptTo(Session.class);
        List<ContentFragmentArticle> list = new ArrayList<>();
        Highlights highlights = new Highlights();
        try {
            Resource resource = resolver.getResource(ASSETS_CONTENT_ROOT);
            if (Objects.nonNull(resource)) {
                SearchResult result = CommonUtility.getQueryResults(ASSETS_CONTENT_ROOT, builder, session);
                list = CommonUtility.getContentFragmentArticles(resolver, result);
            }
            List<HighlightsModel> highlightsModelList = new ArrayList<>();
            if (list != null) {
                for (ContentFragmentArticle article : list) {
                    HighlightsModel highlightsModel = new HighlightsModel();
                    highlightsModel.setArticleTitle(article.getArticleTitle());
                    highlightsModel.setArticleContent(article.getArticleContent());
                    highlightsModel.setArticlePublishDate(article.getArticlePublishDate());
                    highlightsModelList.add(highlightsModel);
                }
            }
            highlights.setHighlightsModels(highlightsModelList);

        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if (null != resolver && resolver.isLive()) {
                resolver.close();
            }
        }
        Gson gson = new Gson();
        response.getWriter().println(gson.toJson(highlights));

    }


}




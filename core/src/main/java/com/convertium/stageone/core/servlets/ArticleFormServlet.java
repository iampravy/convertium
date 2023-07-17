package com.convertium.stageone.core.servlets;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.convertium.stageone.core.utils.CommonUtility;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

@Component(service = {Servlet.class})
@SlingServletResourceTypes(resourceTypes = "convertium/components/articleform",
        methods = HttpConstants.METHOD_POST, selectors = "articleform", extensions = "json")
@ServiceDescription("Article Form Submission Servlet")
public class ArticleFormServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ArticleFormServlet.class);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private SlingSettingsService slingSettingsService;

    private static final String ARTICLE_CONTENT_FRAGMENT_MODEL = "/conf/convertium/settings/dam/cfm/models/article-model";
    private static final String ASSETS_CONTENT_ROOT = "/content/dam/convertium-article-content-fragments/us/en";
    private ResourceResolver resolver;

    @Override
    protected void doPost(SlingHttpServletRequest request,
                          SlingHttpServletResponse response) throws ServletException, IOException {
        String articleTitle = request.getParameter("articleTitle");
        String articleContent = request.getParameter("articleContent");
        String articlePublishDate = request.getParameter("articlePublishDate");

        Resource currentResource = request.getResource();
        ValueMap properties = currentResource.adaptTo(ValueMap.class);
        resolver = CommonUtility.getWriterResourcerResolver(resolverFactory);
        Session session = resolver.adaptTo(Session.class);
        try {
            Resource resource = resolver.getResource(ASSETS_CONTENT_ROOT);
            if (Objects.nonNull(resource)) {
                Resource templateOrModelRes = resolver.getResource(ARTICLE_CONTENT_FRAGMENT_MODEL.concat("/").concat("jcr:content"));
                if (Objects.isNull(templateOrModelRes))
                    return;
                FragmentTemplate fragmentTemplate = templateOrModelRes.adaptTo(FragmentTemplate.class);
                String randomTimeStamp = System.currentTimeMillis() + "-" + ((int) Math.floor(Math.random() * 1000));
                String articleCFPath = resource.getPath() + "/article-" + randomTimeStamp;
                String articleCFDataMasterPath = articleCFPath + "/jcr:content/data/master";
                ContentFragment newFragment = fragmentTemplate.createFragment(resource, "article-" + randomTimeStamp, StringUtils.EMPTY);
                newFragment.setTitle(articleTitle);
                resolver.commit();
                Node cfDataNode = session.getNode(articleCFDataMasterPath);
                cfDataNode.setProperty("articleTitle", articleTitle);
                cfDataNode.setProperty("articleContent", articleContent);
                cfDataNode.setProperty("articlePublishDate", articlePublishDate);
                Node articleCFNode = session.getNode(articleCFPath + "/jcr:content");
                boolean isPublish = this.slingSettingsService.getRunModes().contains("publish");
                if (isPublish) {
                    articleCFNode.setProperty("cq:lastModified", Calendar.getInstance());
                    articleCFNode.setProperty("cq:lastModifiedBy", session.getUserID());
                    articleCFNode.setProperty("cq:distribute", true);
                }
            }
            // save the changes to the repository
            session.save();
        } catch (RepositoryException re) {
            log.error(re.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if (null != resolver && resolver.isLive()) {
                resolver.close();
            }
        }
    }


}



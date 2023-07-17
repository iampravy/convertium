package com.convertium.stageone.core.workflow;

import com.convertium.stageone.core.utils.CommonUtility;
import com.day.cq.replication.Replicator;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;

@Component(service = WorkflowProcess.class,
        property = {"process.label=Article Fragment Cleanup Workflow Process"})
public class ArticleFragmentProcess implements WorkflowProcess {
    private final Logger logger = LoggerFactory.getLogger(ArticleFragmentProcess.class);

    @Reference
    private ResourceResolverFactory resolverFactory;
    @Reference
    private Replicator replicator;

    public void execute(WorkItem workItem, WorkflowSession wfSession, MetaDataMap metaDataMap)
            throws WorkflowException {
        ResourceResolver resolver =null;
        try {
            String payloadPathJCR = workItem.getWorkflowData().getPayload().toString();
            resolver = CommonUtility.getWriterResourcerResolver(resolverFactory);
            Session session = resolver.adaptTo(Session.class);
            Node cfDataNode = session.getNode(payloadPathJCR);
            if(cfDataNode.hasProperty("cq:lastModified")) {
                cfDataNode.getProperty("cq:lastModified").remove();
            }
            if(cfDataNode.hasProperty("cq:lastModifiedBy")) {
                cfDataNode.getProperty("cq:lastModifiedBy").remove();
            }
            if(cfDataNode.hasProperty("cq:distribute")) {
                cfDataNode.getProperty("cq:distribute").remove();
            }
            String payloadPath = payloadPathJCR.replace("/jcr:content", "");
            session.save();

        } catch (Exception e) {
            logger.error("Article Fragment Process ", e);
        } finally {
            if(null != resolver && resolver.isLive()) {
                resolver.close();
            }
        }

    }
}
/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.wizard;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.*;
import com.haulmont.reports.ReportingBean;
import com.haulmont.reports.ReportingConfig;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.app.service.ReportService;
import com.haulmont.reports.entity.*;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import com.haulmont.reports.entity.wizard.RegionProperty;
import com.haulmont.reports.entity.wizard.ReportData;
import com.haulmont.reports.entity.wizard.ReportRegion;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.ManagedBean;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.Embeddable;
import java.util.*;

/**
 * @author artamonov
 * @version $Id$
 */
@ManagedBean(ReportingWizardApi.NAME)
public class ReportingWizardBean implements ReportingWizardApi {
    public static final String ROOT_BAND_DEFINITION_NAME = "Root";
    public static List<String> IGNORED_ENTITY_PROPERTIES = Arrays.asList("id", "createTs", "createdBy", "version", "updateTs", "updatedBy", "deleteTs", "deletedBy");
    @Inject
    protected Persistence persistence;
    @Inject
    protected Metadata metadata;
    @Inject
    protected ReportingBean reportingBean;
    protected Log log = LogFactory.getLog(ReportingBean.class);

    @Override
    public Report toReport(ReportData reportData, byte[] templateByteArray, boolean isTmp) {
        Report report = metadata.create(Report.class);
        report.setIsTmp(isTmp);
        report.setReportType(ReportType.SIMPLE);
        report.setGroup(reportData.getGroup());

        int reportInputParameterPos = 0;
        ReportInputParameter reportInputParameter = metadata.create(ReportInputParameter.class);
        reportInputParameter.setReport(report);
        reportInputParameter.setName(reportData.getEntityTreeRootNode().getLocalizedName());
        if (reportData.getIsTabulatedReport()) {
            reportInputParameter.setType(ParameterType.ENTITY_LIST);
        } else {
            reportInputParameter.setType(ParameterType.ENTITY);
        }
        reportInputParameter.setRequired(Boolean.TRUE);
        reportInputParameter.setAlias(reportData.getEntityTreeRootNode().getName());
        reportInputParameter.setEntityMetaClass(reportData.getEntityTreeRootNode().getWrappedMetaClass().getName());
        reportInputParameter.setPosition(reportInputParameterPos++);
        report.getInputParameters().add(reportInputParameter);


        View parameterView = createViewByReportRegions(reportData.getEntityTreeRootNode(), reportData.getReportRegions());
        BandDefinition rootReportBandDefinition = metadata.create(BandDefinition.class);
        rootReportBandDefinition.setPosition(0);
        rootReportBandDefinition.setName(ROOT_BAND_DEFINITION_NAME);
        Set<BandDefinition> bandDefinitions = new LinkedHashSet<>(reportData.getReportRegions().size() + 1); //plus rootBand
        bandDefinitions.add(rootReportBandDefinition);
        List<BandDefinition> childBands = new ArrayList<>();
        int bandDefPos = 0;
        Messages messages = AppBeans.get(Messages.NAME);
        for (ReportRegion reportRegion : reportData.getReportRegions()) {
            if (reportRegion.isTabulatedRegion() && (reportData.getOutputFileType() == ReportOutputType.XLSX ||
                    reportData.getOutputFileType() == ReportOutputType.XLS)) {
                BandDefinition headerBandDefinition = metadata.create(BandDefinition.class);
                headerBandDefinition.setParentBandDefinition(rootReportBandDefinition);
                headerBandDefinition.setOrientation(Orientation.HORIZONTAL);
                headerBandDefinition.setName(reportRegion.getNameForHeaderBand());
                headerBandDefinition.setPosition(bandDefPos++);
                headerBandDefinition.setReport(report);
                childBands.add(headerBandDefinition);
                bandDefinitions.add(headerBandDefinition);
            }
            BandDefinition bandDefinition = metadata.create(BandDefinition.class);
            bandDefinition.setParentBandDefinition(rootReportBandDefinition);
            bandDefinition.setOrientation(Orientation.HORIZONTAL);
            bandDefinition.setName(reportRegion.getNameForBand());
            bandDefinition.setPosition(bandDefPos++);
            bandDefinition.setReport(report);
            DataSet bandDataSet = metadata.create(DataSet.class);

            bandDataSet.setName(messages.getMessage(getClass(), "dataSet"));

            if (reportData.getIsTabulatedReport()) {
                bandDataSet.setType(DataSetType.MULTI);
                bandDataSet.setListEntitiesParamName(reportData.getEntityTreeRootNode().getName());
            } else {
                if (reportRegion.getIsTabulatedRegion()) {
                    bandDataSet.setType(DataSetType.MULTI);
                    bandDataSet.setListEntitiesParamName(reportInputParameter.getAlias() + "#" + reportRegion.getRegionPropertiesRootNode().getName());
                } else {
                    bandDataSet.setType(DataSetType.SINGLE);
                    bandDataSet.setEntityParamName(reportInputParameter.getAlias());
                }
            }
            bandDataSet.setView(parameterView);

            bandDataSet.setBandDefinition(bandDefinition);
            bandDefinition.getDataSets().add(bandDataSet);
            bandDefinitions.add(bandDefinition);
            childBands.add(bandDefinition);
        }
        rootReportBandDefinition.getChildrenBandDefinitions().addAll(childBands);

        report.setBands(bandDefinitions);

        ReportTemplate reportTemplate = metadata.create(ReportTemplate.class);
        reportTemplate.setReport(report);
        reportTemplate.setCode(ReportService.DEFAULT_TEMPLATE_CODE);

        reportTemplate.setName(reportData.getTemplateFileName());
        reportTemplate.setContent(templateByteArray);
        reportTemplate.setCustomFlag(Boolean.FALSE);
        Integer outputFileTypeId = reportData.getOutputFileType().getId();
        reportTemplate.setReportOutputType(ReportOutputType.fromId(outputFileTypeId));
        report.setDefaultTemplate(reportTemplate);
        report.setTemplates(Collections.singletonList(reportTemplate));

        Transaction t = persistence.createTransaction();
        try {
            report.setName(reportingBean.generateReportName(reportData.getName(), 0));
            String xml = reportingBean.convertToXml(report);
            report.setXml(xml);
            if (!isTmp) {
                EntityManager em = persistence.getEntityManager();
                em.persist(reportTemplate);
                em.persist(report);
                t.commit();
            }
        } finally {
            t.end();
        }

        return report;
    }

    @Override
    public View createViewByReportRegions(EntityTreeNode entityTreeRootNode, List<ReportRegion> reportRegions) {
        View view = new View(entityTreeRootNode.getWrappedMetaClass().getJavaClass());

        Set<String> allRegionProps = new HashSet<>();
        for (ReportRegion reportRegion : reportRegions) {
            for (RegionProperty regionProperty : reportRegion.getRegionProperties()) {
                allRegionProps.add(regionProperty.getHierarchicalName());
            }
        }
        //iterate over whole entity model
        createViewForNode(entityTreeRootNode, view, allRegionProps);
        return view;
    }

    /**
     * Create report region using view and whole entity model as entityTree param
     * For creating tabulated report region for collection of entity (when used # in alias of dataset) view and
     * parameters must to be non-nul values because otherwise necessary ReportRegion.regionPropertiesRootNode field value
     * will be null. That value is determined by that view.
     *
     * @param entityTree             the whole entity tree model
     * @param isTabulated            determine which region will be created
     * @param view                   by that view region will be created
     * @param collectionPropertyName must to be non-null for a tabulated region
     * @return report region
     */
    @Override
    public ReportRegion createReportRegionByView(EntityTree entityTree, boolean isTabulated, @Nullable View view, @Nullable String collectionPropertyName) {
        if (StringUtils.isNotBlank(collectionPropertyName) && view == null) {
            //without view we can`t correctly set rootNode for region which is necessary for tabulated regions for a
            // collection of entities (when alias contain #)
            log.warn("Detected incorrect parameters for createReportRegionByView method. View must not to be null if " +
                    "collection collectionPropertyName is not null (" + collectionPropertyName + ")");
        }
        ReportRegion reportRegion = metadata.create(ReportRegion.class);

        EntityTreeNode entityTreeRootNode = entityTree.getEntityTreeRootNode();

        Map<String, EntityTreeNode> allNodesAndHierarchicalPathsMap = new HashMap<>();
        nodesToMap(entityTreeRootNode, allNodesAndHierarchicalPathsMap);
        boolean scalarOnly;//code below became less readable if we will use isTabulated parameter instead of that 'scalarOnly' variable
        if (isTabulated) {
            reportRegion.setIsTabulatedRegion(Boolean.TRUE);
            reportRegion.setRegionPropertiesRootNode(allNodesAndHierarchicalPathsMap.get(collectionPropertyName));
            scalarOnly = false;
        } else {
            reportRegion.setIsTabulatedRegion(Boolean.FALSE);
            reportRegion.setRegionPropertiesRootNode(entityTreeRootNode);
            scalarOnly = true;
        }
        if (view != null) {
            iterateViewAndCreatePropertiesForRegion(scalarOnly, view, allNodesAndHierarchicalPathsMap, reportRegion.getRegionProperties(), collectionPropertyName, 0);
        }
        return reportRegion;
    }
    protected void iterateViewAndCreatePropertiesForRegion(final boolean scalarOnly, final View parentView, final Map<String, EntityTreeNode> allNodesAndHierarchicalPathsMap, final List<RegionProperty> regionProperties, String pathFromParentView, long propertyOrderNum) {
        if (pathFromParentView == null) {
            pathFromParentView = "";
        }
        for (ViewProperty viewProperty : parentView.getProperties()) {

            if (scalarOnly) {
                MetaClass metaClass = metadata.getClass(parentView.getEntityClass());
                MetaProperty metaProperty = metaClass.getProperty(viewProperty.getName());
                if (metaProperty != null && metaProperty.getRange().getCardinality().isMany()) {
                    continue;
                }
            }

            if (viewProperty.getView() != null) {
                iterateViewAndCreatePropertiesForRegion(scalarOnly, viewProperty.getView(), allNodesAndHierarchicalPathsMap, regionProperties, pathFromParentView + "." + viewProperty.getName(), propertyOrderNum);
            } else {
                EntityTreeNode entityTreeNode = allNodesAndHierarchicalPathsMap.get(StringUtils.removeStart(pathFromParentView + "." + viewProperty.getName(), "."));

                if (entityTreeNode != null) {
                    RegionProperty regionProperty = metadata.create(RegionProperty.class);
                    regionProperty.setOrderNum(++propertyOrderNum);
                    regionProperty.setEntityTreeNode(entityTreeNode);
                    regionProperties.add(regionProperty);
                }
            }
        }
    }

    protected void nodesToMap(EntityTreeNode node, final Map<String, EntityTreeNode> allNodesAndHierarchicalPathsMap) {
        if (!node.getChildren().isEmpty()) {
            allNodesAndHierarchicalPathsMap.put(node.getHierarchicalNameExceptRoot(), node);
            for (EntityTreeNode entityTreeNode : node.getChildren()) {
                nodesToMap(entityTreeNode, allNodesAndHierarchicalPathsMap);
            }
        } else {
            allNodesAndHierarchicalPathsMap.put(node.getHierarchicalNameExceptRoot(), node);
        }
    }

    protected void createViewForNode(EntityTreeNode entityTreeNode, View parentView, final Set<String> viewPropsWhiteList) {

        for (EntityTreeNode child : entityTreeNode.getChildren()) {
            if (child.getWrappedMetaClass() == null) {
                if (viewPropsWhiteList.contains(child.getHierarchicalName())) {
                    parentView.addProperty(child.getWrappedMetaProperty().getName()); //1)add property to view if it is exists in regionProperties of report
                }
            } else {
                View newParentView = new View(child.getWrappedMetaClass().getJavaClass());
                createViewForNode(child, newParentView, viewPropsWhiteList);
                if (!newParentView.getProperties().isEmpty()) {
                    //2) add views only with properties
                    parentView.addProperty(child.getWrappedMetaProperty().getName(), newParentView);
                }
            }
        }
    }

    @Override
    public boolean isEntityAllowedForReportWizard(final MetaClass effectiveMetaClass) {
        if (StringUtils.startsWithAny(effectiveMetaClass.getName(), EntityTreeModelBuilder.IGNORED_ENTITIES_PREFIXES) ||
                effectiveMetaClass.getJavaClass().isAnnotationPresent(Embeddable.class) ||
                effectiveMetaClass.getJavaClass().isAnnotationPresent(SystemLevel.class) ||
                //&& userSession.isEntityOpPermitted(effectiveMetaClass, EntityOp.READ)
                effectiveMetaClass.getOwnProperties().isEmpty()) {
            return false;
        }
        List<String> whiteListedEntities = getWizardWhiteListedEntities();
        if (!whiteListedEntities.isEmpty()) {
            //use white list cause it has more meaningful priority
            if (!whiteListedEntities.contains(effectiveMetaClass.getName())) {
                return false;
            }
        } else {
            //otherwise filter by a blacklist
            if (getWizardBlackListedEntities().contains(effectiveMetaClass.getName())) {
                return false;
            }
        }
        Collection<Object> ownPropsNamesList = CollectionUtils.collect(effectiveMetaClass.getOwnProperties(), new Transformer() {
            @Override
            public Object transform(Object input) {
                return ((MetaProperty) input).getName();
            }
        });

        if (getWizardBlackListedProperties().isEmpty()) {
            ownPropsNamesList.removeAll(IGNORED_ENTITY_PROPERTIES);
            if (ownPropsNamesList.isEmpty()) {
                return false;
            }
        }

        ownPropsNamesList.removeAll(CollectionUtils.collect(getWizardBlackListedProperties(), new Transformer() {
            @Override
            public Object transform(Object input) {
                if (effectiveMetaClass.getName().equals(StringUtils.substringBefore((String) input, "."))) {
                    return StringUtils.substringAfter((String) input, ".");
                }
                return null;
            }
        }));
        return !ownPropsNamesList.isEmpty();
    }

    @Override
    public boolean isPropertyAllowedForReportWizard(MetaClass metaClass, MetaProperty metaProperty) {
        //here we can`t just to determine metaclass using property argument cause it can be an ancestor of it
        if (getWizardBlackListedProperties().contains(metaClass.getName() + "." + metaProperty.getName())) {
            return false;
        }
        if (getWizardBlackListedProperties().isEmpty() && IGNORED_ENTITY_PROPERTIES.contains(metaProperty.getName())) {
            return false;
        }
        return true;
    }

    protected List<String> getWizardBlackListedEntities() {
        String entitiesBlackList = AppBeans.get(Configuration.class).getConfig(ReportingConfig.class).getWizardEntitiesBlackList();
        if (StringUtils.isNotBlank(entitiesBlackList)) {
            return Arrays.asList(StringUtils.split(entitiesBlackList, ','));
        }

        return Collections.emptyList();
    }

    protected List<String> getWizardWhiteListedEntities() {
        String entitiesWhiteList = AppBeans.get(Configuration.class).getConfig(ReportingConfig.class).getWizardEntitiesWhiteList();
        if (StringUtils.isNotBlank(entitiesWhiteList)) {
            return Arrays.asList(StringUtils.split(entitiesWhiteList, ','));
        }

        return Collections.emptyList();
    }

    protected List<String> getWizardBlackListedProperties() {
        String propertiesBlackList = AppBeans.get(Configuration.class).getConfig(ReportingConfig.class).getWizardPropertiesBlackList();
        if (StringUtils.isNotBlank(propertiesBlackList)) {
            return Arrays.asList(StringUtils.split(propertiesBlackList, ','));
        }

        return Collections.emptyList();
    }
}
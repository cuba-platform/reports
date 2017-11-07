/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.reports.wizard;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.reports.ReportingConfig;
import com.haulmont.reports.app.EntityTree;
import com.haulmont.reports.app.EntityTreeStructureInfo;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;

import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component(EntityTreeModelBuilderApi.NAME)
@Scope("prototype")
public class EntityTreeModelBuilder implements EntityTreeModelBuilderApi {
    protected MessageTools messageTools = AppBeans.get(MessageTools.NAME);
    protected ReportingWizardApi reportingWizardApi = AppBeans.get(ReportingWizardApi.NAME);
    protected int entityTreeModelMaxDeep = AppBeans.get(Configuration.class).getConfig(ReportingConfig.class).getEntityTreeModelMaxDeep();
    protected Metadata metadata = AppBeans.get(Metadata.NAME);

    public int getEntityTreeModelMaxDeep() {
        return entityTreeModelMaxDeep;
    }

    public void setEntityTreeModelMaxDeep(int entityTreeModelMaxDeep) {
        this.entityTreeModelMaxDeep = entityTreeModelMaxDeep;
    }

    @Override
    public EntityTree buildEntityTree(MetaClass metaClass) {
        EntityTree entityTree = new EntityTree();
        EntityTreeStructureInfo entityTreeStructureInfo = new EntityTreeStructureInfo();
        EntityTreeNode root = metadata.create(EntityTreeNode.class);
        root.setName(metaClass.getName());
        root.setLocalizedName(StringUtils.isEmpty(messageTools.getEntityCaption(metaClass)) ? metaClass.getName() : messageTools.getEntityCaption(metaClass));
        root.setWrappedMetaClass(metaClass);
        fillChildNodes(root, 1, new HashSet<String>(), entityTreeStructureInfo);
        entityTree.setEntityTreeRootNode(root);
        entityTree.setEntityTreeStructureInfo(entityTreeStructureInfo);
        return entityTree;
    }

    protected EntityTreeNode fillChildNodes(final EntityTreeNode parentEntityTreeNode, int depth, final Set<String> alreadyAddedMetaProps, final EntityTreeStructureInfo entityTreeStructureInfo) {

        if (depth > getEntityTreeModelMaxDeep()) {
            return parentEntityTreeNode;
        }
        MetaClass fileDescriptorMetaClass = metadata.getClassNN(FileDescriptor.class);
        for (com.haulmont.chile.core.model.MetaProperty metaProperty : parentEntityTreeNode.getWrappedMetaClass().getProperties()) {
            if (!reportingWizardApi.isPropertyAllowedForReportWizard(parentEntityTreeNode.getWrappedMetaClass(), metaProperty)) {
                continue;
            }
            if (metaProperty.getRange().isClass()) {
                MetaClass metaClass = metaProperty.getRange().asClass();
                MetaClass effectiveMetaClass = metadata.getExtendedEntities().getEffectiveMetaClass(metaClass);
                //does we need to do security checks here? no

                MetadataTools metadataTools = metadata.getTools();

                if (fileDescriptorMetaClass.equals(effectiveMetaClass) || !metadataTools.isSystemLevel(effectiveMetaClass) && !metadataTools.isSystemLevel(metaProperty)) {
                    int newDepth = depth + 1;
                    EntityTreeNode newParentModelNode = metadata.create(EntityTreeNode.class);
                    newParentModelNode.setName(metaProperty.getName());
                    //newParentModelNode.setLocalizedName(messageTools.getEntityCaption(effectiveMetaClass));
                    newParentModelNode.setLocalizedName(
                            StringUtils.isEmpty(messageTools.getPropertyCaption(parentEntityTreeNode.getWrappedMetaClass(), metaProperty.getName())) ?
                                    metaProperty.getName() : messageTools.getPropertyCaption(parentEntityTreeNode.getWrappedMetaClass(), metaProperty.getName())
                    );
                    newParentModelNode.setWrappedMetaClass(effectiveMetaClass);
                    newParentModelNode.setWrappedMetaProperty(metaProperty);
                    newParentModelNode.setParent(parentEntityTreeNode);


                    if (alreadyAddedMetaProps.contains(getTreeNodeInfo(parentEntityTreeNode) + "|" + getTreeNodeInfo(newParentModelNode))) {
                        continue; //avoid parent-child-parent-... infinite loops
                    }
                    //alreadyAddedMetaProps.add(getTreeNodeInfo(parentEntityTreeNode) + "|" + getTreeNodeInfo(newParentModelNode));
                    alreadyAddedMetaProps.add(getTreeNodeInfo(newParentModelNode) + "|" + getTreeNodeInfo(parentEntityTreeNode));

                    //System.err.println(StringUtils.leftPad("", newDepth * 2, " ") + getTreeNodeInfo(parentEntityTreeNode) + "     |     " + getTreeNodeInfo(newParentModelNode));
                    //System.err.println(StringUtils.leftPad("", newDepth * 2, " ") + getTreeNodeInfo(newParentModelNode) + "     |     " + getTreeNodeInfo(parentEntityTreeNode));
                    //System.err.println("");

                    if (!entityTreeStructureInfo.isEntityTreeRootHasCollections() && metaProperty.getRange().getCardinality().isMany() && depth == 1) {
                        entityTreeStructureInfo.setEntityTreeRootHasCollections(true);//TODO set to true if only simple attributes of that collection as children exists
                    }
                    fillChildNodes(newParentModelNode, newDepth, alreadyAddedMetaProps, entityTreeStructureInfo);

                    parentEntityTreeNode.getChildren().add(newParentModelNode);
                }
            } else {
                if (!entityTreeStructureInfo.isEntityTreeHasSimpleAttrs()) {
                    entityTreeStructureInfo.setEntityTreeHasSimpleAttrs(true);
                }
                EntityTreeNode child = metadata.create(EntityTreeNode.class);
                child.setName(metaProperty.getName());
                child.setLocalizedName(StringUtils.isEmpty(messageTools.
                        getPropertyCaption(parentEntityTreeNode.getWrappedMetaClass(), metaProperty.getName())) ?
                        metaProperty.getName() : messageTools.getPropertyCaption(parentEntityTreeNode.getWrappedMetaClass(), metaProperty.getName()));
                child.setWrappedMetaProperty(metaProperty);
                child.setParent(parentEntityTreeNode);
                parentEntityTreeNode.getChildren().add(child);

            }

        }
        return parentEntityTreeNode;
    }

    private String getTreeNodeInfo(EntityTreeNode parentEntityTreeNode) {
        if (parentEntityTreeNode.getWrappedMetaProperty() != null) {
            return (parentEntityTreeNode.getWrappedMetaProperty().getDomain().getName().equals(parentEntityTreeNode.getWrappedMetaClass().getName()) ?
                    "" : parentEntityTreeNode.getWrappedMetaProperty().getDomain() + ".") +
                    parentEntityTreeNode.getWrappedMetaClass().getName() + " isMany:" + parentEntityTreeNode.getWrappedMetaProperty().getRange().getCardinality().isMany();
        } else {
            return parentEntityTreeNode.getWrappedMetaClass().getName() + " isMany:false";
        }
    }
}


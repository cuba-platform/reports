/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.reports.gui.report.wizard.region;

import com.haulmont.bali.datastruct.Node;
import com.haulmont.bali.datastruct.Tree;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.data.impl.AbstractTreeDatasource;
import com.haulmont.reports.entity.wizard.EntityTreeNode;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.text.Collator;
import java.util.*;

/**
 * Build Tree using rootEntity as root Node
 *
 * @author fedorchenko
 * @version $Id$
 */
public class EntityTreeNodeDs extends AbstractTreeDatasource<EntityTreeNode, UUID> {
    protected boolean collectionsOnly;
    protected boolean scalarOnly;
    protected boolean showRoot;
    protected Comparator<EntityTreeNode> nodeComparator = new Comparator<EntityTreeNode>() {
        @Override
        public int compare(EntityTreeNode o1, EntityTreeNode o2) {
            //return o1.getNodeChildrenDepth().compareTo(o2.getNodeChildrenDepth());
            Collator collator = Collator.getInstance();
            return collator.compare(o1.getHierarchicalLocalizedNameExceptRoot(), o2.getHierarchicalLocalizedNameExceptRoot());
        }
    };

    @Override
    protected Tree loadTree(Map<String, Object> params) {
        collectionsOnly = isTreeForCollectionsOnly(params);
        scalarOnly = isTreeForScalarOnly(params);
        showRoot = isTreeMustContainRoot(params);
        Tree<EntityTreeNode> resultTree = new Tree<>();
        String searchValue = StringUtils.defaultIfBlank(((TextField) params.get("component$reportPropertyName")).<String>getValue(), "");
        if (params.get("rootEntity") != null) {
            EntityTreeNode rootNodeObject = (EntityTreeNode) params.get("rootEntity");
            List<Node<EntityTreeNode>> rootNodes;
            if (isTreeMustContainRoot(params)) {
                Node<EntityTreeNode> rootNode = new Node<>(rootNodeObject);
                if (rootNodeObject.getLocalizedName().toLowerCase().contains(searchValue.toLowerCase()))
                    fill(rootNode);
                else
                    fill(rootNode, searchValue);
                rootNodes = !rootNode.getChildren().isEmpty() ? Collections.singletonList(rootNode) : Collections.<Node<EntityTreeNode>>emptyList();
            } else {//don`t show current node in the tree. show only children
                rootNodes = new ArrayList<>(rootNodeObject.getChildren().size());
                for (EntityTreeNode child : rootNodeObject.getChildren()) {
                    if (scalarOnly && child.getWrappedMetaProperty().getRange().getCardinality().isMany()) {
                        continue;
                    }
                    Node<EntityTreeNode> newRootNode = new Node<>(child);
                    if (child.getLocalizedName().toLowerCase().contains(searchValue.toLowerCase()))
                        fill(newRootNode);
                    else
                        fill(newRootNode, searchValue);
                    if (!newRootNode.getChildren().isEmpty())
                        rootNodes.add(newRootNode);
                }
            }
            resultTree.setRootNodes(rootNodes);
        }
        return resultTree;
    }

    protected void fill(final Node<EntityTreeNode> parentNode, String searchValue) {
        Collections.sort(parentNode.getData().getChildren(), nodeComparator);
        for (EntityTreeNode child : parentNode.getData().getChildren()) {
            if (collectionsOnly && !child.getWrappedMetaProperty().getRange().getCardinality().isMany()) {
                continue;
            }

            if (collectionsOnly && (
                    (showRoot && parentNode.getParent() != null && parentNode.getParent().getParent() == null) ||
                            (!showRoot && parentNode.getParent() == null)
            )) {
                //for collections max selection depth is limited to 2 cause reporting is not supported collection multiplying. And it is good )
                continue;
            }
            if (scalarOnly && child.getWrappedMetaProperty().getRange().getCardinality().isMany()) {
                continue;
            }
            if (!child.getChildren().isEmpty()) {

                Node<EntityTreeNode> newParentNode = new Node<>(child);
                newParentNode.parent = parentNode;
                if (StringUtils.isEmpty(searchValue) || child.getLocalizedName().toLowerCase().contains(searchValue.toLowerCase())) {
                    fill(newParentNode);
                    parentNode.addChild(newParentNode);
                } else {
                    fill(newParentNode, searchValue);
                    if (!newParentNode.getChildren().isEmpty())
                        parentNode.addChild(newParentNode);
                }
            } else {
                if (scalarOnly && child.getWrappedMetaProperty().getRange().isClass()) {
                    //doesn`t fetch if it is a last entity and is a class cause we can`t select it in UI anyway
                    continue;
                }
                Node childNode = new Node<>(child);
                if (StringUtils.isEmpty(searchValue) || child.getLocalizedName().toLowerCase().contains(searchValue.toLowerCase())) {
                    parentNode.addChild(childNode);
                }
            }
        }
    }


    protected void fill(final Node<EntityTreeNode> parentNode) {
        fill(parentNode, "");
    }

    protected boolean findSearchProperty(final Node<EntityTreeNode> parentNode, String searchValue) {
        if (StringUtils.isEmpty(searchValue))
            return true;
        Collections.sort(parentNode.getData().getChildren(), nodeComparator);
        for (EntityTreeNode child : parentNode.getData().getChildren()) {
            if (collectionsOnly && !child.getWrappedMetaProperty().getRange().getCardinality().isMany()) {
                continue;
            }

            if (collectionsOnly && (
                    (showRoot && parentNode.getParent() != null && parentNode.getParent().getParent() == null) ||
                            (!showRoot && parentNode.getParent() == null)
            )) {
                //for collections max selection depth is limited to 2 cause reporting is not supported collection multiplying. And it is good )
                continue;
            }
            if (scalarOnly && child.getWrappedMetaProperty().getRange().getCardinality().isMany()) {
                continue;
            }
            if (!child.getChildren().isEmpty()) {

                Node<EntityTreeNode> newParentNode = new Node<>(child);
                newParentNode.parent = parentNode;

                boolean hasProperty = findSearchProperty(newParentNode, searchValue);
                if (hasProperty)
                    return true;
            } else {
                if (scalarOnly && child.getWrappedMetaProperty().getRange().isClass()) {
                    //doesn`t fetch if it is a last entity and is a class cause we can`t select it in UI anyway
                    continue;
                }
                if (child.getLocalizedName().contains(searchValue))
                    return true;
            }
        }
        return false;
    }

    protected boolean isTreeForCollectionsOnly(Map<String, Object> params) {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) params.get("collectionsOnly"), false);
    }

    protected boolean isTreeForScalarOnly(Map<String, Object> params) {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) params.get("scalarOnly"), false);
    }

    protected boolean isTreeMustContainRoot(Map<String, Object> params) {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) params.get("showRoot"), true);
    }
}

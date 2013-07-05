package com.maxifier.mxcache.impl.resource;

import java.util.Queue;
import java.util.Set;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 21.03.11
* Time: 17:01
*/
public interface DependencyNodeVisitor {
    void visit(DependencyNode node);

    Set<DependencyNode> getNodes();

    Queue<DependencyNode> getQueue();
}

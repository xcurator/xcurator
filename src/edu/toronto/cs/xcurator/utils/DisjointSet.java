/*
 *    Copyright (c) 2013, University of Toronto.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */
package edu.toronto.cs.xcurator.utils;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple representation of disjoint sets
 */
public class DisjointSet<T> {

    /**
     * The resource this set represents
     */
    private T data;

    /**
     * The parent set in a union
     */
    private DisjointSet<T> m_parent;

    private List<DisjointSet<T>> m_children;

    /**
     * Heuristic used to build balanced unions
     */
    private int m_rank;

    /**
     * The link to the distinguished member set
     */
    private DisjointSet<T> m_ancestor;

    /**
     * Set to true when the node has been processed
     */
    private boolean m_black = false;

    /**
     * Set to true when we've inspected a black set, since the result is only
     * correct just after both of the sets for u and v have been marked black
     */
    private boolean m_used = false;

    public DisjointSet(T data) {
        this.data = data;
        m_rank = 0;
        m_parent = this;
        m_children = new LinkedList<DisjointSet<T>>();
    }

    public T getData() {
        return data;
    }

    public DisjointSet<T> getParent() {
        return m_parent;
    }

    public void setParent(DisjointSet<T> parent) {
        m_parent = parent;
        parent.m_children.add(this);
        parent.m_children.addAll(this.m_children);
    }

    public int getRank() {
        return m_rank;
    }

    public void incrementRank() {
        m_rank++;
    }

    public DisjointSet<T> getAncestor() {
        return m_ancestor;
    }

    public void setAncestor(DisjointSet<T> anc) {
        m_ancestor = anc;
    }

    public void setBlack() {
        m_black = true;
    }

    public boolean isBlack() {
        return m_black;
    }

    public boolean used() {
        return m_used;
    }

    public void setUsed() {
        m_used = true;
    }

    public List<DisjointSet<T>> getChildren() {
        return m_children;
    }

    /**
     * The find operation collapses the pointer to the root parent, which is one
     * of Tarjan's standard optimisations.
     *
     * @return The representative of the union containing this set
     */
    public DisjointSet<T> find() {
        DisjointSet<T> root;
        if (getParent() == this) {
            // the representative of the set
            root = this;
        } else {
            // otherwise, seek the representative of my parent and save it
            root = getParent().find();
            setParent(root);
        }

        return root;
    }

    /**
     * The union of two sets
     *
     * @param y
     */
    public void union(DisjointSet<T> y) {
        DisjointSet<T> xRoot = find();
        DisjointSet<T> yRoot = y.find();

        if (xRoot.getRank() > yRoot.getRank()) {
            yRoot.setParent(xRoot);
        } else if (yRoot.getRank() > xRoot.getRank()) {
            xRoot.setParent(yRoot);
        } else if (xRoot != yRoot) {
            yRoot.setParent(xRoot);
            xRoot.incrementRank();
        }
    }

    /**
     * @see java.lang.Object#toString()
     * @return A string representation of this set for debugging
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("DisjointSet{node=");
        buf.append(data);
        buf.append(",anc=");
        buf.append((getAncestor() == this) ? "self" : (getAncestor() == null ? "null" : getAncestor().toShortString()));
        buf.append(",parent=");
        buf.append((getParent() == this) ? "self" : (getParent() == null ? "null" : getParent().toShortString()));
        buf.append(",rank=");
        buf.append(getRank());
        buf.append(m_black ? ",black" : ",white");
        buf.append("}");

        return buf.toString();
    }

    public String toShortString() {
        StringBuffer buf = new StringBuffer();
        buf.append("DisjointSet{node=");
        buf.append(data);
        buf.append(",parent=");
        buf.append((getParent() == this) ? "self" : (getParent() == null ? "null" : getParent().toShortString()));
        buf.append("...}");

        return buf.toString();
    }
}

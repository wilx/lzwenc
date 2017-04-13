/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jul 9, 2005
 */

package lzwenc;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.impl.SparseTree;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.visualization.LayoutMutable;

/**
 * @author Karlheinz Toni, modified by Václav Haisman
 *
 */

public class TreeLayout
    extends AbstractLayout
        implements LayoutMutable
{

    protected static final String C_DIMENSION_X_BASE_KEY = "DimsionX";

    public static int DEFAULT_DISTX = 60;

    public static int DEFAULT_DISTY = 75;

    protected transient Set<Vertex> allreadyDone = new HashSet<>();

    protected int distX = DEFAULT_DISTX;

    protected int distY = DEFAULT_DISTY;

    protected transient Point m_currentPoint = new Point ();

    protected Pair m_dimensionKey;

    protected Vertex m_rootVertex;

    public TreeLayout (final SparseTree g)
    {
        super (g);
        m_rootVertex = g.getRoot ();
    }

    public TreeLayout (final SparseTree g, final int distx)
    {
        super (g);
        m_rootVertex = g.getRoot ();
        distX = distx;
    }

    public TreeLayout (final SparseTree g, final int distx, final int disty)
    {
        super (g);
        m_rootVertex = g.getRoot ();
        distX = distx;
        distY = disty;
    }

    /**
     * ?
     *
     * @see edu.uci.ics.jung.visualization.Layout#advancePositions()
     */
    @Override
    public void advancePositions ()
    {
    }

    void buildTree ()
    {
        m_currentPoint = new Point (getCurrentSize ().width / 2, 20);
        if (m_rootVertex != null && getGraph () != null)
        {
            calculateDimensionX (m_rootVertex);
            buildTree (m_rootVertex, m_currentPoint.x);
        }
    }

    void buildTree (final Vertex v, final int x)
    {
        if (!allreadyDone.contains (v))
        {
            allreadyDone.add (v);

            m_currentPoint.y += distY;
            m_currentPoint.x = x;

            setCurrentPositionFor (v);

            final int sizeXofCurrent = (Integer) v.getUserDatum(getDimensionBaseKey());

            int lastX = x - sizeXofCurrent / 2;

            int sizeXofChild;
            int startXofChild;

            for (final Vertex element : (Set<Vertex>)v.getSuccessors()) {
                sizeXofChild = (Integer) element.getUserDatum(getDimensionBaseKey());
                startXofChild = lastX + sizeXofChild / 2;
                buildTree(element, startXofChild);
                lastX = lastX + sizeXofChild + distX;
            }
            m_currentPoint.y -= distY;
        }
    }

    class VertexComparator
            implements Comparator<Vertex>
    {
        public VertexComparator (final StringLabeller l)
        {
            labeller = l;
        }

        @Override
        public int compare (final Vertex o1, final Vertex o2)
        {
            final Integer n1 = Integer.parseInt (labeller.getLabel (o1));
            final int n2 = Integer.parseInt (labeller.getLabel (o2));
            return n1.compareTo (n2);
        }

        protected StringLabeller labeller;
    }

    protected int calculateDimensionX (final Vertex v)
    {
        int size = 0;
        final int childrenNum = v.getSuccessors ().size ();
        final StringLabeller ll = StringLabeller.getLabeller ((Graph) v.getGraph ());
        final TreeSet<Vertex> successors = new TreeSet<>(new VertexComparator(
                ll));
        successors.addAll (v.getSuccessors ());
        if (childrenNum != 0)
        {
            Vertex element;
            for (Vertex successor : successors) {
                element = successor;
                size += calculateDimensionX(element) + distX;
            }
        }
        size = Math.max (0, size - distX);
        v.setUserDatum (getDimensionBaseKey (), size, UserData.REMOVE);
        return size;
    }

    public int getDepth (final Vertex v)
    {
        int depth = 0;
        for (final Vertex c : (Set<Vertex>)v.getSuccessors()) {
            if (c.getSuccessors().isEmpty()) {
                depth = 0;
            } else {
                depth = Math.max(depth, getDepth(c));
            }
        }

        return depth + 1;
    }

    protected Object getDimensionBaseKey ()
    {
        if (m_dimensionKey == null)
        {
            m_dimensionKey = new Pair (this, C_DIMENSION_X_BASE_KEY);
        }
        return m_dimensionKey;
    }

    /**
     * @return Returns the rootVertex_.
     */
    public Vertex getRootVertex ()
    {
        return m_rootVertex;
    }

    /**
     * ?
     *
     * @see edu.uci.ics.jung.visualization.Layout#incrementsAreDone()
     */
    @Override
    public boolean incrementsAreDone ()
    {
        return true;
    }

    @Override
    public void initialize (final Dimension size)
    {
        super.initialize (size);
        buildTree ();
    }

    // protected void initialize_local() {
    //
    // }
    /**
     * ?
     *
     * @see edu.uci.ics.jung.visualization.AbstractLayout#initialize_local_vertex(edu.uci.ics.jung.graph.Vertex)
     */
    @Override
    protected void initialize_local_vertex (final Vertex v)
    {
    }

    @Override
    protected void initializeLocations ()
    {
        for (final Vertex v : (Set<Vertex>)getGraph().getVertices()) {
            Coordinates coord = (Coordinates) v.getUserDatum(getBaseKey());
            if (coord == null) {
                coord = new Coordinates();
                v.addUserDatum(getBaseKey(), coord, UserData.REMOVE);
            }
            initialize_local_vertex(v);
        }
    }

    /**
     * ?
     *
     * @see edu.uci.ics.jung.visualization.Layout#isIncremental()
     */
    @Override
    public boolean isIncremental ()
    {
        return false;
    }

    protected void setCurrentPositionFor (final Vertex vertex)
    {
        final Coordinates coord = getCoordinates (vertex);
        coord.setX (m_currentPoint.x);
        coord.setY (m_currentPoint.y);
    }

    /**
     * @param rootVertex_
     *            The rootVertex_ to set.
     */
    public void setRootVertex (final Vertex rootVertex_)
    {
        m_rootVertex = rootVertex_;
    }

    @Override
    public synchronized void update ()
    {
        try
        {
            for (final Vertex v : (Set<Vertex>)getGraph().getVertices()) {
                Coordinates coord = getCoordinates(v);
                if (coord == null) {
                    coord = new Coordinates();
                    v.addUserDatum(getBaseKey(), coord, UserData.REMOVE);
                    initializeLocation(v, coord, getCurrentSize());
                    initialize_local_vertex(v);
                }
            }
            allreadyDone.clear ();
            buildTree ();
        }
        catch (final ConcurrentModificationException cme)
        {
            update ();
        }
        initialize_local ();
    }
}

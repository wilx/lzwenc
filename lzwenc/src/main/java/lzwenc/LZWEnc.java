/**
 * This package is implementation of LZW compression algorithm
 * as applet for the 36KOD course on CTU FEE, Prague.
 */
package lzwenc;

import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.EdgeFontFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.StringLabeller.UniqueLabelException;
import edu.uci.ics.jung.graph.decorators.VertexColorFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.graph.impl.SimpleSparseVertex;
import edu.uci.ics.jung.graph.impl.SparseTree;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import gui.BasicApplet;
import gui.Localisation;
import gui.utils.BwEpsFileFilter;
import gui.utils.ColorEpsFileFilter;
import gui.utils.EpsFileFilter;
import gui.utils.GrEpsFileFilter;
import gui.utils.PngFileFilter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import lzwenc.coder.Binary;
import lzwenc.compr.LZWEncContext;
import lzwenc.compr.Node;

import org.jibble.epsgraphics.EpsGraphics2D;

import wilx.utils.S;
import wilx.utils.Utils;


/**
 * @author Václav Haisman
 * @version v0.1
 */
public
class LZWEnc
    extends BasicApplet
    implements LZWEncContext.CompressionObserver
{
    private static final long serialVersionUID = 1L;

    public
    LZWEnc ()
    { }

    @Override
    public
    int
    doNextStep ()
    {
        switch (state)
        {
        // "  while not eof(input) do begin"
        case 0:
        {
            highlightLine (4);
            final String tmp = readNext ();
            if (tmp.equals (""))
            {
                state = 50;
                break;
            }
            assert tmp.length () == 1;
            read_char = tmp.charAt (0);
            final int chval = Character.codePointAt (tmp, 0);
            assert chval < 128;
            next_char = chew_char_num (chval);

            current_node = ctx.getCurrent ();
            node_to_vertex (current_node)
                .setUserDatum ("picked", Boolean.TRUE, UserData.REMOVE);

            state = 1;
            break;
        }

        // "     letter = getchar(input)"
        case 1:
        {
            highlightLine (5);

            state = 2;
            break;
        }

        // "     next_node = current_node.edges[letter]"
        case 2:
        {
            highlightLine (6);

            input_bytes += 1;
            final int len = (int)Math.ceil (Utils.log2 (VALID.length ()));
            stats_panel.addInputBits (len);

            state = 3;
            break;
        }

        // "     if can't go further from current_node then begin"
        case 3:
        {
            highlightLine (7);

            lzw_output = ctx.encLetter (next_char);
            if (lzw_output != -1)
                state = 11;
            else
                state = 21;
            break;
        }

        // "       output = current_node.number"
        case 11:
        {
            highlightLine (8);

            state = 12;
            break;
        }

        // "       insert new edge through input letter from current_node with number = node_count"
        case 12:
        {
            highlightLine (9);

            final int len = (int)Math.ceil (Utils.log2 (ctx.getNodeCount ()));
            coder.setWidth (len);
            //String binary_string = Utils.bitSet2String (coder.encode (lzw_output));
            //write (" " + lzw_output + "(" + binary_string + ")");
            write (" " + lzw_output);
            stats_panel.addOutputBits (len);

            state = 13;
            break;
        }

        // "       increment node_count"
        case 13:
        {
            highlightLine (10);

            final DirectedSparseVertex new_v = new DirectedSparseVertex ();
            assert new_v !=null;
            final DirectedSparseVertex prev_v = (DirectedSparseVertex)node_to_vertex (prev_node);
            assert prev_v != null;

            graph.addVertex (new_v);
            new_v.setUserDatum ("added", Boolean.TRUE, UserData.REMOVE);
            try
            {
                labeller.setLabel (new_v, Integer.toString (added_node.getNum ()));
            }
            catch (final UniqueLabelException e)
            {
                Utils.abort ();
            }
            final DirectedSparseEdge e = new DirectedSparseEdge (prev_v, new_v);
            graph.addEdge (e);
            e.setUserDatum ("letter", Character.valueOf (read_char), UserData.CLONE);
            //vv = new VisualizationViewer (layout, renderer);

            repaint_graph ();

            state = 14;
            break;
        }

        // "       current_node = move from root to next node through input letter"
        case 14:
        {
            highlightLine (11);

            state = 4;
            break;
        }

        // "       current_node = next_node"
        case 21:
        {
            highlightLine (14);

            state = 4;
            break;
        }

        case 4:
        {
            highlightLine (16);

            node_to_vertex (prev_node)
                .setUserDatum ("picked", Boolean.FALSE, UserData.REMOVE);
            node_to_vertex (current_node)
                .setUserDatum ("picked", Boolean.TRUE, UserData.REMOVE);
            if (added_node != null)
            {
                final DirectedSparseVertex vx = (DirectedSparseVertex)node_to_vertex (added_node);
                vx.setUserDatum ("added", Boolean.FALSE, UserData.REMOVE);
            }

            state = 0;
            break;
        }

        case 50:
        {
            highlightLine (17);

            lzw_output = ctx.finishEncoding ();
            if (lzw_output != -1)
            {
                final int len = (int)Math.ceil (Utils.log2 (ctx.getNodeCount ()));
                coder.setWidth (len);
                write (" " + lzw_output);
                stats_panel.addOutputBits (len);
            }
            repaint ();
            return -1;
        }

        // XXX Should not happen.
        default:
            Utils.abort ();
        }

        repaint ();
        return 0;
    }

    public
    byte
    chew_char_num (final int num)
    {
        assert num < 256;
        for (int i = 0; i <= VALID.length (); ++i)
            if (VALID.codePointAt(i) == num)
                return (byte)(i + Byte.MIN_VALUE);
        Utils.abort ();
        return -1;
    }

    public
    Vertex
    node_to_vertex (final Node n)
    {
        final int num = n.getNum ();
        final Vertex vx = labeller.getVertex (num == -1
                ? ROOT_NODE_LABEL : Integer.toString (num));
        return vx;
    }

    public
    void
    repaint_graph ()
    {
        for(final Iterator<Vertex> iterator=graph.getVertices().iterator(); iterator.hasNext(); ) {
            layout.unlockVertex(iterator.next());
        }
        layout.update ();
        if (!vv.isVisRunnerRunning())
            vv.init();
        vv.repaint ();
    }

    @Override
    public
    void
    compressionStep (final LZWEncContext ctx_, final byte letter, final int output,
            final Node prev, final Node current, final Node added)
    {
        prev_node = prev;
        current_node = current;
        added_node = added;
    }

    @Override
    public
    boolean
    isValidInput (final String s_)
    {
        // Povol pouze neprazdne retezce se znaky [a-zA-Z0-9].
        if (s_.equals (""))
            return false;
        for (int i = 0; i < s_.length (); ++i)
        {
            final char ch = s_.charAt (i);
            if (! ('a' <= ch && ch <= 'z'
                   || 'A' <= ch && ch <= 'Z'
                   || '0' <= ch && ch <= '9'))
                return false;
        }

        return true;
    }

    public
    static
    void
    main (final String[] args)
    {
        LZWEnc shApplet;
        if (args.length == 1)
        {
            Localisation.setLocale(new Locale(args[0]));
        }
        shApplet = new LZWEnc ();
        final JFrame sfWindow = new JFrame("LZW encoding");
        sfWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sfWindow.setContentPane(shApplet);
        shApplet.init();
        sfWindow.pack();
        sfWindow.setVisible(true);
    }

    @Override
    public
    void
    init ()
    {
        try
        {
            String locale_name = null;
            if (isActive ())
            {
                locale_name = getParameter ("locale");
                if (locale_name != null && ! locale_name.equals (""))
                {
                    System.out.println("locale from getParameter (\"locale\")");
                    locale_name = getParameter ("locale");
                }
            }

            if (locale_name == null && ! Localisation.getLocale ().getLanguage ().equals (""))
            {
                System.out.println("locale from Localisation.getLocale ().getLanguage ()");
                locale_name = Localisation.getLocale ().getLanguage ();
            }
            else
            {
                System.out.println("locale en as default");
                locale_name = "en";
            }

            final Locale l = new Locale (locale_name);
            Locale.setDefault (l);
            Localisation.setLocale (l);
            S.setBundle ("lzwenc.lzwenc", l);
        }
        catch (final NullPointerException e)
        {
            // ??? Pouziji se defaultni, zde nic nesmi byt, aby to fungovalo i jako aplikace.
            Utils.abort ();
        }

        stats_panel = new LZWStatsPanel ();
        setAdditionalControlsPanel (stats_panel);
        super.init ();
        stats_panel.setMinimumSize (new Dimension (100, 20));
        // Disalbe save image button because it doesn't work with jung, yet.
        saveBtn.setEnabled (false);

        reset ();

        setCodelines (get_algo_text ());
        setHeadLine (S.str_("LZW encoding"));
        highlightLine (1);
        setTwoCounters (false);
        hideStatsPanel ();

        repaint ();
    }

    /**
     * Resets internal variables of the applet.
     */
    @SuppressWarnings("deprecation")
    @Override
    public
    void
    reset ()
    {
        super.reset ();

        stats_panel.reset ();
        // Reset state to initial.
        state = 0;
        ctx = new LZWEncContext (this, VALID.length ());
        coder = new Binary ((int)Math.ceil (Utils.log2 (ctx.getNodeCount ())));
        input_bytes = 0;

        // Inicializace zobrazeni slovniku.
        final SimpleSparseVertex v = new SimpleSparseVertex ();
        graph = new SparseTree (v);
        labeller = StringLabeller.getLabeller (graph);
        try
        {
            labeller.setLabel (v, ROOT_NODE_LABEL);
        }
        catch (final UniqueLabelException e1)
        {
            Utils.abort ();
        }
        layout = new TreeLayout (graph);

        renderer = new PluggableRenderer ();
        renderer.setEdgeFontFunction (
            new EdgeFontFunction ()
            {
                @Override
                public
                Font
                getFont (final Edge e)
                {
                    return new Font ("SansSerif", Font.BOLD, 12);
                }
            });
        renderer.setEdgeShapeFunction(new EdgeShape.Line ());
        renderer.getGraphLabelRenderer ().setRotateEdgeLabels (false);
        renderer.setEdgeLabelClosenessFunction (
            new NumberEdgeValue ()
            {
                public final Double DIST = 0.5;

                @Override
                public
                Number
                getNumber (final ArchetypeEdge e)
                {
                    return DIST;
                }

                @Override
                public
                void
                setNumber (final ArchetypeEdge e, final Number n)
                {
                    Utils.abort ();
                }
            });
        renderer.setVertexLabelCentering (true);
        renderer.setVertexColorFunction (
            new VertexColorFunction ()
            {
                @Override
                public
                Color
                getBackColor (final Vertex vx)
                {
                    final Object picked = vx.getUserDatum ("picked");
                    final Object added = vx.getUserDatum ("added");
                    if (picked != null && ((Boolean)picked).booleanValue ())
                        return Color.RED;
                    else if (added != null && ((Boolean)added).booleanValue ())
                        return Color.YELLOW;
                    else
                        return Color.GREEN;
                }

                @Override
                public
                Color
                getForeColor (final Vertex vx)
                {
                    return Color.BLACK;
                }
            });
        renderer.setVertexStringer (
            new VertexStringer ()
            {
                @Override
                public
                String
                getLabel (final ArchetypeVertex vx)
                {
                    Object o;
                    o = labeller.getLabel (vx);
                    if (o != null)
                        return o.toString ();
                    return "";
                }
            });
        renderer.setEdgeStringer (
            new EdgeStringer ()
            {
                @Override
                public
                String
                getLabel (final ArchetypeEdge e)
                {
                    Object o;
                    if (e != null && (o = e.getUserDatum ("letter")) != null)
                        return o.toString ();
                    return "";
                }
            });

        for (int i = 0; i < VALID.length (); ++i)
            try
            {
                final DirectedSparseVertex vx = new DirectedSparseVertex ();
                graph.addVertex (vx);
                labeller.setLabel (vx, Integer.toString (i));
                final DirectedSparseEdge edg = new DirectedSparseEdge (graph.getRoot (), vx);
                graph.addEdge (edg);
                edg.setUserDatum ("letter", VALID.charAt (i), UserData.CLONE);
            }
            catch (final UniqueLabelException e)
            {
                Utils.abort ();
            }

        vv = new VisualizationViewer (layout, renderer);
        vv.setMinimumSize (new Dimension (400, 300));
        final GraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);
        pane = new GraphZoomScrollPane (vv);
        pane.setVisible (true);

        splitPane.setLeftComponent (pane);
    }
    /**
     *
     * @return Array of strings that is the text of the algorithm.
     */
    protected
    String[]
    get_algo_text ()
    {
        algo_text = new String[]
            {
                S.str_("begin"),
                S.str_("  initialize dictionary"),
                S.str_("  current_node = root"),
                S.str_("  while not eof(input) do begin "),
                S.str_("     letter = getchar(input)"),
                S.str_("     next_node = current_node.edges[letter]"),
                S.str_("     if can't go further from current_node then begin"),
                S.str_("       output = current_node.number"),
                S.str_("       insert new edge through input letter from current_node with number = node_count"),
                S.str_("       increment node_count"),
                S.str_("       current_node = move from root to next node through input letter"),
                S.str_("     end"),
                S.str_("     else begin"),
                S.str_("       current_node = next_node"),
                S.str_("     end"),
                S.str_("  end"),
                S.str_("end")
            };
        return algo_text;
    }

    class JPEGFileFilter
        extends FileFilter
    {
        @Override
        public
        boolean
        accept (final File file)
        {
            if (file != null)
            {
                if (file.isDirectory ())
                    return true;
                else
                    return file.getName ().endsWith (".jpg");
            }
            else
                return false;
        }

        @Override
        public
        String
        getDescription ()
        {
            return "JPEG";
        }

    }

    @Override
    protected
    void
    save ()
    {
        final boolean dbmode = vv.isDoubleBuffered ();
        vv.setDoubleBuffered (false);
        try
        {
            final JFileChooser save = new JFileChooser (lastDir);
            save.addChoosableFileFilter (new PngFileFilter ());
            save.addChoosableFileFilter (new ColorEpsFileFilter ());
            save.addChoosableFileFilter (new GrEpsFileFilter ());
            save.addChoosableFileFilter (new BwEpsFileFilter ());
            save.addChoosableFileFilter (new JPEGFileFilter ());
            save.setDialogTitle (Localisation.getString ("saveImg"));
            save.setLocale (Localisation.getLocale ());
            final int result = save.showSaveDialog (this);
            if (result == JFileChooser.APPROVE_OPTION)
            {
                final int colorMode = EpsGraphics2D.RGB;
                final File outputFile = save.getSelectedFile ();
                final Dimension size = layout.getCurrentSize ();

                final FileFilter filter = save.getFileFilter ();
                if (filter instanceof EpsFileFilter)
                {
                    try
                    {
                        final EpsGraphics2D out = new EpsGraphics2D ("Komprese",
                                outputFile, 0, 0, size.width, size.height);
                        out.setColorDepth (colorMode);
                        //vv.paintComponents (out);
                        vv.paintAll(out);
                        out.flush ();
                        out.close ();
                        out.dispose ();
                    }
                    catch (final IOException ex)
                    {
                        System.err.println (ex.getLocalizedMessage ());
                    }
                }
                else if (filter instanceof PngFileFilter)
                {
                    final BufferedImage image=new BufferedImage (size.width, size.height, BufferedImage.TYPE_3BYTE_BGR);
                    final Graphics g = image.getGraphics ();
                    g.setColor (Color.WHITE);
                    g.fillRect (0,0, size.width, size.height);
                    //vv.paintComponents (g);
                    vv.paintAll (g);
                    try
                    {
                        ImageIO.write(image, "png", outputFile);
                    }
                    catch (final IOException ex)
                    {
                        System.err.println(ex.getLocalizedMessage());
                    } finally
                    {
                        g.dispose();
                    }
                }
                else if (filter instanceof JPEGFileFilter)
                {
                    final BufferedImage image=new BufferedImage (size.width, size.height, BufferedImage.TYPE_3BYTE_BGR);
                    final Graphics g = image.getGraphics ();
                    g.setColor (Color.WHITE);
                    g.fillRect (0,0, size.width, size.height);
                    //vv.paintComponents (g);
                    vv.paintAll(g);
                    try (final OutputStream out = new FileOutputStream (outputFile))
                    {
                        final boolean ret = ImageIO.write(image, "jpg", out);
                        if (! ret)
                            System.err.println("no JPG writer");
                    }
                    catch (final Exception ex)
                    {
                        System.err.println(ex.getLocalizedMessage());
                    }
                    finally
                    {
                        g.dispose ();
                    }
                }
            }
        }
        catch (final AccessControlException ex)
        {
            JOptionPane.showMessageDialog (this, Localisation
                    .getString ("accessError"), Localisation
                    .getString ("accessErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
        vv.setDoubleBuffered (dbmode);
    }

    protected LZWEncContext ctx;
    protected Binary coder;

    protected String[] algo_text;
    protected int state;
    protected byte next_char;
    protected char read_char;
    protected int lzw_output;
    protected int input_bytes;

    protected LZWStatsPanel stats_panel;

    protected SparseTree graph;
    protected TreeLayout layout;
    protected VisualizationViewer vv;
    protected GraphZoomScrollPane pane;
    protected PluggableRenderer renderer;
    protected StringLabeller labeller;

    protected Node prev_node;
    protected Node current_node;
    protected Node added_node;
    protected Vertex current_vertex;

    static
    {
        final StringBuilder abc = new StringBuilder ();

        // This will insert all interesting characters but it makes the graph
        // too big to show.
        //for (char ch = '0'; ch <= '9'; ++ch)
        //    abc.append (ch);
        //for (char ch = 'a'; ch <= 'z'; ++ch)
        //{
        //    abc.append (ch);
        //    abc.append (Character.toUpperCase (ch));
        //}

        // Use smaller alphabet instead.
        for (char ch = 'a'; ch <= 'g'; ++ch)
            abc.append (ch);

        VALID = abc.toString ();
    }
    public static final String ROOT_NODE_LABEL = "R";
};

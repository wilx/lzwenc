/**
 *
 */
package lzwenc;

import java.awt.FlowLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import wilx.utils.S;

/**
 * @author Václav Haisman
 *
 */
public
class LZWStatsPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    LZWStatsPanel ()
    {
        input_label = new JLabel ();
        input_label.setText ("  " + S.str_("Input:"));
        input_value = new JLabel ();
        input_value.setText ("0b");

        output_label = new JLabel ();
        output_label.setText (S.str_("Output:"));
        output_value = new JLabel ();
        output_value.setText ("0b");

        ratio_label = new JLabel ();
        ratio_label.setText (S.str_("Compression ration:"));
        ratio_value = new JLabel ();
        ratio_value.setText ("N/A");


        setLayout (new FlowLayout ());
        //this.setSize (300, 200);
        this.add (input_label, null);
        this.add (input_value, null);
        this.add (output_label, null);
        this.add (output_value, null);
        this.add (ratio_label, null);
        this.add (ratio_value, null);


        reset ();
    }

    public
    void
    reset ()
    {
        input_bits = 0;
        output_bits = 0;

        input_value.setText (input_bits + "b");
        output_value.setText (output_bits + "b");
        ratio_value.setText ("N/A");
    }

    public
    void
    addOutputBits (final int bits)
    {
        final DecimalFormat df = new DecimalFormat ("##.##");
        output_bits += bits;
        output_value.setText (output_bits + "b");
        ratio_value.setText
            (output_bits == 0 ? "N/A" : df.format ((double)output_bits / input_bits));
    }

    public
    void
    addInputBits (final int bits)
    {
        final DecimalFormat df = new DecimalFormat("##.##");
        input_bits += bits;
        input_value.setText (input_bits + "b");
        ratio_value.setText
            (output_bits == 0 ? "N/A" : df.format ((double)output_bits / input_bits));
    }

    public
    int
    getOutputBits ()
    {
        return output_bits;
    }

    public
    int
    getInputBits ()
    {
        return input_bits;
    }

    protected int input_bits;
    protected int output_bits;

    protected JLabel input_label;
    protected JLabel input_value;
    protected JLabel output_label;
    protected JLabel output_value;
    protected JLabel ratio_label;
    protected JLabel ratio_value;
}

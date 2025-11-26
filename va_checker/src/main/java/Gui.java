import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.ProcessBuilder;
import java.util.ArrayList;
import java.util.HashMap;


// date: 2021-04-22
// 1) CHANGE: reworked how tf and cb are generated, now done by one method (setFormRow()). It generates complete 
// set of tf and corresponding cb /cbg with generic local names inside method.
// MOTIVATION: no need to assign variable names for tf/cb + fewer HashMaps. -> significantly less code overall.
//2) replaced Strings (names) in some hashmaps with Gen.NAMES[0...6]

/**
 * The {@code Gui} class is used to create a custom instance of the {@code Window} class.
 *<p>
 * The custom instance holds several {@code Panel}, {@code TextArea}, {@code Button}, {@code Label}, {@code Checkbox} and {@code CheckboxGroup}
 * objects. It extends {@code Frame} and implements {@code WindowListener}, {@code ItemListener} and {@code Actio Listener}.
 *<p>
 *Gui provides the basis for the Graphical User Interface of {@code VA Prüfer}<br>
 *(Verwaltungsakt Prüfer = Administrative act checker).<br>
 *VA Prüfer can be used to automatically generate a Report on the status of a legal note.
 *<p>
 *The following code is used to instantiate Gui with window dimensions of width = 1200 pixels, height = 940 pixels.
 *If the setLayout argument is {@code null}, elements will be added one after the other horizontally until the width limit is reached, additional
 *Elements will be placed in a new line. If the setVisible parameter is {@code false} the window will be invisible to the user.
 *
 *<hr><blockquote><pre>
 * Gui window = new Gui("VA Prüfer");
 window.setSize(1200, 940);
 window.setLayout(null);
 window.setVisible(true);
 * </pre></blockquote><hr>
 *
 *@author Edi Softic
 *
 */
public class Gui extends Frame implements WindowListener, ItemListener, ActionListener {
    // private static final long serialVersionUID = 1;
    // boolean fileFound;

    // Maps
    ArrayList<Panel> FormRows = new ArrayList<Panel>();
    HashMap<String, TextField> nameToTextField = new HashMap<String, TextField>();
    HashMap <String, String> file_data = new HashMap <String, String>();
    HashMap <String, String> nameToKey = new HashMap <String, String>();
    HashMap <String, String> keyToName= new HashMap <String, String>();
    HashMap<String, String> fileValueToGuiValue = new HashMap<String, String>();
    String path;
    String errors = "";


    // Panels -> p_sform = superpanel of p_form...
    Panel p_main = new Panel(null);
    Panel p_sform = new Panel(null);
    Panel p_sverh = new Panel(null);
    Panel p_form = new Panel(null);
    Panel p_verh = new Panel(null);


    // TextAreas
    TextArea ta_errors = new TextArea();
    TextArea ta_report = new TextArea();


    // TextFields
    TextField tf_verh = new TextField(30);
    TextField tf_geeig = new TextField(30);
    TextField tf_erford = new TextField(30);
    TextField tf_angem = new TextField(30);
    TextField tf_form = new TextField(30);
    TextField tf_recht = new TextField(30);
    TextField tf_begr = new TextField(30);

    TextField tf_loaded_file = new TextField(200);


    // Labels
    Label lb_loaded_file = new Label("Currently Loaded File: ");
    Label lb_report = new Label("- - - -  Report  - - - -");
    Label lb_errors = new Label("- - - -  Errors  - - - -");
    Label lb_checkpoints = new Label("- - - -  Checkpoints  - - - -");

    // Buttons
    Button b_check = new Button("Check");
    Button b_reset = new Button("Reset");
    Button b_load = new Button("Load File");
    Button b_edit = new Button("Edit File");

    // Checkbox for class methods()
    Checkbox cb_ini = new Checkbox();


    // interface demands class implement these -> dummy methods.
    public void windowOpened(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}

    /**
     *
     *@param  title  the {@code String title} to set the window title.
     */
    // Constructor
    public Gui(String title) {
        super(title); // super = method (in this case: constructor) of parent class is called

        // maps Checkboxes which logically belong to Top Node to TextFields which display Top Node name.
        // If time left: create a class that produces panels with content: 1 tf + n*cb. -> through .getComponent()
        // map those cb to the tf and has method to add that mapping to the global Gui-Map.
        // -> maybe no cb/tf variables necessary at all -> generic obj that only carry cb.Label -> Implemented
        // in this version!
        nameToTextField.put(Gen.NAMES[0], tf_verh);
        nameToTextField.put(Gen.NAMES[1], tf_geeig);
        nameToTextField.put(Gen.NAMES[2], tf_erford);
        nameToTextField.put(Gen.NAMES[3], tf_angem);
        nameToTextField.put(Gen.NAMES[4], tf_form);
        nameToTextField.put(Gen.NAMES[5],tf_recht);
        nameToTextField.put(Gen.NAMES[6], tf_begr);

        // maps TextField text to keys expected by Gen and vice versa for fileToForm().
        // maps value format in file (y, n, u) -> value format in Gui (True, False, Undef)
        setNameToKey();
        setKeyToName();
        setFileValueToGuiValue();

        // needed to close window
        addWindowListener(this); // this = self

        int b_pdl = 50;
        int b_pdt = 80;
        int b_w = 80;
        int b_h = 50;

        int p_pdl = 50;
        int p_pdt = 180;
        int p_w = 340;
        int p_h = 190;
        int tf_h = 30;

        b_check.setBounds(b_pdl, b_pdt, b_w, b_h);
        b_check.addActionListener(this);
        b_check.setActionCommand("check");
        add(b_check);


        b_load.setBounds(b_pdl+980, b_pdt+10, b_w-20, tf_h);
        b_load.addActionListener(this);
        b_load.setActionCommand("load");
        add(b_load);

        b_edit.setBounds(b_pdl+130, b_pdt,b_w, b_h);
        b_edit.addActionListener(this);
        b_edit.setActionCommand("edit");
        add(b_edit);

        b_reset.setBounds(b_pdl+260, b_pdt, b_w, b_h);
        b_reset.addActionListener(this);
        b_reset.setActionCommand("reset");
        add(b_reset);

        // setting up labels
        lb_loaded_file.setBounds(b_pdl+400, b_pdt, b_w+50, b_h);
        tf_loaded_file.setBounds(b_pdl+540, b_pdt+13, b_w+360, b_h-25);
        tf_loaded_file.setEditable(false);
        lb_report.setBounds(b_pdl+655, b_pdt+60, b_w+50, b_h-15);
        lb_errors.setBounds(b_pdl+655, b_pdt+460, b_w+50, b_h-15);
        lb_checkpoints.setBounds(b_pdl+101, b_pdt+60, b_w+60, b_h-15);
        add(lb_loaded_file);
        add(tf_loaded_file);
        add(lb_report);
        add(lb_errors);
        add(lb_checkpoints);


        // same as in setting up buttons, but for panels
        // p_s... = super panel, which contains other panel.
        p_sverh.setBounds(p_pdl, p_pdt, p_w, p_h);
        p_verh.setBounds(5, 5, 330, 180);
        p_sform.setBounds(p_pdl, p_pdt+200, p_w, p_h-35);
        p_form.setBounds(5, 5, 330, 145);
        p_sverh.setBackground(Color.gray);
        p_verh.setBackground(Color.white);
        p_sform.setBackground(Color.gray);
        p_form.setBackground(Color.white);

        ta_report.setBounds(455, 180, 695, 356);
        ta_report.setVisible(false);
        ta_report.setEditable(false);
        add(ta_report);

        ta_errors.setBounds(455, 580,695,320);
        ta_errors.setVisible(false);
        ta_errors.setEditable(false);
        add(ta_errors);

        p_main.setBounds(0,0,1000, 880);
        p_sform.add(p_form);
        p_main.add(p_sform);
        add(p_main);
        p_sverh.add(p_verh);
        p_main.add(p_sverh);

        int ini = 5;
        int ini_ = 5;
        int tf_sep = 45;

        //Setting up generic cb + tf in panel
        setFormRow(p_verh, Gen.NAMES[0], ini, ini_);
        setFormRow(p_verh, Gen.NAMES[1], ini, ini_+tf_sep);
        setFormRow(p_verh, Gen.NAMES[2], ini,ini_+2*tf_sep);
        setFormRow(p_verh, Gen.NAMES[3], ini, ini_+3*tf_sep);

        setFormRow(p_form, Gen.NAMES[4], ini, ini_);
        setFormRow(p_form,Gen.NAMES[5], ini, ini_+tf_sep);
        setFormRow(p_form, Gen.NAMES[6], ini, ini_+2*tf_sep);
    }

    // using panels to organize elements.
    // superpane overrides setBounds parameters of child panels keeping relative
    // distances between child panels, as defined in their own setBounds, intact!
    // -> indenting sub panels relative to top panel  possible :)
    //components in super panel need to be bounded to near (0,0) otherwise not centered.
    // p_verh is component of super panel -> use small left/top margin to make setting
    // bounds for superpanel easier, because once panel is inside superpanel every bound
    // is relative.
    // p_sverh provides "Border" for p_verh, by having slightly bigger rectangle and different background-color
    // -> added p_verh to p_sverh, so whole thing can be moved easily.
    // IMPORTANT: p_sverh needs to be added to window before p_verh, otherwise p_sverh covers p_verh.

    // core method, sets up almost all checkboxes + listeners + textFields +cbgs inside panels
    // -> tf and cb are now related by same .getParent()!

    /**
     * This custom method generates one {@code TextField}, three instances of {@code Checkbox}, {@code ItemListener}
     * for each Checkbox, one {@code CheckboxGroup, adds those {@code Component} to a {@code Panel} and adds
     * the panel to a class List for reference. Since {@code awt} does not have Radiobuttons, the behaviour is emulated
     * using Checkboxes added to a CheckboxGroup. One such Panel corresponds to one selection Field in the Form created
     * with {@code Guia}.
     * <p>
     * Each element's Bounds are set manually inside the panel to maximize flexibility, using a {@code Layout}
     * for the panel is adviced for reliable Display across devices.
     * <p>The advantage of this class compared to adding these elements to the {@code Window} directly
     * is, not having to assign a unique variable name to every Component (generic variable names).
     * Also, since {@code awt} does not provide methods to make the state of a {@code TextField} depend
     * on Checkboxes, this implementation enables logical linking by using the {@code getParent()} method
     * and getComponents() method.
     * <p>
     * The following code will generate a Panel containing a TextField with the Name = "textField" and
     * content = "textField", with both top- and left-padding of 50 pixels. The panel gets added to
     * the superpanel, which can be added to the java.awt.Window inside this method or in the constructor
     * of the java.awt.Window.
     * <hr><blockquote><pre>
     * setFormRow(superpanel, textField, 50,50)
     * </pre></blockquote><hr>
     *
     *
     *
     *
     *
     * @param		superpanel 		passes the Panel that will hold the Panel containing the TextField
     * 							    with the corresponding Checkboxes.
     * @param        tf_text	   sets the Name and Text of the generic TextField.
     * @param 			ppl		   sets padding-left for the generic panel.
     * @param 			ppt		   sets padding-top for the generic panel.
     */
    public void setFormRow(Panel superpanel, String tf_text, int p_pdl, int p_pdt) {
        Panel panel = new Panel(null);
        panel.setName(tf_text);
        TextField tf = new TextField(tf_text);
        tf.setName(tf_text);
        CheckboxGroup cbg = new CheckboxGroup();
        Checkbox a = new Checkbox("True", cbg, false);
        Checkbox b = new Checkbox("False", cbg, false);
        Checkbox c = new Checkbox("Undef", cbg, true);
        a.addItemListener(this);
        b.addItemListener(this);
        c.addItemListener(this);
        panel.setBounds(p_pdl, p_pdt, 400, 30);
        tf.setBounds(0,0,120,30);
        tf.setEditable(false);
        int a_pdl = 140;
        int a_pdt = 0;
        int a_w = 50;
        int a_h = 30;
        int cb_sep = 60;
        a.setBounds(a_pdl, a_pdt, a_w, a_h);
        b.setBounds(a_pdl+cb_sep, a_pdt, a_w, a_h);
        c.setBounds(a_pdl+2*cb_sep, a_pdt, a_w, a_h);
        a.setName(tf_text+"_True");
        b.setName(tf_text+"_False");
        c.setName(tf_text+"_Undef");
        panel.add(tf);
        panel.add(a);
        panel.add(b);
        panel.add(c);
        superpanel.add(panel);
        FormRows.add(panel);
        nameToTextField.put(tf_text, tf);

    }
    public void setFileValueToGuiValue() {
        fileValueToGuiValue.put("y", "True");
        fileValueToGuiValue.put("n", "False");
        fileValueToGuiValue.put("u", "Undef");
    }

    public void setNameToKey() {
        for(int i = 0; i < Gen.VALID_KEYS.length; i++)
        {
            nameToKey.put(Gen.NAMES[i], Gen.VALID_KEYS[i]);
        }
    }

    public void setKeyToName() {
        for(int i = 0; i < Gen.VALID_KEYS.length; i++)
        {
            keyToName.put(Gen.VALID_KEYS[i], Gen.NAMES[i]);
        }
    }

    public void fileToForm() {
        // dummy event for efficiency purposes -> create ItemEvent Obj once and swap source after every iteration.
        // no method to swap other elements(like cb_ini.getLabel()), BUT those are not relevant for functionality!
        ItemEvent e = new ItemEvent(cb_ini, ItemEvent.ITEM_STATE_CHANGED, cb_ini.getLabel(), ItemEvent.DESELECTED);
        for(String key : file_data.keySet()) {
            TextField tf = nameToTextField.get(keyToName.get(key));
            String val = file_data.get(key);
            String GuiVal = fileValueToGuiValue.get(val);
            for (Panel panel : FormRows) {
                for(Component comp : panel.getComponents()) {
                    if (comp.getClass() == cb_ini.getClass()) {
                        cb_ini = ((Checkbox)comp);
                        // if cb and ft ave the same parent (=in same panel), they must belong together
                        // if the cb, has the gui pendant of the filedata value it must be the cb and tf to be
                        // set according to the loaded file.
                        if (cb_ini.getParent() == tf.getParent() && cb_ini.getLabel().equals(GuiVal)) {
                            cb_ini.getCheckboxGroup().setSelectedCheckbox(cb_ini);
                            e.setSource(cb_ini);
                            itemStateChanged(e);
                        }
                    }
                }
            }
        }
    }

    // generates file_data from Form, cb in cbg holds the critical information.
    // -> TODO: Test Class: test for appropriate class
    public HashMap<String, String> getCheckBoxData() {
        Checkbox test = new Checkbox();
        for (Panel panel: FormRows) {
            String key = nameToKey.get(panel.getName());
            for (Component com : panel.getComponents()) {
                if (com.getClass() == test.getClass() && ((Checkbox)com).getState() == true) {
                    String cb_name = com.getName();
                    if (cb_name.contains("True")) {
                        file_data.put(key, "y");
                    }
                    else if (cb_name.contains("False")) {
                        file_data.put(key, "n");
                    }
                    else if (cb_name.contains("Undef")) {
                        file_data.put(key, "u");
                    }
                }
            }
        }
        return file_data;
    }

    // when check is clicked, names are mapped to expected keys, and values are mapped to expected
    // vales (y,n,u). From those, file_data hashmap is generated and passed to genOutput.
    // of Gen that does not include calling the Parser. BUT might be good to call at least Parser.validate()
    // to ensure file_data map contains only valid values / keys.
    public void check() {
        //ta_errors.setText(""); // optional: hide errors.
        ta_report.setVisible(true);
        ta_report.setText(Gen.genOutput(this.getCheckBoxData()));
        ta_report.setEditable(false);
        ta_report.setVisible(true);
    }

    public void load() {
        FileDialog filedialog = new FileDialog(this, "Choose a file", FileDialog.LOAD);
        filedialog.setDirectory(System.getProperty("user.dir")); // current working directory
        filedialog.setFile("*.txt");
        filedialog.setVisible(true);
        // getFile() returns only filename of the selected file -> for complete path getDirectory() is also needed.
        path = filedialog.getDirectory() + filedialog.getFile();
        // "nullnull" because both dir and getFile() can return null and are concatenated.
        // path is reassigned to null, to avoid issues with edit(), which expects null or valid path.
        if (path.equals("nullnull")) {
            path = null;
            ta_errors.setText("loading file cancelled");
        }
        else {
            // in case user reloads files multiple times, errors must be cleared to prevent errors accumulating
            // in the Parsers error log List.
            tf_loaded_file.setText(path);
            file_data.putAll(Parser.parse(path)); // presets valid entries from file_data in GUI
            ta_errors.setVisible(true);
            StringBuilder errors = new StringBuilder();
            for(String error : Parser.errors) {
                errors.append(error).append("\n");
            }
            ta_report.setText(""); // ensures no traces of potential previous checks remain
            ta_errors.setText(""); // ensures no traces of potential previous checks remain
            ta_errors.setText(errors.toString());
            fileToForm();
        }
    }
    public void edit() {
        if (path != null && !path.isEmpty()) {
            ProcessBuilder pb = new ProcessBuilder("C:/Windows/notepad.exe", path);
            try {
                pb.start();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        // this case occurs when a file has already been loaded and the user attempts loading another, but
        // cancels. -> edit() should open the file (last successfully opened) that is still written
        // in tf_loaded_file. Otherwise edit() will receive null as path.
        else if (path == null && !tf_loaded_file.getText().isEmpty()) {
            ProcessBuilder pb = new ProcessBuilder("C:/Windows/notepad.exe", tf_loaded_file.getText());
            try {
                pb.start();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else if(tf_loaded_file.getText().isEmpty() || path.isEmpty()) {
            ta_errors.setText("file not loaded");
            ta_errors.setVisible(true);
        }
    }

    public void reset() {
        ta_report.setText("");
        ta_report.setVisible(false);
        path = "";
        ta_errors.setText("");
        ta_errors.setVisible(false);
        errors = "";
        tf_loaded_file.setText("");
        // dummy event for efficiency purposes -> create ItemEvent Obj once and only swap source after every iter.
        // no method to swap other elements(like cb_ini.getLabel()), BUT those are not relevant for functionality!
        ItemEvent e = new ItemEvent(cb_ini, ItemEvent.ITEM_STATE_CHANGED, cb_ini.getLabel(), ItemEvent.DESELECTED);
        for (Panel panel : FormRows) {
            for(Component comp : panel.getComponents()) {
                if (comp.getClass() == cb_ini.getClass()) {
                    if (((Checkbox)comp).getLabel().equals("Undef")) {
                        cb_ini = ((Checkbox)comp);
                        break;
                    }
                }
            }
            cb_ini.getCheckboxGroup().setSelectedCheckbox(cb_ini);
            e.setSource(cb_ini);
            itemStateChanged(e);
        }
    }
    // called when a button has been clicked
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("check")) {
            check();
        }
        else if (e.getActionCommand().equals("load")) {
            load();
        }
        else if (e.getActionCommand().equals("edit")) {
            edit();
        }
        else if (e.getActionCommand().equals("reset")) {
            reset();
        }
    }
    // e.getSource() returns an Object which we know is a checkbox (TODO: Test class)
    // gets currently selected checkbox from cbg and looks up corresponding tf, which is mapped
    // in cbgToTextField. Switches background color of TextField according to Label (True, False, Undef)
    // (= represents Node value) of selected cb.
    public void itemStateChanged(ItemEvent e) {
        Checkbox cb_source = (Checkbox) e.getSource();
        if (cb_source.getName().contains("True")) {
            nameToTextField.get(cb_source.getParent().getName()).setBackground(Color.green);
        }
        else if (cb_source.getName().contains("False")) {
            nameToTextField.get(cb_source.getParent().getName()).setBackground(Color.red);
        }
        if (cb_source.getName().contains("Undef")) {
            nameToTextField.get(cb_source.getParent().getName()).setBackground(null);
        }
    }
    public void windowClosing(WindowEvent e) {
        dispose();
        System.exit(0); //closes window, returns 0 if executed without error -> convention.
    }


}
package simulator;

import algorithms.ExplorationAlgo;
import algorithms.FastestPathAlgo;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import utilities.CommMgr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.FileSystems;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.Font;

import javax.swing.JTextField;

import java.awt.FlowLayout;

import static utilities.MapDescriptor.createMapDescriptor;
import static utilities.MapDescriptor.loadMap;

/**
 * Simulator application for robot exploration and navigation in a virtual arena.
 *
 * @author MDP Group 3
 */

public class UIlayout_v2 extends JFrame implements ActionListener {
    /**
     *
     */
    private static UIlayout_v2 instance;
    private static final long serialVersionUID = 1L;
    private static final String EXPLORE_PANEL = "Explore arena";
    private static final String FFP_PANEL = "Find fastest path";

    private String mapNum;
    private static int speed; 
    private static JFrame _appFrame = null;         // application JFrame
    private static JFrame main_frame;
    private static JPanel _mapCards = null;         // JPanel for map views
    private static JPanel _buttons = null;          // JPanel for buttons

    private JTextField[] explore_TextFields, ffp_TextFields;
    private JTextField loadmap_TextField;
    private JButton exploreBtn, ffpBtn,loadMapButton;

    private JPanel arena_panel,input_panel,loadmap_panel;
    private JPanel main_panel;
    private static Robot bot;

    private static Map actualMap = null;              // real map
    private static Map exploredMap = null;          // map of the explored parts of the arena

    private static int timeLimit = 360;            // time limit in seconds
    private static int coverageLimit = 300;         // coverage limit in number of cells

    private static final CommMgr comm = CommMgr.getCommMgr();
    private static final boolean actualRun = false;
    private static boolean fastest_ready= false;


    /**
     * Initialises the different maps and displays the application.
     */
    public void start() {
        if (actualRun) comm.startConnection();

        bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, actualRun);

        if (!actualRun) {
            actualMap = new Map(bot);
            actualMap.setExploredArea();
        }

        exploredMap = new Map(bot);
        exploredMap.setAndIncExploredArea();

        displayEverything();
    }

    /**
     * Initialises the different parts of the application.
     */
    private void displayEverything() {

        //initialise the main frame
        
        
        main_frame = new JFrame();
        main_frame.setTitle("Simulator_Group 3");
        main_frame.setSize(new Dimension(1200,700));
        main_frame.setResizable(true);
        main_frame.setVisible(true);
        main_frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        main_panel = new JPanel();
        main_panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        main_panel.setLayout(new BorderLayout(0,0));

        arena_panel = new JPanel(new CardLayout());

        main_frame.setContentPane(main_panel);
        initContent(main_panel);
        main_frame.pack(); 
        
        
    }

    private void initContent(JPanel contentPane) {
        
        //Add panel for arena
        arena_panel.setPreferredSize(new Dimension(600,700));
        if (!actualRun) {
            arena_panel.add(actualMap, "REAL_MAP");
        }
        arena_panel.add(exploredMap, "EXPLORATION");

        CardLayout cl = ((CardLayout) arena_panel.getLayout());
        if (!actualRun) {
            cl.show(arena_panel, "REAL_MAP");
        } else {
            cl.show(arena_panel, "EXPLORATION");
        }

        //Add input panel
        input_panel = new JPanel(new BorderLayout());
        input_panel.setBorder(new EmptyBorder(50,20,50,20));

        /*String comboBoxItems[] = { EXPLORE_PANEL, FFP_PANEL };
        JComboBox control_switch = new JComboBox(comboBoxItems);
        control_switch.setFont(new Font("Tahoma", Font.BOLD, 16));
        control_switch.setEditable(false);
        control_switch.setActionCommand("switchComboBox");
        control_switch.addActionListener(this);
        input_panel.add(control_switch, BorderLayout.NORTH);*/
        
        

        // -------------------------Add control panel for exploring.---------------------------
        JLabel[] exploreCtrlLabels = new JLabel[3];
        explore_TextFields = new JTextField[3];
                
        //-----------------explore button---------------
        exploreBtn = new JButton("Explore");
        if(actualRun){
            exploreBtn.setEnabled(false);
        }
        else{
            exploreBtn.setActionCommand("ExploreMaze");
            exploreBtn.addActionListener(this);
        }//end exploreBtn
        
        exploreCtrlLabels[0] = new JLabel("Speed (steps/sec): ");
        exploreCtrlLabels[1] = new JLabel("Target coverage (%): ");
        exploreCtrlLabels[2] = new JLabel("Time limit (sec): ");
        for (int i = 0; i < 3; i++) {
            explore_TextFields[i] = new JTextField(10);
            if (actualRun) {
                explore_TextFields[i].setEditable(false);
            }
        }
        
        JPanel exploreInputPane = new JPanel(new GridLayout(3, 2));
        
        exploreInputPane.add(exploreCtrlLabels[0]);
        exploreInputPane.add(explore_TextFields[0]);
        exploreInputPane.add(exploreCtrlLabels[1]);
        exploreInputPane.add(explore_TextFields[1]);
        exploreInputPane.add(exploreCtrlLabels[2]);
        exploreInputPane.add(explore_TextFields[2]);
        
        if (!actualRun) {
            
            exploreCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
            explore_TextFields[0].setText("10");
            explore_TextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
            explore_TextFields[0].getDocument().putProperty("name", "Robot Explore Speed");
            
            exploreCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
            explore_TextFields[1].setText("100");
            explore_TextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
            explore_TextFields[1].getDocument().putProperty("name", "Target Coverage");
            
            exploreCtrlLabels[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
            explore_TextFields[2].setText("360");
            explore_TextFields[2].setFont(new Font("Tahoma", Font.PLAIN, 14));
            explore_TextFields[2].getDocument().putProperty("name", "Robot Explore Time Limit");
        }
                
        JPanel exploreBtnPane = new JPanel();
        exploreBtnPane.add(exploreBtn);

        JPanel exploreCtrlPane = new JPanel(new BorderLayout());
        exploreCtrlPane.setPreferredSize(new Dimension(400,200));

        //exploration label
        JLabel explr_label = new JLabel("Exploration");
        explr_label.setFont(new Font("Tahoma", Font.BOLD, 16));
        exploreCtrlPane.add(explr_label, BorderLayout.NORTH);

        exploreCtrlPane.add(exploreInputPane, BorderLayout.CENTER);
        exploreCtrlPane.add(exploreBtnPane, BorderLayout.SOUTH);
        exploreCtrlPane.setBorder(new EmptyBorder(20, 20, 20, 20));
//-------------------------end explore--------------------------------------------

        

        //Add control panel for fastest path
        JLabel[] ffpCtrlLabels = new JLabel[2];
        ffp_TextFields = new JTextField[2];

         //---------------fastest path button--------------------
        ffpBtn = new JButton("Navigate");   
        if (actualRun) {
            ffpBtn.setEnabled(false);
        } //end if
        else {
            ffpBtn.setActionCommand("FindFastestPath");
            ffpBtn.addActionListener(this);
        }//end else

        ffpCtrlLabels[0] = new JLabel("Speed (steps/sec): ");
        ffpCtrlLabels[1] = new JLabel("Time limit (sec): ");
        for (int i = 0; i < 2; i++) {
            ffp_TextFields[i] = new JTextField(10);
            if (actualRun) {
                ffp_TextFields[i].setEditable(false);
            }
        }

        JPanel ffpInputPane = new JPanel(new GridLayout(2, 2));
        ffpInputPane.add(ffpCtrlLabels[0]);
        ffpInputPane.add(ffp_TextFields[0]);
        ffpInputPane.add(ffpCtrlLabels[1]);
        ffpInputPane.add(ffp_TextFields[1]);
        
        if (!actualRun) {
            ffpCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
            ffp_TextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
            ffp_TextFields[0].setEditable(false);
    
            ffp_TextFields[1].setText("120");
            ffpCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
            ffp_TextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
            ffp_TextFields[1].getDocument().putProperty("name", "Robot FFP Time Limit");
    }

        JPanel ffpBtnPane = new JPanel();
        ffpBtnPane.add(ffpBtn);

        JPanel ffpCtrlPane = new JPanel(new BorderLayout());
        ffpCtrlPane.setPreferredSize(new Dimension(400,100));

        //Fastest path label
        JLabel ftp_label = new JLabel("Fastest Path Navigation");
        ftp_label.setFont(new Font("Tahoma", Font.BOLD, 16));
        ffpCtrlPane.add(ftp_label,BorderLayout.NORTH);

        ffpCtrlPane.add(ffpInputPane,BorderLayout.CENTER);
        ffpCtrlPane.add(ffpBtnPane,BorderLayout.SOUTH);
        ffpCtrlPane.setBorder(new EmptyBorder(20, 20, 20, 20));

        
        
        // Add card panel to switch between explore and shortest path panels.
        /*JPanel cardPane = new JPanel(new CardLayout());
        cardPane.add(exploreCtrlPane, EXPLORE_PANEL);
        cardPane.add(ffpCtrlPane, FFP_PANEL);
        cardPane.setPreferredSize(new Dimension(280, 300));
        input_panel.add(cardPane, BorderLayout.CENTER);*/

        input_panel.add(exploreCtrlPane,BorderLayout.NORTH);
        input_panel.add(ffpCtrlPane,BorderLayout.CENTER);

        //Add loadmap panel within input panel
        loadmap_panel = new JPanel(new BorderLayout());

        JPanel loadmap_console = new JPanel(new GridLayout(1, 2));        
        JLabel loadmapLabel = new JLabel("Load Map:");
        loadmap_TextField = new JTextField();
        if(!actualRun){
        loadmap_TextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
        loadmap_TextField.setText("Map2");
        loadmap_TextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
        loadmap_TextField.getDocument().putProperty("name", "Robot Initial Position");
        }
            
        JPanel loadBtnPanel = new JPanel();
        loadMapButton = new JButton("Load");
        if(actualRun){
            loadMapButton.setEnabled(false);
        }
        else{
            loadMapButton.setActionCommand("loadMap");
            loadMapButton.addActionListener(this);
        }//end exploreBtn

        loadBtnPanel.add(loadMapButton);
        loadmap_console.add(loadmapLabel); 
        loadmap_console.add(loadmap_TextField);
        loadmap_panel.add(loadmap_console, BorderLayout.CENTER);
        loadmap_panel.add(loadBtnPanel, BorderLayout.SOUTH);
        input_panel.add(loadmap_panel,BorderLayout.SOUTH);


        contentPane.add(input_panel, BorderLayout.EAST);
        contentPane.add(arena_panel, BorderLayout.WEST);
        
    } //end initContent


    public void switchComboBox(JComboBox cb, JPanel cardPanel) {
        CardLayout cardLayout = (CardLayout) (cardPanel.getLayout());
        cardLayout.show(cardPanel, (String) cb.getSelectedItem());
    }//end switchComboBox


        //----------------------------------------------------------------------------
         // FastestPath Class for Multithreading
        class FastestPath extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.revalidate();

                if (actualRun) {
                    while (true) {
                        System.out.println("Waiting for START_FAST_PATH...");
                        String msg = comm.receiveMsg();
                        if (msg.equals(CommMgr.START_FAST_PATH)) break;
                    }
                }

                FastestPathAlgo fastestPath;
                fastestPath = new FastestPathAlgo(exploredMap, bot);

                fastestPath.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
                //exploredMap.repaint();
                return 222;
            }
        }
        //----------------------------------------------------------------------------------

        
        // Exploration Class for Multithreading
        class Exploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                actualMap.revalidate();
                int row, col;
                fastest_ready = true;
                row = RobotConstants.START_ROW;
                col = RobotConstants.START_COL;

                bot.setRobotPos(row, col);
                exploredMap.revalidate();

                ExplorationAlgo exploration;
                exploration = new ExplorationAlgo(exploredMap, actualMap, bot, coverageLimit, timeLimit, speed);

                if (actualRun) {
                    CommMgr.getCommMgr().sendMsg(null, CommMgr.ROBOT_START);
                }

                exploration.runExploration();
                createMapDescriptor(exploredMap);

                if (actualRun) {
                    new FastestPath().execute();
                }

                return 111;
            }
        }
        //------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        String cmd = e.getActionCommand();
        /*if(cmd.matches("switchComboBox")){
            JComboBox cb = (JComboBox) e.getSource();
            JPanel cardPanel = (JPanel) input_panel.getComponent(1);
            switchComboBox(cb, cardPanel);
        }*/

        if(cmd.matches("loadMap")){
            mapNum = loadmap_TextField.getText();
            System.out.print(mapNum);
            loadMap(actualMap, mapNum);
            CardLayout cl = ((CardLayout) arena_panel.getLayout());
            cl.show(arena_panel, "REAL_MAP");
            actualMap.revalidate();
        }


        if(cmd.matches("ExploreMaze")){
            speed = Integer.parseInt(explore_TextFields[0].getText());
            timeLimit = Integer.parseInt(explore_TextFields[2].getText());
            coverageLimit = (int) ((Integer.parseInt(explore_TextFields[1].getText())) * MapConstants.NUM_CELLS / 100.0);
            CardLayout c2 = ((CardLayout) arena_panel.getLayout());
            c2.show(arena_panel, "EXPLORATION");
            new Exploration().execute();
        }

        if(cmd.matches("FindFastestPath")){
            actualMap.revalidate();
            CardLayout c3 = ((CardLayout) arena_panel.getLayout());
            c3.show(arena_panel, "EXPLORATION");
            new FastestPath().execute();
        }
    }
    
public static UIlayout_v2 getInstance() {
		if (instance == null) {
			instance = new UIlayout_v2();
		}
		return instance;
	}


}

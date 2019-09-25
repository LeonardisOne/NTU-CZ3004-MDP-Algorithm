package simulator;

import algorithms.ExplorationAlgo;
import algorithms.FastestPathAlgo;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.MOVEMENT;
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

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import java.awt.Font;
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
    private static boolean stop_explore = false;
    private JTextField[] explore_TextFields, ffp_TextFields;
    private JRadioButton [] speed_RadioButtons = new JRadioButton[5];

    private JTextField loadmap_TextField;
    private JButton exploreBtn, ffpBtn,loadMapButton,stop_button, resetButton;

    private static JLabel display_coverage;
    private static JLabel display_timeRemaining;


    private JPanel arena_panel,input_panel,loadmap_panel;
    private JPanel main_panel;
    private static Robot bot;

    private static Map actualMap = null;              // real map
    private static Map exploredMap = null;          // map of the explored parts of the arena

    private static int timeLimit = 3600;            // time limit in seconds
    private static int coverageLimit = 300;         // coverage limit in number of cells

    private  static CommMgr comm = CommMgr.getCommMgr();
    private static boolean actualRun = false;


    /**
     * Initialises the different maps and displays the application.
     */
    public void start() {
        if (actualRun) 
            comm.startConnection();

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


        // -------------------------Add control panel for exploring---------------------------
        JPanel exploration_main = new JPanel(new BorderLayout());
        JLabel[] exploreCtrlLabels = new JLabel[2];
        explore_TextFields = new JTextField[2];
                
        //-----------------explore button---------------
        exploreBtn = new JButton("Explore");
        exploreBtn.setPreferredSize(new Dimension(150,30));
        stop_button = new JButton("Stop Exploration");
        stop_button.setPreferredSize(new Dimension(150,30));
        stop_button.setActionCommand("Stop_Exploration");
        stop_button.addActionListener(this);
        if(actualRun){
            exploreBtn.setActionCommand("ExploreMaze_actual");
            exploreBtn.addActionListener(this);
        }
        else{
            exploreBtn.setActionCommand("ExploreMaze");
            exploreBtn.addActionListener(this);
        }//end exploreBtn
        
        exploreCtrlLabels[0] = new JLabel("Target coverage (%): ");
        exploreCtrlLabels[1] = new JLabel("Time limit (sec): ");
        for (int i = 0; i < 2; i++) {
            explore_TextFields[i] = new JTextField(10);
            if (actualRun) {
                explore_TextFields[i].setEditable(false);
            }
        }

        //add radio buttons for speed
        JPanel radioButton_panel = new JPanel(new GridLayout(5,2));
        speed_RadioButtons[0] = new JRadioButton("20");
        speed_RadioButtons[0].addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                speed = 20;
                speed_RadioButtons[1].setSelected(false);
                speed_RadioButtons[2].setSelected(false);
                speed_RadioButtons[3].setSelected(false);
                speed_RadioButtons[4].setSelected(false);
            }
        });

        speed_RadioButtons[1] = new JRadioButton("40");
        speed_RadioButtons[1].addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                speed = 40;
                speed_RadioButtons[0].setSelected(false);
                speed_RadioButtons[2].setSelected(false);
                speed_RadioButtons[3].setSelected(false);
                speed_RadioButtons[4].setSelected(false);

            }
        });

        speed_RadioButtons[2] = new JRadioButton("60");
        speed_RadioButtons[2].addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                speed = 60;
                speed_RadioButtons[1].setSelected(false);
                speed_RadioButtons[0].setSelected(false);
                speed_RadioButtons[3].setSelected(false);
                speed_RadioButtons[4].setSelected(false);

            }
        });

        speed_RadioButtons[3] = new JRadioButton("80");
        speed_RadioButtons[3].addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                speed = 80;
                speed_RadioButtons[1].setSelected(false);
                speed_RadioButtons[2].setSelected(false);
                speed_RadioButtons[0].setSelected(false);
                speed_RadioButtons[4].setSelected(false);

            }
        });

        speed_RadioButtons[4] = new JRadioButton("100");
        speed_RadioButtons[4].addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                speed = 100;
                speed_RadioButtons[1].setSelected(false);
                speed_RadioButtons[2].setSelected(false);
                speed_RadioButtons[3].setSelected(false);
                speed_RadioButtons[0].setSelected(false);

            }
        });


        JLabel[] speed_Labels = new JLabel[5];
        speed_Labels[0] = new JLabel("    Speed (steps/sec): ");
        speed_Labels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
        speed_Labels[1] = new JLabel("");
        speed_Labels[2] = new JLabel("");
        speed_Labels[3] = new JLabel("");
        speed_Labels[4] = new JLabel("");

        radioButton_panel.add(speed_Labels[0]);
        radioButton_panel.add(speed_RadioButtons[0]);
        radioButton_panel.add(speed_Labels[1]);
        radioButton_panel.add(speed_RadioButtons[1]);
        radioButton_panel.add(speed_Labels[2]);
        radioButton_panel.add(speed_RadioButtons[2]);
        radioButton_panel.add(speed_Labels[3]);
        radioButton_panel.add(speed_RadioButtons[3]);
        radioButton_panel.add(speed_Labels[4]);
        radioButton_panel.add(speed_RadioButtons[4]);


        JLabel explr_label = new JLabel("Exploration");
        explr_label.setFont(new Font("Tahoma", Font.BOLD, 16));
        exploration_main.add(explr_label, BorderLayout.NORTH);
        exploration_main.add(radioButton_panel, BorderLayout.CENTER);

        //end radio buttons portion

        JPanel exploreInputPane = new JPanel(new GridLayout(2, 2));
        
        exploreInputPane.add(exploreCtrlLabels[0]);
        exploreInputPane.add(explore_TextFields[0]);
        exploreInputPane.add(exploreCtrlLabels[1]);
        exploreInputPane.add(explore_TextFields[1]);

        

            
        exploreCtrlLabels[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
        explore_TextFields[0].setText("100");
        explore_TextFields[0].setFont(new Font("Tahoma", Font.PLAIN, 14));
        explore_TextFields[0].getDocument().putProperty("name", "Target Coverage");
           
        exploreCtrlLabels[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
        explore_TextFields[1].setText("360");
        explore_TextFields[1].setFont(new Font("Tahoma", Font.PLAIN, 14));
        explore_TextFields[1].getDocument().putProperty("name", "Robot Explore Time Limit");
            

        
                
        JPanel exploreBtnPane = new JPanel(new BorderLayout());
        exploreBtnPane.add(exploreBtn,BorderLayout.WEST);
        exploreBtnPane.add(stop_button,BorderLayout.EAST);

        JPanel exploreCtrlPane = new JPanel(new BorderLayout());
        exploreCtrlPane.setPreferredSize(new Dimension(400,200));

        //exploration label
        //exploreCtrlPane.add(explr_label, BorderLayout.NORTH);

        exploreCtrlPane.add(exploreInputPane, BorderLayout.CENTER);
        exploreCtrlPane.add(exploreBtnPane, BorderLayout.SOUTH);
        exploreCtrlPane.setBorder(new EmptyBorder(20, 20, 20, 20));


        exploration_main.add(exploreCtrlPane, BorderLayout.SOUTH);
//-------------------------end explore--------------------------------------------

//add a panel display current coverage% and currently explored map
        
        JPanel mid_Panel = new JPanel(new BorderLayout());
        JPanel status_panel = new JPanel(new BorderLayout());

        JPanel statusConsole = new JPanel(new GridLayout(2, 2));
		statusConsole.setPreferredSize(new Dimension(280, 100));
        JLabel coverage_progress = new JLabel("Coverage (%): ");
        coverage_progress.setFont(new Font("Tahoma", Font.PLAIN, 14));

        JLabel time_remaining = new JLabel("Time left (sec): ");
        time_remaining.setFont(new Font("Tahoma", Font.PLAIN, 14));

        display_coverage = new JLabel("0%");
        display_timeRemaining = new JLabel("360");

        statusConsole.add(coverage_progress);
        statusConsole.add(display_coverage);
        statusConsole.add(time_remaining);
        statusConsole.add(display_timeRemaining);
        status_panel.add(statusConsole);

        mid_Panel.add(status_panel,BorderLayout.NORTH);

        //Add control panel for fastest path
        JLabel[] ffpCtrlLabels = new JLabel[2];
        ffp_TextFields = new JTextField[2];

         //---------------fastest path button--------------------
        ffpBtn = new JButton("Navigate");   
        if (actualRun) {
            ffpBtn.setActionCommand("FindFastestPath_actual");
            ffpBtn.addActionListener(this);
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
        mid_Panel.add(ffpCtrlPane,BorderLayout.CENTER);
        mid_Panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        
        // Add card panel to switch between explore and shortest path panels.
        /*JPanel cardPane = new JPanel(new CardLayout());
        cardPane.add(exploreCtrlPane, EXPLORE_PANEL);
        cardPane.add(ffpCtrlPane, FFP_PANEL);
        cardPane.setPreferredSize(new Dimension(280, 300));
        input_panel.add(cardPane, BorderLayout.CENTER);*/

        input_panel.add(exploration_main,BorderLayout.NORTH);
        input_panel.add(mid_Panel,BorderLayout.CENTER);

        //Add loadmap panel within input panel
        loadmap_panel = new JPanel(new BorderLayout());

        JPanel loadmap_console = new JPanel(new GridLayout(1, 2));        
        JLabel loadmapLabel = new JLabel("Load Map:");
        loadmap_TextField = new JTextField();
        if(!actualRun){
        loadmap_TextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
        loadmap_TextField.setText("Map3");
        loadmap_TextField.setFont(new Font("Tahoma", Font.PLAIN, 14));
        loadmap_TextField.getDocument().putProperty("name", "Robot Initial Position");
        }
            
        JPanel loadBtnPanel = new JPanel();
        loadMapButton = new JButton("Load");
        resetButton = new JButton("Reset Map");

        if(actualRun){
            loadMapButton.setEnabled(false);
            resetButton.setEnabled(false);
        }
        else{
            loadMapButton.setActionCommand("loadMap");
            loadMapButton.addActionListener(this);
            resetButton.setActionCommand("resetMap");
            resetButton.addActionListener(this);
        }//end exploreBtn

        loadBtnPanel.add(loadMapButton,BorderLayout.WEST);
        loadBtnPanel.add(resetButton,BorderLayout.EAST);
        loadmap_console.add(loadmapLabel); 
        loadmap_console.add(loadmap_TextField);
        loadmap_panel.add(loadmap_console, BorderLayout.CENTER);
        loadmap_panel.add(loadBtnPanel, BorderLayout.SOUTH);
        input_panel.add(loadmap_panel,BorderLayout.SOUTH);


        contentPane.add(input_panel, BorderLayout.EAST);
        contentPane.add(arena_panel, BorderLayout.WEST);
        
    } //end initContent

    //----------------------------------------------------------------------------
        // FastestPath Class for Multithreading
        static class FastestPath extends SwingWorker<Integer, String> {
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
        static class Exploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {

                int row, col;

                row = RobotConstants.START_ROW;
                col = RobotConstants.START_COL;

                bot.setRobotPos(row, col);
                exploredMap.revalidate();

                ExplorationAlgo exploration;
                exploration = new ExplorationAlgo(exploredMap, actualMap, bot, coverageLimit, timeLimit,speed);
                
                if (actualRun) {
                    CommMgr.getCommMgr().sendMsg(null, CommMgr.ROBOT_START);
                }

                exploration.runExploration();
               
                createMapDescriptor(exploredMap);

                if (actualRun) {
                    new FastestPath().execute();
                }
                //send robot position
                //comm.sendMsg(bot.getRobotPosRow() + "," + bot.getRobotPosCol() + "," + RobotConstants.DIRECTION.print(bot.getRobotCurDir()), CommMgr.ROBOT_POS);

                return 111;
            }
        }


        // TimeExploration Class for Multithreading
        static class TimeExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.revalidate();

                ExplorationAlgo timeExplo = new ExplorationAlgo(exploredMap, actualMap, bot, coverageLimit, timeLimit,speed);
                timeExplo.runExploration();

                createMapDescriptor(exploredMap);

                return 333;
            }
        }

        // CoverageExploration Class for Multithreading
        static class CoverageExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.revalidate();
                System.out.println("doing coverage"+ coverageLimit);
                ExplorationAlgo coverageExplo = new ExplorationAlgo(exploredMap, actualMap, bot, coverageLimit, timeLimit,speed);
                coverageExplo.runExploration();

                createMapDescriptor(exploredMap);

                return 444;
            }
        }


    @Override
    public void actionPerformed(ActionEvent e) {
        //------------------------------------------------------------
        // TODO Auto-generated method stub
        String cmd = e.getActionCommand();
        
        if(cmd.matches("resetMap")){
            bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
            unSelectspeed();
            if (!actualRun) {
                actualMap = new Map(bot);
                actualMap.setExploredArea();
            }
    
            exploredMap = new Map(bot);
            exploredMap.setAndIncExploredArea();
            System.out.println("reseting Map...");
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
        }

        if(cmd.matches("loadMap")){
            mapNum = loadmap_TextField.getText();
            System.out.print(mapNum);
            loadMap(actualMap, mapNum);
            CardLayout cl = ((CardLayout) arena_panel.getLayout());
            cl.show(arena_panel, "REAL_MAP");
            actualMap.revalidate();
        }

        if(cmd.matches("Stop_Exploration")){
            stop_explore=true;
        }

        if(cmd.matches("ExploreMaze")){
            coverageLimit = (int) ((Integer.parseInt(explore_TextFields[0].getText())) * 300 / 100.0);
            timeLimit = Integer.parseInt(explore_TextFields[1].getText());

            if(checkSpeedRadio()){
                if(coverageLimit<0 || coverageLimit > 300){
                    JOptionPane.showMessageDialog(null, "Invalid Coverage!!");
                }
                else if(coverageLimit==300){
                    CardLayout c2 = ((CardLayout) arena_panel.getLayout());
                    c2.show(arena_panel, "EXPLORATION");
                    new Exploration().execute();
                }
                else if(timeLimit<0){
                    System.out.println("testing time limit");
                    JOptionPane.showMessageDialog(null, "Invalid Time Limit!!");
                }
                else if(timeLimit>0 && timeLimit<360){
                    if(timeLimit % 1!=0){
                        JOptionPane.showMessageDialog(null, "Time Limit must be in Integer");
                    }
                    else{
                        CardLayout cl = ((CardLayout) arena_panel.getLayout());
                        cl.show(arena_panel, "EXPLORATION");
                        new TimeExploration().execute();
                    }
                }
                else{
                    new CoverageExploration().execute();
                    CardLayout coverage = ((CardLayout) arena_panel.getLayout());
                    coverage.show(arena_panel, "EXPLORATION");
                }
            }//end if(checkspeedRadio())
            else{
                JOptionPane.showMessageDialog(null, "Please select robot speed!!");
            }
        }

        if(cmd.matches("ExploreMaze_actual")){
            speed = 100 ;
            timeLimit = 3600;
            coverageLimit = 100;
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


        if(cmd.matches("FindFastestPath_actual")){
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
    
    public boolean checkSpeedRadio(){
        if(speed_RadioButtons[0].isSelected() ||
           speed_RadioButtons[1].isSelected() ||
           speed_RadioButtons[2].isSelected() ||
           speed_RadioButtons[3].isSelected() ||
           speed_RadioButtons[4].isSelected()){
            return true;
        }
        else
            return false;
    }
    public void unSelectspeed(){
        speed_RadioButtons[0].setSelected(false);
        speed_RadioButtons[1].setSelected(false);
        speed_RadioButtons[2].setSelected(false);
        speed_RadioButtons[3].setSelected(false);
        speed_RadioButtons[4].setSelected(false);

    }

    public void setCoverageUpdate (Float coverage) {
		display_coverage.setText(String.format("%.1f", coverage));
	}

    public void setTimer (int timeLeft) {
		display_timeRemaining.setText(Integer.toString(timeLeft));
	}
}

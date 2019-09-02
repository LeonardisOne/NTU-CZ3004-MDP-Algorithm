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

import static utilities.MapDescriptor.createMapDescriptor;
import static utilities.MapDescriptor.loadMap;

/**
 * Simulator application for robot exploration and navigation in a virtual arena.
 *
 * @author MDP Group 3
 */

public class SimulatorApp {
    private static JFrame _appFrame = null;         // application JFrame

    private static JPanel _mapCards = null;         // JPanel for map views
    private static JPanel _buttons = null;          // JPanel for buttons

    private static Robot bot;

    private static Map actualMap = null;              // real map
    private static Map exploredMap = null;          // map of the explored parts of the arena

    private static int timeLimit = 3600;            // time limit in seconds
    private static int coverageLimit = 300;         // coverage limit in number of cells

    private static final CommMgr comm = CommMgr.getCommMgr();
    private static final boolean actualRun = false;

    /**
     * Initialises the different maps and displays the application.
     */
    public static void main(String[] args) {
        if (actualRun) comm.startConnection();

        bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, actualRun);

        if (!actualRun) {
            actualMap = new Map(bot);
            //actualMap.setAllUnexplored();
            actualMap.setExploredArea();
        }

        exploredMap = new Map(bot);
        //exploredMap.setAllUnexplored();
        exploredMap.setAndIncExploredArea();

        displayEverything();
    }

    /**
     * Initialises the different parts of the application.
     */
    private static void displayEverything() {
        // Initialise main frame for display
        _appFrame = new JFrame();
        _appFrame.setTitle("MDP Group 3 Simulator");
        _appFrame.setSize(new Dimension(690, 700));
        _appFrame.setResizable(false);

        // Center the main frame in the middle of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        _appFrame.setLocation(dim.width / 2 - _appFrame.getSize().width / 2, dim.height / 2 - _appFrame.getSize().height / 2);

        // Create the CardLayout for storing the different maps
        _mapCards = new JPanel(new CardLayout());

        // Create the JPanel for the buttons
        _buttons = new JPanel();

        // Add _mapCards & _buttons to the main frame's content pane
        Container contentPane = _appFrame.getContentPane();
        contentPane.add(_mapCards, BorderLayout.CENTER);
        contentPane.add(_buttons, BorderLayout.PAGE_END);

        // Initialize the main map view
        initMainLayout();

        // Initialize the buttons
        initButtonsLayout();

        // Display the application
        _appFrame.setVisible(true);
        _appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Initialises the main map view by adding the different maps as cards in the CardLayout. Displays actualMap
     * by default.
     */
    private static void initMainLayout() {
        if (!actualRun) {
            _mapCards.add(actualMap, "REAL_MAP");
        }
        _mapCards.add(exploredMap, "EXPLORATION");

        CardLayout cl = ((CardLayout) _mapCards.getLayout());
        if (!actualRun) {
            cl.show(_mapCards, "REAL_MAP");
        } else {
            cl.show(_mapCards, "EXPLORATION");
        }
    }

    /**
     * Initialises the JPanel for the buttons.
     */
    private static void initButtonsLayout() {
        _buttons.setLayout(new GridLayout());
        addButtons();
    }

    /**
     * Helper method to create a JButton with a default font.
     * 
     * The default font is Arial, size 13 in Bold.
     */
    private static JButton makeButton(String btnText) {
        JButton btn = new JButton(btnText);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        return btn;
    }

    /**
     * Helper method to create a JDialog with default size and layout.
     * 
     * The default size is width 400, height 60.
     */
    private static JDialog makeDialog(String headerText, String displayText){
        JDialog dialog = new JDialog(_appFrame, headerText, true);
        dialog.setSize(400, 60);
        dialog.setLayout(new FlowLayout());
        dialog.add(new JLabel(displayText));
        return dialog;
    }

    /**
     * Initialises and adds the five main buttons. Also creates the relevant classes (for multithreading) and JDialogs
     * (for user input) for the different functions of the buttons.
     */
    private static void addButtons() {
        if (!actualRun) {
            // Load Map Button
            JButton btn_LoadMap = makeButton("Load Map");
            btn_LoadMap.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    JDialog loadMapDialog = makeDialog("Load Map", "File Name: ");

                    final JTextField loadTF = new JTextField(15);
                    JButton loadMapButton = new JButton("Load");

                    loadMapButton.addMouseListener(new MouseAdapter() {
                        public void mousePressed(MouseEvent e) {
                            loadMapDialog.setVisible(false);
                            loadMap(actualMap, loadTF.getText());
                            CardLayout cl = ((CardLayout) _mapCards.getLayout());
                            cl.show(_mapCards, "REAL_MAP");
                            actualMap.repaint();
                        }
                    });

                    loadMapDialog.add(loadTF);
                    loadMapDialog.add(loadMapButton);
                    loadMapDialog.setVisible(true);
                }
            });
            _buttons.add(btn_LoadMap);
        }

        // FastestPath Class for Multithreading
        class FastestPath extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.repaint();

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

                return 222;
            }
        }

        // Exploration Class for Multithreading
        class Exploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                int row, col;

                row = RobotConstants.START_ROW;
                col = RobotConstants.START_COL;

                bot.setRobotPos(row, col);
                exploredMap.repaint();

                ExplorationAlgo exploration;
                exploration = new ExplorationAlgo(exploredMap, actualMap, bot, coverageLimit, timeLimit);

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

        // Exploration Button
        JButton btn_Exploration = makeButton("Exploration");
        btn_Exploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "EXPLORATION");
                new Exploration().execute();
            }
        });
        _buttons.add(btn_Exploration);

        // Fastest Path Button
        JButton btn_FastestPath = makeButton("Fastest Path");
        btn_FastestPath.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "EXPLORATION");
                new FastestPath().execute();
            }
        });
        _buttons.add(btn_FastestPath);


        // TimeExploration Class for Multithreading
        class TimeExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.repaint();

                ExplorationAlgo timeExplo = new ExplorationAlgo(exploredMap, actualMap, bot, coverageLimit, timeLimit);
                timeExplo.runExploration();

                createMapDescriptor(exploredMap);

                return 333;
            }
        }

        // Time-limited Exploration Button
        JButton btn_TimeExploration = makeButton("Time-Limited");
        btn_TimeExploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JDialog timeExploDialog = makeDialog("Time-Limited Exploration", "Time Limit (in MM:SS): ");
                final JTextField timeTF = new JTextField(5);
                JButton timeSaveButton = new JButton("Run");

                timeSaveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        timeExploDialog.setVisible(false);
                        String time = timeTF.getText();
                        String[] timeArr = time.split(":");
                        timeLimit = (Integer.parseInt(timeArr[0]) * 60) + Integer.parseInt(timeArr[1]);
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "EXPLORATION");
                        new TimeExploration().execute();
                    }
                });

                timeExploDialog.add(timeTF);
                timeExploDialog.add(timeSaveButton);
                timeExploDialog.setVisible(true);
            }
        });
        _buttons.add(btn_TimeExploration);


        // CoverageExploration Class for Multithreading
        class CoverageExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                exploredMap.repaint();

                ExplorationAlgo coverageExplo = new ExplorationAlgo(exploredMap, actualMap, bot, coverageLimit, timeLimit);
                coverageExplo.runExploration();

                createMapDescriptor(exploredMap);

                return 444;
            }
        }

        // Coverage-limited Exploration Button
        JButton btn_CoverageExploration = makeButton("Coverage-Limited");
        btn_CoverageExploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JDialog coverageExploDialog = makeDialog("Coverage-Limited Exploration", "Coverage Limit (% of maze): ");
                final JTextField coverageTF = new JTextField(5);
                JButton coverageSaveButton = new JButton("Run");

                coverageSaveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        coverageExploDialog.setVisible(false);
                        coverageLimit = (int) ((Integer.parseInt(coverageTF.getText())) * MapConstants.NUM_CELLS / 100.0);
                        new CoverageExploration().execute();
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "EXPLORATION");
                    }
                });

                coverageExploDialog.add(coverageTF);
                coverageExploDialog.add(coverageSaveButton);
                coverageExploDialog.setVisible(true);
            }
        });
        _buttons.add(btn_CoverageExploration);
    }
}

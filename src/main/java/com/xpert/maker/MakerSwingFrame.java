package com.xpert.maker;

import com.xpert.faces.primefaces.PrimeFacesVersion;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 *
 * @author ayslan
 */
public class MakerSwingFrame extends javax.swing.JFrame {

    private static final String JAVA_PROJECT_PREFFIX = File.separator + "java";
    private static final Logger logger = Logger.getLogger(MakerSwingFrame.class.getName());
    private BeanConfiguration beanConfiguration;
    private ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
    private File lastFile;

    public String getDefaultPackage(){
        return null;
    }

    public String getDefaultTemplatePath(){
        return null;
    }

    public String getDefaultResourceBundle(){
        return null;
    }

    public String getDefaultBaseDAOImpl(){
        return null;
    }

    public String getManagedBeanSuffix(){
        return null;
    }

    public String getBusinessObjectSuffix(){
        return null;
    }

    public PrimeFacesVersion getPrimeFacesVersion() {
        return PrimeFacesVersion.VERSION_3;
    }

    /**
     * Creates new form MakerMainFrame
     */
    public MakerSwingFrame() {
        initCustomLayout();
        initComponents();
        initFromConfiguration();
    }

    public final void initCustomLayout() {
        //remove bold frm fonts
        Font oldLabelFont = UIManager.getFont("Label.font");
        UIManager.put("Label.font", oldLabelFont.deriveFont(Font.PLAIN));
    }

    public static void run(final MakerSwingFrame maker) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                maker.center();
                maker.setVisible(true);
            }
        });
    }
    
     /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ex) {
                    Logger.getLogger(MakerSwingFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                MakerSwingFrame makerSwingFrame = new MakerSwingFrame();
                makerSwingFrame.center();
                makerSwingFrame.setVisible(true);
            }
        });
    }

    public void searchClasses() {
        try {
            ArrayList<Class<?>> allClasses = ClassEnumerator.getClassesForPackage(textPackageName.getText());
            classes = new ArrayList<Class<?>>();
            for (Class entity : allClasses) {
                if (!entity.isEnum() && !entity.isInterface()) {
                    classes.add(entity);
                }
            }
            listClasses.setListData(classes.toArray(new Class[classes.size()]));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Message: " + ex.getMessage() + ". See java log for details", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void selectAll() {
        if (classes != null && !classes.isEmpty()) {
            int[] indices = new int[classes.size()];
            for (int i = 0; i < classes.size(); i++) {
                indices[i] = i;
            }
            listClasses.setSelectedIndices(indices);
        }
    }

    public void selectNone() {
        listClasses.setSelectedIndices(new int[0]);
    }

    public boolean validateField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, fieldName + " is required", "Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean validateConfiguration() {

        if (!validateField(beanConfiguration.getManagedBeanLocation(), "ManagedBean location")) {
            return false;
        }
        if (!validateField(beanConfiguration.getManagedBean(), "ManagedBean package")) {
            return false;
        }
        if (!validateField(beanConfiguration.getBusinessObjectLocation(), "Business Object (BO) location")) {
            return false;
        }
        if (!validateField(beanConfiguration.getBusinessObject(), "Business Object (BO) package")) {
            return false;
        }
        if (!validateField(beanConfiguration.getDaoLocation(), "DAO location")) {
            return false;
        }
        if (!validateField(beanConfiguration.getDao(), "DAO package")) {
            return false;
        }
        if (!validateField(beanConfiguration.getDaoImplLocation(), "DAO Implementation location")) {
            return false;
        }
        if (!validateField(beanConfiguration.getDaoImpl(), "DAO Implementation package")) {
            return false;
        }
        if (!validateField(beanConfiguration.getViewLocation(), "View location")) {
            return false;
        }
        if (!validateField(beanConfiguration.getTemplate(), "Facelets Template")) {
            return false;
        }

        return true;
    }

    public void generate() {

        Object[] selectedClasses = listClasses.getSelectedValues();

        if (selectedClasses == null || selectedClasses.length == 0) {
            JOptionPane.showMessageDialog(this, "No Classes Select", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Class> classesList = new ArrayList<Class>();
        for (Object object : selectedClasses) {
            classesList.add((Class) object);
        }

        beanConfiguration = new BeanConfiguration();
        beanConfiguration.setPrimeFacesVersion((PrimeFacesVersion) comboPrimeFacesVersion.getSelectedItem());
        beanConfiguration.setTemplate(textTemplatePath.getText());
        beanConfiguration.setAuthor(textAuthor.getText());
        beanConfiguration.setBaseDAO(textBaseDAOImpl.getText());
        beanConfiguration.setBusinessObject(textPackageBO.getText());
        beanConfiguration.setManagedBean(textPackageMB.getText());
        beanConfiguration.setDao(textPackageDAO.getText());
        beanConfiguration.setDaoImpl(textPackageDAOImpl.getText());
        beanConfiguration.setResourceBundle(textResourceBundle.getText());
        //location
        beanConfiguration.setManagedBeanLocation(textManagedBean.getText());
        beanConfiguration.setBusinessObjectLocation(textBusinessObject.getText());
        beanConfiguration.setDaoLocation(textDAO.getText());
        beanConfiguration.setDaoImplLocation(textDAOImpl.getText());
        beanConfiguration.setViewLocation(textView.getText());
        //suffix/preffix
        beanConfiguration.setManagedBeanSuffix(textManagedBeanSuffix.getText());
        beanConfiguration.setBusinessObjectSuffix(textBusinessObjectSuffix.getText());
        try {
            PersistenceMappedBean persistenceMappedBean = new PersistenceMappedBean(null);
            List<MappedBean> mappedBeans = persistenceMappedBean.getMappedBeans(classesList, beanConfiguration);
            textAreaI18n.setText(BeanCreator.getI18N(mappedBeans));
            textAreaClassBean.setText(BeanCreator.getClassBean(classesList, beanConfiguration));
            StringBuilder logBuilder = new StringBuilder();
            BeanCreator.writeBean(mappedBeans, beanConfiguration, logBuilder);
            textAreaLog.setText(logBuilder.toString());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Message: " + ex.getMessage() + ". See java log for details", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showFileChooser(JTextField textSelection, JTextField textPackage) {
        JFileChooser chooser = new JFileChooser();
        if (lastFile != null) {
            chooser.setCurrentDirectory(lastFile);
        } else {
            chooser.setCurrentDirectory(new java.io.File("."));
        }
        chooser.setDialogTitle("Select a Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (textSelection != null) {
                textSelection.setText(path);
            }
            if (textPackage != null) {
                int javaPathIndex = path.lastIndexOf(JAVA_PROJECT_PREFFIX);
                if (javaPathIndex > 0) {
                    int fullIndex = javaPathIndex + JAVA_PROJECT_PREFFIX.length() + 1;
                    if (path.length() > fullIndex) {
                        String javaPath = path.substring(fullIndex, path.length());
                        textPackage.setText(javaPath.replace(File.separator, "."));
                    }
                }
            }
            lastFile = chooser.getSelectedFile();
        }
    }

    public final void initFromConfiguration() {
        if (getDefaultPackage() != null) {
            textPackageName.setText(getDefaultPackage());
        }
        if (getDefaultTemplatePath() != null) {
            textTemplatePath.setText(getDefaultTemplatePath());
        } else {
            textTemplatePath.setText(BeanCreator.DEFAULT_TEMPLATE);
        }
        if (getDefaultResourceBundle() != null) {
            textResourceBundle.setText(getDefaultResourceBundle());
        } else {
            textResourceBundle.setText(BeanCreator.DEFAULT_RESOURCE_BUNDLE);
        }
        if (getDefaultBaseDAOImpl() != null) {
            textBaseDAOImpl.setText(getDefaultBaseDAOImpl());
        }
        if (getManagedBeanSuffix() != null) {
            textManagedBeanSuffix.setText(getManagedBeanSuffix());
        } else {
            textManagedBeanSuffix.setText(BeanCreator.SUFFIX_MANAGED_BEAN);
        }
        if (getBusinessObjectSuffix() != null) {
            textBusinessObjectSuffix.setText(getBusinessObjectSuffix());
        } else {
            textBusinessObjectSuffix.setText(BeanCreator.SUFFIX_BUSINESS_OBJECT);
        }

        textAuthor.setText(System.getProperty("user.name"));
        PrimeFacesVersion[] versions = PrimeFacesVersion.values();
        for (PrimeFacesVersion version : versions) {
            comboPrimeFacesVersion.addItem(version);
        }
        comboPrimeFacesVersion.setSelectedItem(getPrimeFacesVersion());
        comboPrimeFacesVersion.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    setText(((PrimeFacesVersion) value).getDescription());
                }
                return this;
            }
        });
    }

    public void center() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        this.setLocation((screenSize.width - this.getSize().width) / 2, (screenSize.height - this.getSize().height) / 2);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        tabbedPanelMain = new javax.swing.JTabbedPane();
        panelSelectClasses = new javax.swing.JPanel();
        scrollPaneSelectClasses = new javax.swing.JScrollPane();
        listClasses = new javax.swing.JList();
        labelSelectClasses = new javax.swing.JLabel();
        labelPackageName = new javax.swing.JLabel();
        textPackageName = new javax.swing.JTextField();
        buttonSearchClasses = new javax.swing.JButton();
        buttonSelectAll = new javax.swing.JButton();
        buttonSelectNone = new javax.swing.JButton();
        panelConfiguration = new javax.swing.JPanel();
        panelMB = new javax.swing.JPanel();
        labelMBPackage = new javax.swing.JLabel();
        textPackageMB = new javax.swing.JTextField();
        labelMBLocation = new javax.swing.JLabel();
        textManagedBean = new javax.swing.JTextField();
        buttonSelectMB = new javax.swing.JButton();
        panelOthers = new javax.swing.JPanel();
        labelXHTMLLocation = new javax.swing.JLabel();
        textView = new javax.swing.JTextField();
        buttonSelectView = new javax.swing.JButton();
        labelFaceletsTemplate = new javax.swing.JLabel();
        textTemplatePath = new javax.swing.JTextField();
        labelResourceBundle = new javax.swing.JLabel();
        textResourceBundle = new javax.swing.JTextField();
        labelAuthor = new javax.swing.JLabel();
        textAuthor = new javax.swing.JTextField();
        labelBaseDAOImpl = new javax.swing.JLabel();
        textBaseDAOImpl = new javax.swing.JTextField();
        labelMBSuffix = new javax.swing.JLabel();
        textManagedBeanSuffix = new javax.swing.JTextField();
        textBusinessObjectSuffix = new javax.swing.JTextField();
        labelBOSuffix = new javax.swing.JLabel();
        labelPrimeFacesVersion = new javax.swing.JLabel();
        comboPrimeFacesVersion = new javax.swing.JComboBox();
        panelBO = new javax.swing.JPanel();
        labelBOPackage = new javax.swing.JLabel();
        textPackageBO = new javax.swing.JTextField();
        textBusinessObject = new javax.swing.JTextField();
        buttonSelectBO = new javax.swing.JButton();
        labelBOLocation = new javax.swing.JLabel();
        panelDAO = new javax.swing.JPanel();
        labelDAOPackage = new javax.swing.JLabel();
        textPackageDAO = new javax.swing.JTextField();
        labelDAOLocation = new javax.swing.JLabel();
        textDAO = new javax.swing.JTextField();
        buttonSelectDAO = new javax.swing.JButton();
        panelDAOImpl = new javax.swing.JPanel();
        labelDAOImplPackage = new javax.swing.JLabel();
        labelDAOImplLocation = new javax.swing.JLabel();
        textDAOImpl = new javax.swing.JTextField();
        buttonSelectDAOImpl = new javax.swing.JButton();
        textPackageDAOImpl = new javax.swing.JTextField();
        panelCreateClasses = new javax.swing.JPanel();
        scrollPaneLog = new javax.swing.JScrollPane();
        textAreaLog = new javax.swing.JTextArea();
        labelLogMaker = new javax.swing.JLabel();
        buttonCreateClasses = new javax.swing.JButton();
        scrollPaneClassBean = new javax.swing.JScrollPane();
        textAreaClassBean = new javax.swing.JTextArea();
        labelI18N = new javax.swing.JLabel();
        labelClassBean = new javax.swing.JLabel();
        scrollPaneI18N = new javax.swing.JScrollPane();
        textAreaI18n = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Xpert-Maker");
        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        tabbedPanelMain.setBackground(new java.awt.Color(255, 255, 255));

        panelSelectClasses.setBackground(new java.awt.Color(255, 255, 255));

        scrollPaneSelectClasses.setViewportView(listClasses);

        labelSelectClasses.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelSelectClasses.setText("Classes:");

        labelPackageName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelPackageName.setText("Package Name:");

        buttonSearchClasses.setLabel("Search Classes");
        buttonSearchClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSearchClassesActionPerformed(evt);
            }
        });

        buttonSelectAll.setLabel("Select All");
        buttonSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectAllActionPerformed(evt);
            }
        });

        buttonSelectNone.setText("Select None");
        buttonSelectNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectNoneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSelectClassesLayout = new javax.swing.GroupLayout(panelSelectClasses);
        panelSelectClasses.setLayout(panelSelectClassesLayout);
        panelSelectClassesLayout.setHorizontalGroup(
            panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSelectClassesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSelectClassesLayout.createSequentialGroup()
                        .addComponent(labelSelectClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scrollPaneSelectClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 457, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelSelectClassesLayout.createSequentialGroup()
                                .addComponent(buttonSelectAll)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSelectNone)))
                        .addContainerGap(194, Short.MAX_VALUE))
                    .addGroup(panelSelectClassesLayout.createSequentialGroup()
                        .addComponent(labelPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSearchClasses)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        panelSelectClassesLayout.setVerticalGroup(
            panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSelectClassesLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPackageName)
                    .addComponent(textPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSearchClasses))
                .addGap(2, 2, 2)
                .addGroup(panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSelectClassesLayout.createSequentialGroup()
                        .addComponent(labelSelectClasses)
                        .addGap(0, 464, Short.MAX_VALUE))
                    .addComponent(scrollPaneSelectClasses))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSelectAll)
                    .addComponent(buttonSelectNone))
                .addGap(21, 21, 21))
        );

        tabbedPanelMain.addTab("Select Classes", panelSelectClasses);

        panelConfiguration.setBackground(new java.awt.Color(255, 255, 255));

        panelMB.setBackground(new java.awt.Color(255, 255, 255));
        panelMB.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Managed Bean", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(0, 51, 102))); // NOI18N
        panelMB.setPreferredSize(new java.awt.Dimension(723, 85));

        labelMBPackage.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelMBPackage.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelMBPackage.setLabelFor(textPackageMB);
        labelMBPackage.setText("Package:");

        textPackageMB.setToolTipText("Package for your Managed Bean. Example: com.yourproject.mb");
        textPackageMB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textPackageMBActionPerformed(evt);
            }
        });

        labelMBLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelMBLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelMBLocation.setLabelFor(textManagedBean);
        labelMBLocation.setText("Location:");

        buttonSelectMB.setText("...");
        buttonSelectMB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectMBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMBLayout = new javax.swing.GroupLayout(panelMB);
        panelMB.setLayout(panelMBLayout);
        panelMBLayout.setHorizontalGroup(
            panelMBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMBLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMBLayout.createSequentialGroup()
                        .addComponent(textManagedBean)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectMB, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(textPackageMB, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelMBPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelMBLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelMBLayout.setVerticalGroup(
            panelMBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMBLayout.createSequentialGroup()
                .addComponent(labelMBLocation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textManagedBean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectMB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelMBPackage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textPackageMB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        labelMBLocation.getAccessibleContext().setAccessibleParent(panelConfiguration);
        textManagedBean.getAccessibleContext().setAccessibleParent(panelConfiguration);
        buttonSelectMB.getAccessibleContext().setAccessibleParent(panelConfiguration);

        panelOthers.setBackground(new java.awt.Color(255, 255, 255));
        panelOthers.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Others", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(0, 51, 102))); // NOI18N
        panelOthers.setPreferredSize(new java.awt.Dimension(723, 158));

        labelXHTMLLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelXHTMLLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelXHTMLLocation.setLabelFor(textView);
        labelXHTMLLocation.setText("XHTML Location:");

        buttonSelectView.setText("...");
        buttonSelectView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectViewActionPerformed(evt);
            }
        });

        labelFaceletsTemplate.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelFaceletsTemplate.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelFaceletsTemplate.setLabelFor(textTemplatePath);
        labelFaceletsTemplate.setText("Facelets Template:");

        labelResourceBundle.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelResourceBundle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelResourceBundle.setLabelFor(textResourceBundle);
        labelResourceBundle.setText("Resource Bundle:");

        labelAuthor.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelAuthor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelAuthor.setLabelFor(textAuthor);
        labelAuthor.setText("Author:");

        labelBaseDAOImpl.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelBaseDAOImpl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelBaseDAOImpl.setLabelFor(textAuthor);
        labelBaseDAOImpl.setText("BaseDAOImpl:");

        labelMBSuffix.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelMBSuffix.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelMBSuffix.setLabelFor(textAuthor);
        labelMBSuffix.setText("Managed Bean Suffix:");

        labelBOSuffix.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelBOSuffix.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelBOSuffix.setLabelFor(textAuthor);
        labelBOSuffix.setText("Business Object Suffix:");

        labelPrimeFacesVersion.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelPrimeFacesVersion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelPrimeFacesVersion.setLabelFor(textAuthor);
        labelPrimeFacesVersion.setText("PrimeFaces Version:");

        javax.swing.GroupLayout panelOthersLayout = new javax.swing.GroupLayout(panelOthers);
        panelOthers.setLayout(panelOthersLayout);
        panelOthersLayout.setHorizontalGroup(
            panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOthersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOthersLayout.createSequentialGroup()
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelOthersLayout.createSequentialGroup()
                                .addComponent(textView, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSelectView, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(labelXHTMLLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelBaseDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textBaseDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelOthersLayout.createSequentialGroup()
                        .addComponent(textTemplatePath, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textResourceBundle, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelOthersLayout.createSequentialGroup()
                        .addComponent(labelFaceletsTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(161, 161, 161)
                        .addComponent(labelResourceBundle, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelOthersLayout.createSequentialGroup()
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textManagedBeanSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelMBSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelBOSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textBusinessObjectSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comboPrimeFacesVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelPrimeFacesVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelOthersLayout.setVerticalGroup(
            panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOthersLayout.createSequentialGroup()
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelXHTMLLocation)
                    .addComponent(labelBaseDAOImpl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectView)
                    .addComponent(textBaseDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFaceletsTemplate)
                    .addComponent(labelResourceBundle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textTemplatePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textResourceBundle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPrimeFacesVersion)
                    .addComponent(labelMBSuffix)
                    .addComponent(labelBOSuffix)
                    .addComponent(labelAuthor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboPrimeFacesVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textManagedBeanSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textBusinessObjectSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(92, 92, 92))
        );

        panelBO.setBackground(new java.awt.Color(255, 255, 255));
        panelBO.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DAO Implementation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(0, 51, 102))); // NOI18N
        panelBO.setPreferredSize(new java.awt.Dimension(723, 85));

        labelBOPackage.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelBOPackage.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelBOPackage.setLabelFor(textPackageBO);
        labelBOPackage.setText("Package:");

        textPackageBO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textPackageBOActionPerformed(evt);
            }
        });

        buttonSelectBO.setText("...");
        buttonSelectBO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectBOActionPerformed(evt);
            }
        });

        labelBOLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelBOLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelBOLocation.setLabelFor(textBusinessObject);
        labelBOLocation.setText("Location:");

        javax.swing.GroupLayout panelBOLayout = new javax.swing.GroupLayout(panelBO);
        panelBO.setLayout(panelBOLayout);
        panelBOLayout.setHorizontalGroup(
            panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBOLayout.createSequentialGroup()
                .addComponent(labelBOLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(panelBOLayout.createSequentialGroup()
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelBOPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelBOLayout.createSequentialGroup()
                        .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(textPackageBO, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                            .addComponent(textBusinessObject, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectBO, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        panelBOLayout.setVerticalGroup(
            panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBOLayout.createSequentialGroup()
                .addComponent(labelBOLocation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textBusinessObject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectBO))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelBOPackage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textPackageBO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelDAO.setBackground(new java.awt.Color(255, 255, 255));
        panelDAO.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DAO Implementation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(0, 51, 102))); // NOI18N
        panelDAO.setPreferredSize(new java.awt.Dimension(723, 85));

        labelDAOPackage.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelDAOPackage.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDAOPackage.setLabelFor(textPackageDAO);
        labelDAOPackage.setText("Package:");

        textPackageDAO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textPackageDAOActionPerformed(evt);
            }
        });

        labelDAOLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelDAOLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDAOLocation.setLabelFor(textDAO);
        labelDAOLocation.setText("Location:");

        buttonSelectDAO.setText("...");
        buttonSelectDAO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectDAOActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelDAOLayout = new javax.swing.GroupLayout(panelDAO);
        panelDAO.setLayout(panelDAOLayout);
        panelDAOLayout.setHorizontalGroup(
            panelDAOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDAOLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDAOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelDAOPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelDAOLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelDAOLayout.createSequentialGroup()
                        .addGroup(panelDAOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(textPackageDAO, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                            .addComponent(textDAO, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectDAO, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelDAOLayout.setVerticalGroup(
            panelDAOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDAOLayout.createSequentialGroup()
                .addComponent(labelDAOLocation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDAOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSelectDAO)
                    .addComponent(textDAO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelDAOPackage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textPackageDAO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        panelDAOImpl.setBackground(new java.awt.Color(255, 255, 255));
        panelDAOImpl.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DAO Implementation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(0, 51, 102))); // NOI18N
        panelDAOImpl.setPreferredSize(new java.awt.Dimension(723, 85));

        labelDAOImplPackage.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelDAOImplPackage.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDAOImplPackage.setLabelFor(textPackageDAOImpl);
        labelDAOImplPackage.setText("Package:");

        labelDAOImplLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelDAOImplLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDAOImplLocation.setLabelFor(textDAOImpl);
        labelDAOImplLocation.setText("Location:");

        buttonSelectDAOImpl.setText("...");
        buttonSelectDAOImpl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectDAOImplActionPerformed(evt);
            }
        });

        textPackageDAOImpl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textPackageDAOImplActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelDAOImplLayout = new javax.swing.GroupLayout(panelDAOImpl);
        panelDAOImpl.setLayout(panelDAOImplLayout);
        panelDAOImplLayout.setHorizontalGroup(
            panelDAOImplLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDAOImplLayout.createSequentialGroup()
                .addComponent(labelDAOImplLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(panelDAOImplLayout.createSequentialGroup()
                .addGroup(panelDAOImplLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDAOImplLayout.createSequentialGroup()
                        .addGroup(panelDAOImplLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelDAOImplPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(textPackageDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        panelDAOImplLayout.setVerticalGroup(
            panelDAOImplLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDAOImplLayout.createSequentialGroup()
                .addComponent(labelDAOImplLocation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDAOImplLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectDAOImpl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelDAOImplPackage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textPackageDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelConfigurationLayout = new javax.swing.GroupLayout(panelConfiguration);
        panelConfiguration.setLayout(panelConfigurationLayout);
        panelConfigurationLayout.setHorizontalGroup(
            panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConfigurationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelOthers, javax.swing.GroupLayout.DEFAULT_SIZE, 706, Short.MAX_VALUE)
                    .addGroup(panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelConfigurationLayout.createSequentialGroup()
                            .addComponent(panelDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelMB, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE))
                        .addGroup(panelConfigurationLayout.createSequentialGroup()
                            .addComponent(panelBO, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelDAO, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        panelConfigurationLayout.setVerticalGroup(
            panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConfigurationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelDAO, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                    .addComponent(panelBO, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelDAOImpl, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addComponent(panelMB, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelOthers, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(122, Short.MAX_VALUE))
        );

        tabbedPanelMain.addTab("Project Configuration", panelConfiguration);

        panelCreateClasses.setBackground(new java.awt.Color(255, 255, 255));

        textAreaLog.setColumns(20);
        textAreaLog.setRows(5);
        scrollPaneLog.setViewportView(textAreaLog);

        labelLogMaker.setText("Log:");

        buttonCreateClasses.setText("Create Classes");
        buttonCreateClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCreateClassesActionPerformed(evt);
            }
        });

        textAreaClassBean.setColumns(20);
        textAreaClassBean.setRows(5);
        scrollPaneClassBean.setViewportView(textAreaClassBean);

        labelI18N.setText("I18N Resource Bundle:");

        labelClassBean.setText("Class Bean:");

        textAreaI18n.setColumns(20);
        textAreaI18n.setRows(5);
        scrollPaneI18N.setViewportView(textAreaI18n);

        javax.swing.GroupLayout panelCreateClassesLayout = new javax.swing.GroupLayout(panelCreateClasses);
        panelCreateClasses.setLayout(panelCreateClassesLayout);
        panelCreateClassesLayout.setHorizontalGroup(
            panelCreateClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCreateClassesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCreateClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneLog)
                    .addComponent(scrollPaneClassBean, javax.swing.GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE)
                    .addGroup(panelCreateClassesLayout.createSequentialGroup()
                        .addGroup(panelCreateClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonCreateClasses)
                            .addComponent(labelLogMaker)
                            .addComponent(labelI18N)
                            .addComponent(labelClassBean))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(scrollPaneI18N, javax.swing.GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelCreateClassesLayout.setVerticalGroup(
            panelCreateClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCreateClassesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonCreateClasses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelLogMaker)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneLog, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelI18N)
                .addGap(1, 1, 1)
                .addComponent(scrollPaneI18N, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelClassBean)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneClassBean, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(65, Short.MAX_VALUE))
        );

        tabbedPanelMain.addTab("Create Classes", panelCreateClasses);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPanelMain)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPanelMain)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSearchClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSearchClassesActionPerformed
        searchClasses();
    }//GEN-LAST:event_buttonSearchClassesActionPerformed

    private void buttonSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectAllActionPerformed
        selectAll();
    }//GEN-LAST:event_buttonSelectAllActionPerformed

    private void buttonSelectNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectNoneActionPerformed
        selectNone();
    }//GEN-LAST:event_buttonSelectNoneActionPerformed

    private void buttonSelectViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectViewActionPerformed
        showFileChooser(textView, null);
    }//GEN-LAST:event_buttonSelectViewActionPerformed

    private void buttonSelectDAOImplActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectDAOImplActionPerformed
        showFileChooser(textDAOImpl, textPackageDAOImpl);
    }//GEN-LAST:event_buttonSelectDAOImplActionPerformed

    private void buttonSelectMBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectMBActionPerformed
        showFileChooser(textManagedBean, textPackageMB);
    }//GEN-LAST:event_buttonSelectMBActionPerformed

    private void textPackageMBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textPackageMBActionPerformed
    }//GEN-LAST:event_textPackageMBActionPerformed

    private void textPackageBOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textPackageBOActionPerformed
    }//GEN-LAST:event_textPackageBOActionPerformed

    private void buttonSelectBOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectBOActionPerformed
        showFileChooser(textBusinessObject, textPackageBO);
    }//GEN-LAST:event_buttonSelectBOActionPerformed

    private void textPackageDAOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textPackageDAOActionPerformed
    }//GEN-LAST:event_textPackageDAOActionPerformed

    private void buttonSelectDAOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectDAOActionPerformed
        showFileChooser(textDAO, textPackageDAO);
    }//GEN-LAST:event_buttonSelectDAOActionPerformed

    private void textPackageDAOImplActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textPackageDAOImplActionPerformed
    }//GEN-LAST:event_textPackageDAOImplActionPerformed

    private void buttonCreateClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCreateClassesActionPerformed
        generate();
    }//GEN-LAST:event_buttonCreateClassesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCreateClasses;
    private javax.swing.JButton buttonSearchClasses;
    private javax.swing.JButton buttonSelectAll;
    private javax.swing.JButton buttonSelectBO;
    private javax.swing.JButton buttonSelectDAO;
    private javax.swing.JButton buttonSelectDAOImpl;
    private javax.swing.JButton buttonSelectMB;
    private javax.swing.JButton buttonSelectNone;
    private javax.swing.JButton buttonSelectView;
    private javax.swing.JComboBox comboPrimeFacesVersion;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelAuthor;
    private javax.swing.JLabel labelBOLocation;
    private javax.swing.JLabel labelBOPackage;
    private javax.swing.JLabel labelBOSuffix;
    private javax.swing.JLabel labelBaseDAOImpl;
    private javax.swing.JLabel labelClassBean;
    private javax.swing.JLabel labelDAOImplLocation;
    private javax.swing.JLabel labelDAOImplPackage;
    private javax.swing.JLabel labelDAOLocation;
    private javax.swing.JLabel labelDAOPackage;
    private javax.swing.JLabel labelFaceletsTemplate;
    private javax.swing.JLabel labelI18N;
    private javax.swing.JLabel labelLogMaker;
    private javax.swing.JLabel labelMBLocation;
    private javax.swing.JLabel labelMBPackage;
    private javax.swing.JLabel labelMBSuffix;
    private javax.swing.JLabel labelPackageName;
    private javax.swing.JLabel labelPrimeFacesVersion;
    private javax.swing.JLabel labelResourceBundle;
    private javax.swing.JLabel labelSelectClasses;
    private javax.swing.JLabel labelXHTMLLocation;
    private javax.swing.JList listClasses;
    private javax.swing.JPanel panelBO;
    private javax.swing.JPanel panelConfiguration;
    private javax.swing.JPanel panelCreateClasses;
    private javax.swing.JPanel panelDAO;
    private javax.swing.JPanel panelDAOImpl;
    private javax.swing.JPanel panelMB;
    private javax.swing.JPanel panelOthers;
    private javax.swing.JPanel panelSelectClasses;
    private javax.swing.JScrollPane scrollPaneClassBean;
    private javax.swing.JScrollPane scrollPaneI18N;
    private javax.swing.JScrollPane scrollPaneLog;
    private javax.swing.JScrollPane scrollPaneSelectClasses;
    private javax.swing.JTabbedPane tabbedPanelMain;
    private javax.swing.JTextArea textAreaClassBean;
    private javax.swing.JTextArea textAreaI18n;
    private javax.swing.JTextArea textAreaLog;
    private javax.swing.JTextField textAuthor;
    private javax.swing.JTextField textBaseDAOImpl;
    private javax.swing.JTextField textBusinessObject;
    private javax.swing.JTextField textBusinessObjectSuffix;
    private javax.swing.JTextField textDAO;
    private javax.swing.JTextField textDAOImpl;
    private javax.swing.JTextField textManagedBean;
    private javax.swing.JTextField textManagedBeanSuffix;
    private javax.swing.JTextField textPackageBO;
    private javax.swing.JTextField textPackageDAO;
    private javax.swing.JTextField textPackageDAOImpl;
    private javax.swing.JTextField textPackageMB;
    private javax.swing.JTextField textPackageName;
    private javax.swing.JTextField textResourceBundle;
    private javax.swing.JTextField textTemplatePath;
    private javax.swing.JTextField textView;
    // End of variables declaration//GEN-END:variables
}

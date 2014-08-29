package com.xpert.maker;

import com.xpert.faces.bootstrap.BootstrapVersion;
import com.xpert.faces.primefaces.PrimeFacesVersion;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import com.xpert.Constants;

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

    public String getDefaultPackage() {
        return null;
    }

    public String getDefaultTemplatePath() {
        return null;
    }

    public String getDefaultResourceBundle() {
        return null;
    }

    public String getDefaultBaseDAOImpl() {
        return null;
    }

    public String getManagedBeanSuffix() {
        return null;
    }

    public String getBusinessObjectSuffix() {
        return null;
    }

    public PrimeFacesVersion getPrimeFacesVersion() {
        return PrimeFacesVersion.VERSION_3;
    }

    public BootstrapVersion getBootstrapVersion() {
        return null;
    }

    public boolean isUseCDIBeans() {
        return false;
    }

    public boolean isGeneratesSecurityArea() {
        return true;
    }

    /**
     * Creates new form MakerMainFrame
     */
    public MakerSwingFrame() {
        initComponents();
        initCustomLayout();
        initFromConfiguration();
    }

    public final void initCustomLayout() {
        createLabelTabbedPanel("Select Classes", 0);
        createLabelTabbedPanel("Project Configuration", 1);
        createLabelTabbedPanel("Create Classes", 2);

        //create links
        createHiperLink(labelLinkDocs, "https://code.google.com/p/xpert-framework/wiki/Download?tm=2", "https://code.google.com/p/xpert-framework/wiki/Download?tm=2");
        createHiperLink(labelLinkShowcase, "http://showcase.xpertsistemas.com.br/", "http://showcase.xpertsistemas.com.br/");
        createHiperLink(labelLinkXpertSistemas, "http://www.xpertsistemas.com.br/", "http://www.xpertsistemas.com.br/");
    }

    private void createHiperLink(JLabel label, final String url, String text) {
        label.setToolTipText("go to " + url);
        label.setText("<html><a href=\"\">" + text + "</a></html>");
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private void createLabelTabbedPanel(String title, int index) {

        JLabel labelTab = new JLabel(title);
        labelTab.setPreferredSize(new Dimension(130, 30));
        labelTab.setForeground(Color.WHITE);
        labelTab.setHorizontalAlignment(SwingConstants.CENTER);
        tabbedPanelMain.setTabComponentAt(index, labelTab);
    }

    public static void run(final MakerSwingFrame maker) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
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

        run(new MakerSwingFrame());
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
        if (comboBootstrapVersion.getSelectedItem() != null && comboBootstrapVersion.getSelectedItem() instanceof BootstrapVersion) {
            beanConfiguration.setBootstrapVersion((BootstrapVersion) comboBootstrapVersion.getSelectedItem());
        }
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
        beanConfiguration.setUseCDIBeans(checkUseCDIBeans.isSelected());
        beanConfiguration.setGeneratesSecurityArea(checkGeneratesSecurityArea.isSelected());
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
        checkUseCDIBeans.setSelected(isUseCDIBeans());
        checkGeneratesSecurityArea.setSelected(isGeneratesSecurityArea());
        textAuthor.setText(System.getProperty("user.name"));
        loadPrimefacesCombo();
        loadBootstrapCombo();

    }

    private void loadPrimefacesCombo() {
        PrimeFacesVersion[] versions = PrimeFacesVersion.values();
        comboPrimeFacesVersion.removeAllItems();
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

    private void loadBootstrapCombo() {
        BootstrapVersion[] versions = BootstrapVersion.values();
        comboBootstrapVersion.removeAllItems();
        comboBootstrapVersion.addItem(null);
        for (BootstrapVersion version : versions) {
            comboBootstrapVersion.addItem(version);
        }
        comboBootstrapVersion.setSelectedItem(getBootstrapVersion());
        comboBootstrapVersion.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null && value instanceof BootstrapVersion) {
                    setText(((BootstrapVersion) value).getDescription());
                } else {
                    setText("Don't use");
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

        jPanelMain = new javax.swing.JPanel();
        tabbedPanelMain = new javax.swing.JTabbedPane();
        panelSelectClasses = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        labelPackageName = new javax.swing.JLabel();
        textPackageName = new javax.swing.JTextField();
        buttonSearchClasses = new javax.swing.JButton();
        scrollPaneSelectClasses = new javax.swing.JScrollPane();
        listClasses = new javax.swing.JList();
        labelSelectClasses = new javax.swing.JLabel();
        buttonSelectAll = new javax.swing.JButton();
        buttonSelectNone = new javax.swing.JButton();
        labelSelectClasses1 = new javax.swing.JLabel();
        buttonTab1Next = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        labelLinkDocs = new javax.swing.JLabel();
        labelLinkShowcase = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        labelLinkXpertSistemas = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        panelConfiguration = new javax.swing.JPanel();
        panelOthers = new javax.swing.JPanel();
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
        checkGeneratesSecurityArea = new javax.swing.JCheckBox();
        checkUseCDIBeans = new javax.swing.JCheckBox();
        panelBO = new javax.swing.JPanel();
        labelBOPackage = new javax.swing.JLabel();
        textPackageBO = new javax.swing.JTextField();
        textBusinessObject = new javax.swing.JTextField();
        buttonSelectBO = new javax.swing.JButton();
        labelBOLocation = new javax.swing.JLabel();
        labelDAOLocation = new javax.swing.JLabel();
        textDAO = new javax.swing.JTextField();
        buttonSelectDAO = new javax.swing.JButton();
        labelDAOPackage = new javax.swing.JLabel();
        textPackageDAO = new javax.swing.JTextField();
        labelDAOImplLocation = new javax.swing.JLabel();
        labelDAOImplPackage = new javax.swing.JLabel();
        buttonSelectDAOImpl = new javax.swing.JButton();
        textDAOImpl = new javax.swing.JTextField();
        textPackageDAOImpl = new javax.swing.JTextField();
        labelMBLocation = new javax.swing.JLabel();
        textManagedBean = new javax.swing.JTextField();
        buttonSelectMB = new javax.swing.JButton();
        labelMBPackage = new javax.swing.JLabel();
        textPackageMB = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        labelXHTMLLocation = new javax.swing.JLabel();
        textView = new javax.swing.JTextField();
        buttonSelectView = new javax.swing.JButton();
        labelFaceletsTemplate = new javax.swing.JLabel();
        textTemplatePath = new javax.swing.JTextField();
        labelPrimeFacesVersion = new javax.swing.JLabel();
        comboPrimeFacesVersion = new javax.swing.JComboBox();
        labelBootstrapVersion = new javax.swing.JLabel();
        comboBootstrapVersion = new javax.swing.JComboBox();
        buttonTab2Back = new javax.swing.JButton();
        buttonTab2Next = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        panelCreateClasses = new javax.swing.JPanel();
        scrollPaneLog = new javax.swing.JScrollPane();
        textAreaLog = new javax.swing.JTextArea();
        scrollPaneClassBean = new javax.swing.JScrollPane();
        textAreaClassBean = new javax.swing.JTextArea();
        labelI18N = new javax.swing.JLabel();
        labelClassBean = new javax.swing.JLabel();
        scrollPaneI18N = new javax.swing.JScrollPane();
        textAreaI18n = new javax.swing.JTextArea();
        jPanel9 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        buttonTab3Back = new javax.swing.JButton();
        buttonCreateClasses = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Xpert-framework. Version " + Constants.VERSION
        );
        setBackground(new java.awt.Color(255, 255, 255));

        jPanelMain.setBackground(new java.awt.Color(239, 239, 239));

        tabbedPanelMain.setBackground(new java.awt.Color(102, 102, 255));
        tabbedPanelMain.setForeground(new java.awt.Color(255, 255, 255));
        tabbedPanelMain.setFocusable(false);
        tabbedPanelMain.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N

        panelSelectClasses.setBackground(new java.awt.Color(237, 242, 253));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Class Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(0, 0, 153))); // NOI18N

        labelPackageName.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelPackageName.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelPackageName.setText("Package Name:");

        textPackageName.setToolTipText("Java package of your classes");

        buttonSearchClasses.setBackground(new java.awt.Color(102, 102, 255));
        buttonSearchClasses.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        buttonSearchClasses.setForeground(new java.awt.Color(255, 255, 255));
        buttonSearchClasses.setLabel("Search Classes");
        buttonSearchClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSearchClassesActionPerformed(evt);
            }
        });

        listClasses.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        scrollPaneSelectClasses.setViewportView(listClasses);

        labelSelectClasses.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelSelectClasses.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelSelectClasses.setText("Classes found in package:");

        buttonSelectAll.setBackground(new java.awt.Color(102, 102, 255));
        buttonSelectAll.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        buttonSelectAll.setForeground(new java.awt.Color(255, 255, 255));
        buttonSelectAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        buttonSelectAll.setLabel("All");
        buttonSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectAllActionPerformed(evt);
            }
        });

        buttonSelectNone.setBackground(new java.awt.Color(102, 102, 255));
        buttonSelectNone.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        buttonSelectNone.setForeground(new java.awt.Color(255, 255, 255));
        buttonSelectNone.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        buttonSelectNone.setLabel("None");
        buttonSelectNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectNoneActionPerformed(evt);
            }
        });

        labelSelectClasses1.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelSelectClasses1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelSelectClasses1.setText("Select:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(labelSelectClasses1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectAll, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonSelectNone, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollPaneSelectClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 441, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(textPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSearchClasses))
                    .addComponent(labelSelectClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelPackageName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSearchClasses))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelSelectClasses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneSelectClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSelectClasses1)
                    .addComponent(buttonSelectAll)
                    .addComponent(buttonSelectNone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(27, 27, 27))
        );

        buttonSelectNone.getAccessibleContext().setAccessibleName("None");

        buttonTab1Next.setBackground(new java.awt.Color(102, 102, 255));
        buttonTab1Next.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        buttonTab1Next.setForeground(new java.awt.Color(255, 255, 255));
        buttonTab1Next.setText("Next");
        buttonTab1Next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTab1NextActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel1.setBackground(new java.awt.Color(204, 204, 255));
        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Xpert-framework Maker");
        jLabel1.setOpaque(true);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(204, 204, 204));
        jLabel2.setText("Xpert Sistemas");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel3.setText("Xpert-framework documentation:");

        labelLinkDocs.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        labelLinkDocs.setForeground(new java.awt.Color(51, 51, 255));
        labelLinkDocs.setText("https://code.google.com/p/xpert-framework/wiki/Download?tm=2");

        labelLinkShowcase.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        labelLinkShowcase.setForeground(new java.awt.Color(51, 51, 255));
        labelLinkShowcase.setText("http://showcase.xpertsistemas.com.br/");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel4.setText("Xpert-framework showcase:");

        jLabel5.setBackground(new java.awt.Color(241, 241, 241));
        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel5.setText("<html> O xpert-maker é um facilitador na geração<br/>  de CRUDs.<br/> A geração de código é baseada nas  <br/> classes modelos,  construídas a partir das <br/> especificações JPA e Bean Validation. </html>");
        jLabel5.setOpaque(true);

        jLabel6.setBackground(new java.awt.Color(241, 241, 241));
        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel6.setText("<html>\nThe xpert-maker is a facilitator in generating <br/> CRUD.<br/>\nCode generation is based on class models,<br/> built from the JPA\nand  <br/>Bean Validation specification.\n</html>");
        jLabel6.setOpaque(true);

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel7.setText("Xpert Sistemas:");

        labelLinkXpertSistemas.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        labelLinkXpertSistemas.setForeground(new java.awt.Color(51, 51, 255));
        labelLinkXpertSistemas.setText("http://www.xpertsistemas.com.br/");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelLinkDocs, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel2))
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(labelLinkXpertSistemas, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(labelLinkShowcase, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addGap(0, 10, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelLinkShowcase)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelLinkDocs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelLinkXpertSistemas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(153, 204, 255));
        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 153, 255), 1, true));

        jLabel11.setBackground(new java.awt.Color(51, 0, 204));
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("1");
        jLabel11.setOpaque(true);

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel12.setText("Choose your java model classes, these classes must be JPA entities");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel12))
        );

        javax.swing.GroupLayout panelSelectClassesLayout = new javax.swing.GroupLayout(panelSelectClasses);
        panelSelectClasses.setLayout(panelSelectClassesLayout);
        panelSelectClassesLayout.setHorizontalGroup(
            panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSelectClassesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSelectClassesLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(buttonTab1Next, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelSelectClassesLayout.setVerticalGroup(
            panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSelectClassesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSelectClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelSelectClassesLayout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonTab1Next)
                .addContainerGap(96, Short.MAX_VALUE))
        );

        tabbedPanelMain.addTab("Select Classes", panelSelectClasses);

        panelConfiguration.setBackground(new java.awt.Color(237, 242, 253));

        panelOthers.setBackground(new java.awt.Color(255, 255, 255));
        panelOthers.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Others", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(0, 51, 102))); // NOI18N
        panelOthers.setPreferredSize(new java.awt.Dimension(723, 158));

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
        labelMBSuffix.setLabelFor(textManagedBeanSuffix);
        labelMBSuffix.setText("Managed Bean Suffix:");

        textManagedBeanSuffix.setToolTipText("Managed Bean suffix. Example suffix \"MB\" for class Person generates \"PersonMB\"");

        textBusinessObjectSuffix.setToolTipText("Business Object suffix. Example suffix \"BO\" for class Person generates \"PersonBO\"");

        labelBOSuffix.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelBOSuffix.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelBOSuffix.setLabelFor(textBusinessObjectSuffix);
        labelBOSuffix.setText("Business Object Suffix:");

        checkGeneratesSecurityArea.setBackground(new java.awt.Color(255, 255, 255));
        checkGeneratesSecurityArea.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        checkGeneratesSecurityArea.setText("Generate securityArea (use acess control in forms)");

        checkUseCDIBeans.setBackground(new java.awt.Color(255, 255, 255));
        checkUseCDIBeans.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        checkUseCDIBeans.setText("Use CDI Beans (@Named instead of @ManagedBean)");
        checkUseCDIBeans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkUseCDIBeansActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelOthersLayout = new javax.swing.GroupLayout(panelOthers);
        panelOthers.setLayout(panelOthersLayout);
        panelOthersLayout.setHorizontalGroup(
            panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOthersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkUseCDIBeans, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelOthersLayout.createSequentialGroup()
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelBaseDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textBaseDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(labelResourceBundle, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .addComponent(textResourceBundle))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelOthersLayout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addComponent(labelMBSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelOthersLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textManagedBeanSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textBusinessObjectSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelBOSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(checkGeneratesSecurityArea, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelOthersLayout.setVerticalGroup(
            panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOthersLayout.createSequentialGroup()
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelBaseDAOImpl)
                    .addComponent(labelResourceBundle)
                    .addComponent(labelAuthor)
                    .addComponent(labelMBSuffix)
                    .addComponent(labelBOSuffix))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOthersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textBaseDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textResourceBundle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textManagedBeanSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textBusinessObjectSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkGeneratesSecurityArea)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkUseCDIBeans))
        );

        panelBO.setBackground(new java.awt.Color(255, 255, 255));
        panelBO.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Java Classes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(0, 51, 102))); // NOI18N
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

        textBusinessObject.setToolTipText("Your directory of Business Object");

        buttonSelectBO.setBackground(new java.awt.Color(102, 102, 255));
        buttonSelectBO.setForeground(new java.awt.Color(255, 255, 255));
        buttonSelectBO.setText("...");
        buttonSelectBO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectBOActionPerformed(evt);
            }
        });

        labelBOLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelBOLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelBOLocation.setLabelFor(textBusinessObject);
        labelBOLocation.setText("Business Object Location:");

        labelDAOLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelDAOLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDAOLocation.setLabelFor(textDAO);
        labelDAOLocation.setText("DAO (Interface) Location:");

        buttonSelectDAO.setBackground(new java.awt.Color(102, 102, 255));
        buttonSelectDAO.setForeground(new java.awt.Color(255, 255, 255));
        buttonSelectDAO.setText("...");
        buttonSelectDAO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectDAOActionPerformed(evt);
            }
        });

        labelDAOPackage.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelDAOPackage.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDAOPackage.setLabelFor(textPackageDAO);
        labelDAOPackage.setText("Package:");

        textPackageDAO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textPackageDAOActionPerformed(evt);
            }
        });

        labelDAOImplLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelDAOImplLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDAOImplLocation.setLabelFor(textDAOImpl);
        labelDAOImplLocation.setText("DAO (Implementation) Location:");

        labelDAOImplPackage.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelDAOImplPackage.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelDAOImplPackage.setLabelFor(textPackageDAOImpl);
        labelDAOImplPackage.setText("Package:");

        buttonSelectDAOImpl.setBackground(new java.awt.Color(102, 102, 255));
        buttonSelectDAOImpl.setForeground(new java.awt.Color(255, 255, 255));
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

        labelMBLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelMBLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelMBLocation.setLabelFor(textManagedBean);
        labelMBLocation.setText("Managed Bean Location:");

        buttonSelectMB.setBackground(new java.awt.Color(102, 102, 255));
        buttonSelectMB.setForeground(new java.awt.Color(255, 255, 255));
        buttonSelectMB.setText("...");
        buttonSelectMB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectMBActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout panelBOLayout = new javax.swing.GroupLayout(panelBO);
        panelBO.setLayout(panelBOLayout);
        panelBOLayout.setHorizontalGroup(
            panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBOLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBOLayout.createSequentialGroup()
                        .addComponent(textBusinessObject, javax.swing.GroupLayout.PREFERRED_SIZE, 523, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectBO, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelDAOPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelBOPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textPackageBO, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(22, Short.MAX_VALUE))
                    .addGroup(panelBOLayout.createSequentialGroup()
                        .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelBOLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelDAOLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelBOLayout.createSequentialGroup()
                                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelBOLayout.createSequentialGroup()
                                        .addComponent(textDAO, javax.swing.GroupLayout.PREFERRED_SIZE, 523, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buttonSelectDAO, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(labelDAOImplLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelDAOImplPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textPackageDAO, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(panelBOLayout.createSequentialGroup()
                                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(panelBOLayout.createSequentialGroup()
                                            .addComponent(textManagedBean, javax.swing.GroupLayout.PREFERRED_SIZE, 523, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(buttonSelectMB, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelBOLayout.createSequentialGroup()
                                            .addComponent(textDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 523, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(buttonSelectDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(labelMBLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelMBPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textPackageDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(textPackageMB, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        panelBOLayout.setVerticalGroup(
            panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBOLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelBOLayout.createSequentialGroup()
                        .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelBOLocation)
                            .addComponent(labelBOPackage))
                        .addGap(29, 29, 29))
                    .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(textBusinessObject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonSelectBO, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(textPackageBO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDAOLocation)
                    .addComponent(labelDAOPackage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textDAO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectDAO, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textPackageDAO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDAOImplLocation)
                    .addComponent(labelDAOImplPackage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textPackageDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelMBLocation)
                    .addComponent(labelMBPackage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBOLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textManagedBean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectMB, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textPackageMB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        labelMBLocation.getAccessibleContext().setAccessibleParent(panelConfiguration);
        textManagedBean.getAccessibleContext().setAccessibleParent(panelConfiguration);
        buttonSelectMB.getAccessibleContext().setAccessibleParent(panelConfiguration);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "View Configuration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12), new java.awt.Color(0, 51, 102))); // NOI18N

        labelXHTMLLocation.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelXHTMLLocation.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelXHTMLLocation.setLabelFor(textView);
        labelXHTMLLocation.setText("XHTML Location:");

        buttonSelectView.setBackground(new java.awt.Color(102, 102, 255));
        buttonSelectView.setForeground(new java.awt.Color(255, 255, 255));
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

        labelPrimeFacesVersion.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelPrimeFacesVersion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelPrimeFacesVersion.setLabelFor(comboPrimeFacesVersion);
        labelPrimeFacesVersion.setText("PrimeFaces:");

        comboPrimeFacesVersion.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        comboPrimeFacesVersion.setToolTipText("Version of primefaces");

        labelBootstrapVersion.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelBootstrapVersion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelBootstrapVersion.setLabelFor(comboBootstrapVersion);
        labelBootstrapVersion.setText("Bootstrap:");
        labelBootstrapVersion.setToolTipText("");

        comboBootstrapVersion.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        comboBootstrapVersion.setToolTipText("Version of bootstrap");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(textView)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectView, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labelXHTMLLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labelFaceletsTemplate, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(124, 124, 124)
                        .addComponent(labelPrimeFacesVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(textTemplatePath, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboPrimeFacesVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(comboBootstrapVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelBootstrapVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelXHTMLLocation)
                    .addComponent(labelFaceletsTemplate)
                    .addComponent(labelPrimeFacesVersion)
                    .addComponent(labelBootstrapVersion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectView, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textTemplatePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboPrimeFacesVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBootstrapVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        buttonTab2Back.setBackground(new java.awt.Color(102, 102, 255));
        buttonTab2Back.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        buttonTab2Back.setForeground(new java.awt.Color(255, 255, 255));
        buttonTab2Back.setText("Back");
        buttonTab2Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTab2BackActionPerformed(evt);
            }
        });

        buttonTab2Next.setBackground(new java.awt.Color(102, 102, 255));
        buttonTab2Next.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        buttonTab2Next.setForeground(new java.awt.Color(255, 255, 255));
        buttonTab2Next.setText("Next");
        buttonTab2Next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTab2NextActionPerformed(evt);
            }
        });

        jPanel5.setBackground(new java.awt.Color(153, 204, 255));
        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 153, 255), 1, true));

        jLabel13.setBackground(new java.awt.Color(51, 0, 204));
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("4");
        jLabel13.setOpaque(true);

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel14.setText("Other configurations. Do you use CDI? Need acess control?");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel14)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel14))
        );

        jPanel6.setBackground(new java.awt.Color(153, 204, 255));
        jPanel6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 153, 255), 1, true));

        jLabel15.setBackground(new java.awt.Color(51, 0, 204));
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("2");
        jLabel15.setOpaque(true);

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel16.setText("Configure the path where you to generate the CRUD java classes");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel16)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel16))
        );

        jPanel7.setBackground(new java.awt.Color(153, 204, 255));
        jPanel7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 153, 255), 1, true));

        jLabel17.setBackground(new java.awt.Color(51, 0, 204));
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("3");
        jLabel17.setOpaque(true);

        jLabel18.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel18.setText("Configure the path to generate the JSF pages (XHTML).  Choose the primefaces version and boostrap (if you want a responsive layout)");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel18)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel18))
        );

        javax.swing.GroupLayout panelConfigurationLayout = new javax.swing.GroupLayout(panelConfiguration);
        panelConfiguration.setLayout(panelConfigurationLayout);
        panelConfigurationLayout.setHorizontalGroup(
            panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConfigurationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelBO, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(panelOthers, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelConfigurationLayout.createSequentialGroup()
                        .addComponent(buttonTab2Back, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonTab2Next, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelConfigurationLayout.setVerticalGroup(
            panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConfigurationLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(panelBO, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelOthers, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonTab2Back)
                    .addComponent(buttonTab2Next))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPanelMain.addTab("Project Configuration", panelConfiguration);

        panelCreateClasses.setBackground(new java.awt.Color(237, 242, 253));

        textAreaLog.setColumns(20);
        textAreaLog.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textAreaLog.setRows(5);
        scrollPaneLog.setViewportView(textAreaLog);

        textAreaClassBean.setColumns(20);
        textAreaClassBean.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textAreaClassBean.setRows(5);
        scrollPaneClassBean.setViewportView(textAreaClassBean);

        labelI18N.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelI18N.setText("I18N Resource Bundle:");

        labelClassBean.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        labelClassBean.setText("Class Bean:");

        textAreaI18n.setColumns(20);
        textAreaI18n.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textAreaI18n.setRows(5);
        scrollPaneI18N.setViewportView(textAreaI18n);

        jPanel9.setBackground(new java.awt.Color(153, 204, 255));
        jPanel9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 153, 255), 1, true));

        jLabel19.setBackground(new java.awt.Color(51, 0, 204));
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("5");
        jLabel19.setOpaque(true);

        jLabel20.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel20.setText("Is everything ok? Just click above and see the log");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel20)
                .addGap(0, 519, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel20))
        );

        jPanel10.setBackground(new java.awt.Color(153, 204, 255));
        jPanel10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 153, 255), 1, true));

        jLabel21.setBackground(new java.awt.Color(51, 0, 204));
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("6");
        jLabel21.setOpaque(true);

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel22.setText("Copy the generated resource bundle tou your i18n file");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel22)
                .addGap(0, 499, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel22))
        );

        jPanel11.setBackground(new java.awt.Color(153, 204, 255));
        jPanel11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 153, 255), 1, true));

        jLabel23.setBackground(new java.awt.Color(51, 0, 204));
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("7");
        jLabel23.setOpaque(true);

        jLabel24.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel24.setText("Copy the methods to your Class Bean (Managed Bean with classes of the project)");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel24)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel24))
        );

        buttonTab3Back.setBackground(new java.awt.Color(102, 102, 255));
        buttonTab3Back.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        buttonTab3Back.setForeground(new java.awt.Color(255, 255, 255));
        buttonTab3Back.setText("Back");
        buttonTab3Back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTab3BackActionPerformed(evt);
            }
        });

        buttonCreateClasses.setBackground(new java.awt.Color(102, 102, 255));
        buttonCreateClasses.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        buttonCreateClasses.setForeground(new java.awt.Color(255, 255, 255));
        buttonCreateClasses.setText("Create Classes");
        buttonCreateClasses.setFocusPainted(false);
        buttonCreateClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCreateClassesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCreateClassesLayout = new javax.swing.GroupLayout(panelCreateClasses);
        panelCreateClasses.setLayout(panelCreateClassesLayout);
        panelCreateClassesLayout.setHorizontalGroup(
            panelCreateClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCreateClassesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCreateClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneLog)
                    .addComponent(scrollPaneClassBean, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(scrollPaneI18N, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelCreateClassesLayout.createSequentialGroup()
                        .addGroup(panelCreateClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelI18N)
                            .addComponent(labelClassBean)
                            .addComponent(buttonTab3Back, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonCreateClasses))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelCreateClassesLayout.setVerticalGroup(
            panelCreateClassesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCreateClassesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonCreateClasses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelI18N)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneI18N, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelClassBean)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneClassBean, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonTab3Back)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        tabbedPanelMain.addTab("Create Classes", panelCreateClasses);

        javax.swing.GroupLayout jPanelMainLayout = new javax.swing.GroupLayout(jPanelMain);
        jPanelMain.setLayout(jPanelMainLayout);
        jPanelMainLayout.setHorizontalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPanelMain)
        );
        jPanelMainLayout.setVerticalGroup(
            jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPanelMain)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void buttonTab1NextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTab1NextActionPerformed
        tabbedPanelMain.setSelectedIndex(1);
    }//GEN-LAST:event_buttonTab1NextActionPerformed

    private void buttonTab2BackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTab2BackActionPerformed
        tabbedPanelMain.setSelectedIndex(0);
    }//GEN-LAST:event_buttonTab2BackActionPerformed

    private void buttonTab2NextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTab2NextActionPerformed
        tabbedPanelMain.setSelectedIndex(2);
    }//GEN-LAST:event_buttonTab2NextActionPerformed

    private void buttonTab3BackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTab3BackActionPerformed
        tabbedPanelMain.setSelectedIndex(1);
    }//GEN-LAST:event_buttonTab3BackActionPerformed

    private void checkUseCDIBeansActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkUseCDIBeansActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_checkUseCDIBeansActionPerformed

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
    private javax.swing.JButton buttonTab1Next;
    private javax.swing.JButton buttonTab2Back;
    private javax.swing.JButton buttonTab2Next;
    private javax.swing.JButton buttonTab3Back;
    private javax.swing.JCheckBox checkGeneratesSecurityArea;
    private javax.swing.JCheckBox checkUseCDIBeans;
    private javax.swing.JComboBox comboBootstrapVersion;
    private javax.swing.JComboBox comboPrimeFacesVersion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JLabel labelAuthor;
    private javax.swing.JLabel labelBOLocation;
    private javax.swing.JLabel labelBOPackage;
    private javax.swing.JLabel labelBOSuffix;
    private javax.swing.JLabel labelBaseDAOImpl;
    private javax.swing.JLabel labelBootstrapVersion;
    private javax.swing.JLabel labelClassBean;
    private javax.swing.JLabel labelDAOImplLocation;
    private javax.swing.JLabel labelDAOImplPackage;
    private javax.swing.JLabel labelDAOLocation;
    private javax.swing.JLabel labelDAOPackage;
    private javax.swing.JLabel labelFaceletsTemplate;
    private javax.swing.JLabel labelI18N;
    private javax.swing.JLabel labelLinkDocs;
    private javax.swing.JLabel labelLinkShowcase;
    private javax.swing.JLabel labelLinkXpertSistemas;
    private javax.swing.JLabel labelMBLocation;
    private javax.swing.JLabel labelMBPackage;
    private javax.swing.JLabel labelMBSuffix;
    private javax.swing.JLabel labelPackageName;
    private javax.swing.JLabel labelPrimeFacesVersion;
    private javax.swing.JLabel labelResourceBundle;
    private javax.swing.JLabel labelSelectClasses;
    private javax.swing.JLabel labelSelectClasses1;
    private javax.swing.JLabel labelXHTMLLocation;
    private javax.swing.JList listClasses;
    private javax.swing.JPanel panelBO;
    private javax.swing.JPanel panelConfiguration;
    private javax.swing.JPanel panelCreateClasses;
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

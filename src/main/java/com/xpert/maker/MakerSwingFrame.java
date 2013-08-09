package com.xpert.maker;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author ayslan
 */
public abstract class MakerSwingFrame extends javax.swing.JFrame {

    private static final String JAVA_PROJECT_PREFFIX = File.separator + "java";
    private static final Logger logger = Logger.getLogger(MakerSwingFrame.class.getName());
    private String i18n;
    private BeanConfiguration beanConfiguration;
    private ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
    private File lastFile;

    public abstract String getDefaultPackage();

    public abstract String getDefaultTemplatePath();

    public abstract String getDefaultResourceBundle();

    public abstract String getDefaultBaseDAOImpl();

    public abstract String getManagedBeanSuffix();

    public abstract String getBusinessObjectSuffix();

    public static void run(final MakerSwingFrame maker) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                maker.center();
                maker.setVisible(true);
            }
        });
    }

    public void searchClasses() {
        try {
            ArrayList<Class<?>> allClasses = ClassEnumerator.getClassesForPackage(textPackageName.getText());
            classes = new ArrayList<Class<?>>();
            for (Class entity : allClasses) {
                if (!entity.isEnum()) {
                    classes.add(entity);
                }
            }
            listClasses.setListData(classes.toArray(new Class[classes.size()]));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage() + ". See java log for details", "Error", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "No Classes Select", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Class> classesList = new ArrayList<Class>();
        for (Object object : selectedClasses) {
            classesList.add((Class) object);
        }

        beanConfiguration = new BeanConfiguration();
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

        PersistenceMappedBean persistenceMappedBean = new PersistenceMappedBean(null);
        List<MappedBean> mappedBeans = persistenceMappedBean.getMappedBeans(classesList, beanConfiguration);
        i18n = BeanCreator.getI18N(mappedBeans);
        textAreaI18n.setText(i18n);
        StringBuilder logBuilder = new StringBuilder();
        BeanCreator.writeBean(mappedBeans, beanConfiguration, logBuilder);
        textAreaLog.setText(logBuilder.toString());

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

    /**
     * Creates new form MakerMainFrame
     */
    public MakerSwingFrame() {
        initComponents();
        initFromConfiguration();

    }

    public void initFromConfiguration() {
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

        tabbedPanelMain = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listClasses = new javax.swing.JList();
        labelSelectClasses = new javax.swing.JLabel();
        labelPackageName = new javax.swing.JLabel();
        textPackageName = new javax.swing.JTextField();
        buttonSearchClasses = new javax.swing.JButton();
        buttonSelectAll = new javax.swing.JButton();
        buttonSelectNone = new javax.swing.JButton();
        panelConfiguration = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textPackageMB = new javax.swing.JTextField();
        labelManagedBean = new javax.swing.JLabel();
        textManagedBean = new javax.swing.JTextField();
        buttonSelectMB = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        labelView = new javax.swing.JLabel();
        textView = new javax.swing.JTextField();
        buttonSelectView = new javax.swing.JButton();
        labelView1 = new javax.swing.JLabel();
        textTemplatePath = new javax.swing.JTextField();
        labelView2 = new javax.swing.JLabel();
        textResourceBundle = new javax.swing.JTextField();
        labelView3 = new javax.swing.JLabel();
        textAuthor = new javax.swing.JTextField();
        labelView4 = new javax.swing.JLabel();
        textBaseDAOImpl = new javax.swing.JTextField();
        labelView5 = new javax.swing.JLabel();
        textManagedBeanSuffix = new javax.swing.JTextField();
        textBusinessObjectSuffix = new javax.swing.JTextField();
        labelView6 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        textPackageBO = new javax.swing.JTextField();
        textBusinessObject = new javax.swing.JTextField();
        buttonSelectBO = new javax.swing.JButton();
        labelManagedBean2 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        textPackageDAO = new javax.swing.JTextField();
        labelManagedBean3 = new javax.swing.JLabel();
        textDAO = new javax.swing.JTextField();
        buttonSelectDAO = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        labelManagedBean5 = new javax.swing.JLabel();
        textDAOImpl = new javax.swing.JTextField();
        buttonSelectDAOImpl = new javax.swing.JButton();
        textPackageDAOImpl = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        textAreaLog = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        textAreaI18n = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Xpert-Maker");

        jScrollPane1.setViewportView(listClasses);

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labelSelectClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 457, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(buttonSelectAll)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSelectNone)))
                        .addContainerGap(147, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labelPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSearchClasses)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelPackageName)
                    .addComponent(textPackageName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSearchClasses))
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labelSelectClasses)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSelectAll)
                    .addComponent(buttonSelectNone))
                .addGap(21, 21, 21))
        );

        tabbedPanelMain.addTab("Select Classes", jPanel2);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Managed Bean", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12))); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(723, 85));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setLabelFor(textPackageMB);
        jLabel1.setText("Package:");

        textPackageMB.setToolTipText("Package for your Managed Bean. Example: com.yourproject.mb");
        textPackageMB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textPackageMBActionPerformed(evt);
            }
        });

        labelManagedBean.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelManagedBean.setLabelFor(textManagedBean);
        labelManagedBean.setText("Location:");

        buttonSelectMB.setText("...");
        buttonSelectMB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectMBActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelManagedBean, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(textManagedBean, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectMB))
                    .addComponent(textPackageMB, javax.swing.GroupLayout.PREFERRED_SIZE, 482, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelManagedBean)
                    .addComponent(textManagedBean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectMB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textPackageMB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        labelManagedBean.getAccessibleContext().setAccessibleParent(panelConfiguration);
        textManagedBean.getAccessibleContext().setAccessibleParent(panelConfiguration);
        buttonSelectMB.getAccessibleContext().setAccessibleParent(panelConfiguration);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Others", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12))); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(723, 158));

        labelView.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelView.setLabelFor(textView);
        labelView.setText("View Location:");

        buttonSelectView.setText("...");
        buttonSelectView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectViewActionPerformed(evt);
            }
        });

        labelView1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelView1.setLabelFor(textTemplatePath);
        labelView1.setText("Facelets Template:");

        labelView2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelView2.setLabelFor(textResourceBundle);
        labelView2.setText("Resource Bundle:");

        labelView3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelView3.setLabelFor(textAuthor);
        labelView3.setText("Author:");

        labelView4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelView4.setLabelFor(textAuthor);
        labelView4.setText("BaseDAOImpl:");

        labelView5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelView5.setLabelFor(textAuthor);
        labelView5.setText("Managed Bean Suffix:");

        labelView6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelView6.setLabelFor(textAuthor);
        labelView6.setText("Business Object Suffix:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(labelView1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelView2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelView5, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                    .addComponent(labelView4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(textResourceBundle, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
                            .addComponent(textManagedBeanSuffix))
                        .addGap(4, 4, 4)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(labelView6, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(textBusinessObjectSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(labelView3, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(textAuthor))))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(textView, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectView))
                    .addComponent(textBaseDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textTemplatePath, javax.swing.GroupLayout.PREFERRED_SIZE, 480, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelView)
                    .addComponent(textView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectView))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelView4)
                    .addComponent(textBaseDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelView1)
                    .addComponent(textTemplatePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelView2)
                    .addComponent(textResourceBundle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelView3)
                    .addComponent(textAuthor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textManagedBeanSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelView5)
                    .addComponent(labelView6)
                    .addComponent(textBusinessObjectSuffix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Business Object (BO)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12))); // NOI18N
        jPanel6.setPreferredSize(new java.awt.Dimension(723, 85));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setLabelFor(textPackageBO);
        jLabel6.setText("Package:");

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

        labelManagedBean2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelManagedBean2.setLabelFor(textBusinessObject);
        labelManagedBean2.setText("Location:");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelManagedBean2, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(textBusinessObject, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectBO))
                    .addComponent(textPackageBO, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelManagedBean2)
                    .addComponent(textBusinessObject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectBO))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textPackageBO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DAO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12))); // NOI18N
        jPanel7.setPreferredSize(new java.awt.Dimension(723, 85));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setLabelFor(textPackageDAO);
        jLabel7.setText("Package:");

        textPackageDAO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textPackageDAOActionPerformed(evt);
            }
        });

        labelManagedBean3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelManagedBean3.setLabelFor(textDAO);
        labelManagedBean3.setText("Location:");

        buttonSelectDAO.setText("...");
        buttonSelectDAO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectDAOActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelManagedBean3, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(textDAO, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectDAO))
                    .addComponent(textPackageDAO, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelManagedBean3)
                    .addComponent(textDAO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectDAO))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(textPackageDAO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DAO Implementation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.BELOW_TOP, new java.awt.Font("Tahoma", 0, 12))); // NOI18N
        jPanel9.setPreferredSize(new java.awt.Dimension(723, 85));

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setLabelFor(textPackageDAOImpl);
        jLabel9.setText("Package:");

        labelManagedBean5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelManagedBean5.setLabelFor(textDAOImpl);
        labelManagedBean5.setText("Location:");

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

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelManagedBean5, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(textDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectDAOImpl))
                    .addComponent(textPackageDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(69, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelManagedBean5)
                    .addComponent(buttonSelectDAOImpl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(textPackageDAOImpl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelConfigurationLayout = new javax.swing.GroupLayout(panelConfiguration);
        panelConfiguration.setLayout(panelConfigurationLayout);
        panelConfigurationLayout.setHorizontalGroup(
            panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConfigurationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelConfigurationLayout.createSequentialGroup()
                        .addGroup(panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelConfigurationLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelConfigurationLayout.setVerticalGroup(
            panelConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConfigurationLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPanelMain.addTab("Project Configuration", panelConfiguration);

        textAreaLog.setColumns(20);
        textAreaLog.setRows(5);
        jScrollPane2.setViewportView(textAreaLog);

        jLabel2.setText("Log:");

        jButton1.setText("Create Classes");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        textAreaI18n.setColumns(20);
        textAreaI18n.setRows(5);
        jScrollPane3.setViewportView(textAreaI18n);

        jLabel3.setText("I18N Resource Bundle:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 721, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
        );

        tabbedPanelMain.addTab("Create Classes", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPanelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 585, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        generate();
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonSearchClasses;
    private javax.swing.JButton buttonSelectAll;
    private javax.swing.JButton buttonSelectBO;
    private javax.swing.JButton buttonSelectDAO;
    private javax.swing.JButton buttonSelectDAOImpl;
    private javax.swing.JButton buttonSelectMB;
    private javax.swing.JButton buttonSelectNone;
    private javax.swing.JButton buttonSelectView;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel labelManagedBean;
    private javax.swing.JLabel labelManagedBean2;
    private javax.swing.JLabel labelManagedBean3;
    private javax.swing.JLabel labelManagedBean5;
    private javax.swing.JLabel labelPackageName;
    private javax.swing.JLabel labelSelectClasses;
    private javax.swing.JLabel labelView;
    private javax.swing.JLabel labelView1;
    private javax.swing.JLabel labelView2;
    private javax.swing.JLabel labelView3;
    private javax.swing.JLabel labelView4;
    private javax.swing.JLabel labelView5;
    private javax.swing.JLabel labelView6;
    private javax.swing.JList listClasses;
    private javax.swing.JPanel panelConfiguration;
    private javax.swing.JTabbedPane tabbedPanelMain;
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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

class Club {
    int id;
    String name;
    String desc;
    ArrayList<Integer> members = new ArrayList<>();
    String folderPath = "";

    Club(int id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }
}

class Student {
    int id;
    String name;
    Student(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

public class ClubManagementGUI extends JFrame {
    static ArrayList<Club> clubs = new ArrayList<>();
    static ArrayList<Student> students = new ArrayList<>();
    static int LIMIT = 30;
    static String DATA_FILE = "club_data.txt";

    JTextField clubIdField, clubNameField, clubDescField;
    JTextField studentIdField, studentNameField;
    JTextField searchField;
    JTextArea outputArea;
    JTable clubTable, searchTable;
    DefaultTableModel clubModel, searchModel;
    JComboBox<String> studentCombo, clubCombo, memberClubCombo, folderClubCombo;
    JTextArea folderArea;
    JLabel folderLabel;
    String selectedFolderPath = "";

    Club getClub(int id) {
        for (Club c : clubs) if (c.id == id) return c;
        return null;
    }
    Student getStudent(int id) {
        for (Student s : students) if (s.id == id) return s;
        return null;
    }

    Club getClubOfStudent(int sid) {
        for (Club c : clubs) {
            if (c.members.contains(sid)) return c;
        }
        return null;
    }

    void refreshCombos() {
        studentCombo.removeAllItems();
        clubCombo.removeAllItems();
        memberClubCombo.removeAllItems();
        folderClubCombo.removeAllItems();
        for (Student s : students) studentCombo.addItem(s.id + " - " + s.name);
        for (Club c : clubs) {
            clubCombo.addItem(c.id + " - " + c.name);
            memberClubCombo.addItem(c.id + " - " + c.name);
            folderClubCombo.addItem(c.id + " - " + c.name);
        }
    }

    void refreshTable() {
        clubModel.setRowCount(0);
        for (Club c : clubs) {
            String status;
            if (c.members.size() > LIMIT) status = "EXCESS MEMBERS!";
            else if (c.members.size() == LIMIT) status = "FULL";
            else status = "AVAILABLE";
            String folder = c.folderPath.isEmpty() ? "No Folder" : "Folder Attached";
            clubModel.addRow(new Object[]{c.id, c.name, c.desc, c.members.size() + "/30", status, folder});
        }
    }

    void refreshStudentsArea() {
        outputArea.setText("Students:\n");
        for (Student s : students) {
            Club cc = getClubOfStudent(s.id);
            String clubInfo = (cc == null) ? "No Club" : "Joined in: " + cc.name;
            outputArea.append(s.id + " - " + s.name + " | " + clubInfo + "\n");
        }
    }

    void saveData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Student s : students) {
                bw.write("STUDENT|" + s.id + "|" + s.name);
                bw.newLine();
            }
            for (Club c : clubs) {
                String membersStr = "";
                for (int m : c.members) membersStr += m + ",";
                String safeFolder = c.folderPath.replace("|", "");
                bw.write("CLUB|" + c.id + "|" + c.name + "|" + c.desc + "|" + membersStr + "|" + safeFolder);
                bw.newLine();
            }
        } catch (Exception e) { System.out.println("Save error: " + e); }
    }

    void loadData() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p[0].equals("STUDENT") && p.length >= 3) {
                    students.add(new Student(Integer.parseInt(p[1]), p[2]));
                } else if (p[0].equals("CLUB") && p.length >= 6) {
                    Club c = new Club(Integer.parseInt(p[1]), p[2], p[3]);
                    if (!p[4].isEmpty()) {
                        for (String ms : p[4].split(",")) {
                            if (!ms.isEmpty()) c.members.add(Integer.parseInt(ms));
                        }
                    }
                    c.folderPath = p[5];
                    clubs.add(c);
                }
            }
        } catch (Exception e) { System.out.println("Load error: " + e); }
    }

    void doSearch(String q) {
        searchModel.setRowCount(0);
        q = q.trim().toLowerCase();
        if (q.isEmpty()) {
            for (Club c : clubs) searchModel.addRow(new Object[]{c.id, c.name, c.desc, c.members.size(), c.folderPath});
            return;
        }
        boolean found = false;
        for (Club c : clubs) {
            if (String.valueOf(c.id).contains(q) || c.name.toLowerCase().contains(q) || c.desc.toLowerCase().contains(q)) {
                found = true;
                searchModel.addRow(new Object[]{c.id, c.name, c.desc, c.members.size(), c.folderPath.isEmpty() ? "No Folder" : c.folderPath});
            }
        }
        if (!found) JOptionPane.showMessageDialog(this, "Club Not Found!");
    }

    // NEW: Folder lo unna file ni chadivi DIRECT ga Data Store lo store cheyadam
    // Format: 101 - Aarav  OR  101, Aarav  OR  101 | Aarav  OR  101 Aarav
    int importFolderToDataStore(String folderPath, Club targetClub) {
        int added = 0;
        int skipped = 0;
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files == null) return 0;
        for (File f : files) {
            if (f.isDirectory()) continue;
            String fname = f.getName().toLowerCase();
            if (!(fname.endsWith(".txt") || fname.endsWith(".csv"))) continue;
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    if (line.toLowerCase().startsWith("club:") || line.toLowerCase().startsWith("total") || line.toLowerCase().startsWith("id")) continue;
                    // ID and Name separate cheyadam
                    String[] parts = line.split("[-,|\\s]+", 2);
                    // Example: "101 - Aarav Sharma" -> parts[0]=101, parts[1]=Aarav Sharma
                    // "101 Aarav" kuda same
                    if (parts.length < 2) continue;
                    try {
                        int sid = Integer.parseInt(parts[0].trim());
                        String sname = parts[1].trim();
                        // Remove extra "-" at start if any
                        if (sname.startsWith("-")) sname = sname.substring(1).trim();
                        if (sname.isEmpty()) continue;

                        // Student lekapothe create chey
                        if (getStudent(sid) == null) {
                            students.add(new Student(sid, sname));
                        }
                        // Rule 1: Oka student okate club lo undali
                        Club already = getClubOfStudent(sid);
                        if (already != null && already.id != targetClub.id) {
                            skipped++;
                            continue;
                        }
                        // Rule: 30 LIMIT
                        if (targetClub.members.size() >= LIMIT) {
                            skipped++;
                            continue;
                        }
                        if (!targetClub.members.contains(sid)) {
                            targetClub.members.add(sid);
                            added++;
                        }
                    } catch (NumberFormatException ex) {
                        // First word number kadu ante skip
                        continue;
                    }
                }
            } catch (Exception e) { System.out.println("Import error: " + e); }
        }
        return added;
    }

    ClubManagementGUI() {
        loadData();
        setTitle("Club Membership Management System");
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) { saveData(); }
        });

        JTabbedPane tabs = new JTabbedPane();

        JPanel clubPanel = new JPanel(new BorderLayout(5,5));
        JPanel top = new JPanel(new GridLayout(3, 2, 5, 5));
        clubIdField = new JTextField();
        clubNameField = new JTextField();
        clubDescField = new JTextField();
        top.add(new JLabel("Club ID:")); top.add(clubIdField);
        top.add(new JLabel("Club Name:")); top.add(clubNameField);
        top.add(new JLabel("Description:")); top.add(clubDescField);

        JPanel btnPanel = new JPanel();
        JButton createBtn = new JButton("Create Club");
        JButton editBtn = new JButton("Edit Club");
        JButton deleteBtn = new JButton("Delete Club");
        JButton viewBtn = new JButton("View / Refresh Clubs");
        btnPanel.add(createBtn); btnPanel.add(editBtn); btnPanel.add(deleteBtn); btnPanel.add(viewBtn);

        clubModel = new DefaultTableModel(new String[]{"ID", "Name", "Desc", "Members", "Status", "Folder"}, 0);
        clubTable = new JTable(clubModel);
        clubTable.getSelectionModel().addListSelectionListener(e -> {
            int r = clubTable.getSelectedRow();
            if (r >= 0) {
                clubIdField.setText(String.valueOf(clubModel.getValueAt(r, 0)));
                clubNameField.setText(String.valueOf(clubModel.getValueAt(r, 1)));
                clubDescField.setText(String.valueOf(clubModel.getValueAt(r, 2)));
            }
        });

        JPanel clubTopWrap = new JPanel(new BorderLayout());
        clubTopWrap.add(top, BorderLayout.NORTH);
        clubTopWrap.add(btnPanel, BorderLayout.SOUTH);
        clubPanel.add(clubTopWrap, BorderLayout.NORTH);
        clubPanel.add(new JScrollPane(clubTable), BorderLayout.CENTER);

        createBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(clubIdField.getText().trim());
                if (getClub(id) != null) { JOptionPane.showMessageDialog(this, "A club with this ID already exists!"); return; }
                if (clubNameField.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Please enter Club Name"); return; }
                clubs.add(new Club(id, clubNameField.getText().trim(), clubDescField.getText().trim()));
                JOptionPane.showMessageDialog(this, "Club created successfully!");
                refreshTable(); refreshCombos(); saveData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Please enter Club ID as a number"); }
        });

        editBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(clubIdField.getText().trim());
                Club c = getClub(id);
                if (c == null) { JOptionPane.showMessageDialog(this, "Club not found!"); return; }
                c.name = clubNameField.getText().trim();
                c.desc = clubDescField.getText().trim();
                JOptionPane.showMessageDialog(this, "Club updated successfully!");
                refreshTable(); refreshCombos(); saveData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Please enter Club ID"); }
        });

        deleteBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(clubIdField.getText().trim());
                Club c = getClub(id);
                if (c == null) { JOptionPane.showMessageDialog(this, "Club not found!"); return; }
                clubs.remove(c);
                JOptionPane.showMessageDialog(this, "Club deleted successfully!");
                refreshTable(); refreshCombos(); saveData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Please enter Club ID"); }
        });

        viewBtn.addActionListener(e -> refreshTable());

        JPanel stuPanel = new JPanel(new BorderLayout());
        JPanel stuTop = new JPanel(new GridLayout(2, 2, 5, 5));
        studentIdField = new JTextField();
        studentNameField = new JTextField();
        stuTop.add(new JLabel("Student ID:")); stuTop.add(studentIdField);
        stuTop.add(new JLabel("Student Name:")); stuTop.add(studentNameField);
        JButton addStuBtn = new JButton("Add Student");
        outputArea = new JTextArea(15, 50);
        outputArea.setEditable(false);
        stuPanel.add(stuTop, BorderLayout.NORTH);
        stuPanel.add(addStuBtn, BorderLayout.CENTER);
        stuPanel.add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        addStuBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(studentIdField.getText().trim());
                if (getStudent(id) != null) { JOptionPane.showMessageDialog(this, "This student already exists!"); return; }
                students.add(new Student(id, studentNameField.getText().trim()));
                JOptionPane.showMessageDialog(this, "Student added successfully!");
                refreshCombos(); refreshStudentsArea(); saveData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Please enter Student ID as a number"); }
        });

        JPanel memPanel = new JPanel(new GridLayout(8, 1, 5, 5));
        studentCombo = new JComboBox<>();
        clubCombo = new JComboBox<>();
        memberClubCombo = new JComboBox<>();
        folderClubCombo = new JComboBox<>();
        JButton joinBtn = new JButton("Join Club");
        JButton showMemBtn = new JButton("Show Members");
        JButton excessBtn = new JButton("Check Excess Members");
        memPanel.add(new JLabel("Student:")); memPanel.add(studentCombo);
        memPanel.add(new JLabel("Club (For Join):")); memPanel.add(clubCombo);
        memPanel.add(joinBtn);
        memPanel.add(new JLabel("Club (To View Members):")); memPanel.add(memberClubCombo);
        memPanel.add(showMemBtn); memPanel.add(excessBtn);

        joinBtn.addActionListener(e -> {
            if (studentCombo.getSelectedItem() == null || clubCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please add student and club first."); return;
            }
            int sid = Integer.parseInt(studentCombo.getSelectedItem().toString().split(" - ")[0]);
            int cid = Integer.parseInt(clubCombo.getSelectedItem().toString().split(" - ")[0]);
            Club c = getClub(cid);
            Club already = getClubOfStudent(sid);
            if (already != null) {
                JOptionPane.showMessageDialog(this, "Already joined in '" + already.name + "' club!\nA student must belong to only one club.\nIf you want to add to another club, first remove from this club.");
                return;
            }
            if (c.members.size() >= LIMIT) { JOptionPane.showMessageDialog(this, "Registration rejected! Club is full (30/30)."); return; }
            c.members.add(sid);
            JOptionPane.showMessageDialog(this, "Joined successfully!");
            refreshTable(); refreshStudentsArea(); saveData();
        });

        showMemBtn.addActionListener(e -> {
            if (memberClubCombo.getSelectedItem() == null) return;
            int cid = Integer.parseInt(memberClubCombo.getSelectedItem().toString().split(" - ")[0]);
            Club c = getClub(cid);
            String msg = "Members of " + c.name + "\n";
            if (c.members.isEmpty()) msg += "No members yet.";
            for (int sid : c.members) {
                Student s = getStudent(sid);
                if (s != null) msg += s.id + " - " + s.name + "\n";
            }
            msg += "Total: " + c.members.size();
            JOptionPane.showMessageDialog(this, msg);
        });

        excessBtn.addActionListener(e -> {
            String msg = "";
            boolean found = false;
            for (Club c : clubs) {
                if (c.members.size() > LIMIT) { found = true; msg += c.name + " - " + c.members.size() + " EXCESS!\n"; }
            }
            if (!found) msg = "No Excess clubs. Maximum 30 members only.";
            else msg += "\nMembers are not deleted, only flagged.";
            JOptionPane.showMessageDialog(this, msg);
        });

        JPanel searchPanel = new JPanel(new BorderLayout(5,5));
        JPanel searchTop = new JPanel(new BorderLayout(5,5));
        searchField = new JTextField();
        JButton searchBtn = new JButton("Search Club");
        JButton showAllBtn = new JButton("Show All");
        searchTop.add(new JLabel("Search by ID / Name / Desc: "), BorderLayout.WEST);
        searchTop.add(searchField, BorderLayout.CENTER);
        JPanel searchBtnWrap = new JPanel(new FlowLayout());
        searchBtnWrap.add(searchBtn); searchBtnWrap.add(showAllBtn);
        searchTop.add(searchBtnWrap, BorderLayout.SOUTH);
        searchModel = new DefaultTableModel(new String[]{"ID", "Name", "Desc", "Members", "Folder"}, 0);
        searchTable = new JTable(searchModel);
        searchPanel.add(searchTop, BorderLayout.NORTH);
        searchPanel.add(new JScrollPane(searchTable), BorderLayout.CENTER);
        searchField.addActionListener(e -> doSearch(searchField.getText()));
        searchBtn.addActionListener(e -> doSearch(searchField.getText()));
        showAllBtn.addActionListener(e -> { searchField.setText(""); doSearch(""); });

        // --- TAB 5 : FOLDER UPLOAD - CHANGED: Direct to Data Store ---
        JPanel uploadPanel = new JPanel(new BorderLayout(5,5));
        JPanel uploadTop = new JPanel(new GridLayout(3, 2, 5, 5));
        folderLabel = new JLabel("No folder selected");
        JButton chooseFolderBtn = new JButton("Choose Folder");
        JButton attachBtn = new JButton("Import Folder to Data Store");
        JButton viewStoredBtn = new JButton("View Stored Members");
        uploadTop.add(new JLabel("Select Club:")); uploadTop.add(folderClubCombo);
        uploadTop.add(new JLabel("Selected Folder:")); uploadTop.add(folderLabel);
        uploadTop.add(chooseFolderBtn); uploadTop.add(attachBtn);
        folderArea = new JTextArea();
        folderArea.setEditable(false);
        uploadPanel.add(uploadTop, BorderLayout.NORTH);
        uploadPanel.add(new JScrollPane(folderArea), BorderLayout.CENTER);
        uploadPanel.add(viewStoredBtn, BorderLayout.SOUTH);

        chooseFolderBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select Manual Folder");
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File folder = chooser.getSelectedFile();
                selectedFolderPath = folder.getAbsolutePath();
                folderLabel.setText(selectedFolderPath);
                folderArea.setText("Folder: " + selectedFolderPath + "\n\nFiles found:\n");
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (!f.isDirectory() && (f.getName().toLowerCase().endsWith(".txt") || f.getName().toLowerCase().endsWith(".csv"))) {
                            folderArea.append("[File] " + f.getName() + " - Will be imported to Data Store\n");
                        } else {
                            folderArea.append((f.isDirectory() ? "[Folder] " : "[File] ") + f.getName() + "\n");
                        }
                    }
                }
                folderArea.append("\nClick 'Import Folder to Data Store' to save directly to club_data.txt");
            }
        });

        attachBtn.addActionListener(e -> {
            if (folderClubCombo.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "Please select a Club"); return; }
            if (selectedFolderPath.isEmpty()) { JOptionPane.showMessageDialog(this, "Please select a folder first using Choose Folder"); return; }
            int cid = Integer.parseInt(folderClubCombo.getSelectedItem().toString().split(" - ")[0]);
            Club c = getClub(cid);
            c.folderPath = selectedFolderPath;
            // MAIN CHANGE: File open cheyakunda, direct ga Data Store lo save
            int added = importFolderToDataStore(selectedFolderPath, c);
            saveData(); // Permanent save to club_data.txt
            refreshTable(); refreshCombos(); refreshStudentsArea(); doSearch(searchField.getText());
            JOptionPane.showMessageDialog(this, "Imported Successfully!\n" + added + " members added to '" + c.name + "' club.\nData saved directly to Data Store (club_data.txt).\nNo need to open file externally.");
            folderArea.append("\n\nImported: " + added + " members stored to Data Store.");
        });

        viewStoredBtn.addActionListener(e -> {
            if (folderClubCombo.getSelectedItem() == null) return;
            int cid = Integer.parseInt(folderClubCombo.getSelectedItem().toString().split(" - ")[0]);
            Club c = getClub(cid);
            String msg = "Stored Members of " + c.name + " (From Data Store)\n\n";
            if (c.members.isEmpty()) msg += "No members yet.";
            for (int sid : c.members) {
                Student s = getStudent(sid);
                if (s != null) msg += s.id + " - " + s.name + "\n";
            }
            msg += "\nTotal: " + c.members.size() + " / 30";
            JOptionPane.showMessageDialog(this, msg);
        });

        tabs.add("Clubs", clubPanel);
        tabs.add("Students", stuPanel);
        tabs.add("Membership", memPanel);
        tabs.add("Search", searchPanel);
        tabs.add("Upload Folder", uploadPanel);
        add(tabs);
        refreshTable(); refreshCombos(); refreshStudentsArea(); doSearch("");
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClubManagementGUI());
    }
}
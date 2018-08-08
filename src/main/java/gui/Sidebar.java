package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import util.GeneralFileFilter;

public class Sidebar extends JPanel {
	private File dir;
	private final TextField field;
	private final JList<String> list;

	private final DefaultListModel<String> model = new DefaultListModel<>();
	private final DxfViewer viewer;

	public Sidebar(DxfViewer viewer) {
		this.viewer = viewer;
		this.list = new JList<>(this.model);
		this.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JLabel frameDxfLabel = new JLabel("外枠のDXFファイル");
		this.field = new TextField(19);
		JButton frameFileButton = new JButton("Open");
		frameFileButton.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new GeneralFileFilter("dxf", "DXF", true));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int selected = chooser.showOpenDialog(Sidebar.this);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				Sidebar.this.field.setText(file.getAbsolutePath());
			}
		});

		JLabel dxfDirLabel = new JLabel("DXFファイル一覧");

		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(list);
		JButton chgDirbutton = new JButton("Change Dir");
		chgDirbutton.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new GeneralFileFilter("dxf", "DXF", true));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int selected = chooser.showOpenDialog(Sidebar.this);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				File dir = file;
				Sidebar.this.openDirectry(dir);
			}
		});
		this.list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String selection = Sidebar.this.list.getSelectedValue();
					try {
						File file = new File(Sidebar.this.dir.getCanonicalPath() + File.separatorChar + selection);
						if (file.isDirectory()) {
							Sidebar.this.openDirectry(file);
						} else {
							Sidebar.this.viewer.openFile(file);
						}
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setPreferredSize(new Dimension(280, 5));
		scroll.setPreferredSize(new Dimension(250, 100));

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.ipadx = 0;
		gbc.ipady = 10;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		layout.setConstraints(frameDxfLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		layout.setConstraints(this.field, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		layout.setConstraints(frameFileButton, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.ipadx = 0;
		gbc.ipady = 5;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(separator, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.ipadx = 0;
		gbc.ipady = 10;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		layout.setConstraints(dxfDirLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 1.0;
		gbc.ipadx = 10;
		gbc.ipady = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.VERTICAL;
		layout.setConstraints(scroll, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		layout.setConstraints(chgDirbutton, gbc);

		this.add(frameDxfLabel);
		this.add(this.field);
		this.add(frameFileButton);
		this.add(separator);
		this.add(dxfDirLabel);
		this.add(scroll);
		this.add(chgDirbutton);
	}

	void openDirectry(File dir) {
		if (dir.isDirectory()) {
			this.model.clear();
			this.dir = dir;
			try {
				if (!"/".equals(this.dir.getCanonicalPath())) {
					this.model.addElement("../");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (File f : dir.listFiles(new GeneralFileFilter("dxf", "DXF", true))) {
				this.model.addElement(f.getName());
			}
		}
	}
}

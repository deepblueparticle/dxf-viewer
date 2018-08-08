package gui;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import util.GeneralFileFilter;
import util.MapList;
import dxf.DxfData;
import dxf.DxfReader;
import dxf.section.entities.DxfDimension;
import dxf.section.entities.DxfInsert;
import dxf.section.entities.DxfEntity;

/**
 * Dimension & Insert Viewer
 * 
 * @author FUJIWARA Masayasu
 * @since 0.02
 */
public class DimensionViewer extends JFrame {

	public static final boolean debug = true;

	/**
	 * JFrameに表示するタイトル
	 */
	private static final String TITLE = "Dimension & Insert Viewer";

	/**
	 * DXFファイル表示のためのコンポーネント
	 */
	final DimensionComponent comp;

	private List<DxfEntity> entities;

	private int index = 0;

	/**
	 * DXFファイルリーダ
	 */
	final DxfReader reader;

	/**
	 * コンストラクタ メニューなどの設定を行います。
	 */
	public DimensionViewer() {
		this.entities = new ArrayList<DxfEntity>();
		this.setTitle(DimensionViewer.TITLE);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		final ViewingEnvironment env = new ViewingEnvironment();
		this.comp = new DimensionComponent(env);
		new MouseListenerAdaptor2(this.comp);
		ActionListener actionListener = e -> {
			String command = e.getActionCommand();
			Object source = e.getSource();
			if (source instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) source;
				env.setParam(command, checkbox.getState() ? 1 : 0);
				this.comp.repaint();
			}
		};

		JMenuBar menubar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic('F');
		menubar.add(menuFile);

		{
			JMenuItem item = new JMenuItem("Open File");
			item.setMnemonic('O');
			item.addActionListener(e -> {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new GeneralFileFilter("dxf", "DXF File Format"));
				int ret = chooser.showOpenDialog(this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					if (file.isFile()) {
						this.openFile(file);
					}
				}
			});
			menuFile.add(item);
		}

		menuFile.addSeparator();

		{
			JMenuItem item = new JMenuItem("Exit");
			item.setMnemonic('X');
			item.addActionListener(e -> {
				System.exit(0);
			});
			menuFile.add(item);
		}

		JMenu menuView = new JMenu("View");
		menuView.setMnemonic('V');
		menubar.add(menuView);

		{
			JMenuItem item = new JMenuItem("Previous Entity");
			item.setMnemonic('P');
			item.setAccelerator(KeyStroke.getKeyStroke('P', InputEvent.ALT_DOWN_MASK));
			item.addActionListener(e -> {
				this.previousEntity();
			});
			menuView.add(item);
		}
		
		{
			JMenuItem item = new JMenuItem("Next Entity");
			item.setMnemonic('N');
			item.setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.ALT_DOWN_MASK));
			item.addActionListener(e -> {
				this.nextEntity();
			});
			menuView.add(item);
		}

		menuView.addSeparator();

		{
			JMenuItem item = new JMenuItem("Reset View");
			item.setMnemonic('0');
			item.setAccelerator(KeyStroke.getKeyStroke('0', InputEvent.CTRL_DOWN_MASK));
			item.addActionListener(e -> {
				this.comp.setDefaultView();
			});
			menuView.add(item);
		}

		{
			JMenuItem item = new JMenuItem("Scale Up");
			item.setMnemonic('+');
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_DOWN_MASK));
			item.addActionListener(e -> {
				this.comp.updateScale(1.1, this.comp.getWidth() / 2, this.comp.getHeight() / 2);
			});
			menuView.add(item);
		}

		{
			JMenuItem item = new JMenuItem("Scale Down");
			item.setMnemonic('-');
			item.setAccelerator(KeyStroke.getKeyStroke('-', InputEvent.CTRL_DOWN_MASK));
			item.addActionListener(e -> {
				this.comp.updateScale(0.9, this.comp.getWidth() / 2, this.comp.getHeight() / 2);
			});
			menuView.add(item);
		}

		{
			JMenuItem item = new JMenuItem("Reset Scale");
			item.addActionListener(e -> {
				env.setX(0);
				env.setY(0);
				env.setScale(1);
			});
			menuView.add(item);
		}

		menuView.addSeparator();

		{
			JMenuItem item = new JMenu("Line");
			menuView.add(item);

			ActionListener lineMenuListener = e -> {
				String command = e.getActionCommand();
				Object source = e.getSource();
				if (source instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) source;
					env.switchLineVisible(command, checkbox.getState());
					this.comp.repaint();
				}
			};

			String[] styles = new String[] { "CONTINUOUS", "HIDDEN", "CENTER", "PHANTOM", "BYBLOCK" };
			for (String style : styles) {
				JCheckBoxMenuItem subitem = new JCheckBoxMenuItem(style, env.checkLineVisuble(style));
				subitem.addActionListener(lineMenuListener);
				item.add(subitem);
			}
		}

		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(ViewingEnvironment.labelDimension, env.getParam(ViewingEnvironment.labelDimension) > 0);
			item.addActionListener(actionListener);
			menuView.add(item);
		}

		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(ViewingEnvironment.labelInsert, env.getParam(ViewingEnvironment.labelInsert) > 0);
			item.addActionListener(actionListener);
			menuView.add(item);
		}

		menuView.addSeparator();

		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(ViewingEnvironment.labelAntialiasing, env.getParam(ViewingEnvironment.labelAntialiasing) > 0);
			item.addActionListener(actionListener);
			menuView.add(item);
		}

		menuView.addSeparator();

		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(ViewingEnvironment.labelSelectionAlways, env.getParam(ViewingEnvironment.labelSelectionAlways) > 0);
			item.addActionListener(actionListener);
			menuView.add(item);
		}

		menuView.addSeparator();

		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(ViewingEnvironment.labelError, env.getParam(ViewingEnvironment.labelError) > 0);
			item.addActionListener(actionListener);
			menuView.add(item);
		}

		{
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(ViewingEnvironment.labelCheckline, env.getParam(ViewingEnvironment.labelCheckline) > 0);
			item.addActionListener(actionListener);
			menuView.add(item);
		}

		menuView.addSeparator();

		this.setJMenuBar(menubar);

		DropTargetAdapter drop = new DropTargetAdapter() {

			public void dragOver(DropTargetDragEvent dtde) {
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
				} else {
					dtde.rejectDrag();
				}
			}

			public void drop(DropTargetDropEvent dtde) {
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					Transferable transferable = dtde.getTransferable();
					Object obj;
					try {
						obj = transferable.getTransferData(DataFlavor.javaFileListFlavor);
						if (obj instanceof List) {
							@SuppressWarnings("unchecked")
							List<Object> list = (List<Object>) obj;
							Object o = list.get(0);
							if (o instanceof File) {
								File file = (File) o;
								openFile(file);
							}
						}
					} catch (UnsupportedFlavorException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		new DropTarget(this.comp, DnDConstants.ACTION_COPY_OR_MOVE, drop, true);

		this.sizeScreen();
		this.add(this.comp);

		this.reader = new DxfReader();
	}

	void nextEntity() {
		if (!this.entities.isEmpty()) {
			this.index = (this.index + 1) % this.entities.size();
			System.out.println(this.index);
			this.setEntity(this.entities.get(this.index));
		} else {
			JOptionPane.showMessageDialog(this, "ファイルが読み込まれていません");
		}
	}

	/**
	 * ファイルを開くメソッド
	 * 
	 * @param file 開くファイル
	 */
	public void openFile(File file) {
		System.out.println(DimensionViewer.class.getName() + " # open file: " + file.getName());
		try {
			DxfData dxf = this.reader.readDXF(file);
			this.setTitle(DimensionViewer.TITLE + " - " + file.getName());
			MapList<String, DxfEntity> map = dxf.getDxfData();
			List<DxfEntity> dimensions = map.get(DxfDimension.class.getName());
			List<DxfEntity> inserts = map.get(DxfInsert.class.getName());
			this.entities.addAll(dimensions);
			this.entities.addAll(inserts);
			this.index = 0;
			if (this.entities.size() > 0) {
				this.setEntity(this.entities.get(this.index));
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, new String[] { "指定されたファイルを開くことができませんでした。", e.getMessage() });
			System.exit(1);
		}
	}

	/**
	 * ファイルを開くメソッド
	 * 
	 * @param path 開くファイルのパス
	 */
	public void openFile(String path) {
		this.openFile(new File(path));
	}

	void previousEntity() {
		if (!this.entities.isEmpty()) {
			this.index = (this.entities.size() + this.index - 1) % this.entities.size();
			System.out.println(this.index);
			this.setEntity(this.entities.get(this.index));
		} else {
			JOptionPane.showMessageDialog(this, "ファイルが読み込まれていません");
		}
	}

	private void setEntity(DxfEntity entity) {
		this.comp.set(entity);
		this.comp.repaint();
	}

	/**
	 * フレームをスクリーンいっぱいに表示させるためのメソッド
	 */
	private void sizeScreen() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		GraphicsConfiguration config = this.getGraphicsConfiguration();
		Insets insets = kit.getScreenInsets(config);
		screenSize.width -= insets.left + insets.right;
		screenSize.height -= insets.top + insets.bottom;
		this.setSize(screenSize);
		this.setLocation(insets.left, insets.top);
	}
}

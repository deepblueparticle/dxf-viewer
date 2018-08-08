package gui;

import java.awt.BorderLayout;
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
import dxf.DxfData;
import dxf.DxfReader;
import dxf.checker.DimensionChecker;
import dxf.checker.DxfChecker;

/**
 * DXF Viewer
 * 
 * @author FUJIWARA Masayasu
 * @since 0.02
 */
public class DxfViewer extends JFrame {

	public static boolean debug = false;

	/**
	 * JFrameに表示するタイトル
	 */
	private static final String TITLE = "DXF Checker";

	/**
	 * DXF Checker
	 */
	private List<DxfChecker> checkers;

	/**
	 * DXFファイル表示のためのコンポーネント
	 */
	final DxfComponent comp;

	/**
	 * DXFファイルリーダ
	 */
	final DxfReader reader;

	final Sidebar sidebar;

	/**
	 * コンストラクタ メニューなどの設定を行います。
	 */
	public DxfViewer() {
		this.setTitle(DxfViewer.TITLE);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		final ViewingEnvironment env = new ViewingEnvironment();
		this.comp = new DxfComponent(env);
		this.sidebar = new Sidebar(this);
		new MouseListenerAdaptor(this.comp);
		ActionListener actionListener = e -> {
			String command = e.getActionCommand();
			Object source = e.getSource();
			if (source instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) source;
				env.setParam(command, checkbox.getState() ? 1 : 0);
				this.comp.repaint();
			}
		};

		this.checkers = new ArrayList<DxfChecker>();
		// this.checkers.add(new ScrewChecker());
		this.checkers.add(new DimensionChecker());
		// this.checkers.add(new PositionChecker());

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
				this.setVisible(false);
				this.dispose();
			});
			menuFile.add(item);
		}

		JMenuItem menuEdit = new JMenu("Edit");
		menuEdit.setMnemonic('E');
		menubar.add(menuEdit);

		{
			JMenuItem item = new JMenuItem("Clear Selection Area");
			item.addActionListener(e -> {
				this.comp.clearSelection();
				this.comp.repaint();
			});
			menuEdit.add(item);
		}

		JMenu menuView = new JMenu("View");
		menuView.setMnemonic('V');
		menubar.add(menuView);

		{
			JMenuItem item = new JMenuItem("Reset View");
			item.setMnemonic('0');
			item.setAccelerator(KeyStroke.getKeyStroke('0', InputEvent.CTRL_DOWN_MASK));
			item.addActionListener(e -> {
				this.comp.setDefaultView();
			});
			menuEdit.add(item);
		}

		{
			JMenuItem item = new JMenuItem("Scale Up");
			item.setMnemonic('+');
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_DOWN_MASK));
			item.addActionListener(e -> {
				this.comp.updateScale(1.1, this.comp.getWidth() / 2, this.comp.getHeight() / 2);
			});
			menuEdit.add(item);
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
			JMenu item = new JMenu("Line");
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

		
		JMenu menuInspection = new JMenu("Inspection");
		//menubar.add(menuInspection);

		{
			JMenuItem item = new JMenuItem("Inspection");
			item.addActionListener(e -> {
				DxfData dxf = this.comp.getDXF();
				this.comp.clearErrorEntities();
				this.checkDrawing(dxf);
				this.comp.repaint();
			});
			menuInspection.add(item);
		}

		{
			JMenuItem item = new JMenuItem("Clear Result");
			item.addActionListener(e -> {
				this.comp.clearErrorEntities();
				this.comp.repaint();
			});
			menuInspection.add(item);
		}

		menuInspection.addSeparator();

		for (int i = 0; i < this.checkers.size(); i++) {
			final DxfChecker checker = this.checkers.get(i);
			JMenuItem item = new JMenuItem(checker.toString());
			item.addActionListener(e -> {
				DxfData dxf = this.comp.getDXF();
				this.checkDrawing(dxf, checker);
				this.comp.repaint();
			});
			menuInspection.add(item);
		}

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

		this.add(this.sidebar, BorderLayout.WEST);
		this.add(this.comp, BorderLayout.CENTER);
		this.setVisible(true);

		this.reader = new DxfReader();
	}

	/**
	 * すべてのチェッカーで図面をチェックします
	 * 
	 * @param dxf DXFファイルデータ
	 */
	void checkDrawing(DxfData dxf) {
		boolean result = true;
		for (DxfChecker checker : this.checkers) {
			result &= this.checkDrawing(dxf, checker);
		}
		if (result) {
			JOptionPane.showMessageDialog(this.comp, "異常は検出できませんでした。");
		}
	}

	/**
	 * 指定したチェッカーで図面をチェックします。
	 * 
	 * @param dxf DXFファイルのデータ
	 * @param checker チェッカー
	 * @return 異常がなければtrue、異常があればfalseを返します。
	 */
	boolean checkDrawing(DxfData dxf, DxfChecker checker) {
		boolean result = checker.check(dxf);
		if (!result) {
			this.comp.addErrorEntities(checker.getErrorEntities());
			JOptionPane.showMessageDialog(this, checker.getErrorMessage());
		}
		return result;
	}

	public void openDirectory(File dir) {
		System.out.println(dir.getPath());
		this.sidebar.openDirectry(dir);
	}

	public void openDirectory(String path) {
		this.openDirectory(new File(path));
	}

	/**
	 * ファイルを開くメソッド
	 * 
	 * @param file 開くファイル
	 */
	public void openFile(File file) {
		System.out.println(DxfViewer.class.getName() + " # open file: " + file.getName());
		try {
			DxfData dxf = this.reader.readDXF(file);
			this.setTitle(DxfViewer.TITLE + " - " + file.getName());
			this.comp.set(dxf);
			this.checkDrawing(dxf);
			this.comp.repaint();
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

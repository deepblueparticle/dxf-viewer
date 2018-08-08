package util;

import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * 出力用メソッド
 * 
 * @author ma38su
 */
public class Output {

	/**
	 * PNG形式にエクスポートを行います。
	 * 
	 * @param panel
	 *            エクスポート可能なパネル
	 */
	public static void exportPng(final ExportableComponent panel) {
		final JDialog dialog = new JDialog();
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setTitle("エクスポート");
		dialog.setLayout(new GridLayout(0, 1, 0, 0));
		dialog.add(new JLabel("画像のサイズを指定してください", SwingConstants.CENTER));
		LayoutManager fieldLayout = new FlowLayout(FlowLayout.CENTER, 5, 3);
		JPanel widthFieldPanel = new JPanel(fieldLayout);
		final JTextField widthField = new JTextField(5);
		widthField.setText(Integer.toString(panel.getWidth()));
		widthFieldPanel.add(new JLabel("幅 :"));
		widthFieldPanel.add(widthField);

		JPanel heightFieldPanel = new JPanel(fieldLayout);
		final JTextField heightField = new JTextField(5);
		heightField.setText(Integer.toString(panel.getHeight()));
		heightField.setName("高さ");
		heightFieldPanel.add(new JLabel("高さ :"));
		heightFieldPanel.add(heightField);

		JPanel antialiasPanel = new JPanel(fieldLayout);
		final JCheckBox antialiasCheckBox = new JCheckBox();
		antialiasCheckBox.setSelected(true);
		antialiasCheckBox.setText("アンチエイリアス");
		antialiasPanel.add(antialiasCheckBox);

		FieldListener.fixedProportion(widthField, heightField);

		JPanel fieldPanel = new JPanel(fieldLayout);
		fieldPanel.add(widthFieldPanel);
		fieldPanel.add(heightFieldPanel);

		JPanel buttonPanel = new JPanel(fieldLayout);
		JButton export = new JButton("エクスポート");
		export.addActionListener(e -> {
			int width = 0;
			int height = 0;
			try {
				width = Integer.parseInt(widthField.getText());
				height = Integer.parseInt(heightField.getText());
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(dialog, "整数を入力してください。", "警告", JOptionPane.ERROR_MESSAGE);
				return;
			}
			dialog.setAlwaysOnTop(false);
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
			GeneralFileFilter.addRasterGraphicsFileFilter(chooser);
			int returnVal = chooser.showDialog(panel, "選択");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("EPS EXPORT");
				dialog.setVisible(false);
				File file = chooser.getSelectedFile();
				String path = file.getPath();
				int index = path.lastIndexOf('.');
				String inputFormat = null;
				if (index != -1) {
					inputFormat = path.substring(index + 1, path.length());
				}

				String selectFormat = ((GeneralFileFilter) chooser.getFileFilter()).getDefaultExtension();
				if (inputFormat == null || inputFormat.compareToIgnoreCase(selectFormat) != 0) {
					file = new File(file.getAbsolutePath() + '.' + selectFormat);
				}
				BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = bi.createGraphics();
				if (antialiasCheckBox.isSelected()) {
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
				}
				panel.printComponent(g, 0, 0, width, height);
				try {
					ImageIO.write(bi, selectFormat, file);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				System.out.println("EPS FIN");
			} else {
				dialog.setAlwaysOnTop(true);
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> {
			dialog.dispose();
		});

		buttonPanel.add(export);
		buttonPanel.add(cancel);

		dialog.add(fieldPanel);
		dialog.add(antialiasPanel);
		dialog.add(buttonPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(panel);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		dialog.setFocusable(true);
	}

	public static void print(Printable printable) throws PrinterException {
		PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(printable);
		if (job.printDialog(attributes)) {
			job.print(attributes);
		}
	}

	private Output() {
	}
}

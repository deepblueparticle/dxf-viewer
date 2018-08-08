package util;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * JTextFormatのサイズの比を維持するためのCaretListenerです。 数値以外の入力は受け付けません。
 * 
 * @author ma38su
 */
public class FieldListener implements CaretListener, FocusListener {

	/**
	 * JTextFieldのフィールドの初期値の比を維持するためのListenerを設定します。
	 * CaretListenerとFocusListenerにより実装されます。 なお、フィールドの初期値は数値でなければなりません。
	 * 
	 * @param field1
	 * @param field2
	 */
	public static void fixedProportion(JTextField field1, JTextField field2) {
		FieldListener listener = new FieldListener(field1, field2);
		field1.addCaretListener(listener);
		field1.addFocusListener(listener);
		field2.addCaretListener(listener);
		field2.addFocusListener(listener);
	}

	private JTextField heightField;
	private double hfRate;
	private boolean isUpdate;
	private int oldHeight;
	private int oldWidth;
	private double wfRate;

	private JTextField widthField;

	/**
	 * 比を維持したいJTextFieldを指定してインスタンスを生成します。
	 * 
	 * @param field1
	 * @param field2
	 */
	private FieldListener(JTextField field1, JTextField field2) {
		try {
			this.oldWidth = Integer.parseInt(field1.getText());
			this.oldHeight = Integer.parseInt(field2.getText());
			this.wfRate = (double) this.oldHeight / this.oldWidth;
			this.hfRate = (double) this.oldWidth / this.oldHeight;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("JTextFieldの値は整数でなければならない。");
		}
		this.widthField = field1;
		this.heightField = field2;
		this.isUpdate = false;
	}

	public void caretUpdate(CaretEvent e) {
		if (!this.isUpdate) {
			this.isUpdate = true;
			Object source = e.getSource();
			if (source.equals(this.widthField)) {
				try {
					String txt = this.widthField.getText();
					if (txt.equals("")) {
						txt = "0";
					} else if (txt.startsWith("0")) {
						throw new NumberFormatException();
					}
					int width = Integer.parseInt(txt);
					int height = (int) (this.wfRate * width + 0.5);
					if (height <= 0) {
						height = 1;
					}
					this.heightField.setText(Integer.toString(height));
					this.oldWidth = width;
					this.oldHeight = height;
				} catch (NumberFormatException ex) {
					int position = e.getDot();
					Document doc = new PlainDocument();
					try {
						doc.insertString(0, Integer.toString(this.oldWidth), null);
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
					this.widthField.setDocument(doc);
					this.widthField.setCaretPosition(position - 1);
				}
			} else if (source.equals(this.heightField)) {
				try {
					String txt = this.heightField.getText();
					if (txt.equals("")) {
						txt = "0";
					} else if (txt.startsWith("0")) {
						throw new NumberFormatException();
					}
					int height = Integer.parseInt(txt);
					int width = (int) (this.hfRate * height + 0.5);
					if (width <= 0) {
						width = 1;
					}
					this.widthField.setText(Integer.toString(width));
					this.oldHeight = height;
					this.oldWidth = width;
				} catch (NumberFormatException ex) {
					int position = e.getDot();
					Document doc = new PlainDocument();
					try {
						doc.insertString(0, Integer.toString(this.oldHeight), null);
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
					this.heightField.setDocument(doc);
					this.heightField.setCaretPosition(position - 1);
				}
			} else {
				throw new IllegalArgumentException("コンストラクタで与えた以外のインスタンス以外からの呼び出しは受け付けない");
			}
			this.isUpdate = false;
		}
	}

	public void focusGained(FocusEvent e) {
		if ("".equals(this.heightField.getText())) {
			Document doc = new PlainDocument();
			try {
				doc.insertString(0, "1", null);
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
			this.heightField.setDocument(doc);
		}
		if ("".equals(this.widthField.getText())) {
			Document doc = new PlainDocument();
			try {
				doc.insertString(0, "1", null);
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
			this.widthField.setDocument(doc);
		}
	}

	public void focusLost(FocusEvent e) {
	}
}
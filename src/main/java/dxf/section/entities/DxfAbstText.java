package dxf.section.entities;

public interface DxfAbstText extends DxfEntity {
	public String getText();

	public double getX();

	public double getY();

	public boolean isRequired();
}

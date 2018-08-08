package dxf.section.entities;

import java.util.Collection;

public abstract class DxfEntities implements DxfEntity {
	protected void addEntity(DxfEntity entity) {
		this.getEntities().add(entity);
	}

	private double getDistance(double x, double y) {
		double minDist = Double.MAX_VALUE;
		for (DxfEntity entity : getEntities()) {
			if (entity instanceof DxfLine) {
				DxfLine line = (DxfLine) entity;
				double d = line.ptSegDistSq(x, y);
				if (minDist > d) {
					minDist = d;
				}
			}
		}
		return minDist;
	}

	protected abstract Collection<DxfEntity> getEntities();

	public boolean hasText() {
		for (DxfEntity entity : this.getEntities()) {
			if (entity instanceof DxfText || entity instanceof DxfMtext) {
				return true;
			}
		}
		return false;
	}

	public void joinText(Collection<DxfAbstText> texts) {
		double minDist = Double.POSITIVE_INFINITY;
		DxfAbstText nearest = null;
		for (DxfAbstText text : texts) {
			double d = getDistance(text.getX(), text.getY());
			if (minDist > d) {
				minDist = d;
				nearest = text;
			}
		}
		if (nearest != null) {
			addEntity(nearest);
		}
	}
}

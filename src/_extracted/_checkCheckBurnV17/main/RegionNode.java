package main;

public class RegionNode {
	
	
	private RegionNode parentRegion;
	private Region region;
	private Integer distanceFromStart;
	
	
	public RegionNode(RegionNode parentRegion,Region region,Integer distanceFromStart)
	{
		this.setDistanceFromStart(distanceFromStart);
		this.setParentRegion(parentRegion);
		this.setRegion(region);
	}


	public RegionNode getParentRegion() {
		return parentRegion;
	}


	public void setParentRegion(RegionNode parentRegion) {
		this.parentRegion = parentRegion;
	}


	public Region getRegion() {
		return region;
	}


	public void setRegion(Region region) {
		this.region = region;
	}


	public Integer getDistanceFromStart() {
		return distanceFromStart;
	}


	public void setDistanceFromStart(Integer distanceFromStart) {
		this.distanceFromStart = distanceFromStart;
	}

}

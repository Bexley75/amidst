package amidst.gui.seedsearcher;

import amidst.documentation.Immutable;
import amidst.mojangapi.world.WorldType;
import amidst.mojangapi.world.filter.WorldFilter;

@Immutable
public class SeedSearcherConfiguration {
	private final WorldFilter worldFilter;
	private final WorldType worldType;
	private final boolean searchContinuously;
	private final boolean isRandom;
	public long currentSeed = 0;

	public SeedSearcherConfiguration(WorldFilter worldFilter, WorldType worldType, boolean searchContinuously, boolean isRandom, long currentSeed) {
		this.worldFilter = worldFilter;
		this.worldType = worldType;
		this.searchContinuously = searchContinuously;
		this.isRandom = isRandom;
		this.currentSeed = currentSeed;
	}

	public WorldFilter getWorldFilter() {
		return worldFilter;
	}

	public WorldType getWorldType() {
		return worldType;
	}

	public boolean isSearchContinuously() {
		return searchContinuously;
	}
	public boolean isRandom() {
		return isRandom;
	}

}

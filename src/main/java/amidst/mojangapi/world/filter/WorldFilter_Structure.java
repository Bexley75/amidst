package amidst.mojangapi.world.filter;

import amidst.documentation.Immutable;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;
import amidst.mojangapi.world.icon.WorldIcon;
import amidst.mojangapi.world.icon.producer.NameFilteredWorldIconCollector;
import amidst.mojangapi.world.icon.producer.WorldIconCollector;
import amidst.mojangapi.world.icon.producer.WorldIconProducer;
import amidst.mojangapi.world.icon.type.DefaultWorldIconTypes;

import java.util.LinkedList;
import java.util.List;

@Immutable
public class WorldFilter_Structure extends WorldFilter {
	private final DefaultWorldIconTypes structure;
	private final int count;
	private int clusterDistance = 180;
	private final int clusterSize;

	public WorldFilter_Structure(long worldFilterSize, DefaultWorldIconTypes structure, int count, int clusterSize, int clusterDistance) {
		super(worldFilterSize);
		this.structure = structure;
		this.count = count;
		this.clusterSize = clusterSize;
		if(clusterDistance > 0)
		{
			this.clusterDistance = clusterDistance;
		}
	}

	@Override
	public boolean isValid(World world) {
		WorldIconCollector structureCollector = getCollector();
		procudeAndCollect(getProducer(world), structureCollector);
		if (clusterSize > 1) {
			return checkClusterSize(structureCollector.get());
		} else {
			return structureCollector.get().size() >= count;
		}
	}

	private void procudeAndCollect(WorldIconProducer<Void> structureProducer, WorldIconCollector structureCollector) {
		for (long x = 0; x < 2 * worldFilterSize; x += 512) {
			for (long y = 0; y < 2 * worldFilterSize; y += 512) {
				structureProducer.produce(CoordinatesInWorld.from(x, y).add(corner), structureCollector, null);
			}
		}
	}
	private boolean checkClusterSize(List<WorldIcon> structures) {

		for (WorldIcon structure : structures)
		{
			List<WorldIcon> structuresInRange = new LinkedList<>();

			structuresInRange.add(structure);

			for (WorldIcon structureToCompare : structures)
			{
				if (structure != structureToCompare && IsWithinRange(structure, structureToCompare, clusterDistance))
				{
					structuresInRange.add(structureToCompare);
					int count = 1;//starting with this one
					for (WorldIcon structureInRange : structuresInRange)
					{
						if (structureToCompare != structureInRange && IsWithinRange(structureToCompare, structureInRange, clusterDistance))
						{
							count++;//a structure was in range of the one we just added
						}
					}

					if(count >= clusterSize)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean IsWithinRange(WorldIcon s1, WorldIcon s2, long maxDistance)
	{
		return Math.abs(s1.getCoordinates().getX() - s2.getCoordinates().getX()) < maxDistance
				&& Math.abs(s1.getCoordinates().getY() - s2.getCoordinates().getY()) < maxDistance;
	}

	private WorldIconProducer<Void> getProducer(World world) {
		switch (structure) {
		case JUNGLE:
		case DESERT:
		case IGLOO:
		case WITCH:
			return world.getTempleProducer();
		case STRONGHOLD:
			return world.getStrongholdProducer();
		case VILLAGE:
			return world.getVillageProducer();
		case OCEAN_MONUMENT:
			return world.getOceanMonumentProducer();
		case MINESHAFT:
			return world.getMineshaftProducer();
		default:
			throw new IllegalArgumentException("Unsupported structure type: " + structure.getName());
		}
	}

	private WorldIconCollector getCollector() {
		switch (structure) {
		case JUNGLE:
		case DESERT:
		case IGLOO:
		case WITCH:
			return new NameFilteredWorldIconCollector(structure.getLabel());
		case STRONGHOLD:
		case VILLAGE:
		case OCEAN_MONUMENT:
		case MINESHAFT:
			return new WorldIconCollector();
		default:
			throw new IllegalArgumentException("Unsupported structure type: " + structure.getName());
		}
	}
}

package amidst.gui.seedsearcher;

import java.util.function.Consumer;

import amidst.documentation.AmidstThread;
import amidst.documentation.CalledOnlyBy;
import amidst.documentation.NotThreadSafe;
import amidst.gui.main.MainWindowDialogs;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.MojangApi;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.WorldSeed;
import amidst.threading.WorkerExecutor;
import amidst.threading.worker.ProgressReporter;
import amidst.threading.worker.ProgressReportingWorker;

@NotThreadSafe
public class SeedSearcher {
	private final MainWindowDialogs dialogs;
	private final MojangApi mojangApi;
	private final WorkerExecutor workerExecutor;

	private volatile boolean isSearching = false;
	private volatile boolean isStopRequested = false;

	@CalledOnlyBy(AmidstThread.EDT)
	public SeedSearcher(MainWindowDialogs dialogs, MojangApi mojangApi, WorkerExecutor workerExecutor) {
		this.dialogs = dialogs;
		this.mojangApi = mojangApi.createSilentPlayerlessCopy();
		this.workerExecutor = workerExecutor;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void search(SeedSearcherConfiguration configuration, Consumer<WorldSeed> onWorldSeedFound) {
		this.isSearching = true;
		this.isStopRequested = false;
		workerExecutor.run(createSearcher(configuration), onWorldSeedFound);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private ProgressReportingWorker<WorldSeed> createSearcher(SeedSearcherConfiguration configuration) {
		return reporter -> this.trySearch(reporter, configuration);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void stop() {
		this.isStopRequested = true;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void dispose() {
		stop();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public boolean isSearching() {
		return isSearching;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public boolean isStopRequested() {
		return isStopRequested;
	}

	@CalledOnlyBy(AmidstThread.WORKER)
	private void trySearch(ProgressReporter<WorldSeed> reporter, SeedSearcherConfiguration configuration) {
		try {
			doSearch(reporter, configuration);
		} catch (IllegalStateException | MinecraftInterfaceException e) {
			AmidstLogger.warn(e);
			dialogs.displayError(e);
		} finally {
			this.isSearching = false;
			this.isStopRequested = false;
		}
	}

	@CalledOnlyBy(AmidstThread.WORKER)
	private void doSearch(ProgressReporter<WorldSeed> reporter, SeedSearcherConfiguration configuration)
			throws IllegalStateException,
			MinecraftInterfaceException {
		do {
			doSearchOne(reporter, configuration);
		} while (configuration.isSearchContinuously() && !isStopRequested);
	}

	@CalledOnlyBy(AmidstThread.WORKER)
	private void doSearchOne(ProgressReporter<WorldSeed> reporter, SeedSearcherConfiguration configuration)
			throws MinecraftInterfaceException {

			while (!isStopRequested)
			{
				World world = null;
				if (configuration.isRandom())
				{
					world = mojangApi.createWorldFromSeed(WorldSeed.random(), configuration.getWorldType());
					configuration.currentSeed = world.getWorldSeed().getLong();
				} else
				{
					world = mojangApi.createWorldFromSeed(WorldSeed.fromSaveGame(configuration.currentSeed++), configuration.getWorldType());
				}
				if (configuration.getWorldFilter().isValid(world))
				{
					reporter.report(world.getWorldSeed());
					break;
				}
			}
	}
}
package amidst.gui.seedsearcher;

import java.util.function.Consumer;

import amidst.documentation.AmidstThread;
import amidst.documentation.CalledOnlyBy;
import amidst.documentation.NotThreadSafe;
import amidst.gui.main.MainWindowDialogs;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.MojangApi;
import amidst.mojangapi.minecraftinterface.MinecraftInterfaceException;
import amidst.seedSearch.SearchFilter;
import amidst.seedSearch.SearchResult;
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
	public void search(SearchFilter searchFilter, Consumer<SearchResult> onSearchResult) {
		this.isSearching = true;
		this.isStopRequested = false;
		workerExecutor.run(createSearcher(searchFilter), onSearchResult);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private ProgressReportingWorker<SearchResult> createSearcher(SearchFilter searchFilter) {
		return reporter -> this.trySearch(reporter, searchFilter);
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
	private void trySearch(ProgressReporter<SearchResult> reporter, SearchFilter searchFilter) {
		try {
			doSearch(reporter, searchFilter);
		} catch (IllegalStateException | MinecraftInterfaceException e) {
			AmidstLogger.warn(e);
			dialogs.displayError(e);
		} finally {
			this.isSearching = false;
			this.isStopRequested = false;
		}
	}

	@CalledOnlyBy(AmidstThread.WORKER)
	private void doSearch(ProgressReporter<SearchResult> reporter, SearchFilter searchFilter)
			throws IllegalStateException,
			MinecraftInterfaceException {
		while (!isStopRequested && doSearchOne(reporter, searchFilter))
		{
		}
	}

	@CalledOnlyBy(AmidstThread.WORKER)
	private boolean doSearchOne(ProgressReporter<SearchResult> reporter, SearchFilter searchFilter)
			throws MinecraftInterfaceException {

		while (!isStopRequested)
		{
			SearchResult searchResult = searchFilter.search(mojangApi);
			reporter.report(searchResult);
			if(searchResult.worldSeed == null)
			{
				return false;
			}
		}
		return true;
	}
}

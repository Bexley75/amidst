package amidst.gui.seedsearcher;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.border.LineBorder;

import amidst.AmidstMetaData;
import amidst.documentation.AmidstThread;
import amidst.documentation.CalledOnlyBy;
import amidst.documentation.NotThreadSafe;
import amidst.gui.main.MainWindowDialogs;
import amidst.gui.main.WorldSwitcher;
import amidst.gui.main.viewer.ViewerFacade;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.world.World;
import amidst.mojangapi.world.WorldSeed;
import amidst.mojangapi.world.WorldType;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;
import amidst.mojangapi.world.filter.WorldFilter;
import amidst.mojangapi.world.icon.WorldIcon;
import amidst.seedSearch.SearchFilter;
import amidst.seedSearch.SearchResult;
import net.miginfocom.swing.MigLayout;

@NotThreadSafe
public class SeedSearcherWindow
{
    private final AmidstMetaData metadata;
    private final MainWindowDialogs dialogs;
    private final WorldSwitcher worldSwitcher;
    private final SeedSearcher seedSearcher;
    private final Supplier<ViewerFacade> viewerFacadeSupplier;

    private final JTextArea searchQueryTextArea;
    private final JComboBox<SearchResult> resultsComboBox;
    private final JComboBox<WorldIcon> coordsComboBox;
    private final JCheckBox searchContinuouslyCheckBox;
    private final JCheckBox randomCheckBox;
    private final JTextField seedField;
    private final JButton searchButton;
    private final JButton clearButton;
    private final JFrame frame;

    @CalledOnlyBy(AmidstThread.EDT)
    public SeedSearcherWindow(
            AmidstMetaData metadata,
            MainWindowDialogs dialogs,
            WorldSwitcher worldSwitcher,
            SeedSearcher seedSearcher,
            Supplier<ViewerFacade> viewerFacadeSupplier)
    {
        this.metadata = metadata;
        this.dialogs = dialogs;
        this.worldSwitcher = worldSwitcher;
        this.viewerFacadeSupplier = viewerFacadeSupplier;
        this.seedSearcher = seedSearcher;
        this.seedField = new JTextField();
        this.searchQueryTextArea = createSearchQueryTextArea();
        this.resultsComboBox = createResultsComboBox();
        this.coordsComboBox = createCoordsComboBox();
        this.searchContinuouslyCheckBox = createSearchContinuouslyCheckBox();
        this.randomCheckBox = new JCheckBox("search randomly");
        this.searchButton = createSearchButton();
        this.clearButton = createClearButton();
        this.frame = createFrame();
    }

    @CalledOnlyBy(AmidstThread.EDT)
    public void show()
    {
        this.frame.setVisible(true);
        updateGUI();
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JTextArea createSearchQueryTextArea()
    {
        return new JTextArea();
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JCheckBox createSearchContinuouslyCheckBox()
    {
        return new JCheckBox("search continuously");
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JComboBox<SearchResult> createResultsComboBox()
    {
        JComboBox<SearchResult> result = new JComboBox<>();
        result.addActionListener(e -> resultsComboBoxChanged());
        return result;
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JComboBox<WorldIcon> createCoordsComboBox()
    {
        JComboBox<WorldIcon> result = new JComboBox<>();
        result.addActionListener(e -> coordsComboBoxChanged());
        return result;
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JButton createSearchButton()
    {
        JButton result = new JButton("Search");
        result.addActionListener(e -> searchButtonClicked());
        return result;
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JButton createClearButton()
    {
        JButton result = new JButton("Clear");
        result.addActionListener(e -> clearButtonClicked());
        return result;
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JFrame createFrame()
    {
        JFrame result = new JFrame("Seed Searcher");
        result.setIconImages(metadata.getIcons());
        result.getContentPane().setLayout(new MigLayout());
        result.add(new JLabel("Search Query:"), "cell 0 0");
        result.add(createScrollPane(searchQueryTextArea), "cell 0 1,dock center");
        result.add(new JLabel("Search Results:"), "cell 0 2");
        result.add(resultsComboBox, "cell 0 2, growx");
        result.add(coordsComboBox, "cell 0 2,width 25%");
        result.add(searchButton, "cell 0 3");
        result.add(clearButton, "cell 0 3");
        result.add(seedField, "cell 0 3, width 25%");
        result.setSize(1024, 768);
        result.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        return result;
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JScrollPane createScrollPane(JTextArea textArea)
    {
        JScrollPane result = new JScrollPane(textArea);
        result.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        result.setBorder(new LineBorder(Color.darkGray, 1));
        return result;
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private void coordsComboBoxChanged()
    {
        WorldIcon worldIcon = coordsComboBox.getItemAt(coordsComboBox.getSelectedIndex());
        if (worldIcon != null)
        {
            viewerFacadeSupplier.get().centerOn(worldIcon);
        }
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private void resultsComboBoxChanged()
    {
        SearchResult searchResult = resultsComboBox.getItemAt(resultsComboBox.getSelectedIndex());
        if (searchResult != null)
        {
            worldSwitcher.displayWorld(searchResult.worldSeed, searchResult.worldType);
            coordsComboBox.removeAllItems();
            for (WorldIcon worldIcon : searchResult.worldIcons)
            {
                coordsComboBox.addItem(worldIcon);
            }
        }
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private void clearButtonClicked()
    {
        worldSwitcher.clearWorld();
        resultsComboBox.removeAllItems();
        coordsComboBox.removeAllItems();
    }

    @CalledOnlyBy(AmidstThread.EDT)
	private void searchButtonClicked() {
		if (seedSearcher.isSearching()) {
			seedSearcher.stop();
		} else {

			Optional<SearchFilter> searchFilter = createSeedSearch();
			if (searchFilter.isPresent())
			{
				seedSearcher.search(searchFilter.get(), searchResult -> onSearchResult(searchResult));
			}
			else {
				AmidstLogger.warn("invalid configuration");
				dialogs.displayError("invalid configuration");
			}
		}
		updateGUI();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private Optional<SearchFilter> createSeedSearch() {
		return SearchFilter.from(searchQueryTextArea.getText());
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void onSearchResult(SearchResult searchResult) {

	    if(searchResult.match)
        {
            resultsComboBox.addItem(searchResult);
        }
        seedField.setText(searchResult.toString());
		updateGUI();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void updateGUI() {
		if (seedSearcher.isSearching() && !seedSearcher.isStopRequested()) {
			searchButton.setText("Stop");
			searchQueryTextArea.setEditable(false);
			searchContinuouslyCheckBox.setEnabled(false);
		} else {
			searchButton.setText("Search");
			searchQueryTextArea.setEditable(true);
			searchContinuouslyCheckBox.setEnabled(true);
		}
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void dispose() {
		seedSearcher.dispose();
	}
}

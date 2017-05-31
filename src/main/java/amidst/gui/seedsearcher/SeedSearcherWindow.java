package amidst.gui.seedsearcher;

import java.awt.Color;
import java.util.Optional;

import javax.swing.*;
import javax.swing.border.LineBorder;

import amidst.AmidstMetaData;
import amidst.documentation.AmidstThread;
import amidst.documentation.CalledOnlyBy;
import amidst.documentation.NotThreadSafe;
import amidst.gui.main.MainWindowDialogs;
import amidst.gui.main.WorldSwitcher;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.file.json.filter.WorldFilterJson_MatchAll;
import amidst.mojangapi.world.WorldSeed;
import amidst.mojangapi.world.WorldType;
import amidst.mojangapi.world.filter.WorldFilter;
import net.miginfocom.swing.MigLayout;

@NotThreadSafe
public class SeedSearcherWindow {
	private final AmidstMetaData metadata;
	private final MainWindowDialogs dialogs;
	private final WorldSwitcher worldSwitcher;
	private final SeedSearcher seedSearcher;

	private final JTextArea searchQueryTextArea;
	private final JComboBox<WorldType> worldTypeComboBox;
	private final JCheckBox searchContinuouslyCheckBox;
	private final JCheckBox randomCheckBox;
	private final JTextField seedField;
	private final JButton searchButton;
	private final JFrame frame;

	@CalledOnlyBy(AmidstThread.EDT)
	public SeedSearcherWindow(
			AmidstMetaData metadata,
			MainWindowDialogs dialogs,
			WorldSwitcher worldSwitcher,
			SeedSearcher seedSearcher) {
		this.metadata = metadata;
		this.dialogs = dialogs;
		this.worldSwitcher = worldSwitcher;
		this.seedSearcher = seedSearcher;
		this.seedField = new JTextField(50);
		this.searchQueryTextArea = createSearchQueryTextArea();
		this.worldTypeComboBox = createWorldTypeComboBox();
		this.searchContinuouslyCheckBox = createSearchContinuouslyCheckBox();
		this.randomCheckBox = new JCheckBox("search randomly");
		this.searchButton = createSearchButton();
		this.frame = createFrame();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void show() {
		this.frame.setVisible(true);
		updateGUI();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private JTextArea createSearchQueryTextArea() {
		return new JTextArea();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private JComboBox<WorldType> createWorldTypeComboBox() {
		return new JComboBox<>(WorldType.getSelectableArray());
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private JCheckBox createSearchContinuouslyCheckBox() {
		return new JCheckBox("search continuously");
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private JButton createSearchButton() {
		JButton result = new JButton("Search");
		result.addActionListener(e -> searchButtonClicked());
		return result;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private JFrame createFrame() {
		JFrame result = new JFrame("Seed Searcher");
		result.setIconImages(metadata.getIcons());
		result.getContentPane().setLayout(new MigLayout());
		result.add(new JLabel("Search Query:"), "growx, pushx, wrap");
		result.add(createScrollPane(searchQueryTextArea), "grow, push, wrap");
		result.add(new JLabel("World Type:"), "growx, pushx, wrap");
		result.add(worldTypeComboBox, "growx, pushx, wrap");
		result.add(randomCheckBox, "growx, pushx, wrap");
		result.add(searchButton, "pushx, wrap");
		result.add(seedField, "pushx, wrap");
		result.setSize(800, 600);
		result.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		return result;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private JScrollPane createScrollPane(JTextArea textArea) {
		JScrollPane result = new JScrollPane(textArea);
		result.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.setBorder(new LineBorder(Color.darkGray, 1));
		return result;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void searchButtonClicked() {
		if (seedSearcher.isSearching()) {
			seedSearcher.stop();
		} else {
			Optional<SeedSearcherConfiguration> configuration = createSeedSearcherConfiguration();
			if (configuration.isPresent()) {
				SeedSearcherConfiguration seedSearcherConfiguration = configuration.get();
				WorldType worldType = seedSearcherConfiguration.getWorldType();
				seedSearcher.search(seedSearcherConfiguration, worldSeed -> seedFound(worldSeed, worldType, seedSearcherConfiguration.currentSeed));
			} else {
				AmidstLogger.warn("invalid configuration");
				dialogs.displayError("invalid configuration");
			}
		}
		updateGUI();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private Optional<SeedSearcherConfiguration> createSeedSearcherConfiguration() {
		return WorldFilterJson_MatchAll
				.from(searchQueryTextArea.getText())
				.flatMap(WorldFilterJson_MatchAll::createValidWorldFilter)
				.map(this::createSeedSearcherConfiguration);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private SeedSearcherConfiguration createSeedSearcherConfiguration(WorldFilter worldFilter) {
		long seed = 0;
		try
		{
			seed = Long.parseLong(seedField.getText());
		}
		catch(NumberFormatException ex)
		{
			seed = 0;
		}
		return new SeedSearcherConfiguration(
				worldFilter,
				(WorldType) worldTypeComboBox.getSelectedItem(),
				searchContinuouslyCheckBox.isSelected(),
				randomCheckBox.isSelected(),
				seed);
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void seedFound(WorldSeed worldSeed, WorldType worldType, long currentSeed) {
		worldSwitcher.displayWorld(worldSeed, worldType);
		seedField.setText(Long.toString(currentSeed));
		updateGUI();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void updateGUI() {
		if (seedSearcher.isSearching() && !seedSearcher.isStopRequested()) {
			searchButton.setText("Stop");
			searchQueryTextArea.setEditable(false);
			worldTypeComboBox.setEnabled(false);
			searchContinuouslyCheckBox.setEnabled(false);
		} else {
			searchButton.setText("Search");
			searchQueryTextArea.setEditable(true);
			worldTypeComboBox.setEnabled(true);
			searchContinuouslyCheckBox.setEnabled(true);
		}
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public void dispose() {
		seedSearcher.dispose();
	}
}

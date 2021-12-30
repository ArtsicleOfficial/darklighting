package com.darklighting;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
@PluginDescriptor(
	name = "Darklighting"
)
public class DarklightingPlugin extends Plugin
{
	private static final String MARK = "Darklight tile";
	private static final String UNMARK = "Undarklight tile";
	private static final String WALK_HERE = "Walk here";


	@Inject
	private Client client;

	@Inject
	private DarklightingConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ItemManager itemManager;

	private DarklightingOverlay overlay;


	public ArrayList<WorldPoint> groundMarkers = new ArrayList<>();
	public ArrayList<NpcSpawned> nearbyEntities = new ArrayList<>();
	public ArrayList<ItemSpawned> nearbyItems = new ArrayList<>();

	public File markers = new File(RuneLite.RUNELITE_DIR,"darklightmarkers.txt");

	public void loadMakersFromFile() {
		if(!markers.exists()) {
			return;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(markers));
			String line;
			while((line = reader.readLine()) != null) {
				String split[] = line.split(",");
				if(split.length != 3) {
					continue;
				}
				groundMarkers.add(new WorldPoint(Integer.parseInt(split[0]),Integer.parseInt(split[1]),Integer.parseInt(split[2])));
			}
			reader.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	public void saveMarkersToFile() {
		if(groundMarkers.size() == 0) {
			return;
		}
		if(!markers.exists()) {
			try {
				markers.createNewFile();
			} catch(IOException ex) {
				ex.printStackTrace();
				return;
			}
		}
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(markers));
			for(WorldPoint point : groundMarkers) {
				writer.println(point.getX() + "," + point.getY() + "," + point.getPlane());
			}
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	//Ground markers plugin implementation
	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		final boolean hotKeyPressed = client.isKeyPressed(KeyCode.KC_SHIFT);
		if (hotKeyPressed && event.getOption().equals(WALK_HERE))
		{
			final Tile selectedSceneTile = client.getSelectedSceneTile();

			if (selectedSceneTile == null)
			{
				return;
			}

			final WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, selectedSceneTile.getLocalLocation());
			final int regionId = worldPoint.getRegionID();
			final WorldPoint point = new WorldPoint(worldPoint.getX(), worldPoint.getY(), client.getPlane());
			boolean exists = false;
			for(WorldPoint p : groundMarkers) {
				if(p.equals(point)) {
					exists = true;
					break;
				}
			}

			MenuEntry[] menuEntries = client.getMenuEntries();
			menuEntries = Arrays.copyOf(menuEntries, menuEntries.length + 1);
			MenuEntry mark = menuEntries[menuEntries.length - 1] = client.createMenuEntry(menuEntries.length-1);
			mark.setOption(exists ? UNMARK : MARK);
			mark.setTarget(event.getTarget());
			mark.setType(MenuAction.RUNELITE);

			client.setMenuEntries(menuEntries);
		}
	}


	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned) {
		nearbyEntities.add(npcSpawned);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		nearbyEntities.removeIf(spawned -> spawned.getNpc().equals(npcDespawned.getNpc()));
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned) {
		nearbyItems.add(itemSpawned);
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned) {
		nearbyItems.removeIf(spawned -> spawned.getTile().equals(itemDespawned.getTile()) && spawned.getItem().equals(itemDespawned.getItem()));
	}

	//Ground markers plugin implementation
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction().getId() != MenuAction.RUNELITE.getId())
		{
			return;
		}

		Tile target = client.getSelectedSceneTile();
		if (target == null)
		{
			return;
		}

		final String option = event.getMenuOption();
		if (option.equals(MARK) || option.equals(UNMARK))
		{
			markTile(target.getWorldLocation());
		}
	}

	public void markTile(WorldPoint point) {
		for(WorldPoint p : groundMarkers) {
			if(p.equals(point)) {
				groundMarkers.remove(p);
				return;
			}
		}
		groundMarkers.add(point);
	}

	@Override
	protected void startUp()
	{
		overlay = new DarklightingOverlay(this,config.unlitBackground(),config.litBackground(),client.getViewportWidth(),client.getViewportHeight(), config.padding());
		overlayManager.add(overlay);
		loadMakersFromFile();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		saveMarkersToFile();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		overlay.setColors(config.unlitBackground(),config.litBackground());
		overlay.padding = config.padding();
	}

	@Subscribe
	public void onBeforeRender(BeforeRender beforeRender) {
		if(client.getGameState().getState() != GameState.LOGGED_IN.getState()) {
			return;
		}
		if(client.isMenuOpen()) {
			return;
		}

		ArrayList<Rectangle> allRects = new ArrayList<>();
		//Ground markers
		for(WorldPoint point : groundMarkers) {
			if(point.distanceTo2D(client.getLocalPlayer().getWorldLocation()) > config.maxDistance()) {
				continue;
			}
			LocalPoint localPoint = LocalPoint.fromWorld(client,point);
			if(localPoint == null) {
				continue;
			}
			allRects.add(overlay.getRectangle(Perspective.getCanvasTilePoly(client, localPoint)));
		}
		//True/destination/hovered tiles
		if(config.hoveredTile()) {
			//if(client.getSelectedSceneTile() != null) {
				//allRects.add(overlay.getRectangle(Perspective.getCanvasTilePoly(client,client.getSelectedSceneTile().getLocalLocation())));
			if(client.getMouseCanvasPosition().getX() > 0 && client.getMouseCanvasPosition().getY() > 0 && client.getMouseCanvasPosition().getX() < client.getCanvasWidth() && client.getMouseCanvasPosition().getY() < client.getCanvasHeight()) {
				allRects.add(overlay.getRectangle(new Rectangle(client.getMouseCanvasPosition().getX() - config.flashlightSize()/2, client.getMouseCanvasPosition().getY() - config.flashlightSize()/2, config.flashlightSize(), config.flashlightSize())));
			}
		}
		if(config.trueTile()) {
			if(client.getLocalPlayer().getLocalLocation() != null) {
				allRects.add(overlay.getRectangle(Perspective.getCanvasTilePoly(client,client.getLocalPlayer().getLocalLocation())));
			}
		}
		if(config.destinationTile()) {
			if(client.getLocalDestinationLocation() != null) {
				allRects.add(overlay.getRectangle(Perspective.getCanvasTilePoly(client,client.getLocalDestinationLocation())));
			}
		}
		//Item markers
		for(ItemSpawned spawned : nearbyItems) {
			if(spawned.getTile().getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) > config.maxDistance()) {
				continue;
			}
			boolean overridePrice = false;
			if(config.itemCustomNames().split(",").length > 0 && !config.itemCustomNames().split(",")[0].isEmpty()) {
				for (String s : config.itemCustomNames().split(",")) {
					if (client.getItemDefinition(spawned.getItem().getId()).getName().toLowerCase().replaceAll(" ", "").startsWith(s.toLowerCase().replaceAll(" ", ""))) {
						overridePrice = true;
					}
				}
			}
			if(!overridePrice && itemManager.getItemPrice(spawned.getItem().getId()) * spawned.getItem().getQuantity() < config.itemMinimumValue()) {
				continue;
			}
			LocalPoint localPoint = LocalPoint.fromWorld(client,spawned.getTile().getWorldLocation());
			allRects.add(overlay.getRectangle(Perspective.getCanvasTilePoly(client, localPoint)));
		}
		//Entity markers
		ArrayList<NpcSpawned> toRemove = new ArrayList<>();
		for(NpcSpawned spawned : nearbyEntities) {
			if(config.entityNames().split(",").length == 0 || config.entityNames().split(",")[0].isEmpty()) {
				continue;
			}
			if(spawned == null || spawned.getNpc() == null || spawned.getNpc().getWorldLocation() == null || spawned.getNpc().getName() == null) {
				continue;
			}
			if(spawned.getNpc().getWorldLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) > config.maxDistance()) {
				continue;
			}
			boolean has = false;
			for(String s : config.entityNames().split(",")) {
				if(spawned.getNpc().getName().toLowerCase().replaceAll(" ","").startsWith(s.toLowerCase().replaceAll(" ",""))) {
					has = true;
				}
			}
			if(!has) {
				continue;
			}
			if(spawned.getNpc().getHealthRatio() == 0) {
				toRemove.add(spawned);
			}
			try {
				Rectangle rect = Perspective.getClickbox(client, spawned.getNpc().getModel(), spawned.getNpc().getOrientation(), spawned.getActor().getLocalLocation()).getBounds();
				allRects.add(overlay.getRectangle(rect));
			} catch (NullPointerException ignored) {
				//probably the player hasn't loaded in yet
			}
		}
		for(NpcSpawned s : toRemove) {
			nearbyEntities.remove(s);
		}
		overlay.update(client.getViewportWidth(),client.getViewportHeight(), allRects);
	}

	@Subscribe
	public void onClientShutdown(ClientShutdown clientShutdown)
	{
		saveMarkersToFile();
	}

	@Provides
	DarklightingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DarklightingConfig.class);
	}
}

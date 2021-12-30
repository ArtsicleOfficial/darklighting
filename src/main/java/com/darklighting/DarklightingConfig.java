package com.darklighting;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("darklighting")
public interface DarklightingConfig extends Config
{

	@ConfigSection(
			name = "General",
			description = "General settings for the plugin",
			position = -1
	)
	String general = "general";

	@Alpha
	@ConfigItem(
			keyName = "padding",
			name = "Highlight Padding",
			description = "Makes the highlights larger by n pixels on each side.",
			section = general
	)
	default int padding() { return 5; }

	@Alpha
	@ConfigItem(
			keyName = "litBackground",
			name = "Overlay Color (Lit)",
			description = "The color of the dark overlay when there is something to highlight on screen.",
			section = general
	)
	default Color litBackground() { return new Color(0,0,0,200); }

	@Alpha
	@ConfigItem(
			keyName = "unlitBackground",
			name = "Overlay Color (Unlit)",
			description = "The color of the dark overlay when there is not something to highlight on screen.",
			section = general
	)
	default Color unlitBackground() { return new Color(0,0,0,240); }

	@ConfigItem(
			keyName = "maxDistance",
			name = "Max Distance (Tiles)",
			description = "Maximum distance for highlights from the character",
			section = general
	)
	default int maxDistance()
	{
		return 50;
	}

	@ConfigItem(
			keyName = "trueTile",
			name = "Highlight Character",
			description = "Whether to highlight your character",
			section = general
	)
	default boolean trueTile()
	{
		return false;
	}

	@ConfigSection(
			name = "Ground Markers",
			description = "(Shift right click a tile to mark/unmark it)",
			position = 2
	)
	String groundMarkers = "groundMarkers";

	@ConfigItem(
			keyName = "destinationTile",
			name = "Destination Tile",
			description = "Whether to highlight your destination tile",
			section = groundMarkers
	)
	default boolean destinationTile()
	{
		return false;
	}


	@ConfigSection(
			name = "Item Markers",
			description = "Darklighting around items",
			position = 0
	)
	String itemMarkers = "itemMarkers";

	@ConfigItem(
		keyName = "itemMinimumValue",
		name = "Item Value Threshold",
		description = "The GE value of an item in order for it to be lit up",
		position = 0,
		section = itemMarkers
	)
	default int itemMinimumValue()
	{
		return 10000;
	}
	@ConfigItem(
			keyName = "itemCustomNames",
			name = "Extra Item Names (,)",
			description = "Comma separated list of items to light up regardless of value",
			section = itemMarkers,
			position = 1
	)
	default String itemCustomNames()
	{
		return "Twisted Bow,Harmonised orb,Inquisitor's Mace";
	}

	@ConfigSection(
			name = "Entity Markers",
			description = "Darklighting around entities",
			position = 1
	)
	String entityMarkers = "entityMarkers";

	@ConfigItem(
			keyName = "entityNames",
			name = "Names (,)",
			description = "Comma separated list of entity names",
			position = 0,
			section = entityMarkers
	)
	default String entityNames()
	{
		return "Monk,Phosani's Nightmare,Vyrewatch Sentinel";
	}

	@ConfigSection(
			name = "Flashlight",
			description = "Darklighting around your mouse",
			position = 3
	)
	String flashlight = "flashlight";

	@ConfigItem(
			keyName = "hoveredTile",
			name = "Mouse Flashlight",
			description = "Whether to highlight the area your mouse is over",
			section = flashlight
	)
	default boolean hoveredTile()
	{
		return true;
	}
	@ConfigItem(
			keyName = "flashlightSize",
			name = "Size",
			description = "Size of flashlight in pixels",
			section = flashlight
	)
	default int flashlightSize()
	{
		return 50;
	}
}

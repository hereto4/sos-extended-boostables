package view.tool;

import util.text.D;

public class PlacableMessages {

	private PlacableMessages() {
		
	}
	
	public static CharSequence ¤¤SOLID_BLOCK = "¤Must be placed on non-solid tiles.";
	public static CharSequence ¤¤ROOM_BLOCK = "¤Must not be placed on room.";
	public static CharSequence ¤¤ROOM_MUST = "¤Must be placed on room.";
	public static CharSequence ¤¤MISC = "¤Blocked by something.";
	public static CharSequence ¤¤STRUCTURE_BLOCK = "¤Blocked by structure. Dismantle it first.";
	public static CharSequence ¤¤ROCK_MUST = "¤Must be placed on rock.";
	public static CharSequence ¤¤TREE_MUST = "¤Must be placed on tree.";
	public static CharSequence ¤¤WATER_MUST = "¤Must be placed on water.";
	public static CharSequence ¤¤WATER_RETURN = "¤Water can only be dug where there is ground water.";
	public static CharSequence ¤¤MOUNTAIN_MUST = "¤Must be placed on solid mountain.";
	public static CharSequence ¤¤MOUNTAIN_NOT = "¤Can't be placed on mountains or caves. Use mountain specific tools for this.";
	public static CharSequence ¤¤STRUCTURE_CLEAR = "¤Must be placed on structures. Walls, ceilings, fortifications or roads.";
	public static CharSequence ¤¤ROAD_ALREADY = "¤Road already exists there.";
	public static CharSequence ¤¤JOB_BLOCK = "¤Blocked by other job.";
	public static CharSequence ¤¤JOB_MUST = "¤Must be placed on jobs.";
	public static CharSequence ¤¤BROKEN_MUST = "¤Must be placed on broken walls or rooms.";
	public static CharSequence ¤¤CAVE_MUST = "¤Must be placed on a cave.";



	public static CharSequence ¤¤MAX_REACHED = "¤Max reached!";
	public static CharSequence ¤¤MAX_SIZE_REACHED = "¤Max size reached!";
	public static CharSequence ¤¤MAX_DIMENSION_REACHED = "¤Max dimension reached!";
	public static CharSequence ¤¤TERRAIN_BLOCK = "¤Blocked by terrain!";
	public static CharSequence ¤¤NOT_EDIBLE = "¤Must be placed on edible vegetation!";
	public static CharSequence ¤¤NOT_RIPE = "¤Edible vegetation must be ripe (Late Summer)!";
	public static CharSequence ¤¤IN_MAP = "¤Must be placed within map!";
	public static CharSequence ¤¤ONE_CLEAR_TILE = "¤Needs at least 1 unblocked tile.";
	public static CharSequence ¤¤SAME_REGION = "¤Needs to be in the same region.";
	public static CharSequence ¤¤REGION = "¤Must be placed in a region!";
	public static CharSequence ¤¤BLOCKED = "¤Blocked!";
	public static CharSequence ¤¤BLOCK_WILL = "¤Will block other tile!";
	public static CharSequence ¤¤BLOCKED_WILL = "¤Will be blocked by other tile!";
	public static CharSequence ¤¤ROOM_OR_STRUCTURE_MUST = "¤Must be placed on rooms or structures!";
	public static CharSequence ¤¤ITEM_MUST = "¤Must be placed on items!";

	
	static {
		D.ts(PlacableMessages.class);
	}
}

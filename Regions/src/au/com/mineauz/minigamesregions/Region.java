package au.com.mineauz.minigamesregions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Event;

import au.com.mineauz.minigames.MinigamePlayer;
import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigamesregions.actions.ActionInterface;
import au.com.mineauz.minigamesregions.conditions.ConditionInterface;

public class Region {
	private String name;
	private Location point1;
	private Location point2;
	private List<RegionExecutor> executors = new ArrayList<RegionExecutor>();
	private List<MinigamePlayer> players = new ArrayList<MinigamePlayer>();
	private long taskDelay = 20;
	private int taskID;
	
	public Region(String name, Location point1, Location point2){
		Location[] locs = MinigameUtils.getMinMaxSelection(point1, point2);
		this.point1 = locs[0].clone();
		this.point2 = locs[1].clone();
		this.name = name;
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Minigames.plugin, new Runnable() {
			
			@Override
			public void run() {
				List<MinigamePlayer> plys = new ArrayList<MinigamePlayer>(players);
				for(MinigamePlayer player : plys){
					execute(RegionTrigger.TICK, player, null);
				}
			}
		}, 0, taskDelay);
	}
	
	public boolean playerInRegion(MinigamePlayer player){
		if(player.getLocation().getWorld() == point1.getWorld()){
			int minx = point1.getBlockX();
			int maxx = point2.getBlockX();
			int plyx = player.getLocation().getBlockX();
			
			if(plyx >= minx && plyx <= maxx){
				int miny = point1.getBlockY();
				int maxy = point2.getBlockY();
				int plyy = player.getLocation().getBlockY();
				
				if(plyy >= miny && plyy <= maxy){
					int minz = point1.getBlockZ();
					int maxz = point2.getBlockZ();
					int plyz = player.getLocation().getBlockZ();
					
					if(plyz >= minz && plyz <= maxz){
						return true;
					}
				}
				
			}
		}
		return false;
	}
	
	public boolean locationInRegion(Location loc){
		if(loc.getWorld() == point1.getWorld()){
			int minx = point1.getBlockX();
			int maxx = point2.getBlockX();
			int plyx = loc.getBlockX();
			
			if(plyx >= minx && plyx <= maxx){
				int miny = point1.getBlockY();
				int maxy = point2.getBlockY();
				int plyy = loc.getBlockY();
				
				if(plyy >= miny && plyy <= maxy){
					int minz = point1.getBlockZ();
					int maxz = point2.getBlockZ();
					int plyz = loc.getBlockZ();
					
					if(plyz >= minz && plyz <= maxz){
						return true;
					}
				}
				
			}
		}
		return false;
	}
	
	public String getName(){
		return name;
	}
	
	public Location getFirstPoint(){
		return point1.clone();
	}
	
	public Location getSecondPoint(){
		return point2.clone();
	}
	
	public boolean hasPlayer(MinigamePlayer player){
		return players.contains(player);
	}
	
	public void addPlayer(MinigamePlayer player){
		players.add(player);
	}
	
	public void removePlayer(MinigamePlayer player){
		players.remove(player);
	}
	
	public List<MinigamePlayer> getPlayers(){
		return players;
	}
	
	public int addExecutor(RegionTrigger trigger){
		executors.add(new RegionExecutor(trigger));
		return executors.size();
	}
	
	public int addExecutor(RegionExecutor exec){
		executors.add(exec);
		return executors.size();
	}
	
	public List<RegionExecutor> getExecutors(){
		return executors;
	}
	
	public void removeExecutor(int id){
		if(executors.size() <= id){
			executors.remove(id - 1);
		}
	}
	
	public void removeExecutor(RegionExecutor executor){
		if(executors.contains(executor)){
			executors.remove(executor);
		}
	}
	
	public void changeTickDelay(long delay){
		removeTickTask();
		taskDelay = delay;
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Minigames.plugin, new Runnable() {
			
			@Override
			public void run() {
				List<MinigamePlayer> plys = new ArrayList<MinigamePlayer>(players);
				for(MinigamePlayer player : plys){
					execute(RegionTrigger.TICK, player, null);
				}
			}
		}, 0, delay);
	}
	
	public long getTickDelay(){
		return taskDelay;
	}
	
	public void removeTickTask(){
		Bukkit.getScheduler().cancelTask(taskID);
	}
	
	public void execute(RegionTrigger trigger, MinigamePlayer player, Event event){
		List<RegionExecutor> toExecute = new ArrayList<RegionExecutor>();
		for(RegionExecutor exec : executors){
			if(exec.getTrigger() == trigger){
				boolean cont = true;
				for(ConditionInterface con : exec.getConditions()){
					if(!con.checkRegionCondition(player, exec.getArguments(), this, event)){
						cont = false;
						break;
					}
				}
				if(cont)
					toExecute.add(exec);
			}
		}
		for(RegionExecutor exec : toExecute)
			for(ActionInterface act : exec.getActions())
				act.executeRegionAction(player, exec.getArguments(), this, event);
	}
}
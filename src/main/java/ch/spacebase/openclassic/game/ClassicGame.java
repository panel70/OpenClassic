package ch.spacebase.openclassic.game;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import ch.spacebase.openclassic.api.Client;
import ch.spacebase.openclassic.api.Color;
import ch.spacebase.openclassic.api.Game;
import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.command.Command;
import ch.spacebase.openclassic.api.command.CommandExecutor;
import ch.spacebase.openclassic.api.command.Sender;
import ch.spacebase.openclassic.api.config.Configuration;
import ch.spacebase.openclassic.api.config.yaml.YamlConfig;
import ch.spacebase.openclassic.api.event.game.CommandNotFoundEvent;
import ch.spacebase.openclassic.api.event.game.PreCommandEvent;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.api.pkg.PackageManager;
import ch.spacebase.openclassic.api.plugin.Plugin;
import ch.spacebase.openclassic.api.plugin.PluginManager;
import ch.spacebase.openclassic.api.scheduler.Scheduler;
import ch.spacebase.openclassic.api.translate.Language;
import ch.spacebase.openclassic.api.translate.Translator;
import ch.spacebase.openclassic.game.scheduler.ClassicScheduler;

import com.zachsthings.onevent.EventManager;

public abstract class ClassicGame implements Game {

	private final File directory;
	
	private final Configuration config;
	private final ClassicScheduler scheduler = new ClassicScheduler(this instanceof Client ? "Client" : "Server");
	
	private final PluginManager pluginManager = new PluginManager();
	private final PackageManager pkgManager;
	private final Translator translator = new Translator();
	
	private final Map<Object, List<Command>> commands = new HashMap<Object, List<Command>>();
	protected final Map<Object, List<CommandExecutor>> executors = new HashMap<Object, List<CommandExecutor>>();
	private final Map<String, Generator> generators = new HashMap<String, Generator>();
	
	public ClassicGame(File directory) {
		this.directory = directory;
		this.translator.register(new Language("English", Main.class.getResourceAsStream("/lang/en_US.lang")));
		this.translator.setDefault("English"); 
		
		File file = new File(this.getDirectory(), "config.yml");
		if (!file.exists()) {
			if(!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		OpenClassic.setGame(this);
		this.pkgManager = new PackageManager();
		this.config = new YamlConfig(file);
		this.config.load();
	}
	
	@Override
	public PackageManager getPackageManager() {
		return this.pkgManager;
	}

	@Override
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	@Override
	public PluginManager getPluginManager() {
		return this.pluginManager;
	}
	
	@Override
	public void registerCommand(Object owner, Command command) {
		Validate.notNull(owner, "Owner cannot be null.");
		Validate.notNull(command, "Command cannot be null.");
		if(!this.commands.containsKey(owner)) {
			this.commands.put(owner, new ArrayList<Command>());
		}
		
		this.commands.get(owner).add(command);
	}
	
	@Override
	public void registerExecutor(Object owner, CommandExecutor executor) {
		Validate.notNull(owner, "Owner cannot be null.");
		Validate.notNull(executor, "Executor cannot be null.");
		if(!this.executors.containsKey(owner)) {
			this.executors.put(owner, new ArrayList<CommandExecutor>());
		}
		
		this.executors.get(owner).add(executor);
	}
	
	@Override
	public void unregisterCommands(Object owner) {
		Validate.notNull(owner, "Owner cannot be null.");
		if(!this.commands.containsKey(owner)) {
			return;
		}
		
		for(Command command : new ArrayList<Command>(this.commands.get(owner))) {
			this.commands.remove(command);
		}
	}

	@Override
	public void unregisterExecutors(Object owner) {
		Validate.notNull(owner, "Owner cannot be null.");
		if(!this.executors.containsKey(owner)) {
			return;
		}
		
		for(CommandExecutor command : new ArrayList<CommandExecutor>(this.executors.get(owner))) {
			this.executors.remove(command);
		}
	}

	@Override
	public void processCommand(Sender sender, String command) {
		if(command.length() == 0) return;
		PreCommandEvent event = EventManager.callEvent(new PreCommandEvent(sender, command));
		if(event.isCancelled()) {
			return;
		}
		
		String split[] = event.getCommand().split(" ");
		for(CommandExecutor executor : this.getCommandExecutors()) {
			if(executor.getCommand(split[0]) != null) {
				try {
					Method method = executor.getCommand(split[0]);
					ch.spacebase.openclassic.api.command.annotation.Command annotation = method.getAnnotation(ch.spacebase.openclassic.api.command.annotation.Command.class);
					
					if(annotation.senders().length > 0) {
						boolean match = false;
						
						for(Class<? extends Sender> allowed : annotation.senders()) {
							if(allowed.isAssignableFrom(sender.getClass())) {
								match = true;
							}
						}
						
						if(!match) {
							if(annotation.senders().length == 1) {
								sender.sendMessage(Color.RED + String.format(this.translator.translate("command.wrong-sender.single", sender.getLanguage()), annotation.senders()[0].getSimpleName().toLowerCase()));
							} else {
								sender.sendMessage(Color.RED + String.format(this.translator.translate("command.wrong-sender.multi", sender.getLanguage()), Arrays.toString(annotation.senders()).toLowerCase()));
							}
							
							return;
						}
					}
					
					if(!sender.hasPermission(annotation.permission())) {
						sender.sendMessage(Color.RED + this.translator.translate("command.no-perm", sender.getLanguage()));
						return;
					}
					
					if(split.length - 1 < annotation.min() || split.length - 1 > annotation.max()) {
						sender.sendMessage(Color.RED + this.translator.translate("command.usage", sender.getLanguage()) + ": " + sender.getCommandPrefix() + split[0] + " " + annotation.usage());
						return;
					}
					
					method.invoke(executor, sender, split[0], Arrays.copyOfRange(split, 1, split.length));
				} catch (Exception e) {
					OpenClassic.getLogger().severe(String.format(this.translator.translate("command.fail-invoke"), split[0]));
					e.printStackTrace();
				}
				
				return;
			}
		}
		
		for(Command cmd : this.getCommands()) {
			if(Arrays.asList(cmd.getAliases()).contains(split[0])) {
				if(cmd.getSenders() != null && cmd.getSenders().length > 0) {
					boolean match = false;
					
					for(Class<? extends Sender> allowed : cmd.getSenders()) {
						if(sender.getClass() == allowed) {
							match = true;
						}
					}
					
					if(!match) {
						if(cmd.getSenders().length == 1) {
							sender.sendMessage(Color.RED + String.format(this.translator.translate("command.wrong-sender.single", sender.getLanguage()), cmd.getSenders()[0].getSimpleName().toLowerCase()));
						} else {
							sender.sendMessage(Color.RED + String.format(this.translator.translate("command.wrong-sender.multi", sender.getLanguage()), Arrays.toString(cmd.getSenders()).toLowerCase()));
						}
						return;
					}
				}
				
				if(!sender.hasPermission(cmd.getPermission())) {
					sender.sendMessage(Color.RED + this.translator.translate("command.no-perm", sender.getLanguage()));
					return;
				}
				
				if((split.length - 1) < cmd.getMinArgs() || (split.length - 1) > cmd.getMaxArgs()) {
					sender.sendMessage(Color.RED + this.translator.translate("command.usage", sender.getLanguage()) + ": " + sender.getCommandPrefix() + split[0] + " " + cmd.getUsage());
					return;
				}
				
				cmd.execute(sender, split[0], Arrays.copyOfRange(split, 1, split.length));
				return;
			}
			
			break;
		}
		
		CommandNotFoundEvent e = EventManager.callEvent(new CommandNotFoundEvent(sender, command));
		if(e.showMessage()) {
			sender.sendMessage(Color.RED + this.translator.translate("command.unknown", sender.getLanguage()));
		}
	}

	public List<Command> getCommands() {
		List<Command> result = new ArrayList<Command>();
		for(List<Command> commands : this.commands.values()) {
			result.addAll(commands);
		}
		
		return result;
	}

	@Override
	public List<CommandExecutor> getCommandExecutors() {
		List<CommandExecutor> result = new ArrayList<CommandExecutor>();
		for(List<CommandExecutor> executors : this.executors.values()) {
			result.addAll(executors);
		}
		
		return result;
	}

	@Override
	public Configuration getConfig() {
		return this.config;
	}

	public void registerGenerator(String name, Generator generator) {
		Validate.notNull(name, "Name cannot be null.");
		Validate.notNull(generator, "Generator cannot be null.");
		if(generator == null) return;
		this.generators.put(name, generator);
	}
	
	public Generator getGenerator(String name) {
		return this.generators.get(name);
	}
	
	public Map<String, Generator> getGenerators() {
		return new HashMap<String, Generator>(this.generators);
	}
	
	public boolean isGenerator(String name) {
		return this.getGenerator(name) != null;
	}

	@Override
	public File getDirectory() {
		return this.directory;
	}

	@Override
	public void reload() {
		this.config.save();
		this.config.load();
		
		for(Plugin plugin : this.pluginManager.getPlugins()) {
			plugin.reload();
		}
	}
	
	@Override
	public Translator getTranslator() {
		return this.translator;
	}
	
	@Override
	public String getLanguage() {
		return this.config.getString("options.language", "English");
	}

}

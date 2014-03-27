package de.craftlancer.clutil.buildings.commands;

import de.craftlancer.clutil.CLUtil;
import de.craftlancer.core.command.CommandHandler;

public class BuildingCommandHandler extends CommandHandler
{
    //TODO tab complete
    // TODO externalise
    private static String HELP_PERM = "clutil.building.help";
    private static String LIST_PERM = "clutil.building.help";
    private static String INFO_PERM = "clutil.building.help";
    private static String BUILD_PERM = "clutil.building.help";
    private static String UNDO_PERM = "clutil.building.help";
    private static String PROGRESS_PERM = "clutil.building.help";
    private static String PREVIEW_PERM = "clutil.building.help";
    private static String SET_PERM = "clutil.building.help";
    private static String CREATE_PERM = "clutil.building.help";
    
    public BuildingCommandHandler(CLUtil plugin)
    {
        registerSubCommand("help", new BuildingHelpCommand(HELP_PERM, plugin, getCommands()));
        registerSubCommand("list", new BuildingListCommand(LIST_PERM, plugin));
        registerSubCommand("info", new BuildingInfoCommand(INFO_PERM, plugin));
        //registerSubCommand("build", new BuildingBuildCommand(BUILD_PERM, plugin));
        registerSubCommand("undo", new BuildingUndoCommand(UNDO_PERM, plugin));
        registerSubCommand("progress", new BuildingProgressCommand(PROGRESS_PERM, plugin));
        registerSubCommand("preview", new BuildingPreviewCommand(PREVIEW_PERM, plugin));
        //registerSubCommand("set", new BuildingProgressCommand(SET_PERM, plugin));
        //registerSubCommand("create", new BuildingProgressCommand(CREATE_PERM, plugin));
    }
}
